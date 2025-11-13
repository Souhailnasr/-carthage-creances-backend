# Guide de Test Postman - Suppression d'Enquête (DELETE)

## Configuration de la Requête

### 1. Méthode et URL

**Méthode** : `DELETE`

**URL** : 
```
http://localhost:8089/carthage-creance/api/enquettes/{id}
```

**Exemple** :
```
http://localhost:8089/carthage-creance/api/enquettes/8
```

Remplacez `{id}` par l'ID réel de l'enquête que vous voulez supprimer.

---

## Configuration des Headers

### Headers Requis

Dans l'onglet **Headers** de Postman, ajoutez :

| Key | Value |
|-----|-------|
| `Authorization` | `Bearer {votre_token_jwt}` |
| `Content-Type` | `application/json` |

**Exemple de valeur pour Authorization** :
```
Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

### Comment Obtenir le Token JWT ?

1. **Via l'application frontend** :
   - Connectez-vous à l'application
   - Ouvrez les DevTools (F12)
   - Allez dans l'onglet Application/Storage
   - Cherchez le token dans localStorage ou sessionStorage
   - Copiez le token complet

2. **Via l'endpoint de login** :
   - Faites une requête POST à `/api/auth/login`
   - Le token sera dans la réponse
   - Copiez-le pour l'utiliser dans les autres requêtes

---

## Configuration du Body

**IMPORTANT** : Pour une requête DELETE, le body est généralement vide. 

Dans l'onglet **Body**, sélectionnez :
- `none` (pas de body)

---

## Scénarios de Test

### Scénario 1 : Suppression Réussie (204 NO_CONTENT)

**Configuration** :
- **Méthode** : DELETE
- **URL** : `http://localhost:8089/carthage-creance/api/enquettes/8`
- **Headers** : Authorization avec token valide
- **Body** : Aucun

**Réponse Attendue** :
- **Status** : `204 No Content`
- **Body** : Vide (aucun contenu)
- **Headers** : Peuvent contenir des informations de cache

**Vérification** :
1. ✅ Le statut est 204
2. ✅ Le body est vide
3. ✅ Vérifiez dans la base de données que l'enquête a été supprimée
4. ✅ Vérifiez que les validations associées ont été supprimées

**Requête Postman** :
```
DELETE http://localhost:8089/carthage-creance/api/enquettes/8
Authorization: Bearer {token}
```

---

### Scénario 2 : Enquête Non Trouvée (404 NOT_FOUND)

**Configuration** :
- **Méthode** : DELETE
- **URL** : `http://localhost:8089/carthage-creance/api/enquettes/99999`
- **Headers** : Authorization avec token valide
- **Body** : Aucun

**Réponse Attendue** :
- **Status** : `404 Not Found`
- **Body** : 
  ```json
  "Enquête non trouvée ou erreur lors de la suppression : Enquette not found with id: 99999"
  ```

**Vérification** :
1. ✅ Le statut est 404
2. ✅ Le message d'erreur est clair

---

### Scénario 3 : Contrainte de Base de Données (409 CONFLICT)

**Configuration** :
- **Méthode** : DELETE
- **URL** : `http://localhost:8089/carthage-creance/api/enquettes/{id}`
- **Headers** : Authorization avec token valide
- **Body** : Aucun

**Réponse Attendue** :
- **Status** : `409 Conflict`
- **Body** : 
  ```
  "Impossible de supprimer l'enquête : une contrainte de base de données empêche la suppression. L'enquête est probablement liée à un dossier ou à d'autres entités."
  ```

**Note** : Cette erreur peut se produire si l'enquête a des relations qui empêchent sa suppression.

---

### Scénario 4 : Erreur Serveur (500 INTERNAL_SERVER_ERROR)

**Configuration** :
- **Méthode** : DELETE
- **URL** : `http://localhost:8089/carthage-creance/api/enquettes/{id}`
- **Headers** : Authorization avec token valide
- **Body** : Aucun

**Réponse Attendue** :
- **Status** : `500 Internal Server Error`
- **Body** : 
  ```
  "Erreur serveur lors de la suppression de l'enquête : [message détaillé]"
  ```

---

### Scénario 5 : Token Invalide ou Manquant (401 UNAUTHORIZED)

**Configuration** :
- **Méthode** : DELETE
- **URL** : `http://localhost:8089/carthage-creance/api/enquettes/8`
- **Headers** : Pas d'Authorization ou token invalide
- **Body** : Aucun

**Réponse Attendue** :
- **Status** : `401 Unauthorized` ou `403 Forbidden`
- **Body** : Message d'erreur d'authentification

---

## Étapes Détaillées dans Postman

### Étape 1 : Créer une Nouvelle Requête

