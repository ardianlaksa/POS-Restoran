package com.dnhsolution.restokabmalang.sistem.produk;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.sqlite.SQLiteException;
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
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dnhsolution.restokabmalang.BuildConfig;
import com.dnhsolution.restokabmalang.MainActivity;
import com.dnhsolution.restokabmalang.R;
import com.dnhsolution.restokabmalang.cetak.MainCetak;
import com.dnhsolution.restokabmalang.database.DatabaseHandler;
import com.dnhsolution.restokabmalang.sistem.MainMaster;
import com.dnhsolution.restokabmalang.sistem.produk.lokal.LokalFragment;
import com.dnhsolution.restokabmalang.sistem.produk.server.ServerFragment;
import com.dnhsolution.restokabmalang.sistem.produk.ui.main.SectionsPagerAdapter;
import com.dnhsolution.restokabmalang.utilities.CheckNetwork;
import com.dnhsolution.restokabmalang.utilities.Url;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class MasterProduk extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    RecyclerView rvProduk;
    private AdapterProduk adapterProduk;
    private List<ItemProduk> itemProduks = new ArrayList<>();
    int RecyclerViewClickedItemPos;
    View ChildView;

    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    String tempNameFile = "POSRestoran.jpg";
    private static final int FILE_SELECT_CODE = 5;
    private Uri filePath;
    private static final String IMAGE_DIRECTORY = "/POSRestoran";
    private File destFile;
    private SimpleDateFormat dateFormatter;
    File wallpaperDirectory;
    String e_nama_file = "", t_nama_file = "";
    String status = "";
    ImageView ivGambarBaru, ivGambar;

    ProgressDialog progressdialog;

    String e_nama, e_harga, e_ket, e_id, e_gambar_lama;
    String t_nama, t_harga, t_ket, t_id;

    TextView tvKet;

    DatabaseHandler databaseHandler;
    FloatingActionButton fab;
    TextView tv_count;
    int jml_data = 0;
    private Toolbar toolbar;
    private Menu menuTemp;
    private int statusJaringan = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE);
        String label = sharedPreferences.getString(Url.setLabel, "Belum disetting");
        String tema = sharedPreferences.getString(Url.setTema, "0");
        if(tema.equalsIgnoreCase("0")){
            MasterProduk.this.setTheme(R.style.Theme_First);
        }else if(tema.equalsIgnoreCase("1")){
            MasterProduk.this.setTheme(R.style.Theme_Second);
        }else if(tema.equalsIgnoreCase("2")){
            MasterProduk.this.setTheme(R.style.Theme_Third);
        }else if(tema.equalsIgnoreCase("3")){
            MasterProduk.this.setTheme(R.style.Theme_Fourth);
        }else if(tema.equalsIgnoreCase("4")){
            MasterProduk.this.setTheme(R.style.Theme_Fifth);
        }else if(tema.equalsIgnoreCase("5")){
            MasterProduk.this.setTheme(R.style.Theme_Sixth);
        }

        databaseHandler = new DatabaseHandler(this);

        setContentView(R.layout.activity_master_produk);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(label);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        fab = findViewById(R.id.fab);
        tv_count = (TextView)findViewById(R.id.text_count);

        requestMultiplePermissions();

        if(MainActivity.Companion.getAdMasterProduk() == 1) return;

