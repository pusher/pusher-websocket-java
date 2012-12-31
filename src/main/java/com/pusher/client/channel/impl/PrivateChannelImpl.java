package com.pusher.client.channel.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.pusher.client.channel.PrivateChannel;

public class PrivateChannelImpl extends ChannelImpl implements PrivateChannel {

    public PrivateChannelImpl(String channelName) {
	super(channelName);
    }

    /* PrivateChannel implementation */
    
    @Override
    public boolean trigger(String eventName, String data) {
	// TODO Auto-generated method stub
	return false;
    }
    
    /* Base class overrides */

    @Override
    @SuppressWarnings("rawtypes")
    public String toSubscribeMessage(String... extraArguments) {

	if(extraArguments.length < 1) {
	    throw new IllegalArgumentException("The auth response must be provided to build a private channel subscription message");
	}
	
	String authResponse = extraArguments[0];
	Map authResponseMap = new Gson().fromJson(authResponse, Map.class);
	String authKey = (String) authResponseMap.get("auth");
	
	Map<Object, Object> jsonObject = new LinkedHashMap<Object, Object>();
	jsonObject.put("event", "pusher:subscribe");
	
	Map<Object, Object> dataMap = new LinkedHashMap<Object, Object>();
	dataMap.put("channel", name);
	dataMap.put("auth", authKey);
	
	jsonObject.put("data", dataMap);
	
	String json = new Gson().toJson(jsonObject);
	return json;
    }
    
    @Override
    protected String[] getDisallowedNameExpressions() {
	return new String[] {
		"^(?!private-).*"
	};
    }
}