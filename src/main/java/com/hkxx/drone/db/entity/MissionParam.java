package com.hkxx.drone.db.entity;

import java.util.List;

public class MissionParam {
    private int taskId;
    private List<DeviceInfo> device;

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public List<DeviceInfo> getDevice() {
        return device;
    }

    public void setDevice(List<DeviceInfo> device) {
        this.device = device;
    }
}
