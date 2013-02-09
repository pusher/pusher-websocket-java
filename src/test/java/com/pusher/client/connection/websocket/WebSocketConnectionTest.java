package com.pusher.client.connection.websocket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.pusher.client.channel.impl.ChannelManager;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.connection.impl.InternalConnection;
import com.pusher.client.util.Factory;
import com.pusher.client.util.InstantExecutor;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Factory.class})
public class WebSocketConnectionTest {

    private static final String API_KEY = "123456";
    private static final String EVENT_NAME = "my-event";
    private static final String INCOMING_MESSAGE = "{\"event\":\"" + EVENT_NAME + "\",\"channel\":\"my-channel\",\"data\":{\"fish\":\"chips\"}}";
    
    private WebSocketConnection connection;
    private @Mock ChannelManager mockChannelManager;
    private @Mock WebSocketClientWrapper mockUnderlyingConnection;
    private @Mock ConnectionEventListener mockEventListener;
    
    @Before
    public void setUp() throws URISyntaxException {
	
	PowerMockito.mockStatic(Factory.class);
	when(Factory.getChannelManager(any(InternalConnection.class))).thenReturn(mockChannelManager);
	when(Factory.newWebSocketClientWrapper(any(URI.class), any(WebSocketConnection.class))).thenReturn(mockUnderlyingConnection);
	when(Factory.getEventQueue()).thenReturn(new InstantExecutor());
	
	this.connection = new WebSocketConnection(API_KEY);
	this.connection.bind(ConnectionState.ALL, mockEventListener);
    }
    
    @Test
    public void testVerifyURLIsCorrect() {
    	this.connection.connect();
    	ArgumentCaptor<URI> argument = ArgumentCaptor.forClass(URI.class);
	
    	PowerMockito.verifyStatic();
    	Factory.newWebSocketClientWrapper(argument.capture(), eq(connection));
	
    	assertEquals("ws://ws.pusherapp.com:80/app/" + API_KEY + "?client=java-client&protocol=5&version=0.0.0", argument.getValue().toString());
    }
    
    @Test
    public void testStartsInDisconnectedState() {
	assertSame(ConnectionState.DISCONNECTED, connection.getState());
    }
    
    @Test
    public void testConnectCallIsDelegatedToUnderlyingConnection() {
	connection.connect();
	verify(mockUnderlyingConnection).connect();
    }
    
    @Test
    public void testConnectUpdatesStateAndNotifiesListener() {
	connection.connect();
	verify(mockEventListener).onConnectionStateChange(new ConnectionStateChange(ConnectionState.DISCONNECTED, ConnectionState.CONNECTING));
	assertEquals(ConnectionState.CONNECTING, connection.getState());
    }
    
    @Test
    public void testConnectDoesNotCallConnectOnUnderlyingConnectionIfAlreadyInConnectingState() {
	connection.connect();
	connection.connect();
	
	verify(mockUnderlyingConnection, times(1)).connect();
	verify(mockEventListener, times(1)).onConnectionStateChange(any(ConnectionStateChange.class));
    }
    
    @Test
    public void testListenerDoesNotReceiveConnectingEventIfItIsOnlyBoundToTheConnectedEvent() throws URISyntaxException {
	connection = new WebSocketConnection(API_KEY);
	connection.bind(ConnectionState.CONNECTED, mockEventListener);
	connection.connect();
	
	verify(mockEventListener, never()).onConnectionStateChange(any(ConnectionStateChange.class));
    }
    
    @Test
    public void testReceivePusherConnectionEstablishedMessageIsTranslatedToAConnectedCallback() {
	connection.connect();
	verify(mockEventListener).onConnectionStateChange(new ConnectionStateChange(ConnectionState.DISCONNECTED, ConnectionState.CONNECTING));
	
	connection.onMessage("{\"event\":\"pusher:connection_established\",\"data\":\"{\\\"socket_id\\\":\\\"21112.816204\\\"}\"}");
	verify(mockEventListener).onConnectionStateChange(new ConnectionStateChange(ConnectionState.CONNECTING, ConnectionState.CONNECTED));
	
	assertEquals(ConnectionState.CONNECTED, connection.getState());
    }
    
    @Test
    public void testReceivePusherConnectionEstablishedMessageSetsSocketId() {
	assertNull(connection.getSocketId());
	
	connection.connect();
	connection.onMessage("{\"event\":\"pusher:connection_established\",\"data\":\"{\\\"socket_id\\\":\\\"21112.816204\\\"}\"}");
	
	assertEquals("21112.816204", connection.getSocketId());
    }
    
    @Test
    public void testReceivePusherConnectionEstablishedMessageWhenAlreadyConnectedIsIgnored() {
	connection.connect();
	connection.onMessage("{\"event\":\"pusher:connection_established\",\"data\":\"{\\\"socket_id\\\":\\\"21112.816204\\\"}\"}");
	connection.onMessage("{\"event\":\"pusher:connection_established\",\"data\":\"{\\\"socket_id\\\":\\\"21112.816204\\\"}\"}");
	
	verify(mockEventListener, times(2)).onConnectionStateChange(any(ConnectionStateChange.class));
    }
    
