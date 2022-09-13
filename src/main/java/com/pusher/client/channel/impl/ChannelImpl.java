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
                        "Channel name " +
                                channelName +
                                " is invalid. Private channel names must start with \"private-\" and presence channel names must start with \"presence-\""
                );
            }
        }

        name = channelName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("[Public Channel: name=%s]", name);
    }

    protected String[] getDisallowedNameExpressions() {
        return new String[]{"^private-.*", "^presence-.*"};
    }
}
