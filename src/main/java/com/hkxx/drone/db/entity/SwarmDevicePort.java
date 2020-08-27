package com.hkxx.drone.db.entity;

public class SwarmDevicePort {

    private Integer deviceId;
    private Integer clientPort;

    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getClientPort() {
        return clientPort;
    }

    public void setClientPort(Integer clientPort) {
        this.clientPort = clientPort;
    }
}
