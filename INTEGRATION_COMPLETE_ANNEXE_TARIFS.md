# 📋 Intégration Complète de l'Annexe : Prix Fixes, Avances et Commissions

## 🎯 Objectif

Intégrer tous les éléments de l'annexe du contrat dans le système de calcul des tarifs et de validation :
1. **Prix fixes** (selon annexe)
2. **Avances** (frais de recouvrement judiciaire)
3. **Commissions** (par phase de recouvrement)
4. **Tarifs d'audience et honoraires d'avocat** (saisis manuellement par le chef)
5. **Calcul après validation des frais**

---

## 📊 Analyse de l'Annexe

### Capture 1 : Prix Fixes et Avances

**ARTICLE 1 : AVANCE SUR FRAIS DE TRAITEMENT PAR DOSSIER**

| Service | Montant (TND) | Type | Création |
|---------|---------------|------|----------|
| Relance Factures datées de moins de 6 mois | **Gratuit** | Prix fixe | Automatique (si applicable) |
| Frais fixes de réception et d'ouverture de dossier | **250 TND** | Prix fixe | Automatique lors validation dossier |
| Frais Enquête Précontentieuse | **300 TND** | Prix fixe | Automatique lors validation enquête |
| Avance sur frais de recouvrement judiciaire | **1000 TND** | Avance | Automatique lors passage phase JURIDIQUE |
| Attestation de carence à la demande du mandant | **500 TND** | Prix fixe | Manuel (à la demande) |

### Capture 2 : Commissions

**Taux de Commission par Phase :**

| Phase de Recouvrement | Taux de Commission | Base de Calcul |
|----------------------|-------------------|----------------|
| Relance Factures datées de moins de 6 mois | **5%** | Montant recouvré en phase Relance |
| Recouvrement Amiable | **12%** | `montantRecouvrePhaseAmiable` |
| Recouvrement Judiciaire | **15%** | `montantRecouvrePhaseJuridique` |
| Commission sur les intérêts | **50%** | `montantInteretsRecouvres` (si applicable) |

---

## 🔍 État Actuel du Code

### Prix Fixes Actuels

**Fichier :** `TarifDossierServiceImpl.java` (ligne 62)

```java
private static final BigDecimal FRAIS_CREATION_DOSSIER = new BigDecimal("250.00");
```

**✅ Correct :** Le montant de 250 TND correspond à l'annexe.

**❌ Manquant :**
- Frais Enquête Précontentieuse : 300 TND
- Avance sur frais de recouvrement judiciaire : 1000 TND
- Attestation de carence : 500 TND

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

## ✅ Solution : Intégration Complète

### 1. Ajouter les Constantes pour les Prix Fixes et Taux de Commission

**Fichier :** `TarifDossierServiceImpl.java`

**Constantes à ajouter :**

```java
// Prix fixes selon annexe
private static final BigDecimal FRAIS_CREATION_DOSSIER = new BigDecimal("250.00");  // ✅ Existe déjà
private static final BigDecimal FRAIS_ENQUETE_PRECONTENTIEUSE = new BigDecimal("300.00");  // ✅ NOUVEAU
private static final BigDecimal AVANCE_RECOUVREMENT_JURIDIQUE = new BigDecimal("1000.00");  // ✅ NOUVEAU
private static final BigDecimal ATTESTATION_CARENCE = new BigDecimal("500.00");  // ✅ NOUVEAU

// Taux de commission selon annexe
private static final BigDecimal TAUX_COMMISSION_RELANCE = new BigDecimal("0.05");  // 5%
private static final BigDecimal TAUX_COMMISSION_AMIABLE = new BigDecimal("0.12");  // 12%
private static final BigDecimal TAUX_COMMISSION_JURIDIQUE = new BigDecimal("0.15");  // 15%
private static final BigDecimal TAUX_COMMISSION_INTERETS = new BigDecimal("0.50");  // 50%
```

### 2. Créer Automatiquement les Tarifs Fixes

#### 2.1. Tarif de Création (250 TND)

**Quand :** Lors de la validation d'un dossier

**Méthode :** `createTarifCreationAutomatique(Dossier dossier)` - ✅ Existe déjà

**Modification nécessaire :** S'assurer qu'elle est appelée dans `DossierServiceImpl.validerDossier()`

