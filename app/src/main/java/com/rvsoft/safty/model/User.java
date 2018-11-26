package com.rvsoft.safty.model;

import java.io.Serializable;

/**
 * Created by Ravi on 11/23/2018.
 * Algante
 * ravikant.vishwakarma@algante.com
 */
public class User implements Serializable {
    private String userID;
    private String userName;
    private String userMobile;
    private String fcm;
    private String role = "C";

    public User() {

    }

    public User(String fcm, String userID, String userMobile) {
        this.fcm = fcm;
        this.userID = userID;
        this.userMobile = userMobile;
    }

    public String getFcm() {
        return fcm;
    }

    public void setFcm(String fcm) {
        this.fcm = fcm;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserMobile() {
        return userMobile;
    }

    public void setUserMobile(String userMobile) {
        this.userMobile = userMobile;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
