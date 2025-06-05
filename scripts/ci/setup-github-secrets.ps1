# ============================================================================
# SCRIPT DE CONFIGURATION DES SECRETS GITHUB ACTIONS - VIBE-TICKETS
# ============================================================================
#
# Ce script aide à configurer automatiquement les secrets GitHub Actions
# nécessaires pour les pipelines CI/CD du projet Vibe-Tickets.
#
# PRÉREQUIS :
# - GitHub CLI (gh) installé et configuré
# - AWS CLI configuré avec les bonnes permissions
# - Accès en écriture au repository GitHub
#
# UTILISATION :
#   .\setup-github-secrets.ps1
#   .\setup-github-secrets.ps1 -Repository "username/vibe-tickets"
#   .\setup-github-secrets.ps1 -DryRun
#
# FONCTIONNALITÉS :
# - Récupération automatique des credentials AWS
# - Configuration des secrets GitHub via GitHub CLI
# - Validation des permissions et accès
# - Mode dry-run pour tester sans modifier
#
# ============================================================================

param(
    [Parameter(Mandatory=$false)]
    [string]$Repository = "",
    
    [Parameter(Mandatory=$false)]
    [switch]$DryRun = $false,
    
    [Parameter(Mandatory=$false)]
    [switch]$Force = $false,
    
    [Parameter(Mandatory=$false)]
    [switch]$Help = $false
)

# Configuration des couleurs pour l'affichage
$ErrorActionPreference = "Stop"

# ============================================================================
# FONCTIONS UTILITAIRES
# ============================================================================

function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    
    $colorMap = @{
        "Red" = "Red"
        "Green" = "Green"
        "Yellow" = "Yellow"
        "Blue" = "Blue"
        "Cyan" = "Cyan"
        "Magenta" = "Magenta"
        "White" = "White"
    }
    
    Write-Host $Message -ForegroundColor $colorMap[$Color]
}

function Write-Info {
    param([string]$Message)
    Write-ColorOutput "ℹ️  [INFO] $Message" "Blue"
}

function Write-Success {
    param([string]$Message)
    Write-ColorOutput "✅ [SUCCESS] $Message" "Green"
}

function Write-Warning {
    param([string]$Message)
    Write-ColorOutput "⚠️  [WARNING] $Message" "Yellow"
}

function Write-Error {
    param([string]$Message)
    Write-ColorOutput "❌ [ERROR] $Message" "Red"
}

function Show-Help {
    Write-Host "🚀 SCRIPT DE CONFIGURATION DES SECRETS GITHUB ACTIONS - VIBE-TICKETS"
    Write-Host ""
    Write-Host "UTILISATION:"
    Write-Host "  .\setup-github-secrets.ps1 [OPTIONS]"
    Write-Host ""
    Write-Host "OPTIONS:"
    Write-Host "  -Repository [repo]    Repository GitHub (format: username/repo-name)"
    Write-Host "  -DryRun              Mode test - affiche les actions sans les executer"
    Write-Host "  -Force               Force la recreation des secrets existants"
    Write-Host "  -Help                Affiche cette aide"
    Write-Host ""
    Write-Host "EXEMPLES:"
    Write-Host "  .\setup-github-secrets.ps1"
    Write-Host "  .\setup-github-secrets.ps1 -Repository 'monuser/vibe-tickets'"
    Write-Host "  .\setup-github-secrets.ps1 -DryRun"
    Write-Host "  .\setup-github-secrets.ps1 -Force"
    Write-Host ""
    Write-Host "PREREQUIS:"
    Write-Host "  - GitHub CLI (gh) installe et configure"
    Write-Host "  - AWS CLI configure avec les bonnes permissions"
    Write-Host "  - Acces en ecriture au repository GitHub"
    Write-Host ""
    Write-Host "SECRETS CONFIGURES:"
    Write-Host "  - AWS_ACCESS_KEY_ID"
    Write-Host "  - AWS_SECRET_ACCESS_KEY"
    Write-Host "  - AWS_ACCOUNT_ID"
    Write-Host ""
}

# ============================================================================
# VALIDATION DES PRÉREQUIS
# ============================================================================

