# 📋 Intégration de l'Annexe : Prix Fixes, Avances et Commissions

## 🎯 Objectif

Intégrer tous les éléments de l'annexe du contrat dans le système de calcul des tarifs :
1. **Prix fixes** (selon annexe)
2. **Avances** (frais de recouvrement judiciaire)
3. **Commissions** (par phase de recouvrement)
4. **Tarifs d'audience et honoraires d'avocat** (saisis manuellement par le chef)

---

## 📊 Analyse de l'Annexe

### Capture 1 : Prix Fixes et Avances

**ARTICLE 1 : AVANCE SUR FRAIS DE TRAITEMENT PAR DOSSIER**

| Service | Montant (TND) | Type |
|---------|---------------|------|
| Relance Factures datées de moins de 6 mois | **Gratuit** | Prix fixe |
| Frais fixes de réception et d'ouverture de dossier | **250 TND** | Prix fixe |
| Frais Enquête Précontentieuse | **300 TND** | Prix fixe |
| Avance sur frais de recouvrement judiciaire | **1000 TND** | Avance |
| Attestation de carence à la demande du mandant | **500 TND** | Prix fixe |

### Capture 2 : Commissions

**Taux de Commission par Phase :**

| Phase de Recouvrement | Taux de Commission |
|----------------------|-------------------|
| Relance Factures datées de moins de 6 mois | **5%** |
| Recouvrement Amiable | **12%** |
| Recouvrement Judiciaire | **15%** |
| Commission sur les intérêts | **50%** |

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

**❌ Non implémenté :** Les commissions sont à 0, elles doivent être calculées selon l'annexe.

---

## ✅ Solution : Intégration Complète

### 1. Ajouter les Constantes pour les Prix Fixes

**Fichier :** `TarifDossierServiceImpl.java`

**Constantes à ajouter :**
```java
// Prix fixes selon annexe
private static final BigDecimal FRAIS_CREATION_DOSSIER = new BigDecimal("250.00"); // ✅ Déjà présent
private static final BigDecimal FRAIS_ENQUETE_PRECONTENTIEUSE = new BigDecimal("300.00"); // ❌ À ajouter
private static final BigDecimal AVANCE_RECOUVREMENT_JURIDIQUE = new BigDecimal("1000.00"); // ❌ À ajouter
private static final BigDecimal ATTESTATION_CARENCE = new BigDecimal("500.00"); // ❌ À ajouter

// Taux de commission selon annexe
private static final BigDecimal TAUX_COMMISSION_RELANCE = new BigDecimal("0.05"); // 5%
private static final BigDecimal TAUX_COMMISSION_AMIABLE = new BigDecimal("0.12"); // 12%
private static final BigDecimal TAUX_COMMISSION_JURIDIQUE = new BigDecimal("0.15"); // 15%
private static final BigDecimal TAUX_COMMISSION_INTERETS = new BigDecimal("0.50"); // 50%
```

### 2. Créer Automatiquement les Tarifs Fixes

#### 2.1. Tarif de Création (250 TND)

**Déjà implémenté :** `createTarifCreationAutomatique()`
- ✅ Montant : 250 TND (correct)
- ✅ Créé automatiquement lors de la validation du dossier
- ✅ Statut : VALIDE

#### 2.2. Tarif d'Enquête Précontentieuse (300 TND)

**À implémenter :** `createTarifEnqueteAutomatique()`
- Montant : 300 TND
- Créé automatiquement lors de la validation de l'enquête
- Statut : VALIDE

#### 2.3. Avance sur Frais de Recouvrement Judiciaire (1000 TND)

**À implémenter :** Création lors du passage en phase juridique
- Montant : 1000 TND
- Type : Avance (à déduire des frais finaux)
- Créé automatiquement quand le dossier passe en phase JURIDIQUE
- Statut : VALIDE

#### 2.4. Attestation de Carence (500 TND)

**À implémenter :** Création à la demande
- Montant : 500 TND
- Créé manuellement par le chef si demandé par le mandant
- Statut : EN_ATTENTE_VALIDATION (puis validé par le chef)

### 3. Calculer les Commissions

#### 3.1. Base de Calcul des Commissions

**Les commissions sont calculées sur :**
- **Montant recouvré** (montantRecouvre) pour chaque phase
- **Intérêts recouvrés** (si applicable) pour la commission sur intérêts

