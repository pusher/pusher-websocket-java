package com.pusher.client;

public class AuthorizationFailureException extends Exception {

    private static final long serialVersionUID = -7208133561904200801L;

    public AuthorizationFailureException() {
	super();
    }
    
    public AuthorizationFailureException(Exception cause) {
	super(cause);
    }
}