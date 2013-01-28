package com.pusher.client.example;

import java.util.Set;

import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.User;
import com.pusher.client.channel.PresenceChannel;
import com.pusher.client.channel.PresenceChannelEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.util.HttpAuthorizer;

public class PresenceChannelExampleApp implements ConnectionEventListener, PresenceChannelEventListener {

    private final Pusher pusher;
    private final String channelName;
    private final String eventName;
    
    private PresenceChannel channel;
    
    public static void main(String[] args) {
	new PresenceChannelExampleApp(args);
    }
    
    public PresenceChannelExampleApp(String[] args) {
	
	String apiKey = (args.length > 0) ? args[0] : "a87fe72c6f36272aa4b1";
	channelName = (args.length > 1) ? args[1] : "presence-my-channel";
	eventName = (args.length > 2) ? args[2] : "my-event";
	
	HttpAuthorizer authorizer = new HttpAuthorizer("http://www.leggetter.co.uk/pusher/pusher-examples/php/authentication/src/presence_auth.php");
	PusherOptions options = new PusherOptions().setAuthorizer(authorizer);
	
	pusher = new Pusher(apiKey, options);
	pusher.connect(this);
    }

    /* ConnectionEventListener implementation */
    
    @Override
    public void onConnectionStateChange(ConnectionStateChange change) {
	
	System.out.println(String.format("Connection state changed from [%s] to [%s]", change.getPreviousState(), change.getCurrentState()));
	
	if(change.getCurrentState() == ConnectionState.CONNECTED) {
	    channel = pusher.subscribePresence(channelName, this, eventName);
	}
    }
    
    @Override
    public void onError(String message, String code, Exception e) {
	
	System.out.println(String.format("An error was received with message [%s], code [%s], exception [%s]", message, code, e));
    }

    /* PresenceChannelEventListener implementation */

    @Override
    public void onUsersInformationReceived(String channelName, Set<User> users) {
	
	System.out.println("Received user information");
	
	printCurrentlySubscribedUsers();
    }

    @Override
    public void userSubscribed(String channelName, User user) {
	
	System.out.println(String.format("A new user has joined channel [%s]: %s", channelName, user.toString()));
	
	printCurrentlySubscribedUsers();
    }

    @Override
    public void userUnsubscribed(String channelName, User user) {

	System.out.println(String.format("A user has left channel [%s]: %s", channelName, user));
	
	printCurrentlySubscribedUsers();
    }
    
    @Override
    public void onEvent(String channelName, String eventName, String data) {
	
	System.out.println(String.format("Received event [%s] on channel [%s] with data [%s]", eventName, channelName, data));
    }

    @Override
    public void onSubscriptionSucceeded(String channelName) {
	
	System.out.println(String.format("Subscription to channel [%s] succeeded", channel.getName()));
    }

    @Override
    public void onAuthenticationFailure(String message, Exception e) {
	
	System.out.println(String.format("Authentication failure due to [%s], exception was [%s]", message, e));
    }
    
    private void printCurrentlySubscribedUsers() {
	StringBuilder sb = new StringBuilder("Users now subscribed to the channel:");
	for(User remainingUser : channel.getUsers()) {
	    sb.append("\n\t");
	    sb.append(remainingUser.toString());
	    
	    if(remainingUser.equals(channel.getMe())) {
		sb.append(" (me)");
	    }
	}
	
	System.out.println(sb.toString());
    }
}