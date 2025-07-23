-- PC Shop Database Sample Data
-- Compatible with existing Spring Boot application structure

-- Clear existing data (optional - use with caution)
-- SET FOREIGN_KEY_CHECKS = 0;
-- TRUNCATE TABLE order_items;
-- TRUNCATE TABLE cart_items;
-- TRUNCATE TABLE products;
-- TRUNCATE TABLE categories;
-- TRUNCATE TABLE orders;
-- TRUNCATE TABLE carts;
-- TRUNCATE TABLE users;
-- TRUNCATE TABLE roles;
-- SET FOREIGN_KEY_CHECKS = 1;

-- Insert Roles
INSERT INTO roles (id, name) VALUES 
(1, 'ADMIN'),
(2, 'USER')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Insert Categories for PC Shop
INSERT INTO categories (id, name, image) VALUES 
(1, 'Gaming PCs', 'gaming-pc-category.jpg'),
(2, 'Office PCs', 'office-pc-category.jpg'),
(3, 'Workstation PCs', 'workstation-pc-category.jpg'),
(4, 'Gaming Laptops', 'gaming-laptop-category.jpg'),
(5, 'Business Laptops', 'business-laptop-category.jpg'),
(6, 'All-in-One PCs', 'aio-pc-category.jpg'),
(7, 'Mini PCs', 'mini-pc-category.jpg'),
(8, 'Custom Build PCs', 'custom-pc-category.jpg')
ON DUPLICATE KEY UPDATE 
name = VALUES(name), 
image = VALUES(image);

