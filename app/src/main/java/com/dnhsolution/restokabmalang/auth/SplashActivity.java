package com.dnhsolution.restokabmalang.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.dnhsolution.restokabmalang.MainActivity;
import com.dnhsolution.restokabmalang.R;
import com.dnhsolution.restokabmalang.utilities.Url;

public class SplashActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE);
        final String status = sharedPreferences.getString(Url.SESSION_STS_LOGIN, "0");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (status != null) {
                    if(status.equalsIgnoreCase("0")){
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    }else{
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    }
                }

                finish();

            }
        },2000);
    }
}
