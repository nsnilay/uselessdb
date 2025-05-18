package org.useless.server;

/**
 * Interface for a server that can handle client connections.
 */
public interface Server {
    /**
     * Starts the server.
     * @throws IllegalStateException if the server is already running
     */
    void start();

    /**
     * Stops the server and releases all resources.
     * Can be called multiple times safely.
     */
    void stop();

    /**
     * @return true if the server is currently running, false otherwise
     */
    boolean isRunning();
}
