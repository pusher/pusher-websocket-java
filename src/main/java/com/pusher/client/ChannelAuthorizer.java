package com.pusher.client;

/**
 * Subscriptions to {@link com.pusher.client.channel.PrivateChannel Private} and
 * {@link com.pusher.client.channel.PresenceChannel presence} channels need to
 * be authorized. This interface provides an {@link #authorize} method as a mechanism
 * for doing this.
 *
 * <p>
 * See the {@link com.pusher.client.util.HttpChannelAuthorizer HttpChannelAuthorizer} as an
 * example.
 * </p>
 */
public interface ChannelAuthorizer {

    /**
     * Called when a channel subscription is to be authorized.
     *
     * @param channelName
     *            The name of the channel to be authorized.
     * @param socketId
     *            A unique socket connection ID to be used with the
     *            authorization. This uniquely identifies the connection that
     *            the subscription is being authorized for.
     * @return A channel authorization token.
     * @throws AuthorizationFailureException
     *             if the authorization fails.
     */
    String authorize(String channelName, String socketId) throws AuthorizationFailureException;
}
