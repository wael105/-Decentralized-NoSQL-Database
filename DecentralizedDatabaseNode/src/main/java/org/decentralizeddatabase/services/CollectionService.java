package org.decentralizeddatabase.services;

import org.decentralizeddatabase.models.Affinity;
import org.decentralizeddatabase.DatabaseUserState;
import org.decentralizeddatabase.models.idlocksincollection.IdLocksInCollection;
import org.decentralizeddatabase.models.Index;
import org.decentralizeddatabase.constants.FailedResponse;
import org.decentralizeddatabase.constants.Response;
import org.decentralizeddatabase.constants.SuccessfulResponse;
import org.decentralizeddatabase.models.Collection;
import org.decentralizeddatabase.models.DatabaseObject;
import org.decentralizeddatabase.models.schema.Schema;

import java.util.concurrent.locks.StampedLock;

public enum CollectionService {

    INSTANCE(DiskService.INSTANCE, DocumentService.INSTANCE);

    private final DiskService diskService;

    private final DocumentService documentService;

    CollectionService(DiskService diskService, DocumentService documentService) {
        this.diskService = diskService;
        this.documentService = documentService;
    }

    public Response createCollection(String collectionName, DatabaseObject database, Schema schema) {
        // this is in a write lock because two threads could try to create the same collection at the same time
        long stamp = database.getLock().writeLock();
        try {
            if (doesCollectionExist(collectionName, database))
                return FailedResponse.COLLECTION_ALREADY_EXISTS;

            diskService.createDirectory(database.getCollectionPath(collectionName));
            Index index = new Index();
            IdLocksInCollection idLocksInCollection = new IdLocksInCollection();
            Affinity affinity = new Affinity();
            Collection collection = new Collection(collectionName, database.getDatabaseName(), index, schema, idLocksInCollection, affinity);

            diskService.write(database.getCollectionPath(collectionName) + "\\" + "index.json", index);
            diskService.write(database.getCollectionPath(collectionName) + "\\" + "schema.json", schema);
            diskService.write(database.getCollectionPath(collectionName) + "\\" + "ids.json", idLocksInCollection);
            diskService.write(database.getCollectionPath(collectionName) + "\\" + "affinity.json", affinity);
            database.addCollection(collectionName, collection);

            return SuccessfulResponse.COLLECTION_CREATED;
        } finally {
            database.getLock().unlockWrite(stamp);
        }
    }

    public Response setCollection(String collectionName, DatabaseUserState databaseUserState) {
        DatabaseObject database = databaseUserState.getCurrentDatabaseObject();
        if (!doesCollectionExist(collectionName, database))
            return FailedResponse.COLLECTION_DOES_NOT_EXIST;

        Collection collection = loadCollection(collectionName, database);

        StampedLock lock = collection.getLock();
        long stamp = lock.tryOptimisticRead();

        databaseUserState.setCurrentCollection(collection);

        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                if (database.containsCollection(collectionName))
                    databaseUserState.setCurrentCollection(collection);
                else
                    databaseUserState.setCurrentDatabaseObject(null);
            } finally {
                lock.unlockRead(stamp);
            }
        }

        return SuccessfulResponse.COLLECTION_SET;
    }

    // if the collection is already set it still can be used, it will just fail because it doesn't exist on disk
    public Response deleteCollection(String collectionName, DatabaseObject database) {
        if (!doesCollectionExist(collectionName, database))
            return FailedResponse.COLLECTION_DOES_NOT_EXIST;

        Collection collection = loadCollection(collectionName, database);
        StampedLock lock = collection.getLock();
        long stamp = lock.writeLock();

        try {
            for (Integer id : collection.getIdsIndex().getIdLocks().keySet()) {
                // doesn't matter that its without affinity, because affinity file is deleted anyway
                documentService.deleteDocumentWithoutAffinity(collection, id);
            }
            diskService.deleteDirectory(database.getCollectionPath(collectionName) + "\\" + "index.json");
            diskService.deleteDirectory(database.getCollectionPath(collectionName) + "\\" + "schema.json");
            diskService.deleteDirectory(database.getCollectionPath(collectionName) + "\\" + "ids.json");
            diskService.deleteDirectory(database.getCollectionPath(collectionName) + "\\" + "affinity.json");
            diskService.deleteDirectory(database.getCollectionPath(collectionName));
            // needs to be the last one to prevent the creation of a document with the same id while deleting it
            database.removeCollection(collectionName);

            return SuccessfulResponse.COLLECTION_DELETED;

        } finally {
            lock.unlockWrite(stamp);
        }
    }

    private Collection loadCollection(String collectionName, DatabaseObject database) {
        Collection collection;

        // this is in a lock because two threads could try to load the same collection at the same time
        // resulting in two threads having different collection objects for the same collection

        long stamp = database.getLock().writeLock();
        try {
            if (database.containsCollection(collectionName)) {
                collection = database.getCollection(collectionName);
            } else {
                Index index = diskService.read(database.getCollectionPath(collectionName) + "\\" + "index.json", Index.class);
                Schema schema = diskService.read(database.getCollectionPath(collectionName) + "\\" + "schema.json", Schema.class);
                IdLocksInCollection idLocksInCollection = diskService.read(database.getCollectionPath(collectionName) + "\\" + "ids.json", IdLocksInCollection.class);
                Affinity affinity = diskService.read(database.getCollectionPath(collectionName) + "\\" + "affinity.json", Affinity.class);
                collection = new Collection(collectionName, database.getDatabaseName(), index, schema, idLocksInCollection, affinity);
                database.addCollection(collectionName, collection);
            }
        } finally {
            database.getLock().unlockWrite(stamp);
        }

        return collection;
    }

    private boolean doesCollectionExist(String collectionName, DatabaseObject database) {
        if (database.containsCollection(collectionName)) {
            return true;
        }

        return diskService.doesFileExist(database.getCollectionPath(collectionName));
    }
}
