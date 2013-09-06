package com.pusher.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PusherOptionsTest {

    private PusherOptions pusherOptions;
    private @Mock Authorizer mockAuthorizer;
    
    @Before
    public void setUp() {
    	pusherOptions = new PusherOptions();
    }
    
    @Test
    public void testEncryptedInitializedAsTrue() {
    	assert(pusherOptions.isEncrypted());
    }
    
    @Test
    public void testAuthorizerIsInitiallyNull() {
    	assertNull(pusherOptions.getAuthorizer());
    }
    
    @Test
    public void testAuthorizerCanBeSet() {
    	pusherOptions.setAuthorizer(mockAuthorizer);
    	assertSame(mockAuthorizer, pusherOptions.getAuthorizer());
    }
    
    @Test
    public void testEncryptedCanBeSetToTrue() {
    	pusherOptions.setEncrypted(true);
    	assertSame(true, pusherOptions.isEncrypted());
    }
    
    @Test
    public void testSetAuthorizerReturnsSelf() {
    	assertSame(pusherOptions, pusherOptions.setAuthorizer(mockAuthorizer));
    }
    
    @Test
    public void testSetEncryptedReturnsSelf() {
    	assertSame(pusherOptions, pusherOptions.setEncrypted(true));
    }

    @Test
    public void testDefaultHost() {
        assertSame("ws.pusherapp.com", pusherOptions.getHost());
    }

    @Test
    public void testSetHostReturnSelf() {
        assertSame(pusherOptions, pusherOptions.setHost("192.168.1.100"));
    }

    @Test
    public void tesHostCanBeSet() {
        pusherOptions.setHost("192.168.1.100");
        assertEquals(pusherOptions.getHost(), "192.168.1.100");
    }

    @Test
    public void testDefaultWSPort() {
        assertEquals(new Integer(80), pusherOptions.getWsPort());
    }

    @Test
    public void testSetWSPortReturnSelf() {
        assertSame(pusherOptions, pusherOptions.setWsPort(3000));
    }

    @Test
    public void testWSPortCanBeSet() {
        pusherOptions.setWsPort(3000);
        assertEquals(new Integer(3000), pusherOptions.getWsPort());
    }

    @Test
    public void testDefaultWSSPort() {
        assertEquals(new Integer(443), pusherOptions.getWssPort());
    }

    @Test
    public void testSetWSSPortReturnSelf() {
        assertSame(pusherOptions, pusherOptions.setWssPort(3000));
    }

    @Test
    public void testWSSPortCanBeSet() {
        pusherOptions.setWssPort(54);
        assertEquals(new Integer(54), pusherOptions.getWssPort());
    }

}