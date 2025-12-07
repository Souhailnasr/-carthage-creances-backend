# ✅ Correction : Validation Manuelle des Tarifs d'Actions Amiables

## 🎯 Modification Appliquée

**Changement** : Suppression de la validation automatique. Les tarifs d'actions amiables sont maintenant créés/mis à jour avec le statut `EN_ATTENTE_VALIDATION` et nécessitent une **validation manuelle** depuis l'interface.

---

## 📋 Comportement Modifié

### Avant (Validation Automatique)

- ✅ Création/mise à jour → Statut = `VALIDE` automatiquement
- ✅ Date de validation = maintenant automatiquement
- ❌ Pas de contrôle manuel

### Après (Validation Manuelle)

- ✅ Création/mise à jour → Statut = `EN_ATTENTE_VALIDATION`
- ✅ Date de validation = `null` (pas encore validé)
- ✅ Validation manuelle requise via l'interface (bouton "Valider" séparé)

---

## 🔄 Nouveau Flux

### Scénario 1 : Création d'un Nouveau Tarif

```
1. Utilisateur clique "Enregistrer" avec coutUnitaire = 5.00
2. Backend vérifie : aucun tarif pour action_id = X
3. Backend crée : nouveau tarif avec coutUnitaire = 5.00
4. Backend définit : statut = EN_ATTENTE_VALIDATION (pas VALIDE)
5. Backend retourne : tarif créé avec statut EN_ATTENTE_VALIDATION
6. Frontend affiche : "NON_VALIDE" (statut orange)
7. Utilisateur clique "Valider" (bouton séparé) → Validation manuelle
8. Backend valide : statut = VALIDE, dateValidation = maintenant
9. Frontend affiche : "VALIDÉ" (statut vert)
10. Totaux mis à jour : "Frais Phase Amiable" = 10.00 TND
```

### Scénario 2 : Mise à Jour d'un Tarif Existant

```
1. Utilisateur clique "Enregistrer" avec coutUnitaire = 7.00
2. Backend vérifie : tarif existe pour action_id = X avec coutUnitaire = 5.00
3. Backend compare : 7.00 ≠ 5.00 (différent)
4. Backend met à jour : 
   - coutUnitaire = 7.00
   - montantTotal = 7.00 × quantite
   - statut = EN_ATTENTE_VALIDATION (garde ou remet en attente)
   - dateValidation = null (si pas encore validé)
5. Backend retourne : tarif mis à jour avec statut EN_ATTENTE_VALIDATION
6. Frontend affiche : "NON_VALIDE" (statut orange)
7. Utilisateur clique "Valider" (bouton séparé) → Validation manuelle
8. Backend valide : statut = VALIDE, dateValidation = maintenant
9. Frontend affiche : "VALIDÉ" (statut vert)
```

### Scénario 3 : Tarif Déjà Validé

```
1. Utilisateur clique "Enregistrer" avec coutUnitaire = 5.00
2. Backend vérifie : tarif existe pour action_id = X avec statut = VALIDE
3. Backend met à jour : coutUnitaire si différent
4. Backend garde : statut = VALIDE (ne change pas si déjà validé)
5. Backend retourne : tarif mis à jour, toujours VALIDE
6. Frontend affiche : "VALIDÉ" (statut vert, pas de changement)
```

---

## 📋 Code Modifié

### Fichier : `TarifDossierServiceImpl.java`

#### Modification 1 : Tarif Existant

**Avant** :
```java
// ✅ VALIDER LE TARIF (même s'il était déjà validé, on le re-valide)
tarifExistant.setStatut(StatutTarif.VALIDE);
tarifExistant.setDateValidation(LocalDateTime.now());
```

**Après** :
```java
// ✅ NE PAS VALIDER AUTOMATIQUEMENT : Garder le statut actuel ou EN_ATTENTE_VALIDATION
// Si le tarif était déjà validé, on le garde validé
// Si le tarif était en attente, on le garde en attente (validation manuelle requise)
if (tarifExistant.getStatut() == null || tarifExistant.getStatut() == StatutTarif.EN_ATTENTE_VALIDATION) {
    tarifExistant.setStatut(StatutTarif.EN_ATTENTE_VALIDATION);
    tarifExistant.setDateValidation(null); // Pas de date de validation si pas encore validé
}
// Si déjà VALIDE, on garde VALIDE (pas de changement)
```

#### Modification 2 : Nouveau Tarif

**Avant** :
```java
// ✅ TARIF N'EXISTE PAS : Créer et valider automatiquement
tarif.setAction(action);
tarif.setStatut(StatutTarif.VALIDE); // ✅ Valider automatiquement
tarif.setDateValidation(LocalDateTime.now()); // ✅ Date de validation
```

**Après** :
```java
// ✅ TARIF N'EXISTE PAS : Créer avec statut EN_ATTENTE_VALIDATION (validation manuelle requise)
tarif.setAction(action);
tarif.setStatut(StatutTarif.EN_ATTENTE_VALIDATION); // ✅ Validation manuelle requise
tarif.setDateValidation(null); // Pas de date de validation si pas encore validé
```

---

## 🔄 Flux Complet avec Validation Manuelle

### Étape 1 : Enregistrement (Création/Mise à Jour)

**Endpoint** : `POST /api/finances/dossier/{dossierId}/tarifs`

**Action** : Crée ou met à jour le tarif avec statut `EN_ATTENTE_VALIDATION`

