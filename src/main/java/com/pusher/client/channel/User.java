package com.pusher.client.channel;

import com.google.gson.Gson;

/**
 * Represents a user that is subscribed to a
 * {@link com.pusher.client.channel.PresenceChannel PresenceChannel}.
 */
public class User {
    private static final Gson GSON = new Gson();
    private final String id;
    private final String jsonData;

    /**
     * Create a new user. Users should not be created within an application.
     * Users are created within the library and represent subscriptions to
     * presence channels.
     *
     * @param id The user id
     * @param jsonData The user JSON data
     */
    public User(final String id, final String jsonData) {
        this.id = id;
        this.jsonData = jsonData;
    }

    /**
     * A unique identifier for the user within a Pusher application.
     *
     * @return The unique id.
     */
    public String getId() {
        return id;
    }

    /**
     * Custom additional information about a user as a String encoding a JSON
     * hash
     * 
     * @return The user info as a JSON string
     */
    public String getInfo() {
        return jsonData;
    }

    /**
     * <p>
     * Custom additional information about a user decoded as a new instance of
     * the provided POJO bean type
     * </p>
     *
     * <p>
     * e.g. if {@link #getInfo()} returns
     * <code>{"name":"Mr User","number":9}</code> then you might implement as
     * follows:
     * </p>
     *
     * <pre>
     * public class UserInfo {
     *     private String name;
     *     private Integer number;
     *
     *     public String getName() { return name; }
     *     public void setName(String name) { this.name = name; }
     *
     *     public Integer getNumber() { return number; }
     *     public void setNumber(Integer number) { this.number = number; }
     * }
     *
     * UserInfo info = user.getInfo(UserInfo.class);
     *
     * info.getName() // returns "Mr User"
     * info.getNumber() // returns 9
     * </pre>
     *
     * @param <V> The class of the info
     * @param clazz
     *            the class into which the user info JSON representation should
     *            be parsed.
     * @return V An instance of clazz, populated with the user info
     */
    public <V> V getInfo(final Class<V> clazz) {
        return GSON.fromJson(jsonData, clazz);
    }

    @Override
    public String toString() {
        return String.format("[User id=%s, data=%s]", id, jsonData);
    }

    @Override
    public int hashCode() {
        return id.hashCode() + (jsonData != null ? jsonData.hashCode() : 0);
    }

    @Override
    public boolean equals(final Object other) {

        if (other instanceof User) {
            final User otherUser = (User)other;
            return getId().equals(otherUser.getId()) && this.getInfo().equals(otherUser.getInfo());
        }

        return false;
    }
}
