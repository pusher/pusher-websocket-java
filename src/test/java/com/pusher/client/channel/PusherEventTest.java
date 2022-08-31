package com.pusher.client.channel;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PusherEventTest {

    @Before
    public void setUp() {
    }

    @Test
    public void testChannelNameIsExtracted() {
        final PusherEvent e = PusherEvent.fromJson(
                "{\"channel\": \"my-channel\", \"event\":\"my-event\",\"data\":\"{\\\"fish\\\":\\\"chips\\\"}\"}"
        );
        assertEquals("my-channel", e.getChannelName());
    }

    @Test
    public void testEventNameIsExtracted() {
        final PusherEvent e = PusherEvent.fromJson(
                "{\"channel\": \"my-channel\", \"event\":\"my-event\",\"data\":\"{\\\"fish\\\":\\\"chips\\\"}\"}"
        );
        assertEquals("my-event", e.getEventName());
    }

    @Test
    public void testDataIsExtracted() {
        final PusherEvent e = PusherEvent.fromJson(
                "{\"channel\": \"my-channel\", \"event\":\"my-event\",\"data\":\"{\\\"fish\\\":\\\"chips\\\"}\"}"
        );
        assertEquals("{\"fish\":\"chips\"}", e.getData());
    }

    @Test
    public void testUserIdIsExtracted() {
        final PusherEvent e = PusherEvent.fromJson(
                "{\"channel\": \"my-channel\", \"event\":\"my-event\",\"data\":\"{\\\"fish\\\":\\\"chips\\\"}\", \"user_id\": \"my-user-id\"}"
        );
        assertEquals("my-user-id", e.getUserId());
    }
}
