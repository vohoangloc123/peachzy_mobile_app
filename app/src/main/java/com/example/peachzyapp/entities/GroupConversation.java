package com.example.peachzyapp.entities;

public class GroupConversation {
    private String groupConversationID;
    private String groupName;
    private String message;
    private String time;
    private String avatar;
    private String name;

    public GroupConversation(String groupConversationID, String groupName, String message, String time, String avatar, String name) {
        this.groupConversationID = groupConversationID;
        this.groupName = groupName;
        this.message = message;
        this.time = time;
        this.avatar = avatar;
        this.name = name;
    }

    public String getGroupConversationID() {
        return groupConversationID;
    }

    public void setGroupConversationID(String groupConversationID) {
        this.groupConversationID = groupConversationID;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
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
        return "GroupConversation{" +
                "groupConversationID='" + groupConversationID + '\'' +
                ", groupName='" + groupName + '\'' +
                ", message='" + message + '\'' +
                ", time='" + time + '\'' +
                ", avatar='" + avatar + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
