package com.hkxx.drone.db.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 航点飞行任务对象
 */
public class PointFlyContent {
    private int deviceId;
    private int deviceType;
    private int sysId;
    private float height;
    private float speed;
    private List<Waypoint> flightContent = new ArrayList<>();

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public int getSysId() {
        return sysId;
    }

    public void setSysId(int sysId) {
        this.sysId = sysId;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public List<Waypoint> getFlightContent() {
        return flightContent;
    }

    public void setFlightContent(List<Waypoint> flightContent) {
        this.flightContent = flightContent;
    }
}
