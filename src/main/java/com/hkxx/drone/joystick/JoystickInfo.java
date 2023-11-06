package com.hkxx.drone.joystick;

public class JoystickInfo {
    private String joystickName = "";
    private String hidReportDescriptor = "";
    private int reportId = 0;
    private int vendorId = 0;
    private int productId = 0;
    private String serialNumber = "";
    private String commandConfig = ""; //存储摇杆按键命令映射配置，json字符串格式
    private Object commandConfigObj = null; //摇杆配置对应的对象

    public JoystickInfo() {
    }

    public String getJoystickName() {
        return joystickName;
    }

    public void setJoystickName(String joystickName) {
        this.joystickName = joystickName;
    }

    public String getHidReportDescriptor() {
        return hidReportDescriptor;
    }

    public void setHidReportDescriptor(String hidReportDescriptor) {
        this.hidReportDescriptor = hidReportDescriptor;
    }

    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public int getVendorId() {
        return vendorId;
    }

    public void setVendorId(int vendorId) {
        this.vendorId = vendorId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getCommandConfig() {
        return commandConfig;
    }

    public void setCommandConfig(String commandConfig) {
        this.commandConfig = commandConfig;
    }

    public Object getCommandConfigObj() {
        return commandConfigObj;
    }

    public void setCommandConfigObj(Object commandConfigObj) {
        this.commandConfigObj = commandConfigObj;
    }
}
