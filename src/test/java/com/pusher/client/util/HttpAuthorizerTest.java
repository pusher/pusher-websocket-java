package com.pusher.client.util;

import junit.framework.Assert;

import org.junit.Test;

public class HttpAuthorizerTest {

	@Test(expected = IllegalArgumentException.class)
	public void testConstructWithMalformedURLThrowsRuntimeException() {
		new HttpAuthorizer("bad url");
	}

	@Test
	public void testHTTPURLIsIdentfiedAsSSL() {
		HttpAuthorizer auth = new HttpAuthorizer("http://example.com/auth");
		Assert.assertFalse(auth.isSSL());
	}
	
	@Test
	public void testHTTPSURLIsIdentfiedAsSSL() {
		HttpAuthorizer auth = new HttpAuthorizer("https://example.com/auth");
		Assert.assertTrue(auth.isSSL());
	}
}