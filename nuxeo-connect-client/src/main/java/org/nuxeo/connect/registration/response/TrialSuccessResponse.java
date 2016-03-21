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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author <a href="mailto:ak@nuxeo.com">Arnaud Kervern</a>
 * @since 1.4.25
 */
public class TrialSuccessResponse extends TrialRegistrationResponse {

    protected String email;

    protected String company;

    protected Map<String, String> token;

    @Override
    protected void readValue(Object value) throws JSONException {
        JSONObject obj = (JSONObject) value;
        this.email = obj.getString("email");
        this.company = obj.getString("company");
        this.token = parseToken(obj.getString("wizardToken"));
    }

    protected static Map<String, String> parseToken(String wizToken) {
        Map<String, String> token = new HashMap<>();

        String tokenData = new String(Base64.decodeBase64(wizToken.getBytes()));
        String[] tokenDataLines = tokenData.split("\n");
        for (String line : tokenDataLines) {
            String[] parts = line.split(":");
            if (parts.length > 1) {
                token.put(parts[0], parts[1]);
            }
        }
        return token;
    }

    public String getEmail() {
        return email;
    }

    public String getCompany() {
        return company;
    }

    public Map<String, String> getToken() {
        return token;
    }
}
