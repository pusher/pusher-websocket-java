package com.pusher.client.connection.websocket;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.connection.impl.InternalConnection;
import com.pusher.client.util.Factory;

public class WebSocketConnection implements InternalConnection, WebSocketListener {
    private static final Logger log = LoggerFactory.getLogger(WebSocketConnection.class);

    private static final String INTERNAL_EVENT_PREFIX = "pusher:";
    static final String PING_EVENT_SERIALIZED = "{\"event\": \"pusher:ping\"}";

    private final Factory factory;
    private final Map<ConnectionState, Set<ConnectionEventListener>> eventListeners = new HashMap<ConnectionState, Set<ConnectionEventListener>>();
    private volatile ConnectionState state = ConnectionState.DISCONNECTED;
    private WebSocketClient underlyingConnection;
    private final URI webSocketUri;
    private String socketId;

    private final long activityTimeout;
    private final long pongTimeout;
    long lastActivity;
    long lastPong;

    public WebSocketConnection(String url, long activityTimeout, long pongTimeout, Factory factory) throws URISyntaxException {
        this.webSocketUri = new URI(url);
        this.activityTimeout = activityTimeout;
        this.pongTimeout = pongTimeout;
        this.factory = factory;

        for (ConnectionState state : ConnectionState.values()) {
            eventListeners.put(state, new HashSet<ConnectionEventListener>());
        }
    }

    /* Connection implementation */

    @Override
    public void connect() {
        factory.getEventQueue().execute(new Runnable() {
            @Override
            public void run() {
                if (state == ConnectionState.DISCONNECTED) {
                    try {
                        underlyingConnection = factory
                                .newWebSocketClientWrapper(webSocketUri, WebSocketConnection.this);

                        updateState(ConnectionState.CONNECTING);
                        underlyingConnection.connect();
                    } catch (SSLException e) {
                        sendErrorToAllListeners("Error connecting over SSL", null, e);
                    }
                }
            }
        });
    }

    @Override
    public void disconnect() {
        factory.getEventQueue().execute(new Runnable() {
            @Override
            public void run() {
                if (state == ConnectionState.CONNECTED) {
                    updateState(ConnectionState.DISCONNECTING);
                    underlyingConnection.close();
                }
            }
        });
    }

    @Override
    public void bind(ConnectionState state, ConnectionEventListener eventListener) {
        eventListeners.get(state).add(eventListener);
    }

    @Override
    public boolean unbind(ConnectionState state, ConnectionEventListener eventListener) {
        return eventListeners.get(state).remove(eventListener);
    }

    @Override
    public ConnectionState getState() {
        return state;
    }

    /* InternalConnection implementation detail */

