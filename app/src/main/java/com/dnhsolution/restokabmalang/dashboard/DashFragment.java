package com.dnhsolution.restokabmalang.dashboard;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dnhsolution.restokabmalang.BuildConfig;
import com.dnhsolution.restokabmalang.MainActivity;
import com.dnhsolution.restokabmalang.R;
import com.dnhsolution.restokabmalang.database.DatabaseHandler;
import com.dnhsolution.restokabmalang.sistem.produk.MasterProduk;
import com.dnhsolution.restokabmalang.sistem.produk.RealPathUtil;
import com.dnhsolution.restokabmalang.tersimpan.DataTersimpanActivity;
import com.dnhsolution.restokabmalang.utilities.Url;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class DashFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Layout tampilan untuk fragment ini
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_dashboard, parent, false);
    }

    DatabaseHandler databaseHandler;
    TextView tvTrxTersimpan, tvBatas;
    CardView cvTransaksi;
    SharedPreferences sharedPreferences;
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    String tempNameFile = "Logo.jpg";
    private static final int FILE_SELECT_CODE = 5;
    private Uri filePath;
    private static final String IMAGE_DIRECTORY = "/POSRestoran";
    File wallpaperDirectory;
    String t_nama_file = "";
    ImageView ivGambar;

    Handler mHandler;
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        requestMultiplePermissions();
        databaseHandler = new DatabaseHandler(getContext());
        sharedPreferences = getContext().getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE);

        tvTrxTersimpan = view.findViewById(R.id.tvTransaksiTersimpan);
        tvBatas= view.findViewById(R.id.tvBatas);
        cvTransaksi= view.findViewById(R.id.cvTransaksi);

        String batas = sharedPreferences.getString(Url.SESSION_BATAS_WAKTU, "7");
        int jml_trx = databaseHandler.CountDataTersimpan();
        int jml_produk = databaseHandler.CountDataProduk();
        String tgl_trx = "";

        SQLiteDatabase db = databaseHandler.getReadableDatabase();

        Cursor cTrx = db.rawQuery(
                "SELECT tanggal_trx FROM transaksi WHERE status='0' ORDER BY tanggal_trx ASC LIMIT 1",
                null
        );

        cTrx.moveToFirst();
        if(jml_trx>0){
            tgl_trx = formatDate(cTrx.getString(0), "yyyyMMdd");
            tvBatas.setText(getBatas(tgl_trx, Integer.parseInt(batas)));
        }
        else{
            tvBatas.setText(getResources().getString(R.string.tanggal_kosong));
        }

        tvTrxTersimpan.setText(String.valueOf(jml_trx));

        cvTransaksi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), DataTersimpanActivity.class));
            }
        });

