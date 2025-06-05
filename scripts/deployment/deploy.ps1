# ============================================================================
# SCRIPT DE DÉPLOIEMENT AUTOMATISÉ VIBE-TICKETS
# ============================================================================
#
# Ce script automatise le déploiement complet de l'application Vibe-Tickets :
# 1. Build de l'application Spring Boot avec Maven
# 2. Création et tag de l'image Docker avec timestamp
# 3. Génération automatique des clés SSH pour sécurité
# 4. Nettoyage et push vers Amazon ECR
# 5. Déploiement de l'infrastructure avec Terraform
# 6. Vérification de l'état de l'application
#
# Prérequis :
# - AWS CLI configuré avec les bonnes permissions
# - Docker installé et démarré
# - Terraform installé
# - Maven installé
# - Fichier terraform/terraform.tfvars configuré
#
# Usage :
#   .\deploy.ps1                    # Déploiement complet
#   .\deploy.ps1 -SkipBuild        # Skip le build Maven (utilise le JAR existant)
#
# ============================================================================

param(
    [switch]$SkipBuild = $false,
    [string]$Environment = "prod"
)

$ErrorActionPreference = "Stop"

# ============================================================================
# CONFIGURATION GLOBALE
# ============================================================================

$AWS_REGION = "eu-west-3"
$ECR_REPOSITORY = "756942038699.dkr.ecr.eu-west-3.amazonaws.com/vibe-tickets"
$TIMESTAMP = Get-Date -Format "yyyyMMdd-HHmmss"
$IMAGE_TAG = "v$TIMESTAMP"
$SSH_KEY_NAME = "vibe-tickets-key-$TIMESTAMP"

Write-Host "============================================================================" -ForegroundColor Green
Write-Host "               DÉPLOIEMENT AUTOMATISÉ VIBE-TICKETS" -ForegroundColor Green
Write-Host "============================================================================" -ForegroundColor Green
Write-Host ""
Write-Host "Configuration du déploiement :" -ForegroundColor Cyan
Write-Host "  • Environnement    : $Environment" -ForegroundColor White
Write-Host "  • Région AWS       : $AWS_REGION" -ForegroundColor White
Write-Host "  • Timestamp        : $TIMESTAMP" -ForegroundColor White
Write-Host "  • Tag image Docker : $IMAGE_TAG" -ForegroundColor White
Write-Host "  • Nom clé SSH      : $SSH_KEY_NAME" -ForegroundColor White
Write-Host ""
Write-Host "Identifiants de test :" -ForegroundColor Cyan
Write-Host "  • ADMIN : admin@vibe-tickets.com / AdminVibe2024!" -ForegroundColor White
Write-Host "  • USER1 : alice.martin@email.com / AliceSecure123!" -ForegroundColor White
Write-Host "  • USER2 : bob.dupont@email.com / BobStrong456!" -ForegroundColor White
Write-Host ""

# ============================================================================
# ÉTAPE 1: BUILD DE L'APPLICATION
# ============================================================================

if (-not $SkipBuild) {
    Write-Host "ETAPE 1: Build de l'application Spring Boot..." -ForegroundColor Cyan

    mvn clean package -DskipTests

    if ($LASTEXITCODE -ne 0) {
        Write-Host "Erreur lors du build Maven" -ForegroundColor Red
        exit 1
    }

    Write-Host "Build termine" -ForegroundColor Green
    Write-Host ""
}

# ============================================================================
# ÉTAPE 2: BUILD DE L'IMAGE DOCKER
# ============================================================================

Write-Host "ETAPE 2: Build de l'image Docker..." -ForegroundColor Cyan

docker build -t "vibe-tickets:$IMAGE_TAG" .
if ($LASTEXITCODE -ne 0) {
    Write-Host "Erreur lors du build Docker" -ForegroundColor Red
    exit 1
}

docker tag "vibe-tickets:$IMAGE_TAG" "${ECR_REPOSITORY}:$IMAGE_TAG"

