package com.pusher.client.util;

import com.pusher.client.Authorizer;

/**
 * @deprecated Please use {@link com.pusher.client.util.HttpChannelAuthorizer}
 */
@Deprecated
public class HttpAuthorizer
        extends HttpChannelAuthorizer
        implements Authorizer {

    /**
     * Creates a new authorizer.
     *
     * @param endPoint The endpoint to be called when authenticating.
     */
    public HttpAuthorizer(final String endPoint) {
        super(endPoint);
    }

    /**
     * Creates a new authorizer.
     *
     * @param endPoint          The endpoint to be called when authenticating.
     * @param connectionFactory a custom connection factory to be used for building the connection
     */
    public HttpAuthorizer(
            final String endPoint,
            final ConnectionFactory connectionFactory
    ) {
        super(endPoint, connectionFactory);
    }
}
