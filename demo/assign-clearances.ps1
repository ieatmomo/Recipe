# Assign ACG and COI to demo users
# Run this AFTER creating users with create-demo-users.ps1

$baseUrl = "http://localhost:8081/auth/admin"

# Get admin token first
Write-Host "Logging in as admin..." -ForegroundColor Cyan
$loginBody = @{ username = "admin@example.com"; password = "Admin123!" } | ConvertTo-Json
$adminToken = Invoke-RestMethod -Uri "http://localhost:8081/auth/generateToken" -Method POST -ContentType "application/json" -Body $loginBody -TimeoutSec 30
$headers = @{ Authorization = "Bearer $adminToken" }

Write-Host "Admin logged in successfully" -ForegroundColor Green
Write-Host ""

# Assign Alice - SECRET clearance
Write-Host "Assigning SECRET clearance to Alice..." -ForegroundColor Yellow
$body = @{ action = "ADD"; accessControlGroups = @("SECRET") } | ConvertTo-Json
try {
    Invoke-RestMethod -Uri "$baseUrl/user/alice@example.com/acg" -Method POST -Headers $headers -ContentType "application/json" -Body $body -TimeoutSec 30 | Out-Null
    Write-Host "Alice: SECRET clearance assigned" -ForegroundColor Green
} catch {
    Write-Host "Failed to assign ACG: $($_.Exception.Message)" -ForegroundColor Red
}

$body = @{ action = "ADD"; communitiesOfInterest = @("DESSERT") } | ConvertTo-Json
try {
    Invoke-RestMethod -Uri "$baseUrl/user/alice@example.com/coi" -Method POST -Headers $headers -ContentType "application/json" -Body $body -TimeoutSec 30 | Out-Null
    Write-Host "Alice: DESSERT COI assigned" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "Failed to assign COI: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

# Assign Bob - TOP_SECRET and SECRET clearance
Write-Host "Assigning TOP_SECRET + SECRET clearance to Bob..." -ForegroundColor Yellow
$body = @{ action = "ADD"; accessControlGroups = @("SECRET", "TOP_SECRET") } | ConvertTo-Json
try {
    Invoke-RestMethod -Uri "$baseUrl/user/bob@example.com/acg" -Method POST -Headers $headers -ContentType "application/json" -Body $body -TimeoutSec 30 | Out-Null
    Write-Host "Bob: SECRET, TOP_SECRET clearances assigned" -ForegroundColor Green
} catch {
    Write-Host "Failed to assign ACG: $($_.Exception.Message)" -ForegroundColor Red
}

$body = @{ action = "ADD"; communitiesOfInterest = @("BEEF") } | ConvertTo-Json
try {
    Invoke-RestMethod -Uri "$baseUrl/user/bob@example.com/coi" -Method POST -Headers $headers -ContentType "application/json" -Body $body -TimeoutSec 30 | Out-Null
    Write-Host "Bob: BEEF COI assigned" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "Failed to assign COI: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

# Assign Charlie - NATO clearance
Write-Host "Assigning NATO clearance to Charlie..." -ForegroundColor Yellow
$body = @{ action = "ADD"; accessControlGroups = @("NATO") } | ConvertTo-Json
try {
    Invoke-RestMethod -Uri "$baseUrl/user/charlie@example.com/acg" -Method POST -Headers $headers -ContentType "application/json" -Body $body -TimeoutSec 30 | Out-Null
    Write-Host "Charlie: NATO clearance assigned" -ForegroundColor Green
} catch {
    Write-Host "Failed to assign ACG: $($_.Exception.Message)" -ForegroundColor Red
}

$body = @{ action = "ADD"; communitiesOfInterest = @("CHICKEN") } | ConvertTo-Json
try {
    Invoke-RestMethod -Uri "$baseUrl/user/charlie@example.com/coi" -Method POST -Headers $headers -ContentType "application/json" -Body $body -TimeoutSec 30 | Out-Null
    Write-Host "Charlie: CHICKEN COI assigned" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "Failed to assign COI: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

# Assign Dave - NO ACG (public only), but with COIs
Write-Host "Assigning COIs to Dave (no ACG clearance)..." -ForegroundColor Yellow
$body = @{ action = "ADD"; communitiesOfInterest = @("VEGETARIAN") } | ConvertTo-Json
try {
    Invoke-RestMethod -Uri "$baseUrl/user/dave@example.com/coi" -Method POST -Headers $headers -ContentType "application/json" -Body $body -TimeoutSec 30 | Out-Null
    Write-Host "Dave: VEGETARIAN COI assigned (no ACG - public access only)" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "Failed to assign COI: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "All clearances assigned successfully!" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "Summary:" -ForegroundColor Yellow
Write-Host "• Alice: SECRET clearance → Can see public + SECRET recipes"
Write-Host "• Bob: SECRET + TOP_SECRET clearance → Can see public + SECRET + TOP_SECRET recipes"
Write-Host "• Charlie: NATO clearance → Can see public + NATO recipes"
Write-Host "• Dave: No clearance → Can see public recipes only"
