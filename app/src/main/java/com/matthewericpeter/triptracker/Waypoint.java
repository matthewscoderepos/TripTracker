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
}
