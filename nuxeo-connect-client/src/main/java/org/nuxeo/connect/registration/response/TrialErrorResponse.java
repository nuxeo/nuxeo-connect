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
import java.util.ArrayList;
import java.util.List;

import org.nuxeo.connect.data.JSONHelper;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * @author <a href="mailto:ak@nuxeo.com">Arnaud Kervern</a>
 * @since 1.4.25
 */
public class TrialErrorResponse extends TrialRegistrationResponse {

    protected List<Error> errors = new ArrayList<>();

    @Override
    protected void readValue(JsonNode value) throws IOException {
        ArrayNode array = JSONHelper.toArrayNode(value);
        for (int i = 0; i < array.size(); i++) {
            ObjectNode ob = JSONHelper.toObjectNode(array.get(i));
            errors.add(new Error(ob));
        }
    }

    public List<Error> getErrors() {
        return errors;
    }

    public static class Error {
        private final String message;

        private final String field;

        Error(ObjectNode obj) throws IOException {
            this.message = JSONHelper.getString(obj, "message");
            this.field = JSONHelper.getString(obj, "field");
        }

        public String getMessage() {
            return message;
        }

        public String getField() {
            return field;
        }
    }

    public static TrialErrorResponse UNKNOWN() {
        TrialErrorResponse res = new TrialErrorResponse();
        res.message = "Unable to register instance for now. Try later.";
        res.type = "error";
        return res;
    }
}
