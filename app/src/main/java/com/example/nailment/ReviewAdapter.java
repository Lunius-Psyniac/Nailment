package com.example.nailment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private static final String TAG = "ReviewAdapter";
    private List<Review> reviews;
    private SimpleDateFormat dateFormat;

    public ReviewAdapter() {
        this.reviews = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.review_item, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        Log.d(TAG, "Binding review at position " + position + ": " + review.getUserName());

        holder.userNameTextView.setText(review.getUserName());
        holder.ratingBar.setRating((float) review.getRating());
        holder.commentTextView.setText(review.getComment());

        String formattedDate = dateFormat.format(new Date(review.getTimestamp()));
        holder.dateTextView.setText(formattedDate);
        Log.d(TAG, "Set date for review: " + formattedDate);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public void setReviews(List<Review> reviews) {
        Log.d(TAG, "Setting " + reviews.size() + " reviews");
        this.reviews = reviews;
        notifyDataSetChanged();
    }

    public void addReview(Review review) {
        Log.d(TAG, "Adding new review from " + review.getUserName());
        reviews.add(review);
        notifyItemInserted(reviews.size() - 1);
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTextView;
        RatingBar ratingBar;
        TextView commentTextView;
        TextView dateTextView;

        ReviewViewHolder(View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.review_user_name);
            ratingBar = itemView.findViewById(R.id.review_rating);
            commentTextView = itemView.findViewById(R.id.review_comment);
            dateTextView = itemView.findViewById(R.id.review_date);
        }
    }
} 