**Réponse** :
```json
{
  "id": 68,
  "dossierId": 11,
  "actionId": 5,
  "phase": "AMIABLE",
  "coutUnitaire": 5.00,
  "montantTotal": 10.00,
  "statut": "EN_ATTENTE_VALIDATION",  // ✅ Pas encore validé
  "dateValidation": null              // ✅ Pas de date de validation
}
```

### Étape 2 : Validation Manuelle

**Endpoint** : `POST /api/finances/tarifs/{tarifId}/valider`

**Action** : Valide manuellement le tarif (statut = `VALIDE`)

**Réponse** :
```json
{
  "id": 68,
  "dossierId": 11,
  "actionId": 5,
  "phase": "AMIABLE",
  "coutUnitaire": 5.00,
  "montantTotal": 10.00,
  "statut": "VALIDE",                 // ✅ Maintenant validé
  "dateValidation": "2025-01-05T21:30:00"  // ✅ Date de validation
}
```

---

## 📊 États des Tarifs

| État | Statut | Date Validation | Bouton Frontend | Action |
|------|--------|-----------------|-----------------|--------|
| **Créé/Mis à jour** | `EN_ATTENTE_VALIDATION` | `null` | "Enregistrer" → Crée/Met à jour | "Valider" → Valide |
| **Validé** | `VALIDE` | Date présente | "VALIDÉ" (affiché) | Pas d'action nécessaire |
| **Rejeté** | `REJETE` | `null` | "REJETÉ" (affiché) | Pas d'action possible |

---

## ✅ Checklist de Vérification

- [x] **Code modifié** : `createTarif()` ne valide plus automatiquement
- [x] **Statut initial** : Les tarifs sont créés avec `EN_ATTENTE_VALIDATION`
- [x] **Mise à jour** : Les tarifs existants gardent leur statut (ou passent à `EN_ATTENTE_VALIDATION` si modifié)
- [x] **Validation manuelle** : L'endpoint `POST /api/finances/tarifs/{tarifId}/valider` existe déjà pour la validation manuelle
- [ ] **Frontend** : Vérifier que le bouton "Valider" est présent et fonctionne dans l'interface

---

## 🔍 Endpoints Disponibles

### 1. Créer/Mettre à Jour un Tarif (Sans Validation)

**Endpoint** : `POST /api/finances/dossier/{dossierId}/tarifs`

**Comportement** :
- Crée un nouveau tarif si n'existe pas → `statut = EN_ATTENTE_VALIDATION`
- Met à jour le tarif existant si existe → `statut = EN_ATTENTE_VALIDATION` (ou garde `VALIDE` si déjà validé)
- **Ne valide PAS automatiquement**

### 2. Valider un Tarif (Validation Manuelle)

**Endpoint** : `POST /api/finances/tarifs/{tarifId}/valider`

**Comportement** :
- Change le statut de `EN_ATTENTE_VALIDATION` à `VALIDE`
- Définit `dateValidation = maintenant`
- Met à jour les totaux du dossier

### 3. Rejeter un Tarif

**Endpoint** : `POST /api/finances/tarifs/{tarifId}/rejeter`

**Comportement** :
- Change le statut à `REJETE`
- Ajoute un commentaire de rejet

---

## 📝 Notes Importantes

1. **Bouton "Enregistrer"** : Crée ou met à jour le tarif, mais ne le valide PAS
2. **Bouton "Valider"** : Doit être un bouton séparé dans l'interface qui appelle `POST /api/finances/tarifs/{tarifId}/valider`
3. **Statut après "Enregistrer"** : Toujours `EN_ATTENTE_VALIDATION` (sauf si le tarif était déjà `VALIDE`)
4. **Totaux** : Ne sont mis à jour qu'après validation manuelle (quand statut = `VALIDE`)

---

## 🧪 Tests à Effectuer

### Test 1 : Création et Validation Manuelle

1. **Action 1** : Cliquer sur "Enregistrer" pour une action amiable sans tarif
2. **Résultat attendu** :
   - ✅ Tarif créé avec `statut = EN_ATTENTE_VALIDATION`
   - ✅ Interface affiche "NON_VALIDE" (orange)
   - ✅ Totaux restent à 0.00 TND (pas encore validé)

3. **Action 2** : Cliquer sur "Valider" (bouton séparé)
4. **Résultat attendu** :
   - ✅ Tarif validé avec `statut = VALIDE`
   - ✅ Interface affiche "VALIDÉ" (vert)
   - ✅ Totaux mis à jour : "Frais Phase Amiable" = montant du tarif

### Test 2 : Mise à Jour et Validation Manuelle

1. **Action 1** : Cliquer sur "Enregistrer" pour une action amiable avec tarif existant (coût différent)
2. **Résultat attendu** :
   - ✅ Tarif mis à jour avec nouveau coût
   - ✅ Statut = `EN_ATTENTE_VALIDATION` (ou reste `VALIDE` si déjà validé)
   - ✅ Interface affiche "NON_VALIDE" ou "VALIDÉ" selon le statut

3. **Action 2** : Si statut = `EN_ATTENTE_VALIDATION`, cliquer sur "Valider"
4. **Résultat attendu** :
   - ✅ Tarif validé
   - ✅ Totaux mis à jour avec le nouveau montant

---

**Date** : 2025-01-05  
**Status** : ✅ Correction appliquée - Validation manuelle activée
