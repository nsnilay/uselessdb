package org.useless.server.threadpool;

import org.useless.server.AbstractServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A server implementation that uses a thread pool to handle client connections.
 */
public class ThreadPoolServer extends AbstractServer {
    private final int maxThreads;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private Thread serverThread;

    /**
     * Creates a new ThreadPoolServer.
     *
     * @param port the port to listen on
     * @param maxThreads maximum number of threads in the pool
     * @throws IllegalArgumentException if port is invalid or maxThreads is not positive
     */
    public ThreadPoolServer(int port, int maxThreads) {
        super(port);
        if (maxThreads <= 0) {
            throw new IllegalArgumentException("Max threads must be positive");
        }
        this.maxThreads = maxThreads;
    }

    @Override
    public void start() {
        if (!isRunning.compareAndSet(false, true)) {
            throw new IllegalStateException("Server is already running");
        }

        try {
            serverSocket = new ServerSocket(port);
            threadPool = Executors.newFixedThreadPool(maxThreads, r -> {
                Thread t = new Thread(r, "server-worker-" + System.currentTimeMillis());
                return t;
            });

            System.out.println("ThreadPoolServer started on port " + port + " with " + maxThreads + " threads");

            serverThread = new Thread(this::runServer, "server-acceptor");
            serverThread.start();
            Thread.sleep(1000);
        } catch (Exception e) {
            isRunning.set(false);
            throw new RuntimeException("Failed to start server", e);
        }
    }

    private void runServer() {
        try {
            while (isRunning.get()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.execute(() -> handleClient(clientSocket));
                } catch (IOException e) {
                    if (isRunning.get()) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } finally {
            stop();
        }
    }

    @Override
    public void stop() {
        if (!isRunning.compareAndSet(true, false)) {
            return;
        }

        System.out.println("Shutting down ThreadPoolServer...");

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
        }

        if (threadPool != null) {
            threadPool.shutdown();
        }

        if (serverThread != null) {
            serverThread.interrupt();
        }
    }
}
