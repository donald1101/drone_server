package com.hkxx.drone.db.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 航点飞行任务对象
 */
public class PointFlyContent {
    private String entityName;
    private float height;
    private float speed;
    private List<Waypoint> flightContent = new ArrayList<>();

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
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
