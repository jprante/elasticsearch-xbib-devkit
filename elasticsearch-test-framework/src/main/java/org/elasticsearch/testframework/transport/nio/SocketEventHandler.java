package org.elasticsearch.testframework.transport.nio;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.testframework.transport.nio.channel.NioChannel;
import org.elasticsearch.testframework.transport.nio.channel.NioSocketChannel;
import org.elasticsearch.testframework.transport.nio.channel.SelectionKeyUtils;
import org.elasticsearch.testframework.transport.nio.channel.WriteContext;

import java.io.IOException;

/**
 * Event handler designed to handle events from non-server sockets
 */
public class SocketEventHandler extends EventHandler {

    private final Logger logger;

    public SocketEventHandler(Logger logger) {
        super(logger);
        this.logger = logger;
    }

    /**
     * This method is called when a NioSocketChannel is successfully registered. It should only be called
     * once per channel.
     *
     * @param channel that was registered
     */
    public void handleRegistration(NioSocketChannel channel) {
        SelectionKeyUtils.setConnectAndReadInterested(channel);
    }

    /**
     * This method is called when an attempt to register a channel throws an exception.
     *
     * @param channel that was registered
     * @param exception that occurred
     */
    public void registrationException(NioSocketChannel channel, Exception exception) {
        logger.debug(() -> new ParameterizedMessage("failed to register socket channel: {}", channel), exception);
        exceptionCaught(channel, exception);
    }

    /**
     * This method is called when a NioSocketChannel is successfully connected. It should only be called
     * once per channel.
     *
     * @param channel that was registered
     */
    public void handleConnect(NioSocketChannel channel) {
        SelectionKeyUtils.removeConnectInterested(channel);
    }

    /**
     * This method is called when an attempt to connect a channel throws an exception.
     *
     * @param channel that was connecting
     * @param exception that occurred
     */
    public void connectException(NioSocketChannel channel, Exception exception) {
        logger.debug(() -> new ParameterizedMessage("failed to connect to socket channel: {}", channel), exception);
        exceptionCaught(channel, exception);
    }

    /**
     * This method is called when a channel signals it is ready for be read. All of the read logic should
     * occur in this call.
     *
     * @param channel that can be read
     */
    public void handleRead(NioSocketChannel channel) throws IOException {
        int bytesRead = channel.getReadContext().read();
        if (bytesRead == -1) {
            handleClose(channel);
        }
    }

    /**
     * This method is called when an attempt to read from a channel throws an exception.
     *
     * @param channel that was being read
     * @param exception that occurred
     */
    public void readException(NioSocketChannel channel, Exception exception) {
        logger.debug(() -> new ParameterizedMessage("exception while reading from socket channel: {}", channel), exception);
        exceptionCaught(channel, exception);
    }

    /**
     * This method is called when a channel signals it is ready to receive writes. All of the write logic
     * should occur in this call.
     *
     * @param channel that can be read
     */
    public void handleWrite(NioSocketChannel channel) throws IOException {
        WriteContext channelContext = channel.getWriteContext();
        channelContext.flushChannel();
        if (channelContext.hasQueuedWriteOps()) {
            SelectionKeyUtils.setWriteInterested(channel);
        } else {
            SelectionKeyUtils.removeWriteInterested(channel);
        }
    }

    /**
     * This method is called when an attempt to write to a channel throws an exception.
     *
     * @param channel that was being written to
     * @param exception that occurred
     */
    public void writeException(NioSocketChannel channel, Exception exception) {
        logger.debug(() -> new ParameterizedMessage("exception while writing to socket channel: {}", channel), exception);
        exceptionCaught(channel, exception);
    }

    /**
     * This method is called when handling an event from a channel fails due to an unexpected exception.
     * An example would be if checking ready ops on a {@link java.nio.channels.SelectionKey} threw
     * {@link java.nio.channels.CancelledKeyException}.
     *
     * @param channel that caused the exception
     * @param exception that was thrown
     */
    public void genericChannelException(NioChannel channel, Exception exception) {
        super.genericChannelException(channel, exception);
        exceptionCaught((NioSocketChannel) channel, exception);
    }

    /**
     * This method is called when a listener attached to a channel operation throws an exception.
     *
     * @param listener that was called
     * @param exception that occurred
     */
    public <V> void listenerException(ActionListener<V> listener, Exception exception) {
        logger.warn(new ParameterizedMessage("exception while executing listener: {}", listener), exception);
    }

    private void exceptionCaught(NioSocketChannel channel, Exception e) {
        channel.getExceptionContext().accept(channel, e);
    }
}