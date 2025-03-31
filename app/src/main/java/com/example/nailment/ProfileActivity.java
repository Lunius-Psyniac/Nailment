package com.example.nailment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {

    private TextView nameTextView, servicesTextView, locationTextView;
    private ImageView profileImageView;
    private Button bookButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> finish());

        // Retrieve the manicurist data from the Intent
        Manicurist manicurist = (Manicurist) getIntent().getSerializableExtra("manicurist");

        // Set up views
        nameTextView = findViewById(R.id.profile_name);
        servicesTextView = findViewById(R.id.profile_services);
        locationTextView = findViewById(R.id.profile_location);
        profileImageView = findViewById(R.id.profile_image);
        bookButton = findViewById(R.id.book_button);

        // Set manicurist data in views
        if (manicurist != null) {
            nameTextView.setText(manicurist.getName());
            servicesTextView.setText(manicurist.getServices());
            locationTextView.setText(manicurist.getLocation());
            profileImageView.setImageResource(manicurist.getImageResource());

            // Set up chat button text
            bookButton.setText("Chat with " + manicurist.getName());
        }

        // Handle book button click to open chat
        bookButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ChatActivity.class);
            startActivity(intent);
        });

        // Bottom Navigation Bar
        findViewById(R.id.homeButton).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }
}
