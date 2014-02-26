/*
 * (C) Copyright 2006-2014 Nuxeo SA (http://nuxeo.com/) and contributors.
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

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.nuxeo.connect.connector.http.proxy.ProxyPacResolver;
import org.nuxeo.connect.connector.http.proxy.RhinoProxyPacResolver;

/**
 * Helper class to handle the HTTP Configuration
 *
 * @author tiry
 *
 */
public class ProxyHelper {

    protected static boolean useNTLM = false;

    protected static ProxyPacResolver pacResolver = new RhinoProxyPacResolver();

    protected static String PROXY_PAC_DIRECT = "DIRECT";

    public static void configureProxyIfNeeded(HttpClient httpClient, String url) {
        if (ConnectUrlConfig.useProxy()) {
            // configure http proxy
            if (ConnectUrlConfig.useProxyPac()) {
                String[] proxy = pacResolver.findProxy(url);
                if (proxy != null) {
                    httpClient.getHostConfiguration().setProxy(proxy[0],
                            Integer.parseInt(proxy[1]));
                }
            } else {
                httpClient.getHostConfiguration().setProxy(
                        ConnectUrlConfig.getProxyHost(),
                        ConnectUrlConfig.getProxyPort());
            }

            // configure proxy auth in BA
            if (ConnectUrlConfig.isProxyAuthenticated()) {
                if (ConnectUrlConfig.isProxyNTLM()) {
                    NTCredentials ntlmCredential = new NTCredentials(
                            ConnectUrlConfig.getProxyLogin(),
                            ConnectUrlConfig.getProxyPassword(),
                            ConnectUrlConfig.getProxyNTLMHost(),
                            ConnectUrlConfig.getProxyNTLMDomain());
                    httpClient.getState().setProxyCredentials(
                            new AuthScope(null, -1, AuthScope.ANY_REALM),
                            ntlmCredential);
                } else {
                    Credentials ba = new UsernamePasswordCredentials(
                            ConnectUrlConfig.getProxyLogin(),
                            ConnectUrlConfig.getProxyPassword());
                    httpClient.getState().setProxyCredentials(
                            new AuthScope(null, -1, AuthScope.ANY_REALM), ba);
                }
            }
        }
    }
}
