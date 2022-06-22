package com.pusher.client.util;

import org.junit.Test;

import com.pusher.client.AuthenticationFailureException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HttpUserAuthenticatorTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructWithMalformedURLThrowsRuntimeException() {
        new HttpUserAuthenticator("bad url");
    }

    @Test
    public void testHTTPURLIsIdentifiedAsSSL() {
        final HttpUserAuthenticator auth = new HttpUserAuthenticator("http://example.com/auth");
        assertFalse(auth.isSSL());
    }

    @Test
    public void testHTTPSURLIsIdentifiedAsSSL() {
        final HttpUserAuthenticator auth = new HttpUserAuthenticator("https://example.com/auth");
        assertTrue(auth.isSSL());
    }

    @Test(expected = AuthenticationFailureException.class)
    public void testNon200ResponseThrowsAuthenticationFailtureException() {
        final HttpUserAuthenticator auth = new HttpUserAuthenticator("https://127.0.0.1/no-way-this-is-a-valid-url");
        auth.authenticate("some socket id");
    }
}
