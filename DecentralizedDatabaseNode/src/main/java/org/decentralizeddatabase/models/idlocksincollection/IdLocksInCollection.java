package org.decentralizeddatabase.models.idlocksincollection;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;

@Getter
@Setter
@JsonDeserialize(using = IdLocksInCollectionJacksonDeserializer.class)
@JsonSerialize(using = IdLocksInCollectionJacksonSerializer.class)
public class IdLocksInCollection {
    private Map<Integer, StampedLock> idLocks;

    public IdLocksInCollection() {
        idLocks = new ConcurrentHashMap<>();
    }
}