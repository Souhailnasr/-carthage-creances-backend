# 🔍 Guide : Vérifier les Tarifs Enregistrés pour un Dossier

## 📋 Requêtes SQL pour Dossier ID = 10

### 1. Vérifier TOUS les Tarifs du Dossier #10

**Requête complète** :
```sql
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
    td.date_creation,
    td.date_validation,
    td.commentaire,
    td.action_id,
    td.action_huissier_id,
    td.document_huissier_id,
    td.audience_id,
    td.enquete_id
FROM tarif_dossier td
WHERE td.dossier_id = 10
ORDER BY td.phase, td.date_creation;
```

**Ce que cette requête montre** :
- ✅ Tous les tarifs du dossier #10
- ✅ Leur phase (CREATION, ENQUETE, AMIABLE, JURIDIQUE)
- ✅ Leur statut (EN_ATTENTE_VALIDATION, VALIDE, REJETE)
- ✅ Les montants (coût unitaire, quantité, montant total)
- ✅ Les relations (action_id, audience_id, etc.)

---

### 2. Vérifier les Tarifs par Phase

#### Phase AMIABLE uniquement
```sql
SELECT 
    td.id AS tarif_id,
    td.phase,
    td.categorie,
    td.type_element,
    td.cout_unitaire,
    td.quantite,
    td.montant_total,
    td.statut,
    td.action_id,
    a.type AS action_type,
    a.date_action,
    td.date_creation,
    td.date_validation
FROM tarif_dossier td
LEFT JOIN action a ON td.action_id = a.id
WHERE td.dossier_id = 10 
  AND td.phase = 'AMIABLE'
ORDER BY td.date_creation;
```

#### Phase JURIDIQUE uniquement
```sql
SELECT 
    td.id AS tarif_id,
    td.phase,
    td.categorie,
    td.type_element,
    td.cout_unitaire,
    td.quantite,
    td.montant_total,
    td.statut,
    td.action_huissier_id,
    td.audience_id,
    td.document_huissier_id,
    td.date_creation,
    td.date_validation
FROM tarif_dossier td
WHERE td.dossier_id = 10 
  AND td.phase = 'JURIDIQUE'
ORDER BY td.date_creation;
```

#### Phase CREATION uniquement
```sql
SELECT 
    td.id AS tarif_id,
    td.phase,
    td.categorie,
    td.type_element,
    td.cout_unitaire,
    td.quantite,
    td.montant_total,
    td.statut,
    td.date_creation,
    td.date_validation
FROM tarif_dossier td
WHERE td.dossier_id = 10 
  AND td.phase = 'CREATION'
ORDER BY td.date_creation;
```

#### Phase ENQUETE uniquement
```sql
SELECT 
    td.id AS tarif_id,
    td.phase,
    td.categorie,
    td.type_element,
    td.cout_unitaire,
    td.quantite,
    td.montant_total,
    td.statut,
    td.enquete_id,
    td.date_creation,
    td.date_validation
FROM tarif_dossier td
WHERE td.dossier_id = 10 
  AND td.phase = 'ENQUETE'
ORDER BY td.date_creation;
```

---

### 3. Vérifier les Tarifs des Actions Amiables Spécifiquement

**Requête détaillée avec informations des actions** :
```sql
SELECT 
    td.id AS tarif_id,
    td.statut AS tarif_statut,
    td.cout_unitaire,
    td.quantite,
    td.montant_total,
    td.date_creation AS tarif_date_creation,
    td.date_validation AS tarif_date_validation,
    td.commentaire,
    a.id AS action_id,
    a.type AS action_type,
    a.date_action,
    a.cout_unitaire AS action_cout_unitaire,
    a.nb_occurrences,
    a.commentaire AS action_commentaire
FROM tarif_dossier td
INNER JOIN action a ON td.action_id = a.id
WHERE td.dossier_id = 10 
  AND td.phase = 'AMIABLE'
ORDER BY a.date_action, td.date_creation;
```

**Ce que cette requête montre** :
- ✅ Les tarifs associés aux actions amiables
- ✅ Les détails de chaque action (type, date, coût)
- ✅ Le statut de validation de chaque tarif
- ✅ Si un tarif existe pour chaque action

---

### 4. Vérifier les Actions Amiables SANS Tarif

**Requête pour identifier les actions qui n'ont PAS de tarif** :
```sql
SELECT 
    a.id AS action_id,
    a.type AS action_type,
    a.date_action,
    a.cout_unitaire AS action_cout_unitaire,
    a.nb_occurrences,
    a.commentaire,
    'PAS DE TARIF' AS statut_tarif
FROM action a
WHERE a.dossier_id = 10
  AND NOT EXISTS (
    SELECT 1 
    FROM tarif_dossier td 
    WHERE td.dossier_id = 10 
      AND td.action_id = a.id 
      AND td.phase = 'AMIABLE'
  )
ORDER BY a.date_action;
```

**Utilité** : Identifie les actions amiables qui n'ont pas encore de tarif créé, ce qui explique pourquoi elles ne peuvent pas être validées.

---

