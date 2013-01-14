package com.pusher.client.channel.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.PresenceChannelEventListener;
import com.pusher.client.channel.PrivateChannelEventListener;
import com.pusher.client.connection.impl.InternalConnection;
import com.pusher.client.util.Factory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Factory.class})
public class PresenceChannelImplTest extends ChannelImplTest {

    private @Mock InternalConnection mockConnection;
    private @Mock PresenceChannelEventListener mockEventListener;
    
    @Test
    @Override
    public void testConstructWithPresenceChannelNameThrowsException() {
	// overridden because this test is not valid for this class - we don't want to throw an exception
    }
    
    @Ignore /* TODO: the subscription_succeeded message is not being parsed as valid JSON for some reason, even though it's copied directly from an actual message */
    @Override
    public void testReturnsCorrectSubscribeMessage() {
	
	String message = channel.toSubscribeMessage("{\"auth\":\"a87fe72c6f36272aa4b1:da71b9f58aa6fdedb52a82d250436406b1bf59d653694c003dfde56a1e932b02\",\"channel_data\":{\"user_id\":\"50e33d5e542d5\",\"user_info\":{\"name\":\"Phil Leggetter\",\"twitter_id\":\"@leggetter\"}}}");
	assertEquals("{\"event\":\"pusher:subscribe\",\"data\":{\"channel\":\"" + getChannelName() + "\",\"auth\":\"a87fe72c6f36272aa4b1:da71b9f58aa6fdedb52a82d250436406b1bf59d653694c003dfde56a1e932b02\",\"channel_data\":{\"user_id\":\"50e33d5e542d5\",\"user_info\":{\"name\":\"Phil Leggetter\",\"twitter_id\":\"@leggetter\"}}}}", message);
    }

    @Ignore /* TODO: the subscription_succeeded message is not being parsed as valid JSON for some reason, even though it's copied directly from an actual message */
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testInternalSubscriptionSucceededMessageIsTranslatedToASubscriptionSuccessfulCallback() {
	
	ArgumentCaptor<Set> argument =  ArgumentCaptor.forClass(Set.class);
	channel.bind("my-event", mockEventListener);
	channel.onMessage("pusher_internal:subscription_succeeded", "{\"event\":\"pusher_internal:subscription_succeeded\",\"data\":\"{\"presence\":{\"count\":1,\"ids\":[\"50e33468acbe7\"],\"hash\":{\"50e33468acbe7\":{\"name\":\"Phil Leggetter\",\"twitter_id\":\"@leggetter\"}}}}\",\"channel\":\"presence-my-channel\"}");                                                                              
	
	InOrder inOrder = inOrder(mockEventListener);
	inOrder.verify(mockEventListener).onSubscriptionSucceeded(getChannelName());
	inOrder.verify(mockEventListener).onUserInformationReceived(getChannelName(), argument.capture());
	
	assertEquals(1, argument.getValue().size());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testCannotBindIfListenerIsNotAPresenceChannelEventListener() {
	ChannelEventListener listener = mock(PrivateChannelEventListener.class);
	channel.bind("private-myEvent", listener);
    }
    
    /* end of tests */

    @Override
    protected ChannelImpl newInstance(String channelName) {
	return new PresenceChannelImpl(mockConnection, channelName);
    }

    @Override
    protected String getChannelName() {
	return "presence-myChannel";
    }
    
    protected ChannelEventListener getEventListener() {
	PresenceChannelEventListener listener = mock(PresenceChannelEventListener.class);
	return listener;
    }
}