package com.example.nailment;

public class Message {
    public static final String TYPE_TEXT = "text";
    public static final String TYPE_IMAGE = "image";
    
    public static final String STATUS_SENT = "sent";
    public static final String STATUS_DELIVERED = "delivered";
    public static final String STATUS_READ = "read";

    private String text;
    private String senderId;
    private String receiverId;
    private String timestamp;
    private String type;
    private String imageUrl;
    private String status;

    // Empty constructor required for Firebase
    public Message() {
        this.status = STATUS_SENT;
    }

    // Constructor for text messages
    public Message(String text, String senderId, String receiverId) {
        this.text = text;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.timestamp = String.valueOf(System.currentTimeMillis());
        this.type = TYPE_TEXT;
        this.status = STATUS_SENT;
    }

    // Constructor for image messages
    public Message(String imageUrl, String senderId, String receiverId, boolean isImageMessage) {
        this.imageUrl = imageUrl;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.timestamp = String.valueOf(System.currentTimeMillis());
        this.type = TYPE_IMAGE;
        this.status = STATUS_SENT;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isImageMessage() {
        return TYPE_IMAGE.equals(type);
    }
} 