Write-Host "Image Docker creee avec le tag: $IMAGE_TAG" -ForegroundColor Green
Write-Host ""

# ============================================================================
# ÉTAPE 3: NETTOYAGE ET PUSH VERS ECR
# ============================================================================

Write-Host "ETAPE 3: Nettoyage et push vers ECR..." -ForegroundColor Cyan

# Connexion à ECR
Write-Host "Connexion a ECR..." -ForegroundColor Yellow
$loginCommand = aws ecr get-login-password --region $AWS_REGION
if ($LASTEXITCODE -ne 0) {
    Write-Host "Erreur lors de la recuperation du token ECR" -ForegroundColor Red
    exit 1
}

$loginCommand | docker login --username AWS --password-stdin $ECR_REPOSITORY
if ($LASTEXITCODE -ne 0) {
    Write-Host "Erreur lors de la connexion a ECR" -ForegroundColor Red
    exit 1
}

# Suppression des anciennes images dans ECR
Write-Host "Suppression des anciennes images dans ECR..." -ForegroundColor Yellow
try {
    $existingImages = aws ecr list-images --repository-name vibe-tickets --region $AWS_REGION --query 'imageIds[*]' --output json | ConvertFrom-Json

    if ($existingImages -and $existingImages.Count -gt 0) {
        Write-Host "Suppression de $($existingImages.Count) image(s) existante(s)..." -ForegroundColor Yellow

        foreach ($image in $existingImages) {
            if ($image.imageTag) {
                Write-Host "  - Suppression de l'image avec tag: $($image.imageTag)" -ForegroundColor White
                aws ecr batch-delete-image --repository-name vibe-tickets --region $AWS_REGION --image-ids imageTag=$($image.imageTag) | Out-Null
            } elseif ($image.imageDigest) {
                Write-Host "  - Suppression de l'image avec digest: $($image.imageDigest.Substring(0,20))..." -ForegroundColor White
                aws ecr batch-delete-image --repository-name vibe-tickets --region $AWS_REGION --image-ids imageDigest=$($image.imageDigest) | Out-Null
            }
        }
        Write-Host "Anciennes images supprimees" -ForegroundColor Green
    } else {
        Write-Host "Aucune image existante a supprimer" -ForegroundColor White
    }
} catch {
    Write-Host "Erreur lors de la suppression (normal si repository vide): $($_.Exception.Message)" -ForegroundColor Yellow
}

# Push de la nouvelle image
Write-Host "Push de la nouvelle image vers ECR..." -ForegroundColor Yellow
docker push "${ECR_REPOSITORY}:$IMAGE_TAG"
if ($LASTEXITCODE -ne 0) {
    Write-Host "Erreur lors du push de l'image" -ForegroundColor Red
    exit 1
}

Write-Host "Nouvelle image pushee vers ECR: $IMAGE_TAG" -ForegroundColor Green
Write-Host ""

# ============================================================================
# ÉTAPE 4: GÉNÉRATION DES CLÉS SSH SÉCURISÉES
# ============================================================================

Write-Host "ETAPE 4: Generation des cles SSH securisees..." -ForegroundColor Cyan

Set-Location terraform

# Supprimer les anciennes clés SSH si elles existent
$sshKeyFiles = @("ssh-key", "ssh-key.pub", "vibe-tickets-terraform-key.pem", "vibe-tickets-terraform-key.pub")
foreach ($keyFile in $sshKeyFiles) {
    if (Test-Path $keyFile) {
        Write-Host "  • Suppression de l'ancienne cle: $keyFile" -ForegroundColor Yellow
        Remove-Item $keyFile -Force
    }
}

