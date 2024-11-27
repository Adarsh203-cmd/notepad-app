package com.example.recyclebin;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private ImageButton backButton;
    private TextView titleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Replace with your XML file name

        // Initialize views
        backButton = findViewById(R.id.backButton);
        titleText = findViewById(R.id.titleText);

        // Set up title
        titleText.setText("Recycle Bin");

        // Set up back button click listener
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Navigate back to the previous activity
            }
        });

        // Set up the toolbar as the action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    // Inflate the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.recycle_bin_menu, menu); // Inflate the menu resource
        return true;
    }

    // Handle menu item clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Use if-else to handle menu item selection
        if (item.getItemId() == R.id.restore) {
            // Handle restore action
            Toast.makeText(this, "Restore option selected", Toast.LENGTH_SHORT).show();
            // Add your restore logic here
            return true;
        } else if (item.getItemId() == R.id.permanently_delete) {
            // Handle permanently delete action
            Toast.makeText(this, "Permanently delete option selected", Toast.LENGTH_SHORT).show();
            // Add your permanently delete logic here
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}



