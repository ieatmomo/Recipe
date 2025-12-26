-- ============================================
-- ABAC DEMO - RECIPE DATA
-- Load into recipe_db database
-- ============================================

USE recipe_db;

-- Clear existing demo recipes
DELETE FROM recipe_acg WHERE recipe_id IN (SELECT id FROM Recipes WHERE author LIKE '%Demo%' OR ownerEmail LIKE '%@demo.com' OR ownerEmail LIKE '%@example.com');
DELETE FROM recipe_coi WHERE recipe_id IN (SELECT id FROM Recipes WHERE author LIKE '%Demo%' OR ownerEmail LIKE '%@demo.com' OR ownerEmail LIKE '%@example.com');
DELETE FROM Recipes WHERE author LIKE '%Demo%' OR ownerEmail LIKE '%@demo.com' OR ownerEmail LIKE '%@example.com';

-- ============================================
-- PUBLIC RECIPES (No restrictions)
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
'Eve Demo', 'eve@demo.com', 'Breakfast', 'US', FALSE),

('Margherita Pizza', 'Classic Italian pizza',
'["Pizza dough", "Tomato sauce", "Mozzarella", "Basil", "Olive oil"]',
'Hannah Demo', 'hannah@demo.com', 'Main Course', 'EU', FALSE),

('Caesar Salad', 'Creamy romaine salad',
'["Romaine lettuce", "Croutons", "Parmesan", "Caesar dressing", "Lemon"]',
'Dave Demo', 'dave@example.com', 'Salad', 'US', FALSE),

('Pancakes', 'Fluffy American pancakes',
'["Flour", "Eggs", "Milk", "Baking powder", "Butter", "Syrup"]',
'Eve Demo', 'eve@demo.com', 'Breakfast', 'US', FALSE),

('Vegetable Curry', 'Spicy and aromatic vegan curry',
'["Mixed vegetables", "Curry paste", "Coconut milk", "Rice", "Spices"]',
'Frank Demo', 'frank@demo.com', 'Main Course', 'ASIA', FALSE),

('Apple Pie', 'All-American dessert',
'["Apples", "Pie crust", "Sugar", "Cinnamon", "Butter"]',
'Grace Demo', 'grace@demo.com', 'Dessert', 'US', FALSE);

-- ============================================
-- SECRET RESTRICTED RECIPES
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
-- TOP_SECRET RESTRICTED RECIPES
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
-- NATO RESTRICTED RECIPES
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
-- ASSIGN COI TAGS TO RECIPES
-- ============================================
-- Italian cuisine tags
INSERT INTO recipe_coi (recipe_id, coi)
SELECT id, 'ITALIAN' FROM Recipes 
WHERE name LIKE '%Carbonara%' OR name LIKE '%Tiramisu%' OR name LIKE '%Bruschetta%' OR name LIKE '%Pizza%';

-- Desserts tags
INSERT INTO recipe_coi (recipe_id, coi)
SELECT id, 'DESSERTS' FROM Recipes 
WHERE name LIKE '%Cookie%' OR name LIKE '%Cake%' OR name LIKE '%Tiramisu%' OR name LIKE '%Scone%' OR name LIKE '%Pie%';

-- Asian cuisine tags
INSERT INTO recipe_coi (recipe_id, coi)
SELECT id, 'ASIAN' FROM Recipes 
WHERE name LIKE '%Stir Fry%' OR name LIKE '%Pad Thai%' OR name LIKE '%Curry%';

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
WHERE name LIKE '%French Toast%' OR name LIKE '%Breakfast%' OR name LIKE '%Burrito%' OR name LIKE '%Pancake%';

-- Vegan tags
INSERT INTO recipe_coi (recipe_id, coi)
SELECT id, 'VEGAN' FROM Recipes WHERE name LIKE '%Vegetable Curry%';

-- ============================================
-- RECIPE DATA LOADED!
-- ============================================
SELECT 'Recipes loaded successfully!' AS Status;
SELECT COUNT(*) AS TotalRecipes FROM Recipes WHERE author LIKE '%Demo%';
SELECT COUNT(*) AS PublicRecipes FROM Recipes WHERE author LIKE '%Demo%' AND is_restricted = FALSE;
SELECT COUNT(*) AS RestrictedRecipes FROM Recipes WHERE author LIKE '%Demo%' AND is_restricted = TRUE;
SELECT COUNT(*) AS TotalACGs FROM recipe_acg;
SELECT COUNT(*) AS TotalCOIs FROM recipe_coi;
