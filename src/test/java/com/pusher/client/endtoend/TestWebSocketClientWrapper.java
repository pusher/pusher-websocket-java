package com.pusher.client.endtoend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.nio.channels.NotYetConnectedException;
import java.util.ArrayList;
import java.util.List;

import com.pusher.client.connection.websocket.WebSocketClientWrapper;
import com.pusher.client.connection.websocket.WebSocketListener;

public class TestWebSocketClientWrapper extends WebSocketClientWrapper {

	private final List<String> messagesSent = new ArrayList<String>();
	private boolean connectCalled = false;
	
	public TestWebSocketClientWrapper(URI uri, WebSocketListener proxy) {
		super(uri, proxy);
	}

	void assertConnectCalled() {
		assertTrue(connectCalled);
	}
	
	void assertLatestMessageWas(String msg) {
		assertFalse("No messages have been sent", messagesSent.isEmpty());
		assertEquals(msg, messagesSent.get(messagesSent.size()-1));
	}

	void assertNumberOfMessagesSentIs(int count) {
		assertEquals(count, messagesSent.size());
	}

	@Override
	public void send(String text) throws NotYetConnectedException {
		messagesSent.add(text);
		super.send(text);
	}

	@Override
	public void connect() {
		this.connectCalled = true;
	}
}