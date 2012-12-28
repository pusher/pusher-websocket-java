package com.pusher.client.channel;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.pusher.client.util.Factory;
import com.pusher.client.util.InstantExecutor;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Factory.class})
public class PublicChannelTest {

    private static final String CHANNEL_NAME = "my-channel";
    private static final String EVENT_NAME = "my-event";
    private PublicChannel channel;
    private @Mock ChannelEventListener mockListener;
    
    @Before
    public void setUp() {
	PowerMockito.mockStatic(Factory.class);
	when(Factory.getEventQueue()).thenReturn(new InstantExecutor());
	
	this.channel = new PublicChannel(CHANNEL_NAME);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testConstructWithNullChannelNameThrowsException() {
	new PublicChannel(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConstructWithEmptyChannelNameThrowsException() {
	new PublicChannel("");
    }
    
    @Test
    public void testGetNameReturnsName() {
	assertEquals(CHANNEL_NAME, channel.getName());
    }
    
    @Test
    public void testReturnsCorrectSubscribeMessage() {
	assertEquals("{\"event\":\"pusher:subscribe\",\"data\":{\"channel\":\"my-channel\"}}", channel.toSubscribeMessage());
    }

    @Test
    public void testReturnsCorrectUnsubscribeMessage() {
	assertEquals("{\"event\":\"pusher:unsubscribe\",\"data\":{\"channel\":\"my-channel\"}}", channel.toUnsubscribeMessage());
    }
    
    @Test
    public void testDataIsExtractedFromMessageAndPassedToSingleListener() {
	channel.bind(EVENT_NAME, mockListener);
	channel.onMessage(EVENT_NAME, "{\"event\":\"event1\",\"data\":{\"fish\":\"chips\"}}");
	
	verify(mockListener).onEvent(CHANNEL_NAME, EVENT_NAME, "{\"fish\":\"chips\"}");
    }
    
    @Test
    public void testDataIsExtractedFromMessageAndPassedToMultipleListeners() {
	ChannelEventListener mockListener2 = mock(ChannelEventListener.class);
	
	channel.bind(EVENT_NAME, mockListener);
	channel.bind(EVENT_NAME, mockListener2);
	channel.onMessage(EVENT_NAME, "{\"event\":\"event1\",\"data\":{\"fish\":\"chips\"}}");
	
	verify(mockListener).onEvent(CHANNEL_NAME, EVENT_NAME, "{\"fish\":\"chips\"}");
	verify(mockListener2).onEvent(CHANNEL_NAME, EVENT_NAME, "{\"fish\":\"chips\"}");
    }   

    @Test
    public void testEventIsNotPassedOnIfThereAreNoMatchingListeners() {
	
	channel.bind(EVENT_NAME, mockListener);
	channel.onMessage("DifferentEventName", "{\"event\":\"event1\",\"data\":{\"fish\":\"chips\"}}");
	
	verify(mockListener, never()).onEvent(anyString(), anyString(), anyString());
    }    

    @Test
    public void testEventIsNotPassedOnIfListenerHasUnboundFromEvent() {
	
	channel.bind(EVENT_NAME, mockListener);
	channel.unbind(EVENT_NAME, mockListener);
	channel.onMessage(EVENT_NAME, "{\"event\":\"event1\",\"data\":{\"fish\":\"chips\"}}");
	
	verify(mockListener, never()).onEvent(anyString(), anyString(), anyString());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testBindWithNullEventNameThrowsException() {
	channel.bind(null, mockListener);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testBindWithNullListenerThrowsException() {
	channel.bind(EVENT_NAME, null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testUnbindWithNullEventNameThrowsException() {
	channel.bind(EVENT_NAME, mockListener);
	channel.unbind(null, mockListener);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testUnbindWithNullListenerThrowsException() {
	channel.bind(EVENT_NAME, null);
	channel.unbind(EVENT_NAME, null);
    }
    
    @Test
    public void testUnbindWhenListenerIsNotBoundToEventIsIgnoredAndDoesNotThrowException() {
	channel.bind(EVENT_NAME, mockListener);
	channel.unbind("different event name", mockListener);
    }
    
    @Test
    public void testUpdateStateToSubscribeSentNotifiesListenerThatSubscriptionSucceeded() {
	channel.bind(EVENT_NAME, mockListener);
	channel.updateState(ChannelState.SUBSCRIBE_SENT);
	
	verify(mockListener).onSubscriptionSucceeded(channel);
    }
    
    @Test(expected=IllegalStateException.class)
    public void testBindWhenInUnsubscribedStateThrowsException() {
	channel.updateState(ChannelState.UNSUBSCRIBED);
	channel.bind(EVENT_NAME, mockListener);
    }
    
    @Test(expected=IllegalStateException.class)
    public void testUnbindWhenInUnsubscribedStateThrowsException() {
	channel.bind(EVENT_NAME, mockListener);
	channel.updateState(ChannelState.UNSUBSCRIBED);
	channel.unbind(EVENT_NAME, mockListener);
    }    
}