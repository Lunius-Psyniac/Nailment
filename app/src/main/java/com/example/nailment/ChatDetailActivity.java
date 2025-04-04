package com.example.nailment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ChatDetailActivity extends AppCompatActivity {
    private static final String TAG = "ChatDetailActivity";
    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private Button sendButton;
    private MessageAdapter messageAdapter;
    private DatabaseReference messagesRef;
    private DatabaseReference reviewsRef;
    private FirebaseAuth mAuth;
    private String chatId;
    private String chatPartnerName;
    private String chatPartnerId;
    private String currentUserId;
    private String currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        currentUserName = mAuth.getCurrentUser().getEmail();

        // Get chat partner info from intent
        chatPartnerName = getIntent().getStringExtra("chat_partner_name");
        chatPartnerId = getIntent().getStringExtra("chat_partner_id");
        
        // Initialize Firebase Database references
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        messagesRef = database.getReference("messages");
        reviewsRef = database.getReference("reviews");
        DatabaseReference usersRef = database.getReference("users").child(chatPartnerId);

        // Initialize views
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        TextView chatTitleText = findViewById(R.id.chatTitleText);
        ImageButton backButton = findViewById(R.id.backButton);
        ImageView chatPartnerProfilePicture = findViewById(R.id.chatPartnerProfilePicture);
        Button reviewButton = findViewById(R.id.reviewButton);

        // Set chat title
        chatTitleText.setText(chatPartnerName);

        // Setup review button
        reviewButton.setOnClickListener(v -> showReviewDialog());

        // Load chat partner's profile picture
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User chatPartner = dataSnapshot.getValue(User.class);
                if (chatPartner != null) {
                    String profilePictureUrl = chatPartner.getProfilePictureLink();
                    if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                        Glide.with(ChatDetailActivity.this)
                            .load(profilePictureUrl)
                            .circleCrop()
                            .into(chatPartnerProfilePicture);
                    } else {
                        chatPartnerProfilePicture.setImageResource(R.drawable.ic_person);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading chat partner data: " + databaseError.getMessage());
            }
        });

        // Setup back button
        backButton.setOnClickListener(view -> finish());

        // Setup RecyclerView
        messageAdapter = new MessageAdapter();
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(messageAdapter);

        // Generate chat ID
        chatId = getChatId(currentUserId, chatPartnerId);

        // Load messages
        loadMessages();

        // Setup message input
        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessageIfNotEmpty();
                return true;
            }
            return false;
        });

        // Setup send button
        sendButton.setOnClickListener(v -> sendMessageIfNotEmpty());
    }

    private void showReviewDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_review, null);
        RatingBar ratingBar = dialogView.findViewById(R.id.review_rating_bar);
        EditText commentInput = dialogView.findViewById(R.id.review_comment);

        new AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Submit", (dialog, which) -> {
                float rating = ratingBar.getRating();
                String comment = commentInput.getText().toString().trim();
                
                if (!comment.isEmpty()) {
                    submitReview(rating, comment);
                } else {
                    Toast.makeText(this, "Please write a comment", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void submitReview(float rating, String comment) {
        Log.d(TAG, "Submitting review for manicurist: " + chatPartnerId + " with rating: " + rating);
        String reviewId = reviewsRef.child(chatPartnerId).push().getKey();
        if (reviewId != null) {
            Log.d(TAG, "Generated review ID: " + reviewId);
            Review review = new Review(reviewId, currentUserId, chatPartnerId, currentUserName, rating, comment);
            
            reviewsRef.child(chatPartnerId).child(reviewId).setValue(review)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Review submitted successfully");
                    Toast.makeText(this, "Review submitted successfully", Toast.LENGTH_SHORT).show();
                    updateManicuristRating(chatPartnerId, rating);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error submitting review: " + e.getMessage());
                    Toast.makeText(this, "Error submitting review", Toast.LENGTH_SHORT).show();
                });
        } else {
            Log.e(TAG, "Failed to generate review ID");
        }
    }

    private void updateManicuristRating(String manicuristId, float newRating) {
        Log.d(TAG, "Updating rating for manicurist: " + manicuristId + " with new rating: " + newRating);
        DatabaseReference manicuristRef = FirebaseDatabase.getInstance().getReference("users").child(manicuristId);
        manicuristRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Manicurist manicurist = dataSnapshot.getValue(Manicurist.class);
                if (manicurist != null) {
                    double currentRating = manicurist.getAvgRating();
                    int ratingCount = manicurist.getRatingCount();
                    Log.d(TAG, "Current rating: " + currentRating + ", rating count: " + ratingCount);
                    
                    // Calculate new average rating
                    double newAvgRating;
                    if (ratingCount == 0) {
                        // If this is the first rating, set it directly
                        newAvgRating = newRating;
                    } else {
                        // Calculate weighted average for existing ratings
                        newAvgRating = ((currentRating * ratingCount) + newRating) / (ratingCount + 1);
                    }
                    
                    Log.d(TAG, "New average rating: " + newAvgRating);
                    
                    // Update manicurist's rating
                    manicurist.setAvgRating(newAvgRating);
                    manicurist.setRatingCount(ratingCount + 1);
                    
                    manicuristRef.setValue(manicurist)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Manicurist rating updated successfully"))
                        .addOnFailureListener(e -> Log.e(TAG, "Error updating manicurist rating: " + e.getMessage()));
                } else {
                    Log.e(TAG, "Failed to get manicurist data");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error updating manicurist rating: " + databaseError.getMessage());
            }
        });
    }

    private void sendMessageIfNotEmpty() {
        String messageText = messageInput.getText().toString().trim();
        if (!messageText.isEmpty()) {
            sendMessage(messageText);
            messageInput.setText("");
        }
    }

    private void loadMessages() {
        messagesRef.child(chatId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Message> messages = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    if (message != null) {
                        messages.add(message);
                    }
                }
                messageAdapter.setMessages(messages);
                messagesRecyclerView.scrollToPosition(messages.size() - 1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading messages: " + databaseError.getMessage());
                Toast.makeText(ChatDetailActivity.this, 
                    "Error loading messages: " + databaseError.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage(String messageText) {
        String currentUserId = mAuth.getCurrentUser().getUid();
        String userName = mAuth.getCurrentUser().getEmail();
        Message message = new Message(messageText, currentUserId, userName);
        
        String messageId = messagesRef.child(chatId).push().getKey();
        if (messageId != null) {
            messagesRef.child(chatId).child(messageId).setValue(message)
                .addOnSuccessListener(aVoid -> {
                    messageInput.setText("");
                    Log.d(TAG, "Message sent successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error sending message: " + e.getMessage());
                    Toast.makeText(ChatDetailActivity.this, 
                        "Error sending message: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                });
        }
    }

    private String getChatId(String userId1, String userId2) {
        return userId1.compareTo(userId2) < 0 ? 
               userId1 + "_" + userId2 : 
               userId2 + "_" + userId1;
    }
} 