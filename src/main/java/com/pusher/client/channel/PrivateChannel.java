package com.pusher.client.channel;

public interface PrivateChannel extends Channel {

    void trigger(String eventName, String data);
}
