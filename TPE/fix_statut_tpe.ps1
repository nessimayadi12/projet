# Script PowerShell pour exécuter la correction des statuts TPE
# Nécessite: SQL Server installé localement avec l'instance par défaut

param(
    [string]$Server = "localhost",
    [string]$Database = "TPE_Managements",
    [string]$Username = "sa",
    [string]$Password = "Password123!"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Correction des statuts TPE affectés" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Chemin du script SQL
$SqlFile = Join-Path $PSScriptRoot "fix_statut_tpe_affectes.sql"

if (-not (Test-Path $SqlFile)) {
    Write-Host "ERREUR: Fichier SQL introuvable: $SqlFile" -ForegroundColor Red
    exit 1
}

Write-Host "Serveur      : $Server" -ForegroundColor Yellow
Write-Host "Base de données : $Database" -ForegroundColor Yellow
Write-Host "Utilisateur     : $Username" -ForegroundColor Yellow
Write-Host ""

# Vérifier si sqlcmd est disponible
$sqlcmdPath = Get-Command sqlcmd -ErrorAction SilentlyContinue

if (-not $sqlcmdPath) {
    Write-Host "ERREUR: sqlcmd n'est pas installé ou n'est pas dans le PATH" -ForegroundColor Red
    Write-Host "Veuillez installer SQL Server Command Line Utilities" -ForegroundColor Yellow
    Write-Host "Téléchargement: https://docs.microsoft.com/en-us/sql/tools/sqlcmd-utility" -ForegroundColor Yellow
    exit 1
}

Write-Host "Exécution du script SQL..." -ForegroundColor Green
Write-Host ""

# Exécuter le script SQL
try {
    # Utiliser -C pour faire confiance au certificat serveur (TrustServerCertificate=true)
    $result = sqlcmd -S $Server -d $Database -U $Username -P $Password -i $SqlFile -b -C
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host $result
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "  Correction effectuée avec succès !" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "Prochaines étapes:" -ForegroundColor Yellow
        Write-Host "1. Rafraîchissez votre dashboard (F5)" -ForegroundColor White
        Write-Host "2. Le compteur 'TPE Affectés' devrait maintenant afficher le bon nombre" -ForegroundColor White
    } else {
        Write-Host "ERREUR lors de l'exécution du script SQL" -ForegroundColor Red
        Write-Host $result
        exit 1
    }
} catch {
    Write-Host "ERREUR: $_" -ForegroundColor Red
    exit 1
}
