package com.pusher.client.channel.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.pusher.client.AuthorizationFailureException;
import com.pusher.client.Authorizer;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.ChannelState;
import com.pusher.client.channel.PrivateChannelEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.connection.impl.InternalConnection;
import com.pusher.client.util.Factory;
import com.pusher.client.util.InstantExecutor;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Factory.class})
public class ChannelManagerTest {

    private static final String CHANNEL_NAME = "my-channel";
    private static final String PRIVATE_CHANNEL_NAME = "private-my-channel";
    private static final String OUTGOING_SUBSCRIBE_MESSAGE = "{\"event\":\"pusher:subscribe\"}";
    private static final String OUTGOING_UNSUBSCRIBE_MESSAGE = "{\"event\":\"pusher:unsubscribe\"}";
    private static final String SOCKET_ID = "21234.41243";
    private static final String AUTH_RESPONSE = "{\"auth\":\"appKey:123456\"}";
    private static final String PRIVATE_OUTGOING_SUBSCRIBE_MESSAGE = "{\"event\":\"pusher:subscribe\", \"data\":{}}";
    
    private ChannelManager channelManager;
    private @Mock PusherOptions mockPusherOptions;
    private @Mock Authorizer mockAuthorizer;
    private @Mock InternalConnection mockConnection;
    private @Mock InternalChannel mockInternalChannel;
    private @Mock ChannelEventListener mockEventListener;
    private @Mock PrivateChannelImpl mockPrivateChannel;
    private @Mock PrivateChannelEventListener mockPrivateChannelEventListener;
    
    @Before
    public void setUp() throws AuthorizationFailureException {
	
	PowerMockito.mockStatic(Factory.class);
	
	when(Factory.getEventQueue()).thenReturn(new InstantExecutor());
	when(mockInternalChannel.getName()).thenReturn(CHANNEL_NAME);
	when(mockInternalChannel.toSubscribeMessage()).thenReturn(OUTGOING_SUBSCRIBE_MESSAGE);
	when(mockInternalChannel.toUnsubscribeMessage()).thenReturn(OUTGOING_UNSUBSCRIBE_MESSAGE);
	when(mockConnection.getSocketId()).thenReturn(SOCKET_ID);
	when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
	when(mockPusherOptions.getAuthorizer()).thenReturn(mockAuthorizer);
	when(mockPrivateChannel.getName()).thenReturn(PRIVATE_CHANNEL_NAME);
	when(mockAuthorizer.authorize(PRIVATE_CHANNEL_NAME, SOCKET_ID)).thenReturn(AUTH_RESPONSE);
	when(mockPrivateChannel.toSubscribeMessage(AUTH_RESPONSE)).thenReturn(PRIVATE_OUTGOING_SUBSCRIBE_MESSAGE);
	
	this.channelManager = new ChannelManager(mockConnection, mockPusherOptions);
    }
    