    @Override
    public void sendMessage(final String message) {
        factory.getEventQueue().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (state == ConnectionState.CONNECTED) {
                        underlyingConnection.send(message);
                    } else {
                        sendErrorToAllListeners("Cannot send a message while in " + state + " state", null, null);
                    }
                } catch (Exception e) {
                    sendErrorToAllListeners("An exception occurred while sending message [" + message + "]", null, e);
                }
            }
        });
    }

    @Override
    public String getSocketId() {
        return socketId;
    }

    /* implementation detail */

    private void updateState(ConnectionState newState) {
        log.debug("State transition requested, current [" + state + "], new [" + newState + "]");

        final ConnectionStateChange change = new ConnectionStateChange(state, newState);
        this.state = newState;

        Set<ConnectionEventListener> interestedListeners = new HashSet<ConnectionEventListener>();
        interestedListeners.addAll(eventListeners.get(ConnectionState.ALL));
        interestedListeners.addAll(eventListeners.get(newState));

        for (final ConnectionEventListener listener : interestedListeners) {
            factory.getEventQueue().execute(new Runnable() {
                @Override
                public void run() {
                    listener.onConnectionStateChange(change);
                }
            });
        }
    }

    private void handleEvent(String event, String wholeMessage) {
        if (event.startsWith(INTERNAL_EVENT_PREFIX)) {
            handleInternalEvent(event, wholeMessage);
        } else {
            factory.getChannelManager().onMessage(event, wholeMessage);
        }
    }

    private void handleInternalEvent(String event, String wholeMessage) {
        if (event.equals("pusher:connection_established")) {
            handleConnectionMessage(wholeMessage);
        } else if (event.equals("pusher:error")) {
            handleError(wholeMessage);
        } else if (event.equals("pusher:pong")) {
            handlePong(wholeMessage);
        }
    }

    @SuppressWarnings("rawtypes")
    private void handleConnectionMessage(String message) {
        Map jsonObject = new Gson().fromJson(message, Map.class);
        String dataString = (String) jsonObject.get("data");
        Map dataMap = new Gson().fromJson(dataString, Map.class);
        socketId = (String) dataMap.get("socket_id");

        updateState(ConnectionState.CONNECTED);

        factory.getEventQueue().schedule(new PingCheck(), activityTimeout, TimeUnit.MILLISECONDS);
    }

    @SuppressWarnings("rawtypes")
    private void handleError(String wholeMessage) {
        Map json = new Gson().fromJson(wholeMessage, Map.class);
        Object data = json.get("data");

        Map dataMap;
        if (data instanceof String) {
            dataMap = new Gson().fromJson(((String) data), Map.class);
        } else {
            dataMap = (Map) data;
        }

        String message = (String) dataMap.get("message");

        Object codeObject = dataMap.get("code");
        String code = null;
        if (codeObject != null) {
            code = String.valueOf(Math.round((Double) codeObject));
        }

        sendErrorToAllListeners(message, code, null);
    }

    private void handlePong(String wholeMessage) {
        lastPong = factory.timeNow();
    }

    private void sendErrorToAllListeners(final String message, final String code, final Exception e) {
        Set<ConnectionEventListener> allListeners = new HashSet<ConnectionEventListener>();
        for (Set<ConnectionEventListener> listenersForState : eventListeners.values()) {
            allListeners.addAll(listenersForState);
        }

        for (final ConnectionEventListener listener : allListeners) {
            factory.getEventQueue().execute(new Runnable() {
                @Override
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
        factory.getEventQueue().execute(new Runnable() {
            @Override
            public void run() {
                lastActivity = factory.timeNow();

                Map<String, String> map = new Gson().fromJson(message, Map.class);
                String event = map.get("event");
                handleEvent(event, message);
            }
        });
    }

    @Override
    public void onClose(final int code, final String reason, final boolean remote) {
        factory.getEventQueue().execute(new Runnable() {
            @Override
            public void run() {
                if (state != ConnectionState.DISCONNECTED) {
                    updateState(ConnectionState.DISCONNECTED);

                    factory.shutdownEventQueue();
                } else {
                    log.error("Received close from underlying socket when already disconnected. "
                            + "Close code [" + code + "], Reason [" + reason + "], Remote [" + remote + "]");
                }
            }
        });
    }

    @Override
    public void onError(final Exception ex) {
        factory.getEventQueue().execute(new Runnable() {
            @Override
            public void run() {
                // Do not change connection state as Java_WebSocket will also call onClose.
                // See: https://github.com/leggetter/pusher-java-client/issues/8#issuecomment-16128590
                //updateState(ConnectionState.DISCONNECTED);
                sendErrorToAllListeners("An exception was thrown by the websocket",    null, ex);
            }
        });
    }

    /* Package private for testing */
    class PingCheck implements Runnable {

        @Override
        public void run() {
            final long timeNow = factory.timeNow();
            if (state == ConnectionState.CONNECTED) {
                long nextCheck;
                if (lastActivity + activityTimeout < timeNow) {
                    log.debug("Sending ping message");
                    sendMessage(PING_EVENT_SERIALIZED);

                    factory.getEventQueue().schedule(new PongCheck(timeNow), pongTimeout, TimeUnit.MILLISECONDS);

                    // If we have sent a ping, reschedule for one ping period, plus
                    // a small buffer within which we hope to have received the pong from the server
                    nextCheck = activityTimeout + 500;
                } else {
                    // If there's been some activity since this check was scheduled,
                    // reschedule one period after that activity occurred
                    nextCheck = lastActivity + activityTimeout - timeNow;
                }
                factory.getEventQueue().schedule(new PingCheck(), nextCheck, TimeUnit.MILLISECONDS);
            }
            // If we're not connected, don't ping, nor schedule further checks.
            // A new check will be scheduled if and when we reconnect.
        }
    }

    class PongCheck implements Runnable {
        private final long pingSent;

        public PongCheck(final long pingSent) {
            this.pingSent = pingSent;
        }

        @Override
        public void run() {
            if (lastPong < pingSent) {
                log.info("Timed out awaiting pong response from server. Moving to disconnected state.");
                disconnect();
            }
        }
    }

    /* For testing only */
    void pingCheckNow() {
        new PingCheck().run();
    }
}
