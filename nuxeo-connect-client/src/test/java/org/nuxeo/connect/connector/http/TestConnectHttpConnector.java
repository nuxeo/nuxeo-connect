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
 *     Yannis JULIENNE
 */
package org.nuxeo.connect.connector.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.assertj.core.api.Fail;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nuxeo.connect.NuxeoConnectClient;
import org.nuxeo.connect.connector.AbstractConnectConnector;
import org.nuxeo.connect.connector.ConnectClientVersionMismatchError;
import org.nuxeo.connect.connector.ConnectSecurityError;
import org.nuxeo.connect.connector.ConnectServerError;
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.data.DownloadingPackage;
import org.nuxeo.connect.data.SubscriptionStatus;
import org.nuxeo.connect.update.PackageType;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * @since 1.5
 */
public class TestConnectHttpConnector {

    private final String testTargetPlatform = "server-10.3";

    private ConnectHttpConnector httpConnector;

    private MockWebServer mockServer;

    @Before
    public void setUp() throws Exception {

        mockServer = new MockWebServer();
        // Configure httpConnector
        httpConnector = new ConnectHttpConnector();
        httpConnector.overrideUrl = mockServer.url("/").toString();
    }

    @After
    public void tearDown() throws Exception {

        try {
            httpConnector.flushCache();
            // remove potentially loaded DownloadingPackages
            for (DownloadingPackage downloadingPackage : NuxeoConnectClient.getDownloadManager()
                                                                           .listDownloadingPackages()) {
                NuxeoConnectClient.getDownloadManager().removeDownloadingPackage(downloadingPackage.getId());
            }
        } finally {
            mockServer.shutdown();
        }
    }

    @Test
    public void it_should_handle_OK_response_for_connect_status() throws ConnectServerError, InterruptedException {

        // GIVEN a server answering with a OK response
        mockServer.enqueue(buildDefaultResponse());

        // WHEN getting connect status
        SubscriptionStatus connectStatus = httpConnector.getConnectStatus();

        // THEN it should not have throw any exception and have created a SubscriptionStatus object
        assertThat(connectStatus).isNotNull();
        assertThatPathIsCalled(mockServer, AbstractConnectConnector.GET_STATUS_SUFFIX);

    }

    @Test
    public void it_should_handle_OK_response_for_get_downloads() throws ConnectServerError, InterruptedException {
        // GIVEN a server answering with a OK response
        mockServer.enqueue(buildDefaultResponse().setBody("[{\"name\" : \"test1\"}, {\"name\" : \"test2\"}]"));

        // WHEN getting downloads
        String typeStr = String.valueOf(PackageType.ADDON);
        List<DownloadablePackage> downloads = httpConnector.getDownloads(PackageType.ADDON, testTargetPlatform);

        // THEN it should not have throw any exception and have created a list of DownloadablePackage objects
        assertThat(downloads).isNotNull().hasSize(2);
        assertThatPathIsCalled(mockServer, AbstractConnectConnector.GET_DOWNLOADS_SUFFIX + "/" + typeStr
                + "?targetPlatform=" + testTargetPlatform);
    }

    @Test
    public void it_should_handle_OK_response_for_get_download() throws ConnectServerError, InterruptedException {
        // GIVEN a server answering with a OK response
        String pkgName = "test1";
        String pkgVersion = "1.0.0";
        String pkgId = pkgName + "-" + pkgVersion;

        String payload = String.format("{\"name\" : \"%s\", \"version\" : \"%s\"}", pkgName, pkgVersion);
        mockServer.enqueue(buildDefaultResponse().setBody(payload));

        // WHEN getting download
        DownloadingPackage download = httpConnector.getDownload(pkgId);

        // THEN it should not have throw any exception and have created a list of DownloadablePackage objects
        assertThatPathIsCalled(mockServer, AbstractConnectConnector.GET_DOWNLOAD_SUFFIX + "/" + pkgId);
        assertThat(download).isNotNull();
    }

    @Test
    public void it_should_handle_NOT_FOUND_response_for_connect_status() throws ConnectServerError {
        // GIVEN a server answering with a NOT_FOUND response
        MockResponse response = buildDefaultResponse().setResponseCode(404);
        mockServer.enqueue(response);

        // WHEN getting connect status
        SubscriptionStatus connectStatus = httpConnector.getConnectStatus();

        // THEN it should not have throw any exception and have created a SubscriptionStatus object
        assertThat(connectStatus).isNotNull();
    }

