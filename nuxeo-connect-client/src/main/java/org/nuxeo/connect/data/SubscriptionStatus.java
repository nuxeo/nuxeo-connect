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

/**
 * DTO to transfer Subscription related information.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class SubscriptionStatus extends AbstractJSONSerializableData {

    protected String contractStatus;

    protected String endDate;

    protected String message;

    protected String description;

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

    public static SubscriptionStatus loadFromJSON(JSONObject ob) throws JSONException {

        SubscriptionStatus status = new SubscriptionStatus();

        status.contractStatus = ob.getString("contractStatus");

        if (ob.has("description")) {
            status.description = ob.getString("description");
        }
        if (ob.has("message")) {
            status.message = ob.getString("message");
        }
        if (ob.has("endDate")) {
            status.endDate = ob.getString("endDate");
        }

        if (ob.has("errorMessage")) {
            status.errorMessage = ob.getString("errorMessage");
        }

        if (ob.has("instanceType")) {
            status.instanceType = NuxeoClientInstanceType.fromString(ob.getString("instanceType"));
        }

        return status;
    }

    public static SubscriptionStatus loadFromJSON(String json) throws JSONException {
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

        sb.append(contractStatus);
        sb.append("\n");
        sb.append(endDate);
        sb.append("\n");
        sb.append(message);
        sb.append("\n");

        return sb.toString();
    }

}
