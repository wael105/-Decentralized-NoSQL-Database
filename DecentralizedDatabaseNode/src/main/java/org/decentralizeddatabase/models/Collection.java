package org.decentralizeddatabase.models;

import org.decentralizeddatabase.constants.NodeProperties;
import org.decentralizeddatabase.models.idlocksincollection.IdLocksInCollection;
import org.decentralizeddatabase.models.schema.Schema;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;

@Getter
public class Collection {
    private final Index indexes;

    private final Schema schema;

    private final IdLocksInCollection idsIndex;

    private final String collectionName;

    private final NodeProperties properties;

    private final String databaseName;

    private final Affinity affinityOfOtherNodes;

    private final StampedLock lock;

    public Collection(String collectionName,
                      String databaseName,
                      Index indexes,
                      Schema schema,
                      IdLocksInCollection idLocksInCollection,
                      Affinity affinityOfOtherNodes) {
        this.collectionName = collectionName;
        this.databaseName = databaseName;
        this.properties = NodeProperties.INSTANCE;
        this.indexes = indexes;
        this.idsIndex = idLocksInCollection;
        this.schema = schema;
        this.affinityOfOtherNodes = affinityOfOtherNodes;
        this.lock = new StampedLock();
    }

    public boolean doesIdExist(int id) {
        return idsIndex.getIdLocks().containsKey(id);
    }

    public Set<Integer> getListOfJsonsContainingAttribute(String property, String attribute) {
        // if the property is not indexed, return an empty list
        Set<Integer> result = indexes.getCollectionIndexes().get(property).get(attribute);
        if (result == null) {
            result = new HashSet<>();
        }
        return result;
    }

    public void addDocument(int id, Map<String, Object> document) {
        idsIndex.getIdLocks().put(id, new StampedLock());
        for (Map.Entry<String, Object> entry : document.entrySet()) {
            if (entry.getKey().equals("id") || !schema.getRequiredProperties().containsKey(entry.getKey()) || entry.getValue() instanceof Map || entry.getValue() instanceof List) {
                continue;
            }
            addToIndex(entry.getKey(), entry.getValue().toString(), id);
        }
    }

    private void addToIndex(String key, String stringValue, Integer id) {
        indexes.getCollectionIndexes().computeIfAbsent(key, k -> new ConcurrentHashMap<>())
                            .computeIfAbsent(stringValue, k -> ConcurrentHashMap.newKeySet())
                            .add(id);
    }

    public void updateDocument(int id, Map<String, Object> newDocument, Map<String, Object> oldDocument) {
        for (Map.Entry<String, Object> entry : newDocument.entrySet()) {
            if (entry.getKey().equals("id") || !schema.getRequiredProperties().containsKey(entry.getKey())) {
                continue;
            }

            String oldValue = oldDocument.get(entry.getKey()).toString();

            indexes.getCollectionIndexes().get(entry.getKey()).get(oldValue).remove(id);
            if (indexes.getCollectionIndexes().get(entry.getKey()).get(oldValue).isEmpty()) {
                indexes.getCollectionIndexes().get(entry.getKey()).remove(oldValue);
            }

            addToIndex(entry.getKey(), entry.getValue().toString(), id);
        }
    }

    public void deleteDocument(int id, Map<String, Object> document) {
        idsIndex.getIdLocks().remove(id);
        for (Map.Entry<String, Object> entry : document.entrySet()) {
            if (entry.getKey().equals("id") || !schema.getRequiredProperties().containsKey(entry.getKey())) {
                continue;
            }

            String value = document.get(entry.getKey()).toString();

            indexes.getCollectionIndexes().get(entry.getKey()).get(value).remove(id);
            if (indexes.getCollectionIndexes().get(entry.getKey()).get(value).isEmpty()) {
                indexes.getCollectionIndexes().get(entry.getKey()).remove(value);
            }
        }
    }

    public String getFilePath(int id) {
        return properties.getDatabaseLocation() + "\\" + databaseName + "\\" + collectionName + "\\" + id + ".json";
    }

    public String getIndexPath() {
        return properties.getDatabaseLocation() + "\\" + databaseName + "\\" + collectionName + "\\" + "index.json";
    }

    public String getAffinityPath() {
        return properties.getDatabaseLocation() + "\\" + databaseName + "\\" + collectionName + "\\" + "affinity.json";
    }

    public void addAffinity(String nodeName, int id) {
        affinityOfOtherNodes.getIds().computeIfAbsent(nodeName, k -> ConcurrentHashMap.newKeySet()).add(id);
    }

    public void deleteAffinity(String nodeName, int documentId) {
        affinityOfOtherNodes.getIds().get(nodeName).remove(documentId);
    }

    public boolean hasAffinity(int id) {
        for (Set<Integer> set: affinityOfOtherNodes.getIds().values()) {
            if (set.contains(id)) {
                return false;
            }
        }
        return true;
    }

    public String getIdsPath() {
        return properties.getDatabaseLocation() + "\\" + databaseName + "\\" + collectionName + "\\" + "ids.json";
    }

    public String getAffinityNode(int id) {
        for (Map.Entry<String, Set<Integer>> entry : affinityOfOtherNodes.getIds().entrySet()) {
            if (entry.getValue().contains(id)) {
                return entry.getKey();
            }
        }
        // not a problem because we check if the node has affinity before calling this method
        return null;
    }

    public StampedLock getDocumentLock(int id) {
        return idsIndex.getIdLocks().get(id);
    }
}