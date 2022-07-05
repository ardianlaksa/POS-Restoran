package com.dnhsolution.restokabmalang.sistem

import androidx.appcompat.app.AppCompatActivity
import android.content.SharedPreferences
import com.dnhsolution.restokabmalang.database.DatabaseHandler
import android.os.Bundle
import com.dnhsolution.restokabmalang.R
import androidx.appcompat.widget.SwitchCompat
import android.widget.CompoundButton
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Button
import com.dnhsolution.restokabmalang.MainActivity
import com.dnhsolution.restokabmalang.SplashActivity
import com.dnhsolution.restokabmalang.database.AppRoomDatabase
import com.dnhsolution.restokabmalang.databinding.ActivityMainMasterBinding
import com.dnhsolution.restokabmalang.sistem.produk.ProdukMasterActivity
import com.dnhsolution.restokabmalang.sistem.theme.ThemeFragment
import com.dnhsolution.restokabmalang.utilities.*
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

class MainSistem : AppCompatActivity() {
    private lateinit var scCetakBilling: SwitchCompat
    private val _tag: String = javaClass.simpleName
    private lateinit var sharedPreferences: SharedPreferences
    var btnInput: Button? = null
    var btnTheme: Button? = null
    var btnProduk: Button? = null
    var btnLogout: Button? = null
    var databaseHandler: DatabaseHandler? = null
    private var isCetakBilling = 1
    lateinit var binding : ActivityMainMasterBinding

    interface IsCetakBillingServices {
        @FormUrlEncoded
        @POST("${Url.serverPos}setUpdateUserCetakBilling")
        fun getPosts(@Field("id") id: String,@Field("isCetakBilling") isCetakBillingValue: Int): Call<DefaultPojo>
    }

    object CetakBillingResultFeedback {
        fun create(): IsCetakBillingServices {
            val gson = GsonBuilder()
                .setLenient()
                .create()
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(Url.serverBase)
                .build()
            return retrofit.create(IsCetakBillingServices::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences(Url.SESSION_NAME, MODE_PRIVATE)
        val idUser = sharedPreferences.getString(Url.SESSION_ID_PENGGUNA, "").toString()
        val spIsCetakBilling = sharedPreferences.getString(Url.SESSION_ISCETAK_BILLING, "")
        val jenisPajak = MainActivity.jenisPajak
        val label = sharedPreferences.getString(Url.setLabel, "Belum disetting")
        val tema = sharedPreferences.getString(Url.setTema, "0")
        when {
            tema.equals("0", ignoreCase = true) -> {
                this@MainSistem.setTheme(R.style.Theme_First)
            }
            tema.equals("1", ignoreCase = true) -> {
                this@MainSistem.setTheme(R.style.Theme_Second)
            }
            tema.equals("2", ignoreCase = true) -> {
                this@MainSistem.setTheme(R.style.Theme_Third)
            }
            tema.equals("3", ignoreCase = true) -> {
                this@MainSistem.setTheme(R.style.Theme_Fourth)
            }
            tema.equals("4", ignoreCase = true) -> {
                this@MainSistem.setTheme(R.style.Theme_Fifth)
            }
            tema.equals("5", ignoreCase = true) -> {
                this@MainSistem.setTheme(R.style.Theme_Sixth)
            }
        }

        binding = ActivityMainMasterBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)
        scCetakBilling = binding.scCetakBilling
        supportActionBar?.title = label
        databaseHandler = DatabaseHandler(this)
        btnInput = binding.bInput
        btnTheme = binding.bTheme
        btnProduk = binding.bProduk
        btnLogout = binding.bLogout
        btnProduk?.setText(R.string.title_daftar_produk)
//        btnInput?.visibility = View.GONE
        val getAppDatabase = AppRoomDatabase.getAppDataBase(this)

        scCetakBilling.isChecked = spIsCetakBilling == "1"

            scCetakBilling.setOnCheckedChangeListener { compoundButton: CompoundButton?, isChecked: Boolean ->

                if (!CheckNetwork().checkingNetwork(this)){
                    scCetakBilling.isChecked = !scCetakBilling.isChecked
                    return@setOnCheckedChangeListener
                }

                scCetakBilling.isEnabled = false
                isCetakBilling = if (isChecked) {
                    1
                } else 0
                isCetakBillingFungsi(idUser)
            }

        btnInput?.setOnClickListener { v: View? ->
            val ft = supportFragmentManager.beginTransaction()
            val prev = supportFragmentManager.findFragmentByTag("dialog")
            if (prev != null) {
                ft.remove(prev)
            }
            val dialogFragment = IsianTextFragmentDialog()
            dialogFragment.show(ft, "dialog")
        }
        btnLogout?.setOnClickListener { v: View? ->

            if(dataSinkron > 0) return@setOnClickListener

            val sharedPreferences = getSharedPreferences(Url.SESSION_NAME, MODE_PRIVATE)

            //membuat editor untuk menyimpan data ke shared preferences
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
            Thread {
                getAppDatabase!!.tblProdukKategoriDao().deleteAll()
//                getAppDatabase?.tblProdukKategoriDao()?.deleteAll()
                databaseHandler!!.deleteAllTable()
                startActivity(Intent(applicationContext, SplashActivity::class.java))
                finishAffinity()
            }.start()
        }
        btnTheme!!.setOnClickListener {
            startActivity(
                Intent(
                    this@MainSistem,
                    ThemeFragment::class.java
                )
            )
        }

//        if(jenisPajak == "03")
//            btnProduk?.visibility = View.GONE

        btnProduk!!.setOnClickListener {
                startActivity(
                    Intent(
                        this@MainSistem,
                        ProdukMasterActivity::class.java
                    )
                )
        }
        val editor = sharedPreferences.edit()

        //menambah data ke editor
        editor.putString(Url.SESSION_TEMP_TEMA, "null")

        //menyimpan data ke editor
        editor.apply()
    }

    private fun isCetakBillingFungsi(value : String){
        val postServices = CetakBillingResultFeedback.create()
        postServices.getPosts(value,isCetakBilling).enqueue(object : Callback<DefaultPojo> {

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
                        if(feedback.success == 0) {
                            scCetakBilling.isChecked = !scCetakBilling.isChecked
                            return@let
                        }

                        val editor = sharedPreferences.edit()
                        editor.putString(Url.SESSION_ISCETAK_BILLING, isCetakBilling.toString())
                        editor.apply()
                    }
                } else {
                    scCetakBilling.isChecked = !scCetakBilling.isChecked
                }
                scCetakBilling.isEnabled = true
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val label = sharedPreferences.getString(Url.setLabel, "Belum disetting")
        supportActionBar!!.title = label
        val tema = sharedPreferences.getString(Url.setTema, "0")
        if (tema.equals("0", ignoreCase = true)) {
            this@MainSistem.setTheme(R.style.Theme_First)
        } else if (tema.equals("1", ignoreCase = true)) {
            this@MainSistem.setTheme(R.style.Theme_Second)
        } else if (tema.equals("2", ignoreCase = true)) {
            this@MainSistem.setTheme(R.style.Theme_Third)
        } else if (tema.equals("3", ignoreCase = true)) {
            this@MainSistem.setTheme(R.style.Theme_Fourth)
        } else if (tema.equals("4", ignoreCase = true)) {
            this@MainSistem.setTheme(R.style.Theme_Fifth)
        } else if (tema.equals("5", ignoreCase = true)) {
            this@MainSistem.setTheme(R.style.Theme_Sixth)
        }
    }

    private val dataSinkron : Int
        get() {
            return databaseHandler!!.CountDataTersimpanUpload()

        }
}