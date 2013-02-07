package com.pusher.client.connection.websocket;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.google.gson.Gson;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.connection.impl.InternalConnection;
import com.pusher.client.util.Factory;

public class WebSocketConnection implements InternalConnection, WebSocketListener {

    // The version is populated from the pom.xml when running the application as a built library. However when running
    // the source locally this will return null, so a default version of 0.0.0 will be used instead.
    private static final String APP_VERSION = (WebSocketConnection.class.getPackage().getImplementationVersion() != null) ? WebSocketConnection.class.getPackage().getImplementationVersion() : "0.0.0";
    private static final String WS_SCHEME = "ws";
    private static final String WSS_SCHEME = "wss";
    private static final String HOST = "ws.pusherapp.com";
    private static final int WS_PORT = 80;
    private static final int WSS_PORT = 443;
    private static final String URI_SUFFIX = "?client=java-client&protocol=5&version=" + APP_VERSION;
    private static final String INTERNAL_EVENT_PREFIX = "pusher:";
    
    private final Map<ConnectionState, Set<ConnectionEventListener>> eventListeners = new HashMap<ConnectionState, Set<ConnectionEventListener>>();
    private volatile ConnectionState state = ConnectionState.DISCONNECTED;
    private WebSocketClient underlyingConnection;
    private final URI webSocketUri;
    private String socketId;
    
    public WebSocketConnection(String apiKey, boolean encrypted) throws URISyntaxException {
    	String url = String.format(
    			"%s://ws.pusherapp.com:%s/app/%s%s", (encrypted? WSS_SCHEME : WS_SCHEME), (encrypted? WSS_PORT : WS_PORT), apiKey, URI_SUFFIX
    		);
    	webSocketUri = new URI(url);
			for(ConnectionState state : ConnectionState.values()) {
			    eventListeners.put(state, new HashSet<ConnectionEventListener>());
			}	
    }
    
    /* Connection implementation */
    
    @Override
    public void connect() {
	
    	Factory.getEventQueue().execute(new Runnable() {

    		public void run() {
    			if(state == ConnectionState.DISCONNECTED) {
    				try {
							WebSocketConnection.this.underlyingConnection = 
									Factory.newWebSocketClientWrapper(WebSocketConnection.this.webSocketUri, WebSocketConnection.this);
							
							WebSocketConnection.this.updateState(ConnectionState.CONNECTING);
	    				WebSocketConnection.this.underlyingConnection.connect();
						} catch (SSLException e) {
							// TODO Decide how to handle this exception.
							e.printStackTrace();
						}
    				
    			}
    		}
			});
    }
    
    @Override
    public void disconnect() {
	
    	Factory.getEventQueue().execute(new Runnable() {
    		public void run() {
    			if(state == ConnectionState.CONNECTED) {
    				WebSocketConnection.this.updateState(ConnectionState.DISCONNECTING);
    				WebSocketConnection.this.underlyingConnection.close();
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

    @Override
    public String getSocketId() {
	return socketId;
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
	    Factory.getChannelManager(this, null).onMessage(event, wholeMessage);
	}
    }
    
    private void handleInternalEvent(String event, String wholeMessage) {
	
	if(event.equals("pusher:connection_established") && state == ConnectionState.CONNECTING) {
	    handleConnectionMessage(wholeMessage);
	} else if(event.equals("pusher:error")) {
	    handleError(wholeMessage);
	}
    }
    
    @SuppressWarnings("rawtypes")
    private void handleConnectionMessage(String message) {
	
	Map jsonObject = new Gson().fromJson(message, Map.class);
	String dataString = (String) jsonObject.get("data");
	Map dataMap = new Gson().fromJson(dataString, Map.class);
	socketId = (String) dataMap.get("socket_id");
	
	updateState(ConnectionState.CONNECTED);
    }

    @SuppressWarnings("rawtypes")
    private void handleError(String wholeMessage) {

	Map json = new Gson().fromJson(wholeMessage, Map.class);
	Object data = json.get("data");
	
	Map dataMap;
	if(data instanceof String) {
	    dataMap = new Gson().fromJson(((String)data), Map.class);
	} else {
	    dataMap = (Map) data;
	}
	
	String message = (String) dataMap.get("message");
	
	Object codeObject = dataMap.get("code");
	String code = null;
	if(codeObject != null) {
	    code = String.valueOf(Math.round((Double)codeObject));
	}
	
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