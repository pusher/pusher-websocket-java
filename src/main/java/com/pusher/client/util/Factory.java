package com.pusher.client.util;

import com.pusher.client.ChannelAuthorizer;
import com.pusher.client.PusherOptions;
import com.pusher.client.UserAuthenticator;
import com.pusher.client.channel.impl.ChannelImpl;
import com.pusher.client.channel.impl.ChannelManager;
import com.pusher.client.channel.impl.PresenceChannelImpl;
import com.pusher.client.channel.impl.PrivateChannelImpl;
import com.pusher.client.channel.impl.PrivateEncryptedChannelImpl;
import com.pusher.client.connection.impl.InternalConnection;
import com.pusher.client.connection.websocket.WebSocketClientWrapper;
import com.pusher.client.connection.websocket.WebSocketConnection;
import com.pusher.client.connection.websocket.WebSocketListener;
import com.pusher.client.crypto.nacl.SecretBoxOpenerFactory;
import com.pusher.client.user.impl.InternalUser;

import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import javax.net.ssl.SSLException;

/**
 * This is a lightweight way of doing dependency injection and enabling classes
 * to be unit tested in isolation. No class in this library instantiates another
 * class directly, otherwise they would be tightly coupled. Instead, they all
 * call the factory methods in this class when they want to create instances of
 * another class.
 * <p>
 * An instance of Factory is provided on construction to each class which may
 * require it, the initial factory is instantiated in the Pusher constructor,
 * the only constructor which a library consumer should need to call directly.
 * <p>
 * Conventions:
 * <p>
 * - any method that starts with "new", such as
 * {@link #newPublicChannel(String)} creates a new instance of that class every
 * time it is called.
 */
public class Factory {

    private InternalConnection connection;
    private ChannelManager channelManager;
    private ExecutorService eventQueue;
    private ScheduledExecutorService timers;
    private static final Object eventLock = new Object();

    public synchronized InternalConnection getConnection(
            final String apiKey,
            final PusherOptions options,
            final PusherEventHandler eventHandler
    ) {
        if (connection == null) {
            try {
                connection =
                        new WebSocketConnection(
                                options.buildUrl(apiKey),
                                options.getActivityTimeout(),
                                options.getPongTimeout(),
                                options.getMaxReconnectionAttempts(),
                                options.getMaxReconnectGapInSeconds(),
                                options.getProxy(),
                                eventHandler,
                                this
                        );
            } catch (final URISyntaxException e) {
                throw new IllegalArgumentException("Failed to initialise connection", e);
            }
        }
        return connection;
    }

    public WebSocketClientWrapper newWebSocketClientWrapper(
            final URI uri,
            final Proxy proxy,
            final WebSocketListener webSocketListener
    ) throws SSLException {
        return new WebSocketClientWrapper(uri, proxy, webSocketListener);
    }

    public synchronized ScheduledExecutorService getTimers() {
        if (timers == null) {
            timers = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory("timers"));
        }
        return timers;
    }

    public ChannelImpl newPublicChannel(final String channelName) {
        return new ChannelImpl(channelName, this);
    }

    public PrivateChannelImpl newPrivateChannel(
            final InternalConnection connection,
            final String channelName,
            final ChannelAuthorizer channelAuthorizer
    ) {
        return new PrivateChannelImpl(connection, channelName, channelAuthorizer, this);
    }

    public PrivateEncryptedChannelImpl newPrivateEncryptedChannel(
            final InternalConnection connection,
            final String channelName,
            final ChannelAuthorizer channelAuthorizer
    ) {
        return new PrivateEncryptedChannelImpl(connection, channelName, channelAuthorizer, this, new SecretBoxOpenerFactory());
    }

    public PresenceChannelImpl newPresenceChannel(
            final InternalConnection connection,
            final String channelName,
            final ChannelAuthorizer channelAuthorizer
    ) {
        return new PresenceChannelImpl(connection, channelName, channelAuthorizer, this);
    }

    public InternalUser newUser(InternalConnection connection, UserAuthenticator userAuthenticator) {
        return new InternalUser(connection, userAuthenticator, this);
    }

    public synchronized ChannelManager getChannelManager() {
        if (channelManager == null) {
            channelManager = new ChannelManager(this);
        }
        return channelManager;
    }

    public synchronized void queueOnEventThread(final Runnable r) {
        if (eventQueue == null) {
            eventQueue = Executors.newSingleThreadExecutor(new DaemonThreadFactory("eventQueue"));
        }
        eventQueue.execute(() -> {
            synchronized (eventLock) {
                r.run();
            }
        });
    }

    public synchronized void shutdownThreads() {
        if (eventQueue != null) {
            eventQueue.shutdown();
            eventQueue = null;
        }
        if (timers != null) {
            timers.shutdown();
            timers = null;
        }
    }

    private static class DaemonThreadFactory implements ThreadFactory {

        private final String name;

        public DaemonThreadFactory(final String name) {
            this.name = name;
        }

        @Override
        public Thread newThread(final Runnable r) {
            final Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("pusher-java-client " + name);
            return t;
        }
    }
}
