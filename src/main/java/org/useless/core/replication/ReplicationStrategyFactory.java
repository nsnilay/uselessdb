package org.useless.core.replication;

import org.useless.core.replication.masterslave.MasterSlaveReplicationStrategy;

/**
 * Factory for creating replication strategies.
 */
public class ReplicationStrategyFactory {
    
    /**
     * Creates a replication strategy based on the provided configuration.
     * 
     * @param config The replication configuration
     * @return A replication strategy instance
     * @throws ReplicationException If the strategy type is not supported
     */
    public static ReplicationStrategy createStrategy(ReplicationConfig config) throws ReplicationException {
        ReplicationStrategy strategy;
        
        switch (config.getStrategyType()) {
            case MASTER_SLAVE:
                strategy = new MasterSlaveReplicationStrategy();
                break;
            // Add more strategy types here as they are implemented
            default:
                throw new ReplicationException("Unsupported replication strategy type: " + config.getStrategyType());
        }
        
        // Initialize the strategy with the provided configuration
        strategy.initialize(config);
        
        return strategy;
    }
}
