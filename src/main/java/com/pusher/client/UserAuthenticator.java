package com.pusher.client;

/**
 * Sigining in as a User on a connection requires authentication.
 * This interface provides an {@link #authenticate} method as a mechanism
 * for doing this.
 *
 * <p>
 * See the {@link com.pusher.client.util.HttpUserAuthenticator} as an
 * example.
 * </p>
 */
public interface UserAuthenticator {

    /**
     * Called when a user is to be authenticated.
     *
     * @param socketId
     *            A unique socket connection ID to be used with the
     *            authentication. This uniquely identifies the connection that
     *            on which the user is being authenticated.
     * @return A user authentication token.
     * @throws AuthenticationFailureException
     *            if the authentication fails.
     */
    String authenticate(String socketId) throws AuthenticationFailureException;
}
