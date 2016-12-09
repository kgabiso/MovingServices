package com.example.codetribe.movingservices;

/**
 * Created by Codetribe on 11/15/2016.
 */
public class MovingServices {


    String title;
    String description;
    String contactNumber;
    String emailAddress;
    String location;
    String image;
    String postdate;
    String username;

    public MovingServices() {

    }

    public MovingServices(String title, String description, String contactNumber, String email, String location, String image, String username) {
        this.title = title;
        this.description = description;
        this.contactNumber = contactNumber;
        this.emailAddress = email;
        this.location = location;
        this.image = image;
        this.username = username;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return emailAddress;
    }

    public void setEmail(String email) {
        this.emailAddress = email;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDate() {

        return postdate;
    }

    public void setDate(String date) {
        this.postdate = date;
    }
}
