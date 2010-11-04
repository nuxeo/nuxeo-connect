package org.nuxeo.connect.data;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.nuxeo.connect.data.marshaling.JSONExportMethod;
import org.nuxeo.connect.data.marshaling.JSONExportableField;

public class IntrospectionHelper {

    protected static Map<String, Map<String,Object>> readDataStructures = new HashMap<String, Map<String,Object>>();

    // JAVA 2 JSON

    public static Map<String, Object> getDataToSerialize(AbstractJSONSerializableData targetInstance) {

        Map<String, Object> dataStructure = readDataStructures.get(targetInstance.getClass().getName());
        if (dataStructure==null) {
            dataStructure = new HashMap<String, Object>();
            fetchDataStructureToSerialize(dataStructure, targetInstance.getClass());
            readDataStructures.put(targetInstance.getClass().getName(), dataStructure);
        }

        Map<String, Object> data = new HashMap<String, Object>();

        for (String key : dataStructure.keySet()) {
            Object fieldOrMethod = dataStructure.get(key);
            if (fieldOrMethod instanceof Method) {
                Method method = (Method) fieldOrMethod;
                try {
                    data.put(key, method.invoke(targetInstance, (Object[])null));
                }
                catch (Exception e) {
                    // NOP
                }
            } else if (fieldOrMethod instanceof Field) {
                Field field = (Field) fieldOrMethod;
                try {
                    data.put(key, field.get(targetInstance));
                }
                catch (Exception e) {
                    // NOP
                }
            }
        }
        return data;
    }

    protected static void fetchDataStructureToSerialize(Map<String, Object> data, Class<?> klass) {

        Class<?> parentKlass = klass.getSuperclass();
        if (parentKlass!=null) {
            fetchDataStructureToSerialize(data, parentKlass);
        }

        for (Field field : klass.getDeclaredFields()) {
            if (field.getAnnotation(JSONExportableField.class)!=null) {
                try {
                    data.put(field.getName(), field);
                } catch (Exception e) {
                    // NOP
                }
            }
        }

        for (Method method : klass.getDeclaredMethods()) {
            if (method.getAnnotation(JSONExportMethod.class)!=null) {
                try {
                    data.put(method.getAnnotation(JSONExportMethod.class).name(), method);
                } catch (Exception e) {
                    // NOP
                }
            }
        }
    }

    // JSON 2 JAVA

    protected static Map<String, Map<String,Object>> writeDataStructures = new HashMap<String, Map<String,Object>>();



}

