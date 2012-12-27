package com.pusher.client;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.PublicChannel;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.InternalConnection;
import com.pusher.client.util.Factory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Factory.class})
public class PusherTest {

    private static final String API_KEY = "123456";
    private static final String PUBLIC_CHANNEL_NAME = "my-channel";
    
    private Pusher pusher;
    private @Mock InternalConnection mockConnection;
    private @Mock ConnectionEventListener mockConnectionEventListener;
    private @Mock PublicChannel mockPublicChannel;
    private @Mock ChannelEventListener mockChannelEventListener;
    
    @Before
    public void setUp()
    {
	PowerMockito.mockStatic(Factory.class);
	when(Factory.newConnection(API_KEY)).thenReturn(mockConnection);
	when(Factory.newPublicChannel(PUBLIC_CHANNEL_NAME)).thenReturn(mockPublicChannel);
	
	this.pusher = new Pusher(API_KEY);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullAPIKeyThrowsIllegalArgumentException() {
	new Pusher(null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testEmptyAPIKeyThrowsIllegalArgumentException() {
	new Pusher("");
    }
    
    @Test
    public void testCreatesConnectionObjectWhenConstructed() {
	assertNotNull(pusher.getConnection());
	assertSame(mockConnection, pusher.getConnection());
    }
    
    @Test
    public void testConnectCallIsDelegatedToUnderlyingConnection() {
	pusher.connect();
	verify(mockConnection).connect();
    }
    
    @Test
    public void testConnectCallWithListenerIsDelegatedToUnderlyingConnection() {
	pusher.connect(mockConnectionEventListener);
	
	InOrder inOrder = inOrder(mockConnection);
	inOrder.verify(mockConnection).setEventListener(mockConnectionEventListener);
	inOrder.verify(mockConnection).connect();
    }
    
    @Test
    public void testSubscribeCreatesPublicChannelAndPassesItToTheConnection() {
	when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
	pusher.subscribe(PUBLIC_CHANNEL_NAME, mockChannelEventListener);
	
	verify(mockConnection).subscribeTo(mockPublicChannel);
    }
    
    @Test
    public void testSubscribeWithEventNamesCreatesPublicChannelAndBindsTheListenerToTheEvents() {
	when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
	pusher.subscribe(PUBLIC_CHANNEL_NAME, mockChannelEventListener, "event1", "event2");
	
	verify(mockPublicChannel).bind("event1", mockChannelEventListener);
	verify(mockPublicChannel).bind("event2", mockChannelEventListener);
    }

    @Test(expected=IllegalStateException.class)
    public void testSubscribeWhenConnectingThrowsException() {
	when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTING);
	
	pusher.subscribe(PUBLIC_CHANNEL_NAME, mockChannelEventListener);
    }
    
    @Test(expected=IllegalStateException.class)
    public void testSubscribeWhenDisconnectedThrowsException() {
	when(mockConnection.getState()).thenReturn(ConnectionState.DISCONNECTED);
	
	pusher.subscribe(PUBLIC_CHANNEL_NAME, mockChannelEventListener);
    }
}