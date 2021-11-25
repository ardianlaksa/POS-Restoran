package com.dnhsolution.restokabmalang.sistem.master

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.utilities.SistemMasterOnTask
import com.dnhsolution.restokabmalang.sistem.master.file_utilities.DeleteFileOnTask
import com.dnhsolution.restokabmalang.sistem.master.file_utilities.FileListAdapter
import com.dnhsolution.restokabmalang.sistem.master.file_utilities.FileListElement
import com.dnhsolution.restokabmalang.utilities.AddingIDRCurrency
import com.dnhsolution.restokabmalang.utilities.CheckNetwork
import com.dnhsolution.restokabmalang.utilities.RealPathUtil
import com.dnhsolution.restokabmalang.utilities.Url
import kotlinx.android.synthetic.main.activity_master_sistem.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SistemMasterActivity:AppCompatActivity(),DeleteFileOnTask,
    SistemMasterOnTask {

    private var idTmpUsaha: String? = null
    private val folderName = "POS_Resto"

    companion object {
        const val fileChooserCode = 11
        const val cameraIntentCode = 12
    }
    private var fileArray = ArrayList<FileListElement>()
    private var fileAdapter: FileListAdapter? = null
    private val _tag = javaClass.simpleName
    private var valueHarga: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences: SharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)
        idTmpUsaha = sharedPreferences.getString(Url.SESSION_ID_TEMPAT_USAHA, "")
        val label = sharedPreferences.getString(Url.setLabel, "Belum disetting")
        val tema = sharedPreferences.getString(Url.setTema, "0")
        when {
            tema!!.equals("0", ignoreCase = true) -> {
                this@SistemMasterActivity.setTheme(R.style.Theme_First)
            }
            tema.equals("1", ignoreCase = true) -> {
                this@SistemMasterActivity.setTheme(R.style.Theme_Second)
            }
            tema.equals("2", ignoreCase = true) -> {
                this@SistemMasterActivity.setTheme(R.style.Theme_Third)
            }
            tema.equals("3", ignoreCase = true) -> {
                this@SistemMasterActivity.setTheme(R.style.Theme_Fourth)
            }
            tema.equals("4", ignoreCase = true) -> {
                this@SistemMasterActivity.setTheme(R.style.Theme_Fifth)
            }
            tema.equals("5", ignoreCase = true) -> {
                this@SistemMasterActivity.setTheme(R.style.Theme_Sixth)
            }
        }
        setContentView(R.layout.activity_master_sistem)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = label

        etHarga.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() == "") return
                if (s.toString() != current)
                    etHarga.removeTextChangedListener(this)

                var cleanString = s.toString().replace("Rp ", "")
                println("SetOmzet1 : $cleanString")
                cleanString = cleanString.replaceAfter(",", "")
                println("SetOmzet2 : $cleanString")
                cleanString = cleanString.replace(",", "")
                println("SetOmzet3 : $cleanString")
                cleanString = cleanString.replace(".", "")

                println("SetOmzet4 : $cleanString")
                if (cleanString.isEmpty()) {
                    valueHarga = 0.0
                    current = "Rp 0,00"
                } else try {
                    valueHarga = cleanString.toDouble()
                    current = AddingIDRCurrency().formatIdrCurrencyNonKoma(valueHarga)
                } catch (ex: NumberFormatException) {
                    ex.printStackTrace()
                }
                println("SetOmzet5 : $current")

                etHarga.setText(current)
                etHarga.setSelection(current.length)
                etHarga.addTextChangedListener(this)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        bSimpan.setOnClickListener {
            val nmBarang = etNmBarang.text.toString()
            val keterangan = etDiskripsi.text.toString()
            if (nmBarang.isEmpty()) {

            } else if (valueHarga == 0.0) {

            } else if (keterangan.isEmpty()) {

            } else {
                masterDataPost(it,nmBarang,valueHarga,keterangan)
            }
        }

        ibCameraIntent.setOnClickListener {
            val sizeFile = fileArray.size
            if (sizeFile < 3) {
                setupPermissions(1)
            }
        }
        ibTmbhBerkas.setOnClickListener {
            val sizeFile = fileArray.size
            if (sizeFile < 3) {
                setupPermissions(2)
            }
        }
    }

    private fun masterDataPost(it: View,nmBarang: String,harga: Double,keterangan: String){
        if(CheckNetwork().checkingNetwork(it.context)) {
            val jmlFile = fileArray.size
            val params = HashMap<String, String>()
            params["ID_TEMPAT_USAHA"] = idTmpUsaha.toString()
            params["NM_BARANG"] = nmBarang
            params["HARGA"] = harga.toString()
            params["KETERANGAN"] = keterangan
            params["JML_FILE"] = jmlFile.toString()

            val files = HashMap<String, String>()
            for (i in 0 until jmlFile) {
                val sourceFile = fileArray[i].item
//                    val parts = sourceFile.split("/")
//                    val fileName = parts[parts.size - 1]
                files["uploaded_file${i+1}"] = sourceFile
            }

            SistemMasterJsonTask(
                this,
                params,
                files,
                this
            ).execute(Url.setSistemMaster)
        } else {
            Toast.makeText(this, getString(R.string.tidak_terkoneksi_internet), Toast.LENGTH_SHORT).show()
        }
    }

    override fun sistemMasterOnTask(result: String?) {
        if (result == null) {
            Toast.makeText(this,R.string.error_get_data,Toast.LENGTH_SHORT).show()
            return
        } else if (result == "") {
            Toast.makeText(this,R.string.empty_data,Toast.LENGTH_SHORT).show()
            return
        }

        Log.e(_tag, "Response from url:$result")
        try {
            val jsonObj = JSONObject(result)
            val success = jsonObj.getInt("success")
            val message = jsonObj.getString("message")
            if (success == 1)
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        } catch (ex : JSONException) {
            ex.printStackTrace()
            Toast.makeText(this, getString(R.string.error_data), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupPermissions(selected: Int) {
        if (selected == 1) {
//            val permission = ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.CAMERA
//            )
            val permissions = arrayOf(
                Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE
                ,Manifest.permission.READ_EXTERNAL_STORAGE)

            var result:Int
            val listPermissionsNeeded = ArrayList<String>()
            for (p in permissions) {
                result = ContextCompat.checkSelfPermission(this,p)
                if (result != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(p)
                }
            }

            if (!listPermissionsNeeded.isEmpty()) makeRequest(1)
            else cameraIntent()

//            if (permission != PackageManager.PERMISSION_GRANTED) {
//                Log.i(_tag, "Permission to record denied")
//                makeRequest(1)
//            } else {
//                cameraIntent()
//            }

        } else if (selected == 2) {
            val permission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )

            if (permission != PackageManager.PERMISSION_GRANTED) {
                Log.i(_tag, "Permission to record denied")
                makeRequest(2)
            } else {
                showFileChooser()
            }
        }
    }

    override fun deleteFileOnTask(index: Int) {
        fileArray.removeAt(index)
        recyclerView.post{ fileAdapter?.notifyDataSetChanged() }
    }

    private fun showFileChooser() {
        val intent = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            Intent(Intent.ACTION_OPEN_DOCUMENT)
        } else {
            Intent(Intent.ACTION_GET_CONTENT)
        }

        val mimeTypes = arrayOf("image/*", "application/pdf")

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)

            intent.putExtra("CONTENT_TYPE", mimeTypes)
            intent.addCategory((Intent.CATEGORY_DEFAULT))
        } else {
            intent.type = "*/*"
        }

        val chooserIntent: Intent
        if (packageManager.resolveActivity(intent, 0) != null) {
            chooserIntent = Intent.createChooser(intent, "Selecting File To Upload")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intent)
            startActivityForResult(chooserIntent, fileChooserCode)
        } else {
            chooserIntent = Intent.createChooser(intent, "Selecting File To Upload")
            startActivityForResult(chooserIntent, fileChooserCode)
        }
    }

    private fun makeRequest(selected: Int) {
        if (selected == 1)
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE),
                cameraIntentCode)
        else if (selected == 2)
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                fileChooserCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        Log.d(_tag, "onRequestPermissionsResult")
        when (requestCode) {
            cameraIntentCode -> {
                if (!grantResults.isEmpty()) {
                    var permissionsDenied = ""
                    for (per in permissions) {
                        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                            permissionsDenied += "\n" + per

                        }

                    }
                }
            }
            fileChooserCode -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(_tag, "Permission has been denied by user")
                    Toast.makeText(this, getString(R.string.permission_denied),Toast.LENGTH_SHORT).show()
                } else {
                    Log.i(_tag, "Permission has been granted by user")
                    showFileChooser()
                }
            }
        }
    }

    private fun cameraIntent() {

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(intent.resolveActivity(packageManager) != null) {
            val photoFile: File?
            try {
                photoFile = createImageFile()

                val photoURI = FileProvider.getUriForFile(this,
                    "com.dnhsolution.restokabmalang.provider",
                    photoFile)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(intent, cameraIntentCode)

            } catch (ex : IOException) {
                Log.e(_tag, "Camera Intent eror")
            }
        }
    }

    private var imageFilePath: String? = null

    private fun createImageFile() : File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir = "${Environment.getExternalStorageDirectory().absolutePath}/$folderName"
        Log.d(_tag,storageDir)

        var destDirectory: File? = null
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED)
            destDirectory = File(Environment.getExternalStorageDirectory().absolutePath,
                "/$folderName")
        else Log.e(_tag,"createImageFile gagal")

        if(!destDirectory?.exists()!!)
            destDirectory.mkdirs()
        else Log.e(_tag,"createImageFile gagal1")

        val destinationFile = File(Environment.getExternalStorageDirectory().absolutePath,
            "/$folderName")

        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",         /* suffix */
            destinationFile      /* directory */
        )

        imageFilePath = image.absolutePath
        return image
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            fileChooserCode -> {
                if (resultCode == RESULT_OK) {
                    val dataUri = data?.data ?: return
                    Log.d(_tag,dataUri.toString())
                    val path = RealPathUtil.getRealPath(this, dataUri)

                    if (path == null) {
                        Toast.makeText(this,getString(R.string.file_kosong),Toast.LENGTH_SHORT).show()
                        return
                    }
                    for (item in fileArray) {
                        if (path == item.item) {
                            Toast.makeText(this, getString(R.string.duplikat_file),Toast.LENGTH_SHORT).show()
                            return
                        }
                    }

                    Log.d(_tag, "File Path: $path")
                    println("mimetype : ${getMimeType(path)}")
                    val splitMimeType = getMimeType(path).split("/")
                    if (splitMimeType[0] != "image" || splitMimeType[0] == "Application") {
                        if (splitMimeType[0] != "Application" || splitMimeType[1] != "pdf") {
                            Toast.makeText(this,getString(R.string.extensi_file_tidak_didukung),Toast.LENGTH_SHORT).show()
                            return
                        }
                    }
                    val file = File(path)

//                    Bitmap bmp = BitmapFactory.decodeFile(miFoto)
//                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                    bmp.compress(CompressFormat.JPEG, 70, bos);

                    val fileSize = file.length() / 1024

                    if (fileSize > 1024) {
                        Toast.makeText(this, getString(R.string.limit_file),Toast.LENGTH_SHORT).show()
                        return
                    } else if (fileSize < 1) {
                        Toast.makeText(this, getString(R.string.file_kosong),Toast.LENGTH_SHORT).show()
                        return
                    }

                    fileArray.add(
                        FileListElement(
                            fileArray.size + 1,
                            path,
                            fileSize.toString()
                        )
                    )
                    Log.d("File Size", "$fileSize,${fileArray.size}")

                    if (fileAdapter?.itemCount != null) {
                        fileAdapter?.notifyDataSetChanged()
                    } else {
                        fileAdapter =
                            FileListAdapter(
                                fileArray,
                                this,
                                this
                            )
                        recyclerView.adapter = fileAdapter
                        recyclerView?.layoutManager = (LinearLayoutManager(this))
                    }
                }
            } cameraIntentCode -> {

            if (resultCode == RESULT_OK) {
                if (imageFilePath == null) return
                val file = File(imageFilePath!!)

                val bmp = BitmapFactory.decodeFile(imageFilePath!!)
                val bos = ByteArrayOutputStream()
                bmp.compress(Bitmap.CompressFormat.JPEG, 70, bos)
                val bitmapdata = bos.toByteArray()

                //write the bytes in file
                val fos = FileOutputStream(file);
                fos.write(bitmapdata)
                fos.flush()
                fos.close()

                val fileSize = file.length() / 1024

                fileArray.add(
                    FileListElement(
                        fileArray.size + 1,
                        imageFilePath!!,
                        fileSize.toString()
                    )
                )

                if (fileAdapter?.itemCount != null) {
                    fileAdapter?.notifyDataSetChanged()
                } else {
                    fileAdapter =
                        FileListAdapter(
                            fileArray,
                            this,
                            this
                        )
                    recyclerView.adapter = fileAdapter
                    recyclerView?.layoutManager = (LinearLayoutManager(this))
                }
            }
        } else -> {print("b")}
        }
    }

    fun getMimeType(path: String): String {
        val type = "Application/docx" // Default Value
        val extension = MimeTypeMap.getFileExtensionFromUrl(path)
        return if (extension != null) {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: ""
        } else type
    }
}