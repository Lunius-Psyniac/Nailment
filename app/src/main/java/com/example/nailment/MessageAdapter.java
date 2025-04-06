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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messages = new ArrayList<>();
    private String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        
        // Set sender name
        holder.userNameText.setText(message.getSenderId().equals(currentUserId) ? "You" : "Other");
        
        // Format timestamp
        long timestamp = Long.parseLong(message.getTimestamp());
        String timeString = timeFormat.format(new Date(timestamp));
        holder.messageTime.setText(timeString);
        
        // Check if it's an image message
        if (message.isImageMessage()) {
            // Hide text view and show image view
            holder.messageText.setVisibility(View.GONE);
            holder.messageImage.setVisibility(View.VISIBLE);
            
            // Load image using Glide
            Glide.with(holder.itemView.getContext())
                    .load(message.getText()) // Use text field as image URL
                    .into(holder.messageImage);
        } else {
            // Hide image view and show text view
            holder.messageText.setVisibility(View.VISIBLE);
            holder.messageImage.setVisibility(View.GONE);
            
            // Set message text
            holder.messageText.setText(message.getText());
        }
        
        // Align message to right if sent by current user, left otherwise
        View messageContainer = holder.itemView.findViewById(R.id.messageContainer);
        ViewGroup.LayoutParams params = messageContainer.getLayoutParams();
        ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) params;
        
        if (message.getSenderId().equals(currentUserId)) {
            marginParams.setMargins(100, 0, 0, 0);
            holder.userNameText.setVisibility(View.GONE);
        } else {
            marginParams.setMargins(0, 0, 100, 0);
            holder.userNameText.setVisibility(View.VISIBLE);
        }
        
        messageContainer.setLayoutParams(marginParams);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView userNameText;
        TextView messageText;
        TextView messageTime;
        ImageView messageImage;

        MessageViewHolder(View itemView) {
            super(itemView);
            userNameText = itemView.findViewById(R.id.userNameText);
            messageText = itemView.findViewById(R.id.messageText);
            messageTime = itemView.findViewById(R.id.messageTime);
            messageImage = itemView.findViewById(R.id.messageImage);
        }
    }
} 