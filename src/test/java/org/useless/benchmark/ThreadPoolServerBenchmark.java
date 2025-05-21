package org.useless.benchmark;

import org.useless.server.Server;
import org.useless.server.threadpool.ThreadPoolServer;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Performance benchmark for ThreadPoolServer.
 * Tests various metrics including throughput, latency, and concurrent connections.
 */
public class ThreadPoolServerBenchmark {
    private static final int SERVER_PORT = 8080;
    private static final int WARMUP_ITERATIONS = 5;
    private static final int MEASUREMENT_ITERATIONS = 10;
    private static final int REQUESTS_PER_ITERATION = 1000;
    private static final int MAX_THREADS = 100;
    private static final int KEY_SPACE_SIZE = 1000;
    private static final int VALUE_SIZE = 100; // characters
    private static final int CLIENT_POOL_SIZE = 50;

    private final Server server;
    private final ExecutorService clientPool;
    private final Random random = new Random();
    private final String[] testKeys;
    private final String[] testValues;

    public ThreadPoolServerBenchmark() {
        this.server = new ThreadPoolServer(SERVER_PORT, Runtime.getRuntime().availableProcessors() * 2);
        this.clientPool = Executors.newFixedThreadPool(MAX_THREADS);

        // Initialize test data
        this.testKeys = new String[KEY_SPACE_SIZE];
        this.testValues = new String[KEY_SPACE_SIZE];
        for (int i = 0; i < KEY_SPACE_SIZE; i++) {
            testKeys[i] = "key" + i;
            testValues[i] = generateRandomString(VALUE_SIZE);
        }
    }

    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private void runBenchmark() throws Exception {
        System.out.println("=== Starting ThreadPoolServer Benchmark ===");
        System.out.println("Warmup iterations: " + WARMUP_ITERATIONS);
        System.out.println("Measurement iterations: " + MEASUREMENT_ITERATIONS);
        System.out.println("Requests per iteration: " + REQUESTS_PER_ITERATION);
        System.out.println("Max concurrent clients: " + MAX_THREADS);
        System.out.println("Key space size: " + KEY_SPACE_SIZE);
        System.out.println("Value size: " + VALUE_SIZE + " characters\n");

        // Start the server
        server.start();

        try {
            // Warmup phase
            System.out.println("=== Warming up ===");
//            runTest(WARMUP_ITERATIONS, REQUESTS_PER_ITERATION, true);

            // Measurement phase
            System.out.println("\n=== Running measurements ===");
            runTest(MEASUREMENT_ITERATIONS, REQUESTS_PER_ITERATION, false);

        } finally {
            // Cleanup
            server.stop();
            clientPool.shutdown();
            clientPool.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private void runTest(int iterations, int requestsPerIteration, boolean warmup) throws Exception {
        List<Long> throughputs = new ArrayList<>();
        List<Long> latencies = new ArrayList<>();

        for (int i = 0; i < iterations; i++) {
            // Test SET operations
            System.out.printf("%s Iteration %d: Testing SET operations...\n",
                warmup ? "Warmup" : "Measurement", i + 1);
            BenchmarkResult setResult = runOperationTest("SET", requestsPerIteration);

            // Test GET operations
            System.out.printf("%s Iteration %d: Testing GET operations...\n",
                warmup ? "Warmup" : "Measurement", i + 1);
            BenchmarkResult getResult = runOperationTest("GET", requestsPerIteration);

            if (!warmup) {
                throughputs.add((setResult.throughput + getResult.throughput) / 2);
                latencies.add((setResult.avgLatency + getResult.avgLatency) / 2);

                System.out.printf("  Throughput: %d ops/sec, Avg Latency: %.2f ms\n",
                    (setResult.throughput + getResult.throughput) / 2,
                    (setResult.avgLatency + getResult.avgLatency) / 2.0);
            }
        }

        if (!warmup) {
            double avgThroughput = throughputs.stream().mapToLong(l -> l).average().orElse(0);
            double avgLatency = latencies.stream().mapToLong(l -> l).average().orElse(0);

            System.out.println("\n=== Benchmark Results ===");
            System.out.printf("Average Throughput: %.2f ops/sec\n", avgThroughput);
            System.out.printf("Average Latency: %.2f ms\n", avgLatency);
            System.out.printf("Total Operations: %d\n", iterations * requestsPerIteration * 2);
        }
    }

    private BenchmarkResult runOperationTest(String operation, int numRequests) throws Exception {
        AtomicInteger completed = new AtomicInteger(0);
        AtomicLong totalLatency = new AtomicLong(0);
        CountDownLatch latch = new CountDownLatch(numRequests);

        BlockingQueue<PooledClient> pooledClients = new ArrayBlockingQueue<>(CLIENT_POOL_SIZE);
        for (int i = 0; i < CLIENT_POOL_SIZE; i++) {
            pooledClients.offer(new PooledClient("localhost", SERVER_PORT));
        }

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numRequests; i++) {
            final int idx = i % KEY_SPACE_SIZE;
            clientPool.submit(() -> {
                PooledClient client = null;

                    try {
                        client = pooledClients.take(); // borrow client from pool
                        long opStart = System.nanoTime();

                        if ("SET".equals(operation)) {
                            client.out.println(operation + " " + testKeys[idx] + " " + testValues[idx]);
                        } else { // GET
                            client.out.println(operation + " " + testKeys[idx]);
                        }

                        String response = client.in.readLine();
                        if (response == null || response.startsWith("ERROR")) {
                            System.err.println("Error in operation: " + response);
                        }

                        long latency = (System.nanoTime() - opStart) / 1_000_000; // ms
                        totalLatency.addAndGet(latency);
                        completed.incrementAndGet();

                } catch (Exception e) {
                    System.err.println("Error in client: " + e.getMessage() + " " + e);
                } finally {
                        if (client != null) pooledClients.offer(client);

                    latch.countDown();
                }
            });

        }

            // Control the rate of request submission to avoid overwhelming the system
//            if (i % 100 == 0) {
//                Thread.sleep(10);
//            }


        // Wait for all requests to complete with a timeout
        latch.await(5, TimeUnit.SECONDS);

        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println(" total time: " + totalTime + " ms");
        long throughput = totalTime > 0 ? (completed.get() * 1000L) / totalTime : 0;
        System.out.println("completed: " + completed.get() + " ops/sec");
        long avgLatency = completed.get() > 0 ? totalLatency.get() / completed.get() : 0;
        System.out.println("avgLatency: " + avgLatency + " ms");

//        for (PooledClient client : pooledClients) {
//            client.close();
//        }

        return new BenchmarkResult(throughput, avgLatency);
    }

    static class PooledClient {
        private final Socket socket;
        private final PrintWriter out;
        private final BufferedReader in;

        public PooledClient(String host, int port) throws IOException {
            this.socket = new Socket(host, port);
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        public synchronized String sendCommand(String command) throws IOException {
            out.println(command);
            return in.readLine();
        }

        public void close() throws IOException {
            socket.close();
        }
    }


    private static class BenchmarkResult {
        final long throughput; // ops/sec
        final long avgLatency; // ms

        BenchmarkResult(long throughput, long avgLatency) {
            this.throughput = throughput;
            this.avgLatency = avgLatency;
        }
    }

    public static void main(String[] args) throws Exception {
        ThreadPoolServerBenchmark benchmark = new ThreadPoolServerBenchmark();
        benchmark.runBenchmark();
    }
}
