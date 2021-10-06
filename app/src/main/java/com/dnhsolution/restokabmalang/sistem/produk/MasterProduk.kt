package com.dnhsolution.restokabmalang.sistem.produk

import android.Manifest
import com.dnhsolution.restokabmalang.MainActivity.Companion.adMasterProduk
import androidx.appcompat.app.AppCompatActivity
import android.content.SharedPreferences
import androidx.recyclerview.widget.RecyclerView
import android.app.ProgressDialog
import com.dnhsolution.restokabmalang.database.DatabaseHandler
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.os.Bundle
import com.dnhsolution.restokabmalang.utilities.Url
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.sistem.produk.ui.main.SectionsPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.dnhsolution.restokabmalang.MainActivity
import androidx.core.content.ContextCompat
import com.dnhsolution.restokabmalang.utilities.CheckNetwork
import android.content.DialogInterface
import android.os.Environment
import com.dnhsolution.restokabmalang.sistem.produk.MasterProduk
import android.os.Build
import android.content.pm.PackageManager
import android.content.Intent
import android.provider.MediaStore
import androidx.core.content.FileProvider
import android.text.TextWatcher
import android.text.Editable
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionRequestErrorListener
import com.karumi.dexter.listener.DexterError
import android.content.ActivityNotFoundException
import android.app.Activity
import android.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestListener
import android.graphics.drawable.Drawable
import android.net.Uri
import com.bumptech.glide.load.engine.GlideException
import android.os.AsyncTask
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.target.Target
import com.dnhsolution.restokabmalang.BuildConfig
import com.dnhsolution.restokabmalang.sistem.produk.server.IsPajakListElement
import com.dnhsolution.restokabmalang.sistem.produk.server.IsPajakSpinAdapter
import com.dnhsolution.restokabmalang.sistem.produk.server.TipeProdukListElement
import com.dnhsolution.restokabmalang.sistem.produk.server.TipeProdukSpinAdapter
import com.karumi.dexter.listener.PermissionRequest
import java.io.*
import java.lang.NumberFormatException
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class MasterProduk : AppCompatActivity() {
    lateinit var sharedPreferences: SharedPreferences
    var rvProduk: RecyclerView? = null
    private val adapterProduk: AdapterProduk? = null
    private val itemProduks: List<ItemProduk> = ArrayList()
    var RecyclerViewClickedItemPos = 0
    var ChildView: View? = null
    var tempNameFile = "POSRestoran.jpg"
    private var filePath: Uri? = null
    private val destFile: File? = null
    private val dateFormatter: SimpleDateFormat? = null
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
    var t_nama: String? = null
    var t_harga: String? = null
    var t_ket: String? = null
    var t_id: String? = null
    var tvKet: TextView? = null
    var databaseHandler: DatabaseHandler? = null
    var fab: FloatingActionButton? = null
    var tv_count: TextView? = null
    var jml_data = 0
    private var toolbar: Toolbar? = null
    private var menuTemp: Menu? = null
    private var statusJaringan = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences(Url.SESSION_NAME, MODE_PRIVATE)
        val label = sharedPreferences.getString(Url.setLabel, "Belum disetting")
        val tema = sharedPreferences.getString(Url.setTema, "0")
        if (tema.equals("0", ignoreCase = true)) {
            this@MasterProduk.setTheme(R.style.Theme_First)
        } else if (tema.equals("1", ignoreCase = true)) {
            this@MasterProduk.setTheme(R.style.Theme_Second)
        } else if (tema.equals("2", ignoreCase = true)) {
            this@MasterProduk.setTheme(R.style.Theme_Third)
        } else if (tema.equals("3", ignoreCase = true)) {
            this@MasterProduk.setTheme(R.style.Theme_Fourth)
        } else if (tema.equals("4", ignoreCase = true)) {
            this@MasterProduk.setTheme(R.style.Theme_Fifth)
        } else if (tema.equals("5", ignoreCase = true)) {
            this@MasterProduk.setTheme(R.style.Theme_Sixth)
        }
        databaseHandler = DatabaseHandler(this)
        setContentView(R.layout.activity_master_produk)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle(label)
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager = findViewById<ViewPager>(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs = findViewById<TabLayout>(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        fab = findViewById(R.id.fab)
        tv_count = findViewById<View>(R.id.text_count) as TextView
        requestMultiplePermissions()
        if (adMasterProduk == 1) return

//        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
//        alertDialog.setTitle("Tutorial");
//        alertDialog.setMessage("1. Tap tombol tambah kanan atas untuk menambahkan produk\n\n" +
//                "Icon panah kanan & kiri hijau menandakan status data produk sudah tersinkron.\n" +
//                "Icon refresh kuning menandakan status data produk butuh disinkron.");
//        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
//                (dialog, which) -> dialog.dismiss());
//        alertDialog.show();
        adMasterProduk = 1
    }

    override fun onResume() {
        super.onResume()
        val label = sharedPreferences.getString(Url.setLabel, "Belum disetting")
        supportActionBar!!.title = label
        val tema = sharedPreferences.getString(Url.setTema, "0")
        if (tema.equals("0", ignoreCase = true)) {
            this@MasterProduk.setTheme(R.style.Theme_First)
        } else if (tema.equals("1", ignoreCase = true)) {
            this@MasterProduk.setTheme(R.style.Theme_Second)
        } else if (tema.equals("2", ignoreCase = true)) {
            this@MasterProduk.setTheme(R.style.Theme_Third)
        } else if (tema.equals("3", ignoreCase = true)) {
            this@MasterProduk.setTheme(R.style.Theme_Fourth)
        } else if (tema.equals("4", ignoreCase = true)) {
            this@MasterProduk.setTheme(R.style.Theme_Fifth)
        } else if (tema.equals("5", ignoreCase = true)) {
            this@MasterProduk.setTheme(R.style.Theme_Sixth)
        }
    }

    fun gantiIconWifi(value: Boolean) {
        if (value) {
            menuTemp!!.getItem(2).icon =
                ContextCompat.getDrawable(this, R.drawable.ic_baseline_wifi_24_green)
            statusJaringan = 1
        } else {
            menuTemp!!.getItem(2).icon =
                ContextCompat.getDrawable(this, R.drawable.ic_baseline_wifi_24_gray)
            statusJaringan = 0
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_master, menu)
        menuTemp = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_menu_lanjut) {
            if (CheckNetwork().checkingNetwork(this) && statusJaringan == 1) dialogTambah() else Toast.makeText(
                this,
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

    private fun tampilAlertDialogTutorial() {
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setMessage(
            """1. Saat ada icon refresh warna
    kuning dimasing-masing daftar
    produk, menandakan jika produk
    diload dari peralatan lokal.
2. Saat ada icon panah kanan kiri
    warna hijau dimasing-masing
    daftar produk, menandakan jika
    produk tersinkron dengan server.
3. Tombol icon (+) samping icon [?]
    di kanan atas untuk mulai
    transaksi dengan produk yang
    dipilih."""
        )
        alertDialog.setButton(
            AlertDialog.BUTTON_NEGATIVE, "OK"
        ) { dialog, which -> dialog.dismiss() }
        alertDialog.show()
    }

    private val isPajakList: ArrayList<IsPajakListElement>
        get(){
            val isPajak = ArrayList<IsPajakListElement>()
            isPajak.add(IsPajakListElement("1","Pajak"))
            isPajak.add(IsPajakListElement("2","Non Pajak"))
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

    fun dialogTambah() {
        val dialogBuilder = AlertDialog.Builder(this).create()
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

        val spinIsPajakAdapter = IsPajakSpinAdapter(
                this,
                R.layout.item_spi_ispajak,
                isPajakList
            )

        spiIsPajak.adapter = spinIsPajakAdapter

        val spinTipeProdukAdapter = TipeProdukSpinAdapter(
                this,
                R.layout.item_spi_tipe_produk,
                tipeProdukList
            )

        spiTipeProduk.adapter = spinTipeProdukAdapter

        btnSimpan.setOnClickListener {
            t_nama = etNama.text.toString()
            t_harga = etHarga.text.toString().replace(".", "")
            t_ket = etKeterangan.text.toString()
            if (t_nama!!.trim { it <= ' ' }.equals("", ignoreCase = true)) {
                etNama.requestFocus()
                etNama.error = "Silahkan isi form ini !"
            } else if (t_harga!!.trim { it <= ' ' }.equals("", ignoreCase = true)) {
                etHarga.requestFocus()
                etHarga.error = "Silahkan isi form ini !"
            } else if (t_ket!!.trim { it <= ' ' }.equals("", ignoreCase = true)) {
                etKeterangan.requestFocus()
                etKeterangan.error = "Silahkan isi form ini !"
            } else if (t_nama_file.trim { it <= ' ' }.equals("", ignoreCase = true)) {
                Toast.makeText(this@MasterProduk, "Silahkan pilih gambar !", Toast.LENGTH_SHORT)
                    .show()
            } else {
                if (CheckNetwork().checkingNetwork(this@MasterProduk)) {
                    TambahData()
                }
                dialogBuilder.dismiss()
            }
        }
        ivTambahFoto.setOnClickListener {
            wallpaperDirectory =
                File(Environment.getExternalStorageDirectory().toString() + IMAGE_DIRECTORY)
            if (!wallpaperDirectory!!.exists()) {  // have the object build the directory structure, if needed.
                wallpaperDirectory!!.mkdirs()
            }
            val builder = AlertDialog.Builder(this@MasterProduk)
            builder.setMessage("Pilihan Tambah Foto")
                .setPositiveButton("Galeri") { dialog, id ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.CAMERA)
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
                        if (checkSelfPermission(Manifest.permission.CAMERA)
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
                                applicationContext,
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

    private fun requestMultiplePermissions() {
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {  // check if all permissions are granted
                        //Toast.makeText(getApplicationContext(), "All permissions are granted by user!", Toast.LENGTH_SHORT).show();
                    }
                    if (report.isAnyPermissionPermanentlyDenied) { // check for permanent denial of any permission
                        // show alert dialog navigating to Settings
                        showSettingsDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }

                fun onPermissionDenied(response: PermissionDeniedResponse) {
                    // check for permanent denial of permission
                    if (response.isPermanentlyDenied) {
                        showSettingsDialog()
                    }
                } //                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                //                        token.continuePermissionRequest();
                //                    }
            }).withErrorListener {
                Toast.makeText(
                    applicationContext,
                    "Some Error! ",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .onSameThread()
            .check()
    }

    private fun showSettingsDialog() {
        val builder = AlertDialog.Builder(this@MasterProduk)
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
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, 101)
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
                this, "Please install a File Manager.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_CANCELED) {
            return
        }
        if (requestCode == FILE_SELECT_CODE) {
            filePath = data!!.data
            val a = RealPathUtil.getRealPath(
                applicationContext, filePath
            )
            if (resultCode == RESULT_OK) {
                // Get the Uri of the selected file
                val uri = data.data
                Log.d("Foto", "File Uri: " + uri.toString())
                // Get the path
                Log.d("Foto", "File Path: $a")
                val sourceLocation = File(a)
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
                        .into(ivGambarBaru!!)
                    ivGambarBaru!!.visibility = View.VISIBLE
                    if (!e_nama_file.equals("", ignoreCase = true)) {
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
                        .into(ivGambar!!)
                    ivGambar!!.visibility = View.VISIBLE
                    if (!t_nama_file.equals("", ignoreCase = true)) {
                        val fl = File(t_nama_file)
                        val deleted = fl.delete()
                    }
                    t_nama_file = file.absolutePath
                }
            }
        } else if (requestCode == CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) {
                println("CAMERA_REQUEST1")
                //                        Bitmap photo = (Bitmap) data.getExtras().get("data");
                var f = File(wallpaperDirectory.toString())
                Log.d("File", f.toString())
                for (temp in f.listFiles()) {
                    if (temp.name == tempNameFile) {
                        f = temp
                        val filePhoto = File(wallpaperDirectory.toString(), tempNameFile)
                        val file_size = (filePhoto.length() / 1024).toString().toInt()
                        Log.d("PirangMB", file_size.toString())
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
                                .into(ivGambarBaru!!)
                            ivGambarBaru!!.visibility = View.VISIBLE
                            if (!e_nama_file.equals("", ignoreCase = true)) {
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
                                .into(ivGambar!!)
                            ivGambar!!.visibility = View.VISIBLE
                            if (!t_nama_file.equals("", ignoreCase = true)) {
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

    private fun TambahData() {
        class TambahData : AsyncTask<Void?, Int?, String?>() {
            //ProgressDialog uploading;
            override fun onPreExecute() {
                super.onPreExecute()
                progressdialog = ProgressDialog(this@MasterProduk)
                progressdialog!!.setCancelable(false)
                progressdialog!!.setMessage("Upload data ke server ...")
                progressdialog!!.show()
                //uploading = ProgressDialog.show(SinkronActivity.this, "Mengirim data ke Server", "Mohon Tunggu...", false, false);
            }

            override fun onPostExecute(s: String?) {
                super.onPostExecute(s)
                // uploading.dismiss();
                Log.d("HASIL", s!!)
                //Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show();
                // tvStatus.setText(s);
                //
                if (s.equals("sukses", ignoreCase = true)) {
                    if (progressdialog!!.isShowing) progressdialog!!.dismiss()
                    Toast.makeText(
                        this@MasterProduk,
                        "Data berhasil ditambah !",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(applicationContext, MasterProduk::class.java))
                    finish()
                } else if (s.equals("gagal", ignoreCase = true)) {
                    if (progressdialog!!.isShowing) progressdialog!!.dismiss()
                    Toast.makeText(this@MasterProduk, "Data gagal ditambah !", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    if (progressdialog!!.isShowing) progressdialog!!.dismiss()
                    Toast.makeText(this@MasterProduk, s, Toast.LENGTH_SHORT).show()
                }
            }

            override fun doInBackground(vararg p0: Void?): String? {
                val id_tmp_usaha = sharedPreferences.getString(Url.SESSION_ID_TEMPAT_USAHA, "")
                databaseHandler!!.insert_produk(
                    com.dnhsolution.restokabmalang.database.ItemProduk(
                        0, id_tmp_usaha, t_nama, t_harga, t_ket, t_nama_file, "1"
                    )
                )

                if (databaseHandler == null) {
                    Log.d("DATABASE_INSERT", "gagal")
                } else {
                    Log.d("DATABASE_INSERT", "berhasil")
                }
                val u = UploadData()
                var msg: String? = null
                //                String id_tmp_usaha = sharedPreferences.getString(Url.SESSION_ID_TEMPAT_USAHA,"");
                msg = u.uploadDataBaru(t_nama, t_ket, t_harga, t_nama_file, id_tmp_usaha)
                return msg
            }
        }

        val uv = TambahData()
        uv.execute()
    }

    companion object {
        private const val CAMERA_REQUEST = 1888
        private const val MY_CAMERA_PERMISSION_CODE = 100
        private const val FILE_SELECT_CODE = 5
        private const val IMAGE_DIRECTORY = "/POSRestoran"
    }
}