package com.hkxx.drone;

import io.dronefleet.mavlink.annotations.MavlinkMessageBuilder;
import io.dronefleet.mavlink.annotations.MavlinkMessageInfo;
import io.dronefleet.mavlink.common.CommonDialect;
import io.dronefleet.mavlink.protocol.MavlinkPacket;
import io.dronefleet.mavlink.serialization.MavlinkSerializationException;
import io.dronefleet.mavlink.serialization.payload.reflection.ReflectionPayloadDeserializer;
import io.dronefleet.mavlink.serialization.payload.reflection.ReflectionPayloadSerializer;

import java.util.Arrays;

public class MavlinkUtil {

    public static byte[] payloadObjectToRawBytes(Object payloadObject, int sequence, int systemId, int componentId, int mavlinkVersion) {
        byte[] rt = null;
        try {
            MavlinkPacket mavlinkPacket = null;
            MavlinkMessageInfo messageInfo = (MavlinkMessageInfo) payloadObject.getClass().getAnnotation(MavlinkMessageInfo.class);
            ReflectionPayloadSerializer serializer = new ReflectionPayloadSerializer();
            byte[] payload = serializer.serialize(payloadObject);

            mavlinkPacket = null;
            if (mavlinkVersion == 1) {
                //生成1版本的
                mavlinkPacket = MavlinkPacket.createMavlink1Packet(sequence, systemId, componentId, messageInfo.id(), messageInfo.crc(), payload);
            } else if (mavlinkVersion == 2) {
                //生成2版本的
                //协议只在序列化消息有效负载的末尾截断空字节；有效负载正文中的任何空字节/空字段都不受影响。
                int len = payload.length;
                for (int i = payload.length - 1; i > 0; i--) {
                    if (payload[i] == 0) {
                        len--;
                    } else {
                        break;
                    }
                }
                //mavlink第二版的协议，需要去掉payload末尾的空字节
                byte[] changedPayload = new byte[len];
                System.arraycopy(payload, 0, changedPayload, 0, len);
                mavlinkPacket = MavlinkPacket.createUnsignedMavlink2Packet(sequence, systemId, componentId, messageInfo.id(), messageInfo.crc(), changedPayload);
                //mavlinkPacket = MavlinkPacket.createUnsignedMavlink2Packet(sequence, systemId, componentId, messageInfo.id(), messageInfo.crc(), payload);
            }
            rt = mavlinkPacket.getRawBytes();
        } catch (Exception e) {
            e.printStackTrace();
            rt = null;
        }
        return rt;
    }

    public static Object rawBytesToPayloadObject(byte[] rawBytes) {
        Object rt = null;
        try {
            int stx = (rawBytes[0] & 0xff);
            MavlinkPacket mavlinkPacket = null;
            if (stx == 0xfe) {
                // mavlink第一版协议
                mavlinkPacket = MavlinkPacket.fromV1Bytes(rawBytes);
            } else if (stx == 0xfd) {
                // mavlink第二版协议
                mavlinkPacket = MavlinkPacket.fromV2Bytes(rawBytes);
            }
            if (mavlinkPacket == null) {
                return null;
            }
            CommonDialect dialect = new CommonDialect();
            ReflectionPayloadDeserializer deserializer = new ReflectionPayloadDeserializer();
            Class<?> messageType = dialect.resolve(mavlinkPacket.getMessageId());
            if (messageType == null) {
                return null;
            }
            rt = deserializer.deserialize(mavlinkPacket.getPayload(), messageType);
        } catch (Exception e) {
            e.printStackTrace();
            rt = null;
        }
        return rt;
    }


    public static Object packetToPayloadObject(MavlinkPacket mavlinkPacket) {
        Object rt = null;
        try {
            CommonDialect dialect = new CommonDialect();
            ReflectionPayloadDeserializer deserializer = new ReflectionPayloadDeserializer();
            Class<?> messageType = dialect.resolve(mavlinkPacket.getMessageId());
            if (messageType == null) {
                return null;
            }
            byte[] changedPayload = mavlinkPacket.getPayload();
            if (mavlinkPacket.isMavlink2()) {
                //如果是mavlink2协议，需要将payload部分，补充0，进行反序列化
                Object builder = Arrays.stream(messageType.getMethods())
                        .filter(m -> m.isAnnotationPresent(MavlinkMessageBuilder.class))
                        .findFirst()
                        .orElseThrow(() -> new MavlinkSerializationException(
                                "Message " + messageType.getName() + " does not have a builder"))
                        .invoke(null);
                Object payloadObject = builder.getClass().getMethod("build").invoke(builder);
                ReflectionPayloadSerializer serializer = new ReflectionPayloadSerializer();
                byte[] payload = serializer.serialize(payloadObject);
                //获得实际对象的payload长度，新建一个数组填充0
                changedPayload = new byte[payload.length];
                System.arraycopy(mavlinkPacket.getPayload(), 0, changedPayload, 0, mavlinkPacket.getPayload().length);
            }
            rt = deserializer.deserialize(changedPayload, messageType);
        } catch (Exception e) {
            e.printStackTrace();
            rt = null;
        }
        return rt;
    }

    public static MavlinkPacket payloadObjectToPacket(Object payloadObject, int sequence, int systemId, int componentId, int mavlinkVersion) {
        MavlinkPacket mavlinkPacket = null;
        try {
            MavlinkMessageInfo messageInfo = (MavlinkMessageInfo) payloadObject.getClass().getAnnotation(MavlinkMessageInfo.class);
            ReflectionPayloadSerializer serializer = new ReflectionPayloadSerializer();
            byte[] payload = serializer.serialize(payloadObject);

            mavlinkPacket = null;
            if (mavlinkVersion == 1) {
                //生成1版本的
                mavlinkPacket = MavlinkPacket.createMavlink1Packet(sequence, systemId, componentId, messageInfo.id(), messageInfo.crc(), payload);
            } else if (mavlinkVersion == 2) {
                //生成2版本的
                //协议只在序列化消息有效负载的末尾截断空字节；有效负载正文中的任何空字节/空字段都不受影响。
                int len = payload.length;
                for (int i = payload.length - 1; i > 0; i--) {
                    if (payload[i] == 0) {
                        len--;
                    } else {
                        break;
                    }
                }
                //mavlink第二版的协议，需要去掉payload末尾的空字节
                byte[] changedPayload = new byte[len];
                System.arraycopy(payload, 0, changedPayload, 0, len);
                mavlinkPacket = MavlinkPacket.createUnsignedMavlink2Packet(sequence, systemId, componentId, messageInfo.id(), messageInfo.crc(), changedPayload);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mavlinkPacket = null;
        }
        return mavlinkPacket;
    }


}
