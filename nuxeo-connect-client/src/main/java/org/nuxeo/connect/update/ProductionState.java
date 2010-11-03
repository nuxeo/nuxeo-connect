package org.nuxeo.connect.update;

import java.util.EnumSet;

public enum ProductionState {

    PROTO("proto"), TESTING("testing"), PRODUCTION_READY("production_ready");

    private final String value;

    ProductionState(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getValue();
    }

    public static ProductionState getByValue(String value) {
        ProductionState returnValue = null;
        for (final ProductionState element : EnumSet.allOf(ProductionState.class)) {
            if (element.toString().equals(value)) {
                returnValue = element;
            }
        }
        return returnValue;
    }

}
