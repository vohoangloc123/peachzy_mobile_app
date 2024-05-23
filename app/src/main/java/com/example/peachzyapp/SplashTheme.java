package com.example.peachzyapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SplashTheme extends AppCompatActivity {

    String uid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_layout);  // Set content view là layout mới tạo

        ImageView imageView = findViewById(R.id.imageView5);
        Animation rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_anim);
        imageView.startAnimation(rotateAnimation);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashTheme.this, MainActivity.class);
                uid = getIntent().getStringExtra("uid");
                intent.putExtra("uid", String.valueOf(uid));
                startActivity(intent);
                finish();  // Kết thúc splash screen activity sau khi chuyển sang MainActivity
            }
        }, 500); // Chờ 3 giây trước khi chuyển sang MainActivity
    }


}
