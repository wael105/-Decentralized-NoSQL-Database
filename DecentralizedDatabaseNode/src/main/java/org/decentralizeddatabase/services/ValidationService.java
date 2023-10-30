package org.decentralizeddatabase.services;

import org.decentralizeddatabase.constants.DataType;
import org.decentralizeddatabase.models.Collection;
import org.decentralizeddatabase.models.schema.Schema;

import java.util.List;
import java.util.Map;

public enum ValidationService {

    INSTANCE;

    public boolean isDocumentValid(Collection collection, Map<String, Object> data) {
        Schema schema = collection.getSchema();
        return areRequiredPropertiesPresent(schema, data);
    }

    private boolean areRequiredPropertiesPresent(Schema schema, Map<String, Object> data) {
        for (String key : schema.getRequiredProperties().keySet()) {
            if (!data.containsKey(key) && isDataTypeCorrect(schema.getRequiredProperties().get(key), key, data))
                return false;
        }
        return true;
    }

    private boolean isDataTypeCorrect(DataType dataType, String propertyName, Map<String, Object> data) {
        boolean response = true;

        switch (dataType) {
            case STRING -> {
                if (!(data.get(propertyName) instanceof String)) {
                    response = false;
                }
            }
            case LONG -> {
                if (!(data.get(propertyName) instanceof Integer || data.get(propertyName) instanceof Long)) {
                    response = false;
                }
            }
            case BOOLEAN -> {
                if (!(data.get(propertyName) instanceof Boolean)) {
                    response = false;
                }
            }
            case DOUBLE -> {
                if (!(data.get(propertyName) instanceof Double || data.get(propertyName) instanceof Float || data.get(propertyName) instanceof Integer)) {
                    response = false;
                }
            }
            case OBJECT -> {
                if (!(data.get(propertyName) instanceof Map)) {
                    response = false;
                }
            }
            case ARRAY -> {
                if (!(data.get(propertyName) instanceof List)) {
                    response = false;
                }
            }
        }

        return response;
    }
}
