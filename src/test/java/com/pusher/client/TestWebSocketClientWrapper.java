package com.pusher.client;

import static org.junit.Assert.*;

import java.net.Proxy;
import java.net.URI;
import java.nio.channels.NotYetConnectedException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLException;

import com.pusher.client.connection.websocket.WebSocketClientWrapper;
import com.pusher.client.connection.websocket.WebSocketListener;

public class TestWebSocketClientWrapper extends WebSocketClientWrapper {

    private final List<String> messagesSent = new ArrayList<String>();
    private boolean connectCalled = false;

    public TestWebSocketClientWrapper(final URI uri, final Proxy proxy, final WebSocketListener webSocketListener) throws SSLException {
        super(uri, proxy, webSocketListener);
    }

    void assertConnectCalled() {
        assertTrue(connectCalled);
    }

    void assertLatestMessageWas(final String msg) {
        assertFalse("No messages have been sent", messagesSent.isEmpty());
        assertEquals(msg, messagesSent.get(messagesSent.size() - 1));
    }

    void assertNumberOfMessagesSentIs(final int count) {
        assertEquals(count, messagesSent.size());
    }

    @Override
    public void send(final String text) throws NotYetConnectedException {
        messagesSent.add(text);
        super.send(text);
    }

    @Override
    public void connect() {
        connectCalled = true;
    }
}
