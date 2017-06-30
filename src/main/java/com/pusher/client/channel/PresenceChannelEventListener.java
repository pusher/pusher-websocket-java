package com.pusher.client.channel;

import java.util.Set;

/**
 * Used to listen for presence specific events as well as those defined by the
 * {@link com.pusher.client.channel.PrivateChannelEventListener
 * PrivateChannelEventListener} and parent interfaces.
 */
public interface PresenceChannelEventListener extends PrivateChannelEventListener {

    /**
     * Called when the subscription has succeeded and an initial list of
     * subscribed users has been received from Pusher.
     *
     * @param channelName
     *            The name of the channel the list is for.
     * @param users
     *            The users.
     */
    void onUsersInformationReceived(String channelName, Set<User> users);

    /**
     * Called when a new user subscribes to the channel.
     *
     * @param channelName
     *            channelName The name of the channel the list is for.
     * @param user
     *            The newly subscribed user.
     */
    void userSubscribed(String channelName, User user);

    /**
     * Called when an existing user unsubscribes from the channel.
     *
     * @param channelName
     *            The name of the channel that the user unsubscribed from.
     * @param user
     *            The user who unsubscribed.
     */
    void userUnsubscribed(String channelName, User user);
}
