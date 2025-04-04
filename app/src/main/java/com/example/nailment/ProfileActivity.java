package com.example.nailment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private TextView nameTextView, descriptionTextView, locationTextView, ratingCountTextView;
    private ImageView profileImageView;
    private Button bookButton;
    private RatingBar ratingBar;
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
        if (manicurist == null) {
            Log.e(TAG, "No manicurist data provided");
            finish();
            return;
        }
        Log.d(TAG, "Manicurist data: " + manicurist.getName() + ", ID: " + manicurist.getUid());

        // Set up views
        nameTextView = findViewById(R.id.profile_name);
        descriptionTextView = findViewById(R.id.profile_description);
        locationTextView = findViewById(R.id.profile_location);
        profileImageView = findViewById(R.id.profile_image);
        bookButton = findViewById(R.id.book_button);
        ratingBar = findViewById(R.id.profile_rating);
        ratingCountTextView = findViewById(R.id.rating_count);

        // Set manicurist data in views
        nameTextView.setText(manicurist.getName());
        descriptionTextView.setText(manicurist.getSelfDescription());
        locationTextView.setText(manicurist.getLocation());
        
        // Set rating with logging
        float rating = (float) manicurist.getAvgRating();
        Log.d(TAG, "Setting rating for " + manicurist.getName() + ": " + rating);
        ratingBar.setRating(rating);

        // Set rating count
        int ratingCount = manicurist.getRatingCount();
        String ratingText = String.format("(%d %s)", ratingCount, ratingCount == 1 ? "rating" : "reviews");
        ratingCountTextView.setText(ratingText);
        Log.d(TAG, "Setting rating count for " + manicurist.getName() + ": " + ratingText);

        // Load profile picture using Glide
        if (manicurist.getProfilePictureLink() != null && !manicurist.getProfilePictureLink().isEmpty()) {
            Glide.with(this)
                .load(manicurist.getProfilePictureLink())
                .circleCrop()
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(profileImageView);
        } else {
            profileImageView.setImageResource(R.drawable.placeholder_image);
        }

        // Set up chat button text
        bookButton.setText("Chat with " + manicurist.getName());

        // Set up read reviews button
        Button readReviewsButton = findViewById(R.id.read_reviews_button);
        readReviewsButton.setOnClickListener(v -> {
            Log.d(TAG, "Opening reviews for manicurist: " + manicurist.getUid());
            Intent intent = new Intent(ProfileActivity.this, ReviewsActivity.class);
            intent.putExtra("manicurist_id", manicurist.getUid());
            startActivity(intent);
        });

        // Handle book button click to open chat
        bookButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ChatActivity.class);
            intent.putExtra("manicurist", manicurist);
            startActivity(intent);
        });

        // Bottom Navigation Bar
        findViewById(R.id.homeButton).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }
}
