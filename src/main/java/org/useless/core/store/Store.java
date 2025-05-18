package org.useless.core.store;

public interface Store<K, V> {

    V get(K key);
    void put(K key, V value);
    void remove(K key);
}
