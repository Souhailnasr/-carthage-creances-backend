# 📋 Guide d'Implémentation : Intégration Complète de l'Annexe

## 🎯 Objectif

Intégrer tous les éléments de l'annexe du contrat dans le système :
- **Prix fixes** : 250 TND (création), 300 TND (enquête), 1000 TND (avance), 500 TND (attestation)
- **Avances** : 1000 TND (recouvrement judiciaire)
- **Commissions** : 5% (relance), 12% (amiable), 15% (juridique), 50% (intérêts)
- **Tarifs variables** : Audience et honoraires d'avocat (saisis par le chef)
- **Calcul après validation** : Les commissions sont calculées après la validation des frais et après le recouvrement

---

## 📊 Éléments de l'Annexe à Intégrer

### 1. Prix Fixes (Créés Automatiquement)

| Service | Montant | Phase | Création |
|---------|---------|-------|----------|
| Frais fixes de réception et d'ouverture de dossier | **250 TND** | CREATION | Automatique lors validation dossier |
| Frais Enquête Précontentieuse | **300 TND** | ENQUETE | Automatique lors validation enquête |
| Avance sur frais de recouvrement judiciaire | **1000 TND** | JURIDIQUE | Automatique lors passage phase JURIDIQUE |
| Attestation de carence à la demande du mandant | **500 TND** | JURIDIQUE | Manuel (à la demande) |

### 2. Commissions (Calculées Automatiquement)

| Phase | Taux | Base de Calcul | Quand |
|-------|------|----------------|-------|
| Relance Factures < 6 mois | **5%** | montantRecouvreRelance | Après recouvrement relance |
| Recouvrement Amiable | **12%** | montantRecouvrePhaseAmiable | Après recouvrement amiable |
| Recouvrement Judiciaire | **15%** | montantRecouvrePhaseJuridique | Après recouvrement juridique |
| Commission sur intérêts | **50%** | montantInteretsRecouvres | Après recouvrement intérêts |

### 3. Tarifs Variables (Saisis par le Chef)

| Type | Saisie | Validation |
|------|--------|------------|
| Tarif Audience | Chef saisit dans page validation | Chef valide |
| Honoraires Avocat | Chef saisit dans page validation | Chef valide |

---

## 🔍 État Actuel du Code

### Constantes Existantes

**Fichier :** `TarifDossierServiceImpl.java`

```java
private static final BigDecimal FRAIS_CREATION_DOSSIER = new BigDecimal("250.00");  // ✅ Existe
private static final BigDecimal FRAIS_ENQUETE_PRECONTENTIEUSE = new BigDecimal("300.00");  // ✅ Existe
```

**Constantes Manquantes :**
- `AVANCE_RECOUVREMENT_JURIDIQUE = 1000.00`
- `ATTESTATION_CARENCE = 500.00`
- `TAUX_COMMISSION_RELANCE = 0.05`
- `TAUX_COMMISSION_AMIABLE = 0.12`
- `TAUX_COMMISSION_JURIDIQUE = 0.15`
- `TAUX_COMMISSION_INTERETS = 0.50`

### Champs dans Dossier

**Fichier :** `Dossier.java`

**Champs existants :**
- ✅ `montantRecouvrePhaseAmiable` (Double)
- ✅ `montantRecouvrePhaseJuridique` (Double)

**Champs à vérifier :**
- ❓ `montantInteretsRecouvres` (Double) - À vérifier si existe

---

## ✅ Plan d'Implémentation

### Étape 1 : Ajouter les Constantes Manquantes

**Fichier :** `TarifDossierServiceImpl.java`

**Constantes à ajouter :**

```java
// Prix fixes selon annexe
private static final BigDecimal FRAIS_CREATION_DOSSIER = new BigDecimal("250.00");  // ✅ Existe
private static final BigDecimal FRAIS_ENQUETE_PRECONTENTIEUSE = new BigDecimal("300.00");  // ✅ Existe
private static final BigDecimal AVANCE_RECOUVREMENT_JURIDIQUE = new BigDecimal("1000.00");  // ✅ NOUVEAU
private static final BigDecimal ATTESTATION_CARENCE = new BigDecimal("500.00");  // ✅ NOUVEAU

// Taux de commission selon annexe
private static final BigDecimal TAUX_COMMISSION_RELANCE = new BigDecimal("0.05");  // 5% - ✅ NOUVEAU
private static final BigDecimal TAUX_COMMISSION_AMIABLE = new BigDecimal("0.12");  // 12% - ✅ NOUVEAU
private static final BigDecimal TAUX_COMMISSION_JURIDIQUE = new BigDecimal("0.15");  // 15% - ✅ NOUVEAU
private static final BigDecimal TAUX_COMMISSION_INTERETS = new BigDecimal("0.50");  // 50% - ✅ NOUVEAU
```

### Étape 2 : Créer les Méthodes pour les Tarifs Automatiques

#### 2.1. Tarif d'Enquête (300 TND)

**Méthode :** `createTarifEnqueteAutomatique(Enquette enquete)`

