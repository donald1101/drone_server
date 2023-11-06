package com.hkxx.drone.db.entity;

import java.util.List;

public class AisStatusParam {
    List<AisInfoEntity> aisInfo;

    public List<AisInfoEntity> getAisInfo() {
        return aisInfo;
    }

    public void setAisInfo(List<AisInfoEntity> aisInfo) {
        this.aisInfo = aisInfo;
    }
}
