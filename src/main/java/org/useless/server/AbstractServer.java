package org.useless.server;

import org.useless.core.store.Store;
import org.useless.core.store.StoreManager;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstract base class for server implementations.
 * Contains common functionality shared by different server types.
 */
public abstract class AbstractServer implements Server {
    protected final int port;
    protected final AtomicBoolean isRunning = new AtomicBoolean(false);
    protected final Store store;

    /**
     * Creates a new server.
     *
     * @param port the port to listen on
     * @throws IllegalArgumentException if port is invalid
     */
    protected AbstractServer(int port) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 0 and 65535");
        }
        this.port = port;
        this.store = StoreManager.getStore();
    }

    /**
     * Handles a client connection.
     * This method is common to all server implementations.
     *
     * @param socket the client socket
     */
    protected void handleClient(Socket socket) {
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
    public boolean isRunning() {
        return isRunning.get();
    }
}
