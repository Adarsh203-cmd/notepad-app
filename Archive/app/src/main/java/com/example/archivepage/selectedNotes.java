package com.example.archivepage;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.app.AlertDialog; // Import AlertDialog
import android.content.DialogInterface; // Import DialogInterface

public class selectedNotes extends AppCompatActivity {

    private TextView noteTitleTextView;
    private TextView noteContentTextView;
    private int notePosition; // Variable to hold the position of the note

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_notes);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        noteTitleTextView = findViewById(R.id.noteTitle);
        noteContentTextView = findViewById(R.id.noteContent);

        Intent intent = getIntent();
        String noteTitle = intent.getStringExtra("noteTitle");
        String noteContent = intent.getStringExtra("noteContent");
        notePosition = intent.getIntExtra("notePosition", -1); // Get the note position

        noteTitleTextView.setText(noteTitle);
        noteContentTextView.setText(noteContent);

        ImageButton cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(view -> {
            // Just navigate back to the main page without any deletion
            finish(); // Finish the current activity
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_selected_notes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_unarchive) {
            unarchiveNote(); // Directly unarchive without confirmation
            return true;
        } else if (item.getItemId() == R.id.action_delete) {
            confirmDeleteNote(); // Call the method to confirm deletion
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmDeleteNote() {
        // Create an AlertDialog to confirm deletion
        new AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteNote(); // Call the delete note method
                })
                .setNegativeButton("Cancel", null) // Dismiss the dialog
                .show();
    }

    private void deleteNote() {
        if (notePosition != -1) {
            // Set the result to indicate the note was deleted
            Intent resultIntent = new Intent();
            resultIntent.putExtra("deletedPosition", notePosition);
            setResult(RESULT_OK, resultIntent); // Set result

            finish(); // Finish the current activity
        } else {
            Toast.makeText(this, "Error deleting note", Toast.LENGTH_SHORT).show();
        }
    }

    private void unarchiveNote() {
        if (notePosition != -1) {
            // Set the result to indicate the note was unarchived
            Intent resultIntent = new Intent();
            resultIntent.putExtra("unarchivedPosition", notePosition);
            setResult(RESULT_OK, resultIntent); // Set result

            Toast.makeText(this, "Note Unarchived", Toast.LENGTH_SHORT).show(); // Show unarchive message
            finish(); // Finish the current activity
        } else {
            Toast.makeText(this, "Error unarchiving note", Toast.LENGTH_SHORT).show();
        }
    }
}
