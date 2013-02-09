package com.pusher.client.channel.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import com.google.gson.Gson;
import com.pusher.client.AuthorizationFailureException;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.ChannelState;
import com.pusher.client.channel.PrivateChannelEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.connection.impl.InternalConnection;
import com.pusher.client.util.Factory;

public class ChannelManager implements ConnectionEventListener {

	private final Map<String, InternalChannel> channelNameToChannelMap = new HashMap<String, InternalChannel>();
	private final Set<InternalChannel> queuedChannels = new ConcurrentSkipListSet<InternalChannel>();
	private final InternalConnection connection;

	public ChannelManager(InternalConnection connection) {

		if (connection == null) {
			throw new IllegalArgumentException("Cannot construct ChannelManager with a null connection");
		}

		this.connection = connection;
		connection.bind(ConnectionState.CONNECTED, this);
	}

	public void subscribeTo(InternalChannel channel, ChannelEventListener listener, String... eventNames) {

		validateArgumentsAndBindEvents(channel, listener, eventNames);
		channelNameToChannelMap.put(channel.getName(), channel);
		sendOrQueueSubscribeMessage(channel);
	}
	
	public void unsubscribeFrom(String channelName) {

		if (channelName == null) {
			throw new IllegalArgumentException("Cannot unsubscribe from null channel");
		}

		InternalChannel channel = channelNameToChannelMap.remove(channelName);
		if (channel != null) {
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

		if (channelNameObject != null) {
			String channelName = (String) channelNameObject;
			InternalChannel channel = channelNameToChannelMap.get(channelName);

			if (channel != null) {
				channel.onMessage(event, wholeMessage);
			}
		}
	}

	public void clear() {
		channelNameToChannelMap.clear();
		queuedChannels.clear();
	}

	/* ConnectionEventListener implementation */

	@Override
	public void onConnectionStateChange(ConnectionStateChange change) {
		
		if (change.getCurrentState() == ConnectionState.CONNECTED) {

			for (InternalChannel channel : queuedChannels) {
				queuedChannels.remove(channel);
				sendOrQueueSubscribeMessage(channel);
			}
		}
	}

	@Override
	public void onError(String message, String code, Exception e) {
		// ignore or log
	}
	
	/* implementation detail */
	
	private void sendOrQueueSubscribeMessage(final InternalChannel channel) {

		Factory.getEventQueue().execute(new Runnable() {

			@Override
			public void run() {
					
				if (connection.getState() == ConnectionState.CONNECTED) {
					try {
						String message = channel.toSubscribeMessage();
						connection.sendMessage(message);
						channel.updateState(ChannelState.SUBSCRIBE_SENT);
					} catch(AuthorizationFailureException e) {
						clearDownSubscription(channel, e);
					}
				} else {
					queuedChannels.add(channel);
				}
			}
		});
	}

	private void clearDownSubscription(final InternalChannel channel, final Exception e) {
		
		channelNameToChannelMap.remove(channel.getName());
		channel.updateState(ChannelState.UNSUBSCRIBED);
		
		if(channel.getEventListener() != null) {
			Factory.getEventQueue().execute(new Runnable() {
				
				public void run() {
					// Note: this cast is safe because an AuthorizationFailureException will never be thrown
					// when subscribing to a non-private channel
					ChannelEventListener eventListener = channel.getEventListener();
					PrivateChannelEventListener privateChannelListener = (PrivateChannelEventListener) eventListener;
					privateChannelListener.onAuthenticationFailure(e.getMessage(), e);
				}
			});
		}
	}
	
	private void validateArgumentsAndBindEvents(InternalChannel channel, ChannelEventListener listener, String... eventNames) {

		if (channel == null) {
			throw new IllegalArgumentException("Cannot subscribe to a null channel");
		}

		if (channelNameToChannelMap.containsKey(channel.getName())) {
			throw new IllegalArgumentException("Already subscribed to a channel with name " + channel.getName());
		}

		for (String eventName : eventNames) {
			channel.bind(eventName, listener);
		}
		
		channel.setEventListener(listener);
	}
}