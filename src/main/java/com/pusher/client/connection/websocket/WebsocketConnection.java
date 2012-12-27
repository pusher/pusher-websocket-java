package com.pusher.client.connection.websocket;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.google.gson.Gson;
import com.pusher.client.connection.Connection;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.util.Factory;

public class WebsocketConnection implements Connection, WebSocketListener {

    private static final String CONNECTION_ESTABLISHED = "connection_established";
    private static final String URI_PREFIX = "ws://ws.pusherapp.com:80/app/";
    private static final String URI_SUFFIX = "?client=java-client&version=" + WebsocketConnection.class.getPackage().getImplementationVersion();
    private static final String INTERNAL_EVENT_PREFIX = "pusher:";
    
    private ConnectionState state = ConnectionState.DISCONNECTED;
    private WebSocketClient underlyingConnection;
    private ConnectionEventListener eventListener = new NullConnectionEventListener();
    
    public WebsocketConnection(String apiKey) throws URISyntaxException {
	this.underlyingConnection = Factory.newWebSocketClientWrapper(new URI(URI_PREFIX + apiKey + URI_SUFFIX), this);
    }
    
    /* Connection implementation */
    
    @Override
    public void connect() {
	
	Factory.getEventQueue().execute(new Runnable() {
	    @Override
	    public void run() {
		if(state == ConnectionState.DISCONNECTED) {
		    WebsocketConnection.this.updateState(ConnectionState.CONNECTING);
		    WebsocketConnection.this.underlyingConnection.connect();
		}
	    }
	});
    }

    @Override
    public void setEventListener(ConnectionEventListener eventListener) {
	this.eventListener = eventListener;
    }

    @Override
    public void bind(ConnectionState state, ConnectionEventListener eventListener) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public ConnectionState getState() {
	return state;
    }

    /** implementation detail **/
    
    private void updateState(ConnectionState newState) {
	ConnectionStateChange change = new ConnectionStateChange(state, newState);
	this.state = newState;
	this.eventListener.onConnectionStateChange(change);
    }
    
    private void handleEvent(String event, String wholeMessage) {
	if(event.startsWith(INTERNAL_EVENT_PREFIX)) {
	    handleInternalEvent(event, wholeMessage);
	} else {
	    // TODO
	}
    }
    
    /* WebSocketListener implementation */
    
    private void handleInternalEvent(String event, String wholeMessage) {
	String internalEventType = event.split(":")[1];
	
	if(internalEventType.equals(CONNECTION_ESTABLISHED) && state == ConnectionState.CONNECTING) {
	    updateState(ConnectionState.CONNECTED);
	} else if(internalEventType.equals("error")) {
	    // TODO: what do we do with these?
	}
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
	// TODO: log the handshake data
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onMessage(final String message) {
	
	Factory.getEventQueue().execute(new Runnable() {
	    @Override
	    public void run() {
		System.out.println("onMessage: " + message);
		Map<String, String> map = new Gson().fromJson(message, Map.class);
		String event = map.get("event");
		handleEvent(event, message);
	    }
	});
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
	
	Factory.getEventQueue().execute(new Runnable() {
	    @Override
	    public void run() {
		updateState(ConnectionState.DISCONNECTED);
	    }
	});
    }

    @Override
    public void onError(Exception ex) {
	
	Factory.getEventQueue().execute(new Runnable() {
	    @Override
	    public void run() {
		updateState(ConnectionState.DISCONNECTED);
	    }
	});
    }
    
    private class NullConnectionEventListener implements ConnectionEventListener {
	@Override
	public void onConnectionStateChange(ConnectionStateChange change) {
	    // ignore
	}
    }
}