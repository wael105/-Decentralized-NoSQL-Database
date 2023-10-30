package org.decentralizeddatabase.services;

import org.decentralizeddatabase.LRUCache;
import org.decentralizeddatabase.constants.FailedResponse;
import org.decentralizeddatabase.constants.Response;
import org.decentralizeddatabase.constants.SuccessfulResponse;
import org.decentralizeddatabase.models.Collection;
import org.decentralizeddatabase.models.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;

public enum DocumentService {

    INSTANCE(DiskService.INSTANCE, new LRUCache<>(100));

    private final DiskService diskService;

    private final LRUCache<String, Document> cache;

    private static final String DOCUMENT_VERSION = "Document version: ";

    DocumentService(DiskService diskService, LRUCache<String, Document> cache) {
        this.diskService = diskService;
        this.cache = cache;
    }

    public Response createDocumentAndAddAffinity(Collection collection, Map<String, Object> data, String nodeName, int documentId) {
        // This lock is used to ensure that the same document is not creating by two processes/threads at the same time.
        long collectionStamp = collection.getLock().writeLock();

        try {
            int id = (int) data.get("id");
            if (doesIdExist(collection, id))
                return FailedResponse.DOCUMENT_ALREADY_EXISTS;

            data.put(DOCUMENT_VERSION, 1);

            diskService.write(collection.getFilePath(id), data);
            collection.addDocument(id, data);
            diskService.write(collection.getIdsPath(), collection.getIdsIndex());
            diskService.write(collection.getIndexPath(), collection.getIndexes());


            collection.addAffinity(nodeName, documentId);
            diskService.write(collection.getAffinityPath(), collection.getAffinityOfOtherNodes());

            return SuccessfulResponse.DOCUMENT_INSERTED;
        } finally {
            collection.getLock().unlockWrite(collectionStamp);
        }
    }

    public Response createDocumentWithoutAddingAffinity(Collection collection, Map<String, Object> data) {
        // This lock is used to ensure that the same document is not creating by two processes/threads at the same time.
        long collectionStamp = collection.getLock().writeLock();
        try {
            int id = (int) data.get("id");
            if (doesIdExist(collection, id))
                return FailedResponse.DOCUMENT_ALREADY_EXISTS;

            data.put(DOCUMENT_VERSION, 1);

            diskService.write(collection.getFilePath(id), data);
            collection.addDocument(id, data);
            diskService.write(collection.getIdsPath(), collection.getIdsIndex());
            diskService.write(collection.getIndexPath(), collection.getIndexes());

            return SuccessfulResponse.DOCUMENT_INSERTED;
        } finally {
            collection.getLock().unlockWrite(collectionStamp);
        }
    }

    public Response updateDocument(Collection collection, Map<String, Object> document) {
        int id = (int) document.get("id");
        if (!doesIdExist(collection, id))
            return FailedResponse.DOCUMENT_DOES_NOT_EXIST;
        // This lock is used to ensure that the document is not modified by another process/thread while we are deleting it.
        StampedLock lock = collection.getDocumentLock(id);
        // using a read lock here means that the delete might starve (stamped locks do not have a preference policy)
        long stamp = lock.readLock();

        try {
            if (!doesIdExist(collection, id))
                return FailedResponse.DOCUMENT_DOES_NOT_EXIST;

            Document oldDocument = getExistingDocument(collection, id);
            int version = (int) oldDocument.getValue().get(DOCUMENT_VERSION);

            // Check if the document has been modified by another process/thread.
            if (version != (int) document.get(DOCUMENT_VERSION)) {
                return FailedResponse.CONCURRENCY_CONFLICT;
            }

            // Update the document with a new version.
            document.put(DOCUMENT_VERSION, version + 1);
            cache.remove(collection.getDatabaseName() + collection.getCollectionName() + id);
            diskService.write(collection.getFilePath(id), document);
            collection.updateDocument(id, document, oldDocument.getValue());
            diskService.write(collection.getIndexPath(), collection.getIndexes());
            // change the version of the document back to the old one so that the sync requests can update it
            document.put(DOCUMENT_VERSION, version);
        } finally {
            lock.unlockRead(stamp);
        }

        return SuccessfulResponse.DOCUMENT_UPDATED;
    }