    @Test
    public void it_should_handle_NOT_FOUND_response_for_get_downloads()
            throws ConnectServerError, InterruptedException {
        // GIVEN a server answering with a NOT_FOUND response
        String typeStr = String.valueOf(PackageType.ADDON);

        MockResponse response = buildDefaultResponse().setBody("[{\"name\" : \"test1\"}, {\"name\" : \"test2\"}]")
                                                      .setResponseCode(404);
        mockServer.enqueue(response);

        // WHEN getting downloads
        List<DownloadablePackage> downloads = httpConnector.getDownloads(PackageType.ADDON, testTargetPlatform);

        // THEN it should not have throw any exception and have created a list of DownloadablePackage objects
        assertThat(downloads).isNotNull().hasSize(2);
        assertThatPathIsCalled(mockServer, AbstractConnectConnector.GET_DOWNLOADS_SUFFIX + "/" + typeStr
                + "?targetPlatform=" + testTargetPlatform);
    }

    @Test
    public void it_should_handle_NOT_FOUND_response_for_get_download() throws ConnectServerError {
        // GIVEN a server answering with a OK response
        String pkgName = "test1";
        String pkgVersion = "1.0.0";
        String pkgId = pkgName + "-" + pkgVersion;

        MockResponse response = buildDefaultResponse().setBody(
                "{\"name\" : \"" + pkgName + "\", \"version\" : \"" + pkgVersion + "\"}").setResponseCode(404);
        mockServer.enqueue(response);

        // WHEN getting download
        DownloadingPackage download = httpConnector.getDownload(pkgId);

        // THEN it should not have throw any exception and have created a DownloadingPackage object
        assertThat(download).isNotNull();
    }

    @Test
    public void it_should_handle_NO_CONTENT_response_for_connect_status() throws InterruptedException {
        // GIVEN a server answering with a NO_CONTENT response
        MockResponse response = buildDefaultResponse().setResponseCode(HttpServletResponse.SC_NO_CONTENT);
        mockServer.enqueue(response);

        // WHEN getting connect status
        try {
            httpConnector.getConnectStatus();
            Fail.failBecauseExceptionWasNotThrown(ConnectServerError.class);
        } catch (ConnectServerError e) {
            // THEN it should have thrown a ConnectServerError with the correct message
            assertThatPathIsCalled(mockServer, AbstractConnectConnector.GET_STATUS_SUFFIX);
            assertThat(e.getMessage()).isEqualTo("null response from server");
        }
    }

    @Test
    public void it_should_handle_NO_CONTENT_response_for_get_downloads()
            throws ConnectServerError, InterruptedException {
        // GIVEN a server answering with a NOT_FOUND response
        mockServer.enqueue(buildDefaultResponse().setResponseCode(HttpServletResponse.SC_NO_CONTENT));

        // WHEN getting downloads
        String typeStr = String.valueOf(PackageType.ADDON);
        List<DownloadablePackage> downloads = httpConnector.getDownloads(PackageType.ADDON, testTargetPlatform);

        // THEN it should not have throw any exception and have returned an empty list of DownloadablePackage objects
        assertThat(downloads).isNotNull().hasSize(0);
        assertThatPathIsCalled(mockServer, AbstractConnectConnector.GET_DOWNLOADS_SUFFIX + "/" + typeStr
                + "?targetPlatform=" + testTargetPlatform);
    }

    @Test
    public void it_should_handle_NO_CONTENT_response_for_get_download()
            throws ConnectServerError, InterruptedException {
        // GIVEN a server answering with a OK response
        String pkgName = "test1";
        String pkgVersion = "1.0.0";
        String pkgId = pkgName + "-" + pkgVersion;

        mockServer.enqueue(buildDefaultResponse().setResponseCode(HttpServletResponse.SC_NO_CONTENT));

        // WHEN getting download
        DownloadingPackage download = httpConnector.getDownload(pkgId);

        // THEN it should not have throw any exception and have returned null
        assertThat(download).isNull();
        assertThatPathIsCalled(mockServer, AbstractConnectConnector.GET_DOWNLOAD_SUFFIX + "/" + pkgId);
    }

