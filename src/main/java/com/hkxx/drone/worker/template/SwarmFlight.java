package com.hkxx.drone.worker.template;

import java.util.List;

@Template(name = "swarm_flight.lrp")
public class SwarmFlight extends LinkhubProfile {

    @PlaceHolder(value = "__RLI_ARRAY__")
    public List<SwarmFlightRLIItem> flightRLIItems;
    @PlaceHolder(value = "__RR_ARRAY__")
    public List<SwarmFlightRRItem> flightRRItems;

    public SwarmFlight(String name, String desc, String updateDate, List<SwarmFlightRLIItem> flightRliItems,
                       List<SwarmFlightRRItem> flightRRItems) {
        super(name, desc, updateDate);
        this.flightRLIItems = flightRliItems;
        this.flightRRItems = flightRRItems;
    }

}
