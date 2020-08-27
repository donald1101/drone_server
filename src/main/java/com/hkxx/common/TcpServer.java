package com.hkxx.common;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class TcpServer {

	private static Logger log = LoggerFactory.getLogger(TcpServer.class); // 日志对象
	private NioSocketAcceptor acceptor = new NioSocketAcceptor(); // TCP服务器对象

	// 调用start之前，需要初始化的参数
	String name = "local"; // TCP服务器对象名称
	int readBufSize = 4096000; // Session读缓冲区大小
	int idleTime = 10 * 60; // Session进入空闲状态的时间间隔，单位秒
	int serverPort = 60000; // TCP服务器端口号
	IoFilter filter = null; // 编解码，过滤器
	IoHandler handler = null;// 处理器

	// 启动TCP服务器
	public void start() {
		try {
			// 设置session的读缓冲区大小
			acceptor.getSessionConfig().setReadBufferSize(readBufSize);
			// 设置session的进入空闲时间间隔
			acceptor.getSessionConfig().setIdleTime(IdleStatus.READER_IDLE,
					idleTime);
			// 加载编解码，过滤器
			if (filter != null) {
				acceptor.getFilterChain().addLast("codec", filter);
			}
			// 加载处理器
			if (handler != null) {
				acceptor.setHandler(handler);
			}
			// 绑定端口，并启动服务器
			acceptor.bind(new InetSocketAddress(serverPort));
			log.info("TCP Server Started.Name:" + name + " Port:" + serverPort);
		} catch (Exception e) {
			// TODO: handle exception
			log.error(e.getMessage());
		}
	}

	// 停止TCP服务器
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

	public NioSocketAcceptor getAcceptor() {
		return acceptor;
	}

	public void setAcceptor(NioSocketAcceptor value) {
		acceptor = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String value) {
		name = value;
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

}
