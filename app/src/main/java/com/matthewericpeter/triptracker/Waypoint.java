package com.matthewericpeter.triptracker;

public class Waypoint {
    public String name;
    public double latitude;
    public double longitude;
    Waypoint(String _name, double _lat, double _lon){
        name = _name;
        latitude = _lat;
        longitude = _lon;
    }
    //empty constructor for Waypoint, used by database to create objects, studio says never used but it is
    Waypoint () {}

    //getter & setter methods for data handling
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
