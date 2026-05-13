package com.tekup.circuithub.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class DatabaseConfig {
    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    private static final String CREATE_USERS = """
            CREATE TABLE IF NOT EXISTS users (
                id VARCHAR(36) PRIMARY KEY,
                full_name VARCHAR(100) NOT NULL,
                email VARCHAR(100) UNIQUE NOT NULL,
                password_hash VARCHAR(255) NOT NULL,
                join_date DATE NOT NULL,
                role VARCHAR(20) NOT NULL DEFAULT 'USER'
            )
            """;

    private static final String CREATE_PRODUCTS = """
            CREATE TABLE IF NOT EXISTS products (
                id VARCHAR(36) PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                category VARCHAR(50) NOT NULL,
                price DECIMAL(10, 2) NOT NULL,
                description TEXT,
                image_url VARCHAR(1000),
                stock INT NOT NULL,
                image_data BYTEA
            )
            """;

    private static final String CREATE_PRODUCT_SPECS = """
            CREATE TABLE IF NOT EXISTS product_specs (
                id SERIAL PRIMARY KEY,
                product_id VARCHAR(36) NOT NULL,
                spec_key VARCHAR(100) NOT NULL,
                spec_value VARCHAR(500) NOT NULL,
                FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
                UNIQUE (product_id, spec_key)
            )
            """;

    private static final String CREATE_ORDERS = """
            CREATE TABLE IF NOT EXISTS orders (
                id VARCHAR(50) PRIMARY KEY,
                user_id VARCHAR(36) NOT NULL,
                total DECIMAL(10, 2) NOT NULL,
                order_date DATE NOT NULL,
                status VARCHAR(50) NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
            """;

    private static final String CREATE_ORDER_ITEMS = """
            CREATE TABLE IF NOT EXISTS order_items (
                id SERIAL PRIMARY KEY,
                order_id VARCHAR(50) NOT NULL,
                product_id VARCHAR(36) NOT NULL,
                quantity INT NOT NULL,
                FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
            )
            """;

    private static final String[] SCHEMA_STATEMENTS = {
            CREATE_USERS,
            CREATE_PRODUCTS,
            CREATE_PRODUCT_SPECS,
            CREATE_ORDERS,
            CREATE_ORDER_ITEMS,
    };

    static {
        Properties props = new Properties();
        try (var in = DatabaseConfig.class.getResourceAsStream(
                "/com/tekup/circuithub/config.properties")) {
            if (in != null) {
                props.load(in);
            } else {
                System.err.println("✗ config.properties not found, using defaults");
            }
        } catch (Exception e) {
            System.err.println("✗ Failed to load config.properties: " + e.getMessage());
        }
        URL      = props.getProperty("db.url",      "jdbc:postgresql://localhost:5432/circuithub");
        USER     = props.getProperty("db.user",     "postgres");
        PASSWORD = props.getProperty("db.password", "postgres");
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            for (String ddl : SCHEMA_STATEMENTS) {
                stmt.execute(ddl);
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
            if (in == null) {
                System.err.println("✗ seed.sql not found");
                return;
            }
            String sql = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
            try (Statement stmt = conn.createStatement()) {
                for (String statement : splitStatements(sql)) {
                    if (!statement.isBlank()) {
                        stmt.execute(statement);
                    }
                }
            }
            System.out.println("✓ Seed applied");
        } catch (Exception e) {
            System.err.println("✗ Seed failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Split SQL on semicolons that are outside single-quoted string literals and -- comments. */
    private static List<String> splitStatements(String sql) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inString = false;
        boolean inLineComment = false;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (inLineComment) {
                if (c == '\n') {
                    inLineComment = false;
                    current.append(c);
                }
            } else if (c == '-' && !inString && i + 1 < sql.length() && sql.charAt(i + 1) == '-') {
                inLineComment = true;
                i++;
            } else if (c == '\'' && !inString) {
                inString = true;
                current.append(c);
            } else if (c == '\'' && inString) {
                current.append(c);
                if (i + 1 < sql.length() && sql.charAt(i + 1) == '\'') {
                    current.append(sql.charAt(++i));
                } else {
                    inString = false;
                }
            } else if (c == ';' && !inString) {
                String trimmed = current.toString().strip();
                if (!trimmed.isEmpty()) result.add(trimmed);
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        String trimmed = current.toString().strip();
        if (!trimmed.isEmpty()) result.add(trimmed);
        return result;
    }
}
