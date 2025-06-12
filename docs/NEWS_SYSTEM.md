# 📰 Système de Gestion d'Actualités/Blog - Vibe-Tickets

## 📋 Vue d'ensemble

Le système de gestion d'actualités permet aux administrateurs de créer, modifier et gérer des articles de blog pour l'application Vibe-Tickets. Les utilisateurs peuvent consulter les articles publiés pour rester informés des dernières actualités et événements.

## 🏗️ Architecture

### Base de données
- **Table** : `news`
- **Colonnes** :
  - `id` : Clé primaire auto-incrémentée (BIGINT)
  - `title` : Titre de l'article (VARCHAR 200, NOT NULL)
  - `description` : Contenu de l'article (TEXT, NOT NULL)
  - `author_id` : Référence vers l'utilisateur admin (FOREIGN KEY)
  - `created_date` : Date de création (TIMESTAMP, NOT NULL, défaut NOW())
  - `updated_date` : Date de modification (TIMESTAMP, NULL)
  - `published` : Statut de publication (BOOLEAN, NOT NULL, défaut true)
  - `image_url` : URL d'image optionnelle (VARCHAR 500, NULL)

### Classes Java
- **Model** : `News.java` - Entité JPA avec validations
- **Repository** : `NewsRepository.java` - Accès aux données essentielles (findByTitle, findByAuthor, findByDate)
- **Service** : `NewsService.java` (interface) et `NewsServiceImpl.java` (implémentation simplifiée)
- **Controller** : `NewsController.java` - Endpoints REST essentiels

## 🔗 API Endpoints

### Consultation publique (sans authentification)

#### Récupérer tous les articles publiés
```http
GET /api/news/published?page=0&size=10
```

#### Récupérer un article publié par ID
```http
GET /api/news/published/{id}
```



### Administration (authentification ADMIN requise)

#### Récupérer tous les articles (publiés et non publiés)
```http
GET /api/news?page=0&size=10&sortBy=createdDate&sortDir=desc
```

#### Récupérer un article par ID
```http
GET /api/news/{id}
```

#### Créer un nouvel article
```http
POST /api/news?authorId={authorId}
Content-Type: application/json

{
  "title": "Titre de l'article",
  "description": "Contenu de l'article...",
  "published": true,
  "imageUrl": "https://example.com/image.jpg"
}
```

#### Mettre à jour un article
```http
PUT /api/news/{id}
Content-Type: application/json

{
  "title": "Nouveau titre",
  "description": "Nouveau contenu...",
  "published": true,
  "imageUrl": "https://example.com/new-image.jpg"
}
```

#### Supprimer un article
```http
DELETE /api/news/{id}
```

#### Rechercher des articles par titre
```http
GET /api/news/search/title?title=concert
```

#### Récupérer les articles d'un auteur
```http
GET /api/news/author/{authorId}
```

#### Récupérer les articles par période
```http
GET /api/news/search/date?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59
```

## 📝 Exemples d'utilisation

### Créer un article
```bash
curl -X POST "http://localhost:8080/api/news?authorId=2" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "title": "Nouvelle programmation concerts 2024",
    "description": "Découvrez tous les concerts prévus cette année...",
    "published": true,
    "imageUrl": "https://example.com/concerts2024.jpg"
  }'
```

### Rechercher des articles par titre
```bash
curl "http://localhost:8080/api/news/search/title?title=festival"
```

### Récupérer les articles d'un auteur
```bash
curl "http://localhost:8080/api/news/author/2"
```

## 🔒 Sécurité

- **Consultation** : Accessible à tous (articles publiés uniquement)
- **Administration** : Réservée aux utilisateurs avec le rôle `ADMIN`
- **Validation** : Toutes les données d'entrée sont validées
- **Gestion d'erreurs** : Réponses JSON structurées pour toutes les erreurs

## ✅ Fonctionnalités

### Gestion des articles
- ✅ Création, lecture, mise à jour, suppression (CRUD)
- ✅ Statut de publication (publié/brouillon)
- ✅ Gestion des auteurs
- ✅ Images d'illustration optionnelles
- ✅ Horodatage automatique (création/modification)

### Recherche et filtrage essentiels
- ✅ Recherche par titre (findByTitle)
- ✅ Filtrage par auteur (findByAuthor)
- ✅ Filtrage par période (findByDate)
- ✅ Filtrage par statut de publication

### Pagination
- ✅ Pagination sur les endpoints de liste principaux
- ✅ Tri par date de création décroissante

## 🧪 Tests

### Tests unitaires
Le fichier `NewsServiceTest.java` contient des tests complets pour :
- ✅ Opérations CRUD
- ✅ Validation des données
- ✅ Gestion des erreurs
- ✅ Recherche et filtrage
- ✅ Pagination

### Exécuter les tests
```bash
mvn test -Dtest=NewsServiceTest
```

## 📊 Données d'exemple

Le système contient déjà 5 articles d'exemple :
1. "Nouvelle saison de concerts à Paris" (publié)
2. "Festival Rock en Seine 2024 : La programmation dévoilée" (publié)
3. "Théâtres parisiens : Nouvelle saison théâtrale" (publié)
4. "Guide des spectacles familiaux" (brouillon)
5. "Les événements sportifs à ne pas manquer" (publié)

## 🔧 Configuration

### Validation des données
- **Titre** : Obligatoire, max 200 caractères
- **Description** : Obligatoire, texte libre
- **Auteur** : Obligatoire, doit exister en base
- **URL image** : Optionnelle, max 500 caractères

### Index de base de données
- Index sur `published` pour les requêtes de filtrage
- Index sur `author_id` pour les requêtes par auteur
- Index sur `created_date` pour le tri chronologique

## 🚀 Intégration frontend

Le système est prêt pour l'intégration avec le frontend Angular :
- Endpoints REST standardisés
- Réponses JSON structurées
- Gestion CORS configurée
- Pagination compatible avec Angular Material

## 📈 Évolutions possibles

- 🔄 Système de catégories d'articles
- 🔄 Commentaires sur les articles
- 🔄 Système de tags/mots-clés
- 🔄 Gestion des médias (upload d'images)
- 🔄 Système de notifications pour nouveaux articles
- 🔄 Versionning des articles
- 🔄 Planification de publication
