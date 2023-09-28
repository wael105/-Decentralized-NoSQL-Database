package org.decentralizeddatabase;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.StampedLock;

public class LRUCache<K, V> {
    private final int capacity;
    private final Map<K, V> cache;
    private final Queue<K> evictionQueue;
    private final StampedLock lock;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.cache = new ConcurrentHashMap<>(capacity);
        this.evictionQueue = new ConcurrentLinkedQueue<>();
        this.lock = new StampedLock();
    }

    public V get(K key) {
        long stamp = lock.tryOptimisticRead();
        V value = cache.get(key);
        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                value = cache.get(key);
            } finally {
                lock.unlockRead(stamp);
            }
        }

        return value;
    }

    public void put(K key, V value) {
        long stamp = lock.writeLock();
        try {
            cache.put(key, value);
            evictionQueue.add(key);

            if (evictionQueue.size() > capacity) {
                K oldestKey = evictionQueue.poll();
                if (oldestKey != null) {
                    cache.remove(oldestKey);
                }
            }
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public void remove(K key) {
        long stamp = lock.writeLock();
        try {
            cache.remove(key);
            evictionQueue.remove(key);
        } finally {
            lock.unlockWrite(stamp);
        }
    }
}