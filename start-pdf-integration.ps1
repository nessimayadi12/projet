# Script de démarrage complet - Backend + Frontend
# start-pdf-integration.ps1

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  DÉMARRAGE INTÉGRATION PDF" -ForegroundColor Cyan
Write-Host "  Backend Java + Frontend Angular" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Vérifier les prérequis
Write-Host "🔍 Vérification des prérequis..." -ForegroundColor Yellow

# Vérifier Java
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "✅ Java installé: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Java non trouvé. Installez Java 17 ou supérieur" -ForegroundColor Red
    exit 1
}

# Vérifier Maven
try {
    $mvnVersion = mvn -version | Select-String "Apache Maven"
    Write-Host "✅ Maven installé: $mvnVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Maven non trouvé. Installez Maven" -ForegroundColor Red
    exit 1
}

# Vérifier Node.js
try {
    $nodeVersion = node -v
    Write-Host "✅ Node.js installé: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Node.js non trouvé. Installez Node.js" -ForegroundColor Red
    exit 1
}

# Vérifier npm
try {
    $npmVersion = npm -v
    Write-Host "✅ npm installé: v$npmVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ npm non trouvé" -ForegroundColor Red
    exit 1
}

# Étape 1: Nettoyer et compiler le backend
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "📦 Étape 1: Compilation du Backend" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Push-Location TPE

Write-Host "🧹 Nettoyage Maven..." -ForegroundColor Yellow
mvn clean | Out-Null

Write-Host "🔨 Compilation du projet (avec dépendances iTextPDF)..." -ForegroundColor Yellow
$compileResult = mvn compile 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Compilation réussie" -ForegroundColor Green
} else {
    Write-Host "❌ Erreur de compilation" -ForegroundColor Red
    Write-Host $compileResult -ForegroundColor Gray
    Pop-Location
    exit 1
}

# Vérifier la dépendance iTextPDF
Write-Host "`n🔍 Vérification de la dépendance iTextPDF..." -ForegroundColor Yellow
$pdfDep = Select-String -Path "pom.xml" -Pattern "itextpdf" -Context 0,2
if ($pdfDep) {
    Write-Host "✅ iTextPDF configuré dans pom.xml" -ForegroundColor Green
} else {
    Write-Host "⚠️  iTextPDF non trouvé dans pom.xml" -ForegroundColor Yellow
}

Pop-Location

# Étape 2: Installer dépendances frontend
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "📦 Étape 2: Installation Frontend" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Push-Location "front end"

if (Test-Path "node_modules") {
    Write-Host "✅ node_modules existe déjà" -ForegroundColor Green
} else {
    Write-Host "📥 Installation des dépendances npm..." -ForegroundColor Yellow
    npm install
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Dépendances installées" -ForegroundColor Green
    } else {
        Write-Host "❌ Erreur lors de l'installation npm" -ForegroundColor Red
        Pop-Location
        exit 1
    }
}

Pop-Location

# Étape 3: Démarrer les services
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "🚀 Étape 3: Démarrage des Services" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "📋 Instructions de démarrage:" -ForegroundColor Yellow
Write-Host ""
Write-Host "Option 1: Démarrage manuel dans 2 terminals séparés" -ForegroundColor White
Write-Host "  Terminal 1 - Backend:" -ForegroundColor Cyan
Write-Host "    cd TPE" -ForegroundColor Gray
Write-Host "    mvn spring-boot:run" -ForegroundColor Gray
Write-Host ""
Write-Host "  Terminal 2 - Frontend:" -ForegroundColor Cyan
Write-Host "    cd 'front end'" -ForegroundColor Gray
Write-Host "    npm start" -ForegroundColor Gray
Write-Host ""
Write-Host "Option 2: Utiliser le script start-all.ps1" -ForegroundColor White
Write-Host "    ./start-all.ps1" -ForegroundColor Gray
Write-Host ""

# Proposer de démarrer automatiquement
$response = Read-Host "Voulez-vous démarrer automatiquement? (O/N)"

