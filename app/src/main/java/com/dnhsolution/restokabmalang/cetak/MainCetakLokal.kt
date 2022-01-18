package com.dnhsolution.restokabmalang.cetak

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import com.dnhsolution.restokabmalang.cetak.BluetoothHandler.HandlerInterface
import androidx.recyclerview.widget.RecyclerView
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.DividerItemDecoration
import android.widget.TextView
import com.zj.btsdk.BluetoothService
import android.content.SharedPreferences
import com.dnhsolution.restokabmalang.database.DatabaseHandler
import android.os.Bundle
import com.dnhsolution.restokabmalang.R
import android.app.Activity
import android.content.Intent
import com.dnhsolution.restokabmalang.cetak.DeviceActivity
import com.dnhsolution.restokabmalang.cetak.MainCetakLokal
import butterknife.ButterKnife
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Button
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import com.dnhsolution.restokabmalang.cetak.BluetoothHandler
import butterknife.OnClick
import com.dnhsolution.restokabmalang.cetak.PrinterCommands
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.textparser.PrinterTextParserImg
import com.dnhsolution.restokabmalang.MainActivity
import com.dnhsolution.restokabmalang.databinding.ActivityMainCetakBinding
import com.dnhsolution.restokabmalang.utilities.Url
import java.lang.Exception
import java.lang.StringBuilder
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class MainCetakLokal : AppCompatActivity(), PermissionCallbacks, HandlerInterface {
    private lateinit var rvData: RecyclerView
    var itemProduk: MutableList<ItemProduk>? = null
    var linearLayout: LinearLayout? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private var dividerItemDecoration: DividerItemDecoration? = null
    private var adapter: RecyclerView.Adapter<*>? = null
    var tvSubtotal: TextView? = null
    var tvDisc: TextView? = null
    var tvJmlDisc: TextView? = null
    var tvTotal: TextView? = null
    var tv_status: TextView? = null
    var tvJmlPajak: TextView? = null
    private lateinit var btnKembali: Button
    var btnCetak: Button? = null
    private lateinit var btnPilih: Button
    private val _tag = javaClass.simpleName
    private var mService: BluetoothService? = null
    private var isPrinterReady = false
    private lateinit var sharedPreferences: SharedPreferences
    var idTrx: String? = "0"
    var databaseHandler: DatabaseHandler? = null
    private var tipeStruk: String? = null
    private lateinit var binding: ActivityMainCetakBinding
    private var requestCode = 0
    private var nmPetugas = ""

    var resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            when (requestCode) {
                MainCetak.RC_ENABLE_BLUETOOTH -> if (result.resultCode == RESULT_OK) {
                    Log.i(_tag, "onActivityResult: bluetooth aktif")
                } else Log.i(_tag, "onActivityResult: bluetooth harus aktif untuk menggunakan fitur ini")
                MainCetak.RC_CONNECT_DEVICE -> if (result.resultCode == RESULT_OK) {
                    val address = data!!.extras!!.getString(DeviceActivity.EXTRA_DEVICE_ADDRESS)
                    val mDevice = mService?.getDevByMac(address)
                    mService?.connect(mDevice)
                }
            }
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences(Url.SESSION_NAME, MODE_PRIVATE)
        val label = sharedPreferences.getString(Url.setLabel, "Belum disetting")
        val tema = sharedPreferences.getString(Url.setTema, "0")
        tipeStruk = sharedPreferences.getString(Url.SESSION_TIPE_STRUK, "")
        nmPetugas = MainActivity.namaUser ?: ""
        databaseHandler = DatabaseHandler(this@MainCetakLokal)
        when {
            tema.equals("0", ignoreCase = true) -> {
                this@MainCetakLokal.setTheme(R.style.Theme_First)
            }
            tema.equals("1", ignoreCase = true) -> {
                this@MainCetakLokal.setTheme(R.style.Theme_Second)
            }
            tema.equals("2", ignoreCase = true) -> {
                this@MainCetakLokal.setTheme(R.style.Theme_Third)
            }
            tema.equals("3", ignoreCase = true) -> {
                this@MainCetakLokal.setTheme(R.style.Theme_Fourth)
            }
            tema.equals("4", ignoreCase = true) -> {
                this@MainCetakLokal.setTheme(R.style.Theme_Fifth)
            }
            tema.equals("5", ignoreCase = true) -> {
                this@MainCetakLokal.setTheme(R.style.Theme_Sixth)
            }
        }

        binding = ActivityMainCetakBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.title = label
        rvData = binding.recyclerView
        linearLayout = binding.linearLayout
        val llPajak = binding.llPajak
        tvSubtotal = binding.tvSubtotal
        tvDisc = binding.tvDisc
        tvJmlDisc = binding.tvJmlDisc
        tvJmlPajak = binding.tvJmlPajak
        tvTotal = binding.tvTotal
        tv_status = binding.tvStatus
        btnCetak = binding.btnCetak
        btnKembali = binding.btnKembali
        btnPilih = binding.btnPilihBT

        itemProduk = ArrayList()
        adapter = AdapterProduk(itemProduk, this@MainCetakLokal)
        linearLayoutManager = LinearLayoutManager(this@MainCetakLokal)
        linearLayoutManager!!.orientation = RecyclerView.VERTICAL
        dividerItemDecoration =
            DividerItemDecoration(rvData.getContext(), linearLayoutManager!!.orientation)
        rvData.setHasFixedSize(true)
        rvData.setLayoutManager(linearLayoutManager)
        //recyclerView.addItemDecoration(dividerItemDecoration);
        rvData.setAdapter(adapter)
        if (tipeStruk.equals("2", ignoreCase = true)) llPajak.visibility = View.GONE
        btnKembali.setOnClickListener(View.OnClickListener { v: View? ->
            setResult(RESULT_OK)
            finish()
        })
        binding.bBatalTrx.setOnClickListener { v: View? ->
            Log.d(_tag, "")
            databaseHandler!!.delete_by_id_trx(idTrx!!.toInt())
            databaseHandler!!.delete_by_id_detail_trx(idTrx!!.toInt())
            finish()
        }
        btnPilih.setOnClickListener { v: View? ->
            mService.let {
                if(it != null) {
                    if (!it.isAvailable) {
                        Log.i(_tag, "printText: perangkat tidal support bluetooth")
                        return@setOnClickListener
                    }
                    if (it.isBTopen) {
                        resultLauncher.launch(Intent(this@MainCetakLokal, DeviceActivity::class.java))
                        requestCode = MainCetak.RC_CONNECT_DEVICE
                    } else requestBluetooth()
                }
            }
        }
        if (!intent.getStringExtra("idTrx")!!.isEmpty()) {
            idTrx = intent.getStringExtra("idTrx")
            data
        }

        setupBluetooth()
        val sharedPreferences = getSharedPreferences(Url.SESSION_NAME, MODE_PRIVATE)
        val bt_device = sharedPreferences.getString(Url.SESSION_PRINTER_BT, "")
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val mBluetoothAdapter = bluetoothManager.adapter
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        } else if (!mBluetoothAdapter.isEnabled) {
            // Bluetooth is not enabled :)
        } else {
            if (!bt_device.equals("", ignoreCase = true)) {
                val mDevice = mService!!.getDevByMac(bt_device)
                mService!!.connect(mDevice)
                Log.d("BT_DEVICE", bt_device!!)
            }
        }

        btnCetak?.setOnClickListener {
            printText()
        }
    }

    override fun onResume() {
        super.onResume()
        val label = sharedPreferences.getString(Url.setLabel, "Belum disetting")
        supportActionBar!!.title = label
    }// Passing values

    // Do something Here with values
    //data transaksi
    private val data: Unit
        //detail transaksi
        get() {
            itemProduk!!.clear()
            adapter!!.notifyDataSetChanged()
            val db = databaseHandler!!.readableDatabase

            //data transaksi
            val cTrx = db.rawQuery(
                "select id, omzet, disc_rp, pajak_rp from transaksi where id='$idTrx'",
                null
            )
            cTrx.moveToFirst()
            val id_trx = cTrx.getInt(0)
            val omzet = cTrx.getString(1).toInt()
            val disc_rp = cTrx.getString(2).toInt()
            val pajak_rp = cTrx.getString(3).toInt()
            val sub_total = omzet + disc_rp - pajak_rp
            tvSubtotal!!.text = currencyFormatter(sub_total.toString())
            tvJmlPajak!!.text = currencyFormatter(pajak_rp.toString())
            tvJmlDisc!!.text = currencyFormatter(disc_rp.toString())
            tvTotal!!.text = currencyFormatter(omzet.toString())
            cTrx.close()

            //detail transaksi
            val cDetailTrx = db.rawQuery(
                "select dt.nama_produk, dt.qty, dt.harga,p.ispajak " +
                        "from detail_transaksi dt LEFT JOIN produk p ON dt.id_produk = p.id " +
                        "where id_trx ='" + idTrx + "'", null
            )
            if (cDetailTrx.moveToFirst()) {
                var no = 1
                do {
                    // Passing values
                    val total_harga =
                        cDetailTrx.getString(1).toInt() * cDetailTrx.getString(2).toInt()
                    val id = ItemProduk()
                    id.setNo(no)
                    id.setNama_produk(cDetailTrx.getString(0))
                    id.setQty(cDetailTrx.getString(1))
                    id.setHarga(currencyFormatter(cDetailTrx.getString(2)))
                    id.isPajak = cDetailTrx.getString(3)
                    id.setTotal_harga(currencyFormatter(total_harga.toString()))
                    itemProduk!!.add(id)
                    no++
                    // Do something Here with values
                } while (cDetailTrx.moveToNext())
            }
            cDetailTrx.close()
            db.close()
            adapter!!.notifyDataSetChanged()
        }

    @AfterPermissionGranted(RC_BLUETOOTH)
    private fun setupBluetooth() {
        val params = arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)
        if (!EasyPermissions.hasPermissions(this, *params)) {
            EasyPermissions.requestPermissions(
                this, "You need bluetooth permission",
                RC_BLUETOOTH, *params
            )
            return
        }
        mService = BluetoothService(this, BluetoothHandler(this))
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        // TODO: 10/11/17 do something
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        // TODO: 10/11/17 do something
    }

    override fun onDeviceConnected() {
        isPrinterReady = true
        tv_status!!.text = resources.getString(R.string.terhubung_dengan_perangkat)
    }

    override fun onDeviceConnecting() {
        tv_status!!.text = resources.getString(R.string.sedang_menghubungkan)
    }

    override fun onDeviceConnectionLost() {
        isPrinterReady = false
        tv_status!!.text = resources.getString(R.string.koneksi_terputus)
    }

    override fun onDeviceUnableToConnect() {
        tv_status!!.text = resources.getString(R.string.tidak_terhubung_ke_perangkat)
    }

    private fun printText() {
        mService.let {
            if (it != null) {
                if (!it.isAvailable) {
                    Log.i(_tag, "printText: perangkat tidak support bluetooth")
                    return
                }

                val connection: BluetoothConnection? =
                    BluetoothPrintersConnections.selectFirstPaired()
                val printer = EscPosPrinter(connection, 203, 48f, 32)

                if (isPrinterReady) {
                    val sharedPreferences = getSharedPreferences(Url.SESSION_NAME, MODE_PRIVATE)
                    val nmTmpUsaha = sharedPreferences.getString(Url.SESSION_NAMA_TEMPAT_USAHA, "0")
                    val alamat = sharedPreferences.getString(Url.SESSION_ALAMAT, "0")
                    val tanggal = dateTime
                    var text = "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer,
                        applicationContext.resources.getDrawableForDensity(R.drawable.ic_malang_makmur_grayscale,
                            DisplayMetrics.DENSITY_LOW, theme
                        )) + "</img>\n" +
                            "[L]\n"+
                            "[C]<b>$nmTmpUsaha</b>\n"+
                            "[C]$alamat\n"+
                            "[L]\n"+
                            "[L]\n"+
                            "[L]No. Trx : $idTrx\n"+
                            "[L]Tanggal : $tanggal\n"+
                            "[L]Kasir   : $nmPetugas\n"+
                            "[C]--------------------------------\n"

                    for (i in itemProduk!!.indices) {
                        val nmProduk = itemProduk!![i].getNama_produk()
                        val qty = itemProduk!![i].getQty()
                        val harga = itemProduk!![i].getHarga()
                        val totalHarga = itemProduk!![i].getTotal_harga()
                        text += "[L]$nmProduk\n" +
                                "[L] $harga x $qty[R]${gantiKetitik(totalHarga)}\n"
                    }
                    val subTotal = tvSubtotal?.text.toString().replace(".", "")

                    text += "[C]--------------------------------\n"+
                            "[L]Subtotal[R]$subTotal\n"+
                            "[L]Disc[R]${tvJmlDisc?.text}\n"+
                            "[L]Pajak Resto[R]${tvJmlPajak?.text}\n"+
                            "[C]--------------------------------\n"+
                            "[C]<font size='tall'>TOTAL : ${gantiKetitik(tvTotal?.text.toString())}</font>\n"+
                            "[L]\n"+
                            "[C]- Terima Kasih -"
                    printer.printFormattedText(text)
                } else {
                    Toast.makeText(this, "Tidak terhubung printer manapun !", Toast.LENGTH_SHORT)
                        .show()
                    if (!it.isBTopen) requestBluetooth()
                }
            }
        }
    }

    private fun gantiKetitik(value: String): String {
        var value = value
        value = value.replace(",", ".")
        return value
    }

    private fun requestBluetooth() {
        mService.let {
            if (it != null) {
                if (!it.isBTopen) {
                    resultLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                    requestCode = MainCetak.RC_ENABLE_BLUETOOTH
                }
            }
        }
    }

    private val dateTime: String
        get() {
            val dateFormat: DateFormat =
                SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            val date = Date()
            return dateFormat.format(date)
        }

    private fun writePrint(align: ByteArray?, msg: String) {
        var msg = msg
        mService!!.write(align)
        val space = StringBuilder("   ")
        val l = msg.length
        if (l < 31) {
            for (x in 31 - l downTo 0) {
                space.append(" ")
            }
        }
        msg = msg.replace(" : ", space.toString())
        mService!!.write(msg.toByteArray())
    }

    fun currencyFormatter(num: String): String {
        val m = num.toDouble()
        val formatter = DecimalFormat("###,###,###")
        return formatter.format(m)
    }

    protected fun printConfig(bill: String, size: Int, style: Int, align: Int) {
        //size 1 = large, size 2 = medium, size 3 = small
        //style 1 = Regular, style 2 = Bold
        //align 0 = left, align 1 = center, align 2 = right
        try {
            val format = byteArrayOf(27, 33, 0)
            val change = byteArrayOf(27, 33, 0)
            mService!!.write(format)

            //different sizes, same style Regular
            if (size == 1 && style == 1) //large
            {
                change[2] = 0x10.toByte() //large
                mService!!.write(change)
            } else if (size == 2 && style == 1) //medium
            {
                //nothing to change, uses the default settings
            } else if (size == 3 && style == 1) //small
            {
                change[2] = 0x3.toByte() //small
                mService!!.write(change)
            }

            //different sizes, same style Bold
            if (size == 1 && style == 2) //large
            {
                change[2] = (0x10 or 0x8).toByte() //large
                mService!!.write(change)
            } else if (size == 2 && style == 2) //medium
            {
                change[2] = 0x8.toByte()
                mService!!.write(change)
            } else if (size == 3 && style == 2) //small
            {
                change[2] = (0x3 or 0x8).toByte() //small
                mService!!.write(change)
            }
            when (align) {
                0 ->                     //left align
                    mService!!.write(PrinterCommands.ESC_ALIGN_LEFT)
                1 ->                     //center align
                    mService!!.write(PrinterCommands.ESC_ALIGN_CENTER)
                2 ->                     //right align
                    mService!!.write(PrinterCommands.ESC_ALIGN_RIGHT)
            }
            mService!!.write(bill.toByteArray())
            mService!!.write(byteArrayOf(PrinterCommands.LF))
        } catch (ex: Exception) {
            Log.e("error", ex.toString())
        }
    }

    protected fun printNewLine() {
        mService!!.write(PrinterCommands.FEED_LINE)
    }

    companion object {
        const val RC_BLUETOOTH = 0
        const val RC_CONNECT_DEVICE = 1
        const val RC_ENABLE_BLUETOOTH = 2
    }
}