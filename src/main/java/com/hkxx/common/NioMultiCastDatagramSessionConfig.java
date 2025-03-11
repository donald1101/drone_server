package com.hkxx.drone.common;

import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.transport.socket.AbstractDatagramSessionConfig;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.channels.DatagramChannel;

public class NioMultiCastDatagramSessionConfig extends AbstractDatagramSessionConfig {
    /**
     * The associated channel
     */
    private final DatagramChannel channel;

    private InetAddress group;
    private NetworkInterface networkInterface;

    /**
     * Creates a new instance of NioDatagramSessionConfig, associated
     * with the given DatagramChannel.
     *
     * @param channel The associated DatagramChannel
     */
    NioMultiCastDatagramSessionConfig(DatagramChannel channel) {
        this.channel = channel;
    }

    /**
     * Get the Socket receive buffer size for this DatagramChannel.
     *
     * @return the DatagramChannel receive buffer size.
     * @throws RuntimeIoException if the socket is closed or if we
     *                            had a SocketException
     * @see DatagramSocket#getReceiveBufferSize()
     */
    @Override
    public int getReceiveBufferSize() {
        try {
            return channel.socket().getReceiveBufferSize();
        } catch (SocketException e) {
            throw new RuntimeIoException(e);
        }
    }

    /**
     * Set the Socket receive buffer size for this DatagramChannel. <br>
     * <br>
     * Note : The underlying Socket may not accept the new buffer's size.
     * The user has to check that the new value has been set.
     *
     * @param receiveBufferSize the DatagramChannel receive buffer size.
     * @throws RuntimeIoException if the socket is closed or if we
     *                            had a SocketException
     * @see DatagramSocket#setReceiveBufferSize(int)
     */
    @Override
    public void setReceiveBufferSize(int receiveBufferSize) {
        try {
            channel.socket().setReceiveBufferSize(receiveBufferSize);
        } catch (SocketException e) {
            throw new RuntimeIoException(e);
        }
    }

    /**
     * Tells if SO_BROADCAST is enabled.
     *
     * @return <code>true</code> if SO_BROADCAST is enabled
     * @throws RuntimeIoException If the socket is closed or if we get an
     *                            {@link SocketException}
     */
    @Override
    public boolean isBroadcast() {
        try {
            return channel.socket().getBroadcast();
        } catch (SocketException e) {
            throw new RuntimeIoException(e);
        }
    }

    @Override
    public void setBroadcast(boolean broadcast) {
        try {
            channel.socket().setBroadcast(broadcast);
        } catch (SocketException e) {
            throw new RuntimeIoException(e);
        }
    }

    /**
     * @throws RuntimeIoException If the socket is closed or if we get an
     *                            {@link SocketException}
     */
    @Override
    public int getSendBufferSize() {
        try {
            return channel.socket().getSendBufferSize();
        } catch (SocketException e) {
            throw new RuntimeIoException(e);
        }
    }

    /**
     * @throws RuntimeIoException If the socket is closed or if we get an
     *                            {@link SocketException}
     */
    @Override
    public void setSendBufferSize(int sendBufferSize) {
        try {
            channel.socket().setSendBufferSize(sendBufferSize);
        } catch (SocketException e) {
            throw new RuntimeIoException(e);
        }
    }

    /**
     * Tells if SO_REUSEADDR is enabled.
     *
     * @return <code>true</code> if SO_REUSEADDR is enabled
     * @throws RuntimeIoException If the socket is closed or if we get an
     *                            {@link SocketException}
     */
    @Override
    public boolean isReuseAddress() {
        try {
            return channel.socket().getReuseAddress();
        } catch (SocketException e) {
            throw new RuntimeIoException(e);
        }
    }

    /**
     * @throws RuntimeIoException If the socket is closed or if we get an
     *                            {@link SocketException}
     */
    @Override
    public void setReuseAddress(boolean reuseAddress) {
        try {
            channel.socket().setReuseAddress(reuseAddress);
        } catch (SocketException e) {
            throw new RuntimeIoException(e);
        }
    }

    /**
     * Get the current Traffic Class for this Socket, if any. As this is
     * not a mandatory feature, the returned value should be considered as
     * a hint.
     *
     * @return The Traffic Class supported by this Socket
     * @throws RuntimeIoException If the socket is closed or if we get an
     *                            {@link SocketException}
     */
    @Override
    public int getTrafficClass() {
        try {
            return channel.socket().getTrafficClass();
        } catch (SocketException e) {
            throw new RuntimeIoException(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws RuntimeIoException If the socket is closed or if we get an
     *                            {@link SocketException}
     */
    @Override
    public void setTrafficClass(int trafficClass) {
        try {
            channel.socket().setTrafficClass(trafficClass);
        } catch (SocketException e) {
            throw new RuntimeIoException(e);
        }
    }

    public InetAddress getGroup() {
        return group;
    }

    public void setGroup(InetAddress group) {
        this.group = group;
    }

    public NetworkInterface getNetworkInterface() {
        return networkInterface;
    }

    public void setNetworkInterface(NetworkInterface networkInterface) {
        this.networkInterface = networkInterface;
    }

    public void setMultiCastOption() {
        try {
            //NetworkInterface networkInterface = NetworkInterface.getByInetAddress(InetAddress.getByName("127.0.0.1"));
            /*添加到组播*/
            if (group != null && networkInterface != null) {
                channel.join(group, networkInterface);
            }
        } catch (Exception e) {
            throw new RuntimeIoException(e);
        }
    }

    @Override
    public void setAll(IoSessionConfig config) {
        super.setAll(config);
        setMultiCastOption();
    }
}
