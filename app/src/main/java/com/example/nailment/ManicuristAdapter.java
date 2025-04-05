package com.example.nailment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ManicuristAdapter extends RecyclerView.Adapter<ManicuristAdapter.ManicuristViewHolder> {

    private final List<Manicurist> manicurists;
    private final OnManicuristClickListener clickListener;

    // Interface to handle clicks on each manicurist item
    public interface OnManicuristClickListener {
        void onClick(Manicurist manicurist);
    }

    public ManicuristAdapter(List<Manicurist> manicurists, OnManicuristClickListener clickListener) {
        this.manicurists = manicurists;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ManicuristViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item in the RecyclerView
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.manicurist_item, parent, false);
        return new ManicuristViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ManicuristViewHolder holder, int position) {
        // Bind data to each item in the RecyclerView
        Manicurist manicurist = manicurists.get(position);
        
        // Set name
        holder.nameTextView.setText(manicurist.getName());
        
        // Set location
        holder.locationTextView.setText(manicurist.getLocation());
        
        // Set description
        holder.descriptionTextView.setText(manicurist.getSelfDescription());
        
        // Set rating with logging
        float rating = (float) manicurist.getAvgRating();
        Log.d("ManicuristAdapter", "Setting rating for " + manicurist.getName() + ": " + rating);
        holder.ratingBar.setRating(rating);
        
        // Load profile picture using Glide
        if (manicurist.getProfilePictureLink() != null && !manicurist.getProfilePictureLink().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(manicurist.getProfilePictureLink())
                .circleCrop()
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.placeholder_image);
        }

        // Set click listener for each item
        holder.itemView.setOnClickListener(v -> clickListener.onClick(manicurist));
    }

    @Override
    public int getItemCount() {
        return manicurists.size();
    }

    // ViewHolder class to hold references to each item's views
    public static class ManicuristViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, descriptionTextView, locationTextView;
        ImageView imageView;
        RatingBar ratingBar;

        public ManicuristViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.manicuristName);
            descriptionTextView = itemView.findViewById(R.id.manicuristDescription);
            locationTextView = itemView.findViewById(R.id.manicuristLocation);
            imageView = itemView.findViewById(R.id.manicuristImage);
            ratingBar = itemView.findViewById(R.id.manicuristRating);
        }
    }
}
