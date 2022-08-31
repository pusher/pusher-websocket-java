package com.pusher.client.util;

import com.pusher.client.AuthenticationFailureException;
import com.pusher.client.UserAuthenticator;

import java.io.IOException;

/**
 * Used to authenticate the user when signing in on a Pusher Channels connection.
 *
 * <p>
 * Makes an HTTP request to a defined HTTP endpoint. Expects a user authentication
 * token to be returned.
 * </p>
 *
 * <p>
 * For more information see the <a
 * href="http://pusher.com/docs/authenticating_users">Authenticating Users
 * documentation</a>.
 */
public class HttpUserAuthenticator extends BaseHttpAuthClient implements UserAuthenticator {

    /**
     * Creates a new user authenticator.
     *
     * @param endPoint The endpoint to be called when authenticating.
     */
    public HttpUserAuthenticator(final String endPoint) {
        super(endPoint);
    }

    /**
     * Creates a new user authenticator.
     *
     * @param endPoint          The endpoint to be called when authenticating.
     * @param connectionFactory a custom connection factory to be used for building the connection
     */
    public HttpUserAuthenticator(final String endPoint, final ConnectionFactory connectionFactory) {
        super(endPoint, connectionFactory);
    }

    @Override
    public String authenticate(final String socketId) throws AuthenticationFailureException {
        mConnectionFactory.setSocketId(socketId);
        return performAuthRequest();
    }

    @Override
    protected RuntimeException authFailureException(String msg) {
        return new AuthenticationFailureException(msg);
    }

    @Override
    protected RuntimeException authFailureException(IOException e) {
        return new AuthenticationFailureException(e);
    }
}
