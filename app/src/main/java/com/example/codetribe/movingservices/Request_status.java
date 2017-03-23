package com.example.codetribe.movingservices;

/**
 * Created by Codetribe on 3/8/2017.
 */
public class Request_status {


    String user_id;
    String post_id;
    String owner_id;
    String request_date;
    public Request_status() {

    }
    public Request_status(String owner_id, String post_id, String user_id) {
        this.owner_id = owner_id;
        this.post_id = post_id;
        this.user_id = user_id;
    }

    public String getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(String owner_id) {
        this.owner_id = owner_id;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getRequest_date() {
        return request_date;
    }

    public void setRequest_date(String request_date) {
        this.request_date = request_date;
    }
}