    @Test
    public void testReceivePusherErrorMessageRaisesErrorEvent() {
	connection.connect();
	verify(mockEventListener).onConnectionStateChange(new ConnectionStateChange(ConnectionState.DISCONNECTED, ConnectionState.CONNECTING));
	
	connection.onMessage("{\"event\":\"pusher:error\",\"data\":{\"code\":4001,\"message\":\"Could not find app by key 12345\"}}");
	verify(mockEventListener).onError("Could not find app by key 12345", "4001", null);
    }
    
    @Test
    public void testSendMessageSendsMessageToPusher() {
	connect();
	
	connection.sendMessage("message");
	
	verify(mockUnderlyingConnection).send("message");
    }
    
    @Test
    public void testSendMessageWhenNotConnectedRaisesErrorEvent() {
	connection.sendMessage("message");
	
	verify(mockUnderlyingConnection, never()).send("message");
	verify(mockEventListener).onError("Cannot send a message while in " + ConnectionState.DISCONNECTED.toString() + " state", null, null);
    }
    
    @Test
    public void testSendMessageWhenWebSocketLibraryThrowsExceptionRaisesErrorEvent() {
	connect();
	
	RuntimeException e = new RuntimeException();
	doThrow(e).when(mockUnderlyingConnection).send(anyString());
	
	connection.sendMessage("message");
	
	verify(mockEventListener).onError("An exception occurred while sending message [message]", null, e);
    }
    
    @Test
    public void testReceiveUserMessagePassesMessageToChannelManager() {
	connect();
	
	connection.onMessage(INCOMING_MESSAGE);
	
	verify(mockChannelManager).onMessage(EVENT_NAME, INCOMING_MESSAGE);
    }
    
    @Test
    public void testOnCloseCallbackUpdatesStateToDisconnected() {
	connection.connect();
	verify(mockEventListener).onConnectionStateChange(new ConnectionStateChange(ConnectionState.DISCONNECTED, ConnectionState.CONNECTING));
	
	connection.onClose(1, "reason", true);
	verify(mockEventListener).onConnectionStateChange(new ConnectionStateChange(ConnectionState.CONNECTING, ConnectionState.DISCONNECTED));
    }

    @Test
    public void testOnCloseCallbackDoesNotCallListenerIfItIsNotBoundToDisconnectedEvent() throws URISyntaxException {
	connection = new WebSocketConnection(API_KEY);
	connection.bind(ConnectionState.CONNECTED, mockEventListener);
	
	connection.connect();
	connection.onClose(1, "reason", true);
	verify(mockEventListener, never()).onConnectionStateChange(any(ConnectionStateChange.class));
    }    
    
    @Test
    public void testOnErrorCallbackUpdatesStateToDisconnectedAndRaisesErrorEvent() {
	connection.connect();
	verify(mockEventListener).onConnectionStateChange(new ConnectionStateChange(ConnectionState.DISCONNECTED, ConnectionState.CONNECTING));
	
	Exception e = new Exception();
	connection.onError(e);
	verify(mockEventListener).onConnectionStateChange(new ConnectionStateChange(ConnectionState.CONNECTING, ConnectionState.DISCONNECTED));
	verify(mockEventListener).onError("An exception was thrown by the websocket", null, e);
    }
    
    @Test
    public void testDisonnectCallIsDelegatedToUnderlyingConnection() {
    	connection.connect();
    	connection.onMessage("{\"event\":\"pusher:connection_established\",\"data\":\"{\\\"socket_id\\\":\\\"21112.816204\\\"}\"}");
    	
    	connection.disconnect();
    	verify(mockUnderlyingConnection).close();
    }
    
    @Test
    public void testDisconnectInConnectedStateUpdatesStateToDisconnectingAndNotifiesListener() {
    	connection.connect();
    	connection.onMessage("{\"event\":\"pusher:connection_established\",\"data\":\"{\\\"socket_id\\\":\\\"21112.816204\\\"}\"}");
    	
    	connection.disconnect();
    	verify(mockEventListener).onConnectionStateChange(
    			new ConnectionStateChange(ConnectionState.CONNECTED, ConnectionState.DISCONNECTING)
    		);
    	assertEquals(ConnectionState.DISCONNECTING, connection.getState());
    }
    
    @Test
    public void testDisconnectInDisconnectedStateIsIgnored() {
    	connection.disconnect();
    	
    	verify(mockUnderlyingConnection, times(0)).close();
    	verify(mockEventListener, times(0)).onConnectionStateChange(any(ConnectionStateChange.class));
    }
    
    @Test
    public void testDisconnectInConnectingStateIsIgnored() {
    	connection.connect();
    	
    	connection.disconnect();
    	
    	verify(mockUnderlyingConnection, times(0)).close();
    	verify(mockEventListener, times(1)).onConnectionStateChange(any(ConnectionStateChange.class));
    }
    
    @Test
    public void testDisconnectInDisconnectingStateIsIgnored() {
    	connection.connect();
    	connection.onMessage("{\"event\":\"pusher:connection_established\",\"data\":\"{\\\"socket_id\\\":\\\"21112.816204\\\"}\"}");
    	
    	connection.disconnect();
    	
    	verify(mockUnderlyingConnection, times(1)).close();
    	verify(mockEventListener, times(3)).onConnectionStateChange(any(ConnectionStateChange.class));
    }
    
    /* end of tests */
    
    private void connect() {
	connection.connect();
	connection.onMessage("{\"event\":\"pusher:connection_established\",\"data\":\"{\\\"socket_id\\\":\\\"21112.816204\\\"}\"}");	
    }
}