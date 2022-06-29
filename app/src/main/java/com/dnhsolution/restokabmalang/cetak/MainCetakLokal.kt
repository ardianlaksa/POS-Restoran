package com.dnhsolution.restokabmalang.cetak

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dantsu.escposprinter.connection.DeviceConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.textparser.PrinterTextParserImg
import com.dnhsolution.restokabmalang.MainActivity
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.database.DatabaseHandler
import com.dnhsolution.restokabmalang.databinding.ActivityMainCetakBinding
import com.dnhsolution.restokabmalang.utilities.TampilanBarcode
import com.dnhsolution.restokabmalang.utilities.Url
import com.zj.btsdk.BluetoothService
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class MainCetakLokal : AppCompatActivity() {
    private var idPengguna: String? = null
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
    private var nomorTerakhirKarcis = ""

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
        idPengguna = sharedPreferences.getString(Url.SESSION_ID_PENGGUNA, "")
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

//        var ch11 = ""
//        var ch12 = ""
//        var ch = 'A'
//        var ch2 = 'A'
//
//        while (ch <= 'Z') {
//            while (ch2 <= 'Z') {
//                if (ch == 'A' && ch2 == 'B') {
//                    ++ch2
//                    ch11 = ch.toString()
//                    ch12 = ch2.toString()
//                    break
//                }
//                ++ch2
//            }
//            ++ch
//        }
//        println("asdf $ch11$ch12")
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
                        val splitNomorKarcis = nomorTerakhirKarcis.split("|")
                        for(a in splitNomorKarcis) {
                            val splitA = a.split(":")
                            databaseHandler?.updateNomorTerakhirKarcisProduk(
                                splitA[0],
                                splitA[1],
                                splitA[2]
                            )
                        }
                        Log.i(
                            "Async.OnPrintFinished",
                            "AsyncEscPosPrint.OnPrintFinished : Print is finished !"
                        )
                    }
                }
            ).execute(
                when (MainActivity.jenisPajak) {
                    "01" -> printTextHotelHiburan(selectedDevice)
                    "02" -> printText(selectedDevice)
                    else -> {
                        printTextHiburanKarcis(selectedDevice)
                    }
                })
        }
    }

    private val getData: Unit
        get() {
            itemProduk!!.clear()
            val db = databaseHandler!!.readableDatabase

            //data transaksi
            val cTrx = db.rawQuery(
                "select ${databaseHandler?.col_id}, ${databaseHandler?.col_omzet}," +
                        "${databaseHandler?.col_disc_rp}, ${databaseHandler?.col_pajak_rp}," +
                        "${databaseHandler?.col_service_charge_rp} " +
                        "from transaksi where id='$idTrx'",null
            )
            cTrx.moveToFirst()
            val idTrx = cTrx.getInt(0)
            val omzet = cTrx.getString(1).toInt()
            val discRp = cTrx.getString(2).toInt()
            val pajakRp = cTrx.getString(3).toInt()
            val serviceChargeRp = cTrx.getString(4).toInt()
            val subTotal = omzet + discRp - pajakRp
            tvSubtotal!!.text = currencyFormatter(subTotal.toString())
            tvJmlPajak!!.text = currencyFormatter(pajakRp.toString())
            tvJmlDisc!!.text = currencyFormatter(discRp.toString())
            tvTotal!!.text = currencyFormatter(omzet.toString())
            binding.tvJmlServiceCharge.text = currencyFormatter(serviceChargeRp.toString())
            cTrx.close()

            //detail transaksi
            val cDetailTrx = db.rawQuery(
                "select p.${databaseHandler?.col_id},dt.${databaseHandler?.col_nama_produk}," +
                        "dt.${databaseHandler?.col_qty}, dt.${databaseHandler?.col_harga}," +
                        "p.${databaseHandler?.col_ispajak},p.${databaseHandler?.col_kode_produk}," +
                        "p.${databaseHandler?.col_seri_produk}," +
                        "${databaseHandler?.col_range_transaksi_karcis_awal}," +
                        "${databaseHandler?.col_range_transaksi_karcis_akhir} " +
                        "from detail_transaksi dt LEFT JOIN produk p ON dt.id_produk = p.id " +
                        "where id_trx ='" + idTrx + "'", null
            )
            if (cDetailTrx.moveToFirst()) {
                var no = 1
                do {
                    // Passing values
                    val totalharga =
                        cDetailTrx.getString(2).toInt() * cDetailTrx.getString(3).toInt()
                    val id = ItemProduk()
                    id.setNo(no)
                    id.id_produk = cDetailTrx.getString(0)
                    id.setNama_produk(cDetailTrx.getString(1))
                    id.setQty(cDetailTrx.getString(2))
                    id.setHarga(currencyFormatter(cDetailTrx.getString(3)))
                    id.isPajak = cDetailTrx.getString(4)
                    id.setTotal_harga(currencyFormatter(totalharga.toString()))
                    val nomorKarcis = "$dateTahun-${cDetailTrx.getString(5)}-${MainActivity.idTempatUsaha}-${cDetailTrx.getString(6)}-${cDetailTrx.getString(7)}"
                    id.nomorKarcis = nomorKarcis
                    id.rangeTransaksiKarcisAkhir = cDetailTrx.getString(8)
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

        val serviceChargeRp = binding.tvJmlServiceCharge.text
        if(serviceChargeRp != "")
            text += "[L]Service Charge[R]$serviceChargeRp\n"

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
                "[L]Pajak[R]${tvJmlPajak?.text}\n"

        val serviceChargeRp = binding.tvJmlServiceCharge.text
        if(serviceChargeRp != "")
            text += "[L]Service Charge[R]$serviceChargeRp\n"

        text += "[C]--------------------------------\n"+
                "[C]<font size='tall'>Grand Total : ${gantiKetitik(tvTotal?.text.toString())}</font>\n"+
                "[L]\n"+
                "[C]- Terima Kasih -"

        return printer.addTextToPrint(text)
    }

    private fun printTextHiburanKarcis(printerConnection: DeviceConnection?) : AsyncEscPosPrinter  {
        val printer = AsyncEscPosPrinter(printerConnection, 203, 48f, 32)
        val nmTmpUsaha = MainActivity.namaTempatUsaha
        val alamat = MainActivity.alamatTempatUsaha
        val namaPetugas = MainActivity.namaPetugas
        val tanggal = dateTime
        var text = ""
        var iQty = 0
        for (h in itemProduk!!.indices) {
            val nomorKarcis = itemProduk!![h].nomorKarcis
            val pisahNomorKarcis = nomorKarcis.split("-")
            val idProduk = itemProduk!![h].getId_produk()
            val qtyKarcis = itemProduk!![h].getQty()
            val hargaKarcis = itemProduk!![h].getHarga()
//            val rangeKarcisAkhir = itemProduk!![h].rangeTransaksiKarcisAkhir.toInt()

            val rangeKarcisAkhir1 = itemProduk!![h].rangeTransaksiKarcisAkhir
            var rangeKarcisAkhir = 0
            if(rangeKarcisAkhir1.contains(".")) {
                val splRangeKarcisAkhir = rangeKarcisAkhir1.split(".")
                rangeKarcisAkhir = splRangeKarcisAkhir[0].toInt()
            }
            iQty = qtyKarcis.toInt()
            if(nomorTerakhirKarcis != "") nomorTerakhirKarcis += "|"
            for (i in 0 until iQty) {

                var pnk = pisahNomorKarcis[4].toInt() + i // asli
//                var pnk = 5000 + i
                var nomorSeriBaru = pisahNomorKarcis[3]
                val seriToCharArray = nomorSeriBaru.toCharArray()
                if(rangeKarcisAkhir < pnk) {
                    var ch11 = ""
                    var ch12 = ""
                    var ch = 'A'
                    var ch2 = 'A'

                    while (ch <= 'Z') {
                        while (ch2 <= 'Z') {
                            if (ch == seriToCharArray[0] && ch2 == seriToCharArray[1]) {
                                ++ch2
                                ch11 = ch.toString()
                                ch12 = ch2.toString()
                                nomorSeriBaru = "$ch11$ch12"
                                pnk = 1
                                break
                            }
                            ++ch2
                        }
                        ++ch
                    }
                }
                val nomorUrutKarcisLengkap = "${pisahNomorKarcis[0]}-${pisahNomorKarcis[1]}-${pisahNomorKarcis[2]}-$nomorSeriBaru-$pnk"
                text += "[L]\n" +
                        "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(
                    printer,
                    applicationContext.resources.getDrawableForDensity(
                        R.drawable.ic_malang_makmur_grayscale,
                        DisplayMetrics.DENSITY_LOW, theme
                    )
                ) + "</img>\n" +
                        "[L]\n" +
                        "[C]<b>$nmTmpUsaha</b>\n" +
                        "[C]$alamat\n" +
                        "[L]\n" +
                        "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(
                            printer,
                            TampilanBarcode().displayBitmap(this, nomorUrutKarcisLengkap)
                        ) + "</img>\n" +
                        "[L]\n" +
                        "[C]$nomorUrutKarcisLengkap\n" +
                        "[L]\n" +
                        "[L]Tanggal : $tanggal\n" +
                        "[L]Kasir   : $namaPetugas\n" +
                        "[C]--------------------------------\n" +
                        "[C]Nominal : $hargaKarcis\n" +
                        "[L]\n" +
                        "[C]Terima Kasih\n" +
                        "[C]Atas Kunjungan\n" +
                        "[C]Anda\n"
                //                --iQty
                if((iQty-1)==i)
                    nomorTerakhirKarcis += "$idProduk:${pnk+1}:$nomorSeriBaru"
            }
        }
        text += "[L]\n"+
                "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer,
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
                "[L]Pajak[R]${tvJmlPajak?.text}\n"

        val serviceChargeRp = binding.tvJmlServiceCharge.text
        if(serviceChargeRp != "")
            text += "[L]Service Charge[R]$serviceChargeRp\n"

        text += "[C]--------------------------------\n"+
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

    private val dateTahun: String
        get() {
            val dateFormat: DateFormat =
                SimpleDateFormat("yyyy", Locale.getDefault())
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