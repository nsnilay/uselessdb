package org.useless.core.store;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleKVStore<K, V> implements Store<K, V> {

    Map<K, V> map;

    public SimpleKVStore() {
        map = new ConcurrentHashMap<>();
    }

    @Override
    public V get(K key) {
        if (!map.containsKey(key)) {
            return (V) "";
        }

        return map.get(key);
    }

    @Override
    public void put(K key, V value) {
        map.computeIfAbsent(key, k -> value);
    }

    @Override
    public void remove(K key) {
        if (!map.containsKey(key)) {
            throw new NoSuchElementException();
        }
        map.remove(key);
    }
}
