package com.example.peachzyapp.entities;

import android.graphics.Bitmap;

public class Item {
    private String time;
    private String message;
    private String avatar;
    private String userID;
    private String imageUrl;
    private String bitmapString;

    public Item(String time, String message, String avatar, String userID) {
        this.time = time;
        this.message = message;
        this.avatar = avatar;
        this.userID = userID;
        this.imageUrl = null;
    }

    public Item(String time, String message, String userID) {
        this.time = time;
        this.message = message;
        this.userID = userID;
    }

    public Item(String time, String message) {
        this.time = time;
        this.message = message;
    }
    public String getBitmapString() {
        return bitmapString;
    }

    public void setBitmapString(String bitmapString) {
        this.bitmapString = bitmapString;
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
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    @Override
    public String toString() {
        return "Item{" +
                "time='" + time + '\'' +
                ", message='" + message + '\'' +
                ", avatar='" + avatar + '\'' +
                ", userID=" + userID +
                '}';
    }
}
