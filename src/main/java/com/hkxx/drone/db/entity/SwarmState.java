package com.hkxx.drone.db.entity;

import java.util.ArrayList;
import java.util.List;

public class SwarmState {
    private int swarmId;
    private List<DeviceState> deviceStateList = new ArrayList<>();

    public int getSwarmId() {
        return swarmId;
    }

    public void setSwarmId(int swarmId) {
        this.swarmId = swarmId;
    }

    public List<DeviceState> getDeviceStateList() {
        return deviceStateList;
    }

    public void setDeviceStateList(List<DeviceState> deviceStateList) {
        this.deviceStateList = deviceStateList;
    }
}
