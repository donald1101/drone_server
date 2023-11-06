package com.hkxx.drone.db.entity;

import java.util.ArrayList;
import java.util.List;

public class CreateObliqueContent {
    float height;
    List<Point> vertexList = new ArrayList<>();

    public CreateObliqueContent() {
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public List<Point> getVertexList() {
        return vertexList;
    }

    public void setVertexList(List<Point> vertexList) {
        this.vertexList = vertexList;
    }
}
