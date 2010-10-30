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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.nuxeo.connect.data.marshaling.JSONExportMethod;
import org.nuxeo.connect.data.marshaling.JSONExportableField;
import org.nuxeo.connect.data.marshaling.JSONImportMethod;

/**
 * Base class for Data Transfer Object used for the communication between Nuxeo Connect Client and Server.
 * <p>
 * => mainly helpers for JSON marshaling / unmarshaling
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

    public JSONObject asJSONold() {
        return new JSONObject(this);
    }

    public JSONObject asJSON() {
        return new JSONObject(getDataToSerialize());
    }

    protected Map<String, Object> getDataToSerialize() {
        Map<String, Object> data = new HashMap<String, Object>();

        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.getAnnotation(JSONExportableField.class)!=null) {
                try {
                    data.put(field.getName(), field.get(this));
                } catch (Exception e) {
                    // NOP
                }
            }
        }

        for (Method method : this.getClass().getDeclaredMethods()) {
            if (method.getAnnotation(JSONExportMethod.class)!=null) {
                try {
                    data.put(method.getAnnotation(JSONExportMethod.class).name(), method.invoke(this,(Object[])null));
                } catch (Exception e) {
                    // NOP
                }
            }
        }
        return data;
    }

    protected static Object doLoadFromJSON(JSONObject data, Object instance) throws JSONException {

        List<String> fieldNames = new ArrayList<String>();
        for (Method method : instance.getClass().getDeclaredMethods()) {
            if (method.getAnnotation(JSONImportMethod.class)!=null) {
                try {
                    String name = method.getAnnotation(JSONImportMethod.class).name();
                    fieldNames.add(name);
                    Object value = data.get(name);
                    method.invoke(instance, new Object[]{value});
                } catch (Exception e) {
                    // NOP
                }
            }
        }

        for (Field field : instance.getClass().getDeclaredFields()) {
            if (field.getAnnotation(JSONExportableField.class)!=null && (!fieldNames.contains(field.getName()))) {
                try {
                    field.set(instance, data.get(field.getName()));
                } catch (Exception e) {
                    // NOP
                }
            }
        }
        return instance;
    }

    public static <T> T loadFromJSON(Class<T> targetClass, JSONObject data) throws JSONException {
        try {
            return targetClass.cast(doLoadFromJSON(data, targetClass.newInstance()));
        } catch (Exception e) {
          throw new JSONException(e);
        }
    }

    public static <T> T loadFromJSON(Class<T> targetClass, String dataStr) throws JSONException {
        JSONObject data = new JSONObject(dataStr);
        return loadFromJSON(targetClass, data);
    }
}
