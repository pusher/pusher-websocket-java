package com.pusher.client.channel;

/**
 * <p>
 * Client applications should implement this interface if they want to be
 * notified when events are received on a public or private channel.
 * </p>
 *
 * <p>
 * To bind your implementation of this interface to a channel, either:
 * <ul>
 * <li>Call {@link com.pusher.client.Pusher#subscribe(String)} to subscribe and
 * receive an instance of {@link Channel}.</li>
 * <li>Call {@link Channel#bind(String, SubscriptionEventListener)} to bind your
 * listener to a specified event.</li>
 * </ul>
 *
 * Or, call
 * {@link com.pusher.client.Pusher#subscribe(String, ChannelEventListener, String...)}
 * to subscribe to a channel and bind your listener to one or more events at the
 * same time.
 * </p>
 *
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
     * @param resumeSuccessful
     *            If true, the subscription will resume from the requested point,
     *            all "missed" messages were available and will be forwarded.
     *            If false, the subscription could not be resumed from the requested
     *            point as some historical data was not available. What data was
     *            available will be delivered to the appropriate {@link
     *            SubscriptionEventListener#onEvent} callbacks.
     *            If null, resume was not requested for this subscription.
     */
    void onSubscriptionSucceeded(String channelName, Boolean resumeSuccessful);

    /**
     * <p>
     * Callback that is fired if a subscription request cannot be completed.
     * </p>
     *
     * @param channelName
     *            The name of the channel for which the subscription failed.
     * @param errorCode
     *            A code classifying the type of error
     * @param errorDescription
     *            A human readable description of the error
     */
    void onSubscriptionFailed(String channelName, Integer errorCode, String errorDescription);

}
