# Script de test des endpoints backend
# Usage: .\test-backend-endpoints.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Test des endpoints Backend TPE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8080/api/tpe-posting"

# Test 1: Verify TPE
Write-Host "[1/3] Test de verify-tpe..." -ForegroundColor Yellow
Write-Host "   URL: $baseUrl/verify-tpe/123456789" -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/verify-tpe/123456789" -Method Get -TimeoutSec 10
    Write-Host "✓ Réponse reçue:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 10 | Write-Host
    Write-Host ""
} catch {
    if ($_.Exception.Response.StatusCode -eq 404) {
        Write-Host "✓ Endpoint accessible (TPE non trouvé - normal en mode simulation)" -ForegroundColor Green
        Write-Host "   Message: $($_.ErrorDetails.Message)" -ForegroundColor Yellow
    } else {
        Write-Host "✗ Erreur: $($_.Exception.Message)" -ForegroundColor Red
    }
    Write-Host ""
}

# Test 2: Verify Porteur
Write-Host "[2/3] Test de verify-porteur..." -ForegroundColor Yellow
Write-Host "   URL: $baseUrl/verify-porteur/4000000000000001" -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/verify-porteur/4000000000000001" -Method Get -TimeoutSec 10
    Write-Host "✓ Réponse reçue:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 10 | Write-Host
    Write-Host ""
} catch {
    if ($_.Exception.Response.StatusCode -eq 404) {
        Write-Host "✓ Endpoint accessible (Porteur non trouvé - normal en mode simulation)" -ForegroundColor Green
        Write-Host "   Message: $($_.ErrorDetails.Message)" -ForegroundColor Yellow
    } else {
        Write-Host "✗ Erreur: $($_.Exception.Message)" -ForegroundColor Red
    }
    Write-Host ""
}

# Test 3: Insert Postings
Write-Host "[3/3] Test de insert-postings..." -ForegroundColor Yellow
Write-Host "   URL: $baseUrl/insert-postings" -ForegroundColor Cyan

$testData = @(
    @{
        date_v = "2025-01-15"
        time_t = "09:00:00"
        branch = "001"
        partie = "PORTEUR"
        profit_centre = "100"
        n_compt = "001-100-CLIENT001-12345678"
        deal_no = "DEAL-TEST-001"
        amount = 100.000
        devise = "TND"
        dt_ct = "D"
        client_id = "CLIENT001"
        n_carte = "4000000000000001"
        n_affiliation = "123456789"
    },
    @{
        date_v = "2025-01-15"
        time_t = "09:00:00"
        branch = "001"
        partie = "TPE"
        profit_centre = "200"
        n_compt = "001-200-CLIENT002-87654321"
        deal_no = "DEAL-TEST-001"
        amount = 100.000
        devise = "TND"
        dt_ct = "C"
        client_id = "CLIENT002"
        n_carte = "4000000000000001"
        n_affiliation = "123456789"
    }
)

$jsonBody = $testData | ConvertTo-Json -Depth 10

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/insert-postings" `
                                  -Method Post `
                                  -Body $jsonBody `
                                  -ContentType "application/json" `
                                  -TimeoutSec 10
    Write-Host "✓ Réponse reçue:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 10 | Write-Host
    Write-Host ""
} catch {
    Write-Host "✗ Erreur: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails) {
        Write-Host "   Détails: $($_.ErrorDetails.Message)" -ForegroundColor Yellow
    }
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Tests terminés" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Notes:" -ForegroundColor Yellow
Write-Host "  - Si le backend n'est pas démarré, tous les tests échoueront" -ForegroundColor Yellow
Write-Host "  - En mode simulation, les verify endpoints retournent des données par défaut" -ForegroundColor Yellow
Write-Host "  - Le endpoint insert-postings nécessite une connexion DB active" -ForegroundColor Yellow
Write-Host ""
Write-Host "Commandes curl équivalentes:" -ForegroundColor Magenta
Write-Host "  curl http://localhost:8080/api/tpe-posting/verify-tpe/123456789" -ForegroundColor Cyan
Write-Host "  curl http://localhost:8080/api/tpe-posting/verify-porteur/4000000000000001" -ForegroundColor Cyan
Write-Host "  curl -X POST http://localhost:8080/api/tpe-posting/insert-postings -H 'Content-Type: application/json' -d '@test-data.json'" -ForegroundColor Cyan
Write-Host ""
