package com.example.notepad;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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


public class HomePage extends AppCompatActivity {
    FloatingActionButton mcreatenotesfab;
    private EditText msearch;
    private ImageView msearchbutton;

    private FirebaseAuth firebaseAuth;
    RecyclerView mrecyclerview;
    StaggeredGridLayoutManager staggeredGridLayoutManager;
    FirebaseUser firebaseUser;
    FirebaseFirestore firebaseFirestore;

    FirestoreRecyclerAdapter<firebasemodel, NoteViewHolder> noteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        msearch = findViewById(R.id.search_box);
        msearchbutton = findViewById(R.id.searchbutton);
        mcreatenotesfab = findViewById(R.id.createnotefab);
        firebaseAuth = FirebaseAuth.getInstance();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();



        mcreatenotesfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomePage.this, CreateNote.class));
            }
        });

        // Load notes when activity starts
        loadNotes("");

        msearchbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchTerm = msearch.getText().toString();
                loadNotes(searchTerm); // Load notes based on the search term
            }
        });

        setupBottomNavigation();
    }

    // Method to load notes based on search term
    private void loadNotes(String searchTerm) {
        Query query;
        if (searchTerm.isEmpty()) {
            // Default query to load all notes
            query = firebaseFirestore.collection("users")
                    .document(firebaseUser.getUid())
                    .collection("myNotes")
                    .whereEqualTo("archive", false)
                    .whereEqualTo("recycle", false)
                    .orderBy("datetime", Query.Direction.DESCENDING);
        } else {
            // Query to search notes by title or content
            query = firebaseFirestore.collection("users")
                    .document(firebaseUser.getUid())
                    .collection("myNotes")
                    .whereEqualTo("archive", false)
                    .whereEqualTo("recycle", false)
                    .orderBy("title")
                    .startAt(searchTerm)
                    .endAt(searchTerm + "\uf8ff");
        }

        FirestoreRecyclerOptions<firebasemodel> allusernotes = new FirestoreRecyclerOptions.Builder<firebasemodel>()
                .setQuery(query, firebasemodel.class)
                .build();

        noteAdapter = new FirestoreRecyclerAdapter<firebasemodel, NoteViewHolder>(allusernotes) {
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int i, @NonNull firebasemodel firebasemodel) {
                ImageView popupbutton = noteViewHolder.itemView.findViewById(R.id.menupopbutton);

                noteViewHolder.notetitle.setText(firebasemodel.getTitle());
                noteViewHolder.notetime.setText(firebasemodel.getDatetime());
                noteViewHolder.notecontent.setText(firebasemodel.getContent());

                String docId = noteAdapter.getSnapshots().getSnapshot(i).getId();

                noteViewHolder.itemView.setOnClickListener(view -> {
                    Intent intent = new Intent(view.getContext(), EditNote.class);
                    intent.putExtra("title", firebasemodel.getTitle());
                    intent.putExtra("content", firebasemodel.getContent());
                    intent.putExtra("noteId", docId);
                    view.getContext().startActivity(intent);
                });

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


                    popupMenu.getMenu().add("Archive").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault());
                            String currentDateAndTime = sdf.format(new Date());
                            DocumentReference documentReference = firebaseFirestore.collection("users").document(firebaseUser.getUid()).collection("myNotes").document(docId);
                            Map<String,Object> note = new HashMap<>();
                            note.put("datetime", currentDateAndTime);
                            note.put("title", firebasemodel.getTitle());
                            note.put("content", firebasemodel.getContent());
                            note.put("archive", true);
                            note.put("recycle", false);
                            note.put("deletedate", null);
                            documentReference.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(HomePage.this, "Note Archive", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(HomePage.this, HomePage.class));
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(HomePage.this, "Failed to Archive", Toast.LENGTH_SHORT).show();
                                }
                            });
                            return false;
                        }
                    });


                    popupMenu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                            new android.app.AlertDialog.Builder(HomePage.this)
                                    .setTitle("Delete Note")
                                    .setMessage("Are you sure you want to delete this note?")
                                    .setPositiveButton("Delete", (dialog, which) -> {
                                        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault());
                                        String currentDateAndTime = sdf.format(new Date());
                                        DocumentReference documentReference = firebaseFirestore.collection("users").document(firebaseUser.getUid()).collection("myNotes").document(docId);
                                        Map<String, Object> note = new HashMap<>();
                                        note.put("datetime", currentDateAndTime);
                                        note.put("title", firebasemodel.getTitle());
                                        note.put("content", firebasemodel.getContent());
                                        note.put("archive", false);
                                        note.put("recycle", true);
                                        long currentTimeMillis = System.currentTimeMillis();
                                        long deleteTimeMillis = currentTimeMillis + 5 * 60 * 1000;
                                        note.put("deletedate", deleteTimeMillis);

                                        documentReference.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(HomePage.this, "Note moved to Recycle Bin", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(HomePage.this, HomePage.class));
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(HomePage.this, "Failed to Recycle Bin", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    })
                                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                    .show();

                            return false;
                        }
                    });


                    popupMenu.show();
                });
            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_layout, parent, false);
                return new NoteViewHolder(view);
            }
        };

        mrecyclerview = findViewById(R.id.recyclerview);
        mrecyclerview.setHasFixedSize(true);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        mrecyclerview.setLayoutManager(staggeredGridLayoutManager);
        mrecyclerview.setAdapter(noteAdapter);
        noteAdapter.startListening();
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder {
        private TextView notetime, notetitle, notecontent;
        LinearLayout mnote;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            notetitle = itemView.findViewById(R.id.notetitle);
            notetime = itemView.findViewById(R.id.notetime);
            notecontent = itemView.findViewById(R.id.notecontent);
            mnote = itemView.findViewById(R.id.note);
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.home) {
            } else if(itemId == R.id.archive){
                startActivity(new Intent(HomePage.this, Archive.class));
            } else if (itemId == R.id.recycle_bin) {
                startActivity(new Intent(HomePage.this, RecycleBin.class));
            } else if (itemId == R.id.profile) {
                startActivity(new Intent(HomePage.this, Profile.class));
            }
            return true;
        });
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
}