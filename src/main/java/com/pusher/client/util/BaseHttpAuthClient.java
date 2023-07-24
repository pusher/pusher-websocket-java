package com.pusher.client.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/**
 * Base class for {@link com.pusher.client.util.HttpChannelAuthorizer} and {@link com.pusher.client.util.HttpUserAuthenticator}
 */

abstract class BaseHttpAuthClient {

    private final URL endPoint;
    private Map<String, String> mHeaders = new HashMap<>();
    protected ConnectionFactory mConnectionFactory;
    protected SSLSocketFactory mSslSocketFactory = null;

    /**
     * Creates a new auth client.
     *
     * @param endPoint The endpoint to be called when authorizing or authenticating.
     */
    public BaseHttpAuthClient(final String endPoint) {
        try {
            this.endPoint = new URL(endPoint);
            this.mConnectionFactory = new UrlEncodedConnectionFactory();
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException("Could not parse channel authorization end point into a valid URL", e);
        }
    }

    /**
     * Creates a new auth client.
     *
     * @param endPoint          The endpoint to be called when authorizing or authenticating.
     * @param connectionFactory a custom connection factory to be used for building the connection
     */
    public BaseHttpAuthClient(final String endPoint, final ConnectionFactory connectionFactory) {
        try {
            this.endPoint = new URL(endPoint);
            this.mConnectionFactory = connectionFactory;
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException("Could not parse channel authorization end point into a valid URL", e);
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
     * Set the SSL socket factory that is used for the connection. This allows to
     * configure custom certificate handling (self-signed certificates, mutual TLS, ...)
     * @param sslSocketFactory The SSL socket factory to use
     */
    public void setSslSocketFactory(final SSLSocketFactory sslSocketFactory) {
        mSslSocketFactory = sslSocketFactory;
    }

    /**
     * Identifies if the HTTP request will be sent over HTTPS.
     *
     * @return true if the endpoint protocol is 'https'
     */
    public Boolean isSSL() {
        return endPoint.getProtocol().equals("https");
    }

    /**
     * Performs an HTTP request to the endpoint provided on construction.
     * <p>
     * The request shall include the headers and parameters provided by
     * the connectionFactory.
     * <p>
     * Child classes must provide the connection parameters to the
     * connectionFactory before calling this method.
     *
     * @return HTTP request response body
     */
    protected String performAuthRequest() {
        try {
            String body = mConnectionFactory.getBody();

            final HashMap<String, String> defaultHeaders = new HashMap<>();
            defaultHeaders.put("Content-Type", mConnectionFactory.getContentType());
            defaultHeaders.put("charset", mConnectionFactory.getCharset());

            HttpURLConnection connection;
            if (isSSL()) {
                connection = (HttpsURLConnection) endPoint.openConnection();

                if(mSslSocketFactory != null) {
                    ((HttpsURLConnection) connection)
                            .setSSLSocketFactory(mSslSocketFactory);
                }
            } else {
                connection = (HttpURLConnection) endPoint.openConnection();
            }
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");

            // Add in the user defined headers
            defaultHeaders.putAll(mHeaders);
            // Add in the Content-Length, so it can't be overwritten by mHeaders
            defaultHeaders.put("Content-Length", "" + body.getBytes().length);

            for (final String headerName : defaultHeaders.keySet()) {
                final String headerValue = defaultHeaders.get(headerName);
                connection.setRequestProperty(headerName, headerValue);
            }

            connection.setUseCaches(false);

            // Send request
            final DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(body);
            wr.flush();
            wr.close();

            // Read response
            final InputStream is = connection.getInputStream();
            final BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            final StringBuilder response = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                response.append(line);
            }
            rd.close();

            final int responseHttpStatus = connection.getResponseCode();
            if (responseHttpStatus != 200 && responseHttpStatus != 201) {
                throw authFailureException(response.toString());
            }

            return response.toString();
        } catch (final IOException e) {
            throw authFailureException(e);
        }
    }

    protected abstract RuntimeException authFailureException(String msg);

    protected abstract RuntimeException authFailureException(IOException e);
}
