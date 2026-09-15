/*
 * (C) Copyright 2006-2026 Nuxeo (http://nuxeo.com/) and others.
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
 *     Nuxeo - initial API and implementation
 */
package org.nuxeo.connect.data;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.nuxeo.connect.data.marshaling.JSONExportableField;
import org.nuxeo.connect.data.marshaling.JSONImportMethod;

import tools.jackson.databind.node.ObjectNode;

/**
 * Base class for Data Transfer Object used for the communication between Nuxeo Connect Client and Server.
 * <p>
 * Mainly helpers for JSON marshaling / unmarshaling
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public abstract class AbstractJSONSerializableData {

    @JSONExportableField
    protected String errorMessage;

    public boolean isError() {
        return errorMessage != null;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String serializeAsJSON() {
        return asJSON().toString();
    }

    public ObjectNode asJSONold() {
        return JSONHelper.asObjectNode(this);
    }

    public ObjectNode asJSON() {
        return JSONHelper.asObjectNode(IntrospectionHelper.getDataToSerialize(this));
    }

    protected static Object doLoadFromJSON(ObjectNode data, Class<?> klass, Object instance)
            throws ConnectJSONException {

        if (klass.getSuperclass() != null) {
            instance = doLoadFromJSON(data, klass.getSuperclass(), instance);
        }

        List<String> fieldNames = new ArrayList<>();
        for (Method method : instance.getClass().getDeclaredMethods()) {
            if (method.getAnnotation(JSONImportMethod.class) != null) {
                try {
                    String name = method.getAnnotation(JSONImportMethod.class).name();
                    fieldNames.add(name);
                    Object value = JSONHelper.getValue(data, name, method.getParameterTypes()[0]);
                    method.invoke(instance, new Object[] { value });
                } catch (Exception e) {
                    // NOP
                }
            }
        }

        for (Field field : instance.getClass().getDeclaredFields()) {
            if (field.getAnnotation(JSONExportableField.class) != null && (!fieldNames.contains(field.getName()))) {
                try {
                    field.set(instance, JSONHelper.getValue(data, field.getName(), field.getType()));
                } catch (Exception e) {
                    // NOP
                }
            }
        }
        return instance;
    }

    public static <T> T loadFromJSON(Class<T> targetClass, ObjectNode data) throws ConnectJSONException {
        try {
            return targetClass.cast(
                    doLoadFromJSON(data, targetClass, targetClass.getDeclaredConstructor().newInstance()));
        } catch (Exception e) {
            throw new ConnectJSONException(e);
        }
    }

    public static <T> T loadFromJSON(Class<T> targetClass, String dataStr) throws ConnectJSONException {
        ObjectNode data = JSONHelper.readObject(dataStr);
        return loadFromJSON(targetClass, data);
    }
}
