package com.example.peachzyapp.entities;

public class Conversation {
    private String conversationID;
    private String message;
    private String time;
    private String avatar;
    private String name;

    public Conversation(String conversationID, String message, String time, String avatar, String name) {
        this.conversationID = conversationID;
        this.message = message;
        this.time = time;
        this.avatar = avatar;
        this.name = name;
    }

    public Conversation() {
    }

    public String getConversationID() {
        return conversationID;
    }

    public void setConversationID(String conversationID) {
        this.conversationID = conversationID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "conversationID='" + conversationID + '\'' +
                ", message='" + message + '\'' +
                ", time='" + time + '\'' +
                ", avatar='" + avatar + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
