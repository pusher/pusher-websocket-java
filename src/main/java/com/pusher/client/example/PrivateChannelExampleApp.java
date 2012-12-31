package com.pusher.client.example;

import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.PrivateChannel;
import com.pusher.client.channel.PrivateChannelEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.util.HttpAuthorizer;

public class PrivateChannelExampleApp implements ConnectionEventListener, PrivateChannelEventListener {

    private final Pusher pusher;
    private final String channelName;
    private final String eventName;
    
    private PrivateChannel channel;
    
    public static void main(String[] args) {
	new PrivateChannelExampleApp(args);
    }
    
    public PrivateChannelExampleApp(String[] args) {
	
	String apiKey = (args.length > 0) ? args[0] : "a87fe72c6f36272aa4b1";
	channelName = (args.length > 1) ? args[1] : "private-my-channel";
	eventName = (args.length > 2) ? args[2] : "my-event";
	
	HttpAuthorizer authorizer = new HttpAuthorizer("http://www.leggetter.co.uk/pusher/pusher-examples/php/authentication/src/private_auth.php");
	PusherOptions options = new PusherOptions().setAuthorizer(authorizer);
	
	pusher = new Pusher(apiKey, options);
	pusher.connect(this);
    }

    /* ConnectionEventListener implementation */
    
    @Override
    public void onConnectionStateChange(ConnectionStateChange change) {
	
	System.out.println(String.format("Connection state changed from [%s] to [%s]", change.getPreviousState(), change.getCurrentState()));
	
	if(change.getCurrentState() == ConnectionState.CONNECTED) {
	    channel = pusher.subscribe(channelName, this, eventName);
	}
    }
    
    @Override
    public void onError(String message, String code, Exception e) {
	
	System.out.println(String.format("An error was received with message [%s], code [%s], exception [%s]", message, code, e));
    }

    /* PrivateChannelEventListener implementation */
    
    @Override
    public void onEvent(String channelName, String eventName, String data) {
	
	System.out.println(String.format("Received event [%s] on channel [%s] with data [%s]", eventName, channelName, data));
    }

    @Override
    public void onSubscriptionSucceeded(String channelName) {
	
	System.out.println(String.format("Subscription to channel [%s] succeeded", channel.getName()));
	
	channel.trigger("client-myEvent", "{\"myName\":\"Bob\"}");
    }

    @Override
    public void onAuthenticationFailure(String message, Exception e) {
	
	System.out.println(String.format("Authentication failure due to [%s], exception was [%s]", message, e));
    }
}