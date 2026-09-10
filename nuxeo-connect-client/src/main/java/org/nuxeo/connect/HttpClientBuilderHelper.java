/*
 * (C) Copyright 2006-2026 Nuxeo (http://nuxeo.com/) and others.
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
 *     Gildas Lefevre
 */
package org.nuxeo.connect;

import static org.nuxeo.connect.connector.http.ConnectHttpConnector.CONNECT_HTTP_TIMEOUT;

import org.apache.hc.client5.http.auth.CredentialsStore;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.nuxeo.connect.connector.http.ProxyHelper;

/**
 * Helper to define the creation of the HttpClientBuilder at only one location.
 *
 * @since 1.8.1
 */
public class HttpClientBuilderHelper {

    protected static int connectHttpTimeout = Integer.parseInt(
            NuxeoConnectClient.getProperty(CONNECT_HTTP_TIMEOUT, "10000"));

    public static HttpClientBuilder getHttpClientBuilder(Integer socketTimeout, Integer connectTimeout, String url) {
        return getHttpClientBuilder(socketTimeout, connectTimeout, url, true);
    }

    public static HttpClientBuilder getHttpClientBuilderWithoutProxy(Integer socketTimeout, Integer connectTimeout,
            String url) {
        return getHttpClientBuilder(socketTimeout, connectTimeout, url, false);
    }

    protected static HttpClientBuilder getHttpClientBuilder(Integer socketTimeout, Integer connectTimeout, String url,
            boolean useProxy) {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        // Define request configuration
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        // https://issues.apache.org/jira/browse/HTTPCLIENT-1763
        requestConfigBuilder.setCookieSpec(StandardCookieSpec.RELAXED);
        if (socketTimeout != null) {
            requestConfigBuilder.setResponseTimeout(Timeout.ofMilliseconds(socketTimeout));
        }
        if (connectTimeout != null) {
            requestConfigBuilder.setConnectTimeout(Timeout.ofMilliseconds(connectTimeout));
        }

        CredentialsStore credentialsProvider = new BasicCredentialsProvider();
        if (useProxy) {
            ProxyHelper.configureProxyIfNeeded(requestConfigBuilder, credentialsProvider, url);
        }

        httpClientBuilder.setDefaultRequestConfig(requestConfigBuilder.build());
        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        httpClientBuilder.setConnectionManager(
                PoolingHttpClientConnectionManagerBuilder.create()
                                                         .setDefaultConnectionConfig(
                                                                 ConnectionConfig.custom()
                                                                                 .setTimeToLive(
                                                                                         TimeValue.ofMilliseconds(
                                                                                                 connectHttpTimeout))
                                                                                 .build())
                                                         .build());

        return httpClientBuilder;
    }
}
