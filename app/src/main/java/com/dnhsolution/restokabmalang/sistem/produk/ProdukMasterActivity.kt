package com.dnhsolution.restokabmalang.sistem.produk

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.dnhsolution.restokabmalang.MainActivity
import com.dnhsolution.restokabmalang.MainActivity.Companion.adMasterProduk
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.database.AppRoomDatabase
import com.dnhsolution.restokabmalang.database.DatabaseHandler
import com.dnhsolution.restokabmalang.sistem.produk.tab_fragment.*
import com.dnhsolution.restokabmalang.transaksi.KategoriElement
import com.dnhsolution.restokabmalang.transaksi.KategoriListViewModel
import com.dnhsolution.restokabmalang.transaksi.KategoriListlement
import com.dnhsolution.restokabmalang.transaksi.tab_fragment.ProdukListFragment
import com.dnhsolution.restokabmalang.utilities.ManagePermissions
import com.dnhsolution.restokabmalang.utilities.PilihanAttachmentFragmentDialog
import com.dnhsolution.restokabmalang.utilities.Url
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class ProdukMasterActivity : AppCompatActivity() {

    private val _tag = javaClass.simpleName
    private lateinit var slctdJenisProduk: String
    private lateinit var slctdIspajak: String

    lateinit var sharedPreferences: SharedPreferences
    var ChildView: View? = null
    var tempNameFile = "POSRestoran.jpg"
    private var filePath: Uri? = null
    var wallpaperDirectory: File? = null
    var e_nama_file = ""
    var t_nama_file = ""
    var status = ""
    var ivGambarBaru: ImageView? = null
    var ivGambar: ImageView? = null
    var progressdialog: ProgressDialog? = null
    var t_nama: String? = null
    var t_harga: String? = null
    var t_ket: String? = null
    var databaseHandler: DatabaseHandler? = null
    private var toolbar: Toolbar? = null
    private val requestPermissionRequestStorageCode = 2
    private val requestCaptureImage:Int = 100
    private val requestPickImage = 1046
    private val PermissionsRequestCode = 123
    private lateinit var kategoriListViewModel: KategoriListViewModel
    val kategoriList = ArrayList<KategoriElement>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences(Url.SESSION_NAME, MODE_PRIVATE)
        idPengguna = sharedPreferences.getString(Url.SESSION_ID_PENGGUNA, "0")
        uuid = sharedPreferences.getString(Url.SESSION_UUID, "")
        val label = sharedPreferences.getString(Url.setLabel, "Belum disetting")
        val tema = sharedPreferences.getString(Url.setTema, "0")
        when {
            tema.equals("0", ignoreCase = true) -> {
                this@ProdukMasterActivity.setTheme(R.style.Theme_First)
            }
            tema.equals("1", ignoreCase = true) -> {
                this@ProdukMasterActivity.setTheme(R.style.Theme_Second)
            }
            tema.equals("2", ignoreCase = true) -> {
                this@ProdukMasterActivity.setTheme(R.style.Theme_Third)
            }
            tema.equals("3", ignoreCase = true) -> {
                this@ProdukMasterActivity.setTheme(R.style.Theme_Fourth)
            }
            tema.equals("4", ignoreCase = true) -> {
                this@ProdukMasterActivity.setTheme(R.style.Theme_Fifth)
            }
            tema.equals("5", ignoreCase = true) -> {
                this@ProdukMasterActivity.setTheme(R.style.Theme_Sixth)
            }
        }
        databaseHandler = DatabaseHandler(this)
        setContentView(R.layout.activity_master_produk)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = label

        val viewPager = findViewById<ViewPager2>(R.id.view_pager)
        val tabMain = findViewById<TabLayout>(R.id.tabs)

        kategoriListViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())[KategoriListViewModel::class.java]
        argTab = MainActivity.argTab
        val getAppDatabase = AppRoomDatabase.getAppDataBase(this)
        val a= getAppDatabase?.tblProdukKategoriDao()?.getAll()
        a?.observe(this) { it ->
            if(kategoriList.size == 0)
                for (b in it) {
                    Log.d(_tag, a.toString())
                    kategoriList.add(
                        KategoriElement(
                            b.idKategoriServer.toString(),
                            b.nama,
                            b.idTempatUsaha,
                            b.idPengguna
                        )
                    )
                }
            kategoriListViewModel.items.value = KategoriListlement(kategoriList)
            viewPager.adapter?.notifyDataSetChanged()
        }

        // Create the observer which updates the ui
        val kategoriListObserver = Observer<KategoriListlement>{ arg ->
            val fragmentAdapter = ScreenSlidePagerAdapter(this)
            viewPager.adapter = fragmentAdapter
            TabLayoutMediator(tabMain, viewPager) { tab, position ->
                tab.text = arg.list1[position].value1
            }.attach()
        }

        // Observe the live data, passing in this activity as the life cycle owner and the observer
        kategoriListViewModel.items.observe(this,kategoriListObserver)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                Log.e("onPageScrolled", position.toString())
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                Log.e("onPageScrollState", state.toString())
            }
        })

        val list = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        // Initialize a new instance of ManagePermissions class
        val managePermissions = ManagePermissions(this,list,PermissionsRequestCode)
        managePermissions.checkPermissions()

        if (adMasterProduk == 1) return
        adMasterProduk = 1
    }

    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = kategoriList.size
        override fun createFragment(position: Int): Fragment = ProdukMasterListFragment.newInstance(
            kategoriList[position].id)
    }
