# 📊 Guide de Test Postman - Endpoints Statistiques

## 🎯 Objectif

Ce guide vous permet de tester tous les endpoints de statistiques avec Postman.

---

## 📋 Prérequis

1. **Postman installé** sur votre machine
2. **Backend démarré** sur `http://localhost:8089`
3. **Token JWT valide** d'un utilisateur connecté

---

## 🚀 Installation de la Collection

### Option 1 : Importer la Collection JSON

1. Ouvrez Postman
2. Cliquez sur **Import** (en haut à gauche)
3. Sélectionnez le fichier `COLLECTION_POSTMAN_STATISTIQUES.json`
4. La collection "Statistiques - Carthage Créance" apparaîtra dans votre workspace

### Option 2 : Créer Manuellement

Suivez les instructions ci-dessous pour créer chaque requête.

---

## 🔑 Configuration du Token JWT

### Étape 1 : Obtenir un Token JWT

1. **Connectez-vous** via l'endpoint de login :
   ```
   POST http://localhost:8089/carthage-creance/api/auth/login
   Body (JSON):
   {
     "email": "votre.email@example.com",
     "motDePasse": "votre_mot_de_passe"
   }
   ```

2. **Copiez le token** depuis la réponse (champ `token` ou `accessToken`)

### Étape 2 : Configurer le Token dans Postman

#### Méthode 1 : Variable de Collection (Recommandé)

1. Ouvrez la collection "Statistiques - Carthage Créance"
2. Cliquez sur l'onglet **Variables**
3. Modifiez la variable `jwt_token` avec votre token
4. Toutes les requêtes utiliseront automatiquement ce token

#### Méthode 2 : Header Manuel

Pour chaque requête, ajoutez un header :
- **Key:** `Authorization`
- **Value:** `Bearer VOTRE_TOKEN_ICI`

---

## 📝 Endpoints Disponibles

### 1. SuperAdmin - Statistiques Globales

#### 1.1 GET Statistiques Globales
```
GET {{base_url}}/statistiques/globales
Authorization: Bearer {{jwt_token}}
```

**Rôle requis :** `SUPER_ADMIN`

**Réponse attendue :**
```json
{
  "totalDossiers": 150,
  "dossiersEnCours": 80,
  "dossiersClotures": 70,
  "dossiersValides": 120,
  "dossiersRejetes": 30,
  "tachesCompletees": 45,
  "tachesEnCours": 20,
  "tachesEnRetard": 5,
  "montantRecouvre": 500000.00,
  "montantEnCours": 300000.00,
  "montantRecouvrePhaseAmiable": 200000.00,
  "montantRecouvrePhaseJuridique": 300000.00,
  "tauxReussiteGlobal": 46.67
}
```

---

#### 1.2 GET Statistiques par Période
```
GET {{base_url}}/statistiques/periode?dateDebut=2025-01-01&dateFin=2025-12-31
Authorization: Bearer {{jwt_token}}
```

**Rôle requis :** `SUPER_ADMIN`

**Paramètres de requête :**
- `dateDebut` (requis) : Date de début au format `YYYY-MM-DD`
- `dateFin` (requis) : Date de fin au format `YYYY-MM-DD`

**Exemple :**
```
dateDebut=2025-01-01
dateFin=2025-12-31
```

---

#### 1.3 GET Statistiques Tous les Chefs
```
GET {{base_url}}/statistiques/chefs
Authorization: Bearer {{jwt_token}}
```

**Rôle requis :** `SUPER_ADMIN`

---

#### 1.4 GET Statistiques Dossiers
```
GET {{base_url}}/statistiques/dossiers
Authorization: Bearer {{jwt_token}}
```

**Rôle requis :** `SUPER_ADMIN`

---

#### 1.5 GET Statistiques Actions Amiables
```
GET {{base_url}}/statistiques/actions-amiables
Authorization: Bearer {{jwt_token}}
```

**Rôle requis :** `SUPER_ADMIN`

---

#### 1.6 GET Statistiques Audiences
```
GET {{base_url}}/statistiques/audiences
Authorization: Bearer {{jwt_token}}
```

**Rôle requis :** `SUPER_ADMIN`

---

#### 1.7 GET Statistiques Tâches
```
GET {{base_url}}/statistiques/taches
Authorization: Bearer {{jwt_token}}
```

**Rôle requis :** `SUPER_ADMIN`

---

#### 1.8 GET Statistiques Financières
```
GET {{base_url}}/statistiques/financieres
Authorization: Bearer {{jwt_token}}
```

