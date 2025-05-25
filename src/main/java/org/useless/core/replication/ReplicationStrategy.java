package org.useless.core.replication;

/**
 * Interface defining the contract for different replication strategies.
 * This allows for different replication implementations (master-slave, peer-to-peer, etc.)
 * while maintaining a consistent API.
 */
public interface ReplicationStrategy {

    public enum ReplicationStrategyType {
        MASTER_SLAVE,
        MULTI_MASTER,
        LEADERLESS
    }
    
    /**
     * Initialize the replication strategy.
     * 
     * @param config The configuration for this replication strategy
     * @throws ReplicationException if initialization fails
     */
    void initialize(ReplicationConfig config) throws ReplicationException;
    
    /**
     * Propagate a write operation to replicas.
     * 
     * @param operation The operation to propagate
     * @throws ReplicationException if propagation fails
     */
    void propagateWrite(WriteOperation operation) throws ReplicationException;
    
    /**
     * Check if this node can accept writes.
     * 
     * @return true if this node can accept writes, false otherwise
     */
    boolean canAcceptWrites();
    
    /**
     * Start the replication process.
     * 
     * @throws ReplicationException if starting replication fails
     */
    void start() throws ReplicationException;
    
    /**
     * Stop the replication process.
     */
    void stop();
    
    /**
     * Get the current replication status.
     * 
     * @return the current replication status
     */
    ReplicationStatus getStatus();
}
