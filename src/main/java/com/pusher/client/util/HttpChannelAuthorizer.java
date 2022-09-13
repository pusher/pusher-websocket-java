package com.pusher.client.util;

import com.pusher.client.AuthorizationFailureException;
import com.pusher.client.ChannelAuthorizer;

import java.io.IOException;

/**
 * Used to authorize a {@link com.pusher.client.channel.PrivateChannel
 * private} or {@link com.pusher.client.channel.PresenceChannel presence}
 * channel subscription.
 *
 * <p>
 * Makes an HTTP request to a defined HTTP endpoint. Expects a channel authorization
 * token to be returned.
 * </p>
 *
 * <p>
 * For more information see the <a
 * href="http://pusher.com/docs/authorizing_users">Authorizing Users
 * documentation</a>.
 */

public class HttpChannelAuthorizer extends BaseHttpAuthClient implements ChannelAuthorizer {

    /**
     * Creates a new channel authorizer.
     *
     * @param endPoint The endpoint to be called when authorizing.
     */
    public HttpChannelAuthorizer(final String endPoint) {
        super(endPoint);
    }

    /**
     * Creates a new channel authorizer.
     *
     * @param endPoint          The endpoint to be called when authorizing.
     * @param connectionFactory a custom connection factory to be used for building the connection
     */
    public HttpChannelAuthorizer(final String endPoint, final ConnectionFactory connectionFactory) {
        super(endPoint, connectionFactory);
    }

    @Override
    public String authorize(final String channelName, final String socketId) throws AuthorizationFailureException {
        mConnectionFactory.setChannelName(channelName);
        mConnectionFactory.setSocketId(socketId);
        return performAuthRequest();
    }

    @Override
    protected RuntimeException authFailureException(String msg) {
        return new AuthorizationFailureException(msg);
    }

    @Override
    protected RuntimeException authFailureException(IOException e) {
        return new AuthorizationFailureException(e);
    }
}
