package com.pusher.client;

/**
 * Options to be used with a {@link com.pusher.client.Pusher} instance. 
 */
public class PusherOptions {

		private boolean encrypted = false;
    private Authorizer authorizer;
    
    /**
     * Gets whether an encrypted (SSL) connection should be used when connecting to Pusher.
     * @return true if an encrypted connection should be used; otherwise false.
     */
    public boolean getEncrypted() {
    	return encrypted;
    }
    
    /**
     * Sets an encrypted (SSL) connection should be used when connecting to Pusher.
     * @param encrypted
     * @return this, for chaining
     */
    public PusherOptions setEncrypted(boolean encrypted) {
    	this.encrypted = encrypted;
    	return this;
    }

    /**
     * Gets the authorizer to be used when authenticating private and presence channels.
     * @return the authorizer
     */
    public Authorizer getAuthorizer() {
    	return authorizer;
    }
    
    /**
     * Sets the authorizer to be used when authenticating private and presence channels.
     * @param authorizer The authorizer to be used.
     * @return this, for chaining
     */
    public PusherOptions setAuthorizer(Authorizer authorizer) {
    	this.authorizer = authorizer;
    	return this;
    }
}
