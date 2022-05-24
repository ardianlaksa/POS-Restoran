package com.dnhsolution.restokabmalang.utilities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.dnhsolution.restokabmalang.MainActivity
import com.dnhsolution.restokabmalang.databinding.IsianTextFragmentDialogBinding
import com.dnhsolution.restokabmalang.sistem.MainSistem
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

class IsianTextFragmentDialog : DialogFragment() {

    interface serviceChargeServices {
        @FormUrlEncoded
        @POST("${Url.serverPos}setServiceCharge")
        fun getPosts(@Field("idPengguna") idPengguna: String
                     , @Field("uuid") uuid: String
                     , @Field("serviceCharge") serviceCharge: Int): Call<DefaultPojo>
    }

    object serviceChargeServicesResultFeedback {
        fun create(): serviceChargeServices {
            val gson = GsonBuilder()
                .setLenient()
                .create()
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(Url.serverBase)
                .build()
            return retrofit.create(serviceChargeServices::class.java)
        }
    }

    private val _tag = javaClass.simpleName
    lateinit var binding : IsianTextFragmentDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, 0)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Do all the stuff to initialize your custom view
        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences(Url.SESSION_NAME
            , Context.MODE_PRIVATE)
        binding = IsianTextFragmentDialogBinding.inflate(layoutInflater)

        val idPengguna = MainActivity.idPengguna ?: ""
        val uuid = MainActivity.uuid ?: ""
        val pajakPersen = MainActivity.pajakPersen
        val spServiceCharge = sharedPreferences.getInt(Url.SESSION_SERVICE_CHARGE,0)
        binding.etLabel.hint = spServiceCharge.toString()
        binding.bSimpan.setOnClickListener {
            val isianServiceCharge = binding.etLabel.text.toString()
            if(isianServiceCharge != "")
                if(isianServiceCharge.toInt() < pajakPersen)
                    serviceChargeFungsi(sharedPreferences,idPengguna,uuid,isianServiceCharge.toInt())
                else
                    Toast.makeText(requireContext(),"Service Charge melebihi batas pajak",Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    private fun serviceChargeFungsi(sharedPreferences : SharedPreferences,idPengguna : String
                                ,uuid : String,serviceCharge : Int){
        val postServices = serviceChargeServicesResultFeedback.create()
        postServices.getPosts(idPengguna,uuid,serviceCharge).enqueue(object : Callback<DefaultPojo> {

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
                        if(feedback.success == 1) {
                            val editor = sharedPreferences.edit()
                            editor.putInt(Url.SESSION_SERVICE_CHARGE, serviceCharge)
                            editor.apply()
                            dismiss()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(),"Gagal Menyimpan", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}