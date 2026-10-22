package com.canteen;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * CanteenUI.java - Java Swing User Interface.
 *
 * This class builds the main application window with sections for:
 * - Managing food items (add, view, update availability)
 * - Placing orders
 * - Viewing orders and updating order status
 *
 * Uses standard Swing components: JFrame, JPanel, JLabel, JTextField,
 * JComboBox, JButton, JTable, JScrollPane, JOptionPane.
 *
 * Layout: JTabbedPane with three tabs for clean organization.
 * All buttons perform their intended actions via DAO classes.
 */
public class CanteenUI extends JFrame {

    // DAO instances for database operations
    private final FoodItemDAO foodItemDAO = new FoodItemDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    // Tables and their models (need class-level access for refreshing)
    private DefaultTableModel foodTableModel;
    private JTable foodTable;

    private DefaultTableModel orderTableModel;
    private JTable orderTable;

    // Order form components (need access for refreshing dropdown)
    private JComboBox<String> orderItemCombo;
    private List<String[]> availableItemsCache;

    public CanteenUI() {
        // ---- Frame Setup ----
        setTitle("College Canteen Order Management System");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen

        // ---- Tabbed Pane ----
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Food Items", createFoodItemsPanel());
        tabbedPane.addTab("Place Order", createPlaceOrderPanel());
        tabbedPane.addTab("View Orders", createViewOrdersPanel());

        // Refresh data when tabs are switched
        tabbedPane.addChangeListener(e -> {
            int index = tabbedPane.getSelectedIndex();
            if (index == 0) refreshFoodTable();
            if (index == 1) refreshOrderItemCombo();
            if (index == 2) refreshOrderTable();
        });

        add(tabbedPane);

        // Load initial data
        refreshFoodTable();

        setVisible(true);
    }

    // ================================================================
    // TAB 1: FOOD ITEMS PANEL
    // ================================================================

    private JPanel createFoodItemsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ---- Top: Add Food Item Form ----
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add Food Item"));

        JLabel nameLabel = new JLabel("Item Name:");
        JTextField nameField = new JTextField();

        JLabel priceLabel = new JLabel("Price (₹):");
        JTextField priceField = new JTextField();

        JLabel availLabel = new JLabel("Available:");
        JComboBox<String> availCombo = new JComboBox<>(new String[]{"Yes", "No"});

        JButton addButton = new JButton("Add Food Item");

        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(priceLabel);
        formPanel.add(priceField);
        formPanel.add(availLabel);
        formPanel.add(availCombo);
        formPanel.add(new JLabel()); // spacer
        formPanel.add(addButton);

