package com.example.codetribe.movingservices;

/**
 * Created by Codetribe on 2/2/2017.
 */
public class Message {
    String location_to;
    String location_from;
    String move_date;
    String move_time;
    String desc_req;
    String status;
    String user_id;
    String post_id;



    public Message() {

    }

    public Message(String desc_req, String location_from, String location_to, String move_date, String move_time, String status, String user_id, String post_id) {
        this.desc_req = desc_req;
        this.location_from = location_from;
        this.location_to = location_to;
        this.move_date = move_date;
        this.move_time = move_time;
        this.status = status;
        this.user_id = user_id;
        this.post_id = post_id;


    }

    public String getDesc_req() {
        return desc_req;
    }

    public void setDesc_req(String desc_req) {
        this.desc_req = desc_req;
    }

    public String getLocation_from() {
        return location_from;
    }

    public void setLocation_from(String location_from) {
        this.location_from = location_from;
    }

    public String getLocation_to() {
        return location_to;
    }

    public void setLocation_to(String location_to) {
        this.location_to = location_to;
    }

    public String getMove_date() {
        return move_date;
    }

    public void setMove_date(String move_date) {
        this.move_date = move_date;
    }

    public String getMove_time() {
        return move_time;
    }

    public void setMove_time(String move_time) {
        this.move_time = move_time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

}