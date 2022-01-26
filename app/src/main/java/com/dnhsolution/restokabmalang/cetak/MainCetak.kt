package com.dnhsolution.restokabmalang.cetak

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.textparser.PrinterTextParserImg
import com.dnhsolution.restokabmalang.MainActivity
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.cetak.BluetoothHandler.HandlerInterface
import com.dnhsolution.restokabmalang.databinding.ActivityMainCetakBinding
import com.dnhsolution.restokabmalang.utilities.DefaultPojo
import com.dnhsolution.restokabmalang.utilities.Url
import com.google.gson.GsonBuilder
import org.json.JSONException
import org.json.JSONObject
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

open class MainCetak : AppCompatActivity(), PermissionCallbacks, HandlerInterface {
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private val permissionBluetooth: Int = 100
    private lateinit var sharedPreferences: SharedPreferences
    var rvData: RecyclerView? = null
    var itemProduk: MutableList<ItemProduk>? = null
    var linearLayout: LinearLayout? = null
    private val _tag = javaClass.simpleName
    private var linearLayoutManager: LinearLayoutManager? = null
    private var dividerItemDecoration: DividerItemDecoration? = null
    private var adapter: RecyclerView.Adapter<*>? = null
    var tvSubtotal: TextView? = null
    var tvDisc: TextView? = null
    var tvJmlDisc: TextView? = null
    var tvTotal: TextView? = null
    var tv_status: TextView? = null
    private var tvJmlPajak: TextView? = null
    var btnKembali: Button? = null
    var btnCetak: Button? = null
    var btnPilih: Button? = null
    private val TAG = MainActivity::class.java.simpleName
//    private var mService: BluetoothService? = null
    private var isPrinterReady = false
    private var tipeStruk: String? = null
    private var idTrx: String? = "0"
    private var nmPetugas = ""
    private var requestCode = 0
    private lateinit var binding: ActivityMainCetakBinding

    interface ValidasiTransaksiServices {
        @FormUrlEncoded
        @POST("pdrd/Android/AndroidJsonPOS/setUpdateValidasiTransaksi")
        fun getPosts(@Field("idPengguna") idPengguna: String
                     , @Field("UUID") uuid: String
                     , @Field("idTrx") idTrx: String): Call<DefaultPojo>
    }

    object ValidasiTransaksiResultFeedback {
        fun create(): ValidasiTransaksiServices {
            val gson = GsonBuilder()
                .setLenient()
                .create()
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(Url.serverBase)
                .build()
            return retrofit.create(ValidasiTransaksiServices::class.java)
        }
    }

