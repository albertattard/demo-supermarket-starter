package demo.supermarket.e2e.harness;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import static java.util.concurrent.TimeUnit.SECONDS;

public final class ApplicationHarness implements AutoCloseable {

    private static final Duration STARTUP_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration PROBE_TIMEOUT = Duration.ofSeconds(2);

    private final Process process;
    private final String baseUrl;
    private final List<String> logs = new CopyOnWriteArrayList<>();

    private ApplicationHarness(final Process process, final String baseUrl) {
        this.process = Objects.requireNonNull(process);
        this.baseUrl = Objects.requireNonNull(baseUrl);
    }

    static ApplicationHarness start() {
        final int port = configuredPort();
        final Path appJar = configuredJar();
        if (!Files.isRegularFile(appJar)) {
            throw new IllegalStateException("Application jar does not exist: " + appJar);
        }

        final List<String> command = new ArrayList<>();
        command.add(javaExecutable());
        command.add("-jar");
        command.add(appJar.toString());
        command.add("--server.port=" + port);

        try {
            final Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();

            ApplicationHarness harness = null;
            try {
                harness = new ApplicationHarness(process, "http://localhost:" + port);
                harness.streamLogs();
                harness.waitUntilReady();
                return harness;
            } catch (final RuntimeException e) {
                Closeables.closeAllSuppressing(e, harness);
                throw e;
            }
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to start application under test", e);
        }
    }

    String baseUrl() {
        return baseUrl;
    }

    @Override
    public void close() {
        process.destroy();
        try {
            if (!process.waitFor(10, SECONDS)) {
                process.destroyForcibly();
                process.waitFor(5, SECONDS);
            }
        } catch (final InterruptedException _) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
        }
    }

    private ApplicationHarness waitUntilReady() {
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(PROBE_TIMEOUT)
                .build()) {
            final URI health = URI.create(baseUrl + "/actuator/health");
            final long deadline = System.nanoTime() + STARTUP_TIMEOUT.toNanos();

            while (System.nanoTime() < deadline) {
                if (!process.isAlive()) {
                    throw new IllegalStateException("Application exited before readiness. Logs:\n" + recentLogs());
                }

                try {
                    final HttpRequest request = HttpRequest.newBuilder(health)
                            .timeout(PROBE_TIMEOUT)
                            .GET()
                            .build();
                    final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200) {
                        return this;
                    }
                } catch (final IOException _) {
                    // The process may still be binding the HTTP port.
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while waiting for application readiness", e);
                }

                try {
                    Thread.sleep(250);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while waiting for application readiness", e);
                }
            }

            throw new IllegalStateException("Application did not become ready at " + health + ". Logs:\n" + recentLogs());
        }
    }

    private ApplicationHarness streamLogs() {
        Thread.ofPlatform()
                .name("e2e-app-logs")
                .daemon(true)
                .start(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        final boolean printLogs = Configuration.configuredBoolean("e2e.app.logs", "E2E_APP_LOGS", false);
                        for (String line; (line = reader.readLine()) != null;) {
                            logs.add(line);
                            if (printLogs) {
                                System.out.println("[app] " + line);
                            }
                        }
                    } catch (final IOException e) {
                        logs.add("Failed to read application logs: " + e.getMessage());
                    }
                });

        return this;
    }

    private String recentLogs() {
        final int from = Math.max(0, logs.size() - 80);
        return String.join(System.lineSeparator(), logs.subList(from, logs.size()));
    }

    private static int configuredPort() {
        return Configuration.configured("e2e.server.port", "E2E_SERVER_PORT")
                .map(port -> parsePort(port, "e2e.server.port", "E2E_SERVER_PORT"))
                .orElseGet(ApplicationHarness::allocatePort);
    }

    private static int parsePort(final String port, final String property, final String environment) {
        try {
            final int parsedPort = Integer.parseInt(port);
            if (parsedPort < 1024 || parsedPort > 65535) {
                throw new IllegalArgumentException("Configured E2E server port must be between 1024 and 65535: "
                        + property + " / " + environment + "=" + port);
            }

            return parsedPort;
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("Configured E2E server port must be a number: "
                    + property + " / " + environment + "=" + port, e);
        }
    }

    private static Path configuredJar() {
        return Configuration.configured("e2e.app.jar", "E2E_APP_JAR")
                .map(Path::of)
                .orElseThrow(() -> new IllegalStateException("Set e2e.app.jar or E2E_APP_JAR to the packaged application jar"));
    }

    private static int allocatePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to allocate a free local port", e);
        }
    }

    private static String javaExecutable() {
        return Path.of(System.getProperty("java.home"), "bin", "java").toString();
    }
}
