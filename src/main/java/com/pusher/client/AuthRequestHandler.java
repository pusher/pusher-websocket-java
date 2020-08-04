package com.pusher.client;

import com.pusher.client.channel.impl.AuthResponseData;

import java.util.List;

public interface AuthRequestHandler {

    AuthResponseData authRequest(String socketId, List<String> channels, Boolean appendToken);
}
