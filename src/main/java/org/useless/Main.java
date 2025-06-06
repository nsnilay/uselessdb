package org.useless;

import org.useless.server.Server;
import org.useless.server.ServerFactory;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        // Create a thread pool server with 10 worker threads
        Server server = ServerFactory.createServer(
                ServerFactory.ServerType.THREAD_POOL,
                8080,  // port
                100     // max threads
        );

        // Add shutdown hook for graceful shutdown
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            System.out.println("Shutting down server...");
//            server.stop();
//        }));

        try {
            // Start the server
            server.start();
            System.out.println("Server started on port 8080. Press Ctrl+C to stop.");

            // Keep the main thread alive
            //In your ThreadPoolServer implementation, the thread pool's non-daemon threads keep the application alive
            // even without the Thread.currentThread().join() call. This is why it appeared to work correctly.
            //In contrast, the VirtualThreadServer's virtual threads are daemon threads, so they don't keep the
            // application alive when the main thread exits.
            Thread.currentThread().join();
        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
            server.stop();
            System.exit(1);
        }
    }
}