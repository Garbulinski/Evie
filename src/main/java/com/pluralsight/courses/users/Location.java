package com.pluralsight.courses.users;

public class Location{
    private double latitude;
    private double Longitude;

    public Location(float latitude, float longitude) {
        this.latitude = latitude;
        Longitude = longitude;
    }

    public Location() {
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double longitude) {
        Longitude = longitude;
    }

    @Override
    public String toString() {
        return "Location{" +
                "Latitude=" + latitude +
                ", Longitude=" + Longitude +
                '}';
    }
}
