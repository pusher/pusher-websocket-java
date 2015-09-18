package com.pusher.client.connection;

/**
 * Represents a change in connection state.
 */
public class ConnectionStateChange {

    private final ConnectionState previousState;
    private final ConnectionState currentState;

    /**
     * Used within the library to create a connection state change. Not be used
     * used as part of the API.
     *
     * @param previousState
     * @param currentState
     */
    public ConnectionStateChange(final ConnectionState previousState, final ConnectionState currentState) {

        if (previousState == currentState) {
            throw new IllegalArgumentException(
                    "Attempted to create an connection state update where both previous and current state are: "
                            + currentState);
        }

        this.previousState = previousState;
        this.currentState = currentState;
    }

    /**
     * The previous connections state. The state the connection has transitioned
     * from.
     *
     * @return the previous state
     */
    public ConnectionState getPreviousState() {
        return previousState;
    }

    /**
     * The current connection state. The state the connection has transitioned
     * to.
     *
     * @return the new (current) state
     */
    public ConnectionState getCurrentState() {
        return currentState;
    }

    @Override
    public int hashCode() {
        return previousState.hashCode() + currentState.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj instanceof ConnectionStateChange) {
            final ConnectionStateChange other = (ConnectionStateChange)obj;
            return currentState == other.currentState && previousState == other.previousState;
        }

        return false;
    }
}
