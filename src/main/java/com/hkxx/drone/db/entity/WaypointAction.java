package com.hkxx.drone.db.entity;

public class WaypointAction {
    private int actionType = 0;
    private int actionParam = 0;

    public WaypointAction() {

    }

    public WaypointAction(int actionType, int actionParam) {
        this.actionType = actionType;
        this.actionParam = actionParam;
    }

    public int getActionType() {
        return actionType;
    }

    public void setActionType(int actionType) {
        this.actionType = actionType;
    }

    public int getActionParam() {
        return actionParam;
    }

    public void setActionParam(int actionParam) {
        this.actionParam = actionParam;
    }
}
