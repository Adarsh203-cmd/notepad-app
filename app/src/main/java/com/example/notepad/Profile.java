package com.example.notepad;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class Profile extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1; // Request code for image picking
    private String email, user;
    private Button logout, deleteAccount;
    private TextView userName, emailId, welcomeMessage;
    private ImageButton backButton, changePasswordIcon, addImageButton; // Added addImageButton
    private ImageView avatar; // ImageView to display the avatar
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference; // Reference for Firebase Storage
    private FirebaseStorage firebaseStorage;
    private Uri imageUri; // Uri to hold the selected image

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize UI elements
        logout = findViewById(R.id.logout);
        deleteAccount = findViewById(R.id.deleteaccount);
        userName = findViewById(R.id.userid);
        emailId = findViewById(R.id.email);
        backButton = findViewById(R.id.back_button);
        welcomeMessage = findViewById(R.id.welcome_message);
        changePasswordIcon = findViewById(R.id.change_password_icon);
        addImageButton = findViewById(R.id.add_image_button); // Initialized ImageButton
        avatar = findViewById(R.id.avatar); // Initialize ImageView for avatar

        // Initialize Firebase components
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference("profile_images"); // Folder for profile images

        // Fetch user data from Firestore
        if (firebaseUser != null) {
            DocumentReference documentReference = firebaseFirestore.collection("users").document(firebaseUser.getUid());

            documentReference.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        email = documentSnapshot.getString("email");
                        String profileImageUrl = documentSnapshot.getString("profileImage"); // Get profile image URL
                        if (email != null) {
                            int idx = email.lastIndexOf("@");
                            user = email.substring(0, idx);
                            userName.setText(user);
                            emailId.setText(email);
                            welcomeMessage.setText("Welcome to your space, " + user + "!");
                            welcomeMessage.setVisibility(View.VISIBLE);
                            // Load profile image if available
                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                // Load image using Glide or Picasso (example using Glide)
                                Glide.with(this).load(profileImageUrl).into(avatar);
                            }
                        } else {
                            userName.setText("Unknown User");
                            emailId.setText("Email not found");
                        }
                    } else {
                        userName.setText("User not found");
                        emailId.setText("Email not found");
                    }
                } else {
                    userName.setText("Error fetching user data");
                    emailId.setText(task.getException().getMessage());
                }
            });
        } else {
            userName.setText("No user signed in");
            emailId.setText("No email available");
        }

        // Back Button
        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(Profile.this, HomePage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // Logout Button
        logout.setOnClickListener(view -> {
            new AlertDialog.Builder(Profile.this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        firebaseAuth.signOut();
                        finish();
                        startActivity(new Intent(Profile.this, MainActivity.class));
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // Delete Account Button
        deleteAccount.setOnClickListener(view -> {
            if (firebaseUser != null) {
                new AlertDialog.Builder(Profile.this)
                        .setTitle("Delete Account")
                        .setMessage("Are you sure you want to delete your account?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            DocumentReference documentReference = firebaseFirestore.collection("users").document(firebaseUser.getUid());

                            documentReference.delete().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    firebaseUser.delete().addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Intent intent = new Intent(Profile.this, SplashActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(Profile.this, "Error deleting account: " + task1.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                } else {
                                    Toast.makeText(Profile.this, "Error deleting user data: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show();
            } else {
                Toast.makeText(Profile.this, "No user signed in", Toast.LENGTH_SHORT).show();
            }
        });

        // Change Password Button (OnClickListener for ">" button)
        changePasswordIcon.setOnClickListener(view -> {
            // Navigate to ResetPasswordActivity
            Intent intent = new Intent(Profile.this, ForgotPassword.class);
            startActivity(intent);
        });

        // Add Image Button (to change profile picture)
        addImageButton.setOnClickListener(view -> showImageOptionsDialog());
    }

    // Show dialog options for changing avatar
    private void showImageOptionsDialog() {
        String[] options = {"Gallery", "Delete image"}; // Removed Change Avatar option

        AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
        builder.setTitle("Select Option")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Gallery
                            openGallery();
                            break;
                        case 1: // Delete Avatar
                            deleteAvatar();
                            break;
                    }
                });
        builder.create().show();
    }

    // Method to delete avatar
    private void deleteAvatar() {
        // Remove avatar image from Firebase Storage and Firestore
        StorageReference fileReference = storageReference.child(firebaseUser.getUid() + ".jpg");
        fileReference.delete().addOnSuccessListener(aVoid -> {
            // Update Firestore to remove the profileImage field
            DocumentReference documentReference = firebaseFirestore.collection("users").document(firebaseUser.getUid());
            documentReference.update("profileImage", null).addOnSuccessListener(aVoid1 -> {
                avatar.setImageResource(R.drawable.user); // Set a default image or clear the avatar
                Toast.makeText(Profile.this, "profile image deleted successfully!", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(Profile.this, "Error updating profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(Profile.this, "Error deleting avatar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    // Open Gallery to select an image
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Handle the result of the image selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            if (imageUri != null) {
                // Set the selected image to the ImageView
                avatar.setImageURI(imageUri);
                uploadImageToFirebase(imageUri); // Call method to upload image to Firebase
            } else {
                Toast.makeText(this, "Error selecting image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to upload image to Firebase Storage
    private void uploadImageToFirebase(Uri imageUri) {
        if (firebaseUser != null) {
            // Create a unique filename for the image
            StorageReference fileReference = storageReference.child(firebaseUser.getUid() + ".jpg");
            UploadTask uploadTask = fileReference.putFile(imageUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Get the download URL and update Firestore
                fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    DocumentReference documentReference = firebaseFirestore.collection("users").document(firebaseUser.getUid());
                    documentReference.update("profileImage", uri.toString()).addOnSuccessListener(aVoid -> {
                        Toast.makeText(Profile.this, "Profile image updated successfully!", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(Profile.this, "Error updating profile image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(Profile.this, "Error uploading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "No user signed in", Toast.LENGTH_SHORT).show();
        }
    }
}
