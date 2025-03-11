package com.hkxx.drone.common;

import org.apache.mina.core.filterchain.DefaultIoFilterChain;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.session.AbstractIoSession;
import org.apache.mina.core.session.IoSession;

import java.nio.channels.ByteChannel;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;

public abstract class NioMultiCastSession extends AbstractIoSession {
    /**
     * The NioMultiCastSession processor
     */
    protected final IoProcessor<NioMultiCastSession> processor;

    /**
     * The communication channel
     */
    protected final Channel channel;

    /**
     * The SelectionKey used for this session
     */
    private SelectionKey key;

    /**
     * The FilterChain created for this session
     */
    private final IoFilterChain filterChain;

    /**
     * Creates a new instance of NioMultiCastSession, with its associated IoProcessor.
     * <br>
     * This method is only called by the inherited class.
     *
     * @param processor The associated {@link IoProcessor}
     * @param service   The associated {@link IoService}
     * @param channel   The associated {@link Channel}
     */
    protected NioMultiCastSession(IoProcessor<NioMultiCastSession> processor, IoService service, Channel channel) {
        super(service);
        this.channel = channel;
        this.processor = processor;
        filterChain = new DefaultIoFilterChain(this);
    }

    /**
     * @return The ByteChannel associated with this {@link IoSession}
     */
    public abstract ByteChannel getChannel();

    /**
     * {@inheritDoc}
     */
    @Override
    public IoFilterChain getFilterChain() {
        return filterChain;
    }

    /**
     * @return The {@link SelectionKey} associated with this {@link IoSession}
     */
    /* No qualifier*/
    public SelectionKey getSelectionKey() {
        return key;
    }

    /**
     * Sets the {@link SelectionKey} for this {@link IoSession}
     *
     * @param key The new {@link SelectionKey}
     */
    /* No qualifier*/
    public void setSelectionKey(SelectionKey key) {
        this.key = key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IoProcessor<NioMultiCastSession> getProcessor() {
        return processor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isActive() {
        return key.isValid();
    }
}
