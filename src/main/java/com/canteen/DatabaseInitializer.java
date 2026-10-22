package com.canteen;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DatabaseInitializer.java - Creates required database tables.
 *
 * Called once at application startup. Uses QueryGenerator to obtain
 * the DDL (CREATE TABLE) statements and executes them.
 * IF NOT EXISTS ensures tables are not recreated if they already exist.
 */
public class DatabaseInitializer {

    /**
     * Creates the food_items and orders tables if they do not already exist.
     * Uses try-with-resources to ensure Connection and Statement are closed.
     */
    public static void initializeTables() {
        // Get DDL statements from QueryGenerator
        String createFoodItems = QueryGenerator.createFoodItemsTable();
        String createOrders    = QueryGenerator.createOrdersTable();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Create food_items table first (orders depends on it via FK)
            stmt.execute(createFoodItems);
            System.out.println("Table 'food_items' is ready.");

            // Create orders table (has FK to food_items)
            stmt.execute(createOrders);
            System.out.println("Table 'orders' is ready.");

        } catch (SQLException e) {
            System.err.println("Error initializing database tables: " + e.getMessage());
            // Show a user-friendly dialog if Swing is available
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Failed to initialize database tables.\n" +
                    "Please ensure MySQL is running and canteen_db exists.\n\n" +
                    "Error: " + e.getMessage(),
                    "Database Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
}
