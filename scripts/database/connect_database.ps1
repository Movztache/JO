# ============================================================================
# SCRIPT POUR SE CONNECTER À LA BASE DE DONNÉES RDS VIA TUNNEL SSH
# ============================================================================

Write-Host "============================================================================" -ForegroundColor Green
Write-Host "CONNEXION À LA BASE DE DONNÉES RDS VIA TUNNEL SSH" -ForegroundColor Green
Write-Host "============================================================================" -ForegroundColor Green

# Récupérer les informations depuis Terraform
Write-Host "Récupération des informations d'infrastructure..." -ForegroundColor Yellow

Set-Location terraform

try {
    $ec2_ip = (terraform output -raw ec2_public_ip)
    $rds_endpoint = (terraform output -raw rds_endpoint).Split(':')[0]
    $db_name = (terraform output -raw rds_database_name)

    Write-Host ""
    Write-Host "Informations récupérées :" -ForegroundColor Green
    Write-Host "  EC2 IP: $ec2_ip" -ForegroundColor White
    Write-Host "  RDS Endpoint: $rds_endpoint" -ForegroundColor White
    Write-Host "  Database: $db_name" -ForegroundColor White
    Write-Host ""

    # Vérifier que la clé SSH existe
    $sshKeyPath = "vibe-tickets-terraform-key.pem"
    if (-not (Test-Path $sshKeyPath)) {
        Write-Host "ERREUR: Clé SSH non trouvée : $sshKeyPath" -ForegroundColor Red
        Write-Host "Assurez-vous que la clé SSH est présente dans le dossier terraform/" -ForegroundColor Red
        exit 1
    }

    Write-Host "============================================================================" -ForegroundColor Cyan
    Write-Host "CONFIGURATION INTELLIJ IDEA" -ForegroundColor Cyan
    Write-Host "============================================================================" -ForegroundColor Cyan
    Write-Host "1. Ouvrez Database Tool dans IntelliJ (View → Tool Windows → Database)" -ForegroundColor Yellow
    Write-Host "2. Cliquez sur '+' → Data Source → PostgreSQL" -ForegroundColor Yellow
    Write-Host "3. Configurez :" -ForegroundColor Yellow
    Write-Host "   Host: localhost" -ForegroundColor White
    Write-Host "   Port: 5433" -ForegroundColor White
    Write-Host "   Database: $db_name" -ForegroundColor White
    Write-Host "   User: adminco" -ForegroundColor White
    Write-Host "   Password: adminco123" -ForegroundColor White
    Write-Host "4. Testez la connexion après avoir démarré le tunnel ci-dessous" -ForegroundColor Yellow
    Write-Host ""

    Write-Host "Création du tunnel SSH..." -ForegroundColor Yellow
    Write-Host "Port local 5433 -> RDS via EC2" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "IMPORTANT:" -ForegroundColor Red
    Write-Host "- Laissez cette fenêtre OUVERTE pendant que vous utilisez la base de données" -ForegroundColor Red
    Write-Host "- Appuyez sur Ctrl+C pour arrêter le tunnel" -ForegroundColor Red
    Write-Host ""

    # Créer le tunnel SSH
    Write-Host "Démarrage du tunnel SSH..." -ForegroundColor Green
    Write-Host "Commande: ssh -i $sshKeyPath -L 5433:${rds_endpoint}:5432 -N ec2-user@$ec2_ip" -ForegroundColor Gray
    Write-Host ""

    # Exécuter le tunnel SSH
    & ssh -i $sshKeyPath -L 5433:${rds_endpoint}:5432 -N ec2-user@$ec2_ip

} catch {
    Write-Host "ERREUR: $($_.Exception.Message)" -ForegroundColor Red
} finally {
    Set-Location ..
}

Write-Host ""
Write-Host "Tunnel SSH fermé." -ForegroundColor Yellow
