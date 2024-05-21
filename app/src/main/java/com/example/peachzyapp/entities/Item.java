package com.example.peachzyapp.entities;

import android.graphics.Bitmap;

import java.util.Objects;

public class Item {
    private String time;
    private String message;
    private String avatar;
    private boolean isSentByMe;
    private String imageUrl;
    private String bitmapString;
    private String type;



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

    public Item(String time, String message, boolean isSentByMe, String type) {
        this.time = time;
        this.message = message;
        this.isSentByMe = isSentByMe;
        this.type = type;
    }

    public Item(String time, String message, String avatar, boolean isSentByMe, String type) {
        this.time = time;
        this.message = message;
        this.avatar = avatar;
        this.isSentByMe = isSentByMe;
        this.type = type;
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


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(time, item.time) &&
                Objects.equals(message, item.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, message);
    }
}
