package com.jxl.studybuddy;

/**
 * Created by Xavier on 23/10/2017.
 */

public class Users {
    public String name;
    public String thumb_image;
    public String courses;

    public Users(){

    }

    public Users(String name, String thumb_image, String courses) {
        this.name = name;
        this.thumb_image = thumb_image;
        this.courses = courses;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public String getCourses() {
        return courses;
    }

    public void setCourses(String courses) {
        this.courses = courses;
    }
}
