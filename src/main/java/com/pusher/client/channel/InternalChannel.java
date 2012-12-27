package com.pusher.client.channel;

public interface InternalChannel extends Channel {
    
    String toSubscriptionMessage();

    void onMessage(String event, String message);
    
    void subscribeSent();
}