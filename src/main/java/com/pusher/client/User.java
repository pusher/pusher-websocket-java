package com.pusher.client;

public class User {

    private final String id;
    private final String jsonData;

    public User(String id, String jsonData) {
	this.id = id;
	this.jsonData = jsonData;
    }

    public String getId() {
        return id;
    }

    public String getJsonData() {
        return jsonData;
    }
    
    @Override
    public String toString() {
	return String.format("[User id=%s, data=%s]", id, jsonData);
    }
}