**Rôle requis :** `SUPER_ADMIN`

---

#### 1.9 GET Statistiques Recouvrement par Phase ✅ NOUVEAU
```
GET {{base_url}}/statistiques/recouvrement-par-phase
Authorization: Bearer {{jwt_token}}
```

**Rôle requis :** `SUPER_ADMIN`, `CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE`, `CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE`, `CHEF_DEPARTEMENT_FINANCE`

**Réponse attendue :**
```json
{
  "montantRecouvrePhaseAmiable": 200000.00,
  "montantRecouvrePhaseJuridique": 300000.00,
  "montantRecouvreTotal": 500000.00,
  "dossiersAvecRecouvrementAmiable": 25,
  "dossiersAvecRecouvrementJuridique": 15,
  "tauxRecouvrementAmiable": 45.5,
  "tauxRecouvrementJuridique": 27.3,
  "tauxRecouvrementTotal": 72.8,
  "montantTotalCreances": 1100000.00
}
```

---

#### 1.10 POST Recalculer Statistiques
```
POST {{base_url}}/statistiques/recalculer
Authorization: Bearer {{jwt_token}}
```

**Rôle requis :** `SUPER_ADMIN`

**Réponse attendue :**
```
"Statistiques recalculées avec succès"
```

---

### 2. Chefs - Statistiques Département

#### 2.1 GET Statistiques Département
```
GET {{base_url}}/statistiques/departement
Authorization: Bearer {{jwt_token}}
```

**Rôle requis :** `CHEF_DEPARTEMENT_DOSSIER`, `CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE`, `CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE`, `CHEF_DEPARTEMENT_FINANCE`

**Description :** Récupère les statistiques du département du chef connecté (l'utilisateur est extrait depuis le token JWT)

**⚠️ Important :** Assurez-vous que votre token JWT n'est pas expiré !

---

#### 2.2 GET Statistiques Mes Agents
```
GET {{base_url}}/statistiques/mes-agents
Authorization: Bearer {{jwt_token}}
```

**Rôle requis :** `CHEF_DEPARTEMENT_DOSSIER`, `CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE`, `CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE`, `CHEF_DEPARTEMENT_FINANCE`

**Description :** Récupère les statistiques des agents du chef connecté

---

#### 2.3 GET Statistiques Agent Spécifique
```
GET {{base_url}}/statistiques/agent/{agentId}
Authorization: Bearer {{jwt_token}}
```

**Rôle requis :** `CHEF_DEPARTEMENT_DOSSIER`, `CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE`, `CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE`, `CHEF_DEPARTEMENT_FINANCE`, `SUPER_ADMIN`

**Paramètres :**
- `agentId` (path) : ID de l'agent (remplacer dans l'URL)

**Exemple :**
```
GET {{base_url}}/statistiques/agent/5
```

---

#### 2.4 GET Statistiques Recouvrement par Phase - Département ✅ NOUVEAU
```
GET {{base_url}}/statistiques/recouvrement-par-phase/departement
Authorization: Bearer {{jwt_token}}
```

**Rôle requis :** `CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE`, `CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE`, `CHEF_DEPARTEMENT_FINANCE`, `SUPER_ADMIN`

**Description :** Récupère les statistiques de recouvrement par phase pour le département du chef connecté

---

### 3. Agents - Statistiques Personnelles

#### 3.1 GET Statistiques Mes Dossiers
```
GET {{base_url}}/statistiques/mes-dossiers
Authorization: Bearer {{jwt_token}}
```

**Rôle requis :** `AGENT_DOSSIER`, `AGENT_RECOUVREMENT_AMIABLE`, `AGENT_RECOUVREMENT_JURIDIQUE`, `AGENT_FINANCE`

**Description :** Récupère les statistiques des dossiers de l'agent connecté

---

### 4. Historique Recouvrement

#### 4.1 GET Historique Dossier
```
GET {{base_url}}/historique-recouvrement/dossier/{dossierId}
Authorization: Bearer {{jwt_token}}
```

**Rôle requis :** `SUPER_ADMIN`, `CHEF_DEPARTEMENT_DOSSIER`, `CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE`, `CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE`, `CHEF_DEPARTEMENT_FINANCE`

