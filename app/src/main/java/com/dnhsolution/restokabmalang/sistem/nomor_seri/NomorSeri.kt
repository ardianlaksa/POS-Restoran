package com.dnhsolution.restokabmalang.sistem.nomor_seri

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.databinding.ActivityNomorSeriBinding
import com.dnhsolution.restokabmalang.utilities.*
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import android.app.ProgressDialog
import android.view.*
import com.dnhsolution.restokabmalang.sistem.nomor_seri.NomorSeriAdapter
import com.dnhsolution.restokabmalang.sistem.nomor_seri.NomorSeriNetworkTask

data class NSModel(val id:Int, val tgl_request:String, val status:String, val jml_nomor_seri:Int)

interface NomorSeriServices {
    @GET("pdrd/Android/AndroidJsonPOS_Dev2/getNomorSeri")
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

class NomorSeri : AppCompatActivity(), NomorSeriOnDataFetched {
    private var dbTambahNomorSeri: AlertDialog? = null
    private var dbEditTarifParkir: AlertDialog? = null
    private lateinit var idTmpUsaha: String
    private lateinit var psUserId: String
    private lateinit var tipe_parkir: String
    private lateinit var rvData: RecyclerView
    internal var ChildView: View? = null
    internal var RecyclerViewClickedItemPos: Int = 0

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    var sharedPreferences: SharedPreferences? = null
    lateinit var binding: ActivityNomorSeriBinding
    private val _tag = javaClass.simpleName
    lateinit var nomorseriList : List<NSModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNomorSeriBinding.inflate(
            layoutInflater
        )
        val view: View = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)
        rvData = findViewById<View>(R.id.rvData) as RecyclerView

        sharedPreferences = getSharedPreferences(Url.SESSION_NAME, MODE_PRIVATE)
        idTmpUsaha = sharedPreferences?.getString(Url.SESSION_ID_TEMPAT_USAHA, "-1").toString()
        psUserId = sharedPreferences?.getString(Url.SESSION_ID_PENGGUNA, "0").toString()
        val label = sharedPreferences?.getString(Url.setLabel, "Belum disetting")

