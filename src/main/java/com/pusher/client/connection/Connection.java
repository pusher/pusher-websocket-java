package com.pusher.client.connection;

/**
 * Represents a connection to Pusher.
 *
 */
public interface Connection {

    /**
     * No need to call this via the API. Instead use {@link com.pusher.client.Pusher#connect}.
     */
    void connect();

    /**
     * Bind to connection events.
     *
     * @param state
     *            The states to bind to.
     * @param eventListener
     *            A listener to be called when the state changes.
     */
    void bind(ConnectionState state, ConnectionEventListener eventListener);

    /**
     * Unbind from connection state changes.
     *
     * @param state
     *            The state to unbind from.
     * @param eventListener
     *            The listener to be unbound.
     * @return <code>true</code> if the unbind was successful, otherwise
     *         <code>false</code>.
     */
    boolean unbind(ConnectionState state, ConnectionEventListener eventListener);

    /**
     * Gets the current connection state.
     *
     * @return The state.
     */
    ConnectionState getState();

    /**
     * Gets a unique connection ID.
     *
     * @return The id.
     */
    String getSocketId();
}
