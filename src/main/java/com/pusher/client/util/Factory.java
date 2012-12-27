package com.pusher.client.util;

import com.pusher.client.connection.Connection;
import com.pusher.client.connection.WebsocketConnection;

public class Factory {

    public static Connection newConnection() {
	return new WebsocketConnection();
    }
}