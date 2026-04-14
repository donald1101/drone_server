package com.hkxx.drone.db.entity;

public class CommandTakeOff {
    private String entityName;
    private String message;

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
