package com.pusher.client.example;

import com.pusher.client.Pusher;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;

public class ExampleApp implements ConnectionEventListener, ChannelEventListener {

    private final Pusher pusher;
    private final String channelName;
    private final String eventName;
    
    public static void main(String[] args) {
	new ExampleApp(args);
    }
    
    public ExampleApp(String[] args) {
	
	String apiKey = (args.length > 0) ? args[0] : "161717a55e65825bacf1";
	channelName = (args.length > 1) ? args[1] : "my-channel";
	eventName = (args.length > 2) ? args[1] : "my-event";
	
	pusher = new Pusher(apiKey);
	pusher.connect(this);
    }

    /* ConnectionEventListener implementation */
    
    @Override
    public void onConnectionStateChange(ConnectionStateChange change) {
	
	System.out.println(String.format("Connection state changed from [%s] to [%s]", change.getPreviousState(), change.getCurrentState()));
	
	if(change.getCurrentState() == ConnectionState.CONNECTED) {
	    pusher.subscribe(channelName, this, eventName);
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

    @Override
    public void onError(String message, String code, Exception e) {
	
	System.out.println(String.format("An error was received with message [%s], code [%s], exception [%s]", message, code, e));
    }
}