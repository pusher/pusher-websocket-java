package com.pusher.client.channel.impl;

import com.pusher.client.channel.PresenceChannel;
import com.pusher.client.connection.impl.InternalConnection;

public class PresenceChannelImpl extends PrivateChannelImpl implements PresenceChannel {

    public PresenceChannelImpl(InternalConnection connection, String channelName) {
	super(connection, channelName);
    }

    @Override
    protected String[] getDisallowedNameExpressions() {
	return new String[] {
		"^(?!presence-).*"
	};
    }
}