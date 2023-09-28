package org.decentralizeddatabase.models.idlocksincollection;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;

public class IdLocksInCollectionJacksonDeserializer extends JsonDeserializer<IdLocksInCollection> {

    @Override
    public IdLocksInCollection deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        ObjectNode objectNode = jsonParser.getCodec().readTree(jsonParser);
        IdLocksInCollection idLocksInCollection = new IdLocksInCollection();
        Map<Integer, StampedLock> idLocks = new ConcurrentHashMap<>();

        if (objectNode != null) {
            for (Map.Entry<String, JsonNode> entry : objectNode.properties()) {
                int key = Integer.parseInt(entry.getKey());
                idLocks.put(key, new StampedLock());
            }
        }

        idLocksInCollection.setIdLocks(idLocks);
        return idLocksInCollection;
    }
}