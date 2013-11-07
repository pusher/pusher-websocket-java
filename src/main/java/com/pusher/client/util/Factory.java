package com.pusher.client.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.net.ssl.SSLException;

import org.java_websocket.client.WebSocketClient;

import com.pusher.client.Authorizer;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.impl.ChannelImpl;
import com.pusher.client.channel.impl.ChannelManager;
import com.pusher.client.channel.impl.PresenceChannelImpl;
import com.pusher.client.channel.impl.PrivateChannelImpl;
import com.pusher.client.connection.impl.InternalConnection;
import com.pusher.client.connection.websocket.WebSocketClientWrapper;
import com.pusher.client.connection.websocket.WebSocketConnection;
import com.pusher.client.connection.websocket.WebSocketListener;

/**
 * This is a lightweight way of doing dependency injection and enabling classes
 * to be unit tested in isolation. No class in this library instantiates another
 * class directly, otherwise they would be tightly coupled. Instead, they all
 * call the factory methods in this class when they want to create instances of
 * another class.
 *
 * An instance of Factory is provided on construction to each class which may
 * require it, the initial factory is instantiated in the Pusher constructor,
 * the only constructor which a library consumer should need to call directly.
 *
 * Conventions:
 *
 * - any method that starts with "new", such as
 * {@link #newPublicChannel(String)} creates a new instance of that class every
 * time it is called.
 *
 * - any method that starts with "get", such as {@link #getEventQueue()} returns
 * a singleton.
 */
public class Factory {

    private InternalConnection connection;
    private ChannelManager channelManager;
    private ScheduledExecutorService eventQueue;

    public InternalConnection getConnection(String apiKey, PusherOptions options) {
        if (connection == null) {
            try {
                connection = new WebSocketConnection(options.buildUrl(apiKey),
                                                     options.getActivityTimeout(),
                                                     options.getPongTimeout(),
                                                     this);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Failed to initialise connection", e);
            }
        }
        return connection;
    }

    public WebSocketClient newWebSocketClientWrapper(URI uri, WebSocketListener proxy) throws SSLException {
        return new WebSocketClientWrapper(uri, proxy);
    }

    public ScheduledExecutorService getEventQueue() {
        if (eventQueue == null) {
            eventQueue = Executors.newSingleThreadScheduledExecutor();
        }
        return eventQueue;
    }

    public ChannelImpl newPublicChannel(String channelName) {
        return new ChannelImpl(channelName, this);
    }

    public PrivateChannelImpl newPrivateChannel(InternalConnection connection, String channelName, Authorizer authorizer) {
        return new PrivateChannelImpl(connection, channelName, authorizer, this);
    }

    public PresenceChannelImpl newPresenceChannel(InternalConnection connection, String channelName,
            Authorizer authorizer) {
        return new PresenceChannelImpl(connection, channelName, authorizer, this);
    }

    public ChannelManager getChannelManager() {
        if (channelManager == null) {
            channelManager = new ChannelManager(this);
        }
        return channelManager;
    }

    public long timeNow() {
        return System.currentTimeMillis();
    }
}