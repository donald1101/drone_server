package com.hkxx.drone.db.entity;

import java.util.ArrayList;
import java.util.List;

public class Waypoint {

    private float alt;
    private double lat;
    private double lng;

    public float getAlt() {
        return alt;
    }

    public void setAlt(float alt) {
        this.alt = alt;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
