package com.pusher.client.channel;

/**
 * Represents a user that is subscribed to a
 * {@link com.pusher.client.channel.PresenceChannel PresenceChannel}.
 */
public class User {

    private final String id;
    private final String jsonData;

    /**
     * Create a new user. Users should not be created within an application. Users are created within the library and represent subscriptions to presence channels.
     * @param id
     * @param jsonData
     */
    public User(String id, String jsonData) {
        this.id = id;
        this.jsonData = jsonData;
    }

    /**
     * A unique identifier for the user within a Pusher application.
     * @return The unique id.
     */
    public String getId() {
        return id;
    }

    /**
     * Custom additional information about a user.
     * @return
     */
    public String getInfo() {
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

        if (other instanceof User) {
            User otherUser = (User) other;
            return this.getId().equals(otherUser.getId())
                    && this.getInfo().equals(otherUser.getInfo());
        }

        return false;
    }
}