# Supprimer les anciennes clés AWS si elles existent
Write-Host "  • Nettoyage des anciennes cles AWS..." -ForegroundColor Yellow
try {
    $existingKeys = aws ec2 describe-key-pairs --region $AWS_REGION --query 'KeyPairs[?starts_with(KeyName, `vibe-tickets-key-`)].KeyName' --output text
    if ($existingKeys) {
        $keyList = $existingKeys -split "`t"
        foreach ($oldKey in $keyList) {
            if ($oldKey.Trim()) {
                Write-Host "    - Suppression de la cle AWS: $($oldKey.Trim())" -ForegroundColor White
                aws ec2 delete-key-pair --key-name $oldKey.Trim() --region $AWS_REGION | Out-Null
            }
        }
    }
} catch {
    Write-Host "    - Aucune ancienne cle a supprimer" -ForegroundColor White
}

# Générer une nouvelle paire de clés SSH avec timestamp
Write-Host "  • Generation d'une nouvelle paire de cles SSH..." -ForegroundColor Yellow
$privateKeyPath = "ssh-key"
$publicKeyPath = "ssh-key.pub"

try {
    # Générer la clé avec ssh-keygen
    $comment = "vibe-tickets-deployment-$TIMESTAMP"
    & ssh-keygen -t rsa -b 2048 -f $privateKeyPath -N '""' -C $comment

    if ($LASTEXITCODE -ne 0) {
        throw "Erreur lors de la generation de la cle SSH"
    }

    # Vérifier que les clés ont été créées
    if (-not (Test-Path $privateKeyPath) -or -not (Test-Path $publicKeyPath)) {
        throw "Les cles SSH n'ont pas ete creees correctement"
    }

    Write-Host "  • Cles SSH generees avec succes!" -ForegroundColor Green
    Write-Host "    - Cle privee: $privateKeyPath" -ForegroundColor White
    Write-Host "    - Cle publique: $publicKeyPath" -ForegroundColor White

    # Afficher l'empreinte de la clé pour vérification
    $fingerprint = & ssh-keygen -lf $publicKeyPath
    Write-Host "    - Empreinte: $fingerprint" -ForegroundColor White

} catch {
    Write-Host "Erreur lors de la generation des cles SSH: $($_.Exception.Message)" -ForegroundColor Red
    Set-Location ..
    exit 1
}

Write-Host ""

# ============================================================================
# ÉTAPE 5: DÉPLOIEMENT TERRAFORM
# ============================================================================

Write-Host "ETAPE 5: Deploiement avec Terraform..." -ForegroundColor Cyan

# Mise à jour des variables dans terraform.tfvars
Write-Host "  • Mise a jour des variables Terraform..." -ForegroundColor Yellow
$tfvarsContent = Get-Content terraform.tfvars
$newContent = @()
$imageTagFound = $false
$keyNameFound = $false

foreach ($line in $tfvarsContent) {
    if ($line -match "^image_tag\s*=") {
        $newContent += "image_tag = `"$IMAGE_TAG`""
        $imageTagFound = $true
        Write-Host "    - Image tag mis a jour: $IMAGE_TAG" -ForegroundColor White
    } elseif ($line -match "^ec2_key_name\s*=") {
        $newContent += "ec2_key_name = `"$SSH_KEY_NAME`""
        $keyNameFound = $true
        Write-Host "    - Nom de cle SSH mis a jour: $SSH_KEY_NAME" -ForegroundColor White
    } else {
        $newContent += $line
    }
}

# Ajouter les variables si elles n'existent pas
if (-not $imageTagFound) {
    $newContent += "image_tag = `"$IMAGE_TAG`""
    Write-Host "    - Image tag ajoute: $IMAGE_TAG" -ForegroundColor White
}

if (-not $keyNameFound) {
    $newContent += "ec2_key_name = `"$SSH_KEY_NAME`""
    Write-Host "    - Nom de cle SSH ajoute: $SSH_KEY_NAME" -ForegroundColor White
}

$newContent | Set-Content terraform.tfvars

Write-Host "Deploiement de l'infrastructure..." -ForegroundColor Yellow
terraform apply -auto-approve

if ($LASTEXITCODE -ne 0) {
    Write-Host "Erreur lors du deploiement Terraform" -ForegroundColor Red
    Set-Location ..
    exit 1
}

