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
import org.nuxeo.connect.connector.NuxeoClientInstanceType;
import org.nuxeo.connect.data.marshaling.JSONExportableField;
import org.nuxeo.connect.data.marshaling.JSONImportMethod;

/**
 * DTO to transfer Subscription related information.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class SubscriptionStatus extends AbstractJSONSerializableData {

    @JSONExportableField
    protected String contractStatus;

    @JSONExportableField
    protected String endDate;

    @JSONExportableField
    protected String message;

    @JSONExportableField
    protected String description;

    @JSONExportableField
    protected NuxeoClientInstanceType instanceType;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public NuxeoClientInstanceType getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(NuxeoClientInstanceType instanceType) {
        this.instanceType = instanceType;
    }

    @JSONImportMethod(name="instanceType")
    protected void setInstanceType(String instanceType) {
        this.instanceType = NuxeoClientInstanceType.fromString(instanceType);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getContractStatus() {
        return contractStatus;
    }

    public SubscriptionStatusType status() {
        return SubscriptionStatusType.getByValue(contractStatus);
    }

    public void setContractStatus(String contractStatus) {
        this.contractStatus = contractStatus;
    }
    public String getEndDate() {
        return endDate;
    }
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    @Deprecated
    public static SubscriptionStatus loadFromJSON(JSONObject ob) throws JSONException {
        return SubscriptionStatus.loadFromJSON(SubscriptionStatus.class, ob);
    }

    @Deprecated
    public static SubscriptionStatus loadFromJSON(String json) throws JSONException {
        return SubscriptionStatus.loadFromJSON(SubscriptionStatus.class, json);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (isError()) {
            sb.append("Error : ");
            sb.append(errorMessage);
            sb.append("\n");
        }

        sb.append(contractStatus);
        sb.append("\n");
        sb.append(endDate);
        sb.append("\n");
        sb.append(message);
        sb.append("\n");

        return sb.toString();
    }

}
