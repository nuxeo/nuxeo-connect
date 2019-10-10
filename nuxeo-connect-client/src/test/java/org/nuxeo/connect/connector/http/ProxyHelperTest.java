/*
 * (C) Copyright 2017 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Damien METZLER
 */
package org.nuxeo.connect.connector.http;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nuxeo.connect.connector.ConnectServerError;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class ProxyHelperTest {

    private ConnectHttpConnector httpConnector;

    private MockWebServer proxyServer;

    private MockWebServer connectServer;

    @Before
    public void setUp() throws Exception {
        proxyServer = new MockWebServer();
        connectServer = new MockWebServer();
        // Forces the useProxy computation
        ConnectUrlConfig.useProxy = null;
        System.setProperty(ConnectUrlConfig.NUXEO_PROXY_HOST_PROPERTY, proxyServer.getHostName());
        System.setProperty(ConnectUrlConfig.NUXEO_PROXY_PORT_PROPERTY, Integer.toString(proxyServer.getPort()));
        // Configure httpConnector
        httpConnector = new ConnectHttpConnector();
        httpConnector.overrideUrl = connectServer.url("/").toString();
    }

    @After
    public void tearDown() throws Exception {
        try {
            httpConnector.flushCache();
        } finally {
            proxyServer.shutdown();
        }
        System.getProperties().remove(ConnectUrlConfig.NUXEO_PROXY_HOST_PROPERTY);
        System.getProperties().remove(ConnectUrlConfig.NUXEO_PROXY_PORT_PROPERTY);
        // Put back to init value
        ConnectUrlConfig.useProxy = null;
    }

    @Test
    public void it_should_call_only_the_proxy() throws ConnectServerError, InterruptedException {
        // GIVEN server and proxy returning a response
        proxyServer.enqueue(buildDefaultResponse());
        connectServer.enqueue(buildDefaultResponse());

        // WHEN getting connect status
        httpConnector.getConnectStatus();

        // THEN Connect server should not be contacted
        assertThat(connectServer.getRequestCount()).isEqualTo(0);

        // THEN proxy should take the request
        RecordedRequest request = proxyServer.takeRequest();
        assertThat(request).isNotNull();

    }

    private MockResponse buildDefaultResponse() {
        MockResponse response = new MockResponse()//
                                                  .setResponseCode(200)
                                                  .setBody("{}");
        return response;
    }

}