    @Test
    public void it_should_handle_UNAUTHORIZED_response() throws InterruptedException {
        // GIVEN a server answering with a UNAUTHORIZED response
        MockResponse response = buildDefaultResponse().setResponseCode(HttpServletResponse.SC_UNAUTHORIZED);
        mockServer.enqueue(response);

        // WHEN getting connect status

        try {
            httpConnector.getConnectStatus();
            Fail.failBecauseExceptionWasNotThrown(ConnectServerError.class);
        } catch (ConnectServerError e) {
            // THEN it should have thrown a ConnectServerError with the correct message
            assertThatPathIsCalled(mockServer, AbstractConnectConnector.GET_STATUS_SUFFIX);
            assertThat(e.getMessage()).isEqualTo("Connect server refused authentication (returned 401)");
        }

        // AND

        // WHEN getting downloads
        mockServer.enqueue(response);
        try {
            httpConnector.getDownloads(PackageType.ADDON, testTargetPlatform);
            Fail.failBecauseExceptionWasNotThrown(ConnectServerError.class);
        } catch (ConnectServerError e) {
            // THEN it should have thrown a ConnectServerError with the correct message
            String typeStr = String.valueOf(PackageType.ADDON);
            assertThatPathIsCalled(mockServer, AbstractConnectConnector.GET_DOWNLOADS_SUFFIX + "/" + typeStr
                    + "?targetPlatform=" + testTargetPlatform);
            assertThat(e.getMessage()).isEqualTo("Connect server refused authentication (returned 401)");
        }

        // AND

        // WHEN getting download
        String pkgName = "test1";
        String pkgVersion = "1.0.0";
        String pkgId = pkgName + "-" + pkgVersion;
        mockServer.enqueue(response);

        try {
            httpConnector.getDownload(pkgId);
            Fail.failBecauseExceptionWasNotThrown(ConnectServerError.class);
        } catch (ConnectServerError e) {
            // THEN it should have thrown a ConnectServerError with the correct message
            assertThatPathIsCalled(mockServer, AbstractConnectConnector.GET_DOWNLOAD_SUFFIX + "/" + pkgId);
            assertThat(e.getMessage()).isEqualTo("Connect server refused authentication (returned 401)");
        }
    }

    @Test
    public void it_should_handle_GATEWAY_TIMEOUT_response() throws InterruptedException {
        // GIVEN a server answering with a GATEWAY_TIMEOUT response\MockResponse response = new MockResponse()//
        MockResponse response = new MockResponse()//
                                                  .setBody("{}")
                                                  .setResponseCode(HttpServletResponse.SC_GATEWAY_TIMEOUT);

        mockServer.enqueue(response);

        // WHEN getting connect status
        try {
            httpConnector.getConnectStatus();
            Fail.failBecauseExceptionWasNotThrown(ConnectServerError.class);
        } catch (ConnectServerError e) {
            // THEN it should have thrown a ConnectServerError with the correct message
            assertThatPathIsCalled(mockServer, AbstractConnectConnector.GET_STATUS_SUFFIX);
            assertThat(e.getMessage()).isEqualTo("Timeout " + HttpServletResponse.SC_GATEWAY_TIMEOUT);
        }

        // AND

        // WHEN getting downloads
        mockServer.enqueue(response);
        try {
            httpConnector.getDownloads(PackageType.ADDON, testTargetPlatform);
            Fail.failBecauseExceptionWasNotThrown(ConnectServerError.class);
        } catch (ConnectServerError e) {
            // THEN it should have thrown a ConnectServerError with the correct message
            String typeStr = String.valueOf(PackageType.ADDON);
            assertThatPathIsCalled(mockServer, AbstractConnectConnector.GET_DOWNLOADS_SUFFIX + "/" + typeStr
                    + "?targetPlatform=" + testTargetPlatform);
            assertThat(e.getMessage()).isEqualTo("Timeout " + HttpServletResponse.SC_GATEWAY_TIMEOUT);
        }

        // AND

        // WHEN getting download
        mockServer.enqueue(response);
        String pkgName = "test1";
        String pkgVersion = "1.0.0";
        String pkgId = pkgName + "-" + pkgVersion;

        try {
            httpConnector.getDownload(pkgId);
            Fail.failBecauseExceptionWasNotThrown(ConnectServerError.class);
        } catch (ConnectServerError e) {
            // THEN it should have thrown a ConnectServerError with the correct message
            assertThatPathIsCalled(mockServer, AbstractConnectConnector.GET_DOWNLOAD_SUFFIX + "/" + pkgId);
            assertThat(e.getMessage()).isEqualTo("Timeout " + HttpServletResponse.SC_GATEWAY_TIMEOUT);
        }
    }

