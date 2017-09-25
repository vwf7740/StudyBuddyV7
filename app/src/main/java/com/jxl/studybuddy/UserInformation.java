package com.jxl.studybuddy;

import java.util.HashSet;

/**
 * Created by Xavier on 25/09/2017.
 */

public class UserInformation {
    private String name;
    private String email;
    private HashSet<String> courses;

    public UserInformation(){

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

    public HashSet<String> getCourses() {
        return courses;
    }

    public void setCourses(HashSet<String> courses) {
        this.courses = courses;
    }
}
