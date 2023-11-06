package com.hkxx.drone.db.entity;

public class PathElement {

    String type = PathElementType.POINT; //point或者area
    Waypoint point;
    Area area;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Waypoint getPoint() {
        return point;
    }

    public void setPoint(Waypoint point) {
        this.point = point;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }
}
