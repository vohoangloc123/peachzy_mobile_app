package com.example.peachzyapp.entities;

import java.io.Serializable;

public class ChatBox implements Serializable {
    public String name;

    public ChatBox(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
