package com.dnhsolution.restokabmalang.sistem.produk;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dnhsolution.restokabmalang.BuildConfig;
import com.dnhsolution.restokabmalang.R;
import com.dnhsolution.restokabmalang.utilities.Url;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DetailProduk extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    ImageView ivFotoLama, ivFotoBaru;
    Button btnSimpan, btnGanti;
    EditText etNama, etHarga, etKeterangan;
    String url_image, nama, id, harga, ket;

    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    String tempNameFile = "POSRestoran.jpg";
    private static final int FILE_SELECT_CODE = 5;
    private Uri filePath;
    private static final String IMAGE_DIRECTORY = "/POSRestoran";
    private File destFile;
    private SimpleDateFormat dateFormatter;
    File wallpaperDirectory;
    String nama_file = "";

    ProgressDialog progressdialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE);
        String label = sharedPreferences.getString(Url.setLabel, "Belum disetting");
        String tema = sharedPreferences.getString(Url.setTema, "0");
        if(tema.equalsIgnoreCase("0")){
            DetailProduk.this.setTheme(R.style.Theme_First);
        }else if(tema.equalsIgnoreCase("1")){
            DetailProduk.this.setTheme(R.style.Theme_Second);
        }else if(tema.equalsIgnoreCase("2")){
            DetailProduk.this.setTheme(R.style.Theme_Third);
        }else if(tema.equalsIgnoreCase("3")){
            DetailProduk.this.setTheme(R.style.Theme_Fourth);
        }else if(tema.equalsIgnoreCase("4")){
            DetailProduk.this.setTheme(R.style.Theme_Fifth);
        }else if(tema.equalsIgnoreCase("5")){
            DetailProduk.this.setTheme(R.style.Theme_Sixth);
        }
        
        setContentView(R.layout.activity_detail_produk);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(label);

        ivFotoBaru = (ImageView)findViewById(R.id.ivFotoBaru);
        ivFotoLama = (ImageView)findViewById(R.id.ivFotoLama);
        btnGanti = (Button) findViewById(R.id.btnGantiFoto);
        btnSimpan = (Button) findViewById(R.id.btnSimpan);
        etNama = (EditText) findViewById(R.id.etNama);
        etHarga = (EditText) findViewById(R.id.etHarga);
        etKeterangan = (EditText) findViewById(R.id.etKeterangan);

        url_image = getIntent().getStringExtra("url_image");
        nama = getIntent().getStringExtra("nama_barang");
        id = getIntent().getStringExtra("id_barang");
        harga = getIntent().getStringExtra("harga");
        ket = getIntent().getStringExtra("ket");

        requestMultiplePermissions();

        String originalString = harga;

        Long longval;
        if (originalString.contains(".")) {
            originalString = originalString.replace(".", "");
        }
        longval = Long.parseLong(originalString);

        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        formatter.applyPattern("#,###,###,###");
        String formattedString = formatter.format(longval);

        //setting text after format to EditText
        etHarga.setText(formattedString.replace(",", "."));
        etHarga.setSelection(etHarga.getText().length());

        etNama.setText(nama);
        etKeterangan.setText(ket);



        etHarga.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {

                etHarga.removeTextChangedListener(this);

                try {
                    String originalString = s.toString();

                    Long longval;
                    if (originalString.contains(".")) {
                        originalString = originalString.replace(".", "");
                    }
                    longval = Long.parseLong(originalString);

                    DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
                    formatter.applyPattern("#,###,###,###");
                    String formattedString = formatter.format(longval);

                    //setting text after format to EditText
                    etHarga.setText(formattedString.replace(",", "."));
                    etHarga.setSelection(etHarga.getText().length());

                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }

                etHarga.addTextChangedListener(this);
                // TODO Auto-generated method stub
            }
        });

        Glide.with(ivFotoLama.getContext()).load(Url.serverFoto+url_image)
                .centerCrop()
                .fitCenter()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.e("xmx1","Error "+e.toString());
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        Log.e("xmx1","no Error ");
                        return false;
                    }
                })
                .into(ivFotoLama);

        btnGanti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
                if (!wallpaperDirectory.exists()) {  // have the object build the directory structure, if needed.
                    wallpaperDirectory.mkdirs();
                }
                    AlertDialog.Builder builder = new AlertDialog.Builder(DetailProduk.this);
                    builder.setMessage("Pilihan Tambah Foto")
                            .setPositiveButton("Galeri", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        if (checkSelfPermission(Manifest.permission.CAMERA)
                                                != PackageManager.PERMISSION_GRANTED) {
                                            requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                    MY_CAMERA_PERMISSION_CODE);
                                            //showFileChooser();
                                        } else {
                                            wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
                                            if (!wallpaperDirectory.exists()) {  // have the object build the directory structure, if needed.
                                                wallpaperDirectory.mkdirs();
                                            }

                                            showFileChooser();
                                        }
                                    } else {
                                        wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
                                        if (!wallpaperDirectory.exists()) {  // have the object build the directory structure, if needed.
                                            wallpaperDirectory.mkdirs();
                                        }

                                        showFileChooser();
                                    }
                                }
                            })
                            .setNegativeButton("Kamera", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        if (checkSelfPermission(Manifest.permission.CAMERA)
                                                != PackageManager.PERMISSION_GRANTED) {
                                            requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                    MY_CAMERA_PERMISSION_CODE);
                                            //showFileChooser();
                                        } else {
                                            wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
                                            if (!wallpaperDirectory.exists()) {  // have the object build the directory structure, if needed.
                                                wallpaperDirectory.mkdirs();
                                            }

                                            Calendar cal = Calendar.getInstance();
                                            SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyHHmmss", Locale.getDefault());
                                            tempNameFile = "Cam_"+sdf.format(cal.getTime())+".jpg";
                                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                            File f = new File(wallpaperDirectory, tempNameFile);
                                            Uri photoURI = FileProvider.getUriForFile(getApplicationContext(),
                                                    BuildConfig.APPLICATION_ID + ".provider",
                                                    f);
                                            //cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                                            startActivityForResult(cameraIntent, CAMERA_REQUEST);
                                        }
                                    } else {
                                        wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
                                        if (!wallpaperDirectory.exists()) {  // have the object build the directory structure, if needed.
                                            wallpaperDirectory.mkdirs();
                                        }

                                        Calendar cal = Calendar.getInstance();
                                        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyHHmmss", Locale.getDefault());
                                        tempNameFile = "Cam_"+sdf.format(cal.getTime())+".jpg";
                                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                        File f = new File(wallpaperDirectory, tempNameFile);
                                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                                        startActivityForResult(intent, CAMERA_REQUEST);
                                    }

                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
            }
        });

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendData();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        String label = sharedPreferences.getString(Url.setLabel, "Belum disetting");
        String tema = sharedPreferences.getString(Url.setTema, "0");
        if(tema.equalsIgnoreCase("0")){
            DetailProduk.this.setTheme(R.style.Theme_First);
        }else if(tema.equalsIgnoreCase("1")){
            DetailProduk.this.setTheme(R.style.Theme_Second);
        }else if(tema.equalsIgnoreCase("2")){
            DetailProduk.this.setTheme(R.style.Theme_Third);
        }else if(tema.equalsIgnoreCase("3")){
            DetailProduk.this.setTheme(R.style.Theme_Fourth);
        }else if(tema.equalsIgnoreCase("4")){
            DetailProduk.this.setTheme(R.style.Theme_Fifth);
        }else if(tema.equalsIgnoreCase("5")){
            DetailProduk.this.setTheme(R.style.Theme_Sixth);
        }
    }

    private void requestMultiplePermissions() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {

                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {  // check if all permissions are granted
                            //Toast.makeText(getApplicationContext(), "All permissions are granted by user!", Toast.LENGTH_SHORT).show();
                        }

                        if (report.isAnyPermissionPermanentlyDenied()) { // check for permanent denial of any permission
                            // show alert dialog navigating to Settings
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }

                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        // check for permanent denial of permission
                        if (response.isPermanentlyDenied()) {
                            showSettingsDialog();
                        }
                    }

