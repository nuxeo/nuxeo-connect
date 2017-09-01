/*
 * (C) Copyright 2006-2017 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 *     Yannis JULIENNE
 *
 */

package org.nuxeo.connect.connector.http;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuxeo.connect.NuxeoConnectClient;
import org.nuxeo.connect.connector.AbstractConnectConnector;
import org.nuxeo.connect.connector.CanNotReachConnectServer;
import org.nuxeo.connect.connector.ConnectClientVersionMismatchError;
import org.nuxeo.connect.connector.ConnectConnector;
import org.nuxeo.connect.connector.ConnectSecurityError;
import org.nuxeo.connect.connector.ConnectServerError;
import org.nuxeo.connect.connector.ConnectServerResponse;
import org.nuxeo.connect.data.SubscriptionStatus;

/**
 * Real HTTP based {@link ConnectConnector} implementation. Manages communication with the Nuxeo Connect Server via
 * JAX-RS
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class ConnectHttpConnector extends AbstractConnectConnector {

    public String overrideUrl = null;

    protected SubscriptionStatus cachedStatus = null;

    protected boolean connectServerNotReachable;

    public static final String CONNECT_HTTP_CACHE_MINUTES_PROPERTY = "org.nuxeo.connect.http.cache.duration";

    public static final String CONNECT_HTTP_TIMEOUT = "org.nuxeo.connect.http.timeout";

    protected int connectHttpTimeout = Integer.parseInt(NuxeoConnectClient.getProperty(CONNECT_HTTP_TIMEOUT, "10000"));

    protected long lastStatusFetchTime;

    @Override
    protected String getBaseUrl() {
        if (overrideUrl != null) {
            return overrideUrl;
        }
        return super.getBaseUrl();
    }

    @Override
    protected ConnectServerResponse execServerCall(String url, Map<String, String> headers) throws ConnectServerError {
        return execServer(true, url, headers);
    }

    @Override
    protected ConnectServerResponse execServerPost(String url, Map<String, String> headers) throws ConnectServerError {
        return execServer(false, url, headers);
    }

    protected ConnectServerResponse execServer(boolean get, String url, Map<String, String> headers)
            throws ConnectServerError {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        ProxyHelper.configureProxyIfNeeded(httpClientBuilder, url);
        httpClientBuilder.setConnectionTimeToLive(connectHttpTimeout, TimeUnit.MILLISECONDS);
        HttpUriRequest method = get ? new HttpGet(url) : new HttpPost(url);

        for (String name : headers.keySet()) {
            method.addHeader(name, headers.get(name));
        }

        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpResponse = null;
        try {
            // We do not use autoclose on the httpClient nor on the httpResponse since we may return them yet
            // not consumed in the ConnectHttpResponse
            httpClient = httpClientBuilder.build();
            httpResponse = httpClient.execute(method);
            int rc = httpResponse.getStatusLine().getStatusCode();
            switch (rc) {
            case HttpStatus.SC_OK:
            case HttpStatus.SC_NO_CONTENT:
            case HttpStatus.SC_NOT_FOUND:
                return new ConnectHttpResponse(httpClient, httpResponse);
            case HttpStatus.SC_UNAUTHORIZED:
                httpResponse.close();
                httpClient.close();
                throw new ConnectSecurityError("Connect server refused authentication (returned 401)");
            case HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED:
                httpResponse.close();
                httpClient.close();
                throw new ConnectSecurityError("Proxy server require authentication (returned 407)");
            case HttpStatus.SC_GATEWAY_TIMEOUT:
            case HttpStatus.SC_REQUEST_TIMEOUT:
                httpResponse.close();
                httpClient.close();
                throw new ConnectServerError("Timeout " + rc);
            default:
                try {
                    String body = EntityUtils.toString(httpResponse.getEntity());
                    JSONObject obj = new JSONObject(body);
                    String message = obj.getString("message");
                    String errorClass = obj.getString("errorClass");
                    ConnectServerError error;
                    if (ConnectSecurityError.class.getSimpleName().equals(errorClass)) {
                        error = new ConnectSecurityError(message);
                    } else if (ConnectClientVersionMismatchError.class.getSimpleName().equals(errorClass)) {
                        error = new ConnectClientVersionMismatchError(message);
                    } else {
                        error = new ConnectServerError(message);
                    }
                    throw error;
                } catch (JSONException e) {
                    log.debug("Can't parse server error " + rc, e);
                    throw new ConnectServerError("Server returned a code " + rc);
                } finally {
                    httpResponse.close();
                    httpClient.close();
                }
            }
        } catch (ConnectServerError cse) {
            throw cse;
        } catch (IOException e) {
            IOUtils.closeQuietly(httpResponse);
            IOUtils.closeQuietly(httpClient);
            throw new ConnectServerError("Error during communication with the Nuxeo Connect Server", e);
        }
    }

    protected int httpCacheDurationInMinutes() {
        String cacheInMinutes = NuxeoConnectClient.getProperty(CONNECT_HTTP_CACHE_MINUTES_PROPERTY, "0");
        try {
            return Integer.parseInt(cacheInMinutes);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    protected boolean useHttpCache() {
        return httpCacheDurationInMinutes() > 0;
    }

    @Override
    public SubscriptionStatus getConnectStatus() throws ConnectServerError {
        if (!isConnectServerReachable()) {
            throw new CanNotReachConnectServer("Connect server set as not reachable");
        }

        if (useHttpCache() && cachedStatus != null) {
            if ((System.currentTimeMillis() - lastStatusFetchTime) < httpCacheDurationInMinutes() * 60 * 1000) {
                return cachedStatus;
            }
        }

        try {
            SubscriptionStatus status = super.getConnectStatus();
            if (!NuxeoConnectClient.isTestModeSet() && useHttpCache()) {
                // no cache for testing
                cachedStatus = status;
            }
            lastStatusFetchTime = System.currentTimeMillis();
            return status;
        } catch (CanNotReachConnectServer e) {
            if (cachedStatus != null) {
                return cachedStatus;
            } else {
                throw e;
            }
        }
    }

}
