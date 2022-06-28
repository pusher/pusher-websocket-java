package com.pusher.client.connection.impl.message;

import com.google.gson.annotations.SerializedName;

public class AuthenticationResponse {
    private String auth;

    @SerializedName("user_data")
    private String userData;

    public String getAuth() {
        return auth;
    }

    public String getUserData() {
        return userData;
    }
}