#### 2.2. Tarif d'Enquête (300 TND)

**Quand :** Lors de la validation d'une enquête

**Méthode à créer :** `createTarifEnqueteAutomatique(Enquette enquete)`

**Logique :**
```
1. Vérifier si un tarif existe déjà pour (dossierId, phase=ENQUETE, categorie=ENQUETE_PRECONTENTIEUSE, enqueteId)
2. Si aucun tarif n'existe :
   - Créer TarifDossier avec :
     - dossier = dossier de l'enquête
     - enquete = enquête validée
     - phase = ENQUETE
     - categorie = "ENQUETE_PRECONTENTIEUSE"
     - coutUnitaire = 300.00 (FRAIS_ENQUETE_PRECONTENTIEUSE)
     - quantite = 1
     - montantTotal = 300.00
     - statut = VALIDE (validé automatiquement)
     - dateCreation = maintenant
     - dateValidation = maintenant
     - commentaire = "Frais fixe selon annexe - Validation automatique lors de la validation de l'enquête"
3. Sauvegarder le tarif
```

**Appel :** Dans `EnquetteServiceImpl.validerEnquette()` après validation

#### 2.3. Avance sur Frais Judiciaire (1000 TND)

**Quand :** Lors du passage d'un dossier en phase JURIDIQUE

**Méthode à créer :** `createAvanceRecouvrementJuridique(Dossier dossier)`

**Logique :**
```
1. Vérifier si un tarif existe déjà pour (dossierId, phase=JURIDIQUE, categorie=AVANCE_RECOUVREMENT_JURIDIQUE)
2. Si aucun tarif n'existe :
   - Créer TarifDossier avec :
     - dossier = dossier
     - phase = JURIDIQUE
     - categorie = "AVANCE_RECOUVREMENT_JURIDIQUE"
     - typeElement = "Avance sur frais de recouvrement judiciaire"
     - coutUnitaire = 1000.00 (AVANCE_RECOUVREMENT_JURIDIQUE)
     - quantite = 1
     - montantTotal = 1000.00
     - statut = VALIDE (validé automatiquement)
     - dateCreation = maintenant
     - dateValidation = maintenant
     - commentaire = "Avance fixe selon annexe - Création automatique lors du passage en phase juridique"
3. Sauvegarder le tarif
```

**Appel :** Dans `DossierServiceImpl` quand `typeRecouvrement` passe à `JURIDIQUE`

#### 2.4. Attestation de Carence (500 TND)

**Quand :** À la demande du mandant (manuel)

**Méthode à créer :** `createTarifAttestationCarence(Long dossierId, String commentaire)`

**Logique :**
```
1. Vérifier si un tarif existe déjà pour (dossierId, phase=JURIDIQUE, categorie=ATTESTATION_CARENCE)
2. Si aucun tarif n'existe :
   - Créer TarifDossier avec :
     - dossier = dossier
     - phase = JURIDIQUE
     - categorie = "ATTESTATION_CARENCE"
     - typeElement = "Attestation de carence à la demande du mandant"
     - coutUnitaire = 500.00 (ATTESTATION_CARENCE)
     - quantite = 1
     - montantTotal = 500.00
     - statut = EN_ATTENTE_VALIDATION (doit être validé par le chef)
     - dateCreation = maintenant
     - commentaire = commentaire fourni ou "Attestation de carence - À la demande du mandant"
3. Sauvegarder le tarif
```

**Appel :** Via un endpoint dédié ou depuis la page de validation des tarifs

### 3. Tarifs Saisis Manuellement par le Chef

#### 3.1. Tarif d'Audience

**Processus actuel :**
- Le chef saisit le tarif dans la page de validation des tarifs
- Le chef valide le tarif
- Le tarif est lié à une audience spécifique

**✅ Déjà implémenté :** L'endpoint `POST /api/finances/dossier/{dossierId}/tarifs` permet de créer un tarif avec `audienceId`

**Vérification :** S'assurer que le frontend permet bien la saisie du tarif d'audience

#### 3.2. Honoraires d'Avocat

**Processus actuel :**
- Le chef saisit les honoraires dans la page de validation des tarifs
- Le chef valide le tarif
- Le tarif est lié à une audience via `avocatId` (qui est mappé vers `audienceId`)

