package com.hkxx.common;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class MultiCastUdpServer {
    private static Logger log = LoggerFactory.getLogger(MultiCastUdpServer.class); // 日志对象
    private NioMultiCastDatagramAcceptor acceptor = new NioMultiCastDatagramAcceptor(); // UDP服务器对象

    // 调用start之前，需要初始化的参数
    String name = "local"; // UDP服务器对象名称
    int recvBufSize = 4096000; // Session读缓冲区大小
    int idleTime = 10 * 60; // Session进入空闲状态的时间间隔，单位秒
    int serverPort = 60000; // UDP服务器端口号
    IoFilter filter = null; // 编解码，过滤器
    IoHandler handler = null;// 处理器
    String group;
    String networkInterface;

    // 启动UDP服务器
    public void start() {
        try {
            // 设置session的读缓冲区大小
            acceptor.getSessionConfig().setMinReadBufferSize(64 * 1024);
            // acceptor.getSessionConfig().setReadBufferSize(recvBufSize);
            acceptor.getSessionConfig().setReceiveBufferSize(recvBufSize);
            // 设置session的进入空闲时间间隔
            acceptor.getSessionConfig().setIdleTime(IdleStatus.READER_IDLE,
                    idleTime);
            acceptor.getSessionConfig().setBroadcast(true);
            acceptor.getSessionConfig().setReuseAddress(true);
            acceptor.setGroup(group);
            acceptor.setNetworkInterface(networkInterface);
            // 加载编解码，过滤器
            if (filter != null) {
                acceptor.getFilterChain().addLast("codec", filter);
            }
            // 加载处理器
            if (handler != null) {
                acceptor.setHandler(handler);
            }
            //MulticastSocketProvider provider = new MulticastSocketProvider("224.0.1.100", 9999);
            // 绑定端口，并启动服务器

            ////测试udp组播接收
            //acceptor.bind(new InetSocketAddress("234.186.3.1", serverPort));


            acceptor.bind(new InetSocketAddress(serverPort));
            // log.info("MinReadBufSize:"
            // + acceptor.getSessionConfig().getMinReadBufferSize());
            // log.info("ReadBufferSize:"
            // + acceptor.getSessionConfig().getReadBufferSize());
            // log.info("ReceiveBufferSize:"
            // + acceptor.getSessionConfig().getReceiveBufferSize());
            // log.info("SendBufferSize:"
            // + acceptor.getSessionConfig().getSendBufferSize());
            log.info("MultiCast UDP Server Started.Name:" + name + " Port:" + serverPort);
        } catch (Exception e) {
            // TODO: handle exception
            log.error(e.getMessage());
        }
    }

    // 停止UDP服务器
    public void stop() {
        try {
            if (acceptor != null) {
                acceptor.unbind();
                acceptor.dispose();
            }

        } catch (Exception e) {
            // TODO: handle exception
            log.error(e.getMessage());
        }
    }

    public NioMultiCastDatagramAcceptor getAcceptor() {
        return acceptor;
    }

    public void setAcceptor(NioMultiCastDatagramAcceptor value) {
        acceptor = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    public int getReadBufSize() {
        return recvBufSize;
    }

    public void setReadBufSize(int value) {
        recvBufSize = value;
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

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getNetworkInterface() {
        return networkInterface;
    }

    public void setNetworkInterface(String networkInterface) {
        this.networkInterface = networkInterface;
    }
}
