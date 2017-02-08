package com.example.codetribe.movingservices;

/**
 * Created by Codetribe on 2/2/2017.
 */
public class Message {
    private String id;
    private String text;
    private String name;
    private String photoUrl;

    public Message() {

    }

    public Message(String id, String name, String photoUrl, String text) {
        this.id = id;
        this.name = name;
        this.photoUrl = photoUrl;
        this.text = text;
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

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
