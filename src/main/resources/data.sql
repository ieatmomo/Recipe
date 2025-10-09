SET FOREIGN_KEY_CHECKS=0;
DROP TABLE IF EXISTS recipes;
SET FOREIGN_KEY_CHECKS=1;

CREATE TABLE recipes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    author VARCHAR(255),
    description TEXT,
    ingredients TEXT
);

INSERT INTO recipes (name, author, description, ingredients) VALUES
('Tomato Soup 1', 'Bob', 'A quick and easy recipe for busy nights.', 'milk, pepper, tofu, egg, pork, garlic'),
('Sushi Rolls 2', 'Bob', 'A hearty and delicious meal perfect for dinner.', 'butter, flour, tofu, onion, lettuce'),
('Pancakes 3', 'Edward', 'A fun and creative dish to try at home.', 'cheese, carrot, tofu, egg, chicken, flour'),
('Beef Stew 4', 'Ian', 'A tasty treat the whole family will love.', 'rice, garlic, tomato, beef, cheese, lettuce'),
('Spaghetti Bolognese 5', 'Julia', 'A warming dish great for cold days.', 'garlic, rice, beef, pork, tofu, salt'),
('Caesar Salad 6', 'Fiona', 'Simple yet satisfying comfort food.', 'lettuce, carrot, salt, pork, butter, beef'),
('Sushi Rolls 7', 'Fiona', 'A quick and easy recipe for busy nights.', 'salt, onion, beef'),
('Tacos 8', 'Edward', 'A healthy option packed with veggies.', 'flour, ginger, butter, tofu, pepper, potato'),
('Caesar Salad 9', 'George', 'A healthy option packed with veggies.', 'pepper, carrot, pork, potato, chicken'),
('Grilled Cheese 10', 'Ian', 'An all-time favorite snack or meal.', 'chicken, egg, salt, pork'),
('Beef Stew 11', 'Ian', 'A quick and easy recipe for busy nights.', 'noodles, milk, egg, lettuce'),
('Grilled Cheese 12', 'Charlie', 'A traditional dish with rich flavors.', 'milk, cheese, salt, butter'),
('Grilled Cheese 13', 'George', 'A warming dish great for cold days.', 'carrot, onion, tofu, potato, chicken'),
('Tomato Soup 14', 'Hannah', 'Simple yet satisfying comfort food.', 'chicken, milk, tomato, rice, butter, flour'),
('Chicken Curry 15', 'Alice', 'A warming dish great for cold days.', 'salt, pork, noodles, pepper'),
('Tomato Soup 16', 'George', 'A healthy option packed with veggies.', 'onion, butter, milk, tomato, lettuce, flour'),
('Grilled Cheese 17', 'Bob', 'A healthy option packed with veggies.', 'carrot, onion, cheese, butter');


