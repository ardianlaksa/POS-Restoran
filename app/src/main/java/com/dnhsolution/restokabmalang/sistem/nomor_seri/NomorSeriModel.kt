package com.dnhsolution.parkirkabmalang.sistem.nomor_seri

import com.dnhsolution.restokabmalang.utilities.Url
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class NSModel(val id:Int, val tgl_request:String, val status:String, val jml_nomor_seri:Int)

interface NomorSeriServices {
    @GET("pdrd/Android/AndroidJsonPOS/getNomorSeri")
    fun getPosts(@Query("idTmpUsaha") nilai:String): Call<List<NSModel>>
}

object NomorSeriRepository {
    fun create(): NomorSeriServices {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(Url.serverBase)
            .build()
        return retrofit.create(NomorSeriServices::class.java)
    }
}

class NomorSeriModel {
}