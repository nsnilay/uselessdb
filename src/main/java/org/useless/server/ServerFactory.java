package org.useless.server;

import org.useless.server.threadpool.ThreadPoolServer;
import org.useless.server.virtualthread.VirtualThreadServer;

import java.util.Objects;

/**
 * Factory class for creating server instances.
 */
public class ServerFactory {
    /**
     * Creates a new server instance of the specified type.
     *
     * @param type the type of server to create
     * @param port the port to listen on
     * @param maxThreads maximum number of worker threads (for thread pool servers)
     * @return a new Server instance
     * @throws IllegalArgumentException if type is null or unknown
     */
    public static Server createServer(ServerType type, int port, int maxThreads) {
        Objects.requireNonNull(type, "Server type cannot be null");
        
        switch (type) {
            case THREAD_POOL:
                return new ThreadPoolServer(port, maxThreads);
            case SINGLE_THREADED:
                return new ThreadPoolServer(port, 1); // Single-threaded variant
            case VIRTUAL_THREAD:
                return new VirtualThreadServer(port); // Virtual thread implementation
            default:
                throw new IllegalArgumentException("Unsupported server type: " + type);
        }
    }
    
    /**
     * Supported server types.
     */
    public enum ServerType {
        /**
         * Uses a thread pool to handle client connections.
         */
        THREAD_POOL,
        
        /**
         * Uses a single thread to handle all client connections.
         */
        SINGLE_THREADED,

        /**
         * Uses Java's virtual threads (Project Loom) to handle client connections.
         * Each connection gets its own lightweight virtual thread.
         */
        VIRTUAL_THREAD,
        
        NETTY
    }
}
