package com.hkxx.drone.db.entity;

public class SubscribeAppControlParam {
    private int deviceId;
    private String clientType = "";


    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }
}
