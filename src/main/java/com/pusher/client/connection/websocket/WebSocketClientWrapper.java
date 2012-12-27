package com.pusher.client.connection.websocket;

import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

/**
 * A thin wrapper around the WebSocketClient class from the Java-WebSocket library. The purpose of this
 * class is to enable the WebSocketConnection class to be unit tested by swapping out an instance of
 * this wrapper for a mock version. 
 */
public class WebSocketClientWrapper extends WebSocketClient {

    private final WebSocketListener proxy;
    
    public WebSocketClientWrapper(URI uri, WebSocketListener proxy) {
	super(uri);
	this.proxy = proxy;
    }
    
    @Override
    public void onOpen(ServerHandshake handshakedata) {
	proxy.onOpen(handshakedata);
    }

    @Override
    public void onMessage(String message) {
	proxy.onMessage(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
	proxy.onClose(code, reason, remote);
    }

    @Override
    public void onError(Exception ex) {
	proxy.onError(ex);
    }    
}