package org.decentralizeddatabase.models.idlocksincollection;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;

public class IdLocksInCollectionJacksonSerializer extends JsonSerializer<IdLocksInCollection> {

    @Override
    public void serialize(IdLocksInCollection idLocksInCollection, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        Map<Integer, StampedLock> idLocks = idLocksInCollection.getIdLocks();

        if (idLocks != null) {
            for (Map.Entry<Integer, StampedLock> entry : idLocks.entrySet()) {
                objectNode.putPOJO(entry.getKey().toString(), null);
            }
        }

        jsonGenerator.writeObject(objectNode);
    }
}