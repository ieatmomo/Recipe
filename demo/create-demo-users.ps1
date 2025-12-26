# Create Demo Users Script
# This script properly creates demo users via the API with valid BCrypt passwords

$baseUrl = "http://localhost:8081"

Write-Host "Creating demo users..." -ForegroundColor Cyan

# User 1: Alice (SECRET clearance)
Write-Host "`nCreating Alice..." -ForegroundColor Yellow
$alice = @{
    name = "Alice Anderson"
    email = "alice@example.com"
    password = "Alice123!"
    roles = "ROLE_USER"
    region = "US"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/auth/addNewUser" -Method POST -ContentType "application/json" -Body $alice -TimeoutSec 30
    Write-Host "✓ Alice created successfully" -ForegroundColor Green
} catch {
    Write-Host "✗ Failed to create Alice: $($_.Exception.Message)" -ForegroundColor Red
}

# User 2: Bob (TOP_SECRET clearance)
Write-Host "`nCreating Bob..." -ForegroundColor Yellow
$bob = @{
    name = "Bob Brown"
    email = "bob@example.com"
    password = "Bob456!"
    roles = "ROLE_USER"
    region = "US"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/auth/addNewUser" -Method POST -ContentType "application/json" -Body $bob -TimeoutSec 30
    Write-Host "✓ Bob created successfully" -ForegroundColor Green
} catch {
    Write-Host "✗ Failed to create Bob: $($_.Exception.Message)" -ForegroundColor Red
}

# User 3: Charlie (NATO clearance)
Write-Host "`nCreating Charlie..." -ForegroundColor Yellow
$charlie = @{
    name = "Charlie Chen"
    email = "charlie@example.com"
    password = "Charlie789!"
    roles = "ROLE_USER"
    region = "EU"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/auth/addNewUser" -Method POST -ContentType "application/json" -Body $charlie -TimeoutSec 30
    Write-Host "✓ Charlie created successfully" -ForegroundColor Green
} catch {
    Write-Host "✗ Failed to create Charlie: $($_.Exception.Message)" -ForegroundColor Red
}

# User 4: Dave (no clearance)
Write-Host "`nCreating Dave..." -ForegroundColor Yellow
$dave = @{
    name = "Dave Davis"
    email = "dave@example.com"
    password = "Dave012!"
    roles = "ROLE_USER"
    region = "US"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/auth/addNewUser" -Method POST -ContentType "application/json" -Body $dave -TimeoutSec 30
    Write-Host "✓ Dave created successfully" -ForegroundColor Green
} catch {
    Write-Host "✗ Failed to create Dave: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Demo users created!" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "Now you need to login as admin and assign clearances:" -ForegroundColor Yellow
Write-Host "1. Login as admin to get JWT token"
Write-Host "2. Use the token to assign ACG clearances to users"
Write-Host "3. See demo/CREATE_DEMO_USERS_POSTMAN.md for details"
