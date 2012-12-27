package com.pusher.client.connection;

import com.pusher.client.channel.InternalChannel;

public interface InternalConnection extends Connection {

    void subscribeTo(InternalChannel channel);
}