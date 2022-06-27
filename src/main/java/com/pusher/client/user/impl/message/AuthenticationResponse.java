package com.pusher.client.connection.impl.message;

import com.google.gson.annotations.SerializedName;

public class AuthenticationResponse {
    private String auth;

    // we want to keep this as a String until needed because we send this back on requests
    @SerializedName("user_data")
    private String userData;

    public String getAuth() {
        return auth;
    }

    public String getUserData() {
        return userData;
    }
}
