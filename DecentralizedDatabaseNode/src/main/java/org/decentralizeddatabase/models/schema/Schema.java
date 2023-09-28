package org.decentralizeddatabase.models.schema;

import org.decentralizeddatabase.constants.DataType;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class Schema {

    // Here because jackson can't deserialize with the default constructor
    public Schema() {
        this.requiredProperties = new ConcurrentHashMap<>();
    }

    public Schema(Map<String, String> requiredProperties) {
        this.requiredProperties = new ConcurrentHashMap<>();
        for (Map.Entry<String, String> entry : requiredProperties.entrySet()) {
            this.requiredProperties.put(entry.getKey(), DataType.valueOf(entry.getValue()));
        }
    }

    private Map<String, DataType> requiredProperties;
}
