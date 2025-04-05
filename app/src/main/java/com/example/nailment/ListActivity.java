package com.example.nailment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
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
import java.util.Comparator;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    private static final String TAG = "ListActivity";
    private RecyclerView manicuristRecyclerView;
    private List<Manicurist> manicuristList;
    private List<Manicurist> filteredList;
    private ManicuristAdapter adapter;
    private DatabaseReference usersRef;
    private EditText searchField;
    private ImageButton sortButton;
    private boolean isSortedByRating = false;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // Get current user ID
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "Current user ID: " + currentUserId);

        // Back button to go to the previous activity
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> finish());

        // Initialize Firebase Database reference
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        Log.d(TAG, "Database reference path: " + usersRef.toString());

        // Initialize search and sort views
        searchField = findViewById(R.id.searchField);
        sortButton = findViewById(R.id.sortButton);

        // Set up search functionality
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterManicurists(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Set up sort functionality
        sortButton.setOnClickListener(v -> {
            isSortedByRating = !isSortedByRating;
            sortManicurists();
        });

        // RecyclerView setup
        manicuristRecyclerView = findViewById(R.id.manicuristRecyclerView);
        manicuristRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize lists and adapter
        manicuristList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new ManicuristAdapter(filteredList, this::openProfileActivity);
        manicuristRecyclerView.setAdapter(adapter);

        // Load manicurists from Firebase
        loadManicurists();

        // Bottom Navigation Bar
        findViewById(R.id.homeButton).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }

    private void filterManicurists(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(manicuristList);
        } else {
            query = query.toLowerCase();
            for (Manicurist manicurist : manicuristList) {
                if (manicurist.getName().toLowerCase().contains(query) ||
                    manicurist.getLocation().toLowerCase().contains(query)) {
                    filteredList.add(manicurist);
                }
            }
        }
        sortManicurists();
    }

    private void sortManicurists() {
        if (isSortedByRating) {
            filteredList.sort((m1, m2) -> Double.compare(m2.getAvgRating(), m1.getAvgRating()));
        } else {
            filteredList.sort(Comparator.comparing(Manicurist::getName));
        }
        adapter.notifyDataSetChanged();
    }

    private void loadManicurists() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                manicuristList.clear();
                Log.d(TAG, "Loading manicurists from database...");
                Log.d(TAG, "Number of children in snapshot: " + dataSnapshot.getChildrenCount());
                
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "Processing user with key: " + userSnapshot.getKey());
                    Log.d(TAG, "User data: " + userSnapshot.getValue().toString());
                    
                    Manicurist manicurist = userSnapshot.getValue(Manicurist.class);
                    if (manicurist != null) {
                        Log.d(TAG, "Found user: " + manicurist.getName() + 
                              ", Type: " + manicurist.getUserType() + 
                              ", Active: " + manicurist.isAccountActive() +
                              ", Rating: " + manicurist.getAvgRating());
                        // Only add manicurists that are active, of type MANICURIST, and not the current user
                        if ("MANICURIST".equals(manicurist.getUserType()) && 
                            manicurist.isAccountActive() && 
                            !manicurist.getUid().equals(currentUserId)) {
                            manicuristList.add(manicurist);
                            Log.d(TAG, "Added manicurist to list: " + manicurist.getName() + 
                                  " with rating: " + manicurist.getAvgRating());
                        }
                    } else {
                        Log.e(TAG, "Failed to convert snapshot to Manicurist object for key: " + userSnapshot.getKey());
                    }
                }
                Log.d(TAG, "Total manicurists found: " + manicuristList.size());
                
                // Initialize filtered list with all manicurists
                filteredList.clear();
                filteredList.addAll(manicuristList);
                sortManicurists();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading manicurists: " + databaseError.getMessage());
                Toast.makeText(ListActivity.this, 
                    "Error loading manicurists: " + databaseError.getMessage(),
                    Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openProfileActivity(Manicurist manicurist) {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("manicurist", manicurist);
        startActivity(intent);
    }
}
