/*
 * (C) Copyright 2006-2015 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 *
 */

package org.nuxeo.connect.connector.http;

import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
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
        HttpClient httpClient = new HttpClient();
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(connectHttpTimeout);
        ProxyHelper.configureProxyIfNeeded(httpClient, url);
        HttpMethod method = new GetMethod(url);
        method.setFollowRedirects(true);

        for (String name : headers.keySet()) {
            method.addRequestHeader(name, headers.get(name));
        }

        int rc = 0;
        try {
            rc = httpClient.executeMethod(method);
            switch (rc) {
            case HttpStatus.SC_OK:
            case HttpStatus.SC_NO_CONTENT:
            case HttpStatus.SC_NOT_FOUND:
                return new ConnectHttpResponse(httpClient, method);
            case HttpStatus.SC_UNAUTHORIZED:
                throw new ConnectSecurityError("Connect server refused authentication (returned 401)");
            case HttpStatus.SC_GATEWAY_TIMEOUT:
            case HttpStatus.SC_REQUEST_TIMEOUT:
                throw new ConnectServerError("Timeout " + rc);
            default:
                try {
                    String body = method.getResponseBodyAsString();
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
                }
            }
        } catch (ConnectServerError cse) {
            method.releaseConnection();
            throw cse;
        } catch (Throwable t) {
            String exName = t.getClass().getName();
            if (exName.startsWith("java.net") || exName.startsWith("org.apache.commons.httpclient")) {
                log.warn("Connect Server is not reachable");
                method.releaseConnection();
                throw new CanNotReachConnectServer(t.getMessage(), t);
            }
            method.releaseConnection();
            throw new ConnectServerError("Error during communication with the Nuxeo Connect Server", t);
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
