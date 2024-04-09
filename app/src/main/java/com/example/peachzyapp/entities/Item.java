package com.example.peachzyapp.entities;

import android.graphics.Bitmap;

public class Item {
    private String time;
    private String message;
    private String avatar;
    private boolean isSentByMe;
    private String imageUrl;
    private String bitmapString;

    public Item(String time, String message, String avatar, boolean isSentByMe) {
        this.time = time;
        this.message = message;
        this.avatar = avatar;
        this.isSentByMe = isSentByMe;
        this.imageUrl = null;
    }

    public Item(String time, String message, boolean isSentByMe) {
        this.time = time;
        this.message = message;
        this.isSentByMe = isSentByMe;
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
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    @Override
    public String toString() {
        return "Item{" +
                "time='" + time + '\'' +
                ", message='" + message + '\'' +
                ", avatar='" + avatar + '\'' +
                ", sentByMe=" + isSentByMe +
                '}';
    }
}
