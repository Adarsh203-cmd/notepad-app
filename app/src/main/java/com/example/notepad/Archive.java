package com.example.notepad;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Archive extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    RecyclerView mrecyclerview;

    FirebaseUser firebaseUser;
    FirebaseFirestore firebaseFirestore;

    FirestoreRecyclerAdapter<firebasemodel, Archive.NoteViewHolder> noteAdapter;

    // Empty view members
    LinearLayout emptyView;
    ImageView emptyImageView;
    TextView emptyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        // Initialize empty view components
        emptyView = findViewById(R.id.empty_view);
        emptyImageView = findViewById(R.id.empty_image);
        emptyTextView = findViewById(R.id.empty_text);

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(Archive.this, HomePage.class); // Navigate back to MainActivity (Home)
            startActivity(intent);
            finish();
        });

        Query query = firebaseFirestore.collection("users")
                .document(firebaseUser.getUid())
                .collection("myNotes")
                .whereEqualTo("archive", true)
                .whereEqualTo("recycle", false)
                .orderBy("datetime", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<firebasemodel> allusernotes = new FirestoreRecyclerOptions.Builder<firebasemodel>()
                .setQuery(query, firebasemodel.class)
                .build();

        noteAdapter = new FirestoreRecyclerAdapter<firebasemodel, Archive.NoteViewHolder>(allusernotes) {
            @Override
            protected void onBindViewHolder(Archive.NoteViewHolder noteViewHolder, int i, firebasemodel firebasemodel) {
                ImageView popupbutton = noteViewHolder.itemView.findViewById(R.id.menupopbutton);
                noteViewHolder.notetitle.setText(firebasemodel.getTitle());
                noteViewHolder.notetime.setText(firebasemodel.getDatetime());
                noteViewHolder.notecontent.setText(firebasemodel.getContent());

                String docId = noteAdapter.getSnapshots().getSnapshot(i).getId();

                // Set up note click to open EditNote activity
                noteViewHolder.itemView.setOnClickListener(view -> {
                    Intent intent = new Intent(view.getContext(), EditNote.class);
                    intent.putExtra("title", firebasemodel.getTitle());
                    intent.putExtra("content", firebasemodel.getContent());
                    intent.putExtra("noteId", docId);
                    view.getContext().startActivity(intent);
                });

                // Popup menu for options
                popupbutton.setOnClickListener(view -> {
                    PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
                    popupMenu.setGravity(Gravity.END);

                    popupMenu.getMenu().add("Edit").setOnMenuItemClickListener(menuItem -> {
                        Intent intent = new Intent(view.getContext(), EditNote.class);
                        intent.putExtra("title", firebasemodel.getTitle());
                        intent.putExtra("content", firebasemodel.getContent());
                        intent.putExtra("noteId", docId);
                        view.getContext().startActivity(intent);
                        return false;
                    });


                    popupMenu.getMenu().add("UnArchive").setOnMenuItemClickListener(menuItem -> {
                        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault());
                        String currentDateAndTime = sdf.format(new Date());
                        DocumentReference documentReference = firebaseFirestore.collection("users")
                                .document(firebaseUser.getUid())
+                                .collection("myNotes")
                                .document(docId);

                        Map<String, Object> note = new HashMap<>();
                        note.put("datetime", currentDateAndTime);
                        note.put("title", firebasemodel.getTitle());
                        note.put("content", firebasemodel.getContent());
                        note.put("archive", false);
                        note.put("recycle", false);
                        note.put("deletedate", null);

                        documentReference.set(note).addOnSuccessListener(unused -> {
                            Toast.makeText(Archive.this, "Note UnArchived", Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(Archive.this, Archive.class));
                            finish(); // Close the current activity
                        }).addOnFailureListener(e ->
                                Toast.makeText(Archive.this, "Failed to UnArchive", Toast.LENGTH_SHORT).show());

                        return false;
                    });


                    popupMenu.getMenu().add("Delete").setOnMenuItemClickListener(menuItem -> {
                        new androidx.appcompat.app.AlertDialog.Builder(Archive.this)
                                .setTitle("Confirm Delete")
                                .setMessage("Are you sure you want to delete this note?")
                                .setPositiveButton("Delete", (dialog, which) -> {
                                    SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault());
                                    String currentDateAndTime = sdf.format(new Date());
                                    DocumentReference documentReference = firebaseFirestore.collection("users")
                                            .document(firebaseUser.getUid())
                                            .collection("myNotes")
                                            .document(docId);

                                    Map<String, Object> note = new HashMap<>();
                                    note.put("datetime", currentDateAndTime);
                                    note.put("title", firebasemodel.getTitle());
                                    note.put("content", firebasemodel.getContent());
                                    note.put("archive", false);
                                    note.put("recycle", true);
                                    long currentTimeMillis = System.currentTimeMillis();
                                    long deleteTimeMillis = currentTimeMillis + 5 * 60 * 1000;
                                    note.put("deletedate", deleteTimeMillis);

                                    documentReference.set(note).addOnSuccessListener(unused -> {
                                        Toast.makeText(Archive.this, "Note moved to Recycle Bin", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(Archive.this, Archive.class));
                                        finish();
                                    }).addOnFailureListener(e ->
                                            Toast.makeText(Archive.this, "Failed to move to Recycle Bin", Toast.LENGTH_SHORT).show());
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                        return false;
                    });

                    popupMenu.show();
                });
            }

            @NonNull
            @Override
            public Archive.NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_layout, parent, false);
                return new Archive.NoteViewHolder(view);
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                checkIfEmpty(); // Check if empty after data changes
            }
        };

        // Setup RecyclerView
        mrecyclerview = findViewById(R.id.recyclerview);
        mrecyclerview.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mrecyclerview.setLayoutManager(linearLayoutManager);
        mrecyclerview.setAdapter(noteAdapter);

//        // Bottom navigation setup
//        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
//        bottomNavigationView.setSelectedItemId(R.id.archive);
//        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
//            if (item.getItemId() == R.id.home) {
//                startActivity(new Intent(getApplicationContext(), MainActivity.class));
//                overridePendingTransition(0, 0);
//                return true;
//            } else if (item.getItemId() == R.id.archive) {
//                return true;
//            } else if (item.getItemId() == R.id.recycle_bin) {
//                startActivity(new Intent(getApplicationContext(), RecycleBin.class));
//                overridePendingTransition(0, 0);
//                return true;
//            } else if (item.getItemId() == R.id.profile) {
//                startActivity(new Intent(getApplicationContext(), Profile.class));
//                overridePendingTransition(0, 0);
//                return true;
//            }
//            return false;
//        });

        checkIfEmpty(); // Initial check
    }

    @Override
    protected void onStart() {
        super.onStart();
        noteAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (noteAdapter != null) {
            noteAdapter.stopListening();
        }
    }

    // ViewHolder class for notes
    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView notetitle, notecontent, notetime;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            notetitle = itemView.findViewById(R.id.notetitle);
            notetime = itemView.findViewById(R.id.notetime);
            notecontent = itemView.findViewById(R.id.notecontent);
        }
    }

    // Method to check if the RecyclerView is empty
    private void checkIfEmpty() {
        boolean isEmpty = noteAdapter.getItemCount() == 0;
        if (isEmpty) {
            mrecyclerview.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            mrecyclerview.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
}

