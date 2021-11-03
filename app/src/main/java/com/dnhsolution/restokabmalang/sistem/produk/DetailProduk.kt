package com.dnhsolution.restokabmalang.sistem.produk

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.content.SharedPreferences
import android.app.ProgressDialog
import android.os.Bundle
import com.dnhsolution.restokabmalang.utilities.Url
import com.dnhsolution.restokabmalang.R
import android.text.TextWatcher
import android.text.Editable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestListener
import android.graphics.drawable.Drawable
import com.bumptech.glide.load.engine.GlideException
import android.os.Environment
import android.content.DialogInterface
import android.os.Build
import android.content.pm.PackageManager
import android.content.Intent
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionRequestErrorListener
import com.karumi.dexter.listener.DexterError
import android.content.ActivityNotFoundException
import android.app.AlertDialog
import android.net.Uri
import android.os.AsyncTask
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.target.Target
import com.dnhsolution.restokabmalang.BuildConfig
import com.dnhsolution.restokabmalang.sistem.produk.server.IsPajakListElement
import com.dnhsolution.restokabmalang.sistem.produk.server.TipeProdukListElement
import com.karumi.dexter.listener.PermissionRequest
import java.io.*
import java.lang.NumberFormatException
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class DetailProduk() : AppCompatActivity() {
    lateinit var sharedPreferences: SharedPreferences
    var ivFotoLama: ImageView? = null
    var ivFotoBaru: ImageView? = null
    var btnSimpan: Button? = null
    var btnGanti: Button? = null
    var etNama: EditText? = null
    var etHarga: EditText? = null
    var etKeterangan: EditText? = null
    var url_image: String? = null
    var nama: String? = null
    var id: String? = null
    var harga: String? = null
    var ket: String? = null
    var tempNameFile = "POSRestoran.jpg"
    private var filePath: Uri? = null
    private val destFile: File? = null
    private val dateFormatter: SimpleDateFormat? = null
    var wallpaperDirectory: File? = null
    var nama_file = ""
    var progressdialog: ProgressDialog? = null

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

    private lateinit var slctdTipeProduk: String
    private lateinit var slctdIspajak: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences(Url.SESSION_NAME, MODE_PRIVATE)
        val label = sharedPreferences.getString(Url.setLabel, "Belum disetting")
        val tema = sharedPreferences.getString(Url.setTema, "0")
        if (tema.equals("0", ignoreCase = true)) {
            this@DetailProduk.setTheme(R.style.Theme_First)
        } else if (tema.equals("1", ignoreCase = true)) {
            this@DetailProduk.setTheme(R.style.Theme_Second)
        } else if (tema.equals("2", ignoreCase = true)) {
            this@DetailProduk.setTheme(R.style.Theme_Third)
        } else if (tema.equals("3", ignoreCase = true)) {
            this@DetailProduk.setTheme(R.style.Theme_Fourth)
        } else if (tema.equals("4", ignoreCase = true)) {
            this@DetailProduk.setTheme(R.style.Theme_Fifth)
        } else if (tema.equals("5", ignoreCase = true)) {
            this@DetailProduk.setTheme(R.style.Theme_Sixth)
        }
        setContentView(R.layout.activity_detail_produk)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle(label)
        ivFotoBaru = findViewById<View>(R.id.ivFotoBaru) as ImageView
        ivFotoLama = findViewById<View>(R.id.ivFotoLama) as ImageView
        btnGanti = findViewById<View>(R.id.btnGantiFoto) as Button
        btnSimpan = findViewById<View>(R.id.btnSimpan) as Button
        etNama = findViewById<View>(R.id.etNama) as EditText
        etHarga = findViewById<View>(R.id.etHarga) as EditText
        etKeterangan = findViewById<View>(R.id.etKeterangan) as EditText
        val spiIsPajak = findViewById<View>(R.id.spiIsPajak) as Spinner
        val spiTipeProduk = findViewById<View>(R.id.spiTipeProduk) as Spinner

        spiIsPajak.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                slctdIspajak = isPajakList[position].idItem
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        spiTipeProduk.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                slctdTipeProduk = tipeProdukList[position].idItem
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        url_image = intent.getStringExtra("url_image")
        nama = intent.getStringExtra("nama_barang")
        id = intent.getStringExtra("id_barang")
        harga = intent.getStringExtra("harga")
        ket = intent.getStringExtra("ket")
        requestMultiplePermissions()
        var originalString = harga
        val longval: Long
        if (originalString!!.contains(".")) {
            originalString = originalString.replace(".", "")
        }
        longval = originalString.toLong()
        val formatter = NumberFormat.getInstance(Locale.US) as DecimalFormat
        formatter.applyPattern("#,###,###,###")
        val formattedString = formatter.format(longval)

        //setting text after format to EditText
        etHarga!!.setText(formattedString.replace(",", "."))
        etHarga!!.setSelection(etHarga!!.text.length)
        etNama!!.setText(nama)
        etKeterangan!!.setText(ket)
        etHarga!!.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                // TODO Auto-generated method stub
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                // TODO Auto-generated method stub
            }

            override fun afterTextChanged(s: Editable) {
                etHarga!!.removeTextChangedListener(this)
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
                    etHarga!!.setText(formattedString.replace(",", "."))
                    etHarga!!.setSelection(etHarga!!.text.length)
                } catch (nfe: NumberFormatException) {
                    nfe.printStackTrace()
                }
                etHarga!!.addTextChangedListener(this)
                // TODO Auto-generated method stub
            }
        })

        Glide.with(ivFotoLama!!.context).load(Url.serverFoto + url_image)
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
            .into((ivFotoLama)!!)
        btnGanti!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                wallpaperDirectory =
                    File(Environment.getExternalStorageDirectory().toString() + IMAGE_DIRECTORY)
                if (!wallpaperDirectory!!.exists()) {  // have the object build the directory structure, if needed.
                    wallpaperDirectory!!.mkdirs()
                }
                val builder = AlertDialog.Builder(this@DetailProduk)
                builder.setMessage("Pilihan Tambah Foto")
                    .setPositiveButton("Galeri", DialogInterface.OnClickListener { dialog, id ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if ((checkSelfPermission(Manifest.permission.CAMERA)
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
                        }
                    })
                    .setNegativeButton("Kamera", object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, id: Int) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if ((checkSelfPermission(Manifest.permission.CAMERA)
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
                                        applicationContext,
                                        BuildConfig.APPLICATION_ID + ".provider",
                                        f
                                    )
                                    //cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                                    startActivityForResult(cameraIntent, CAMERA_REQUEST)
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
                            }
                        }
                    })
                val alert = builder.create()
                alert.show()
            }
        })
        btnSimpan!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                SendData()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val label = sharedPreferences!!.getString(Url.setLabel, "Belum disetting")
        val tema = sharedPreferences!!.getString(Url.setTema, "0")
        if (tema.equals("0", ignoreCase = true)) {
            this@DetailProduk.setTheme(R.style.Theme_First)
        } else if (tema.equals("1", ignoreCase = true)) {
            this@DetailProduk.setTheme(R.style.Theme_Second)
        } else if (tema.equals("2", ignoreCase = true)) {
            this@DetailProduk.setTheme(R.style.Theme_Third)
        } else if (tema.equals("3", ignoreCase = true)) {
            this@DetailProduk.setTheme(R.style.Theme_Fourth)
        } else if (tema.equals("4", ignoreCase = true)) {
            this@DetailProduk.setTheme(R.style.Theme_Fifth)
        } else if (tema.equals("5", ignoreCase = true)) {
            this@DetailProduk.setTheme(R.style.Theme_Sixth)
        }
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
            }).withErrorListener(object : PermissionRequestErrorListener {
                override fun onError(error: DexterError) {
                    Toast.makeText(applicationContext, "Some Error! ", Toast.LENGTH_SHORT).show()
                }
            })
            .onSameThread()
            .check()
    }

    private fun showSettingsDialog() {
        val builder = AlertDialog.Builder(this@DetailProduk)
        builder.setTitle("Perizian dibutuhkan !")
        builder.setMessage("Aplikasi ini membutuhkan perizinan untuk akses beberapa feature. Anda dapat mengatur di Pengaturan Aplikasi.")
        builder.setPositiveButton("Pengaturan", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, which: Int) {
                dialog.cancel()
                openSettings()
            }
        })
        builder.setNegativeButton("Batal", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, which: Int) {
                dialog.cancel()
            }
        })
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
                Glide.with(ivFotoBaru!!.context).load(File(file.absolutePath).toString())
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
                    .into((ivFotoBaru)!!)
                if (nama_file.equals("", ignoreCase = true)) {
                } else {
                    val fl = File(nama_file)
                    val deleted = fl.delete()
                }
                nama_file = file.absolutePath

