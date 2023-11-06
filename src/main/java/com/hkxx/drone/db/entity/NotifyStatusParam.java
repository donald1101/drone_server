package com.hkxx.drone.db.entity;

public class NotifyStatusParam {
    private String name;
    private int deviceType;
    private double lat;
    private double lng;
    private float alt;
    private float hSpeed;
    private float vSpeed;
    private float yaw;
    private float roll;
    private float pitch;
    private float battery;
    private int state;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
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

    public float gethSpeed() {
        return hSpeed;
    }

    public void sethSpeed(float hSpeed) {
        this.hSpeed = hSpeed;
    }

    public float getvSpeed() {
        return vSpeed;
    }

    public void setvSpeed(float vSpeed) {
        this.vSpeed = vSpeed;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getRoll() {
        return roll;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getBattery() {
        return battery;
    }

    public void setBattery(float battery) {
        this.battery = battery;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
