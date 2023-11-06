package com.hkxx.common;

import java.util.Arrays;

/**
 * USB 设备操作工具类
 */
public class UsbUtil {

    public static final String ITEM_TYPE_MAIN = "Main";
    public static final String ITEM_TYPE_GLOBAL = "Global";
    public static final String ITEM_TYPE_LOCAL = "Local";
    public static final String ITEM_TAG_INPUT = "Input";
    public static final String ITEM_TAG_OUTPUT = "Output";
    public static final String ITEM_TAG_FEATURE = "Feature";
    public static final String ITEM_TAG_COLLECTION = "Collection";
    public static final String ITEM_TAG_END_COLLECTION = "End Collection";
    public static final String ITEM_TAG_USAGE_PAGE = "Usage Page";
    public static final String ITEM_TAG_LOGICAL_MINIMUM = "Logical Minimum";
    public static final String ITEM_TAG_LOGICAL_MAXIMUM = "Logical Maximum";
    public static final String ITEM_TAG_PHYSICAL_MINIMUM = "Physical Minimum";
    public static final String ITEM_TAG_PHYSICAL_MAXIMUM = "Physical Maximum";
    public static final String ITEM_TAG_UNIT_EXPONENT = "Unit Exponent";
    public static final String ITEM_TAG_UNIT = "Unit";
    public static final String ITEM_TAG_REPORT_SIZE = "Report Size";
    public static final String ITEM_TAG_REPORT_ID = "Report ID";
    public static final String ITEM_TAG_REPORT_COUNT = "Report Count";
    public static final String ITEM_TAG_PUSH = "Push";
    public static final String ITEM_TAG_POP = "Pop";
    public static final String ITEM_TAG_USAGE = "Usage";
    public static final String ITEM_TAG_USAGE_MINIMUM = "Usage Minimum";
    public static final String ITEM_TAG_USAGE_MAXIMUM = "Usage Maximum";
    public static final String ITEM_TAG_DESIGNATOR_INDEX = "Designator Index";
    public static final String ITEM_TAG_DESIGNATOR_MINIMUM = "Designator Minimum";
    public static final String ITEM_TAG_DESIGNATOR_MAXIMUM = "Designator Maximum";
    public static final String ITEM_TAG_STRING_INDEX = "String Index";
    public static final String ITEM_TAG_STRING_MINIMUM = "String Minimum";
    public static final String ITEM_TAG_STRING_MAXIMUM = "String Maximum";
    public static final String ITEM_TAG_DELIMITER = "Delimiter";


    /**
     * 解析usb的hid类设备报告描述符
     *
     * @param reportDescriptor 报告描述符，16进制字符串表示，中间有空格隔开
     */
    public static void parseHidReportDescriptor(String reportDescriptor) {
        //删除描述符中的空格，转换为byte数组
        System.out.println("Start Parsing HidReportDescriptor...");
        byte[] buf = Convert.hexStringToByte(Convert.deleteSpace(reportDescriptor));
        int itemSize = 0;
        int itemType = 0;
        int itemTag = 0;
        String tempStr = "";
        byte[] data = null;
        for (int i = 0; i < buf.length; ) {
            //解析每个item项
            //第一个字节为item的前缀
            //低2位为itemSize，中间2位为itemType，最高4位为itemTag
            itemSize = buf[i] & 0x03;
            itemType = (buf[i] & 0x0c) >> 2;
            itemTag = (buf[i] & 0xf0) >> 4;

            tempStr = getHidItemType(itemType);
            tempStr = "ItemType:" + tempStr + " ";
            tempStr += getHidItemTag(itemType, itemTag);
            //根据itemSize，获取item的数据部分，0,1,2,3 分别代表0,1,2,4个字节的数据部分
            switch (itemSize) {
                case 0:
                    i++;
                    break;
                case 1:
                case 2:
                    data = Arrays.copyOfRange(buf, i + 1, i + 1 + itemSize);
                    //获取数据部分的描述
                    tempStr += getHidItemData(itemType, itemTag, data);
                    i += itemSize + 1;
                    break;
                case 3:
                    data = Arrays.copyOfRange(buf, i + 1, i + 1 + 4);
                    //获取数据部分的描述
                    tempStr += getHidItemData(itemType, itemTag, data);
                    i += 4 + 1;
                    break;
                default:
                    break;
            }
            //输出item的描述
            System.out.println(tempStr);
        }
        System.out.println("Parse HidReportDescriptor over.");
    }