//        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
//        alertDialog.setTitle("Tutorial");
//        alertDialog.setMessage("1. Tap tombol tambah kanan atas untuk menambahkan produk\n\n" +
//                "Icon panah kanan & kiri hijau menandakan status data produk sudah tersinkron.\n" +
//                "Icon refresh kuning menandakan status data produk butuh disinkron.");
//        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
//                (dialog, which) -> dialog.dismiss());
//        alertDialog.show();

        MainActivity.Companion.setAdMasterProduk(1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String label = sharedPreferences.getString(Url.setLabel, "Belum disetting");
        getSupportActionBar().setTitle(label);
        String tema = sharedPreferences.getString(Url.setTema, "0");
        if(tema.equalsIgnoreCase("0")){
            MasterProduk.this.setTheme(R.style.Theme_First);
        }else if(tema.equalsIgnoreCase("1")){
            MasterProduk.this.setTheme(R.style.Theme_Second);
        }else if(tema.equalsIgnoreCase("2")){
            MasterProduk.this.setTheme(R.style.Theme_Third);
        }else if(tema.equalsIgnoreCase("3")){
            MasterProduk.this.setTheme(R.style.Theme_Fourth);
        }else if(tema.equalsIgnoreCase("4")){
            MasterProduk.this.setTheme(R.style.Theme_Fifth);
        }else if(tema.equalsIgnoreCase("5")){
            MasterProduk.this.setTheme(R.style.Theme_Sixth);
        }
    }

    public void gantiIconWifi(Boolean value){
        if(value) {
            menuTemp.getItem(2).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_wifi_24_green));
            statusJaringan = 1;
        } else {
            menuTemp.getItem(2).setIcon(ContextCompat.getDrawable(this,R.drawable.ic_baseline_wifi_24_gray));
            statusJaringan = 0;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_master, menu);
        menuTemp = menu;
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_menu_lanjut) {
            if(new CheckNetwork().checkingNetwork(this) && statusJaringan == 1)
                DialogTambah();
            else Toast.makeText(this, R.string.tidak_terkoneksi_internet, Toast.LENGTH_SHORT).show();
            return true;
        } else if(item.getItemId() == R.id.action_menu_bantuan){
            tampilAlertDialogTutorial();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void tampilAlertDialogTutorial(){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage("" +
                "1. Saat ada icon refresh warna\n" +
                "    kuning dimasing-masing daftar\n" +
                "    produk, menandakan jika produk\n" +
                "    diload dari peralatan lokal.\n" +
                "2. Saat ada icon panah kanan kiri\n" +
                "    warna hijau dimasing-masing\n" +
                "    daftar produk, menandakan jika\n" +
                "    produk tersinkron dengan server.\n" +
                "3. Tombol icon (+) samping icon [?]\n" +
                "    di kanan atas untuk mulai\n" +
                "    transaksi dengan produk yang\n" +
                "    dipilih.");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public void DialogTambah(){
        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_tambah_produk, null);

        final EditText etNama, etKeterangan, etHarga;
        final Button btnSimpan;
        final ImageView ivTambahFoto;

        etNama = (EditText) dialogView.findViewById(R.id.etNama);
        etKeterangan = (EditText) dialogView.findViewById(R.id.etKeterangan);
        etHarga = (EditText) dialogView.findViewById(R.id.etHarga);

        btnSimpan = (Button) dialogView.findViewById(R.id.btnSimpan);

        ivGambar = (ImageView)dialogView.findViewById(R.id.ivGambar);
        ivTambahFoto = (ImageView)dialogView.findViewById(R.id.ivTambahFoto);

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                t_nama = etNama.getText().toString();
                t_harga = etHarga.getText().toString().replace(".", "");
                t_ket = etKeterangan.getText().toString();

                if(t_nama.trim().equalsIgnoreCase("")){
                    etNama.requestFocus();
                    etNama.setError("Silahkan isi form ini !");
                }else if(t_harga.trim().equalsIgnoreCase("")){
                    etHarga.requestFocus();
                    etHarga.setError("Silahkan isi form ini !");
                }else if(t_ket.trim().equalsIgnoreCase("")){
                    etKeterangan.requestFocus();
                    etKeterangan.setError("Silahkan isi form ini !");
                }else if(t_nama_file.trim().equalsIgnoreCase("")){
                    Toast.makeText(MasterProduk.this, "Silahkan pilih gambar !", Toast.LENGTH_SHORT).show();
                }else{
                    if(new CheckNetwork().checkingNetwork(MasterProduk.this)){
                        TambahData();
                    }
                    dialogBuilder.dismiss();
                }
            }
        });

        ivTambahFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
                if (!wallpaperDirectory.exists()) {  // have the object build the directory structure, if needed.
                    wallpaperDirectory.mkdirs();
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(MasterProduk.this);
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
                                        status = "t";
                                    }
                                } else {
                                    wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
                                    if (!wallpaperDirectory.exists()) {  // have the object build the directory structure, if needed.
                                        wallpaperDirectory.mkdirs();
                                    }

                                    showFileChooser();
                                    status = "t";
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
                                        status = "t";
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
                                    status = "t";
                                }

                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

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

                    if (originalString.contains(".")) {
                        originalString = originalString.replace(".", "");
                    }
                    long longval = Long.parseLong(originalString);

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
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(MasterProduk.this);
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

        if(resultCode == RESULT_CANCELED){
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

                if(status.equalsIgnoreCase("e")){
                    Glide.with(ivGambarBaru.getContext()).load(new File(file.getAbsolutePath()).toString())
                            .placeholder(R.mipmap.ic_foto)
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
                            .into(ivGambarBaru);

                    ivGambarBaru.setVisibility(View.VISIBLE);

                    if(!e_nama_file.equalsIgnoreCase("")){
                        File fl = new File(e_nama_file);
                        boolean deleted = fl.delete();
                    }

                    e_nama_file = file.getAbsolutePath();
                }else if(status.equalsIgnoreCase("t")){
                    Glide.with(ivGambar.getContext()).load(new File(file.getAbsolutePath()).toString())
                            .placeholder(R.mipmap.ic_foto)
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
                            .into(ivGambar);

                    ivGambar.setVisibility(View.VISIBLE);

                    if(!t_nama_file.equalsIgnoreCase("")) {
                        File fl = new File(t_nama_file);
                        boolean deleted = fl.delete();
                    }

                    t_nama_file = file.getAbsolutePath();
                }

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

                        int file_size = Integer.parseInt(String.valueOf(filePhoto.length() / 1024));

                        Log.d("PirangMB", String.valueOf(file_size));
                        if(status.equalsIgnoreCase("e")){
                            Glide.with(ivGambarBaru.getContext()).load(new File(f.getAbsolutePath()).toString())
                                    .placeholder(R.mipmap.ic_foto)
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
                                    .into(ivGambarBaru);

                            ivGambarBaru.setVisibility(View.VISIBLE);
                            if(!e_nama_file.equalsIgnoreCase("")){
                                File fl = new File(e_nama_file);
                                boolean deleted = fl.delete();
                            }

                            e_nama_file = f.getAbsolutePath();
                        }else if(status.equalsIgnoreCase("t")){
                            Glide.with(ivGambar.getContext()).load(new File(f.getAbsolutePath()).toString())
                                    .placeholder(R.mipmap.ic_foto)
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
                                    .into(ivGambar);
                            ivGambar.setVisibility(View.VISIBLE);

                            if(!t_nama_file.equalsIgnoreCase("")){
                                File fl = new File(t_nama_file);
                                boolean deleted = fl.delete();
                            }

                            t_nama_file = f.getAbsolutePath();
                        }
                        break;
                    }

                }

            }
        }
    }

