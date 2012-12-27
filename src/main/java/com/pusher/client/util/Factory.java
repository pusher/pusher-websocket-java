package com.pusher.client.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.java_websocket.client.WebSocketClient;

import com.pusher.client.connection.Connection;
import com.pusher.client.connection.websocket.WebSocketClientWrapper;
import com.pusher.client.connection.websocket.WebSocketListener;
import com.pusher.client.connection.websocket.WebsocketConnection;

public class Factory {

    private static ExecutorService eventQueue;
    
    public static Connection newConnection(String apiKey) {
	try {
	    return new WebsocketConnection(apiKey);
	} catch (URISyntaxException e) {
	    throw new IllegalArgumentException("Failed to initialise connection", e);
	}
    }

    public static WebSocketClient newWebSocketClientWrapper(URI uri, WebSocketListener proxy) {
	return new WebSocketClientWrapper(uri, proxy);
    }
    
    public static ExecutorService getEventQueue() {
	if(eventQueue == null) {
	    eventQueue = Executors.newSingleThreadExecutor();
	}
	return eventQueue;
    }
}