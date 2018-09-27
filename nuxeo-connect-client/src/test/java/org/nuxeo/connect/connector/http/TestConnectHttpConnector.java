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

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.assertj.core.api.Fail;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
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

/**
 * @since 1.5
 */
public class TestConnectHttpConnector {

    private Server server;

    private static final String BASE_RESOURCE = "jetty-test";

    private static final String HOST = "localhost";

    private static final int PORT = 17488;

    private final String testTargetPlatform = "server-10.3";

    private ConnectHttpConnector httpConnector;

    public class CustomTestRequestHandler extends AbstractHandler {

        private String expectedTargetSuffix = "";

        private int expectedResponseStatus = HttpServletResponse.SC_OK;

        private String expectedJSONResponse = "{}";

        private boolean expectGzipHeader = true;

        public void setExpectGzipHeader(boolean expectGzipHeader) {
            this.expectGzipHeader = expectGzipHeader;
        }

        public void setExpectedTargetSuffix(String expectedTargetSuffix) {
            this.expectedTargetSuffix = expectedTargetSuffix;
        }

        public void setExpectedResponseStatus(int expectedResponseStatus) {
            this.expectedResponseStatus = expectedResponseStatus;
        }

        public void setExpectedJSONResponse(String expectedJSONResponse) {
            this.expectedJSONResponse = expectedJSONResponse;
        }

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                throws IOException, ServletException {
            assertThat(target.endsWith("/" + expectedTargetSuffix)).as("Target url %s should end with %s", target,
                    expectedTargetSuffix).isTrue();
            if (expectGzipHeader) {
                assertThat(request.getHeader("Accept-Encoding")).contains("gzip");
            }
            response.setStatus(expectedResponseStatus);
            try (ServletOutputStream os = response.getOutputStream()) {
                os.print(expectedJSONResponse);
            }
        }

    }

    @Before
    public void setUp() throws Exception {
        // Configure and start Jetty server
        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setHost(HOST);
        connector.setPort(PORT);
        connector.setIdleTimeout(60 * 1000); // 60 seconds
        server.addConnector(connector);
        // Enable gzip compression
        GzipHandler gzipHandler = new GzipHandler();
        gzipHandler.setMinGzipSize(0);
        gzipHandler.setHandler(new CustomTestRequestHandler());
        server.setHandler(gzipHandler);
        server.start();

        // Configure httpConnector
        httpConnector = new ConnectHttpConnector();
        httpConnector.overrideUrl = "http://" + HOST + ":" + PORT + "/" + BASE_RESOURCE + "/";
    }

    @After
    public void tearDown() throws Exception {
        try {
            httpConnector.flushCache();
            // remove potentially loaded DownloadingPackages
            for (DownloadingPackage downloadingPackage : NuxeoConnectClient.getDownloadManager().listDownloadingPackages()) {
                NuxeoConnectClient.getDownloadManager().removeDownloadingPackage(downloadingPackage.getId());
            }
        } finally {
            server.stop();
            server.destroy();
        }
    }

    @Test
    public void it_should_handle_OK_response_for_connect_status() throws ConnectServerError {
        // GIVEN a server answering with a OK response
        getCustomHandler().setExpectedTargetSuffix(AbstractConnectConnector.GET_STATUS_SUFFIX);

        // WHEN getting connect status
        SubscriptionStatus connectStatus = httpConnector.getConnectStatus();

        // THEN it should not have throw any exception and have created a SubscriptionStatus object
        assertThat(connectStatus).isNotNull();
    }

    @Test
    public void it_should_handle_OK_response_for_get_downloads() throws ConnectServerError {
        // GIVEN a server answering with a OK response
        String typeStr = String.valueOf(PackageType.ADDON);
        getCustomHandler().setExpectedTargetSuffix(AbstractConnectConnector.GET_DOWNLOADS_SUFFIX + "/" + typeStr);
        getCustomHandler().setExpectedJSONResponse("[{\"name\" : \"test1\"}, {\"name\" : \"test2\"}]");

        // WHEN getting downloads
        List<DownloadablePackage> downloads = httpConnector.getDownloads(PackageType.ADDON, testTargetPlatform);

        // THEN it should not have throw any exception and have created a list of DownloadablePackage objects
        assertThat(downloads).isNotNull().hasSize(2);
    }