**✅ Déjà implémenté :** L'endpoint `POST /api/finances/dossier/{dossierId}/tarifs` permet de créer un tarif avec `avocatId`

**Vérification :** S'assurer que le frontend permet bien la saisie des honoraires d'avocat

### 4. Calcul des Commissions

#### 4.1. Quand Calculer les Commissions ?

**Important :** Les commissions sont calculées **APRÈS la validation des frais** et **APRÈS le recouvrement effectif**.

**Raison :**
- Les commissions dépendent du montant recouvré
- Le montant recouvré n'est connu qu'après le recouvrement effectif
- Les commissions sont ajoutées au total de la facture

**Processus :**
1. Le chef valide tous les frais (tarifs d'audience, honoraires d'avocat, etc.)
2. Le système calcule le total des frais validés
3. **Ensuite**, quand un montant est recouvré :
   - Le système enregistre le montant recouvré dans `montantRecouvrePhaseAmiable` ou `montantRecouvrePhaseJuridique`
   - Le système calcule automatiquement les commissions selon la phase
   - Les commissions sont ajoutées au total de la facture

#### 4.2. Méthode de Calcul des Commissions

**Fichier :** `TarifDossierServiceImpl.java`

**Méthode à créer :** `calculerCommissions(Dossier dossier)`

**Logique :**

```java
// Commission Amiable (12%)
BigDecimal commissionAmiable = BigDecimal.ZERO;
if (dossier.getMontantRecouvrePhaseAmiable() != null && 
    dossier.getMontantRecouvrePhaseAmiable() > 0) {
    commissionAmiable = BigDecimal.valueOf(dossier.getMontantRecouvrePhaseAmiable())
        .multiply(TAUX_COMMISSION_AMIABLE);
}

// Commission Juridique (15%)
BigDecimal commissionJuridique = BigDecimal.ZERO;
if (dossier.getMontantRecouvrePhaseJuridique() != null && 
    dossier.getMontantRecouvrePhaseJuridique() > 0) {
    commissionJuridique = BigDecimal.valueOf(dossier.getMontantRecouvrePhaseJuridique())
        .multiply(TAUX_COMMISSION_JURIDIQUE);
}

// Commission Relance (5%) - Si applicable
BigDecimal commissionRelance = BigDecimal.ZERO;
// TODO: Implémenter si le système tracke la phase "Relance" séparément
// Pour l'instant, si le dossier est en phase "Relance" avant amiable :
// commissionRelance = montantRecouvreRelance × TAUX_COMMISSION_RELANCE

// Commission sur Intérêts (50%) - Si applicable
BigDecimal commissionInterets = BigDecimal.ZERO;
if (dossier.getMontantInteretsRecouvres() != null && 
    dossier.getMontantInteretsRecouvres() > 0) {
    commissionInterets = BigDecimal.valueOf(dossier.getMontantInteretsRecouvres())
        .multiply(TAUX_COMMISSION_INTERETS);
}

// Total commissions
BigDecimal totalCommissions = commissionAmiable
    .add(commissionJuridique)
    .add(commissionRelance)
    .add(commissionInterets);
```

#### 4.3. Intégration dans le Calcul de la Facture

**Fichier :** `TarifDossierServiceImpl.java`

**Méthode :** `calculerDetailFacture(Long dossierId)`

**Modifications :**

1. **Récupérer le dossier :**
   ```java
   Dossier dossier = dossierRepository.findById(dossierId)
       .orElseThrow(() -> new RuntimeException("Dossier non trouvé"));
   ```

2. **Calculer les commissions :**
   ```java
   BigDecimal commissionAmiable = calculerCommissionAmiable(dossier);
   BigDecimal commissionJuridique = calculerCommissionJuridique(dossier);
   BigDecimal commissionInterets = calculerCommissionInterets(dossier);
   ```

3. **Ajouter les commissions au total HT :**
   ```java
   BigDecimal totalHT = fraisCreation
       .add(fraisEnquete)
       .add(coutGestionTotal)
       .add(fraisAmiable)
       .add(fraisJuridique)
       .add(fraisAvocat)
       .add(fraisHuissier)
       .add(commissionAmiable)      // ✅ NOUVEAU
       .add(commissionJuridique)   // ✅ NOUVEAU
       .add(commissionInterets);    // ✅ NOUVEAU (si applicable)
   ```

