package org.useless.core.replication;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents a write operation that needs to be replicated.
 * This class is serializable to allow for network transmission.
 */
@Getter
public class WriteOperation implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public enum OperationType {
        PUT,
        REMOVE
    }
    
    private final String id;
    private final OperationType type;
    private final Serializable key;
    private final Serializable value;
    private final long timestamp;
    private final String sourceNodeId;
    
    /**
     * Create a new write operation.
     * 
     * @param type The type of operation (PUT or REMOVE)
     * @param key The key being operated on
     * @param value The value (for PUT operations, can be null for REMOVE)
     * @param sourceNodeId The ID of the node that originated this operation
     */
    public WriteOperation(OperationType type, Serializable key, Serializable value, String sourceNodeId) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.key = key;
        this.value = value;
        this.timestamp = Instant.now().toEpochMilli();
        this.sourceNodeId = sourceNodeId;
    }

    @Override
    public String toString() {
        return "WriteOperation{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", key=" + key +
                ", value=" + (type == OperationType.PUT ? value : "null") +
                ", timestamp=" + timestamp +
                ", sourceNodeId='" + sourceNodeId + '\'' +
                '}';
    }
}
