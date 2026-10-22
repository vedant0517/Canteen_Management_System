-- ============================================================
-- College Canteen Order Management System
-- Database Schema (DDL) + Sample Data (DML)
-- ============================================================

-- Create the database
CREATE DATABASE IF NOT EXISTS canteen_db;
USE canteen_db;

-- ============================================================
-- DDL: Table 1 - food_items
-- Stores the canteen menu items with name, price, availability.
-- ============================================================
CREATE TABLE IF NOT EXISTS food_items (
    item_id    INT            PRIMARY KEY AUTO_INCREMENT,
    item_name  VARCHAR(50)    NOT NULL,
    price      DECIMAL(8,2)   NOT NULL,
    available  BOOLEAN        NOT NULL DEFAULT TRUE,

    -- Constraint: price must not be negative
    CONSTRAINT chk_price_non_negative CHECK (price >= 0)
) ENGINE=InnoDB;

-- ============================================================
-- DDL: Table 2 - orders
-- Stores customer orders with item reference, quantity, total.
-- ============================================================
CREATE TABLE IF NOT EXISTS orders (
    order_id     INT            PRIMARY KEY AUTO_INCREMENT,
    item_id      INT            NOT NULL,
    quantity     INT            NOT NULL,
    total_amount DECIMAL(10,2)  NOT NULL,
    order_status VARCHAR(20)    NOT NULL DEFAULT 'Pending',

    -- Foreign key: each order references an existing food item
    CONSTRAINT fk_order_item FOREIGN KEY (item_id)
        REFERENCES food_items(item_id),

    -- Constraint: quantity must be positive (>= 1)
    CONSTRAINT chk_quantity_positive CHECK (quantity >= 1),

    -- Constraint: total_amount must not be negative
    CONSTRAINT chk_total_non_negative CHECK (total_amount >= 0)
) ENGINE=InnoDB;

-- ============================================================
-- DML: Sample food items (initial menu data)
-- ============================================================
INSERT INTO food_items (item_name, price, available) VALUES 
('Vada Pav', 20.00, TRUE),
('Samosa (2 pcs)', 30.00, TRUE),
('Masala Dosa', 50.00, TRUE),
('Veg Grilled Sandwich', 45.00, TRUE),
('Cheese Maggi', 40.00, TRUE),
('Cutting Chai', 10.00, TRUE),
('Filter Coffee', 25.00, TRUE),
('Cold Coffee', 35.00, TRUE),
('Veg Pulao', 60.00, TRUE),
('Paneer Roll', 70.00, FALSE);

