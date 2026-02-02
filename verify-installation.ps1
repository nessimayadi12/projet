# Script de vérification post-installation (PowerShell)
# Système de Demande d'Affectation TPE

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Vérification du Système TPE" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# Compteurs
$TestsPassed = 0
$TestsFailed = 0

# Configuration
$ApiUrl = "http://localhost:8080/api"
$FrontendUrl = "http://localhost:4200"

# Fonction de test HTTP
function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Url,
        [int]$ExpectedCode
    )
    
    Write-Host "Test: $Name ... " -NoNewline
    
    try {
        $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -ErrorAction Stop
        $actualCode = $response.StatusCode
        
        if ($actualCode -eq $ExpectedCode) {
            Write-Host "✓ PASS" -ForegroundColor Green -NoNewline
            Write-Host " (Code: $actualCode)"
            return $true
        } else {
            Write-Host "✗ FAIL" -ForegroundColor Red -NoNewline
            Write-Host " (Attendu: $ExpectedCode, Reçu: $actualCode)"
            return $false
        }
    }
    catch {
        $actualCode = $_.Exception.Response.StatusCode.value__
        if ($actualCode -eq $ExpectedCode) {
            Write-Host "✓ PASS" -ForegroundColor Green -NoNewline
            Write-Host " (Code: $actualCode)"
            return $true
        } else {
            Write-Host "✗ FAIL" -ForegroundColor Red -NoNewline
            Write-Host " (Attendu: $ExpectedCode, Reçu: $actualCode ou erreur)"
            return $false
        }
    }
}

# Vérification des services
Write-Host "=== Vérification des Services ===" -ForegroundColor Yellow
Write-Host ""

# Backend
Write-Host "Backend (API) ... " -NoNewline
try {
    $null = Invoke-WebRequest -Uri $ApiUrl -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
    Write-Host "✓ RUNNING" -ForegroundColor Green -NoNewline
    Write-Host " ($ApiUrl)"
}
catch {
    Write-Host "✗ NOT RUNNING" -ForegroundColor Red
    Write-Host "Veuillez démarrer le backend avec: cd TPE; mvn spring-boot:run" -ForegroundColor Yellow
    exit 1
}

# Frontend
Write-Host "Frontend (Angular) ... " -NoNewline
try {
    $null = Invoke-WebRequest -Uri $FrontendUrl -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
    Write-Host "✓ RUNNING" -ForegroundColor Green -NoNewline
    Write-Host " ($FrontendUrl)"
}
catch {
    Write-Host "⚠ NOT RUNNING" -ForegroundColor Yellow
    Write-Host "Info: Démarrez le frontend avec: cd 'front end'; ng serve" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== Tests des Endpoints API ===" -ForegroundColor Yellow
Write-Host ""

# Test endpoints
if (Test-Endpoint -Name "Health Check" -Url "$ApiUrl/actuator/health" -ExpectedCode 200) {
    $TestsPassed++
} else {
    $TestsFailed++
}

if (Test-Endpoint -Name "Liste des demandes (doit être 401)" -Url "$ApiUrl/demandes" -ExpectedCode 401) {
    $TestsPassed++
} else {
    $TestsFailed++
}

Write-Host ""
Write-Host "=== Vérification des Fichiers ===" -ForegroundColor Yellow
Write-Host ""

# Fichiers à vérifier
$FilesToCheck = @(
    "front end\src\app\demandes\demande-validation\demande-validation.component.ts",
    "front end\src\app\demandes\demande-validation\demande-validation.component.html",
    "front end\src\app\demandes\demande-validation\demande-validation.component.css",
    "TPE\src\main\resources\db\migration\V2__add_demande_affectation_fields.sql",
    "DEMANDE-AFFECTATION-TPE.md",
    "RECAPITULATIF-MODIFICATIONS.md",
    "GUIDE-INSTALLATION.md"
)

foreach ($file in $FilesToCheck) {
    Write-Host "  $file ... " -NoNewline
    if (Test-Path $file) {
        Write-Host "✓ EXISTS" -ForegroundColor Green
        $TestsPassed++
    } else {
        Write-Host "✗ MISSING" -ForegroundColor Red
        $TestsFailed++
    }
}

Write-Host ""
Write-Host "=== Vérification de la Base de Données ===" -ForegroundColor Yellow
Write-Host ""

# Vérifier si sqlcmd est disponible
$sqlcmdExists = Get-Command sqlcmd -ErrorAction SilentlyContinue

if ($sqlcmdExists) {
    Write-Host "sqlcmd trouvé - Vérification des colonnes..." -ForegroundColor Green
    
    $expectedColumns = @(
        "raison_sociale",
        "activite",
        "numero_compte",
        "adresse",
        "code_postal",
        "code_agence",
        "telephone",
        "email_notification",
        "mcc",
        "taux_commission",
        "numero_terminal"
    )
    
    Write-Host "Colonnes attendues dans la table 'demandes':"
    foreach ($col in $expectedColumns) {
        Write-Host "  - $col" -ForegroundColor Cyan
    }
    
    Write-Host ""
    Write-Host "Pour vérifier manuellement, exécutez dans SQL Server:" -ForegroundColor Yellow
    Write-Host "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'demandes';" -ForegroundColor Gray
} else {
    Write-Host "⚠ sqlcmd non installé - Vérification BD ignorée" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Pour vérifier manuellement:" -ForegroundColor Yellow
    Write-Host "1. Ouvrir SQL Server Management Studio" -ForegroundColor Gray
    Write-Host "2. Exécuter: SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'demandes';" -ForegroundColor Gray
}

Write-Host ""
Write-Host "=== Résumé des Tests ===" -ForegroundColor Yellow
Write-Host ""

$TotalTests = $TestsPassed + $TestsFailed
Write-Host "Tests réussis: $TestsPassed" -ForegroundColor Green
Write-Host "Tests échoués: $TestsFailed" -ForegroundColor Red
Write-Host "Total: $TotalTests"
Write-Host ""

if ($TestsFailed -eq 0) {
    Write-Host "✓ Tous les tests sont passés!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Prochaines étapes:" -ForegroundColor Cyan
    Write-Host "1. Exécuter la migration SQL:" -ForegroundColor White
    Write-Host "   TPE\src\main\resources\db\migration\V2__add_demande_affectation_fields.sql" -ForegroundColor Gray
    Write-Host "2. Se connecter à l'application: $FrontendUrl" -ForegroundColor White
    Write-Host "3. Créer une demande de test" -ForegroundColor White
    Write-Host "4. Valider la demande avec un utilisateur Monétique" -ForegroundColor White
    Write-Host ""
    Write-Host "Documentation disponible:" -ForegroundColor Cyan
    Write-Host "- GUIDE-INSTALLATION.md" -ForegroundColor Gray
    Write-Host "- DEMANDE-AFFECTATION-TPE.md" -ForegroundColor Gray
    Write-Host "- RECAPITULATIF-MODIFICATIONS.md" -ForegroundColor Gray
    exit 0
} else {
    Write-Host "✗ Certains tests ont échoué" -ForegroundColor Red
    Write-Host ""
    Write-Host "Veuillez consulter:" -ForegroundColor Yellow
    Write-Host "- GUIDE-INSTALLATION.md pour les instructions détaillées" -ForegroundColor Gray
    Write-Host "- RECAPITULATIF-MODIFICATIONS.md pour la liste des changements" -ForegroundColor Gray
    exit 1
}
