package com.example.notepad;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateNote extends AppCompatActivity {

    private EditText mcreatetitleofnote, mcreatecontentofnote;
    private TextView mdatetime;
    private FloatingActionButton msavenote;

    private ImageButton imageButton;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        msavenote = findViewById(R.id.savenote);
        mcreatetitleofnote = findViewById(R.id.createtitleofnote);
        mcreatecontentofnote = findViewById(R.id.createcontentofnote);
        mdatetime = findViewById(R.id.datetime);

        imageButton = findViewById(R.id.btnBack);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault());
        String currentDateAndTime = sdf.format(new Date());
        mdatetime.setText(currentDateAndTime);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        msavenote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = mcreatetitleofnote.getText().toString();
                String content = mcreatecontentofnote.getText().toString();
                if(title.isEmpty() || content.isEmpty()){
                    Toast.makeText(CreateNote.this, "Title & Content field are Required", Toast.LENGTH_SHORT).show();
                }
                else{
                    DocumentReference documentReference = firebaseFirestore.collection("users").document(firebaseUser.getUid()).collection("myNotes").document();
                    Map<String, Object> note = new HashMap<>();
                    note.put("datetime", currentDateAndTime);
                    note.put("title", title);
                    note.put("content", content);
                    note.put("archive", false);
                    note.put("recycle", false);
                    note.put("deletedate", null);

                    documentReference.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(CreateNote.this, "Note Created Successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(CreateNote.this, HomePage.class));
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CreateNote.this, "Failed to create note", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(CreateNote.this, "No Note Created", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(CreateNote.this, HomePage.class));
            }
        });

    }
}