    @Test
    public void it_should_handle_OK_response_for_get_download() throws ConnectServerError {
        // GIVEN a server answering with a OK response
        String pkgName = "test1";
        String pkgVersion = "1.0.0";
        String pkgId = pkgName + "-" + pkgVersion;
        getCustomHandler().setExpectedTargetSuffix(AbstractConnectConnector.GET_DOWNLOAD_SUFFIX + "/" + pkgId);
        getCustomHandler().setExpectedJSONResponse(
                "{\"name\" : \"" + pkgName + "\", \"version\" : \"" + pkgVersion + "\"}");

        // WHEN getting download
        DownloadingPackage download = httpConnector.getDownload(pkgId);

        // THEN it should not have throw any exception and have created a DownloadingPackage object
        assertThat(download).isNotNull();
    }

    @Test
    public void it_should_handle_NOT_FOUND_response_for_connect_status() throws ConnectServerError {
        // GIVEN a server answering with a NOT_FOUND response
        getCustomHandler().setExpectedTargetSuffix(AbstractConnectConnector.GET_STATUS_SUFFIX);
        getCustomHandler().setExpectedResponseStatus(HttpServletResponse.SC_NOT_FOUND);

        // WHEN getting connect status
        SubscriptionStatus connectStatus = httpConnector.getConnectStatus();

        // THEN it should not have throw any exception and have created a SubscriptionStatus object
        assertThat(connectStatus).isNotNull();
    }

    @Test
    public void it_should_handle_NOT_FOUND_response_for_get_downloads() throws ConnectServerError {
        // GIVEN a server answering with a NOT_FOUND response
        String typeStr = String.valueOf(PackageType.ADDON);
        getCustomHandler().setExpectedTargetSuffix(AbstractConnectConnector.GET_DOWNLOADS_SUFFIX + "/" + typeStr);
        getCustomHandler().setExpectedResponseStatus(HttpServletResponse.SC_NOT_FOUND);
        getCustomHandler().setExpectedJSONResponse("[{\"name\" : \"test1\"}, {\"name\" : \"test2\"}]");

        // WHEN getting downloads
        List<DownloadablePackage> downloads = httpConnector.getDownloads(PackageType.ADDON, testTargetPlatform);

        // THEN it should not have throw any exception and have created a list of DownloadablePackage objects
        assertThat(downloads).isNotNull().hasSize(2);
    }

    @Test
    public void it_should_handle_NOT_FOUND_response_for_get_download() throws ConnectServerError {
        // GIVEN a server answering with a OK response
        String pkgName = "test1";
        String pkgVersion = "1.0.0";
        String pkgId = pkgName + "-" + pkgVersion;
        getCustomHandler().setExpectedTargetSuffix(AbstractConnectConnector.GET_DOWNLOAD_SUFFIX + "/" + pkgId);
        getCustomHandler().setExpectedResponseStatus(HttpServletResponse.SC_NOT_FOUND);
        getCustomHandler().setExpectedJSONResponse(
                "{\"name\" : \"" + pkgName + "\", \"version\" : \"" + pkgVersion + "\"}");

        // WHEN getting download
        DownloadingPackage download = httpConnector.getDownload(pkgId);

        // THEN it should not have throw any exception and have created a DownloadingPackage object
        assertThat(download).isNotNull();
    }

