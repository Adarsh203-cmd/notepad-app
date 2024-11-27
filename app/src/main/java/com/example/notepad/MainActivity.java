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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private Button signupButton, resetButton, mlogin;
    private EditText mloginemail, mloginpassword;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Make the status bar transparent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        // Initialize buttons and fields
        signupButton = findViewById(R.id.signUpButton);
        resetButton = findViewById(R.id.buttonReset);
        mloginemail = findViewById(R.id.emailInput);
        mloginpassword = findViewById(R.id.passwordInput);
        mlogin = findViewById(R.id.loginButton);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        // Check if the user is already logged in
        if (firebaseUser != null) {
            // User is already logged in, navigate to Splash screen
            navigateToSplash();
        }

        // Set onClickListener for sign-up button
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignUp.class);
                startActivity(intent);
            }
        });

        // Set onClickListener for reset password button
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, ForgotPassword.class);
                startActivity(i);
            }
        });

        mlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mail = mloginemail.getText().toString().trim();
                String password = mloginpassword.getText().toString().trim();

                if (mail.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "All fields are Required", Toast.LENGTH_SHORT).show();
                } else if (password.length() < 7) {
                    Toast.makeText(MainActivity.this, "Password should be greater than 7 digits", Toast.LENGTH_SHORT).show();
                } else {
                    // Log in the user
                    firebaseAuth.signInWithEmailAndPassword(mail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                checkMailVerification();  // Verify email and proceed
                            } else {
                                // Handle failure cases
                                Exception exception = task.getException();
                                if (exception instanceof FirebaseAuthException) {
                                    String errorCode = ((FirebaseAuthException) exception).getErrorCode();

                                    switch (errorCode) {
                                        case "ERROR_INVALID_EMAIL":
                                            Toast.makeText(MainActivity.this, "The email address is badly formatted.", Toast.LENGTH_SHORT).show();
                                            break;
                                        case "ERROR_WRONG_PASSWORD":
                                            Toast.makeText(MainActivity.this, "Invalid password.", Toast.LENGTH_SHORT).show();
                                            break;
                                        case "ERROR_USER_NOT_FOUND":
                                            Toast.makeText(MainActivity.this, "Account doesn't exist.", Toast.LENGTH_SHORT).show();
                                            break;
                                        default:
                                            Toast.makeText(MainActivity.this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                                            break;
                                    }
                                } else {
                                    // Handle generic errors
                                    Toast.makeText(MainActivity.this, "An error occurred: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            }
        });
    }


        // Check if email is verified before logging in
    private void checkMailVerification() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {  // Check to avoid NullPointerException
            if (firebaseUser.isEmailVerified()) {
                Toast.makeText(MainActivity.this, "Logged In", Toast.LENGTH_SHORT).show();
                navigateToSplash();  // Navigate to Splash screen after login
            } else {
                Toast.makeText(MainActivity.this, "Verify your mail first", Toast.LENGTH_SHORT).show();
                firebaseAuth.signOut();
            }
        } else {
            Toast.makeText(MainActivity.this, "Failed to authenticate", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToSplash() {
        Intent intent = new Intent(MainActivity.this, MainSplash.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear the activity stack
        finish(); // Finish MainActivity
        startActivity(intent); // Start Splash screen
    }
}
