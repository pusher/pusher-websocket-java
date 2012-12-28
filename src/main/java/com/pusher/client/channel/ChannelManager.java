package com.pusher.client.channel;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.pusher.client.connection.InternalConnection;

public class ChannelManager {

    private final Map<String, InternalChannel> channelNameToChannelMap = new HashMap<String, InternalChannel>();
    private final InternalConnection connection;

    public ChannelManager(InternalConnection connection) {
	
	if(connection == null) {
	    throw new IllegalArgumentException("Cannot construct ChannelManager with a null connection");
	}
	
	this.connection = connection;
    }
    
    public void subscribeTo(InternalChannel channel, ChannelEventListener listener, String... eventNames) {
	
	if(channel == null) {
	    throw new IllegalArgumentException("Cannot subscribe to a null channel");
	}
	
	if(channelNameToChannelMap.containsKey(channel.getName())) {
	    throw new IllegalArgumentException("Already subscribed to a channel with name " + channel.getName());
	}
	
	for(String eventName : eventNames) {
	    channel.bind(eventName, listener);
	}
	
	channelNameToChannelMap.put(channel.getName(), channel);
	
	connection.sendMessage(channel.toSubscribeMessage());
	
	channel.updateState(ChannelState.SUBSCRIBE_SENT);
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
}