        supportActionBar?.title = label
        if (CheckNetwork().checkingNetwork(this)) {
            // get post data
            val progress = ProgressDialog(this)
            progress.setTitle("Loading")
            progress.setMessage("Tunggu sebentar ...")
            progress.setCancelable(false) // disable dismiss by tapping outside of the dialog

            progress.show()
            val postServices = NomorSeriRepository.create()
            postServices.getPosts(idTmpUsaha).enqueue(object : Callback<List<NSModel>> {

                override fun onFailure(call: Call<List<NSModel>>, error: Throwable) {
                    Log.e(_tag, "errornya ${error.message}")
                }

                override fun onResponse(
                    call: Call<List<NSModel>>,
                    response: retrofit2.Response<List<NSModel>>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        data?.let {
                            nomorseriList = it
                            val NomorSeriAdapter = NomorSeriAdapter(
                                nomorseriList,
                                this@NomorSeri
                            )
                            val mLayoutManagerss: RecyclerView.LayoutManager =
                                LinearLayoutManager(this@NomorSeri)
                            rvData.layoutManager = mLayoutManagerss
                            rvData.itemAnimator = DefaultItemAnimator()
                            rvData.adapter = NomorSeriAdapter
                        }
                    }
                }
            })
            progress.dismiss()
        }

        rvData.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {

            internal var gestureDetector =
                GestureDetector(applicationContext, object : GestureDetector.SimpleOnGestureListener() {

                    override fun onSingleTapUp(motionEvent: MotionEvent): Boolean {
                        return true
                    }

                })

            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
//                ChildView = rvData.findChildViewUnder(e.x, e.y)
//
//                if (ChildView != null && gestureDetector.onTouchEvent(e)) {
//                    RecyclerViewClickedItemPos = rvData.getChildAdapterPosition(ChildView!!)
//                    val id = nomorseriList!!.get(RecyclerViewClickedItemPos).id
//                    val nama = ""
//                    val nominal = 0
//                    val tgl_buat = ""
//                    val tgl_ubah = ""
//                    val interval = 0
//                    val tarif_tambahan = 0
//                    val jns_kendaraan = 0
//                    dialogEdit(id, nama, nominal, tgl_buat, tgl_ubah, interval, tarif_tambahan, jns_kendaraan)
//                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_tambah, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_menu_tambah -> {
                dialogTambah()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun cekData() {
        if (CheckNetwork().checkingNetwork(this)) {
            // get post data
            val progress = ProgressDialog(this)
            progress.setTitle("Loading")
            progress.setMessage("Tunggu sebentar ...")
            progress.setCancelable(false) // disable dismiss by tapping outside of the dialog

            progress.show()
            val postServices = NomorSeriRepository.create()
            postServices.getPosts(idTmpUsaha).enqueue(object : Callback<List<NSModel>> {

                override fun onFailure(call: Call<List<NSModel>>, error: Throwable) {
                    Log.e(_tag, "errornya ${error.message}")
                }

                override fun onResponse(
                    call: Call<List<NSModel>>,
                    response: retrofit2.Response<List<NSModel>>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        data?.let {
                            nomorseriList = it
                            val NomorSeriAdapter = NomorSeriAdapter(
                                nomorseriList,
                                this@NomorSeri
                            )
                            val mLayoutManagerss: RecyclerView.LayoutManager =
                                LinearLayoutManager(this@NomorSeri)
                            rvData.layoutManager = mLayoutManagerss
                            rvData.itemAnimator = DefaultItemAnimator()
                            rvData.adapter = NomorSeriAdapter
                        }
                    }
                }
            })
            progress.dismiss()
        }
    }

    private fun dialogTambah() {
        dbTambahNomorSeri = AlertDialog.Builder(this).create()
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_tambah_nomor_seri, null)
        val etJmlNomorSeri: EditText = dialogView.findViewById<View>(R.id.etJmlNomorSeri) as EditText
        val btnSimpan: Button = dialogView.findViewById<View>(R.id.btnSimpan) as Button


        btnSimpan.setOnClickListener {
            val jml_nomor_seri = etJmlNomorSeri.text.toString().replace(".", "")
            if (jml_nomor_seri.trim { it <= ' ' }.equals("", ignoreCase = true)) {
                etJmlNomorSeri.requestFocus()
                etJmlNomorSeri.error = "Silahkan isi form ini !"
            }else {
                if(CheckNetwork().checkingNetwork(applicationContext)) {
                    val params = HashMap<String, String>()
                    params["idTmpUsaha"] = idTmpUsaha
                    params["ps_user_id"] = psUserId
                    params["jml_nomor_seri"] = etJmlNomorSeri.getText().toString().replace(".", "")
                    Log.d("NomorSeri", "dialogTambah: "+params)
                    val runner = TaskRunner()
                    runner.executeAsync(
                        NomorSeriNetworkTask(
                            this,
                            applicationContext,
                            Url.TambahNomorSeri,
                            params
                        )
                    )
                }

                dbTambahNomorSeri?.dismiss()
                cekData()
            }
        }


        etJmlNomorSeri.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                etJmlNomorSeri.removeTextChangedListener(this)
                try {
                    var originalString = s.toString()
                    if (originalString.contains(".")) {
                        originalString = originalString.replace(".", "")
                    }
                    val longval: Long = originalString.toLong()
                    val formatter = NumberFormat.getInstance(Locale.US) as DecimalFormat
                    formatter.applyPattern("#,###,###,###")
                    val formattedString = formatter.format(longval)

                    //setting text after format to EditText
                    etJmlNomorSeri.setText(formattedString.replace(",", "."))
                    etJmlNomorSeri.setSelection(etJmlNomorSeri.text.length)
                } catch (nfe: NumberFormatException) {
                    nfe.printStackTrace()
                }
                etJmlNomorSeri.addTextChangedListener(this)
            }
        })
        dbTambahNomorSeri?.setView(dialogView)
        dbTambahNomorSeri?.show()
    }

    override fun showProgressBar() {
    }

    override fun hideProgressBar() {
    }

    override fun setDataInPageWithResult(result: Any?) {
        when (result) {
            "" -> {
                Toast.makeText(applicationContext, R.string.empty_data, Toast.LENGTH_SHORT).show()
                return
            }
            "-1" -> {
                Toast.makeText(applicationContext, R.string.time_out, Toast.LENGTH_SHORT).show()
                return
            }
            "cancelled" -> return
        }

        Log.e(_tag, "Response from url:$result")

        try {
            val jsonObj = JSONObject(result.toString())
            val success = jsonObj.getString("success")
            if (success == "1") {
                dbTambahNomorSeri?.dismiss()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            Toast.makeText(applicationContext, R.string.error_data, Toast.LENGTH_SHORT).show()
        }
    }
}