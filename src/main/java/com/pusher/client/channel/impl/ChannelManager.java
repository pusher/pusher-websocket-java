package com.pusher.client.channel.impl;

import java.util.HashMap;
import java.util.Map;

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
    private final Factory factory;
    private InternalConnection connection;

    public ChannelManager(Factory factory) {
        this.factory = factory;
    }

    public void setConnection(InternalConnection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Cannot construct ChannelManager with a null connection");
        }

        if(this.connection != null) {
            this.connection.unbind(ConnectionState.CONNECTED, this);
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
        if (channel == null) {
            return;
        }

        channel.updateState(ChannelState.UNSUBSCRIBED);

        if (connection.getState() == ConnectionState.CONNECTED){
            connection.sendMessage(channel.toUnsubscribeMessage());
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

    /* ConnectionEventListener implementation */

    @Override
    public void onConnectionStateChange(ConnectionStateChange change) {

        if (change.getCurrentState() == ConnectionState.CONNECTED) {

            for (InternalChannel channel : channelNameToChannelMap.values()) {
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

        factory.getEventQueue().execute(new Runnable() {

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
                }
            }
        });
    }

    private void clearDownSubscription(final InternalChannel channel, final Exception e) {

        channelNameToChannelMap.remove(channel.getName());
        channel.updateState(ChannelState.FAILED);

        if(channel.getEventListener() != null) {
            factory.getEventQueue().execute(new Runnable() {

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
