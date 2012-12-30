package com.pusher.client.channel.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.ChannelState;
import com.pusher.client.connection.impl.InternalConnection;
import com.pusher.client.util.Factory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Factory.class})
public class ChannelManagerTest {

    private static final String CHANNEL_NAME = "my-channel";
    private static final String OUTGOING_SUBSCRIBE_MESSAGE = "{\"event\":\"pusher:subscribe\"}";
    private static final String OUTGOING_UNSUBSCRIBE_MESSAGE = "{\"event\":\"pusher:unsubscribe\"}";
    
    private ChannelManager channelManager;
    private @Mock InternalConnection mockConnection;
    private @Mock InternalChannel mockInternalChannel;
    private @Mock ChannelEventListener mockEventListener;
    
    @Before
    public void setUp() {
	
	when(mockInternalChannel.getName()).thenReturn(CHANNEL_NAME);
	when(mockInternalChannel.toSubscribeMessage()).thenReturn(OUTGOING_SUBSCRIBE_MESSAGE);
	when(mockInternalChannel.toUnsubscribeMessage()).thenReturn(OUTGOING_UNSUBSCRIBE_MESSAGE);
	
	this.channelManager = new ChannelManager(mockConnection);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testConstructWithNullConnectionThrowsException() {
	new ChannelManager(null);
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
}