/*
 * (C) Copyright 2006-2015 Nuxeo SA (http://nuxeo.com/) and others.
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

package org.nuxeo.connect.downloads;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.nuxeo.connect.NuxeoConnectClient;
import org.nuxeo.connect.connector.ConnectServerError;
import org.nuxeo.connect.connector.http.ConnectUrlConfig;
import org.nuxeo.connect.connector.http.ProxyHelper;
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
        HttpClient httpClient = new HttpClient();
        HttpConnectionManagerParams httpParams = httpClient.getHttpConnectionManager().getParams();
        httpParams.setConnectionTimeout(CONNECTION_TIMEOUT_MS);
        httpParams.setSoTimeout(SO_TIMEOUT_MS);
        ProxyHelper.configureProxyIfNeeded(httpClient, sourceUrl);
        HttpMethod method = new GetMethod(sourceUrl);
        method.setFollowRedirects(true);
        try {
            setPackageState(PackageState.DOWNLOADING);
            if (!sourceUrl.contains(ConnectUrlConfig.CONNECT_TEST_MODE_BASEURL + "test")) { // for testing
                Map<String, String> headers = SecurityHeaderGenerator.getHeaders();
                for (String headerName : headers.keySet()) {
                    method.addRequestHeader(headerName, headers.get(headerName));
                }
            }
            int rc = 0;
            rc = httpClient.executeMethod(method);
            switch (rc) {
            case HttpStatus.SC_OK:
                if (sourceSize == 0) {
                    Header clheader = method.getResponseHeader("content-length");
                    if (clheader != null) {
                        sourceSize = Long.parseLong(clheader.getValue());
                    }
                }
                InputStream in = method.getResponseBodyAsStream();
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
            method.releaseConnection();
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
