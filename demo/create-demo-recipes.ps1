# Create 20 demo recipes covering all access levels and categories
$baseUrl = "http://localhost:8082"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Creating 20 Demo Recipes" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Login as admin to get token
Write-Host "Logging in as admin..." -ForegroundColor Yellow
$loginBody = @{ username = "admin@example.com"; password = "Admin123!" } | ConvertTo-Json
$adminToken = Invoke-RestMethod -Uri "http://localhost:8081/auth/generateToken" -Method POST -ContentType "application/json" -Body $loginBody -TimeoutSec 30
$headers = @{ Authorization = "Bearer $adminToken" }
Write-Host "Admin logged in successfully`n" -ForegroundColor Green

# Define access levels and categories
$accessLevels = @(
    @{ name = "PUBLIC"; acg = @() },
    @{ name = "RESTRICTED"; acg = @("RESTRICTED") },
    @{ name = "SECRET"; acg = @("SECRET") },
    @{ name = "TOP_SECRET"; acg = @("TOP_SECRET") }
)

$categories = @(
    @{ name = "BEEF"; description = "beef-based dish" },
    @{ name = "CHICKEN"; description = "chicken-based dish" },
    @{ name = "DESSERT"; description = "sweet dessert" },
    @{ name = "SEAFOOD"; description = "seafood dish" },
    @{ name = "VEGETARIAN"; description = "vegetarian dish" }
)

# Recipe templates with different names and ingredients
$recipeTemplates = @{
    "BEEF" = @(
        @{ name = "Beef Wellington"; ingredients = "beef tenderloin, puff pastry, mushroom duxelles, prosciutto" },
        @{ name = "Beef Stroganoff"; ingredients = "beef strips, mushrooms, sour cream, onions" },
        @{ name = "Beef Tacos"; ingredients = "ground beef, taco shells, lettuce, cheese, salsa" },
        @{ name = "Beef Stir Fry"; ingredients = "beef slices, vegetables, soy sauce, ginger" }
    )
    "CHICKEN" = @(
        @{ name = "Chicken Parmesan"; ingredients = "chicken breast, marinara sauce, mozzarella, parmesan" },
        @{ name = "Chicken Alfredo"; ingredients = "chicken breast, fettuccine, cream, parmesan" },
        @{ name = "Grilled Chicken"; ingredients = "chicken breast, olive oil, herbs, lemon" },
        @{ name = "Chicken Tikka Masala"; ingredients = "chicken, yogurt, tomato sauce, spices" }
    )
    "DESSERT" = @(
        @{ name = "Chocolate Cake"; ingredients = "flour, cocoa powder, eggs, sugar, butter" },
        @{ name = "Tiramisu"; ingredients = "ladyfingers, mascarpone, espresso, cocoa" },
        @{ name = "Creme Brulee"; ingredients = "cream, egg yolks, sugar, vanilla" },
        @{ name = "Apple Pie"; ingredients = "apples, pie crust, cinnamon, sugar" }
    )
    "SEAFOOD" = @(
        @{ name = "Grilled Salmon"; ingredients = "salmon fillet, lemon, dill, olive oil" },
        @{ name = "Shrimp Scampi"; ingredients = "shrimp, garlic, butter, white wine, pasta" },
        @{ name = "Fish Tacos"; ingredients = "white fish, cabbage slaw, tortillas, lime" },
        @{ name = "Lobster Bisque"; ingredients = "lobster, cream, brandy, tomato paste" }
    )
    "VEGETARIAN" = @(
        @{ name = "Vegetable Stir Fry"; ingredients = "mixed vegetables, tofu, soy sauce, sesame oil" },
        @{ name = "Margherita Pizza"; ingredients = "pizza dough, tomato sauce, mozzarella, basil" },
        @{ name = "Vegetable Curry"; ingredients = "mixed vegetables, coconut milk, curry spices" },
        @{ name = "Greek Salad"; ingredients = "cucumber, tomatoes, feta, olives, olive oil" }
    )
}

$recipeCount = 0

# Create recipes for each access level
foreach ($accessLevel in $accessLevels) {
    Write-Host "`n--- Creating $($accessLevel.name) Recipes ---" -ForegroundColor Cyan
    
    $categoryIndex = 0
    foreach ($category in $categories) {
        $template = $recipeTemplates[$category.name][$accessLevels.IndexOf($accessLevel)]
        
        $recipeName = "[$($accessLevel.name)] $($template.name)"
        
        $recipeBody = @{
            name = $recipeName
            ingredients = $template.ingredients
            instructions = "1. Prepare ingredients. 2. Cook according to traditional methods. 3. Serve hot and enjoy!"
            accessControlGroups = $accessLevel.acg
            communityTags = @($category.name)
        } | ConvertTo-Json
        
        try {
            $response = Invoke-RestMethod -Uri "$baseUrl/recipe/addRecipe" -Method POST -Headers $headers -ContentType "application/json" -Body $recipeBody -TimeoutSec 30
            $recipeCount++
            Write-Host "  Created: $recipeName (COI: $($category.name))" -ForegroundColor Green
        }
        catch {
            Write-Host "  Failed to create $recipeName : $_" -ForegroundColor Red
        }
        
        $categoryIndex++
    }
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Recipe Creation Complete!" -ForegroundColor Green
Write-Host "Created $recipeCount recipes" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`nSummary:" -ForegroundColor Yellow
Write-Host "  4 Access Levels: PUBLIC, RESTRICTED, SECRET, TOP_SECRET" -ForegroundColor White
Write-Host "  5 Categories: BEEF, CHICKEN, DESSERT, SEAFOOD, VEGETARIAN" -ForegroundColor White
Write-Host "  Total: 20 recipes covering all combinations" -ForegroundColor White
