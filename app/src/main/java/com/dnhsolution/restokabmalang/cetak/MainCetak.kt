package com.dnhsolution.restokabmalang.cetak

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dantsu.escposprinter.connection.DeviceConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.textparser.PrinterTextParserImg
import com.dnhsolution.restokabmalang.MainActivity
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.database.DatabaseHandler
import com.dnhsolution.restokabmalang.databinding.ActivityMainCetakBinding
import com.dnhsolution.restokabmalang.utilities.DefaultPojo
import com.dnhsolution.restokabmalang.utilities.TampilanBarcode
import com.dnhsolution.restokabmalang.utilities.Url
import com.google.gson.GsonBuilder
import org.json.JSONException
import org.json.JSONObject
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

open class MainCetak : AppCompatActivity() {

    private var bitmap: Bitmap? = null

    //    private var rangeTransaksiKarcisAkhir: String = "0"
    private var idHiburanNomor: String = ""
    private var nomorTerakhirKarcis: String = ""
    private var iQty: Int = 0
    private var idPengguna: String? = null
    private var uuid: String? = null
//    private var nomorKarcis: String = ""
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
    private var selectedDevice: BluetoothConnection? = null
    private val PERMISSION_BLUETOOTH = 100
    private val PERMISSION_BLUETOOTH_ADMIN = 101
    private val PERMISSION_BLUETOOTH_CONNECT = 102
    private val PERMISSION_BLUETOOTH_SCAN = 103

    interface ValidasiTransaksiServices {
        @FormUrlEncoded
        @POST("${Url.serverPos}setUpdateValidasiTransaksi")
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

    interface UpdateHiburanNomorServices {
        @FormUrlEncoded
        @POST(Url.setUpdateHiburanNomor)
        fun getPosts(@Field("idPengguna") idPengguna: String
            , @Field("UUID") uuid: String
            , @Field("idTempatUsaha") idTempatUsaha: String
            , @Field("nomorTerakhirKarcis") nomorTerakhirKarcis: String
            , @Field("arrIdHiburanNomor") arrIdHiburanNomor: String): Call<DefaultPojo>
    }

    object UpdateHiburanNomorResultFeedback {
        fun create(): UpdateHiburanNomorServices {
            val gson = GsonBuilder()
                .setLenient()
                .create()
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(Url.serverBase)
                .build()
            return retrofit.create(UpdateHiburanNomorServices::class.java)
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
        uuid = sharedPreferences.getString(Url.SESSION_UUID, "")
        idPengguna = sharedPreferences.getString(Url.SESSION_ID_PENGGUNA, "")
        val btDevice = sharedPreferences.getString(Url.SESSION_PRINTER_BT, "")
        supportActionBar?.title = label

        nmPetugas = MainActivity.namaPetugas ?: ""

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
        linearLayoutManager = LinearLayoutManager(this@MainCetak)
        linearLayoutManager?.orientation = RecyclerView.VERTICAL
        dividerItemDecoration =
            DividerItemDecoration(rvData?.context, linearLayoutManager!!.orientation)
        rvData?.setHasFixedSize(true)
        rvData?.layoutManager = linearLayoutManager
        //recyclerView.addItemDecoration(dividerItemDecoration);
        itemProduk = ArrayList()
        adapter = AdapterProduk(itemProduk, this@MainCetak)
        rvData?.adapter = adapter

        if (tipeStruk.equals("2", ignoreCase = true)) llPajak.visibility = View.GONE

        binding.bBatalTrx.setOnClickListener {
            Log.d(_tag,"$idPengguna, $uuid, $idTrx")
            validasiTransaksiFungsi(idPengguna.toString(),uuid.toString(),idTrx.toString())
        }

        btnKembali!!.setOnClickListener { v: View? ->
//            val resultIntent = Intent()
//            resultIntent.putExtra("kembali", "0")
            setResult(RESULT_OK)
            finish()
        }

        btnPilih?.setOnClickListener { v: View? ->
//            resultLauncher.launch(Intent(this@MainCetak, DeviceActivity::class.java))
//            requestCode = RC_CONNECT_DEVICE
            browseBluetoothDevice();
        }

        btnCetak?.setOnClickListener {
            printBluetooth()
        }

        getData

        var destinationFile = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/"
        destinationFile += "ic_malang_makmur_grayscale.png"
        bitmap = BitmapFactory.decodeFile(destinationFile)
        bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(destinationFile),128,112,true)
    }

