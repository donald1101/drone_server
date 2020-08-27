package com.hkxx.drone.worker.template;

import java.util.List;

@Template(name = "swarm_flight_rr_item.lrp")
public class SwarmFlightRRItem extends BaseTemplate {

    @PlaceHolder(value = "__DEVICE_ID__")
    public String deviceId;

    @PlaceHolder(value = "__ARRAY__")
    public List<SwarmFlightRRItemArrayItem> array;

    public SwarmFlightRRItem(String deviceId, List<SwarmFlightRRItemArrayItem> array) {
        this.deviceId = deviceId;
        this.array = array;
    }
}