**Logique :**
1. Vérifier si un tarif existe déjà pour `(dossierId, phase=ENQUETE, categorie=ENQUETE_PRECONTENTIEUSE, enqueteId)`
2. Si aucun tarif n'existe → Créer avec :
   - `coutUnitaire = 300.00`
   - `statut = VALIDE`
   - `dateValidation = maintenant`

**Appel :** Dans `EnquetteServiceImpl.validerEnquette()` après validation

#### 2.2. Avance Judiciaire (1000 TND)

**Méthode :** `createAvanceRecouvrementJuridique(Dossier dossier)`

**Logique :**
1. Vérifier si un tarif existe déjà pour `(dossierId, phase=JURIDIQUE, categorie=AVANCE_RECOUVREMENT_JURIDIQUE)`
2. Si aucun tarif n'existe → Créer avec :
   - `coutUnitaire = 1000.00`
   - `statut = VALIDE`
   - `dateValidation = maintenant`

**Appel :** Dans `DossierServiceImpl` quand `typeRecouvrement` passe à `JURIDIQUE`

#### 2.3. Attestation de Carence (500 TND)

**Méthode :** `createTarifAttestationCarence(Long dossierId, String commentaire)`

**Logique :**
1. Vérifier si un tarif existe déjà pour `(dossierId, phase=JURIDIQUE, categorie=ATTESTATION_CARENCE)`
2. Si aucun tarif n'existe → Créer avec :
   - `coutUnitaire = 500.00`
   - `statut = EN_ATTENTE_VALIDATION` (doit être validé par le chef)
   - `commentaire = commentaire fourni`

**Appel :** Via un endpoint dédié ou depuis la page de validation

### Étape 3 : Implémenter le Calcul des Commissions

#### 3.1. Méthodes de Calcul

**Méthode 1 :** `calculerCommissionAmiable(Dossier dossier)`

```java
BigDecimal commissionAmiable = BigDecimal.ZERO;
if (dossier.getMontantRecouvrePhaseAmiable() != null && 
    dossier.getMontantRecouvrePhaseAmiable() > 0) {
    commissionAmiable = BigDecimal.valueOf(dossier.getMontantRecouvrePhaseAmiable())
        .multiply(TAUX_COMMISSION_AMIABLE);
}
return commissionAmiable;
```

**Méthode 2 :** `calculerCommissionJuridique(Dossier dossier)`

```java
BigDecimal commissionJuridique = BigDecimal.ZERO;
if (dossier.getMontantRecouvrePhaseJuridique() != null && 
    dossier.getMontantRecouvrePhaseJuridique() > 0) {
    commissionJuridique = BigDecimal.valueOf(dossier.getMontantRecouvrePhaseJuridique())
        .multiply(TAUX_COMMISSION_JURIDIQUE);
}
return commissionJuridique;
```

**Méthode 3 :** `calculerCommissionInterets(Dossier dossier)`

```java
BigDecimal commissionInterets = BigDecimal.ZERO;
if (dossier.getMontantInteretsRecouvres() != null && 
    dossier.getMontantInteretsRecouvres() > 0) {
    commissionInterets = BigDecimal.valueOf(dossier.getMontantInteretsRecouvres())
        .multiply(TAUX_COMMISSION_INTERETS);
}
return commissionInterets;
```

**Méthode 4 :** `calculerCommissions(Dossier dossier)`

```java
CommissionDTO commissions = new CommissionDTO();
commissions.setCommissionAmiable(calculerCommissionAmiable(dossier));
commissions.setCommissionJuridique(calculerCommissionJuridique(dossier));
commissions.setCommissionInterets(calculerCommissionInterets(dossier));
// Commission Relance : À implémenter si applicable
commissions.setTotalCommissions(
    commissions.getCommissionAmiable()
        .add(commissions.getCommissionJuridique())
        .add(commissions.getCommissionInterets())
);
return commissions;
```

#### 3.2. Intégration dans `calculerDetailFacture()`

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
   CommissionDTO commissions = calculerCommissions(dossier);
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
       .add(commissions.getCommissionAmiable())
       .add(commissions.getCommissionJuridique())
       .add(commissions.getCommissionInterets());
   ```

4. **Mettre à jour le DTO :**
   ```java
   dto.setCommissionAmiable(commissions.getCommissionAmiable());
   dto.setCommissionJuridique(commissions.getCommissionJuridique());
   // Si le DTO supporte commissionInterets :
   // dto.setCommissionInterets(commissions.getCommissionInterets());
   ```

### Étape 4 : Créer le DTO pour les Commissions

**Fichier :** `CommissionDTO.java` (nouveau)

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionDTO {
    private BigDecimal commissionAmiable;      // 12%
    private BigDecimal commissionJuridique;   // 15%
    private BigDecimal commissionRelance;     // 5% (si applicable)
    private BigDecimal commissionInterets;    // 50% (si applicable)
    private BigDecimal totalCommissions;
}
```

### Étape 5 : Modifier les Méthodes de Validation

