package com.pusher.client.example;

import com.pusher.client.Pusher;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;

public class ExampleApp implements ConnectionEventListener, ChannelEventListener {

    private final Pusher pusher;
    
    public static void main(String[] args) {
	new ExampleApp();
    }
    
    public ExampleApp() {
	
	pusher = new Pusher("161717a55e65825bacf1");
	pusher.connect(this);
    }

    /* ConnectionEventListener implementation */
    
    @Override
    public void onConnectionStateChange(ConnectionStateChange change) {
	
	System.out.println(String.format("Connection state changed from [%s] to [%s]", change.getPreviousState(), change.getCurrentState()));
	
	if(change.getCurrentState() == ConnectionState.CONNECTED) {
	    pusher.subscribe("my-channel", this, "my-event");
	}
    }

    /* ChannelEventListener implementation */
    
    @Override
    public void onEvent(String channelName, String eventName, String data) {
	
	System.out.println(String.format("Received event [%s] on channel [%s] with data [%s]", eventName, channelName, data));
    }

    @Override
    public void onSubscriptionSucceeded(Channel channel) {
	
	System.out.println(String.format("Subscription to channel [%s] succeeded", channel.getName()));
    }
}