**Formule :**
```
Commission = Montant Recouvré × Taux de Commission
```

#### 3.2. Commission par Phase

**Commission Amiable (12%) :**
- Calculée sur : `montantRecouvrePhaseAmiable`
- Taux : 12%
- Formule : `Commission Amiable = montantRecouvrePhaseAmiable × 0.12`

**Commission Juridique (15%) :**
- Calculée sur : `montantRecouvrePhaseJuridique`
- Taux : 15%
- Formule : `Commission Juridique = montantRecouvrePhaseJuridique × 0.15`

**Commission Relance (5%) :**
- Calculée sur : Montant recouvré en phase "Relance" (si applicable)
- Taux : 5%
- Formule : `Commission Relance = montantRecouvreRelance × 0.05`

**Commission sur Intérêts (50%) :**
- Calculée sur : Montant des intérêts recouvrés (si applicable)
- Taux : 50%
- Formule : `Commission Intérêts = montantInteretsRecouvres × 0.50`

#### 3.3. Quand Calculer les Commissions ?

**Important :** Les commissions sont calculées **APRÈS la validation des frais** et **APRÈS le recouvrement effectif**.

**Processus :**
1. Le chef valide tous les frais (tarifs d'audience, honoraires d'avocat, etc.)
2. Le système calcule le total des frais validés
3. **Ensuite**, quand un montant est recouvré :
   - Le système calcule automatiquement les commissions selon la phase
   - Les commissions sont ajoutées au total de la facture

### 4. Tarifs d'Audience et Honoraires d'Avocat

#### 4.1. Processus Actuel

**D'après votre description :**
- Chaque audience a un **tarif** que le chef entre dans la page de validation
- Chaque audience a un **honoraire d'avocat** que le chef entre dans la page de validation
- Le calcul se fait **après la validation des frais**

#### 4.2. Implémentation Actuelle

**✅ Déjà implémenté :**
- Le chef peut créer un tarif pour une audience via `POST /api/finances/dossier/{dossierId}/tarifs`
- Le chef peut créer un tarif d'honoraire d'avocat via `POST /api/finances/dossier/{dossierId}/tarifs` avec `avocatId`
- Les tarifs sont validés via `POST /api/finances/tarifs/{tarifId}/valider`

**✅ Structure dans `TraitementsDossierDTO` :**
- `phaseJuridique.audiences[]` contient :
  - `coutAudience` : Tarif de l'audience
  - `tarifAudience` : Détails du tarif
  - `coutAvocat` : Honoraires d'avocat
  - `tarifAvocat` : Détails du tarif d'avocat

#### 4.3. Processus de Calcul Après Validation

**Étape 1 : Validation des Frais**
- Le chef valide tous les tarifs (audiences, avocats, etc.)
- Les tarifs validés sont marqués avec `statut = VALIDE`

**Étape 2 : Calcul du Total des Frais**
- Le système somme tous les tarifs validés :
  - Frais de création (250 TND)
  - Frais d'enquête (300 TND)
  - Frais d'audiences (saisis par le chef)
  - Honoraires d'avocat (saisis par le chef)
  - Autres frais validés

**Étape 3 : Calcul des Commissions (Après Recouvrement)**
- Quand un montant est recouvré :
  - Calculer la commission selon la phase (12% amiable, 15% juridique)
  - Ajouter la commission au total de la facture

**Étape 4 : Calcul Final**
```
Total HT = Frais Validés + Commissions
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

### 3. Créer la Méthode pour l'Avance Judiciaire

**Fichier :** `TarifDossierServiceImpl.java`

**Méthode :** `createAvanceRecouvrementJuridique(Dossier dossier)`
- Montant : 1000 TND
- Phase : JURIDIQUE
- Catégorie : "AVANCE_RECOUVREMENT_JURIDIQUE"
- Statut : VALIDE
- Créé automatiquement quand le dossier passe en phase JURIDIQUE

### 4. Implémenter le Calcul des Commissions

**Fichier :** `TarifDossierServiceImpl.java`

**Méthode :** `calculerCommissions(Dossier dossier)`

**Logique :**
```java
// Commission Amiable (12%)
BigDecimal commissionAmiable = BigDecimal.ZERO;
if (dossier.getMontantRecouvrePhaseAmiable() != null && dossier.getMontantRecouvrePhaseAmiable() > 0) {
    commissionAmiable = BigDecimal.valueOf(dossier.getMontantRecouvrePhaseAmiable())
        .multiply(TAUX_COMMISSION_AMIABLE);
}

