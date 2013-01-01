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
    
    @Override
    public int hashCode() {
	return id.hashCode() + ((jsonData != null) ? jsonData.hashCode() : 0);
    }
    
    @Override
    public boolean equals(Object other) {
	
	if(other instanceof User) {
	    User otherUser = (User) other;
	    return this.getId().equals(otherUser.getId()) && this.getJsonData().equals(otherUser.getJsonData());
	}
	
	return false;
    }
}