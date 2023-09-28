package org.decentralizeddatabase.models;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class Index {
    // Map<property, Map<attribute, Set<id>>>
    private Map<String, Map<String, Set<Integer>>> collectionIndexes;

    public Index () {
        collectionIndexes = new ConcurrentHashMap<>();
    }
}