//                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
//                        token.continuePermissionRequest();
//                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Some Error! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DetailProduk.this);
        builder.setTitle("Perizian dibutuhkan !");
        builder.setMessage("Aplikasi ini membutuhkan perizinan untuk akses beberapa feature. Anda dapat mengatur di Pengaturan Aplikasi.");
        builder.setPositiveButton("Pengaturan", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == this.RESULT_CANCELED){
            return;
        }
        if(requestCode == FILE_SELECT_CODE){
            filePath = data.getData();
            String a = RealPathUtil.getRealPath(getApplicationContext(), filePath);

            if (resultCode == RESULT_OK) {
                // Get the Uri of the selected file
                Uri uri = data.getData();
                Log.d("Foto", "File Uri: " + uri.toString());
                // Get the path
                String path = a;

                Log.d("Foto", "File Path: " + path);
                File sourceLocation = new File (path);
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyHHmmss", Locale.getDefault());
                String filename = "Gallery_"+sdf.format(cal.getTime())+".jpg";
                File targetLocation = new File (wallpaperDirectory.toString(), filename);

                if(sourceLocation.exists()){

                    Log.v("Pesan", "Proses Pindah");
                    try {
                        InputStream in = new FileInputStream(sourceLocation);
                        OutputStream out = new FileOutputStream(targetLocation);
                        // Copy the bits from instream to outstream
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                        in.close();
                        out.close();
                        Log.v("Pesan", "Copy file successful.");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    Log.v("Pesan", "Copy file failed. Source file missing.");
                }

                File file = new File(wallpaperDirectory.toString(),filename);
                int file_size = Integer.parseInt(String.valueOf(file.length() / 1024));

                Log.d("PirangMB", String.valueOf(file_size));

                Glide.with(ivFotoBaru.getContext()).load(new File(file.getAbsolutePath()).toString())
                        .centerCrop()
                        .fitCenter()
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                Log.e("xmx1","Error "+e.toString());
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                Log.e("xmx1","no Error ");
                                return false;
                            }
                        })
                        .into(ivFotoBaru);

                if(nama_file.equalsIgnoreCase("")){}
                else{
                    File fl = new File(nama_file);
                    boolean deleted = fl.delete();
                }

                nama_file = file.getAbsolutePath();

