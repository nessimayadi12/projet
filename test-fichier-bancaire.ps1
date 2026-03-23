# Script de test pour l'API Fichier Bancaire
# Usage: .\test-fichier-bancaire.ps1

$baseUrl = "http://localhost:8080/api"
$fichierBancaireUrl = "$baseUrl/fichier-bancaire"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   TEST API FICHIER BANCAIRE TPE      " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Fonction pour afficher les résultats
function Show-Result {
    param (
        [string]$TestName,
        [object]$Response,
        [bool]$Success
    )
    
    Write-Host "📋 Test: $TestName" -ForegroundColor Yellow
    
    if ($Success) {
        Write-Host "✅ Succès" -ForegroundColor Green
    } else {
        Write-Host "❌ Échec" -ForegroundColor Red
    }
    
    if ($Response) {
        Write-Host "Réponse:" -ForegroundColor Gray
        $Response | ConvertTo-Json -Depth 3 | Write-Host -ForegroundColor White
    }
    
    Write-Host ""
}

# Test 1: Vérifier que l'API est accessible
Write-Host "🔍 Test 1: Vérification de l'API..." -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$fichierBancaireUrl/test" -Method Get
    Show-Result -TestName "Ping API" -Response $response -Success $true
} catch {
    Show-Result -TestName "Ping API" -Response $_.Exception.Message -Success $false
    Write-Host "❌ L'API n'est pas accessible. Vérifiez que le backend est démarré." -ForegroundColor Red
    exit 1
}

# Test 2: Créer un fichier de test
Write-Host "📝 Test 2: Création d'un fichier de test..." -ForegroundColor Cyan

$sessionDate = (Get-Date -Format "yyyyMMdd")
$testFilePath = Join-Path $PSScriptRoot "test_fichier_bancaire_auto.txt"

# Créer un fichier de test avec des lignes valides
$ligneType10 = "10              1234567890                        Test Commission TPE 001                                                                                                                                                                          " + ("0" * 12) + "000000001000" + (" " * 5) + ("0" * 12) + "000000000500"
$ligneType20 = "20              1234567890                    Test Payment                                                        1234567890123456                                    " + $sessionDate.Substring(2) + "123456              000000002000"

# Assurer que les lignes font au moins 250 caractères
$ligneType10 = $ligneType10.PadRight(260)
$ligneType20 = $ligneType20.PadRight(260)

$ligneType10, $ligneType20 | Out-File -FilePath $testFilePath -Encoding UTF8

Write-Host "✅ Fichier de test créé: $testFilePath" -ForegroundColor Green
Write-Host "   - Lignes: 2" -ForegroundColor Gray
Write-Host "   - Type 10 (Commission): 1 ligne" -ForegroundColor Gray
Write-Host "   - Type 20 (Paiement): 1 ligne" -ForegroundColor Gray
Write-Host ""

# Test 3: Upload du fichier
Write-Host "📤 Test 3: Upload du fichier bancaire..." -ForegroundColor Cyan

try {
    $form = @{
        file = Get-Item -Path $testFilePath
        sessionDate = $sessionDate
    }
    
    $response = Invoke-RestMethod -Uri "$fichierBancaireUrl/upload" -Method Post -Form $form
    Show-Result -TestName "Upload Fichier" -Response $response -Success $response.success
    
    if ($response.success) {
        Write-Host "📊 Statistiques:" -ForegroundColor Cyan
        Write-Host "   - Lignes lues: $($response.lignesLues)" -ForegroundColor White
        Write-Host "   - Écritures créées: $($response.ecrituresCreees)" -ForegroundColor White
        Write-Host "   - Session date: $($response.sessionDate)" -ForegroundColor White
    }
} catch {
    $errorDetails = $_.Exception.Message
    if ($_.ErrorDetails) {
        $errorDetails = $_.ErrorDetails.Message | ConvertFrom-Json
    }
    Show-Result -TestName "Upload Fichier" -Response $errorDetails -Success $false
    
    Write-Host "⚠️  Causes possibles:" -ForegroundColor Yellow
    Write-Host "   - TPE avec numero_terminal='1234567890' n'existe pas dans la base" -ForegroundColor Gray
    Write-Host "   - Le TPE n'est pas affecté à un commerçant" -ForegroundColor Gray
    Write-Host "   - Le commerçant n'a pas de numero_compte" -ForegroundColor Gray
}

Write-Host ""