    /**
     * 根据hid设备的itemType，返回对应的类型描述
     *
     * @param itemType
     * @return 对应的类型描述
     */
    public static String getHidItemType(int itemType) {
        String rt = "";
        switch (itemType) {
            case 0:
                rt = ITEM_TYPE_MAIN;
                break;
            case 1:
                rt = ITEM_TYPE_GLOBAL;
                break;
            case 2:
                rt = ITEM_TYPE_LOCAL;
                break;
            case 3:
                rt = "Reserved";
                break;
            default:
                break;
        }
        return rt;
    }

    /**
     * 根据itemType和itemTag，返回具体的itemTag描述
     *
     * @param itemType
     * @param itemTag
     * @return
     */
    public static String getHidItemTag(int itemType, int itemTag) {
        String rt = "";
        switch (itemType) {
            case 0:
                //Main
                switch (itemTag) {
                    case 0x08:
                        rt = ITEM_TAG_INPUT;
                        break;
                    case 0x09:
                        rt = ITEM_TAG_OUTPUT;
                        break;
                    case 0x0b:
                        rt = ITEM_TAG_FEATURE;
                        break;
                    case 0x0a:
                        rt = ITEM_TAG_COLLECTION;
                        break;
                    case 0x0c:
                        rt = ITEM_TAG_END_COLLECTION;
                        break;
                    default:
                        break;
                }
                break;
            case 1:
                //Global
                switch (itemTag) {
                    case 0x00:
                        rt = ITEM_TAG_USAGE_PAGE;
                        break;
                    case 0x01:
                        rt = ITEM_TAG_LOGICAL_MINIMUM;
                        break;
                    case 0x02:
                        rt = ITEM_TAG_LOGICAL_MAXIMUM;
                        break;
                    case 0x03:
                        rt = ITEM_TAG_PHYSICAL_MINIMUM;
                        break;
                    case 0x04:
                        rt = ITEM_TAG_PHYSICAL_MAXIMUM;
                        break;
                    case 0x05:
                        rt = ITEM_TAG_UNIT_EXPONENT;
                        break;
                    case 0x06:
                        rt = ITEM_TAG_UNIT;
                        break;
                    case 0x07:
                        rt = ITEM_TAG_REPORT_SIZE;
                        break;
                    case 0x08:
                        rt = ITEM_TAG_REPORT_ID;
                        break;
                    case 0x09:
                        rt = ITEM_TAG_REPORT_COUNT;
                        break;
                    case 0x0a:
                        rt = ITEM_TAG_PUSH;
                        break;
                    case 0x0b:
                        rt = ITEM_TAG_POP;
                        break;
                    default:
                        break;
                }
                break;
            case 2:
                //Local
                switch (itemTag) {
                    case 0x00:
                        rt = ITEM_TAG_USAGE;
                        break;
                    case 0x01:
                        rt = ITEM_TAG_USAGE_MINIMUM;
                        break;
                    case 0x02:
                        rt = ITEM_TAG_USAGE_MAXIMUM;
                        break;
                    case 0x03:
                        rt = ITEM_TAG_DESIGNATOR_INDEX;
                        break;
                    case 0x04:
                        rt = ITEM_TAG_DESIGNATOR_MINIMUM;
                        break;
                    case 0x05:
                        rt = ITEM_TAG_DESIGNATOR_MAXIMUM;
                        break;
                    case 0x07:
                        rt = ITEM_TAG_STRING_INDEX;
                        break;
                    case 0x08:
                        rt = ITEM_TAG_STRING_MINIMUM;
                        break;
                    case 0x09:
                        rt = ITEM_TAG_STRING_MAXIMUM;
                        break;
                    case 0x0a:
                        rt = ITEM_TAG_DELIMITER;
                        break;
                    default:
                        break;
                }
                break;
            case 3:
                rt = "Reserved";
                break;
            default:
                break;
        }
        return rt;
    }

