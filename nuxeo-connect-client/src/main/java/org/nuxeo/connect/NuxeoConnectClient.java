/*
 * (C) Copyright 2010-2012 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 */
package org.nuxeo.connect;

import java.io.InputStream;
import java.util.PropertyResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.connect.connector.ConnectConnector;
import org.nuxeo.connect.connector.service.ConnectGatewayComponent;
import org.nuxeo.connect.downloads.ConnectDownloadManager;
import org.nuxeo.connect.packages.PackageManager;
import org.nuxeo.connect.packages.PackageManagerImpl;
import org.nuxeo.connect.registration.ConnectRegistrationService;
import org.nuxeo.connect.update.PackageUpdateService;

public class NuxeoConnectClient {

    protected static final Log log = LogFactory.getLog(NuxeoConnectClient.class);

    protected static final String PROTOCOL_VERSION = "1.4";

    protected static String buildVersion = null;

    protected static ConnectGatewayComponent connectGatewayComponent = null;

    public static final String PROTOCOL_VERSION_OVERRIDE = "org.nuxeo.connect.protol.version.override";

    protected static PackageManager packageManager = null;

    protected static CallbackHolder cbHolder = new DefaultCallbackHolder();

    public static void setCallBackHolder(CallbackHolder cb) {
        cbHolder = cb;
    }

    protected static synchronized ConnectGatewayComponent getConnectGatewayComponent() {
        if (connectGatewayComponent == null) {
            connectGatewayComponent = new ConnectGatewayComponent();
        }
        return connectGatewayComponent;
    }

    public static ConnectRegistrationService getConnectRegistrationService() {
        return getConnectGatewayComponent();
    }

    public static ConnectConnector getConnectConnector() {
        return getConnectGatewayComponent().getConnector();
    }

    public static ConnectDownloadManager getDownloadManager() {
        return getConnectGatewayComponent().getDownloadManager();
    }

    public static PackageUpdateService getPackageUpdateService() {
        return cbHolder.getUpdateService();
    }

    public static synchronized PackageManager getPackageManager() {
        if (packageManager == null) {
            packageManager = new PackageManagerImpl();
        }
        return packageManager;
    }

    public static void resetPackageManager() {
        packageManager = null;
    }

    public static boolean isTestModeSet() {
        return cbHolder.isTestModeSet();
    }

    public static String getProperty(String key, String defaultValue) {
        return cbHolder.getProperty(key, defaultValue);
    }

    public static String getHomePath() {
        return cbHolder.getHomePath();
    }

    public static String getProtocolVersion() {
        return getProperty(PROTOCOL_VERSION_OVERRIDE, PROTOCOL_VERSION);
    }

    public static String getBuildVersion() {
        if (buildVersion == null) {
            try {
                InputStream pomStream = NuxeoConnectClient.class.getClassLoader().getResourceAsStream(
                        "META-INF/maven/org.nuxeo.connect/nuxeo-connect-client/pom.properties");
                if (pomStream == null) {
                    buildVersion = "Unknown (no pom)";
                } else {
                    PropertyResourceBundle prb = new PropertyResourceBundle(
                            pomStream);
                    buildVersion = prb.getString("version");
                    if (buildVersion == null) {
                        buildVersion = "Unknown (not found)";
                    }
                }
            } catch (Throwable t) {
                log.error("Unable to find Nuxeo Client Build Version", t);
                buildVersion = "Unknown (error)";
            }
        }
        return buildVersion;
    }

    public static String getVersion() {
        return "protocol: " + getProtocolVersion() + " / build:"
                + getBuildVersion();
    }
}
