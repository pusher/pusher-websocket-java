package com.pusher.client.channel;

/**
 * An object that represents a Pusher channel. An implementation of this
 * interface is returned when you call
 * {@link com.pusher.client.Pusher#subscribe(String)} or
 * {@link com.pusher.client.Pusher#subscribe(String, ChannelEventListener, String...)}
 * .
 *
 */
public interface Channel {

    /**
     * Gets the name of the Pusher channel that this object represents.
     *
     * @return The name of the channel.
     */
    String getName();

    /**
     * Binds a {@link SubscriptionEventListener} to an event. The
     * {@link SubscriptionEventListener} will be notified whenever the specified
     * event is received on this channel.
     *
     * @param eventName
     *            The name of the event to listen to.
     * @param listener
     *            A listener to receive notifications when the event is
     *            received.
     * @throws IllegalArgumentException
     *             If either of the following are true:
     *             <ul>
     *             <li>The name of the event is null.</li>
     *             <li>The {@link SubscriptionEventListener} is null.</li>
     *             </ul>
     * @throws IllegalStateException
     *             If the channel has been unsubscribed by calling
     *             {@link com.pusher.client.Pusher#unsubscribe(String)}. This
     *             puts the {@linkplain Channel} in a terminal state from which
     *             it can no longer be used. To resubscribe, call
     *             {@link com.pusher.client.Pusher#subscribe(String)} or
     *             {@link com.pusher.client.Pusher#subscribe(String, ChannelEventListener, String...)}
     *             again to receive a fresh {@linkplain Channel} instance.
     */
    void bind(String eventName, SubscriptionEventListener listener);

    /**
     * <p>
     * Unbinds a previously bound {@link SubscriptionEventListener} from an
     * event. The {@link SubscriptionEventListener} will no longer be notified
     * whenever the specified event is received on this channel.
     * </p>
     *
     * <p>
     * Calling this method does not unsubscribe from the channel even if there
     * are no more {@link SubscriptionEventListener}s bound to it. If you want
     * to unsubscribe from the channel completely, call
     * {@link com.pusher.client.Pusher#unsubscribe(String)}. It is not necessary
     * to unbind your {@link SubscriptionEventListener}s first.
     * </p>
     *
     * @param eventName
     *            The name of the event to stop listening to.
     * @param listener
     *            The listener to unbind from the event.
     * @throws IllegalArgumentException
     *             If either of the following are true:
     *             <ul>
     *             <li>The name of the event is null.</li>
     *             <li>The {@link SubscriptionEventListener} is null.</li>
     *             </ul>
     * @throws IllegalStateException
     *             If the channel has been unsubscribed by calling
     *             {@link com.pusher.client.Pusher#unsubscribe(String)}. This
     *             puts the {@linkplain Channel} in a terminal state from which
     *             it can no longer be used. To resubscribe, call
     *             {@link com.pusher.client.Pusher#subscribe(String)} or
     *             {@link com.pusher.client.Pusher#subscribe(String, ChannelEventListener, String...)}
     *             again to receive a fresh {@linkplain Channel} instance.
     */
    void unbind(String eventName, SubscriptionEventListener listener);

    /**
     *
     * @return Whether or not the channel is subscribed.
     */
    boolean isSubscribed();
}
