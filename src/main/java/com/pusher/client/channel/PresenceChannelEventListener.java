package com.pusher.client.channel;

import java.util.Set;

import com.pusher.client.User;

public interface PresenceChannelEventListener extends PrivateChannelEventListener {

    void onUserInformationReceived(String channelName, Set<User> users);

    void onUserAdded(String channelName, User user);

    void onUserRemoved(String channelName, String id);
}