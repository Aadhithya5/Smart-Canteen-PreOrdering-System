-- ============================================================
--  Smart Canteen Pre-Ordering & Digital Queue Management System
--  Database Script - MySQL
-- ============================================================

CREATE DATABASE IF NOT EXISTS smart_canteen;
USE smart_canteen;

-- -------------------------------------------------------
-- Table: users
-- Stores student/admin account information
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id     INT AUTO_INCREMENT PRIMARY KEY,
    full_name   VARCHAR(100)        NOT NULL,
    email       VARCHAR(100)        NOT NULL UNIQUE,
    password    VARCHAR(255)        NOT NULL,  -- Store hashed password in production
    phone       VARCHAR(15)         NOT NULL,
    role        ENUM('student','admin') NOT NULL DEFAULT 'student',
    created_at  TIMESTAMP           DEFAULT CURRENT_TIMESTAMP
);

-- -------------------------------------------------------
-- Table: menu
-- Stores all canteen food items
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS menu (
    item_id       INT AUTO_INCREMENT PRIMARY KEY,
    item_name     VARCHAR(100)   NOT NULL,
    description   VARCHAR(255),
    price         DECIMAL(8,2)   NOT NULL,
    category      VARCHAR(50)    NOT NULL,   -- e.g. Breakfast, Lunch, Snacks, Beverages
    is_available  TINYINT(1)     NOT NULL DEFAULT 1,  -- 1 = Available, 0 = Unavailable
    image_url     VARCHAR(255),
    created_at    TIMESTAMP      DEFAULT CURRENT_TIMESTAMP
);

-- -------------------------------------------------------
-- Table: orders
-- Stores each order placed by a student
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS orders (
    order_id      INT AUTO_INCREMENT PRIMARY KEY,
    user_id       INT            NOT NULL,
    total_amount  DECIMAL(10,2)  NOT NULL,
    status        ENUM('Pending','Preparing','Ready','Completed','Cancelled')
                                 NOT NULL DEFAULT 'Pending',
    order_time    TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    pickup_time   TIMESTAMP      NULL,
    notes         VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- -------------------------------------------------------
-- Table: order_items
-- Line-items for every order (normalised)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id      INT            NOT NULL,
    item_id       INT            NOT NULL,
    quantity      INT            NOT NULL DEFAULT 1,
    unit_price    DECIMAL(8,2)   NOT NULL,
    subtotal      DECIMAL(10,2)  GENERATED ALWAYS AS (quantity * unit_price) STORED,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id)  REFERENCES menu(item_id)   ON DELETE CASCADE
);

-- -------------------------------------------------------
-- Table: payments
-- Tracks payment status for each order
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS payments (
    payment_id     INT AUTO_INCREMENT PRIMARY KEY,
    order_id       INT            NOT NULL UNIQUE,
    amount         DECIMAL(10,2)  NOT NULL,
    payment_method ENUM('Cash','UPI','Card','Wallet') NOT NULL DEFAULT 'Cash',
    payment_status ENUM('Pending','Paid','Failed','Refunded') NOT NULL DEFAULT 'Pending',
    transaction_id VARCHAR(100),
    payment_time   TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
);

-- ============================================================
--  SAMPLE DATA
-- ============================================================

-- Admin user  (password: admin123 — plain text for demo only)
INSERT INTO users (full_name, email, password, phone, role) VALUES
('Admin User',        'admin@canteen.com',   'admin123',    '9999999999', 'admin'),
('Arjun Sharma',      'arjun@college.edu',   'student123',  '9876543210', 'student'),
('Priya Nair',        'priya@college.edu',   'student123',  '9876543211', 'student'),
('Rahul Verma',       'rahul@college.edu',   'student123',  '9876543212', 'student');

-- Menu items
INSERT INTO menu (item_name, description, price, category, is_available) VALUES
-- Breakfast
('Idli Sambar',       '2 soft idlis with hot sambar and chutney',   30.00, 'Breakfast', 1),
('Masala Dosa',       'Crispy dosa with spiced potato filling',       50.00, 'Breakfast', 1),
('Poha',              'Flattened rice with spices and peanuts',        25.00, 'Breakfast', 1),
('Bread Omelette',    '2 egg omelette with toasted bread',            40.00, 'Breakfast', 1),

-- Lunch
('Veg Thali',         'Rice, dal, 2 sabzi, roti, salad, pickle',     80.00, 'Lunch', 1),
('Chicken Biryani',   'Aromatic basmati rice with spiced chicken',    90.00, 'Lunch', 1),
('Paneer Butter Masala','Rich tomato-based paneer curry with rice',   85.00, 'Lunch', 1),
('Chole Bhature',     '2 bhature with spiced chickpea curry',         60.00, 'Lunch', 1),

-- Snacks
('Samosa (2 pcs)',    'Crispy pastry filled with spiced potatoes',    20.00, 'Snacks', 1),
('Vada Pav',          'Mumbai-style spiced potato burger',            25.00, 'Snacks', 1),
('French Fries',      'Salted crispy potato fries',                   40.00, 'Snacks', 1),
('Maggi Noodles',     'Classic spicy Maggi with veggies',             35.00, 'Snacks', 1),

-- Beverages
('Masala Chai',       'Spiced Indian milk tea',                       15.00, 'Beverages', 1),
('Cold Coffee',       'Blended iced coffee with milk',                45.00, 'Beverages', 1),
('Fresh Lime Soda',   'Chilled lime soda — sweet or salted',          30.00, 'Beverages', 1),
('Lassi',             'Sweet or salted yoghurt drink',                35.00, 'Beverages', 1);

-- Sample order
INSERT INTO orders (user_id, total_amount, status) VALUES (2, 145.00, 'Preparing');
INSERT INTO order_items (order_id, item_id, quantity, unit_price) VALUES
(1, 6, 1, 90.00),
(1, 1, 1, 30.00),
(1, 13, 1, 15.00),
(1, 9, 1, 10.00);   -- 2 samosas counted as 1 portion

INSERT INTO payments (order_id, amount, payment_method, payment_status) VALUES
(1, 145.00, 'UPI', 'Paid');
