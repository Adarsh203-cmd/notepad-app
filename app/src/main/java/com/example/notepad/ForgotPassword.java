package com.example.notepad;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {
    ImageView arrowback;
    private EditText mforgotpassword;
    private Button mpasswordrecoverbutton;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Make the status bar transparent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        arrowback = findViewById(R.id.backArrow);
        mforgotpassword = findViewById(R.id.etForgotPasswordEmail);
        mpasswordrecoverbutton = findViewById(R.id.btnSendLink);
        firebaseAuth = FirebaseAuth.getInstance();

        arrowback.setOnClickListener(view -> {
            Intent intent = new Intent(ForgotPassword.this, MainActivity.class);
            startActivity(intent);
        });

        mpasswordrecoverbutton.setOnClickListener(v -> {
            String mail = mforgotpassword.getText().toString().trim();
            if (mail.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Enter your mail first", Toast.LENGTH_SHORT).show();
            } else {
                firebaseAuth.sendPasswordResetEmail(mail).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPassword.this, "Mail sent, you can recover your password using mail.", Toast.LENGTH_SHORT).show();
                        firebaseAuth.signOut();
                        Intent intent = new Intent(ForgotPassword.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(ForgotPassword.this, "Email is incorrect or account does not exist.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
