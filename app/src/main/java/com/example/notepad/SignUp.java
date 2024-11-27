package com.example.notepad;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {

    private Button loginButton, msignup;
    private ImageView backArrow;
    private EditText msignupemail, msignuppassword, mconfirmpassword;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);


        loginButton = findViewById(R.id.loginButton);
        msignupemail = findViewById(R.id.emailInput);
        msignuppassword = findViewById(R.id.passwordInput);
        mconfirmpassword = findViewById(R.id.confirmPasswordInput);
        msignup = findViewById(R.id.signUpButton);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        // Navigate to login screen
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SignUp.this, MainActivity.class);
                startActivity(i);
            }
        });


        // Navigate back to previous screen
        backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SignUp.this, MainActivity.class);
                startActivity(i);
            }
        });

        // Sign up logic
        msignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mail = msignupemail.getText().toString().trim();
                String password = msignuppassword.getText().toString().trim();
                String confirmPassword = mconfirmpassword.getText().toString().trim(); // Get confirm password input

                if (mail.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(SignUp.this, "All fields are required", Toast.LENGTH_SHORT).show();
                } else if (password.length() < 7) {
                    Toast.makeText(SignUp.this, "Password should be greater than 7 characters", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirmPassword)) {
                    Toast.makeText(SignUp.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                } else if (!isAlphanumeric(password)) {
                    Toast.makeText(SignUp.this, "Password must contain at least one letter and one number", Toast.LENGTH_SHORT).show();
                } else {
                    firebaseAuth.createUserWithEmailAndPassword(mail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                                if (firebaseUser != null) {
                                    String userId = firebaseUser.getUid(); // Get user's UID

                                    // Create a map to store user data
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("email", mail);

                                    user.put("createdAt", System.currentTimeMillis());

                                    // Save user data in Firestore under "users" collection
                                    db.collection("users").document(userId).set(user)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(SignUp.this, "User Data Saved", Toast.LENGTH_SHORT).show();
                                                    sendEmailVerification(); // Proceed to email verification
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(SignUp.this, "Failed to Save User Data", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            } else {
                                Toast.makeText(SignUp.this, "Failed to Register", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            // Method to check if the password is alphanumeric
            private boolean isAlphanumeric(String password) {
                String regex = "^(?=.*[a-zA-Z])(?=.*[0-9]).+$";
                return password.matches(regex);
            }
        });
    }


        // Send email verification
    private void sendEmailVerification() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(getApplicationContext(), "Verification Email Sent. Verify and Login again.", Toast.LENGTH_SHORT).show();
                    firebaseAuth.signOut();
                    finish();
                    startActivity(new Intent(SignUp.this, MainActivity.class));
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "Failed to Send Verification Email", Toast.LENGTH_SHORT).show();
        }
    }
}
