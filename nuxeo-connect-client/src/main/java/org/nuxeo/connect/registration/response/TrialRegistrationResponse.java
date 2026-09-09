/*
 * (C) Copyright 2016-2026 Nuxeo (http://nuxeo.com/) and others.
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

import org.nuxeo.connect.data.JSONHelper;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * @author <a href="mailto:ak@nuxeo.com">Arnaud Kervern</a>
 * @since 1.4.25
 */
public abstract class TrialRegistrationResponse {

    protected ObjectNode json;

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
            var obj = JSONHelper.readObject(body);
            var oType = JSONHelper.getString(obj, "type");
            var o = switch (oType) {
                case "error" -> new TrialErrorResponse();
                case "message" -> new TrialSuccessResponse();
                default -> throw new IOException("Unknown type: " + oType);
            };

            o.readJSON(obj);
            return o;
        } catch (JacksonException | IllegalArgumentException e) {
            throw new IOException(e);
        }
    }

    protected void readJSON(ObjectNode obj) throws IOException {
        this.json = obj;
        this.type = JSONHelper.getString(obj, "type");
        this.message = JSONHelper.getString(obj, "message");
        if (obj.has("value")) {
            readValue(obj.get("value"));
        }
    }

    protected abstract void readValue(JsonNode value) throws IOException;

    public boolean isError() {
        return "error".equals(type);
    }
}
