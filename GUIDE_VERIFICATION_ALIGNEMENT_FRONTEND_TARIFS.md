# 📋 Guide de Vérification : Alignement Frontend-Backend - Gestion des Tarifs

## 🎯 Objectif

Vérifier l'alignement entre le frontend et le backend après les corrections appliquées pour :
1. La contrainte d'unicité (audienceId + categorie)
2. Le support de `avocatId` pour les honoraires d'avocat
3. L'endpoint `/api/finances/dossier/{dossierId}/traitements`

---

## ✅ Corrections Appliquées Côté Backend

### 1. Contrainte d'Unicité (audienceId + categorie)

**Fichier modifié :** `TarifDossier.java`
- ✅ Ajout de la contrainte d'unicité `@UniqueConstraint(columnNames = {"audience_id", "categorie"})`

**Fichier modifié :** `TarifDossierRepository.java`
- ✅ Ajout de la méthode `findByDossierIdAndAudienceIdAndCategorie()`

**Fichier modifié :** `TarifDossierServiceImpl.java`
- ✅ Vérification de l'unicité avant création dans `createTarif()`
- ✅ Exception levée si un tarif existe déjà pour (audienceId, categorie)

**Migration SQL :** `V1_3__Add_Unique_Constraint_TarifDossier.sql`
- ✅ Contrainte UNIQUE ajoutée en base de données

### 2. Support de avocatId

**Fichier modifié :** `TarifDossierRequest.java`
- ✅ Ajout du champ `avocatId`

**Fichier modifié :** `TarifDossierServiceImpl.java`
- ✅ Gestion de `avocatId` dans `createTarif()`
- ✅ Mapping automatique : `avocatId` → `audienceId` (audience la plus récente)
- ✅ Priorité : `audienceId` > `avocatId` (si les deux sont fournis)

### 3. Endpoint `/api/finances/dossier/{dossierId}/traitements`

**Status :** ✅ Existe et fonctionne (aucune modification nécessaire)

---

## 📋 Points de Vérification Frontend

### 1. Création de Tarif avec audienceId

**Endpoint :** `POST /api/finances/dossier/{dossierId}/tarifs`

**Requête attendue :**
```json
{
  "phase": "JURIDIQUE",
  "categorie": "AUDIENCE",
  "typeElement": "Audience au tribunal",
  "coutUnitaire": 150.00,
  "quantite": 1,
  "audienceId": 123,
  "commentaire": "Tarif pour l'audience"
}
```

**Comportement attendu :**
- ✅ Si un tarif existe déjà pour cette audience avec la même catégorie → **Erreur 400** avec message : `"Un tarif existe déjà pour cette audience (123) avec la catégorie (AUDIENCE)"`
- ✅ Si aucun tarif n'existe → **Création réussie** (201)

**Vérifications frontend :**
- [ ] Gérer l'erreur 400 si un tarif existe déjà
- [ ] Afficher un message d'erreur clair à l'utilisateur
- [ ] Empêcher la création de doublons côté frontend (vérifier avant d'envoyer)

---

### 2. Création de Tarif avec avocatId (Honoraires d'Avocat)

**Endpoint :** `POST /api/finances/dossier/{dossierId}/tarifs`

**Requête attendue :**
```json
{
  "phase": "JURIDIQUE",
  "categorie": "HONORAIRES_AVOCAT",
  "typeElement": "Honoraires d'avocat",
  "coutUnitaire": 500.00,
  "quantite": 1,
  "avocatId": 45,
  "commentaire": "Honoraires pour l'avocat"
}
```

**Comportement attendu :**
- ✅ Le backend trouve automatiquement l'audience associée à cet avocat pour ce dossier
- ✅ Utilise l'audience la plus récente si plusieurs audiences existent
- ✅ Si aucune audience trouvée → **Erreur 400** : `"Aucune audience trouvée pour l'avocat 45 dans le dossier {dossierId}"`
- ✅ Si un tarif existe déjà pour cette audience avec la catégorie "HONORAIRES_AVOCAT" → **Erreur 400**

**Vérifications frontend :**
- [ ] Permettre l'envoi de `avocatId` au lieu de `audienceId` pour les honoraires d'avocat
- [ ] Gérer l'erreur si aucune audience n'est trouvée
- [ ] Gérer l'erreur si un tarif existe déjà
- [ ] Les catégories acceptées pour `avocatId` : `"AVOCAT"`, `"HONORAIRES_AVOCAT"`, etc. (contient "AVOCAT" en majuscules)

---

### 3. Priorité audienceId vs avocatId

**Comportement attendu :**
- ✅ Si **les deux** `audienceId` et `avocatId` sont fournis → **`audienceId` est prioritaire**
- ✅ Le backend ignore `avocatId` si `audienceId` est fourni

**Vérifications frontend :**
- [ ] Ne pas envoyer les deux en même temps (ou documenter que `audienceId` sera prioritaire)
- [ ] Si l'utilisateur sélectionne une audience spécifique, utiliser `audienceId`
- [ ] Si l'utilisateur sélectionne un avocat, utiliser `avocatId` (le backend trouvera l'audience)

