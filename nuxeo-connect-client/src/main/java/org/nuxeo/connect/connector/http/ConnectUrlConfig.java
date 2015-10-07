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

import org.nuxeo.connect.NuxeoConnectClient;

/**
 *
 * Helper to manage URL configuration when accessing Nuxceo Connect Services
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class ConnectUrlConfig {

    public static final String CONNECT_DEFAULT_BASEURL = "https://connect.nuxeo.com/nuxeo/site/";

    public static final String CONNECT_URL_PROPERTY = "org.nuxeo.connect.url";

    /**
     * @deprecated Since 1.4.15
     */
    @Deprecated
    public static final String CONNECT_PROXY_HOST_PROPERTY = "org.nuxeo.connect.proxy.host";

    /**
     * @since 1.4.15
     */
    public static final String NUXEO_PROXY_PAC_URL = "nuxeo.http.proxy.pac.url";

    /**
     * @deprecated Since 1.4.15
     */
    @Deprecated
    public static final String CONNECT_PROXY_PORT_PROPERTY = "org.nuxeo.connect.proxy.port";

    /**
     * @deprecated Since 1.4.15
     */
    @Deprecated
    public static final String CONNECT_PROXY_LOGIN_PROPERTY = "org.nuxeo.connect.proxy.login";

    /**
     * @deprecated Since 1.4.15
     */
    @Deprecated
    public static final String CONNECT_PROXY_PASSWORD_PROPERTY = "org.nuxeo.connect.proxy.password";

    /**
     * @deprecated Since 1.4.15
     */
    @Deprecated
    public static final String CONNECT_PROXY_NTLM_HOST = "org.nuxeo.connect.proxy.ntlm.host";

    /**
     * @deprecated Since 1.4.15
     */
    @Deprecated
    public static final String CONNECT_PROXY_NTLM_DOMAIN = "org.nuxeo.connect.proxy.ntlm.domain";

    public static final String NUXEO_PROXY_HOST_PROPERTY = "nuxeo.http.proxy.host";

    public static final String NUXEO_PROXY_PORT_PROPERTY = "nuxeo.http.proxy.port";

    public static final String NUXEO_PROXY_LOGIN_PROPERTY = "nuxeo.http.proxy.login";

    public static final String NUXEO_PROXY_PASSWORD_PROPERTY = "nuxeo.http.proxy.password";

    public static final String NUXEO_PROXY_NTLM_HOST = "nuxeo.http.proxy.ntlm.host";

    public static final String NUXEO_PROXY_NTLM_DOMAIN = "nuxeo.http.proxy.ntlm.domain";

    public static final String CONNECT_ROOT_PATH = "connect-gateway/";

    public static final String CONNECT_REGISTERED_ROOT_PATH = "registred/";

    public static final String CONNECT_UNREGISTERED_ROOT_PATH = "unregistered/";

    public static final String STUDIO_BASEURL = "studio/ide";

    public static final String STUDIO_PROJECT_PARAMETER = "project";

    public static String getBaseUrl() {
        if (NuxeoConnectClient.isTestModeSet()) {
            return "http://127.0.0.1:8082/";
        } else {
            return NuxeoConnectClient.getProperty(CONNECT_URL_PROPERTY,
                    CONNECT_DEFAULT_BASEURL);
        }
    }

    /**
     * @since 1.4.24
     */
    public static String getStudioUrl() {
        return getBaseUrl() + STUDIO_BASEURL;
    }

    /**
     * @since 1.4.24
     */
    public static String getStudioUrl(String projectId) {
        return getStudioUrl() + String.format("?%s=%s", STUDIO_PROJECT_PARAMETER, projectId);
    }

    public static String getDownloadBaseUrl() {
        return getBaseUrl().replace("/site/", "");
    }

    public static String getRegistrationBaseUrl() {
        return getBaseUrl() + CONNECT_ROOT_PATH;
    }

    /**
     * @since 1.4
     */
    public static String getRegistredBaseUrl() {
        return getRegistrationBaseUrl() + CONNECT_REGISTERED_ROOT_PATH;
    }

    /**
     * @since 1.4
     */
    public static String getUnregisteredBaseUrl() {
        return getRegistrationBaseUrl() + CONNECT_UNREGISTERED_ROOT_PATH;
    }

    /**
     * @since 1.4.15
     */
    public static String getProxyPacUrl() {
        return NuxeoConnectClient.getProperty(NUXEO_PROXY_PAC_URL, null);
    }

    // Proxy settings management
    protected static Boolean useProxy = null;

    protected static Boolean isProxyAuthenticated = null;

    protected static Boolean useProxyPac = null;

    public static boolean useProxy() {
        if (useProxy == null) {
            String host = getProxyHost();
            if (host == null || host.isEmpty() || host.startsWith("$")) {
                useProxy = false;
            } else {
                useProxy = true;
            }
        }
        return useProxy || useProxyPac();
    }

    public static boolean useProxyPac() {
        if (useProxyPac == null) {
            String proxyPacUrl = getProxyPacUrl();
            useProxyPac = proxyPacUrl != null && proxyPacUrl.length() > 0;
        }
        return useProxyPac;
    }

    public static boolean isProxyAuthenticated() {
        if (isProxyAuthenticated == null) {
            String login = getProxyLogin();
            if (login == null || login.isEmpty() || login.startsWith("$")) {
                isProxyAuthenticated = false;
            } else {
                isProxyAuthenticated = true;
            }
        }
        return isProxyAuthenticated;
    }

    public static String getProxyHost() {
        return NuxeoConnectClient.getProperty(CONNECT_PROXY_HOST_PROPERTY,
                NuxeoConnectClient.getProperty(NUXEO_PROXY_HOST_PROPERTY, null));
    }

    public static int getProxyPort() {
        String portAsString = NuxeoConnectClient.getProperty(
                CONNECT_PROXY_PORT_PROPERTY,
                NuxeoConnectClient.getProperty(NUXEO_PROXY_PORT_PROPERTY, null));
        if (portAsString == null || portAsString.isEmpty()
                || portAsString.startsWith("$")) {
            return 80;
        }
        try {
            return Integer.parseInt(portAsString);
        } catch (NumberFormatException e) {
            return 80;
        }
    }

    public static String getProxyLogin() {
        return NuxeoConnectClient.getProperty(
                CONNECT_PROXY_LOGIN_PROPERTY,
                NuxeoConnectClient.getProperty(NUXEO_PROXY_LOGIN_PROPERTY, null));
    }

    public static String getProxyPassword() {
        return NuxeoConnectClient.getProperty(CONNECT_PROXY_PASSWORD_PROPERTY,
                NuxeoConnectClient.getProperty(NUXEO_PROXY_PASSWORD_PROPERTY,
                        null));
    }

    public static String getProxyNTLMHost() {
        return NuxeoConnectClient.getProperty(CONNECT_PROXY_NTLM_HOST,
                NuxeoConnectClient.getProperty(NUXEO_PROXY_NTLM_HOST, null));
    }

    public static String getProxyNTLMDomain() {
        return NuxeoConnectClient.getProperty(CONNECT_PROXY_NTLM_DOMAIN,
                NuxeoConnectClient.getProperty(NUXEO_PROXY_NTLM_DOMAIN, null));
    }

    public static boolean isProxyNTLM() {
        String host = getProxyNTLMHost();
        if (host == null || host.isEmpty() || host.startsWith("$")) {
            return false;
        }
        String domain = getProxyNTLMDomain();
        if (domain == null || domain.isEmpty() || domain.startsWith("$")) {
            return false;
        }
        return true;
    }
}
