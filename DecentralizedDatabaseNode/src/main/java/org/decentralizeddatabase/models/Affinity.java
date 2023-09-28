package org.decentralizeddatabase.models;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class Affinity {
    private Map<String, Set<Integer>> ids;

    public Affinity() {
        ids = new ConcurrentHashMap<>();
    }
}
