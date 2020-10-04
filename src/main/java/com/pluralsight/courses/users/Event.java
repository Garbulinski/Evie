package com.pluralsight.courses.users;

import android.os.Bundle;

import java.util.Comparator;
import java.util.List;

public class Event {
    private String event_uid;
    private String event_name;
    private String event_description;
    private Location location;
    private String event_id;
    private List<String> users;
    private List<Image> images;
    private boolean event_public;
    private String event_image_description;

    public Event(String event_uid, String event_name, String event_description, Location location, String event_id, List<String> users, List<Image> images, boolean event_public, String event_image_description) {
        this.event_uid = event_uid;
        this.event_name = event_name;
        this.event_description = event_description;
        this.location = location;
        this.event_id = event_id;
        this.users = users;
        this.images = images;
        this.event_public = event_public;
        this.event_image_description = event_image_description;
    }

    public String getEvent_id() {
        return event_id;
    }

    public void setEvent_id(String event_id) {
        this.event_id = event_id;
    }

    public Event() {
    }

    public String getEvent_uid() {
        return event_uid;
    }

    public void setEvent_uid(String event_uid) {
        this.event_uid = event_uid;
    }

    public String getEvent_name() {
        return event_name;
    }

    public void setEvent_name(String event_name) {
        this.event_name = event_name;
    }

    public String getEvent_description() {
        return event_description;
    }

    public void setEvent_description(String event_description) {
        this.event_description = event_description;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public boolean isEvent_public() {
        return event_public;
    }

    public void setEvent_public(boolean event_public) {
        this.event_public = event_public;
    }

    public String getEvent_image_description() {
        return event_image_description;
    }

    public void setEvent_image_description(String event_image_description) {
        this.event_image_description = event_image_description;
    }
    public static Comparator<Event> AppreciationsCompare= new Comparator<Event>() {
        @Override
        public int compare(Event event, Event t1) {
            List<Image> images1 = event.getImages();
            List<Image> images2 = t1.getImages();
            Integer nrAppreciations=0;
            Integer nrAppreciations1=0;
            for (Image image : images1){
                nrAppreciations += image.getApreciations().size();
            }
            for (Image image : images2){
                nrAppreciations1 += image.getApreciations().size();
            }

            return nrAppreciations-nrAppreciations1;
        }
    };
    @Override
    public String toString() {
        return "Event{" +
                "event_uid='" + event_uid + '\'' +
                ", evenet_name='" + event_name + '\'' +
                ", event_descrioption='" + event_description + '\'' +
                ", location=" + location.toString() +
                ", users=" + users +
                ", images=" + images +
                ", event_public=" + event_public +
                ", event_image_description='" + event_image_description + '\'' +
                '}';
    }
}
