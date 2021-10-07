package com.dnhsolution.restokabmalang.sistem.produk.server

import android.Manifest
import com.dnhsolution.restokabmalang.R
import androidx.recyclerview.widget.RecyclerView
import android.app.ProgressDialog
import com.dnhsolution.restokabmalang.database.DatabaseHandler
import com.dnhsolution.restokabmalang.utilities.Url
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import android.view.GestureDetector.SimpleOnGestureListener
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
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.android.volley.*
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.target.Target
import com.dnhsolution.restokabmalang.BuildConfig
import com.dnhsolution.restokabmalang.sistem.produk.*
import java.io.*
import java.lang.Exception
import java.lang.NumberFormatException
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ServerFragment() : Fragment() {
    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_server, parent, false)
    }

    private var slctdTipeProduk: String? = null
    private var slctdIspajak: String? = null
    private var isRunnerRunning: Boolean = false

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    var rvProduk: RecyclerView? = null
    private var adapterProduk: AdapterProduk? = null
    private val itemProduks: MutableList<ItemProduk> = ArrayList()
    var RecyclerViewClickedItemPos = 0
    var ChildView: View? = null
    var tvKet: TextView? = null
    private var idTmpUsaha: String? = "-1"
    private val _tag = javaClass.simpleName
    var tempNameFile = "POSRestoran.jpg"
    private var filePath: Uri? = null
    private val destFile: File? = null
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Setup any handles to view objects here
        // EditText etFoo = (EditText) view.findViewById(R.id.etFoo);
        val sharedPreferences = requireContext().getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)
        idTmpUsaha = sharedPreferences.getString(Url.SESSION_ID_TEMPAT_USAHA, "")
        databaseHandler = DatabaseHandler(context)
        rvProduk = view.findViewById<View>(R.id.rvProduk) as RecyclerView
        tvKet = view.findViewById<View>(R.id.tvKet) as TextView
        adapterProduk = AdapterProduk(itemProduks, context)
        val mLayoutManagerss: RecyclerView.LayoutManager = LinearLayoutManager(context)
        rvProduk!!.layoutManager = GridLayoutManager(context, 3)
        rvProduk!!.itemAnimator = DefaultItemAnimator()
        rvProduk!!.adapter = adapterProduk
        rvProduk!!.addOnItemTouchListener(object : OnItemTouchListener {
            var gestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
                override fun onSingleTapUp(motionEvent: MotionEvent): Boolean {
                    return true
                }
            })

            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                ChildView = rvProduk!!.findChildViewUnder(e.x, e.y)
                if (ChildView != null && gestureDetector.onTouchEvent(e)) {
                    RecyclerViewClickedItemPos = rvProduk!!.getChildAdapterPosition(ChildView!!)
                    val url_image = itemProduks[RecyclerViewClickedItemPos].url_image
                    val nama_barang = itemProduks[RecyclerViewClickedItemPos].nama_barang
                    val id_barang = itemProduks[RecyclerViewClickedItemPos].id_barang
                    val harga = itemProduks[RecyclerViewClickedItemPos].harga
                    val ket = itemProduks[RecyclerViewClickedItemPos].keterangan
                    val isPajak = itemProduks[RecyclerViewClickedItemPos].isPajak
                    val jenisProduk = itemProduks[RecyclerViewClickedItemPos].jenisProduk
                    dialogEdit(url_image, nama_barang, id_barang, harga, ket, isPajak, jenisProduk)
                    Log.d("TAG", RecyclerViewClickedItemPos.toString())
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
        if (CheckNetwork().checkingNetwork((context)!!)) {
            data
            tvKet!!.visibility = View.GONE
            isRunnerRunning = true
        } else {
            dataLokal
        }

        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                if(!isRunnerRunning) {
                    if(context == null) return
                    cekData
                    isRunnerRunning = true
                }
                mainHandler.postDelayed(this, 5000)
            }
        })
    }

    private val data: Unit
        get() {
            itemProduks.clear()
            adapterProduk!!.notifyDataSetChanged()
            val progressDialog = ProgressDialog(context)
            progressDialog.setMessage("Mencari data...")
            progressDialog.show()
            val queue = Volley.newRequestQueue(context)
            val sharedPreferences =
                requireContext().getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)
            val id_tempat_usaha = sharedPreferences.getString(Url.SESSION_ID_TEMPAT_USAHA, "0")
            Log.d("ID_TEMPAT_USAHA", (id_tempat_usaha)!!)
            val url = Url.serverPos + "getProduk?idTmpUsaha=" + id_tempat_usaha
            //Toast.makeText(WelcomeActivity.this, url, Toast.LENGTH_LONG).show();
            Log.i(_tag, url)
            val stringRequest: StringRequest =
                object : StringRequest(Method.GET, url, Response.Listener { response: String? ->
                    try {
                        val jsonObject: JSONObject = JSONObject(response)
                        val status: Int = jsonObject.getInt("success")
                        val jsonArray: JSONArray = jsonObject.getJSONArray("result")
                        val json: JSONObject = jsonArray.getJSONObject(0)
                        if (status == 1) {
                            var i: Int
                            i = 0
                            while (i < jsonArray.length()) {
                                try {
                                    val jO: JSONObject = jsonArray.getJSONObject(i)
                                    val id: ItemProduk = ItemProduk()
                                    val idBarang: String = jO.getString("ID_BARANG")
                                    id.id_barang = idBarang
                                    id.nama_barang = jO.getString("NM_BARANG")
                                    id.url_image = jO.getString("FOTO")
                                    id.harga = jO.getString("HARGA")
                                    id.keterangan = jO.getString("KETERANGAN")
                                    id.isPajak = jO.getString("ISPAJAK")
                                    id.jenisProduk = jO.getString("JENIS_PRODUK")
                                    id.status = true
                                    Log.d("NM_BARANG", jO.getString("NM_BARANG"))
                                    if (databaseHandler!!.CountDataProdukId(idBarang.toInt()) == 0) tambahDataLokal(
                                        id
                                    )
                                    itemProduks.add(id)
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                    progressDialog.dismiss()
                                }
                                i++
                            }
                            (activity as MasterProduk).gantiIconWifi(true)
                            isRunnerRunning = false
                        } else {
                            Toast.makeText(
                                getContext(),
                                "Jaringan masih sibuk !",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    //Toast.makeText(SinkronisasiActivity.this, response, Toast.LENGTH_SHORT).show();
                    adapterProduk!!.notifyDataSetChanged()
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

    private val cekData: Unit
        get() {
            val queue = Volley.newRequestQueue(context)
            Log.d("ID_TEMPAT_USAHA", (idTmpUsaha)!!)
            val url = Url.serverPos + "getProduk?idTmpUsaha=" + idTmpUsaha
            //Toast.makeText(WelcomeActivity.this, url, Toast.LENGTH_LONG).show();
            Log.i(_tag, url)
            val stringRequest: StringRequest =
                object : StringRequest(Method.GET, url, Response.Listener { response: String? ->
                    try {
                        val jsonObject = JSONObject(response)
                        val status: Int = jsonObject.getInt("success")
                        val jsonArray: JSONArray = jsonObject.getJSONArray("result")
                        if (status == 1) {
                            if(activity == null) return@Listener
                            (activity as MasterProduk).gantiIconWifi(true)
                            isRunnerRunning = false
                            return@Listener
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    (activity as MasterProduk).gantiIconWifi(false)
                    isRunnerRunning = false
                }, Response.ErrorListener { error: VolleyError ->
                    (activity as MasterProduk).gantiIconWifi(false)
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

    private val isPajakList: ArrayList<IsPajakListElement>
        get(){
            val isPajak = ArrayList<IsPajakListElement>()
            isPajak.add(IsPajakListElement("1","Pajak"))
            isPajak.add(IsPajakListElement("2","Tanpa Pajak"))
            return isPajak
        }

    private val tipeProdukList: ArrayList<TipeProdukListElement>
        get(){
            val tipeProduk = ArrayList<TipeProdukListElement>()
            tipeProduk.add(TipeProdukListElement("1","Beverage"))
            tipeProduk.add(TipeProdukListElement("2","Food"))
            tipeProduk.add(TipeProdukListElement("3","Dll"))
            return tipeProduk
        }

    fun dialogEdit(url_image: String, nama_barang: String?,
        id_barang: String?, harga: String, ket: String?, isPajak: String?, jenisProduk: String?) {
        slctdIspajak = null
        slctdTipeProduk = null

        val dialogBuilder = AlertDialog.Builder(context).create()
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_edit_produk, null)
        val etNama: EditText
        val etKeterangan: EditText
        val etHarga: EditText
        val btnSimpan: Button
        val ivGambarLama: ImageView
        val ivTambahGambar: ImageView
        etNama = dialogView.findViewById<View>(R.id.etNama) as EditText
        etKeterangan = dialogView.findViewById<View>(R.id.etKeterangan) as EditText
        etHarga = dialogView.findViewById<View>(R.id.etHarga) as EditText
        btnSimpan = dialogView.findViewById<View>(R.id.btnSimpan) as Button
        ivGambarLama = dialogView.findViewById<View>(R.id.ivGambarLama) as ImageView
        ivGambarBaru = dialogView.findViewById<View>(R.id.ivGambarBaru) as ImageView
        ivTambahGambar = dialogView.findViewById<View>(R.id.ivTambahGambar) as ImageView
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

        val spinTipeProdukAdapter = context?.let {
            TipeProdukSpinAdapter(
                it,
                R.layout.item_spi_tipe_produk,
                tipeProdukList
            )
        }

        spiTipeProduk.adapter = spinTipeProdukAdapter

        btnSimpan.setOnClickListener(View.OnClickListener {
            e_nama = etNama.text.toString()
            e_harga = etHarga.text.toString().replace(".", "")
            e_ket = etKeterangan.text.toString()
            e_id = id_barang
            e_gambar_lama = url_image
            if (e_nama!!.trim { it <= ' ' }.equals("", ignoreCase = true)) {
                etNama.requestFocus()
                etNama.error = "Silahkan isi form ini !"
            } else if (e_harga!!.trim { it <= ' ' }.equals("", ignoreCase = true)) {
                etHarga.requestFocus()
                etHarga.error = "Silahkan isi form ini !"
            } else if (e_ket!!.trim { it <= ' ' }.equals("", ignoreCase = true)) {
                etKeterangan.requestFocus()
                etKeterangan.error = "Silahkan isi form ini !"
            } else {
                updateData()
                dialogBuilder.dismiss()
            }
        })
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
        ivTambahGambar.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
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
        })
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
                if (s.equals("sukses", ignoreCase = true)) {
                    if (progressdialog!!.isShowing) progressdialog!!.dismiss()
                    Toast.makeText(context, "Data berhasil diupdate !", Toast.LENGTH_SHORT).show()
                    val fl = File(e_nama_file)
                    val deleted = fl.delete()
                    e_nama_file = ""
                    updateDataLokal()
                    data
                } else if (s.equals("gagal", ignoreCase = true)) {
                    if (progressdialog!!.isShowing) progressdialog!!.dismiss()
                    Toast.makeText(context, "Data gagal diupdate !", Toast.LENGTH_SHORT).show()
                } else {
                    if (progressdialog!!.isShowing) progressdialog!!.dismiss()
                    Toast.makeText(context, s, Toast.LENGTH_SHORT).show()
                }
            }

            override fun doInBackground(vararg p0: Void?): String? {
                val u = UploadData()
                var msg: String? = null
                msg = u.uploadDataUmum(e_nama, e_ket, e_harga, e_id, e_gambar_lama, e_nama_file
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
            adapterProduk!!.notifyDataSetChanged()
            val jml_data = databaseHandler!!.CountDataProduk()
            if (jml_data == 0) {
                tvKet!!.visibility = View.VISIBLE
            } else {
                tvKet!!.visibility = View.GONE
            }
            try {
                val listDataProduk: List<ItemProduk> = databaseHandler!!.dataProduk as List<ItemProduk>
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
            adapterProduk!!.notifyDataSetChanged()
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

    companion object {
        private val CAMERA_REQUEST = 1888
        private val MY_CAMERA_PERMISSION_CODE = 100
        private val FILE_SELECT_CODE = 5
        private val IMAGE_DIRECTORY = "/POSRestoran"
    }
}