### 5. Statistiques des Tarifs par Statut

**Requête de synthèse** :
```sql
SELECT 
    td.phase,
    td.statut,
    COUNT(*) AS nombre_tarifs,
    SUM(td.montant_total) AS montant_total,
    AVG(td.cout_unitaire) AS cout_unitaire_moyen
FROM tarif_dossier td
WHERE td.dossier_id = 10
GROUP BY td.phase, td.statut
ORDER BY td.phase, td.statut;
```

**Ce que cette requête montre** :
- ✅ Nombre de tarifs par phase et par statut
- ✅ Montant total par phase et statut
- ✅ Coût unitaire moyen

---

### 6. Vérifier les Tarifs avec Détails Complets (Toutes Relations)

**Requête complète avec toutes les relations** :
```sql
SELECT 
    td.id AS tarif_id,
    td.dossier_id,
    d.numero_dossier,
    td.phase,
    td.categorie,
    td.type_element,
    td.cout_unitaire,
    td.quantite,
    td.montant_total,
    td.statut,
    td.date_creation,
    td.date_validation,
    td.commentaire,
    -- Relations
    td.action_id,
    a.type AS action_type,
    a.date_action AS action_date,
    td.action_huissier_id,
    ah.type_action AS action_huissier_type,
    td.document_huissier_id,
    dh.type_document AS document_type,
    td.audience_id,
    aud.date_audience,
    aud.type_audience,
    td.enquete_id,
    e.date_enquete
FROM tarif_dossier td
INNER JOIN dossier d ON td.dossier_id = d.id
LEFT JOIN action a ON td.action_id = a.id
LEFT JOIN action_huissier ah ON td.action_huissier_id = ah.id
LEFT JOIN document_huissier dh ON td.document_huissier_id = dh.id
LEFT JOIN audience aud ON td.audience_id = aud.id
LEFT JOIN enquette e ON td.enquete_id = e.id
WHERE td.dossier_id = 10
ORDER BY td.phase, td.date_creation;
```

---

### 7. Vérifier les Tarifs EN_ATTENTE_VALIDATION

**Requête pour les tarifs non validés** :
```sql
SELECT 
    td.id AS tarif_id,
    td.phase,
    td.categorie,
    td.type_element,
    td.cout_unitaire,
    td.montant_total,
    td.date_creation,
    td.commentaire,
    CASE 
        WHEN td.action_id IS NOT NULL THEN CONCAT('Action: ', a.type)
        WHEN td.audience_id IS NOT NULL THEN CONCAT('Audience: ', aud.type_audience)
        WHEN td.document_huissier_id IS NOT NULL THEN CONCAT('Document: ', dh.type_document)
        WHEN td.enquete_id IS NOT NULL THEN 'Enquête'
        ELSE 'Autre'
    END AS type_element_associe
FROM tarif_dossier td
LEFT JOIN action a ON td.action_id = a.id
LEFT JOIN audience aud ON td.audience_id = aud.id
LEFT JOIN document_huissier dh ON td.document_huissier_id = dh.id
WHERE td.dossier_id = 10 
  AND td.statut = 'EN_ATTENTE_VALIDATION'
ORDER BY td.date_creation;
```

**Utilité** : Identifie tous les tarifs qui attendent une validation, ce qui explique pourquoi le bouton "Enregistre" ne fonctionne peut-être pas.

---

### 8. Vérifier les Tarifs VALIDÉS

**Requête pour les tarifs validés** :
```sql
SELECT 
    td.id AS tarif_id,
    td.phase,
    td.categorie,
    td.type_element,
    td.cout_unitaire,
    td.montant_total,
    td.date_validation,
    td.commentaire
FROM tarif_dossier td
WHERE td.dossier_id = 10 
  AND td.statut = 'VALIDE'
ORDER BY td.date_validation;
```

---

### 9. Résumé Complet du Dossier #10

**Requête de synthèse globale** :
```sql
SELECT 
    'Dossier Info' AS section,
    d.id AS dossier_id,
    d.numero_dossier,
    d.type_recouvrement,
    NULL AS phase,
    NULL AS statut,
    NULL AS nombre,
    NULL AS montant_total
FROM dossier d
WHERE d.id = 10

UNION ALL

SELECT 
    'Tarifs par Phase' AS section,
    10 AS dossier_id,
    NULL AS numero_dossier,
    NULL AS type_recouvrement,
    td.phase,
    NULL AS statut,
    COUNT(*) AS nombre,
    SUM(td.montant_total) AS montant_total
FROM tarif_dossier td
WHERE td.dossier_id = 10
GROUP BY td.phase

UNION ALL

SELECT 
    'Tarifs par Statut' AS section,
    10 AS dossier_id,
    NULL AS numero_dossier,
    NULL AS type_recouvrement,
    NULL AS phase,
    td.statut,
    COUNT(*) AS nombre,
    SUM(td.montant_total) AS montant_total
FROM tarif_dossier td
WHERE td.dossier_id = 10
GROUP BY td.statut;
```

---

## 🔍 Diagnostic Rapide

