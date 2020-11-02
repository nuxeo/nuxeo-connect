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

package org.nuxeo.connect.downloads;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.nuxeo.connect.HttpClientBuilderHelper;
import org.nuxeo.connect.NuxeoConnectClient;
import org.nuxeo.connect.connector.ConnectServerError;
import org.nuxeo.connect.connector.http.ConnectUrlConfig;
import org.nuxeo.connect.data.DownloadingPackage;
import org.nuxeo.connect.data.PackageDescriptor;
import org.nuxeo.connect.identity.SecurityHeaderGenerator;
import org.nuxeo.connect.update.AlreadyExistsPackageException;
import org.nuxeo.connect.update.PackageException;
import org.nuxeo.connect.update.PackageState;
import org.nuxeo.connect.update.PackageUpdateService;

/**
 * Implementation of the {@link DownloadingPackage} interface. Encapsulate download management : ( implements
 * {@link Runnable} to be used in a {@link ThreadPoolExecutor})
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class LocalDownloadingPackage extends PackageDescriptor implements DownloadingPackage, Runnable {

    /**
     * Timeout until a connection is established
     *
     * @since 1.4.24
     */
    public static final int CONNECTION_TIMEOUT_MS = 30000; // 10s

    /**
     * Timeout for waiting for data
     *
     * @since 1.4.24
     */
    public static final int SO_TIMEOUT_MS = 120000; // 120s

    protected static final Log log = LogFactory.getLog(LocalDownloadingPackage.class);

    protected File file = null;

    private boolean completed = false;

    private boolean serverError = false;

    public LocalDownloadingPackage(PackageDescriptor descriptor) {
        super(descriptor);
        sourceUrl = ConnectUrlConfig.getDownloadBaseUrl() + descriptor.getSourceUrl();
        sourceDigest = descriptor.getSourceDigest();
        sourceSize = descriptor.getSourceSize();
    }

    protected void saveStreamAsFile(InputStream in) throws IOException {
        ConnectDownloadManager cdm = NuxeoConnectClient.getDownloadManager();
        String path = cdm.getDownloadedBundleLocalStorage();
        file = new File(path, getId());
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    @Override
    public int getDownloadProgress() {
        if (file == null || !file.exists()) {
            return 0;
        }
        if (getSourceSize() == 0) {
            return 0;
        }
        return (int) ((file.length() * 100) / getSourceSize());
    }

    public File getFile() {
        return file;
    }

    @Override
    public boolean isDigestOk() {
        return false;
    }

    @Override
    public void run() {
        setPackageState(PackageState.REMOTE);
        HttpClientBuilder httpClientBuilder = HttpClientBuilderHelper.getHttpClientBuilder(SO_TIMEOUT_MS,
                CONNECTION_TIMEOUT_MS, sourceUrl);

        try (CloseableHttpClient httpClient = httpClientBuilder.build()) {
            setPackageState(PackageState.DOWNLOADING);
            HttpGet method = new HttpGet(sourceUrl);
            if (!sourceUrl.contains(ConnectUrlConfig.getBaseUrl() + "test")) { // for testing
                Map<String, String> headers = SecurityHeaderGenerator.getHeaders();
                for (String headerName : headers.keySet()) {
                    method.addHeader(headerName, headers.get(headerName));
                }
            }
            try (CloseableHttpResponse httpResponse = httpClient.execute(method)) {
                int rc = httpResponse.getStatusLine().getStatusCode();
                switch (rc) {
                case HttpStatus.SC_OK:
                    if (sourceSize == 0) {
                        Header clheader = httpResponse.getFirstHeader("content-length");
                        if (clheader != null) {
                            sourceSize = Long.parseLong(clheader.getValue());
                        }
                    }
                    InputStream in = httpResponse.getEntity().getContent();
                    saveStreamAsFile(in);
                    registerDownloadedPackage();
                    setPackageState(PackageState.DOWNLOADED);
                    break;

                case HttpStatus.SC_NOT_FOUND:
                    throw new ConnectServerError(String.format("Package not found (%s).", rc));
                case HttpStatus.SC_FORBIDDEN:
                    throw new ConnectServerError(String.format("Access refused (%s).", rc));
                case HttpStatus.SC_UNAUTHORIZED:
                    throw new ConnectServerError(String.format("Registration required (%s).", rc));
                default:
                    serverError = true;
                    throw new ConnectServerError(String.format("Connect server HTTP response code %s.", rc));
                }
            }
        } catch (IOException e) { // Expected SocketTimeoutException or ConnectTimeoutException
            serverError = true;
            setPackageState(PackageState.REMOTE);
            log.debug(e, e);
            errorMessage = e.getMessage();
        } catch (ConnectServerError e) {
            setPackageState(PackageState.REMOTE);
            log.debug(e, e);
            errorMessage = e.getMessage();
        } finally {
            ConnectDownloadManager cdm = NuxeoConnectClient.getDownloadManager();
            cdm.removeDownloadingPackage(getId());
            completed = true;
        }
    }

    protected void registerDownloadedPackage() {
        PackageUpdateService pus = NuxeoConnectClient.getPackageUpdateService();
        try {
            pus.addPackage(file);
            log.info("Added " + getId());
        } catch (AlreadyExistsPackageException e) {
            log.error(e.getMessage());
        } catch (PackageException e) {
            log.error(e);
        }
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    @Override
    public boolean isServerError() {
        return serverError;
    }

}
