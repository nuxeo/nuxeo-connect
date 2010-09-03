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

package org.nuxeo.connect.data;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * DTO representing a (client) Project.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class ConnectProject extends AbstractJSONSerializableData {

    protected String name;

    protected String uuid;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


    public static ConnectProject loadFromJSON(JSONObject ob) throws JSONException {

        ConnectProject prj = new ConnectProject();

        if (ob.has("errorMessage")) {
            prj.errorMessage = ob.getString("errorMessage");
        }

        prj.name = ob.getString("name");
        prj.uuid = ob.getString("uuid");

        return prj;
    }

    public static ConnectProject loadFromJSON(String json) throws JSONException {
        JSONObject ob = new JSONObject(json);
        return loadFromJSON(ob);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (isError()) {
            sb.append("Error : ");
            sb.append(errorMessage);
            sb.append("\n");
        }

        sb.append(name);
        sb.append("\n");
        sb.append(uuid);
        sb.append("\n");

        return sb.toString();
    }

}
