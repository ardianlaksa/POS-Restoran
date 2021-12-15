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
import com.dnhsolution.restokabmalang.MainActivity
import com.zj.btsdk.BluetoothService
import android.content.SharedPreferences
import android.os.Bundle
import android.content.Intent
import com.dnhsolution.restokabmalang.R
import android.app.Activity
import com.dnhsolution.restokabmalang.cetak.DeviceActivity
import com.dnhsolution.restokabmalang.cetak.MainCetak
import butterknife.ButterKnife
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.app.ProgressDialog
import android.util.Log
import android.view.View
import android.widget.Button
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject
import org.json.JSONArray
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import org.json.JSONException
import kotlin.Throws
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import butterknife.OnClick
import com.android.volley.*
import com.dnhsolution.restokabmalang.sistem.MainMaster
import com.dnhsolution.restokabmalang.utilities.DefaultPojo
import com.dnhsolution.restokabmalang.utilities.Url
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main_cetak.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import java.lang.Exception
import java.lang.StringBuilder
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

open class MainCetak : AppCompatActivity(), PermissionCallbacks, HandlerInterface {
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
    private var mService: BluetoothService? = null
    private var isPrinterReady = false
    private var tipeStruk: String? = null
    private var idTrx: String? = "0"
    private var nmPetugas = ""

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
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences(Url.SESSION_NAME, MODE_PRIVATE)
        val label = sharedPreferences.getString(Url.setLabel, "Belum disetting")
        val tema = sharedPreferences.getString(Url.setTema, "0")
        tipeStruk = sharedPreferences.getString(Url.SESSION_TIPE_STRUK, "")
        val uuid = sharedPreferences.getString(Url.SESSION_UUID, "")
        val idPengguna = sharedPreferences.getString(Url.SESSION_ID_PENGGUNA, "")

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

        setContentView(R.layout.activity_main_cetak)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.title = label
        rvData = findViewById<View>(R.id.recyclerView) as RecyclerView
        linearLayout = findViewById<View>(R.id.linearLayout) as LinearLayout
        val llPajak = findViewById<View>(R.id.llPajak) as LinearLayout
        tvSubtotal = findViewById<View>(R.id.tvSubtotal) as TextView
        tvDisc = findViewById<View>(R.id.tvDisc) as TextView
        tvJmlDisc = findViewById<View>(R.id.tvJmlDisc) as TextView
        tvJmlPajak = findViewById<View>(R.id.tvJmlPajak) as TextView
        tvTotal = findViewById<View>(R.id.tvTotal) as TextView
        tv_status = findViewById<View>(R.id.tv_status) as TextView
        btnCetak = findViewById<View>(R.id.btnCetak) as Button
        btnKembali = findViewById<View>(R.id.btnKembali) as Button
        btnPilih = findViewById<View>(R.id.btnPilihBT) as Button
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

        bBatalTrx.setOnClickListener {
            Log.d(_tag,"$idPengguna, $uuid, $idTrx")
            validasiTransaksiFungsi(idPengguna.toString(),uuid.toString(),idTrx.toString())
        }

