package com.pluralsight.courses.users;

import java.util.List;

public class ScheduledEvent {
    private String scheduled_event_id;
    private String image_description_uri;
    private String name;
    private String details;
    private String address;
    private String date;
    private List<String> intersted_users;

    public ScheduledEvent() {
    }

    public ScheduledEvent(String scheduled_event_id
            , String image_description_uri
            , String name
            , String details
            , String address
            , String date
            , List<String> intersted_users) {
        this.scheduled_event_id = scheduled_event_id;
        this.image_description_uri = image_description_uri;
        this.name = name;
        this.details = details;
        this.address = address;
        this.date = date;
        this.intersted_users = intersted_users;
    }

    public List<String> getIntersted_users() {
        return intersted_users;
    }

    public void setIntersted_users(List<String> intersted_users) {
        this.intersted_users = intersted_users;
    }

    public String getScheduled_event_id() {
        return scheduled_event_id;
    }

    public void setScheduled_event_id(String scheduled_event_id) {
        this.scheduled_event_id = scheduled_event_id;
    }

    public String getImage_description_uri() {
        return image_description_uri;
    }

    public void setImage_description_uri(String image_description_uri) {
        this.image_description_uri = image_description_uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
