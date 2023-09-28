package org.decentralizeddatabase.services;

import org.decentralizeddatabase.DatabaseLookUp;
import org.decentralizeddatabase.DatabaseUserState;
import org.decentralizeddatabase.constants.FailedResponse;
import org.decentralizeddatabase.constants.NodeProperties;
import org.decentralizeddatabase.constants.Response;
import org.decentralizeddatabase.constants.SuccessfulResponse;
import org.decentralizeddatabase.models.DatabaseObject;

import java.util.concurrent.locks.StampedLock;

public enum DatabaseService {

    INSTANCE(DiskService.INSTANCE, CollectionService.INSTANCE, NodeProperties.INSTANCE, DatabaseLookUp.INSTANCE);

    private final DiskService diskService;

    private final CollectionService collectionService;

    private final NodeProperties nodeProperties;

    private final DatabaseLookUp databaseLookUp;

    DatabaseService(DiskService diskService, CollectionService collectionService, NodeProperties nodeProperties, DatabaseLookUp databaseLookUp) {
        this.diskService = diskService;
        this.collectionService = collectionService;
        this.nodeProperties = nodeProperties;
        this.databaseLookUp = databaseLookUp;
    }

    public Response createDatabase(String databaseName) {
        synchronized (this) {
            if (doesDatabaseExist(databaseName, databaseLookUp))
                return FailedResponse.DATABASE_ALREADY_EXISTS;

            diskService.createDirectory(nodeProperties.getDatabaseLocation() + "\\" + databaseName);
            // TODO containskey concurrency problem (no exclusive access and visibility guarantee)
            databaseLookUp.addDatabaseObject(new DatabaseObject(databaseName));

            return SuccessfulResponse.DATABASE_CREATED;
        }
    }

    public Response setDatabase(String databaseName, DatabaseUserState databaseUserState) {
        if (!doesDatabaseExist(databaseName, databaseLookUp))
            return FailedResponse.DATABASE_DOES_NOT_EXIST;
        // might load a database that was deleted on disk then set it because someone created it before the hasDatabaseObject check (same for collection)
        DatabaseObject databaseObject = loadDatabase(databaseName, databaseLookUp);

        StampedLock lock = databaseObject.getLock();
        long stamp = lock.tryOptimisticRead();

        databaseUserState.setCurrentDatabaseObject(databaseObject);

        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                if (databaseLookUp.hasDatabaseObject(databaseName))
                    databaseUserState.setCurrentDatabaseObject(databaseObject);
                else
                    databaseUserState.setCurrentDatabaseObject(null);
            } finally {
                lock.unlockRead(stamp);
            }
        }

        databaseUserState.setCurrentCollection(null);

        return SuccessfulResponse.DATABASE_SET;
    }

    // this doesn't create a conflict with create database because the database exists in the memory until it's removed from disk so it's not possible to create a database that already exists
    // if the database is already set it might still be used
    public Response deleteDatabase(String databaseName) {
        if (!doesDatabaseExist(databaseName, databaseLookUp))
            return FailedResponse.DATABASE_DOES_NOT_EXIST;

        DatabaseObject databaseObject = loadDatabase(databaseName, databaseLookUp);
        StampedLock lock = databaseObject.getLock();
        long stamp = lock.writeLock();

        try {
            for (String collectionName : databaseObject.getCollections()) {
                collectionService.deleteCollection(collectionName, databaseObject);
            }
            diskService.deleteDirectory(nodeProperties.getDatabaseLocation() + "\\" + databaseName);
            databaseLookUp.removeDatabaseObject(databaseName);
        } finally {
            lock.unlockWrite(stamp);
        }

        return SuccessfulResponse.DATABASE_DELETED;
    }

    private DatabaseObject loadDatabase(String databaseName, DatabaseLookUp databaseLookUp) {
        DatabaseObject databaseObject;
        synchronized (this) {
            if (databaseLookUp.hasDatabaseObject(databaseName)) {
                databaseObject = databaseLookUp.getDatabaseObject(databaseName);
            } else {
                databaseObject = new DatabaseObject(databaseName);
                databaseLookUp.addDatabaseObject(databaseObject);
            }
        }
        return databaseObject;
    }

    private boolean doesDatabaseExist(String databaseName, DatabaseLookUp databaseLookUp) {
        if (databaseLookUp.hasDatabaseObject(databaseName))
            return true;

        return diskService.doesFileExist(nodeProperties.getDatabaseLocation() + "\\" + databaseName);
    }
}