    var resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            when (requestCode) {
                RC_ENABLE_BLUETOOTH -> if (result.resultCode == RESULT_OK) {
                    Log.i(TAG, "onActivityResult: bluetooth aktif")
                } else Log.i(TAG, "onActivityResult: bluetooth harus aktif untuk menggunakan fitur ini")

                RC_CONNECT_DEVICE -> if (result.resultCode == RESULT_OK) {
//                    val address = data!!.extras!!.getString(DeviceActivity.EXTRA_DEVICE_ADDRESS)
//                    val mDevice = mService?.getDevByMac(address)
//                    mService?.connect(mDevice)
                    isPrinterReady = true
                    tv_status?.text = resources.getString(R.string.terhubung_dengan_perangkat)
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
        val uuid = sharedPreferences.getString(Url.SESSION_UUID, "")
        val idPengguna = sharedPreferences.getString(Url.SESSION_ID_PENGGUNA, "")
        val btDevice = sharedPreferences.getString(Url.SESSION_PRINTER_BT, "")
        nmPetugas = MainActivity.namaUser ?: ""

        val intent = intent
        idTrx = intent.getStringExtra("getIdItem")
        Log.i(_tag, idTrx!!)
        when {
            tema.equals("0", ignoreCase = true) -> {
                this@MainCetak.setTheme(R.style.Theme_First)
            }
            tema.equals("1", ignoreCase = true) -> {
                this@MainCetak.setTheme(R.style.Theme_Second)
            }
            tema.equals("2", ignoreCase = true) -> {
                this@MainCetak.setTheme(R.style.Theme_Third)
            }
            tema.equals("3", ignoreCase = true) -> {
                this@MainCetak.setTheme(R.style.Theme_Fourth)
            }
            tema.equals("4", ignoreCase = true) -> {
                this@MainCetak.setTheme(R.style.Theme_Fifth)
            }
            tema.equals("5", ignoreCase = true) -> {
                this@MainCetak.setTheme(R.style.Theme_Sixth)
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
        adapter = AdapterProduk(itemProduk, this@MainCetak)
        linearLayoutManager = LinearLayoutManager(this@MainCetak)
        linearLayoutManager!!.orientation = RecyclerView.VERTICAL
        dividerItemDecoration =
            DividerItemDecoration(rvData!!.context, linearLayoutManager!!.orientation)
        rvData!!.setHasFixedSize(true)
        rvData!!.layoutManager = linearLayoutManager
        //recyclerView.addItemDecoration(dividerItemDecoration);
        rvData!!.adapter = adapter
        if (tipeStruk.equals("2", ignoreCase = true)) llPajak.visibility = View.GONE

        // Check to see if the Bluetooth classic feature is available.
        packageManager.takeIf { it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH) }?.also {
            Toast.makeText(this, R.string.bluetooth_tidak_didukung, Toast.LENGTH_SHORT).show()
            finish()
        }
        // Check to see if the BLE feature is available.
        packageManager.takeIf { it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }?.also {
            Toast.makeText(this, R.string.ble_tidak_didukung, Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.bBatalTrx.setOnClickListener {
            Log.d(_tag,"$idPengguna, $uuid, $idTrx")
            validasiTransaksiFungsi(idPengguna.toString(),uuid.toString(),idTrx.toString())
        }

        btnKembali!!.setOnClickListener { v: View? ->
            setResult(RESULT_OK)
            finish()
        }

        btnPilih?.setOnClickListener { v: View? ->
            resultLauncher.launch(Intent(this@MainCetak, DeviceActivity::class.java))
            requestCode = RC_CONNECT_DEVICE
        }

        data

//        setupBluetooth()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH), permissionBluetooth)
        } else {

        }

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter
        cekKeberadaanBluetooth(btDevice)
        btnCetak?.setOnClickListener {
            if(cekKeberadaanBluetooth(btDevice))
                printText()
        }
    }

    private fun cekKeberadaanBluetooth(btDevice: String?) : Boolean {
        mBluetoothAdapter?.let {
            if (mBluetoothAdapter == null) {
                // Device does not support Bluetooth
                isPrinterReady = false
                tv_status?.text = resources.getString(R.string.bluetooth_perangkat_tidak_cocok)
            } else if (!it.isEnabled) {
                // Bluetooth is not enabled :)
                isPrinterReady = false
                tv_status?.text = resources.getString(R.string.bluetooth_tidak_aktif)
            } else {
                if (!btDevice.equals("", ignoreCase = true)) {
                    isPrinterReady = true
                    tv_status!!.text = resources.getString(R.string.terhubung_dengan_perangkat)
                    return true
                }
            }
        }
        return false
    }

//    private fun requestBluetooth() {
//        mService.let {
//            if (it != null) {
//                if (!it.isBTopen) {
//                    resultLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
//                    requestCode = RC_ENABLE_BLUETOOTH
//                }
//            }
//        }
//    }

    private fun PackageManager.missingSystemFeature(name: String): Boolean = !hasSystemFeature(name)

    override fun onResume() {
        super.onResume()
        val label = sharedPreferences.getString(Url.setLabel, "Belum disetting")
        supportActionBar!!.title = label
    }

    private fun validasiTransaksiFungsi(value : String,value1 : String,value2 : String){
        val postServices = ValidasiTransaksiResultFeedback.create()
        postServices.getPosts(value,value1,value2).enqueue(object : Callback<DefaultPojo> {

            override fun onFailure(call: Call<DefaultPojo>, error: Throwable) {
                Log.e(_tag, "errornya ${error.message}")
            }

            override fun onResponse(
                call: Call<DefaultPojo>,
                response: retrofit2.Response<DefaultPojo>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()
                    data?.let {
                        val feedback = it
                        println("${feedback.success}, ${feedback.message}")
                        setResult(RESULT_OK)
                        finish()
                    }
                } else {
                    Log.e(_tag, response.isSuccessful.toString())
                }
            }
        })
    }

    private val data: Unit
        get() {
            itemProduk!!.clear()
            adapter!!.notifyDataSetChanged()
            val progressDialog = ProgressDialog(this@MainCetak)
            progressDialog.setMessage("Mencari data...")
            progressDialog.show()
            val queue = Volley.newRequestQueue(this@MainCetak)
            val url = Url.serverPos + "getDataStruk"
            //Toast.makeText(WelcomeActivity.this, url, Toast.LENGTH_LONG).show();
            val stringRequest: StringRequest =
                object : StringRequest(Method.POST, url, Response.Listener { response ->
                    try {
                        val jsonObject = JSONObject(response)
                        val jsonArray = jsonObject.getJSONArray("result")
                        val json = jsonArray.getJSONObject(0)
                        Log.d(_tag, jsonObject.toString())
                        val pesan = json.getString("pesan")
                        if (pesan.equals("0", ignoreCase = true) || pesan.equals(
                                "2",
                                ignoreCase = true
                            )
                        ) {
                            Toast.makeText(this@MainCetak, R.string.data_kosong, Toast.LENGTH_SHORT)
                                .show()
                        } else if (pesan.equals("1", ignoreCase = true)) {
                            idTrx = json.getString("idTrx")
                            tvSubtotal!!.text = json.getString("sub_total")
//                            nmPetugas = json.getString("nmPetugas")
                            tvJmlDisc!!.text = json.getString("jml_disc")
                            tvJmlPajak!!.text = json.getString("jml_pajak")
                            tvTotal!!.text = json.getString("total")
                            var i: Int
                            i = 1
                            while (i < jsonArray.length()) {
                                try {
                                    val jO = jsonArray.getJSONObject(i)
                                    val id = ItemProduk()
                                    id.setNo(i)
                                    id.setNama_produk(jO.getString("nama_produk"))
                                    id.setQty(jO.getString("qty"))
                                    id.isPajak = jO.getString("ispajak")
                                    id.setHarga(jO.getString("harga"))
                                    id.setTotal_harga(jO.getString("total_harga"))
                                    itemProduk!!.add(id)
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                    progressDialog.dismiss()
                                }
                                i++
                            }
                        } else {
                            Toast.makeText(
                                this@MainCetak,
                                "Jaringan masih sibuk !",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    //Toast.makeText(SinkronisasiActivity.this, response, Toast.LENGTH_SHORT).show();
                    adapter!!.notifyDataSetChanged()
                    progressDialog.dismiss()
                }, Response.ErrorListener { error ->
                    progressDialog.dismiss()
                    Toast.makeText(this@MainCetak, error.toString(), Toast.LENGTH_SHORT).show()
                }) {
                    @Throws(AuthFailureError::class)
                    override fun getParams(): Map<String, String> {
                        val params: MutableMap<String, String> = HashMap()
                        val sharedPreferences = getSharedPreferences(Url.SESSION_NAME, MODE_PRIVATE)
                        val kd_pengguna = sharedPreferences.getString(Url.SESSION_ID_PENGGUNA, "0")
                        val id_tempat_usaha =
                            sharedPreferences.getString(Url.SESSION_ID_TEMPAT_USAHA, "")
                        params["kd_pengguna"] = kd_pengguna!!
                        params["id_tempat_usaha"] = id_tempat_usaha!!
                        params["idTrx"] = idTrx!!
                        return params
                    }
                }
            stringRequest.retryPolicy = object : RetryPolicy {
                override fun getCurrentTimeout(): Int {
                    return 50000
                }

                override fun getCurrentRetryCount(): Int {
                    return 50000
                }

                @Throws(VolleyError::class)
                override fun retry(error: VolleyError) {
                }
            }
            queue.add(stringRequest)
        }

//    @AfterPermissionGranted(RC_BLUETOOTH)
//    private fun setupBluetooth() {
//        val params = arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)
//        if (!EasyPermissions.hasPermissions(this, *params)) {
//            EasyPermissions.requestPermissions(
//                this, "You need bluetooth permission",
//                RC_BLUETOOTH, *params
//            )
//            return
//        }
//        mService = BluetoothService(this, BluetoothHandler(this))
//    }

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
//        mService.let {
//            if (it != null) {
//                if (!it.isAvailable) {
//                    Log.i(TAG, "printText: perangkat tidak support bluetooth")
//                    return
//                }

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
//                    val subTotal = tvSubtotal?.text.toString().replace(".", "")

                     text += "[C]--------------------------------\n"+
                     "[L]Subtotal[R]${gantiKetitik(tvSubtotal?.text.toString())}\n"+
                     "[L]Disc[R]${tvJmlDisc?.text}\n"

                    if (tipeStruk.equals("1", ignoreCase = true)) {
//                        writePrint(
//                            PrinterCommands.ESC_ALIGN_CENTER,
//                            "Pajak Resto : " + tvJmlPajak!!.text.toString()
//                        )
                        text += "[L]Pajak Resto[R]${tvJmlPajak?.text}\n"
                    }
                    text += "[C]--------------------------------\n"+
                     "[C]<font size='tall'>TOTAL : ${gantiKetitik(tvTotal?.text.toString())}</font>\n"+
                     "[L]\n"+
                     "[C]- Terima Kasih -"
                    printer.printFormattedText(text)

//                    it.write(PrinterCommands.ESC_ALIGN_CENTER)
//                    it.sendMessage(nmTmpUsaha, "")
//                    it.write(PrinterCommands.ESC_ALIGN_CENTER)
//                    it.sendMessage(alamat, "")
//                    it.write(PrinterCommands.ESC_ENTER)
//                    it.write(PrinterCommands.ESC_ALIGN_LEFT)
//                    it.sendMessage("No. Trx : $idTrx", "")
//                    it.write(PrinterCommands.ESC_ALIGN_LEFT)
//                    it.sendMessage("Tanggal : $tanggal", "")
//                    if (!nmPetugas.equals("", ignoreCase = true)) {
//                        it.write(PrinterCommands.ESC_ALIGN_LEFT)
//                        it.sendMessage("Kasir   : $nmPetugas", "")
//                    }
//                    it.write(PrinterCommands.ESC_ALIGN_CENTER)
//                    it.sendMessage("--------------------------------", "")
//                    for (i in itemProduk!!.indices) {
////                if(itemProduk.get(i).getIsPajak().equalsIgnoreCase("1")) {
//                        val nama_produk = itemProduk!![i].getNama_produk()
//                        val qty = itemProduk!![i].getQty()
//                        val harga = itemProduk!![i].getHarga()
//                        val total_harga = itemProduk!![i].getTotal_harga()
//                        it.write(PrinterCommands.ESC_ALIGN_LEFT)
//                        it.sendMessage(nama_produk, "")
//                        writePrint(
//                            PrinterCommands.ESC_ALIGN_CENTER,
//                            gantiKetitik(harga) + " x " + qty + " : " + gantiKetitik(total_harga)
//                        )
//                        //                }
//                    }
//                    it.write(PrinterCommands.ESC_ALIGN_CENTER)
//                    it.sendMessage("--------------------------------", "")
//                    val subTotal = tvSubtotal!!.text.toString().replace(".", "")
//                    writePrint(
//                        PrinterCommands.ESC_ALIGN_CENTER,
//                        "Subtotal : " + gantiKetitik(subTotal)
//                    )
//                    writePrint(
//                        PrinterCommands.ESC_ALIGN_CENTER,
//                        tvDisc!!.text.toString() + " : " + tvJmlDisc!!.text.toString()
//                    )
//                    if (tipeStruk.equals("1", ignoreCase = true)) {
//                        writePrint(
//                            PrinterCommands.ESC_ALIGN_CENTER,
//                            "Pajak Resto : " + tvJmlPajak!!.text.toString()
//                        )
//                    }
//                    it.write(PrinterCommands.ESC_ALIGN_CENTER)
//                    it.sendMessage("--------------------------------", "")
//
////            writePrint(PrinterCommands.ESC_ALIGN_CENTER, "Total : "+tvTotal.getText().toString());
//                    val a = "Total : " + gantiKetitik(tvTotal!!.text.toString())
//                    printConfig(a, 1, 2, 1)
//                    it.write(PrinterCommands.ESC_ENTER)
//                    printConfig("- Terima Kasih -", 3, 1, 1)
//                    it.write(PrinterCommands.ESC_ENTER)
//                    it.write(PrinterCommands.ESC_ENTER)
                } else {
                    Toast.makeText(this, "Tidak terhubung printer manapun !", Toast.LENGTH_SHORT)
                        .show()
                }
//            }
//        }
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

//    private fun writePrint(align: ByteArray?, msg0: String) {
//        var msg = msg0
//        mService?.write(align)
//        val space = StringBuilder("   ")
//        val l = msg.length
//        if (l < 31) {
//            for (x in 31 - l downTo 0) {
//                space.append(" ")
//            }
//        }
//        msg = msg.replace(" : ", space.toString())
//        mService?.write(msg.toByteArray())
//    }

//    private fun printConfig(bill: String, size: Int, style: Int, align: Int) {
//        //size 1 = large, size 2 = medium, size 3 = small
//        //style 1 = Regular, style 2 = Bold
//        //align 0 = left, align 1 = center, align 2 = right
//        try {
//            val format = byteArrayOf(27, 33, 0)
//            val change = byteArrayOf(27, 33, 0)
//            mService?.write(format)
//
//            //different sizes, same style Regular
//            if (size == 1 && style == 1) //large
//            {
//                change[2] = 0x10.toByte() //large
//                mService?.write(change)
//            } else if (size == 2 && style == 1) //medium
//            {
//                //nothing to change, uses the default settings
//            } else if (size == 3 && style == 1) //small
//            {
//                change[2] = 0x3.toByte() //small
//                mService?.write(change)
//            }
//
//            //different sizes, same style Bold
//            if (size == 1 && style == 2) //large
//            {
//                change[2] = (0x10 or 0x8).toByte() //large
//                mService?.write(change)
//            } else if (size == 2 && style == 2) //medium
//            {
//                change[2] = 0x8.toByte()
//                mService?.write(change)
//            } else if (size == 3 && style == 2) //small
//            {
//                change[2] = (0x3 or 0x8).toByte() //small
//                mService?.write(change)
//            }
//            when (align) {
//                0 ->                     //left align
//                    mService?.write(PrinterCommands.ESC_ALIGN_LEFT)
//                1 ->                     //center align
//                    mService?.write(PrinterCommands.ESC_ALIGN_CENTER)
//                2 ->                     //right align
//                    mService?.write(PrinterCommands.ESC_ALIGN_RIGHT)
//            }
//            mService?.write(bill.toByteArray())
//            mService?.write(byteArrayOf(PrinterCommands.LF))
//        } catch (ex: Exception) {
//            Log.e("error", ex.toString())
//        }
//    }
//
//    protected fun printNewLine() {
//        mService?.write(PrinterCommands.FEED_LINE)
//    }

    companion object {
        const val RC_BLUETOOTH = 0
        const val RC_CONNECT_DEVICE = 1
        const val RC_ENABLE_BLUETOOTH = 2
    }
}