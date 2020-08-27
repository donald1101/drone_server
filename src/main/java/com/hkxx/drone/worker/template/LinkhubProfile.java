package com.hkxx.drone.worker.template;

@Template(name = "default")
public class LinkhubProfile extends BaseTemplate {

    @PlaceHolder(value = "__ROUTER_NAME__")
    public String name;
    @PlaceHolder(value = "__ROUTER_DESC__")
    public String desc;
    @PlaceHolder(value = "__ROUTER_UPDATE_DATE__")
    public String updateDate;

    public LinkhubProfile(String name, String desc, String updateDate) {
        this.name = name;
        this.desc = desc;
        this.updateDate = updateDate;
    }
}
