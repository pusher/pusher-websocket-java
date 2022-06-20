package com.pusher.client;

/**
 * Used to indicate an authentication failure.
 *
 * @see com.pusher.client.UserAuthenticator
 */
public class AuthenticationFailureException extends RuntimeException {

    private static final long serialVersionUID = -7208133561904200801L;

    public AuthenticationFailureException() {
        super();
    }

    public AuthenticationFailureException(final String msg) {
        super(msg);
    }

    public AuthenticationFailureException(final Exception cause) {
        super(cause);
    }

    public AuthenticationFailureException(final String msg, final Exception cause) {
        super(msg, cause);
    }
}
