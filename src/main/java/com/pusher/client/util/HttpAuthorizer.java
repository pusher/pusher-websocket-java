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
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import com.pusher.client.AuthorizationFailureException;
import com.pusher.client.Authorizer;

/**
 * Used to authenticate a {@link com.pusher.client.channel.PrivateChannel
 * private} or {@link com.pusher.client.channel.PresenceChannel presence}
 * channel subscription.
 *
 * <p>
 * Makes an HTTP request to a defined HTTP endpoint. Expects an authentication
 * token to be returned.
 * </p>
 *
 * <p>
 * For more information see the <a
 * href="http://pusher.com/docs/authenticating_users">Authenticating Users
 * documentation</a>.
 */
public class HttpAuthorizer implements Authorizer {

    private final URL endPoint;
    private Map<String, String> mHeaders = new HashMap<String, String>();
    private Map<String, String> mQueryStringParameters = new HashMap<String, String>();
    private final String ENCODING_CHARACTER_SET = "UTF-8";

    /**
     * Creates a new authorizer.
     *
     * @param endPoint
     *            The endpoint to be called when authenticating.
     */
    public HttpAuthorizer(final String endPoint) {
        try {
            this.endPoint = new URL(endPoint);
        }
        catch (final MalformedURLException e) {
            throw new IllegalArgumentException("Could not parse authentication end point into a valid URL", e);
        }
    }

    /**
     * Set additional headers to be sent as part of the request.
     *
     * @param headers A map of headers
     */
    public void setHeaders(final Map<String, String> headers) {
        mHeaders = headers;
    }

    /**
     * Identifies if the HTTP request will be sent over HTTPS.
     * @return true if the endpoint protocol is 'https'
     */
    public Boolean isSSL() {
        return endPoint.getProtocol().equals("https");
    }

    /**
     * This methods is for passing extra parameters authentication that needs to
     * be added to query string.
     *
     * @param queryStringParameters
     *            the query parameters
     */
    public void setQueryStringParameters(final HashMap<String, String> queryStringParameters) {
        mQueryStringParameters = queryStringParameters;
    }

    @Override
    public String authorize(final String channelName, final String socketId) throws AuthorizationFailureException {

        try {
            final StringBuffer urlParameters = new StringBuffer();
            urlParameters.append("channel_name=").append(URLEncoder.encode(channelName, ENCODING_CHARACTER_SET));
            urlParameters.append("&socket_id=").append(URLEncoder.encode(socketId, ENCODING_CHARACTER_SET));

            // Adding extra parameters supplied to be added to query string.
            for (final String parameterName : mQueryStringParameters.keySet()) {
                urlParameters.append("&").append(parameterName).append("=");
                urlParameters.append(URLEncoder.encode(mQueryStringParameters.get(parameterName),
                        ENCODING_CHARACTER_SET));
            }

            HttpURLConnection connection;
            if (isSSL()) {
                connection = (HttpsURLConnection)endPoint.openConnection();
            }
            else {
                connection = (HttpURLConnection)endPoint.openConnection();
            }
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length",
                    "" + Integer.toString(urlParameters.toString().getBytes().length));

            // Add in the user defined headers
            for (final String headerName : mHeaders.keySet()) {
                final String headerValue = mHeaders.get(headerName);
                connection.setRequestProperty(headerName, headerValue);
            }

            connection.setUseCaches(false);

            // Send request
            final DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(urlParameters.toString());
            wr.flush();
            wr.close();

            // Read response
            final InputStream is = connection.getInputStream();
            final BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            final StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
            }
            rd.close();

            final int responseHttpStatus = connection.getResponseCode();
            if (responseHttpStatus != 200 && responseHttpStatus != 201) {
                throw new AuthorizationFailureException(response.toString());
            }

            return response.toString();

        }
        catch (final IOException e) {
            throw new AuthorizationFailureException(e);
        }
    }
}
