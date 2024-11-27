package com.example.archivepage;

import java.util.ArrayList;
import java.util.List;

public class NotesManager {
    private static NotesManager instance;
    private List<Note> notes;

    private NotesManager() {
        notes = new ArrayList<>();
        // Sample data
        notes.add(new Note("Title 1", "This is the content of note 1"));
        notes.add(new Note("Title 2", "This is the content of note 2"));
        notes.add(new Note("Title 3", "This is the content of note 3"));
    }

    public static synchronized NotesManager getInstance() {
        if (instance == null) {
            instance = new NotesManager();
        }
        return instance;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void deleteNoteByIndex(int index) {
        if (index >= 0 && index < notes.size()) {
            notes.remove(index);
        }
    }
}