    public static String getHidItemData(int itemType, int itemTag, byte[] data) {
        String rt = "";
        int temp = 0;
        String tempStr = "";
        if (data != null && data.length > 0) {
            switch (itemType) {
                case 0:
                    //Main
                    switch (itemTag) {
                        case 0x08:
                            //rt = ITEM_TAG_INPUT;
                            temp = data[0] & 0x01;
                            if (temp == 0) {
                                tempStr = "Data";
                            } else {
                                tempStr = "Constant";
                            }
                            rt += tempStr + ",";
                            temp = (data[0] & 0x02) >> 1;
                            if (temp == 0) {
                                tempStr = "Array";
                            } else {
                                tempStr = "Variable";
                            }
                            rt += tempStr + ",";
                            temp = (data[0] & 0x04) >> 2;
                            if (temp == 0) {
                                tempStr = "Absolute";
                            } else {
                                tempStr = "Relative";
                            }
                            rt += tempStr + ",";
                            temp = (data[0] & 0x08) >> 3;
                            if (temp == 0) {
                                tempStr = "No Wrap";
                            } else {
                                tempStr = "Wrap";
                            }
                            rt += tempStr + ",";
                            temp = (data[0] & 0x10) >> 4;
                            if (temp == 0) {
                                tempStr = "Linear";
                            } else {
                                tempStr = "Non Linear";
                            }
                            rt += tempStr + ",";
                            temp = (data[0] & 0x20) >> 5;
                            if (temp == 0) {
                                tempStr = "Preferred State";
                            } else {
                                tempStr = "No Preferred";
                            }
                            rt += tempStr + ",";
                            temp = (data[0] & 0x40) >> 6;
                            if (temp == 0) {
                                tempStr = "No Null position";
                            } else {
                                tempStr = "Null state";
                            }
                            rt += tempStr;
                            if (data.length > 1) {
                                temp = data[1] & 0x01;
                                if (temp == 0) {
                                    tempStr = "Bit Field";
                                } else {
                                    tempStr = "Buffered Bytes";
                                }
                                rt += "," + tempStr;
                            }
                            rt = "(" + rt + ")";
                            break;
                        case 0x09:
                        case 0x0b:
                            //rt = ITEM_TAG_FEATURE;
                            //rt = ITEM_TAG_OUTPUT;
                            temp = data[0] & 0x01;
                            if (temp == 0) {
                                tempStr = "Data";
                            } else {
                                tempStr = "Constant";
                            }
                            rt += tempStr + ",";
                            temp = (data[0] & 0x02) >> 1;
                            if (temp == 0) {
                                tempStr = "Array";
                            } else {
                                tempStr = "Variable";
                            }
                            rt += tempStr + ",";
                            temp = (data[0] & 0x04) >> 2;
                            if (temp == 0) {
                                tempStr = "Absolute";
                            } else {
                                tempStr = "Relative";
                            }
                            rt += tempStr + ",";
                            temp = (data[0] & 0x08) >> 3;
                            if (temp == 0) {
                                tempStr = "No Wrap";
                            } else {
                                tempStr = "Wrap";
                            }
                            rt += tempStr + ",";
                            temp = (data[0] & 0x10) >> 4;
                            if (temp == 0) {
                                tempStr = "Linear";
                            } else {
                                tempStr = "Non Linear";
                            }
                            rt += tempStr + ",";
                            temp = (data[0] & 0x20) >> 5;
                            if (temp == 0) {
                                tempStr = "Preferred State";
                            } else {
                                tempStr = "No Preferred";
                            }
                            rt += tempStr + ",";
                            temp = (data[0] & 0x40) >> 6;
                            if (temp == 0) {
                                tempStr = "No Null position";
                            } else {
                                tempStr = "Null state";
                            }
                            rt += tempStr;
                            temp = (data[0] & 0x80) >> 7;
                            if (temp == 0) {
                                tempStr = "Non Volatile";
                            } else {
                                tempStr = "Volatile";
                            }
                            rt += tempStr;
                            if (data.length > 1) {
                                temp = data[1] & 0x01;
                                if (temp == 0) {
                                    tempStr = "Bit Field";
                                } else {
                                    tempStr = "Buffered Bytes";
                                }
                                rt += "," + tempStr;
                            }
                            rt = "(" + rt + ")";
                            break;
                        case 0x0a:
                            //rt = ITEM_TAG_COLLECTION;
                            switch (data[0]) {
                                case 0x00:
                                    rt = "(Physical)";
                                    break;
                                case 0x01:
                                    rt = "(Application)";
                                    break;
                                case 0x02:
                                    rt = "(Logical)";
                                    break;
                                case 0x03:
                                    rt = "(Report)";
                                    break;
                                case 0x04:
                                    rt = "(Named Array)";
                                    break;
                                case 0x05:
                                    rt = "(Usage Switch)";
                                    break;
                                case 0x06:
                                    rt = "(Usage Modifier)";
                                    break;
                                default:
                                    break;
                            }
                            break;
                        case 0x0c:
                            //rt = ITEM_TAG_END_COLLECTION;
                            break;
                        default:
                            break;
                    }
                    break;
                case 1:
                    //Global
                    switch (itemTag) {

                        case 0x01:  //rt = ITEM_TAG_LOGICAL_MINIMUM;
                        case 0x02:  //rt = ITEM_TAG_LOGICAL_MAXIMUM;
                        case 0x03:  //rt = ITEM_TAG_PHYSICAL_MINIMUM;
                        case 0x04:  //rt = ITEM_TAG_PHYSICAL_MAXIMUM;
                        case 0x05:  //rt = ITEM_TAG_UNIT_EXPONENT;
                            rt = "(" + Convert.bytesToSignedInt(data) + ")";
                            break;
                        case 0x00://rt = ITEM_TAG_USAGE_PAGE;
                            temp = Convert.bytesToUnsignedInt(data);
                            rt = "(" + temp + " " + getUsagePageName(temp) + ")";
                            break;
                        case 0x06://rt = ITEM_TAG_UNIT;
                        case 0x07:  //rt = ITEM_TAG_REPORT_SIZE;
                        case 0x08: //rt = ITEM_TAG_REPORT_ID;
                        case 0x09:  //rt = ITEM_TAG_REPORT_COUNT;
                        case 0x0a:  //rt = ITEM_TAG_PUSH;
                        case 0x0b:  //rt = ITEM_TAG_POP;
                            rt = "(" + Convert.bytesToUnsignedInt(data) + ")";
                            break;
                        default:
                            break;
                    }
                    break;
                case 2:
                    //Local
                    switch (itemTag) {
                        case 0x00:
                            //rt = ITEM_TAG_USAGE;
                            if (data.length == 1) {
                                temp = data[0] & 0xff;
                                rt = "(UsageID:" + temp + ")";
                            } else if (data.length == 2) {
                                temp = (data[0] & 0xff) + ((data[1] & 0xff) << 8);
                                rt = "(UsageID:" + temp + ")";
                            } else if (data.length == 4) {
                                //高16位为UsagePageID
                                temp = (data[2] & 0xff) + ((data[3] & 0xff) << 8);
                                rt = "(UsagePageID:" + temp + " ";
                                //低16位为Usage ID
                                temp = (data[0] & 0xff) + ((data[1] & 0xff) << 8);
                                rt += "UsageID:" + temp + ")";
                            }
                            break;
                        case 0x01://rt = ITEM_TAG_USAGE_MINIMUM;
                        case 0x02://rt = ITEM_TAG_USAGE_MAXIMUM;
                        case 0x03: //rt = ITEM_TAG_DESIGNATOR_INDEX;
                        case 0x04: //rt = ITEM_TAG_DESIGNATOR_MINIMUM;
                        case 0x05: //rt = ITEM_TAG_DESIGNATOR_MAXIMUM;
                        case 0x07: //rt = ITEM_TAG_STRING_INDEX;
                        case 0x08: //rt = ITEM_TAG_STRING_MINIMUM;
                        case 0x09: //rt = ITEM_TAG_STRING_MAXIMUM;
                        case 0x0a: //rt = ITEM_TAG_DELIMITER;
                            rt = "(" + Convert.bytesToUnsignedInt(data) + ")";
                            break;
                        default:
                            break;
                    }
                    break;
                case 3:
                    rt = "Reserved";
                    break;
                default:
                    break;
            }
        }
        return rt;
    }

