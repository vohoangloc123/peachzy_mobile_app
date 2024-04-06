package com.example.peachzyapp.entities;

public class Item {
    private String time;
    private String message;
    private String avatar;

    public Item(String time, String name, String avatar) {
        this.time = time;
        this.message = name;
        this.avatar = avatar;
    }

    public Item(String time, String message) {
        this.time = time;
        this.message = message;
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
