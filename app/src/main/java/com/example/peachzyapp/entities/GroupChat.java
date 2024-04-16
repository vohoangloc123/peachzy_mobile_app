package com.example.peachzyapp.entities;

public class GroupChat {
    private String id;
    private String groupName;
    private String avatar;
    private String message;

    private String name;
    private String time;
    private String userID;

    public GroupChat(String avatar, String message, String name, String time, String userID) {
        this.avatar = avatar;
        this.message = message;
        this.name = name;
        this.time = time;
        this.userID = userID;
    }

    public GroupChat(String id, String groupName, String avatar, String message, String name, String time) {
        this.id = id;
        this.groupName=groupName;
        this.avatar = avatar;
        this.message = message;
        this.name = name;
        this.time = time;

    }
    public GroupChat(String id, String groupName, String avatar, String message, String name, String time, String userID) {
        this.id = id;
        this.groupName=groupName;
        this.avatar = avatar;
        this.message = message;
        this.name = name;
        this.time = time;
        this.userID=userID;

    }
    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "GroupChat{" +
                "id='" + id + '\'' +
                ", groupName='" + groupName + '\'' +
                ", avatar='" + avatar + '\'' +
                ", message='" + message + '\'' +
                ", name='" + name + '\'' +
                ", time='" + time + '\'' +
                ", userID='" + userID + '\'' +
                '}';
    }
}
