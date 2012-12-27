package com.pusher.client.connection.websocket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.pusher.client.connection.ConnectionState;
import com.pusher.client.util.Factory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Factory.class})
public class WebsocketConnectionTest {

    private static final String API_KEY = "161717a55e65825bacf1";
    private WebsocketConnection connection;
    private @Mock WebSocketClientWrapper mockUnderlyingConnection;
    
    @Before
    public void setUp() throws URISyntaxException {
	
	PowerMockito.mockStatic(Factory.class);
	when(Factory.newWebSocketClientWrapper(any(URI.class), any(WebsocketConnection.class))).thenReturn(mockUnderlyingConnection);
	
	this.connection = new WebsocketConnection(API_KEY);
    }
    
    @Test
    public void testStartsInDisconnectedState() {
	assertSame(ConnectionState.DISCONNECTED, connection.getState());
    }
    
    @Test
    public void testConnectCallsConnectOnUnderlyingConnectionAndUpdatesState() {
	connection.connect();
	verify(mockUnderlyingConnection).connect();
	assertEquals(ConnectionState.CONNECTING, connection.getState());
    }
}