package com.dnhsolution.restokabmalang.sistem.produk.server

import android.Manifest
import com.dnhsolution.restokabmalang.R
import androidx.recyclerview.widget.RecyclerView
import android.app.ProgressDialog
import com.dnhsolution.restokabmalang.database.DatabaseHandler
import com.dnhsolution.restokabmalang.utilities.Url
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.DefaultItemAnimator
import com.dnhsolution.restokabmalang.utilities.CheckNetwork
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject
import org.json.JSONArray
import org.json.JSONException
import kotlin.Throws
import android.text.TextWatcher
import android.text.Editable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestListener
import android.graphics.drawable.Drawable
import com.bumptech.glide.load.engine.GlideException
import android.content.pm.PackageManager
import android.provider.MediaStore
import androidx.core.content.FileProvider
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.os.*
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.android.volley.*
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.target.Target
import com.dnhsolution.restokabmalang.BuildConfig
import com.dnhsolution.restokabmalang.sistem.produk.*
import com.dnhsolution.restokabmalang.utilities.dialog.AdapterWizard
import com.dnhsolution.restokabmalang.utilities.dialog.ItemView
import java.io.*
import java.lang.Exception
import java.lang.NumberFormatException
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import retrofit2.Call
import android.widget.Toast
import com.dnhsolution.restokabmalang.MainActivity
import com.dnhsolution.restokabmalang.utilities.HapusProdukMasterOnTask
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import com.google.gson.GsonBuilder


