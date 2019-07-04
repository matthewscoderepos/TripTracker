package com.matthewericpeter.triptracker;

import android.location.Location;

public class WaypointAndDistance {
    public Waypoint waypoint;
    public float distance;
    WaypointAndDistance(Waypoint w, double _lat, double _lon){
        float[] results = new float[1];
        Location.distanceBetween(w.latitude, w.longitude, _lat, _lon, results);
        waypoint = w;
        distance = results[0];
    }
    WaypointAndDistance(Waypoint w, float dist){
        waypoint = w;
        distance = dist;
    }
}
