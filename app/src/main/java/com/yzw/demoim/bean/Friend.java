package com.yzw.demoim.bean;

import java.io.Serializable;

/**
 * Created by yzw on 2016/3/17 0017.
 */
public class Friend implements Serializable {

    private String username;
    private String name;
    private String mode;
    private String groupname;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getGroupname() {
        return groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    @Override
    public String toString() {
        return "Friend{" +
                "username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", mode='" + mode + '\'' +
                ", groupname='" + groupname + '\'' +
                '}';
    }
}