//                Berkas berkas = new Berkas(file.getAbsolutePath(), file_size);
//                berkasList.add(berkas);
//                bAdapter.notifyDataSetChanged();
            }
        } else if (requestCode == CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) {
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
                        Glide.with(ivFotoBaru!!.context).load(File(f.absolutePath).toString())
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
                            .into((ivFotoBaru)!!)
                        if (nama_file.equals("", ignoreCase = true)) {
                        } else {
                            val fl = File(nama_file)
                            val deleted = fl.delete()
                        }
                        nama_file = f.absolutePath

//                        Berkas berkas = new Berkas(f.getAbsolutePath(), file_size);
//                        berkasList.add(berkas);
//                        //gridberkasList.add(berkas);
//
//                        bAdapter.notifyDataSetChanged();
                        //gbAdapter.notifyDataSetChanged();
                        break
                    }
                }
            }
        }
    }

    private fun SendData() {
        class SendData() : AsyncTask<Void?, Int?, String?>() {
            //ProgressDialog uploading;
            override fun onPreExecute() {
                super.onPreExecute()
                progressdialog = ProgressDialog(this@DetailProduk)
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
                    Toast.makeText(
                        this@DetailProduk,
                        "Data berhasil diupdate !",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this@DetailProduk, ProdukMasterActivity::class.java))
                    finish()
                } else if (s.equals("gagal", ignoreCase = true)) {
                    if (progressdialog!!.isShowing) progressdialog!!.dismiss()
                    Toast.makeText(this@DetailProduk, "Data gagal diupdate !", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    if (progressdialog!!.isShowing) progressdialog!!.dismiss()
                    Toast.makeText(this@DetailProduk, s, Toast.LENGTH_SHORT).show()
                }
            }

            override fun doInBackground(vararg p0: Void?): String? {
                val u = UploadData()
                var msg: String? = null
                msg = u.uploadDataUmum(
                    etNama!!.text.toString(), etKeterangan!!.text.toString(),
                    etHarga!!.text.toString().replace(".", ""), id, url_image, nama_file,
                    slctdIspajak,slctdTipeProduk
                )
                return msg
            }
        }

        val uv = SendData()
        uv.execute()
    }

    companion object {
        private val CAMERA_REQUEST = 1888
        private val MY_CAMERA_PERMISSION_CODE = 100
        private val FILE_SELECT_CODE = 5
        private val IMAGE_DIRECTORY = "/POSRestoran"
    }
}