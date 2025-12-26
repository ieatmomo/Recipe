-- ============================================
-- COMPREHENSIVE ABAC DEMO DATA
-- ============================================
-- This script populates both databases with extensive demo data
-- Run this after services are up and databases are created

USE auth_db;

-- Clear existing demo data
DELETE FROM user_acg WHERE user_id IN (SELECT id FROM user_info WHERE email LIKE '%@demo.com' OR email LIKE '%@example.com');
DELETE FROM user_coi WHERE user_id IN (SELECT id FROM user_info WHERE email LIKE '%@demo.com' OR email LIKE '%@example.com');
DELETE FROM user_info WHERE email LIKE '%@demo.com' OR email LIKE '%@example.com';

-- ============================================
-- 1. USERS WITH DIFFERENT CLEARANCES
-- ============================================
-- Password for all demo users: "password123" (BCrypt encoded)
-- $2a$10$pZ1Z5M4wX7yY6jQ8nK0VK.eJ5Z3Z4Z5Z6Z7Z8Z9Z0Z1Z2Z3Z4Z5Z6

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
-- 2. ASSIGN ACCESS CONTROL GROUPS (ACGs)
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
-- 3. ASSIGN COMMUNITIES OF INTEREST (COIs)
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
-- NOW SWITCH TO RECIPE DATABASE
-- ============================================
USE recipe_db;

-- Clear existing demo recipes
DELETE FROM recipe_acg WHERE recipe_id IN (SELECT id FROM Recipes WHERE author LIKE '%Demo%' OR ownerEmail LIKE '%@demo.com' OR ownerEmail LIKE '%@example.com');
DELETE FROM recipe_coi WHERE recipe_id IN (SELECT id FROM Recipes WHERE author LIKE '%Demo%' OR ownerEmail LIKE '%@demo.com' OR ownerEmail LIKE '%@example.com');
DELETE FROM Recipes WHERE author LIKE '%Demo%' OR ownerEmail LIKE '%@demo.com' OR ownerEmail LIKE '%@example.com';

-- ============================================
-- 4. PUBLIC RECIPES (No restrictions)
-- ============================================
INSERT INTO Recipes (name, description, ingredients, author, ownerEmail, category, region, is_restricted) VALUES
('Classic Spaghetti Carbonara', 'Traditional Italian pasta dish with eggs, cheese, and pancetta', 
'["400g spaghetti", "200g pancetta", "4 eggs", "100g Parmesan", "Black pepper", "Salt"]',
'Alice Demo', 'alice@example.com', 'Main Course', 'EU', FALSE),

('Chocolate Chip Cookies', 'Soft and chewy homemade cookies', 
'["2 cups flour", "1 cup butter", "1 cup sugar", "2 eggs", "2 cups chocolate chips", "1 tsp vanilla"]',
'Dave Demo', 'dave@example.com', 'Dessert', 'EU', FALSE),

('Greek Salad', 'Fresh Mediterranean salad', 
'["Tomatoes", "Cucumbers", "Red onion", "Feta cheese", "Kalamata olives", "Olive oil", "Oregano"]',
'Hannah Demo', 'hannah@demo.com', 'Salad', 'EU', FALSE),

('Chicken Stir Fry', 'Quick and healthy Asian-inspired dish',
'["500g chicken breast", "Mixed vegetables", "Soy sauce", "Ginger", "Garlic", "Sesame oil", "Rice"]',
'Bob Demo', 'bob@example.com', 'Main Course', 'ASIA', FALSE),

('French Toast', 'Classic breakfast dish',
'["4 slices bread", "2 eggs", "1/4 cup milk", "Cinnamon", "Vanilla", "Butter", "Maple syrup"]',
'Eve Demo', 'eve@demo.com', 'Breakfast', 'US', FALSE);

-- ============================================
-- 5. SECRET RESTRICTED RECIPES
-- ============================================
INSERT INTO Recipes (name, description, ingredients, author, ownerEmail, category, region, is_restricted) VALUES
('Classified Tiramisu', 'Secret family recipe from Northern Italy - contains classified ingredient ratios',
'["500g mascarpone", "6 eggs", "Ladyfinger biscuits", "Espresso", "Cocoa powder", "SECRET: Special liqueur blend"]',
'Alice Demo', 'alice@example.com', 'Dessert', 'EU', TRUE),

