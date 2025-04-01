package com.example.nailment;

public class Message {
    private String id;
    private String text;
    private String userId;
    private String userName;
    private long timestamp;

    // Required empty constructor for Firebase
    public Message() {}

    public Message(String text, String userId, String userName) {
        this.text = text;
        this.userId = userId;
        this.userName = userName;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
} 