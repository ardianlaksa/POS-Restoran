package com.dnhsolution.restokabmalang.auth

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.telephony.TelephonyManager
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dnhsolution.restokabmalang.BuildConfig
import com.dnhsolution.restokabmalang.MainActivity
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.database.AppRoomDatabase
import com.dnhsolution.restokabmalang.database.TblProdukKategori
import com.dnhsolution.restokabmalang.databinding.ActivityLoginBinding
import com.dnhsolution.restokabmalang.utilities.CheckNetwork
import com.dnhsolution.restokabmalang.utilities.DownloadFileNetworkResult
import com.dnhsolution.restokabmalang.utilities.TaskRunner
import com.dnhsolution.restokabmalang.utilities.Url
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class LoginActivity : AppCompatActivity(),DownloadFileNetworkResult {
    private lateinit var progressDialog1: ProgressDialog
    private var logo: String = ""
    private lateinit var sharedPreferences: SharedPreferences
    private var getAppDatabase: AppRoomDatabase? = null
    var etUsername: EditText? = null
    var etPassword: EditText? = null
    var et1: EditText? = null
    var et2: EditText? = null
    var et3: EditText? = null
    var et4: EditText? = null
    var btnLogin: Button? = null
    var bPassword: Button? = null
    var bvisible = true
    var LAktivasi: LinearLayout? = null
    var LLogin: LinearLayout? = null
    var LKeterangan: LinearLayout? = null
    var kode_aktivasi = ""
    private var uniqueID = ""
    private var versiApp = ""
    private var packageNameApp = ""
    private var _tag = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)
        sharedPreferences = getSharedPreferences(Url.SESSION_NAME, MODE_PRIVATE)
        etPassword = binding.etPassword
        etUsername = binding.etUsername
        btnLogin = binding.btnLogin
        bPassword = binding.bPassword
        LAktivasi = binding.LAktivasi
        LLogin = binding.LLogin
        LKeterangan = binding.LKeterangan
        et1 = binding.et1
        et2 = binding.et2
        et3 = binding.et3
        et4 = binding.et4
        uniqueID = UUID.randomUUID().toString()
        versiApp = BuildConfig.VERSION_CODE.toString() + "." + BuildConfig.VERSION_NAME
        getAppDatabase = AppRoomDatabase.getAppDataBase(this)

        packageNameApp = this.packageName
        et1!!.requestFocus()
        et1!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                et1!!.requestFocus()
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                if (et1!!.text.toString().isEmpty()) {
                    et1!!.requestFocus()
                } else {
                    et2!!.requestFocus()
                }
            }
        })
        et2!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                et2!!.requestFocus()
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                if (et2!!.text.toString().isEmpty()) {
                    et1!!.requestFocus()
                } else {
                    et3!!.requestFocus()
                }
            }
        })
        et3!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                et3!!.requestFocus()
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                if (et3!!.text.toString().isEmpty()) {
                    et2!!.requestFocus()
                } else {
                    et4!!.requestFocus()
                }
            }
        })
        et4!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                et4!!.requestFocus()
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                if (et4!!.text.toString().isEmpty()) {
                    et3!!.requestFocus()
                } else {
                    when {
                        et1!!.text.toString().equals("", ignoreCase = true) -> {
                            et1!!.requestFocus()
                            et1!!.error = "Harap isi bidang ini !"
                        }
                        et2!!.text.toString().equals("", ignoreCase = true) -> {
                            et2!!.requestFocus()
                            et2!!.error = "Harap isi bidang ini !"
                        }
                        et3!!.text.toString().equals("", ignoreCase = true) -> {
                            et3!!.requestFocus()
                            et3!!.error = "Harap isi bidang ini !"
                        }
                        et4!!.text.toString().equals("", ignoreCase = true) -> {
                            et4!!.requestFocus()
                            et4!!.error = "Harap isi bidang ini !"
                        }
                        else -> {
                            val ka =
                                (et1!!.text.toString() + et2!!.text.toString() + et3!!.text.toString()
                                        + et4!!.text.toString())
                            sendAktivasi(ka)
                        }
                    }
                }
            }
        })
        bPassword!!.setOnClickListener { v: View? -> visible() }
        btnLogin!!.setOnClickListener { v: View? ->
            btnLogin!!.isEnabled = false
            val a = (etUsername as TextInputEditText).text.toString().trim { it <= ' ' }
            val b = (etPassword as TextInputEditText).text.toString().trim { it <= ' ' }
            when {
                a.equals("", ignoreCase = true) -> {
                    (etUsername as TextInputEditText).requestFocus()
                    (etUsername as TextInputEditText).error = "Username tidak boleh kosong !"
                    btnLogin!!.isEnabled = true
                }
                b.equals("", ignoreCase = true) -> {
                    (etPassword as TextInputEditText).requestFocus()
                    (etPassword as TextInputEditText).error = "Password tidak boleh kosong !"
                    btnLogin!!.isEnabled = true
                }
                else -> {
                    sendData()
                }
            }
        }
        (etUsername as TextInputEditText).setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                (etPassword as TextInputEditText).requestFocus()
                handled = true
            }
            handled
        })

