package com.pluralsight.courses.users;

public class Comment {
    private String commnent;
    private String comment_id;
    private String user_id;

    public Comment(String commnent, String commnet_id, String user_id) {
        this.commnent = commnent;
        this.comment_id = commnet_id;
        this.user_id = user_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Comment() {
    }

    public String getCommnent() {
        return commnent;
    }

    public void setCommnent(String commnent) {
        this.commnent = commnent;
    }

    public String getComment_id() {
        return comment_id;
    }

    public void setComment_id(String comment_id) {
        this.comment_id = comment_id;
    }

    @Override
    public String toString() {
        return "Commnent{" +
                "commnent='" + commnent + '\'' +
                ", commnet_id='" + comment_id + '\'' +
                '}';
    }
}
