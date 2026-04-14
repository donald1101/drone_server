//package com.hkxx.test;
//
//import com.google.gson.Gson;
//import com.hkxx.drone.common.Convert;
//import com.hkxx.drone.common.MultiCastUdpServer;
//import com.hkxx.drone.common.UdpClient;
//import com.hkxx.drone.common.UdpServer;
//import com.hkxx.drone.*;
//import com.hkxx.drone.db.entity.*;
//import org.apache.mina.core.buffer.IoBuffer;
//import org.apache.mina.filter.codec.ProtocolCodecFilter;
//import org.junit.Test;
//
//import java.io.InputStream;
//import java.nio.charset.StandardCharsets;
//import java.util.*;
//import java.util.concurrent.atomic.AtomicBoolean;
//
//public class TestCommand {
//
//
//    @Test
//    public void testGenUploadTask() {
//        Command command = new Command();
//        command.setCmd(CommandType.UPLOAD_TASK);
//        TaskInfo taskInfo = new TaskInfo();
//
//        PointFlyContent pointFlyContent = new PointFlyContent();
//        pointFlyContent.setDeviceId(5);
//        pointFlyContent.setHeight(30);
//        pointFlyContent.setSpeed(1);
//        pointFlyContent.setSysId(1);
//        List<Waypoint> waypoints = new ArrayList<>();
//        Waypoint pt1 = new Waypoint();
//        pt1.setLat(30.510058);
//        pt1.setLng(114.3880229);
//        pt1.setAlt(30);
//        Waypoint pt2 = new Waypoint();
//        pt2.setLat(30.510068);
//        pt2.setLng(114.3880239);
//        pt2.setAlt(30);
//        Waypoint pt3 = new Waypoint();
//        pt3.setLat(30.51078);
//        pt3.setLng(114.3880279);
//        pt3.setAlt(30);
//        waypoints.add(pt1);
//        waypoints.add(pt2);
//        waypoints.add(pt3);
//        pointFlyContent.setFlightContent(waypoints);
//        taskInfo.setTaskType(TaskType.POINT_FLY);
//        taskInfo.setContent(pointFlyContent);
//        command.setParam(taskInfo);
//        Gson gson = new Gson();
//        String str = gson.toJson(command);
//        System.out.println(str);
//    }
//
//    @Test
//    public void testGenStartMission() {
//        List<Integer> testIntList = new ArrayList<>();
//        testIntList.add(100);
//        testIntList.add(200);
//        testIntList.add(300);
//        System.out.println(testIntList.toString());
//
//        Command command = new Command();
//        command.setCmd(CommandType.START_MISSION);
//        MissionParam missionParam = new MissionParam();
//        missionParam.setTaskId(236);
//        List<DeviceInfo> deviceInfoList = new ArrayList<>();
//        DeviceInfo deviceInfo = new DeviceInfo();
//        deviceInfo.setDeviceId(5);
//        deviceInfo.setDeviceType(0);
//        deviceInfo.setSysId(2);
//        deviceInfoList.add(deviceInfo);
//        missionParam.setDevice(deviceInfoList);
//        command.setParam(missionParam);
//        Gson gson = new Gson();
//        String str = gson.toJson(command);
//        System.out.println(str);
//    }
//
//    @Test
//    public void testGetString() {
//        byte[] buf = {0x57, 0x50, 0x5f, 0x53, 0x50, 0x45, 0x45, 0x44};
//        String str = new String(buf, StandardCharsets.UTF_8);
//        System.out.println(str);
//    }
//
//    @Test
//    public void testNotifyStatus() {
//        Command command = new Command();
//        command.setCmd(CommandType.NOTIFY_STATUS);
//        NotifyStatusParam notifyStatusParam = new NotifyStatusParam();
//        notifyStatusParam.setName("drone-mobile");
//        notifyStatusParam.setDeviceType(0);
//        notifyStatusParam.setLat(30.5120718);
//        notifyStatusParam.setLng(114.38832712);
//        notifyStatusParam.setAlt(30);
//        notifyStatusParam.sethSpeed(1f);
//        notifyStatusParam.setvSpeed(0.2f);
//        notifyStatusParam.setYaw(18.2f);
//        notifyStatusParam.setRoll(1.3f);
//        notifyStatusParam.setPitch(1.1f);
//        notifyStatusParam.setBattery(90f);
//        notifyStatusParam.setState(3);
//        command.setParam(notifyStatusParam);
//        Gson gson = new Gson();
//        String str = gson.toJson(command);
//        System.out.println(str);
//    }
//
//
//    @Test
//    public void testScanner() {
//        Scanner s = new Scanner(System.in);
//        System.out.println("请输入字符串：");
//        while (true) {
//            String line = s.nextLine();
//            if (line.equals("exit")) break;
//            System.out.println(">>>" + line);
//        }
//    }
//
////    @Test
////    public void testJoystickConfig() {
////        X56LeftConfig x56LeftConfig = new X56LeftConfig();
////        x56LeftConfig.getButtonMaps().add(new ButtonMap(1, "north"));
////        Gson gson = new Gson();
////        String str = gson.toJson(x56LeftConfig);
////        System.out.println(str);
////    }
//
//    @Test
//    public void testVisionLand() {
//        Command command = new Command();
//        command.setCmd(CommandType.SUBSCRIBE_VISION_LAND);
//        SubscribeVisionLandParam subscribeVisionLandParam = new SubscribeVisionLandParam();
//        subscribeVisionLandParam.setClientType("Android");
//        subscribeVisionLandParam.setSysId(1);
//        command.setParam(subscribeVisionLandParam);
//        Gson gson = new Gson();
//        String str = gson.toJson(command);
//        System.out.println(str);
//
//        command.setCmd(CommandType.VISION_LAND);
//        VisionLandParam visionLandParam = new VisionLandParam();
//        VisionLandMoveParam visionLandMoveParam = new VisionLandMoveParam();
//        visionLandMoveParam.setSysId(1);
//        visionLandMoveParam.setX(5);
//        visionLandMoveParam.setY(6);
//        visionLandMoveParam.setA(7);
//        visionLandParam.setMove(visionLandMoveParam);
//        command.setParam(visionLandParam);
//        str = gson.toJson(command);
//        System.out.println(str);
//
//        CmdVisionLandParam param = new CmdVisionLandParam();
//        param.setVideoUrl("");
//        param.setSysId(1);
//        command.setCmd(CommandType.START_VISION_LAND);
//        command.setParam(param);
//        str = gson.toJson(command);
//        System.out.println(str);
//
//        param.setVideoUrl("");
//        param.setSysId(1);
//        command.setCmd(CommandType.STOP_VISION_LAND);
//        command.setParam(param);
//        str = gson.toJson(command);
//        System.out.println(str);
//    }
//
//    @Test
//    public void testInstruction() {
//        Command command = new Command();
//        command.setCmd(CommandType.SEND_INSTRUCTION);
//        SendInstructionInfo sendInstructionInfo = new SendInstructionInfo();
//
//        sendInstructionInfo.setInstructionName("航点飞行指令");
//        sendInstructionInfo.setUserId(1);
//        sendInstructionInfo.setClientType(ClientType.MOBILE);
//        sendInstructionInfo.setInstructionType(InstructionType.POINT_FLY);
//        PointFlyContent pointFlyContent = new PointFlyContent();
//        pointFlyContent.setDeviceId(5);
//        pointFlyContent.setHeight(30);
//        pointFlyContent.setSpeed(1);
//        pointFlyContent.setSysId(1);
//        List<Waypoint> waypoints = new ArrayList<>();
//        Waypoint pt1 = new Waypoint();
//        pt1.setLat(30.510058);
//        pt1.setLng(114.3880229);
//        pt1.setAlt(30);
//        Waypoint pt2 = new Waypoint();
//        pt2.setLat(30.510068);
//        pt2.setLng(114.3880239);
//        pt2.setAlt(30);
//        Waypoint pt3 = new Waypoint();
//        pt3.setLat(30.51078);
//        pt3.setLng(114.3880279);
//        pt3.setAlt(30);
//        waypoints.add(pt1);
//        waypoints.add(pt2);
//        waypoints.add(pt3);
//        pointFlyContent.setFlightContent(waypoints);
//        sendInstructionInfo.setContent(pointFlyContent);
//        command.setParam(sendInstructionInfo);
//        Gson gson = new Gson();
//        String str = gson.toJson(command);
//        System.out.println(str);
//
//        sendInstructionInfo.setInstructionName("实时对抗");
//        sendInstructionInfo.setInstructionType(InstructionType.CHECK_POINT);
//        CheckPointContent checkPointContent = new CheckPointContent();
//        checkPointContent.setPoint(new Point(32.11, 112.113));
//        sendInstructionInfo.setContent(checkPointContent);
//        command.setParam(sendInstructionInfo);
//        str = gson.toJson(command);
//        System.out.println(str);
//
//        sendInstructionInfo.setInstructionName("创建全景图");
//        sendInstructionInfo.setInstructionType(InstructionType.CREATE_PANORAMA);
//        CreatePanoramaContent createPanoramaContent = new CreatePanoramaContent();
//        createPanoramaContent.setPoint(new Point3D(32.112, 112.11344, 11));
//        sendInstructionInfo.setContent(createPanoramaContent);
//        command.setParam(sendInstructionInfo);
//        str = gson.toJson(command);
//        System.out.println(str);
//
//        sendInstructionInfo.setInstructionName("创建正射影像");
//        sendInstructionInfo.setInstructionType(InstructionType.CREATE_ORTHO);
//        CreateOrthoContent createOrthoContent = new CreateOrthoContent();
//        createOrthoContent.setHeight(100);
//        List<Point> vertexList = new ArrayList<>();
//        vertexList.add(new Point(32.114, 112.22));
//        vertexList.add(new Point(32.115, 112.232));
//        vertexList.add(new Point(32.116, 112.252));
//        vertexList.add(new Point(32.117, 112.2672));
//        createOrthoContent.setVertexList(vertexList);
//        sendInstructionInfo.setContent(createOrthoContent);
//        command.setParam(sendInstructionInfo);
//        str = gson.toJson(command);
//        System.out.println(str);
//
//    }
//
//    @Test
//    public void testPathElement() {
//        Gson gson = new Gson();
//
//        PathElement pathElement = new PathElement();
//        pathElement.setType(PathElementType.POINT);
//        Waypoint waypoint = new Waypoint();
//        waypoint.setLat(32.11);
//        waypoint.setLng(112.33);
//        waypoint.setAlt(30);
//        pathElement.setPoint(waypoint);
//        Area area = new Area();
//        List<Point> points = new ArrayList<>();
//        points.add(new Point(32.11, 112.23));
//        points.add(new Point(32.11, 112.24));
//        points.add(new Point(32.10, 112.25));
//        points.add(new Point(32.12, 112.26));
//        area.setVertexList(points);
//        pathElement.setArea(area);
//        String str = gson.toJson(pathElement);
//        System.out.println(str);
//
//        Panorama panorama = new Panorama();
//        panorama.setSpeed(1);
//        panorama.setWaypoint(waypoint);
//        str = gson.toJson(panorama);
//        System.out.println(str);
//    }
//
//    @Test
//    public void testUserStatus() {
//        Gson gson = new Gson();
//        Command command = new Command();
//        command.setCmd(CommandType.NOTIFY_USER_STATUS);
//        NotifyUserStatusParam notifyUserStatusParam = new NotifyUserStatusParam();
//        notifyUserStatusParam.setLat(33.222);
//        notifyUserStatusParam.setLng(112.343);
//        notifyUserStatusParam.setUserId(1);
//        notifyUserStatusParam.setName("admin");
//        notifyUserStatusParam.setTaskId(1);
//        command.setParam(notifyUserStatusParam);
//        String str = gson.toJson(command);
//        System.out.println(str);
//    }
//
//    @Test
//    public void testDetectedInfo() {
//        Gson gson = new Gson();
//        Command command = new Command();
//        command.setCmd(CommandType.DETECTED_INFO);
//        DetectedInfoParam detectedInfoParam = new DetectedInfoParam();
//        List<SignLayerInfo> signLayerInfoList = new ArrayList<>();
//        SignLayerInfo signLayerInfo = new SignLayerInfo();
//        signLayerInfo.setSignName("目标1");
//        signLayerInfo.setLat(33.221);
//        signLayerInfo.setLng(114.123);
//        signLayerInfo.setCreatedTime(new Date());
//        signLayerInfo.setSignType(0);
//        signLayerInfo.setDescription("sss");
//        signLayerInfo.setUpdateTime(new Date());
//        signLayerInfoList.add(signLayerInfo);
//        detectedInfoParam.setTargets(signLayerInfoList);
//        command.setParam(detectedInfoParam);
//        String str = gson.toJson(command);
//        System.out.println(str);
//    }
//
//
//    @Test
//    public void testAisStatus() {
//        Gson gson = new Gson();
//        Command command = new Command();
//        command.setCmd(CommandType.NOTIFY_USER_STATUS);
//        NotifyUserStatusParam notifyUserStatusParam = new NotifyUserStatusParam();
//        notifyUserStatusParam.setLat(33.222);
//        notifyUserStatusParam.setLng(112.343);
//        notifyUserStatusParam.setUserId(1);
//        notifyUserStatusParam.setName("admin");
//        notifyUserStatusParam.setTaskId(1);
//        command.setParam(notifyUserStatusParam);
//        String str = gson.toJson(command);
//        System.out.println(str);
//    }
//
//    @Test
//    public void testVoice() {
//        Command command = new Command();
//        command.setCmd("take_off");
//        command.setParam(new Object());
//        Gson gson = new Gson();
//        String str = gson.toJson(command) + "\r\n";
//        System.out.println(str);
//
//    }
//
//    @Test
//    public void testAppControl() {
//        Command command = new Command();
//        command.setCmd(CommandType.SUBSCRIBE_APP_CONTROL);
//        SubscribeAppControlParam subscribeAppControlParam = new SubscribeAppControlParam();
//        subscribeAppControlParam.setClientType("Android");
//        subscribeAppControlParam.setDeviceId(5);
//        command.setParam(subscribeAppControlParam);
//        Gson gson = new Gson();
//        String str = gson.toJson(command);
//        System.out.println(str);
//    }
//
//    @Test
//    public void testManualControl() {
//        Command command = new Command();
//        command.setCmd(CommandType.MANUAL_CONTROL);
//        ManualControlParam manualControlParam = new ManualControlParam();
//        manualControlParam.setDeviceId(5);
//        manualControlParam.setDeviceType(0);
//        manualControlParam.setSysId(2);
//        manualControlParam.setX(1);
//        manualControlParam.setY(2);
//        manualControlParam.setZ(3);
//        manualControlParam.setR(4);
//        command.setParam(manualControlParam);
//        Gson gson = new Gson();
//        String str = gson.toJson(command);
//        System.out.println(str);
//    }
//
//    @Test
//    public void testNotifyAll() {
//        Object object = new Object();
//        AtomicBoolean canDoing = new AtomicBoolean(true);
//        try {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(1000);
//                        synchronized (object) {
//                            object.notifyAll();
//                        }
//                        canDoing.set(true);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
//            if (canDoing.compareAndSet(true, false)) {
//                String str = "test 1";
//                synchronized (object) {
//                    object.wait(10000);
//                }
//                System.out.println(str);
////                str = "test 2";
////                synchronized (object) {
////                    object.wait(5000);
////                }
////                System.out.println(str);
////                str = "test 3";
////                synchronized (object) {
////                    object.wait(5000);
////                }
////                System.out.println(str);
//            }
//            if (canDoing.compareAndSet(true, false)) {
//                String str = "test 4";
//                synchronized (object) {
//                    object.wait(5000);
//                }
//                System.out.println(str);
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void testUdpMultiCastClient() {
//        try {
//            UdpClient udpClient = new UdpClient();
//            udpClient.setServerIP("234.186.3.1");
//            //udpClient.setServerIP("127.0.0.1");
//            udpClient.setIdleTime(Config.idleTime);
//            udpClient.setConnectTimeout(Config.connectTimeout);
//            udpClient.setServerPort(8203);
//            //udpClient.setServerPort(60000);
////            udpClient.getConnector().getSessionConfig().setReuseAddress(true);
////            udpClient.getConnector().getSessionConfig().setBroadcast(true);
//
//
//            CommandEncoder encoder = new CommandEncoder();
//            CommandDecoder decoder = new CommandDecoder();
//            udpClient.setFilter(new ProtocolCodecFilter(new SimpleCodecFactory(
//                    decoder, encoder)));
//            CommandHandler handler = new CommandHandler();
//            udpClient.setHandler(handler);
//            udpClient.initial();
//            udpClient.connect();
//            udpClient.send(IoBuffer.wrap("test udp multicast".getBytes(StandardCharsets.UTF_8)));
//            System.in.read();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void testUdpServer() {
//        try {
//            UdpServer udpServer = new UdpServer();
//            udpServer.setIdleTime(Config.idleTime);
//            udpServer.setServerPort(8203);
//            CommandEncoder encoder = new CommandEncoder();
//            CommandDecoder decoder = new CommandDecoder();
//            udpServer.setFilter(new ProtocolCodecFilter(
//                    new SimpleCodecFactory(decoder, encoder)));
//            CommandHandler handler = new CommandHandler();
//            udpServer.setHandler(handler);
//            udpServer.start();
//            System.in.read();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void testUdpMultiCastServer() {
//        try {
//            MultiCastUdpServer udpServer = new MultiCastUdpServer();
//            udpServer.setIdleTime(Config.idleTime);
//            udpServer.setServerPort(8203);
//            udpServer.setGroup("234.186.3.1");
//            udpServer.setNetworkInterface("192.168.2.233");
//            CommandEncoder encoder = new CommandEncoder();
//            CommandDecoder decoder = new CommandDecoder();
//            udpServer.setFilter(new ProtocolCodecFilter(
//                    new SimpleCodecFactory(decoder, encoder)));
//            CommandHandler handler = new CommandHandler();
//            udpServer.setHandler(handler);
//            udpServer.start();
//            System.in.read();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void testUtf8() {
//        try {
//            long a = 2548476052L;
//            System.out.println(Long.toHexString(a));
//            byte[] buf = Convert.hexStringToByteWithSpace("e6 9c ba e8 bd bd e9 a2 84 e8 ad a6 e9 9b b7 e8 be be 5f 31 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00");
//            String bufString = Convert.bytesToHexString(buf, true);
//            System.out.println(bufString);
//            String s = new String(buf, StandardCharsets.UTF_8);
//            System.out.println(s);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}