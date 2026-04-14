package com.hkxx.mq;

import com.hkxx.drone.Config;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RabbitConsumer {
    private static Logger log = LoggerFactory.getLogger(RabbitConsumer.class);
    private Channel channel = RabbitUtils.getChannel();
    private String message = "";

    public String getMessage() {
        return message;
    }

    public RabbitConsumer() throws Exception {
    }

    public void init() throws Exception {
        log.info("RabbitMQ is waiting for messages ......");
        channel.exchangeDeclare(Config.FROM_EXCHANGE_NAME, "fanout");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, Config.FROM_EXCHANGE_NAME, "");
        this.messageRecv(queueName);
    }

    public void messageRecv(String queueName) throws IOException {
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            returnMessage(message);
            System.out.println(" [*Rx] Received message : '" + message + "'");
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }

    public void returnMessage(String message){
        this.message = message;
    }

}