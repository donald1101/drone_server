package com.hkxx.drone.joystick;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class CommandResultHandler extends IoHandlerAdapter {

    private static Logger log = LoggerFactory.getLogger(CommandResultHandler.class);
    int tsWait = 5000; // 等待信号量事件通知的时间，单位毫秒
    private SessionTimeUpdatedListener sessionTimeUpdatedListener = null;

    @Override
    public void exceptionCaught(IoSession session, Throwable cause)
            throws Exception {
        // TODO Auto-generated method stub
        super.exceptionCaught(session, cause);
    }

    @Override
    public void messageReceived(IoSession session, Object message)
            throws Exception {
        // TODO Auto-generated method stub
        // super.messageReceived(session, message);
        try {
            IoBuffer recv = (IoBuffer) message;
            // 将IBuffer转换为字节数组
            byte[] data = new byte[recv.remaining()];
            recv.get(data, 0, data.length);
            String msg = "";

            // 采用ASCII解码
            msg = new String(data, StandardCharsets.UTF_8);
            log.info("Recv:" + msg);
            if (sessionTimeUpdatedListener != null) {
                sessionTimeUpdatedListener.onSessionTimeUpdated();
            }
        } catch (Exception e) {
            // TODO: handle exception
            log.error(e.getMessage());
        }

    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        // TODO Auto-generated method stub
        super.messageSent(session, message);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        // TODO Auto-generated method stub
        super.sessionClosed(session);
        InetSocketAddress remote = (InetSocketAddress) session
                .getRemoteAddress();
        log.info("Session closed." + remote.getHostString() + ":"
                + remote.getPort());
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        // TODO Auto-generated method stub
        super.sessionCreated(session);
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status)
            throws Exception {
        // TODO Auto-generated method stub
        super.sessionIdle(session, status);
        // InetSocketAddress remote = (InetSocketAddress) session
        // .getRemoteAddress();
        // log.info("IdleStatus changed.Status:" + status.toString() + " "
        // + remote.getHostString() + "：" + remote.getPort());
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        // TODO Auto-generated method stub
        // super.sessionOpened(session);
        InetSocketAddress remote = (InetSocketAddress) session
                .getRemoteAddress();
        log.info("Session opened." + remote.getHostString() + ":"
                + remote.getPort());
    }


    public SessionTimeUpdatedListener getSessionTimeUpdatedListener() {
        return sessionTimeUpdatedListener;
    }

    public void setSessionTimeUpdatedListener(SessionTimeUpdatedListener sessionTimeUpdatedListener) {
        this.sessionTimeUpdatedListener = sessionTimeUpdatedListener;
    }

    public interface SessionTimeUpdatedListener {
        public void onSessionTimeUpdated();
    }
}

