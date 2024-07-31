package com.example.bookworld;

public class Message {
    private String senderUsername;
    private String recipientUsername;
    private String messageContent;
    private String senderId;

    public Message() {
        // Default constructor required for Firestore
    }

    public Message(String senderUsername, String recipientUsername, String messageContent, String senderId) {
        this.senderUsername = senderUsername;
        this.recipientUsername = recipientUsername;
        this.messageContent = messageContent;
        this.senderId = senderId;
    }

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

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
}
