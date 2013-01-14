package com.pusher.client;

public interface Authorizer {

    String authorize(String channelName, String socketId) throws AuthorizationFailureException;
}