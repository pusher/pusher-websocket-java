package com.pusher.client.channel.impl;

import com.pusher.client.util.Factory;

public class ChannelImpl extends BaseChannel {
    protected final String name;

    public ChannelImpl(final String channelName, final Factory factory) {
        super(factory);
        if (channelName == null) {
            throw new IllegalArgumentException("Cannot subscribe to a channel with a null name");
        }

        for (final String disallowedPattern : getDisallowedNameExpressions()) {
            if (channelName.matches(disallowedPattern)) {
                throw new IllegalArgumentException(
                        "Channel name "
                                + channelName
                                + " is invalid. Private channel names must start with \"private-\" and presence channel names must start with \"presence-\"");
            }
        }

        name = channelName;
    }

    @Override
    public String getName() {
        return name;
    }
<<<<<<< HEAD
=======

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
    public void unbind(final String eventName, final SubscriptionEventListener listener) {

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
    public PusherEvent prepareEvent(String event, String message) {
        return GSON.fromJson(message, PusherEvent.class);
    }

    @Override
    public void onMessage(final String event, final String message) {

        if (event.equals(SUBSCRIPTION_SUCCESS_EVENT)) {
            updateState(ChannelState.SUBSCRIBED);
        } else if (event.equals(SUBSCRIPTION_COUNT_EVENT)) {
            handleSubscriptionCountEvent(message);
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
    public String toSubscribeMessage() {
        return GSON.toJson(new SubscribeMessage(name));
    }

    @Override
    public String toUnsubscribeMessage() {
        return GSON.toJson(
                new UnsubscribeMessage(name));
    }

    @Override
    public void updateState(final ChannelState state) {

        this.state = state;

        if (state == ChannelState.SUBSCRIBED && eventListener != null) {
            factory.queueOnEventThread(new Runnable() {
                @Override
                public void run() {
                    eventListener.onSubscriptionSucceeded(ChannelImpl.this.getName());
                }
            });
        }
    }

    private void handleSubscriptionCountEvent(final String message) {
        String channelName = this.getName();
        final SubscriptionCountData subscriptionCountMessage = GSON.fromJson(message, SubscriptionCountData.class);
        if (eventListener != null ) {
            factory.queueOnEventThread(new Runnable() {
                @Override
                public void run() {
                    eventListener.onSubscriptionCountChanged(channelName, subscriptionCountMessage.getCount());
                }
            });
        }
    }

    /* Comparable implementation */

    @Override
    public void setEventListener(final ChannelEventListener listener) {
        eventListener = listener;
    }

    @Override
    public ChannelEventListener getEventListener() {
        return eventListener;
    }

    @Override
    public int compareTo(final InternalChannel other) {
        return getName().compareTo(other.getName());
    }

    /* implementation detail */

>>>>>>> ae4c039 (Change listner name)
    @Override
    public String toString() {
        return String.format("[Public Channel: name=%s]", name);
    }

    protected String[] getDisallowedNameExpressions() {
        return new String[] { "^private-.*", "^presence-.*" };
    }
}
