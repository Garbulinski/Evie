package com.pluralsight.courses.users;

import java.util.List;

public class Image {
    private String image_uri;
    private String image_id;
    private String image_uid;
    private List<String> apreciations;
    private String image_eventid;
    private List<Comment> comments;

    public String getImage_eventid() {
        return image_eventid;
    }

    public void setImage_eventid(String image_eventid) {
        this.image_eventid = image_eventid;
    }

    public Image() {
    }

    public String getImage_uid() {
        return image_uid;
    }

    public void setImage_uid(String image_uid) {
        this.image_uid = image_uid;
    }

    public String getImage_uri() {
        return image_uri;
    }

    public void setImage_uri(String image_uri) {
        this.image_uri = image_uri;
    }

    public Image(String image_uri, String image_id, String image_uid, List<String> apreciations, String image_eventid, List<Comment> comments) {
        this.image_uri = image_uri;
        this.image_id = image_id;
        this.image_uid = image_uid;
        this.apreciations = apreciations;
        this.image_eventid = image_eventid;
        this.comments = comments;
    }

    public String getImage_id() {
        return image_id;
    }

    public void setImage_id(String image_id) {
        this.image_id = image_id;
    }

    public List<String> getApreciations() {
        return apreciations;
    }

    public void setApreciations(List<String> apreciations) {
        this.apreciations = apreciations;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    @Override
    public String toString() {
        return "Image{" +
                "imageURI='" + image_uri + '\n' +
                ", imageId='" + image_id + '\n' +
                ", apreciations='" + apreciations + '\n' +
                ", imageEventId='" + image_eventid + '\n' +
                ", comments=" + comments +
                '}';
    }
}
