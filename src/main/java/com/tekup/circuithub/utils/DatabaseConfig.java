package com.tekup.circuithub.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.stream.Collectors;

public class DatabaseConfig {
    private static final String URL = "jdbc:postgresql://localhost:5432/circuithub";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            // Create users table
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        id VARCHAR(36) PRIMARY KEY,
                        full_name VARCHAR(100) NOT NULL,
                        email VARCHAR(100) UNIQUE NOT NULL,
                        password_hash VARCHAR(255) NOT NULL,
                        join_date DATE NOT NULL
                    )
                """);
            }

            // Create products table
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS products (
                        id VARCHAR(36) PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        category VARCHAR(50) NOT NULL,
                        price DECIMAL(10, 2) NOT NULL,
                        description TEXT,
                        image_url VARCHAR(1000),
                        stock INT NOT NULL
                    )
                """);
            }

            // Create product_specs table
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS product_specs (
                        id SERIAL PRIMARY KEY,
                        product_id VARCHAR(36) NOT NULL,
                        spec_key VARCHAR(100) NOT NULL,
                        spec_value VARCHAR(500) NOT NULL,
                        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
                        UNIQUE (product_id, spec_key)
                    )
                """);
            }

            // Create orders table
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS orders (
                        id VARCHAR(50) PRIMARY KEY,
                        user_id VARCHAR(36) NOT NULL,
                        total DECIMAL(10, 2) NOT NULL,
                        order_date DATE NOT NULL,
                        status VARCHAR(50) NOT NULL,
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                    )
                """);
            }

            // Create order_items table
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS order_items (
                        id SERIAL PRIMARY KEY,
                        order_id VARCHAR(50) NOT NULL,
                        product_id VARCHAR(36) NOT NULL,
                        quantity INT NOT NULL,
                        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
                    )
                """);
            }

            runSeed(conn);
            System.out.println("✓ Database initialized successfully");
        } catch (SQLException e) {
            System.err.println("✗ Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runSeed(Connection conn) {
        try (var in = DatabaseConfig.class.getResourceAsStream("/com/tekup/circuithub/data/seed.sql")) {
            if (in == null) { System.err.println("✗ seed.sql not found"); return; }
            String sql = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
            for (String statement : sql.split(";")) {
                // Strip leading comment lines — a block may start with "-- ..." before the SQL keyword
                String trimmed = statement.lines()
                        .filter(line -> !line.strip().startsWith("--"))
                        .collect(Collectors.joining("\n"))
                        .strip();
                if (!trimmed.isEmpty()) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(trimmed);
                    }
                }
            }
            System.out.println("✓ Seed applied");
        } catch (Exception e) {
            System.err.println("✗ Seed failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

