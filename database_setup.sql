-- =====================================================
-- GOS Gaming Online Shop Database Setup Script
-- =====================================================

-- Drop and create database
DROP DATABASE IF EXISTS online_shop;
CREATE DATABASE online_shop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE online_shop;

-- =====================================================
-- 1. ROLES TABLE
-- =====================================================
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO roles (name) VALUES 
('ADMIN'),
('USER');

-- =====================================================
-- 2. USERS TABLE
-- =====================================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(20),
    address TEXT,
    avatar VARCHAR(255),
    is_enabled BOOLEAN DEFAULT TRUE,
    is_verified BOOLEAN DEFAULT FALSE,
    verification_code VARCHAR(10),
    reset_password_token VARCHAR(255),
    provider VARCHAR(50) DEFAULT 'LOCAL',
    provider_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create default admin user (password: admin123)
INSERT INTO users (email, password, first_name, last_name, is_enabled, is_verified, provider) VALUES
('admin@gos.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRdvX1PM3il.L2PgxR2AzSGxWoS', 'Admin', 'GOS', TRUE, TRUE, 'LOCAL');

-- =====================================================
-- 3. USER_ROLES TABLE
-- =====================================================
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Assign admin role to admin user
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);

-- =====================================================
-- 4. CATEGORIES TABLE
-- =====================================================
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    image VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO categories (id, name, description, image) VALUES
(1, 'Gaming PC', 'High-performance gaming computers with latest graphics cards', 'gaming-pc.jpg'),
(2, 'Business PC', 'Professional computers for office and business use', 'business-pc.jpg'),
(3, 'Workstation', 'Professional workstations for CAD, rendering, and heavy computing', 'workstation.jpg'),
(4, 'Gaming Laptop', 'Portable gaming laptops with powerful graphics', 'gaming-laptop.jpg'),
(5, 'Business Laptop', 'Professional laptops for business and productivity', 'business-laptop.jpg'),
(6, 'All-in-One PC', 'Space-saving all-in-one desktop computers', 'aio-pc.jpg'),
(7, 'Mini PC', 'Compact and energy-efficient mini computers', 'mini-pc.jpg'),
(8, 'Custom Build', 'Custom-built PCs tailored to specific needs', 'custom-build.jpg');

-- =====================================================
-- 5. PRODUCTS TABLE
-- =====================================================
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    sold INT NOT NULL DEFAULT 0,
    status BOOLEAN DEFAULT TRUE,
    image VARCHAR(255),
    category_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_category_id (category_id),
    INDEX idx_status (status),
    INDEX idx_price (price)
);

