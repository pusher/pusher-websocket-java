package com.pusher.client.user;

import com.pusher.client.channel.SubscriptionEventListener;

/**
 * An object that represents a user on a Pusher connection. An implementation of this
 * interface is returned when you call {@link com.pusher.client.Pusher#user()}.
 */
public interface User {
    /**
     * @return The user id of the signed in user. Null if no user is signed in;
     */
    String userId();

    /**
     * Binds a {@link SubscriptionEventListener} to an event. The
     * {@link SubscriptionEventListener} will be notified whenever the specified
     * event is received for this user.
     *
     * @param eventName The name of the event to listen to.
     * @param listener  A listener to receive notifications when the event is
     *                  received.
     * @throws IllegalArgumentException If either of the following are true:
     *                                  <ul>
     *                                  <li>The name of the event is null.</li>
     *                                  <li>The {@link SubscriptionEventListener} is null.</li>
     *                                  </ul>
     */
    void bind(String eventName, SubscriptionEventListener listener);

    /**
     * Binds a {@link SubscriptionEventListener} to all events. The
     * {@link SubscriptionEventListener} will be notified whenever an
     * event is received for this user.
     *
     * @param listener A listener to receive notifications when the event is
     *                 received.
     * @throws IllegalArgumentException If the {@link SubscriptionEventListener} is null.
     */
    void bindGlobal(SubscriptionEventListener listener);

    /**
     * <p>
     * Unbinds a previously bound {@link SubscriptionEventListener} from an
     * event. The {@link SubscriptionEventListener} will no longer be notified
     * whenever the specified event is received for this user.
     * </p>
     *
     * @param eventName The name of the event to stop listening to.
     * @param listener  The listener to unbind from the event.
     * @throws IllegalArgumentException If either of the following are true:
     *                                  <ul>
     *                                  <li>The name of the event is null.</li>
     *                                  <li>The {@link SubscriptionEventListener} is null.</li>
     *                                  </ul>
     */
    void unbind(String eventName, SubscriptionEventListener listener);

    /**
     * <p>
     * Unbinds a previously bound {@link SubscriptionEventListener} from global
     * events. The {@link SubscriptionEventListener} will no longer be notified
     * whenever the any event is received for this user.
     * </p>
     *
     * @param listener The listener to unbind from the event.
     * @throws IllegalArgumentException If the {@link SubscriptionEventListener} is null.
     */
    void unbindGlobal(SubscriptionEventListener listener);
}
