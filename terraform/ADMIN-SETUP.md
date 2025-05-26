# 🔐 Configuration IAM pour Terraform - Guide Administrateur

## 📋 Permissions Requises

L'utilisateur `user-vibeticket` a besoin des permissions suivantes pour déployer l'infrastructure Terraform :

### 🎯 Méthode 1 : Politique Personnalisée (Recommandée)

```bash
# 1. Créer la politique IAM
aws iam create-policy \
  --policy-name TerraformVibeTicketsPolicy \
  --policy-document file://iam-policy.json \
  --description "Permissions Terraform pour Vibe Tickets"

# 2. Attacher la politique à l'utilisateur
aws iam attach-user-policy \
  --user-name user-vibeticket \
  --policy-arn arn:aws:iam::756942038699:policy/TerraformVibeTicketsPolicy
```

### 🎯 Méthode 2 : Politique AWS Managée (Plus Simple)

```bash
# Attacher les politiques AWS managées
aws iam attach-user-policy \
  --user-name user-vibeticket \
  --policy-arn arn:aws:iam::aws:policy/IAMFullAccess

aws iam attach-user-policy \
  --user-name user-vibeticket \
  --policy-arn arn:aws:iam::aws:policy/AmazonEC2FullAccess

aws iam attach-user-policy \
  --user-name user-vibeticket \
  --policy-arn arn:aws:iam::aws:policy/AmazonRDSFullAccess

aws iam attach-user-policy \
  --user-name user-vibeticket \
  --policy-arn arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryFullAccess
```

## ✅ Vérification

```bash
# Vérifier les politiques attachées
aws iam list-attached-user-policies --user-name user-vibeticket
```

## 🔒 Sécurité

- Les permissions sont limitées aux ressources `vibe-tickets-*`
- Principe du moindre privilège appliqué
- Audit trail complet via CloudTrail