    @Test
    public void testRegistersAsAConnectionListenerWhenConstructed() {
	verify(mockConnection).bind(ConnectionState.CONNECTED, channelManager);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testConstructWithNullConnectionThrowsException() {
	new ChannelManager(null, mockPusherOptions);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testConstructWithNullPusherOptionsThrowsException() {
	new ChannelManager(mockConnection, null);
    }
    
    @Test
    public void testSubscribeWithAListenerAndNoEventsSubscribes() {
	channelManager.subscribeTo(mockInternalChannel, mockEventListener);
	
	verify(mockConnection).sendMessage(OUTGOING_SUBSCRIBE_MESSAGE);
	verify(mockInternalChannel, never()).bind(anyString(), any(ChannelEventListener.class));
    }
    
    @Test
    public void testSubscribeWithAListenerAndEventsBindsTheListenerToTheEventsBeforeSubscribing() {
	channelManager.subscribeTo(mockInternalChannel, mockEventListener, "event1", "event2");
	
	verify(mockInternalChannel).bind("event1", mockEventListener);
	verify(mockInternalChannel).bind("event2", mockEventListener);
	verify(mockConnection).sendMessage(OUTGOING_SUBSCRIBE_MESSAGE);
    }
    
    @Test
    public void testSubscribeSetsStatusOfChannelToSubscribeSent() {
	channelManager.subscribeTo(mockInternalChannel, mockEventListener);
	verify(mockInternalChannel).updateState(ChannelState.SUBSCRIBE_SENT);
    }
    
    @Test
    public void testSubscribeWithANullListenerAndNoEventsSubscribes() {
	channelManager.subscribeTo(mockInternalChannel, null);
	
	verify(mockConnection).sendMessage(OUTGOING_SUBSCRIBE_MESSAGE);
	verify(mockInternalChannel, never()).bind(anyString(), any(ChannelEventListener.class));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testSubscribeWithNullChannelThrowsException() {
	channelManager.subscribeTo(null, mockEventListener);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testSubscribeWithADuplicateNameThrowsException() {
	channelManager.subscribeTo(mockInternalChannel, mockEventListener);
	channelManager.subscribeTo(mockInternalChannel, mockEventListener);
    }
    
    @Test
    public void testSubscribeToPrivateChannelAuthenticatesAndSubscribes() throws AuthorizationFailureException {
	channelManager.subscribeTo(mockPrivateChannel, mockPrivateChannelEventListener);
	
	verify(mockConnection).getSocketId();
	verify(mockAuthorizer).authorize(PRIVATE_CHANNEL_NAME, SOCKET_ID);
	verify(mockPrivateChannel).toSubscribeMessage(AUTH_RESPONSE);
	verify(mockConnection).sendMessage(PRIVATE_OUTGOING_SUBSCRIBE_MESSAGE);
    }
    
    @Test
    public void testSubscribeToPrivateChannelWhenAuthorizerThrowsExceptionPassesItToListener() throws AuthorizationFailureException {
	AuthorizationFailureException e = new AuthorizationFailureException();
	doThrow(e).when(mockAuthorizer).authorize(PRIVATE_CHANNEL_NAME, SOCKET_ID);
	
	channelManager.subscribeTo(mockPrivateChannel, mockPrivateChannelEventListener);
	
	verify(mockPrivateChannelEventListener).onAuthenticationFailure("Encountered an exception during authorization", e);
	verify(mockPrivateChannel, never()).toSubscribeMessage(anyString());
	verify(mockConnection, never()).sendMessage(anyString());
    }
    
    @Test
    public void testSubscribeWhileDisconnectedQueuesSubscriptionUntilConnectedCallbackIsReceived() {
	when(mockConnection.getState()).thenReturn(ConnectionState.DISCONNECTED);
	
	channelManager.subscribeTo(mockInternalChannel, mockEventListener);
	verify(mockConnection, never()).sendMessage(anyString());
	
	channelManager.onConnectionStateChange(new ConnectionStateChange(ConnectionState.CONNECTING, ConnectionState.CONNECTED));
	verify(mockConnection).sendMessage(OUTGOING_SUBSCRIBE_MESSAGE);
    }
    
    @Test
    public void testReceiveMessageForSubscribedChannelPassesItToChannel() {
	channelManager.subscribeTo(mockInternalChannel, mockEventListener, "my-event");
	channelManager.onMessage("my-event", "{\"event\":\"my-event\",\"data\":{\"fish\":\"chips\"},\"channel\":\"" + CHANNEL_NAME + "\"}");
	
	verify(mockInternalChannel).onMessage("my-event", "{\"event\":\"my-event\",\"data\":{\"fish\":\"chips\"},\"channel\":\"" + CHANNEL_NAME + "\"}");
    }
    
    @Test
    public void testReceiveMessageWithNoMatchingChannelIsIgnoredAndDoesNotThrowException() {
	channelManager.subscribeTo(mockInternalChannel, mockEventListener, "my-event");
	channelManager.onMessage("my-event", "{\"event\":\"my-event\",\"data\":{\"fish\":\"chips\"},\"channel\":\"" + "DIFFERENT_CHANNEL_NAME" + "\"}");
	
	verify(mockInternalChannel, never()).onMessage(anyString(), anyString());
    }
    
    @Test
    public void testReceiveMessageWithNoChannelIsIgnoredAndDoesNotThrowException() {
	channelManager.onMessage("connection_established", "{\"event\":\"connection_established\",\"data\":{\"socket_id\":\"21098.967780\"}}");
    }
    
    @Test
    public void testUnsubscribeFromSubscribedChannelUnsubscribes() {
	channelManager.subscribeTo(mockInternalChannel, mockEventListener);
	channelManager.unsubscribeFrom(CHANNEL_NAME);
	
	verify(mockConnection).sendMessage(OUTGOING_UNSUBSCRIBE_MESSAGE);
    }
    
    @Test
    public void testUnsubscribeFromSubscribedChannelSetsStatusOfChannelToUnsubscribed() {
	channelManager.subscribeTo(mockInternalChannel, mockEventListener);
	channelManager.unsubscribeFrom(CHANNEL_NAME);
	
	verify(mockInternalChannel).updateState(ChannelState.UNSUBSCRIBED);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testUnsubscribeWithNullChannelNameThrowsException() {
	channelManager.unsubscribeFrom(null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testUnsubscribeWhenNotSubscribedThrowsException() {
	channelManager.unsubscribeFrom(CHANNEL_NAME);
    }
    
    @Test
    public void testReceiveMessageAfterUnsubscribeDoesNotPassItToChannel() {
	channelManager.subscribeTo(mockInternalChannel, mockEventListener, "my-event");
	channelManager.unsubscribeFrom(CHANNEL_NAME);
	channelManager.onMessage("my-event", "{\"event\":\"my-event\",\"data\":{\"fish\":\"chips\"},\"channel\":\"" + CHANNEL_NAME + "\"}");
	
	verify(mockInternalChannel, never()).onMessage(anyString(), anyString());	
    }
    
    @Test
    public void testReceiveMessageAfterClearDoesNotPassItToChannel() {
	channelManager.subscribeTo(mockInternalChannel, mockEventListener, "my-event");
	channelManager.clear();
	channelManager.onMessage("my-event", "{\"event\":\"my-event\",\"data\":{\"fish\":\"chips\"},\"channel\":\"" + CHANNEL_NAME + "\"}");
	
	verify(mockInternalChannel, never()).onMessage(anyString(), anyString());	
    }
}