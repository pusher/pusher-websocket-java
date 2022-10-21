package com.pusher.client.channel.impl.message;

import com.google.gson.annotations.SerializedName;

public class SubscriptionCountData {

    @SerializedName("subscription_count")
    public Integer count;

    public int getCount() {
        return count;
    }
}
