package com.pusher.client;

import com.pusher.client.channel.Channel;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.PublicChannel;
import com.pusher.client.connection.Connection;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.InternalConnection;
import com.pusher.client.util.Factory;

public class Pusher {

    private final InternalConnection connection;
    
    public Pusher(String apiKey) {
	
	if(apiKey == null || apiKey.isEmpty()) {
	    throw new IllegalArgumentException("API Key cannot be null or empty");
	}
	
	this.connection = Factory.newConnection(apiKey);
    }
    
    /* Connection methods */
    
    public Connection getConnection() {
	return connection;
    }
    
    public void connect() {
	connection.connect();
    }
    
    public void connect(ConnectionEventListener eventListener) {
	connection.setEventListener(eventListener);
	connection.connect();
    }
    
    /* Subscription methods */
    
    public Channel subscribe(String channelName) {
	return subscribe(channelName, null);
    }
    
    public Channel subscribe(String channelName, ChannelEventListener listener, String... eventNames) {
	
	if(connection.getState() != ConnectionState.CONNECTED) {
	    throw new IllegalStateException("Cannot subscribe to public channel " + channelName + " while not connected");
	}
	
	PublicChannel channel = Factory.newPublicChannel(channelName);
	
	for(String eventName : eventNames) {
	    channel.bind(eventName, listener);
	}
	
	connection.subscribeTo(channel);
	
	return channel;
    }
    
    public void unsubscribe(String channelName) {
	
	if(connection.getState() != ConnectionState.CONNECTED) {
	    throw new IllegalStateException("Cannot unsubscribe from channel " + channelName + " while not connected");
	}
	
	connection.unsubscribeFrom(channelName);
    }
}