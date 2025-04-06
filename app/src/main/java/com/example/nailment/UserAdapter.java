package com.example.nailment;

import android.content.Context;
import android.content.Intent;
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
    private Context context;

    public UserAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
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

        // Set click listener to open chat with this user
        holder.itemView.setOnClickListener(v -> {
            // Get current user's ID
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            
            // Create a chat ID using both user IDs to ensure uniqueness and consistency
            String chatId = getChatId(currentUserId, user.getUid());
            
            // Open chat activity with this user
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("chat_partner_name", user.getName());
            intent.putExtra("chat_id", chatId);
            intent.putExtra("chat_partner_id", user.getUid());
            context.startActivity(intent);
        });
    }

    /**
     * Generates a consistent chat ID for two users regardless of who initiates the chat
     */
    private String getChatId(String userId1, String userId2) {
        return userId1.compareTo(userId2) < 0 ? 
               userId1 + "_" + userId2 : 
               userId2 + "_" + userId1;
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePicture;
        TextView userNameText;
        TextView userEmailText;

        UserViewHolder(View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            userNameText = itemView.findViewById(R.id.userNameText);
            userEmailText = itemView.findViewById(R.id.userEmailText);
        }
    }
} 