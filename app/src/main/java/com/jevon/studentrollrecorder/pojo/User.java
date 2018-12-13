package com.jevon.studentrollrecorder.pojo;

/**
 * Created by Shiva on 4/3/2016.
 */
public class User {
    private String name;
    private String profileImageURL;

    public User(String name, String profileImageURL){
        this.name=name;
        this.profileImageURL=profileImageURL;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImageURL() {
        return profileImageURL;
    }

    public void setProfileImageURL(String profileImageURL) {
        this.profileImageURL = profileImageURL;
    }
}
