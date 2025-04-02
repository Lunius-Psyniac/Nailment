package com.example.nailment;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
    private FirebaseAuth mAuth;
    private String chatId;
    private String chatPartnerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        mAuth = FirebaseAuth.getInstance();

        // Get chat partner info from intent
        chatPartnerName = getIntent().getStringExtra("chat_partner_name");
        String chatPartnerId = getIntent().getStringExtra("chat_partner_id");
        
        // Initialize Firebase Database references
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        messagesRef = database.getReference("messages");
        DatabaseReference usersRef = database.getReference("users").child(chatPartnerId);

        // Initialize views
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        TextView chatTitleText = findViewById(R.id.chatTitleText);
        ImageButton backButton = findViewById(R.id.backButton);
        ImageView chatPartnerProfilePicture = findViewById(R.id.chatPartnerProfilePicture);

        // Set chat title
        chatTitleText.setText(chatPartnerName);

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
        String currentUserId = mAuth.getCurrentUser().getUid();
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