    @Test
    public void it_should_handle_NO_CONTENT_response_for_connect_status() {
        // GIVEN a server answering with a NO_CONTENT response
        getCustomHandler().setExpectedTargetSuffix(AbstractConnectConnector.GET_STATUS_SUFFIX);
        getCustomHandler().setExpectedResponseStatus(HttpServletResponse.SC_NO_CONTENT);

        // WHEN getting connect status
        try {
            httpConnector.getConnectStatus();
            Fail.failBecauseExceptionWasNotThrown(ConnectServerError.class);
        } catch (ConnectServerError e) {
            // THEN it should have thrown a ConnectServerError with the correct message
            assertThat(e.getMessage()).isEqualTo("null response from server");
        }
    }

    @Test
    public void it_should_handle_NO_CONTENT_response_for_get_downloads() throws ConnectServerError {
        // GIVEN a server answering with a NOT_FOUND response
        String typeStr = String.valueOf(PackageType.ADDON);
        getCustomHandler().setExpectedTargetSuffix(AbstractConnectConnector.GET_DOWNLOADS_SUFFIX + "/" + typeStr);
        getCustomHandler().setExpectedResponseStatus(HttpServletResponse.SC_NO_CONTENT);

        // WHEN getting downloads
        List<DownloadablePackage> downloads = httpConnector.getDownloads(PackageType.ADDON, testTargetPlatform);

        // THEN it should not have throw any exception and have returned an empty list of DownloadablePackage objects
        assertThat(downloads).isNotNull().hasSize(0);
    }

    @Test
    public void it_should_handle_NO_CONTENT_response_for_get_download() throws ConnectServerError {
        // GIVEN a server answering with a OK response
        String pkgName = "test1";
        String pkgVersion = "1.0.0";
        String pkgId = pkgName + "-" + pkgVersion;
        getCustomHandler().setExpectedTargetSuffix(AbstractConnectConnector.GET_DOWNLOAD_SUFFIX + "/" + pkgId);
        getCustomHandler().setExpectedResponseStatus(HttpServletResponse.SC_NO_CONTENT);

        // WHEN getting download
        DownloadingPackage download = httpConnector.getDownload(pkgId);

        // THEN it should not have throw any exception and have returned null
        assertThat(download).isNull();
    }

    @Test
    public void it_should_handle_UNAUTHORIZED_response() {
        // GIVEN a server answering with a UNAUTHORIZED response
        getCustomHandler().setExpectedResponseStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // WHEN getting connect status
        getCustomHandler().setExpectedTargetSuffix(AbstractConnectConnector.GET_STATUS_SUFFIX);
        try {
            httpConnector.getConnectStatus();
            Fail.failBecauseExceptionWasNotThrown(ConnectServerError.class);
        } catch (ConnectServerError e) {
            // THEN it should have thrown a ConnectServerError with the correct message
            assertThat(e.getMessage()).isEqualTo("Connect server refused authentication (returned 401)");
        }

        // AND

        // WHEN getting downloads
        String typeStr = String.valueOf(PackageType.ADDON);
        getCustomHandler().setExpectedTargetSuffix(AbstractConnectConnector.GET_DOWNLOADS_SUFFIX + "/" + typeStr);
        try {
            httpConnector.getDownloads(PackageType.ADDON, testTargetPlatform);
            Fail.failBecauseExceptionWasNotThrown(ConnectServerError.class);
        } catch (ConnectServerError e) {
            // THEN it should have thrown a ConnectServerError with the correct message
            assertThat(e.getMessage()).isEqualTo("Connect server refused authentication (returned 401)");
        }

        // AND

        // WHEN getting download
        String pkgName = "test1";
        String pkgVersion = "1.0.0";
        String pkgId = pkgName + "-" + pkgVersion;
        getCustomHandler().setExpectedTargetSuffix(AbstractConnectConnector.GET_DOWNLOAD_SUFFIX + "/" + pkgId);
        try {
            httpConnector.getDownload(pkgId);
            Fail.failBecauseExceptionWasNotThrown(ConnectServerError.class);
        } catch (ConnectServerError e) {
            // THEN it should have thrown a ConnectServerError with the correct message
            assertThat(e.getMessage()).isEqualTo("Connect server refused authentication (returned 401)");
        }
    }

