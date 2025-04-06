package com.example.nailment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private TextView nameTextView, descriptionTextView, locationTextView, ratingCountTextView;
    private ImageView profileImageView;
    private Button bookButton, chatButton;
    private RatingBar ratingBar;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private Manicurist manicurist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> finish());

        // Retrieve the manicurist data from the Intent
        manicurist = (Manicurist) getIntent().getSerializableExtra("manicurist");
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
        chatButton = findViewById(R.id.chat_button);
        ratingBar = findViewById(R.id.profile_rating);
        ratingCountTextView = findViewById(R.id.rating_count);

        // Set manicurist data in views
        nameTextView.setText(manicurist.getName());
        descriptionTextView.setText(manicurist.getSelfDescription());
        locationTextView.setText(manicurist.getLocation());
        
        // Set up real-time rating listener
        database.getReference("users").child(manicurist.getUid())
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Manicurist updatedManicurist = dataSnapshot.getValue(Manicurist.class);
                    if (updatedManicurist != null) {
                        // Update rating
                        float rating = (float) updatedManicurist.getAvgRating();
                        Log.d(TAG, "Rating updated for " + updatedManicurist.getName() + ": " + rating);
                        ratingBar.setRating(rating);

                        // Update rating count
                        int ratingCount = updatedManicurist.getRatingCount();
                        String ratingText = String.format("(%d %s)", ratingCount, ratingCount == 1 ? "rating" : "reviews");
                        ratingCountTextView.setText(ratingText);
                        Log.d(TAG, "Rating count updated for " + updatedManicurist.getName() + ": " + ratingText);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "Error listening to rating updates: " + databaseError.getMessage());
                }
            });
        
        // Set rating with logging
        float rating = (float) manicurist.getAvgRating();
        Log.d(TAG, "Setting initial rating for " + manicurist.getName() + ": " + rating);
        ratingBar.setRating(rating);

        // Set rating count
        int ratingCount = manicurist.getRatingCount();
        String ratingText = String.format("(%d %s)", ratingCount, ratingCount == 1 ? "rating" : "reviews");
        ratingCountTextView.setText(ratingText);
        Log.d(TAG, "Setting initial rating count for " + manicurist.getName() + ": " + ratingText);

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
        chatButton.setText("Chat with " + manicurist.getName());

        // Set up read reviews button
        Button readReviewsButton = findViewById(R.id.read_reviews_button);
        readReviewsButton.setOnClickListener(v -> {
            Log.d(TAG, "Opening reviews for manicurist: " + manicurist.getUid());
            Intent intent = new Intent(ProfileActivity.this, ReviewsActivity.class);
            intent.putExtra("manicurist_id", manicurist.getUid());
            startActivity(intent);
        });

        // Handle book button click to open chat
        chatButton.setOnClickListener(v -> {
            // Create a chat ID using both user IDs to ensure uniqueness
            String currentUserId = mAuth.getCurrentUser().getUid();
            String chatId = getChatId(currentUserId, manicurist.getUid());
            
            // Open chat activity with this manicurist
            Intent intent = new Intent(ProfileActivity.this, ChatActivity.class);
            intent.putExtra("chat_partner_name", manicurist.getName());
            intent.putExtra("chat_id", chatId);
            intent.putExtra("chat_partner_id", manicurist.getUid());
            startActivity(intent);
        });

        // Handle book appointment button click
        bookButton.setOnClickListener(v -> showDateTimePicker());

        // Bottom Navigation Bar
        findViewById(R.id.homeButton).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        
        // Camera button to open CameraActivity
        findViewById(R.id.cameraButton).setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, CameraActivity.class);
            startActivity(intent);
        });
        
        // Chat button to navigate to ChatActivity
        findViewById(R.id.chatButton).setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ChatActivity.class);
            startActivity(intent);
        });
        
        // Profile button to navigate to UserProfileActivity
        findViewById(R.id.profileButton).setOnClickListener(v -> {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            if (mAuth.getCurrentUser() != null) {
                Intent intent = new Intent(ProfileActivity.this, UserProfileActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(ProfileActivity.this, "You must be logged in to view your profile", Toast.LENGTH_SHORT).show();
                // Optionally navigate to login screen
                Intent intent = new Intent(ProfileActivity.this, AuthActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showDateTimePicker() {
        // Create date picker
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select appointment date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            // Store the selected date
            long selectedDate = selection;
            
            // Create time picker
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTitleText("Select appointment time")
                    .setHour(9) // Default to 9 AM
                    .setMinute(0)
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .build();

            timePicker.addOnPositiveButtonClickListener(view -> {
                // Handle the selected time
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();
                
                // Combine date and time
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(selectedDate);
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                
                // Format the date and time
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
                SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
                String formattedDate = dateFormat.format(calendar.getTime());
                String formattedTime = timeFormat.format(calendar.getTime());
                
                // Create the booking message
                String bookingMessage = "Hello, I would like to book an appointment at " + formattedTime + " on " + formattedDate;
                
                // Send the message to chat
                sendBookingMessage(bookingMessage);
            });

            timePicker.show(getSupportFragmentManager(), "TIME_PICKER");
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void sendBookingMessage(String message) {
        if (mAuth.getCurrentUser() == null || manicurist == null) {
            Log.e(TAG, "Cannot send booking message: " + 
                  (mAuth.getCurrentUser() == null ? "User is not authenticated" : "") +
                  (manicurist == null ? "Manicurist is null" : ""));
            return;
        }

        // Create a chat ID using both user IDs to ensure uniqueness
        String currentUserId = mAuth.getCurrentUser().getUid();
        String chatId = getChatId(currentUserId, manicurist.getUid());
        Log.d(TAG, "Creating chat with ID: " + chatId);
        
        // First check if the chat exists and create it if it doesn't
        database.getReference("chats").child(chatId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Chat exists, proceed to open chat activity
                    openChatActivity(chatId, message);
                } else {
                    // Chat doesn't exist, create it first
                    Log.d(TAG, "Chat doesn't exist, creating it first");
                    createChatStructure(chatId, message);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error checking chat existence: " + databaseError.getMessage());
                Toast.makeText(ProfileActivity.this, 
                    "Error creating chat: " + databaseError.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createChatStructure(String chatId, String message) {
        String currentUserId = mAuth.getCurrentUser().getUid();
        
        // Create chat metadata
        ChatMetadata metadata = new ChatMetadata(currentUserId, manicurist.getUid());
        
        // Create the chat structure with metadata and messages node
        DatabaseReference chatRef = database.getReference("chats").child(chatId);
        chatRef.child("metadata").setValue(metadata)
            .addOnSuccessListener(aVoid -> {
                // Initialize empty messages node
                chatRef.child("messages").setValue(null)
                    .addOnSuccessListener(messagesVoid -> {
                        Log.d(TAG, "Chat structure created successfully");
                        // Now open the chat activity with the initial message
                        openChatActivity(chatId, message);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to create messages node: " + e.getMessage());
                        Toast.makeText(ProfileActivity.this, 
                            "Error creating chat: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to create chat structure: " + e.getMessage());
                Toast.makeText(ProfileActivity.this, 
                    "Error creating chat: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            });
    }

    private void openChatActivity(String chatId, String message) {
        // Navigate to chat activity with the booking message
        Intent intent = new Intent(ProfileActivity.this, ChatActivity.class);
        intent.putExtra("chat_partner_name", manicurist.getName());
        intent.putExtra("chat_id", chatId);
        intent.putExtra("chat_partner_id", manicurist.getUid());
        intent.putExtra("initial_message", message);
        startActivity(intent);
    }

    /**
     * Generates a consistent chat ID for two users regardless of who initiates the chat
     */
    private String getChatId(String userId1, String userId2) {
        return userId1.compareTo(userId2) < 0 ? 
               userId1 + "_" + userId2 : 
               userId2 + "_" + userId1;
    }
}
