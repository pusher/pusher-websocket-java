package com.pusher.client;

public class PusherOptions {

    private Authorizer authorizer;
    
    public boolean getEncrypted() {
	return false;
    }
/*    
    public void setEncrypted() {
	// TODO: implement wss protocol
    }
*/
    public Authorizer getAuthorizer() {
	return authorizer;
    }
    
    public PusherOptions setAuthorizer(Authorizer authorizer) {
	this.authorizer = authorizer;
	return this;
    }
}
