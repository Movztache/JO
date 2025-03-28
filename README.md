# JO
A version for the Olympics


## Gestion des erreurs
| Code HTTP | Type d'erreur | Format de réponse | Cas d'utilisation |
| --- | --- | --- | --- |
| 400 | MethodArgumentNotValidException | `{"champErreur1": "message", "champErreur2": "message", ...}` | Validation des données d'entrée (`@Valid`) |
| 403 | SecurityException | `{"error": "Accès refusé", "message": "détail"}` | Accès non autorisé à une ressource |
| 404 | EntityNotFoundException | `{"error": "Ressource non trouvée", "message": "détail"}` | Ressource demandée introuvable |
| 500 | Exception | `{"error": "Erreur interne du serveur", "message": "Une erreur inattendue s'est produite. Veuillez réessayer plus tard."}` | Erreurs non gérées spécifiquement |