    @Test
    public void it_should_handle_REQUEST_TIMEOUT_response() throws InterruptedException {
        // GIVEN a server answering with a REQUEST_TIMEOUT response
        MockResponse response = buildDefaultResponse().setResponseCode(HttpServletResponse.SC_REQUEST_TIMEOUT);

        // WHEN getting connect status
        mockServer.enqueue(response);
        try {
            httpConnector.getConnectStatus();
            Fail.failBecauseExceptionWasNotThrown(ConnectServerError.class);
        } catch (ConnectServerError e) {
            // THEN it should have thrown a ConnectServerError with the correct message
            assertThatPathIsCalled(mockServer, AbstractConnectConnector.GET_STATUS_SUFFIX);
            assertThat(e.getMessage()).isEqualTo("Timeout " + HttpServletResponse.SC_REQUEST_TIMEOUT);
        }

        // AND

        // WHEN getting downloads
        mockServer.enqueue(response);
        String typeStr = String.valueOf(PackageType.ADDON);
        try {
            httpConnector.getDownloads(PackageType.ADDON, testTargetPlatform);
            Fail.failBecauseExceptionWasNotThrown(ConnectServerError.class);
        } catch (ConnectServerError e) {
            // THEN it should have thrown a ConnectServerError with the correct message
            assertThatPathIsCalled(mockServer, AbstractConnectConnector.GET_DOWNLOADS_SUFFIX + "/" + typeStr
                    + "?targetPlatform=" + testTargetPlatform);
            assertThat(e.getMessage()).isEqualTo("Timeout " + HttpServletResponse.SC_REQUEST_TIMEOUT);
        }

        // AND

        // WHEN getting download
        mockServer.enqueue(response);
        String pkgName = "test1";
        String pkgVersion = "1.0.0";
        String pkgId = pkgName + "-" + pkgVersion;
        try {
            httpConnector.getDownload(pkgId);
            Fail.failBecauseExceptionWasNotThrown(ConnectServerError.class);
        } catch (ConnectServerError e) {
            // THEN it should have thrown a ConnectServerError with the correct message
            assertThatPathIsCalled(mockServer, AbstractConnectConnector.GET_DOWNLOAD_SUFFIX + "/" + pkgId);
            assertThat(e.getMessage()).isEqualTo("Timeout " + HttpServletResponse.SC_REQUEST_TIMEOUT);
        }
    }

