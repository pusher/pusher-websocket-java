package com.pusher.client.channel;

/**
 * Client applications should implement this interface if they want to be
 * notified when events are received on a public or private channel.
 *
 * <p>
 * To bind your implementation of this interface to a channel, either:
 * </p>
 * <ul>
 * <li>Call {@link com.pusher.client.Pusher#subscribe(String)} to subscribe and
 * receive an instance of {@link Channel}.</li>
 * <li>Call {@link Channel#bind(String, SubscriptionEventListener)} to bind your
 * listener to a specified event.</li>
 * </ul>
 * 
 * <p>
 * Or, call
 * {@link com.pusher.client.Pusher#subscribe(String, ChannelEventListener, String...)}
 * to subscribe to a channel and bind your listener to one or more events at the
 * same time.
 * </p>
 */
public interface ChannelEventListener extends SubscriptionEventListener {

    /**
     * <p>
     * Callback that is fired when a subscription success acknowledgement
     * message is received from Pusher after subscribing to the channel.
     * </p>
     *
     * <p>
     * For public channels this callback will be more or less immediate,
     * assuming that you are connected to Pusher at the time of subscription.
     * For private channels this callback will not be fired unless you are
     * successfully authenticated.
     * </p>
     *
     * @param channelName
     *            The name of the channel that was successfully subscribed.
     */
    void onSubscriptionSucceeded(String channelName);
}
