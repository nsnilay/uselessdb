package org.useless.core.replication;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Configuration for replication strategies.
 * Contains settings that apply to all replication strategies as well as
 * strategy-specific settings.
 */
public class ReplicationConfig {
    
    public enum NodeRole {
        MASTER,
        SLAVE,
        PEER
    }
    
    private final String nodeId;
    private final NodeRole role;
    private final List<NodeInfo> nodes;
    private final int replicationPort;
    private final int syncIntervalMs;
    private final int connectionTimeoutMs;
    private final int maxRetries;
    private final boolean asyncReplication;
    private final ReplicationStrategy.ReplicationStrategyType strategyType;

    
    private ReplicationConfig(Builder builder) {
        this.nodeId = builder.nodeId;
        this.role = builder.role;
        this.nodes = builder.nodes;
        this.replicationPort = builder.replicationPort;
        this.syncIntervalMs = builder.syncIntervalMs;
        this.connectionTimeoutMs = builder.connectionTimeoutMs;
        this.maxRetries = builder.maxRetries;
        this.asyncReplication = builder.asyncReplication;
        this.strategyType = builder.strategyType;
    }
    
    public String getNodeId() {
        return nodeId;
    }
    
    public NodeRole getRole() {
        return role;
    }
    
    public List<NodeInfo> getNodes() {
        return new ArrayList<>(nodes);
    }
    
    public int getReplicationPort() {
        return replicationPort;
    }
    
    public int getSyncIntervalMs() {
        return syncIntervalMs;
    }
    
    public int getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }
    
    public int getMaxRetries() {
        return maxRetries;
    }
    
    public boolean isAsyncReplication() {
        return asyncReplication;
    }

    public ReplicationStrategy.ReplicationStrategyType getStrategyType() {
        return strategyType;
    }

    /**
     * Builder for ReplicationConfig.
     */
    public static class Builder {
        private String nodeId = UUID.randomUUID().toString();
        private NodeRole role = NodeRole.SLAVE;
        private List<NodeInfo> nodes = new ArrayList<>();
        private int replicationPort = 9090;
        private int syncIntervalMs = 1000;
        private int connectionTimeoutMs = 5000;
        private int maxRetries = 3;
        private boolean asyncReplication = true;
        private ReplicationStrategy.ReplicationStrategyType strategyType;
        
        public Builder nodeId(String nodeId) {
            this.nodeId = nodeId;
            return this;
        }
        
        public Builder role(NodeRole role) {
            this.role = role;
            return this;
        }
        
        public Builder addNode(NodeInfo node) {
            this.nodes.add(node);
            return this;
        }
        
        public Builder nodes(List<NodeInfo> nodes) {
            this.nodes = new ArrayList<>(nodes);
            return this;
        }
        
        public Builder replicationPort(int replicationPort) {
            this.replicationPort = replicationPort;
            return this;
        }
        
        public Builder syncIntervalMs(int syncIntervalMs) {
            this.syncIntervalMs = syncIntervalMs;
            return this;
        }
        
        public Builder connectionTimeoutMs(int connectionTimeoutMs) {
            this.connectionTimeoutMs = connectionTimeoutMs;
            return this;
        }
        
        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }
        
        public Builder asyncReplication(boolean asyncReplication) {
            this.asyncReplication = asyncReplication;
            return this;
        }

        public Builder strategyType(ReplicationStrategy.ReplicationStrategyType strategyType) {
            this.strategyType = strategyType;
            return this;
        }
        
        public ReplicationConfig build() {
            return new ReplicationConfig(this);
        }
    }
}
