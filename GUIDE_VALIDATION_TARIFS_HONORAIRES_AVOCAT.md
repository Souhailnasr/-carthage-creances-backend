# 📋 Guide : Comment Valider les Tarifs "Honoraires Avocat"

## 🎯 Vue d'Ensemble

Les tarifs "Honoraires Avocat" sont des tarifs spéciaux associés aux audiences judiciaires. Ils nécessitent une **validation manuelle** après leur création.

---

## 🔍 Étape 1 : Identifier les Tarifs "Honoraires Avocat"

### Requête SQL pour Trouver les Tarifs d'Honoraires Avocat

```sql
-- Trouver tous les tarifs "Honoraires Avocat" pour un dossier
SELECT 
    td.id AS tarif_id,
    td.dossier_id,
    td.phase,
    td.categorie,
    td.type_element,
    td.cout_unitaire,
    td.quantite,
    td.montant_total,
    td.statut,
    td.audience_id,
    td.date_creation,
    td.date_validation,
    a.id AS audience_id,
    a.date_audience,
    a.type_audience,
    av.id AS avocat_id,
    av.nom AS avocat_nom,
    av.prenom AS avocat_prenom
FROM tarif_dossier td
LEFT JOIN audience a ON td.audience_id = a.id
LEFT JOIN avocat av ON a.avocat_id = av.id
WHERE td.dossier_id = 11  -- Remplacez 11 par votre dossier_id
  AND td.categorie LIKE '%AVOCAT%'  -- Ou categorie = 'HONORAIRES_AVOCAT'
  AND td.phase = 'JURIDIQUE'
ORDER BY td.date_creation;
```

### Requête SQL pour Trouver les Tarifs EN_ATTENTE_VALIDATION

```sql
-- Trouver les tarifs d'honoraires avocat en attente de validation
SELECT 
    td.id AS tarif_id,
    td.dossier_id,
    td.categorie,
    td.cout_unitaire,
    td.montant_total,
    td.statut,
    td.audience_id,
    a.date_audience,
    av.nom AS avocat_nom
FROM tarif_dossier td
LEFT JOIN audience a ON td.audience_id = a.id
LEFT JOIN avocat av ON a.avocat_id = av.id
WHERE td.dossier_id = 11
  AND td.categorie LIKE '%AVOCAT%'
  AND td.phase = 'JURIDIQUE'
  AND td.statut = 'EN_ATTENTE_VALIDATION'
ORDER BY td.date_creation;
```

---

## 📋 Étape 2 : Comprendre la Structure des Tarifs "Honoraires Avocat"

### Caractéristiques

- **Phase** : `JURIDIQUE`
- **Catégorie** : `HONORAIRES_AVOCAT` (ou contient "AVOCAT")
- **Relation** : Associé à une `audience_id` (via l'avocat assigné à l'audience)
- **Statut initial** : `EN_ATTENTE_VALIDATION` (après création)
- **Statut après validation** : `VALIDE`

### Exemple de Tarif "Honoraires Avocat"

```
tarif_id: 72
dossier_id: 11
phase: JURIDIQUE
categorie: HONORAIRES_AVOCAT
type_element: Honoraires d'avocat
cout_unitaire: 500.00
quantite: 1
montant_total: 500.00
statut: EN_ATTENTE_VALIDATION
audience_id: 5
date_creation: 2025-01-05 20:30:00
date_validation: NULL (pas encore validé)
```

---

## ✅ Étape 3 : Processus de Validation Manuelle

### Option 1 : Via l'Interface Frontend (Recommandé)

1. **Accéder à la page de validation des tarifs**
   - URL : `http://localhost:4200/finance/validation-tarifs/{dossierId}`
   - Exemple : `http://localhost:4200/finance/validation-tarifs/11`

2. **Aller sur l'onglet "Juridique"**
   - Cliquer sur l'onglet "Juridique" pour voir les audiences et honoraires d'avocat

3. **Identifier les tarifs d'honoraires avocat**
   - Chercher les lignes avec "Honoraires Avocat" ou "HONORAIRES_AVOCAT"
   - Vérifier le statut : doit être "NON_VALIDE" ou "EN_ATTENTE_VALIDATION"

4. **Valider le tarif**
   - Cliquer sur le bouton "Valider" à côté du tarif d'honoraires avocat
   - Le système appelle automatiquement : `POST /api/finances/tarifs/{tarifId}/valider`

5. **Vérifier le résultat**
   - Le statut doit passer à "VALIDÉ" (vert)
   - La date de validation doit être affichée
   - Les totaux doivent être mis à jour

---

### Option 2 : Via l'API REST (Postman/cURL)

#### Étape 1 : Trouver le tarif_id