### Vérification en 3 Étapes

#### Étape 1 : Vérifier si des tarifs existent
```sql
SELECT COUNT(*) AS nombre_tarifs
FROM tarif_dossier
WHERE dossier_id = 10;
```

**Résultat attendu** : Si `nombre_tarifs = 0`, aucun tarif n'existe pour ce dossier.

---

#### Étape 2 : Vérifier les tarifs de la phase AMIABLE
```sql
SELECT COUNT(*) AS nombre_tarifs_amiable
FROM tarif_dossier
WHERE dossier_id = 10 
  AND phase = 'AMIABLE';
```

**Résultat attendu** : Devrait correspondre au nombre d'actions amiables du dossier.

---

#### Étape 3 : Vérifier le statut des tarifs
```sql
SELECT 
    statut,
    COUNT(*) AS nombre
FROM tarif_dossier
WHERE dossier_id = 10
GROUP BY statut;
```

**Résultat attendu** :
- `EN_ATTENTE_VALIDATION` : Tarifs créés mais pas encore validés
- `VALIDE` : Tarifs validés
- `REJETE` : Tarifs rejetés

---

## 📊 Exemple de Résultats Attendus

### Cas Normal (Dossier avec Tarifs)

```
tarif_id | phase    | categorie              | statut                  | montant_total
---------|----------|------------------------|-------------------------|---------------
1        | CREATION | OUVERTURE_DOSSIER      | VALIDE                  | 250.00
2        | ENQUETE  | ENQUETE_PRECONTENTIEUSE| VALIDE                  | 300.00
3        | AMIABLE  | ACTION_AMIABLE         | EN_ATTENTE_VALIDATION   | 5.00
4        | AMIABLE  | ACTION_AMIABLE         | EN_ATTENTE_VALIDATION   | 10.00
5        | JURIDIQUE| AUDIENCE               | VALIDE                  | 500.00
```

### Cas Problématique (Pas de Tarifs pour Actions Amiables)

```
tarif_id | phase    | categorie              | statut | montant_total
---------|----------|------------------------|--------|---------------
1        | CREATION | OUVERTURE_DOSSIER      | VALIDE | 250.00
2        | ENQUETE  | ENQUETE_PRECONTENTIEUSE| VALIDE | 300.00
```

**Problème** : Aucun tarif pour la phase AMIABLE, donc les actions amiables ne peuvent pas être validées.

---

## ✅ Checklist de Vérification

- [ ] **Vérifier l'existence de tarifs** : Au moins un tarif doit exister pour le dossier #10
- [ ] **Vérifier les tarifs AMIABLE** : Au moins un tarif avec `phase = 'AMIABLE'` doit exister
- [ ] **Vérifier les relations** : Les `action_id` doivent correspondre aux actions du dossier
- [ ] **Vérifier les statuts** : Les tarifs doivent avoir le statut `EN_ATTENTE_VALIDATION` pour pouvoir être validés
- [ ] **Vérifier les montants** : Les `montant_total` doivent être calculés correctement (`cout_unitaire × quantite`)

---

## 🚨 Problèmes Courants et Solutions

### Problème 1 : Aucun Tarif pour les Actions Amiables

**Symptôme** : `nombre_tarifs_amiable = 0`

**Cause** : Les tarifs n'ont pas été créés pour les actions amiables.

**Solution** : Créer les tarifs via l'endpoint `POST /api/finances/dossier/10/tarifs` avant de pouvoir les valider.

---

### Problème 2 : Tarifs avec Statut Incorrect

**Symptôme** : Tous les tarifs ont le statut `VALIDE` ou `REJETE`

**Cause** : Les tarifs ont déjà été validés ou rejetés.

**Solution** : Créer de nouveaux tarifs avec le statut `EN_ATTENTE_VALIDATION` pour pouvoir les valider.

---

### Problème 3 : `action_id` NULL ou Incorrect

**Symptôme** : Les tarifs existent mais `action_id` est `NULL` ou ne correspond à aucune action

**Cause** : Les tarifs n'ont pas été correctement associés aux actions.

**Solution** : Vérifier que lors de la création du tarif, l'`action_id` est bien fourni et correspond à une action existante.

---

## 📝 Notes Importantes

1. **Ordre des Opérations** :
   - D'abord : Créer les actions amiables
   - Ensuite : Créer les tarifs pour ces actions (`POST /api/finances/dossier/{dossierId}/tarifs`)
   - Enfin : Valider les tarifs (`POST /api/finances/tarifs/{tarifId}/valider`)

2. **Statuts des Tarifs** :
   - `EN_ATTENTE_VALIDATION` : Peut être validé ou rejeté
   - `VALIDE` : Déjà validé, ne peut plus être modifié
   - `REJETE` : Rejeté, ne peut plus être validé

3. **Calcul du Montant Total** :
   - `montant_total = cout_unitaire × quantite`
   - Calculé automatiquement par Hibernate via `@PrePersist` et `@PreUpdate`

---

**Date** : 2025-01-05  
**Status** : ✅ Guide complet pour vérifier les tarifs dans la base de données
