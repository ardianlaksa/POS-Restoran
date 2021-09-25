package com.dnhsolution.restokabmalang



import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dnhsolution.restokabmalang.dashboard.DashFragment
import com.dnhsolution.restokabmalang.data.DataFragment
import com.dnhsolution.restokabmalang.database.DatabaseHandler
import com.dnhsolution.restokabmalang.sistem.MainMaster
import com.dnhsolution.restokabmalang.tersimpan.DataTersimpanActivity
import com.dnhsolution.restokabmalang.transaksi.produk_list.ProdukListFragment
import com.dnhsolution.restokabmalang.utilities.BottomMenuHelper
import com.dnhsolution.restokabmalang.utilities.CheckNetwork
import com.dnhsolution.restokabmalang.utilities.Url
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object{

        var valueArgsFromKeranjang:Int? = null
        var adDashboard = 0
        var adMasterProduk = 0
        var adTransaksi = 0

    }

    private val _tag = javaClass.simpleName

    //    private lateinit var textMessage: TextView

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->

        when (item.itemId) {

            R.id.navigation_home -> {

                supportFragmentManager.beginTransaction()

                    .replace(R.id.frameLayout, DashFragment()).commit()
                return@OnNavigationItemSelectedListener true

            }

            R.id.navigation_transaksi -> {
                if(status_batas.equals("nonaktif")){
                    supportFragmentManager.beginTransaction()

                        .replace(R.id.frameLayout, ProdukListFragment()).commit()
                }else{
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Pemberitahuan !")
                    builder.setMessage("Menu Transaksi sementara tidak dapat diakses karena anda melebihi batas waktu sinkron.")

                    builder.setPositiveButton(android.R.string.yes) { dialog, which ->

                    }

                    builder.show()
                }

                return@OnNavigationItemSelectedListener true

            }

            R.id.navigation_data -> {

//                textMessage.setText(R.string.title_data)



                supportFragmentManager.beginTransaction()

                    .replace(R.id.frameLayout, DataFragment()).commit()

                return@OnNavigationItemSelectedListener true

            }

            R.id.navigation_data_tersimpan -> {

                startActivity(Intent(this, DataTersimpanActivity::class.java))

                return@OnNavigationItemSelectedListener true

            }



            R.id.navigation_master -> {

//                textMessage.setText(R.string.title_sistem)

                startActivity(Intent(this, MainMaster::class.java))

                return@OnNavigationItemSelectedListener true

            }

        }

        false

    }

    var nama: String = ""
    var email: String = ""
    var telp: String = ""
    var alamat: String = ""
    var tgl_trx : String = ""
    var databaseHandler: DatabaseHandler? = null
    var status_batas: String = ""

    var handler: Handler = Handler()
    var mHandler: Handler? = null
    var navView: BottomNavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val sharedPreferences: SharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)

        val label = sharedPreferences.getString(Url.setLabel, "Belum disetting")

        val tema = sharedPreferences.getString(Url.setTema, "0")

        when {
            tema!!.equals("0", ignoreCase = true) -> {

                this@MainActivity.setTheme(R.style.Theme_First)

            }
            tema.equals("1", ignoreCase = true) -> {

                this@MainActivity.setTheme(R.style.Theme_Second)

            }
            tema.equals("2", ignoreCase = true) -> {

                this@MainActivity.setTheme(R.style.Theme_Third)

            }
            tema.equals("3", ignoreCase = true) -> {

                this@MainActivity.setTheme(R.style.Theme_Fourth)

            }
            tema.equals("4", ignoreCase = true) -> {

                this@MainActivity.setTheme(R.style.Theme_Fifth)

            }
            tema.equals("5", ignoreCase = true) -> {

                this@MainActivity.setTheme(R.style.Theme_Sixth)

            }
        }

        if(CheckNetwork().checkingNetwork(this)) {
            getConfig()
        } else {
            Toast.makeText(this, getString(R.string.check_network), Toast.LENGTH_SHORT).show()
        }

        databaseHandler = DatabaseHandler(this)

        val alamat = sharedPreferences.getString(Url.SESSION_ALAMAT, "0")
        val email = sharedPreferences.getString(Url.SESSION_EMAIL, "0")
        val telp = sharedPreferences.getString(Url.SESSION_TELP, "0")
        val namausaha = sharedPreferences.getString(Url.SESSION_NAMA_TEMPAT_USAHA, "0")
        val id_pengguna = sharedPreferences.getString(Url.SESSION_ID_PENGGUNA, "null")
        status_batas = sharedPreferences.getString(Url.SESSION_STATUS_BATAS, "nonaktif").toString()

        Log.i("json", "$alamat, $email, $telp, $namausaha, $id_pengguna");

        if(alamat!!.equals("", ignoreCase = true) || email!!.equals("", ignoreCase = true) ||
            telp!!.equals("", ignoreCase = true) || namausaha!!.equals("", ignoreCase = true)){
            DialogKelengkapan()
        }

        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        supportActionBar!!.title = label

        navView = findViewById(R.id.nav_view)

        navView!!.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, DashFragment()).commit()

        //show badge in icon menu data tersimpan
        val countDataTersimpan:Int = databaseHandler!!.CountDataTersimpan()

        if(countDataTersimpan > 0){
            var jml: String = ""
            if(countDataTersimpan > 9){
                jml = "9+"
            }else{
                jml = countDataTersimpan.toString()
            }
            BottomMenuHelper.showBadge(this, navView, R.id.navigation_data_tersimpan, jml)
        }

        val db = databaseHandler!!.readableDatabase

        val cTrx = db.rawQuery(
            "SELECT tanggal_trx FROM transaksi WHERE status='0' ORDER BY tanggal_trx ASC LIMIT 1",
            null
        )
        cTrx.moveToFirst()

        val jml_trx : Int = databaseHandler!!.CountDataTersimpan()

        if(jml_trx>0){
            val batas = sharedPreferences.getString(Url.SESSION_BATAS_WAKTU, "3") ?: "0"
            tgl_trx = formatDate(cTrx.getString(0), "yyyyMMdd")
            val tgl_now : String = getDateTime().toString()
            val tgl_batas : String = getDays(tgl_trx, batas.toInt())

            if(tgl_now.toInt() > tgl_batas.toInt()){
                val sharedPreferences = this.getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString(Url.SESSION_STATUS_BATAS, "aktif")
                editor.apply()

                navView!!.menu.findItem(R.id.navigation_transaksi).isEnabled = false


            }else{
                val sharedPreferences = this.getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString(Url.SESSION_STATUS_BATAS, "nonaktif")
                editor.apply()
                navView!!.menu.findItem(R.id.navigation_transaksi).isEnabled = true
            }

            Log.d("TANGGAL", "tgl_trx="+tgl_trx+"/////tgl_now="+tgl_now+"/////tgl_batas="+tgl_batas)
        }else{
            navView!!.menu.findItem(R.id.navigation_transaksi).isEnabled = true
        }


    }

    fun DialogKelengkapan() {
        val dialogBuilder = AlertDialog.Builder(this).create()
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_detail_op, null)

        val etNamaUsaha: EditText
        val etEmail: EditText
        val etTelp: EditText
        val etAlamat: EditText
        val btnSimpan: Button

        etNamaUsaha = dialogView.findViewById<View>(R.id.etNamaUsaha) as EditText
        etEmail = dialogView.findViewById<View>(R.id.etEmail) as EditText
        etTelp = dialogView.findViewById<View>(R.id.etTelp) as EditText
        etAlamat = dialogView.findViewById<View>(R.id.etAlamat) as EditText

        btnSimpan = dialogView.findViewById<View>(R.id.btnSimpan) as Button
        var sharedPreferences: SharedPreferences

        sharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)
        val a = sharedPreferences?.getString(Url.SESSION_ALAMAT, "0")
        val b = sharedPreferences?.getString(Url.SESSION_EMAIL, "0")
        val c = sharedPreferences?.getString(Url.SESSION_TELP, "0")
        val d = sharedPreferences?.getString(Url.SESSION_NAMA_TEMPAT_USAHA, "0")

        etAlamat.setText(a)
        etEmail.setText(b)
        etTelp.setText(c)
        etNamaUsaha.setText(d)

        btnSimpan.setOnClickListener {
            nama = etNamaUsaha.getText().toString()
            email  = etEmail.getText().toString()
            telp = etTelp.getText().toString()
            alamat = etAlamat.getText().toString()

            if (nama.trim({ it <= ' ' }).equals("", ignoreCase = true)) {
                etNamaUsaha.requestFocus()
                etNamaUsaha.setError("Silahkan isi form ini !")
            } else if (email.trim({ it <= ' ' }).equals("", ignoreCase = true)) {
                etEmail.requestFocus()
                etEmail.setError("Silahkan isi form ini !")
            } else if (telp.trim({ it <= ' ' }).equals("", ignoreCase = true)) {
                etTelp.requestFocus()
                etTelp.setError("Silahkan isi form ini !")
            } else if (alamat.trim({ it <= ' ' }).equals("", ignoreCase = true)) {
                etAlamat.requestFocus()
                etAlamat.setError("Silahkan isi form ini !")
            } else {
                sendData()
                dialogBuilder.dismiss()
            }
        }

        dialogBuilder.setView(dialogView)
        dialogBuilder.show()
        dialogBuilder.setCanceledOnTouchOutside(false)
    }

