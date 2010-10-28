/*
 * (C) Copyright 2006-2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 *
 * $Id$
 */

package org.nuxeo.connect.downloads;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.connect.NuxeoConnectClient;
import org.nuxeo.connect.connector.ConnectServerError;
import org.nuxeo.connect.connector.http.ConnectUrlConfig;
import org.nuxeo.connect.data.DownloadingPackage;
import org.nuxeo.connect.data.PackageDescriptor;
import org.nuxeo.connect.identity.SecurityHeaderGenerator;
import org.nuxeo.connect.update.PackageException;
import org.nuxeo.connect.update.PackageState;
import org.nuxeo.connect.update.PackageUpdateService;

/**
 *
 * Implementation of the {@link DownloadingPackage} interface.
 *
 * Encapsulate download management.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class LocalDownloadingPackage extends PackageDescriptor implements
        DownloadingPackage, Runnable {

    protected static final Log log = LogFactory.getLog(LocalDownloadingPackage.class);

    protected boolean completed = false;

    protected File file = null;

    public LocalDownloadingPackage(PackageDescriptor descriptor) {
        super();
        this.classifier = descriptor.getClassifier();
        this.description = descriptor.getDescription();
        this.homePage = descriptor.getHomePage();
        this.name = descriptor.getName();
        this.targetPlatforms = descriptor.getTargetPlatforms();
        this.title = descriptor.getTitle();
        this.type = descriptor.getType();
        this.version = descriptor.getVersion();
        this.sourceUrl = ConnectUrlConfig.getDownloadBaseUrl()
                + descriptor.getSourceUrl();
        this.sourceDigest = descriptor.getSourceDigest();
        this.sourceSize = descriptor.getSourceSize();
    }

    @Override
    public int getState() {
        if (completed) {
            return PackageState.DOWNLOADED;
        } else {
            return PackageState.DOWNLOADING;
        }
    }

    protected void saveStreamAsFile(InputStream in) throws IOException {

        ConnectDownloadManager cdm = NuxeoConnectClient.getDownloadManager();

        String path = cdm.getDownloadedBundleLocalStorage();

        path = path + getId();
        file = new File(path);

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

    public boolean isDigestOk() {
        return false;
    }

    public void run() {

        HttpClient httpClient = new HttpClient();
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(
                10000);
        HttpMethod method = new GetMethod(sourceUrl);
        method.setFollowRedirects(true);

        try {

            if (!sourceUrl.contains("127.0.0.1:8082/test")) { // for testing
                Map<String, String> headers = SecurityHeaderGenerator.getHeaders();

                for (String name : headers.keySet()) {
                    method.addRequestHeader(name, headers.get(name));
                }
            }

            int rc = 0;
            try {
                rc = httpClient.executeMethod(method);
                if (rc == 200) {
                    if (sourceSize==0) {
                        Header clheader = method.getResponseHeader("content-length");
                        if (clheader!=null) {
                            sourceSize = Long.parseLong(clheader.getValue());
                        }
                    }
                    InputStream in = method.getResponseBodyAsStream();
                    saveStreamAsFile(in);

                    completed = true;
                    registerDownloadedPackage();
                    ConnectDownloadManager cdm = NuxeoConnectClient.getDownloadManager();
                    cdm.removeDownloadingPackage(getId());
                }
            } catch (Exception e) {
                throw new ConnectServerError(
                        "Error during communication with the Nuxeo Connect Server",
                        e);
            } finally {
                method.releaseConnection();
            }
        } catch (Exception e) {

        } finally {
            method.releaseConnection();
        }
    }

    protected void registerDownloadedPackage() {

        PackageUpdateService pus = NuxeoConnectClient.getPackageUpdateService();
        if (pus == null) {
            if (!NuxeoConnectClient.isTestModeSet()) {
                log.error("Unable to locate PackageUpdateService ... exiting");
            }
            return;
        }

        try {
            pus.addPackage(file);
        } catch (PackageException e) {
            log.error(
                    "Unable when adding package to PackageUpdateService ... exiting",
                    e);
        }

    }

    public boolean isCompleted() {
        return completed;
    }

}
