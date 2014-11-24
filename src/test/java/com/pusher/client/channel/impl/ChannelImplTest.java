package com.pusher.client.channel.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.ChannelState;
import com.pusher.client.util.Factory;
import com.pusher.client.util.InstantExecutor;

@RunWith(MockitoJUnitRunner.class)
public class ChannelImplTest {

    private static final String EVENT_NAME = "my-event";
    protected ChannelImpl channel;
    protected @Mock Factory factory;
    private @Mock ChannelEventListener mockListener;

    @Before
    public void setUp() {
        when(factory.getEventQueue()).thenReturn(new InstantExecutor());

        this.mockListener = getEventListener();
        this.channel = newInstance(getChannelName());
        this.channel.setEventListener(mockListener);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullChannelNameThrowsException() {
        newInstance(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrivateChannelName() {
        newInstance("private-my-channel");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPresenceChannelName() {
        newInstance("presence-my-channel");
    }

    @Test
    public void testPublicChannelName() {
        newInstance("my-channel");
    }

    @Test
    public void testGetNameReturnsName() {
        assertEquals(getChannelName(), channel.getName());
    }

    @Test
    public void testReturnsCorrectSubscribeMessage() {
        assertEquals("{\"event\":\"pusher:subscribe\",\"data\":{\"channel\":\""
                + getChannelName() + "\"}}", channel.toSubscribeMessage());
    }

    @Test
    public void testReturnsCorrectUnsubscribeMessage() {
        assertEquals("{\"event\":\"pusher:unsubscribe\",\"data\":{\"channel\":\""
                + getChannelName() + "\"}}", channel.toUnsubscribeMessage());
    }

    @Test
    public void testInternalSubscriptionSucceededMessageIsTranslatedToASubscriptionSuccessfulCallback() {
        channel.bind(EVENT_NAME, mockListener);
        channel
                .onMessage(
                        "pusher_internal:subscription_succeeded",
                        "{\"event\":\"pusher_internal:subscription_succeeded\",\"data\":\"{}\",\"channel\":\""
                                + getChannelName() + "\"}");

        verify(mockListener).onSubscriptionSucceeded(getChannelName());
    }

    @Test
    public void testDataIsExtractedFromMessageAndPassedToSingleListener() {
        // {"event":"my-event","data":"{\"some\":\"data\"}","channel":"my-channel"}
        channel.bind(EVENT_NAME, mockListener);
        channel.onMessage(EVENT_NAME,
                "{\"event\":\"event1\",\"data\":\"{\\\"fish\\\":\\\"chips\\\"}\"}");

        verify(mockListener).onEvent(getChannelName(), EVENT_NAME,
                "{\"fish\":\"chips\"}");
    }

    @Test
    public void testDataIsExtractedFromMessageAndPassedToMultipleListeners() {
        ChannelEventListener mockListener2 = getEventListener();

        channel.bind(EVENT_NAME, mockListener);
        channel.bind(EVENT_NAME, mockListener2);
        channel.onMessage(EVENT_NAME,
                "{\"event\":\"event1\",\"data\":\"{\\\"fish\\\":\\\"chips\\\"}\"}");

        verify(mockListener).onEvent(getChannelName(), EVENT_NAME,
                "{\"fish\":\"chips\"}");
        verify(mockListener2).onEvent(getChannelName(), EVENT_NAME,
                "{\"fish\":\"chips\"}");
    }

    @Test
    public void testEventIsNotPassedOnIfThereAreNoMatchingListeners() {

        channel.bind(EVENT_NAME, mockListener);
        channel.onMessage("DifferentEventName",
                "{\"event\":\"event1\",\"data\":{\"fish\":\"chips\"}}");

        verify(mockListener, never())
                .onEvent(anyString(), anyString(), anyString());
    }

    @Test
    public void testEventIsNotPassedOnIfListenerHasUnboundFromEvent() {

        channel.bind(EVENT_NAME, mockListener);
        channel.unbind(EVENT_NAME, mockListener);
        channel.onMessage(EVENT_NAME,
                "{\"event\":\"event1\",\"data\":\"{\\\"fish\\\":\\\"chips\\\"}\"}");

        verify(mockListener, never())
                .onEvent(anyString(), anyString(), anyString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBindWithNullEventNameThrowsException() {
        channel.bind(null, mockListener);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBindWithNullListenerThrowsException() {
        channel.bind(EVENT_NAME, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBindToInternalEventThrowsException() {
        channel.bind("pusher_internal:subscription_succeeded", mockListener);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnbindWithNullEventNameThrowsException() {
        channel.bind(EVENT_NAME, mockListener);
        channel.unbind(null, mockListener);
    }

    @Test(expected = IllegalArgumentException.class)
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
    public void testUpdateStateToSubscribeSentDoesNotNotifyListenerThatSubscriptionSucceeded() {
        channel.bind(EVENT_NAME, mockListener);
        channel.updateState(ChannelState.SUBSCRIBE_SENT);

        verify(mockListener, never()).onSubscriptionSucceeded(getChannelName());
    }

    @Test
    public void testUpdateStateToSubscribedNotifiesListenerThatSubscriptionSucceeded() {
        channel.bind(EVENT_NAME, mockListener);
        channel.updateState(ChannelState.SUBSCRIBE_SENT);
        channel.updateState(ChannelState.SUBSCRIBED);

        verify(mockListener).onSubscriptionSucceeded(getChannelName());
    }

    @Test(expected = IllegalStateException.class)
    public void testBindWhenInUnsubscribedStateThrowsException() {
        channel.updateState(ChannelState.UNSUBSCRIBED);
        channel.bind(EVENT_NAME, mockListener);
    }

    @Test(expected = IllegalStateException.class)
    public void testUnbindWhenInUnsubscribedStateThrowsException() {
        channel.bind(EVENT_NAME, mockListener);
        channel.updateState(ChannelState.UNSUBSCRIBED);
        channel.unbind(EVENT_NAME, mockListener);
    }

    /* end of tests */

    /**
     * This method is overridden in the test subclasses so that these tests can be
     * run against PrivateChannelImpl and PresenceChannelImpl.
     */
    protected ChannelImpl newInstance(String channelName) {
        return new ChannelImpl(channelName, factory);
    }

    /**
     * This method is overridden in the test subclasses so that the private
     * channel tests can run with a valid private channel name and the presence
     * channel tests can run with a valid presence channel name.
     */
    protected String getChannelName() {
        return "my-channel";
    }

    /**
     * This method is overridden to allow the private and presence channel tests
     * to use the appropriate listener subclass.
     */
    protected ChannelEventListener getEventListener() {
        ChannelEventListener listener = mock(ChannelEventListener.class);
        return listener;
    }
}
