package com.example.archivepage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class NoteMovedToBinActivity extends AppCompatActivity {

    private TextView movedToBinText;
    private ImageButton backButton;
    private ArrayList<Integer> deletedNotePositions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_moved_to_bin);

        movedToBinText = findViewById(R.id.splashText);
        movedToBinText.setText("Note Moved to Bin");

        // Retrieve the list of deleted note positions
        deletedNotePositions = getIntent().getIntegerArrayListExtra("deletedNotePositions");

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.putIntegerArrayListExtra("deletedNotePositions", deletedNotePositions);
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putIntegerArrayListExtra("deletedNotePositions", deletedNotePositions);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }
}