4. **Mettre à jour le DTO :**
   ```java
   dto.setCommissionAmiable(commissionAmiable);
   dto.setCommissionJuridique(commissionJuridique);
   // Ajouter commissionInterets si le DTO le supporte
   ```

### 5. Ordre de Calcul et Validation

#### 5.1. Processus Complet

**Étape 1 : Validation du Dossier**
- Créer automatiquement tarif "OUVERTURE_DOSSIER" (250 TND, VALIDE)

**Étape 2 : Validation de l'Enquête**
- Créer automatiquement tarif "ENQUETE_PRECONTENTIEUSE" (300 TND, VALIDE)

**Étape 3 : Passage en Phase Juridique**
- Créer automatiquement avance "AVANCE_RECOUVREMENT_JURIDIQUE" (1000 TND, VALIDE)

**Étape 4 : Saisie Manuelle par le Chef**
- Chef saisit les tarifs d'audience (un par audience)
- Chef saisit les honoraires d'avocat (un par audience avec avocat)
- Chef valide chaque tarif

**Étape 5 : Validation des Frais**
- Tous les frais sont validés (statut = VALIDE)
- Le système calcule le total des frais validés

**Étape 6 : Recouvrement Effectif**
- Un montant est recouvré (phase amiable ou juridique)
- Le système enregistre le montant dans `montantRecouvrePhaseAmiable` ou `montantRecouvrePhaseJuridique`

**Étape 7 : Calcul des Commissions (AUTOMATIQUE)**
- Le système calcule automatiquement les commissions selon la phase
- Commission Amiable = montantRecouvrePhaseAmiable × 12%
- Commission Juridique = montantRecouvrePhaseJuridique × 15%
- Commission Intérêts = montantInteretsRecouvres × 50% (si applicable)

**Étape 8 : Calcul Final de la Facture**
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

## 📋 Modifications Requises

### 1. Ajouter les Constantes

**Fichier :** `TarifDossierServiceImpl.java`

**Action :** Ajouter les constantes pour les prix fixes et taux de commission

### 2. Créer la Méthode pour le Tarif d'Enquête

**Fichier :** `TarifDossierServiceImpl.java`

**Méthode :** `createTarifEnqueteAutomatique(Enquette enquete)`
- Montant : 300 TND
- Phase : ENQUETE
- Catégorie : "ENQUETE_PRECONTENTIEUSE"
- Statut : VALIDE

**Appel :** Dans `EnquetteServiceImpl.validerEnquette()`

### 3. Créer la Méthode pour l'Avance Judiciaire

**Fichier :** `TarifDossierServiceImpl.java`

**Méthode :** `createAvanceRecouvrementJuridique(Dossier dossier)`
- Montant : 1000 TND
- Phase : JURIDIQUE
- Catégorie : "AVANCE_RECOUVREMENT_JURIDIQUE"
- Statut : VALIDE
- Créé automatiquement quand le dossier passe en phase JURIDIQUE

**Appel :** Dans `DossierServiceImpl` quand `typeRecouvrement` passe à `JURIDIQUE`

### 4. Créer la Méthode pour l'Attestation de Carence

**Fichier :** `TarifDossierServiceImpl.java`

**Méthode :** `createTarifAttestationCarence(Long dossierId, String commentaire)`
- Montant : 500 TND
- Phase : JURIDIQUE
- Catégorie : "ATTESTATION_CARENCE"
- Statut : EN_ATTENTE_VALIDATION (doit être validé par le chef)

**Appel :** Via un endpoint dédié ou depuis la page de validation

### 5. Implémenter le Calcul des Commissions

**Fichier :** `TarifDossierServiceImpl.java`

**Méthodes à créer :**
- `calculerCommissionAmiable(Dossier dossier)` → 12% de `montantRecouvrePhaseAmiable`
- `calculerCommissionJuridique(Dossier dossier)` → 15% de `montantRecouvrePhaseJuridique`
- `calculerCommissionInterets(Dossier dossier)` → 50% de `montantInteretsRecouvres`
- `calculerCommissions(Dossier dossier)` → Retourne un DTO avec toutes les commissions