---

### 4. Récupération des Traitements

**Endpoint :** `GET /api/finances/dossier/{dossierId}/traitements`

**Réponse attendue :**
```json
{
  "phaseCreation": { ... },
  "phaseEnquete": { ... },
  "phaseAmiable": {
    "actions": [
      {
        "id": 1,
        "type": "APPEL",
        "date": "2025-01-05",
        "coutAction": 5.00,
        "tarifAction": { ... }
      }
    ]
  },
  "phaseJuridique": {
    "audiences": [
      {
        "id": 123,
        "date": "2025-01-10",
        "type": "TRIBUNAL_COMMERCIAL",
        "avocatId": 45,
        "avocatNom": "Dupont",
        "coutAudience": 150.00,
        "tarifAudience": { ... },
        "coutAvocat": 500.00,
        "tarifAvocat": { ... },
        "statut": "VALIDE"
      }
    ],
    "documentsHuissier": [ ... ],
    "actionsHuissier": [ ... ]
  }
}
```

**Vérifications frontend :**
- [ ] L'endpoint retourne bien les traitements organisés par phase
- [ ] Pour chaque audience, `avocatId` et `avocatNom` sont présents si un avocat est assigné
- [ ] Les tarifs d'audience et d'avocat sont bien séparés (`tarifAudience` vs `tarifAvocat`)
- [ ] Le statut de chaque traitement est affiché correctement

---

## 🔍 Tests à Effectuer

### Test 1 : Création Tarif avec audienceId - Cas Normal

**Action :**
1. Créer un tarif pour une audience avec catégorie "AUDIENCE"
2. Vérifier que la création réussit

**Résultat attendu :** ✅ 201 Created

---

### Test 2 : Création Tarif avec audienceId - Doublon

**Action :**
1. Créer un tarif pour audienceId=123 avec categorie="AUDIENCE"
2. Essayer de créer un autre tarif pour la même audienceId=123 avec la même categorie="AUDIENCE"

**Résultat attendu :** ❌ 400 Bad Request avec message d'erreur

---

### Test 3 : Création Tarif avec avocatId - Cas Normal

**Action :**
1. S'assurer qu'une audience existe pour l'avocat dans le dossier
2. Créer un tarif avec avocatId=45 et categorie="HONORAIRES_AVOCAT"

**Résultat attendu :** ✅ 201 Created, le tarif est lié à l'audience la plus récente

---

### Test 4 : Création Tarif avec avocatId - Aucune Audience

**Action :**
1. Créer un tarif avec avocatId=999 (avocat sans audience dans ce dossier) et categorie="HONORAIRES_AVOCAT"

**Résultat attendu :** ❌ 400 Bad Request : "Aucune audience trouvée pour l'avocat 999..."

---

### Test 5 : Création Tarif avec avocatId - Doublon

**Action :**
1. Créer un tarif avec avocatId=45 et categorie="HONORAIRES_AVOCAT"
2. Essayer de créer un autre tarif avec le même avocatId et la même catégorie

**Résultat attendu :** ❌ 400 Bad Request (car le tarif est lié à la même audience)

---

### Test 6 : Priorité audienceId vs avocatId

**Action :**
1. Créer un tarif avec audienceId=123 ET avocatId=45
2. Vérifier que le tarif est bien lié à audienceId=123 (pas à l'audience de l'avocat)

**Résultat attendu :** ✅ Le tarif est lié à audienceId=123

---

### Test 7 : Récupération des Traitements

**Action :**
1. Appeler `GET /api/finances/dossier/{dossierId}/traitements`
2. Vérifier la structure de la réponse

**Résultat attendu :** ✅ Réponse complète avec toutes les phases et traitements

---

## ⚠️ Incohérences Potentielles à Vérifier

### 1. Gestion des Erreurs

**Problème potentiel :** Le frontend ne gère pas les erreurs 400 pour les doublons

**Solution :**
- Vérifier que le frontend affiche un message d'erreur clair
- Suggérer à l'utilisateur de modifier le tarif existant au lieu d'en créer un nouveau

---

### 2. Catégories pour avocatId

**Problème potentiel :** Le frontend envoie `avocatId` avec une catégorie qui ne contient pas "AVOCAT"

