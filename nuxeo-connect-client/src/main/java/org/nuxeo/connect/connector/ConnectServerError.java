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

package org.nuxeo.connect.connector;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Exception class for security errors returned by Nuxeo Connect Server.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class ConnectServerError extends Exception {

    private static final long serialVersionUID = 1L;

    public ConnectServerError(String message) {
        super(message);
    }

    public ConnectServerError(String message, Throwable e) {
        super(message, e);
    }

    public String toJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put("errorClass", this.getClass().getSimpleName());
            json.put("message", this.getMessage());
            if (this.getCause()!=null) {
                json.put("cause", this.getCause().getMessage());
            }
        }
        catch (JSONException e) {
            // NOP
        }
        return json.toString();
    }
}