**Paramètres :**
- `dossierId` (path) : ID du dossier (remplacer dans l'URL)

**Exemple :**
```
GET {{base_url}}/historique-recouvrement/dossier/4
```

**Réponse attendue :**
```json
[
  {
    "id": 1,
    "dossierId": 4,
    "phase": "JURIDIQUE",
    "montantRecouvre": 149000.05,
    "montantTotalRecouvre": 230000.05,
    "montantRestant": 0.00,
    "typeAction": "FINALISATION_JURIDIQUE",
    "actionId": null,
    "utilisateurId": 1,
    "dateEnregistrement": "2025-12-05T10:30:00",
    "commentaire": "Finalisation juridique - RECOUVREMENT_TOTAL"
  },
  {
    "id": 2,
    "dossierId": 4,
    "phase": "AMIABLE",
    "montantRecouvre": 81000.00,
    "montantTotalRecouvre": 81000.00,
    "montantRestant": 149000.00,
    "typeAction": "ACTION_AMIABLE",
    "actionId": 5,
    "utilisateurId": 2,
    "dateEnregistrement": "2025-11-15T14:20:00",
    "commentaire": "Recouvrement suite à action amiable"
  }
]
```

---

#### 4.2 GET Historique Dossier par Phase
```
GET {{base_url}}/historique-recouvrement/dossier/{dossierId}/phase/{phase}
Authorization: Bearer {{jwt_token}}
```

**Rôle requis :** `SUPER_ADMIN`, `CHEF_DEPARTEMENT_DOSSIER`, `CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE`, `CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE`, `CHEF_DEPARTEMENT_FINANCE`

**Paramètres :**
- `dossierId` (path) : ID du dossier
- `phase` (path) : `AMIABLE` ou `JURIDIQUE`

**Exemples :**
```
GET {{base_url}}/historique-recouvrement/dossier/4/phase/AMIABLE
GET {{base_url}}/historique-recouvrement/dossier/4/phase/JURIDIQUE
```

---

#### 4.3 GET Résumé Recouvrement Dossier
```
GET {{base_url}}/historique-recouvrement/dossier/{dossierId}/resume
Authorization: Bearer {{jwt_token}}
```

**Rôle requis :** `SUPER_ADMIN`, `CHEF_DEPARTEMENT_DOSSIER`, `CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE`, `CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE`, `CHEF_DEPARTEMENT_FINANCE`

**Paramètres :**
- `dossierId` (path) : ID du dossier

**Réponse attendue :**
```json
{
  "dossierId": 4,
  "montantRecouvrePhaseAmiable": 81000.00,
  "montantRecouvrePhaseJuridique": 149000.00,
  "montantRecouvreTotal": 230000.00,
  "nombreOperationsAmiable": 3,
  "nombreOperationsJuridique": 2,
  "dernierEnregistrement": {
    "date": "2025-12-05T10:30:00",
    "montant": 149000.05,
    "phase": "JURIDIQUE",
    "typeAction": "FINALISATION_JURIDIQUE"
  }
}
```

---

## 🔍 Tests à Effectuer

### Test 1 : Vérifier l'Authentification

1. **Testez avec un token valide :**
   - Toutes les requêtes doivent retourner `200 OK` avec des données

2. **Testez avec un token expiré :**
   - Les requêtes doivent retourner `401 Unauthorized` ou `500 Internal Server Error`
   - ⚠️ **Note :** Actuellement, certaines requêtes retournent `500` au lieu de `401` (à corriger)

3. **Testez sans token :**
   - Les requêtes doivent retourner `401 Unauthorized` ou `403 Forbidden`

---

### Test 2 : Vérifier les Rôles

1. **Testez avec un token SuperAdmin :**
   - Tous les endpoints SuperAdmin doivent fonctionner
   - Les endpoints Chefs et Agents doivent aussi fonctionner

2. **Testez avec un token Chef :**
   - Les endpoints Chefs doivent fonctionner
   - Les endpoints SuperAdmin doivent retourner `403 Forbidden`

3. **Testez avec un token Agent :**
   - Seuls les endpoints Agents doivent fonctionner
   - Les autres doivent retourner `403 Forbidden`

---

### Test 3 : Vérifier les Données

1. **Vérifiez que les statistiques sont cohérentes :**
   - `montantRecouvreTotal` = `montantRecouvrePhaseAmiable` + `montantRecouvrePhaseJuridique`
   - `montantRestant` = `montantCreance` - `montantRecouvreTotal`

2. **Vérifiez les calculs :**
   - Les taux de recouvrement doivent être calculés correctement
   - Les nombres de dossiers doivent correspondre aux filtres

---

### Test 4 : Vérifier les Erreurs

1. **Testez avec des paramètres invalides :**
   - Date invalide : `dateDebut=invalid`
   - Agent ID inexistant : `agentId=99999`
   - Dossier ID inexistant : `dossierId=99999`

2. **Vérifiez les messages d'erreur :**
   - Les erreurs doivent être claires et informatives
   - Les codes HTTP doivent être appropriés (400, 401, 403, 404, 500)

---

## 📊 Exemples de Tests avec Postman

### Exemple 1 : Test Statistiques Globales (SuperAdmin)

1. **Créez une nouvelle requête** dans Postman
2. **Méthode :** `GET`
3. **URL :** `http://localhost:8089/carthage-creance/api/statistiques/globales`
4. **Headers :**
   - `Authorization: Bearer VOTRE_TOKEN_SUPERADMIN`
5. **Cliquez sur Send**
6. **Vérifiez la réponse :**
   - Status : `200 OK`
   - Body : JSON avec toutes les statistiques globales

---

### Exemple 2 : Test Statistiques Département (Chef)

1. **Créez une nouvelle requête**
2. **Méthode :** `GET`
3. **URL :** `http://localhost:8089/carthage-creance/api/statistiques/departement`
4. **Headers :**
   - `Authorization: Bearer VOTRE_TOKEN_CHEF`
5. **Cliquez sur Send**
6. **Vérifiez la réponse :**
   - Status : `200 OK`
   - Body : JSON avec les statistiques du département du chef

---

### Exemple 3 : Test Historique Recouvrement

1. **Créez une nouvelle requête**
2. **Méthode :** `GET`
3. **URL :** `http://localhost:8089/carthage-creance/api/historique-recouvrement/dossier/4`
4. **Headers :**
   - `Authorization: Bearer VOTRE_TOKEN`
5. **Cliquez sur Send**
6. **Vérifiez la réponse :**
   - Status : `200 OK`
   - Body : Array JSON avec l'historique complet du dossier 4

---

## ⚠️ Problèmes Courants

### 1. Erreur 401 Unauthorized

**Cause :** Token JWT expiré ou invalide

**Solution :**
- Obtenez un nouveau token via l'endpoint de login
- Mettez à jour la variable `jwt_token` dans Postman

---

### 2. Erreur 403 Forbidden

**Cause :** Rôle insuffisant pour accéder à l'endpoint

**Solution :**
- Utilisez un token avec le rôle approprié
- Vérifiez les rôles requis pour chaque endpoint

---

### 3. Erreur 500 Internal Server Error

**Causes possibles :**
- Token JWT expiré (gestion d'erreur insuffisante)
- Erreur dans le code backend
- Problème de connexion à la base de données

**Solution :**
- Vérifiez les logs du backend
- Vérifiez que le token n'est pas expiré
- Vérifiez la connexion à la base de données

---

### 4. Réponse Vide ou Null

**Causes possibles :**
- Aucune donnée dans la base de données
- Filtres trop restrictifs
- Problème de requête SQL

**Solution :**
- Vérifiez qu'il y a des données dans la base
- Testez avec des filtres moins restrictifs
- Vérifiez les logs Hibernate pour voir les requêtes SQL

---

## 📝 Checklist de Test

### SuperAdmin
- [ ] GET /statistiques/globales
- [ ] GET /statistiques/periode
- [ ] GET /statistiques/chefs
- [ ] GET /statistiques/dossiers
- [ ] GET /statistiques/actions-amiables
- [ ] GET /statistiques/audiences
- [ ] GET /statistiques/taches
- [ ] GET /statistiques/financieres
- [ ] GET /statistiques/recouvrement-par-phase
- [ ] POST /statistiques/recalculer

### Chefs
- [ ] GET /statistiques/departement
- [ ] GET /statistiques/mes-agents
- [ ] GET /statistiques/agent/{id}
- [ ] GET /statistiques/recouvrement-par-phase/departement

### Agents
- [ ] GET /statistiques/mes-dossiers

### Historique
- [ ] GET /historique-recouvrement/dossier/{id}
- [ ] GET /historique-recouvrement/dossier/{id}/phase/{phase}
- [ ] GET /historique-recouvrement/dossier/{id}/resume

---

## 🎯 Résultat Attendu

Après avoir testé tous les endpoints, vous devriez avoir :

1. ✅ **Tous les endpoints fonctionnent** avec les bons rôles
2. ✅ **Les données sont cohérentes** et correctement calculées
3. ✅ **Les erreurs sont gérées** correctement (401, 403, 404)
4. ✅ **Les nouveaux endpoints par phase** fonctionnent correctement
5. ✅ **L'historique des recouvrements** est accessible et complet

---

**Date de création :** 2025-12-05
**Version :** 1.0




