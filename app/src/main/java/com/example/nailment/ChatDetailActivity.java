package com.example.nailment;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatDetailActivity extends AppCompatActivity {
    private static final String TAG = "ChatDetailActivity";
    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private Button sendButton;
    private ImageButton attachImageButton;
    private MessageAdapter messageAdapter;
    private DatabaseReference messagesRef;
    private DatabaseReference reviewsRef;
    private FirebaseAuth mAuth;
    private String chatId;
    private String chatPartnerName;
    private String chatPartnerId;
    private String currentUserId;
    private String currentUserName;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private Uri selectedImageUri;
    
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    uploadImageToFirebaseStorage();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        currentUserName = mAuth.getCurrentUser().getEmail();
        
        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Get chat partner info from intent
        chatPartnerName = getIntent().getStringExtra("chat_partner_name");
        chatPartnerId = getIntent().getStringExtra("chat_partner_id");
        
        // Initialize Firebase Database references
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        messagesRef = database.getReference("chats");
        reviewsRef = database.getReference("reviews");
        DatabaseReference usersRef = database.getReference("users").child(chatPartnerId);

        // Initialize views
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        attachImageButton = findViewById(R.id.attachImageButton);
        TextView chatTitleText = findViewById(R.id.chatTitleText);
        ImageButton backButton = findViewById(R.id.backButton);
        ImageView chatPartnerProfilePicture = findViewById(R.id.chatPartnerProfilePicture);
        Button reviewButton = findViewById(R.id.reviewButton);

        // Set chat title
        chatTitleText.setText(chatPartnerName);

        // Setup review button
        reviewButton.setOnClickListener(v -> showReviewDialog());
        
        // Setup image attachment button
        attachImageButton.setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });

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

        // Get chat ID from intent
        chatId = getIntent().getStringExtra("chat_id");
        if (chatId == null) {
            // Generate chat ID if not provided
            chatId = getChatId(currentUserId, chatPartnerId);
        }

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
    
    private void uploadImageToFirebaseStorage() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show progress dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Uploading image...");
        builder.setCancelable(false);
        AlertDialog progressDialog = builder.create();
        progressDialog.show();
        
        // Create a unique filename for the image
        String imageFileName = "chatImages/" + chatId + "/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageRef.child(imageFileName);
        
        // Upload the image
        UploadTask uploadTask = imageRef.putFile(selectedImageUri);
        
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Get the download URL
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                progressDialog.dismiss();
                String imageUrl = uri.toString();
                Log.d(TAG, "Image uploaded successfully: " + imageUrl);
                
                // Send the image URL as a message
                sendImageMessage(imageUrl);
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Log.e(TAG, "Error getting download URL: " + e.getMessage());
                Toast.makeText(ChatDetailActivity.this, 
                    "Error uploading image: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Log.e(TAG, "Error uploading image: " + e.getMessage());
            Toast.makeText(ChatDetailActivity.this, 
                "Error uploading image: " + e.getMessage(), 
                Toast.LENGTH_SHORT).show();
        }).addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            Log.d(TAG, "Upload progress: " + progress + "%");
        });
    }
    
    private void sendImageMessage(String imageUrl) {
        String currentUserId = mAuth.getCurrentUser().getUid();
        
        Log.d(TAG, "Sending image message with URL: " + imageUrl);
        
        // Create a new message with the image URL
        Message message = new Message(imageUrl, currentUserId, chatPartnerId, true);
        
        // Save the message to Firebase
        messagesRef.child(chatId).child("messages").push().setValue(message)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Image message sent successfully");
                selectedImageUri = null;
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error sending image message: " + e.getMessage());
                Toast.makeText(ChatDetailActivity.this, 
                    "Error sending image: " + e.getMessage(), 
                    Toast.LENGTH_LONG).show();
            });
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
        String currentUserId = mAuth.getCurrentUser().getUid();
        
        messagesRef.child(chatId).child("messages")
            .orderByChild("timestamp")
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<Message> messages = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Message message = snapshot.getValue(Message.class);
                        if (message != null) {
                            // Only add messages where current user is either sender or receiver
                            if (message.getSenderId().equals(currentUserId) || 
                                message.getReceiverId().equals(currentUserId)) {
                                // Log message details for debugging
                                Log.d(TAG, "Loaded message: " + (message.isImageMessage() ? "Image message" : "Text message"));
                                if (message.isImageMessage()) {
                                    Log.d(TAG, "Image URL: " + message.getImageUrl());
                                } else {
                                    Log.d(TAG, "Text: " + message.getText());
                                }
                                Log.d(TAG, "From: " + message.getSenderId() + " To: " + message.getReceiverId());
                                messages.add(message);
                            }
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
        Message message = new Message(messageText, currentUserId, chatPartnerId);
        
        messagesRef.child(chatId).child("messages").push().setValue(message)
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

    private String getChatId(String userId1, String userId2) {
        return userId1.compareTo(userId2) < 0 ? 
               userId1 + "_" + userId2 : 
               userId2 + "_" + userId1;
    }
} 