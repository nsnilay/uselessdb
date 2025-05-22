package org.useless.server.virtualthread;

import org.useless.server.AbstractServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A server implementation that uses virtual threads (Project Loom) to handle client connections.
 * Each client connection is handled by a dedicated virtual thread, which is lightweight and efficient.
 */
public class VirtualThreadServer extends AbstractServer {
    private ServerSocket serverSocket;
    private ExecutorService executor;
    private Thread serverThread;

    /**
     * Creates a new VirtualThreadServer.
     *
     * @param port the port to listen on
     * @throws IllegalArgumentException if port is invalid
     */
    public VirtualThreadServer(int port) {
        super(port);
    }

    @Override
    public void start() {
        if (!isRunning.compareAndSet(false, true)) {
            throw new IllegalStateException("Server is already running");
        }

        try {
            serverSocket = new ServerSocket(port);
            // Create a virtual thread executor
            executor = Executors.newVirtualThreadPerTaskExecutor();

            System.out.println("VirtualThreadServer started on port " + port + " with virtual threads");

            // Create a Runnable for the server loop
            Runnable serverTask = this::runServer;
            
            // Create and start the virtual thread using the builder pattern
            // The difference is we're storing the thread reference first, then starting it
            serverThread = Thread.ofVirtual()
                    .name("server-acceptor")
                    .unstarted(serverTask);
            
            // Start the thread explicitly
            serverThread.start();
            
            // Wait a moment for the server to initialize
            Thread.sleep(1000);
            
            // Log confirmation that server is running
            System.out.println("VirtualThreadServer is now accepting connections on port " + port);
        } catch (Exception e) {
            isRunning.set(false);
            throw new RuntimeException("Failed to start server", e);
        }
    }

    private void runServer() {
        try {
            System.out.println("Server loop started in virtual thread: " + Thread.currentThread().getName());
            while (isRunning.get()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Accepted connection from: " + clientSocket.getInetAddress());
                    executor.execute(() -> handleClient(clientSocket));
                } catch (IOException e) {
                    if (isRunning.get()) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } finally {
            // Only call stop if we're still running
            if (isRunning.get()) {
                System.out.println("Server loop exiting, calling stop()");
                stop();
            }
        }
    }

    @Override
    public void stop() {
        if (!isRunning.compareAndSet(true, false)) {
            return;
        }

        System.out.println("Shutting down VirtualThreadServer...");

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
        }

        if (executor != null) {
            executor.shutdown();
        }

        if (serverThread != null) {
            serverThread.interrupt();
            try {
                // Wait for the server thread to terminate
                serverThread.join(5000);
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting for server thread to terminate");
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("VirtualThreadServer shutdown complete");
    }
}
