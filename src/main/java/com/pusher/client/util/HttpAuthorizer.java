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

import com.pusher.client.AuthorizationFailureException;
import com.pusher.client.Authorizer;

public class HttpAuthorizer implements Authorizer {

    private final URL endPoint;

    public HttpAuthorizer(String endPoint) {
	
	try {
	    this.endPoint = Factory.newURL(endPoint);
	} catch(MalformedURLException e) {
	    throw new IllegalArgumentException("Could not parse authentication end point into a valid URL", e);
	}
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