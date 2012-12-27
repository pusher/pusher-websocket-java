package com.pusher.client.channel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.pusher.client.util.Factory;

public class PublicChannel implements InternalChannel {

    private final String name;
    private final Map<String, Set<ChannelEventListener>> eventNameToListenerMap = new HashMap<String, Set<ChannelEventListener>>(); 

    public PublicChannel(String channelName) {
	
	if(channelName == null || channelName.isEmpty()) {
	    throw new IllegalArgumentException("Cannot subscribe to a channel with a null or empty name");
	}
	
	this.name = channelName;
    }

    /* Channel implementation */
    
    @Override
    public String getName() {
	return name;
    }
    
    @Override
    public void bind(String eventName, ChannelEventListener listener) {
	
	if(eventName == null || eventName.isEmpty()) {
	    throw new IllegalArgumentException("Cannot bind to channel " + name + " with a null or empty event name");
	}
	
	if(listener == null) {
	    throw new IllegalArgumentException("Cannot bind to channel " + name + " with a null listener");
	}
	
	Set<ChannelEventListener> listeners = eventNameToListenerMap.get(eventName);
	if(listeners == null) {
	    listeners = new HashSet<ChannelEventListener>();
	    eventNameToListenerMap.put(eventName, listeners);
	}
	
	listeners.add(listener);
    }

    /* InternalChannel implementation */
   
    @Override
    public void onMessage(final String event, String message) {
	
	Set<ChannelEventListener> listeners = eventNameToListenerMap.get(event);
	if(listeners != null) {
	    for(final ChannelEventListener listener : listeners) {
		
		final String data = extractDataFrom(message);
		
		Factory.getEventQueue().execute(new Runnable() {
		    public void run() {
			listener.onEvent(event, data);
		    }
		});
	    }
	}
    }
    
    @Override
    public String toSubscriptionMessage() {
	
	Map<Object, Object> jsonObject = new LinkedHashMap<Object, Object>();
	jsonObject.put("event", "pusher:subscribe");
	
	Map<Object, Object> dataMap = new LinkedHashMap<Object, Object>();
	dataMap.put("channel", name);
	
	jsonObject.put("data", dataMap);
	
	return new Gson().toJson(jsonObject);
    }

    @Override
    public void subscribeSent() {
	
	for(Set<ChannelEventListener> listeners : eventNameToListenerMap.values()) {
	    for(final ChannelEventListener listener : listeners) {
		Factory.getEventQueue().execute(new Runnable() {
		    public void run() {
			listener.onSubscriptionSucceeded(PublicChannel.this);
		    }
		});
	    }
	}
    }
    
    @Override
    public String toString() {
	return String.format("[Public Channel: name=%s]", name);
    }
    
    @SuppressWarnings("unchecked")
    private String extractDataFrom(String message) {
	Gson gson = new Gson();
	Map<Object, Object> jsonObject = gson.fromJson(message, Map.class);
	return gson.toJson(jsonObject.get("data"));
    }
}