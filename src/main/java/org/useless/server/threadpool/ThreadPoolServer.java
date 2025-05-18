package org.useless.server.threadpool;

import org.useless.core.store.Store;
import org.useless.core.store.StoreManager;
import org.useless.server.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A server implementation that uses a thread pool to handle client connections.
 */
public class ThreadPoolServer implements Server {
    private final int port;
    private final int maxThreads;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private Thread serverThread;
    private Store store;

    /**
     * Creates a new ThreadPoolServer.
     *
     * @param port the port to listen on
     * @param maxThreads maximum number of threads in the pool
     * @throws IllegalArgumentException if port is invalid or maxThreads is not positive
     */
    public ThreadPoolServer(int port, int maxThreads) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 0 and 65535");
        }
        if (maxThreads <= 0) {
            throw new IllegalArgumentException("Max threads must be positive");
        }
        this.port = port;
        this.maxThreads = maxThreads;
        store = StoreManager.getStore();
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
                t.setDaemon(true);
                return t;
            });

            System.out.println("ThreadPoolServer started on port " + port + " with " + maxThreads + " threads");

            serverThread = new Thread(this::runServer, "server-acceptor");
//            serverThread.setDaemon(true);
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

    private void handleClient(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            String line;
            while ((line = in.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 0) continue;

                String response;
                switch (parts[0].toUpperCase()) {
                    case "SET":
                        if (parts.length == 3) {
                            store.put(parts[1], parts[2]);
                            response = "OK";
                        } else {
                            response = "ERROR: Usage SET key value";
                        }
                        break;
                    case "GET":
                        if (parts.length == 2) {
                            response = store.get(parts[1]).toString();
                        } else {
                            response = "ERROR: Usage GET key";
                        }
                        break;
                    case "EXIT":
                        response = "Bye!";
                        out.write(response + "\n");
                        out.flush();
                        socket.close();
                        return;
                    default:
                        response = "ERROR: Unknown command";
                }

                out.write(response + "\n");
                out.flush();
            }

        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
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

    @Override
    public boolean isRunning() {
        return isRunning.get();
    }
}