        // ---- Center: Food Items Table ----
        String[] foodColumns = {"Item ID", "Item Name", "Price (₹)", "Available"};
        foodTableModel = new DefaultTableModel(foodColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        foodTable = new JTable(foodTableModel);
        JScrollPane tableScroll = new JScrollPane(foodTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Menu Items"));

        // ---- Bottom: Update Availability ----
        JPanel updatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        updatePanel.setBorder(BorderFactory.createTitledBorder("Update Availability"));

        JLabel updateIdLabel = new JLabel("Item ID:");
        JTextField updateIdField = new JTextField(6);
        JLabel updateAvailLabel = new JLabel("Set Available:");
        JComboBox<String> updateAvailCombo = new JComboBox<>(new String[]{"Yes", "No"});
        JButton updateButton = new JButton("Update");

        updatePanel.add(updateIdLabel);
        updatePanel.add(updateIdField);
        updatePanel.add(updateAvailLabel);
        updatePanel.add(updateAvailCombo);
        updatePanel.add(updateButton);

        // Auto-fill Item ID when a row is selected in the table
        foodTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = foodTable.getSelectedRow();
                if (selectedRow >= 0) {
                    updateIdField.setText((String) foodTableModel.getValueAt(selectedRow, 0));
                }
            }
        });

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(tableScroll, BorderLayout.CENTER);
        panel.add(updatePanel, BorderLayout.SOUTH);

        // ---- Button Actions ----

        // Add Food Item button
        addButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String priceText = priceField.getText().trim();
            boolean avail = "Yes".equals(availCombo.getSelectedItem());

            // Validation: name must not be empty
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Item name cannot be empty.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Validation: price must be a valid non-negative number
            BigDecimal price;
            try {
                price = new BigDecimal(priceText);
                if (price.compareTo(BigDecimal.ZERO) < 0) {
                    JOptionPane.showMessageDialog(this,
                            "Price must not be negative.",
                            "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid numeric price.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Attempt to add
            boolean success = foodItemDAO.addFoodItem(name, price, avail);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Food item '" + name + "' added successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                nameField.setText("");
                priceField.setText("");
                availCombo.setSelectedIndex(0);
                refreshFoodTable();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to add food item. Check the console for details.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Update Availability button
        updateButton.addActionListener(e -> {
            String idText = updateIdField.getText().trim();

            if (idText.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter an Item ID or select a row from the table.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int itemId;
            try {
                itemId = Integer.parseInt(idText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Item ID must be a valid integer.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean avail = "Yes".equals(updateAvailCombo.getSelectedItem());
            boolean success = foodItemDAO.updateAvailability(itemId, avail);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Availability updated successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                updateIdField.setText("");
                refreshFoodTable();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to update. Check that the Item ID exists.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    // ================================================================
    // TAB 2: PLACE ORDER PANEL
    // ================================================================

    private JPanel createPlaceOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createTitledBorder("Place New Order"));

        JLabel itemLabel = new JLabel("Select Food Item:");
        orderItemCombo = new JComboBox<>();
        refreshOrderItemCombo(); // Load available items

        JLabel qtyLabel = new JLabel("Quantity:");
        JTextField qtyField = new JTextField();

        JLabel totalLabel = new JLabel("Estimated Total (₹):");
        JTextField totalField = new JTextField();
        totalField.setEditable(false);
        totalField.setBackground(Color.LIGHT_GRAY);

        JButton placeButton = new JButton("Place Order");

        formPanel.add(itemLabel);
        formPanel.add(orderItemCombo);
        formPanel.add(qtyLabel);
        formPanel.add(qtyField);
        formPanel.add(totalLabel);
        formPanel.add(totalField);
        formPanel.add(new JLabel()); // spacer
        formPanel.add(placeButton);

        // ---- Info Panel ----
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Instructions"));
        JTextArea infoArea = new JTextArea(
                "1. Select an available food item from the dropdown.\n" +
                "2. Enter the quantity (must be a positive integer).\n" +
                "3. The estimated total will be calculated automatically.\n" +
                "4. Click 'Place Order' to save the order.\n\n" +
                "Note: The actual total is calculated using the price\n" +
                "stored in the database, not the displayed price."
        );
        infoArea.setEditable(false);
        infoArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        infoArea.setBackground(panel.getBackground());
        infoPanel.add(infoArea, BorderLayout.CENTER);

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(infoPanel, BorderLayout.CENTER);

        // ---- Auto-calculate estimated total when quantity changes ----
        qtyField.addActionListener(e -> calculateEstimatedTotal(qtyField, totalField));
        qtyField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                calculateEstimatedTotal(qtyField, totalField);
            }
        });

        // ---- Place Order Action ----
        placeButton.addActionListener(e -> {
            // Validate item selection
            if (orderItemCombo.getSelectedIndex() < 0 || availableItemsCache == null
                    || availableItemsCache.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No food item selected. Add available items first.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Validate quantity
            String qtyText = qtyField.getText().trim();
            if (qtyText.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a quantity.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int quantity;
            try {
                quantity = Integer.parseInt(qtyText);
                if (quantity < 1) {
                    JOptionPane.showMessageDialog(this,
                            "Quantity must be at least 1.",
                            "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Quantity must be a valid positive integer.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Get the selected item ID
            int selectedIndex = orderItemCombo.getSelectedIndex();
            int itemId = Integer.parseInt(availableItemsCache.get(selectedIndex)[0]);

            // Place the order via DAO
            try {
                boolean success = orderDAO.placeOrder(itemId, quantity);
                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Order placed successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    qtyField.setText("");
                    totalField.setText("");
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to place order. Check the console.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        "Order Error", JOptionPane.WARNING_MESSAGE);
            }
        });

        return panel;
    }

    /**
     * Calculates and displays the estimated total in the order form.
     */
    private void calculateEstimatedTotal(JTextField qtyField, JTextField totalField) {
        if (orderItemCombo.getSelectedIndex() < 0 || availableItemsCache == null
                || availableItemsCache.isEmpty()) {
            totalField.setText("");
            return;
        }

        String qtyText = qtyField.getText().trim();
        if (qtyText.isEmpty()) {
            totalField.setText("");
            return;
        }

        try {
            int qty = Integer.parseInt(qtyText);
            if (qty < 1) {
                totalField.setText("");
                return;
            }
            int selectedIndex = orderItemCombo.getSelectedIndex();
            BigDecimal price = new BigDecimal(availableItemsCache.get(selectedIndex)[2]);
            BigDecimal total = price.multiply(BigDecimal.valueOf(qty));
            totalField.setText(total.toString());
        } catch (NumberFormatException ex) {
            totalField.setText("");
        }
    }

    // ================================================================
    // TAB 3: VIEW ORDERS PANEL
    // ================================================================

    private JPanel createViewOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ---- Orders Table ----
        String[] orderColumns = {"Order ID", "Food Item", "Quantity",
                                  "Total Amount (₹)", "Status"};
        orderTableModel = new DefaultTableModel(orderColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        orderTable = new JTable(orderTableModel);
        JScrollPane tableScroll = new JScrollPane(orderTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("All Orders"));

        // ---- Bottom: Update Status ----
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        statusPanel.setBorder(BorderFactory.createTitledBorder("Update Order Status"));

        JLabel orderIdLabel = new JLabel("Order ID:");
        JTextField orderIdField = new JTextField(6);
        JLabel statusLabel = new JLabel("New Status:");
        JComboBox<String> statusCombo = new JComboBox<>(
                new String[]{"Pending", "Preparing", "Completed"});
        JButton updateStatusButton = new JButton("Update Status");
        JButton refreshButton = new JButton("Refresh");

        statusPanel.add(orderIdLabel);
        statusPanel.add(orderIdField);
        statusPanel.add(statusLabel);
        statusPanel.add(statusCombo);
        statusPanel.add(updateStatusButton);
        statusPanel.add(refreshButton);

        // Auto-fill Order ID when a row is selected
        orderTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = orderTable.getSelectedRow();
                if (selectedRow >= 0) {
                    orderIdField.setText(
                            (String) orderTableModel.getValueAt(selectedRow, 0));
                }
            }
        });

        panel.add(tableScroll, BorderLayout.CENTER);
        panel.add(statusPanel, BorderLayout.SOUTH);

        // ---- Update Status Action ----
        updateStatusButton.addActionListener(e -> {
            String idText = orderIdField.getText().trim();

            if (idText.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter an Order ID or select a row from the table.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int orderId;
            try {
                orderId = Integer.parseInt(idText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Order ID must be a valid integer.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String newStatus = (String) statusCombo.getSelectedItem();

            try {
                boolean success = orderDAO.updateOrderStatus(orderId, newStatus);
                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Order #" + orderId + " status updated to '" + newStatus + "'.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    orderIdField.setText("");
                    refreshOrderTable();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to update. Check that the Order ID exists.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        "Status Error", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Refresh button
        refreshButton.addActionListener(e -> refreshOrderTable());

        // Load orders initially
        refreshOrderTable();

        return panel;
    }

    // ================================================================
    // DATA REFRESH METHODS
    // ================================================================

    /** Refreshes the food items table with current data from the database. */
    private void refreshFoodTable() {
        foodTableModel.setRowCount(0); // Clear existing rows
        List<String[]> items = foodItemDAO.getAllFoodItems();
        for (String[] row : items) {
            foodTableModel.addRow(row);
        }
    }

    /** Refreshes the order item dropdown with currently available food items. */
    private void refreshOrderItemCombo() {
        orderItemCombo.removeAllItems();
        availableItemsCache = foodItemDAO.getAvailableFoodItems();
        for (String[] item : availableItemsCache) {
            // Display format: "ItemName - ₹Price"
            orderItemCombo.addItem(item[1] + " - \u20B9" + item[2]);
        }
    }

    /** Refreshes the orders table with current data from the database. */
    private void refreshOrderTable() {
        orderTableModel.setRowCount(0);
        List<String[]> orders = orderDAO.getAllOrders();
        for (String[] row : orders) {
            orderTableModel.addRow(row);
        }
    }
}
