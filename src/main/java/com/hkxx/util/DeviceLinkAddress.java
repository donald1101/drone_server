package com.hkxx.util;

/*
 * Representation & Util of Device's linkAddress.
 * A DeviceLinkAddress consists of three parts
 * (i.e. mav://127.0.0.1:5800):
 * 1. protocol header (mav://)
 * 2. host address (127.0.0.1)
 * 3. port (5800)
 */
public class DeviceLinkAddress {

    public static final String PROTOCOL_RTCP = "rtcp://";
    public static final String PROTOCOL_MAVLINK = "mav://";
    public static final String HOST_LOCALHOST_IP = "127.0.0.1";
    public static final String HOST_LOCALHOST_STR = "localhost";
    public static final Integer PORT_TOGEN = 0;

    public String protocol;
    public String host;
    public Integer port;

    /*
     * Parse from its String format.
     * return null - if format invalid
     */
    public static DeviceLinkAddress parse(String str) {
        if (str == null || str.isEmpty()) {
            return getDefault();
        }
        DeviceLinkAddress ret = new DeviceLinkAddress();

        if (str.contains("://")) {
            if (str.startsWith(PROTOCOL_RTCP)) {
                ret.protocol = PROTOCOL_RTCP;
                str = str.substring(PROTOCOL_RTCP.length());
            } else if (str.startsWith(PROTOCOL_MAVLINK)) {
                ret.protocol = PROTOCOL_MAVLINK;
                str = str.substring(PROTOCOL_MAVLINK.length());
            } else {
                return null; // unknown protocol
            }
        } else {
            ret.protocol = PROTOCOL_RTCP;
        }

        if (str.contains(":")) {
            String[] list = str.split(":");
            if (list != null && list.length > 1) {
                ret.host = list[0];
                if (ret.host.equals(HOST_LOCALHOST_STR)) {
                    ret.host = HOST_LOCALHOST_IP;
                }
                try {
                    ret.port = Integer.valueOf(list[1]);
                    return ret;
                } catch (RuntimeException e) {
                    return null;
                }
            }
        }

        ret.host = HOST_LOCALHOST_IP;
        ret.port = PORT_TOGEN;
        return ret;
    }

    /*
     * return the default LinkAddress.
     */
    public static DeviceLinkAddress getDefault() {
        DeviceLinkAddress ret = new DeviceLinkAddress();
        ret.protocol = PROTOCOL_RTCP;
        ret.host = HOST_LOCALHOST_IP;
        ret.port = PORT_TOGEN;
        return ret;
    }

    /*
     * return the default LinkAddress with Mavlink protocol.
     */
    public static DeviceLinkAddress getDefaultMavlink() {
        DeviceLinkAddress ret = new DeviceLinkAddress();
        ret.protocol = PROTOCOL_MAVLINK;
        ret.host = HOST_LOCALHOST_IP;
        ret.port = PORT_TOGEN;
        return ret;
    }

    /*
     * True - this is a fake address if port is set to 0,
     * it should be generated before use.
     * False - this is a true address.
     */
    public boolean isToGenDeviceAddress() {
        if (port == null) {
            return true;
        }
        return port.intValue() == PORT_TOGEN.intValue();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     * return the String format.
     */
    public String toString() {
        StringBuilder sBuilder = new StringBuilder();
        if (protocol != null) {
            sBuilder.append(protocol);
        }
        if (host != null) {
            sBuilder.append(host);
        }
        if (port != null) {
            sBuilder.append(":");
            sBuilder.append(port.toString());
        }
        return sBuilder.toString();
    }
}