//    fun DialogInfo() {
//        val builder = AlertDialog.Builder(this@MainActivity)
//
//        // Set the alert dialog title
//        builder.setTitle("Info Penting !")
//
//        // Display a message on alert dialog
//        builder.setMessage("Anda tidak terhubung jaringan internet. Silahkan input produk terlebih dahulu untuk melanjutkan transaksi !")
//
//        // Set a positive button and its click listener on alert dialog
//        builder.setPositiveButton("Ok"){dialog, which ->
//            // Do something when user press the positive button
//        }
//
//
//        // Display a neutral button on alert dialog
////        builder.setNeutralButton("Cancel"){_,_ ->
////            Toast.makeText(applicationContext,"You cancelled the dialog.",Toast.LENGTH_SHORT).show()
////        }
//
//        // Finally, make the alert dialog using builder
//        val dialog: AlertDialog = builder.create()
//
//        // Display the alert dialog on app interface
//        dialog.show()
//    }

    fun sendData() {

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.show()
        val queue = Volley.newRequestQueue(this)
        val url = Url.serverPos + "setKelengkapanData"
        //Toast.makeText(WelcomeActivity.this, url, Toast.LENGTH_LONG).show();
        val stringRequest = object : StringRequest(Request.Method.POST, url,
            Response.Listener { response ->
                try {
                    val jsonObject = JSONObject(response)
                    Log.i("json", jsonObject.toString())
                    val jsonArray = jsonObject.getJSONArray("result")
                    var json = jsonArray.getJSONObject(0)
                    val pesan = json.getString("pesan")
                    if (pesan.equals("0", ignoreCase = true)) {
                        Toast.makeText(
                            this,
                            "Gagal Melengkapi data",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (pesan.equals("1", ignoreCase = true)) {
                        Toast.makeText(
                            this,
                            "Sukses Melengkapi data",
                            Toast.LENGTH_SHORT
                        ).show()
                        val sharedPreferences =
                            this!!.getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)

                        //membuat editor untuk menyimpan data ke shared preferences
                        val editor = sharedPreferences.edit()

                        //menambah data ke editor
                        editor.putString(Url.SESSION_NAMA_TEMPAT_USAHA, nama)
                        editor.putString(Url.SESSION_ALAMAT, alamat)
                        editor.putString(Url.SESSION_EMAIL, email)
                        editor.putString(Url.SESSION_TELP, telp)
                        editor.putString(Url.SESSION_KELENGKAPAN, "1")

                        //menyimpan data ke editor
                        editor.apply()
                    } else {
                        Toast.makeText(
                            this,
                            "Jaringan masih sibuk !",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                //Toast.makeText(LoginActivity.this, response, Toast.LENGTH_SHORT).show();
                progressDialog.dismiss()
            }, Response.ErrorListener { error ->
                progressDialog.dismiss()
                Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                val sharedPreferences: SharedPreferences =
                    getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)
                val id_pengguna = sharedPreferences.getString(Url.SESSION_ID_PENGGUNA, "-1")
                params["alamat"] = alamat
                params["nama_usaha"] = nama
                params["email"] = email
                params["telp"] = telp
                params["id_pengguna"] = id_pengguna.toString()

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

            override fun retry(error: VolleyError) {

            }
        }

        queue.add(stringRequest)

    }

    fun getConfig() {
        Log.d("PESAN", "GET CONFIG RUN")
        val queue = Volley.newRequestQueue(this)
        val url = Url.getBatasWaktu
        //Toast.makeText(WelcomeActivity.this, url, Toast.LENGTH_LONG).show();
        val stringRequest = object : StringRequest(Request.Method.POST, url,
            Response.Listener { response ->
                try {
                    val jsonObject = JSONObject(response)
                    Log.i("json", jsonObject.toString())
                    val jsonArray = jsonObject.getJSONArray("result")
                    var json = jsonArray.getJSONObject(0)
                    val pesan = json.getString("pesan")
                    if (pesan.equals("0", ignoreCase = true)) {
                       Log.d("PESAN", "ERROR GET DATA")
                    } else if (pesan.equals("1", ignoreCase = true)) {
                        var batas : String = json.getString("BATAS_WAKTU_SINKRON")
                        Log.d("PESAN", "SUCCESS GET DATA")
                        val sharedPreferences =
                            this!!.getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)

                        //membuat editor untuk menyimpan data ke shared preferences
                        val editor = sharedPreferences.edit()

                        //menambah data ke editor
                        editor.putString(Url.SESSION_BATAS_WAKTU, batas)

                        //menyimpan data ke editor
                        editor.apply()
                    } else {
                        Log.d("PESAN", "JARINGAN SIBUK !")
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                //Toast.makeText(LoginActivity.this, response, Toast.LENGTH_SHORT).show();

            }, Response.ErrorListener { error ->
                Log.d("ERROR", error.toString())
                //Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["status"] = "ok"

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

            override fun retry(error: VolleyError) {

            }
        }

        queue.add(stringRequest)

    }

    private fun getDateTime(): String? {
        val dateFormat: DateFormat = SimpleDateFormat("yyyyMMdd")
        val date = Date()
        return dateFormat.format(date)
    }

    private fun formatDate(date: String, format: String): String{
        var formattedDate = ""
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")

        try {
            val parseDate = sdf.parse(date)
            formattedDate = SimpleDateFormat(format).format(parseDate)
        }catch (e: ParseException){
            e.printStackTrace()
        }

        return formattedDate
    }

    private fun getDays(tanggal: String, days: Int): String {
        var dateInString: String = tanggal // Start date
        var sdf = SimpleDateFormat("yyyyMMdd")
        val c = Calendar.getInstance()
        c.setTime(sdf.parse(dateInString))
        c.add(Calendar.DATE, days)
        sdf = SimpleDateFormat("yyyyMMdd")
        val resultdate = Date(c.getTimeInMillis())
        dateInString = sdf.format(resultdate)

        return dateInString;
    }

    private val m_Runnable: Runnable = object : Runnable {
        override fun run() {
            var countDataTersimpan:Int = databaseHandler!!.CountDataTersimpan()
            var jml: String = ""
            if(countDataTersimpan > 0){

                if(countDataTersimpan > 9){
                    jml = "9+"
                }else{
                    jml = countDataTersimpan.toString()
                }
                BottomMenuHelper.showBadge(this@MainActivity, navView, R.id.navigation_data_tersimpan, jml)
            }else{
                BottomMenuHelper.removeBadge(navView, R.id.navigation_data_tersimpan)
            }
            //Toast.makeText(this@MainActivity, "run looping, total data : "+jml, Toast.LENGTH_SHORT).show()
            mHandler!!.postDelayed(this, 3000)
        }
    } //runnable

    override fun onPause() {
        super.onPause()
        mHandler!!.removeCallbacks(m_Runnable)

    }

    override fun onResume() {
        super.onResume()
        mHandler = Handler()
        mHandler!!.postDelayed(m_Runnable, 3000)
    }
//    fun getDaysAgo(daysAgo: Int): Date {
//        val calendar = Calendar.getInstance()
//        calendar.add(Calendar.DAY_OF_YEAR, +daysAgo)
//
//        return calendar.time
//    }

}