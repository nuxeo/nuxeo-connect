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

package org.nuxeo.connect.connector.http;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
/**
 * Helper class to handle the HTTP Configuration
 *
 * @author tiry
 *
 */
public class ProxyHelper {

    public static void configureProxyIfNeeded(HttpClient httpClient) {
        if (ConnectUrlConfig.useProxy()) {
            // configure http proxy
           httpClient.getHostConfiguration().setProxy(ConnectUrlConfig.getProxyHost(), ConnectUrlConfig.getProxyPort());
           // configure proxy auth in BA
           if (ConnectUrlConfig.getProxyLogin()!=null) {
               Credentials ba = new UsernamePasswordCredentials(ConnectUrlConfig.getProxyLogin(), ConnectUrlConfig.getProxyPassword());
               httpClient.getState().setProxyCredentials(new AuthScope(null, -1, AuthScope.ANY_REALM), ba);
           }
        }
    }
}