**Solution :**
- Le backend vérifie que `categorie.toUpperCase().contains("AVOCAT")`
- Le frontend doit utiliser des catégories comme : `"AVOCAT"`, `"HONORAIRES_AVOCAT"`, `"FRAIS_AVOCAT"`, etc.

---

### 3. Validation Côté Frontend

**Problème potentiel :** Le frontend permet de créer des doublons sans vérification

**Solution :**
- Avant de créer un tarif, vérifier s'il existe déjà :
  - Appeler `GET /api/finances/dossier/{dossierId}/tarifs` pour lister les tarifs existants
  - Vérifier si un tarif existe pour (audienceId, categorie) ou (avocatId, categorie)
  - Afficher un avertissement si un tarif existe déjà

---

### 4. Mapping avocatId → audienceId

**Problème potentiel :** Le frontend ne comprend pas que `avocatId` est mappé vers `audienceId`

**Solution :**
- Documenter que `avocatId` est automatiquement converti en `audienceId` côté backend
- Après création, le tarif retourné contient `audienceId` (pas `avocatId`)
- Le frontend doit utiliser `audienceId` pour les opérations suivantes (modification, suppression)

---

## 📝 Checklist de Vérification Frontend

### Création de Tarifs

- [ ] Le formulaire permet de sélectionner `audienceId` OU `avocatId`
- [ ] Si `avocatId` est sélectionné, la catégorie doit contenir "AVOCAT"
- [ ] Le frontend vérifie les doublons avant d'envoyer la requête
- [ ] Les erreurs 400 sont gérées et affichées clairement
- [ ] Le message d'erreur pour les doublons suggère de modifier le tarif existant

### Récupération des Traitements

- [ ] L'endpoint `/api/finances/dossier/{dossierId}/traitements` est appelé correctement
- [ ] La structure de la réponse est bien parsée
- [ ] Les audiences affichent `avocatId` et `avocatNom` si présents
- [ ] Les tarifs d'audience et d'avocat sont affichés séparément
- [ ] Le statut de chaque traitement est affiché

### Gestion des Erreurs

- [ ] Erreur "Un tarif existe déjà..." → Afficher message + suggérer modification
- [ ] Erreur "Aucune audience trouvée..." → Afficher message + suggérer de créer une audience d'abord
- [ ] Erreur 404 (dossier non trouvé) → Gérer correctement
- [ ] Erreur 500 → Afficher message générique

---

## 🔧 Corrections Frontend Si Nécessaires

### Si le frontend envoie audienceId ET avocatId

**Correction :**
- Prioriser `audienceId` si les deux sont fournis
- Ou empêcher l'envoi des deux en même temps

### Si le frontend ne gère pas les doublons

**Correction :**
- Ajouter une vérification avant création
- Appeler `GET /api/finances/dossier/{dossierId}/tarifs` pour vérifier les tarifs existants
- Filtrer par `audienceId` + `categorie` ou `avocatId` + `categorie`

### Si le frontend ne comprend pas le mapping avocatId

**Correction :**
- Documenter que `avocatId` est converti en `audienceId` côté backend
- Après création, utiliser `audienceId` du tarif retourné pour les opérations suivantes

---

## 📊 Exemples de Requêtes

### Exemple 1 : Créer un tarif d'audience

```http
POST /api/finances/dossier/1/tarifs
Content-Type: application/json

{
  "phase": "JURIDIQUE",
  "categorie": "AUDIENCE",
  "typeElement": "Audience au tribunal",
  "coutUnitaire": 150.00,
  "quantite": 1,
  "audienceId": 123,
  "commentaire": "Tarif pour l'audience du 10 janvier"
}
```

### Exemple 2 : Créer un tarif d'honoraires d'avocat

```http
POST /api/finances/dossier/1/tarifs
Content-Type: application/json

{
  "phase": "JURIDIQUE",
  "categorie": "HONORAIRES_AVOCAT",
  "typeElement": "Honoraires d'avocat",
  "coutUnitaire": 500.00,
  "quantite": 1,
  "avocatId": 45,
  "commentaire": "Honoraires pour l'avocat Dupont"
}
```

### Exemple 3 : Récupérer les traitements

```http
GET /api/finances/dossier/1/traitements
```

---

## ✅ Résumé des Changements Backend

1. ✅ **Contrainte d'unicité** : `(audience_id, categorie)` - Empêche les doublons
2. ✅ **Support avocatId** : Mapping automatique vers `audienceId`
3. ✅ **Vérification d'unicité** : Avant création, vérifie si un tarif existe déjà
4. ✅ **Gestion des erreurs** : Messages d'erreur clairs pour les cas d'échec

---

**Date de création :** 2025-01-05  
**Status :** ✅ Corrections backend appliquées - Prêt pour vérification frontend


