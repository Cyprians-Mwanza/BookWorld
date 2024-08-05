package com.example.bookworld;

public class FriendRequest {
    private String senderUsername;
    private String recipientUsername;
    private String senderId;
    private String requestId; // Field for the request ID

    // Default constructor for Firestore
    public FriendRequest() {
    }

    public FriendRequest(String senderUsername, String recipientUsername, String senderId, String requestId) {
        this.senderUsername = senderUsername;
        this.recipientUsername = recipientUsername;
        this.senderId = senderId;
        this.requestId = requestId; // Initialize requestId
    }

    // Getters and Setters
    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getRecipientUsername() {
        return recipientUsername;
    }

    public void setRecipientUsername(String recipientUsername) {
        this.recipientUsername = recipientUsername;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