('Covert Beef Wellington', 'Gordon Ramsay''s actual technique - not publicly available',
'["1kg beef fillet", "Puff pastry", "Mushrooms", "Pâté", "SECRET: Special herb crust mixture"]',
'Eve Demo', 'eve@demo.com', 'Main Course', 'EU', TRUE),

('Intelligence Agency Chocolate Cake', 'Served at classified CIA cafeteria',
'["Dark chocolate", "Butter", "Sugar", "Eggs", "Flour", "SECRET: Classified frosting technique"]',
'Alice Demo', 'alice@example.com', 'Dessert', 'US', TRUE),

('Undercover Pad Thai', 'Authentic recipe from Thai royal family',
'["Rice noodles", "Shrimp", "Peanuts", "Tamarind", "Fish sauce", "SECRET: Royal family spice blend"]',
'Eve Demo', 'eve@demo.com', 'Main Course', 'ASIA', TRUE);

-- Assign SECRET ACG to these recipes
INSERT INTO recipe_acg (recipe_id, acg)
SELECT id, 'SECRET' FROM Recipes WHERE name LIKE 'Classified%' OR name LIKE 'Covert%' OR name LIKE 'Intelligence%' OR name LIKE 'Undercover%';

-- ============================================
-- 6. TOP_SECRET RESTRICTED RECIPES
-- ============================================
INSERT INTO Recipes (name, description, ingredients, author, ownerEmail, category, region, is_restricted) VALUES
('Pentagon Special Burger', 'Served only at Pentagon executive dining - requires TOP SECRET clearance',
'["Wagyu beef", "Truffle aioli", "Gold leaf", "SECRET SAUCE formula", "CLASSIFIED: Bio-engineered lettuce"]',
'Bob Demo', 'bob@example.com', 'Main Course', 'US', TRUE),

('NSA Breakfast Burrito', 'Contains ingredients monitored by three-letter agencies',
'["Scrambled eggs", "Chorizo", "Cheese", "Avocado", "TOP SECRET: Nano-enhanced hot sauce"]',
'Frank Demo', 'frank@demo.com', 'Breakfast', 'US', TRUE),

('MI6 Afternoon Tea Scones', 'James Bond''s favorite - classified recipe',
'["Flour", "Butter", "Milk", "Baking powder", "TOP SECRET: Agent''s special jam"]',
'Bob Demo', 'bob@example.com', 'Dessert', 'EU', TRUE),

('Area 51 Alien Smoothie', 'Requires cosmic clearance - not for public consumption',
'["Mango", "Pineapple", "Coconut water", "TOP SECRET: Extraterrestrial fruit hybrid"]',
'Frank Demo', 'frank@demo.com', 'Beverage', 'US', TRUE);

-- Assign TOP_SECRET and SECRET ACGs
INSERT INTO recipe_acg (recipe_id, acg)
SELECT id, acg FROM Recipes, (SELECT 'SECRET' AS acg UNION SELECT 'TOP_SECRET') AS acgs
WHERE name LIKE 'Pentagon%' OR name LIKE 'NSA%' OR name LIKE 'MI6%' OR name LIKE 'Area 51%';

-- ============================================
-- 7. NATO RESTRICTED RECIPES
-- ============================================
INSERT INTO Recipes (name, description, ingredients, author, ownerEmail, category, region, is_restricted) VALUES
('NATO Summit Bruschetta', 'Served at NATO headquarters - alliance members only',
'["Baguette", "Tomatoes", "Basil", "Garlic", "Olive oil", "NATO: Strategic herb blend"]',
'Charlie Demo', 'charlie@example.com', 'Appetizer', 'EU', TRUE),

('Allied Forces Energy Bar', 'Military-grade nutrition bar',
'["Oats", "Honey", "Nuts", "Dried fruit", "NATO: Combat-tested protein blend"]',
'Grace Demo', 'grace@demo.com', 'Snack', 'US', TRUE),

('Brussels HQ Coffee Blend', 'Official NATO headquarters coffee - members only',
'["Arabica beans", "Strategic water", "NATO: Classified roasting technique"]',
'Charlie Demo', 'charlie@example.com', 'Beverage', 'EU', TRUE);

-- Assign NATO ACG
INSERT INTO recipe_acg (recipe_id, acg)
SELECT id, 'NATO' FROM Recipes WHERE name LIKE 'NATO%' OR name LIKE 'Allied%' OR name LIKE 'Brussels HQ%';

