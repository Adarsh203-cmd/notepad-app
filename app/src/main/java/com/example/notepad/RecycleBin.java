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

public class RecycleBin extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    RecyclerView mrecyclerview;
    FirebaseUser firebaseUser;
    FirebaseFirestore firebaseFirestore;
    FirestoreRecyclerAdapter<firebasemodel, RecycleBin.NoteViewHolder> noteAdapter;


    LinearLayout emptyView;
    ImageView emptyImageView;
    TextView emptyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle_bin);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        emptyView = findViewById(R.id.empty_view);
        emptyImageView = findViewById(R.id.empty_image);
        emptyTextView = findViewById(R.id.empty_text);


        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(RecycleBin.this, HomePage.class);
            startActivity(intent);
            finish();
        });

        Query query = firebaseFirestore.collection("users")
                .document(firebaseUser.getUid())
                .collection("myNotes")
                .whereEqualTo("recycle", true)
                .orderBy("deletedate", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<firebasemodel> allrecyclednotes = new FirestoreRecyclerOptions.Builder<firebasemodel>()
                .setQuery(query, firebasemodel.class)
                .build();

        noteAdapter = new FirestoreRecyclerAdapter<firebasemodel, RecycleBin.NoteViewHolder>(allrecyclednotes) {
            @Override
            protected void onBindViewHolder(@NonNull RecycleBin.NoteViewHolder holder, int position, @NonNull firebasemodel model) {
                // Binding the view holder
                holder.notetitle.setText(model.getTitle());
                holder.notecontent.setText(model.getContent());
                holder.notetime.setText(model.getDatetime());

                // Note click and popup menu
                String docId = noteAdapter.getSnapshots().getSnapshot(position).getId();

                holder.itemView.setOnClickListener(v -> {
                    // You can implement note click functionality here
                });

                holder.popupbutton.setOnClickListener(v -> {
                    PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                    popupMenu.setGravity(Gravity.END);

                    popupMenu.getMenu().add("Restore").setOnMenuItemClickListener(menuItem -> {
                        // Restore note logic
                        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault());
                        String currentDateAndTime = sdf.format(new Date());
                        DocumentReference documentReference = firebaseFirestore.collection("users")
                                .document(firebaseUser.getUid())
                                .collection("myNotes")
                                .document(docId);

                        Map<String, Object> note = new HashMap<>();
                        note.put("datetime", currentDateAndTime);
                        note.put("title", model.getTitle());
                        note.put("content", model.getContent());
                        note.put("archive", false);
                        note.put("recycle", false);

                        documentReference.set(note).addOnSuccessListener(unused -> {
                            Toast.makeText(RecycleBin.this, "Note restored", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RecycleBin.this, RecycleBin.class));
                            finish();
                        }).addOnFailureListener(e ->
                                Toast.makeText(RecycleBin.this, "Failed to restore note", Toast.LENGTH_SHORT).show());

                        return false;
                    });

                    popupMenu.getMenu().add("Permanently Delete").setOnMenuItemClickListener(menuItem -> {
                        // Show an AlertDialog before permanent deletion
                        new android.app.AlertDialog.Builder(RecycleBin.this)
                                .setTitle("Delete Note Forever")
                                .setMessage("Are you sure you want to permanently delete this note?")
                                .setPositiveButton("Delete", (dialog, which) -> {
                                    // Proceed with deletion if user confirms
                                    DocumentReference documentReference = firebaseFirestore.collection("users")
                                            .document(firebaseUser.getUid())
                                            .collection("myNotes")
                                            .document(docId);

                                    documentReference.delete().addOnSuccessListener(unused -> {
                                        Intent intent = new Intent(RecycleBin.this, SplashBin.class);
                                        startActivity(intent);
                                        finish(); // Optional: Call finish() if you want to remove the current activity from the back stack
                                    }).addOnFailureListener(e ->
                                            Toast.makeText(v.getContext(), "Failed to delete note", Toast.LENGTH_SHORT).show());
                                })
                                .setNegativeButton("Cancel", (dialog, which) -> {
                                    // Dismiss the dialog if user cancels
                                    dialog.dismiss();
                                })
                                .show();

                        return false;
                    });


                    popupMenu.show();
                });
            }

            @NonNull
            @Override
            public RecycleBin.NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_layout, parent, false);
                return new RecycleBin.NoteViewHolder(view);
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                checkIfEmpty();
            }
        };

        mrecyclerview = findViewById(R.id.recyclerview);
        mrecyclerview.setHasFixedSize(true);
        mrecyclerview.setLayoutManager(new LinearLayoutManager(this));
        mrecyclerview.setAdapter(noteAdapter);

//        // Bottom navigation setup
//        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
//        bottomNavigationView.setSelectedItemId(R.id.recycle_bin);
//        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
//            if (item.getItemId() == R.id.home) {
//                startActivity(new Intent(getApplicationContext(), MainActivity.class));
//                overridePendingTransition(0, 0);
//                return true;
//            }else if (item.getItemId() == R.id.archive) {
//                startActivity(new Intent(getApplicationContext(), Archive.class));
//                overridePendingTransition(0, 0);
//                return true;
//            } else if (item.getItemId() == R.id.recycle_bin) {
//                return true;
//            } else if (item.getItemId() == R.id.profile) {
//                startActivity(new Intent(getApplicationContext(), Profile.class));
//                overridePendingTransition(0, 0);
//                return true;
//            }
//            return false;
//        });


        checkIfEmpty(); // Initial check for empty state
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

    // ViewHolder class
    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView notetitle, notecontent, notetime;
        ImageView popupbutton;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            notetitle = itemView.findViewById(R.id.notetitle);
            notetime = itemView.findViewById(R.id.notetime);
            notecontent = itemView.findViewById(R.id.notecontent);
            popupbutton = itemView.findViewById(R.id.menupopbutton);
        }
    }

    // Method to check if RecyclerView is empty
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
