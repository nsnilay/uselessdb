package org.useless.core.store;

import org.useless.core.replication.ReplicationConfig;
import org.useless.core.replication.ReplicationException;
import org.useless.core.replication.ReplicationStrategy;
import org.useless.core.replication.ReplicationStrategyFactory;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manager for creating and retrieving store instances.
 */
public class StoreManager {
    private static final Logger LOGGER = Logger.getLogger(StoreManager.class.getName());

    /**
     * Get a simple key-value store without replication.
     *
     * @param <K> The key type
     * @param <V> The value type
     * @return A new store instance
     */
    public static <K extends Serializable, V extends Serializable> Store<K, V> getStore() {
        return new SimpleKVStore<>();
    }
    
    /**
     * Get a replicated store using the specified replication configuration.
     *
     * @param config The replication configuration
     * @param <K> The key type
     * @param <V> The value type
     * @return A new replicated store instance
     * @throws ReplicationException If there's an error initializing replication
     */
    public static <K extends Serializable, V extends Serializable> Store<K, V> getReplicatedStore(
            ReplicationConfig config) throws ReplicationException {
        
        // Create the underlying store
        Store<K, V> baseStore = new SimpleKVStore<>();
        
        // Create and initialize the replication strategy
        ReplicationStrategy strategy = ReplicationStrategyFactory.createStrategy(config);
        
        // Start the replication strategy
        try {
            strategy.start();
        } catch (ReplicationException e) {
            LOGGER.log(Level.SEVERE, "Failed to start replication strategy", e);
            throw e;
        }
        
        // Create and return the replicated store
        return new ReplicatedStore<>(baseStore, strategy, config.getNodeId());
    }
}
