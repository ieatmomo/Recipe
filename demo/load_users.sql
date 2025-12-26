-- ============================================
-- ABAC DEMO - USER DATA
-- Load into auth_db database
-- ============================================

USE auth_db;

-- Clear existing demo data
DELETE FROM user_acg WHERE user_id IN (SELECT id FROM user_info WHERE email LIKE '%@demo.com' OR email LIKE '%@example.com');
DELETE FROM user_coi WHERE user_id IN (SELECT id FROM user_info WHERE email LIKE '%@demo.com' OR email LIKE '%@example.com');
DELETE FROM user_info WHERE email LIKE '%@demo.com' OR email LIKE '%@example.com';

-- ============================================
-- USERS WITH DIFFERENT CLEARANCES
-- Password for all demo users: "password123" (BCrypt encoded)
-- ============================================
INSERT INTO user_info (name, email, password, roles, region) VALUES
-- ADMIN USERS (see everything)
('Admin User', 'admin@example.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Dz6G6D6tUo6NzT7p7Z.Km', 'ROLE_ADMIN', 'EU'),
('Super Admin', 'superadmin@demo.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Dz6G6D6tUo6NzT7p7Z.Km', 'ROLE_ADMIN', 'US'),

-- SECRET CLEARANCE USERS
('Alice Johnson', 'alice@example.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Dz6G6D6tUo6NzT7p7Z.Km', 'ROLE_USER', 'EU'),
('Eve Martinez', 'eve@demo.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Dz6G6D6tUo6NzT7p7Z.Km', 'ROLE_USER', 'US'),

-- TOP_SECRET CLEARANCE USERS  
('Bob Smith', 'bob@example.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Dz6G6D6tUo6NzT7p7Z.Km', 'ROLE_USER', 'EU'),
('Frank Wilson', 'frank@demo.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Dz6G6D6tUo6NzT7p7Z.Km', 'ROLE_USER', 'ASIA'),

-- NATO CLEARANCE USERS
('Charlie Brown', 'charlie@example.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Dz6G6D6tUo6NzT7p7Z.Km', 'ROLE_USER', 'EU'),
('Grace Lee', 'grace@demo.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Dz6G6D6tUo6NzT7p7Z.Km', 'ROLE_USER', 'US'),

-- NO CLEARANCE USERS (public recipes only)
('Dave Davis', 'dave@example.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Dz6G6D6tUo6NzT7p7Z.Km', 'ROLE_USER', 'EU'),
('Hannah Moore', 'hannah@demo.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Dz6G6D6tUo6NzT7p7Z.Km', 'ROLE_USER', 'ASIA');

-- ============================================
-- ASSIGN ACCESS CONTROL GROUPS (ACGs)
-- ============================================
-- Alice and Eve: SECRET clearance
INSERT INTO user_acg (user_id, acg) 
SELECT id, 'SECRET' FROM user_info WHERE email IN ('alice@example.com', 'eve@demo.com');

-- Bob and Frank: TOP_SECRET clearance (includes SECRET)
INSERT INTO user_acg (user_id, acg)
SELECT id, acg FROM user_info, (SELECT 'SECRET' AS acg UNION SELECT 'TOP_SECRET') AS acgs 
WHERE email IN ('bob@example.com', 'frank@demo.com');

-- Charlie and Grace: NATO clearance
INSERT INTO user_acg (user_id, acg)
SELECT id, 'NATO' FROM user_info WHERE email IN ('charlie@example.com', 'grace@demo.com');

-- ============================================
-- ASSIGN COMMUNITIES OF INTEREST (COIs)
-- ============================================
-- Italian Cuisine enthusiasts
INSERT INTO user_coi (user_id, coi)
SELECT id, 'ITALIAN' FROM user_info WHERE email IN ('alice@example.com', 'charlie@example.com', 'hannah@demo.com');

-- Desserts lovers
INSERT INTO user_coi (user_id, coi)
SELECT id, 'DESSERTS' FROM user_info WHERE email IN ('alice@example.com', 'dave@example.com', 'grace@demo.com');

-- Asian Cuisine fans
INSERT INTO user_coi (user_id, coi)
SELECT id, 'ASIAN' FROM user_info WHERE email IN ('bob@example.com', 'frank@demo.com', 'hannah@demo.com');

-- Main Course specialists
INSERT INTO user_coi (user_id, coi)
SELECT id, 'MAIN_COURSE' FROM user_info WHERE email IN ('bob@example.com', 'eve@demo.com', 'charlie@example.com');

-- Appetizers
INSERT INTO user_coi (user_id, coi)
SELECT id, 'APPETIZERS' FROM user_info WHERE email IN ('charlie@example.com', 'grace@demo.com');

-- Vegan recipes
INSERT INTO user_coi (user_id, coi)
SELECT id, 'VEGAN' FROM user_info WHERE email IN ('dave@example.com', 'frank@demo.com');

-- Breakfast recipes  
INSERT INTO user_coi (user_id, coi)
SELECT id, 'BREAKFAST' FROM user_info WHERE email IN ('eve@demo.com', 'hannah@demo.com');

-- ============================================
-- USER DATA LOADED!
-- ============================================
SELECT 'Users loaded successfully!' AS Status;
SELECT COUNT(*) AS TotalUsers FROM user_info WHERE email LIKE '%@demo.com' OR email LIKE '%@example.com';
SELECT COUNT(*) AS TotalACGs FROM user_acg;
SELECT COUNT(*) AS TotalCOIs FROM user_coi;
