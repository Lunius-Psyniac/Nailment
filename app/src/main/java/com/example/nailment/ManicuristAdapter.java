package com.example.nailment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        holder.nameTextView.setText(manicurist.getName());
        holder.servicesTextView.setText(manicurist.getServices());
        holder.locationTextView.setText(manicurist.getLocation());
        holder.imageView.setImageResource(manicurist.getImageResource());

        // Set click listener for each item
        holder.itemView.setOnClickListener(v -> clickListener.onClick(manicurist));
    }

    @Override
    public int getItemCount() {
        return manicurists.size();
    }

    // ViewHolder class to hold references to each item’s views
    public static class ManicuristViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, servicesTextView, locationTextView;
        ImageView imageView;

        public ManicuristViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.manicuristName);         // Correct ID
            servicesTextView = itemView.findViewById(R.id.manicuristServices); // Correct ID
            locationTextView = itemView.findViewById(R.id.manicuristLocation); // Correct ID
            imageView = itemView.findViewById(R.id.manicuristImage);           // Correct ID
        }
    }
}
