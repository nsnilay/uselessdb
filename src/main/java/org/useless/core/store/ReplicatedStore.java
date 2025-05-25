package org.useless.core.store;

import org.useless.core.replication.*;
import org.useless.core.replication.WriteOperation.OperationType;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Store implementation that replicates operations to other nodes.
 * This class decorates another Store implementation and adds replication functionality.
 */
public class ReplicatedStore<K extends Serializable, V extends Serializable> implements Store<K, V> {
    private static final Logger LOGGER = Logger.getLogger(ReplicatedStore.class.getName());
    
    private final Store<K, V> delegate;
    private final ReplicationStrategy replicationStrategy;
    private final String nodeId;
    
    /**
     * Creates a new ReplicatedStore.
     * 
     * @param delegate The underlying store implementation
     * @param replicationStrategy The replication strategy to use
     * @param nodeId The ID of this node
     */
    public ReplicatedStore(Store<K, V> delegate, ReplicationStrategy replicationStrategy, String nodeId) {
        this.delegate = delegate;
        this.replicationStrategy = replicationStrategy;
        this.nodeId = nodeId;
    }
    
    @Override
    public V get(K key) {
        // Read operations don't need to be replicated
        return delegate.get(key);
    }
    
    @Override
    public void put(K key, V value) {
        // First, apply the operation locally
        delegate.put(key, value);
        
        // Then, if this node can accept writes, propagate the operation to replicas
        if (replicationStrategy.canAcceptWrites()) {
            try {
                WriteOperation operation = new WriteOperation(
                        OperationType.PUT,
                        key,
                        value,
                        nodeId
                );
                replicationStrategy.propagateWrite(operation);
            } catch (ReplicationException e) {
                LOGGER.log(Level.WARNING, "Failed to propagate PUT operation", e);
                // In a production system, we might want to handle this differently
                // For example, we might want to queue the operation for retry
            }
        }
    }
    
    @Override
    public void remove(K key) {
        // First, apply the operation locally
        delegate.remove(key);
        
        // Then, if this node can accept writes, propagate the operation to replicas
        if (replicationStrategy.canAcceptWrites()) {
            try {
                WriteOperation operation = new WriteOperation(
                        OperationType.REMOVE,
                        key,
                        null,
                        nodeId
                );
                replicationStrategy.propagateWrite(operation);
            } catch (ReplicationException e) {
                LOGGER.log(Level.WARNING, "Failed to propagate REMOVE operation", e);
                // In a production system, we might want to handle this differently
                // For example, we might want to queue the operation for retry
            }
        }
    }
    
    /**
     * Apply a write operation from another node.
     * This method is called by the replication system when an operation is received from another node.
     * 
     * @param operation The operation to apply
     */
    public void applyOperation(WriteOperation operation) {
        if (operation.getSourceNodeId().equals(nodeId)) {
            // Skip operations that originated from this node
            return;
        }
        
        try {
            switch (operation.getType()) {
                case PUT:
                    delegate.put((K) operation.getKey(), (V) operation.getValue());
                    break;
                case REMOVE:
                    delegate.remove((K) operation.getKey());
                    break;
                default:
                    LOGGER.warning("Unknown operation type: " + operation.getType());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to apply operation: " + operation, e);
        }
    }
    
    /**
     * Get the replication status.
     * 
     * @return The current replication status
     */
    public ReplicationStatus getReplicationStatus() {
        return replicationStrategy.getStatus();
    }
}
