# 🔐 Clés SSH pour l'Instance EC2

## 📋 Fichiers

- **`ssh-key`** : Clé privée RSA 2048 bits (GARDEZ SECRÈTE !)
- **`ssh-key.pub`** : Clé publique (déployée sur l'instance EC2)

## 🔒 Sécurité

⚠️ **IMPORTANT** : La clé privée `ssh-key` doit rester confidentielle !
- Ne jamais la committer dans Git
- Permissions : 600 (lecture seule pour le propriétaire)
- Utilisée uniquement pour l'accès SSH à l'instance EC2

## 🚀 Utilisation

```bash
# Connexion SSH à l'instance
ssh -i terraform/ssh-key ec2-user@<IP_INSTANCE>

# Exemple avec l'IP actuelle
ssh -i terraform/ssh-key ec2-user@15.237.210.0
```

## 🔄 Régénération

Si vous devez régénérer les clés :
```bash
cd terraform
ssh-keygen -t rsa -b 2048 -f ssh-key -N ""
terraform apply  # Redéploie avec la nouvelle clé
```
