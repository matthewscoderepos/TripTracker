package com.matthewericpeter.triptracker.Model;

public class Coord {
    private double lat,lon;

    public Coord(){}

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    @Override
    public String toString(){
        return new StringBuilder("[").append(this.lat).append(",").append(this.lon).append(']').toString();
    }
}
