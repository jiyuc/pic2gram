package com.example.kulde.instagram.Model;

public class Notice {
    private String user_id_from;
    private String user_id_to;
    private String date_created;
    private String action;
    public Notice(){

    }
    public Notice(String user_id_from, String user_id_to, String date_created, String action) {
        this.user_id_from = user_id_from;
        this.user_id_to = user_id_to;
        this.date_created = date_created;
        this.action = action;
    }

    @Override
    public String toString() {
        return "Notice{" +
                "notice='" +"user_id_from='"+ user_id_from + '\'' +
                ", user_id_to='" + user_id_to + '\'' +
                ", action='" + action + '\'' +
                ", date_created='" + date_created + '\'' +
                '}';
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public String getUser_id_to() {
        return user_id_to;
    }

    public void setUser_id_to(String user_id_to) {
        this.user_id_to = user_id_to;
    }

    public String getUser_id_from() {
        return user_id_from;
    }

    public void setUser_id_from(String user_id_from) {
        this.user_id_from = user_id_from;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }
}