//    private void UpdateData() {
//        class UpdateData extends AsyncTask<Void, Integer, String> {
//
//            //ProgressDialog uploading;
//
//            @Override
//            protected void onPreExecute() {
//                super.onPreExecute();
//                progressdialog = new ProgressDialog(MasterProduk.this);
//                progressdialog.setCancelable(false);
//                progressdialog.setMessage("Upload data ke server ...");
//                progressdialog.show();
//                //uploading = ProgressDialog.show(SinkronActivity.this, "Mengirim data ke Server", "Mohon Tunggu...", false, false);
//            }
//
//            protected void onProgressUpdate(Integer... values)
//            {
//                //progressdialog.setProgress(values[0]);
//
//            }
//
//            @Override
//            protected void onPostExecute(String s) {
//                super.onPostExecute(s);
//                // uploading.dismiss();
//                Log.d("HASIL", s);
//                //Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show();
//                // tvStatus.setText(s);
//                //
//                if(s.equalsIgnoreCase("sukses")){
//
//                    if (progressdialog.isShowing())
//                        progressdialog.dismiss();
//                    Toast.makeText(MasterProduk.this, "Data berhasil diupdate !", Toast.LENGTH_SHORT).show();
//                    File fl = new File(e_nama_file);
//                    boolean deleted = fl.delete();
//                    e_nama_file = "";
//                    getData();
//                }else if(s.equalsIgnoreCase("gagal")){
//                    if (progressdialog.isShowing())
//                        progressdialog.dismiss();
//                    Toast.makeText(MasterProduk.this, "Data gagal diupdate !", Toast.LENGTH_SHORT).show();
//                }else{
//                    if (progressdialog.isShowing())
//                        progressdialog.dismiss();
//                    Toast.makeText(MasterProduk.this, s, Toast.LENGTH_SHORT).show();
//                }
//
//            }
//
//            @Override
//            protected String doInBackground(Void... params) {
//
//                UploadData u = new UploadData();
//                String msg = null;
//                msg = u.uploadDataUmum(e_nama,e_ket,e_harga, e_id, e_gambar_lama, e_nama_file);
//
//                return msg;
//            }
//        }
//        UpdateData uv = new UpdateData();
//        uv.execute();
//    }

