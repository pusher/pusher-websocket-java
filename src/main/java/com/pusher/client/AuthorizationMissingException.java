package com.pusher.client;

public class AuthorizationMissingException extends RuntimeException{

    public AuthorizationMissingException() {
        super();
    }

    public AuthorizationMissingException(final String msg) {
        super(msg);
    }

    public AuthorizationMissingException(final Throwable cause) {
        super(cause);
    }

    public AuthorizationMissingException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
