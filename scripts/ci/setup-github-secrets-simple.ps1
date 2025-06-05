# ============================================================================
# SCRIPT SIMPLE DE CONFIGURATION DES SECRETS GITHUB ACTIONS - VIBE-TICKETS
# ============================================================================

param(
    [Parameter(Mandatory=$false)]
    [string]$Repository = "",
    
    [Parameter(Mandatory=$false)]
    [switch]$DryRun = $false
)

$ErrorActionPreference = "Stop"

function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Blue
}

function Write-Success {
    param([string]$Message)
    Write-Host "[SUCCESS] $Message" -ForegroundColor Green
}

function Write-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

function Write-Warning {
    param([string]$Message)
    Write-Host "[WARNING] $Message" -ForegroundColor Yellow
}

Write-Info "Configuration des secrets GitHub Actions - Vibe-Tickets"
Write-Info "======================================================="

# Vérification de GitHub CLI
try {
    $ghVersion = gh --version 2>$null
    if ($LASTEXITCODE -ne 0) {
        throw "GitHub CLI non trouve"
    }
    Write-Success "GitHub CLI installe"
}
catch {
    Write-Error "GitHub CLI (gh) n'est pas installe ou configure"
    Write-Info "Installation: https://cli.github.com/"
    exit 1
}

# Vérification de AWS CLI
try {
    $awsVersion = aws --version 2>$null
    if ($LASTEXITCODE -ne 0) {
        throw "AWS CLI non trouve"
    }
    Write-Success "AWS CLI installe"
}
catch {
    Write-Error "AWS CLI n'est pas installe ou configure"
    Write-Info "Installation: https://aws.amazon.com/cli/"
    exit 1
}

# Vérification de l'authentification GitHub
try {
    gh auth status 2>$null
    if ($LASTEXITCODE -ne 0) {
        throw "Non authentifie"
    }
    Write-Success "Authentifie sur GitHub"
}
catch {
    Write-Error "Non authentifie sur GitHub"
    Write-Info "Executez: gh auth login"
    exit 1
}

# Vérification de l'authentification AWS
try {
    $awsIdentity = aws sts get-caller-identity 2>$null | ConvertFrom-Json
    if ($LASTEXITCODE -ne 0) {
        throw "Non authentifie"
    }
    Write-Success "Authentifie sur AWS: $($awsIdentity.Arn)"
}
catch {
    Write-Error "Non authentifie sur AWS"
    Write-Info "Executez: aws configure"
    exit 1
}

# Détection du repository
if ($Repository -eq "") {
    try {
        $gitRemote = git remote get-url origin 2>$null
        if ($LASTEXITCODE -eq 0) {
            if ($gitRemote -match "github\.com[:/](.+?)(?:\.git)?$") {
                $Repository = $matches[1]
                Write-Info "Repository detecte automatiquement: $Repository"
            }
        }
    }
    catch {
        # Ignore les erreurs
    }
    
    if ($Repository -eq "") {
        Write-Warning "Impossible de detecter automatiquement le repository"
        $Repository = Read-Host "Entrez le nom du repository (format: username/repo-name)"
        
        if ($Repository -eq "") {
            Write-Error "Nom de repository requis"
            exit 1
        }
    }
}

Write-Info "Repository cible: $Repository"

# Récupération des credentials AWS
Write-Info "Recuperation des credentials AWS..."

try {
    $awsIdentity = aws sts get-caller-identity | ConvertFrom-Json
    $accountId = $awsIdentity.Account
    
    # Récupération de l'Access Key ID depuis AWS CLI
    $awsConfig = aws configure list
    $accessKeyLine = $awsConfig | Where-Object { $_ -match "access_key" }
    if ($accessKeyLine -match "\s+(\w+)\s+") {
        $accessKeyId = $matches[1]
    } else {
        throw "Access Key ID non trouvee"
    }
    
    Write-Warning "La Secret Access Key ne peut pas etre recuperee automatiquement"
    $secretAccessKey = Read-Host "Entrez votre AWS Secret Access Key" -AsSecureString
    $secretAccessKeyPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($secretAccessKey))
    
    Write-Success "Credentials AWS recuperes"
}
catch {
    Write-Error "Erreur lors de la recuperation des credentials AWS: $_"
    exit 1
}

# Configuration des secrets
Write-Info "Configuration des secrets GitHub Actions..."

if ($DryRun) {
    Write-Warning "MODE DRY-RUN - Aucune modification ne sera effectuee"
    Write-Info "Configurerait les secrets suivants:"
    Write-Info "- AWS_ACCESS_KEY_ID: $accessKeyId"
    Write-Info "- AWS_SECRET_ACCESS_KEY: [MASQUE]"
    Write-Info "- AWS_ACCOUNT_ID: $accountId"
} else {
    try {
        # Configuration AWS_ACCESS_KEY_ID
        $accessKeyId | gh secret set AWS_ACCESS_KEY_ID --repo $Repository
        if ($LASTEXITCODE -eq 0) {
            Write-Success "Secret configure: AWS_ACCESS_KEY_ID"
        } else {
            throw "Erreur AWS_ACCESS_KEY_ID"
        }
        
        # Configuration AWS_SECRET_ACCESS_KEY
        $secretAccessKeyPlain | gh secret set AWS_SECRET_ACCESS_KEY --repo $Repository
        if ($LASTEXITCODE -eq 0) {
            Write-Success "Secret configure: AWS_SECRET_ACCESS_KEY"
        } else {
            throw "Erreur AWS_SECRET_ACCESS_KEY"
        }
        
        # Configuration AWS_ACCOUNT_ID
        $accountId | gh secret set AWS_ACCOUNT_ID --repo $Repository
        if ($LASTEXITCODE -eq 0) {
            Write-Success "Secret configure: AWS_ACCOUNT_ID"
        } else {
            throw "Erreur AWS_ACCOUNT_ID"
        }
        
        Write-Success "Tous les secrets ont ete configures avec succes!"
        
    }
    catch {
        Write-Error "Erreur lors de la configuration des secrets: $_"
        exit 1
    }
}

Write-Info ""
Write-Success "Configuration terminee!"
Write-Info ""
Write-Info "PROCHAINES ETAPES:"
Write-Info "1. Verifiez les secrets dans Settings - Secrets and variables - Actions"
Write-Info "2. Configurez les variables d'environnement si necessaire"
Write-Info "3. Testez le pipeline en faisant un push sur main"
Write-Info ""
Write-Info "Documentation complete: docs/GITHUB_ACTIONS_SETUP.md"
