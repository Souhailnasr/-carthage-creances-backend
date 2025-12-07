# 📋 Résumé : Intégration de l'Annexe dans le Système de Tarifs

## 🎯 Objectif

Intégrer tous les éléments de l'annexe du contrat dans le calcul des tarifs et la validation :
- **Prix fixes** : 250 TND, 300 TND, 1000 TND, 500 TND
- **Avances** : 1000 TND (recouvrement judiciaire)
- **Commissions** : 5%, 12%, 15%, 50%
- **Tarifs variables** : Audience et honoraires d'avocat (saisis par le chef)
- **Calcul après validation** : Les commissions sont calculées après la validation des frais

---

## 📊 Éléments de l'Annexe

### 1. Prix Fixes (Selon Capture 1)

| Service | Montant | Type | Création |
|---------|---------|------|----------|
| Frais fixes de réception et d'ouverture de dossier | **250 TND** | Prix fixe | Automatique lors validation dossier |
| Frais Enquête Précontentieuse | **300 TND** | Prix fixe | Automatique lors validation enquête |
| Avance sur frais de recouvrement judiciaire | **1000 TND** | Avance | Automatique lors passage phase JURIDIQUE |
| Attestation de carence à la demande du mandant | **500 TND** | Prix fixe | Manuel (à la demande) |

### 2. Commissions (Selon Capture 2)

| Phase de Recouvrement | Taux | Base de Calcul |
|----------------------|------|----------------|
| Relance Factures datées de moins de 6 mois | **5%** | montantRecouvreRelance |
| Recouvrement Amiable | **12%** | montantRecouvrePhaseAmiable |
| Recouvrement Judiciaire | **15%** | montantRecouvrePhaseJuridique |
| Commission sur les intérêts | **50%** | montantInteretsRecouvres |

### 3. Tarifs Variables (Saisis par le Chef)

| Type | Saisie | Validation |
|------|--------|------------|
| Tarif Audience | Chef saisit dans page validation | Chef valide |
| Honoraires Avocat | Chef saisit dans page validation | Chef valide |

---

## 🔍 État Actuel

### Constantes Existantes

**Fichier :** `TarifDossierServiceImpl.java`

- ✅ `FRAIS_CREATION_DOSSIER = 250.00` - Existe
- ✅ `FRAIS_ENQUETE_PRECONTENTIEUSE = 300.00` - Existe

**Constantes Manquantes :**
- ❌ `AVANCE_RECOUVREMENT_JURIDIQUE = 1000.00`
- ❌ `ATTESTATION_CARENCE = 500.00`
- ❌ `TAUX_COMMISSION_RELANCE = 0.05`
- ❌ `TAUX_COMMISSION_AMIABLE = 0.12`
- ❌ `TAUX_COMMISSION_JURIDIQUE = 0.15`
- ❌ `TAUX_COMMISSION_INTERETS = 0.50`

### Champs dans Dossier

**Champs existants :**
- ✅ `montantRecouvrePhaseAmiable` (Double)
- ✅ `montantRecouvrePhaseJuridique` (Double)

**Champs à vérifier :**
- ❓ `montantInteretsRecouvres` (Double) - À vérifier/ajouter si manquant

### Commissions Actuelles

**Fichier :** `TarifDossierServiceImpl.java` (ligne 673-676)

```java
// Commissions (selon annexe - à calculer selon les règles métier)
// Pour l'instant, on met 0, à implémenter selon les règles de l'annexe
dto.setCommissionAmiable(BigDecimal.ZERO);
dto.setCommissionJuridique(BigDecimal.ZERO);
```

**❌ Problème :** Les commissions ne sont pas calculées, elles sont mises à 0.

---

## ✅ Modifications Requises

### 1. Ajouter les Constantes Manquantes

**Fichier :** `TarifDossierServiceImpl.java`

**Constantes à ajouter :**

