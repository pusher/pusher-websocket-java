package com.pusher.client.channel.impl;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.gson.Gson;

import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.ChannelState;
import com.pusher.client.channel.PresenceChannelEventListener;
import com.pusher.client.channel.PrivateChannelEventListener;
import com.pusher.client.channel.User;

@RunWith(MockitoJUnitRunner.class)
public class PresenceChannelImplTest extends PrivateChannelImplTest {

    private static final String AUTH_RESPONSE = "\"auth\":\"a87fe72c6f36272aa4b1:f9db294eae7\",\"channel_data\":\"{\\\"user_id\\\":\\\"51169fc47abac\\\",\\\"user_info\\\":{\\\"name\\\":\\\"Phil Leggetter\\\",\\\"twitter_id\\\":\\\"@leggetter\\\"}}\"";
    private static final String AUTH_RESPONSE_NUMERIC_ID = "\"auth\":\"a87fe72c6f36272aa4b1:f9db294eae7\",\"channel_data\":\"{\\\"user_id\\\":51169,\\\"user_info\\\":{\\\"name\\\":\\\"Phil Leggetter\\\",\\\"twitter_id\\\":\\\"@leggetter\\\"}}\"";
    private static final String USER_ID = "5116a4519575b";

    @Mock
    private PresenceChannelEventListener mockEventListener;

    @Override
    @Before
    public void setUp() {
        super.setUp();
        channel.setEventListener(mockEventListener);
        when(mockAuthorizer.authorize(eq(getChannelName()), anyString())).thenReturn("{" + AUTH_RESPONSE + "}");
    }

    @Test
    @Override
    public void testReturnsCorrectSubscribeMessage() {
        String message = channel.toSubscribeMessage();
        assertEquals("{\"event\":\"pusher:subscribe\",\"data\":{\"channel\":\"" + getChannelName() + "\","
                + AUTH_RESPONSE + "}}", message);
    }

    @Test
    public void testReturnsCorrectSubscribeMessageWhenNumericId() {
        when(mockAuthorizer.authorize(eq(getChannelName()), anyString())).thenReturn("{" + AUTH_RESPONSE_NUMERIC_ID + "}");

        String message = channel.toSubscribeMessage();
        assertEquals("{\"event\":\"pusher:subscribe\",\"data\":{\"channel\":\"" + getChannelName() + "\","
                + AUTH_RESPONSE_NUMERIC_ID + "}}", message);
    }

    @Test
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testInternalSubscriptionSucceededMessageIsTranslatedToASubscriptionSuccessfulCallback() {

        final String eventName = "pusher_internal:subscription_succeeded";

        final Map<String, Object> userInfo = new LinkedHashMap<String, Object>();
        userInfo.put("name", "Phil Leggetter");
        userInfo.put("twitter_id", "@leggetter");

        final Map<String, Object> hash = new LinkedHashMap<String, Object>();
        hash.put(USER_ID, userInfo);

        final Map<String, Object> presence = new LinkedHashMap<String, Object>();
        presence.put("count", 1);
        presence.put("ids", new String[] { USER_ID });
        presence.put("hash", hash);

        final Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("presence", presence);

        channel.onMessage(eventName, eventJson(eventName, data, getChannelName()));

        InOrder inOrder = inOrder(mockEventListener);
        inOrder.verify(mockEventListener).onSubscriptionSucceeded(getChannelName());
        ArgumentCaptor<Set> argument = ArgumentCaptor.forClass(Set.class);
        inOrder.verify(mockEventListener).onUsersInformationReceived(eq(getChannelName()), argument.capture());

        assertEquals(1, argument.getValue().size());
        assertTrue(argument.getValue().toArray()[0] instanceof User);

        User user = (User) argument.getValue().toArray()[0];
        assertEquals(USER_ID, user.getId());
        assertEquals("{\"name\":\"Phil Leggetter\",\"twitter_id\":\"@leggetter\"}", user.getInfo());
    }

    @Test
    public void testInternalMemberAddedMessageIsTranslatedToUserSubscribedCallback() {
        ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);

        addUser(USER_ID);

        InOrder inOrder = inOrder(mockEventListener);
        inOrder.verify(mockEventListener).userSubscribed(eq(getChannelName()), argument.capture());

        assertTrue(argument.getValue() instanceof User);

        User user = argument.getValue();
        assertEquals(USER_ID, user.getId());
        assertEquals("{\"name\":\"Phil Leggetter\",\"twitter_id\":\"@leggetter\"}", user.getInfo());
    }

    private void addUser(final String userId) {
        final Map<String, String> userInfo = new LinkedHashMap<String, String>();
        userInfo.put("name", "Phil Leggetter");
        userInfo.put("twitter_id", "@leggetter");

        final Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("user_id", userId);
        data.put("user_info", userInfo);

        final String eventName = "pusher_internal:member_added";

        channel.onMessage(eventName, eventJson(eventName, data, getChannelName()));
    }

    @Test
    public void testInternalMemberRemovedMessageIsTranslatedToUserUnsubscribedCallback() {
        String userId = USER_ID;
        addUser(userId);

        final Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("user_id", userId);

        final String eventName = "pusher_internal:member_removed";

        channel.onMessage(eventName, eventJson(eventName, data, getChannelName()));

        ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
        verify(mockEventListener).userUnsubscribed(eq(getChannelName()), argument.capture());

        assertTrue(argument.getValue() instanceof User);

        User user = argument.getValue();
        assertEquals(userId, user.getId());
        assertEquals("{\"name\":\"Phil Leggetter\",\"twitter_id\":\"@leggetter\"}", user.getInfo());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCannotBindIfListenerIsNotAPresenceChannelEventListener() {
        ChannelEventListener listener = mock(PrivateChannelEventListener.class);
        channel.bind("private-myEvent", listener);
    }

    @Test
    @Override
    public void testUpdateStateToSubscribedNotifiesListenerThatSubscriptionSucceeded() {
        channel.updateState(ChannelState.SUBSCRIBE_SENT);
        channel.updateState(ChannelState.SUBSCRIBED);

        verify(mockEventListener).onSubscriptionSucceeded(getChannelName());
    }

    @Override
    @Test(expected = IllegalArgumentException.class)
    public void testPublicChannelName() {
        newInstance("stuffchannel");
    }

    @Override
    @Test(expected = IllegalArgumentException.class)
    public void testPrivateChannelName() {
        newInstance("private-stuffchannel");
    }

    @Override
    @Test
    public void testPresenceChannelName() {
        newInstance("presence-stuffchannel");
    }

    /* end of tests */

    @Override
    protected ChannelImpl newInstance(final String channelName) {
        return new PresenceChannelImpl(mockConnection, channelName, mockAuthorizer, factory);
    }

    @Override
    protected String getChannelName() {
        return "presence-myChannel";
    }

    @Override
    protected ChannelEventListener getEventListener() {
        return mock(PresenceChannelEventListener.class);
    }

    private static String eventJson(final String eventName, final Map<?, ?> data, final String channelName) {
        return eventJson(eventName, new Gson().toJson(data), channelName);
    }

    private static String eventJson(final String eventName, final String dataString, final String channelName) {
        final Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("event", eventName);
        map.put("data", dataString);
        map.put("channel", channelName);
        return new Gson().toJson(map);
    }
}
