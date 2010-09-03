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

    public static final String CONNECT_ROOT_PATH = "connect-gateway/";

    public static final String CONNECT_REGISTRED_ROOT_PATH = "registred/";


    public static String getBaseUrl() {
        if (NuxeoConnectClient.isTestModeSet()) {
            return "http://127.0.0.1:8082/";
        }
        else {
            return NuxeoConnectClient.getProperty(CONNECT_URL_PROPERTY, CONNECT_DEFAULT_BASEURL);
        }
    }

    public static String getDownloadBaseUrl() {
        return getBaseUrl().replace("/site/", "");
    }

    public static String getRegistrationBaseUrl() {
        return getBaseUrl() + CONNECT_ROOT_PATH;
    }

    public static String getRegistredBaseUrl() {
        return getRegistrationBaseUrl() + CONNECT_REGISTRED_ROOT_PATH;
    }


}
