package com.iostate.orca.api;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * For no-code development
 */
public class MapBackedPO extends BasePO {
    private final String modelName;
    private final Map<String, Object> valueMap = new ConcurrentHashMap<>();

    public MapBackedPO(String modelName) {
        this.modelName = modelName;
    }

    public String getModelName() {
        return modelName;
    }

    @Override
    public Object getFieldValue(String name) {
        return valueMap.get(name);
    }

    @Override
    public synchronized void setFieldValue(String name, Object value) {
        Objects.requireNonNull(name, "field name must not be null");
        if (value != null) {
            valueMap.put(name, value);
        } else {
            valueMap.remove(name);
        }
        markUpdatedField(name);
    }
}
