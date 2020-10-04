package com.pluralsight.courses.users;

public class User {
    private String name;
    private String phone;
    private String profile_picture;
    private String user_id;

    public User(String name, String phone, String profile_picture, String user_id) {
        this.name = name;
        this.phone = phone;
        this.profile_picture = profile_picture;
        this.user_id = user_id;
    }
    public User() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfile_picture() {
        return profile_picture;
    }

    public void setProfile_picture(String profile_picture) {
        this.profile_picture = profile_picture;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", profile_picture='" + profile_picture + '\'' +
                ", user_id='" + user_id + '\'' +
                '}';
    }
}
