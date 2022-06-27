package com.pusher.client.connection.impl.message;

import java.util.HashMap;
import java.util.Map;

public class SigninMessage {
    private String event = "pusher:signin";
    private Map<String, String> data = new HashMap<>();

    public SigninMessage(String auth, String userData) {
        data.put("auth", auth);
        data.put("user_data", userData);
    }
}
