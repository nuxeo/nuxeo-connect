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
import org.nuxeo.connect.data.marshaling.JSONExportableField;

/**
 * DTO representing a (client) Project.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class ConnectProject extends AbstractJSONSerializableData {

    @JSONExportableField
    protected String name;

    @JSONExportableField
    protected String symbolicName;

    @JSONExportableField
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

    public String getSymbolicName() {
        return symbolicName;
    }

    public void setSymbolicName(String symbolicName) {
        this.symbolicName = symbolicName;
    }

    @Deprecated
    public static ConnectProject loadFromJSON(JSONObject ob) throws JSONException {
        return ConnectProject.loadFromJSON(ConnectProject.class, ob);
    }

    @Deprecated
    public static ConnectProject loadFromJSON(String json) throws JSONException {
        return ConnectProject.loadFromJSON(ConnectProject.class, json);
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
        sb.append(symbolicName);
        sb.append("\n");
        sb.append(uuid);
        sb.append("\n");

        return sb.toString();
    }

}