//        val telephonyManager: TelephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//        uniqueID = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                if(telephonyManager.phoneType == TelephonyManager.PHONE_TYPE_CDMA)
//                    telephonyManager.meid
//                else telephonyManager.imei
//            } else {
//                telephonyManager.deviceId
//            }
    }

    @SuppressLint("ResourceAsColor")
    fun visible() {
        //Toast.makeText(getApplicationContext(), "Coba", Toast.LENGTH_SHORT).show();
        if (bvisible) {
            etPassword!!.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            bPassword!!.background = resources.getDrawable(R.drawable.ic_visibility_off, null)
            bvisible = false
        } else {
            etPassword!!.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            bPassword!!.background = resources.getDrawable(R.drawable.ic_visibility, null)
            bvisible = true
        }
        etPassword!!.setSelection(etPassword!!.length())
    }

    fun sendAktivasi(kode: String) {
        val progressDialog = ProgressDialog(this@LoginActivity)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        val queue = Volley.newRequestQueue(this@LoginActivity)
        val url = Url.serverPos + "Aktivasi"
        //Toast.makeText(WelcomeActivity.this, url, Toast.LENGTH_LONG).show();
        val stringRequest: StringRequest =
            object : StringRequest(Method.POST, url, Response.Listener { response ->
                try {
                    val jsonObject = JSONObject(response)
                    val jsonArray = jsonObject.getJSONArray("result")
                    val json = jsonArray.getJSONObject(0)
                    val pesan = json.getString("pesan")
                    if (pesan.equals("0", ignoreCase = true)) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Kode Aktivasi Tidak Valid !",
                            Toast.LENGTH_LONG
                        ).show()
                    } else if (pesan.equals("1", ignoreCase = true)) {
                        Toast.makeText(this@LoginActivity, "Aktivasi Berhasil", Toast.LENGTH_SHORT)
                            .show()
                        LAktivasi!!.visibility = View.GONE
                        LKeterangan!!.visibility = View.GONE
                        LLogin!!.visibility = View.VISIBLE
                        kode_aktivasi = kode
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
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
                Toast.makeText(this@LoginActivity, error.toString(), Toast.LENGTH_SHORT).show()
            }) {
                override fun getParams(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["kode_aktivasi"] = kode
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

            override fun retry(error: VolleyError) {}
        }
        queue.add(stringRequest)
    }

    private fun sendData() {
        progressDialog1 = ProgressDialog(this@LoginActivity)
        progressDialog1.setMessage("Loading...")
        progressDialog1.show()
        val queue = Volley.newRequestQueue(this@LoginActivity)
        val url = Url.serverPos + "Auth"
        //Toast.makeText(WelcomeActivity.this, url, Toast.LENGTH_LONG).show();
        val stringRequest: StringRequest =
            object : StringRequest(Method.POST, url, Response.Listener { response ->
                println(response)
                try {
                    val jsonObject = JSONObject(response)
                    Log.i("json", jsonObject.toString())
                    val jsonArray = jsonObject.getJSONArray("result")
                    var json = jsonArray.getJSONObject(0)
                    val pesan = json.getString("pesan")
                    if (pesan.equals("0", ignoreCase = true)) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Gagal Login. Username atau Password salah !",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (pesan.equals("1", ignoreCase = true)) {
                        json = jsonArray.getJSONObject(1)
                        Log.i("json1", json.toString())
                        val idTempatusaha = json.getString("ID_TEMPAT_USAHA")
                        val idPengguna = json.getString("ID_PENGGUNA")
                        val nmTempatUsaha = json.getString("NM_TEMPAT_USAHA")
                        val label = json.getString("LABEL_APP")
                        val tema = json.getString("THEME_APP") ?: "0"
                        val alamat = json.getString("ALAMAT")
                        val email = json.getString("EMAIL")
                        val telp = json.getString("TELP")
                        val tipeStruk = json.getString("TIPE_STRUK")
                        val isCetakBilling = json.getString("ISCETAK_BILLING")
                        val namaUser = json.getString("NAME")
                        val namaPetugas = json.getString("NM_PETUGAS")
                        val idJenisPajak = json.getString("ID_JENISPAJAK")
                        val persenPajak = json.getInt("PERSEN_PAJAK")
                        val idHiburanNomor = json.getInt("ID_HIBURAN_NOMOR")
                        val serviceCharge = json.getInt("SERVICE_CHARGE")
                        logo = json.getString("LOGO")

                        //membuat editor untuk menyimpan data ke shared preferences
                        val editor = sharedPreferences.edit()

                        //menambah data ke editor
                        editor.putString(Url.SESSION_USERNAME, etUsername!!.text.toString())
                        editor.putString(Url.SESSION_ID_PENGGUNA, idPengguna)
                        editor.putString(Url.SESSION_ID_TEMPAT_USAHA, idTempatusaha)
                        editor.putString(Url.SESSION_NAMA_TEMPAT_USAHA, nmTempatUsaha)
                        editor.putString(Url.SESSION_ALAMAT, alamat)
                        editor.putString(Url.SESSION_EMAIL, email)
                        editor.putString(Url.SESSION_TELP, telp)
                        editor.putString(Url.setLabel, label)
                        editor.putString(Url.setTema, tema)
                        editor.putString(Url.SESSION_STS_LOGIN, "1")
                        editor.putString(Url.SESSION_TIPE_STRUK, tipeStruk)
                        editor.putString(Url.SESSION_ISCETAK_BILLING, isCetakBilling)
                        editor.putString(Url.SESSION_UUID, uniqueID)
                        editor.putString(Url.SESSION_NAME, namaUser)
                        editor.putString(Url.SESSION_NAMA_PETUGAS, namaPetugas)
                        editor.putInt(Url.SESSION_SERVICE_CHARGE, serviceCharge)
                        editor.putInt(Url.SESSION_PAJAK_PERSEN, persenPajak)
                        editor.putString(Url.SESSION_JENIS_PAJAK, idJenisPajak)
                        editor.putInt(Url.SESSION_ID_HIBURAN_NOMOR, idHiburanNomor)
                        editor.putString(Url.SESSION_LOGO, logo)

                        //menyimpan data ke editor
                        editor.apply()

                        setKategori(idJenisPajak)
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Jaringan masih sibuk !",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                //Toast.makeText(LoginActivity.this, response, Toast.LENGTH_SHORT).show();
                progressDialog1.dismiss()
                btnLogin!!.isEnabled = true
            }, Response.ErrorListener { error: VolleyError ->
                progressDialog1.dismiss()
                btnLogin!!.isEnabled = true
                Toast.makeText(this@LoginActivity, error.toString(), Toast.LENGTH_SHORT).show()
            }) {
                override fun getParams(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["username"] = etUsername!!.text.toString()
                    params["password"] = etPassword!!.text.toString()
                    params["kode_aktivasi"] = kode_aktivasi
                    params["UUID"] = uniqueID
                    params["versiApp"] = versiApp
                    Log.d("getParams", params.toString())
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

            override fun retry(error: VolleyError) {}
        }
        queue.add(stringRequest)
    }

    private fun setKategori(jenisPajak:String){
        val a = getAppDatabase?.tblProdukKategoriDao()
        Thread {
            when (jenisPajak) {
                "01" -> {
                    a?.insert(
                        TblProdukKategori(0, "Kamar", "idTempatUsaha", "idPengguna",6)
                    )
                    a?.insert(
                        TblProdukKategori(0, "Service", "idTempatUsaha", "idPengguna",7)
                    )
                    a?.insert(
                        TblProdukKategori(0, "DLL", "idTempatUsaha", "idPengguna",3)
                    )
                }
                "02" -> {
                    a?.insert(
                        TblProdukKategori(0, "Makanan", "idTempatUsaha", "idPengguna",1)
                    )
                    a?.insert(
                        TblProdukKategori(0, "Minuman", "idTempatUsaha", "idPengguna",2)
                    )
                    a?.insert(
                        TblProdukKategori(0, "DLL", "idTempatUsaha", "idPengguna",3)
                    )
                }
                else -> {
                    a?.insert(
                        TblProdukKategori(0, "Pelayanan 1", "idTempatUsaha", "idPengguna",4)
                    )
                    a?.insert(
                        TblProdukKategori(0, "Pelayanan 2", "idTempatUsaha", "idPengguna",5)
                    )
                    a?.insert(
                        TblProdukKategori(0, "DLL", "idTempatUsaha", "idPengguna",3)
                    )
                }
            }

            if(CheckNetwork().checkingNetwork(this)) {
                val stringUrl = "${Url.serverFoto}$logo"
                val destination =
                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
                Log.i(_tag,stringUrl)
                val runner = TaskRunner()
                runner.executeAsync(DownloadFileNetworkTask(this,stringUrl,destination))
            } else {
                Toast.makeText(this, getString(R.string.tidak_terkoneksi_internet), Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    override fun downloadFileNetworkResult(result: Any?) {
        progressDialog1.dismiss()
        startActivity(Intent(applicationContext, MainActivity::class.java))
        finishAffinity()
    }
}