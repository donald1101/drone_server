package com.hkxx.drone.worker.template;

@Template(name = "swarm_flight_rli_item.lrp")
public class SwarmFlightRLIItem extends BaseTemplate {

    @PlaceHolder(value = "__DEVICE_ID__")
    public String deviceId;

    @PlaceHolder(value = "__DEVICE_NAME__")
    public String deviceName;

    @PlaceHolder(value = "__DEVICE_HOST__")
    public String deviceHost;

    @PlaceHolder(value = "__DEVICE_PORT__")
    public String devicePort;

    public SwarmFlightRLIItem(String deviceId, String deviceName, String deviceHost, String devicePort) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.deviceHost = deviceHost;
        this.devicePort = devicePort;
    }

}