#### 5.1. `DossierServiceImpl.validerDossier()`

**Action :** Après validation du dossier, appeler `createTarifCreationAutomatique()`

**Code :**
```java
// Après validation du dossier
tarifDossierService.createTarifCreationAutomatique(dossier);
```

#### 5.2. `EnquetteServiceImpl.validerEnquette()`

**Action :** Après validation de l'enquête, appeler `createTarifEnqueteAutomatique()`

**Code :**
```java
// Après validation de l'enquête
Enquette enqueteValidee = enquetteRepository.findById(enquetteId).orElseThrow(...);
tarifDossierService.createTarifEnqueteAutomatique(enqueteValidee);
```

#### 5.3. Passage en Phase Juridique

**Action :** Quand `typeRecouvrement` passe à `JURIDIQUE`, appeler `createAvanceRecouvrementJuridique()`

**Code :**
```java
// Dans DossierServiceImpl, quand typeRecouvrement = JURIDIQUE
if (dossier.getTypeRecouvrement() == TypeRecouvrement.JURIDIQUE) {
    tarifDossierService.createAvanceRecouvrementJuridique(dossier);
}
```

---

## 📋 Processus Complet de Calcul

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
- Le système calcule le total des frais validés :
  ```
  Total Frais Validés = 
    Frais Création (250 TND) +
    Frais Enquête (300 TND) +
    Avance Judiciaire (1000 TND) +
    Frais Audiences (saisis par chef) +
    Honoraires Avocat (saisis par chef) +
    Autres Frais Validés
  ```

**6. Recouvrement Effectif**
- Un montant est recouvré (phase amiable ou juridique)
- Le système enregistre le montant dans :
  - `montantRecouvrePhaseAmiable` (si recouvrement amiable)
  - `montantRecouvrePhaseJuridique` (si recouvrement juridique)
  - `montantInteretsRecouvres` (si intérêts recouvrés)

**7. Calcul des Commissions (AUTOMATIQUE)**
- Le système calcule automatiquement les commissions :
  ```
  Commission Amiable = montantRecouvrePhaseAmiable × 12%
  Commission Juridique = montantRecouvrePhaseJuridique × 15%
  Commission Intérêts = montantInteretsRecouvres × 50% (si applicable)
  ```

**8. Calcul Final de la Facture**
```
Total Frais Validés = [Somme de tous les tarifs validés]
Total Commissions = Commission Amiable + Commission Juridique + Commission Intérêts
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

**Implémentation :**
- Les commissions sont calculées dans `calculerDetailFacture()`
- Si aucun montant n'est recouvré → Commissions = 0
- Si un montant est recouvré → Commissions calculées automatiquement

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

**Vérification Frontend :**
- S'assurer que le frontend permet la saisie des tarifs d'audience
- S'assurer que le frontend permet la saisie des honoraires d'avocat
- S'assurer que le frontend envoie `audienceId` ou `avocatId` correctement

### 3. Avance sur Frais Judiciaire

**Important :** L'avance de 1000 TND est une **avance**, pas un frais définitif.

**Gestion actuelle :**
- L'avance est traitée comme un frais normal
- Elle est ajoutée aux frais initiaux
- Elle est incluse dans le total de la facture

**Gestion future (optionnelle) :**
- L'avance peut être déduite des frais finaux si le recouvrement est inférieur
- Ou elle peut être partiellement remboursée si le recouvrement est supérieur

**Recommandation :** Traiter l'avance comme un frais normal pour l'instant.

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

## 🔍 Vérifications Spécifiques

### 1. Vérifier les Champs dans Dossier

**Vérifier que `Dossier` a les champs suivants :**
- ✅ `montantRecouvrePhaseAmiable` (Double) - Existe
- ✅ `montantRecouvrePhaseJuridique` (Double) - Existe
- ❓ `montantInteretsRecouvres` (Double) - À vérifier

**Si `montantInteretsRecouvres` n'existe pas :**
- Ajouter le champ dans l'entité `Dossier`
- Créer une migration SQL pour ajouter la colonne

### 2. Vérifier le DTO DetailFactureDTO

**Vérifier que `DetailFactureDTO` a les champs suivants :**
- ✅ `commissionAmiable` (BigDecimal) - Existe
- ✅ `commissionJuridique` (BigDecimal) - Existe
- ❓ `commissionInterets` (BigDecimal) - À vérifier/ajouter
- ❓ `commissionRelance` (BigDecimal) - À ajouter si applicable

---

## 📝 Résumé

**Éléments de l'annexe à intégrer :**

1. ✅ **Prix fixes (créés automatiquement) :**
   - OUVERTURE_DOSSIER : 250 TND (lors validation dossier)
   - ENQUETE_PRECONTENTIEUSE : 300 TND (lors validation enquête)
   - AVANCE_RECOUVREMENT_JURIDIQUE : 1000 TND (lors passage phase juridique)
   - ATTESTATION_CARENCE : 500 TND (à la demande, manuel)

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
**Status :** ✅ Guide complet - Prêt pour implémentation