        btnKembali!!.setOnClickListener { v: View? ->
            setResult(RESULT_OK)
            finish()
        }
        btnPilih!!.setOnClickListener { v: View? ->
            if (!mService!!.isAvailable) {
                Log.i(TAG, "printText: perangkat tidak support bluetooth")
                return@setOnClickListener
            }
            if (mService!!.isBTopen) startActivityForResult(
                Intent(
                    this@MainCetak,
                    DeviceActivity::class.java
                ), RC_CONNECT_DEVICE
            ) else requestBluetooth()
        }
        data
        ButterKnife.bind(this)
        setupBluetooth()
        val sharedPreferences = getSharedPreferences(Url.SESSION_NAME, MODE_PRIVATE)
        val bt_device = sharedPreferences.getString(Url.SESSION_PRINTER_BT, "")
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
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
    }

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
                            nmPetugas = json.getString("nmPetugas")
                            //                        tvDisc.setText(json.getString("disc"));
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
        tv_status!!.text = "Terhubung dengan perangkat"
    }

    override fun onDeviceConnecting() {
        tv_status!!.text = "Sedang menghubungkan..."
    }

    override fun onDeviceConnectionLost() {
        isPrinterReady = false
        tv_status!!.text = "Koneksi perangkat terputus"
    }

    override fun onDeviceUnableToConnect() {
        tv_status!!.text = "Tidak terhubung ke perangkat printer"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RC_ENABLE_BLUETOOTH -> if (resultCode == RESULT_OK) {
                Log.i(TAG, "onActivityResult: bluetooth aktif")
            } else Log.i(TAG, "onActivityResult: bluetooth harus aktif untuk menggunakan fitur ini")
            RC_CONNECT_DEVICE -> if (resultCode == RESULT_OK) {
                val address = data!!.extras!!.getString(DeviceActivity.EXTRA_DEVICE_ADDRESS)
                val mDevice = mService!!.getDevByMac(address)
                mService!!.connect(mDevice)
            }
        }
    }

    @OnClick(R.id.btnCetak)
    fun printText(view: View?) {
        if (!mService!!.isAvailable) {
            Log.i(TAG, "printText: perangkat tidak support bluetooth")
            return
        }
        if (isPrinterReady) {
            val sharedPreferences = getSharedPreferences(Url.SESSION_NAME, MODE_PRIVATE)
            val nm_tempat_usaha = sharedPreferences.getString(Url.SESSION_NAMA_TEMPAT_USAHA, "0")
            val alamat = sharedPreferences.getString(Url.SESSION_ALAMAT, "0")
            val tanggal = dateTime
            mService!!.write(PrinterCommands.ESC_ALIGN_CENTER)
            mService!!.sendMessage(nm_tempat_usaha, "")
            mService!!.write(PrinterCommands.ESC_ALIGN_CENTER)
            mService!!.sendMessage(alamat, "")
            mService!!.write(PrinterCommands.ESC_ENTER)
            mService!!.write(PrinterCommands.ESC_ALIGN_LEFT)
            mService!!.sendMessage("No. Trx : $idTrx", "")
            mService!!.write(PrinterCommands.ESC_ALIGN_LEFT)
            mService!!.sendMessage("Tanggal : $tanggal", "")
            if (!nmPetugas.equals("", ignoreCase = true)) {
                mService!!.write(PrinterCommands.ESC_ALIGN_LEFT)
                mService!!.sendMessage("Kasir   : $nmPetugas", "")
            }
            mService!!.write(PrinterCommands.ESC_ALIGN_CENTER)
            mService!!.sendMessage("--------------------------------", "")
            for (i in itemProduk!!.indices) {
//                if(itemProduk.get(i).getIsPajak().equalsIgnoreCase("1")) {
                val nama_produk = itemProduk!![i].getNama_produk()
                val qty = itemProduk!![i].getQty()
                val harga = itemProduk!![i].getHarga()
                val total_harga = itemProduk!![i].getTotal_harga()
                mService!!.write(PrinterCommands.ESC_ALIGN_LEFT)
                mService!!.sendMessage(nama_produk, "")
                writePrint(
                    PrinterCommands.ESC_ALIGN_CENTER,
                    gantiKetitik(harga) + " x " + qty + " : " + gantiKetitik(total_harga)
                )
                //                }
            }
            mService!!.write(PrinterCommands.ESC_ALIGN_CENTER)
            mService!!.sendMessage("--------------------------------", "")
            val subTotal = tvSubtotal!!.text.toString().replace(".", "")
            writePrint(PrinterCommands.ESC_ALIGN_CENTER, "Subtotal : " + gantiKetitik(subTotal))
            writePrint(
                PrinterCommands.ESC_ALIGN_CENTER,
                tvDisc!!.text.toString() + " : " + tvJmlDisc!!.text.toString()
            )
            if (tipeStruk.equals("1", ignoreCase = true)) {
                writePrint(
                    PrinterCommands.ESC_ALIGN_CENTER,
                    "Pajak Resto: " + tvJmlPajak!!.text.toString()
                )
            }
            mService!!.write(PrinterCommands.ESC_ALIGN_CENTER)
            mService!!.sendMessage("--------------------------------", "")

//            writePrint(PrinterCommands.ESC_ALIGN_CENTER, "Total : "+tvTotal.getText().toString());
            val a = "Total : " + gantiKetitik(tvTotal!!.text.toString())
            printConfig(a, 1, 2, 1)
            mService!!.write(PrinterCommands.ESC_ENTER)
            printConfig("- Terima Kasih -", 3, 1, 1)
            mService!!.write(PrinterCommands.ESC_ENTER)
            mService!!.write(PrinterCommands.ESC_ENTER)
        } else {
            Toast.makeText(this, "Tidak terhubung printer manapun !", Toast.LENGTH_SHORT).show()
            //            if (mService.isBTopen())
//                startActivityForResult(new Intent(this, DeviceActivity.class), RC_CONNECT_DEVICE);
//            else
//                requestBluetooth();
            if (!mService!!.isBTopen) requestBluetooth()
        }
    }

    private fun gantiKetitik(value: String): String {
        var value = value
        value = value.replace(",", ".")
        return value
    }

    private fun requestBluetooth() {
        if (mService != null) {
            if (!mService!!.isBTopen) {
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(intent, RC_ENABLE_BLUETOOTH)
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

    fun writePrint(align: ByteArray?, msg: String) {
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