package com.dnhsolution.restokabmalang.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextWatcher
import android.text.Editable
import android.widget.TextView.OnEditorActionListener
import android.view.inputmethod.EditorInfo
import android.annotation.SuppressLint
import android.os.Build
import com.dnhsolution.restokabmalang.R
import android.app.ProgressDialog
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject
import org.json.JSONArray
import org.json.JSONException
import com.android.volley.VolleyError
import com.android.volley.RetryPolicy
import android.content.SharedPreferences
import android.content.Intent
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.*
import com.android.volley.Response
import com.dnhsolution.restokabmalang.BuildConfig
import com.dnhsolution.restokabmalang.MainActivity
import com.dnhsolution.restokabmalang.database.AppRoomDatabase
import com.dnhsolution.restokabmalang.database.TblProdukKategori
import com.dnhsolution.restokabmalang.databinding.ActivityLoginBinding
import com.dnhsolution.restokabmalang.utilities.Url
import com.google.android.material.textfield.TextInputEditText
import java.util.*

class LoginActivity : AppCompatActivity() {
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)
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
                if (et1!!.text.toString().length == 0) {
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
                if (et2!!.text.toString().length == 0) {
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
                if (et3!!.text.toString().length == 0) {
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
    }

    @SuppressLint("ResourceAsColor")
    fun visible() {
        //Toast.makeText(getApplicationContext(), "Coba", Toast.LENGTH_SHORT).show();
        if (bvisible) {
            etPassword!!.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bPassword!!.background = resources.getDrawable(R.drawable.ic_visibility_off, null)
            } else bPassword!!.setBackgroundDrawable(resources.getDrawable(R.drawable.ic_visibility_off))
            bvisible = false
        } else {
            etPassword!!.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bPassword!!.background = resources.getDrawable(R.drawable.ic_visibility, null)
            } else bPassword!!.setBackgroundDrawable(resources.getDrawable(R.drawable.ic_visibility))
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
        val progressDialog = ProgressDialog(this@LoginActivity)
        progressDialog.setMessage("Loading...")
        progressDialog.show()
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
                        val tema = json.getString("THEME_APP")
                        val alamat = json.getString("ALAMAT")
                        val email = json.getString("EMAIL")
                        val telp = json.getString("TELP")
                        val tipeStruk = json.getString("TIPE_STRUK")
                        val isCetakBilling = json.getString("ISCETAK_BILLING")
                        val namaUser = json.getString("NAME")
                        val namaPetugas = json.getString("NM_PETUGAS")
                        val idJenisPajak = json.getString("ID_JENISPAJAK")
                        val sharedPreferences = getSharedPreferences(Url.SESSION_NAME, MODE_PRIVATE)

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
                        var pajakPersen = 0
                        when {
                            idJenisPajak.equals("02", ignoreCase = true) -> pajakPersen =
                                10
                            idJenisPajak.equals("01", ignoreCase = true) -> pajakPersen =
                                35
                            idJenisPajak.equals("03", ignoreCase = true) -> pajakPersen =
                                35
                        }
                        editor.putInt(Url.SESSION_PAJAK_PERSEN, pajakPersen)
                        editor.putString(Url.SESSION_JENIS_PAJAK, idJenisPajak)

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
                progressDialog.dismiss()
                btnLogin!!.isEnabled = true
            }, Response.ErrorListener { error: VolleyError ->
                progressDialog.dismiss()
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
                        TblProdukKategori(6, "Kamar", "idTempatUsaha", "idPengguna")
                    )
                    a?.insert(
                        TblProdukKategori(7, "Service", "idTempatUsaha", "idPengguna")
                    )
                    a?.insert(
                        TblProdukKategori(3, "DLL", "idTempatUsaha", "idPengguna")
                    )
                }
                "02" -> {
                    a?.insert(
                        TblProdukKategori(1, "Makanan", "idTempatUsaha", "idPengguna")
                    )
                    a?.insert(
                        TblProdukKategori(2, "Minuman", "idTempatUsaha", "idPengguna")
                    )
                    a?.insert(
                        TblProdukKategori(3, "DLL", "idTempatUsaha", "idPengguna")
                    )
                }
                else -> {
                    a?.insert(
                        TblProdukKategori(4, "Pelayanan 1", "idTempatUsaha", "idPengguna")
                    )
                    a?.insert(
                        TblProdukKategori(5, "Pelayanan 2", "idTempatUsaha", "idPengguna")
                    )
                    a?.insert(
                        TblProdukKategori(3, "DLL", "idTempatUsaha", "idPengguna")
                    )
                }
            }

            startActivity(Intent(applicationContext, MainActivity::class.java))
            finishAffinity()
        }.start()
    }
}