1. Ouvrez Postman
2. Cliquez sur **New** → **HTTP Request**
3. Ou utilisez le raccourci `Ctrl+N` (Windows) / `Cmd+N` (Mac)

### Étape 2 : Configurer la Méthode

1. Dans le menu déroulant à gauche, sélectionnez **DELETE**
2. Ou tapez `DELETE` dans la barre d'adresse

### Étape 3 : Entrer l'URL

1. Dans la barre d'adresse, entrez :
   ```
   http://localhost:8089/carthage-creance/api/enquettes/8
   ```
2. Remplacez `8` par l'ID de l'enquête à supprimer

### Étape 4 : Ajouter les Headers

1. Cliquez sur l'onglet **Headers**
2. Ajoutez les headers suivants :

   **Header 1** :
   - Key : `Authorization`
   - Value : `Bearer {votre_token}`
   - ✅ Cochez la case pour activer

   **Header 2** :
   - Key : `Content-Type`
   - Value : `application/json`
   - ✅ Cochez la case pour activer

### Étape 5 : Configurer le Body

1. Cliquez sur l'onglet **Body**
2. Sélectionnez **none** (pas de body pour DELETE)

### Étape 6 : Envoyer la Requête

1. Cliquez sur le bouton **Send**
2. Attendez la réponse

### Étape 7 : Analyser la Réponse

Dans l'onglet **Response**, vous verrez :

