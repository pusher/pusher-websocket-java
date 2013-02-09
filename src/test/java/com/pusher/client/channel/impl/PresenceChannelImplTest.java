package com.pusher.client.channel.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.pusher.client.User;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.PresenceChannelEventListener;
import com.pusher.client.channel.PrivateChannelEventListener;
import com.pusher.client.util.Factory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Factory.class})
public class PresenceChannelImplTest extends PrivateChannelImplTest {

	private static final String AUTH_RESPONSE = "\"auth\":\"a87fe72c6f36272aa4b1:f9db294eae7\",\"channel_data\":\"{\\\"user_id\\\":\\\"51169fc47abac\\\",\\\"user_info\\\":{\\\"name\\\":\\\"Phil Leggetter\\\",\\\"twitter_id\\\":\\\"@leggetter\\\"}}\"";
    private @Mock PresenceChannelEventListener mockEventListener;
    
    @Before
    public void setUp() {
    	super.setUp();
    	when(mockAuthorizer.authorize(eq(getChannelName()), anyString())).thenReturn("{" + AUTH_RESPONSE + "}");
    }
    
    @Test
    @Override
    public void testConstructWithPresenceChannelNameThrowsException() {
	// overridden because this test is not valid for this class - we don't want to throw an exception
    }
    
    @Test
    @Override
    public void testReturnsCorrectSubscribeMessage() {
	
	String message = channel.toSubscribeMessage();
	assertEquals("{\"event\":\"pusher:subscribe\",\"data\":{\"channel\":\"" + getChannelName() + "\"," + AUTH_RESPONSE +"}}", message);
    }

    @Test
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testInternalSubscriptionSucceededMessageIsTranslatedToASubscriptionSuccessfulCallback() {
	
	ArgumentCaptor<Set> argument =  ArgumentCaptor.forClass(Set.class);
	channel.bind("my-event", mockEventListener);
	channel.onMessage("pusher_internal:subscription_succeeded", "{\"event\":\"pusher_internal:subscription_succeeded\",\"data\":\"{\\\"presence\\\":{\\\"count\\\":1,\\\"ids\\\":[\\\"5116a4519575b\\\"],\\\"hash\\\":{\\\"5116a4519575b\\\":{\\\"name\\\":\\\"Phil Leggetter\\\",\\\"twitter_id\\\":\\\"@leggetter\\\"}}}}\",\"channel\":\"" + getChannelName() + "\"}");                                                                              
	
	InOrder inOrder = inOrder(mockEventListener);
	inOrder.verify(mockEventListener).onSubscriptionSucceeded(getChannelName());
	inOrder.verify(mockEventListener).onUserInformationReceived(eq(getChannelName()), argument.capture());
	
	assertEquals(1, argument.getValue().size());
	assertTrue(argument.getValue().toArray()[0] instanceof User);
	
	User user = (User) argument.getValue().toArray()[0];
	assertEquals("5116a4519575b", user.getId());
	assertEquals("{name=Phil Leggetter, twitter_id=@leggetter}", user.getInfo());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testCannotBindIfListenerIsNotAPresenceChannelEventListener() {
	ChannelEventListener listener = mock(PrivateChannelEventListener.class);
	channel.bind("private-myEvent", listener);
    }
    
    /* end of tests */

    @Override
    protected ChannelImpl newInstance(String channelName) {
	return new PresenceChannelImpl(mockConnection, channelName, mockAuthorizer);
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