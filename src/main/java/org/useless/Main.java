package org.useless;

import org.useless.core.store.KeyValueStore;
import org.useless.core.store.SimpleInMemoryStore;
import org.useless.server.Server;
import org.useless.server.ServerFactory;

/**
 * Main entry point for the UselessDB server.
 */
public class Main {
    private static final int DEFAULT_PORT = 6379;  // Default Redis port for compatibility
    private static Server server;
    
    public static void main(String[] args) {
        System.out.println("Starting UselessDB server...");
        
        try {
            // Create a simple in-memory store
            KeyValueStore store = new SimpleInMemoryStore();
            
            // Create server configuration
            ServerFactory.ServerConfig config = new ServerFactory.ServerConfig()
                .setMaxThreads(10)  // Number of threads in the thread pool
                .setMaxConnections(100);  // Maximum number of concurrent connections
            
            // Create and start the server
            server = ServerFactory.createServer(
                Server.ServerType.THREAD_PER_REQUEST,
                DEFAULT_PORT,
                config,
                store
            );
            
            // Add shutdown hook to ensure clean shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down UselessDB server...");
                try {
                    if (server != null) {
                        server.stop();
                    }
                } catch (Exception e) {
                    System.err.println("Error during shutdown: " + e.getMessage());
                }
            }));
            
            // Start the server
            server.start();
            
            System.out.println("UselessDB server is running. Press Ctrl+C to stop.");
            
            // Keep the main thread alive
            Thread.currentThread().join();
            
        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}