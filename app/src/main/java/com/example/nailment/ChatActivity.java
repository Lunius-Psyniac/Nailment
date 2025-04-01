package com.example.nailment;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

public class ChatActivity extends AppCompatActivity implements UserAdapter.OnUserClickListener {
    private static final String TAG = "ChatActivity";
    private RecyclerView messagesRecyclerView;
    private RecyclerView usersRecyclerView;
    private EditText messageInput;
    private Button sendButton;
    private MessageAdapter messageAdapter;
    private UserAdapter userAdapter;
    private DatabaseReference messagesRef;
    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;
    private LinearLayout chatContainer;
    private TextView chatTitleText;
    private String currentChatUserId;
    private String currentChatId;
    private LinearLayout userListContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        
        // Get chat partner info if coming from manicurist profile
        String chatPartnerName = getIntent().getStringExtra("chat_partner_name");
        String chatId = getIntent().getStringExtra("chat_id");
        
        // Initialize Firebase Database references
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        messagesRef = database.getReference("messages");
        usersRef = database.getReference("users");

        // Initialize views
        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        chatContainer = findViewById(R.id.chatContainer);
        chatTitleText = findViewById(R.id.chatTitleText);
        userListContainer = findViewById(R.id.userListContainer);

        // Setup RecyclerViews
        setupRecyclerViews();

        // If we have a chat partner, start chat immediately
        if (chatPartnerName != null && chatId != null) {
            userListContainer.setVisibility(View.GONE);
            chatContainer.setVisibility(View.VISIBLE);
            chatTitleText.setText(chatPartnerName);
            loadMessages(chatId);
        } else {
            // Otherwise, load user list as before
            loadUsers();
        }

        // Setup send button
        sendButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessage(messageText);
                messageInput.setText("");
            }
        });
    }

    private void setupRecyclerViews() {
        messageAdapter = new MessageAdapter();
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(messageAdapter);

        userAdapter = new UserAdapter(this);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(userAdapter);
    }

    private void loadUsers() {
        Log.d(TAG, "Loading users...");
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
        currentChatId = chatId;
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
                Toast.makeText(ChatActivity.this, 
                    "Error loading messages: " + databaseError.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage(String messageText) {
        if (currentChatUserId == null) return;

        String currentUserId = mAuth.getCurrentUser().getUid();
        String chatId = getChatId(currentUserId, currentChatUserId);
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
                    Toast.makeText(ChatActivity.this, 
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

    @Override
    public void onUserClick(User user) {
        currentChatUserId = user.getUid();
        chatTitleText.setText("Chat with " + user.getDisplayName());
        chatContainer.setVisibility(View.VISIBLE);
        loadMessages(user.getUid());
    }
} 