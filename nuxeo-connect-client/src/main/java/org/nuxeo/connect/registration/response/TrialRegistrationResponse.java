/*
 * (C) Copyright 2016 Nuxeo SA (http://nuxeo.com/) and others.
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
 */

package org.nuxeo.connect.registration.response;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author <a href="mailto:ak@nuxeo.com">Arnaud Kervern</a>
 * @since 1.4.25
 */
public abstract class TrialRegistrationResponse {

    protected JSONObject json;

    protected String type;

    protected String message;

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public static TrialRegistrationResponse read(String body) throws IOException {
        try {
            JSONObject obj = new JSONObject(body);
            TrialRegistrationResponse o;
            String oType = obj.getString("type");
            switch (oType) {
            case "error":
                o = new TrialErrorResponse();
                break;
            case "message":
                o = new TrialSuccessResponse();
                break;
            default:
                throw new IOException("Unknown type: " + oType);
            }

            o.readJSON(obj);
            return o;
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    protected void readJSON(JSONObject obj) throws JSONException {
        this.json = obj;
        this.type = obj.getString("type");
        this.message = obj.getString("message");
        if (obj.has("value")) {
            readValue(obj.get("value"));
        }
    }

    protected abstract void readValue(Object value) throws JSONException;

    public boolean isError() {
        return "error".equals(type);
    }
}