//    private class ViewPagerFragmentAdapter(fragmentActivity: FragmentActivity) :
//        FragmentStateAdapter(fragmentActivity) {
//
//        override fun createFragment(position: Int): Fragment = ProdukMasterListFragment.newInstance(
//            kategoriList[position].id)
//        override fun getItemCount(): Int = kategoriList.size
//    }

    override fun onResume() {
        super.onResume()
        uuid = sharedPreferences.getString(Url.SESSION_UUID, "")
    }

//    private fun openDialog() {
//        val alertDialog = AlertDialog.Builder(this@ProdukMasterActivity).create()
//        val rowList = layoutInflater.inflate(R.layout.dialog_tutorial, null)
//        val listView: ListView = rowList.findViewById(R.id.listView)
//        val tutorialArrayAdapter: AdapterWizard
//        val arrayList = ArrayList<ItemView>()
//        arrayList.add(
//            ItemView(
//                "1",
//                "Saat ada icon refresh warna kuning dimasing-masing daftar produk, menandakan jika produk diload dari peralatan lokal."
//            )
//        )
//        arrayList.add(
//            ItemView(
//                "2",
//                "Saat ada icon panah kanan kiri warna hijau dimasing-masing daftar produk, menandakan jika produk tersinkron dengan server."
//            )
//        )
//        arrayList.add(
//            ItemView(
//                "3",
//                "Tombol icon (+) samping icon [?] di kanan atas untuk mulai transaksi dengan produk yang dipilih."
//            )
//        )
//        tutorialArrayAdapter = AdapterWizard(this@ProdukMasterActivity, arrayList)
//        listView.adapter = tutorialArrayAdapter
//        alertDialog.setView(rowList)
//        alertDialog.show()
//    }

//    private val isPajakList: ArrayList<IsPajakListElement>
//        get(){
//            val isPajak = ArrayList<IsPajakListElement>()
//            isPajak.add(IsPajakListElement("1","Pajak"))
//            isPajak.add(IsPajakListElement("2","Non Pajak"))
//            return isPajak
//        }

//    private val tipeProdukList: ArrayList<TipeProdukListElement>
//        get(){
//            val tipeProduk = ArrayList<TipeProdukListElement>()
//            tipeProduk.add(TipeProdukListElement("1","Beverage"))
//            tipeProduk.add(TipeProdukListElement("2","Food"))
//            tipeProduk.add(TipeProdukListElement("3","Dll"))
//            return tipeProduk
//        }

