package com.pusher.client.channel;

import java.util.Set;

import com.pusher.client.User;

public interface PresenceChannelEventListener extends PrivateChannelEventListener {

    void onUserInformationReceived(String channelName, Set<User> users);

    void userSubscribed(String channelName, User user);

    void userUnsubscribed(String channelName, User user);
}