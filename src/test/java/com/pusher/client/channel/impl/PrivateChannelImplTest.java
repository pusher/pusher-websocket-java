package com.pusher.client.channel.impl;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.pusher.client.util.Factory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Factory.class})
public class PrivateChannelImplTest extends ChannelImplTest {

    @Test
    public void testConstructWithNonPrivateChannelNameThrowsException() {
	
	String[] invalidNames = new String[] {"my-channel", "private:my-channel", "Private-my-channel", "privatemy-channel"};
	for(String invalidName : invalidNames) {
	    try {
		newInstance(invalidName);
		fail("No exception thrown for invalid name: " + invalidName);
	    } catch(IllegalArgumentException e) {
		// exception correctly thrown
	    }
	}
    }

    @Test
    @Override
    public void testConstructWithPrivateChannelNameThrowsException() {
	// Overridden because this test is not valid for this class
    }    
    
    /* end of tests */

    @Override
    protected ChannelImpl newInstance(String channelName) {
	return new PrivateChannelImpl(channelName);
    }

    @Override
    protected String getChannelName() {
	return "private-my-channel";
    }
}