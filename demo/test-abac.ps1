# Test ABAC filtering for each user
$baseUrl = "http://localhost:8080"

function Test-UserAccess {
    param($email, $password, $name)
    
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "Testing $name ($email)" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    
    # Login
    $loginBody = @{
        username = $email
        password = $password
    } | ConvertTo-Json
    
    try {
        $token = Invoke-RestMethod -Uri "$baseUrl/auth/generateToken" -Method POST -ContentType "application/json" -Body $loginBody -TimeoutSec 30
        Write-Host "âœ" Logged in successfully" -ForegroundColor Green
        
        # Get recipes
        $headers = @{ Authorization = "Bearer $token" }
        $recipes = Invoke-RestMethod -Uri "$baseUrl/recipe/all" -Method GET -Headers $headers -TimeoutSec 30
        
        Write-Host "âœ" Can see $($recipes.Count) recipes:" -ForegroundColor Green
        foreach ($recipe in $recipes | Sort-Object -Property name) {
            $acgDisplay = if ($recipe.accessControlGroups -and $recipe.accessControlGroups.Count -gt 0) { 
                "[$($recipe.accessControlGroups -join ', ')]" 
            } else { 
                "[PUBLIC]" 
            }
            $coiDisplay = if ($recipe.communitiesOfInterest -and $recipe.communitiesOfInterest.Count -gt 0) {
                "[$($recipe.communitiesOfInterest -join ', ')]"
            } else {
                "[No COI]"
            }
            Write-Host "  â€¢ $($recipe.name) $acgDisplay $coiDisplay" -ForegroundColor White
        }
    }
    catch {
        Write-Host "âœ— Error: $_" -ForegroundColor Red
    }
}

# Test each user
Test-UserAccess "alice@example.com" "Alice123!" "Alice (SECRET + DESSERT)"
Test-UserAccess "bob@example.com" "Bob123!" "Bob (SECRET, TOP_SECRET + BEEF)"
Test-UserAccess "charlie@example.com" "Charlie123!" "Charlie (NATO + CHICKEN)"
Test-UserAccess "dave@example.com" "Dave123!" "Dave (No ACG + VEGETARIAN)"
Test-UserAccess "admin@example.com" "Admin123!" "Admin (ADMIN role + EU)"

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "ABAC Testing Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
