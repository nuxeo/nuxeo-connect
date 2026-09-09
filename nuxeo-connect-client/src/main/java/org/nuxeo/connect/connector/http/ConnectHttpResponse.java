/*
 * (C) Copyright 2006-2026 Nuxeo (http://nuxeo.com/) and others.
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
package org.nuxeo.connect.connector.http;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.nuxeo.connect.connector.ConnectServerError;
import org.nuxeo.connect.connector.ConnectServerResponse;

/**
 * Real HTTP base implementation of the {@link ConnectServerResponse}.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class ConnectHttpResponse implements ConnectServerResponse {
    protected static final Log log = LogFactory.getLog(ConnectHttpResponse.class);

    protected CloseableHttpClient httpClient;

    protected CloseableHttpResponse httpResponse;

    public ConnectHttpResponse(CloseableHttpClient httpClient, CloseableHttpResponse httpResponse) {
        this.httpClient = httpClient;
        this.httpResponse = httpResponse;
    }

    @Override
    public InputStream getInputStream() throws ConnectServerError {
        try {
            return httpResponse.getEntity().getContent();
        } catch (IOException e) {
            throw new ConnectServerError("Unable to get Stream", e);
        }
    }

    @Override
    public String getString() throws ConnectServerError {
        try {
            HttpEntity entity = httpResponse.getEntity();
            return entity == null ? null : EntityUtils.toString(entity);
        } catch (IOException | ParseException e) {
            throw new ConnectServerError("Unable to read body", e);
        }
    }

    @Override
    public void release() throws ConnectServerError {
        try {
            httpResponse.close();
            httpClient.close();
        } catch (IOException e) {
            throw new ConnectServerError("Unable to close connection resources", e);
        }
    }

}
