package com.dnhsolution.restokabmalang.sistem;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dnhsolution.restokabmalang.BuildConfig;
import com.dnhsolution.restokabmalang.MainActivity;
import com.dnhsolution.restokabmalang.R;
import com.dnhsolution.restokabmalang.auth.LoginActivity;
import com.dnhsolution.restokabmalang.auth.SplashActivity;
import com.dnhsolution.restokabmalang.cetak.MainCetak;
import com.dnhsolution.restokabmalang.sistem.master.SistemMasterActivity;
import com.dnhsolution.restokabmalang.sistem.produk.MasterProduk;
import com.dnhsolution.restokabmalang.sistem.produk.RealPathUtil;
import com.dnhsolution.restokabmalang.sistem.produk.UploadData;
import com.dnhsolution.restokabmalang.utilities.Url;
import com.dnhsolution.restokabmalang.utilities.Utils;
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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainMaster extends AppCompatActivity {

    Button btnInput, btnTheme, btnProduk, btnLogout;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE);
        String label = sharedPreferences.getString(Url.setLabel, "Belum disetting");
        String tema = sharedPreferences.getString(Url.setTema, "0");
        if(tema.equalsIgnoreCase("0")){
            MainMaster.this.setTheme(R.style.Theme_First);
        }else if(tema.equalsIgnoreCase("1")){
            MainMaster.this.setTheme(R.style.Theme_Second);
        }else if(tema.equalsIgnoreCase("2")){
            MainMaster.this.setTheme(R.style.Theme_Third);
        }else if(tema.equalsIgnoreCase("3")){
            MainMaster.this.setTheme(R.style.Theme_Fourth);
        }else if(tema.equalsIgnoreCase("4")){
            MainMaster.this.setTheme(R.style.Theme_Fifth);
        }else if(tema.equalsIgnoreCase("5")){
            MainMaster.this.setTheme(R.style.Theme_Sixth);
        }

        setContentView(R.layout.activity_main_master);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(label);


        btnInput = (Button)findViewById(R.id.bInput);
        btnTheme = (Button)findViewById(R.id.bTheme);
        btnProduk = (Button)findViewById(R.id.bProduk);
        btnLogout = (Button)findViewById(R.id.bLogout);

        btnProduk.setText("Daftar Produk");

        btnInput.setVisibility(View.GONE);

        btnInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(MainMaster.this, SistemMasterActivity.class));
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE);

                //membuat editor untuk menyimpan data ke shared preferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Url.SESSION_STS_LOGIN, "0");

                //menyimpan data ke editor
                editor.apply();
                startActivity(new Intent(getApplicationContext(), SplashActivity.class));

                finish();
            }
        });

        btnTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainMaster.this, SistemFragment.class));
            }
        });

        btnProduk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainMaster.this, MasterProduk.class));
            }
        });

        SharedPreferences.Editor editor = sharedPreferences.edit();

        //menambah data ke editor
        editor.putString(Url.SESSION_TEMP_TEMA, "null");

        //menyimpan data ke editor
        editor.apply();

    }

    @Override
    protected void onResume() {
        super.onResume();
        String label = sharedPreferences.getString(Url.setLabel, "Belum disetting");
        getSupportActionBar().setTitle(label);
        String tema = sharedPreferences.getString(Url.setTema, "0");
        if(tema.equalsIgnoreCase("0")){
            MainMaster.this.setTheme(R.style.Theme_First);
        }else if(tema.equalsIgnoreCase("1")){
            MainMaster.this.setTheme(R.style.Theme_Second);
        }else if(tema.equalsIgnoreCase("2")){
            MainMaster.this.setTheme(R.style.Theme_Third);
        }else if(tema.equalsIgnoreCase("3")){
            MainMaster.this.setTheme(R.style.Theme_Fourth);
        }else if(tema.equalsIgnoreCase("4")){
            MainMaster.this.setTheme(R.style.Theme_Fifth);
        }else if(tema.equalsIgnoreCase("5")){
            MainMaster.this.setTheme(R.style.Theme_Sixth);
        }
    }

}
