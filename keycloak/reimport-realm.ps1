param(
    [string]$KeycloakUrl = "http://localhost:8180",
    [string]$AdminUser = "admin",
    [string]$AdminPassword = "admin123",
    [string]$RealmFile = ".\keycloak\realm-import.json"
)

Write-Host "Keycloak Realm Re-import Script" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan

# Get admin token from master realm
Write-Host "`nGetting admin token..." -ForegroundColor Yellow
$tokenResponse = Invoke-RestMethod -Uri "$KeycloakUrl/realms/master/protocol/openid-connect/token" `
    -Method POST `
    -ContentType "application/x-www-form-urlencoded" `
    -Body "username=$AdminUser&password=$AdminPassword&grant_type=password&client_id=admin-cli"

$adminToken = $tokenResponse.access_token
Write-Host "Admin token obtained successfully" -ForegroundColor Green

# Delete existing recipe realm
Write-Host "`nDeleting existing 'recipe' realm..." -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "$KeycloakUrl/admin/realms/recipe" `
        -Method DELETE `
        -Headers @{Authorization = "Bearer $adminToken"}
    Write-Host "Recipe realm deleted successfully" -ForegroundColor Green
} catch {
    Write-Host "Realm may not exist or already deleted" -ForegroundColor Yellow
}

# Wait a moment for deletion to complete
Start-Sleep -Seconds 2

# Import realm from JSON file
Write-Host "`nImporting realm from $RealmFile..." -ForegroundColor Yellow
$realmJson = Get-Content $RealmFile -Raw
Invoke-RestMethod -Uri "$KeycloakUrl/admin/realms" `
    -Method POST `
    -Headers @{
        Authorization = "Bearer $adminToken"
        "Content-Type" = "application/json"
    } `
    -Body $realmJson

Write-Host "`nRealm imported successfully!" -ForegroundColor Green
Write-Host "`nTest Users:" -ForegroundColor Cyan
Write-Host "  admin@example.com / Admin123! (ROLE_ADMIN)" -ForegroundColor White
Write-Host "  alice@example.com / Alice123! (ROLE_USER, ACG: PUBLIC)" -ForegroundColor White
Write-Host "  bob@example.com / Bob123! (ROLE_USER, ACG: PUBLIC,CONFIDENTIAL)" -ForegroundColor White
Write-Host "`nKeycloak is ready to use!" -ForegroundColor Green
