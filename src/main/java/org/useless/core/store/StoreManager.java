package org.useless.core.store;

public class StoreManager {

    public static Store getStore() {
        return new SimpleKVStore();
    }
}