Set-Location ..
Write-Host "Deploiement Terraform termine" -ForegroundColor Green
Write-Host ""

# ============================================================================
# ÉTAPE 6: VÉRIFICATION ET TESTS DE L'APPLICATION
# ============================================================================

Write-Host "ETAPE 6: Verification du deploiement..." -ForegroundColor Cyan

# Attendre que l'instance EC2 soit complètement initialisée
Write-Host "  • Attente de l'initialisation de l'instance EC2 (60 secondes)..." -ForegroundColor Yellow
Start-Sleep -Seconds 60

# Récupérer les informations de déploiement
Set-Location terraform
try {
    $PUBLIC_IP = terraform output -raw ec2_public_ip
    $INSTANCE_ID = terraform output -raw ec2_instance_id
    Write-Host "  • Instance EC2 deployee: $INSTANCE_ID" -ForegroundColor White
    Write-Host "  • Adresse IP publique: $PUBLIC_IP" -ForegroundColor White
} catch {
    Write-Host "Erreur lors de la recuperation des outputs Terraform" -ForegroundColor Red
    Set-Location ..
    exit 1
}
Set-Location ..

# Test de santé de l'application
Write-Host "  • Test de sante de l'application..." -ForegroundColor Yellow
$healthCheckUrl = "http://${PUBLIC_IP}:8080/actuator/health"

try {
    $response = Invoke-WebRequest -Uri $healthCheckUrl -TimeoutSec 15
    if ($response.StatusCode -eq 200) {
        Write-Host "  • Application accessible et fonctionnelle!" -ForegroundColor Green
        $healthData = $response.Content | ConvertFrom-Json
        Write-Host "    - Statut: $($healthData.status)" -ForegroundColor White
    }
} catch {
    Write-Host "  • Application pas encore prete (normal lors du premier demarrage)" -ForegroundColor Yellow
    Write-Host "    - URL de test: $healthCheckUrl" -ForegroundColor White
}

Write-Host ""
Write-Host "============================================================================" -ForegroundColor Green
Write-Host "                    DÉPLOIEMENT TERMINÉ AVEC SUCCÈS" -ForegroundColor Green
Write-Host "============================================================================" -ForegroundColor Green
Write-Host ""
Write-Host "Informations de déploiement :" -ForegroundColor Cyan
Write-Host "  • Image Docker deployee : ${ECR_REPOSITORY}:$IMAGE_TAG" -ForegroundColor White
Write-Host "  • URL de l'application  : http://${PUBLIC_IP}:8080" -ForegroundColor White
Write-Host "  • Health check         : http://${PUBLIC_IP}:8080/actuator/health" -ForegroundColor White
Write-Host "  • Cle SSH generee      : $SSH_KEY_NAME" -ForegroundColor White
Write-Host ""
Write-Host "Commandes utiles :" -ForegroundColor Cyan
Write-Host "  • Connexion SSH        : ssh -i terraform/ssh-key ec2-user@$PUBLIC_IP" -ForegroundColor White
Write-Host "  • Logs application     : ssh -i terraform/ssh-key ec2-user@$PUBLIC_IP 'docker logs vibe-tickets'" -ForegroundColor White
Write-Host "  • Statut conteneur     : ssh -i terraform/ssh-key ec2-user@$PUBLIC_IP 'docker ps'" -ForegroundColor White
Write-Host ""
Write-Host "Tests d'authentification :" -ForegroundColor Cyan
Write-Host "  1. Attendez 2-3 minutes que l'application demarre completement" -ForegroundColor White
Write-Host "  2. Testez avec l'admin : admin@vibe-tickets.com / AdminVibe2024!" -ForegroundColor White
Write-Host "  3. Testez avec un user : alice.martin@email.com / AliceSecure123!" -ForegroundColor White
Write-Host "  4. En cas de probleme, verifiez les logs via SSH" -ForegroundColor White
Write-Host ""
