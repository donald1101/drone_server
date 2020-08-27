package com.hkxx.drone.worker.template;

@Template(name = "drone_out_in_2.lrp")
public class DroneOutIn2Profile extends DroneOutIn1Profile {

    public DroneOutIn2Profile(String name, String desc, String updateDate, String deviceId, String deviceName,
                              String deviceHost, String devicePort, String clientId, String clientName, String clientPort,
                              String mavlogPath, String mavlogPrefix) {
        super(name, desc, updateDate, deviceId, deviceName, deviceHost, devicePort, clientId, clientName, clientPort,
                mavlogPath, mavlogPrefix);
    }
}
