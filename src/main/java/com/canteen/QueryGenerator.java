package com.canteen;

import java.util.Set;

/**
 * QueryGenerator.java - Programmatic SQL Statement Construction.
 *
 * This is a CRITICAL ASSIGNMENT REQUIREMENT. Instead of hardcoding every SQL
 * query throughout the application, we construct SQL statements through
 * dedicated Java methods in this class.
 *
 * DESIGN DECISIONS:
 * - All methods return SQL strings (some with '?' placeholders for PreparedStatement).
 * - Table and column names are validated against an allowlist before interpolation
 *   to prevent SQL injection in identifiers.
 * - User-supplied VALUES are never concatenated into SQL; they use '?' placeholders.
 * - DDL (Data Definition Language): CREATE TABLE statements that define structure.
 * - DML (Data Manipulation Language): INSERT, SELECT, UPDATE statements that
 *   manipulate data within existing tables.
 *
 * This class ONLY generates SQL strings. Execution is handled by DAO classes.
 */
public class QueryGenerator {

    // ====================================================================
    // ALLOWLIST: Only these table and column names may be used in queries.
    // This prevents arbitrary identifier injection.
    // ====================================================================
    private static final Set<String> ALLOWED_TABLES = Set.of(
            "food_items", "orders"
    );

    private static final Set<String> ALLOWED_COLUMNS = Set.of(
            "item_id", "item_name", "price", "available",
            "order_id", "quantity", "total_amount", "order_status"
    );

    // ====================================================================
    // VALIDATION HELPERS
    // ====================================================================

    /**
     * Validates that a table name is in the allowlist.
     * Throws IllegalArgumentException if not allowed.
     */
    private static void validateTable(String tableName) {
        if (!ALLOWED_TABLES.contains(tableName)) {
            throw new IllegalArgumentException(
                    "Invalid table name: '" + tableName + "'. " +
                    "Allowed tables: " + ALLOWED_TABLES);
        }
    }

    /**
     * Validates that a column name is in the allowlist.
     * Throws IllegalArgumentException if not allowed.
     */
    private static void validateColumn(String columnName) {
        if (!ALLOWED_COLUMNS.contains(columnName)) {
            throw new IllegalArgumentException(
                    "Invalid column name: '" + columnName + "'. " +
                    "Allowed columns: " + ALLOWED_COLUMNS);
        }
    }

    // ====================================================================
    // DDL QUERIES (Data Definition Language)
    // These define the database structure (CREATE TABLE).
    // ====================================================================

    /**
     * DDL: Generates the CREATE TABLE statement for the food_items table.
     *
     * Creates columns: item_id (PK, auto-increment), item_name, price, available.
     * Includes a CHECK constraint to ensure price >= 0.
     * Uses InnoDB engine for foreign-key support.
     *
     * @return CREATE TABLE SQL for food_items
     */
    public static String createFoodItemsTable() {
        return "CREATE TABLE IF NOT EXISTS food_items (" +
               "  item_id    INT            PRIMARY KEY AUTO_INCREMENT," +
               "  item_name  VARCHAR(50)    NOT NULL," +
               "  price      DECIMAL(8,2)   NOT NULL," +
               "  available  BOOLEAN        NOT NULL DEFAULT TRUE," +
               "  CONSTRAINT chk_price_non_negative CHECK (price >= 0)" +
               ") ENGINE=InnoDB";
    }

    /**
     * DDL: Generates the CREATE TABLE statement for the orders table.
     *
     * Creates columns: order_id (PK), item_id (FK → food_items), quantity,
     * total_amount, order_status.
     * Includes CHECK constraints and a FOREIGN KEY.
     * Uses InnoDB engine for foreign-key support.
     *
     * @return CREATE TABLE SQL for orders
     */
    public static String createOrdersTable() {
        return "CREATE TABLE IF NOT EXISTS orders (" +
               "  order_id     INT            PRIMARY KEY AUTO_INCREMENT," +
               "  item_id      INT            NOT NULL," +
               "  quantity     INT            NOT NULL," +
               "  total_amount DECIMAL(10,2)  NOT NULL," +
               "  order_status VARCHAR(20)    NOT NULL DEFAULT 'Pending'," +
               "  CONSTRAINT fk_order_item FOREIGN KEY (item_id) " +
               "    REFERENCES food_items(item_id)," +
               "  CONSTRAINT chk_quantity_positive CHECK (quantity >= 1)," +
               "  CONSTRAINT chk_total_non_negative CHECK (total_amount >= 0)" +
               ") ENGINE=InnoDB";
    }

