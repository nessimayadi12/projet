# Script de test pour la génération PDF
# test-pdf-generation.ps1

param(
    [string]$SessionDate = "20260224",
    [string]$BackendUrl = "http://localhost:8080"
)

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  TEST GÉNÉRATION PDF - FICHIER BANCAIRE" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Configuration
$testEndpoint = "$BackendUrl/api/fichier-bancaire/test"
$pdfEndpoint = "$BackendUrl/api/fichier-bancaire/rapport/pdf/$SessionDate"
$statsEndpoint = "$BackendUrl/api/fichier-bancaire/stats/$SessionDate"
$outputPdf = "rapport_test_$SessionDate.pdf"

# Fonction pour afficher un résultat
function Show-Result {
    param([bool]$Success, [string]$Message)
    if ($Success) {
        Write-Host "✅ $Message" -ForegroundColor Green
    } else {
        Write-Host "❌ $Message" -ForegroundColor Red
    }
}

# Test 1: Vérifier que le backend est démarré
Write-Host "🔍 Test 1: Vérification du backend..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri $testEndpoint -Method GET -ErrorAction Stop
    Show-Result -Success $true -Message "Backend accessible: $($response.message)"
} catch {
    Show-Result -Success $false -Message "Backend non accessible sur $BackendUrl"
    Write-Host "   Assurez-vous que le backend est démarré avec: mvn spring-boot:run" -ForegroundColor Gray
    exit 1
}

# Test 2: Vérifier les statistiques de la session
Write-Host "`n🔍 Test 2: Vérification des données de session $SessionDate..." -ForegroundColor Yellow
try {
    $stats = Invoke-RestMethod -Uri $statsEndpoint -Method GET -ErrorAction Stop
    
    if ($stats.success) {
        Show-Result -Success $true -Message "Session trouvée avec $($stats.transactionCount) écritures"
        
        if ($stats.transactionCount -eq 0) {
            Write-Host "   ⚠️  Attention: Aucune écriture pour cette session" -ForegroundColor Yellow
            Write-Host "   Le PDF sera généré mais sera vide" -ForegroundColor Yellow
        }
    } else {
        Show-Result -Success $false -Message "Erreur lors de la récupération des stats"
    }
} catch {
    Show-Result -Success $false -Message "Impossible de récupérer les statistiques"
    Write-Host "   Erreur: $_" -ForegroundColor Gray
}

# Test 3: Générer le PDF
Write-Host "`n🔍 Test 3: Génération du rapport PDF..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri $pdfEndpoint -Method GET -UseBasicParsing -ErrorAction Stop
    
    if ($response.StatusCode -eq 200) {
        # Sauvegarder le PDF
        [System.IO.File]::WriteAllBytes($outputPdf, $response.Content)
        
        $sizeKB = [math]::Round($response.Content.Length / 1024, 2)
        Show-Result -Success $true -Message "PDF généré avec succès"
        Write-Host "   📄 Fichier: $outputPdf" -ForegroundColor Cyan
        Write-Host "   📊 Taille: $sizeKB KB" -ForegroundColor Cyan
        Write-Host "   🎯 Content-Type: $($response.Headers['Content-Type'])" -ForegroundColor Cyan
        
        # Vérifier que c'est bien un PDF
        $pdfHeader = $response.Content[0..3]
        if ($pdfHeader[0] -eq 0x25 -and $pdfHeader[1] -eq 0x50 -and $pdfHeader[2] -eq 0x44 -and $pdfHeader[3] -eq 0x46) {
            Write-Host "   ✅ Signature PDF valide (%PDF)" -ForegroundColor Green
        } else {
            Write-Host "   ⚠️  Attention: La signature PDF semble incorrecte" -ForegroundColor Yellow
        }
        
        # Ouvrir le PDF automatiquement
        Write-Host "`n📂 Ouverture du PDF..." -ForegroundColor Cyan
        Start-Process $outputPdf
        
    } else {
        Show-Result -Success $false -Message "Code HTTP: $($response.StatusCode)"
    }
} catch {
    Show-Result -Success $false -Message "Erreur lors de la génération du PDF"
    Write-Host "   Erreur: $_" -ForegroundColor Gray
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "   Code HTTP: $statusCode" -ForegroundColor Gray
        
        switch ($statusCode) {
            404 { Write-Host "   → Endpoint non trouvé. Vérifiez le contrôleur" -ForegroundColor Gray }
            500 { Write-Host "   → Erreur serveur. Vérifiez les logs backend" -ForegroundColor Gray }
        }
    }
    exit 1
}

# Test 4: Tests frontend (si Angular est démarré)
Write-Host "`n🔍 Test 4: Vérification du frontend Angular..." -ForegroundColor Yellow
try {
    $frontendUrl = "http://localhost:4200"
    $response = Invoke-WebRequest -Uri $frontendUrl -Method GET -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop
    Show-Result -Success $true -Message "Frontend accessible sur $frontendUrl"
    Write-Host "   Vous pouvez tester l'interface web:" -ForegroundColor Cyan
    Write-Host "   → $frontendUrl/#/upload-fichier-bancaire" -ForegroundColor Cyan
} catch {
    Show-Result -Success $false -Message "Frontend non accessible"
    Write-Host "   Pour démarrer le frontend: cd 'front end'; npm start" -ForegroundColor Gray
}

# Résumé
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  RÉSUMÉ DES TESTS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Session testée : $SessionDate" -ForegroundColor White
Write-Host "Backend        : $BackendUrl" -ForegroundColor White
Write-Host "PDF généré     : $outputPdf" -ForegroundColor White
Write-Host "`n💡 Conseils :" -ForegroundColor Yellow
Write-Host "  - Vérifiez les logs backend pour plus de détails" -ForegroundColor Gray
Write-Host "  - Testez avec différentes sessions " -ForegroundColor Gray
Write-Host "  - Utilisez l'interface web pour un test complet" -ForegroundColor Gray
Write-Host "`n✅ Tests terminés !`n" -ForegroundColor Green
