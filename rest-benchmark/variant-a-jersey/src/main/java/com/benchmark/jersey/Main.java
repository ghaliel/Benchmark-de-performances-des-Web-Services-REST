package com.benchmark.jersey;

import com.benchmark.jersey.config.JaxRsApplication;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final String BASE_URI = "http://0.0.0.0:8080/";
    private static EntityManagerFactory emf;

    public static void main(String[] args) throws IOException {
        // Initialize EntityManagerFactory with HikariCP configuration
        Map<String, String> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.url",
                System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/benchmark"));
        properties.put("jakarta.persistence.jdbc.user",
                System.getenv().getOrDefault("DB_USER", "benchmark"));
        properties.put("jakarta.persistence.jdbc.password",
                System.getenv().getOrDefault("DB_PASSWORD", "benchmark"));

        // HikariCP settings
        properties.put("hibernate.hikari.maximumPoolSize", "20");
        properties.put("hibernate.hikari.minimumIdle", "10");
        properties.put("hibernate.hikari.connectionTimeout", "30000");

        emf = Persistence.createEntityManagerFactory("benchmark-pu", properties);

        // Create and start server
        final ResourceConfig rc = new JaxRsApplication(emf);
        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);

        LOGGER.info(String.format("Jersey app started with endpoints available at %s", BASE_URI));
        LOGGER.info("Press Ctrl+C to stop...");

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Stopping server...");
            server.shutdownNow();
            if (emf != null && emf.isOpen()) {
                emf.close();
            }
            LOGGER.info("Server stopped.");
        }));

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }
}