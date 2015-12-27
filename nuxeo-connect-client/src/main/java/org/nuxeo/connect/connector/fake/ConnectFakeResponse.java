/*
 * (C) Copyright 2006-2009 Nuxeo SA (http://nuxeo.com/) and others.
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.nuxeo.connect.connector.ConnectServerError;
import org.nuxeo.connect.connector.ConnectServerResponse;

/**
 * Fake implementation of the {@link ConnectServerResponse} for tests
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class ConnectFakeResponse implements ConnectServerResponse {

    protected String data;

    public ConnectFakeResponse(String data) {
        this.data = data;
    }

    public InputStream getInputStream() throws ConnectServerError {
        return new ByteArrayInputStream(data.getBytes());
    }

    public String getString() throws ConnectServerError {
        return data;
    }

    public void release() {
    }

}
