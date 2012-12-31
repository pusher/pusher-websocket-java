package com.pusher.client.channel;

public interface PrivateChannel extends Channel {

    boolean trigger(String eventName, String data);
}
