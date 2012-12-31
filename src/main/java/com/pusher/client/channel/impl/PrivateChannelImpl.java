package com.pusher.client.channel.impl;

import com.pusher.client.channel.PrivateChannel;

public class PrivateChannelImpl extends ChannelImpl implements PrivateChannel {

    public PrivateChannelImpl(String channelName) {
	super(channelName);
    }

    @Override
    public boolean trigger(String eventName, String data) {
	// TODO Auto-generated method stub
	return false;
    }
    
    @Override
    protected String[] getDisallowedNameExpressions() {
	return new String[] {
		"^(?!private-).*"
	};
    }
}