**Intégration :** Dans `calculerDetailFacture()` pour ajouter les commissions au total HT

### 6. Modifier le Calcul du Total de la Facture

**Fichier :** `TarifDossierServiceImpl.java`

**Méthode :** `calculerDetailFacture(Long dossierId)`

**Modifications :**
- Récupérer le dossier pour accéder aux montants recouvrés
- Calculer les commissions via `calculerCommissions()`
- Ajouter les commissions au total HT
- Mettre à jour le DTO avec les commissions

### 7. Ajouter les Champs dans le DTO

**Fichier :** `DetailFactureDTO.java`

**Champs à ajouter (si manquants) :**
- `commissionInterets` (BigDecimal)
- `commissionRelance` (BigDecimal) - si applicable

---

## 📊 Structure des Données

### Tarifs Fixes (Créés Automatiquement)

| Catégorie | Montant | Phase | Création Automatique |
|-----------|---------|-------|----------------------|
| OUVERTURE_DOSSIER | 250 TND | CREATION | Lors validation dossier |
| ENQUETE_PRECONTENTIEUSE | 300 TND | ENQUETE | Lors validation enquête |
| AVANCE_RECOUVREMENT_JURIDIQUE | 1000 TND | JURIDIQUE | Lors passage phase juridique |
| ATTESTATION_CARENCE | 500 TND | JURIDIQUE | À la demande (manuel) |

### Tarifs Variables (Saisis par le Chef)

| Type | Saisie | Validation |
|------|--------|------------|
| Tarif Audience | Chef saisit dans page validation | Chef valide |
| Honoraires Avocat | Chef saisit dans page validation | Chef valide |

### Commissions (Calculées Automatiquement)

| Type | Taux | Base de Calcul | Quand |
|------|------|---------------|-------|
| Commission Amiable | 12% | montantRecouvrePhaseAmiable | Après recouvrement amiable |
| Commission Juridique | 15% | montantRecouvrePhaseJuridique | Après recouvrement juridique |
| Commission Relance | 5% | montantRecouvreRelance | Après recouvrement relance (si applicable) |
| Commission Intérêts | 50% | montantInteretsRecouvres | Après recouvrement intérêts (si applicable) |

---

## ⚠️ Points d'Attention

### 1. Ordre de Calcul

**Important :** Les commissions sont calculées **APRÈS** la validation des frais et **APRÈS** le recouvrement effectif.

**Raison :** Les commissions dépendent du montant recouvré, qui n'est connu qu'après le recouvrement.

**Implémentation :**
- Les commissions sont calculées **dynamiquement** lors du calcul de la facture
- Si aucun montant n'est recouvré → Commissions = 0
- Si un montant est recouvré → Commissions calculées automatiquement

### 2. Avance sur Frais Judiciaire

**Important :** L'avance de 1000 TND est une **avance**, pas un frais définitif.

**Gestion actuelle :**
- L'avance est traitée comme un frais normal
- Elle est ajoutée aux frais initiaux
- Elle est incluse dans le total de la facture

**Gestion future (à implémenter) :**
- L'avance peut être déduite des frais finaux si le recouvrement est inférieur
- Ou elle peut être partiellement remboursée si le recouvrement est supérieur

**Recommandation :** Traiter l'avance comme un frais normal pour l'instant, et gérer les ajustements dans une version future.

### 3. Commission sur Intérêts

**Important :** La commission sur intérêts (50%) nécessite de tracker séparément les intérêts recouvrés.

**Vérification :**
- Vérifier si `Dossier` a un champ `montantInteretsRecouvres`
- Si non → Ajouter ce champ ou utiliser un autre mécanisme pour tracker les intérêts

### 4. Commission Relance

**Important :** La commission relance (5%) nécessite de tracker séparément le montant recouvré en phase "Relance".

**Vérification :**
- Vérifier si le système tracke une phase "Relance" séparée
- Si non → La commission relance peut être ignorée ou intégrée dans la commission amiable

---

## 📝 Checklist d'Implémentation

### Phase 1 : Constantes et Méthodes Helper

