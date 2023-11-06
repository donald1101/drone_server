package com.hkxx.drone.joystick;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.hkxx.common.Convert;
import com.hkxx.common.UsbUtil;
import com.hkxx.drone.CommandType;
import com.hkxx.drone.Config;
import com.hkxx.drone.Program;
import com.hkxx.drone.ProtocolType;
import com.hkxx.drone.db.entity.Command;
import com.hkxx.drone.db.entity.ManualControlParam;
import com.sun.jna.Platform;
import org.hid4java.*;
import org.hid4java.event.HidServicesEvent;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class JoystickService implements HidServicesListener {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    //摇杆配置参数
//    private String joystickName = "";
//    private String hidReportDescriptor = "";
    //private List<JoystickInfo> joystickInfoList = new ArrayList<>();
    private HashMap<String, JoystickInfo> joystickInfoHashMap = new HashMap<>();

    public JoystickService() {

    }


    public void start() {
        try {
            //加载摇杆配置参数，解析报告描述符
            String[] joystickConfigFiles = Config.joystickConfigFile.split(",");
            for (int i = 0; i < joystickConfigFiles.length; i++) {
                Properties prop = new Properties();
                InputStream config = Program.class
                        .getResourceAsStream("/joystick/" + joystickConfigFiles[i]);
                prop.load(config);
                JoystickInfo joystickInfo = new JoystickInfo();
                joystickInfo.setJoystickName(prop.getProperty("joystickName"));
                joystickInfo.setHidReportDescriptor(prop.getProperty("hidReportDescriptor"));
                joystickInfo.setVendorId(Integer.parseInt(prop.getProperty("vendorId"), 16));
                joystickInfo.setProductId(Integer.parseInt(prop.getProperty("productId"), 16));
                joystickInfo.setSerialNumber(prop.getProperty("serialNumber"));
                joystickInfo.setReportId(Integer.parseInt(prop.getProperty("reportId")));
                joystickInfo.setCommandConfig(prop.getProperty("commandConfig"));
                switch (joystickInfo.getJoystickName()) {
                    case JoystickModel.X56_LEFT:
                        joystickInfo.setCommandConfigObj(JSON.parseObject(joystickInfo.getCommandConfig(), X56LeftConfig.class));
                        break;
                    case JoystickModel.X56_RIGHT:
                        joystickInfo.setCommandConfigObj(JSON.parseObject(joystickInfo.getCommandConfig(), X56RightConfig.class));
                        break;
                    default:
                        break;
                }
                joystickInfoHashMap.put(getHidDeviceKey(joystickInfo.getVendorId(), joystickInfo.getProductId(), joystickInfo.getSerialNumber()), joystickInfo);
                //joystickInfoList.add(joystickInfo);
                UsbUtil.parseHidReportDescriptor(joystickInfo.getHidReportDescriptor());
            }


//            prop.setProperty("joystickName", "new joystick name");
//            OutputStream outputStream = new FileOutputStream(Program.class.getResource("/joystick/" + Config.joystickConfigFile).getPath());
//            prop.store(outputStream, "");
            //初始化hid4java库相关参数
            //HidApi.logTraffic=true;
            printPlatform();

            // Configure to use custom specification
            HidServicesSpecification hidServicesSpecification = new HidServicesSpecification();
            // Use the v0.7.0 manual start feature to get immediate attach events
            hidServicesSpecification.setAutoStart(false);
            hidServicesSpecification.setAutoDataRead(true);
            //hidServicesSpecification.setDataReadInterval(500);

            // Get HID services using custom specification
            HidServices hidServices = HidManager.getHidServices(hidServicesSpecification);
            hidServices.addHidServicesListener(this);

            // Manually start the services to get attachment event
            System.out.println(ANSI_GREEN + "Manually starting HID services." + ANSI_RESET);
            hidServices.start();

            System.out.println(ANSI_GREEN + "Enumerating attached devices..." + ANSI_RESET);

            // Provide a list of attached devices
            for (HidDevice hidDevice : hidServices.getAttachedHidDevices()) {
                System.out.println(hidDevice);
            }

            //waitAndShutdown(hidServices);
            //打开设备，开启读取数据线程
            //hidServices.getHidDevice(0x738,0x2221,"k0303882");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {

    }

    public void printPlatform() {

        // System info to assist with library detection
        System.out.println("Platform architecture: " + Platform.ARCH);
        System.out.println("Resource prefix: " + Platform.RESOURCE_PREFIX);
        System.out.println("Libusb activation: " + Platform.isLinux());

    }

    public void waitAndShutdown(HidServices hidServices) {

        System.out.printf(ANSI_YELLOW + "Waiting 30s to demonstrate attach/detach handling. Watch for slow response after write if configured.%n" + ANSI_RESET);

        // Stop the main thread to demonstrate attach and detach events
        sleepNoInterruption();

        // Shut down and rely on auto-shutdown hook to clear HidApi resources
        System.out.printf(ANSI_YELLOW + "Triggering shutdown...%n" + ANSI_RESET);
        hidServices.shutdown();
    }

    /**
     * Invokes {@code unit.}{@link TimeUnit#sleep(long) sleep(sleepFor)}
     * uninterruptibly.
     */
    public static void sleepNoInterruption() {
        boolean interrupted = false;
        try {
            long remainingNanos = TimeUnit.SECONDS.toNanos(5);
            long end = System.nanoTime() + remainingNanos;
            while (true) {
                try {
                    // TimeUnit.sleep() treats negative timeouts just like zero.
                    NANOSECONDS.sleep(remainingNanos);
                    return;
                } catch (InterruptedException e) {
                    interrupted = true;
                    remainingNanos = end - System.nanoTime();
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private String getHidDeviceKey(int vendorId, int productId, String serialNumber) {
        String rt = vendorId + productId + serialNumber;
        return rt;
    }


    @Override
    public void hidDeviceAttached(HidServicesEvent event) {

        System.out.println(ANSI_BLUE + "Device attached: " + event + ANSI_RESET);
        //打开摇杆设备，开启读取数据线程
        JoystickInfo joystickInfo = null;
        HidDevice device = event.getHidDevice();
        String hidDeviceKey = getHidDeviceKey(device.getVendorId(), device.getProductId(), device.getSerialNumber());
        if (joystickInfoHashMap.containsKey(hidDeviceKey)) {
            device.open();
            joystickInfo = joystickInfoHashMap.get(hidDeviceKey);
            switch (joystickInfo.getJoystickName()) {
                case JoystickModel.X56_LEFT:
                    X56LeftConfig x56LeftConfig = (X56LeftConfig) joystickInfo.getCommandConfigObj();
                    x56LeftConfig.startConnection();
                    break;
                case JoystickModel.X56_RIGHT:

                    break;
                default:
                    break;
            }
        }
//        for (int i = 0; i < joystickInfoList.size(); i++) {
//            joystickInfo = joystickInfoList.get(i);
//            if (event.getHidDevice().isVidPidSerial(joystickInfo.getVendorId(), joystickInfo.getProductId(), joystickInfo.getSerialNumber())) {
//                event.getHidDevice().open();
//            }
//        }
//        //方位摇杆，右侧
//        if (event.getHidDevice().isVidPidSerial(0x738, 0x2221, "k0303882")) {
//            event.getHidDevice().open();
//        }
//        //高度速度摇杆，左侧
    }

    @Override
    public void hidDeviceDetached(HidServicesEvent event) {
        System.out.println(ANSI_YELLOW + "Device detached: " + event + ANSI_RESET);
        JoystickInfo joystickInfo = null;
        HidDevice device = event.getHidDevice();
        String hidDeviceKey = getHidDeviceKey(device.getVendorId(), device.getProductId(), device.getSerialNumber());
        if (joystickInfoHashMap.containsKey(hidDeviceKey)) {
            device.open();
            joystickInfo = joystickInfoHashMap.get(hidDeviceKey);
            switch (joystickInfo.getJoystickName()) {
                case JoystickModel.X56_LEFT:
                    X56LeftConfig x56LeftConfig = (X56LeftConfig) joystickInfo.getCommandConfigObj();
                    x56LeftConfig.stopConnection();
                    break;
                case JoystickModel.X56_RIGHT:

                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void hidFailure(HidServicesEvent event) {
        System.out.println(ANSI_RED + "HID failure: " + event + ANSI_RESET);
    }

    @Override
    public void hidDataReceived(HidServicesEvent event) {

        try {
            System.out.printf(ANSI_PURPLE + "Data received:%n");
            byte[] dataReceived = event.getDataReceived();
            System.out.printf("< [%02x]:", dataReceived.length);
            for (byte b : dataReceived) {
                System.out.printf(" %02x", b);
            }
            //System.out.println("\r\n");
            System.out.println(ANSI_RESET);
            JoystickInfo joystickInfo = null;
            HidDevice device = event.getHidDevice();
            String hidDeviceKey = getHidDeviceKey(device.getVendorId(), device.getProductId(), device.getSerialNumber());
            joystickInfo = joystickInfoHashMap.get(hidDeviceKey);
            switch (joystickInfo.getJoystickName()) {
                case JoystickModel.X56_LEFT:
                    processX56LeftInput(dataReceived, (X56LeftConfig) joystickInfo.getCommandConfigObj());
                    break;
                case JoystickModel.X56_RIGHT:
                    processX56RightInput(dataReceived, (X56RightConfig) joystickInfo.getCommandConfigObj());
                    break;
                default:
                    break;
            }

//            //高度速度摇杆，左侧，x56Left
//            if (event.getHidDevice().isVidPidSerial(0x738, 0xa221, "ko046084")) {
//                //解析input report具体含义
//                processX56LeftInput(dataReceived);
//            }
//            //方位摇杆，右侧，x56Right
//            else if (event.getHidDevice().isVidPidSerial(0x738, 0x2221, "k0303882")) {
//                //解析input report具体含义
//                processX56RightInput(dataReceived);
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void processX56LeftInput(byte[] data) {
        //一个input report总共：4+2+3+2=11 个字节
        if (data.length == 13) {
            int x = 0;
            int y = 0;
            int buttons_1to4 = 0; //总共36个button，此变量代表button1至button4
            int buttons_5to36 = 0;//此变量代表button5至button36
            int[] buttons = new int[36];
            int z = 0;
            int rx = 0;
            int rz = 0;
            int ry = 0;
            int slider = 0;
            int dial = 0;

            x = (data[0] & 0xff) + ((data[1] & 0x03) << 8);
            y = (data[1] >> 2 & 0x3f) + ((data[2] & 0x0f) << 6);
            buttons_1to4 = (data[2] >> 4 & 0x0f);
            buttons_5to36 = (data[3] & 0xff) + ((data[4] & 0xff) << 8) + ((data[5] & 0xff) << 16) + ((data[6] & 0xff) << 24);
            String btState = "";
            for (int i = 0; i < buttons.length; i++) {
                if (i < 4) {
                    buttons[i] = (buttons_1to4 >> i) & 0x01;
                } else {
                    buttons[i] = (buttons_5to36 >> (i - 4)) & 0x01;
                }
                btState += (i + 1) + "(" + buttons[i] + ")";
            }
            z = data[7] & 0xff;
            rx = data[8] & 0xff;
            rz = data[9] & 0xff;
            ry = data[10] & 0xff;
            slider = data[11] & 0xff;
            dial = data[12] & 0xff;

            String tempStr = "[X56Left] ";
//            tempStr += "X:" + x + " Y:" + y + " Buttons(1-36):" + Convert.intToBinaryString(buttons_5to36, 32)
//                    + Convert.intToBinaryString(buttons_1to4, 4) + " z:" + z + " Rx:" + rx + " Ry:" + ry
//                    + " Rz:" + rz + " slider:" + slider + " dial:" + dial;
            tempStr += "X:" + x + " Y:" + y + " Buttons(1-36):" + Convert.intToBinaryString(buttons_5to36, 32)
                    + Convert.intToBinaryString(buttons_1to4, 4) + " z:" + z + " Rx:" + rx + " Ry:" + ry
                    + " Rz:" + rz + " slider:" + slider + " dial:" + dial;
            //+ "\nButton Detail:" + btState;
            System.out.println(tempStr);
        }
    }

    private void processX56LeftInput(byte[] data, X56LeftConfig config) {
        //一个input report总共：4+2+3+2=11 个字节
        if (data.length == 13) {
            int x = 0;
            int y = 0;
            int buttons_1to4 = 0; //总共36个button，此变量代表button1至button4
            int buttons_5to36 = 0;//此变量代表button5至button36
            int[] buttons = new int[36];
            int z = 0;
            int rx = 0;
            int rz = 0;
            int ry = 0;
            int slider = 0;
            int dial = 0;

            x = (data[0] & 0xff) + ((data[1] & 0x03) << 8);
            y = (data[1] >> 2 & 0x3f) + ((data[2] & 0x0f) << 6);
            buttons_1to4 = (data[2] >> 4 & 0x0f);
            buttons_5to36 = (data[3] & 0xff) + ((data[4] & 0xff) << 8) + ((data[5] & 0xff) << 16) + ((data[6] & 0xff) << 24);
            String btState = "";
            for (int i = 0; i < buttons.length; i++) {
                if (i < 4) {
                    buttons[i] = (buttons_1to4 >> i) & 0x01;
                } else {
                    buttons[i] = (buttons_5to36 >> (i - 4)) & 0x01;
                }
                btState += (i + 1) + "(" + buttons[i] + ")";
            }
            z = data[7] & 0xff;
            rx = data[8] & 0xff;
            rz = data[9] & 0xff;
            ry = data[10] & 0xff;
            slider = data[11] & 0xff;
            dial = data[12] & 0xff;

            String tempStr = "[X56Left] ";
//            tempStr += "X:" + x + " Y:" + y + " Buttons(1-36):" + Convert.intToBinaryString(buttons_5to36, 32)
//                    + Convert.intToBinaryString(buttons_1to4, 4) + " z:" + z + " Rx:" + rx + " Ry:" + ry
//                    + " Rz:" + rz + " slider:" + slider + " dial:" + dial;
            tempStr += "X:" + x + " Y:" + y + " Buttons(1-36):" + Convert.intToBinaryString(buttons_5to36, 32)
                    + Convert.intToBinaryString(buttons_1to4, 4) + " z:" + z + " Rx:" + rx + " Ry:" + ry
                    + " Rz:" + rz + " slider:" + slider + " dial:" + dial;
            //+ "\nButton Detail:" + btState;
            System.out.println(tempStr);

            //根据配置，执行指定的操作
            boolean isSendCommand = false;
            switch (config.getProtocolType()) {
                case ProtocolType.JSON_ADD_CRLF:
                    ManualControlParam manualControlParam = new ManualControlParam();
                    manualControlParam.setDeviceId(config.getDeviceId());
                    manualControlParam.setDeviceType(config.getDeviceType());
                    manualControlParam.setSysId(config.getSysId());
                    if (!config.getX().isEmpty()) {
                        prepareCommand_JSON_ADD_CRLF(manualControlParam, config.getX(), x);
                        isSendCommand = true;
                    }
                    if (!config.getY().isEmpty()) {
                        prepareCommand_JSON_ADD_CRLF(manualControlParam, config.getY(), y);
                        isSendCommand = true;
                    }
                    if (!config.getZ().isEmpty()) {
                        prepareCommand_JSON_ADD_CRLF(manualControlParam, config.getZ(), z);
                        isSendCommand = true;
                    }
                    if (!config.getRx().isEmpty()) {
                        prepareCommand_JSON_ADD_CRLF(manualControlParam, config.getRx(), rx);
                        isSendCommand = true;
                    }
                    if (!config.getRy().isEmpty()) {
                        prepareCommand_JSON_ADD_CRLF(manualControlParam, config.getRy(), ry);
                        isSendCommand = true;
                    }
                    if (!config.getRz().isEmpty()) {
                        prepareCommand_JSON_ADD_CRLF(manualControlParam, config.getRz(), rz);
                        isSendCommand = true;
                    }
                    if (!config.getSlider().isEmpty()) {
                        prepareCommand_JSON_ADD_CRLF(manualControlParam, config.getSlider(), slider);
                        isSendCommand = true;
                    }
                    if (!config.getDial().isEmpty()) {
                        prepareCommand_JSON_ADD_CRLF(manualControlParam, config.getDial(), dial);
                        isSendCommand = true;
                    }
                    if (!config.getButtonMaps().isEmpty()) {

                    }
                    if (isSendCommand) {
                        //给控制服务器发送命令
                        Command command = new Command();
                        command.setCmd(CommandType.MANUAL_CONTROL);
                        command.setParam(manualControlParam);
                        Gson gson = new Gson();
                        config.sendData(gson.toJson(command, Command.class) + "\r\n");
                    }
                    break;
                case ProtocolType.MAVLINK:
                    break;
                default:
                    break;
            }


        }
    }

    //准备好参数
    private void prepareCommand_JSON_ADD_CRLF(ManualControlParam manualControlParam, String config, int param) {

        try {
            switch (config) {
                case JoystickCommand.ROLL:
                case JoystickCommand.EAST:
                    manualControlParam.setY(param);
                    break;
                case JoystickCommand.YAW:
                    manualControlParam.setR(param);
                    break;
                case JoystickCommand.PITCH:
                case JoystickCommand.NORTH:
                    manualControlParam.setX(param);
                    break;
                case JoystickCommand.ALTITUDE:
                    manualControlParam.setZ(param);
                    break;
                default:
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processX56RightInput(byte[] data) {
        //一个input report总共：4+2+3+2=11 个字节
        if (data.length == 11) {
            int x = 0;
            int y = 0;
            int rz = 0;
            int hatSwitch = 0;
            int buttons_1to17 = 0; //总共17个button
            int[] buttons = new int[17];
            //int buttonArrayIndex = 0; //7个bit
            int rx = 0;
            int ry = 0;

            x = (data[0] & 0xff) + ((data[1] & 0xff) << 8);
            y = (data[2] & 0xff) + ((data[3] & 0xff) << 8);
            rz = (data[4] & 0xff) + ((data[5] & 0x0f) << 8);
            hatSwitch = (data[5] & 0xf0) >> 4;
            buttons_1to17 = (data[6] & 0xff) + ((data[7] & 0xff) << 8) + ((data[8] & 0x01) << 16);
            String btState = "";
            for (int i = 0; i < buttons.length; i++) {
                buttons[i] = (buttons_1to17 >> i) & 0x01;
                btState += (i + 1) + "(" + buttons[i] + ")";
            }
            //buttonArrayIndex = (data[8] & 0xfe) >> 1;
            rx = data[9] & 0xff;
            ry = data[10] & 0xff;
            String tempStr = "[X56Right] ";
//            tempStr += "X:" + x + " Y:" + y + " Rz:" + rz + " HatSwitch:" + hatSwitch
//                    + " Buttons(1-17):" + (buttons & 0x01) + ((buttons & 0x02) >> 1) + ((buttons & 0x04) >> 2)
//                    + ((buttons & 0x08) >> 3) + (buttons & 0x10 >> 4) + ((buttons & 0x20) >> 5) + ((buttons & 0x40) >> 6)
//                    + ((buttons & 0x80) >> 7) + (buttons & 0x100 >> 8) + ((buttons & 0x200) >> 9) + ((buttons & 0x400) >> 10)
//                    + ((buttons & 0x800) >> 11) + (buttons & 0x1000 >> 12) + ((buttons & 0x2000) >> 13) + ((buttons & 0x4000) >> 14)
//                    + ((buttons & 0x8000) >> 15) + (buttons & 0x10000 >> 16) + " ButtonArrayIndex:" + buttonArrayIndex + " Rx:" + rx + " Ry:" + ry;

//            tempStr += "X:" + x + " Y:" + y + " Rz:" + rz + " HatSwitch:" + hatSwitch
//                    + " Buttons(1-17):" + Convert.intToBinaryString(buttons, 17) + " ButtonArrayIndex:" + buttonArrayIndex + " Rx:" + rx + " Ry:" + ry;
            tempStr += "X:" + x + " Y:" + y + " Rz:" + rz + " HatSwitch:" + hatSwitch
                    + " Buttons(1-17):" + Convert.intToBinaryString(buttons_1to17, 17) + " Rx:" + rx + " Ry:" + ry
                    + "\nButton Detail:" + btState;
            System.out.println(tempStr);
        }
    }

    private void processX56RightInput(byte[] data, X56RightConfig config) {
        //一个input report总共：4+2+3+2=11 个字节
        if (data.length == 11) {
            int x = 0;
            int y = 0;
            int rz = 0;
            int hatSwitch = 0;
            int buttons_1to17 = 0; //总共17个button
            int[] buttons = new int[17];
            //int buttonArrayIndex = 0; //7个bit
            int rx = 0;
            int ry = 0;

            x = (data[0] & 0xff) + ((data[1] & 0xff) << 8);
            y = (data[2] & 0xff) + ((data[3] & 0xff) << 8);
            rz = (data[4] & 0xff) + ((data[5] & 0x0f) << 8);
            hatSwitch = (data[5] & 0xf0) >> 4;
            buttons_1to17 = (data[6] & 0xff) + ((data[7] & 0xff) << 8) + ((data[8] & 0x01) << 16);
            String btState = "";
            for (int i = 0; i < buttons.length; i++) {
                buttons[i] = (buttons_1to17 >> i) & 0x01;
                btState += (i + 1) + "(" + buttons[i] + ")";
            }
            //buttonArrayIndex = (data[8] & 0xfe) >> 1;
            rx = data[9] & 0xff;
            ry = data[10] & 0xff;
            String tempStr = "[X56Right] ";
//            tempStr += "X:" + x + " Y:" + y + " Rz:" + rz + " HatSwitch:" + hatSwitch
//                    + " Buttons(1-17):" + (buttons & 0x01) + ((buttons & 0x02) >> 1) + ((buttons & 0x04) >> 2)
//                    + ((buttons & 0x08) >> 3) + (buttons & 0x10 >> 4) + ((buttons & 0x20) >> 5) + ((buttons & 0x40) >> 6)
//                    + ((buttons & 0x80) >> 7) + (buttons & 0x100 >> 8) + ((buttons & 0x200) >> 9) + ((buttons & 0x400) >> 10)
//                    + ((buttons & 0x800) >> 11) + (buttons & 0x1000 >> 12) + ((buttons & 0x2000) >> 13) + ((buttons & 0x4000) >> 14)
//                    + ((buttons & 0x8000) >> 15) + (buttons & 0x10000 >> 16) + " ButtonArrayIndex:" + buttonArrayIndex + " Rx:" + rx + " Ry:" + ry;

//            tempStr += "X:" + x + " Y:" + y + " Rz:" + rz + " HatSwitch:" + hatSwitch
//                    + " Buttons(1-17):" + Convert.intToBinaryString(buttons, 17) + " ButtonArrayIndex:" + buttonArrayIndex + " Rx:" + rx + " Ry:" + ry;
            tempStr += "X:" + x + " Y:" + y + " Rz:" + rz + " HatSwitch:" + hatSwitch
                    + " Buttons(1-17):" + Convert.intToBinaryString(buttons_1to17, 17) + " Rx:" + rx + " Ry:" + ry
                    + "\nButton Detail:" + btState;
            System.out.println(tempStr);

            //根据配置，执行指定的操作
            boolean isSendCommand = false;
            switch (config.getProtocolType()) {
                case ProtocolType.JSON_ADD_CRLF:
                    ManualControlParam manualControlParam = new ManualControlParam();
                    manualControlParam.setDeviceId(config.getDeviceId());
                    manualControlParam.setDeviceType(config.getDeviceType());
                    manualControlParam.setSysId(config.getSysId());
                    if (!config.getX().isEmpty()) {
                        prepareCommand_JSON_ADD_CRLF(manualControlParam, config.getX(), x);
                        isSendCommand = true;
                    }
                    if (!config.getY().isEmpty()) {
                        prepareCommand_JSON_ADD_CRLF(manualControlParam, config.getY(), y);
                        isSendCommand = true;
                    }
                    if (!config.getRx().isEmpty()) {
                        prepareCommand_JSON_ADD_CRLF(manualControlParam, config.getRx(), rx);
                        isSendCommand = true;
                    }
                    if (!config.getRy().isEmpty()) {
                        prepareCommand_JSON_ADD_CRLF(manualControlParam, config.getRy(), ry);
                        isSendCommand = true;
                    }
                    if (!config.getRz().isEmpty()) {
                        prepareCommand_JSON_ADD_CRLF(manualControlParam, config.getRz(), rz);
                        isSendCommand = true;
                    }
                    if (!config.getHatSwitch().isEmpty()) {
                        prepareCommand_JSON_ADD_CRLF(manualControlParam, config.getHatSwitch(), hatSwitch);
                        isSendCommand = true;
                    }
                    if (!config.getButtonMaps().isEmpty()) {

                    }
                    if (isSendCommand) {
                        //给控制服务器发送命令
                        Command command = new Command();
                        command.setCmd(CommandType.MANUAL_CONTROL);
                        command.setParam(manualControlParam);
                        Gson gson = new Gson();
                        config.sendData(gson.toJson(command, Command.class) + "\r\n");
                    }
                    break;
                case ProtocolType.MAVLINK:
                    break;
                default:
                    break;
            }
        }
    }
}