**Requête SQL** :
```sql
SELECT id, categorie, cout_unitaire, statut
FROM tarif_dossier
WHERE dossier_id = 11
  AND categorie LIKE '%AVOCAT%'
  AND phase = 'JURIDIQUE'
  AND statut = 'EN_ATTENTE_VALIDATION';
```

**Résultat** : Notez le `id` du tarif (ex: `tarif_id = 72`)

#### Étape 2 : Valider le tarif via l'API

**Endpoint** : `POST /api/finances/tarifs/{tarifId}/valider`

**URL complète** : `http://localhost:8089/carthage-creance/api/finances/tarifs/72/valider`

**Méthode** : `POST`

**Headers** :
```
Authorization: Bearer {votre_token_jwt}
Content-Type: application/json
```

**Body (optionnel)** :
```json
{
  "commentaire": "Honoraires avocat validés"
}
```

**Exemple avec cURL** :
```bash
curl -X POST "http://localhost:8089/carthage-creance/api/finances/tarifs/72/valider" \
  -H "Authorization: Bearer {votre_token_jwt}" \
  -H "Content-Type: application/json" \
  -d '{"commentaire": "Honoraires avocat validés"}'
```

**Réponse attendue** :
```json
{
  "id": 72,
  "dossierId": 11,
  "phase": "JURIDIQUE",
  "categorie": "HONORAIRES_AVOCAT",
  "coutUnitaire": 500.00,
  "montantTotal": 500.00,
  "statut": "VALIDE",
  "dateValidation": "2025-01-05T21:30:00",
  "audienceId": 5
}
```

---

## 🔍 Étape 4 : Vérifier la Validation

### Requête SQL pour Vérifier

```sql
-- Vérifier que le tarif est bien validé
SELECT 
    td.id AS tarif_id,
    td.categorie,
    td.cout_unitaire,
    td.montant_total,
    td.statut,
    td.date_validation,
    a.date_audience,
    av.nom AS avocat_nom
FROM tarif_dossier td
LEFT JOIN audience a ON td.audience_id = a.id
LEFT JOIN avocat av ON a.avocat_id = av.id
WHERE td.id = 72;  -- Remplacez 72 par votre tarif_id
```

**Résultat attendu** :
- `statut = 'VALIDE'` ✅
- `date_validation` n'est plus `NULL` ✅
- `date_validation` contient la date/heure de validation ✅

---

## 📊 Étape 5 : Vérifier les Totaux Mis à Jour

### Requête SQL pour Vérifier les Totaux

```sql
-- Vérifier les totaux des frais juridiques (incluant les honoraires avocat)
SELECT 
    d.id AS dossier_id,
    d.numero_dossier,
    -- Frais phase juridique (somme des tarifs validés)
    COALESCE(SUM(CASE WHEN td.phase = 'JURIDIQUE' AND td.statut = 'VALIDE' 
                      THEN td.montant_total ELSE 0 END), 0) AS frais_phase_juridique,
    -- Honoraires avocat spécifiquement
    COALESCE(SUM(CASE WHEN td.phase = 'JURIDIQUE' 
                      AND td.categorie LIKE '%AVOCAT%' 
                      AND td.statut = 'VALIDE'
                      THEN td.montant_total ELSE 0 END), 0) AS honoraires_avocat_total
FROM dossier d
LEFT JOIN tarif_dossier td ON d.id = td.dossier_id
WHERE d.id = 11
GROUP BY d.id, d.numero_dossier;
```

---

## 🔄 Processus Complet : De la Création à la Validation

### Scénario Complet

#### 1. Création du Tarif "Honoraires Avocat"

**Quand** : Le chef saisit les honoraires d'avocat dans l'interface

**Endpoint appelé** : `POST /api/finances/dossier/11/tarifs`

**Données envoyées** :
```json
{
  "phase": "JURIDIQUE",
  "categorie": "HONORAIRES_AVOCAT",
  "typeElement": "Honoraires d'avocat",
  "coutUnitaire": 500.00,
  "quantite": 1,
  "avocatId": 3,  // ID de l'avocat
  "commentaire": "Honoraires pour audience du 10/12/2025"
}
```

**Résultat** :
- ✅ Tarif créé avec `statut = EN_ATTENTE_VALIDATION`
- ✅ `audience_id` automatiquement trouvé via `avocatId`
- ✅ `date_validation = NULL` (pas encore validé)

#### 2. Validation Manuelle du Tarif

**Quand** : Le chef clique sur "Valider" dans l'interface

**Endpoint appelé** : `POST /api/finances/tarifs/72/valider`

**Résultat** :
- ✅ Statut changé de `EN_ATTENTE_VALIDATION` à `VALIDE`
- ✅ `date_validation = maintenant`
- ✅ Totaux du dossier mis à jour automatiquement

#### 3. Vérification des Totaux

**Quand** : Après validation, vérifier le récapitulatif

