/*
 * (C) Copyright 2026 Nuxeo (http://nuxeo.com/) and others.
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
 *     Nuxeo
 */
package org.nuxeo.connect.data;

/**
 * Exception thrown when a JSON payload cannot be parsed or does not have the expected shape.
 * <p>
 * Acts as a replacement for the former {@code org.json.JSONException}, since Jackson's
 * {@link tools.jackson.core.JacksonException} cannot be instantiated directly (its constructors are protected).
 *
 * @since 2.0
 */
public class ConnectJSONException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConnectJSONException(String message) {
        super(message);
    }

    public ConnectJSONException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectJSONException(Throwable cause) {
        super(cause);
    }
}
