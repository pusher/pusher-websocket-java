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
public interface SubscriptionEventListener {
    /**
     * Callback that is fired whenever an event that this
     * {@linkplain SubscriptionEventListener} has been bound to is received.
     *
     * @param event A PusherEvent object which exposes the whole event.
     *              See {@linkplain PusherEvent} for more.
     */
    void onEvent(final PusherEvent event);

    /**
     * Callback that is fired whenever an unexpected error occurs processing
     * for this {@linkplain SubscriptionEventListener}.
     *
     * @param message A description of the problem.
     * @param e       An associated exception, if available.
     */
    default void onError(String message, Exception e) {
        // No-op
        return;
    }
}
