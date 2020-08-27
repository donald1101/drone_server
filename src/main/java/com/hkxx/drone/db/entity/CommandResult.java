package com.hkxx.drone.db.entity;

public class CommandResult {
    private String result;
    private int code = 200; //成功返回200，失败返回500

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
