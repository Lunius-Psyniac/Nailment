package com.example.nailment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> users = new ArrayList<>();
    private final OnUserClickListener listener;
    private final String currentUserId;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UserAdapter(OnUserClickListener listener) {
        this.listener = listener;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.userNameText.setText(user.getName());
        
        // Display different information based on user type
        if (user.getUserType().equals("MANICURIST")) {
            holder.userEmailText.setText(String.format("%s • Rating: %.1f", 
                user.getLocation(), user.getAvgRating()));
        } else {
            holder.userEmailText.setText(user.getSelfDescription());
        }

        // Load profile picture
        String profilePictureUrl = user.getProfilePictureLink();
        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(profilePictureUrl)
                .circleCrop()
                .into(holder.profilePicture);
        } else {
            holder.profilePicture.setImageResource(R.drawable.ic_person);
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void setUsers(List<User> users) {
        // Filter out the current user
        List<User> filteredUsers = new ArrayList<>();
        for (User user : users) {
            if (!user.getUid().equals(currentUserId)) {
                filteredUsers.add(user);
            }
        }
        this.users = filteredUsers;
        notifyDataSetChanged();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePicture;
        TextView userNameText;
        TextView userEmailText;

        UserViewHolder(View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            userNameText = itemView.findViewById(R.id.userNameText);
            userEmailText = itemView.findViewById(R.id.userEmailText);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onUserClick(users.get(position));
                }
            });
        }
    }
} 