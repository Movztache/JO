# ============================================================================
# SCRIPT DE DÉPLOIEMENT PRINCIPAL VIBE-TICKETS
# ============================================================================
#
# Ce script est un wrapper qui appelle le script de déploiement principal
# situé dans scripts/deployment/deploy.ps1
#
# Usage :
#   .\deploy.ps1                    # Déploiement complet
#   .\deploy.ps1 -SkipBuild        # Skip le build Maven
#   .\deploy.ps1 -Environment "staging"  # Environnement spécifique
#
# ============================================================================

param(
    [switch]$SkipBuild = $false,
    [string]$Environment = "prod"
)

$ErrorActionPreference = "Stop"

Write-Host "Vibe-Tickets - Deploiement automatise" -ForegroundColor Green
Write-Host "Redirection vers scripts/deployment/deploy.ps1" -ForegroundColor Yellow
Write-Host ""

# Vérifier que le script de déploiement existe
$deploymentScript = "scripts\deployment\deploy.ps1"
if (-not (Test-Path $deploymentScript)) {
    Write-Host "ERREUR: Script de deploiement non trouve : $deploymentScript" -ForegroundColor Red
    Write-Host "Assurez-vous que la structure des dossiers est correcte." -ForegroundColor Red
    exit 1
}

# Appeler le script de déploiement principal avec tous les paramètres
try {
    if ($SkipBuild) {
        & $deploymentScript -SkipBuild -Environment $Environment
    } else {
        & $deploymentScript -Environment $Environment
    }
    
    if ($LASTEXITCODE -ne 0) {
        throw "Le script de déploiement a échoué avec le code : $LASTEXITCODE"
    }
    
} catch {
    Write-Host "ERREUR lors du deploiement : $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Deploiement termine avec succes !" -ForegroundColor Green