//        cvProduk.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(getContext(), MasterProduk.class));
//            }
//        });

        if(MainActivity.Companion.getAdDashboard() == 1) return;

        tampilAlertDialogTutorial();

        MainActivity.Companion.setAdDashboard(1);

        // EditText etFoo = (EditText) view.findViewById(R.id.etFoo);
    }

    private void tampilAlertDialogTutorial(){
        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle("Tutorial");
        alertDialog.setMessage("1. Pilih produk tersimpan untuk menambah produk\n" +
                "2. Pilih transaksi tersimpan untuk melihat data transaksi yang belum tersinkron ke server\n" +
                "3. Batas waktu sinkron digunakan untuk menampilkan waktu batas untuk sinkronisasi data transaksi");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private String formatDate(String date, String format){
        String formattedDate = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            Date parseDate = sdf.parse(date);
            formattedDate = new SimpleDateFormat(format).format(parseDate);
        }catch (ParseException e){
            e.printStackTrace();
        }

        return formattedDate;
    }

    private String getBatas(String tanggal, int days){
        String dateInString = tanggal;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(sdf.parse(dateInString));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.add(Calendar.DATE, days);
        sdf = new SimpleDateFormat("dd / MM / yyyy");
        Date resultdate = new Date(c.getTimeInMillis());
        dateInString = sdf.format(resultdate);

        return dateInString;
    }

    private void requestMultiplePermissions() {
        Dexter.withActivity(getActivity())
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
                        Toast.makeText(getContext(), "Some Error! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
        Uri uri = Uri.fromParts("package", getContext().getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    public void DialogLogo(){
        final AlertDialog dialogBuilder = new AlertDialog.Builder(getContext()).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_form_logo, null);

        final ImageView ivTambahGambar;
        final Button btnSimpan;

        btnSimpan = (Button) dialogView.findViewById(R.id.btnSimpan);

        ivGambar = (ImageView)dialogView.findViewById(R.id.ivGambar);
        ivTambahGambar = (ImageView)dialogView.findViewById(R.id.ivTambahFoto);

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();

                //menambah data ke editor
                editor.putString(Url.SESSION_LOGO, t_nama_file);

                //menyimpan data ke editor
                editor.apply();

                String logo = sharedPreferences.getString(Url.SESSION_LOGO, "");
                if(!logo.equalsIgnoreCase("")){
                    Toast.makeText(getContext(), "Logo Berhasil ditambahkan !", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getContext(), "Logo Gagal ditambahkan !", Toast.LENGTH_SHORT).show();
                }

                startActivity(new Intent(getContext(), MainActivity.class));
                getActivity().finish();
                dialogBuilder.dismiss();

            }
        });

        String logo = sharedPreferences.getString(Url.SESSION_LOGO, "");
        if(!logo.equalsIgnoreCase("")){
            Glide.with(ivGambar.getContext()).load(new File(logo).toString())
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
        }

        ivTambahGambar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
                if (!wallpaperDirectory.exists()) {  // have the object build the directory structure, if needed.
                    wallpaperDirectory.mkdirs();
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Pilihan Tambah Foto")
                        .setPositiveButton("Galeri", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    if (getActivity().checkSelfPermission(Manifest.permission.CAMERA)
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
                                    if (getActivity().checkSelfPermission(Manifest.permission.CAMERA)
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
                                        tempNameFile = "Logo_"+sdf.format(cal.getTime())+".jpg";
                                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                        File f = new File(wallpaperDirectory, tempNameFile);
                                        Uri photoURI = FileProvider.getUriForFile(getContext(),
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
                                    tempNameFile = "Logo_"+sdf.format(cal.getTime())+".jpg";
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


        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
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
            Toast.makeText(getContext(), "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == getActivity().RESULT_CANCELED){
            return;
        }
        if(requestCode == FILE_SELECT_CODE){
            filePath = data.getData();
            String a = RealPathUtil.getRealPath(getContext(), filePath);

            if (resultCode == getActivity().RESULT_OK) {
                // Get the Uri of the selected file
                Uri uri = data.getData();
                Log.d("Foto", "File Uri: " + uri.toString());
                // Get the path
                String path = a;

                Log.d("Foto", "File Path: " + path);
                File sourceLocation = new File (path);
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyHHmmss", Locale.getDefault());
                String filename = "Logo_"+sdf.format(cal.getTime())+".jpg";
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
                t_nama_file = file.getAbsolutePath();
                String logo = sharedPreferences.getString(Url.SESSION_LOGO, "");
                File fl = new File(logo);
                boolean deleted = fl.delete();
            }
        }else if(requestCode == CAMERA_REQUEST){
            if (resultCode == getActivity().RESULT_OK) {
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
                        t_nama_file = f.getAbsolutePath();
                        String logo = sharedPreferences.getString(Url.SESSION_LOGO, "");
                        File fl = new File(logo);
                        boolean deleted = fl.delete();
                        break;
                    }

                }

            }
        }
    }


    private final Runnable m_Runnable = new Runnable()
    {
        public void run()

        {
            mHandler.postDelayed(m_Runnable, 3000);
        }

    };

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(m_Runnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        int jml_trx = databaseHandler.CountDataTersimpan();
        tvTrxTersimpan.setText(String.valueOf(jml_trx));
        this.mHandler = new Handler();
        this.mHandler.postDelayed(m_Runnable,3000);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_help,menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_menu_bantuan) {
            tampilAlertDialogTutorial();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}