# 📜 Scripts Vibe-Tickets

Ce dossier contient tous les scripts d'automatisation pour le projet Vibe-Tickets, organisés par catégorie selon les bonnes pratiques entreprise.

## 📁 Structure

```
scripts/
├── deployment/          # Scripts de déploiement
│   ├── deploy.ps1      # Script principal de déploiement automatisé
│   └── user-data.sh    # Script d'initialisation EC2
├── database/           # Scripts de base de données
│   └── connect_database.ps1  # Tunnel SSH vers RDS
├── testing/            # Scripts de tests
│   └── test-local.sh   # Tests locaux avant déploiement
└── README.md          # Cette documentation
```

## 🚀 Scripts de déploiement

### `deployment/deploy.ps1`
**Script principal de déploiement automatisé**

```powershell
# Déploiement complet
.\scripts\deployment\deploy.ps1

# Déploiement sans rebuild Maven
.\scripts\deployment\deploy.ps1 -SkipBuild

# Déploiement pour un environnement spécifique
.\scripts\deployment\deploy.ps1 -Environment "staging"
```

**Fonctionnalités :**
- ✅ Build Maven automatique
- ✅ Création d'image Docker avec timestamp
- ✅ Génération automatique de clés SSH sécurisées
- ✅ Push vers Amazon ECR
- ✅ Déploiement Terraform automatisé
- ✅ Vérification de santé de l'application

### `deployment/user-data.sh`
**Script d'initialisation EC2**

Script bash exécuté automatiquement au démarrage de l'instance EC2 :
- Installation de Docker, AWS CLI, PostgreSQL client
- Configuration ECR et téléchargement de l'image
- Initialisation de la base de données
- Démarrage de l'application

## 🗄️ Scripts de base de données

### `database/connect_database.ps1`
**Tunnel SSH vers RDS pour développement**

```powershell
.\scripts\database\connect_database.ps1
```

**Fonctionnalités :**
- ✅ Création automatique d'un tunnel SSH vers RDS
- ✅ Configuration pour IntelliJ IDEA Database Tool
- ✅ Port local 5433 → RDS via EC2
- ✅ Instructions de connexion détaillées

**Configuration IntelliJ :**
- Host: `localhost`
- Port: `5433`
- Database: `vibe_tickets`
- User: `adminco`
- Password: `adminco123`

## 🧪 Scripts de tests

### `testing/test-local.sh`
**Tests locaux avant déploiement**

```bash
chmod +x scripts/testing/test-local.sh
./scripts/testing/test-local.sh
```

**Vérifications :**
- ✅ Compilation Maven
- ✅ Validation des hashes BCrypt
- ✅ Build Docker
- ✅ Validation Terraform
- ✅ Vérification des corrections appliquées

## 🎯 Utilisation recommandée

### Workflow de développement

1. **Tests locaux** (optionnel)
   ```bash
   ./scripts/testing/test-local.sh
   ```

2. **Déploiement**
   ```powershell
   .\deploy.ps1  # Utilise le wrapper à la racine
   ```

3. **Debug base de données** (si nécessaire)
   ```powershell
   .\scripts\database\connect_database.ps1
   ```

### Workflow de production

1. **Déploiement automatisé complet**
   ```powershell
   .\deploy.ps1 -Environment "prod"
   ```

2. **Vérification**
   - Health check automatique
   - Tests d'authentification
   - Monitoring des logs

## 🔒 Sécurité

### Bonnes pratiques implémentées

- ✅ **Clés SSH dynamiques** : Générées à chaque déploiement
- ✅ **Nettoyage automatique** : Suppression des anciennes clés
- ✅ **Paramètres sécurisés** : Pas de secrets en dur
- ✅ **Validation** : Vérifications avant déploiement

### Fichiers sensibles

Les scripts gèrent automatiquement :
- Clés SSH temporaires (ignorées par Git)
- Tokens AWS (via AWS CLI)
- Variables d'environnement sécurisées

## 📋 Prérequis

### Outils requis
- **PowerShell** (Windows)
- **Bash** (pour test-local.sh)
- **AWS CLI** configuré
- **Docker Desktop**
- **Terraform**
- **Maven**

### Configuration
- Fichier `terraform/terraform.tfvars` configuré
- Permissions IAM appropriées
- Docker Desktop démarré

## 🐛 Dépannage

### Erreurs communes

| Erreur | Script | Solution |
|--------|--------|----------|
| `deploy.ps1 not found` | deploy.ps1 | Vérifier la structure des dossiers |
| `Permission denied` | test-local.sh | `chmod +x scripts/testing/test-local.sh` |
| `SSH tunnel failed` | connect_database.ps1 | Vérifier que l'instance EC2 est démarrée |
| `Terraform validation failed` | deploy.ps1 | Vérifier `terraform.tfvars` |

### Logs utiles

```powershell
# Logs de déploiement
Get-Content scripts\deployment\deploy.log

# Logs EC2 (via SSH)
ssh -i terraform/ssh-key ec2-user@<IP> 'sudo tail -f /var/log/user-data.log'

# Logs application
ssh -i terraform/ssh-key ec2-user@<IP> 'docker logs vibe-tickets'
```

## 🔄 Maintenance

### Mise à jour des scripts

1. Modifier le script approprié dans son dossier
2. Tester localement si possible
3. Committer les changements
4. Tester en environnement de développement

### Ajout de nouveaux scripts

1. Placer dans le dossier approprié (`deployment/`, `database/`, `testing/`)
2. Ajouter la documentation dans ce README
3. Respecter les conventions de nommage
4. Ajouter les commentaires appropriés

---

**Scripts maintenus par l'équipe DevOps Vibe-Tickets**
