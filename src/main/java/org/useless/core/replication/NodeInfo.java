package org.useless.core.replication;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

/**
 * Information about a node in the replication cluster.
 */
@Getter
@Builder
public class NodeInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final String nodeId;
    private final String hostname;
    private final int port;
    private final ReplicationConfig.NodeRole role;
    
    /**
     * Create a new NodeInfo.
     * 
     * @param nodeId The unique ID of the node
     * @param hostname The hostname or IP address of the node
     * @param port The port on which the node's replication service is listening
     * @param role The role of the node in the replication cluster
     */
    public NodeInfo(String nodeId, String hostname, int port, ReplicationConfig.NodeRole role) {
        this.nodeId = nodeId;
        this.hostname = hostname;
        this.port = port;
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        NodeInfo nodeInfo = (NodeInfo) o;
        return nodeId.equals(nodeInfo.nodeId);
    }
    
    @Override
    public int hashCode() {
        return nodeId.hashCode();
    }
    
    @Override
    public String toString() {
        return "NodeInfo{" +
                "nodeId='" + nodeId + '\'' +
                ", hostname='" + hostname + '\'' +
                ", port=" + port +
                ", role=" + role +
                '}';
    }


}
