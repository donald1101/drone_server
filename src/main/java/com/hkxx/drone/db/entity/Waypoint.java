package com.hkxx.drone.db.entity;

import java.util.ArrayList;
import java.util.List;

public class Waypoint {

    private double lat;
    private double lng;
    private float alt;
    private List<WaypointAction> actions = new ArrayList<>();

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

    public float getAlt() {
        return alt;
    }

    public void setAlt(float alt) {
        this.alt = alt;
    }

    public List<WaypointAction> getActions() {
        return actions;
    }

    public void setActions(List<WaypointAction> actions) {
        this.actions = actions;
    }
}