//                Berkas berkas = new Berkas(file.getAbsolutePath(), file_size);
//                berkasList.add(berkas);
//                bAdapter.notifyDataSetChanged();
            }
        }else if(requestCode == CAMERA_REQUEST){
            if (resultCode == RESULT_OK) {
                System.out.println("CAMERA_REQUEST1");
//                        Bitmap photo = (Bitmap) data.getExtras().get("data");
                File f = new File(wallpaperDirectory.toString());
                Log.d("File", String.valueOf(f));
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals(tempNameFile)) {
                        f = temp;
                        File filePhoto = new File(wallpaperDirectory.toString(), tempNameFile);
                        //pic = photo;

                        int file_size = Integer.parseInt(String.valueOf(filePhoto.length() / 1024));

                        Log.d("PirangMB", String.valueOf(file_size));
                        //tvFileName.setVisibility(View.VISIBLE);
                        // ivBerkas.setVisibility(View.VISIBLE);
                        Glide.with(ivFotoBaru.getContext()).load(new File(f.getAbsolutePath()).toString())
                                .centerCrop()
                                .fitCenter()
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        Log.e("xmx1","Error "+e.toString());
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        Log.e("xmx1","no Error ");
                                        return false;
                                    }
                                })
                                .into(ivFotoBaru);

                        if(nama_file.equalsIgnoreCase("")){}
                        else{
                            File fl = new File(nama_file);
                            boolean deleted = fl.delete();
                        }

                        nama_file = f.getAbsolutePath();

//                        Berkas berkas = new Berkas(f.getAbsolutePath(), file_size);
//                        berkasList.add(berkas);
//                        //gridberkasList.add(berkas);
//
//                        bAdapter.notifyDataSetChanged();
                        //gbAdapter.notifyDataSetChanged();
                        break;
                    }

                }

            }
        }
    }

    private void SendData() {
        class SendData extends AsyncTask<Void, Integer, String> {

            //ProgressDialog uploading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressdialog = new ProgressDialog(DetailProduk.this);
                progressdialog.setCancelable(false);
                progressdialog.setMessage("Upload data ke server ...");
                progressdialog.show();
                //uploading = ProgressDialog.show(SinkronActivity.this, "Mengirim data ke Server", "Mohon Tunggu...", false, false);
            }

            protected void onProgressUpdate(Integer... values)
            {
                //progressdialog.setProgress(values[0]);

            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                // uploading.dismiss();
                Log.d("HASIL", s);
                //Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show();
                // tvStatus.setText(s);
                //
                if(s.equalsIgnoreCase("sukses")){

                    if (progressdialog.isShowing())
                        progressdialog.dismiss();
                    Toast.makeText(DetailProduk.this, "Data berhasil diupdate !", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(DetailProduk.this, MasterProduk.class));
                    finish();
                }else if(s.equalsIgnoreCase("gagal")){
                    if (progressdialog.isShowing())
                        progressdialog.dismiss();
                    Toast.makeText(DetailProduk.this, "Data gagal diupdate !", Toast.LENGTH_SHORT).show();
                }else{
                    if (progressdialog.isShowing())
                        progressdialog.dismiss();
                    Toast.makeText(DetailProduk.this, s, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            protected String doInBackground(Void... params) {

                UploadData u = new UploadData();
                String msg = null;
                msg = u.uploadDataUmum(etNama.getText().toString(),etKeterangan.getText().toString(),
                        etHarga.getText().toString().replace(".", ""), id, url_image, nama_file);

                return msg;
            }
        }
        SendData uv = new SendData();
        uv.execute();
    }


}
