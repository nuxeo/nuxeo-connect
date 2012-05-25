/*
 * (C) Copyright 2006-2012 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
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
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.data.DownloadingPackage;
import org.nuxeo.connect.data.SubscriptionStatus;
import org.nuxeo.connect.update.PackageType;

/**
 *
 * Real HTTP based {@link ConnectConnector} implementation.
 * Manages communication with the Nuxeo Connect Server via JAX-RS
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class ConnectHttpConnector extends AbstractConnectConnector implements
        ConnectConnector {

    public String overrideUrl = null;

    protected SubscriptionStatus cachedStatus = null;

    protected boolean connectServerNotReachable;

    public static final String CONNECT_HTTP_CACHE_MINUTES_PROPERTY = "org.nuxeo.connect.http.cache.duration";

    public static final String CONNECT_SERVER_REACHABLE_PROPERTY = "org.nuxeo.connect.server.reachable";

    protected long lastStatusFetchTime;

    protected String getBaseUrl() {
        if (overrideUrl != null) {
            return overrideUrl;
        }
        return super.getBaseUrl();
    }

    protected boolean isConnectServerReachable() {
        return Boolean.parseBoolean(NuxeoConnectClient.getProperty(
                CONNECT_SERVER_REACHABLE_PROPERTY, "true"));
    }

    @Override
    protected ConnectServerResponse execServerCall(String url,
            Map<String, String> headers) throws ConnectServerError {
        HttpClient httpClient = new HttpClient();
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(
                10000);
        ProxyHelper.configureProxyIfNeeded(httpClient);
        HttpMethod method = new GetMethod(url);
        method.setFollowRedirects(true);

        for (String name : headers.keySet()) {
            method.addRequestHeader(name, headers.get(name));
        }

        int rc = 0;
        try {
            rc = httpClient.executeMethod(method);
            if (rc == 200) {
                return new ConnectHttpResponse(httpClient, method);
            } else {
                String body = method.getResponseBodyAsString();
                if (rc == 401) {
                    throw new ConnectSecurityError(
                            "Connect server refused authentication (returned 401)");
                }
                try {
                    JSONObject obj = new JSONObject(body);
                    String message = obj.getString("message");
                    String errorClass = obj.getString("errorClass");
                    ConnectServerError error;
                    if (ConnectSecurityError.class.getSimpleName().equals(
                            errorClass)) {
                        error = new ConnectServerError(message);
                    } else if (ConnectClientVersionMismatchError.class.getSimpleName().equals(
                            errorClass)) {
                        error = new ConnectClientVersionMismatchError(message);
                    } else {
                        error = new ConnectServerError(message);
                    }
                    throw error;
                } catch (JSONException e) {
                    log.error("Unable to parse error returned by server", e);
                    throw new ConnectServerError("Server returned a code " + rc);
                }
            }
        } catch (ConnectServerError cse) {
            method.releaseConnection();
            throw cse;
        } catch (Throwable t) {
            String exName = t.getClass().getName();
            if (exName.startsWith("java.net")
                    || exName.startsWith("org.apache.commons.httpclient")) {
                log.warn("Connect Server is not reachable");
                method.releaseConnection();
                throw new CanNotReachConnectServer(t.getMessage(), t);
            }
            method.releaseConnection();
            throw new ConnectServerError(
                    "Error during communication with the Nuxeo Connect Server",
                    t);
        }
    }

    protected int httpCacheDurationInMinutes() {
        String cacheInMinutes = NuxeoConnectClient.getProperty(
                CONNECT_HTTP_CACHE_MINUTES_PROPERTY, "0");
        try {
            return Integer.parseInt(cacheInMinutes);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    protected boolean useHttpCache() {
        return httpCacheDurationInMinutes() > 0;
    }

    public SubscriptionStatus getConnectStatus() throws ConnectServerError {
        if (!isConnectServerReachable()) {
            throw new CanNotReachConnectServer(
                    "Connect server set as not reachable");
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

    public List<DownloadablePackage> getDownloads(PackageType type)
            throws ConnectServerError {
        if (!isConnectServerReachable()) {
            return new ArrayList<DownloadablePackage>();
        }
        return super.getDownloads(type);
    }

    public DownloadingPackage getDownload(String id) throws ConnectServerError {
        if (!isConnectServerReachable()) {
            throw new CanNotReachConnectServer(
                    "Connect server set as not reachable");
        }
        return super.getDownload(id);
    }

}
