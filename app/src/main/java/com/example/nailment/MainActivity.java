package com.example.nailment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView logo1 = findViewById(R.id.logo1);
        logo1.setOnClickListener(v -> openWebPage("http://salon1.com"));

        ImageView logo2 = findViewById(R.id.logo2);
        logo2.setOnClickListener(v -> openWebPage("http://salon2.com"));

        ImageView logo3 = findViewById(R.id.logo3);
        logo3.setOnClickListener(v -> openWebPage("http://salon3.com"));

        ImageView logo4 = findViewById(R.id.logo4);
        logo4.setOnClickListener(v -> openWebPage("http://salon4.com"));

        // Bottom Navigation Bar
        findViewById(R.id.homeButton).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        // Camera button to open CameraActivity
        findViewById(R.id.cameraButton).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            startActivity(intent);
        });

        // Book procedure button to navigate to ListActivity
        findViewById(R.id.bookProcedureButton).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ListActivity.class);
            startActivity(intent);
        });
    }

    private void openWebPage(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
