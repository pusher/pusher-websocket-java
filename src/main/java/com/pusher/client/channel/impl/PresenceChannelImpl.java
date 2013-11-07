package com.pusher.client.channel.impl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.pusher.client.AuthorizationFailureException;
import com.pusher.client.Authorizer;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.PresenceChannel;
import com.pusher.client.channel.PresenceChannelEventListener;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.channel.User;
import com.pusher.client.connection.impl.InternalConnection;
import com.pusher.client.util.Factory;

public class PresenceChannelImpl extends PrivateChannelImpl implements
		PresenceChannel {

	private static final String MEMBER_ADDED_EVENT = "pusher_internal:member_added";
	private static final String MEMBER_REMOVED_EVENT = "pusher_internal:member_removed";

	private final Map<String, User> idToUserMap = Collections.synchronizedMap(new LinkedHashMap<String, User>());

	private String myUserID;

	public PresenceChannelImpl(InternalConnection connection, String channelName,
			Authorizer authorizer, Factory factory) {
		super(connection, channelName, authorizer, factory);
	}

	/* PresenceChannel implementation */

	@Override
	public Set<User> getUsers() {
		return new LinkedHashSet<User>(idToUserMap.values());
	}

	@Override
	public User getMe() {
		return idToUserMap.get(myUserID);
	}

	/* Base class overrides */

	@Override
	public void onMessage(String event, String message) {

		super.onMessage(event, message);

		if (event.equals(SUBSCRIPTION_SUCCESS_EVENT)) {
			handleSubscriptionSuccessfulMessage(message);
		} else if (event.equals(MEMBER_ADDED_EVENT)) {
			handleMemberAddedEvent(message);
		} else if (event.equals(MEMBER_REMOVED_EVENT)) {
			handleMemberRemovedEvent(message);
		}
	}

	@Override
	@SuppressWarnings("rawtypes")
	public String toSubscribeMessage() {

		String authResponse = getAuthResponse();

		try {
			Map authResponseMap = new Gson().fromJson(authResponse, Map.class);
			String authKey = (String) authResponseMap.get("auth");
			Object channelData = authResponseMap.get("channel_data");

			storeMyUserId(channelData);

			Map<Object, Object> jsonObject = new LinkedHashMap<Object, Object>();
			jsonObject.put("event", "pusher:subscribe");

			Map<Object, Object> dataMap = new LinkedHashMap<Object, Object>();
			dataMap.put("channel", name);
			dataMap.put("auth", authKey);
			dataMap.put("channel_data", channelData);

			jsonObject.put("data", dataMap);

			String json = new Gson().toJson(jsonObject);

			return json;
		} catch (Exception e) {
			throw new AuthorizationFailureException(
					"Unable to parse response from Authorizer: " + authResponse, e);
		}
	}

	@Override
	public void bind(String eventName, SubscriptionEventListener listener) {

		if ((listener instanceof PresenceChannelEventListener) == false) {
			throw new IllegalArgumentException(
					"Only instances of PresenceChannelEventListener can be bound to a presence channel");
		}

		super.bind(eventName, listener);
	}

	@Override
	protected String[] getDisallowedNameExpressions() {
		return new String[] { "^(?!presence-).*" };
	}

	@Override
	public String toString() {
		return String.format("[Presence Channel: name=%s]", name);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void handleSubscriptionSuccessfulMessage(String message) {

		// extract data from the JSON message
		Map presenceMap = extractPresenceMapFrom(message);

		List<String> ids = (List<String>) presenceMap.get("ids");
		Map hash = (Map) presenceMap.get("hash");

		// build the collection of Users
		for (String id : ids) {
			String userData = (hash.get(id) != null) ? hash.get(id).toString() : null;
			User user = new User(id, userData);
			idToUserMap.put(id, user);
		}

		ChannelEventListener listener = this.getEventListener();
		if( listener != null ) {
			PresenceChannelEventListener presenceListener = (PresenceChannelEventListener)listener;
			presenceListener.onUsersInformationReceived(getName(), getUsers());
		}
	}

	@SuppressWarnings("rawtypes")
	private void handleMemberAddedEvent(String message) {

		Map dataMap = extractDataMapFrom(message);
		String id = String.valueOf(dataMap.get("user_id"));
		String userData = (dataMap.get("user_info") != null) ? dataMap.get("user_info").toString() : null;

		final User user = new User(id, userData);
		idToUserMap.put(id, user);

		ChannelEventListener listener = this.getEventListener();
		if( listener != null ) {
			PresenceChannelEventListener presenceListener = (PresenceChannelEventListener)listener;
			presenceListener.userSubscribed(getName(), user);
		}
	}

	@SuppressWarnings("rawtypes")
	private void handleMemberRemovedEvent(String message) {

		Map dataMap = extractDataMapFrom(message);
		String id = String.valueOf(dataMap.get("user_id"));

		final User user = idToUserMap.remove(id);

		ChannelEventListener listener = this.getEventListener();
		if( listener != null ) {
			PresenceChannelEventListener presenceListener = (PresenceChannelEventListener)listener;
			presenceListener.userUnsubscribed(getName(), user);
		}
	}

	@SuppressWarnings("rawtypes")
	private static Map extractDataMapFrom(String message) {
		Gson gson = new Gson();
		Map jsonObject = gson.fromJson(message, Map.class);
		String dataString = (String) jsonObject.get("data");

		Map dataMap = gson.fromJson(dataString, Map.class);
		return dataMap;
	}

	@SuppressWarnings("rawtypes")
	private static Map extractPresenceMapFrom(String message) {

		Map dataMap = extractDataMapFrom(message);
		Map presenceMap = (Map) dataMap.get("presence");

		return presenceMap;
	}

	@SuppressWarnings("rawtypes")
	private void storeMyUserId(Object channelData) {
		Map channelDataMap = new Gson().fromJson(((String) channelData), Map.class);
		myUserID = String.valueOf(channelDataMap.get("user_id"));
	}
}