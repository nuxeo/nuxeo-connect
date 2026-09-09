/*
 * (C) Copyright 2026 Nuxeo (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Nuxeo
 */

package org.nuxeo.connect.data;

import java.io.IOException;
import java.util.Map;

import tools.jackson.core.JacksonException;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

public final class JSONHelper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(
            JsonFactory.builder()
                       .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
                       .enable(JsonReadFeature.ALLOW_UNQUOTED_PROPERTY_NAMES)
                       .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
                       .enable(JsonReadFeature.ALLOW_MISSING_VALUES)
                       .build());

    private JSONHelper() {
    }

    public static ObjectNode objectNode() {
        return OBJECT_MAPPER.createObjectNode();
    }

    public static ArrayNode arrayNode() {
        return OBJECT_MAPPER.createArrayNode();
    }

    public static ObjectNode asObjectNode(Object data) {
        return toObjectNode(OBJECT_MAPPER.valueToTree(data));
    }

    public static ObjectNode asObjectNode(Map<String, Object> data) {
        return OBJECT_MAPPER.valueToTree(data);
    }

    public static JsonNode readTree(String data) throws JacksonException {
        return OBJECT_MAPPER.readTree(sanitize(data));
    }

    public static ObjectNode readObject(String data) throws JacksonException {
        return toObjectNode(readTree(data));
    }

    public static ArrayNode readArray(String data) throws JacksonException {
        return toArrayNode(readTree(data));
    }

    public static String getString(ObjectNode node, String fieldName) throws IOException {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            throw new IOException("Missing JSON field: " + fieldName);
        }
        return value.asText();
    }

    public static ObjectNode toObjectNode(JsonNode node) {
        if (node != null && node.isObject()) {
            return node.asObject();
        }
        throw new IllegalArgumentException("Expected JSON object");
    }

    public static ArrayNode toArrayNode(JsonNode node) {
        if (node != null && node.isArray()) {
            return node.asArray();
        }
        throw new IllegalArgumentException("Expected JSON array");
    }

    protected static String sanitize(String data) {
        String sanitized = data.trim();
        while (sanitized.endsWith(",")) {
            sanitized = sanitized.substring(0, sanitized.length() - 1).trim();
        }
        return sanitized;
    }
}
