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

package org.nuxeo.connect.connector.http.proxy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract class resolver to find which proxy to use with a configured proxy
 * pac. The proxy pac file is setted with
 * org.nuxeo.connect.connector.http.ConnectUrlConfig#CONNECT_PROXY_PAC_URL
 * system
 * property.
 *
 * @author <a href="mailto:ak@nuxeo.com">Arnaud Kervern</a>
 * @since 1.4.15
 */
public abstract class ProxyPacResolver {

    public static final String NO_PROXY = "DIRECT";

    /**
     * Return proxies returned by the pac script
     *
     * @return an array of pac result or null
     */
    public abstract String[] findPacProxies(String url);

    /**
     * Resolve a proxy host and port for a given url
     *
     * @return an array containing url and port, or if the parsing is not
     *         possible null.
     */
    public String[] findProxy(String url) {
        String[] proxies = findPacProxies(url);
        if (proxies != null && proxies.length > 0
                && !NO_PROXY.equals(proxies[0].trim().toUpperCase())) {
            // Proxy PAC allows to fail over with several proxies; for now,
            // let's assume to have the first one working.
            String[] proxy = parseProxyInfos(proxies[0]);
            if (proxy[0] != null) {
                return proxy;
            }
        }
        return null;
    }

    protected static String getHost(String url) {
        try {
            return new URL(url).getHost();
        } catch (MalformedURLException e) {
            return "";
        }
    }

    /**
     * Parse the proxy PAC configuration returned String to get "host" and
     * "port" in a String array. First is the host, second the port. If there is
     * no port, -1 is set by default.
     */
    protected static String[] parseProxyInfos(String proxy) {
        String[] split = new String[2];
        Pattern p = Pattern.compile("PROXY ([0-9a-zA-Z.]+)(:([0-9]{1,5}))*");
        Matcher matcher = p.matcher(proxy.trim());
        if (matcher.matches()) {
            split[0] = matcher.group(1);
            if (matcher.group(3) != null) {
                split[1] = matcher.group(3);
            } else {
                split[1] = "-1"; // Handled as default port value.
            }
        }
        return split;
    }

}
