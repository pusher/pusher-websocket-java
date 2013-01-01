package com.pusher.client.channel.impl;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.pusher.client.User;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.PresenceChannel;
import com.pusher.client.channel.PresenceChannelEventListener;
import com.pusher.client.connection.impl.InternalConnection;
import com.pusher.client.util.Factory;

public class PresenceChannelImpl extends PrivateChannelImpl implements PresenceChannel {

    public PresenceChannelImpl(InternalConnection connection, String channelName) {
	super(connection, channelName);
    }

    /* Base class overrides */
    
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void onMessage(String event, String message) {

	super.onMessage(event, message);
	
	if(event.equals(SUBSCRIPTION_SUCCESS_EVENT)) {
	    
	    Gson gson = new Gson();
	    Map jsonObject = gson.fromJson(message, Map.class);
	    String dataString = (String) jsonObject.get("data");
	    
	    Map dataMap = gson.fromJson(dataString, Map.class);
	    Map presenceMap = (Map) dataMap.get("presence");

	    List<String> ids = (List<String>) presenceMap.get("ids");
	    
	    Map userDataMap = (Map) presenceMap.get("hash");
	    
	    final Set<User> users = new LinkedHashSet<User>();
	    for(String id : ids) {
		String userData = (userDataMap.get(id) != null) ? userDataMap.get(id).toString() : null;
		User user = new User(id, userData);
		users.add(user);
	    }
	    
	    for(final ChannelEventListener eventListener : getAllEventListeners()) {
		
		Factory.getEventQueue().execute(new Runnable() {
		    public void run() {
			((PresenceChannelEventListener)eventListener).onUserInformationReceived(name, users);
		    }
		});
	    }
	}
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
    
    private Set<ChannelEventListener> getAllEventListeners() {
	
	Set<ChannelEventListener> allListeners = new HashSet<ChannelEventListener>();
	for(Set<ChannelEventListener> x : eventNameToListenerMap.values()) {
	    allListeners.addAll(x);
	}
	return allListeners;
    }
}