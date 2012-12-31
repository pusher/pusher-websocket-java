package com.pusher.client.channel.impl;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.pusher.client.AuthorizationFailureException;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.ChannelState;
import com.pusher.client.channel.PrivateChannelEventListener;
import com.pusher.client.connection.impl.InternalConnection;

public class ChannelManager {

    private final Map<String, InternalChannel> channelNameToChannelMap = new HashMap<String, InternalChannel>();
    private final InternalConnection connection;
    private final PusherOptions pusherOptions;

    public ChannelManager(InternalConnection connection, PusherOptions pusherOptions) {
	
	if(connection == null || pusherOptions == null) {
	    throw new IllegalArgumentException("Cannot construct ChannelManager with a null connection or options");
	}
	
	this.connection = connection;
	this.pusherOptions = pusherOptions;
    }
    
    public void subscribeTo(InternalChannel channel, ChannelEventListener listener, String... eventNames) {
	
	validateArgumentsAndBindEvents(channel, listener, eventNames);
	
	String message = channel.toSubscribeMessage();
	sendSubscribe(channel, message);
    }

    public void subscribeTo(PrivateChannelImpl channel, PrivateChannelEventListener listener, String... eventNames) {
	
	validateArgumentsAndBindEvents(channel, listener, eventNames);
	
	String socketId = connection.getSocketId();
	String authResponse;
	try {
	    authResponse = pusherOptions.getAuthorizer().authorize(channel.getName(), socketId);
	} catch(AuthorizationFailureException e) {
	    
	    listener.onAuthenticationFailure("Encountered an exception during authorization", e);
	    return;
	}
	
	String message = channel.toSubscribeMessage(authResponse);
	sendSubscribe(channel, message);
    }

    public void unsubscribeFrom(String channelName) {
	
	if(channelName == null) {
	    throw new IllegalArgumentException("Cannot unsubscribe from null channel");
	}
	
	InternalChannel channel = channelNameToChannelMap.remove(channelName);
	if(channel != null) {
	    connection.sendMessage(channel.toUnsubscribeMessage());
	    channel.updateState(ChannelState.UNSUBSCRIBED);
	} else {
	    throw new IllegalArgumentException("Cannot unsubscribe to channel " + channelName + ", no subscription found");
	}
    }
    
    @SuppressWarnings("unchecked")
    public void onMessage(String event, String wholeMessage) {
	
	Map<Object, Object> json = new Gson().fromJson(wholeMessage, Map.class);
	Object channelNameObject = json.get("channel");
	
	if(channelNameObject != null) {
	    String channelName = (String) channelNameObject;
	    InternalChannel channel = channelNameToChannelMap.get(channelName);
	    
	    if(channel != null) {
		channel.onMessage(event, wholeMessage);
	    }
	}
    }

    private void sendSubscribe(InternalChannel channel, String message) {
	channelNameToChannelMap.put(channel.getName(), channel);
	connection.sendMessage(message);
	channel.updateState(ChannelState.SUBSCRIBE_SENT);
    }
    
    private void validateArgumentsAndBindEvents(InternalChannel channel, ChannelEventListener listener, String... eventNames) {
	
	if(channel == null) {
	    throw new IllegalArgumentException("Cannot subscribe to a null channel");
	}
	
	if(channelNameToChannelMap.containsKey(channel.getName())) {
	    throw new IllegalArgumentException("Already subscribed to a channel with name " + channel.getName());
	}
	
	for(String eventName : eventNames) {
	    channel.bind(eventName, listener);
	}
    }
}