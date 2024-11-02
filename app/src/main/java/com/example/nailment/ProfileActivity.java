package com.example.nailment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView nameTextView, servicesTextView, locationTextView;
    private ImageView profileImageView;
    private Button bookButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

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

            // Set up dynamic booking button text
            int bookingPrice = 5 * (1 + (int) (Math.random() * 7));  // Generates a random multiple of 5 under 35
            bookButton.setText("Book " + manicurist.getName() + " for " + bookingPrice + "$");
        }

        // Handle book button click to show payment popup
        bookButton.setOnClickListener(v -> showPaymentDialog());

        // Bottom Navigation Bar
        findViewById(R.id.homeButton).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }

    private void showPaymentDialog() {
        // Create a simple dialog for payment input
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_payment, null);

        new AlertDialog.Builder(this)
                .setTitle("Enter Payment Details")
                .setView(dialogView)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    // Handle payment confirmation
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
