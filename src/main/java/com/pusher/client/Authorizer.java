package com.pusher.client;

/**
 * Subscriptions to {@link com.pusher.client.channel.PrivateChannel Private} and
 * {@link com.pusher.client.channel.PresenceChannel presence} channels need to
 * be authorized. This interface provides an {@link #authorize} as a mechanism
 * for doing this.
 *
 * <p>
 * See the {@link com.pusher.client.util.HttpAuthorizer HttpAuthorizer} as an
 * example.
 * </p>
 */
public interface Authorizer {

    /**
     * Called when a channel is to be authenticated.
     *
     * @param channelName
     *            The name of the channel to be authenticated.
     * @param socketId
     *            A unique socket connection ID to be used with the
     *            authentication. This uniquely identifies the connection that
     *            the subscription is being authenticated for.
     * @return An authentication token.
     * @throws AuthorizationFailureException
     *             if the authentication fails.
     */
    String authorize(String channelName, String socketId) throws AuthorizationFailureException;
}
