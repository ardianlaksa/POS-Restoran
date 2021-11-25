package com.dnhsolution.restokabmalang.auth

import androidx.appcompat.app.AppCompatActivity
import android.content.SharedPreferences
import com.jaredrummler.android.widget.AnimatedSvgView
import android.os.Bundle
import com.dnhsolution.restokabmalang.R
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import com.dnhsolution.restokabmalang.auth.LoginActivity
import com.dnhsolution.restokabmalang.MainActivity
import com.dnhsolution.restokabmalang.database.DatabaseHandler
import com.dnhsolution.restokabmalang.sistem.MainMaster
import com.dnhsolution.restokabmalang.sistem.produk.DeletePojo
import com.dnhsolution.restokabmalang.utilities.CheckNetwork
import com.dnhsolution.restokabmalang.utilities.Url
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

class SplashActivity : AppCompatActivity() {
    private var databaseHandler: DatabaseHandler? = null
    private var status : String? = null
    private val _tag: String? = javaClass.simpleName
    var sharedPreferences: SharedPreferences? = null

    /*package*/
    var svgView: AnimatedSvgView? = null

    interface IsCetakBillingServices {
        @FormUrlEncoded
        @POST("pdrd/Android/AndroidJsonPOS/setCekUuid")
        fun getPosts(@Field("id") id: String): Call<CekUUIDPojo>
    }

    object CekUUIDResultFeedback {
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

    /*package*/
    var index = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        sharedPreferences = getSharedPreferences(Url.SESSION_NAME, MODE_PRIVATE)
        status = sharedPreferences?.getString(Url.SESSION_STS_LOGIN, "0")
        val idPengguna = sharedPreferences?.getString(Url.SESSION_ID_PENGGUNA, "0")

        svgView = findViewById<View>(R.id.ivLogoDaerah) as AnimatedSvgView
        databaseHandler = DatabaseHandler(this)
        svgView!!.postDelayed({
            svgView!!.start()
            idPengguna?.let { cekUUIDFungsi(it) }
        }, 500)
        if (!CheckNetwork().checkingNetwork(this)){
            if (status.equals("0", ignoreCase = true)) {
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            } else {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            }
            finish()
        }
//        Handler().postDelayed({
//            if (status.equals("0", ignoreCase = true)) {
//                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
//            } else {
//                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
//            }
//            finish()
//        }, 4000)
    }

    private fun cekUUIDFungsi(value : String){
        val postServices = CekUUIDResultFeedback.create()
        postServices.getPosts(value).enqueue(object : Callback<CekUUIDPojo> {

            override fun onFailure(call: Call<CekUUIDPojo>, error: Throwable) {
                Log.e(_tag, "errornya ${error.message}")
            }

            override fun onResponse(
                call: Call<CekUUIDPojo>,
                response: retrofit2.Response<CekUUIDPojo>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()
                    data?.let {

                        if(data.success == 1) {
                            if (status.equals("0", ignoreCase = true)) {
                                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                            } else {
                                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                            }
                        } else {
                            val sharedPreferences = getSharedPreferences(Url.SESSION_NAME, MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.clear()
                            editor.apply()
                            databaseHandler?.deleteAllTable()
                            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                        }

                        finish()
                    }
                } else {
                    Toast.makeText(this@SplashActivity, R.string.error_data, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}