package com.example.archivepage;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageButton backButton;
    private TextView titleText, emptyArchiveMessage;
    private ImageView emptyArchiveImage; // Added ImageView for empty archive state
    private boolean isSelecting = false; // Flag to track selection mode
    private LinearLayout noteLayout1, noteLayout2, noteLayout3; // Store the note layouts
    private CheckBox noteCheckbox1, noteCheckbox2, noteCheckbox3; // Store the checkboxes
    private ArrayList<Integer> deletedNotePositions = new ArrayList<>(); // Track deleted notes
    private static final int DELETE_NOTE_REQUEST = 1; // Request code for deletion

    private List<LinearLayout> noteLayouts;
    private List<CheckBox> noteCheckboxes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        backButton = findViewById(R.id.backButton);
        titleText = findViewById(R.id.titleText);
        emptyArchiveMessage = findViewById(R.id.emptyArchiveMessage);
        emptyArchiveImage = findViewById(R.id.emptyArchiveImage); // Initialize empty archive image

        // Initialize note layouts
        noteLayout1 = findViewById(R.id.noteLayout1);
        noteLayout2 = findViewById(R.id.noteLayout2);
        noteLayout3 = findViewById(R.id.noteLayout3);

        // Initialize checkboxes
        noteCheckbox1 = findViewById(R.id.noteCheckbox1);
        noteCheckbox2 = findViewById(R.id.noteCheckbox2);
        noteCheckbox3 = findViewById(R.id.noteCheckbox3);

        noteLayouts = new ArrayList<>();
        noteCheckboxes = new ArrayList<>();

        noteLayouts.add(noteLayout1);
        noteLayouts.add(noteLayout2);
        noteLayouts.add(noteLayout3);

        noteCheckboxes.add(noteCheckbox1);
        noteCheckboxes.add(noteCheckbox2);
        noteCheckboxes.add(noteCheckbox3);

        for (int i = 0; i < noteLayouts.size(); i++) {
            final int index = i;
            noteLayouts.get(i).setOnLongClickListener(v -> {
                enableSelectionMode(noteCheckboxes.get(index));
                return true;
            });
        }

        for (int i = 0; i < noteLayouts.size(); i++) {
            final int index = i;
            noteLayouts.get(i).setOnClickListener(v -> toggleCheckbox(noteCheckboxes.get(index), "Title " + (index + 1), "This is the content of note " + (index + 1), index));
        }

        backButton.setOnClickListener(view -> {
            if (isSelecting) {
                disableSelectionMode();
            } else {
                finishAffinity();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_selected_notes, menu);
        menu.setGroupVisible(R.id.menu_group, isSelecting);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (isSelecting) {
            if (item.getItemId() == R.id.action_unarchive) {
                handleUnarchiveAction();
                return true;
            } else if (item.getItemId() == R.id.action_delete) {
                handleDeleteAction();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void enableSelectionMode(CheckBox longTappedCheckbox) {
        isSelecting = true;
        for (CheckBox checkBox : noteCheckboxes) {
            checkBox.setVisibility(View.VISIBLE);
        }
        longTappedCheckbox.setChecked(true);
        invalidateOptionsMenu();
    }

    private void disableSelectionMode() {
        isSelecting = false;
        for (CheckBox checkBox : noteCheckboxes) {
            checkBox.setChecked(false);
            checkBox.setVisibility(View.GONE);
        }
        invalidateOptionsMenu();
    }

    private void toggleCheckbox(CheckBox checkBox, String title, String content, int position) {
        if (isSelecting) {
            checkBox.setChecked(!checkBox.isChecked());
        } else {
            openSelectedNote(title, content, position);
        }
    }

    private void openSelectedNote(String title, String content, int position) {
        Intent intent = new Intent(MainActivity.this, selectedNotes.class);
        intent.putExtra("noteTitle", title);
        intent.putExtra("noteContent", content);
        intent.putExtra("notePosition", position);
        startActivityForResult(intent, DELETE_NOTE_REQUEST);
    }

    private void handleUnarchiveAction() {
        ArrayList<Integer> unarchivedPositions = new ArrayList<>();
        for (int i = 0; i < noteCheckboxes.size(); i++) {
            if (noteCheckboxes.get(i).isChecked()) {
                unarchivedPositions.add(i);
            }
        }
        if (!unarchivedPositions.isEmpty()) {
            for (int pos : unarchivedPositions) {
                noteLayouts.get(pos).setVisibility(View.GONE); // Hide the unarchived note
            }
            Toast.makeText(this, "Notes Unarchived", Toast.LENGTH_SHORT).show(); // Show unarchive message
            checkIfAllNotesDeleted(); // Check if all notes are deleted
            disableSelectionMode();
        }
    }

    private void handleDeleteAction() {
        ArrayList<Integer> deletePositions = new ArrayList<>();
        for (int i = 0; i < noteCheckboxes.size(); i++) {
            if (noteCheckboxes.get(i).isChecked()) {
                deletePositions.add(i);
            }
        }
        if (!deletePositions.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Deletion")
                    .setMessage("Are you sure you want to delete the selected notes?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteNotes(deletePositions))
                    .setNegativeButton("Cancel", null) // Dismiss the dialog
                    .show();
        }
    }

    private void deleteNotes(ArrayList<Integer> deletePositions) {
        for (int pos : deletePositions) {
            deletedNotePositions.add(pos); // Track deleted notes
            noteLayouts.get(pos).setVisibility(View.GONE); // Hide the deleted note
        }

        // Instead of showing a Toast, navigate to NoteMovedToBinActivity
        Intent intent = new Intent(MainActivity.this, NoteMovedToBinActivity.class);
        startActivity(intent);

        // Check if all notes are deleted
        checkIfAllNotesDeleted();
        disableSelectionMode();
    }


    private void checkIfAllNotesDeleted() {
        boolean allDeleted = true;
        for (LinearLayout layout : noteLayouts) {
            if (layout.getVisibility() != View.GONE) {
                allDeleted = false;
                break;
            }
        }
        if (allDeleted) {
            emptyArchiveImage.setVisibility(View.VISIBLE); // Show empty archive image
            emptyArchiveMessage.setVisibility(View.VISIBLE); // Show empty archive message
        } else {
            emptyArchiveImage.setVisibility(View.GONE); // Hide empty archive image
            emptyArchiveMessage.setVisibility(View.GONE); // Hide empty archive message
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DELETE_NOTE_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                int deletedPosition = data.getIntExtra("deletedPosition", -1);
                if (deletedPosition != -1) {
                    noteLayouts.get(deletedPosition).setVisibility(View.GONE); // Hide the deleted note
                }

                int unarchivedPosition = data.getIntExtra("unarchivedPosition", -1);
                if (unarchivedPosition != -1) {
                    noteLayouts.get(unarchivedPosition).setVisibility(View.GONE); // Hide the unarchived note
                }
                checkIfAllNotesDeleted(); // Check if all notes are deleted
            }
        }
    }
}
