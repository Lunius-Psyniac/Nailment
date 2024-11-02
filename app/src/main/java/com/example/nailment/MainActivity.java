package com.example.nailment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Open hyperlink on logo click
        findViewById(R.id.logo1).setOnClickListener(v -> openLink("https://example.com/nailsalon1"));
        findViewById(R.id.logo2).setOnClickListener(v -> openLink("https://example.com/nailsalon2"));
        findViewById(R.id.logo3).setOnClickListener(v -> openLink("https://example.com/nailsalon3"));
        findViewById(R.id.logo4).setOnClickListener(v -> openLink("https://example.com/nailsalon4"));

        // Book procedure button to navigate to ListActivity
        findViewById(R.id.bookProcedureButton).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ListActivity.class);
            startActivity(intent);
        });

        // Bottom Navigation Bar
        findViewById(R.id.homeButton).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }

    private void openLink(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
