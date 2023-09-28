package org.decentralizeddatabase.services;

import org.decentralizeddatabase.constants.DataType;
import org.decentralizeddatabase.models.Collection;
import org.decentralizeddatabase.models.schema.Schema;

import java.util.List;
import java.util.Map;

public enum ValidationService {

    INSTANCE;

    public boolean validateDocument(Collection collection, Map<String, Object> data) {
        Schema schema = collection.getSchema();
        return checkIfRequiredPropertiesArePresent(schema, data);
    }

    private boolean checkIfRequiredPropertiesArePresent(Schema schema, Map<String, Object> data) {
        for (String key : schema.getRequiredProperties().keySet()) {
            if (!data.containsKey(key) && checkDataType(schema.getRequiredProperties().get(key), key, data)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkDataType(DataType dataType, String propertyName, Map<String, Object> data) {
        System.out.println(data.get(propertyName).getClass());
        boolean response = true;
        switch (dataType){
            case STRING:
                if (!(data.get(propertyName) instanceof String)) {
                    response = false;
                }
                break;
            case LONG:
                if (!(data.get(propertyName) instanceof Integer || data.get(propertyName) instanceof Long)) {
                    response = false;
                }
                break;
            case BOOLEAN:
                if (!(data.get(propertyName) instanceof Boolean)) {
                    response = false;
                }
                break;
            case DOUBLE:
                if (!(data.get(propertyName) instanceof Double || data.get(propertyName) instanceof Float || data.get(propertyName) instanceof Integer)) {
                    response = false;
                }
                break;
            case OBJECT:
                if (!(data.get(propertyName) instanceof Map)) {
                    response = false;
                }

                break;
            case ARRAY:
                if (!(data.get(propertyName) instanceof List)) {
                    response = false;
                }
                break;
        }

        return response;
    }
}
