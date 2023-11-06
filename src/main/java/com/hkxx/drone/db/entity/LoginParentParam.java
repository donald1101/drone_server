package com.hkxx.drone.db.entity;

public class LoginParentParam {
    private int userId;
    private String name;
    private String pwd;
    private String clientType = "";
    private String parentIp = "127.0.0.1";
    private int parentPort = 60000;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public String getParentIp() {
        return parentIp;
    }

    public void setParentIp(String parentIp) {
        this.parentIp = parentIp;
    }

    public int getParentPort() {
        return parentPort;
    }

    public void setParentPort(int parentPort) {
        this.parentPort = parentPort;
    }
}
