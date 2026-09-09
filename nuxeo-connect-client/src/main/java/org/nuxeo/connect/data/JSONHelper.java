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

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Helper for JSON parsing and node construction.
 *
 * @since 2.0
 */
public final class JSONHelper {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder(
            JsonFactory.builder()
                       .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
                       .enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)
                       .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
                       .enable(JsonReadFeature.ALLOW_MISSING_VALUES)
                       .build())
                                                                // drop null-valued fields/entries to preserve the
                                                                // pre-Jackson org.json.JSONObject semantics
                                                                .defaultPropertyInclusion(
                                                                        JsonInclude.Value.ALL_NON_NULL)
                                                                .build();

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

    public static JsonNode readTree(String data) throws ConnectJSONException {
        try {
            return OBJECT_MAPPER.readTree(sanitize(data));
        } catch (JsonProcessingException e) {
            throw new ConnectJSONException(e);
        }
    }

    public static ObjectNode readObject(String data) throws ConnectJSONException {
        return toObjectNode(readTree(data));
    }

    public static ArrayNode readArray(String data) throws ConnectJSONException {
        return toArrayNode(readTree(data));
    }

    public static JsonNode getNode(ObjectNode node, String fieldName) throws ConnectJSONException {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            throw new ConnectJSONException("Missing JSON field: " + fieldName);
        }
        return value;
    }

    public static String getString(ObjectNode node, String fieldName) throws ConnectJSONException {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            throw new ConnectJSONException("Missing JSON field: " + fieldName);
        }
        return value.asText();
    }

    public static Object getValue(ObjectNode node, String fieldName, Class<?> expectedType) {
        var nodeValue = getNode(node, fieldName);
        if (ObjectNode.class.isAssignableFrom(expectedType)) {
            return JSONHelper.toObjectNode(nodeValue);
        }
        if (ArrayNode.class.isAssignableFrom(expectedType)) {
            return JSONHelper.toArrayNode(nodeValue);
        }
        if (String.class.equals(expectedType)) {
            return nodeValue.asText();
        }
        if (int.class.equals(expectedType) || Integer.class.equals(expectedType)) {
            return nodeValue.asInt();
        }
        if (long.class.equals(expectedType) || Long.class.equals(expectedType)) {
            return nodeValue.asLong();
        }
        if (boolean.class.equals(expectedType) || Boolean.class.equals(expectedType)) {
            return nodeValue.asBoolean();
        }
        if (String[].class.equals(expectedType) && nodeValue.isArray()) {
            ArrayNode array = (ArrayNode) nodeValue;
            String[] values = new String[array.size()];
            for (int i = 0; i < array.size(); i++) {
                values[i] = array.get(i).asText();
            }
            return values;
        }
        return nodeValue;
    }

    public static ObjectNode toObjectNode(JsonNode node) throws ConnectJSONException {
        if (node != null && node.isObject()) {
            return (ObjectNode) node;
        }
        throw new ConnectJSONException("Expected JSON object");
    }

    public static ArrayNode toArrayNode(JsonNode node) throws ConnectJSONException {
        if (node != null && node.isArray()) {
            return (ArrayNode) node;
        }
        throw new ConnectJSONException("Expected JSON array");
    }

    protected static String sanitize(String data) {
        String sanitized = data.trim();
        while (sanitized.endsWith(",")) {
            sanitized = sanitized.substring(0, sanitized.length() - 1).trim();
        }
        return sanitized;
    }
}
