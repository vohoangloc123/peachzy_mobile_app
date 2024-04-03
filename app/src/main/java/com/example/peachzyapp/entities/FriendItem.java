package com.example.peachzyapp.entities;

public class FriendItem {
    private String id;
    private String avatar;
    private String name;

    public FriendItem(String name) {
        this.name = name;
    }

    public FriendItem(String avatar, String name) {
        this.avatar = avatar;
        this.name = name;
    }

    public FriendItem(String id, String avatar, String name) {
        this.id = id;
        this.avatar = avatar;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getName() {
        return name;
    }
}
