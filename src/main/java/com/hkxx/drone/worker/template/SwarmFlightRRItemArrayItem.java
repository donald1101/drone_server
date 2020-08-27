package com.hkxx.drone.worker.template;

@Template(name = "swarm_flight_rr_item_arrayitem.lrp")
public class SwarmFlightRRItemArrayItem extends BaseTemplate {

    @PlaceHolder(value = "__CLIENT_ID__")
    public String clientId;

    public SwarmFlightRRItemArrayItem(String clientId) {
        this.clientId = clientId;
    }
}
