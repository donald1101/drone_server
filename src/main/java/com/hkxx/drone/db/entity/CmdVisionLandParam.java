package com.hkxx.drone.db.entity;

public class CmdVisionLandParam {
    private int sysId;
    private String videoUrl = "";

    public int getSysId() {
        return sysId;
    }

    public void setSysId(int sysId) {
        this.sysId = sysId;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
