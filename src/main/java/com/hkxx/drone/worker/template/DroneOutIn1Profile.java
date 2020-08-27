package com.hkxx.drone.worker.template;

@Template(name = "drone_out_in_1.lrp")
public class DroneOutIn1Profile extends LinkhubProfile {

    @PlaceHolder(value = "__DEVICE_ID__")
    public String deviceId;
    @PlaceHolder(value = "__DEVICE_NAME__")
    public String deviceName;
    @PlaceHolder(value = "__DEVICE_HOST__")
    public String deviceHost;
    @PlaceHolder(value = "__DEVICE_PORT__")
    public String devicePort;
    @PlaceHolder(value = "__CLIENT_ID__")
    public String clientId;
    @PlaceHolder(value = "__CLIENT_NAME__")
    public String clientName;
    @PlaceHolder(value = "__CLIENT_PORT__")
    public String clientPort;
    @PlaceHolder(value = "__MAVLOG_PATH__")
    public String mavlogPath;
    @PlaceHolder(value = "__MAVLOG_PREFIX__")
    public String mavlogPrefix;

    public DroneOutIn1Profile(String name, String desc, String updateDate, String deviceId, String deviceName,
                              String deviceHost, String devicePort, String clientId, String clientName, String clientPort,
                              String mavlogPath, String mavlogPrefix) {
        super(name, desc, updateDate);
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.deviceHost = deviceHost;
        this.devicePort = devicePort;
        this.clientId = clientId;
        this.clientName = clientName;
        this.clientPort = clientPort;
        this.mavlogPath = mavlogPath;
        this.mavlogPrefix = mavlogPrefix;
    }

}
