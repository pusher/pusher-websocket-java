package com.pusher.client.channel.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;

import com.google.gson.GsonBuilder;
import com.pusher.client.channel.*;
import com.pusher.client.channel.impl.message.SubscribeMessage;
import com.pusher.client.channel.impl.message.UnsubscribeMessage;
import com.pusher.client.util.Factory;

public abstract class BaseChannel implements InternalChannel {
    protected final Gson GSON;
    private static final String INTERNAL_EVENT_PREFIX = "pusher_internal:";
    protected static final String SUBSCRIPTION_SUCCESS_EVENT = "pusher_internal:subscription_succeeded";
    private Set<SubscriptionEventListener> globalListeners = new HashSet<SubscriptionEventListener>();
    private final Map<String, Set<SubscriptionEventListener>> eventNameToListenerMap = new HashMap<String, Set<SubscriptionEventListener>>();
    protected volatile ChannelState state = ChannelState.INITIAL;
    private ChannelEventListener eventListener;
    private final Factory factory;
    private final Object lock = new Object();

    public BaseChannel(final Factory factory) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(PusherEvent.class, new PusherEventDeserializer());
        GSON = gsonBuilder.create();
        this.factory = factory;
    }

    /* Channel implementation */

    @Override
    abstract public String getName();

    @Override
    public void bind(final String eventName, final SubscriptionEventListener listener) {
        validateArguments(eventName, listener);

        synchronized (lock) {
            Set<SubscriptionEventListener> listeners = eventNameToListenerMap.get(eventName);
            if (listeners == null) {
                listeners = new HashSet<SubscriptionEventListener>();
                eventNameToListenerMap.put(eventName, listeners);
            }
            listeners.add(listener);
        }
    }

    @Override
    public void bindGlobal(SubscriptionEventListener listener) {
        validateArguments("", listener);

        synchronized(lock) {
            globalListeners.add(listener);
        }
    }

    @Override
    public void unbind(String eventName, SubscriptionEventListener listener) {
        validateArguments(eventName, listener);

        synchronized (lock) {
            final Set<SubscriptionEventListener> listeners = eventNameToListenerMap.get(eventName);
            if (listeners != null) {
                listeners.remove(listener);
                if (listeners.isEmpty()) {
                    eventNameToListenerMap.remove(eventName);
                }
            }
        }
    }

    @Override
    public void unbindGlobal(SubscriptionEventListener listener) {
        validateArguments("", listener);

        synchronized(lock) {
            if (globalListeners != null) {
                globalListeners.remove(listener);
            }
        }
    }

    @Override
    public boolean isSubscribed() {
        return state == ChannelState.SUBSCRIBED;
    }

    /* InternalChannel implementation */

    @Override
    public String toSubscribeMessage() {
        return GSON.toJson(new SubscribeMessage(getName()));
    }

    @Override
    public String toUnsubscribeMessage() {
        return GSON.toJson(new UnsubscribeMessage(getName()));
    }

    @Override
    public PusherEvent prepareEvent(String event, String message) {
        return GSON.fromJson(message, PusherEvent.class);
    }

    @Override
    public void onMessage(String event, String message) {
        if (event.equals(SUBSCRIPTION_SUCCESS_EVENT)) {
            updateState(ChannelState.SUBSCRIBED);
        } else {
            final Set<SubscriptionEventListener> listeners = getInterestedListeners(event);
            if (listeners != null) {
                final PusherEvent pusherEvent = prepareEvent(event, message);
                if (pusherEvent != null) {
                    for (final SubscriptionEventListener listener : listeners) {
                        factory.queueOnEventThread(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onEvent(pusherEvent);
                                }
                            });
                    }
                }
            }
        }
    }

    @Override
    public void updateState(ChannelState state) {
        this.state = state;

        if (state == ChannelState.SUBSCRIBED && eventListener != null) {
            factory.queueOnEventThread(new Runnable() {
                    @Override
                    public void run() {
                        eventListener.onSubscriptionSucceeded(getName());
                    }
                });
        }
    }

    @Override
    public void setEventListener(final ChannelEventListener listener) {
        eventListener = listener;
    }

    @Override
    public ChannelEventListener getEventListener() {
        return eventListener;
    }

    /* Comparable implementation */

    @Override
    public int compareTo(final InternalChannel other) {
        return getName().compareTo(other.getName());
    }

    /* Implementation detail */

    @Override
    public String toString() {
        return String.format("[Channel: name=%s]", getName());
    }

     private void validateArguments(final String eventName, final SubscriptionEventListener listener) {

        if (eventName == null) {
            throw new IllegalArgumentException("Cannot bind or unbind to channel " + getName() + " with a null event name");
        }

        if (listener == null) {
            throw new IllegalArgumentException("Cannot bind or unbind to channel " + getName() + " with a null listener");
        }

        if (eventName.startsWith(INTERNAL_EVENT_PREFIX)) {
            throw new IllegalArgumentException("Cannot bind or unbind channel " + getName()
                    + " with an internal event name such as " + eventName);
        }

        if (state == ChannelState.UNSUBSCRIBED) {
            throw new IllegalStateException(
                    "Cannot bind or unbind to events on a channel that has been unsubscribed. Call Pusher.subscribe() to resubscribe to this channel");
        }
    }

    protected Set<SubscriptionEventListener> getInterestedListeners(String event) {
        synchronized (lock) {
            Set<SubscriptionEventListener> listeners = new HashSet<SubscriptionEventListener>();

            final Set<SubscriptionEventListener> sharedListeners =
                    eventNameToListenerMap.get(event);

            if (sharedListeners != null ) {
                listeners.addAll(sharedListeners);
            }
            if (!globalListeners.isEmpty()) {
                listeners.addAll(globalListeners);
            }

            if (listeners.isEmpty()){
                return null;
            }

            return listeners;
        }
    }
}
