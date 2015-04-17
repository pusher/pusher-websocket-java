package com.pusher.client;

/**
 * Used to indicate an authorization failure.
 *
 * @see com.pusher.client.Authorizer
 */
public class AuthorizationFailureException extends RuntimeException {

    private static final long serialVersionUID = -7208133561904200801L;

    public AuthorizationFailureException() {
        super();
    }

    public AuthorizationFailureException(final String msg) {
        super(msg);
    }

    public AuthorizationFailureException(final Exception cause) {
        super(cause);
    }

    public AuthorizationFailureException(final String msg, final Exception cause) {
        super(msg, cause);
    }
}
