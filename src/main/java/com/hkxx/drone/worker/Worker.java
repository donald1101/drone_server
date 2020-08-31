package com.hkxx.drone.worker;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Run async tasks.
 */
public class Worker {

    private static final Logger log = LoggerFactory.getLogger(Worker.class);

    public static String makeDroneMavlogPrefix(int droneId) {
        return String.format("drone-%d_", droneId);
    }

    public static String makeDroneProfileName(int droneId) {
        return makeProfileName("drone", droneId);
    }

    public static String makeBoatProfileName(int deviceId) {
        return makeProfileName("boat", deviceId);
    }

    public static String makeJoystickProfileName(int jsId) {
        return makeProfileName("js", jsId);
    }

    public static String makeReplayProfileName(int replayId) {
        return makeProfileName("replay", replayId);
    }

    public static String makeSwarmFlightProfileName(int swarmId) {
        return makeProfileName("swarm", swarmId);
    }

    public static String makeProfileName(String cate, int id) {
        return String.format("%s-%d", cate, id);
    }

    public static String makeReplayLogProviderName(int replayId, int droneId) {
        return String.format("replay-%d-%d", replayId, droneId);
    }

}