- [ ] Ajouter constante `FRAIS_ENQUETE_PRECONTENTIEUSE = 300.00`
- [ ] Ajouter constante `AVANCE_RECOUVREMENT_JURIDIQUE = 1000.00`
- [ ] Ajouter constante `ATTESTATION_CARENCE = 500.00`
- [ ] Ajouter constantes pour les taux de commission (5%, 12%, 15%, 50%)
- [ ] Créer méthode `createTarifEnqueteAutomatique()`
- [ ] Créer méthode `createAvanceRecouvrementJuridique()`
- [ ] Créer méthode `createTarifAttestationCarence()`
- [ ] Créer méthode `calculerCommissionAmiable()`
- [ ] Créer méthode `calculerCommissionJuridique()`
- [ ] Créer méthode `calculerCommissionInterets()`
- [ ] Créer méthode `calculerCommissions()`

### Phase 2 : Intégration dans les Validations

- [ ] Modifier `DossierServiceImpl.validerDossier()` pour appeler `createTarifCreationAutomatique()`
- [ ] Modifier `EnquetteServiceImpl.validerEnquette()` pour appeler `createTarifEnqueteAutomatique()`
- [ ] Modifier `DossierServiceImpl` pour appeler `createAvanceRecouvrementJuridique()` lors du passage en phase JURIDIQUE
- [ ] Créer endpoint pour `createTarifAttestationCarence()` (ou intégrer dans la page de validation)

### Phase 3 : Calcul des Commissions

- [ ] Modifier `calculerDetailFacture()` pour calculer les commissions
- [ ] Ajouter les commissions au total HT
- [ ] Mettre à jour le DTO avec les commissions
- [ ] Vérifier que les commissions sont calculées dynamiquement (pas stockées)

### Phase 4 : Tests

- [ ] Tester la création automatique du tarif de création (250 TND)
- [ ] Tester la création automatique du tarif d'enquête (300 TND)
- [ ] Tester la création automatique de l'avance judiciaire (1000 TND)
- [ ] Tester le calcul des commissions amiable (12%)
- [ ] Tester le calcul des commissions juridique (15%)
- [ ] Tester le calcul des commissions intérêts (50%)
- [ ] Tester le calcul final de la facture (Total HT, TVA, Total TTC)

---

## 🔍 Vérifications

### 1. Vérifier les Champs dans Dossier

**Vérifier que `Dossier` a les champs suivants :**
- `montantRecouvrePhaseAmiable` (Double)
- `montantRecouvrePhaseJuridique` (Double)
- `montantInteretsRecouvres` (Double) - si applicable

**Si manquants :** Ajouter ces champs dans l'entité `Dossier` et créer une migration SQL

### 2. Vérifier le DTO DetailFactureDTO

**Vérifier que `DetailFactureDTO` a les champs suivants :**
- `commissionAmiable` (BigDecimal) - ✅ Existe
- `commissionJuridique` (BigDecimal) - ✅ Existe
- `commissionInterets` (BigDecimal) - À ajouter si manquant
- `commissionRelance` (BigDecimal) - À ajouter si applicable

---

## 📝 Résumé

**Éléments de l'annexe à intégrer :**

1. ✅ **Prix fixes :**
   - OUVERTURE_DOSSIER : 250 TND (créé automatiquement)
   - ENQUETE_PRECONTENTIEUSE : 300 TND (créé automatiquement)
   - AVANCE_RECOUVREMENT_JURIDIQUE : 1000 TND (créé automatiquement)
   - ATTESTATION_CARENCE : 500 TND (créé manuellement)

2. ✅ **Tarifs variables (saisis par chef) :**
   - Tarif Audience : Saisi et validé par le chef
   - Honoraires Avocat : Saisi et validé par le chef

3. ✅ **Commissions (calculées automatiquement) :**
   - Commission Amiable : 12% de `montantRecouvrePhaseAmiable`
   - Commission Juridique : 15% de `montantRecouvrePhaseJuridique`
   - Commission Intérêts : 50% de `montantInteretsRecouvres` (si applicable)
   - Commission Relance : 5% de `montantRecouvreRelance` (si applicable)

4. ✅ **Ordre de calcul :**
   - Validation des frais → Calcul des frais validés
   - Recouvrement effectif → Calcul des commissions
   - Calcul final : Total HT = Frais Validés + Commissions

---

**Date :** 2025-01-05  
**Status :** ✅ Plan d'intégration complet - Prêt pour implémentation


