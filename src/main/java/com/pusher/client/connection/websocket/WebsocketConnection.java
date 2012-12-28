package com.pusher.client.connection.websocket;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.google.gson.Gson;
import com.pusher.client.channel.InternalChannel;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.connection.InternalConnection;
import com.pusher.client.util.Factory;

public class WebsocketConnection implements InternalConnection, WebSocketListener {

    private static final String CONNECTION_ESTABLISHED = "connection_established";
    private static final String URI_PREFIX = "ws://ws.pusherapp.com:80/app/";
    private static final String URI_SUFFIX = "?client=java-client&version=" + WebsocketConnection.class.getPackage().getImplementationVersion();
    private static final String INTERNAL_EVENT_PREFIX = "pusher:";
    
    private final Map<String, InternalChannel> channelNameToChannelMap = new HashMap<String, InternalChannel>();
    
    private volatile ConnectionState state = ConnectionState.DISCONNECTED;
    private WebSocketClient underlyingConnection;
    private volatile ConnectionEventListener eventListener = new NullConnectionEventListener();
    
    public WebsocketConnection(String apiKey) throws URISyntaxException {
	
	this.underlyingConnection = Factory.newWebSocketClientWrapper(new URI(URI_PREFIX + apiKey + URI_SUFFIX), this);
    }
    
    /* Connection implementation */
    
    @Override
    public void connect() {
	
	Factory.getEventQueue().execute(new Runnable() {

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
    
    /** InternalConnection implementation detail **/
    
    @Override
    public void subscribeTo(final InternalChannel channel) {
	
	if(channelNameToChannelMap.containsKey(channel.getName())) {
	    throw new IllegalArgumentException("Already subscribed to channel " + channel.getName());
	}
	
	Factory.getEventQueue().execute(new Runnable() {
	    public void run() {
		if(state == ConnectionState.CONNECTED) {
		    
		    channelNameToChannelMap.put(channel.getName(), channel);
		    
		    String subscriptionMessage = channel.toSubscribeMessage();
		    underlyingConnection.send(subscriptionMessage);
		    
		    channel.subscribeSent();
		} else {
		    // TODO: queue the subscription for when the connection is up
		}
	    }
	});
    }

    @Override
    public void unsubscribeFrom(final String channelName) {

	if(!channelNameToChannelMap.containsKey(channelName)) {
	    throw new IllegalArgumentException("Cannot unsubscribe from channel " + channelName + ", no existing subscription found");
	}
	
	Factory.getEventQueue().execute(new Runnable() {
	    public void run() {
		if(state == ConnectionState.CONNECTED) {
		    
		    InternalChannel channel = channelNameToChannelMap.remove(channelName);
		    
		    String subscriptionMessage = channel.toUnsubscribeMessage();
		    underlyingConnection.send(subscriptionMessage);
		} else {
		    // TODO: queue the unsubscribe for when the connection is up
		}
	    }
	});	
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
	    handleUserEvent(event, wholeMessage);
	}
    }
    
    @SuppressWarnings("rawtypes")
    private void handleUserEvent(String event, String wholeMessage) {
	
	Map json = new Gson().fromJson(wholeMessage, Map.class);
	Object channelNameObject = json.get("channel");
	
	if(channelNameObject != null) {
	    String channelName = (String) channelNameObject;
	    
	    InternalChannel channel = channelNameToChannelMap.get(channelName);
	    if(channel != null) {
		channel.onMessage(event, wholeMessage);
	    }
	}
    }

    private void handleInternalEvent(String event, String wholeMessage) {
	
	String internalEventType = event.split(":")[1];
	
	if(internalEventType.equals(CONNECTION_ESTABLISHED) && state == ConnectionState.CONNECTING) {
	    updateState(ConnectionState.CONNECTED);
	} else if(internalEventType.equals("error")) {
	    // TODO: what do we do with these?
	}
    }

    /* WebSocketListener implementation */

    @Override
    public void onOpen(ServerHandshake handshakedata) {
	// TODO: log the handshake data
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onMessage(final String message) {
	
	Factory.getEventQueue().execute(new Runnable() {
	    public void run() {
		Map<String, String> map = new Gson().fromJson(message, Map.class);
		String event = map.get("event");
		handleEvent(event, message);
	    }
	});
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
	
	Factory.getEventQueue().execute(new Runnable() {
	    public void run() {
		updateState(ConnectionState.DISCONNECTED);
	    }
	});
    }

    @Override
    public void onError(Exception ex) {
	
	Factory.getEventQueue().execute(new Runnable() {
	    public void run() {
		updateState(ConnectionState.DISCONNECTED);
	    }
	});
    }
    
    private class NullConnectionEventListener implements ConnectionEventListener {
	public void onConnectionStateChange(ConnectionStateChange change) { /* ignore */ }
    }
}