    open fun browseBluetoothDevice() {
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

    open fun printBluetooth() {
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

                        if(MainActivity.jenisPajak == "03"){
//                            println("${MainActivity.idTempatUsaha} $idPengguna $uuid $idHiburanNomor $nomorTerakhirKarcis" )
//                            updateHiburanNomorFungsi(MainActivity.idTempatUsaha.toString()
//                                ,idPengguna.toString(),uuid.toString(), nomorTerakhirKarcis.toString()
//                            )
                            updateHiburanNomorFungsi(
                                idPengguna.toString(),uuid.toString(), MainActivity.idTempatUsaha.toString()
                                ,nomorTerakhirKarcis,idHiburanNomor
                            )
                        }
                    }
                }
            ).execute(
                when (MainActivity.jenisPajak) {
                    "01" -> printTextHotelHiburan(selectedDevice)
                    "02" -> printText(selectedDevice)
                    else -> {
                        printTextHiburanKarcis(selectedDevice)
                    }
                }
            )
        }
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

    private fun updateHiburanNomorFungsi(idPengguna : String,uuid : String,idTempatUsaha : String,
                                         nomorTerakhirKarcis : String ,idHiburanNomor : String){

        val postServices = UpdateHiburanNomorResultFeedback.create()
            postServices.getPosts(idPengguna,uuid,idTempatUsaha,nomorTerakhirKarcis,idHiburanNomor
            ).enqueue(object : Callback<DefaultPojo> {

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

                        val databaseHandler = DatabaseHandler(applicationContext)
                        val splitNomorKarcis = nomorTerakhirKarcis.split("|")
                        for(a in splitNomorKarcis) {
                            val splitA = a.split(":")
                            databaseHandler.updateNomorTerakhirKarcisProduk(
                                splitA[0],
                                splitA[1],
                                splitA[2]
                            )
                        }
//                        setResult(RESULT_OK)
//                        finish()
                    }
                } else {
                    Log.e(_tag, "updateHiburanNomorFungsi ${response.isSuccessful}")
                }
            }
        })
    }

    private val getData: Unit
        get() {
            val databaseHandler = DatabaseHandler(this)
            val db = databaseHandler.readableDatabase

            itemProduk?.clear()
//            adapter!!.notifyDataSetChanged()
            val progressDialog = ProgressDialog(this@MainCetak)
            progressDialog.setMessage("Mencari data...")
            progressDialog.show()
            val queue = Volley.newRequestQueue(this@MainCetak)
            val url = Url.getDataStruk
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
                            tvSubtotal?.text = json.getString("sub_total")
                            tvJmlDisc?.text = json.getString("jml_disc")
                            tvJmlPajak?.text = json.getString("jml_pajak")
                            tvTotal?.text = json.getString("total")
                            val serviceChargeRp = json.getString("serviceChargeRp")
                            if(serviceChargeRp != "")
                                binding.tvJmlServiceCharge.text = json.getString("serviceChargeRp")
                            else
                                binding.llServiceCharge.visibility = View.GONE
//                            nomorKarcis = json.getString("nomor_karcis")
                            var i = 1
                            while (i < jsonArray.length()) {
                                try {
                                    val jO = jsonArray.getJSONObject(i)
                                    val id = ItemProduk()
                                    id.setNo(i)
                                    val idProduk = jO.getString("id_produk")
                                    id.setId_produk(idProduk)
                                    id.setNama_produk(jO.getString("nama_produk"))
                                    id.setQty(jO.getString("qty"))
                                    id.isPajak = jO.getString("ispajak")
                                    id.setHarga(jO.getString("harga"))
                                    id.setTotal_harga(jO.getString("total_harga"))
                                    id.nomorKarcis = jO.getString("nomor_karcis")

                                    val cDetailTrx = db.rawQuery("select range_transaksi_karcis_akhir " +
                                            "from produk where id ='" + idProduk + "'", null)
                                    if (cDetailTrx.moveToFirst()) {
                                        do {
                                            val a = cDetailTrx.getString(0)
                                            id.rangeTransaksiKarcisAkhir = a
                                            println("asdf $a $idProduk")
                                        }while(cDetailTrx.moveToNext())
                                    }
                                    cDetailTrx.close()

                                    if(idHiburanNomor != "") idHiburanNomor += "|"
                                    idHiburanNomor += jO.getString("id_hiburan_nomor")
                                    itemProduk?.add(id)
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                    progressDialog.dismiss()
                                }
                                i++
                            }
                            db.close()
                        } else {
                            Toast.makeText(
                                this@MainCetak,
                                R.string.error_get_data,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    //Toast.makeText(SinkronisasiActivity.this, response, Toast.LENGTH_SHORT).show();
                    adapter?.notifyDataSetChanged()
                    progressDialog.dismiss()
                }, Response.ErrorListener { error ->
                    progressDialog.dismiss()
                    Toast.makeText(this@MainCetak, error.toString(), Toast.LENGTH_SHORT).show()
                }) {
                    @Throws(AuthFailureError::class)
                    override fun getParams(): Map<String, String> {
                        val params: MutableMap<String, String> = HashMap()
                        val sharedPreferences = getSharedPreferences(Url.SESSION_NAME, MODE_PRIVATE)
                        val idPengguna = sharedPreferences.getString(Url.SESSION_ID_PENGGUNA, "0")
                        val idTempatusaha =
                            sharedPreferences.getString(Url.SESSION_ID_TEMPAT_USAHA, "")
                        params["kd_pengguna"] = idPengguna ?: ""
                        params["id_tempat_usaha"] = idTempatusaha ?: ""
                        params["idTrx"] = idTrx ?: ""
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

    private fun printText(printerConnection: DeviceConnection?) : AsyncEscPosPrinter  {
//        val printer = EscPosPrinter(connection, 203, 48f, 32)
        val printer = AsyncEscPosPrinter(printerConnection, 203, 48f, 32)

        Log.d(_tag,"ready")
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
        val splTanggal = tanggal.split("-")
        val hari = splTanggal[0]
        val bulan = splTanggal[1]
        var text = ""
        for (h in itemProduk!!.indices) {
            val nomorKarcis = itemProduk!![h].nomorKarcis
            val pisahNomorKarcis = nomorKarcis.split("-")
            val idProduk = itemProduk!![h].getId_produk()
            val qtyKarcis = itemProduk!![h].getQty()
            val hargaKarcis = itemProduk!![h].getHarga()
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
                if(hari == "01" && bulan == "01"){
                    pnk = 1
                    nomorSeriBaru = "AA"
                }
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
                            bitmap
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

    companion object {
        const val RC_BLUETOOTH = 0
        const val RC_CONNECT_DEVICE = 1
        const val RC_ENABLE_BLUETOOTH = 2
    }
}