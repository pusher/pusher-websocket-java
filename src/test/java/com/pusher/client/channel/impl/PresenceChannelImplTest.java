package com.pusher.client.channel.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    public void testConstructWithPresenceChannelNameThrowsException() {
        // overridden because this test is not valid for this class - we don't
        // want to throw an exception
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

        ArgumentCaptor<Set> argument = ArgumentCaptor.forClass(Set.class);
        channel.onMessage(
                "pusher_internal:subscription_succeeded",
                "{\"event\":\"pusher_internal:subscription_succeeded\",\"data\":\"{\\\"presence\\\":{\\\"count\\\":1,\\\"ids\\\":[\\\"5116a4519575b\\\"],\\\"hash\\\":{\\\"5116a4519575b\\\":{\\\"name\\\":\\\"Phil Leggetter\\\",\\\"twitter_id\\\":\\\"@leggetter\\\"}}}}\",\"channel\":\""
                        + getChannelName() + "\"}");

        InOrder inOrder = inOrder(mockEventListener);
        inOrder.verify(mockEventListener).onSubscriptionSucceeded(getChannelName());
        inOrder.verify(mockEventListener).onUsersInformationReceived(eq(getChannelName()), argument.capture());

        assertEquals(1, argument.getValue().size());
        assertTrue(argument.getValue().toArray()[0] instanceof User);

        User user = (User) argument.getValue().toArray()[0];
        assertEquals("5116a4519575b", user.getId());
        assertEquals("{name=Phil Leggetter, twitter_id=@leggetter}", user.getInfo());
    }

    @Test
    public void testInternalMemberAddedMessageIsTranslatedToUserSubscribedCallback() {
        ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);

        String userId = "5116a4519575b";
        addUser(userId);

        InOrder inOrder = inOrder(mockEventListener);
        inOrder.verify(mockEventListener).userSubscribed(eq(getChannelName()), argument.capture());

        assertTrue(argument.getValue() instanceof User);

        User user = argument.getValue();
        assertEquals(userId, user.getId());
        assertEquals("{name=Phil Leggetter, twitter_id=@leggetter}", user.getInfo());
    }

    private void addUser(String userId) {
        String userInfo = "{\"name\":\"Phil Leggetter\",\"twitter_id\":\"@leggetter\"}";
        String userJson = "{" + "\"user_id\":\"" + userId + "\"," + "\"user_info\":" + userInfo + "}";
        userJson = new Gson().toJson(userJson);

        channel.onMessage("pusher_internal:member_added", "{" + "\"event\":\"pusher_internal:member_added\","
                + "\"data\":" + userJson + "," + "\"channel\":\"" + getChannelName() + "\"" + "}");
    }

    @Test
    public void testInternalMemberRemovedMessageIsTranslatedToUserSubscribedCallback() {
        String userId = "5116a4519575b";
        addUser(userId);

        ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
        String userJson = "{" + "\"user_id\":\"" + userId + "\"" + "}";
        userJson = new Gson().toJson(userJson);

        channel.onMessage("pusher_internal:member_removed", "{" + "\"event\":\"pusher_internal:member_removed\","
                + "\"data\":" + userJson + "," + "\"channel\":\"" + getChannelName() + "\"" + "}");

        InOrder inOrder = inOrder(mockEventListener);
        inOrder.verify(mockEventListener).userUnsubscribed(eq(getChannelName()), argument.capture());

        assertTrue(argument.getValue() instanceof User);

        User user = argument.getValue();
        assertEquals(userId, user.getId());
        assertEquals("{name=Phil Leggetter, twitter_id=@leggetter}", user.getInfo());
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

    /* end of tests */

    @Override
    protected ChannelImpl newInstance(String channelName) {
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
}