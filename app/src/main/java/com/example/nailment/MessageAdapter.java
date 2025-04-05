package com.example.nailment;

import android.util.Log;
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

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private static final String TAG = "MessageAdapter";
    private List<Message> messages = new ArrayList<>();
    private String currentUserId;

    public MessageAdapter() {
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

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
        boolean isCurrentUser = message.getSenderId().equals(currentUserId);

        // Set message alignment and background based on sender
        ViewGroup.LayoutParams params = holder.messageContainer.getLayoutParams();
        if (params instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) params;
            if (isCurrentUser) {
                marginParams.setMargins(64, 0, 8, 0);
                holder.messageContainer.setBackgroundResource(R.drawable.bg_message_sent);
            } else {
                marginParams.setMargins(8, 0, 64, 0);
                holder.messageContainer.setBackgroundResource(R.drawable.bg_message_received);
            }
            holder.messageContainer.setLayoutParams(marginParams);
        }

        // Handle image messages
        if (message.isImageMessage() && message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
            holder.messageText.setVisibility(View.GONE);
            holder.messageImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(message.getImageUrl())
                    .into(holder.messageImage);
        } else {
            holder.messageText.setVisibility(View.VISIBLE);
            holder.messageImage.setVisibility(View.GONE);
            holder.messageText.setText(message.getText());
        }
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
        TextView messageText;
        ImageView messageImage;
        View messageContainer;

        MessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            messageImage = itemView.findViewById(R.id.messageImage);
            messageContainer = itemView.findViewById(R.id.messageContainer);
        }
    }
} 