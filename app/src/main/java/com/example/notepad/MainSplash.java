package com.example.notepad;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainSplash extends AppCompatActivity {

    private TextView welcomeMessage;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_splash);

        welcomeMessage = findViewById(R.id.welcomeMessage);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            String email = firebaseUser.getEmail();
            int idx = email != null ? email.indexOf("@") : -1;
            String userName = idx != -1 ? email.substring(0, idx) : "User";
            welcomeMessage.setText("Welcome to NoteBox, " + userName + "!");
            welcomeMessage.setVisibility(View.VISIBLE);
        } else {
            welcomeMessage.setText("Welcome!");
        }

        // Delay before navigating to HomePage
        new Handler().postDelayed(() -> {
            startActivity(new Intent(MainSplash.this, HomePage.class));
            finish();  // Finish SplashActivity so it's not on the back stack
        }, 3000);
    }
}