//    fun dialogTambah() {
//        val dialogBuilder = AlertDialog.Builder(this).create()
//        val inflater = this.layoutInflater
//        val dialogView = inflater.inflate(R.layout.dialog_tambah_produk, null)
//        val etNama: EditText
//        val etKeterangan: EditText
//        val etHarga: EditText
//        val btnSimpan: Button
//        val ivTambahFoto: ImageView
//        etNama = dialogView.findViewById<View>(R.id.etNama) as EditText
//        etKeterangan = dialogView.findViewById<View>(R.id.etKeterangan) as EditText
//        etHarga = dialogView.findViewById<View>(R.id.etHarga) as EditText
//        btnSimpan = dialogView.findViewById<View>(R.id.btnSimpan) as Button
//        ivGambar = dialogView.findViewById<View>(R.id.ivGambar) as ImageView
//        ivTambahFoto = dialogView.findViewById<View>(R.id.ivTambahFoto) as ImageView
//        val spiIsPajak = dialogView.findViewById<View>(R.id.spiIsPajak) as Spinner
//        val spiTipeProduk = dialogView.findViewById<View>(R.id.spiTipeProduk) as Spinner
//
//        spiIsPajak.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                slctdIspajak = isPajakList[position].idItem
//            }
//            override fun onNothingSelected(parent: AdapterView<*>?) { }
//        }
//
//        spiTipeProduk.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                slctdJenisProduk = tipeProdukList[position].idItem
//            }
//            override fun onNothingSelected(parent: AdapterView<*>?) { }
//        }
//
//        val spinIsPajakAdapter = IsPajakSpinAdapter(
//                this,
//                R.layout.item_spi_ispajak,
//                isPajakList
//            )
//
//        spiIsPajak.adapter = spinIsPajakAdapter
//
//        val spinTipeProdukAdapter = TipeProdukSpinAdapter(
//                this,
//                R.layout.item_spi_tipe_produk,
//                kategoriList
//            )
//
//        spiTipeProduk.adapter = spinTipeProdukAdapter
//
//        btnSimpan.setOnClickListener {
//            t_nama = etNama.text.toString()
//            t_harga = etHarga.text.toString().replace(".", "")
//            t_ket = etKeterangan.text.toString()
//            if (t_nama!!.trim { it <= ' ' }.equals("", ignoreCase = true)) {
//                etNama.requestFocus()
//                etNama.error = "Silahkan isi form ini !"
//            } else if (t_harga!!.trim { it <= ' ' }.equals("", ignoreCase = true)) {
//                etHarga.requestFocus()
//                etHarga.error = "Silahkan isi form ini !"
//            } else if (t_ket!!.trim { it <= ' ' }.equals("", ignoreCase = true)) {
//                etKeterangan.requestFocus()
//                etKeterangan.error = "Silahkan isi form ini !"
//            } else if (t_nama_file.trim { it <= ' ' }.equals("", ignoreCase = true)) {
//                Toast.makeText(this@ProdukMasterActivity, "Silahkan pilih gambar !", Toast.LENGTH_SHORT)
//                    .show()
//            } else {
//                if (CheckNetwork().checkingNetwork(this@ProdukMasterActivity)) {
//                    TambahData()
//                }
//                dialogBuilder.dismiss()
//            }
//        }
//
//        ivTambahFoto.setOnClickListener {
//            requestPermissions("storage")
//        }
//
//        etHarga.addTextChangedListener(object : TextWatcher {
//            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//
//                // TODO Auto-generated method stub
//            }
//
//            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
//
//                // TODO Auto-generated method stub
//            }
//
//            override fun afterTextChanged(s: Editable) {
//                etHarga.removeTextChangedListener(this)
//                try {
//                    var originalString = s.toString()
//                    if (originalString.contains(".")) {
//                        originalString = originalString.replace(".", "")
//                    }
//                    val longval = originalString.toLong()
//                    val formatter = NumberFormat.getInstance(Locale.getDefault()) as DecimalFormat
//                    formatter.applyPattern("#,###,###,###")
//                    val formattedString = formatter.format(longval)
//
//                    //setting text after format to EditText
//                    etHarga.setText(formattedString.replace(",", "."))
//                    etHarga.setSelection(etHarga.text.length)
//                } catch (nfe: NumberFormatException) {
//                    nfe.printStackTrace()
//                }
//                etHarga.addTextChangedListener(this)
//            }
//        })
//        dialogBuilder.setView(dialogView)
//        dialogBuilder.show()
//    }

    private fun requestPermissions(text: String) {
        if(text == "storage") {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), requestPermissionRequestStorageCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == requestPermissionRequestStorageCode) {
            when {
                grantResults.isEmpty() -> {
                    // If user interaction was interrupted, the permission request is cancelled and you
                    // receive empty arrays.
                    Log.i(_tag, "User interaction was cancelled.")
                }
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    Log.i(_tag, "Permission granted.")
                    val ft = supportFragmentManager.beginTransaction()
                    val prev = supportFragmentManager.findFragmentByTag("dialog")
                    if (prev != null) {
                        ft.remove(prev)
                    }
//                  ft.addToBackStack(null)
                    val dialogFragment = PilihanAttachmentFragmentDialog()
                    dialogFragment.show(ft, "dialog")
                }
                else -> {
                }
            }
        }
    }

    fun openCameraIntent() {
        val values = ContentValues()
        val timeStamp =  SimpleDateFormat("yyyyMMdd_HHmmss",
            Locale.getDefault()).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        values.put(MediaStore.Images.Media.TITLE, imageFileName)
        values.put(MediaStore.Images.Media.DESCRIPTION, "POS Android")
        contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
//        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempImageUri)
        startActivityForResult(intent, requestCaptureImage)
    }

    // Trigger gallery selection for a photo
    fun onPickPhoto() {
        // Create intent for picking a photo from the gallery
        val intent = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(packageManager) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(intent, requestPickImage)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_CANCELED) {
            return
        }
        if (requestCode == requestPickImage) {
            filePath = data!!.data
            val a = RealPathUtil.getRealPath(
                applicationContext, filePath
            )
            if (resultCode == RESULT_OK) {
                // Get the Uri of the selected file
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
            }
        } else if (requestCode == requestCaptureImage) {
            if (resultCode == RESULT_OK) {
                println("CAMERA_REQUEST1")
                val pathFile = data?.getStringExtra("pathFile") ?: ""
                val filePhoto = File(pathFile)
                val file_size = (filePhoto.length() / 1024).toString().toInt()
                Log.d(_tag, file_size.toString())
                Glide.with(ivGambar!!.context).load(filePhoto)
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
                t_nama_file = pathFile
            }
        }
    }