    // ====================================================================
    // DML QUERIES (Data Manipulation Language)
    // These manipulate data: INSERT, SELECT, UPDATE.
    // ====================================================================

    // ---- FOOD ITEM DML ----

    /**
     * DML: Generates an INSERT statement for a new food item.
     *
     * Uses '?' placeholders for: item_name, price, available.
     * These must be set via PreparedStatement.setXxx() before execution.
     *
     * @return INSERT SQL with placeholders
     */
    public static String insertFoodItem() {
        return "INSERT INTO food_items (item_name, price, available) VALUES (?, ?, ?)";
    }

    /**
     * DML: Generates a SELECT statement to retrieve ALL food items.
     *
     * @return SELECT SQL for all food items
     */
    public static String selectAllFoodItems() {
        return "SELECT item_id, item_name, price, available FROM food_items";
    }

    /**
     * DML: Generates a SELECT statement to retrieve only AVAILABLE food items.
     * Used when placing an order (only available items can be ordered).
     *
     * @return SELECT SQL for available food items
     */
    public static String selectAvailableFoodItems() {
        return "SELECT item_id, item_name, price, available " +
               "FROM food_items WHERE available = TRUE";
    }

    /**
     * DML: Generates an UPDATE statement to change a food item's availability.
     *
     * Uses '?' placeholders for: available (new value), item_id (which item).
     *
     * @return UPDATE SQL with placeholders
     */
    public static String updateFoodItemAvailability() {
        return "UPDATE food_items SET available = ? WHERE item_id = ?";
    }

    /**
     * DML: Generates a SELECT statement to retrieve a single food item by ID.
     *
     * Used internally to fetch the current price when placing an order.
     * Placeholder: item_id.
     *
     * @return SELECT SQL with placeholder
     */
    public static String selectFoodItemById() {
        return "SELECT item_id, item_name, price, available " +
               "FROM food_items WHERE item_id = ?";
    }

    // ---- ORDER DML ----

    /**
     * DML: Generates an INSERT statement for a new order.
     *
     * Uses '?' placeholders for: item_id, quantity, total_amount.
     * order_status defaults to 'Pending' (set by the database).
     *
     * @return INSERT SQL with placeholders
     */
    public static String insertOrder() {
        return "INSERT INTO orders (item_id, quantity, total_amount) VALUES (?, ?, ?)";
    }

    /**
     * DML: Generates a SELECT statement to retrieve all orders
     * using a JOIN with food_items to include the food item name.
     *
     * This JOIN is important because the orders table stores only item_id,
     * but we want to display the item_name to the user.
     *
     * @return SELECT SQL with JOIN
     */
    public static String selectAllOrdersWithItemName() {
        return "SELECT o.order_id, f.item_name, o.quantity, " +
               "       o.total_amount, o.order_status " +
               "FROM orders o " +
               "JOIN food_items f ON o.item_id = f.item_id " +
               "ORDER BY o.order_id DESC";
    }

    /**
     * DML: Generates an UPDATE statement to change an order's status.
     *
     * Uses '?' placeholders for: order_status (new value), order_id (which order).
     *
     * @return UPDATE SQL with placeholders
     */
    public static String updateOrderStatus() {
        return "UPDATE orders SET order_status = ? WHERE order_id = ?";
    }

    // ====================================================================
    // GENERIC HELPER (demonstrating parameterized query generation)
    // ====================================================================

    /**
     * DML: Generates a generic SELECT * statement for an allowed table.
     *
     * The table name is validated against the allowlist before use.
     * This is the approach shown in the assignment example.
     *
     * @param tableName the name of the table (must be in allowlist)
     * @return SELECT * SQL
     */
    public static String selectAll(String tableName) {
        validateTable(tableName);
        return "SELECT * FROM " + tableName;
    }

    /**
     * DML: Generates a generic UPDATE for a single column by primary key.
     *
     * Both the table name, the column to update, and the key column
     * are validated against the allowlist.
     *
     * @param tableName   the table to update
     * @param columnName  the column to set
     * @param keyColumn   the primary key column for the WHERE clause
     * @return UPDATE SQL with two '?' placeholders (new value, key value)
     */
    public static String updateColumn(String tableName, String columnName, String keyColumn) {
        validateTable(tableName);
        validateColumn(columnName);
        validateColumn(keyColumn);
        return "UPDATE " + tableName + " SET " + columnName + " = ? WHERE " + keyColumn + " = ?";
    }
}
