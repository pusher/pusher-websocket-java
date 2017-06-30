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
 *
 */
public interface SubscriptionEventListener {

    /**
     * Callback that is fired whenever an event that this
     * {@linkplain SubscriptionEventListener} has been bound to is received.
     *
     * @param channelName
     *            The name of the channel that the event has been received on.
     *            This is useful if your {@linkplain ChannelEventListener} has
     *            been bound to events on more than one channel.
     * @param eventName
     *            The name of the event that has been received. This will always
     *            be one of the events that your
     *            {@linkplain ChannelEventListener} has been bound to.
     * @param data
     *            The JSON data that was included with the event. This can be
     *            parsed with Google's Gson library, which is a dependency of
     *            this library, or your library of choice.
     */
    void onEvent(String channelName, String eventName, String data);
}
