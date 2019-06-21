package com.matthewericpeter.triptracker;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Trip implements Serializable {
    public Trip(){
        name = "";
        route = new ArrayList<>();
    }
    String name;
    List<LatLng> route;
    LatLng start;
    LatLng end;
    long timeElapsed;
}
