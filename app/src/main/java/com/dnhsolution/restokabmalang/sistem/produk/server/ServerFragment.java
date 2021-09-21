package com.dnhsolution.restokabmalang.sistem.produk.server;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dnhsolution.restokabmalang.BuildConfig;
import com.dnhsolution.restokabmalang.R;
import com.dnhsolution.restokabmalang.database.DatabaseHandler;
import com.dnhsolution.restokabmalang.sistem.produk.AdapterProduk;
import com.dnhsolution.restokabmalang.sistem.produk.ItemProduk;
import com.dnhsolution.restokabmalang.sistem.produk.MasterProduk;
import com.dnhsolution.restokabmalang.sistem.produk.RealPathUtil;
import com.dnhsolution.restokabmalang.sistem.produk.UploadData;
import com.dnhsolution.restokabmalang.utilities.CheckNetwork;
import com.dnhsolution.restokabmalang.utilities.Url;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ServerFragment extends Fragment {
    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment

        return inflater.inflate(R.layout.fragment_server, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    SharedPreferences sharedPreferences;
    RecyclerView rvProduk;
    private AdapterProduk adapterProduk;
    private List<ItemProduk> itemProduks = new ArrayList<>();
    int RecyclerViewClickedItemPos;
    View ChildView;
    TextView tvKet;

    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    String tempNameFile = "POSRestoran.jpg";
    private static final int FILE_SELECT_CODE = 5;
    private Uri filePath;
    private static final String IMAGE_DIRECTORY = "/POSRestoran";
    private File destFile;
    File wallpaperDirectory;
    String e_nama_file = "", t_nama_file = "";
    String status = "";
    ImageView ivGambarBaru, ivGambar;

    ProgressDialog progressdialog;

    String e_nama, e_harga, e_ket, e_id, e_gambar_lama;


    DatabaseHandler databaseHandler;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
        // EditText etFoo = (EditText) view.findViewById(R.id.etFoo);

        sharedPreferences = getContext().getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE);
        databaseHandler = new DatabaseHandler(getContext());
        rvProduk = (RecyclerView)view.findViewById(R.id.rvProduk);
        tvKet = (TextView)view.findViewById(R.id.tvKet);
        adapterProduk= new AdapterProduk(itemProduks, getContext());
        RecyclerView.LayoutManager mLayoutManagerss = new LinearLayoutManager(getContext());
        rvProduk.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rvProduk.setItemAnimator(new DefaultItemAnimator());
        rvProduk.setAdapter(adapterProduk);

        rvProduk.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {

            GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {

                @Override public boolean onSingleTapUp(MotionEvent motionEvent) {
                    return true;
                }

            });
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                ChildView = rvProduk.findChildViewUnder(e.getX(), e.getY());

                if(ChildView != null && gestureDetector.onTouchEvent(e)) {
                    RecyclerViewClickedItemPos = rvProduk.getChildAdapterPosition(ChildView);
                    String url_image = itemProduks.get(RecyclerViewClickedItemPos).getUrl_image();
                    String nama_barang = itemProduks.get(RecyclerViewClickedItemPos).getNama_barang();
                    String id_barang = itemProduks.get(RecyclerViewClickedItemPos).getId_barang();
                    String harga = itemProduks.get(RecyclerViewClickedItemPos).getHarga();
                    String ket = itemProduks.get(RecyclerViewClickedItemPos).getKeterangan();
                    DialogEdit(url_image, nama_barang, id_barang, harga, ket);
                    Log.d("TAG", String.valueOf(RecyclerViewClickedItemPos));
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        if(new CheckNetwork().checkingNetwork(getContext())){
            getData();
            tvKet.setVisibility(View.GONE);
        }else{
            getDataLokal();
//            tvKet.setVisibility(View.VISIBLE);
//            Toast.makeText(getContext(), "Data tidak ditemukan. Silahkan cek koneksi internet !", Toast.LENGTH_SHORT).show();
        }
    }

    private void getData() {
        itemProduks.clear();
        adapterProduk.notifyDataSetChanged();
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Mencari data...");
        progressDialog.show();
        RequestQueue queue = Volley.newRequestQueue(getContext());
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE);
        final String id_tempat_usaha = sharedPreferences.getString(Url.SESSION_ID_TEMPAT_USAHA, "0");
        Log.d("ID_TEMPAT_USAHA", id_tempat_usaha);
        String url = Url.serverPos+"getProduk?idTmpUsaha="+id_tempat_usaha;
        //Toast.makeText(WelcomeActivity.this, url, Toast.LENGTH_LONG).show();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int status = jsonObject.getInt("success");
                    JSONArray jsonArray = jsonObject.getJSONArray("result");
                    JSONObject json = jsonArray.getJSONObject(0);
                    if(status == 1){
                        int i;
                        for (i = 0; i < jsonArray.length(); i++) {
                            try {

                                JSONObject jO = jsonArray.getJSONObject(i);
                                ItemProduk id = new ItemProduk();
                                id.setId_barang(jO.getString("ID_BARANG"));
                                id.setNama_barang(jO.getString("NM_BARANG"));
                                id.setUrl_image(jO.getString("FOTO"));
                                id.setHarga(jO.getString("HARGA"));
                                id.setKeterangan(jO.getString("KETERANGAN"));
                                id.setStatus(true);

                                Log.d("NM_BARANG", jO.getString("NM_BARANG"));

                                itemProduks.add(id);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                progressDialog.dismiss();
                            }
                        }



                    }else{
                        Toast.makeText(getContext(), "Jaringan masih sibuk !", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //Toast.makeText(SinkronisasiActivity.this, response, Toast.LENGTH_SHORT).show();
                adapterProduk.notifyDataSetChanged();
                progressDialog.dismiss();


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("status","ok");

                return params;
            }
        };
        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });

        queue.add(stringRequest);
    }

    public void DialogEdit(String url_image, String nama_barang, String id_barang, String harga, String ket){
        final AlertDialog dialogBuilder = new AlertDialog.Builder(getContext()).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_produk, null);

        final EditText etNama, etKeterangan, etHarga;
        final Button btnSimpan;
        final ImageView ivGambarLama, ivTambahGambar;

        etNama = (EditText) dialogView.findViewById(R.id.etNama);
        etKeterangan = (EditText) dialogView.findViewById(R.id.etKeterangan);
        etHarga = (EditText) dialogView.findViewById(R.id.etHarga);

        btnSimpan = (Button) dialogView.findViewById(R.id.btnSimpan);

        ivGambarLama = (ImageView)dialogView.findViewById(R.id.ivGambarLama);
        ivGambarBaru = (ImageView)dialogView.findViewById(R.id.ivGambarBaru);
        ivTambahGambar = (ImageView)dialogView.findViewById(R.id.ivTambahGambar);

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                e_nama = etNama.getText().toString();
                e_harga = etHarga.getText().toString().replace(".", "");
                e_ket = etKeterangan.getText().toString();
                e_id = id_barang;
                e_gambar_lama = url_image;

                if(e_nama.trim().equalsIgnoreCase("")){
                    etNama.requestFocus();
                    etNama.setError("Silahkan isi form ini !");
                }else if(e_harga.trim().equalsIgnoreCase("")){
                    etHarga.requestFocus();
                    etHarga.setError("Silahkan isi form ini !");
                }else if(e_ket.trim().equalsIgnoreCase("")){
                    etKeterangan.requestFocus();
                    etKeterangan.setError("Silahkan isi form ini !");
                }else{
                    UpdateData();
                    dialogBuilder.dismiss();
                }

            }
        });

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

        etNama.setText(nama_barang);
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

        if(new CheckNetwork().checkingNetwork(getContext())){
            Glide.with(ivGambarLama.getContext()).load(Url.serverFoto+url_image)
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
                    .into(ivGambarLama);
        }else{
            Glide.with(ivGambarLama.getContext()).load(new File(url_image).toString())
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
                    .into(ivGambarLama);
        }


        ivGambarLama.setVisibility(View.VISIBLE);

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
                                        status = "e";
                                    }
                                } else {
                                    wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
                                    if (!wallpaperDirectory.exists()) {  // have the object build the directory structure, if needed.
                                        wallpaperDirectory.mkdirs();
                                    }

                                    showFileChooser();
                                    status = "e";
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
                                        tempNameFile = "Cam_"+sdf.format(cal.getTime())+".jpg";
                                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                        File f = new File(wallpaperDirectory, tempNameFile);
                                        Uri photoURI = FileProvider.getUriForFile(getContext(),
                                                BuildConfig.APPLICATION_ID + ".provider",
                                                f);
                                        //cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                                        startActivityForResult(cameraIntent, CAMERA_REQUEST);
                                        status = "e";
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
                                    status = "e";
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

                    if(e_nama_file.equalsIgnoreCase("")){

                    }
                    else{
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

                    if(t_nama_file.equalsIgnoreCase("")){

                    }
                    else{
                        File fl = new File(t_nama_file);
                        boolean deleted = fl.delete();
                    }

                    t_nama_file = file.getAbsolutePath();
                }

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
                            if(e_nama_file.equalsIgnoreCase("")){

                            }
                            else{
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

                            if(t_nama_file.equalsIgnoreCase("")){

                            }
                            else{
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

    private void UpdateData() {
        class UpdateData extends AsyncTask<Void, Integer, String> {

            //ProgressDialog uploading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressdialog = new ProgressDialog(getContext());
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
                    Toast.makeText(getContext(), "Data berhasil diupdate !", Toast.LENGTH_SHORT).show();
                    File fl = new File(e_nama_file);
                    boolean deleted = fl.delete();
                    e_nama_file = "";
                    getData();
                }else if(s.equalsIgnoreCase("gagal")){
                    if (progressdialog.isShowing())
                        progressdialog.dismiss();
                    Toast.makeText(getContext(), "Data gagal diupdate !", Toast.LENGTH_SHORT).show();
                }else{
                    if (progressdialog.isShowing())
                        progressdialog.dismiss();
                    Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            protected String doInBackground(Void... params) {

                UploadData u = new UploadData();
                String msg = null;
                msg = u.uploadDataUmum(e_nama,e_ket,e_harga, e_id, e_gambar_lama, e_nama_file);

                return msg;
            }
        }
        UpdateData uv = new UpdateData();
        uv.execute();
    }

    private void getDataLokal() {
        itemProduks.clear();
        adapterProduk.notifyDataSetChanged();

        int jml_data = databaseHandler.CountDataProduk();

        if(jml_data==0){
            tvKet.setVisibility(View.VISIBLE);
        }else{
            tvKet.setVisibility(View.GONE);
        }

        try {
            List<ItemProduk> listDataProduk = databaseHandler.getDataProduk();

            for (ItemProduk f : listDataProduk) {
                ItemProduk ip = new ItemProduk();
                ip.setId_barang(f.getId_barang());
                ip.setNama_barang(f.getNama_barang());
                ip.setHarga(f.getHarga());
                ip.setUrl_image(f.getUrl_image());
                ip.setKeterangan(f.getKeterangan());
                ip.setStatus(false);
                itemProduks.add(ip);
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        adapterProduk.notifyDataSetChanged();
    }


}