- **Status** : Code de statut HTTP (204, 404, 409, 500, etc.)
- **Time** : Temps de réponse
- **Size** : Taille de la réponse
- **Body** : Contenu de la réponse (vide pour 204, message d'erreur pour les autres)

---

## Collection Postman Complète

### Créer une Collection

1. Cliquez sur **New** → **Collection**
2. Nommez-la "Carthage Créance - Enquêtes"
3. Ajoutez les requêtes suivantes :

### Requêtes à Ajouter

#### 1. DELETE - Supprimer Enquête (Succès)
```
DELETE http://localhost:8089/carthage-creance/api/enquettes/{{enqueteId}}
Headers:
  Authorization: Bearer {{token}}
```

#### 2. DELETE - Supprimer Enquête (Non Trouvée)
```
DELETE http://localhost:8089/carthage-creance/api/enquettes/99999
Headers:
  Authorization: Bearer {{token}}
```

#### 3. GET - Vérifier Enquête Existe
```
GET http://localhost:8089/carthage-creance/api/enquettes/{{enqueteId}}
Headers:
  Authorization: Bearer {{token}}
```

#### 4. GET - Liste des Enquêtes en Attente
```
GET http://localhost:8089/carthage-creance/api/validation/enquetes/en-attente
Headers:
  Authorization: Bearer {{token}}
```

### Variables d'Environnement

Créez un environnement Postman avec :

| Variable | Valeur Initiale | Description |
|----------|----------------|-------------|
| `baseUrl` | `http://localhost:8089/carthage-creance` | URL de base de l'API |
| `token` | `{votre_token}` | Token JWT |
| `enqueteId` | `8` | ID de l'enquête à tester |

**Utilisation dans les requêtes** :
```
{{baseUrl}}/api/enquettes/{{enqueteId}}
Authorization: Bearer {{token}}
```

---

## Tests Automatisés avec Postman Tests

### Ajouter des Tests à la Requête

Dans l'onglet **Tests** de Postman, ajoutez :

```javascript
// Test 1 : Vérifier le statut 204
pm.test("Status code is 204", function () {
    pm.response.to.have.status(204);
});

// Test 2 : Vérifier que le body est vide
pm.test("Response body is empty", function () {
    pm.response.to.be.empty;
});

// Test 3 : Vérifier le temps de réponse
pm.test("Response time is less than 1000ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(1000);
});

// Test 4 : Vérifier les headers
pm.test("Content-Type header exists", function () {
    pm.response.to.have.header("Content-Type");
});
```

### Tests pour les Erreurs

```javascript
// Test pour 404
pm.test("Status code is 404 for non-existent enquete", function () {
    pm.response.to.have.status(404);
});

pm.test("Error message is present", function () {
    const responseBody = pm.response.text();
    pm.expect(responseBody).to.include("Enquête non trouvée");
});

// Test pour 409
pm.test("Status code is 409 for constraint violation", function () {
    pm.response.to.have.status(409);
});

pm.test("Constraint error message is present", function () {
    const responseBody = pm.response.text();
    pm.expect(responseBody).to.include("contrainte de base de données");
});
```

---

## Vérification dans la Base de Données

### Avant la Suppression

1. Connectez-vous à phpMyAdmin
2. Sélectionnez la base `carthage_creances`
3. Allez dans la table `enquette`
4. Notez l'ID de l'enquête à supprimer
5. Vérifiez les validations associées dans `validation_enquetes`

**Requête SQL** :
```sql
-- Vérifier l'enquête
SELECT * FROM enquette WHERE id = 8;

-- Vérifier les validations associées
SELECT * FROM validation_enquetes WHERE enquete_id = 8;
```

### Après la Suppression

1. Exécutez la requête DELETE dans Postman
2. Vérifiez que le statut est 204
3. Retournez dans phpMyAdmin
4. Vérifiez que l'enquête a été supprimée

**Requête SQL** :
```sql
-- Vérifier que l'enquête n'existe plus
SELECT * FROM enquette WHERE id = 8;
-- Devrait retourner 0 lignes

-- Vérifier que les validations ont été supprimées
SELECT * FROM validation_enquetes WHERE enquete_id = 8;
-- Devrait retourner 0 lignes
```

---

## Exemple de Requête Complète

### Configuration Postman

**Méthode** : `DELETE`

**URL** :
```
http://localhost:8089/carthage-creance/api/enquettes/8
```

**Headers** :
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjMzLCJlbWFpbCI6InNvdWhhaWxuc3Jwcm85OEBnbWFpbC5jb20iLCJub20iOiJuYXNyIiwicHJlbm9tIjoic291aGFpbCIsInJvbGUiOiJBRE1JTl9ET1NTSUVSIn0.xxxxxxxxxxxxx
Content-Type: application/json
```

**Body** : `none`

**Tests** :
```javascript
pm.test("Status is 204", () => pm.response.to.have.status(204));
pm.test("Body is empty", () => pm.response.to.be.empty);
```

---

## Dépannage

### Problème : 401 Unauthorized

**Cause** : Token manquant ou invalide

**Solution** :
1. Vérifiez que le header `Authorization` est présent
2. Vérifiez que le token est valide (pas expiré)
3. Obtenez un nouveau token via l'endpoint de login

### Problème : 404 Not Found

**Cause** : L'enquête n'existe pas

**Solution** :
1. Vérifiez l'ID de l'enquête dans la base de données
2. Utilisez un ID valide

### Problème : 409 Conflict

**Cause** : Contrainte de base de données

**Solution** :
1. Vérifiez les relations de l'enquête dans la base de données
2. Vérifiez les logs du backend pour plus de détails
3. Supprimez d'abord les relations qui empêchent la suppression

### Problème : 500 Internal Server Error

**Cause** : Erreur serveur

**Solution** :
1. Vérifiez les logs du backend
2. Vérifiez que la base de données est accessible
3. Vérifiez que toutes les dépendances sont correctement configurées

---

## Checklist de Test

Avant de tester, assurez-vous que :

- [ ] Le serveur backend est démarré
- [ ] La base de données est accessible
- [ ] Vous avez un token JWT valide
- [ ] L'enquête existe dans la base de données
- [ ] Vous connaissez l'ID de l'enquête à supprimer

Après le test, vérifiez que :

- [ ] Le statut de la réponse est 204 (succès) ou un code d'erreur approprié
- [ ] Le message d'erreur est clair (si erreur)
- [ ] L'enquête a été supprimée de la base de données (si succès)
- [ ] Les validations associées ont été supprimées (si succès)
- [ ] Les logs du backend ne montrent pas d'erreurs

---

## Exemple de Réponse Complète

### Succès (204)

**Status** : `204 No Content`

**Headers** :
```
Content-Length: 0
Date: Mon, 12 Nov 2025 20:00:00 GMT
```

**Body** : (vide)

### Erreur (409)

**Status** : `409 Conflict`

**Headers** :
```
Content-Type: text/plain;charset=UTF-8
Content-Length: 150
```

**Body** :
```
Impossible de supprimer l'enquête : une contrainte de base de données empêche la suppression. L'enquête est probablement liée à un dossier ou à d'autres entités.
```

---

## Commandes cURL (Alternative)

Si vous préférez utiliser cURL au lieu de Postman :

```bash
# Suppression réussie
curl -X DELETE \
  http://localhost:8089/carthage-creance/api/enquettes/8 \
  -H "Authorization: Bearer {votre_token}" \
  -H "Content-Type: application/json" \
  -v

# Avec verbose pour voir les détails
curl -X DELETE \
  http://localhost:8089/carthage-creance/api/enquettes/8 \
  -H "Authorization: Bearer {votre_token}" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\nTime: %{time_total}s\n" \
  -v
```

---

## Résumé

1. **Méthode** : DELETE
2. **URL** : `http://localhost:8089/carthage-creance/api/enquettes/{id}`
3. **Headers** : Authorization (Bearer token) + Content-Type
4. **Body** : Aucun
5. **Réponse Succès** : 204 No Content (body vide)
6. **Réponse Erreur** : 404/409/500 avec message dans le body

Testez avec différents IDs et vérifiez les réponses dans Postman et dans la base de données !

