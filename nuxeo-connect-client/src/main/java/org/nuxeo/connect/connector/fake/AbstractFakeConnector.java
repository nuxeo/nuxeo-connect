/*
 * (C) Copyright 2006-2016 Nuxeo SA (http://nuxeo.com/) and others.
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
 *
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

    protected abstract String getJSONDataForDownload(String pkgId);

    @Override
    protected ConnectServerResponse execServerCall(String url, Map<String, String> headers) throws ConnectServerError {

        String data = null;

        if (url.endsWith("/" + GET_STATUS_SUFFIX)) {
            data = getJSONDataForStatus();
        } else if (url.contains("/" + GET_DOWNLOADS_SUFFIX + "/")) {
            String type = url.split(GET_DOWNLOADS_SUFFIX + "\\/")[1];
            data = getJSONDataForDownloads(type);
        } else if (url.contains("/" + GET_DOWNLOAD_SUFFIX + "/")) {
            String pkgId = url.split(GET_DOWNLOAD_SUFFIX + "\\/")[1];
            data = getJSONDataForDownload(pkgId);
        }

        return new ConnectFakeResponse(data);
    }

    @Override
    protected ConnectServerResponse execServerPost(String url, Map<String, String> headers) throws ConnectServerError {
        throw new UnsupportedOperationException();
    }

}
