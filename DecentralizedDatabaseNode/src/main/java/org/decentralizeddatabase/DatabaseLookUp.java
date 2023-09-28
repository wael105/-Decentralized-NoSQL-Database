package org.decentralizeddatabase;

import org.decentralizeddatabase.models.DatabaseObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum DatabaseLookUp {

    INSTANCE;

    DatabaseLookUp() {
        databaseObjects = new ConcurrentHashMap<>();
    }

    private final Map<String, DatabaseObject> databaseObjects;


    public void addDatabaseObject (DatabaseObject databaseObject) {
        databaseObjects.put(databaseObject.getDatabaseName(), databaseObject);
    }

    public boolean hasDatabaseObject(String databaseName) {
        return databaseObjects.containsKey(databaseName);
    }

    public DatabaseObject getDatabaseObject(String databaseName) {
        return databaseObjects.get(databaseName);
    }

    public void removeDatabaseObject(String databaseName) {
        databaseObjects.remove(databaseName);
    }
}