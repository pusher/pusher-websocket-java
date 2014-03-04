package com.pusher.client.channel.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.ChannelState;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.util.Factory;

public class ChannelImpl implements InternalChannel {

    private static final String INTERNAL_EVENT_PREFIX = "pusher_internal:";
    protected static final String SUBSCRIPTION_SUCCESS_EVENT = "pusher_internal:subscription_succeeded";
    protected final String name;
    protected final Map<String, Set<SubscriptionEventListener>> eventNameToListenerMap = new HashMap<String, Set<SubscriptionEventListener>>();
    protected volatile ChannelState state = ChannelState.INITIAL;
    private ChannelEventListener eventListener;
    private final Factory factory;

    public ChannelImpl(String channelName, Factory factory) {

        if (channelName == null) {
            throw new IllegalArgumentException(
                    "Cannot subscribe to a channel with a null name");
        }

        for (String disallowedPattern : getDisallowedNameExpressions()) {
            if (channelName.matches(disallowedPattern)) {
                throw new IllegalArgumentException(
                        "Channel name "
                                + channelName
                                + " is invalid. Private channel names must start with \"private-\" and presence channel names must start with \"presence-\"");
            }
        }

        this.name = channelName;
        this.factory = factory;
    }

    /* Channel implementation */

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void bind(String eventName, SubscriptionEventListener listener) {

        validateArguments(eventName, listener);

        Set<SubscriptionEventListener> listeners = eventNameToListenerMap
                .get(eventName);
        if (listeners == null) {
            listeners = new HashSet<SubscriptionEventListener>();
            eventNameToListenerMap.put(eventName, listeners);
        }

        listeners.add(listener);
    }

    @Override
    public void unbind(String eventName, SubscriptionEventListener listener) {

        validateArguments(eventName, listener);

        Set<SubscriptionEventListener> listeners = eventNameToListenerMap
                .get(eventName);
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                eventNameToListenerMap.remove(eventName);
            }
        }
    }

    /* InternalChannel implementation */

    @Override
    public void onMessage(final String event, String message) {

        if (event.equals(SUBSCRIPTION_SUCCESS_EVENT)) {
            updateState(ChannelState.SUBSCRIBED);
        } else {
            Set<SubscriptionEventListener> listeners = eventNameToListenerMap
                    .get(event);
            if (listeners != null) {
                for (final SubscriptionEventListener listener : listeners) {

                    final String data = extractDataFrom(message);

                    factory.getEventQueue().execute(new Runnable() {
                        @Override
                        public void run() {
                            listener.onEvent(name, event, data);
                        }
                    });
                }
            }
        }
    }

    @Override
    public String toSubscribeMessage() {

        Map<Object, Object> jsonObject = new LinkedHashMap<Object, Object>();
        jsonObject.put("event", "pusher:subscribe");

        Map<Object, Object> dataMap = new LinkedHashMap<Object, Object>();
        dataMap.put("channel", name);

        jsonObject.put("data", dataMap);

        return new Gson().toJson(jsonObject);
    }

    @Override
    public String toUnsubscribeMessage() {
        Map<Object, Object> jsonObject = new LinkedHashMap<Object, Object>();
        jsonObject.put("event", "pusher:unsubscribe");

        Map<Object, Object> dataMap = new LinkedHashMap<Object, Object>();
        dataMap.put("channel", name);

        jsonObject.put("data", dataMap);

        return new Gson().toJson(jsonObject);
    }

    @Override
    public void updateState(ChannelState state) {

        this.state = state;

        if (state == ChannelState.SUBSCRIBED && eventListener != null) {
            factory.getEventQueue().execute(new Runnable() {
                @Override
                public void run() {
                    eventListener.onSubscriptionSucceeded(ChannelImpl.this.getName());
                }
            });
        }
    }

    /* Comparable implementation */

    @Override
    public void setEventListener(ChannelEventListener listener) {
        this.eventListener = listener;
    }

    @Override
    public ChannelEventListener getEventListener() {
        return eventListener;
    }

    @Override
    public int compareTo(InternalChannel other) {
        return this.getName().compareTo(other.getName());
    }

    /* implementation detail */

    @Override
    public String toString() {
        return String.format("[Public Channel: name=%s]", name);
    }

    @SuppressWarnings("unchecked")
    private String extractDataFrom(String message) {
        Gson gson = new Gson();
        Map<Object, Object> jsonObject = gson.fromJson(message, Map.class);
        return (String) jsonObject.get("data");
    }

    protected String[] getDisallowedNameExpressions() {
        return new String[] { "^private-.*", "^presence-.*" };
    }

    private void validateArguments(String eventName,
            SubscriptionEventListener listener) {

        if (eventName == null) {
            throw new IllegalArgumentException("Cannot bind or unbind to channel "
                    + name + " with a null event name");
        }

        if (listener == null) {
            throw new IllegalArgumentException("Cannot bind or unbind to channel "
                    + name + " with a null listener");
        }

        if (eventName.startsWith(INTERNAL_EVENT_PREFIX)) {
            throw new IllegalArgumentException("Cannot bind or unbind channel "
                    + name + " with an internal event name such as " + eventName);
        }

        if (state == ChannelState.UNSUBSCRIBED) {
            throw new IllegalStateException(
                    "Cannot bind or unbind to events on a channel that has been unsubscribed. Call Pusher.subscribe() to resubscribe to this channel");
        }
    }
}
