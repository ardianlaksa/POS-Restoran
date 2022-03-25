package com.dnhsolution.restokabmalang.cetak

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
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
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.DeviceConnection
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

class MainCetakLokal : AppCompatActivity() {
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
    private var selectedDevice: BluetoothConnection? = null
    private val PERMISSION_BLUETOOTH = 100
    private val PERMISSION_BLUETOOTH_ADMIN = 101
    private val PERMISSION_BLUETOOTH_CONNECT = 102
    private val PERMISSION_BLUETOOTH_SCAN = 103

    private var resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            when (requestCode) {
                RC_ENABLE_BLUETOOTH -> if (result.resultCode == RESULT_OK) {
                    Log.i(_tag, "onActivityResult: bluetooth aktif")
                } else Log.i(_tag, "onActivityResult: bluetooth harus aktif untuk menggunakan fitur ini")
                RC_CONNECT_DEVICE -> if (result.resultCode == RESULT_OK) {
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
        val btDevice = sharedPreferences.getString(Url.SESSION_PRINTER_BT, "")
        nmPetugas = MainActivity.namaPetugas ?: ""
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

        btnKembali.setOnClickListener { v: View? ->
            setResult(RESULT_OK)
            finish()
        }

        binding.bBatalTrx.setOnClickListener { v: View? ->
            Log.d(_tag, "")
            databaseHandler!!.delete_by_id_trx(idTrx!!.toInt())
            databaseHandler!!.delete_by_id_detail_trx(idTrx!!.toInt())
            finish()
        }
        btnPilih.setOnClickListener { v: View? ->
            browseBluetoothDevice()
        }

        if (intent.getStringExtra("idTrx")!!.isNotEmpty()) {
            idTrx = intent.getStringExtra("idTrx")
            getData
        }

        btnCetak?.setOnClickListener {
            printBluetooth()
        }
    }

    private fun browseBluetoothDevice() {
        val bluetoothDevicesList = BluetoothPrintersConnections().list
        if (bluetoothDevicesList != null) {
            val items = arrayOfNulls<String>(bluetoothDevicesList.size + 1)
            items[0] = "Default printer"
            for ((i, device) in bluetoothDevicesList.withIndex()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                items[i + 1] = device.device.name
            }
            val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this)
            alertDialog.setTitle("Bluetooth printer selection")
            alertDialog.setItems(items) { dialogInterface, i ->
                val index = i - 1
                selectedDevice = if (index == -1) {
                    null
                } else {
                    bluetoothDevicesList[index]
                }
//                val button = findViewById<View>(R.id.button_bluetooth_browse) as Button
                tv_status?.text = items[i]
            }
            val alert: AlertDialog = alertDialog.create()
            alert.setCanceledOnTouchOutside(false)
            alert.show()
        }
    }

    private fun printBluetooth() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH),
                PERMISSION_BLUETOOTH
            )
        } else if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_ADMIN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_ADMIN),
                PERMISSION_BLUETOOTH_ADMIN
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                PERMISSION_BLUETOOTH_CONNECT
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_SCAN),
                PERMISSION_BLUETOOTH_SCAN
            )
        } else {
            AsyncBluetoothEscPosPrint(
                this,
                object : AsyncEscPosPrint.OnPrintFinished() {
                    override fun onError(
                        asyncEscPosPrinter: AsyncEscPosPrinter?,
                        codeException: Int
                    ) {
                        Log.e(
                            "Async.OnPrintFinished",
                            "AsyncEscPosPrint.OnPrintFinished : An error occurred !"
                        )
                    }

                    override fun onSuccess(asyncEscPosPrinter: AsyncEscPosPrinter?) {
                        Log.i(
                            "Async.OnPrintFinished",
                            "AsyncEscPosPrint.OnPrintFinished : Print is finished !"
                        )
                    }
                }
            ).execute(
                if(MainActivity.jenisPajak == "02")
                    printText(selectedDevice)
                else
                    printTextHotelHiburan(selectedDevice))
        }
    }

    private val getData: Unit
        get() {
            itemProduk!!.clear()
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
                    val totalharga =
                        cDetailTrx.getString(1).toInt() * cDetailTrx.getString(2).toInt()
                    val id = ItemProduk()
                    id.setNo(no)
                    id.setNama_produk(cDetailTrx.getString(0))
                    id.setQty(cDetailTrx.getString(1))
                    id.setHarga(currencyFormatter(cDetailTrx.getString(2)))
                    id.isPajak = cDetailTrx.getString(3)
                    id.setTotal_harga(currencyFormatter(totalharga.toString()))
                    itemProduk!!.add(id)
                    no++
                    // Do something Here with values
                } while (cDetailTrx.moveToNext())
            }
            cDetailTrx.close()
            db.close()
            adapter?.notifyDataSetChanged()
        }

    private fun printText(printerConnection: DeviceConnection?) : AsyncEscPosPrinter  {
        val printer = AsyncEscPosPrinter(printerConnection, 203, 48f, 32)
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
            text += "[C]--------------------------------\n"+
                    "[L]Subtotal[R]${gantiKetitik(tvSubtotal?.text.toString())}\n"+
                    "[L]Disc[R]${tvJmlDisc?.text}\n"

            if (tipeStruk.equals("1", ignoreCase = true)) {
                text += "[L]Pajak Resto[R]${tvJmlPajak?.text}\n"
            }
            text += "[C]--------------------------------\n"+
                    "[C]<font size='tall'>TOTAL : ${gantiKetitik(tvTotal?.text.toString())}</font>\n"+
                    "[L]\n"+
                    "[C]- Terima Kasih -"
            return printer.addTextToPrint(text)
    }

    private fun printTextHotelHiburan(printerConnection: DeviceConnection?) : AsyncEscPosPrinter  {
        val printer = AsyncEscPosPrinter(printerConnection, 203, 48f, 32)
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

            text += "[C]--------------------------------\n"+
                    "[L]Disc[R]${tvJmlDisc?.text}\n" +
                    "[L]Total[R]${gantiKetitik(tvSubtotal?.text.toString())}\n"+
                    "[L]Pajak[R]${tvJmlPajak?.text}\n" +

                    "[C]--------------------------------\n"+
                    "[C]<font size='tall'>Grand Total : ${gantiKetitik(tvTotal?.text.toString())}</font>\n"+
                    "[L]\n"+
                    "[C]- Terima Kasih -"
            return printer.addTextToPrint(text)
    }

    private fun gantiKetitik(value: String): String {
        return value.replace(",", ".")
    }

    private val dateTime: String
        get() {
            val dateFormat: DateFormat =
                SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            val date = Date()
            return dateFormat.format(date)
        }

    private fun currencyFormatter(num: String): String {
        val m = num.toDouble()
        val formatter = DecimalFormat("###,###,###")
        return formatter.format(m)
    }

    companion object {
        const val RC_BLUETOOTH = 0
        const val RC_CONNECT_DEVICE = 1
        const val RC_ENABLE_BLUETOOTH = 2
    }
}