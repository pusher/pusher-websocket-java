package com.pusher.client.util;

import com.pusher.client.AuthorizationFailureException;
import com.pusher.client.Authorizer;
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

/**
 * @deprecated
 * Please use {@link com.pusher.client.util.HttpChannelAuthorizer}
 */
@Deprecated
public class HttpAuthorizer extends HttpChannelAuthorizer implements Authorizer {
    /**
     * Creates a new authorizer.
     *
     * @param endPoint
     *            The endpoint to be called when authenticating.
     */
    public HttpAuthorizer(final String endPoint) {
        super(endPoint);
    }

    /**
     * Creates a new authorizer.
     *
     * @param endPoint The endpoint to be called when authenticating.
     * @param connectionFactory a custom connection factory to be used for building the connection
     */
    public HttpAuthorizer(final String endPoint, final ConnectionFactory connectionFactory) {
        super(endPoint, connectionFactory);
    }
}
