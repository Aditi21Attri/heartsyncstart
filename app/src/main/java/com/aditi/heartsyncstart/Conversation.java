package com.aditi.heartsyncstart;

public class Conversation {
    private User user;
    private String lastMessage;
    private long timestamp;

    public Conversation() {
    }

    public Conversation(User user, String lastMessage, long timestamp) {
        this.user = user;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
    }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}