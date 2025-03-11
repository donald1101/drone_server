package com.hkxx.drone.common;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioDatagramConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class UdpClient {

    private static Logger log = LoggerFactory.getLogger(UdpClient.class); // 日志对象
    private NioDatagramConnector connector = new NioDatagramConnector(); // UDP客户端连接对象

    // 调用start之前，需要初始化的参数
    String serverIP = "127.0.0.1"; // 服务器IP地址
    int readBufSize = 4096000; // Session读缓冲区大小
    int idleTime = 10 * 60; // Session进入空闲状态的时间间隔，单位秒
    int connectTimeout = 30; // 连接超时时间，单位秒
    int serverPort = 60000; // 服务器端口号
    IoFilter filter = null; // 编解码，过滤器
    IoHandler handler = null;// 处理器
    IoSession session = null; // 客户端连接会话对象

    // 初始化参数
    public void initial() {
        try {
            // 设置session的读缓冲区大小
            connector.getSessionConfig().setReadBufferSize(readBufSize);
            // 设置session的进入空闲时间间隔
            connector.getSessionConfig().setIdleTime(IdleStatus.READER_IDLE,
                    idleTime);
            connector.setConnectTimeoutMillis(connectTimeout * 1000); // 设置连接超时时间
            // 加载编解码，过滤器
            if (filter != null) {
                connector.getFilterChain().addLast("codec", filter);
            }
            // 加载处理器
            if (handler != null) {
                connector.setHandler(handler);
            }
        } catch (Exception e) {
            // TODO: handle exception
            log.error(e.getMessage());
        }
    }

    // 连接服务器
    public void connect() {
        try {

            // 连接服务器
            ConnectFuture cf = connector.connect(new InetSocketAddress(
                    serverIP, serverPort));
            log.info("Connecting UDP Server...IP Address:" + serverIP
                    + " Port:" + serverPort);
            // 等待连接创建完成
            cf.awaitUninterruptibly();
            session = cf.getSession();
            // cf.getSession().getCloseFuture().awaitUninterruptibly();
            // connector.dispose();
            log.info("Connected successfully");

        } catch (Exception e) {
            // TODO: handle exception
            log.error(e.getMessage());
        }
    }

    // 关闭连接
    public void close() {
        try {
            if (connector != null) {
                //session.close();
                // connector.dispose();
                session.close(true);
                session = null;
            }

        } catch (Exception e) {
            // TODO: handle exception
            log.error(e.getMessage());
        }
    }

    // 释放资源
    public void dispose() {
        try {
            if (connector != null) {
                // session.close();
                connector.dispose();
            }

        } catch (Exception e) {
            // TODO: handle exception
            log.error(e.getMessage());
        }
    }

    // 发送数据
    public void send(Object data) {
        try {
            if (session != null) {
                session.write(data);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public NioDatagramConnector getConnector() {
        return connector;
    }

    public void setConnector(NioDatagramConnector connector) {
        this.connector = connector;
    }

    public String getServerIP() {
        return serverIP;
    }

    public void setServerIP(String value) {
        serverIP = value;
    }

    public int getReadBufSize() {
        return readBufSize;
    }

    public void setReadBufSize(int value) {
        readBufSize = value;
    }

    public int getIdleTime() {
        return idleTime;
    }

    public void setIdleTime(int value) {
        idleTime = value;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int value) {
        serverPort = value;
    }

    public IoFilter getFilter() {
        return filter;
    }

    public void setFilter(IoFilter value) {
        filter = value;
    }

    public IoHandler getHandler() {
        return handler;
    }

    public void setHandler(IoHandler value) {
        handler = value;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int value) {
        connectTimeout = value;
    }

    public IoSession getSession() {
        return session;
    }

}
