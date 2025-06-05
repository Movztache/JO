# 🎫 Vibe-Tickets - Application de Billetterie

[![AWS](https://img.shields.io/badge/AWS-Cloud-orange)](https://aws.amazon.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-Latest-red)](https://angular.io/)
[![Docker](https://img.shields.io/badge/Docker-Containerized-blue)](https://www.docker.com/)
[![Terraform](https://img.shields.io/badge/Terraform-IaC-purple)](https://www.terraform.io/)

## 📋 Vue d'ensemble

Vibe-Tickets est une application de billetterie moderne développée avec Spring Boot (backend) et Angular (frontend), déployée automatiquement sur AWS avec Terraform.

### 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        ARCHITECTURE AWS                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────┐    ┌──────────────┐    ┌─────────────────┐    │
│  │  CloudFront │    │     EC2      │    │       RDS       │    │
│  │  (Frontend) │────│ (Spring Boot)│────│  (PostgreSQL)   │    │
│  │   Angular   │    │   + Docker   │    │                 │    │
│  └─────────────┘    └──────────────┘    └─────────────────┘    │
│                                                                 │
│  ┌─────────────┐    ┌──────────────┐                           │
│  │     ECR     │    │  Elastic IP  │                           │
│  │ (Images     │    │  (IP Stable) │                           │
│  │  Docker)    │    │              │                           │
│  └─────────────┘    └──────────────┘                           │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 🚀 Fonctionnalités

- **Authentification JWT** : Système sécurisé avec rôles (Admin/User)
- **Gestion des offres** : CRUD complet pour les événements
- **API REST** : Backend Spring Boot avec documentation Swagger
- **Interface moderne** : Frontend Angular responsive
- **Déploiement automatisé** : Infrastructure as Code avec Terraform
- **Sécurité** : Clés SSH dynamiques, chiffrement, CORS configuré

## 🛠️ Technologies

### Backend
- **Spring Boot 3.x** : Framework Java
- **PostgreSQL** : Base de données
- **JWT** : Authentification
- **BCrypt** : Hachage des mots de passe
- **Docker** : Containerisation

### Frontend
- **Angular** : Framework TypeScript
- **CloudFront** : CDN AWS
- **S3** : Hébergement statique

### Infrastructure
- **AWS** : Cloud provider
- **Terraform** : Infrastructure as Code
- **Docker** : Containerisation
- **ECR** : Registry d'images Docker

## 📦 Prérequis

### Outils requis
- **AWS CLI** configuré avec les bonnes permissions
- **Docker Desktop** installé et démarré
- **Terraform** >= 1.0
- **Maven** >= 3.8
- **PowerShell** (Windows)

### Configuration AWS
```bash
# Configurer AWS CLI
aws configure
# Région recommandée : eu-west-3 (Paris)
```

### Permissions IAM requises
- EC2FullAccess
- RDSFullAccess
- ECRFullAccess
- IAMFullAccess (pour les rôles)

## 🚀 Déploiement

### 1. Déploiement automatisé avec GitHub Actions (Recommandé)

Le projet utilise des pipelines GitHub Actions pour un déploiement entièrement automatisé :

```bash
# Déploiement automatique sur push vers main
git add .
git commit -m "feat: nouvelle fonctionnalité"
git push origin main
```

**Fonctionnalités du pipeline CI/CD :**
- ✅ Tests automatiques (unitaires + intégration)
- ✅ Build Maven et création d'image Docker
- ✅ Scan de sécurité des images
- ✅ Push automatique vers Amazon ECR
- ✅ Déploiement Terraform automatisé
- ✅ Health checks post-déploiement
- ✅ Notifications de statut

📖 **[Guide complet GitHub Actions](docs/GITHUB_ACTIONS_SETUP.md)**

### 2. Déploiement manuel (Développement local)

```bash
# Cloner le repository
git clone <repository-url>
cd vibe-tickets

# Configurer les variables Terraform
cp terraform/terraform.tfvars.example terraform/terraform.tfvars
# Éditer terraform.tfvars avec vos valeurs
```

```powershell
# Déploiement complet (recommandé)
.\deploy.ps1

# Déploiement sans rebuild Maven
.\deploy.ps1 -SkipBuild

# Déploiement pour un environnement spécifique
.\deploy.ps1 -Environment "staging"
```

### 3. Vérification

Le script affiche automatiquement :
- ✅ URL de l'application
- ✅ Commandes SSH pour le debug
- ✅ Identifiants de test

### 4. Health Check automatisé

```bash
# Vérification de la santé de l'application
./scripts/ci/health-check.sh http://13.36.187.182:8080
```

## 🔧 Configuration

### Variables Terraform

Créez `terraform/terraform.tfvars` :

```hcl
# Configuration AWS
aws_region = "eu-west-3"
my_ip      = "YOUR_PUBLIC_IP/32"

# Configuration application
project_name = "vibe-tickets"
environment  = "prod"

# Configuration base de données
db_name     = "vibe_tickets"
db_username = "adminco"
db_password = "your-secure-password"

# URL du frontend
frontend_url = "http://your-cloudfront-url"
```

### Variables d'environnement

Le script `deploy.ps1` configure automatiquement :
- `DATABASE_URL`
- `CORS_ORIGINS`
- `JWT_SECRET`
- `SPRING_PROFILES_ACTIVE=docker`

## 🔄 Pipelines CI/CD

### GitHub Actions

Le projet utilise des pipelines GitHub Actions pour l'automatisation complète :

#### 🚀 Pipeline Principal (`backend-ci-cd.yml`)
- **Déclencheurs** : Push sur `main`, workflow manuel
- **Étapes** : Tests → Build → Docker → Deploy AWS → Health Checks
- **Environnements** : dev, staging, prod
- **Notifications** : Rapports automatiques de déploiement

#### 🔍 Pipeline PR (`backend-pr.yml`)
- **Déclencheurs** : Pull Requests vers `main`
- **Étapes** : Validation → Tests → Docker Build
- **Feedback** : Commentaires automatiques sur les PR

### Monitoring et Qualité

- **Tests automatiques** : Unitaires + Intégration avec PostgreSQL
- **Couverture de code** : Rapports JaCoCo automatiques
- **Sécurité** : Scan Trivy des images Docker
- **Performance** : Health checks et métriques de réponse

📖 **[Configuration complète des pipelines](docs/GITHUB_ACTIONS_SETUP.md)**

## 🧪 Tests

### Identifiants de test

```
👤 ADMINISTRATEUR
Email    : admin@vibe-tickets.com
Password : AdminVibe2024!

👤 UTILISATEUR 1
Email    : alice.martin@email.com
Password : AliceSecure123!

👤 UTILISATEUR 2
Email    : bob.dupont@email.com
Password : BobStrong456!
```

### Endpoints API

```
🔍 Health Check
GET http://<IP>:8080/actuator/health

🔐 Authentification
POST http://<IP>:8080/api/authentication/register
POST http://<IP>:8080/api/authentication/login

🎫 Offres
GET http://<IP>:8080/api/offers
```

## 🐛 Dépannage

### Connexion SSH

```bash
# Connexion à l'instance EC2
ssh -i terraform/ssh-key ec2-user@<IP>

# Vérifier les logs de l'application
docker logs vibe-tickets

# Vérifier le statut du conteneur
docker ps
```

### Logs utiles

```bash
# Logs du déploiement EC2
sudo tail -f /var/log/user-data.log

# Logs de l'application Spring Boot
docker logs vibe-tickets -f

# Statut des services
systemctl status docker
```

### Problèmes courants

| Problème | Solution |
|----------|----------|
| Erreur 500 sur /api/auth | Vérifier les logs BCrypt et les rôles en base |
| CORS Error | Vérifier la configuration CORS dans application-docker.properties |
| Connexion DB échoue | Vérifier les Security Groups RDS |
| Image Docker non trouvée | Vérifier le push ECR et les permissions IAM |

## 📁 Structure du projet

```
vibe-tickets/
├── .github/                     # GitHub Actions CI/CD
│   └── workflows/              # Pipelines automatisés
│       ├── backend-ci-cd.yml  # Pipeline principal (main)
│       └── backend-pr.yml     # Validation Pull Requests
├── src/                          # Code source Spring Boot
│   ├── main/java/               # Code Java
│   └── main/resources/          # Configuration
├── terraform/                   # Infrastructure as Code
│   ├── main.tf                 # Configuration principale
│   ├── variables.tf            # Variables
│   ├── outputs.tf              # Outputs
│   └── terraform.tfvars        # Variables personnalisées
├── scripts/                     # Scripts d'automatisation
│   ├── ci/                     # Scripts CI/CD
│   │   └── health-check.sh    # Vérification santé application
│   ├── deployment/             # Scripts de déploiement
│   │   ├── deploy.ps1         # Script principal de déploiement
│   │   └── user-data.sh       # Script d'initialisation EC2
│   ├── database/              # Scripts de base de données
│   │   └── connect_database.ps1  # Tunnel SSH vers RDS
│   ├── testing/               # Scripts de tests
│   │   └── test-local.sh      # Tests locaux
│   └── README.md              # Documentation des scripts
├── deploy.ps1                  # Wrapper de déploiement (racine)
├── Dockerfile                  # Image Docker
└── README.md                   # Documentation principale
```

## 🔒 Sécurité

### Bonnes pratiques implémentées

- ✅ **Clés SSH dynamiques** : Nouvelles clés à chaque déploiement
- ✅ **Chiffrement** : EBS et RDS chiffrés
- ✅ **Principe du moindre privilège** : IAM policies restrictives
- ✅ **Secrets** : Mots de passe non commitées
- ✅ **CORS** : Configuration restrictive
- ✅ **JWT** : Tokens sécurisés avec expiration

### Fichiers sensibles (.gitignore)

```

terraform/*.tfstate*
terraform/.terraform/

# Configuration locale
terraform/terraform.tfvars
```

