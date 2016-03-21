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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.omg.CORBA.UNKNOWN;

/**
 * @author <a href="mailto:ak@nuxeo.com">Arnaud Kervern</a>
 * @since 1.4.25
 */
public class TrialErrorResponse extends TrialRegistrationResponse {

    protected List<Error> errors = new ArrayList<>();

    @Override
    protected void readValue(Object value) throws JSONException {
        JSONArray array = (JSONArray) value;
        for (int i = 0; i < array.length(); i++) {
            JSONObject ob = (JSONObject) array.get(i);
            errors.add(new Error(ob));
        }
    }

    public List<Error> getErrors() {
        return errors;
    }

    public static class Error {
        private final String message;

        private final String field;

        Error(JSONObject obj) throws JSONException {
            this.message = obj.getString("message");
            this.field = obj.getString("field");
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