-- Insert products data
INSERT INTO products (id, name, description, price, quantity, sold, status, image, category_id) VALUES
(1, 'ASUS ROG Gaming PC RTX 4070', 'High-performance gaming PC featuring NVIDIA RTX 4070 graphics card, Intel Core i7-13700F processor, 16GB DDR4 RAM, and 1TB NVMe SSD. Perfect for gaming, content creation, and professional work. Includes RGB lighting and premium cooling system.', 1299.99, 25, 15, TRUE, 'asus-rog-rtx4070.png', 1),
(2, 'MSI Gaming PC RTX 4060 Ti', 'Powerful gaming setup with RTX 4060 Ti, Intel Core i5-13400F, 16GB DDR4, 500GB NVMe SSD. Excellent for 1440p gaming and streaming. Features MSI RGB lighting and efficient cooling.', 999.99, 30, 22, TRUE, 'msi-gaming-rtx4060ti.jpg', 1),
(3, 'Alienware Aurora R15 RTX 4080', 'Premium gaming machine with RTX 4080, Intel Core i9-13900F, 32GB DDR5, 1TB NVMe SSD. Ultimate gaming performance with iconic Alienware design and advanced thermal management.', 2199.99, 12, 8, TRUE, 'alienware-aurora-rtx4080.jpg', 1),
(4, 'Custom RGB Gaming PC RTX 4070 Super', 'Custom-built gaming PC with RTX 4070 Super, AMD Ryzen 7 7700X, 32GB DDR5, 1TB Gen4 SSD. Features premium RGB components and liquid cooling for maximum performance.', 1499.99, 18, 12, TRUE, 'custom-rgb-rtx4070super.jpg', 1),
(5, 'Dell OptiPlex 7090 Business PC', 'Reliable business desktop with Intel Core i5-11500, 8GB DDR4, 256GB SSD. Perfect for office work, productivity tasks, and business applications. Compact design with excellent reliability.', 699.99, 40, 35, TRUE, 'dell-optiplex-7090.png', 2),
(6, 'HP EliteDesk 800 G9 Mini', 'Compact business PC with Intel Core i3-12100, 8GB DDR4, 256GB SSD. Space-saving design ideal for office environments. Energy efficient and quiet operation.', 549.99, 50, 28, TRUE, 'hp-elitedesk-800-mini.jpg', 2),
(7, 'Lenovo ThinkCentre M90q Tiny', 'Ultra-compact business PC with Intel Core i5-12400T, 16GB DDR4, 512GB SSD. Perfect for modern offices with limited space. Excellent performance and reliability.', 599.99, 35, 20, TRUE, 'lenovo-thinkcentre-m90q.jpg', 2),
(8, 'Dell Precision 7000 Workstation', 'Professional workstation with Intel Xeon W-2245, 64GB ECC RAM, NVIDIA RTX A4000, 2TB NVMe SSD. Designed for CAD, 3D rendering, and professional applications.', 2999.99, 8, 5, TRUE, 'dell-precision-7000.jpg', 3),
(9, 'HP Z4 G5 Workstation', 'High-performance workstation with Intel Xeon W-2235, 32GB ECC RAM, NVIDIA RTX A2000, 1TB SSD. Ideal for engineering, architecture, and content creation.', 2499.99, 10, 7, TRUE, 'hp-z4-g5-workstation.jpg', 3),
(10, 'ASUS ROG Strix G15 RTX 4060', '15.6" gaming laptop with RTX 4060, AMD Ryzen 7 7735HS, 16GB DDR5, 512GB SSD, 144Hz display. Perfect for gaming on the go with excellent performance and portability.', 1199.99, 20, 18, TRUE, 'asus-rog-strix-g15.jpg', 4),
(11, 'MSI Katana 15 RTX 4050', 'Affordable gaming laptop with RTX 4050, Intel Core i5-13420H, 16GB DDR4, 512GB SSD, 144Hz display. Great entry-level gaming laptop with solid performance.', 899.99, 25, 22, TRUE, 'msi-katana-15-rtx4050.jpg', 4),
(12, 'Razer Blade 15 RTX 4070', 'Premium gaming laptop with RTX 4070, Intel Core i7-13800H, 32GB DDR5, 1TB SSD, 240Hz QHD display. Ultra-thin design with maximum gaming performance.', 2299.99, 15, 9, TRUE, 'razer-blade-15-rtx4070.jpg', 4),
(13, 'Lenovo ThinkPad E15 Gen 4', 'Business laptop with Intel Core i5-1235U, 16GB DDR4, 512GB SSD, 15.6" FHD display. Reliable and durable design perfect for business professionals.', 749.99, 30, 25, TRUE, 'lenovo-thinkpad-e15.jpg', 5),
(14, 'Dell Latitude 5530', 'Professional laptop with Intel Core i7-1255U, 16GB DDR4, 512GB SSD, 15.6" FHD display. Enhanced security features and excellent build quality.', 899.99, 28, 20, TRUE, 'dell-latitude-5530.jpg', 5),
(15, 'HP Pavilion 24 All-in-One', '24" All-in-One PC with Intel Core i5-1235U, 16GB DDR4, 512GB SSD, Full HD touchscreen. Space-saving design perfect for home and office use.', 899.99, 22, 15, TRUE, 'hp-pavilion-24-aio.jpg', 6),
(16, 'Dell Inspiron 27 7000 AIO', '27" All-in-One with Intel Core i7-1255U, 16GB DDR4, 1TB SSD, 4K display. Premium design with excellent display quality for multimedia and productivity.', 1199.99, 18, 12, TRUE, 'dell-inspiron-27-aio.jpg', 6),
(17, 'Intel NUC 12 Pro Mini PC', 'Compact mini PC with Intel Core i5-1240P, 16GB DDR4, 512GB SSD. Ultra-small form factor perfect for space-constrained environments.', 449.99, 35, 28, TRUE, 'intel-nuc-12-pro.jpg', 7),
(18, 'ASUS PN64 Mini PC', 'Tiny PC with AMD Ryzen 5 5500U, 8GB DDR4, 256GB SSD. Energy-efficient and quiet operation ideal for basic computing tasks.', 399.99, 40, 32, TRUE, 'asus-pn64-mini.jpg', 7),
(19, 'Custom Enthusiast Build RTX 4090', 'Ultimate custom PC with RTX 4090, Intel Core i9-13900K, 64GB DDR5, 2TB Gen4 SSD. Top-tier performance for extreme gaming and professional workloads.', 3499.99, 5, 3, TRUE, 'custom-enthusiast-rtx4090.jpg', 8),
(20, 'Budget Custom Build GTX 1660 Super', 'Affordable custom PC with GTX 1660 Super, AMD Ryzen 5 5600, 16GB DDR4, 500GB SSD. Great entry-level gaming and productivity build.', 599.99, 25, 20, TRUE, 'budget-custom-gtx1660super.jpg', 8);

