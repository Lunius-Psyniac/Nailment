package com.example.nailment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

public class ChatActivity extends AppCompatActivity implements UserAdapter.OnUserClickListener {
    private static final String TAG = "ChatActivity";
    private RecyclerView usersRecyclerView;
    private UserAdapter userAdapter;
    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> finish());
        
        // Initialize Firebase Database references
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");

        // Initialize views
        usersRecyclerView = findViewById(R.id.usersRecyclerView);

        // Setup RecyclerView
        userAdapter = new UserAdapter(this);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(userAdapter);

        // Load users
        loadUsers();
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

    @Override
    public void onUserClick(User user) {
        Intent intent = new Intent(this, ChatDetailActivity.class);
        intent.putExtra("chat_partner_name", user.getName());
        intent.putExtra("chat_partner_id", user.getUid());
        startActivity(intent);
    }
} 