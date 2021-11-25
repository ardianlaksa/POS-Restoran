package com.dnhsolution.restokabmalang.auth;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dnhsolution.restokabmalang.MainActivity;
import com.dnhsolution.restokabmalang.R;
import com.dnhsolution.restokabmalang.utilities.Url;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoginActivity extends AppCompatActivity {

    EditText etUsername, etPassword, et1, et2, et3, et4;
    Button btnLogin, bPassword;
    Boolean bvisible = true;

    LinearLayout LAktivasi, LLogin, LKeterangan;
    String kode_aktivasi = "";
    private String uniqueID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etPassword = findViewById(R.id.etPassword);
        etUsername = findViewById(R.id.etUsername);
        btnLogin = findViewById(R.id.btnLogin);
        bPassword =  findViewById(R.id.bPassword);

        LAktivasi = findViewById(R.id.LAktivasi);
        LLogin = findViewById(R.id.LLogin);
        LKeterangan = findViewById(R.id.LKeterangan);
        et1 = findViewById(R.id.et1);
        et2 = findViewById(R.id.et2);
        et3 = findViewById(R.id.et3);
        et4 = findViewById(R.id.et4);
        uniqueID = UUID.randomUUID().toString();

        et1.requestFocus();
        et1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                et1.requestFocus();

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(et1.getText().toString().length()==0){
                    et1.requestFocus();
                }else{
                    et2.requestFocus();
                }

            }
        });
        et2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                et2.requestFocus();

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(et2.getText().toString().length()==0){
                    et1.requestFocus();
                }else{
                    et3.requestFocus();
                }

            }
        });
        et3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                et3.requestFocus();

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(et3.getText().toString().length()==0){
                    et2.requestFocus();
                }else{
                    et4.requestFocus();
                }

            }
        });
        et4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                et4.requestFocus();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(et4.getText().toString().length()==0){
                    et3.requestFocus();
                }else{
                    if(et1.getText().toString().equalsIgnoreCase("")){
                        et1.requestFocus();
                        et1.setError("Harap isi bidang ini !");
                    }else if(et2.getText().toString().equalsIgnoreCase("")){
                        et2.requestFocus();
                        et2.setError("Harap isi bidang ini !");
                    }else if(et3.getText().toString().equalsIgnoreCase("")){
                        et3.requestFocus();
                        et3.setError("Harap isi bidang ini !");
                    }else if(et4.getText().toString().equalsIgnoreCase("")){
                        et4.requestFocus();
                        et4.setError("Harap isi bidang ini !");
                    }else{
                        String ka = et1.getText().toString()+et2.getText().toString()+et3.getText().toString()
                                +et4.getText().toString();

                        sendAktivasi(ka);
                    }
                }

            }
        });

        bPassword.setOnClickListener(v -> visible());

        btnLogin.setOnClickListener(v -> {
            btnLogin.setEnabled(false);
                String a = etUsername.getText().toString().trim();
                String b = etPassword.getText().toString().trim();

                if(a.equalsIgnoreCase("")){
                    etUsername.requestFocus();
                    etUsername.setError("Username tidak boleh kosong !");
                    btnLogin.setEnabled(true);
                }else if(b.equalsIgnoreCase("")){
                    etPassword.requestFocus();
                    etPassword.setError("Password tidak boleh kosong !");
                    btnLogin.setEnabled(true);
                }else{
                    sendData();
                }
        });

        etUsername.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    etPassword.requestFocus();
                    handled = true;
                }
                return handled;
            }
        });
    }

    @SuppressLint("ResourceAsColor")
    public void visible() {
        //Toast.makeText(getApplicationContext(), "Coba", Toast.LENGTH_SHORT).show();
        if(bvisible) {
            etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bPassword.setBackground(getResources().getDrawable(R.drawable.ic_visibility_off,null));
            }else
                bPassword.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_visibility_off));
            bvisible = false;
        }else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bPassword.setBackground(getResources().getDrawable(R.drawable.ic_visibility,null));
            }else
                bPassword.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_visibility));
            bvisible = true;
        }
        etPassword.setSelection(etPassword.length());
    }

    public void sendAktivasi(final String kode){

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
        String url = Url.serverPos+"Aktivasi";
        //Toast.makeText(WelcomeActivity.this, url, Toast.LENGTH_LONG).show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("result");
                    JSONObject json = jsonArray.getJSONObject(0);
                    String pesan = json.getString("pesan");
                    if(pesan.equalsIgnoreCase("0")){
                        Toast.makeText(LoginActivity.this, "Kode Aktivasi Tidak Valid !", Toast.LENGTH_LONG).show();
                    }else if(pesan.equalsIgnoreCase("1")){
                        Toast.makeText(LoginActivity.this, "Aktivasi Berhasil", Toast.LENGTH_SHORT).show();
                        LAktivasi.setVisibility(View.GONE);
                        LKeterangan.setVisibility(View.GONE);
                        LLogin.setVisibility(View.VISIBLE);
                        kode_aktivasi = kode;
                    }else{
                        Toast.makeText(LoginActivity.this, "Jaringan masih sibuk !", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(LoginActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("kode_aktivasi", kode);

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

    public void sendData(){

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
        String url = Url.serverPos+"Auth";
        //Toast.makeText(WelcomeActivity.this, url, Toast.LENGTH_LONG).show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println(response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.i("json",jsonObject.toString());
                    JSONArray jsonArray = jsonObject.getJSONArray("result");
                    JSONObject json = jsonArray.getJSONObject(0);
                    String pesan = json.getString("pesan");
                    if(pesan.equalsIgnoreCase("0")){
                        Toast.makeText(LoginActivity.this, "Gagal Login. Username atau Password salah !", Toast.LENGTH_SHORT).show();
                    }else if(pesan.equalsIgnoreCase("1")){
                        json = jsonArray.getJSONObject(1);
                        Log.i("json1",json.toString());
                        String idTempatusaha = json.getString("ID_TEMPAT_USAHA");
                        String idPengguna = json.getString("ID_PENGGUNA");
                        String nmTempatUsaha = json.getString("NM_TEMPAT_USAHA");
                        String label = json.getString("LABEL_APP");
                        String tema = json.getString("THEME_APP");
                        String alamat = json.getString("ALAMAT");
                        String email = json.getString("EMAIL");
                        String telp = json.getString("TELP");
                        String tipeStruk = json.getString("TIPE_STRUK");
                        String isCetakBilling = json.getString("ISCETAK_BILLING");

                        SharedPreferences sharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE);

                        //membuat editor untuk menyimpan data ke shared preferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        //menambah data ke editor
                        editor.putString(Url.SESSION_USERNAME, etUsername.getText().toString());
                        editor.putString(Url.SESSION_ID_PENGGUNA,idPengguna);
                        editor.putString(Url.SESSION_ID_TEMPAT_USAHA,idTempatusaha);
                        editor.putString(Url.SESSION_NAMA_TEMPAT_USAHA,nmTempatUsaha);
                        editor.putString(Url.SESSION_ALAMAT,alamat);
                        editor.putString(Url.SESSION_EMAIL,email);
                        editor.putString(Url.SESSION_TELP,telp);
                        editor.putString(Url.setLabel,label);
                        editor.putString(Url.setTema,tema);
                        editor.putString(Url.SESSION_STS_LOGIN, "1");
                        editor.putString(Url.SESSION_TIPE_STRUK,tipeStruk);
                        editor.putString(Url.SESSION_ISCETAK_BILLING,isCetakBilling);
                        editor.putString(Url.SESSION_UUID,uniqueID);

                        //menyimpan data ke editor
                        editor.apply();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));

                        finishAffinity();
                    }else{
                        Toast.makeText(LoginActivity.this, "Jaringan masih sibuk !", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //Toast.makeText(LoginActivity.this, response, Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }, error -> {
            progressDialog.dismiss();
            btnLogin.setEnabled(true);
            Toast.makeText(LoginActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
        }){
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<>();
                params.put("username", etUsername.getText().toString());
                params.put("password", etPassword.getText().toString());
                params.put("kode_aktivasi", kode_aktivasi);
                params.put("UUID", uniqueID);
                Log.d("getParams", params.toString());

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
}