-- =====================================================
-- 6. ORDERS TABLE
-- =====================================================
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    payment_method VARCHAR(50),
    payment_status VARCHAR(50) DEFAULT 'PENDING',
    shipping_address TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_order_number (order_number)
);

-- =====================================================
-- 7. ORDER_ITEMS TABLE
-- =====================================================
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
);

-- =====================================================
-- 8. CART_ITEMS TABLE
-- =====================================================
CREATE TABLE cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    session_id VARCHAR(255),
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_session_id (session_id),
    INDEX idx_product_id (product_id)
);

-- =====================================================
-- 9. CHAT_HISTORY TABLE (for AI Chat Memory)
-- =====================================================
CREATE TABLE chat_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    session_id VARCHAR(100) NULL,
    conversation_id VARCHAR(100) NOT NULL,
    message_type VARCHAR(10) NOT NULL CHECK (message_type IN ('user', 'bot')),
    message TEXT NOT NULL,
    page_type VARCHAR(50) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_summarized BOOLEAN DEFAULT FALSE,

    -- Foreign key constraint
    CONSTRAINT fk_chat_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    -- Indexes for performance
    INDEX idx_chat_history_user_conversation (user_id, conversation_id),
    INDEX idx_chat_history_session_conversation (session_id, conversation_id),
    INDEX idx_chat_history_created_at (created_at),
    INDEX idx_chat_history_conversation_id (conversation_id)
);

-- =====================================================
-- 10. SPRING SESSION TABLES (for session management)
-- =====================================================
CREATE TABLE SPRING_SESSION (
    PRIMARY_ID CHAR(36) NOT NULL,
    SESSION_ID CHAR(36) NOT NULL,
    CREATION_TIME BIGINT NOT NULL,
    LAST_ACCESS_TIME BIGINT NOT NULL,
    MAX_INACTIVE_INTERVAL INT NOT NULL,
    EXPIRY_TIME BIGINT NOT NULL,
    PRINCIPAL_NAME VARCHAR(100),
    CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
);

CREATE UNIQUE INDEX SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
CREATE INDEX SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
CREATE INDEX SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);

CREATE TABLE SPRING_SESSION_ATTRIBUTES (
    SESSION_PRIMARY_ID CHAR(36) NOT NULL,
    ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
    ATTRIBUTE_BYTES BLOB NOT NULL,
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE
);

-- =====================================================
-- FINAL SETUP
-- =====================================================

-- Reset AUTO_INCREMENT for products to continue from 21
ALTER TABLE products AUTO_INCREMENT = 21;

-- Show database summary
SELECT 'Database setup completed successfully!' as Status;
SELECT COUNT(*) as Total_Categories FROM categories;
SELECT COUNT(*) as Total_Products FROM products;
SELECT COUNT(*) as Total_Users FROM users;

-- Show sample data
SELECT 'Sample Products:' as Info;
SELECT id, name, price, category_id FROM products LIMIT 5;
