package com.appdev.eudemonia;

public class Friend {
    private String name;
    private int profileImageResId; 

    public Friend(String name, int profileImageResId) {
        this.name = name;
        this.profileImageResId = profileImageResId;
    }

    public String getName() {
        return name;
    }

    public int getProfileImageResId() {
        return profileImageResId;
    }
}
