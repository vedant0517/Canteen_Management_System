package com.canteen;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * FoodItemDAO.java - Data Access Object for food_items table.
 *
 * This class handles all database operations related to food items:
 * - Adding a new food item (INSERT)
 * - Retrieving all food items (SELECT)
 * - Retrieving only available food items (SELECT with WHERE)
 * - Updating food item availability (UPDATE)
 *
 * All SQL is obtained from QueryGenerator (never hardcoded here).
 * User-supplied values are set via PreparedStatement placeholders.
 * Resources are closed using try-with-resources.
 */
public class FoodItemDAO {

    /**
     * Adds a new food item to the database.
     *
     * @param itemName  the name of the food item
     * @param price     the price (must be non-negative)
     * @param available whether the item is currently available
     * @return true if the item was inserted successfully
     */
    public boolean addFoodItem(String itemName, BigDecimal price, boolean available) {
        // Get the INSERT SQL from QueryGenerator
        String sql = QueryGenerator.insertFoodItem();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set placeholder values (never concatenated into SQL)
            pstmt.setString(1, itemName);
            pstmt.setBigDecimal(2, price);
            pstmt.setBoolean(3, available);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error adding food item: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all food items from the database.
     *
     * @return a list of String arrays, each containing:
     *         [item_id, item_name, price, available]
     */
    public List<String[]> getAllFoodItems() {
        String sql = QueryGenerator.selectAllFoodItems();
        List<String[]> items = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String[] row = new String[4];
                row[0] = String.valueOf(rs.getInt("item_id"));
                row[1] = rs.getString("item_name");
                row[2] = rs.getBigDecimal("price").toString();
                row[3] = rs.getBoolean("available") ? "Yes" : "No";
                items.add(row);
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving food items: " + e.getMessage());
        }

        return items;
    }

    /**
     * Retrieves only available food items (for the order form dropdown).
     *
     * @return a list of String arrays: [item_id, item_name, price]
     */
    public List<String[]> getAvailableFoodItems() {
        String sql = QueryGenerator.selectAvailableFoodItems();
        List<String[]> items = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String[] row = new String[3];
                row[0] = String.valueOf(rs.getInt("item_id"));
                row[1] = rs.getString("item_name");
                row[2] = rs.getBigDecimal("price").toString();
                items.add(row);
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving available food items: " + e.getMessage());
        }

        return items;
    }

    /**
     * Updates the availability of a food item.
     *
     * @param itemId    the ID of the food item to update
     * @param available the new availability status
     * @return true if the update was successful
     */
    public boolean updateAvailability(int itemId, boolean available) {
        String sql = QueryGenerator.updateFoodItemAvailability();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, available);
            pstmt.setInt(2, itemId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error updating availability: " + e.getMessage());
            return false;
        }
    }

    /**
     * Fetches the current price of a food item by its ID.
     * Used internally when placing an order (we trust the DB price, not the UI).
     *
     * @param itemId the food item ID
     * @return the price as BigDecimal, or null if not found
     */
    public BigDecimal getItemPrice(int itemId) {
        String sql = QueryGenerator.selectFoodItemById();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, itemId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("price");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching item price: " + e.getMessage());
        }

        return null;
    }

    /**
     * Checks whether a food item is available.
     *
     * @param itemId the food item ID
     * @return true if available, false otherwise
     */
    public boolean isItemAvailable(int itemId) {
        String sql = QueryGenerator.selectFoodItemById();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, itemId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("available");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error checking item availability: " + e.getMessage());
        }

        return false;
    }
}
