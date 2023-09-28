package org.decentralizeddatabase.models;

import org.decentralizeddatabase.constants.NodeProperties;
import lombok.Getter;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;

public class DatabaseObject {

    private final Map<String, Collection> collections;

    @Getter
    private final String databaseName;

    private final NodeProperties properties;

    @Getter
    private final StampedLock lock;

    public DatabaseObject(String databaseName) {
        this.properties = NodeProperties.INSTANCE;
        this.databaseName = databaseName;
        collections = new ConcurrentHashMap<>();
        lock = new StampedLock();
    }

    public void addCollection(String collectionName, Collection collection) {
        // this uses putIfAbsent because two threads could try to add the same collection at the same time
        collections.putIfAbsent(collectionName, collection);
    }

    public boolean containsCollection(String collectionName) {
        return collections.containsKey(collectionName);
    }

    public String getCollectionPath(String collectionName) {
        return properties.getDatabaseLocation() + "\\" + databaseName + "\\" + collectionName;
    }

    public Collection getCollection(String collectionName) {
        return collections.get(collectionName);
    }

    public void removeCollection(String collectionName) {
        collections.remove(collectionName);
    }

    public Set<String> getCollections() {
        return collections.keySet();
    }
}