function Test-Prerequisites {
    Write-Info "Vérification des prérequis..."
    
    # Vérification de GitHub CLI
    try {
        $ghVersion = gh --version 2>$null
        if ($LASTEXITCODE -ne 0) {
            throw "GitHub CLI non trouvé"
        }
        Write-Success "GitHub CLI installé: $($ghVersion.Split("`n")[0])"
    }
    catch {
        Write-Error "GitHub CLI (gh) n'est pas installé ou configuré"
        Write-Info "Installation: https://cli.github.com/"
        exit 1
    }
    
    # Vérification de AWS CLI
    try {
        $awsVersion = aws --version 2>$null
        if ($LASTEXITCODE -ne 0) {
            throw "AWS CLI non trouvé"
        }
        Write-Success "AWS CLI installé: $awsVersion"
    }
    catch {
        Write-Error "AWS CLI n'est pas installé ou configuré"
        Write-Info "Installation: https://aws.amazon.com/cli/"
        exit 1
    }
    
    # Vérification de l'authentification GitHub
    try {
        $ghUser = gh auth status 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw "Non authentifié"
        }
        Write-Success "Authentifié sur GitHub"
    }
    catch {
        Write-Error "Non authentifié sur GitHub"
        Write-Info "Exécutez: gh auth login"
        exit 1
    }
    
    # Vérification de l'authentification AWS
    try {
        $awsIdentity = aws sts get-caller-identity 2>$null | ConvertFrom-Json
        if ($LASTEXITCODE -ne 0) {
            throw "Non authentifié"
        }
        Write-Success "Authentifié sur AWS: $($awsIdentity.Arn)"
    }
    catch {
        Write-Error "Non authentifié sur AWS"
        Write-Info "Exécutez: aws configure"
        exit 1
    }
}

# ============================================================================
# DÉTECTION DU REPOSITORY
# ============================================================================

function Get-RepositoryName {
    if ($Repository -ne "") {
        return $Repository
    }
    
    # Tentative de détection automatique via git
    try {
        $gitRemote = git remote get-url origin 2>$null
        if ($LASTEXITCODE -eq 0) {
            # Extraction du nom du repository depuis l'URL git
            if ($gitRemote -match "github\.com[:/](.+?)(?:\.git)?$") {
                $detectedRepo = $matches[1]
                Write-Info "Repository détecté automatiquement: $detectedRepo"
                return $detectedRepo
            }
        }
    }
    catch {
        # Ignore les erreurs de git
    }
    
    # Demander à l'utilisateur
    Write-Warning "Impossible de détecter automatiquement le repository"
    $userRepo = Read-Host "Entrez le nom du repository (format: username/repo-name)"
    
    if ($userRepo -eq "") {
        Write-Error "Nom de repository requis"
        exit 1
    }
    
    return $userRepo
}

# ============================================================================
# RÉCUPÉRATION DES CREDENTIALS AWS
# ============================================================================

function Get-AWSCredentials {
    Write-Info "Récupération des credentials AWS..."
    
    try {
        # Récupération de l'identité AWS
        $awsIdentity = aws sts get-caller-identity | ConvertFrom-Json
        $accountId = $awsIdentity.Account
        
        # Récupération des credentials depuis AWS CLI
        $awsConfig = aws configure list
        
        # Extraction de l'Access Key ID
        $accessKeyLine = $awsConfig | Where-Object { $_ -match "access_key" }
        if ($accessKeyLine -match "\s+(\w+)\s+") {
            $accessKeyId = $matches[1]
        } else {
            throw "Access Key ID non trouvée"
        }
        
        # Pour la Secret Access Key, on ne peut pas la récupérer directement
        # On demande à l'utilisateur de la fournir
        Write-Warning "La Secret Access Key ne peut pas être récupérée automatiquement"
        $secretAccessKey = Read-Host "Entrez votre AWS Secret Access Key" -AsSecureString
        $secretAccessKeyPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($secretAccessKey))
        
        return @{
            AccountId = $accountId
            AccessKeyId = $accessKeyId
            SecretAccessKey = $secretAccessKeyPlain
        }
    }
    catch {
        Write-Error "Erreur lors de la récupération des credentials AWS: $_"
        exit 1
    }
}

# ============================================================================
# CONFIGURATION DES SECRETS GITHUB
# ============================================================================

function Set-GitHubSecret {
    param(
        [string]$Repository,
        [string]$SecretName,
        [string]$SecretValue
    )
    
    if ($DryRun) {
        Write-Info "[DRY-RUN] Configurerait le secret: $SecretName"
        return
    }
    
    try {
        # Vérifier si le secret existe déjà
        $existingSecrets = gh secret list --repo $Repository 2>$null
        $secretExists = $existingSecrets | Where-Object { $_ -match "^$SecretName\s" }
        
        if ($secretExists -and -not $Force) {
            Write-Warning "Le secret $SecretName existe déjà (utilisez -Force pour le remplacer)"
            return
        }
        
        # Configurer le secret
        $SecretValue | gh secret set $SecretName --repo $Repository
        
        if ($LASTEXITCODE -eq 0) {
            Write-Success "Secret configuré: $SecretName"
        } else {
            throw "Erreur lors de la configuration"
        }
    }
    catch {
        Write-Error "Erreur lors de la configuration du secret $SecretName : $_"
    }
}

# ============================================================================
# FONCTION PRINCIPALE
# ============================================================================

function Main {
    Write-Info "🚀 Configuration des secrets GitHub Actions - Vibe-Tickets"
    Write-Info "============================================================"
    
    if ($Help) {
        Show-Help
        exit 0
    }
    
    if ($DryRun) {
        Write-Warning "MODE DRY-RUN ACTIVÉ - Aucune modification ne sera effectuée"
    }
    
    # Validation des prérequis
    Test-Prerequisites
    
    # Détection du repository
    $repoName = Get-RepositoryName
    Write-Info "Repository cible: $repoName"
    
    # Récupération des credentials AWS
    $awsCredentials = Get-AWSCredentials
    
    Write-Info "Configuration des secrets GitHub Actions..."
    
    # Configuration des secrets
    Set-GitHubSecret -Repository $repoName -SecretName "AWS_ACCESS_KEY_ID" -SecretValue $awsCredentials.AccessKeyId
    Set-GitHubSecret -Repository $repoName -SecretName "AWS_SECRET_ACCESS_KEY" -SecretValue $awsCredentials.SecretAccessKey
    Set-GitHubSecret -Repository $repoName -SecretName "AWS_ACCOUNT_ID" -SecretValue $awsCredentials.AccountId
    
    Write-Info ""
    Write-Success "✅ Configuration terminée !"
    Write-Info ""
    Write-Info "PROCHAINES ÉTAPES :"
    Write-Info "1. Verifiez les secrets dans Settings - Secrets and variables - Actions"
    Write-Info "2. Configurez les variables d'environnement si nécessaire"
    Write-Info "3. Testez le pipeline en faisant un push sur main"
    Write-Info ""
    Write-Info "📖 Documentation complète: docs/GITHUB_ACTIONS_SETUP.md"
}

# ============================================================================
# POINT D'ENTRÉE
# ============================================================================

try {
    Main
}
catch {
    Write-Error "Erreur inattendue: $_"
    exit 1
}
