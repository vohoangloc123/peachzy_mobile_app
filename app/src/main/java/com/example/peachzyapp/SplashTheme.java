package com.example.peachzyapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SplashTheme extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashTheme.this, SignIn.class);
                startActivity(intent);
                finish();  // Kết thúc splash screen activity sau khi chuyển sang SignIn
            }
        }, 3000); // Chờ 2 giây trước khi chuyển sang SignIn
    }


}
