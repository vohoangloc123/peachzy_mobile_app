package com.example.peachzyapp.entities;

public class Item {
    private String time;
    private String message;
    private String avatar;
    private boolean isSentByMe;

    public Item(String time, String message, boolean isSentByMe) {
        this.time = time;
        this.message = message;
        this.isSentByMe = isSentByMe;
    }

    public Item(String time, String message) {
        this.time = time;
        this.message = message;
    }

    public boolean isSentByMe() {
        return isSentByMe;
    }

    public void setSentByMe(boolean sentByMe) {
        isSentByMe = sentByMe;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
