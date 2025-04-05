package com.example.nailment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private RecyclerView usersRecyclerView;
    private RecyclerView messagesRecyclerView;
    private UserAdapter userAdapter;
    private MessageAdapter messageAdapter;
    private DatabaseReference usersRef;
    private DatabaseReference messagesRef;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private String currentChatId;
    private String initialMessage;
    private TextView messageInput;
    private View userListContainer;
    private View chatContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Check if user is authenticated
        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "User is not authenticated");
            Toast.makeText(this, "You must be logged in to use chat", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get chat partner name and chat ID from intent
        String chatPartnerName = getIntent().getStringExtra("chat_partner_name");
        currentChatId = getIntent().getStringExtra("chat_id");
        initialMessage = getIntent().getStringExtra("initial_message");
        String chatPartnerId = getIntent().getStringExtra("chat_partner_id");

        // Debug chat ID format
        debugChatIdFormat();

        // Set up back button
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Set up chat title
        TextView chatTitle = findViewById(R.id.chatTitle);
        if (chatPartnerName != null) {
            chatTitle.setText(chatPartnerName);
        }

        // Initialize views
        userListContainer = findViewById(R.id.userListContainer);
        chatContainer = findViewById(R.id.chatContainer);
        
        // Initialize RecyclerView for users
        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(this);
        usersRecyclerView.setAdapter(userAdapter);
        
        // Initialize RecyclerView for messages
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageAdapter();
        messagesRecyclerView.setAdapter(messageAdapter);

        // Initialize message input
        messageInput = findViewById(R.id.messageInput);
        Button sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(v -> sendMessage());

        // If we have a chat ID, show chat view
        if (currentChatId != null) {
            showChatView();
            checkChatAccessAndLoadMessages(currentChatId);
            
            // If we have an initial message, send it
            if (initialMessage != null && !initialMessage.isEmpty()) {
                messageInput.setText(initialMessage);
                sendMessage();
            }
        } else {
            // Show user list if no specific chat
            showUserListView();
            loadUsers();
        }
    }

    private void showChatView() {
        userListContainer.setVisibility(View.GONE);
        chatContainer.setVisibility(View.VISIBLE);
    }

    private void showUserListView() {
        userListContainer.setVisibility(View.VISIBLE);
        chatContainer.setVisibility(View.GONE);
    }

    private void loadUsers() {
        Log.d(TAG, "Loading users...");
        usersRef = database.getReference("users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Users data changed");
                List<User> users = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        Log.d(TAG, "Found user: " + user.getEmail() + " with ID: " + user.getUid());
                        users.add(user);
                    } else {
                        Log.w(TAG, "User data is null for snapshot: " + snapshot.getKey());
                    }
                }
                Log.d(TAG, "Total users found: " + users.size());
                userAdapter.setUsers(users);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading users: " + databaseError.getMessage());
                Log.e(TAG, "Error code: " + databaseError.getCode());
                Log.e(TAG, "Error details: " + databaseError.getDetails());
                Toast.makeText(ChatActivity.this, 
                    "Error loading users: " + databaseError.getMessage(),
                    Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadMessages(String chatId) {
        Log.d(TAG, "Loading messages for chat ID: " + chatId);
        messagesRef = database.getReference("chats").child(chatId).child("messages");
        Log.d(TAG, "Messages reference path: " + messagesRef.toString());
        
        String currentUserId = mAuth.getCurrentUser().getUid();
        
        messagesRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Messages data changed. Number of messages: " + dataSnapshot.getChildrenCount());
                List<Message> messages = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "Processing message with key: " + snapshot.getKey());
                    Message message = snapshot.getValue(Message.class);
                    if (message != null) {
                        // Only add messages where current user is either sender or receiver
                        if (message.getSenderId().equals(currentUserId) || 
                            message.getReceiverId().equals(currentUserId)) {
                            Log.d(TAG, "Message loaded: " + message.getText() + 
                                  " from " + message.getSenderId() + 
                                  " to " + message.getReceiverId());
                            messages.add(message);
                        }
                    } else {
                        Log.e(TAG, "Failed to convert snapshot to Message object for key: " + snapshot.getKey());
                        Log.e(TAG, "Snapshot value: " + snapshot.getValue().toString());
                    }
                }
                Log.d(TAG, "Total messages loaded: " + messages.size());
                messageAdapter.setMessages(messages);
                if (!messages.isEmpty()) {
                    messagesRecyclerView.scrollToPosition(messages.size() - 1);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading messages: " + databaseError.getMessage());
                Log.e(TAG, "Error code: " + databaseError.getCode());
                Log.e(TAG, "Error details: " + databaseError.getDetails());
                Toast.makeText(ChatActivity.this, 
                    "Error loading messages: " + databaseError.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (messageText.isEmpty() || currentChatId == null || mAuth.getCurrentUser() == null) {
            Log.e(TAG, "Cannot send message: " + 
                  (messageText.isEmpty() ? "Message is empty" : "") +
                  (currentChatId == null ? "Chat ID is null" : "") +
                  (mAuth.getCurrentUser() == null ? "User is not authenticated" : ""));
            return;
        }

        sendMessage(messageText);
    }

    private void sendMessage(String messageText) {
        if (messageText.trim().isEmpty()) return;

        String currentUserId = mAuth.getCurrentUser().getUid();
        String chatId = currentChatId;
        
        // Add very visible error logs
        Log.e(TAG, "ERROR CHECK - Current user ID: " + currentUserId);
        Log.e(TAG, "ERROR CHECK - Chat ID: " + chatId);
        Log.e(TAG, "ERROR CHECK - Chat ID length: " + (chatId != null ? chatId.length() : "null"));
        Log.e(TAG, "ERROR CHECK - Current user ID length: " + (currentUserId != null ? currentUserId.length() : "null"));
        
        if (chatId != null && currentUserId != null) {
            Log.e(TAG, "ERROR CHECK - Chat ID contains current user ID: " + chatId.contains(currentUserId));
            Log.e(TAG, "ERROR CHECK - Chat ID contains underscore: " + chatId.contains("_"));
            if (chatId.contains("_")) {
                String[] parts = chatId.split("_");
                Log.e(TAG, "ERROR CHECK - Number of parts after split: " + parts.length);
                for (int i = 0; i < parts.length; i++) {
                    Log.e(TAG, "ERROR CHECK - Part " + i + ": " + parts[i]);
                }
            }
        }

        // First verify the current user exists in the database
        database.getReference("users").child(currentUserId).get()
            .addOnSuccessListener(currentUserSnapshot -> {
                if (!currentUserSnapshot.exists()) {
                    Log.e(TAG, "ERROR CHECK - Current user does not exist in database: " + currentUserId);
                    Toast.makeText(ChatActivity.this, 
                        "Error: User account not found", 
                        Toast.LENGTH_SHORT).show();
                    return;
                }

                // Extract the other user's ID from the chat ID
                final String otherUserId;
                
                // Check if chatId contains the current user ID
                if (chatId != null && currentUserId != null) {
                    // Try different formats of chat ID
                    if (chatId.contains(currentUserId)) {
                        // Remove the current user ID and any underscores to get the other user's ID
                        otherUserId = chatId.replace(currentUserId, "").replace("_", "");
                        Log.e(TAG, "ERROR CHECK - Extracted recipient ID using replace: " + otherUserId);
                    } else if (chatId.contains("_")) {
                        // Try to extract from format like "user1_user2"
                        String[] parts = chatId.split("_");
                        if (parts.length == 2) {
                            otherUserId = parts[0].equals(currentUserId) ? parts[1] : parts[0];
                            Log.e(TAG, "ERROR CHECK - Extracted recipient ID from underscore format: " + otherUserId);
                        } else {
                            Log.e(TAG, "ERROR CHECK - Invalid chat ID format (multiple underscores): " + chatId);
                            Toast.makeText(ChatActivity.this, 
                                "Error: Invalid chat format", 
                                Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        Log.e(TAG, "ERROR CHECK - Chat ID does not contain current user ID or underscore: " + chatId);
                        Toast.makeText(ChatActivity.this, 
                            "Error: Invalid chat format", 
                            Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    Log.e(TAG, "ERROR CHECK - Chat ID or current user ID is null");
                    Toast.makeText(ChatActivity.this, 
                        "Error: Invalid chat data", 
                        Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (otherUserId == null || otherUserId.isEmpty()) {
                    Log.e(TAG, "ERROR CHECK - Failed to extract recipient ID");
                    Toast.makeText(ChatActivity.this, 
                        "Error: Could not identify recipient", 
                        Toast.LENGTH_SHORT).show();
                    return;
                }
                
                Log.e(TAG, "ERROR CHECK - Final extracted recipient ID: " + otherUserId);
                
                // Verify the other user exists in the database
                database.getReference("users").child(otherUserId).get()
                    .addOnSuccessListener(dataSnapshot -> {
                        if (!dataSnapshot.exists()) {
                            Log.e(TAG, "ERROR CHECK - Recipient user does not exist: " + otherUserId);
                            Toast.makeText(ChatActivity.this, 
                                "Cannot send message: Recipient not found", 
                                Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Check if recipient's account is active
                        Boolean isActive = dataSnapshot.child("accountActive").getValue(Boolean.class);
                        if (isActive != null && isActive == false) {
                            Toast.makeText(ChatActivity.this, 
                                "Cannot send message: Recipient's account is deactivated", 
                                Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Get the actual user ID from the database
                        String recipientId = dataSnapshot.getKey();
                        Log.e(TAG, "ERROR CHECK - Verified recipient ID from database: " + recipientId);

                        // Create message with sender and receiver IDs
                        Message message = new Message(
                            messageText,
                            currentUserId,
                            recipientId
                        );

                        // Save message and update metadata
                        DatabaseReference chatRef = database.getReference("chats").child(chatId);
                        DatabaseReference messagesRef = chatRef.child("messages");
                        DatabaseReference metadataRef = chatRef.child("metadata");

                        messagesRef.push().setValue(message)
                                .addOnSuccessListener(aVoid -> {
                                    Log.e(TAG, "ERROR CHECK - Message sent successfully");
                                    messageInput.setText("");
                                    
                                    // Update chat metadata
                                    ChatMetadata metadata = new ChatMetadata();
                                    metadata.updateLastMessage(message);
                                    metadataRef.setValue(metadata);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "ERROR CHECK - Failed to send message: " + e.getMessage());
                                    Toast.makeText(ChatActivity.this, 
                                        "Failed to send message: " + e.getMessage(), 
                                        Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "ERROR CHECK - Failed to get recipient data: " + e.getMessage());
                        Toast.makeText(ChatActivity.this, 
                            "Failed to send message: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "ERROR CHECK - Failed to get current user data: " + e.getMessage());
                Toast.makeText(ChatActivity.this, 
                    "Failed to send message: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }

    private void checkChatAccessAndLoadMessages(String chatId) {
        Log.d(TAG, "Checking access to chat: " + chatId);
        
        // First check if the chat exists
        database.getReference("chats").child(chatId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "Chat exists, loading messages");
                    loadMessages(chatId);
                } else {
                    Log.d(TAG, "Chat does not exist, creating it");
                    String currentUserId = mAuth.getCurrentUser().getUid();
                    
                    Log.d(TAG, "Current user ID: " + currentUserId);
                    Log.d(TAG, "Chat ID: " + chatId);
                    
                    // First verify the current user exists in the database
                    database.getReference("users").child(currentUserId).get()
                        .addOnSuccessListener(currentUserSnapshot -> {
                            if (!currentUserSnapshot.exists()) {
                                Log.e(TAG, "Current user does not exist in database: " + currentUserId);
                                Toast.makeText(ChatActivity.this, 
                                    "Error: User account not found", 
                                    Toast.LENGTH_SHORT).show();
                                return;
                            }
                            
                            // Extract the other user's ID from the chat ID
                            final String otherUserId;
                            
                            // Check if chatId contains the current user ID
                            if (chatId != null && currentUserId != null) {
                                // Try different formats of chat ID
                                if (chatId.contains(currentUserId)) {
                                    // Remove the current user ID and any underscores to get the other user's ID
                                    otherUserId = chatId.replace(currentUserId, "").replace("_", "");
                                    Log.d(TAG, "Extracted recipient ID using replace: " + otherUserId);
                                } else if (chatId.contains("_")) {
                                    // Try to extract from format like "user1_user2"
                                    String[] parts = chatId.split("_");
                                    if (parts.length == 2) {
                                        otherUserId = parts[0].equals(currentUserId) ? parts[1] : parts[0];
                                        Log.d(TAG, "Extracted recipient ID from underscore format: " + otherUserId);
                                    } else {
                                        Log.e(TAG, "Invalid chat ID format (multiple underscores): " + chatId);
                                        Toast.makeText(ChatActivity.this, 
                                            "Error: Invalid chat format", 
                                            Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                } else {
                                    Log.e(TAG, "Chat ID does not contain current user ID or underscore: " + chatId);
                                    Toast.makeText(ChatActivity.this, 
                                        "Error: Invalid chat format", 
                                        Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            } else {
                                Log.e(TAG, "Chat ID or current user ID is null");
                                Toast.makeText(ChatActivity.this, 
                                    "Error: Invalid chat data", 
                                    Toast.LENGTH_SHORT).show();
                                return;
                            }
                            
                            if (otherUserId == null || otherUserId.isEmpty()) {
                                Log.e(TAG, "Failed to extract recipient ID");
                                Toast.makeText(ChatActivity.this, 
                                    "Error: Could not identify recipient", 
                                    Toast.LENGTH_SHORT).show();
                                return;
                            }
                            
                            Log.d(TAG, "Extracted recipient ID for new chat: " + otherUserId);
                            
                            // Verify the other user exists in the database
                            database.getReference("users").child(otherUserId).get()
                                .addOnSuccessListener(userSnapshot -> {
                                    if (!userSnapshot.exists()) {
                                        Log.e(TAG, "Other user does not exist: " + otherUserId);
                                        Toast.makeText(ChatActivity.this, 
                                            "Cannot create chat: User not found", 
                                            Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    
                                    // Get the actual user ID from the database
                                    String recipientId = userSnapshot.getKey();
                                    Log.d(TAG, "Creating chat with user ID: " + recipientId);
                                    
                                    // Create chat metadata with actual database IDs
                                    ChatMetadata metadata = new ChatMetadata(currentUserId, recipientId);
                                    
                                    // Create the chat structure with metadata
                                    database.getReference("chats").child(chatId).setValue(metadata)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Chat structure created successfully");
                                            loadMessages(chatId);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Failed to create chat structure: " + e.getMessage());
                                            Toast.makeText(ChatActivity.this, 
                                                "Error creating chat: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                        });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to get user data: " + e.getMessage());
                                    Toast.makeText(ChatActivity.this, 
                                        "Error creating chat: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                });
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to get current user data: " + e.getMessage());
                            Toast.makeText(ChatActivity.this, 
                                "Error creating chat: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error checking chat access: " + databaseError.getMessage());
                Toast.makeText(ChatActivity.this, 
                    "Error accessing chat: " + databaseError.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Debug method to check the chat ID format
     */
    private void debugChatIdFormat() {
        try {
            Log.e(TAG, "===========================================");
            Log.e(TAG, "============ CHAT ID DEBUG START ============");
            
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Log.e(TAG, "ERROR: Current user is null!");
                return;
            }
            
            String currentUserId = currentUser.getUid();
            String chatId = currentChatId;
            
            Log.e(TAG, "Current user ID: " + (currentUserId != null ? currentUserId : "NULL"));
            Log.e(TAG, "Chat ID: " + (chatId != null ? chatId : "NULL"));
            
            if (chatId == null) {
                Log.e(TAG, "ERROR: Chat ID is null!");
                return;
            }
            
            if (currentUserId == null) {
                Log.e(TAG, "ERROR: Current user ID is null!");
                return;
            }
            
            Log.e(TAG, "Chat ID length: " + chatId.length());
            Log.e(TAG, "Current user ID length: " + currentUserId.length());
            Log.e(TAG, "Chat ID contains current user ID: " + chatId.contains(currentUserId));
            Log.e(TAG, "Chat ID contains underscore: " + chatId.contains("_"));
            
            if (chatId.contains("_")) {
                String[] parts = chatId.split("_");
                Log.e(TAG, "Number of parts after split: " + parts.length);
                for (int i = 0; i < parts.length; i++) {
                    Log.e(TAG, "Part " + i + ": " + parts[i]);
                }
            }
            
            // Try to extract the other user ID
            String otherUserId = null;
            if (chatId.contains(currentUserId)) {
                otherUserId = chatId.replace(currentUserId, "").replace("_", "");
                Log.e(TAG, "Extracted recipient ID using replace: " + otherUserId);
            } else if (chatId.contains("_")) {
                String[] parts = chatId.split("_");
                if (parts.length == 2) {
                    otherUserId = parts[0].equals(currentUserId) ? parts[1] : parts[0];
                    Log.e(TAG, "Extracted recipient ID from underscore format: " + otherUserId);
                }
            }
            
            // Print the chat ID format analysis
            Log.e(TAG, "Chat ID Format Analysis:");
            Log.e(TAG, "1. Is null? " + (chatId == null));
            Log.e(TAG, "2. Is empty? " + (chatId != null && chatId.isEmpty()));
            Log.e(TAG, "3. Contains current user ID? " + (chatId != null && chatId.contains(currentUserId)));
            Log.e(TAG, "4. Contains underscore? " + (chatId != null && chatId.contains("_")));
            if (chatId != null && chatId.contains("_")) {
                Log.e(TAG, "5. Number of underscores: " + chatId.split("_").length);
            }
            
            Log.e(TAG, "============ CHAT ID DEBUG END ============");
            Log.e(TAG, "===========================================");
            
        } catch (Exception e) {
            Log.e(TAG, "ERROR in debugChatIdFormat: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 