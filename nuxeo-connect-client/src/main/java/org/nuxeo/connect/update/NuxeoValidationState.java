package org.nuxeo.connect.update;

import java.util.EnumSet;

public enum NuxeoValidationState {

    NONE("none"), INPROCESS("inprocess"), PRIMARY_VALIDATION("primary_validation"),NUXEO_CERTIFIED("nuxeo_certified");

    private final String value;

    NuxeoValidationState(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getValue();
    }

    public static NuxeoValidationState getByValue(String value) {
        NuxeoValidationState returnValue = null;
        for (final NuxeoValidationState element : EnumSet.allOf(NuxeoValidationState.class)) {
            if (element.toString().equals(value)) {
                returnValue = element;
            }
        }
        return returnValue;
    }

}
