package com.pusher.client.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import com.pusher.client.AuthorizationFailureException;
import com.pusher.client.Authorizer;

public class HttpAuthorizer implements Authorizer {

    private final URL endPoint;
    private final HashMap<String, String> mHeaders;

    public HttpAuthorizer(String endPoint) {
    	this(endPoint, new HashMap<String, String>());
    }
    
    public HttpAuthorizer(String endPoint, HashMap<String, String>headers) {
    	try {
    		this.endPoint = Factory.newURL(endPoint);
    	} catch(MalformedURLException e) {
    		throw new IllegalArgumentException("Could not parse authentication end point into a valid URL", e);
    	}
    	this.mHeaders = headers;
    }

    @Override
    public String authorize(String channelName, String socketId) throws AuthorizationFailureException {

	try {
	    String urlParameters = "channel_name=" + URLEncoder.encode(channelName, "UTF-8") + "&socket_id=" + URLEncoder.encode(socketId, "UTF-8");
	    
	    HttpURLConnection connection = (HttpURLConnection) endPoint.openConnection();
	    connection.setDoOutput(true);
	    connection.setDoInput(true);
	    connection.setInstanceFollowRedirects(false);
	    connection.setRequestMethod("POST");
	    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	    connection.setRequestProperty("charset", "utf-8");
	    connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
	    
	    // Add in the user defined headers
	    for (String headerName : mHeaders.keySet()) {
	    	String headerValue = mHeaders.get(headerName);
	    	connection.setRequestProperty(headerName, headerValue);
	    }
	    
	    connection.setUseCaches(false);

	    // Send request
	    DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
	    wr.writeBytes(urlParameters);
	    wr.flush();
	    wr.close();

	    // Read response
	    InputStream is = connection.getInputStream();
	    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	    String line;
	    StringBuffer response = new StringBuffer();
	    while ((line = rd.readLine()) != null) {
		response.append(line);
	    }
	    rd.close();

	    return response.toString();

	} catch (IOException e) {
	    throw new AuthorizationFailureException(e);
	}
    }
}