class MakananFragment() : Fragment(), HapusProdukMasterOnTask {
    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_server, parent, false)
    }

    private var uuid: String? = null
    private lateinit var idPengguna: String
    private var isSearch: Boolean = false
    private lateinit var searchView: SearchView
    private var slctdTipeProduk: String? = null
    private var slctdIspajak: String? = null
    private var isRunnerRunning: Boolean = false
    var rvProduk: RecyclerView? = null
    internal var produkAdapter: AdapterProduk? = null
    private val itemProduks: MutableList<ItemProduk> = ArrayList()
    private var itemProduksNotFiltered: MutableList<ItemProduk> = ArrayList()
    var RecyclerViewClickedItemPos = 0
    var ChildView: View? = null
    var tvKet: TextView? = null
    private var idTmpUsaha: String? = "-1"
    private val _tag = javaClass.simpleName
    var tempNameFile = "POSRestoran.jpg"
    private var filePath: Uri? = null
    var wallpaperDirectory: File? = null
    var e_nama_file = ""
    var t_nama_file = ""
    var status = ""
    var ivGambarBaru: ImageView? = null
    var ivGambar: ImageView? = null
    var progressdialog: ProgressDialog? = null
    var e_nama: String? = null
    var e_harga: String? = null
    var e_ket: String? = null
    var e_id: String? = null
    var e_gambar_lama: String? = null
    var databaseHandler: DatabaseHandler? = null
    private var statusJaringan = 1
    private var menuTemp: Menu? = null
    private var valueJenisProduk = 1
    val keyParams = "params"

    private val isPajakList: ArrayList<IsPajakListElement>
        get(){
            val isPajak = ArrayList<IsPajakListElement>()
            isPajak.add(IsPajakListElement("1","Pajak"))
            isPajak.add(IsPajakListElement("0","Tanpa Pajak"))
            return isPajak
        }

    private val tipeProdukList: ArrayList<TipeProdukListElement>
        get(){
            val tipeProduk = ArrayList<TipeProdukListElement>()
            tipeProduk.add(TipeProdukListElement("1","Makanan"))
            tipeProduk.add(TipeProdukListElement("2","Minuman"))
            tipeProduk.add(TipeProdukListElement("3","Dll"))
            return tipeProduk
        }

    companion object {

        private val CAMERA_REQUEST = 1888
        private val MY_CAMERA_PERMISSION_CODE = 100
        private val FILE_SELECT_CODE = 5
        private val IMAGE_DIRECTORY = "/POSRestoran"

        @JvmStatic
        fun newInstance(params: String) = MakananFragment().apply {
            arguments = Bundle().apply {
                putString(keyParams,params)
            }
        }
    }

    interface DeleteServices {
        @FormUrlEncoded
        @POST("pdrd/Android/AndroidJsonPOS/setHapusProduk")
        fun getPosts(@Field("id") id: String): Call<DeletePojo>
    }

    object DeleteResultFeedback {

        fun create(): DeleteServices {
            val gson = GsonBuilder()
                .setLenient()
                .create()
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(Url.serverBase)
                .build()
            return retrofit.create(DeleteServices::class.java)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        val sharedPreferences = requireContext().getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)
        idTmpUsaha = sharedPreferences.getString(Url.SESSION_ID_TEMPAT_USAHA, "")
        idPengguna = MainActivity.idPengguna ?: ""
        uuid = ProdukMasterActivity.uuid

        databaseHandler = DatabaseHandler(context)
        rvProduk = view.findViewById<View>(R.id.rvProduk) as RecyclerView
        tvKet = view.findViewById<View>(R.id.tvKet) as TextView
        rvProduk!!.layoutManager = GridLayoutManager(context, 3)
        rvProduk!!.itemAnimator = DefaultItemAnimator()
        produkAdapter = AdapterProduk(itemProduks, itemProduksNotFiltered, requireContext(),this)
        rvProduk!!.adapter = produkAdapter
        if (CheckNetwork().checkingNetwork((context)!!)) {
            ambilData
            tvKet!!.visibility = View.GONE
            isRunnerRunning = true
        } else {
            dataLokal
        }

//        val mainHandler = Handler(Looper.getMainLooper())
//        mainHandler.post(object : Runnable {
//            override fun run() {
//                if(!isRunnerRunning) {
//                    if(context == null) return
//                    cekData
//                    isRunnerRunning = true
//                }
//                mainHandler.postDelayed(this, 5000)
//            }
//        })
    }

    private fun hapusFungsi(idBarang : String){
        val postServices = DeleteResultFeedback.create()
        postServices.getPosts(idBarang).enqueue(object : Callback<DeletePojo> {

            override fun onFailure(call: Call<DeletePojo>, error: Throwable) {
                Log.e(_tag, "errornya ${error.message}")
            }

            override fun onResponse(
                call: Call<DeletePojo>,
                response: retrofit2.Response<DeletePojo>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()
                    data?.let {
                        val feedback = it
                        println("${feedback.success}, ${feedback.message}")
                        if(feedback.success == 1) {
                            startActivity(
                                Intent(
                                    requireContext(),
                                    ProdukMasterActivity::class.java
                                )
                            )
                            activity?.finish()
                        }
                    }
                }
            }
        })
    }

    private val ambilData: Unit
        get() {
            itemProduks.clear()
            val progressDialog = ProgressDialog(context)
            progressDialog.setMessage("Mencari data...")
            progressDialog.show()
            val queue = Volley.newRequestQueue(context)
            Log.d("ID_TEMPAT_USAHA", (idTmpUsaha)!!)
            val url = Url.serverPos + "getProduk?idTmpUsaha=" + idTmpUsaha +
                    "&jenisProduk=${arguments?.get(keyParams).toString()}&idPengguna=$idPengguna"
            //Toast.makeText(WelcomeActivity.this, url, Toast.LENGTH_LONG).show();
            Log.i(_tag, url)
            val stringRequest: StringRequest =
                object : StringRequest(Method.GET, url, Response.Listener { response: String? ->
                    try {
                        val jsonObject = JSONObject(response)
                        val status: Int = jsonObject.getInt("success")
                        val message = jsonObject.getString("message")
                        if (status == 1) {
                            val jsonArray: JSONArray = jsonObject.getJSONArray("result")
                            var i = 0
                            while (i < jsonArray.length()) {
                                try {
                                    val jO: JSONObject = jsonArray.getJSONObject(i)
                                    val itemProduk = ItemProduk()
                                    val idBarang: String = jO.getString("ID_BARANG")
                                    itemProduk.id_barang = idBarang
                                    itemProduk.nama_barang = jO.getString("NM_BARANG")
                                    itemProduk.url_image = jO.getString("FOTO")
                                    itemProduk.harga = jO.getString("HARGA")
                                    itemProduk.keterangan = jO.getString("KETERANGAN")
                                    itemProduk.isPajak = jO.getString("ISPAJAK")
                                    itemProduk.jenisProduk = jO.getString("JENIS_PRODUK")
                                    itemProduk.status = true
                                    Log.d("NM_BARANG", jO.getString("NM_BARANG"))
                                    if (databaseHandler!!.CountDataProdukId(idBarang.toInt()) == 0) tambahDataLokal(
                                        itemProduk
                                    )
                                    itemProduks.add(itemProduk)
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                    progressDialog.dismiss()
                                }
                                i++
                            }
                            gantiIconWifi(true)
                            isRunnerRunning = false
                        } else {
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        isRunnerRunning = false
                    }
                    //Toast.makeText(SinkronisasiActivity.this, response, Toast.LENGTH_SHORT).show();
//                    produkAdapter!!.notifyDataSetChanged()
                    itemProduksNotFiltered = itemProduks
                    produkAdapter = AdapterProduk(itemProduks, itemProduksNotFiltered, requireContext(), this)
                    rvProduk!!.adapter = produkAdapter
                    progressDialog.dismiss()
                    isRunnerRunning = false

                }, Response.ErrorListener { error: VolleyError ->
                    progressDialog.dismiss()
                    Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show()
                    isRunnerRunning = false
                }) {
                    @Throws(AuthFailureError::class)
                    override fun getParams(): Map<String, String> {
                        val params: MutableMap<String, String> = HashMap()
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

                override fun retry(error: VolleyError) {}
            }
            queue.add(stringRequest)
        }

//    private val cekData: Unit
//        get() {
//            if(context == null) return
//            val queue = Volley.newRequestQueue(context)
//            Log.d("ID_TEMPAT_USAHA", (idTmpUsaha)!!)
//            val url = Url.serverPos + "getProduk?idTmpUsaha=" + idTmpUsaha
//            //Toast.makeText(WelcomeActivity.this, url, Toast.LENGTH_LONG).show();
//            Log.i(_tag, url)
//            val stringRequest: StringRequest =
//                object : StringRequest(Method.GET, url, Response.Listener { response: String? ->
//                    try {
//                        val jsonObject = JSONObject(response)
//                        val status: Int = jsonObject.getInt("success")
//                        if (status == 1) {
//                            if(activity == null) return@Listener
//                            gantiIconWifi(true)
//                            isRunnerRunning = false
//                            return@Listener
//                        }
//                    } catch (e: JSONException) {
//                        e.printStackTrace()
//                    }
//                    gantiIconWifi(false)
//                    isRunnerRunning = false
//                }, Response.ErrorListener { error: VolleyError ->
//                    gantiIconWifi(false)
//                    isRunnerRunning = false
//                }) {
//                    @Throws(AuthFailureError::class)
//                    override fun getParams(): Map<String, String> {
//                        val params: MutableMap<String, String> = HashMap()
//                        params["status"] = "ok"
//                        return params
//                    }
//                }
//            stringRequest.retryPolicy = object : RetryPolicy {
//                override fun getCurrentTimeout(): Int {
//                    return 50000
//                }
//
//                override fun getCurrentRetryCount(): Int {
//                    return 50000
//                }
//
//                override fun retry(error: VolleyError) {}
//            }
//            queue.add(stringRequest)
//        }

    private fun tambahDataLokal(itemProduk: ItemProduk) {
        try {
            databaseHandler!!.insert_produk(
                com.dnhsolution.restokabmalang.database.ItemProduk(
                    itemProduk.id_barang.toInt(),
                    idTmpUsaha,
                    itemProduk.nama_barang,
                    itemProduk.harga,
                    itemProduk.keterangan,
                    itemProduk.url_image,
                    "0",
                    itemProduk.isPajak,
                    itemProduk.jenisProduk
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun dialogEdit(url_image: String, nama_barang: String?,
        id_barang: String?, harga: String, ket: String?, isPajak: String?, jenisProduk: String?) {
        slctdIspajak = null
        slctdTipeProduk = null

        val dialogBuilder = AlertDialog.Builder(context).create()
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_edit_produk, null)
        val etNama: EditText = dialogView.findViewById<View>(R.id.etNama) as EditText
        val etKeterangan: EditText = dialogView.findViewById<View>(R.id.etKeterangan) as EditText
        val etHarga: EditText = dialogView.findViewById<View>(R.id.etHarga) as EditText
        val btnSimpan: Button = dialogView.findViewById<View>(R.id.btnSimpan) as Button
        val ivGambarLama: ImageView = dialogView.findViewById<View>(R.id.ivGambarLama) as ImageView
        ivGambarBaru = dialogView.findViewById<View>(R.id.ivGambarBaru) as ImageView
        val ivTambahGambar: ImageView = dialogView.findViewById<View>(R.id.ivTambahGambar) as ImageView
        val spiIsPajak = dialogView.findViewById<View>(R.id.spiIsPajak) as Spinner
        val spiTipeProduk = dialogView.findViewById<View>(R.id.spiTipeProduk) as Spinner

        spiIsPajak.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                println("a :$isPajak")
                if(slctdIspajak == null)
                    isPajakList.forEachIndexed { index, element ->
                        if(element.idItem == isPajak) parent?.setSelection(index)
                    }
                slctdIspajak = isPajakList[position].idItem
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        spiTipeProduk.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                println("b :$jenisProduk")
                if(slctdTipeProduk == null)
                    tipeProdukList.forEachIndexed { index, element ->
                        if(element.idItem == jenisProduk) parent?.setSelection(index)
                    }
                slctdTipeProduk = tipeProdukList[position].idItem
                if(slctdTipeProduk == "3"){
                    slctdIspajak = "0"
                    spiIsPajak.setSelection(1)
                } else {
                    slctdIspajak = "1"
                    spiIsPajak.setSelection(0)
                }

            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        val spinIsPajakAdapter = context?.let {
            IsPajakSpinAdapter(
                it,
                R.layout.item_spi_ispajak,
                isPajakList
            )
        }

        spiIsPajak.adapter = spinIsPajakAdapter
        spiIsPajak.isEnabled = false

        val spinTipeProdukAdapter = context?.let {
            TipeProdukSpinAdapter(
                it,
                R.layout.item_spi_tipe_produk,
                tipeProdukList
            )
        }

        spiTipeProduk.adapter = spinTipeProdukAdapter

        btnSimpan.setOnClickListener {
            e_nama = etNama.text.toString()
            e_harga = etHarga.text.toString().replace(".", "")
            e_ket = etKeterangan.text.toString()
            e_id = id_barang
            e_gambar_lama = url_image
            when {
                e_nama!!.trim { it <= ' ' }.equals("", ignoreCase = true) -> {
                    etNama.requestFocus()
                    etNama.error = "Silahkan isi form ini !"
                }
                e_harga!!.trim { it <= ' ' }.equals("", ignoreCase = true) -> {
                    etHarga.requestFocus()
                    etHarga.error = "Silahkan isi form ini !"
                }
                e_ket!!.trim { it <= ' ' }.equals("", ignoreCase = true) -> {
                    etKeterangan.requestFocus()
                    etKeterangan.error = "Silahkan isi form ini !"
                }
                else -> {
                    updateData()
                    dialogBuilder.dismiss()
                }
            }
        }
        var originalString = harga
        val longval: Long
        if (originalString.contains(".")) {
            originalString = originalString.replace(".", "")
        }
        longval = originalString.toLong()
        val formatter = NumberFormat.getInstance(Locale.US) as DecimalFormat
        formatter.applyPattern("#,###,###,###")
        val formattedString = formatter.format(longval)

        //setting text after format to EditText
        etHarga.setText(formattedString.replace(",", "."))
        etHarga.setSelection(etHarga.text.length)
        etNama.setText(nama_barang)
        etKeterangan.setText(ket)
        etHarga.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                // TODO Auto-generated method stub
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                // TODO Auto-generated method stub
            }

            override fun afterTextChanged(s: Editable) {
                etHarga.removeTextChangedListener(this)
                try {
                    var originalString = s.toString()
                    val longval: Long
                    if (originalString.contains(".")) {
                        originalString = originalString.replace(".", "")
                    }
                    longval = originalString.toLong()
                    val formatter = NumberFormat.getInstance(Locale.US) as DecimalFormat
                    formatter.applyPattern("#,###,###,###")
                    val formattedString = formatter.format(longval)

                    //setting text after format to EditText
                    etHarga.setText(formattedString.replace(",", "."))
                    etHarga.setSelection(etHarga.text.length)
                } catch (nfe: NumberFormatException) {
                    nfe.printStackTrace()
                }
                etHarga.addTextChangedListener(this)
                // TODO Auto-generated method stub
            }
        })

        if (CheckNetwork().checkingNetwork((context)!!)) {
            Glide.with(ivGambarLama.context).load(Url.serverFoto + url_image)
                .placeholder(R.mipmap.ic_foto)
                .centerCrop()
                .fitCenter()
                .listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e("xmx1", "Error " + e.toString())
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any,
                        target: Target<Drawable?>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e("xmx1", "no Error ")
                        return false
                    }
                })
                .into(ivGambarLama)
        } else {
            Glide.with(ivGambarLama.context).load(File(url_image).toString())
                .placeholder(R.mipmap.ic_foto)
                .centerCrop()
                .fitCenter()
                .listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e("xmx1", "Error " + e.toString())
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any,
                        target: Target<Drawable?>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e("xmx1", "no Error ")
                        return false
                    }
                })
                .into(ivGambarLama)
        }
        ivGambarLama.visibility = View.VISIBLE
        ivTambahGambar.setOnClickListener {
            wallpaperDirectory =
                File(Environment.getExternalStorageDirectory().toString() + IMAGE_DIRECTORY)
            if (!wallpaperDirectory!!.exists()) {  // have the object build the directory structure, if needed.
                wallpaperDirectory!!.mkdirs()
            }
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Pilihan Tambah Foto")
                .setPositiveButton("Galeri", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, id: Int) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if ((activity!!.checkSelfPermission(Manifest.permission.CAMERA)
                                        != PackageManager.PERMISSION_GRANTED)
                            ) {
                                requestPermissions(
                                    arrayOf(
                                        Manifest.permission.CAMERA,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    ),
                                    MY_CAMERA_PERMISSION_CODE
                                )
                                //showFileChooser();
                            } else {
                                wallpaperDirectory = File(
                                    Environment.getExternalStorageDirectory()
                                        .toString() + IMAGE_DIRECTORY
                                )
                                if (!wallpaperDirectory!!.exists()) {  // have the object build the directory structure, if needed.
                                    wallpaperDirectory!!.mkdirs()
                                }
                                showFileChooser()
                                status = "e"
                            }
                        } else {
                            wallpaperDirectory = File(
                                Environment.getExternalStorageDirectory()
                                    .toString() + IMAGE_DIRECTORY
                            )
                            if (!wallpaperDirectory!!.exists()) {  // have the object build the directory structure, if needed.
                                wallpaperDirectory!!.mkdirs()
                            }
                            showFileChooser()
                            status = "e"
                        }
                    }
                })
                .setNegativeButton("Kamera", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, id: Int) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if ((activity!!.checkSelfPermission(Manifest.permission.CAMERA)
                                        != PackageManager.PERMISSION_GRANTED)
                            ) {
                                requestPermissions(
                                    arrayOf(
                                        Manifest.permission.CAMERA,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    ),
                                    MY_CAMERA_PERMISSION_CODE
                                )
                                //showFileChooser();
                            } else {
                                wallpaperDirectory = File(
                                    Environment.getExternalStorageDirectory()
                                        .toString() + IMAGE_DIRECTORY
                                )
                                if (!wallpaperDirectory!!.exists()) {  // have the object build the directory structure, if needed.
                                    wallpaperDirectory!!.mkdirs()
                                }
                                val cal = Calendar.getInstance()
                                val sdf = SimpleDateFormat("ddMMyyHHmmss", Locale.getDefault())
                                tempNameFile = "Cam_" + sdf.format(cal.time) + ".jpg"
                                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                val f = File(wallpaperDirectory, tempNameFile)
                                val photoURI = FileProvider.getUriForFile(
                                    (context)!!,
                                    BuildConfig.APPLICATION_ID + ".provider",
                                    f
                                )
                                //cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                                startActivityForResult(cameraIntent, CAMERA_REQUEST)
                                status = "e"
                            }
                        } else {
                            wallpaperDirectory = File(
                                Environment.getExternalStorageDirectory()
                                    .toString() + IMAGE_DIRECTORY
                            )
                            if (!wallpaperDirectory!!.exists()) {  // have the object build the directory structure, if needed.
                                wallpaperDirectory!!.mkdirs()
                            }
                            val cal = Calendar.getInstance()
                            val sdf = SimpleDateFormat("ddMMyyHHmmss", Locale.getDefault())
                            tempNameFile = "Cam_" + sdf.format(cal.time) + ".jpg"
                            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            val f = File(wallpaperDirectory, tempNameFile)
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f))
                            startActivityForResult(intent, CAMERA_REQUEST)
                            status = "e"
                        }
                    }
                })
            val alert = builder.create()
            alert.show()
        }
        dialogBuilder.setView(dialogView)
        dialogBuilder.show()
    }

    private fun showFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        try {
            startActivityForResult(
                Intent.createChooser(intent, "Select a File to Upload"),
                FILE_SELECT_CODE
            )
        } catch (ex: ActivityNotFoundException) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(
                context, "Please install a File Manager.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) {
            return
        }
        if (requestCode == FILE_SELECT_CODE) {
            filePath = data!!.data
            val a = RealPathUtil.getRealPath(context, filePath)
            if (resultCode == Activity.RESULT_OK) {
                // Get the Uri of the selected file
                val uri = data.data
                Log.d("Foto", "File Uri: " + uri.toString())
                // Get the path
                val path = a
                Log.d("Foto", "File Path: $path")
                val sourceLocation = File(path)
                val cal = Calendar.getInstance()
                val sdf = SimpleDateFormat("ddMMyyHHmmss", Locale.getDefault())
                val filename = "Gallery_" + sdf.format(cal.time) + ".jpg"
                val targetLocation = File(wallpaperDirectory.toString(), filename)
                if (sourceLocation.exists()) {
                    Log.v("Pesan", "Proses Pindah")
                    try {
                        val `in`: InputStream = FileInputStream(sourceLocation)
                        val out: OutputStream = FileOutputStream(targetLocation)
                        // Copy the bits from instream to outstream
                        val buf = ByteArray(1024)
                        var len: Int
                        while ((`in`.read(buf).also { len = it }) > 0) {
                            out.write(buf, 0, len)
                        }
                        `in`.close()
                        out.close()
                        Log.v("Pesan", "Copy file successful.")
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else {
                    Log.v("Pesan", "Copy file failed. Source file missing.")
                }
                val file = File(wallpaperDirectory.toString(), filename)
                val file_size = (file.length() / 1024).toString().toInt()
                Log.d("PirangMB", file_size.toString())
                if (status.equals("e", ignoreCase = true)) {
                    Glide.with(ivGambarBaru!!.context).load(File(file.absolutePath).toString())
                        .placeholder(R.mipmap.ic_foto)
                        .centerCrop()
                        .fitCenter()
                        .listener(object : RequestListener<Drawable?> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any,
                                target: Target<Drawable?>,
                                isFirstResource: Boolean
                            ): Boolean {
                                Log.e("xmx1", "Error " + e.toString())
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any,
                                target: Target<Drawable?>,
                                dataSource: DataSource,
                                isFirstResource: Boolean
                            ): Boolean {
                                Log.e("xmx1", "no Error ")
                                return false
                            }
                        })
                        .into((ivGambarBaru)!!)
                    ivGambarBaru!!.visibility = View.VISIBLE
                    if (e_nama_file.equals("", ignoreCase = true)) {
                    } else {
                        val fl = File(e_nama_file)
                        val deleted = fl.delete()
                    }
                    e_nama_file = file.absolutePath
                } else if (status.equals("t", ignoreCase = true)) {
                    Glide.with(ivGambar!!.context).load(File(file.absolutePath).toString())
                        .placeholder(R.mipmap.ic_foto)
                        .centerCrop()
                        .fitCenter()
                        .listener(object : RequestListener<Drawable?> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any,
                                target: Target<Drawable?>,
                                isFirstResource: Boolean
                            ): Boolean {
                                Log.e("xmx1", "Error " + e.toString())
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any,
                                target: Target<Drawable?>,
                                dataSource: DataSource,
                                isFirstResource: Boolean
                            ): Boolean {
                                Log.e("xmx1", "no Error ")
                                return false
                            }
                        })
                        .into((ivGambar)!!)
                    ivGambar!!.visibility = View.VISIBLE
                    if (t_nama_file.equals("", ignoreCase = true)) {
                    } else {
                        val fl = File(t_nama_file)
                        val deleted = fl.delete()
                    }
                    t_nama_file = file.absolutePath
                }
            }
        } else if (requestCode == CAMERA_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                println("CAMERA_REQUEST1")
                //                        Bitmap photo = (Bitmap) data.getExtras().get("data");
                var f = File(wallpaperDirectory.toString())
                Log.d("File", f.toString())
                for (temp: File in f.listFiles()) {
                    if ((temp.name == tempNameFile)) {
                        f = temp
                        val filePhoto = File(wallpaperDirectory.toString(), tempNameFile)
                        //pic = photo;
                        val file_size = (filePhoto.length() / 1024).toString().toInt()
                        Log.d("PirangMB", file_size.toString())
                        //tvFileName.setVisibility(View.VISIBLE);
                        // ivBerkas.setVisibility(View.VISIBLE);
                        if (status.equals("e", ignoreCase = true)) {
                            Glide.with(ivGambarBaru!!.context).load(File(f.absolutePath).toString())
                                .placeholder(R.mipmap.ic_foto)
                                .centerCrop()
                                .fitCenter()
                                .listener(object : RequestListener<Drawable?> {
                                    override fun onLoadFailed(
                                        e: GlideException?,
                                        model: Any,
                                        target: Target<Drawable?>,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        Log.e("xmx1", "Error " + e.toString())
                                        return false
                                    }

                                    override fun onResourceReady(
                                        resource: Drawable?,
                                        model: Any,
                                        target: Target<Drawable?>,
                                        dataSource: DataSource,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        Log.e("xmx1", "no Error ")
                                        return false
                                    }
                                })
                                .into((ivGambarBaru)!!)
                            ivGambarBaru!!.visibility = View.VISIBLE
                            if (e_nama_file.equals("", ignoreCase = true)) {
                            } else {
                                val fl = File(e_nama_file)
                                val deleted = fl.delete()
                            }
                            e_nama_file = f.absolutePath
                        } else if (status.equals("t", ignoreCase = true)) {
                            Glide.with(ivGambar!!.context).load(File(f.absolutePath).toString())
                                .placeholder(R.mipmap.ic_foto)
                                .centerCrop()
                                .fitCenter()
                                .listener(object : RequestListener<Drawable?> {
                                    override fun onLoadFailed(
                                        e: GlideException?,
                                        model: Any,
                                        target: Target<Drawable?>,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        Log.e("xmx1", "Error " + e.toString())
                                        return false
                                    }

                                    override fun onResourceReady(
                                        resource: Drawable?,
                                        model: Any,
                                        target: Target<Drawable?>,
                                        dataSource: DataSource,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        Log.e("xmx1", "no Error ")
                                        return false
                                    }
                                })
                                .into((ivGambar)!!)
                            ivGambar!!.visibility = View.VISIBLE
                            if (t_nama_file.equals("", ignoreCase = true)) {
                            } else {
                                val fl = File(t_nama_file)
                                val deleted = fl.delete()
                            }
                            t_nama_file = f.absolutePath
                        }
                        break
                    }
                }
            }
        }
    }

    private fun updateData() {
        open class UpdateData() : AsyncTask<Void?, Int?, String?>() {
            //ProgressDialog uploading;
            override fun onPreExecute() {
                super.onPreExecute()
                progressdialog = ProgressDialog(context)
                progressdialog!!.setCancelable(false)
                progressdialog!!.setMessage("Upload data ke server ...")
                progressdialog!!.show()
                //uploading = ProgressDialog.show(SinkronActivity.this, "Mengirim data ke Server", "Mohon Tunggu...", false, false);
            }

            override fun onPostExecute(s: String?) {
                super.onPostExecute(s)
                // uploading.dismiss();
                Log.d("HASIL", (s)!!)
                //Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show();
                // tvStatus.setText(s);
                //
                when {
                    s.equals("sukses", ignoreCase = true) -> {
                        if (progressdialog!!.isShowing) progressdialog!!.dismiss()
                        Toast.makeText(context, "Data berhasil diupdate !", Toast.LENGTH_SHORT).show()
                        val fl = File(e_nama_file)
                        val deleted = fl.delete()
                        e_nama_file = ""
                        updateDataLokal()
                        ambilData
                    }
                    s.equals("gagal", ignoreCase = true) -> {
                        if (progressdialog!!.isShowing) progressdialog!!.dismiss()
                        Toast.makeText(context, "Data gagal diupdate !", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        if (progressdialog!!.isShowing) progressdialog!!.dismiss()
                        Toast.makeText(context, s, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun doInBackground(vararg p0: Void?): String? {
                val u = UploadData()
                var msg: String? = null
                msg = u.uploadDataUmum(idPengguna,e_nama, e_ket, e_harga, e_id, e_gambar_lama, e_nama_file
                    , slctdIspajak, slctdTipeProduk)
                return msg
            }
        }

        val uv = UpdateData()
        uv.execute()
    }

    private val dataLokal: Unit
        get() {
            itemProduks.clear()
            produkAdapter!!.notifyDataSetChanged()
            val jmlData = databaseHandler!!.CountDataProduk()
            if (jmlData == 0) {
                tvKet!!.visibility = View.VISIBLE
            } else {
                tvKet!!.visibility = View.GONE
            }
            try {
                val listDataProduk: List<ItemProduk> = databaseHandler!!.getDataProduk2(arguments?.get(keyParams).toString())
                for (f: ItemProduk in listDataProduk) {
                    val ip = ItemProduk()
                    ip.id_barang = f.id_barang
                    ip.nama_barang = f.nama_barang
                    ip.harga = f.harga
                    ip.url_image = f.url_image
                    ip.keterangan = f.keterangan
                    ip.status = false
                    ip.isPajak = f.isPajak
                    ip.jenisProduk = f.jenisProduk
                    itemProduks.add(ip)
                }
            } catch (e: SQLiteException) {
                e.printStackTrace()
            }
            produkAdapter!!.notifyDataSetChanged()
        }

    private fun updateDataLokal() {
        try {
            databaseHandler!!.update_produk(
                com.dnhsolution.restokabmalang.database.ItemProduk(
                    e_id!!.toInt(), idTmpUsaha, e_nama, e_harga, e_ket, e_gambar_lama
                    , "0",slctdIspajak,slctdTipeProduk
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menuTemp = menu
        inflater.inflate(R.menu.menu_master, menu)
        val searchItem = menu.findItem(R.id.action_menu_cari)
        searchView = searchItem.actionView as SearchView
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.onActionViewExpanded()
        val stringTextSearch:CharSequence = getString(R.string.cari)
        val ss1 = SpannableString(stringTextSearch)
        ss1.setSpan(RelativeSizeSpan(0.7f), 0, ss1.length, 0) // set size
        val searchEditText = searchView.findViewById<View>(androidx.appcompat.R.id.search_src_text) as EditText
        searchEditText.hint = ss1

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                produkAdapter!!.filter.filter(newText)
                return false
            }
        })

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
                isSearch = true
                return true
            }

            override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
                isSearch = false
                produkAdapter!!.filter.filter("")
                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_menu_lanjut) {
            if (CheckNetwork().checkingNetwork(requireContext()) && statusJaringan == 1) dialogTambah()
            else Toast.makeText(
                requireContext(),
                R.string.tidak_terkoneksi_internet,
                Toast.LENGTH_SHORT
            ).show()
            return true
        } else if (item.itemId == R.id.action_menu_bantuan) {
            tampilAlertDialogTutorial()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun gantiIconWifi(value: Boolean) {
        println("gantiIconWifi2 : $menuTemp")
//        if (value) {
//            menuTemp?.getItem(3)?.icon =
//                ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_wifi_24_green)
//            statusJaringan = 1
//        } else {
//            menuTemp?.getItem(3)?.icon =
//                ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_wifi_24_gray)
//            statusJaringan = 0
//        }
    }

    private fun tampilAlertDialogTutorial() {
        val alertDialog = AlertDialog.Builder(requireContext()).create()
        val rowList = layoutInflater.inflate(R.layout.dialog_tutorial, null)
        val listView: ListView = rowList.findViewById(R.id.listView)
        val tutorialArrayAdapter: AdapterWizard
        val arrayList = java.util.ArrayList<ItemView>()
        arrayList.add(
            ItemView(
                "1",
                "Saat ada icon refresh warna kuning dimasing-masing daftar produk, menandakan jika produk diload dari peralatan lokal."
            )
        )
        arrayList.add(
            ItemView(
                "2",
                "Saat ada icon panah kanan kiri warna hijau dimasing-masing daftar produk, menandakan jika produk tersinkron dengan server."
            )
        )
        arrayList.add(
            ItemView(
                "3",
                "Tombol icon (+) samping icon [?] di kanan atas untuk mulai transaksi dengan produk yang dipilih."
            )
        )
        tutorialArrayAdapter = AdapterWizard(requireContext(), arrayList)
        listView.adapter = tutorialArrayAdapter
        alertDialog.setView(rowList)
        alertDialog.show()
    }

    private fun dialogTambah() {
        val dialogBuilder = AlertDialog.Builder(requireContext()).create()
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_tambah_produk, null)
        val etNama: EditText
        val etKeterangan: EditText
        val etHarga: EditText
        val btnSimpan: Button
        val ivTambahFoto: ImageView
        etNama = dialogView.findViewById<View>(R.id.etNama) as EditText
        etKeterangan = dialogView.findViewById<View>(R.id.etKeterangan) as EditText
        etHarga = dialogView.findViewById<View>(R.id.etHarga) as EditText
        btnSimpan = dialogView.findViewById<View>(R.id.btnSimpan) as Button
        ivGambar = dialogView.findViewById<View>(R.id.ivGambar) as ImageView
        ivTambahFoto = dialogView.findViewById<View>(R.id.ivTambahFoto) as ImageView
        val spiIsPajak = dialogView.findViewById<View>(R.id.spiIsPajak) as Spinner
        val spiTipeProduk = dialogView.findViewById<View>(R.id.spiTipeProduk) as Spinner

        spiIsPajak.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                slctdIspajak = isPajakList[position].idItem
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        spiTipeProduk.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                slctdTipeProduk = tipeProdukList[position].idItem
                if(slctdTipeProduk == "3"){
                    slctdIspajak = "0"
                    spiIsPajak.setSelection(1)
                } else {
                    slctdIspajak = "1"
                    spiIsPajak.setSelection(0)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        val spinIsPajakAdapter = IsPajakSpinAdapter(
            requireContext(),
            R.layout.item_spi_ispajak,
            isPajakList
        )

        spiIsPajak.adapter = spinIsPajakAdapter

        val spinTipeProdukAdapter = TipeProdukSpinAdapter(
            requireContext(),
            R.layout.item_spi_tipe_produk,
            tipeProdukList
        )

        spiTipeProduk.adapter = spinTipeProdukAdapter

        btnSimpan.setOnClickListener {

            e_nama = etNama.text.toString()
            e_harga = etHarga.text.toString().replace(".", "")
            e_ket = etKeterangan.text.toString()
            when {
                e_nama!!.trim { it <= ' ' }.equals("", ignoreCase = true) -> {
                    etNama.requestFocus()
                    etNama.error = "Silahkan isi form ini !"
                }
                e_harga!!.trim { it <= ' ' }.equals("", ignoreCase = true) -> {
                    etHarga.requestFocus()
                    etHarga.error = "Silahkan isi form ini !"
                }
                e_ket!!.trim { it <= ' ' }.equals("", ignoreCase = true) -> {
                    etKeterangan.requestFocus()
                    etKeterangan.error = "Silahkan isi form ini !"
                }
                t_nama_file.trim { it <= ' ' }.equals("", ignoreCase = true) -> {
                    Toast.makeText(requireContext(), "Silahkan pilih gambar !", Toast.LENGTH_SHORT)
                        .show()
                }
                else -> {
                    if (CheckNetwork().checkingNetwork(requireContext())) {
                        tambahData()
                    }
                    dialogBuilder.dismiss()
                }
            }
        }
        ivTambahFoto.setOnClickListener {
            wallpaperDirectory =
                File(Environment.getExternalStorageDirectory().toString() + IMAGE_DIRECTORY)
            if (!wallpaperDirectory!!.exists()) {  // have the object build the directory structure, if needed.
                wallpaperDirectory!!.mkdirs()
            }
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("Pilihan Tambah Foto")
                .setPositiveButton("Galeri") { dialog, id ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED
                        ) {
                            requestPermissions(
                                arrayOf(
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ),
                                MY_CAMERA_PERMISSION_CODE
                            )
                            //showFileChooser();
                        } else {
                            wallpaperDirectory = File(
                                Environment.getExternalStorageDirectory()
                                    .toString() + IMAGE_DIRECTORY
                            )
                            if (!wallpaperDirectory!!.exists()) {  // have the object build the directory structure, if needed.
                                wallpaperDirectory!!.mkdirs()
                            }
                            showFileChooser()
                            status = "t"
                        }
                    } else {
                        wallpaperDirectory = File(
                            Environment.getExternalStorageDirectory().toString() + IMAGE_DIRECTORY
                        )
                        if (!wallpaperDirectory!!.exists()) {  // have the object build the directory structure, if needed.
                            wallpaperDirectory!!.mkdirs()
                        }
                        showFileChooser()
                        status = "t"
                    }
                }
                .setNegativeButton("Kamera") { dialog, id ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED
                        ) {
                            requestPermissions(
                                arrayOf(
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ),
                                MY_CAMERA_PERMISSION_CODE
                            )
                            //showFileChooser();
                        } else {
                            wallpaperDirectory = File(
                                Environment.getExternalStorageDirectory()
                                    .toString() + IMAGE_DIRECTORY
                            )
                            if (!wallpaperDirectory!!.exists()) {  // have the object build the directory structure, if needed.
                                wallpaperDirectory!!.mkdirs()
                            }
                            val cal = Calendar.getInstance()
                            val sdf = SimpleDateFormat("ddMMyyHHmmss", Locale.getDefault())
                            tempNameFile = "Cam_" + sdf.format(cal.time) + ".jpg"
                            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            val f = File(wallpaperDirectory, tempNameFile)
                            val photoURI = FileProvider.getUriForFile(
                                requireContext(),
                                BuildConfig.APPLICATION_ID + ".provider",
                                f
                            )
                            //cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                            startActivityForResult(cameraIntent, CAMERA_REQUEST)
                            status = "t"
                        }
                    } else {
                        wallpaperDirectory = File(
                            Environment.getExternalStorageDirectory().toString() + IMAGE_DIRECTORY
                        )
                        if (!wallpaperDirectory!!.exists()) {  // have the object build the directory structure, if needed.
                            wallpaperDirectory!!.mkdirs()
                        }
                        val cal = Calendar.getInstance()
                        val sdf = SimpleDateFormat("ddMMyyHHmmss", Locale.getDefault())
                        tempNameFile = "Cam_" + sdf.format(cal.time) + ".jpg"
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        val f = File(wallpaperDirectory, tempNameFile)
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f))
                        startActivityForResult(intent, CAMERA_REQUEST)
                        status = "t"
                    }
                }
            val alert = builder.create()
            alert.show()
        }
        etHarga.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                // TODO Auto-generated method stub
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                // TODO Auto-generated method stub
            }

            override fun afterTextChanged(s: Editable) {
                etHarga.removeTextChangedListener(this)
                try {
                    var originalString = s.toString()
                    if (originalString.contains(".")) {
                        originalString = originalString.replace(".", "")
                    }
                    val longval = originalString.toLong()
                    val formatter = NumberFormat.getInstance(Locale.US) as DecimalFormat
                    formatter.applyPattern("#,###,###,###")
                    val formattedString = formatter.format(longval)

                    //setting text after format to EditText
                    etHarga.setText(formattedString.replace(",", "."))
                    etHarga.setSelection(etHarga.text.length)
                } catch (nfe: NumberFormatException) {
                    nfe.printStackTrace()
                }
                etHarga.addTextChangedListener(this)
            }
        })
        dialogBuilder.setView(dialogView)
        dialogBuilder.show()
    }

    private fun tambahData() {
        class TambahData : AsyncTask<Void?, Int?, String?>() {
            //ProgressDialog uploading;
            override fun onPreExecute() {
                super.onPreExecute()
                progressdialog = ProgressDialog(requireContext())
                progressdialog!!.setCancelable(false)
                progressdialog!!.setMessage("Upload data ke server ...")
                progressdialog!!.show()
                //uploading = ProgressDialog.show(SinkronActivity.this, "Mengirim data ke Server", "Mohon Tunggu...", false, false);
            }

            override fun onPostExecute(s: String?) {
                super.onPostExecute(s)
                Log.d("HASIL", s!!)
                when {
                    s.equals("sukses", ignoreCase = true) -> {
//                        databaseHandler!!.insert_produk(
//                            com.dnhsolution.restokabmalang.database.ItemProduk(
//                                0, idTmpUsaha, e_nama, e_harga, e_ket, t_nama_file, "1",slctdIspajak,slctdTipeProduk
//                            )
//                        )
                        if (progressdialog!!.isShowing) progressdialog!!.dismiss()
                        Toast.makeText(
                            requireContext(),
                            "Data berhasil ditambah !",
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(Intent(requireContext(), ProdukMasterActivity::class.java))
                        activity?.finish()
                    }
                    s.equals("gagal", ignoreCase = true) -> {
                        if (progressdialog!!.isShowing) progressdialog!!.dismiss()
                        Toast.makeText(requireContext(), "Data gagal ditambah !", Toast.LENGTH_SHORT)
                            .show()
                    }
                    else -> {
                        if (progressdialog!!.isShowing) progressdialog!!.dismiss()
                        Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun doInBackground(vararg p0: Void?): String? {
//                if (databaseHandler == null) {
//                    Log.d("DATABASE_INSERT", "gagal")
//                } else {
//                    Log.d("DATABASE_INSERT", "berhasil")
//                }
                val u = UploadData()
                var msg: String? = null
                //                String id_tmp_usaha = sharedPreferences.getString(Url.SESSION_ID_TEMPAT_USAHA,"");
                msg = u.uploadDataBaru(idPengguna,e_nama, e_ket, e_harga, t_nama_file, idTmpUsaha,slctdIspajak,slctdTipeProduk)
                return msg
            }
        }

        val uv = TambahData()
        uv.execute()
    }

    override fun hapusProdukMasterOnTask(tipe: String,posisi: Int) {
        if (!CheckNetwork().checkingNetwork((context)!!)) return

        val idBarang = itemProduks[posisi].id_barang
            if(tipe == "1") {
                val url_image = itemProduks[posisi].url_image
                val nama_barang = itemProduks[posisi].nama_barang
                val harga = itemProduks[posisi].harga
                val ket = itemProduks[posisi].keterangan
                val isPajak = itemProduks[posisi].isPajak
                val jenisProduk = itemProduks[posisi].jenisProduk

                dialogEdit(url_image, nama_barang, idBarang, harga, ket, isPajak, jenisProduk)
            }else {
                databaseHandler?.hapusByIdProduk(idBarang)
                hapusFungsi(idBarang)
            }
    }
}