//    private fun requestMultiplePermissions() {
//        Dexter.withActivity(this)
//            .withPermissions(
//                Manifest.permission.CAMERA,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.READ_EXTERNAL_STORAGE
//            )
//            .withListener(object : MultiplePermissionsListener {
//                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
//                    if (report.areAllPermissionsGranted()) {  // check if all permissions are granted
//                        //Toast.makeText(getApplicationContext(), "All permissions are granted by user!", Toast.LENGTH_SHORT).show();
//                    }
//                    if (report.isAnyPermissionPermanentlyDenied) { // check for permanent denial of any permission
//                        // show alert dialog navigating to Settings
//                        showSettingsDialog()
//                    }
//                }
//
//                override fun onPermissionRationaleShouldBeShown(
//                    permissions: List<PermissionRequest>,
//                    token: PermissionToken
//                ) {
//                    token.continuePermissionRequest()
//                }
//
//                fun onPermissionDenied(response: PermissionDeniedResponse) {
//                    // check for permanent denial of permission
//                    if (response.isPermanentlyDenied) {
//                        showSettingsDialog()
//                    }
//                } //                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
//                //                        token.continuePermissionRequest();
//                //                    }
//            }).withErrorListener {
//                Toast.makeText(
//                    applicationContext,
//                    "Some Error! ",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//            .onSameThread()
//            .check()
//    }

    private fun showSettingsDialog() {
        val builder = AlertDialog.Builder(this@ProdukMasterActivity)
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

//    private fun showFileChooser() {
//        val intent = Intent(Intent.ACTION_GET_CONTENT)
//        intent.type = "*/*"
//        intent.addCategory(Intent.CATEGORY_OPENABLE)
//        try {
//            startActivityForResult(
//                Intent.createChooser(intent, "Select a File to Upload"),
//                FILE_SELECT_CODE
//            )
//        } catch (ex: ActivityNotFoundException) {
//            // Potentially direct the user to the Market with a Dialog
//            Toast.makeText(
//                this, "Please install a File Manager.",
//                Toast.LENGTH_SHORT
//            ).show()
//        }
//    }



//    private fun TambahData() {
//        class TambahData : AsyncTask<Void?, Int?, String?>() {
//            //ProgressDialog uploading;
//            override fun onPreExecute() {
//                super.onPreExecute()
//                progressdialog = ProgressDialog(this@ProdukMasterActivity)
//                progressdialog!!.setCancelable(false)
//                progressdialog!!.setMessage("Upload data ke server ...")
//                progressdialog!!.show()
//                //uploading = ProgressDialog.show(SinkronActivity.this, "Mengirim data ke Server", "Mohon Tunggu...", false, false);
//            }
//
//            override fun onPostExecute(s: String?) {
//                super.onPostExecute(s)
//                // uploading.dismiss();
//                Log.d("HASIL", s!!)
//                //Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show();
//                // tvStatus.setText(s);
//                //
//                if (s.equals("sukses", ignoreCase = true)) {
//                    if (progressdialog!!.isShowing) progressdialog!!.dismiss()
//                    Toast.makeText(
//                        this@ProdukMasterActivity,
//                        "Data berhasil ditambah !",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    startActivity(Intent(applicationContext, ProdukMasterActivity::class.java))
//                    finish()
//                } else if (s.equals("gagal", ignoreCase = true)) {
//                    if (progressdialog!!.isShowing) progressdialog!!.dismiss()
//                    Toast.makeText(this@ProdukMasterActivity, "Data gagal ditambah !", Toast.LENGTH_SHORT)
//                        .show()
//                } else {
//                    if (progressdialog!!.isShowing) progressdialog!!.dismiss()
//                    Toast.makeText(this@ProdukMasterActivity, s, Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun doInBackground(vararg p0: Void?): String? {
//                val id_tmp_usaha = sharedPreferences.getString(Url.SESSION_ID_TEMPAT_USAHA, "")
//                databaseHandler!!.insert_produk(
//                    com.dnhsolution.restokabmalang.database.ItemProduk(
//                        0, id_tmp_usaha, t_nama, t_harga, t_ket, t_nama_file, "1",slctdIspajak,slctdJenisProduk
//                    )
//                )
//
//                if (databaseHandler == null) {
//                    Log.d("DATABASE_INSERT", "gagal")
//                } else {
//                    Log.d("DATABASE_INSERT", "berhasil")
//                }
//                val u = UploadData()
//                var msg: String? = null
//                //                String id_tmp_usaha = sharedPreferences.getString(Url.SESSION_ID_TEMPAT_USAHA,"");
//                msg = u.uploadDataBaru(idPengguna,uuid,t_nama, t_ket, t_harga, t_nama_file, id_tmp_usaha,slctdIspajak,slctdJenisProduk)
//                return msg
//            }
//        }
//
//        val uv = TambahData()
//        uv.execute()
//    }

    companion object {
        private const val CAMERA_REQUEST = 1888
        private const val MY_CAMERA_PERMISSION_CODE = 100
        private const val FILE_SELECT_CODE = 5
        private const val IMAGE_DIRECTORY = "/POSRestoran"
        var idPengguna: String? = null
        var uuid: String? = null
        private var argTab = arrayOf("")
    }
}