package com.pusher.client.util;

import org.junit.Test;

public class HttpAuthorizerTest {

    @Test(expected=IllegalArgumentException.class)
    public void testConstructWithMalformedURLThrowsRuntimeException() {
	new HttpAuthorizer("bad url");
    }
}