// Commission Juridique (15%)
BigDecimal commissionJuridique = BigDecimal.ZERO;
if (dossier.getMontantRecouvrePhaseJuridique() != null && dossier.getMontantRecouvrePhaseJuridique() > 0) {
    commissionJuridique = BigDecimal.valueOf(dossier.getMontantRecouvrePhaseJuridique())
        .multiply(TAUX_COMMISSION_JURIDIQUE);
}

// Commission sur Intérêts (50%) - si applicable
BigDecimal commissionInterets = BigDecimal.ZERO;
if (dossier.getMontantInteretsRecouvres() != null && dossier.getMontantInteretsRecouvres() > 0) {
    commissionInterets = BigDecimal.valueOf(dossier.getMontantInteretsRecouvres())
        .multiply(TAUX_COMMISSION_INTERETS);
}
```

### 5. Modifier le Calcul du Total de la Facture

**Fichier :** `TarifDossierServiceImpl.java`

**Méthode :** `calculerDetailFacture(Long dossierId)`

**Modifications :**
- Inclure les commissions dans le calcul du Total HT
- Les commissions sont calculées **seulement si un montant a été recouvré**

---

## 🔄 Flux de Calcul Complet

### Étape 1 : Validation du Dossier

**Action :** Chef valide le dossier

**Résultat :**
- ✅ Tarif "OUVERTURE_DOSSIER" (250 TND) créé automatiquement avec statut VALIDE

### Étape 2 : Validation de l'Enquête

**Action :** Chef valide l'enquête

**Résultat :**
- ✅ Tarif "ENQUETE_PRECONTENTIEUSE" (300 TND) créé automatiquement avec statut VALIDE

### Étape 3 : Passage en Phase Juridique

**Action :** Dossier passe en phase JURIDIQUE

**Résultat :**
- ✅ Avance "AVANCE_RECOUVREMENT_JURIDIQUE" (1000 TND) créée automatiquement avec statut VALIDE

### Étape 4 : Saisie des Tarifs d'Audience et d'Avocat

**Action :** Chef saisit les tarifs dans la page de validation

**Résultat :**
- ✅ Tarifs d'audience créés (statut EN_ATTENTE_VALIDATION)
- ✅ Tarifs d'honoraires d'avocat créés (statut EN_ATTENTE_VALIDATION)

### Étape 5 : Validation des Frais

**Action :** Chef valide tous les tarifs

**Résultat :**
- ✅ Tous les tarifs passent à statut VALIDE
- ✅ Le système calcule le total des frais validés :
  ```
  Total Frais = Frais Création + Frais Enquête + Avance Judiciaire + 
                Frais Audiences + Honoraires Avocat + Autres Frais
  ```

### Étape 6 : Recouvrement et Calcul des Commissions

**Action :** Un montant est recouvré (phase amiable ou juridique)

**Résultat :**
- ✅ Le système calcule automatiquement les commissions :
  - Commission Amiable = montantRecouvrePhaseAmiable × 12%
  - Commission Juridique = montantRecouvrePhaseJuridique × 15%
  - Commission Intérêts = montantInteretsRecouvres × 50% (si applicable)

### Étape 7 : Calcul Final de la Facture

**Action :** Génération de la facture

**Résultat :**
```
Total Frais Validés = [Somme de tous les tarifs validés]
Total Commissions = Commission Amiable + Commission Juridique + Commission Intérêts
Total HT = Total Frais Validés + Total Commissions
TVA (19%) = Total HT × 0.19
Total TTC = Total HT + TVA
```

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

| Type | Taux | Base de Calcul |
|------|------|----------------|
| Commission Amiable | 12% | montantRecouvrePhaseAmiable |
| Commission Juridique | 15% | montantRecouvrePhaseJuridique |
| Commission Relance | 5% | montantRecouvreRelance |
| Commission Intérêts | 50% | montantInteretsRecouvres |

---

## ⚠️ Points d'Attention

### 1. Ordre de Calcul

**Important :** Les commissions sont calculées **APRÈS** la validation des frais et **APRÈS** le recouvrement effectif.

**Raison :** Les commissions dépendent du montant recouvré, qui n'est connu qu'après le recouvrement.

### 2. Avance sur Frais Judiciaire

**Important :** L'avance de 1000 TND est une **avance**, pas un frais définitif.

**Gestion :**
- L'avance est ajoutée aux frais initiaux
- Elle peut être déduite des frais finaux si le recouvrement est inférieur
- Ou elle peut être partiellement remboursée si le recouvrement est supérieur

**Recommandation :** Traiter l'avance comme un frais normal pour l'instant, et gérer les ajustements dans une version future.

### 3. Commission sur Intérêts

**Important :** La commission sur intérêts (50%) nécessite de tracker séparément les intérêts recouvrés.

**Vérification :** Vérifier si le système tracke déjà les intérêts séparément, ou s'il faut ajouter ce champ.

### 4. Relance Factures < 6 mois

**Important :** Cette phase a une commission de 5%, mais les frais sont gratuits.

**Gestion :**
- Pas de frais fixes pour cette phase
- Commission de 5% sur le montant recouvré en phase "Relance"

---

## 📝 Checklist d'Implémentation

### Phase 1 : Prix Fixes

- [ ] Ajouter constante `FRAIS_ENQUETE_PRECONTENTIEUSE = 300.00`
- [ ] Ajouter constante `AVANCE_RECOUVREMENT_JURIDIQUE = 1000.00`
- [ ] Ajouter constante `ATTESTATION_CARENCE = 500.00`
- [ ] Créer méthode `createTarifEnqueteAutomatique()`
- [ ] Créer méthode `createAvanceRecouvrementJuridique()`
- [ ] Appeler `createTarifEnqueteAutomatique()` lors de la validation de l'enquête
- [ ] Appeler `createAvanceRecouvrementJuridique()` lors du passage en phase juridique

### Phase 2 : Commissions

- [ ] Ajouter constantes pour les taux de commission (5%, 12%, 15%, 50%)
- [ ] Créer méthode `calculerCommissions(Dossier dossier)`
- [ ] Calculer commission amiable (12% sur montantRecouvrePhaseAmiable)
- [ ] Calculer commission juridique (15% sur montantRecouvrePhaseJuridique)
- [ ] Calculer commission intérêts (50% sur montantInteretsRecouvres) - si applicable
- [ ] Intégrer les commissions dans `calculerDetailFacture()`
- [ ] S'assurer que les commissions sont calculées APRÈS le recouvrement

### Phase 3 : Validation et Tests

- [ ] Tester la création automatique du tarif de création (250 TND)
- [ ] Tester la création automatique du tarif d'enquête (300 TND)
- [ ] Tester la création automatique de l'avance judiciaire (1000 TND)
- [ ] Tester le calcul des commissions amiable (12%)
- [ ] Tester le calcul des commissions juridique (15%)
- [ ] Tester le calcul final de la facture avec commissions

---

## 🎯 Résumé

**Prix fixes à intégrer :**
- ✅ OUVERTURE_DOSSIER : 250 TND (déjà implémenté)
- ❌ ENQUETE_PRECONTENTIEUSE : 300 TND (à ajouter)
- ❌ AVANCE_RECOUVREMENT_JURIDIQUE : 1000 TND (à ajouter)
- ❌ ATTESTATION_CARENCE : 500 TND (à ajouter - manuel)

**Commissions à intégrer :**
- ❌ Commission Amiable : 12% (à implémenter)
- ❌ Commission Juridique : 15% (à implémenter)
- ❌ Commission Relance : 5% (à implémenter)
- ❌ Commission Intérêts : 50% (à implémenter)

**Processus :**
1. ✅ Tarifs fixes créés automatiquement lors de la validation
2. ✅ Tarifs d'audience et avocat saisis manuellement par le chef
3. ✅ Validation des frais par le chef
4. ❌ Calcul des commissions APRÈS recouvrement (à implémenter)
5. ✅ Calcul final de la facture (à modifier pour inclure commissions)

---

**Date :** 2025-01-05  
**Status :** ✅ Analyse complétée - Prêt pour implémentation
