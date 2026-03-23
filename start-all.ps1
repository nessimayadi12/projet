# Script de démarrage complet - Backend + Frontend
# Usage: .\start-all.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Démarrage du système TPE CPABC049" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Vérifier si Java est installé
Write-Host "[1/6] Vérification de Java..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "✓ Java installé: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Java n'est pas installé ou pas dans le PATH" -ForegroundColor Red
    Write-Host "   Installez Java 11+ depuis: https://adoptium.net/" -ForegroundColor Yellow
    exit 1
}

# Vérifier si Maven est installé
Write-Host "[2/6] Vérification de Maven..." -ForegroundColor Yellow
try {
    $mavenVersion = mvn -version 2>&1 | Select-String "Apache Maven"
    Write-Host "✓ Maven installé: $mavenVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Maven n'est pas installé ou pas dans le PATH" -ForegroundColor Red
    Write-Host "   Installez Maven depuis: https://maven.apache.org/download.cgi" -ForegroundColor Yellow
    exit 1
}

# Vérifier si Node.js est installé
Write-Host "[3/6] Vérification de Node.js..." -ForegroundColor Yellow
try {
    $nodeVersion = node --version
    Write-Host "✓ Node.js installé: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Node.js n'est pas installé ou pas dans le PATH" -ForegroundColor Red
    Write-Host "   Installez Node.js depuis: https://nodejs.org/" -ForegroundColor Yellow
    exit 1
}

# Vérifier si Angular CLI est installé
Write-Host "[4/6] Vérification d'Angular CLI..." -ForegroundColor Yellow
try {
    $ngVersion = ng version 2>&1 | Select-String "Angular CLI"
    Write-Host "✓ Angular CLI installé: $ngVersion" -ForegroundColor Green
} catch {
    Write-Host "⚠ Angular CLI non trouvé, installation..." -ForegroundColor Yellow
    npm install -g @angular/cli
    Write-Host "✓ Angular CLI installé" -ForegroundColor Green
}

Write-Host ""
Write-Host "[5/6] Démarrage du BACKEND (Spring Boot)..." -ForegroundColor Yellow
Write-Host "   Port: 8080" -ForegroundColor Cyan
Write-Host "   Logs: backend-logs.txt" -ForegroundColor Cyan

# Démarrer le backend en arrière-plan
$backendPath = Join-Path $PSScriptRoot "TPE"
if (Test-Path $backendPath) {
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$backendPath'; Write-Host 'Démarrage du backend...' -ForegroundColor Green; mvn spring-boot:run | Tee-Object -FilePath backend-logs.txt" -WindowStyle Normal
    Write-Host "✓ Backend lancé dans une nouvelle fenêtre" -ForegroundColor Green
} else {
    Write-Host "✗ Dossier TPE non trouvé: $backendPath" -ForegroundColor Red
    exit 1
}

# Attendre que le backend démarre
Write-Host "   Attente du démarrage du backend (30 secondes)..." -ForegroundColor Cyan
Start-Sleep -Seconds 30

# Tester si le backend répond
Write-Host "   Test de connexion au backend..." -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/tpe-posting/verify-tpe/test" -Method Get -TimeoutSec 10 -ErrorAction Stop
    Write-Host "✓ Backend opérationnel sur http://localhost:8080" -ForegroundColor Green
} catch {
    Write-Host "⚠ Backend ne répond pas encore (normal si première compilation)" -ForegroundColor Yellow
    Write-Host "   Vérifiez la fenêtre du backend pour voir les logs" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "[6/6] Démarrage du FRONTEND (Angular)..." -ForegroundColor Yellow
Write-Host "   Port: 4200" -ForegroundColor Cyan
Write-Host "   Logs: frontend-logs.txt" -ForegroundColor Cyan

# Démarrer le frontend en arrière-plan
$frontendPath = Join-Path $PSScriptRoot "front end"
if (Test-Path $frontendPath) {
    # Vérifier si node_modules existe
    $nodeModules = Join-Path $frontendPath "node_modules"
    if (-not (Test-Path $nodeModules)) {
        Write-Host "   Installation des dépendances npm (première fois)..." -ForegroundColor Yellow
        Set-Location $frontendPath
        npm install
        Set-Location $PSScriptRoot
        Write-Host "✓ Dépendances installées" -ForegroundColor Green
    }
    
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$frontendPath'; Write-Host 'Démarrage du frontend...' -ForegroundColor Green; ng serve --open | Tee-Object -FilePath frontend-logs.txt" -WindowStyle Normal
    Write-Host "✓ Frontend lancé dans une nouvelle fenêtre" -ForegroundColor Green
} else {
    Write-Host "✗ Dossier 'front end' non trouvé: $frontendPath" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  ✓ Système démarré avec succès!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Backend:  http://localhost:8080" -ForegroundColor Cyan
Write-Host "Frontend: http://localhost:4200 (s'ouvre automatiquement)" -ForegroundColor Cyan
Write-Host ""
Write-Host "Deux fenêtres PowerShell ont été ouvertes:" -ForegroundColor Yellow
Write-Host "  1. Backend (Spring Boot) - Port 8080" -ForegroundColor Yellow
Write-Host "  2. Frontend (Angular) - Port 4200" -ForegroundColor Yellow
Write-Host ""
Write-Host "Les logs sont enregistrés dans:" -ForegroundColor Yellow
Write-Host "  - backend-logs.txt" -ForegroundColor Yellow
Write-Host "  - frontend-logs.txt" -ForegroundColor Yellow
Write-Host ""
Write-Host "Pour arrêter les serveurs:" -ForegroundColor Red
Write-Host "  - Fermez les fenêtres PowerShell ouvertes" -ForegroundColor Red
Write-Host "  - Ou appuyez sur Ctrl+C dans chaque fenêtre" -ForegroundColor Red
Write-Host ""
Write-Host "📖 Documentation:" -ForegroundColor Magenta
Write-Host "  - README-INTEGRATION-COMPLETE.md" -ForegroundColor Magenta
Write-Host "  - GUIDE-TEST-INTEGRATION.md" -ForegroundColor Magenta
Write-Host "  - BACKEND-FRONTEND-INTEGRATION.md" -ForegroundColor Magenta
Write-Host ""
Write-Host "🧪 Fichier de test:" -ForegroundColor Magenta
Write-Host "  - test_cpabc049_sample.txt" -ForegroundColor Magenta
Write-Host ""
Write-Host "Appuyez sur une touche pour quitter ce script..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
