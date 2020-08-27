package com.MAVLink;

import com.MAVLink.common.CRC;
import org.junit.Test;

import java.nio.ByteBuffer;

public class MavlinkTestDemo {


    public static final int MAVLINK_MSG_ID_ALTITUDE = 141;

    public static final int MAVLINK_MSG_LENGTH = 24;

    private static final long serialVersionUID = MAVLINK_MSG_ID_ALTITUDE;


    private Parser parser = new Parser();//1位解析类


    public CRC generateCRC(byte[] packet){

        CRC crc = new CRC();

        for (int i = 1; i < packet.length - 2; i++) {

            crc.update_checksum(packet[i] & 0xFF);

        }

        crc.finish_checksum(MAVLINK_MSG_ID_ALTITUDE);

        return crc;

    }


    public byte[] generateTestPacket(){

        ByteBuffer payload = ByteBuffer.allocate(6 + MAVLINK_MSG_LENGTH + 2);

        payload.put((byte) MAVLinkPacket.MAVLINK_STX); //stx

        payload.put((byte)MAVLINK_MSG_LENGTH); //len

        payload.put((byte)0); //seq

        payload.put((byte)255); //sysid

        payload.put((byte)190); //comp id

        payload.put((byte)MAVLINK_MSG_ID_ALTITUDE); //msg id

        payload.putFloat((float)18.0); //altitude_monotonic

        payload.putFloat((float)45.0); //altitude_amsl

        payload.putFloat((float)73.0); //altitude_local

        payload.putFloat((float)101.0); //altitude_relative

        payload.putFloat((float)129.0); //altitude_terrain

        payload.putFloat((float)157.0); //bottom_clearance


        CRC crc = generateCRC(payload.array());

        payload.put((byte)crc.getLSB());

        payload.put((byte)crc.getMSB());

        return payload.array();

    }


    @Test
    public void test(){

        //生成mavlink的字节数组
        byte[] packet = generateTestPacket();
        for(int i = 0; i < packet.length - 1; i++){
            parser.mavlink_parse_char(packet[i] & 0xFF);//每次解析1位
        }
        for (byte b : packet) {
            System.out.print(b+" ");
        }


        //字节数组的解析过程
        MAVLinkPacket m = parser.mavlink_parse_char(packet[packet.length - 1] & 0xFF);//最后1位即可返回
            ByteBuffer byteBuffer =  m.payload.payload;
        System.out.print(byteBuffer.getFloat(4)+" ");
        System.out.println(m.payload.payload.get(0));
        byte[] processedPacket = m.encodePacket();//解析
        //assertArrayEquals("msg_altitude", processedPacket, packet);

    }

}