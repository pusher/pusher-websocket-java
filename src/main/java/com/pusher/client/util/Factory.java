package com.pusher.client.util;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;

import com.pusher.client.connection.Connection;
import com.pusher.client.connection.websocket.WebSocketClientWrapper;
import com.pusher.client.connection.websocket.WebSocketListener;
import com.pusher.client.connection.websocket.WebsocketConnection;

public class Factory {

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
}