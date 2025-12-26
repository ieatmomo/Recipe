$KeycloakUrl = 'http://localhost:8180'
$AdminUser = 'admin'
$AdminPassword = 'admin123'
$RealmFile = '.\keycloak\realm-import.json'

Write-Host 'Waiting for Keycloak...' -ForegroundColor Yellow
$ready = $false
for ($i = 0; $i -lt 60 -and -not $ready; $i++) {
    try { $r = Invoke-WebRequest -Uri "$KeycloakUrl/health/ready" -UseBasicParsing -TimeoutSec 2; if ($r.StatusCode -eq 200) { $ready = $true } } catch { Start-Sleep -Seconds 5 }
}
if (-not $ready) { Write-Host 'ERROR: Keycloak not ready' -ForegroundColor Red; exit 1 }
Write-Host 'Keycloak ready!' -ForegroundColor Green

Write-Host 'Getting token...' -ForegroundColor Yellow
$tokenBody = @{ username = $AdminUser; password = $AdminPassword; grant_type = 'password'; client_id = 'admin-cli' }
$tokenResponse = Invoke-RestMethod -Uri "$KeycloakUrl/realms/master/protocol/openid-connect/token" -Method POST -ContentType 'application/x-www-form-urlencoded' -Body $tokenBody
$accessToken = $tokenResponse.access_token
Write-Host 'Got token!' -ForegroundColor Green

$headers = @{ Authorization = "Bearer $accessToken" }
try { Invoke-RestMethod -Uri "$KeycloakUrl/admin/realms/recipe" -Headers $headers -Method DELETE; Write-Host 'Deleted old realm' -ForegroundColor Yellow } catch {}

Write-Host 'Importing realm...' -ForegroundColor Yellow
$realmConfig = Get-Content -Path $RealmFile -Raw
$headers['Content-Type'] = 'application/json'
Invoke-RestMethod -Uri "$KeycloakUrl/admin/realms" -Headers $headers -Method POST -Body $realmConfig
Write-Host 'Realm imported!' -ForegroundColor Green
Write-Host 'Login: admin@example.com / Admin123!' -ForegroundColor Cyan