    @Test
    public void it_should_handle_GATEWAY_TIMEOUT_response() {
        // GIVEN a server answering with a GATEWAY_TIMEOUT response
        getCustomHandler().setExpectedResponseStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);

        // WHEN getting connect status
        getCustomHandler().setExpectedTargetSuffix(AbstractConnectConnector.GET_STATUS_SUFFIX);
        try {
            httpConnector.getConnectStatus();
            Fail.failBecauseExceptionWasNotThrown(ConnectServerError.class);
        } catch (ConnectServerError e) {
            // THEN it should have thrown a ConnectServerError with the correct message
            assertThat(e.getMessage()).isEqualTo("Timeout " + HttpServletResponse.SC_GATEWAY_TIMEOUT);
        }

        // AND

        // WHEN getting downloads
        String typeStr = String.valueOf(PackageType.ADDON);
        getCustomHandler().setExpectedTargetSuffix(AbstractConnectConnector.GET_DOWNLOADS_SUFFIX + "/" + typeStr);
        try {
            httpConnector.getDownloads(PackageType.ADDON, testTargetPlatform);
            Fail.failBecauseExceptionWasNotThrown(ConnectServerError.class);
        } catch (ConnectServerError e) {
            // THEN it should have thrown a ConnectServerError with the correct message
            assertThat(e.getMessage()).isEqualTo("Timeout " + HttpServletResponse.SC_GATEWAY_TIMEOUT);
        }

        // AND

