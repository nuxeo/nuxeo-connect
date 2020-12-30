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
package org.nuxeo.connect.connector.http;

import static org.junit.Assert.assertEquals;
import static org.nuxeo.connect.connector.http.ConnectUrlConfig.NUXEO_PROXY_PAC_URL;

import org.junit.Before;
import org.junit.Test;
import org.nuxeo.connect.connector.http.proxy.ProxyPacResolver;
import org.nuxeo.connect.connector.http.proxy.TestProxyPacResolver;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

/**
 * Test class created after NXP-30018 to avoid having back the regression. The difference with
 * {@link TestProxyPacResolver} is that the proxy.pac file is fetched from a mock server in order to validate the HTTP
 * call made by the HttpClient.
 */
public class ProxyPacConfigurationTest {

    ProxyPacResolver solver;

    private MockWebServer proxyPacServer;

    // private MockWebServer connectServer;

    @Before
    public void setUp() throws Exception {
        proxyPacServer = new MockWebServer();
        // Forces the useProxyPac computation
        ConnectUrlConfig.useProxyPac = null;
        System.setProperty(NUXEO_PROXY_PAC_URL, String.format("http://%s/proxy.pac", proxyPacServer.getHostName()));
        solver = ProxyHelper.pacResolver;
    }

    @Test
    public void itCanFetchTheProxyPacFile() {
        proxyPacServer.enqueue(buildProxyPacResponse());

        String[] proxies = solver.findPacProxies("http://www.nuxeo.com");
        assertEquals(1, proxies.length);
        assertEquals("DIRECT", proxies[0]);
    }

    private MockResponse buildProxyPacResponse() {
        MockResponse response = new MockResponse()//
                                                  .setResponseCode(200)
                                                  .setBody("function FindProxyForURL(url, host) {\n"
                                                          + "    if (dnsResolve(host) == \"127.0.0.1\") {\n"
                                                          + "        return \"PROXY 127.0.0.1\";\n" + "    }\n"
                                                          + "    return \"DIRECT\";\n}");
        return response;
    }
}
