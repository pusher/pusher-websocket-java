package com.pusher.client.channel.impl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.FieldNamingPolicy;

import com.pusher.client.AuthorizationFailureException;
import com.pusher.client.Authorizer;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.PresenceChannel;
import com.pusher.client.channel.PresenceChannelEventListener;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.channel.User;
import com.pusher.client.connection.impl.InternalConnection;
import com.pusher.client.util.Factory;

public class PresenceChannelImpl extends PrivateChannelImpl implements PresenceChannel {

    private static final String MEMBER_ADDED_EVENT = "pusher_internal:member_added";
    private static final String MEMBER_REMOVED_EVENT = "pusher_internal:member_removed";

    private final Map<String, User> idToUserMap = Collections.synchronizedMap(new LinkedHashMap<String, User>());

    private static final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private String myUserID;

    public PresenceChannelImpl(final InternalConnection connection, final String channelName,
            final Authorizer authorizer, final Factory factory) {
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
    public void onMessage(final String event, final String message) {

        super.onMessage(event, message);

        if (event.equals(SUBSCRIPTION_SUCCESS_EVENT)) {
            handleSubscriptionSuccessfulMessage(message);
        }
        else if (event.equals(MEMBER_ADDED_EVENT)) {
            handleMemberAddedEvent(message);
        }
        else if (event.equals(MEMBER_REMOVED_EVENT)) {
            handleMemberRemovedEvent(message);
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public String toSubscribeMessage() {

        final String authResponse = getAuthResponse();

        try {
            final Map authResponseMap = gson.fromJson(authResponse, Map.class);
            final String authKey = (String)authResponseMap.get("auth");
            final Object channelData = authResponseMap.get("channel_data");

            storeMyUserId(channelData);

            final Map<Object, Object> jsonObject = new LinkedHashMap<Object, Object>();
            jsonObject.put("event", "pusher:subscribe");

            final Map<Object, Object> dataMap = new LinkedHashMap<Object, Object>();
            dataMap.put("channel", name);
            dataMap.put("auth", authKey);
            dataMap.put("channel_data", channelData);

            jsonObject.put("data", dataMap);

            final String json = gson.toJson(jsonObject);

            return json;
        }
        catch (final Exception e) {
            throw new AuthorizationFailureException("Unable to parse response from Authorizer: " + authResponse, e);
        }
    }

    @Override
    public void bind(final String eventName, final SubscriptionEventListener listener) {

        if (listener instanceof PresenceChannelEventListener == false) {
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
    private void handleSubscriptionSuccessfulMessage(final String message) {

        // extract data from the JSON message
        final PresenceData presenceData = extractPresenceDataFrom(message);
        final List<String> ids = presenceData.ids;
        final Map<String, Object> hash = presenceData.hash;

        // build the collection of Users
        for (final String id : ids) {
            final String userData = hash.get(id) != null ? gson.toJson(hash.get(id)) : null;
            final User user = new User(id, userData);
            idToUserMap.put(id, user);
        }

        final ChannelEventListener listener = getEventListener();
        if (listener != null) {
            final PresenceChannelEventListener presenceListener = (PresenceChannelEventListener)listener;
            presenceListener.onUsersInformationReceived(getName(), getUsers());
        }
    }

    @SuppressWarnings("rawtypes")
    private void handleMemberAddedEvent(final String message) {
        final String dataString = extractDataStringFrom(message);
        MemberData memberData = gson.fromJson(dataString, MemberData.class);


        final String id = memberData.user_id;
        final String userData = memberData.user_info != null ? gson.toJson(memberData.user_info) : null;

        final User user = new User(id, userData);
        idToUserMap.put(id, user);

        final ChannelEventListener listener = getEventListener();
        if (listener != null) {
            final PresenceChannelEventListener presenceListener = (PresenceChannelEventListener)listener;
            presenceListener.userSubscribed(getName(), user);
        }
    }

    @SuppressWarnings("rawtypes")
    private void handleMemberRemovedEvent(final String message) {

        final String dataString = extractDataStringFrom(message);
        final MemberData memberData = gson.fromJson(dataString, MemberData.class);

        final User user = idToUserMap.remove(memberData.user_id);

        final ChannelEventListener listener = getEventListener();
        if (listener != null) {
            final PresenceChannelEventListener presenceListener = (PresenceChannelEventListener)listener;
            presenceListener.userUnsubscribed(getName(), user);
        }
    }

    @SuppressWarnings("rawtypes")
    private static String extractDataStringFrom(final String message) {
        final Map jsonObject = gson.fromJson(message, Map.class);
        return  (String) jsonObject.get("data");
    }

    @SuppressWarnings("rawtypes")
    private static PresenceData extractPresenceDataFrom(final String message) {
        final String dataString = extractDataStringFrom(message);
        return gson.fromJson(dataString, Presence.class).presence;
    }

    @SuppressWarnings("rawtypes")
    private void storeMyUserId(final Object channelData) {
        final Map channelDataMap = gson.fromJson((String)channelData, Map.class);
        myUserID = String.valueOf(channelDataMap.get("user_id"));
    }

    private class MemberData {
        public String user_id;
        public Object user_info;
    }

    private class PresenceData {
        public Integer count;
        public List<String> ids;
        public Map<String, Object> hash;
    }

    private class Presence {
        public PresenceData presence;
    }

}
