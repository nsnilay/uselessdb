package org.useless.core.replication;

/**
 * Exception thrown when replication operations fail.
 */
public class ReplicationException extends Exception {
    
    public ReplicationException(String message) {
        super(message);
    }
    
    public ReplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