```java
// Prix fixes selon annexe
private static final BigDecimal AVANCE_RECOUVREMENT_JURIDIQUE = new BigDecimal("1000.00");
private static final BigDecimal ATTESTATION_CARENCE = new BigDecimal("500.00");

// Taux de commission selon annexe
private static final BigDecimal TAUX_COMMISSION_RELANCE = new BigDecimal("0.05");  // 5%
private static final BigDecimal TAUX_COMMISSION_AMIABLE = new BigDecimal("0.12");  // 12%
private static final BigDecimal TAUX_COMMISSION_JURIDIQUE = new BigDecimal("0.15");  // 15%
private static final BigDecimal TAUX_COMMISSION_INTERETS = new BigDecimal("0.50");  // 50%
```

### 2. Créer les Méthodes pour les Tarifs Automatiques

#### 2.1. Tarif d'Enquête (300 TND)

**Méthode :** `createTarifEnqueteAutomatique(Enquette enquete)`

**Logique :**
- Vérifier si un tarif existe déjà pour `(dossierId, phase=ENQUETE, categorie=ENQUETE_PRECONTENTIEUSE, enqueteId)`
- Si aucun tarif n'existe → Créer avec `coutUnitaire = 300.00`, `statut = VALIDE`

**Appel :** Dans `EnquetteServiceImpl.validerEnquette()` après validation

#### 2.2. Avance Judiciaire (1000 TND)

**Méthode :** `createAvanceRecouvrementJuridique(Dossier dossier)`

**Logique :**
- Vérifier si un tarif existe déjà pour `(dossierId, phase=JURIDIQUE, categorie=AVANCE_RECOUVREMENT_JURIDIQUE)`
- Si aucun tarif n'existe → Créer avec `coutUnitaire = 1000.00`, `statut = VALIDE`

**Appel :** Dans `DossierServiceImpl` quand `typeRecouvrement` passe à `JURIDIQUE`

#### 2.3. Attestation de Carence (500 TND)

**Méthode :** `createTarifAttestationCarence(Long dossierId, String commentaire)`

**Logique :**
- Vérifier si un tarif existe déjà pour `(dossierId, phase=JURIDIQUE, categorie=ATTESTATION_CARENCE)`
- Si aucun tarif n'existe → Créer avec `coutUnitaire = 500.00`, `statut = EN_ATTENTE_VALIDATION`

**Appel :** Via un endpoint dédié ou depuis la page de validation

### 3. Implémenter le Calcul des Commissions

#### 3.1. Méthodes de Calcul

**Méthode 1 :** `calculerCommissionAmiable(Dossier dossier)`
- Retourne : `montantRecouvrePhaseAmiable × 12%`

**Méthode 2 :** `calculerCommissionJuridique(Dossier dossier)`
- Retourne : `montantRecouvrePhaseJuridique × 15%`

**Méthode 3 :** `calculerCommissionInterets(Dossier dossier)`
- Retourne : `montantInteretsRecouvres × 50%` (si applicable)

**Méthode 4 :** `calculerCommissions(Dossier dossier)`
- Retourne un DTO avec toutes les commissions

#### 3.2. Intégration dans `calculerDetailFacture()`

**Modifications :**
1. Récupérer le dossier pour accéder aux montants recouvrés
2. Calculer les commissions via `calculerCommissions()`
3. Ajouter les commissions au total HT
4. Mettre à jour le DTO avec les commissions

### 4. Modifier les Méthodes de Validation

#### 4.1. `DossierServiceImpl.validerDossier()`

**Action :** Après validation du dossier, appeler `createTarifCreationAutomatique()`

#### 4.2. `EnquetteServiceImpl.validerEnquette()`

**Action :** Après validation de l'enquête, appeler `createTarifEnqueteAutomatique()`

#### 4.3. Passage en Phase Juridique

**Action :** Quand `typeRecouvrement` passe à `JURIDIQUE`, appeler `createAvanceRecouvrementJuridique()`

---

## 📋 Processus Complet

### Ordre d'Exécution

**1. Validation du Dossier**
- ✅ Créer automatiquement tarif "OUVERTURE_DOSSIER" (250 TND, VALIDE)

