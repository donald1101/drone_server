package com.hkxx.mq;

import com.hkxx.drone.Config;
import com.hkxx.drone.Program;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.InputStream;
import java.util.Properties;


public class RabbitUtils {
    public static Channel getChannel() throws Exception {

        Properties prop = new Properties();
        InputStream config = Program.class.getResourceAsStream("/set.properties");
        prop.load(config);

        Config.Host = prop.getProperty("Host");
        Config.Port = Integer.parseInt(prop.getProperty("Port"));
        Config.Username = prop.getProperty("Username");
        Config.Password = prop.getProperty("Password");
        Config.VirtualHost = prop.getProperty("VirtualHost");

        ConnectionFactory factory = new ConnectionFactory();

        factory.setHost(Config.Host);
        factory.setPort(Config.Port);
        factory.setUsername(Config.Username);
        factory.setPassword(Config.Password);
        factory.setVirtualHost(Config.VirtualHost);

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        return channel;
    }
}
