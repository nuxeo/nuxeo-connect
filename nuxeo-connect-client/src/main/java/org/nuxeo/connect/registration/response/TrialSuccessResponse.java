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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.nuxeo.connect.data.JSONHelper;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * @author <a href="mailto:ak@nuxeo.com">Arnaud Kervern</a>
 * @since 1.4.25
 */
public class TrialSuccessResponse extends TrialRegistrationResponse {

    protected String email;

    protected String company;

    protected Map<String, String> token;

    @Override
    protected void readValue(JsonNode value) throws IOException {
        ObjectNode obj = JSONHelper.toObjectNode(value);
        this.email = JSONHelper.getString(obj, "email");
        this.company = JSONHelper.getString(obj, "company");
        this.token = parseToken(JSONHelper.getString(obj, "wizardToken"));
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
