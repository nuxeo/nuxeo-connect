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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.nuxeo.connect.connector.ConnectServerError;
import org.nuxeo.connect.connector.ConnectServerResponse;

/**
 * Real HTTP base implementation of the {@link ConnectServerResponse}.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class ConnectHttpResponse implements ConnectServerResponse {

    protected HttpClient httpClient;
    protected HttpMethod method;

    public ConnectHttpResponse (HttpClient httpClient, HttpMethod method ) {
        this.httpClient=httpClient;
        this.method = method;
    }

    public InputStream getInputStream() throws ConnectServerError {
        try {
            return method.getResponseBodyAsStream();
        } catch (IOException e) {
            throw new ConnectServerError("Unable to get Stream", e);
        }
    }

    public String getString()  throws ConnectServerError  {
        try {
            return method.getResponseBodyAsString();
        }
        catch (IOException e) {
            throw new ConnectServerError("Unable to ready body", e);
        }
    }

    public void release() {
        method.releaseConnection();
    }

}
