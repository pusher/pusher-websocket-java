package com.pusher.client.channel;

import java.util.Set;

import com.pusher.client.User;

public interface PresenceChannel extends PrivateChannel {

    Set<User> getUsers();
    
    User getMe();
}
