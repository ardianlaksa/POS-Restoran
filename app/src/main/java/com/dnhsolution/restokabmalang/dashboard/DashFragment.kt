package com.dnhsolution.restokabmalang.dashboard

import android.Manifest
import com.dnhsolution.restokabmalang.MainActivity.Companion.jenisPajak
import com.dnhsolution.restokabmalang.MainActivity.Companion.adDashboard
import com.dnhsolution.restokabmalang.MainActivity.Companion.messageProgress
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import com.dnhsolution.restokabmalang.database.DatabaseHandler
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.MainActivity
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.dnhsolution.restokabmalang.utilities.dialog.AdapterWizard
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestListener
import android.graphics.drawable.Drawable
import com.bumptech.glide.load.engine.GlideException
import android.os.Environment
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.net.Uri
import android.os.Handler
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.target.Target
import com.dnhsolution.restokabmalang.BuildConfig
import com.dnhsolution.restokabmalang.databinding.FragmentDashboard2Binding
import com.dnhsolution.restokabmalang.sistem.produk.RealPathUtil
import com.dnhsolution.restokabmalang.sistem.produk.tab_fragment.IsPajakSpinAdapter
import com.dnhsolution.restokabmalang.sistem.produk.tab_fragment.ProdukMasterListFragment
import com.dnhsolution.restokabmalang.sistem.produk.tab_fragment.TipeProdukSpinAdapter
import com.dnhsolution.restokabmalang.transaksi.KategoriElement
import com.dnhsolution.restokabmalang.transaksi.KategoriListViewModel
import com.dnhsolution.restokabmalang.transaksi.KategoriListlement
import com.dnhsolution.restokabmalang.utilities.*
import com.dnhsolution.restokabmalang.utilities.dialog.ItemView
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.lang.NumberFormatException
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class DashFragment : Fragment() {
    private var sharedPreferences: SharedPreferences? = null
    private var db: SQLiteDatabase? = null
    private var batasSinkronAngka: String? = null
    private val _tag = javaClass.simpleName
    @JvmField
    var binding: FragmentDashboard2Binding? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        binding = FragmentDashboard2Binding.inflate(
            layoutInflater
        )
        //        return inflater.inflate(R.layout.fragment_dashboard2, parent, false);
        return binding!!.root
    }

    @JvmField
    var databaseHandler: DatabaseHandler? = null
    @JvmField
    var tvTrxTersimpan: TextView? = null
    @JvmField
    var tvBatas: TextView? = null

    @JvmField
    var tempNameFile = "Logo.jpg"
    private var filePath: Uri? = null
    @JvmField
    var wallpaperDirectory: File? = null
    @JvmField
    var t_nama_file = ""
    @JvmField
    var ivGambar: ImageView? = null
    @JvmField
    var ivInfo: ImageView? = null
    var alertDialog: AlertDialog? = null
    @JvmField
    var permissionRequestCode = 1
    @JvmField
    var mHandler: Handler? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        databaseHandler = DatabaseHandler(context)
        sharedPreferences = requireContext().getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)
        tvTrxTersimpan = view.findViewById(R.id.tvTransaksiTersimpan)
        tvBatas = view.findViewById(R.id.tvBatas)
        ivInfo = view.findViewById(R.id.ivInfo)
        val tvJudul = view.findViewById<TextView>(R.id.textView)
        var valueJudul = ""
        if (jenisPajak.equals("01", ignoreCase = true)) valueJudul = "POS Hotel"
        if (jenisPajak.equals("02", ignoreCase = true)) valueJudul = "POS Restoran"
        if (jenisPajak.equals("03", ignoreCase = true)) valueJudul = "POS Hiburan"
        tvJudul.text = valueJudul
        batasSinkronAngka = sharedPreferences?.getString(Url.SESSION_BATAS_WAKTU, "7")
        db = databaseHandler!!.readableDatabase
        batasSinkron()

        ivInfo?.setOnClickListener {
                tampilAlertDialogTutorial()
        }
        if (adDashboard == 1) return
        adDashboard = 1

