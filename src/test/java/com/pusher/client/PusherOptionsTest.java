package com.pusher.client;

import static org.junit.Assert.assertFalse;
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
    public void testEncryptedInitializedAsFalse() {
    	assertFalse(pusherOptions.isEncrypted());
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
}