    public Object getDocument(Collection collection, int id) {
        if (!doesIdExist(collection, id)) {
            return FailedResponse.DOCUMENT_DOES_NOT_EXIST;
        }

        StampedLock lock = collection.getDocumentLock(id);
        long stamp = lock.tryOptimisticRead();

        Document document = cache.get(collection.getDatabaseName() + collection.getCollectionName() + id);
        if (document == null) {
            document = getExistingDocument(collection, id);
            cache.put(collection.getDatabaseName() + collection.getCollectionName() + id, document);
        }
        if (!lock.validate(stamp)) {
            cache.remove(collection.getDatabaseName() + collection.getCollectionName() + id);
            stamp = lock.readLock();
            try {
                document = getExistingDocument(collection, id);
                if (document.getValue() != null)
                    cache.put(collection.getDatabaseName() + collection.getCollectionName() + id, document);
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return document;
    }

    // Can get stale data because the lock works on a single document not the whole operation
    public List<Document> getDocumentsByProperty(Collection collection, String property, String value) {
        List<Document> result = new ArrayList<>();
        for (int id : collection.getListOfJsonsContainingAttribute(property, value)) {
            Object res = getDocument(collection, id);
            if (res instanceof Document document)
                result.add(document);
        }
        return result;
    }

    public Response deleteDocumentAndAffinity(Collection collection, String nodeName, int documentId) {
        if (!doesIdExist(collection, documentId))
            return FailedResponse.DOCUMENT_DOES_NOT_EXIST;

        StampedLock lock = collection.getDocumentLock(documentId);
        long stamp = lock.writeLock();
        try {
            if (!doesIdExist(collection, documentId))
                return FailedResponse.DOCUMENT_DOES_NOT_EXIST;

            cache.remove(collection.getDatabaseName() + collection.getCollectionName() + documentId);
            Document document = getExistingDocument(collection, documentId);
            collection.deleteDocument(documentId, document.getValue());
            diskService.write(collection.getIdsPath(), collection.getIdsIndex());
            diskService.write(collection.getIndexPath(), collection.getIndexes());

            collection.deleteAffinity(nodeName, documentId);
            diskService.write(collection.getAffinityPath(), collection.getAffinityOfOtherNodes());
            // needs to be the last one to prevent the creation of a document with the same id while deleting it (keeps the document in memory as long as possible)
            diskService.deleteDirectory(collection.getFilePath(documentId));

            return SuccessfulResponse.DOCUMENT_DELETED;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public Response deleteDocumentWithoutAffinity(Collection collection, int documentId) {
        if (!doesIdExist(collection, documentId))
            return FailedResponse.DOCUMENT_DOES_NOT_EXIST;

        StampedLock lock = collection.getDocumentLock(documentId);
        long stamp = lock.writeLock();
        try {
            if (!doesIdExist(collection, documentId))
                return FailedResponse.DOCUMENT_DOES_NOT_EXIST;

            cache.remove(collection.getDatabaseName() + collection.getCollectionName() + documentId);
            Document document = getExistingDocument(collection, documentId);
            collection.deleteDocument(documentId, document.getValue());
            diskService.write(collection.getIdsPath(), collection.getIdsIndex());
            diskService.write(collection.getIndexPath(), collection.getIndexes());
            // needs to be the last one to prevent the creation of a document with the same id while deleting it (keeps the document in memory as long as possible)
            diskService.deleteDirectory(collection.getFilePath(documentId));

            return SuccessfulResponse.DOCUMENT_DELETED;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    private Document getExistingDocument(Collection collection, int id) {
        Document document = new Document();
        document.setValue(diskService.read(collection.getFilePath(id), Map.class));

        return document;
    }

    private boolean doesIdExist(Collection collection, int id) {
        if(collection.doesIdExist(id)) {
            return true;
        }

        return diskService.doesFileExist(collection.getFilePath(id));
    }
}