    public static String getUsagePageName(int usagePageId) {
        String rt = "";
        switch (usagePageId) {
            case 0x01:
                rt = "Generic Desktop";
                break;
            case 0x02:
                rt = "Simulation Controls";
                break;
            case 0x03:
                rt = "VR Controls";
                break;
            case 0x04:
                rt = "Sport Controls";
                break;
            case 0x05:
                rt = "Game Controls";
                break;
            case 0x06:
                rt = "Generic Device Controls";
                break;
            case 0x07:
                rt = "Keyboard/Keypad";
                break;
            case 0x08:
                rt = "LED";
                break;
            case 0x09:
                rt = "Button";
                break;
            case 0x0a:
                rt = "Ordinal";
                break;
            case 0x0b:
                rt = "Telephony Device";
                break;
            case 0x0c:
                rt = "Consumer";
                break;
            case 0x0d:
                rt = "Digitizers";
                break;
            case 0x0e:
                rt = "Haptics";
                break;
            case 0x10:
                rt = "Unicode";
                break;
            case 0x12:
                rt = "Eye and Head Trackers";
                break;
            case 0x14:
                rt = "Auxiliary Display";
                break;
            case 0x20:
                rt = "Sensors";
                break;
            case 0x40:
                rt = "Medical Instrument";
                break;
            case 0x41:
                rt = "Braille Display";
                break;
            case 0x59:
                rt = "Lighting And Illumination";
                break;
            case 0x90:
                rt = "Camera Control";
                break;
            case 0x92:
                rt = "Gaming Device";
                break;
            case 0xf1d0:
                rt = "FIDO Alliance";
                break;
            default:
                break;
        }

        return rt;
    }


}
