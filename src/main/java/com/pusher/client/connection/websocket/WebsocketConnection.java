package com.pusher.client.connection.websocket;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.pusher.client.connection.Connection;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.util.Factory;

public class WebsocketConnection implements Connection, WebSocketListener {

    private static final String URI_PREFIX = "ws://ws.pusherapp.com:80/app/";
    private static final String URI_SUFFIX = "?client=java-client&version=0.0.1";
    private ConnectionState state = ConnectionState.DISCONNECTED;
    private WebSocketClient underlyingConnection;
    
    public WebsocketConnection(String apiKey) throws URISyntaxException {
	this.underlyingConnection = Factory.newWebSocketClientWrapper(new URI(URI_PREFIX + apiKey + URI_SUFFIX), this);
    }
    
    /* Connection implementation */
    
    @Override
    public void connect() {
	this.state = ConnectionState.CONNECTING;
	this.underlyingConnection.connect();
    }

    @Override
    public void setEventListener(ConnectionEventListener eventListener) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void bind(ConnectionState state, ConnectionEventListener eventListener) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public ConnectionState getState() {
	return state;
    }

    /* WebSocketListener implementation */
    
    @Override
    public void onOpen(ServerHandshake handshakedata) {
	System.out.println("onOpen");
    }

    @Override
    public void onMessage(String message) {
	System.out.println("onMessage: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
	System.out.println("onClose: " + code + ", " + reason + ", " + remote);
    }

    @Override
    public void onError(Exception ex) {
	System.out.println("onError: " + ex);
    }
}