**Résultat attendu** :
- ✅ "Frais Phase Juridique" inclut maintenant les honoraires avocat validés
- ✅ Les totaux sont correctement calculés

---

## 📋 Checklist de Validation

### Avant la Validation

- [ ] **Identifier le tarif** : Trouver le `tarif_id` du tarif "Honoraires Avocat"
- [ ] **Vérifier le statut** : Doit être `EN_ATTENTE_VALIDATION`
- [ ] **Vérifier les données** : `cout_unitaire`, `montant_total` sont corrects
- [ ] **Vérifier la relation** : `audience_id` est bien associé

### Pendant la Validation

- [ ] **Appeler l'endpoint** : `POST /api/finances/tarifs/{tarifId}/valider`
- [ ] **Vérifier la réponse** : Statut = `VALIDE`, `dateValidation` présente
- [ ] **Vérifier les logs** : Pas d'erreur dans les logs backend

### Après la Validation

- [ ] **Vérifier dans la base** : `statut = 'VALIDE'`, `date_validation` non NULL
- [ ] **Vérifier les totaux** : "Frais Phase Juridique" mis à jour
- [ ] **Vérifier l'interface** : Statut affiché "VALIDÉ" (vert)

---

## 🔍 Identification Rapide des Tarifs à Valider

### Requête SQL : Tous les Tarifs "Honoraires Avocat" en Attente

```sql
SELECT 
    td.id AS tarif_id,
    td.dossier_id,
    d.numero_dossier,
    td.cout_unitaire,
    td.montant_total,
    td.date_creation,
    a.date_audience,
    av.nom AS avocat_nom,
    av.prenom AS avocat_prenom,
    'À VALIDER' AS action_requise
FROM tarif_dossier td
INNER JOIN dossier d ON td.dossier_id = d.id
LEFT JOIN audience a ON td.audience_id = a.id
LEFT JOIN avocat av ON a.avocat_id = av.id
WHERE td.categorie LIKE '%AVOCAT%'
  AND td.phase = 'JURIDIQUE'
  AND td.statut = 'EN_ATTENTE_VALIDATION'
ORDER BY td.dossier_id, td.date_creation;
```

**Utilité** : Liste tous les tarifs d'honoraires avocat qui attendent une validation, avec les informations nécessaires pour les valider.

---

## 🚨 Problèmes Courants et Solutions

### Problème 1 : Tarif Non Trouvé

**Symptôme** : Erreur "Tarif non trouvé avec l'ID: {tarifId}"

**Solution** :
1. Vérifier que le `tarif_id` est correct
2. Vérifier que le tarif existe dans la base de données
3. Vérifier que le tarif n'a pas été supprimé

**Requête de vérification** :
```sql
SELECT id, statut, date_creation
FROM tarif_dossier
WHERE id = 72;  -- Remplacez par votre tarif_id
```

---

### Problème 2 : Tarif Déjà Validé

**Symptôme** : Erreur "Le tarif n'est pas en attente de validation"

**Solution** :
- Le tarif a déjà été validé
- Vérifier le statut : `SELECT statut FROM tarif_dossier WHERE id = 72;`
- Si `statut = 'VALIDE'`, le tarif est déjà validé, pas besoin de re-valider

---

### Problème 3 : Tarif Rejeté

**Symptôme** : Erreur lors de la validation

**Solution** :
- Si `statut = 'REJETE'`, le tarif ne peut plus être validé
- Il faut créer un nouveau tarif si nécessaire

---

## 📝 Notes Importantes

1. **Unicité** : Un seul tarif "Honoraires Avocat" peut exister par audience (contrainte `audience_id + categorie`)

2. **Association** : Les honoraires avocat sont associés à une audience via `audience_id` (trouvé automatiquement via `avocatId`)

3. **Validation Manuelle** : La validation doit être faite manuellement via l'interface ou l'API, pas automatiquement

4. **Totaux** : Les totaux ne sont mis à jour qu'après validation (quand `statut = VALIDE`)

5. **Ordre** : Il est recommandé de valider d'abord les tarifs d'audience, puis les honoraires avocat

---

## ✅ Résumé : Étapes pour Valider un Tarif "Honoraires Avocat"

1. **Identifier le tarif** : Trouver le `tarif_id` via SQL ou l'interface
2. **Vérifier le statut** : Doit être `EN_ATTENTE_VALIDATION`
3. **Valider** : Appeler `POST /api/finances/tarifs/{tarifId}/valider`
4. **Vérifier** : Confirmer que `statut = VALIDE` et `date_validation` est définie
5. **Vérifier les totaux** : Les totaux doivent être mis à jour dans le récapitulatif

---

**Date** : 2025-01-05  
**Status** : ✅ Guide complet pour validation manuelle des tarifs "Honoraires Avocat"
