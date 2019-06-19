package com.dnhsolution.restokabmalang.auth;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dnhsolution.restokabmalang.MainActivity;
import com.dnhsolution.restokabmalang.R;
import com.dnhsolution.restokabmalang.Url;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    Button btnLogin, bPassword;
    Boolean bvisible = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etPassword = (EditText)findViewById(R.id.etPassword);
        etUsername = (EditText)findViewById(R.id.etUsername);
        btnLogin = (Button)findViewById(R.id.btnLogin);
        bPassword = (Button) findViewById(R.id.bPassword);

        bPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visible();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    String a = etUsername.getText().toString().trim();
                    String b = etPassword.getText().toString().trim();

                    if(a.equalsIgnoreCase("")){
                        etUsername.requestFocus();
                        etUsername.setError("Username tidak boleh kosong !");
                    }else if(b.equalsIgnoreCase("")){
                        etPassword.requestFocus();
                        etPassword.setError("Password tidak boleh kosong !");
                    }else{
                        sendData();
                    }
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

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("result");
                    JSONObject json = jsonArray.getJSONObject(0);
                    String pesan = json.getString("pesan");
                    if(pesan.equalsIgnoreCase("0")){
                        Toast.makeText(LoginActivity.this, "Gagal Login. Username atau Password salah !", Toast.LENGTH_SHORT).show();
                    }else if(pesan.equalsIgnoreCase("1")){
                        //String username = json.getString("username");
//                        String dptb1 = json.getString("dptb_1");
//                        String dptb2 = json.getString("dptb_2");
//                        String dpph = json.getString("dpph");

                        SharedPreferences sharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE);

                        //membuat editor untuk menyimpan data ke shared preferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        //menambah data ke editor
                        editor.putString(Url.SESSION_USERNAME, etUsername.getText().toString());
                        editor.putString(Url.SESSION_STS_LOGIN, "1");

                        //menyimpan data ke editor
                        editor.apply();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));

                        finish();
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
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("username", etUsername.getText().toString());
                params.put("password", etPassword.getText().toString());

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
}
