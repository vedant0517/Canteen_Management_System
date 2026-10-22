package com.canteen;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DatabaseConnection.java - Manages JDBC connectivity to MySQL.
 *
 * Responsibilities:
 * - Reads connection settings from db.properties on the classpath.
 * - Falls back to environment variables if the file is missing.
 * - Provides a single static method to obtain a Connection object.
 * - Displays understandable error messages if the database is unavailable.
 *
 * The caller is responsible for closing the returned Connection
 * (preferably using try-with-resources).
 */
public class DatabaseConnection {

    // Connection settings loaded once at class-load time
    private static final String URL;
    private static final String USERNAME;
    private static final String PASSWORD;

    static {
        // Attempt to load db.properties from the classpath
        Properties props = new Properties();
        try (InputStream input = DatabaseConnection.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (input != null) {
                props.load(input);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not read db.properties file.");
        }

        // Use properties if available; otherwise fall back to environment variables;
        // finally fall back to reasonable defaults.
        URL = props.getProperty("db.url",
                System.getenv().getOrDefault("CANTEEN_DB_URL",
                        "jdbc:mysql://localhost:3306/canteen_db"));

        USERNAME = props.getProperty("db.username",
                System.getenv().getOrDefault("CANTEEN_DB_USER", "root"));

        PASSWORD = props.getProperty("db.password",
                System.getenv().getOrDefault("CANTEEN_DB_PASS", ""));
    }

    /**
     * Opens and returns a new JDBC Connection to the MySQL database.
     *
     * @return a live Connection object
     * @throws SQLException if the connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            return conn;
        } catch (SQLException e) {
            // Provide a helpful error message for common problems
            System.err.println("=== DATABASE CONNECTION ERROR ===");
            System.err.println("Could not connect to the database.");
            System.err.println("URL:  " + URL);
            System.err.println("User: " + USERNAME);
            System.err.println("Check that:");
            System.err.println("  1. MySQL server is running.");
            System.err.println("  2. The database 'canteen_db' exists.");
            System.err.println("  3. Username and password are correct.");
            System.err.println("  4. db.properties is in src/main/resources/.");
            System.err.println("Error: " + e.getMessage());
            System.err.println("=================================");
            throw e; // Re-throw so the caller can handle it
        }
    }
}
