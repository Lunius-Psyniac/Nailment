package com.example.nailment;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ReviewsActivity extends AppCompatActivity {
    private static final String TAG = "ReviewsActivity";
    private RecyclerView reviewsRecyclerView;
    private ReviewAdapter reviewAdapter;
    private DatabaseReference reviewsRef;
    private String manicuristId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        manicuristId = getIntent().getStringExtra("manicurist_id");
        if (manicuristId == null) {
            Log.e(TAG, "No manicurist ID provided");
            Toast.makeText(this, "Error: No manicurist ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.d(TAG, "Manicurist ID: " + manicuristId);

        // Initialize Firebase Database reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        reviewsRef = database.getReference().child("reviews").child(manicuristId);
        Log.d(TAG, "Database reference path: " + reviewsRef.toString());

        // Initialize views
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);
        ImageButton backButton = findViewById(R.id.backButton);

        // Setup back button
        backButton.setOnClickListener(view -> finish());

        // Setup RecyclerView
        reviewAdapter = new ReviewAdapter();
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewsRecyclerView.setAdapter(reviewAdapter);

        // Load reviews
        loadReviews();
    }

    private void loadReviews() {
        Log.d(TAG, "Loading reviews for manicurist: " + manicuristId);
        reviewsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Number of reviews: " + dataSnapshot.getChildrenCount());
                List<Review> reviews = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Log.d(TAG, "Review data: " + snapshot.getValue().toString());
                        Review review = snapshot.getValue(Review.class);
                        if (review != null) {
                            reviews.add(review);
                            Log.d(TAG, "Added review from " + review.getUserName() + " with rating " + review.getRating());
                        } else {
                            Log.e(TAG, "Failed to parse review from snapshot: " + snapshot.getValue().toString());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing review: " + e.getMessage());
                    }
                }
                Log.d(TAG, "Total reviews loaded: " + reviews.size());

                // Sort reviews by timestamp (newest first)
                Collections.sort(reviews, (r1, r2) -> Long.compare(r2.getTimestamp(), r1.getTimestamp()));

                reviewAdapter.setReviews(reviews);

                // Show message if no reviews
                if (reviews.isEmpty()) {
                    Toast.makeText(ReviewsActivity.this, "No reviews yet", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading reviews: " + databaseError.getMessage());
                Toast.makeText(ReviewsActivity.this, "Error loading reviews", Toast.LENGTH_SHORT).show();
            }
        });
    }
} 