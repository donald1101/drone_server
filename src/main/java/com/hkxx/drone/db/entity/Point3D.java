package com.hkxx.drone.db.entity;

public class Point3D {
    private double lat;
    private double lng;
    private float alt;

    public Point3D() {
        this.lat = 0;
        this.lng = 0;
        this.alt = 0;
    }

    public Point3D(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
        this.alt = 0;
    }

    public Point3D(double lat, double lng, float alt) {
        this.lat = lat;
        this.lng = lng;
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

    public float getAlt() {
        return alt;
    }

    public void setAlt(float alt) {
        this.alt = alt;
    }
}