-- ============================================
-- 8. ASSIGN COI TAGS TO RECIPES
-- ============================================
-- Italian cuisine tags
INSERT INTO recipe_coi (recipe_id, coi)
SELECT id, 'ITALIAN' FROM Recipes 
WHERE name LIKE '%Carbonara%' OR name LIKE '%Tiramisu%' OR name LIKE '%Bruschetta%';

-- Desserts tags
INSERT INTO recipe_coi (recipe_id, coi)
SELECT id, 'DESSERTS' FROM Recipes 
WHERE name LIKE '%Cookie%' OR name LIKE '%Cake%' OR name LIKE '%Tiramisu%' OR name LIKE '%Scone%';

-- Asian cuisine tags
INSERT INTO recipe_coi (recipe_id, coi)
SELECT id, 'ASIAN' FROM Recipes 
WHERE name LIKE '%Stir Fry%' OR name LIKE '%Pad Thai%';

-- Main course tags
INSERT INTO recipe_coi (recipe_id, coi)
SELECT id, 'MAIN_COURSE' FROM Recipes 
WHERE category = 'Main Course';

-- Appetizers tags
INSERT INTO recipe_coi (recipe_id, coi)
SELECT id, 'APPETIZERS' FROM Recipes 
WHERE name LIKE '%Bruschetta%' OR category = 'Appetizer';

-- Breakfast tags
INSERT INTO recipe_coi (recipe_id, coi)
SELECT id, 'BREAKFAST' FROM Recipes 
WHERE name LIKE '%French Toast%' OR name LIKE '%Breakfast%' OR name LIKE '%Burrito%';

-- ============================================
-- 9. ADDITIONAL PUBLIC RECIPES FOR VARIETY
-- ============================================
INSERT INTO Recipes (name, description, ingredients, author, ownerEmail, category, region, is_restricted) VALUES
('Margherita Pizza', 'Classic Italian pizza',
'["Pizza dough", "Tomato sauce", "Mozzarella", "Basil", "Olive oil"]',
'Hannah Demo', 'hannah@demo.com', 'Main Course', 'EU', FALSE),

('Caesar Salad', 'Creamy romaine salad',
'["Romaine lettuce", "Croutons", "Parmesan", "Caesar dressing", "Lemon"]',
'Dave Demo', 'dave@example.com', 'Salad', 'US', FALSE),

('Pancakes', 'Fluffy American pancakes',
'["Flour", "Eggs", "Milk", "Baking powder", "Butter", "Syrup"]',
'Eve Demo', 'eve@demo.com', 'Breakfast', 'US', FALSE),

('Vegetable Curry', 'Spicy and aromatic',
'["Mixed vegetables", "Curry paste", "Coconut milk", "Rice", "Spices"]',
'Frank Demo', 'frank@demo.com', 'Main Course', 'ASIA', FALSE),

('Apple Pie', 'All-American dessert',
'["Apples", "Pie crust", "Sugar", "Cinnamon", "Butter"]',
'Grace Demo', 'grace@demo.com', 'Dessert', 'US', FALSE);

-- Tag these public recipes
INSERT INTO recipe_coi (recipe_id, coi)
SELECT id, 'ITALIAN' FROM Recipes WHERE name = 'Margherita Pizza';

INSERT INTO recipe_coi (recipe_id, coi)
SELECT id, 'BREAKFAST' FROM Recipes WHERE name = 'Pancakes';

INSERT INTO recipe_coi (recipe_id, coi)
SELECT id, 'ASIAN' FROM Recipes WHERE name = 'Vegetable Curry';

INSERT INTO recipe_coi (recipe_id, coi)
SELECT id, 'VEGAN' FROM Recipes WHERE name = 'Vegetable Curry';

INSERT INTO recipe_coi (recipe_id, coi)
SELECT id, 'DESSERTS' FROM Recipes WHERE name = 'Apple Pie';

-- ============================================
-- DEMO DATA LOADED SUCCESSFULLY!
-- ============================================
-- Summary:
-- - 10 Users with different clearance levels
-- - 17 Recipes (5 public, 4 SECRET, 4 TOP_SECRET, 3 NATO, 1 unclassified)
-- - Multiple COI tags for notifications
-- - Ready for comprehensive ABAC demo!
-- ============================================
