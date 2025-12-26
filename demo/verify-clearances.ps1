# Verify user clearances via API (NO AUTHENTICATION REQUIRED)
$baseUrl = "http://localhost:8090"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Verifying User Clearances (Public API)" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$users = @(
    @{ email = "alice@example.com"; name = "Alice"; expectedACG = "SECRET"; expectedCOI = "DESSERT" }
    @{ email = "bob@example.com"; name = "Bob"; expectedACG = "SECRET, TOP_SECRET"; expectedCOI = "BEEF" }
    @{ email = "charlie@example.com"; name = "Charlie"; expectedACG = "NATO"; expectedCOI = "CHICKEN" }
    @{ email = "dave@example.com"; name = "Dave"; expectedACG = "None"; expectedCOI = "VEGETARIAN" }
    @{ email = "admin@example.com"; name = "Admin"; expectedACG = "None"; expectedCOI = "None" }
)

foreach ($user in $users) {
    Write-Host "$($user.name) ($($user.email)):" -ForegroundColor Yellow
    
    try {
        # Get ACG - NO AUTH REQUIRED
        $acg = Invoke-RestMethod -Uri "$baseUrl/auth/user/$($user.email)/acg" -Method GET -TimeoutSec 30
        if ($acg -and $acg.Count -gt 0) {
            Write-Host "  ✓ ACG: $($acg -join ', ')" -ForegroundColor Green
        } else {
            Write-Host "  ✓ ACG: [None - Public access only]" -ForegroundColor Gray
        }
        
        # Get COI - NO AUTH REQUIRED
        $coi = Invoke-RestMethod -Uri "$baseUrl/auth/user/$($user.email)/coi" -Method GET -TimeoutSec 30
        if ($coi -and $coi.Count -gt 0) {
            Write-Host "  ✓ COI: $($coi -join ', ')" -ForegroundColor Green
        } else {
            Write-Host "  ✓ COI: [None]" -ForegroundColor Gray
        }
    }
    catch {
        Write-Host "  ✗ Error: $_" -ForegroundColor Red
    }
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Green
Write-Host "Verification Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "`nNote: These are PUBLIC endpoints - no authentication required!" -ForegroundColor Cyan
