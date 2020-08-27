package com.hkxx.drone.db.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 集群飞行任务对象
 */
public class ClusterFlyContent {
    private int swarmId;
    private List<DeviceInfo> device = new ArrayList<>();
    private float height;
    private float speed;
    private int collaboration;
    private List<Waypoint> flightContent = new ArrayList<>();

    public int getSwarmId() {
        return swarmId;
    }

    public void setSwarmId(int swarmId) {
        this.swarmId = swarmId;
    }

    public List<DeviceInfo> getDevice() {
        return device;
    }

    public void setDevice(List<DeviceInfo> device) {
        this.device = device;
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

    public int getCollaboration() {
        return collaboration;
    }

    public void setCollaboration(int collaboration) {
        this.collaboration = collaboration;
    }

    public List<Waypoint> getFlightContent() {
        return flightContent;
    }

    public void setFlightContent(List<Waypoint> flightContent) {
        this.flightContent = flightContent;
    }
}