//    private void UpdateDataLokal() {
//        try {
//            String id_tmp_usaha = sharedPreferences.getString(Url.SESSION_ID_TEMPAT_USAHA,"");
//            String f_gambar = "";
//            if(e_nama_file.equalsIgnoreCase("")){
//                f_gambar = e_gambar_lama;
//            }else{
//                f_gambar =  e_nama_file;
//            }
//            databaseHandler.update_produk(new com.dnhsolution.restokabmalang.database.ItemProduk(
//                    Integer.parseInt(e_id), id_tmp_usaha,e_nama,e_harga,e_ket,f_gambar,"0"
//            ));
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        if(databaseHandler.equals("")){
//            Toast.makeText(getApplicationContext(),"Gagal Update Data ! ", Toast.LENGTH_LONG).show();
//        } else {
//            if(!e_nama_file.equalsIgnoreCase("")){
//                File fl = new File(e_gambar_lama);
//                boolean deleted = fl.delete();
//                e_nama_file = "";
//            }
//            getDataLokal();
//            Toast.makeText(getApplicationContext(), "Berhasil Update Data ! ", Toast.LENGTH_LONG).show();
//        }
//    }

    private void TambahData() {
        class TambahData extends AsyncTask<Void, Integer, String> {

            //ProgressDialog uploading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressdialog = new ProgressDialog(MasterProduk.this);
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

                    Toast.makeText(MasterProduk.this, "Data berhasil ditambah !", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MasterProduk.class));
                    MasterProduk.this.finish();
                }else if(s.equalsIgnoreCase("gagal")){
                    if (progressdialog.isShowing())
                        progressdialog.dismiss();
                    Toast.makeText(MasterProduk.this, "Data gagal ditambah !", Toast.LENGTH_SHORT).show();
                }else{
                    if (progressdialog.isShowing())
                        progressdialog.dismiss();
                    Toast.makeText(MasterProduk.this, s, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            protected String doInBackground(Void... params) {
                String id_tmp_usaha = sharedPreferences.getString(Url.SESSION_ID_TEMPAT_USAHA,"");
                databaseHandler.insert_produk(new com.dnhsolution.restokabmalang.database.ItemProduk(
                        0, id_tmp_usaha,t_nama,t_harga,t_ket,t_nama_file,"1"
                ));
                if(databaseHandler.equals("")){
                    Log.d("DATABASE_INSERT", "gagal");
                } else {
                    Log.d("DATABASE_INSERT", "berhasil");
                }
                UploadData u = new UploadData();
                String msg = null;
//                String id_tmp_usaha = sharedPreferences.getString(Url.SESSION_ID_TEMPAT_USAHA,"");
                msg = u.uploadDataBaru(t_nama,t_ket,t_harga, t_nama_file, id_tmp_usaha);

                return msg;
            }
        }
        TambahData uv = new TambahData();
        uv.execute();
    }
}
