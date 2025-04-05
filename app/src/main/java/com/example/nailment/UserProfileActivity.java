package com.example.nailment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = "UserProfileActivity";
    private TextView nameTextView, emailTextView, descriptionTextView, locationTextView;
    private ImageView profileImageView;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Check if user is authenticated
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "You must be logged in to view your profile", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        nameTextView = findViewById(R.id.profile_name);
        emailTextView = findViewById(R.id.profile_email);
        descriptionTextView = findViewById(R.id.profile_description);
        locationTextView = findViewById(R.id.profile_location);
        profileImageView = findViewById(R.id.profile_image);

        // Back button
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> finish());

        // Load user data
        String currentUserId = mAuth.getCurrentUser().getUid();
        userRef = database.getReference("users").child(currentUserId);

        try {
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            nameTextView.setText(user.getName());
                            emailTextView.setText(user.getEmail());
                            descriptionTextView.setText(user.getSelfDescription());

                            // Only show location for manicurists
                            if (user.getUserType().equals("MANICURIST")) {
                                locationTextView.setVisibility(TextView.VISIBLE);
                                locationTextView.setText(user.getLocation());
                            } else {
                                locationTextView.setVisibility(TextView.GONE);
                            }

                            // Load profile picture
                            if (user.getProfilePictureLink() != null && !user.getProfilePictureLink().isEmpty()) {
                                Glide.with(UserProfileActivity.this)
                                        .load(user.getProfilePictureLink())
                                        .circleCrop()
                                        .placeholder(R.drawable.placeholder_image)
                                        .error(R.drawable.placeholder_image)
                                        .into(profileImageView);
                            } else {
                                profileImageView.setImageResource(R.drawable.placeholder_image);
                            }
                        }
                    } else {
                        Log.e(TAG, "User data does not exist for ID: " + currentUserId);
                        Toast.makeText(UserProfileActivity.this, 
                                "User profile not found", 
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "Error loading user data: " + databaseError.getMessage());
                    Toast.makeText(UserProfileActivity.this,
                            "Error loading profile data",
                            Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception when loading user data: " + e.getMessage());
            Toast.makeText(UserProfileActivity.this,
                    "Error loading profile data: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }

        // Bottom Navigation Bar
        findViewById(R.id.homeButton).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }
}