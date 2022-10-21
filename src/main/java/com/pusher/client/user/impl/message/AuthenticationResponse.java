package com.pusher.client.user.impl.message;

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
