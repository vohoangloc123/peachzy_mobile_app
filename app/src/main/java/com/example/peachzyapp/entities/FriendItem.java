package com.example.peachzyapp.entities;

public class FriendItem {
    private String avatar;
    private String name;

    public FriendItem(String avatar, String name) {
        this.avatar = avatar;
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getName() {
        return name;
    }
}