# Test 4: Récupérer les statistiques
Write-Host "📊 Test 4: Récupération des statistiques..." -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri "$fichierBancaireUrl/stats/$sessionDate" -Method Get
    Show-Result -TestName "Statistiques" -Response $response -Success $response.success
} catch {
    Show-Result -TestName "Statistiques" -Response $_.Exception.Message -Success $false
}

# Test 5: Vérifier les écritures dans TPE_POSTING_comp
Write-Host "💾 Test 5: Vérification des écritures..." -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/tpe-posting/recent?limit=10" -Method Get
    
    if ($response -and $response.Count -gt 0) {
        Write-Host "✅ Écritures récupérées: $($response.Count)" -ForegroundColor Green
        
        # Afficher les 3 premières
        Write-Host "📋 Aperçu des écritures:" -ForegroundColor Cyan
        $response | Select-Object -First 3 | ForEach-Object {
            Write-Host "   - Branch: $($_.branch) | Client: $($_.client) | Account: $($_.account)" -ForegroundColor White
            Write-Host "     Amount: $($_.amount) | CR/DR: $($_.crDr) | Date: $($_.date)" -ForegroundColor Gray
            Write-Host "     Narrative: $($_.narrative)" -ForegroundColor Gray
            Write-Host ""
        }
    } else {
        Write-Host "⚠️  Aucune écriture trouvée" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Erreur lors de la récupération des écritures" -ForegroundColor Red
    Write-Host "   $($_.Exception.Message)" -ForegroundColor Gray
}

Write-Host ""

# Test 6: Suggestions pour les données de test
Write-Host "💡 Test 6: Vérification des données de test..." -ForegroundColor Cyan
Write-Host ""
Write-Host "Pour que les tests fonctionnent, assurez-vous d'avoir:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1️⃣  Un TPE avec numero_terminal = '1234567890'" -ForegroundColor White
Write-Host "   SQL: " -ForegroundColor Gray
Write-Host "   INSERT INTO tpes (numero_serie, numero_terminal, type_tpe, statut, marque, modele, commercant_id)" -ForegroundColor Gray
Write-Host "   VALUES ('TEST001', '1234567890', 'PHYSIQUE', 'AFFECTE', 'Ingenico', 'iWL250', 1);" -ForegroundColor Gray
Write-Host ""

Write-Host "2️⃣  Un commerçant avec un numero_compte valide" -ForegroundColor White
Write-Host "   SQL: " -ForegroundColor Gray
Write-Host "   INSERT INTO commercants (raison_sociale, activite, numero_compte, code_agence, telephone, email, statut)" -ForegroundColor Gray
Write-Host "   VALUES ('Test Merchant', 'Commerce', '12345678901234567890', '041', '0612345678', 'test@test.com', 'ACTIF');" -ForegroundColor Gray
Write-Host ""

Write-Host "3️⃣  Mise à jour du TPE pour l'affecter au commerçant" -ForegroundColor White
Write-Host "   SQL: " -ForegroundColor Gray
Write-Host "   UPDATE tpes SET commercant_id = 1 WHERE numero_terminal = '1234567890';" -ForegroundColor Gray
Write-Host ""

# Résumé
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "           RÉSUMÉ DES TESTS            " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "✅ API accessible" -ForegroundColor Green
Write-Host "✅ Fichier de test créé" -ForegroundColor Green
Write-Host ""
Write-Host "📝 Fichier de test: $testFilePath" -ForegroundColor White
Write-Host "📅 Session date: $sessionDate" -ForegroundColor White
Write-Host ""
Write-Host "🔗 Endpoints testés:" -ForegroundColor Cyan
Write-Host "   - GET  $fichierBancaireUrl/test" -ForegroundColor Gray
Write-Host "   - POST $fichierBancaireUrl/upload" -ForegroundColor Gray
Write-Host "   - GET  $fichierBancaireUrl/stats/{date}" -ForegroundColor Gray
Write-Host "   - GET  $baseUrl/tpe-posting/recent" -ForegroundColor Gray
Write-Host ""

# Nettoyage optionnel
Write-Host "🗑️  Nettoyer le fichier de test? (O/N): " -ForegroundColor Yellow -NoNewline
$cleanup = Read-Host

if ($cleanup -eq "O" -or $cleanup -eq "o") {
    Remove-Item -Path $testFilePath -Force
    Write-Host "✅ Fichier supprimé" -ForegroundColor Green
} else {
    Write-Host "ℹ️  Fichier conservé pour inspection" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "         TESTS TERMINÉS                " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
