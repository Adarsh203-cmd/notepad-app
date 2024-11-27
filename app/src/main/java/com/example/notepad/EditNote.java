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

public class EditNote extends AppCompatActivity {

    Intent data;
    EditText medittitleofnote, meditcontentofnote;
    FloatingActionButton msaveeditnote;

    private TextView mdatetime;
    private ImageButton imageButton;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        mdatetime = findViewById(R.id.datetime);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault());
        String currentDateAndTime = sdf.format(new Date());
        mdatetime.setText(currentDateAndTime);

        data=getIntent();

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        medittitleofnote = findViewById(R.id.edittitleofnote);
        meditcontentofnote = findViewById(R.id.editcontentofnote);
        msaveeditnote = findViewById(R.id.saveeditnote);

        imageButton = findViewById(R.id.btnBack);

        msaveeditnote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newtitle = medittitleofnote.getText().toString();
                String newcontent = meditcontentofnote.getText().toString();

                if(newtitle.isEmpty() || newcontent.isEmpty()){
                    Toast.makeText(EditNote.this, "Title and content is required", Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    DocumentReference documentReference = firebaseFirestore.collection("users").document(firebaseUser.getUid()).collection("myNotes").document(data.getStringExtra("noteId"));
                    Map<String,Object> note = new HashMap<>();
                    note.put("datetime", currentDateAndTime);
                    note.put("title", newtitle);
                    note.put("content", newcontent);
                    note.put("archive", false);
                    note.put("recycle", false);
                    note.put("deletedate", null);
                    documentReference.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(EditNote.this, "Note is updated", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(EditNote.this, HomePage.class));
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditNote.this, "Failed to update", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        String notetitle = data.getStringExtra("title");
        String notecontent = data.getStringExtra("content");
        medittitleofnote.setText(notetitle);
        meditcontentofnote.setText(notecontent);


        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(EditNote.this, "No update in Note", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(EditNote.this, HomePage.class));
            }
        });


    }
}