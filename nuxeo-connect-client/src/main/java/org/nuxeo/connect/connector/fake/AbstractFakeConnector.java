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

package org.nuxeo.connect.connector.fake;

import java.util.Map;

import org.nuxeo.connect.connector.AbstractConnectConnector;
import org.nuxeo.connect.connector.ConnectConnector;
import org.nuxeo.connect.connector.ConnectServerError;
import org.nuxeo.connect.connector.ConnectServerResponse;

/**
 * Fake abstract implementation of the {@link ConnectConnector} interface for testing purpose.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public abstract class AbstractFakeConnector extends AbstractConnectConnector {

    protected abstract String getJSONDataForStatus();

    protected abstract String getJSONDataForDownloads(String type);

    @Override
    protected ConnectServerResponse execServerCall(String url,
            Map<String, String> headers) throws ConnectServerError {

        String data = null;

        if (url.endsWith("/" + GET_STATUS_SUFFIX)) {
            data = getJSONDataForStatus();
        } else if (url.contains("/" + GET_DOWNLOADS_SUFFIX + "/")) {
            String type = url.split(GET_DOWNLOADS_SUFFIX + "\\/")[1];
            data = getJSONDataForDownloads(type);
        }

        return new ConnectFakeResponse(data);
    }

    @Override
    protected ConnectServerResponse execServerPost(String url, Map<String, String> headers) throws ConnectServerError {
        throw new UnsupportedOperationException();
    }

}
