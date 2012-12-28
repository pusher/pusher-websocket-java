package com.pusher.client.connection.websocket;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.google.gson.Gson;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.connection.InternalConnection;
import com.pusher.client.util.Factory;

public class WebsocketConnection implements InternalConnection, WebSocketListener {

    private static final String URI_PREFIX = "ws://ws.pusherapp.com:80/app/";
    private static final String URI_SUFFIX = "?client=java-client&version=" + WebsocketConnection.class.getPackage().getImplementationVersion();
    private static final String INTERNAL_EVENT_PREFIX = "pusher:";
    
    private final Map<ConnectionState, Set<ConnectionEventListener>> eventListeners = new HashMap<ConnectionState, Set<ConnectionEventListener>>();
    private volatile ConnectionState state = ConnectionState.DISCONNECTED;
    private WebSocketClient underlyingConnection;
    
    public WebsocketConnection(String apiKey) throws URISyntaxException {
	
	for(ConnectionState state : ConnectionState.values()) {
	    eventListeners.put(state, new HashSet<ConnectionEventListener>());
	}
	
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
    public void bind(ConnectionState state, ConnectionEventListener eventListener) {
	eventListeners.get(state).add(eventListener);
    }

    @Override
    public ConnectionState getState() {
	return state;
    }
    
    /** InternalConnection implementation detail **/

    @Override
    public void sendMessage(final String message) {

	Factory.getEventQueue().execute(new Runnable() {
	    public void run() {
		
		try {
        		if(state == ConnectionState.CONNECTED) {
        		    underlyingConnection.send(message);
        		} else {
        		    sendErrorToAllListeners("Cannot send a message while in " + state + " state", null, null);
        		}
		} catch(Exception e) {
		    sendErrorToAllListeners("An exception occurred while sending message [" + message + "]", null, e);
		}
	    }
	});	
    }
    
    /** implementation detail **/
    
    private void updateState(ConnectionState newState) {
	
	final ConnectionStateChange change = new ConnectionStateChange(state, newState);
	this.state = newState;
	
	Set<ConnectionEventListener> interestedListeners = new HashSet<ConnectionEventListener>();
	interestedListeners.addAll(eventListeners.get(ConnectionState.ALL));
	interestedListeners.addAll(eventListeners.get(newState));
	
	for(final ConnectionEventListener listener : interestedListeners) {
	    
	    Factory.getEventQueue().execute(new Runnable() {
		public void run() {
		    listener.onConnectionStateChange(change);
		}
	    });
	}
    }
    
    private void handleEvent(String event, String wholeMessage) {
	
	if(event.startsWith(INTERNAL_EVENT_PREFIX)) {
	    handleInternalEvent(event, wholeMessage);
	} else {
	    Factory.getChannelManager(this).onMessage(event, wholeMessage);
	}
    }
    
    private void handleInternalEvent(String event, String wholeMessage) {
	
	if(event.equals("pusher:connection_established") && state == ConnectionState.CONNECTING) {
	    updateState(ConnectionState.CONNECTED);
	} else if(event.equals("pusher:error")) {
	    handleError(wholeMessage);
	}
    }

    @SuppressWarnings({ "unchecked" })
    private void handleError(String wholeMessage) {

	Map<Object, Object> json = new Gson().fromJson(wholeMessage, Map.class);
	Map<Object, Object> data = (Map<Object, Object>) json.get("data");
	
	String message = (String) data.get("message");
	String code = String.valueOf(Math.round((Double)data.get("code")));
	
	sendErrorToAllListeners(message, code, null);
    }
    
    private void sendErrorToAllListeners(final String message, final String code, final Exception e) {
	
	Set<ConnectionEventListener> allListeners = new HashSet<ConnectionEventListener>();
	for(Set<ConnectionEventListener> listenersForState : eventListeners.values()) {
	    allListeners.addAll(listenersForState);
	}
	
	for(final ConnectionEventListener listener : allListeners) {
	    Factory.getEventQueue().execute(new Runnable() {
		public void run() {
		    listener.onError(message, code, e);
		}
	    });
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
    public void onError(final Exception ex) {
	
	Factory.getEventQueue().execute(new Runnable() {
	    public void run() {
		updateState(ConnectionState.DISCONNECTED);
		sendErrorToAllListeners("An exception was thrown by the websocket", null, ex);
	    }
	});
    }
}