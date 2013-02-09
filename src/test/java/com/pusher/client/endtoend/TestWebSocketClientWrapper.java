package com.pusher.client.endtoend;

import java.net.URI;
import java.nio.channels.NotYetConnectedException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.pusher.client.connection.websocket.WebSocketClientWrapper;
import com.pusher.client.connection.websocket.WebSocketListener;

public class TestWebSocketClientWrapper extends WebSocketClientWrapper {

	private final List<String> messagesSent = new ArrayList<String>();
	private boolean connectCalled = false;
	
	public TestWebSocketClientWrapper(URI uri, WebSocketListener proxy) {
		super(uri, proxy);
	}

	void assertMessageSent(String msg) {
		
		if(!messagesSent.contains(msg)) {
			StringBuilder sb = new StringBuilder(String.format("Outgoing message \"%s\" not found. Messages that were sent:", msg));
			for(String sentMessage : messagesSent) {
				sb.append("\n");
				sb.append(sentMessage);
			}
			fail(sb.toString());
		}
	}
	
	void assertNoMessagesSent() {
		if(!messagesSent.isEmpty()) {
			StringBuilder sb = new StringBuilder("Expected no outgoing messages but found these:");
			for(String sentMessage : messagesSent) {
				sb.append("\n");
				sb.append(sentMessage);
			}
			fail(sb.toString());
		}		
	}

	void assertConnectCalled() {
		assertTrue(connectCalled);
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