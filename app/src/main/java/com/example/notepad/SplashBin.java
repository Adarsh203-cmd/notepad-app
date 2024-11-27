package com.example.notepad;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashBin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_bin);

        ImageView splashImage = findViewById(R.id.splash_image);
        TextView splashText = findViewById(R.id.splash_text);

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashBin.this, RecycleBin.class));
            finish();
        }, 1500);
    }
}