//        registerReceiver()
        if(CheckNetwork().checkingNetwork(requireContext())) {
            getConfig()
        } else {
            Toast.makeText(context, getString(R.string.tidak_terkoneksi_internet), Toast.LENGTH_SHORT).show()
        }

    }

    private fun getConfig() {
        Log.d("PESAN", "GET CONFIG RUN")
        val queue = Volley.newRequestQueue(context)
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
                        MainActivity.versiTerbaru = json.getString("VERSI_TERBARU")
                        Log.d("PESAN", "SUCCESS GET DATA")
                        val sharedPreferences =
                            context?.getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)

                        //membuat editor untuk menyimpan data ke shared preferences
                        val editor = sharedPreferences?.edit()

                        //menambah data ke editor
                        editor?.putString(Url.SESSION_BATAS_WAKTU, batas)

                        //menyimpan data ke editor
                        editor?.apply()

                        val kodeVersi = BuildConfig.VERSION_CODE
                        val namaVersi = BuildConfig.VERSION_NAME
                        val gbgVersi = "$kodeVersi.$namaVersi".toFloat()
                        val versiTerbaru = MainActivity.versiTerbaru.toFloat()
                        if (checkPermission()) {
                            println("asdf $versiTerbaru $gbgVersi")
                            if(versiTerbaru > gbgVersi)
                                dialogUpdateApk()
                        } else {
                            requestPermission()
                        }
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

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun startDownload() {
        val intent = Intent(requireContext(), DownloadService::class.java)
        requireContext().startService(intent)
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            permissionRequestCode
        )
    }

