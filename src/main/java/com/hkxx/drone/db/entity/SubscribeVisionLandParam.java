package com.hkxx.drone.db.entity;

public class SubscribeVisionLandParam {
    private int sysId;
    private String clientType = "";

    public int getSysId() {
        return sysId;
    }

    public void setSysId(int sysId) {
        this.sysId = sysId;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }
}