**2. Validation de l'Enquête**
- ✅ Créer automatiquement tarif "ENQUETE_PRECONTENTIEUSE" (300 TND, VALIDE)

**3. Passage en Phase Juridique**
- ✅ Créer automatiquement avance "AVANCE_RECOUVREMENT_JURIDIQUE" (1000 TND, VALIDE)

**4. Saisie Manuelle par le Chef**
- Chef saisit les tarifs d'audience (un par audience)
- Chef saisit les honoraires d'avocat (un par audience avec avocat)
- Chef valide chaque tarif

**5. Validation des Frais**
- Tous les frais sont validés (statut = VALIDE)
- Le système calcule le total des frais validés

**6. Recouvrement Effectif**
- Un montant est recouvré (phase amiable ou juridique)
- Le système enregistre le montant dans `montantRecouvrePhaseAmiable` ou `montantRecouvrePhaseJuridique`

**7. Calcul des Commissions (AUTOMATIQUE)**
- Le système calcule automatiquement les commissions :
  - Commission Amiable = montantRecouvrePhaseAmiable × 12%
  - Commission Juridique = montantRecouvrePhaseJuridique × 15%
  - Commission Intérêts = montantInteretsRecouvres × 50% (si applicable)

**8. Calcul Final de la Facture**
```
Total Frais Validés = 
  Frais Création (250 TND) +
  Frais Enquête (300 TND) +
  Avance Judiciaire (1000 TND) +
  Frais Audiences (saisis par chef) +
  Honoraires Avocat (saisis par chef) +
  Autres Frais Validés

Total Commissions = 
  Commission Amiable (12%) +
  Commission Juridique (15%) +
  Commission Intérêts (50% si applicable)

Total HT = Total Frais Validés + Total Commissions
TVA (19%) = Total HT × 0.19
Total TTC = Total HT + TVA
```

---

## ⚠️ Points d'Attention

### 1. Calcul des Commissions APRÈS Validation

**Important :** Les commissions sont calculées **dynamiquement** lors du calcul de la facture, **après** la validation des frais et **après** le recouvrement effectif.

**Raison :**
- Les commissions dépendent du montant recouvré
- Le montant recouvré n'est connu qu'après le recouvrement
- Les commissions ne sont **pas stockées** dans la base, elles sont **calculées à la volée**

### 2. Tarifs d'Audience et Honoraires d'Avocat

**Processus :**
1. Le chef accède à la page de validation des tarifs
2. Pour chaque audience, le chef peut :
   - Saisir le tarif de l'audience
   - Saisir les honoraires d'avocat (si un avocat est assigné)
