package com.hkxx.mq;

import com.hkxx.drone.Config;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class RabbitProducer {

    private final Channel channel = RabbitUtils.getChannel();

    public RabbitProducer() throws Exception {
    }

    public void init() throws Exception {
        channel.exchangeDeclare(Config.TO_EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
    }

    public void messageSend(String message) throws Exception {
        channel.basicPublish(Config.TO_EXCHANGE_NAME,"",null,message.getBytes("UTF-8"));
        System.out.println("[*Tx] send message : " + message);
    }
}
