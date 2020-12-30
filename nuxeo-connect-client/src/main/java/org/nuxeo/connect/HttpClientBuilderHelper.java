/*
 * (C) Copyright 2006-2020 Nuxeo SA (http://nuxeo.com/) and others.
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

import java.util.concurrent.TimeUnit;

import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
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

    public static HttpClientBuilder getHttpClientBuilderWithoutProxy(Integer socketTimeout, Integer connectTimeout, String url) {
        return getHttpClientBuilder(socketTimeout, connectTimeout, url, false);
    }

    protected static HttpClientBuilder getHttpClientBuilder(Integer socketTimeout, Integer connectTimeout, String url, boolean useProxy) {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        // Define request configuration
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        // https://issues.apache.org/jira/browse/HTTPCLIENT-1763
        requestConfigBuilder.setCookieSpec(CookieSpecs.STANDARD);
        if (socketTimeout != null) {
            requestConfigBuilder.setSocketTimeout(socketTimeout);
        }
        if (connectTimeout != null) {
            requestConfigBuilder.setConnectTimeout(connectTimeout);
        }

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        if (useProxy) {
            ProxyHelper.configureProxyIfNeeded(requestConfigBuilder, credentialsProvider, url);
        }

        httpClientBuilder.setDefaultRequestConfig(requestConfigBuilder.build());
        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        httpClientBuilder.setConnectionTimeToLive(connectHttpTimeout, TimeUnit.MILLISECONDS);

        return httpClientBuilder;
    }
}
