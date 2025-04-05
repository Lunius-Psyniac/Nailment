package com.example.nailment;

import java.util.HashMap;
import java.util.Map;

public class ChatMetadata {
    private Map<String, Boolean> participants;
    private String lastMessage;
    private long lastMessageTime;
    private String lastMessageSender;

    // Required empty constructor for Firebase
    public ChatMetadata() {
        participants = new HashMap<>();
    }

    public ChatMetadata(String user1Id, String user2Id) {
        participants = new HashMap<>();
        participants.put(user1Id, true);
        participants.put(user2Id, true);
    }

    public Map<String, Boolean> getParticipants() {
        return participants;
    }

    public void setParticipants(Map<String, Boolean> participants) {
        this.participants = participants;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public String getLastMessageSender() {
        return lastMessageSender;
    }

    public void setLastMessageSender(String lastMessageSender) {
        this.lastMessageSender = lastMessageSender;
    }

    public void updateLastMessage(Message message) {
        this.lastMessage = message.isImageMessage() ? "Image" : message.getText();
        this.lastMessageTime = Long.parseLong(message.getTimestamp());
        this.lastMessageSender = message.getSenderId();
    }
} 