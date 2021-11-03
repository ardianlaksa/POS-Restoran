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
import com.dnhsolution.restokabmalang.database.DatabaseHandler;
import com.dnhsolution.restokabmalang.sistem.produk.ProdukMasterActivity;
import com.dnhsolution.restokabmalang.utilities.Url;

import java.util.Objects;

public class MainMaster extends AppCompatActivity {

    Button btnInput, btnTheme, btnProduk, btnLogout;
    SharedPreferences sharedPreferences;
    DatabaseHandler databaseHandler = null;

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
        Objects.requireNonNull(getSupportActionBar()).setTitle(label);
        databaseHandler = new DatabaseHandler(this);

        btnInput = (Button)findViewById(R.id.bInput);
        btnTheme = (Button)findViewById(R.id.bTheme);
        btnProduk = (Button)findViewById(R.id.bProduk);
        btnLogout = (Button)findViewById(R.id.bLogout);

        btnProduk.setText(R.string.title_daftar_produk);

        btnInput.setVisibility(View.GONE);

        btnInput.setOnClickListener(v -> {
            //startActivity(new Intent(MainMaster.this, SistemMasterActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE);

            //membuat editor untuk menyimpan data ke shared preferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            databaseHandler.deleteAllTable();
            startActivity(new Intent(getApplicationContext(), SplashActivity.class));
            finishAffinity();
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
                startActivity(new Intent(MainMaster.this, ProdukMasterActivity.class));
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
