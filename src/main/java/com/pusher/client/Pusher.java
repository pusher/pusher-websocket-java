package com.pusher.client;

import com.pusher.client.connection.Connection;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.util.Factory;

public class Pusher {

    private final Connection connection;
    private final String apiKey;
    
    public Pusher(String apiKey) {
	
	if(apiKey == null || apiKey.isEmpty()) {
	    throw new IllegalArgumentException("API Key cannot be null or empty");
	}
	
	this.apiKey = apiKey;
	this.connection = Factory.newConnection();
    }
    
    /* Connection methods */
    
    public Connection getConnection()
    {
	return connection;
    }
    
    public void connect()
    {
	connection.connect(apiKey);
    }
    
    public void connect(ConnectionEventListener eventListener)
    {
	connection.setEventListener(eventListener);
	connection.connect(apiKey);
    }
}