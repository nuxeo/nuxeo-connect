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
 *     Nuxeo
 *     Yannis JULIENNE
 */

package org.nuxeo.connect.connector.http;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.nuxeo.connect.connector.http.proxy.NashornProxyPacResolver;
import org.nuxeo.connect.connector.http.proxy.ProxyPacResolver;

/**
 * Helper class to handle the HTTP Configuration
 *
 * @author tiry
 */
public class ProxyHelper {

    protected static boolean useNTLM = false;

    // public for tests
    public static ProxyPacResolver pacResolver = new NashornProxyPacResolver();

    protected static String PROXY_PAC_DIRECT = "DIRECT";

    /**
     * Configure proxy settings.
     *
     * @deprecated since 1.7.8 as it uses {@link HttpClientBuilder#setDefaultRequestConfig(RequestConfig)} that may be
     *             overridden by other calls that do not only affect proxy settings Prefer using
     *             {@link #configureProxyIfNeeded(org.apache.http.client.config.RequestConfig.Builder, CredentialsProvider, String)}
     */
    @Deprecated
    public static void configureProxyIfNeeded(HttpClientBuilder httpClientBuilder, String url) {
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        ProxyHelper.configureProxyIfNeeded(requestConfigBuilder, credentialsProvider, url);
        httpClientBuilder.setDefaultRequestConfig(requestConfigBuilder.build());
        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
    }

    public static void configureProxyIfNeeded(RequestConfig.Builder requestConfigBuilder,
            CredentialsProvider credentialsProvider, String url) {
        if (ConnectUrlConfig.useProxy()) {
            // configure proxy host
            HttpHost proxyHost = null;
            if (ConnectUrlConfig.useProxyPac()) {
                String[] proxy = pacResolver.findProxy(url);
                if (proxy != null) {
                    proxyHost = new HttpHost(proxy[0], Integer.parseInt(proxy[1]));
                }
            } else {
                proxyHost = new HttpHost(ConnectUrlConfig.getProxyHost(), ConnectUrlConfig.getProxyPort());
            }

            if (proxyHost != null) {
                requestConfigBuilder.setProxy(proxyHost);
                // configure proxy auth in BA
                if (ConnectUrlConfig.isProxyAuthenticated()) {
                    AuthScope authScope = new AuthScope(proxyHost.getHostName(), proxyHost.getPort(),
                            AuthScope.ANY_REALM);
                    if (ConnectUrlConfig.isProxyNTLM()) {
                        NTCredentials ntlmCredential = new NTCredentials(ConnectUrlConfig.getProxyLogin(),
                                ConnectUrlConfig.getProxyPassword(), ConnectUrlConfig.getProxyNTLMHost(),
                                ConnectUrlConfig.getProxyNTLMDomain());
                        credentialsProvider.setCredentials(authScope, ntlmCredential);
                    } else {
                        Credentials ba = new UsernamePasswordCredentials(ConnectUrlConfig.getProxyLogin(),
                                ConnectUrlConfig.getProxyPassword());
                        credentialsProvider.setCredentials(authScope, ba);
                    }
                }
            }
        }
    }
}
