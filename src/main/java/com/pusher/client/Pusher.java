package com.pusher.client;

import com.pusher.client.connection.Connection;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.util.Factory;

public class Pusher {

    private final Connection connection;
    
    public Pusher(String apiKey) {
	
	if(apiKey == null || apiKey.isEmpty()) {
	    throw new IllegalArgumentException("API Key cannot be null or empty");
	}
	
	this.connection = Factory.newConnection(apiKey);
    }
    
    /* Connection methods */
    
    public Connection getConnection()
    {
	return connection;
    }
    
    public void connect()
    {
	connection.connect();
    }
    
    public void connect(ConnectionEventListener eventListener)
    {
	connection.setEventListener(eventListener);
	connection.connect();
    }
}