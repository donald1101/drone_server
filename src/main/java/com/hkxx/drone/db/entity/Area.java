package com.hkxx.drone.db.entity;

import java.util.ArrayList;
import java.util.List;

public class Area {
    float paintingWidth;
    float paintingHeight;
    float focalDistance;
    float courseOverlapRate;
    float sidelapRate;
    int pathDirection = 1;
    boolean takeOrthoPhoto = false;
    boolean takeObliquePhoto = false;
    float height;
    float speed;
    List<Point> vertexList = new ArrayList<>();

    public Area() {
    }

    public float getPaintingWidth() {
        return paintingWidth;
    }

    public void setPaintingWidth(float paintingWidth) {
        this.paintingWidth = paintingWidth;
    }

    public float getPaintingHeight() {
        return paintingHeight;
    }

    public void setPaintingHeight(float paintingHeight) {
        this.paintingHeight = paintingHeight;
    }

    public float getFocalDistance() {
        return focalDistance;
    }

    public void setFocalDistance(float focalDistance) {
        this.focalDistance = focalDistance;
    }

    public float getCourseOverlapRate() {
        return courseOverlapRate;
    }

    public void setCourseOverlapRate(float courseOverlapRate) {
        this.courseOverlapRate = courseOverlapRate;
    }

    public float getSidelapRate() {
        return sidelapRate;
    }

    public void setSidelapRate(float sidelapRate) {
        this.sidelapRate = sidelapRate;
    }

    public int getPathDirection() {
        return pathDirection;
    }

    public void setPathDirection(int pathDirection) {
        this.pathDirection = pathDirection;
    }

    public boolean isTakeOrthoPhoto() {
        return takeOrthoPhoto;
    }

    public void setTakeOrthoPhoto(boolean takeOrthoPhoto) {
        this.takeOrthoPhoto = takeOrthoPhoto;
    }

    public boolean isTakeObliquePhoto() {
        return takeObliquePhoto;
    }

    public void setTakeObliquePhoto(boolean takeObliquePhoto) {
        this.takeObliquePhoto = takeObliquePhoto;
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

    public List<Point> getVertexList() {
        return vertexList;
    }

    public void setVertexList(List<Point> vertexList) {
        this.vertexList = vertexList;
    }
}