if ($response -eq 'O' -or $response -eq 'o') {
    Write-Host "`n🚀 Démarrage en cours...`n" -ForegroundColor Green
    
    # Démarrer le backend en arrière-plan
    Write-Host "▶️  Démarrage du backend..." -ForegroundColor Cyan
    $backendJob = Start-Job -ScriptBlock {
        Set-Location $using:PWD
        cd TPE
        mvn spring-boot:run
    }
    Write-Host "   Backend Job ID: $($backendJob.Id)" -ForegroundColor Gray
    
    # Attendre que le backend démarre
    Write-Host "⏳ Attente du démarrage du backend (30 secondes)..." -ForegroundColor Yellow
    Start-Sleep -Seconds 30
    
    # Tester si le backend est accessible
    try {
        $test = Invoke-RestMethod -Uri "http://localhost:8080/api/fichier-bancaire/test" -Method GET
        Write-Host "✅ Backend accessible: $($test.message)" -ForegroundColor Green
    } catch {
        Write-Host "⚠️  Backend pas encore prêt (attente supplémentaire possible)" -ForegroundColor Yellow
    }
    
    # Démarrer le frontend en arrière-plan
    Write-Host "`n▶️  Démarrage du frontend..." -ForegroundColor Cyan
    $frontendJob = Start-Job -ScriptBlock {
        Set-Location $using:PWD
        cd "front end"
        npm start
    }
    Write-Host "   Frontend Job ID: $($frontendJob.Id)" -ForegroundColor Gray
    
    # Attendre que le frontend démarre
    Write-Host "⏳ Attente du démarrage du frontend (20 secondes)..." -ForegroundColor Yellow
    Start-Sleep -Seconds 20
    
    Write-Host "`n========================================" -ForegroundColor Green
    Write-Host "  ✅ SERVICES DÉMARRÉS" -ForegroundColor Green
    Write-Host "========================================`n" -ForegroundColor Green
    
    Write-Host "🌐 URLs:" -ForegroundColor Cyan
    Write-Host "  Backend:  http://localhost:8080" -ForegroundColor White
    Write-Host "  Frontend: http://localhost:4200" -ForegroundColor White
    Write-Host "  Swagger:  http://localhost:8080/swagger-ui.html" -ForegroundColor White
    Write-Host ""
    Write-Host "📄 Test PDF direct:" -ForegroundColor Cyan
    Write-Host "  http://localhost:8080/api/fichier-bancaire/rapport/pdf/20260224" -ForegroundColor White
    Write-Host ""
    Write-Host "🛑 Pour arrêter les services:" -ForegroundColor Yellow
    Write-Host "  Stop-Job $($backendJob.Id), $($frontendJob.Id)" -ForegroundColor Gray
    Write-Host "  Remove-Job $($backendJob.Id), $($frontendJob.Id)" -ForegroundColor Gray
    Write-Host ""
    Write-Host "📊 Voir les logs:" -ForegroundColor Yellow
    Write-Host "  Receive-Job $($backendJob.Id)" -ForegroundColor Gray
    Write-Host "  Receive-Job $($frontendJob.Id)" -ForegroundColor Gray
    Write-Host ""
    
    # Ouvrir le navigateur
    Start-Sleep -Seconds 5
    Write-Host "🌐 Ouverture du navigateur..." -ForegroundColor Cyan
    Start-Process "http://localhost:4200"
    
} else {
    Write-Host "`n✅ Préparation terminée" -ForegroundColor Green
    Write-Host "Démarrez manuellement selon les instructions ci-dessus" -ForegroundColor White
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  📚 DOCUMENTATION" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan
Write-Host "Guide complet: GUIDE-PDF-GENERATION.md" -ForegroundColor White
Write-Host "Script de test: test-pdf-generation.ps1" -ForegroundColor White
Write-Host ""
Write-Host "💡 Exemple d'utilisation:" -ForegroundColor Yellow
Write-Host "  1. Accédez à http://localhost:4200" -ForegroundColor Gray
Write-Host "  2. Menu → Upload Fichier Bancaire" -ForegroundColor Gray
Write-Host "  3. Uploadez test_cpabc049_sample.txt" -ForegroundColor Gray
Write-Host "  4. Cliquez sur 'Export PDF'" -ForegroundColor Gray
Write-Host "`n✅ Configuration terminée !`n" -ForegroundColor Green