-- Insert Sample Users
INSERT INTO users (id, username, password, fullname, email, phone, status, provider, role_id) VALUES 
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8ioctKi7Z2pjKUyPmznxdCnqSgGAa', 'System Administrator', 'admin@pcworld.com', '0123456789', true, 'LOCAL', 1),
(2, 'john_doe', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8ioctKi7Z2pjKUyPmznxdCnqSgGAa', 'John Doe', 'john.doe@email.com', '0987654321', true, 'LOCAL', 2),
(3, 'jane_smith', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8ioctKi7Z2pjKUyPmznxdCnqSgGAa', 'Jane Smith', 'jane.smith@email.com', '0912345678', true, 'LOCAL', 2),
(4, 'mike_wilson', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8ioctKi7Z2pjKUyPmznxdCnqSgGAa', 'Mike Wilson', 'mike.wilson@email.com', '0934567890', true, 'LOCAL', 2)
ON DUPLICATE KEY UPDATE 
username = VALUES(username),
fullname = VALUES(fullname),
email = VALUES(email),
phone = VALUES(phone),
status = VALUES(status);

-- Insert Gaming PCs
INSERT INTO products (id, name, price, image, description, category_id, status, quantity, sold) VALUES 
(1, 'ASUS ROG Gaming PC RTX 4070', 1299.99, 'asus-rog-rtx4070.jpg', 
'High-performance gaming PC featuring NVIDIA RTX 4070 graphics card, Intel Core i7-13700F processor, 16GB DDR4 RAM, and 1TB NVMe SSD. Perfect for gaming, content creation, and professional work. Includes RGB lighting and premium cooling system.', 
1, true, 25, 15),

(2, 'MSI Gaming PC RTX 4060 Ti', 999.99, 'msi-gaming-rtx4060ti.jpg',
'Powerful gaming setup with RTX 4060 Ti, Intel Core i5-13400F, 16GB DDR4, 500GB NVMe SSD. Excellent for 1440p gaming and streaming. Features MSI RGB lighting and efficient cooling.',
1, true, 30, 22),

(3, 'Alienware Aurora R15 RTX 4080', 2199.99, 'alienware-aurora-rtx4080.jpg',
'Premium gaming machine with RTX 4080, Intel Core i9-13900F, 32GB DDR5, 1TB NVMe SSD. Ultimate gaming performance with iconic Alienware design and advanced thermal management.',
1, true, 12, 8),

(4, 'Custom RGB Gaming PC RTX 4070 Super', 1499.99, 'custom-rgb-rtx4070super.jpg',
'Custom-built gaming PC with RTX 4070 Super, AMD Ryzen 7 7700X, 32GB DDR5, 1TB Gen4 SSD. Features premium RGB components and liquid cooling for maximum performance.',
1, true, 18, 12);

-- Insert Office PCs
INSERT INTO products (id, name, price, image, description, category_id, status, quantity, sold) VALUES 
(5, 'Dell OptiPlex 7090 Business PC', 699.99, 'dell-optiplex-7090.jpg',
'Reliable business desktop with Intel Core i5-11500, 8GB DDR4, 256GB SSD. Perfect for office work, productivity tasks, and business applications. Compact design with excellent reliability.',
2, true, 40, 35),

(6, 'HP EliteDesk 800 G9 Mini', 549.99, 'hp-elitedesk-800-mini.jpg',
'Compact business PC with Intel Core i3-12100, 8GB DDR4, 256GB SSD. Space-saving design ideal for office environments. Energy efficient and quiet operation.',
2, true, 50, 28),

(7, 'Lenovo ThinkCentre M90q Tiny', 599.99, 'lenovo-thinkcentre-m90q.jpg',
'Ultra-compact business PC with Intel Core i5-12400T, 16GB DDR4, 512GB SSD. Perfect for modern offices with limited space. Excellent performance and reliability.',
2, true, 35, 20);

-- Insert Workstation PCs  
INSERT INTO products (id, name, price, image, description, category_id, status, quantity, sold) VALUES 
(8, 'Dell Precision 7000 Workstation', 2999.99, 'dell-precision-7000.jpg',
'Professional workstation with Intel Xeon W-2245, 64GB ECC RAM, NVIDIA RTX A4000, 2TB NVMe SSD. Designed for CAD, 3D rendering, and professional applications.',
3, true, 8, 5),

(9, 'HP Z4 G5 Workstation', 2499.99, 'hp-z4-g5-workstation.jpg',
'High-performance workstation with Intel Xeon W-2235, 32GB ECC RAM, NVIDIA RTX A2000, 1TB SSD. Ideal for engineering, architecture, and content creation.',
3, true, 10, 7);

-- Insert Gaming Laptops
INSERT INTO products (id, name, price, image, description, category_id, status, quantity, sold) VALUES 
(10, 'ASUS ROG Strix G15 RTX 4060', 1199.99, 'asus-rog-strix-g15.jpg',
'15.6" gaming laptop with RTX 4060, AMD Ryzen 7 7735HS, 16GB DDR5, 512GB SSD, 144Hz display. Perfect for gaming on the go with excellent performance and portability.',
4, true, 20, 18),

(11, 'MSI Katana 15 RTX 4050', 899.99, 'msi-katana-15-rtx4050.jpg',
'Affordable gaming laptop with RTX 4050, Intel Core i5-13420H, 16GB DDR4, 512GB SSD, 144Hz display. Great entry-level gaming laptop with solid performance.',
4, true, 25, 22),

(12, 'Razer Blade 15 RTX 4070', 2299.99, 'razer-blade-15-rtx4070.jpg',
'Premium gaming laptop with RTX 4070, Intel Core i7-13800H, 32GB DDR5, 1TB SSD, 240Hz QHD display. Ultra-thin design with maximum gaming performance.',
4, true, 15, 9);

-- Insert Business Laptops
INSERT INTO products (id, name, price, image, description, category_id, status, quantity, sold) VALUES 
(13, 'Lenovo ThinkPad E15 Gen 4', 749.99, 'lenovo-thinkpad-e15.jpg',
'Business laptop with Intel Core i5-1235U, 16GB DDR4, 512GB SSD, 15.6" FHD display. Reliable and durable design perfect for business professionals.',
5, true, 30, 25),

(14, 'Dell Latitude 5530', 899.99, 'dell-latitude-5530.jpg',
'Professional laptop with Intel Core i7-1255U, 16GB DDR4, 512GB SSD, 15.6" FHD display. Enhanced security features and excellent build quality.',
5, true, 28, 20);

-- Insert All-in-One PCs
INSERT INTO products (id, name, price, image, description, category_id, status, quantity, sold) VALUES 
(15, 'HP Pavilion 24 All-in-One', 899.99, 'hp-pavilion-24-aio.jpg',
'24" All-in-One PC with Intel Core i5-1235U, 16GB DDR4, 512GB SSD, Full HD touchscreen. Space-saving design perfect for home and office use.',
6, true, 22, 15),

(16, 'Dell Inspiron 27 7000 AIO', 1199.99, 'dell-inspiron-27-aio.jpg',
'27" All-in-One with Intel Core i7-1255U, 16GB DDR4, 1TB SSD, 4K display. Premium design with excellent display quality for multimedia and productivity.',
6, true, 18, 12);

-- Insert Mini PCs
INSERT INTO products (id, name, price, image, description, category_id, status, quantity, sold) VALUES 
(17, 'Intel NUC 12 Pro Mini PC', 449.99, 'intel-nuc-12-pro.jpg',
'Compact mini PC with Intel Core i5-1240P, 16GB DDR4, 512GB SSD. Ultra-small form factor perfect for space-constrained environments.',
7, true, 35, 28),

(18, 'ASUS PN64 Mini PC', 399.99, 'asus-pn64-mini.jpg',
'Tiny PC with AMD Ryzen 5 5500U, 8GB DDR4, 256GB SSD. Energy-efficient and quiet operation ideal for basic computing tasks.',
7, true, 40, 32);

-- Insert Custom Build PCs
INSERT INTO products (id, name, price, image, description, category_id, status, quantity, sold) VALUES 
(19, 'Custom Enthusiast Build RTX 4090', 3499.99, 'custom-enthusiast-rtx4090.jpg',
'Ultimate custom PC with RTX 4090, Intel Core i9-13900K, 64GB DDR5, 2TB Gen4 SSD. Top-tier performance for extreme gaming and professional workloads.',
8, true, 5, 3),

(20, 'Budget Custom Build GTX 1660 Super', 599.99, 'budget-custom-gtx1660super.jpg',
'Affordable custom PC with GTX 1660 Super, AMD Ryzen 5 5600, 16GB DDR4, 500GB SSD. Great entry-level gaming and productivity build.',
8, true, 25, 20);

-- Insert Carts for users
INSERT INTO carts (id, user_id, sum) VALUES 
(1, 2, 0.00),
(2, 3, 0.00),
(3, 4, 0.00)
ON DUPLICATE KEY UPDATE sum = VALUES(sum);

-- Reset AUTO_INCREMENT values
ALTER TABLE roles AUTO_INCREMENT = 3;
ALTER TABLE categories AUTO_INCREMENT = 9;
ALTER TABLE users AUTO_INCREMENT = 5;
ALTER TABLE products AUTO_INCREMENT = 21;
ALTER TABLE carts AUTO_INCREMENT = 4;

-- Note: Password for all users is "123456" (encoded with BCrypt)
-- Admin credentials: username: admin, password: 123456
-- User credentials: username: john_doe, password: 123456

-- Instructions:
-- 1. Create a MySQL database named "online_shop"
-- 2. Run this SQL script to populate the database
-- 3. Make sure your application.properties has correct database connection settings
-- 4. Start your Spring Boot application
-- 5. Access /shop to see the product listing
-- 6. Click on any product to see the product detail page