        // WHEN getting download
        String pkgName = "test1";
        String pkgVersion = "1.0.0";
        String pkgId = pkgName + "-" + pkgVersion;
        getCustomHandler().setExpectedTargetSuffix(AbstractConnectConnector.GET_DOWNLOAD_SUFFIX + "/" + pkgId);
        try {
            httpConnector.getDownload(pkgId);
            Fail.failBecauseExceptionWasNotThrown(ConnectServerError.class);
        } catch (ConnectServerError e) {
            // THEN it should have thrown a ConnectServerError with the correct message
            assertThat(e.getMessage()).isEqualTo("Timeout " + HttpServletResponse.SC_GATEWAY_TIMEOUT);
        }
    }

    @Test
    public void it_should_handle_REQUEST_TIMEOUT_response() {
        // GIVEN a server answering with a REQUEST_TIMEOUT response
        getCustomHandler().setExpectedResponseStatus(HttpServletResponse.SC_REQUEST_TIMEOUT);

        // WHEN getting connect status
        getCustomHandler().setExpectedTargetSuffix(AbstractConnectConnector.GET_STATUS_SUFFIX);
        try {
            httpConnector.getConnectStatus();
            Fail.failBecauseExceptionWasNotThrown(ConnectServerError.class);
        } catch (ConnectServerError e) {
            // THEN it should have thrown a ConnectServerError with the correct message
            assertThat(e.getMessage()).isEqualTo("Timeout " + HttpServletResponse.SC_REQUEST_TIMEOUT);
        }

        // AND

        // WHEN getting downloads
        String typeStr = String.valueOf(PackageType.ADDON);
        getCustomHandler().setExpectedTargetSuffix(AbstractConnectConnector.GET_DOWNLOADS_SUFFIX + "/" + typeStr);
        try {
            httpConnector.getDownloads(PackageType.ADDON, testTargetPlatform);
            Fail.failBecauseExceptionWasNotThrown(ConnectServerError.class);
        } catch (ConnectServerError e) {
            // THEN it should have thrown a ConnectServerError with the correct message
            assertThat(e.getMessage()).isEqualTo("Timeout " + HttpServletResponse.SC_REQUEST_TIMEOUT);
        }

        // AND

        // WHEN getting download
        String pkgName = "test1";
        String pkgVersion = "1.0.0";
        String pkgId = pkgName + "-" + pkgVersion;
        getCustomHandler().setExpectedTargetSuffix(AbstractConnectConnector.GET_DOWNLOAD_SUFFIX + "/" + pkgId);
        try {
            httpConnector.getDownload(pkgId);
            Fail.failBecauseExceptionWasNotThrown(ConnectServerError.class);
        } catch (ConnectServerError e) {
            // THEN it should have thrown a ConnectServerError with the correct message
            assertThat(e.getMessage()).isEqualTo("Timeout " + HttpServletResponse.SC_REQUEST_TIMEOUT);
        }
    }

    @Test
    public void it_should_handle_INTERNAL_SERVER_ERROR_response() throws ConnectServerError {
        // GIVEN a server answering with an INTERNAL_SERVER_ERROR response and a json formatted ConnectSecurityError
        // error
        getCustomHandler().setExpectedResponseStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        getCustomHandler().setExpectedJSONResponse(
                "{\"errorClass\" : \"ConnectSecurityError\", \"message\" : \"server message\"}");

        // WHEN getting connect status
        getCustomHandler().setExpectedTargetSuffix(AbstractConnectConnector.GET_STATUS_SUFFIX);
        try {
            httpConnector.getConnectStatus();
            Fail.failBecauseExceptionWasNotThrown(ConnectSecurityError.class);
        } catch (ConnectSecurityError e) {
            // THEN it should have thrown a ConnectSecurityError with the correct message
            assertThat(e.getMessage()).isEqualTo("server message");
        }

        // GIVEN a server answering with an INTERNAL_SERVER_ERROR response and a json formatted
        // ConnectClientVersionMismatchError error
        getCustomHandler().setExpectedJSONResponse(
                "{\"errorClass\" : \"ConnectClientVersionMismatchError\", \"message\" : \"server message\"}");

        // WHEN getting downloads
        String typeStr = String.valueOf(PackageType.ADDON);
        getCustomHandler().setExpectedTargetSuffix(AbstractConnectConnector.GET_DOWNLOADS_SUFFIX + "/" + typeStr);
        try {
            httpConnector.getDownloads(PackageType.ADDON, testTargetPlatform);
            Fail.failBecauseExceptionWasNotThrown(ConnectClientVersionMismatchError.class);
        } catch (ConnectClientVersionMismatchError e) {
            // THEN it should have thrown a ConnectClientVersionMismatchError with the correct message
            assertThat(e.getMessage()).isEqualTo("server message");
        }

        // GIVEN a server answering with an INTERNAL_SERVER_ERROR response and a json formatted unknown error
        getCustomHandler().setExpectedJSONResponse(
                "{\"errorClass\" : \"UnknownError\", \"message\" : \"server message\"}");

        // WHEN getting download
        String pkgName = "test1";
        String pkgVersion = "1.0.0";
        String pkgId = pkgName + "-" + pkgVersion;
        getCustomHandler().setExpectedTargetSuffix(AbstractConnectConnector.GET_DOWNLOAD_SUFFIX + "/" + pkgId);
        try {
            httpConnector.getDownload(pkgId);
            Fail.failBecauseExceptionWasNotThrown(ConnectServerError.class);
        } catch (ConnectServerError e) {
            // THEN it should have thrown a ConnectServerError with the correct message
            assertThat(e.getMessage()).isEqualTo("server message");
        }

        // GIVEN a server answering with an INTERNAL_SERVER_ERROR response and a malformed json content
        getCustomHandler().setExpectedJSONResponse("{malformed JSON");

        // WHEN getting connect status
        getCustomHandler().setExpectedTargetSuffix(AbstractConnectConnector.GET_STATUS_SUFFIX);
        try {
            httpConnector.getConnectStatus();
            Fail.failBecauseExceptionWasNotThrown(ConnectServerError.class);
        } catch (ConnectServerError e) {
            // THEN it should have thrown a ConnectServerError with the correct message
            assertThat(e.getMessage()).isEqualTo(
                    "Server returned a code " + HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private CustomTestRequestHandler getCustomHandler() {
        return (CustomTestRequestHandler) ((HandlerWrapper) server.getHandler()).getHandler();
    }

}