    @Test
    public void it_should_handle_INTERNAL_SERVER_ERROR_response() throws ConnectServerError, InterruptedException {
        // GIVEN a server answering with an INTERNAL_SERVER_ERROR response and a json formatted ConnectSecurityError
        // error

        MockResponse response = new MockResponse()//
                                                  .setBody(
                                                          "{\"errorClass\" : \"ConnectSecurityError\", \"message\" : \"server message\"}")
                                                  .setResponseCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        mockServer.enqueue(response);
        // WHEN getting connect status
        try {
            httpConnector.getConnectStatus();
            Fail.failBecauseExceptionWasNotThrown(ConnectSecurityError.class);
        } catch (ConnectSecurityError e) {
            // THEN it should have thrown a ConnectSecurityError with the correct message
            assertThatPathIsCalled(mockServer, AbstractConnectConnector.GET_STATUS_SUFFIX);
            assertThat(e.getMessage()).isEqualTo("server message");
        }

        // GIVEN a server answering with an INTERNAL_SERVER_ERROR response and a json formatted
        // ConnectClientVersionMismatchError error
        response = new MockResponse()//
                                     .setBody(
                                             "{\"errorClass\" : \"ConnectClientVersionMismatchError\", \"message\" : \"server message\"}")
                                     .setResponseCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        mockServer.enqueue(response);

        // WHEN getting downloads
        String typeStr = String.valueOf(PackageType.ADDON);
        try {
            httpConnector.getDownloads(PackageType.ADDON, testTargetPlatform);
            Fail.failBecauseExceptionWasNotThrown(ConnectClientVersionMismatchError.class);
        } catch (ConnectClientVersionMismatchError e) {
            // THEN it should have thrown a ConnectClientVersionMismatchError with the correct message
            assertThatPathIsCalled(mockServer, AbstractConnectConnector.GET_DOWNLOADS_SUFFIX + "/" + typeStr
                    + "?targetPlatform=" + testTargetPlatform);
            assertThat(e.getMessage()).isEqualTo("server message");
        }

        // GIVEN a server answering with an INTERNAL_SERVER_ERROR response and a json formatted unknown error
        response = new MockResponse()//
                                     .setBody("{\"errorClass\" : \"UnknownError\", \"message\" : \"server message\"}")
                                     .setResponseCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        mockServer.enqueue(response);

        // WHEN getting download
        String pkgName = "test1";
        String pkgVersion = "1.0.0";
        String pkgId = pkgName + "-" + pkgVersion;
        try {
            httpConnector.getDownload(pkgId);
            Fail.failBecauseExceptionWasNotThrown(ConnectServerError.class);
        } catch (ConnectServerError e) {
            // THEN it should have thrown a ConnectServerError with the correct message
            assertThatPathIsCalled(mockServer, AbstractConnectConnector.GET_DOWNLOAD_SUFFIX + "/" + pkgId);
            assertThat(e.getMessage()).isEqualTo("server message");
        }

        // GIVEN a server answering with an INTERNAL_SERVER_ERROR response and a malformed json content

        response = new MockResponse()//
                                     .setBody("\"{malformed JSON\"")
                                     .setResponseCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        mockServer.enqueue(response);

        // WHEN getting connect status
        try {
            httpConnector.getConnectStatus();
            Fail.failBecauseExceptionWasNotThrown(ConnectServerError.class);
        } catch (ConnectServerError e) {
            // THEN it should have thrown a ConnectServerError with the correct message
            assertThatPathIsCalled(mockServer, AbstractConnectConnector.GET_STATUS_SUFFIX);
            assertThat(e.getMessage()).isEqualTo(
                    "Server returned a code " + HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Test
    public void call_with_set_cookie_with_expires_should_not_raise_a_warning() throws Exception {
        // Setup log4j to capture logs
        TestAppender appender = new TestAppender();
        Logger logger = Logger.getRootLogger();
        logger.addAppender(appender);

        // Given a Connect server that returns a Set-Cookie with Expires value
        MockResponse response = buildDefaultResponse().setHeader("Set-Cookie",
                "Path=/;Max-Age=31536000;Secure;Expires=Wed, 17 Jun 2020 16:00:18 GMT;HttpOnly");
        mockServer.enqueue(response);

        // When I do a simple call on the Connect API
        ConnectHttpConnector httpConnector = new ConnectHttpConnector();
        httpConnector.overrideUrl = mockServer.url("/").url().toString();
        SubscriptionStatus status = httpConnector.getConnectStatus();
        assertNotNull(status);

        List<LoggingEvent> warnings = appender.getLog().stream().filter(l -> l.getLevel().equals(Level.WARN)).collect(
                Collectors.toList());

        // Then I should not get any warnings in the logs
        assertEquals(0, warnings.size());
        logger.removeAppender(appender);
    }

    @Test
    public void all_call_use_gzip_encoding() throws Exception {

        mockServer.enqueue(buildDefaultResponse());
        httpConnector.getConnectStatus();
        RecordedRequest request = mockServer.takeRequest();
        assertThat(request.getHeader("Accept-Encoding")).contains("gzip");

        mockServer.enqueue(buildDefaultResponse().setBody("[]"));
        httpConnector.getDownloads(PackageType.ADDON, testTargetPlatform);
        request = mockServer.takeRequest();
        assertThat(request.getHeader("Accept-Encoding")).contains("gzip");

        String pkgName = "test1";
        String pkgVersion = "1.0.0";
        String pkgId = pkgName + "-" + pkgVersion;
        mockServer.enqueue(buildDefaultResponse().setBody(
                "{\"name\" : \"" + pkgName + "\", \"version\" : \"" + pkgVersion + "\"}"));
        httpConnector.getDownload(pkgId);
        request = mockServer.takeRequest();
        assertThat(request.getHeader("Accept-Encoding")).contains("gzip");

    }

    private static void assertThatPathIsCalled(MockWebServer mockServer, String path) throws InterruptedException {
        RecordedRequest request1 = mockServer.takeRequest();
        assertEquals("/" + path, request1.getPath());
    }

    private MockResponse buildDefaultResponse() {
        MockResponse response = new MockResponse()//
                                                  .setResponseCode(200)
                                                  .setBody("{}");
        return response;
    }

    class TestAppender extends AppenderSkeleton {
        private final List<LoggingEvent> log = new ArrayList<LoggingEvent>();

        @Override
        public boolean requiresLayout() {
            return false;
        }

        @Override
        protected void append(final LoggingEvent loggingEvent) {
            log.add(loggingEvent);
        }

        @Override
        public void close() {
        }

        public List<LoggingEvent> getLog() {
            return new ArrayList<LoggingEvent>(log);
        }
    }

}
