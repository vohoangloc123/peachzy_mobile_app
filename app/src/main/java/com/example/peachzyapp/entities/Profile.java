package com.example.peachzyapp.entities;

public class Profile {
    String id;
    String name;
    String email;
    String avatar;
    Boolean sex;
    String dateOfBirth;
    public Profile(String id, String name, String email, String avtar, Boolean sex, String dateOfBirth) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.avatar=avatar;
        this.sex=sex;
        this.dateOfBirth=dateOfBirth;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Boolean getSex() {
        return sex;
    }

    public void setSex(Boolean sex) {
        this.sex = sex;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
