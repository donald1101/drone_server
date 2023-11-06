package com.hkxx.drone.db.entity;

import java.util.List;

public class DetectedInfoParam {
    List<SignLayerInfo> targets;

    public List<SignLayerInfo> getTargets() {
        return targets;
    }

    public void setTargets(List<SignLayerInfo> targets) {
        this.targets = targets;
    }
}