3. Le chef valide chaque tarif
4. Les tarifs sont stockés dans `tarif_dossier` avec :
   - `audienceId` (pour le tarif d'audience)
   - `audienceId` + `categorie = "HONORAIRES_AVOCAT"` (pour les honoraires)

### 3. Avance sur Frais Judiciaire

**Important :** L'avance de 1000 TND est une **avance**, pas un frais définitif.

**Gestion actuelle :**
- L'avance est traitée comme un frais normal
- Elle est ajoutée aux frais initiaux
- Elle est incluse dans le total de la facture

### 4. Commission sur Intérêts

**Important :** La commission sur intérêts (50%) nécessite de tracker les intérêts recouvrés.

**Vérification :**
- Vérifier si `Dossier` a un champ `montantInteretsRecouvres`
- Si non → Ajouter ce champ dans l'entité `Dossier` et créer une migration SQL

---

## 📝 Checklist d'Implémentation

### Phase 1 : Constantes et Méthodes Helper

- [ ] Ajouter constante `AVANCE_RECOUVREMENT_JURIDIQUE = 1000.00`
- [ ] Ajouter constante `ATTESTATION_CARENCE = 500.00`
- [ ] Ajouter constantes pour les taux de commission (5%, 12%, 15%, 50%)
- [ ] Créer méthode `createTarifEnqueteAutomatique(Enquette enquete)`
- [ ] Créer méthode `createAvanceRecouvrementJuridique(Dossier dossier)`
- [ ] Créer méthode `createTarifAttestationCarence(Long dossierId, String commentaire)`
- [ ] Créer méthode `calculerCommissionAmiable(Dossier dossier)`
- [ ] Créer méthode `calculerCommissionJuridique(Dossier dossier)`
- [ ] Créer méthode `calculerCommissionInterets(Dossier dossier)`
- [ ] Créer méthode `calculerCommissions(Dossier dossier)`
- [ ] Créer DTO `CommissionDTO`

### Phase 2 : Intégration dans les Validations

- [ ] Modifier `DossierServiceImpl.validerDossier()` pour appeler `createTarifCreationAutomatique()`
- [ ] Modifier `EnquetteServiceImpl.validerEnquette()` pour appeler `createTarifEnqueteAutomatique()`
- [ ] Modifier `DossierServiceImpl` pour appeler `createAvanceRecouvrementJuridique()` lors du passage en phase JURIDIQUE
- [ ] Créer endpoint pour `createTarifAttestationCarence()` (ou intégrer dans la page de validation)

### Phase 3 : Calcul des Commissions

- [ ] Modifier `calculerDetailFacture()` pour récupérer le dossier
- [ ] Modifier `calculerDetailFacture()` pour calculer les commissions
- [ ] Modifier `calculerDetailFacture()` pour ajouter les commissions au total HT
- [ ] Mettre à jour `DetailFactureDTO` avec `commissionInterets` si manquant
- [ ] Vérifier que les commissions sont calculées dynamiquement (pas stockées)

### Phase 4 : Vérifications

- [ ] Vérifier que `Dossier` a le champ `montantInteretsRecouvres` (ajouter si manquant)
- [ ] Vérifier que `DetailFactureDTO` a tous les champs de commission
- [ ] Tester la création automatique du tarif de création (250 TND)
- [ ] Tester la création automatique du tarif d'enquête (300 TND)
- [ ] Tester la création automatique de l'avance judiciaire (1000 TND)
- [ ] Tester le calcul des commissions amiable (12%)
- [ ] Tester le calcul des commissions juridique (15%)
- [ ] Tester le calcul des commissions intérêts (50%)
- [ ] Tester le calcul final de la facture (Total HT, TVA, Total TTC)

---

## 📝 Résumé

**Éléments de l'annexe à intégrer :**

1. ✅ **Prix fixes (créés automatiquement) :**
   - OUVERTURE_DOSSIER : 250 TND (lors validation dossier) - ✅ Constante existe
   - ENQUETE_PRECONTENTIEUSE : 300 TND (lors validation enquête) - ✅ Constante existe
   - AVANCE_RECOUVREMENT_JURIDIQUE : 1000 TND (lors passage phase juridique) - ❌ À ajouter
   - ATTESTATION_CARENCE : 500 TND (à la demande, manuel) - ❌ À ajouter

2. ✅ **Tarifs variables (saisis par chef) :**
   - Tarif Audience : Saisi et validé par le chef - ✅ Déjà implémenté
   - Honoraires Avocat : Saisi et validé par le chef - ✅ Déjà implémenté (via avocatId)

3. ✅ **Commissions (calculées automatiquement) :**
   - Commission Amiable : 12% de `montantRecouvrePhaseAmiable` - ❌ À implémenter
   - Commission Juridique : 15% de `montantRecouvrePhaseJuridique` - ❌ À implémenter
   - Commission Intérêts : 50% de `montantInteretsRecouvres` - ❌ À implémenter (vérifier champ)
   - Commission Relance : 5% de `montantRecouvreRelance` - ❌ À implémenter (si applicable)

4. ✅ **Ordre de calcul :**
   - Validation des frais → Calcul des frais validés
   - Recouvrement effectif → Calcul des commissions
   - Calcul final : Total HT = Frais Validés + Commissions

---

**Date :** 2025-01-05  
**Status :** ✅ Plan d'intégration complet - Prêt pour implémentation


