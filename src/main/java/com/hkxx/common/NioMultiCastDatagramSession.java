package com.hkxx.drone.common;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.DefaultTransportMetadata;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.transport.socket.DatagramSessionConfig;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;

public class NioMultiCastDatagramSession extends NioMultiCastSession {
    static final TransportMetadata METADATA = new DefaultTransportMetadata("nio", "MultiCastDatagram", true, false,
            InetSocketAddress.class, DatagramSessionConfig.class, IoBuffer.class);

    private final InetSocketAddress localAddress;

    private final InetSocketAddress remoteAddress;

    /**
     * Creates a new acceptor-side session instance.
     */
    NioMultiCastDatagramSession(IoService service, DatagramChannel channel, IoProcessor<NioMultiCastSession> processor,
                                SocketAddress remoteAddress) {
        super(processor, service, channel);
        config = new NioMultiCastDatagramSessionConfig(channel);
        config.setAll(service.getSessionConfig());
        this.remoteAddress = (InetSocketAddress) remoteAddress;
        this.localAddress = (InetSocketAddress) channel.socket().getLocalSocketAddress();
    }

    /**
     * Creates a new connector-side session instance.
     */
    NioMultiCastDatagramSession(IoService service, DatagramChannel channel, IoProcessor<NioMultiCastSession> processor) {
        this(service, channel, processor, channel.socket().getRemoteSocketAddress());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DatagramSessionConfig getConfig() {
        return (DatagramSessionConfig) config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DatagramChannel getChannel() {
        return (DatagramChannel) channel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransportMetadata getTransportMetadata() {
        return METADATA;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InetSocketAddress getServiceAddress() {
        return (InetSocketAddress) super.getServiceAddress();
    }
}
