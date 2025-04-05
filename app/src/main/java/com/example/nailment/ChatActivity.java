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
        
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Messages data changed. Number of messages: " + dataSnapshot.getChildrenCount());
                List<Message> messages = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "Processing message with key: " + snapshot.getKey());
                    Message message = snapshot.getValue(Message.class);
                    if (message != null) {
                        Log.d(TAG, "Message loaded: " + message.getText() + " from " + message.getUserName());
                        messages.add(message);
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

        // Check if recipient's account is active
        String recipientId = chatId.replace(currentUserId, "").replace("_", "");
        database.getReference("users").child(recipientId).child("accountActive").get()
                .addOnSuccessListener(dataSnapshot -> {
                    Boolean isActive = dataSnapshot.getValue(Boolean.class);
                    // Only block if accountActive is explicitly false
                    if (isActive != null && isActive == false) {
                        Toast.makeText(ChatActivity.this, 
                            "Cannot send message: Recipient's account is deactivated", 
                            Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Get current user's email as username
                    String userName = mAuth.getCurrentUser().getEmail();
                    if (userName == null) {
                        userName = "User";
                    }

                    // Recipient is active or accountActive is not set, proceed with sending message
                    Message message = new Message(
                        messageText,
                        currentUserId,
                        userName
                    );

                    messagesRef.push().setValue(message)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Message sent successfully");
                                messageInput.setText("");
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to send message: " + e.getMessage());
                                Toast.makeText(ChatActivity.this, 
                                    "Failed to send message: " + e.getMessage(), 
                                    Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to check recipient status: " + e.getMessage());
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
                    // Create the chat structure
                    database.getReference("chats").child(chatId).child("messages").setValue(null)
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
} 