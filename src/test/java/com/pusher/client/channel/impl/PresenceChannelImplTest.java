package com.pusher.client.channel.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.pusher.client.connection.impl.InternalConnection;
import com.pusher.client.util.Factory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Factory.class})
public class PresenceChannelImplTest extends ChannelImplTest {

    private @Mock InternalConnection mockConnection;
    
    @Test
    @Override
    public void testConstructWithPresenceChannelNameThrowsException() {
	// overridden because this test is not valid for this class - we don't want to throw an exception
    }
    
    @Test
    @Override
    public void testReturnsCorrectSubscribeMessage() {
	// TODO
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
    
    
}