package com.pusher.client.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLException;

import com.pusher.client.PusherOptions;
import org.java_websocket.client.WebSocketClient;

import com.pusher.client.Authorizer;
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
 * When unit testing we can use PowerMock to mock out the methods in this class
 * to return mocks instead of the actual implementations. This allows us to test
 * classes in isolation.
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

    private static InternalConnection connection;
    private static ChannelManager channelManager;
    private static ExecutorService eventQueue;

    public static InternalConnection getConnection(String apiKey, PusherOptions options) {
	if (connection == null) {
	    try {
		connection = new WebSocketConnection(apiKey, options);
	    } catch (URISyntaxException e) {
		throw new IllegalArgumentException(
			"Failed to initialise connection", e);
	    }
	}
	return connection;
    }

    public static WebSocketClient newWebSocketClientWrapper(URI uri,
	    WebSocketListener proxy) throws SSLException {
	return new WebSocketClientWrapper(uri, proxy);
    }

    public static ExecutorService getEventQueue() {
	if (eventQueue == null) {
	    eventQueue = Executors.newSingleThreadExecutor();
	}
	return eventQueue;
    }

    public static ChannelImpl newPublicChannel(String channelName) {
	return new ChannelImpl(channelName);
    }
    
    public static PrivateChannelImpl newPrivateChannel(InternalConnection connection, String channelName, Authorizer authorizer) {
	return new PrivateChannelImpl(connection, channelName, authorizer);
    }
    
    public static PresenceChannelImpl newPresenceChannel(InternalConnection connection, String channelName, Authorizer authorizer) {
	return new PresenceChannelImpl(connection, channelName, authorizer);
    }

    public static ChannelManager getChannelManager() {
	if (channelManager == null) {
	    channelManager = new ChannelManager();
	}
	return channelManager;
    }

    public static URL newURL(String endPoint) throws MalformedURLException {
	return new URL(endPoint);
    }
}