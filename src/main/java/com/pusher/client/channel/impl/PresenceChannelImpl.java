package com.pusher.client.channel.impl;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.pusher.client.AuthorizationFailureException;
import com.pusher.client.Authorizer;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.ChannelState;
import com.pusher.client.channel.EventMetadata;
import com.pusher.client.channel.PresenceChannel;
import com.pusher.client.channel.PresenceChannelEventListener;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.channel.User;
import com.pusher.client.connection.impl.InternalConnection;
import com.pusher.client.util.Factory;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PresenceChannelImpl extends PrivateChannelImpl implements PresenceChannel {

    private static final String MEMBER_ADDED_EVENT = "pusher_internal:member_added";
    private static final String MEMBER_REMOVED_EVENT = "pusher_internal:member_removed";
    private static final Gson GSON = new Gson();

    private final Map<String, User> idToUserMap = Collections.synchronizedMap(new LinkedHashMap<String, User>());

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

        if (event.equals(SUBSCRIPTION_SUCCESS_EVENT)) {
            updateState(ChannelState.SUBSCRIBED);
        }
        else {
            final Set<PresenceChannelEventListener> listeners;
            synchronized (lock) {
                final Set<PresenceChannelEventListener> sharedListeners = eventNameToListenerMap.get(event);
                if (sharedListeners != null) {
                    listeners = new HashSet<PresenceChannelEventListener>(sharedListeners);
                }
                else {
                    listeners = null;
                }
            }

            if (listeners != null) {
                for (final PresenceChannelEventListener listener : listeners) {
                    final String data = extractDataFrom(message);
                    final EventMetadata metadata = new EventMetadata();

                    // extracting user_id if client-event
                    if (event.startsWith(CLIENT_EVENT_PREFIX)) {

                        // extracting user_id from message
                        String userId = extractUserIdStringFrom(message);

                        metadata.setUserId(userId);

                    }
                        getFactory().queueOnEventThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onEvent(name, event, data);

                            listener.onEventWithMetadata(name, event, data, metadata);
                        }
                    });
                }
            }
        }


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
            final Map authResponseMap = GSON.fromJson(authResponse, Map.class);
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

            final String json = GSON.toJson(jsonObject);

            return json;
        }
        catch (final Exception e) {
            throw new AuthorizationFailureException("Unable to parse response from Authorizer: " + authResponse, e);
        }
    }

    private final Object lock = new Object();
    private final Map<String, Set<PresenceChannelEventListener>> eventNameToListenerMap = new HashMap<String, Set<PresenceChannelEventListener>>();

    @Override
    public void bind(final String eventName, final SubscriptionEventListener listener) {

        if (listener instanceof PresenceChannelEventListener == false) {
            throw new IllegalArgumentException(
                    "Only instances of PresenceChannelEventListener can be bound to a presence channel");
        }

                validateArguments(eventName, listener);

        synchronized (lock) {
            Set<PresenceChannelEventListener> listeners = eventNameToListenerMap.get(eventName);
            if (listeners == null) {
                listeners = new HashSet<PresenceChannelEventListener>();
                eventNameToListenerMap.put(eventName, listeners);
            }
            listeners.add((PresenceChannelEventListener)listener);
        }
    }

    @Override
    public void unbind(final String eventName, final SubscriptionEventListener listener) {
        validateArguments(eventName, listener);

        synchronized (lock) {
            final Set<PresenceChannelEventListener> listeners = eventNameToListenerMap.get(eventName);
            if (listeners != null) {
                listeners.remove(listener);
                if (listeners.isEmpty()) {
                    eventNameToListenerMap.remove(eventName);
                }
            }
        }
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

        if (ids != null && !ids.isEmpty()) {
            // build the collection of Users
            for (final String id : ids) {
                final String userData = hash.get(id) != null ? GSON.toJson(hash.get(id)) : null;
                final User user = new User(id, userData);
                idToUserMap.put(id, user);
            }
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
        MemberData memberData = GSON.fromJson(dataString, MemberData.class);


        final String id = memberData.userId;
        final String userData = memberData.userInfo!= null ? GSON.toJson(memberData.userInfo) : null;

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
        final MemberData memberData = GSON.fromJson(dataString, MemberData.class);

        final User user = idToUserMap.remove(memberData.userId);

        final ChannelEventListener listener = getEventListener();
        if (listener != null) {
            final PresenceChannelEventListener presenceListener = (PresenceChannelEventListener)listener;
            presenceListener.userUnsubscribed(getName(), user);
        }
    }

    @SuppressWarnings("rawtypes")
    private static String extractDataStringFrom(final String message) {
        final Map jsonObject = GSON.fromJson(message, Map.class);
        return (String) jsonObject.get("data");
    }

    private static String extractUserIdStringFrom(final String message) {
        final Map jsonObject = GSON.fromJson(message, Map.class);
        return (String) jsonObject.get("user_id");
    }

    @SuppressWarnings("rawtypes")
    private static PresenceData extractPresenceDataFrom(final String message) {
        final String dataString = extractDataStringFrom(message);
        return GSON.fromJson(dataString, Presence.class).presence;
    }

    @SuppressWarnings("rawtypes")
    private void storeMyUserId(final Object channelData) {
        final Map channelDataMap = GSON.fromJson((String)channelData, Map.class);
        myUserID = String.valueOf(channelDataMap.get("user_id"));
    }

    private class MemberData {
        @SerializedName("user_id")
        public String userId;
        @SerializedName("user_info")
        public Object userInfo;
    }

    private class PresenceData {
        @SerializedName("count")
        public Integer count;
        @SerializedName("ids")
        public List<String> ids;
        @SerializedName("hash")
        public Map<String, Object> hash;
    }

    private class Presence {
        @SerializedName("presence")
        public PresenceData presence;
    }

}
