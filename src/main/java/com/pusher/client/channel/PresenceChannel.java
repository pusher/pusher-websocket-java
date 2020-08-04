package com.pusher.client.channel;

import java.util.Set;

/**
 * An object that represents a Pusher presence channel. An implementation of
 * this interface is returned when you call
 * {@link com.pusher.client.Pusher#subscribePresence(String)} or
 * {@link com.pusher.client.Pusher#subscribePresence(String, PresenceChannelEventListener, String...)}
 * .
 */
public interface PresenceChannel extends PrivateChannel {

    /**
     * Gets a set of users currently subscribed to the channel.
     *
     * @return The users.
     */
    Set<User> getUsers();

    /**
     * Gets the user that represents the currently connected client.
     *
     * @return A user.
     */
    User getMe();
}
