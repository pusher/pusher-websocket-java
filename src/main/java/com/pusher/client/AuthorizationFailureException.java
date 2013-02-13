package com.pusher.client;

public class AuthorizationFailureException extends RuntimeException {

    private static final long serialVersionUID = -7208133561904200801L;

    public AuthorizationFailureException() {
	super();
    }
    
    public AuthorizationFailureException(String msg) {
    	super(msg);
    }
    
    public AuthorizationFailureException(Exception cause) {
	super(cause);
    }
    
    public AuthorizationFailureException(String msg, Exception cause) {
	super(msg, cause);
    }
}