//    private fun registerReceiver() {
//        val bManager = LocalBroadcastManager.getInstance(requireContext())
//        val intentFilter = IntentFilter()
//        intentFilter.addAction(messageProgress)
//        bManager.registerReceiver(broadcastReceiver, intentFilter)
//    }
//
//    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            if (intent.action == messageProgress) {
//                val download = intent.getParcelableExtra<Download>("download")
//                binding!!.progress.progress = download!!.progress
//                if (download.progress == 100) {
//                    binding!!.progressText.text = "File Download Complete"
//
//                    val destination = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}POS_Android_v1.43.apk"
//                    val contentUri = FileProvider.getUriForFile(
//                            context,BuildConfig.APPLICATION_ID + ".provider", File(destination)
//                    );
//                    val install = Intent(Intent.ACTION_VIEW)
//                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                    install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                    install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
//                                        install.setData(contentUri);
//                    context.startActivity(install)
//
////                    context.unregisterReceiver(this);
//                } else {
//                    binding!!.progressText.text = String.format(
//                        "Downloaded (%d/%d) MB",
//                        download.currentFileSize,
//                        download.totalFileSize
//                    )
//                }
//            }
//        }
//    }

    fun batasSinkron() {
        val jml_trx = databaseHandler!!.CountDataTersimpanUpload()
        tvTrxTersimpan!!.text = jml_trx.toString()
        val cTrx = db!!.rawQuery(
            "SELECT tanggal_trx FROM transaksi WHERE status='0' ORDER BY tanggal_trx ASC LIMIT 1",
            null
        )
        cTrx.moveToFirst()
        if (jml_trx > 0) {
            val tgl_trx = formatDate(cTrx.getString(0), "yyyyMMdd")
            tvBatas!!.text = getBatas(tgl_trx, batasSinkronAngka!!.toInt())
            cTrx.close()
        } else {
            tvBatas!!.text = resources.getString(R.string.tanggal_kosong)
        }
    }

    private fun tampilAlertDialogTutorial() {
        val alertDialog = AlertDialog.Builder(context).create()
        val rowList = layoutInflater.inflate(R.layout.dialog_tutorial, null)
        val listView = rowList.findViewById<ListView>(R.id.listView)
        val tutorialArrayAdapter: AdapterWizard
        val arrayList: ArrayList<ItemView> = ArrayList<ItemView>()
        arrayList.add(
            ItemView(
                "1",
                "Transaksi tersimpan : Transaksi yang dilakukan saat tidak ada koneksi."
            )
        )

        arrayList.add(
            ItemView(
                "2",
                "Batas Waktu Sinkron : Batasan waktu maksimal untuk upload transaksi saat tidak ada koneksi internet."
            )
        )
        tutorialArrayAdapter = AdapterWizard(requireContext(), arrayList)
        listView.adapter = tutorialArrayAdapter
        alertDialog.setView(rowList)
        alertDialog.show()
    }

    private fun formatDate(date: String, format: String): String {
        var formattedDate = ""
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        try {
            val parseDate = sdf.parse(date)
            formattedDate = SimpleDateFormat(format).format(parseDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return formattedDate
    }

    private fun getBatas(tanggal: String, days: Int): String {
        var dateInString = tanggal
        var sdf = SimpleDateFormat("yyyyMMdd")
        val c = Calendar.getInstance()
        try {
            c.time = sdf.parse(dateInString)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        c.add(Calendar.DATE, days)
        sdf = SimpleDateFormat("dd / MM / yyyy")
        val resultdate = Date(c.timeInMillis)
        dateInString = sdf.format(resultdate)
        return dateInString
    }

    private fun showSettingsDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Perizian dibutuhkan !")
        builder.setMessage("Aplikasi ini membutuhkan perizinan untuk akses beberapa feature. Anda dapat mengatur di Pengaturan Aplikasi.")
        builder.setPositiveButton("Pengaturan") { dialog, which ->
            dialog.cancel()
            openSettings()
        }
        builder.setNegativeButton("Batal") { dialog, which -> dialog.cancel() }
        builder.show()
    }

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireContext().packageName, null)
        intent.data = uri
        startActivityForResult(intent, 101)
    }

    fun DialogLogo() {
        val dialogBuilder = AlertDialog.Builder(context).create()
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_form_logo, null)
        val ivTambahGambar: ImageView
        val btnSimpan: Button
        btnSimpan = dialogView.findViewById<View>(R.id.btnSimpan) as Button
        ivGambar = dialogView.findViewById<View>(R.id.ivGambar) as ImageView
        ivTambahGambar = dialogView.findViewById<View>(R.id.ivTambahFoto) as ImageView
        btnSimpan.setOnClickListener {
            val editor = sharedPreferences!!.edit()

            //menambah data ke editor
            editor.putString(Url.SESSION_LOGO, t_nama_file)

            //menyimpan data ke editor
            editor.apply()
            val logo = sharedPreferences!!.getString(Url.SESSION_LOGO, "")
            if (!logo.equals("", ignoreCase = true)) {
                Toast.makeText(context, "Logo Berhasil ditambahkan !", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Logo Gagal ditambahkan !", Toast.LENGTH_SHORT).show()
            }
            startActivity(Intent(context, MainActivity::class.java))
            requireActivity().finish()
            dialogBuilder.dismiss()
        }
        val logo = sharedPreferences!!.getString(Url.SESSION_LOGO, "")
        if (!logo.equals("", ignoreCase = true)) {
            Glide.with(ivGambar!!.context).load(File(logo).toString())
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
                .into(ivGambar!!)
            ivGambar!!.visibility = View.VISIBLE
        }
        ivTambahGambar.setOnClickListener {
            wallpaperDirectory =
                File(Environment.getExternalStorageDirectory().toString() + IMAGE_DIRECTORY)
            if (!wallpaperDirectory!!.exists()) {  // have the object build the directory structure, if needed.
                wallpaperDirectory!!.mkdirs()
            }
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Pilihan Tambah Foto")
                .setPositiveButton("Galeri") { dialog, id ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (requireActivity().checkSelfPermission(Manifest.permission.CAMERA)
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
                        }
                    } else {
                        wallpaperDirectory = File(
                            Environment.getExternalStorageDirectory().toString() + IMAGE_DIRECTORY
                        )
                        if (!wallpaperDirectory!!.exists()) {  // have the object build the directory structure, if needed.
                            wallpaperDirectory!!.mkdirs()
                        }
                        showFileChooser()
                    }
                }
                .setNegativeButton("Kamera") { dialog, id ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (requireActivity().checkSelfPermission(Manifest.permission.CAMERA)
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
                            tempNameFile = "Logo_" + sdf.format(cal.time) + ".jpg"
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
                        tempNameFile = "Logo_" + sdf.format(cal.time) + ".jpg"
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        val f = File(wallpaperDirectory, tempNameFile)
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f))
                        startActivityForResult(intent, CAMERA_REQUEST)
                    }
                }
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
                Log.d("Foto", "File Path: $a")
                val sourceLocation = File(a)
                val cal = Calendar.getInstance()
                val sdf = SimpleDateFormat("ddMMyyHHmmss", Locale.getDefault())
                val filename = "Logo_" + sdf.format(cal.time) + ".jpg"
                val targetLocation = File(wallpaperDirectory.toString(), filename)
                if (sourceLocation.exists()) {
                    Log.v("Pesan", "Proses Pindah")
                    try {
                        val `in`: InputStream = FileInputStream(sourceLocation)
                        val out: OutputStream = FileOutputStream(targetLocation)
                        // Copy the bits from instream to outstream
                        val buf = ByteArray(1024)
                        var len: Int
                        while (`in`.read(buf).also { len = it } > 0) {
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
                    .into(ivGambar!!)
                ivGambar!!.visibility = View.VISIBLE
                t_nama_file = file.absolutePath
                val logo = sharedPreferences!!.getString(Url.SESSION_LOGO, "")
                val fl = File(logo)
                val deleted = fl.delete()
            }
        } else if (requestCode == CAMERA_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                println("CAMERA_REQUEST1")
                //                        Bitmap photo = (Bitmap) data.getExtras().get("data");
                var f = File(wallpaperDirectory.toString())
                Log.d("File", f.toString())
                for (temp in f.listFiles()) {
                    if (temp.name == tempNameFile) {
                        f = temp
                        val filePhoto = File(wallpaperDirectory.toString(), tempNameFile)
                        //pic = photo;
                        val file_size = (filePhoto.length() / 1024).toString().toInt()
                        Log.d("PirangMB", file_size.toString())
                        //tvFileName.setVisibility(View.VISIBLE);
                        // ivBerkas.setVisibility(View.VISIBLE);
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
                            .into(ivGambar!!)
                        ivGambar!!.visibility = View.VISIBLE
                        t_nama_file = f.absolutePath
                        val logo = sharedPreferences!!.getString(Url.SESSION_LOGO, "")
                        val fl = File(logo)
                        val deleted = fl.delete()
                        break
                    }
                }
            }
        }
    }

    private val m_Runnable: Runnable = object : Runnable {
        override fun run() {
            mHandler!!.postDelayed(this, 3000)
        }
    }

    override fun onPause() {
        super.onPause()
        mHandler!!.removeCallbacks(m_Runnable)
    }

    override fun onResume() {
        super.onResume()
        val jml_trx = databaseHandler!!.CountDataTersimpanUpload()
        tvTrxTersimpan!!.text = jml_trx.toString()
        mHandler = Handler()
        mHandler!!.postDelayed(m_Runnable, 3000)
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.supportActionBar!!.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_help, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_menu_bantuan) {
            tampilAlertDialogTutorial()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun dialogUpdateApk() {
        val dialogBuilder = AlertDialog.Builder(context).create()
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_update_apk, null)
        val btnYa: Button = dialogView.findViewById<View>(R.id.btnYa) as Button
        val btnTidak: Button = dialogView.findViewById<View>(R.id.btnTidak) as Button

        val apkUrl = Url.getDownloadApk
        val downloadController = DownloadController(requireContext(), apkUrl)

        btnYa.setOnClickListener {
            downloadController.enqueueDownload()
            dialogBuilder.dismiss()
        }

        btnTidak.setOnClickListener {
            dialogBuilder.dismiss()
        }

        dialogBuilder.setView(dialogView)
        dialogBuilder.show()
    }

    companion object {
        private const val CAMERA_REQUEST = 1888
        private const val MY_CAMERA_PERMISSION_CODE = 100
        private const val FILE_SELECT_CODE = 5
        private const val IMAGE_DIRECTORY = "/POSRestoran"
    }
}