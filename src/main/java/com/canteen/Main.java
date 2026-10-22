package com.canteen;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main.java - Application entry point.
 *
 * This class starts the College Canteen Order Management System.
 * It initializes the database tables (if they don't exist) and
 * launches the Swing user interface on the Event Dispatch Thread.
 */
public class Main {

    public static void main(String[] args) {
        // Step 1: Initialize database tables
        System.out.println("Initializing database...");
        DatabaseInitializer.initializeTables();
        System.out.println("Database initialization complete.");

        // Step 2: Launch the Swing UI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Use the system look-and-feel for a native appearance
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Fall back to default look-and-feel if system L&F fails
                System.err.println("Could not set system look and feel: " + e.getMessage());
            }
            new CanteenUI();
        });
    }
}
