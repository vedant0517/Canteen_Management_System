# College Canteen Order Management System

A Java Swing + JDBC + MySQL desktop application for managing food items and customer orders in a college canteen. Built as a DBMS mini-project.

## Technology Stack

| Technology    | Purpose                    |
|--------------|----------------------------|
| Java 17+     | Programming language       |
| Java Swing   | Desktop GUI                |
| MySQL 8.0    | Database                   |
| JDBC         | Database connectivity      |
| Maven        | Build and dependency tool  |

## Prerequisites

1. **Java JDK 17** or later installed.
2. **MySQL Server 8.0** installed and running.
3. **Apache Maven 3.6+** installed.

## Setup Instructions

### Step 1: Create the MySQL Database

Open MySQL command line or MySQL Workbench and run:

```sql
CREATE DATABASE IF NOT EXISTS canteen_db;
```

Alternatively, run the full schema file:

```bash
mysql -u root -p < database/schema.sql
```

### Step 2: Configure Database Credentials

1. Copy the example config file:
   ```bash
   cp src/main/resources/db.properties.example src/main/resources/db.properties
   ```

2. Edit `src/main/resources/db.properties` with your MySQL credentials:
   ```properties
   db.url=jdbc:mysql://localhost:3306/canteen_db
   db.username=root
   db.password=your_password_here
   ```

> **Note:** `db.properties` is in `.gitignore` and will not be committed.

### Step 3: Compile

```bash
mvn compile
```

### Step 4: Run

```bash
mvn exec:java
```

The application will:
1. Connect to MySQL.
2. Create the `food_items` and `orders` tables (if they don't exist).
3. Open the Swing user interface.

## Features

### 1. Manage Food Items
- Add new food items with name, price, and availability.
- View all food items in a table.
- Update food item availability (Yes/No).

### 2. Place Orders
- Select from available food items.
- Enter quantity.
- Total amount is calculated automatically (price × quantity).
- Price is fetched from the database at order time (not trusted from UI).

### 3. View Orders
- All orders displayed in a table with food item names (via SQL JOIN).
- Shows: Order ID, Food Item Name, Quantity, Total Amount, Status.

### 4. Update Order Status
- Select an order and change status to: Pending, Preparing, or Completed.

## Project Structure

```
src/main/java/com/canteen/
├── Main.java                 # Application entry point
├── DatabaseConnection.java   # JDBC connection management
├── QueryGenerator.java       # Programmatic SQL construction
├── DatabaseInitializer.java  # Table creation at startup
├── FoodItemDAO.java          # Food item CRUD operations
├── OrderDAO.java             # Order operations
└── CanteenUI.java            # Swing user interface

src/main/resources/
├── db.properties             # Database credentials (not committed)
└── db.properties.example     # Template for credentials

database/
└── schema.sql                # DDL + sample data SQL

pom.xml                       # Maven configuration
README.md                     # This file
```

## Database Schema

### ER Diagram

```
┌─────────────────┐         ┌─────────────────┐
│   food_items    │         │     orders      │
├─────────────────┤         ├─────────────────┤
│ *item_id  (PK)  │◄────────│  item_id   (FK) │
│  item_name      │    1:N  │ *order_id  (PK) │
│  price          │         │  quantity       │
│  available      │         │  total_amount   │
└─────────────────┘         │  order_status   │
                            └─────────────────┘
```

**Relationship:** One food item can appear in many orders (1:N).

### Tables

**food_items:**
| Column    | Type          | Constraints                   |
|-----------|---------------|-------------------------------|
| item_id   | INT           | PK, AUTO_INCREMENT            |
| item_name | VARCHAR(50)   | NOT NULL                      |
| price     | DECIMAL(8,2)  | NOT NULL, CHECK (>= 0)        |
| available | BOOLEAN       | NOT NULL, DEFAULT TRUE         |

**orders:**
| Column       | Type          | Constraints                         |
|--------------|---------------|-------------------------------------|
| order_id     | INT           | PK, AUTO_INCREMENT                  |
| item_id      | INT           | NOT NULL, FK → food_items(item_id)  |
| quantity     | INT           | NOT NULL, CHECK (>= 1)              |
| total_amount | DECIMAL(10,2) | NOT NULL, CHECK (>= 0)              |
| order_status | VARCHAR(20)   | NOT NULL, DEFAULT 'Pending'          |

## QueryGenerator Overview

The `QueryGenerator` class constructs SQL statements programmatically:

- **DDL methods:** `createFoodItemsTable()`, `createOrdersTable()`
- **DML methods:** `insertFoodItem()`, `selectAllFoodItems()`, `selectAvailableFoodItems()`, `updateFoodItemAvailability()`, `insertOrder()`, `selectAllOrdersWithItemName()`, `updateOrderStatus()`
- **Generic helpers:** `selectAll(tableName)`, `updateColumn(tableName, column, keyColumn)`

All identifiers are validated against an allowlist. User values use `?` placeholders for `PreparedStatement`.

## Troubleshooting

| Problem                          | Solution                                      |
|----------------------------------|-----------------------------------------------|
| Connection refused               | Ensure MySQL is running on port 3306          |
| Access denied                    | Check username/password in db.properties      |
| Unknown database 'canteen_db'    | Run: `CREATE DATABASE canteen_db;` in MySQL   |
| Class not found (JDBC driver)    | Run `mvn compile` to download dependencies    |
