package com.hkxx.test;

import com.google.gson.Gson;
import com.hkxx.drone.CommandType;
import com.hkxx.drone.TaskType;
import com.hkxx.drone.db.entity.*;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TestCommand {

    @Test
    public void testGenUploadTask() {
        Command command = new Command();
        command.setCmd(CommandType.UPLOAD_TASK);
        TaskInfo taskInfo = new TaskInfo();

        PointFlyContent pointFlyContent = new PointFlyContent();
        pointFlyContent.setDeviceId(5);
        pointFlyContent.setHeight(30);
        pointFlyContent.setSpeed(1);
        pointFlyContent.setSysId(1);
        List<Waypoint> waypoints = new ArrayList<>();
        Waypoint pt1 = new Waypoint();
        pt1.setLat(30.510058);
        pt1.setLng(114.3880229);
        pt1.setAlt(30);
        Waypoint pt2 = new Waypoint();
        pt2.setLat(30.510068);
        pt2.setLng(114.3880239);
        pt2.setAlt(30);
        Waypoint pt3 = new Waypoint();
        pt3.setLat(30.51078);
        pt3.setLng(114.3880279);
        pt3.setAlt(30);
        waypoints.add(pt1);
        waypoints.add(pt2);
        waypoints.add(pt3);
        pointFlyContent.setFlightContent(waypoints);
        taskInfo.setTaskType(TaskType.POINT_FLY);
        taskInfo.setContent(pointFlyContent);
        command.setParam(taskInfo);
        Gson gson = new Gson();
        String str = gson.toJson(command);
        System.out.println(str);
    }

    @Test
    public void testGenStartMission() {
        List<Integer> testIntList = new ArrayList<>();
        testIntList.add(100);
        testIntList.add(200);
        testIntList.add(300);
        System.out.println(testIntList.toString());

        Command command = new Command();
        command.setCmd(CommandType.START_MISSION);
        MissionParam missionParam = new MissionParam();
        missionParam.setTaskId(236);
        List<DeviceInfo> deviceInfoList = new ArrayList<>();
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setDeviceId(5);
        deviceInfo.setDeviceType(0);
        deviceInfo.setSysId(2);
        deviceInfoList.add(deviceInfo);
        missionParam.setDevice(deviceInfoList);
        command.setParam(missionParam);
        Gson gson = new Gson();
        String str = gson.toJson(command);
        System.out.println(str);
    }

    @Test
    public void testGetString() {
        byte[] buf = {0x57, 0x50, 0x5f, 0x53, 0x50, 0x45, 0x45, 0x44};
        String str = new String(buf, StandardCharsets.UTF_8);
        System.out.println(str);
    }
}
