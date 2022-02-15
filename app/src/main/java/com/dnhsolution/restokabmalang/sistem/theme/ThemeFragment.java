package com.dnhsolution.restokabmalang.sistem.theme;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dnhsolution.restokabmalang.R;
import com.dnhsolution.restokabmalang.utilities.ThemeApplication;
import com.dnhsolution.restokabmalang.utilities.Url;
import com.dnhsolution.restokabmalang.utilities.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ThemeFragment extends AppCompatActivity {

    private Spinner spThemes;
    EditText etLabel;
    Button bSimpan;
    SharedPreferences sharedPreferences;
    int theme = 0;
    // Here we set the theme for the activity
    // Note `Utils.onActivityCreateSetTheme` must be called before `setContentView`
    String tema = "0";
    String label, temp_tema;
    AlertDialog.Builder dialog;
    LayoutInflater inflater;
    View dialogView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE);
        label = sharedPreferences.getString(Url.setLabel, "0");
        tema = sharedPreferences.getString(Url.setTema, "0");
        temp_tema = sharedPreferences.getString(Url.SESSION_TEMP_TEMA, "null");

        if(temp_tema.equalsIgnoreCase("null")){
            if(tema.equalsIgnoreCase("0")){
                ThemeFragment.this.setTheme(R.style.Theme_First);
            }else if(tema.equalsIgnoreCase("1")){
                ThemeFragment.this.setTheme(R.style.Theme_Second);
            }else if(tema.equalsIgnoreCase("2")){
                ThemeFragment.this.setTheme(R.style.Theme_Third);
            }else if(tema.equalsIgnoreCase("3")){
                ThemeFragment.this.setTheme(R.style.Theme_Fourth);
            }else if(tema.equalsIgnoreCase("4")){
                ThemeFragment.this.setTheme(R.style.Theme_Fifth);
            }else if(tema.equalsIgnoreCase("5")){
                ThemeFragment.this.setTheme(R.style.Theme_Sixth);
            }
        }else{
            Utils.onActivityCreateSetTheme(this);
        }
        // MUST BE SET BEFORE setContentView
        // AFTER SETTING THEME
        setContentView(R.layout.fragment_sistem);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(label);

        spThemes = findViewById(R.id.spThemes);
        theme = Integer.parseInt(tema);


        setupSpinnerItemSelection();


        if(temp_tema.equalsIgnoreCase("null")){
            spThemes.setSelection(theme);
        }

        etLabel = (EditText)findViewById(R.id.etLabel);
        bSimpan = (Button)findViewById(R.id.bSimpan);
        //spThemes = findViewById(R.id.spThemes);


        bSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etLabel.getText().toString().equalsIgnoreCase("")){
                    etLabel.requestFocus();
                    etLabel.setError("Silahkan isi form ini !");
                }else{
                    sendData();
                }
            }
        });

        etLabel.setText(label);

    }


    private void setupSpinnerItemSelection() {

        //ThemeApplication.currentPosition = theme;
        spThemes.setSelection(ThemeApplication.currentPosition);
        ThemeApplication.currentPosition = spThemes.getSelectedItemPosition();
        spThemes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if (ThemeApplication.currentPosition != position) {
                    Utils.changeToTheme(ThemeFragment.this, position);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    //menambah data ke editor
                    editor.putString(Url.SESSION_TEMP_TEMA, String.valueOf(position));

                    //menyimpan data ke editor
                    editor.apply();
                    //theme = position;
                }

                theme = position;
                //Toast.makeText(ThemeFragment.this, String.valueOf(theme), Toast.LENGTH_SHORT).show();
                ThemeApplication.currentPosition = position;
                //System.out.println(theme);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //spThemes.setSelection(theme);
            }
        });
    }

    public void sendData(){

        final ProgressDialog progressDialog = new ProgressDialog(ThemeFragment.this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        RequestQueue queue = Volley.newRequestQueue(ThemeFragment.this);
        String url = Url.serverPos+"UpdateLabel";
        //Toast.makeText(WelcomeActivity.this, url, Toast.LENGTH_LONG).show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.i("json",jsonObject.toString());
                    JSONArray jsonArray = jsonObject.getJSONArray("result");
                    JSONObject json = jsonArray.getJSONObject(0);
                    String pesan = json.getString("pesan");
                    if(pesan.equalsIgnoreCase("0")){
                        Toast.makeText(ThemeFragment.this, "Gagal update label dan tema !", Toast.LENGTH_SHORT).show();
                    }else if(pesan.equalsIgnoreCase("1")){

                        //sharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE);

                        //membuat editor untuk menyimpan data ke shared preferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        //menambah data ke editor
                        editor.putString(Url.setLabel, etLabel.getText().toString());
                        editor.putString(Url.setTema, String.valueOf(theme));

                        //menyimpan data ke editor
                        editor.apply();
                        Toast.makeText(ThemeFragment.this, "Berhasil update label & tema !", Toast.LENGTH_SHORT).show();
                        DialogForm();
                    }else{
                        Toast.makeText(ThemeFragment.this, "Jaringan masih sibuk !", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //Toast.makeText(LoginActivity.this, response, Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(ThemeFragment.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                //sharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE);
                final String kd_pengguna = sharedPreferences.getString(Url.SESSION_ID_PENGGUNA, "0");
                final String id_tempat_usaha = sharedPreferences.getString(Url.SESSION_ID_TEMPAT_USAHA, "");

                params.put("kd_pengguna",kd_pengguna);
                params.put("id_tempat_usaha",id_tempat_usaha);
                params.put("label", etLabel.getText().toString());
                params.put("tema", String.valueOf(theme));

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
            public void retry(VolleyError error) {

            }
        });

        queue.add(stringRequest);

    }

    private void DialogForm() {
        dialog = new AlertDialog.Builder(ThemeFragment.this);
        inflater = getLayoutInflater();
        dialogView = inflater.inflate(R.layout.dialog_action, null);
        dialog.setView(dialogView);
        dialog.setCancelable(true);
        dialog.setIcon(R.drawable.ic_logo);
        dialog.setTitle("Pemberitahuan");


        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        dialog.show();

    }
}
