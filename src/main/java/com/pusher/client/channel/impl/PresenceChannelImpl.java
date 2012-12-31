package com.pusher.client.channel.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.PresenceChannel;
import com.pusher.client.channel.PresenceChannelEventListener;
import com.pusher.client.connection.impl.InternalConnection;

public class PresenceChannelImpl extends PrivateChannelImpl implements PresenceChannel {

    public PresenceChannelImpl(InternalConnection connection, String channelName) {
	super(connection, channelName);
    }

    /* Base class overrides */
    
    @Override
    public void onMessage(String event, String message) {

	// TODO: next
	super.onMessage(event, message);
    }
    
    @Override
    @SuppressWarnings("rawtypes")
    public String toSubscribeMessage(String... extraArguments) {
	
	if(extraArguments.length < 1) {
	    throw new IllegalArgumentException("The auth response must be provided to build a private channel subscription message");
	}
	
	String authResponse = extraArguments[0];
	
	Map authResponseMap = new Gson().fromJson(authResponse, Map.class);
	String authKey = (String) authResponseMap.get("auth");
	Object channelData = authResponseMap.get("channel_data");
	
	Map<Object, Object> jsonObject = new LinkedHashMap<Object, Object>();
	jsonObject.put("event", "pusher:subscribe");
	
	Map<Object, Object> dataMap = new LinkedHashMap<Object, Object>();
	dataMap.put("channel", name);
	dataMap.put("auth", authKey);
	dataMap.put("channel_data", channelData);
	
	jsonObject.put("data", dataMap);
	
	String json = new Gson().toJson(jsonObject);
	
	return json;
    }    

    @Override
    public void bind(String eventName, ChannelEventListener listener) {
	
	if( (listener instanceof PresenceChannelEventListener) == false) {
	    throw new IllegalArgumentException("Only instances of PresenceChannelEventListener can be bound to a presence channel");
	}
	
	super.bind(eventName, listener);
    }
    
    @Override
    protected String[] getDisallowedNameExpressions() {
	return new String[] {
		"^(?!presence-).*"
	};
    }
}