package com.dnhsolution.restokabmalang.sistem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.dnhsolution.restokabmalang.R;
import com.dnhsolution.restokabmalang.auth.SplashActivity;
import com.dnhsolution.restokabmalang.sistem.produk.MasterProduk;
import com.dnhsolution.restokabmalang.utilities.Url;

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
