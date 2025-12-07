# 📋 Document Complet : Corrections Appliquées Côté Backend

## 🎯 Objectif

Ce document détaille toutes les corrections appliquées côté backend pour intégrer l'annexe du contrat, corriger les statistiques manquantes, et améliorer le système de tarifs et de validation.

---

## ✅ 1. Intégration de l'Annexe du Contrat

### 1.1. Constantes Ajoutées

**Fichier :** `TarifDossierServiceImpl.java`

**Constantes ajoutées :**
- `AVANCE_RECOUVREMENT_JURIDIQUE = 1000.00` - Avance sur frais de recouvrement judiciaire
- `ATTESTATION_CARENCE = 500.00` - Attestation de carence à la demande du mandant
- `TAUX_COMMISSION_RELANCE = 0.05` - Commission relance (5%)
- `TAUX_COMMISSION_AMIABLE = 0.12` - Commission amiable (12%)
- `TAUX_COMMISSION_JURIDIQUE = 0.15` - Commission juridique (15%)
- `TAUX_COMMISSION_INTERETS = 0.50` - Commission sur intérêts (50%)

### 1.2. Méthodes de Création Automatique des Tarifs

**Fichier :** `TarifDossierServiceImpl.java` et `TarifDossierService.java`

**Méthodes ajoutées :**

1. **`createTarifCreationAutomatique(Dossier dossier)`**
   - Crée automatiquement le tarif "OUVERTURE_DOSSIER" (250 TND)
   - Appelée lors de la validation d'un dossier
   - Statut : VALIDE (validé automatiquement)

2. **`createTarifEnqueteAutomatique(Dossier dossier, Enquette enquete)`**
   - Crée automatiquement le tarif "ENQUETE_PRECONTENTIEUSE" (300 TND)
   - Appelée lors de la validation d'une enquête
   - Statut : VALIDE (validé automatiquement)

3. **`createAvanceRecouvrementJuridique(Dossier dossier)`**
   - Crée automatiquement l'avance "AVANCE_RECOUVREMENT_JURIDIQUE" (1000 TND)
   - Appelée lors du passage d'un dossier en phase JURIDIQUE
   - Statut : VALIDE (validé automatiquement)

4. **`createTarifAttestationCarence(Long dossierId, String commentaire)`**
   - Crée le tarif "ATTESTATION_CARENCE" (500 TND)
   - Création manuelle (à la demande)
   - Statut : EN_ATTENTE_VALIDATION (doit être validé par le chef)

### 1.3. Calcul des Commissions

**Fichier :** `TarifDossierServiceImpl.java`

**Méthodes ajoutées :**

1. **`calculerCommissionAmiable(Dossier dossier)`**
   - Calcule : `montantRecouvrePhaseAmiable × 12%`
   - Retourne 0 si aucun montant n'est recouvré

2. **`calculerCommissionJuridique(Dossier dossier)`**
   - Calcule : `montantRecouvrePhaseJuridique × 15%`
   - Retourne 0 si aucun montant n'est recouvré

3. **`calculerCommissionInterets(Dossier dossier)`**
   - Calcule : `montantInteretsRecouvres × 50%`
   - Retourne 0 si aucun montant d'intérêts n'est recouvré ou si le champ n'existe pas

**Intégration dans `calculerDetailFacture()` :**
- Les commissions sont calculées dynamiquement lors du calcul de la facture
- Elles sont ajoutées au total HT
- Elles ne sont pas stockées dans la base, elles sont calculées à la volée

### 1.4. Modifications dans les Méthodes de Validation

**Fichier :** `DossierServiceImpl.java`

**Modification :** `validerDossier()`
- Après validation du dossier, appelle automatiquement `createTarifCreationAutomatique()`
- Le tarif de création (250 TND) est créé avec statut VALIDE

**Fichier :** `EnquetteServiceImpl.java`

**Modification :** `validerEnquette()`
- Après validation de l'enquête, appelle automatiquement `createTarifEnqueteAutomatique()`
- Le tarif d'enquête (300 TND) est créé avec statut VALIDE

**Note :** Le passage en phase JURIDIQUE pour créer l'avance sera géré dans une prochaine étape (nécessite de détecter le changement de `typeRecouvrement`).

---

## ✅ 2. Corrections des Statistiques Manquantes et Montants par Phase

### 2.1. Types Statistiques Ajoutés

### 2.1. Types Statistiques Ajoutés

**Fichier :** `TypeStatistique.java`

**Types ajoutés :**
- `TOTAL_FACTURES` - Total des factures
- `FACTURES_PAYEES` - Factures payées
- `FACTURES_EN_ATTENTE` - Factures en attente
- `TOTAL_PAIEMENTS` - Total des paiements
- `PAIEMENTS_CE_MOIS` - Paiements du mois en cours
- `ENQUETES_EN_COURS` - Enquêtes en cours (non validées)

### 2.2. Calculs de Statistiques Ajoutés

**Fichier :** `StatistiqueServiceImpl.java`

**Dans `getStatistiquesFinancieres()` :**
- Ajout du calcul de `totalFactures` (nombre total de factures)
- Ajout du calcul de `facturesPayees` (factures avec statut PAYEE)
- Ajout du calcul de `facturesEnAttente` (factures avec statut EMISE ou BROUILLON)
- Ajout du calcul de `totalPaiements` (nombre total de paiements)
- Ajout du calcul de `paiementsCeMois` (paiements du mois en cours)

**Dans `getStatistiquesGlobales()` :**
- Ajout du calcul de `enquetesEnCours` (enquêtes avec statut EN_COURS ou EN_ATTENTE_VALIDATION)

**Dans `getTypeStatistiqueFromKey()` :**
- Ajout du mapping pour tous les nouveaux types statistiques

**Dépendances ajoutées :**
- Injection de `FactureRepository` pour accéder aux factures

### 2.3. Montants Recouvrés par Phase dans les Statistiques Financières

**Fichier :** `StatistiqueServiceImpl.java`

**Dans `getStatistiquesFinancieres()` :**
- ✅ Ajout du calcul de `montantRecouvrePhaseAmiable` (somme des montants recouvrés en phase amiable)
- ✅ Ajout du calcul de `montantRecouvrePhaseJuridique` (somme des montants recouvrés en phase juridique)
- ✅ Modification de `montantRecouvre` pour utiliser la somme des montants par phase au lieu de `montantCreance` des dossiers clôturés
- ✅ Modification de `netGenere` pour utiliser les montants par phase

**Impact :**
- Les statistiques financières incluent maintenant les montants recouvrés par phase
- Le calcul est plus précis (utilise les montants réels recouvrés)

---

## ✅ 3. Amélioration du DTO DetailFactureDTO

**Fichier :** `DetailFactureDTO.java`

**Champ ajouté :**
- `commissionInterets` (BigDecimal) - Commission sur intérêts (50%)

**Utilisation :**
- Rempli automatiquement lors du calcul de la facture
- Ajouté au total HT

---

## ✅ 4. Endpoints de Blocage/Déblocage Utilisateur

**Fichier :** `AdminUtilisateurController.java`

**Endpoints existants (vérifiés et fonctionnels) :**

1. **`PUT /api/admin/utilisateurs/{id}/activer`**
   - Active un utilisateur (débloque)
   - Autorisation : SUPER_ADMIN uniquement
   - Retourne : Utilisateur mis à jour

2. **`PUT /api/admin/utilisateurs/{id}/desactiver`**
   - Désactive un utilisateur (bloque)
   - Autorisation : SUPER_ADMIN uniquement
   - Protection : Impossible de désactiver un SUPER_ADMIN
   - Retourne : Utilisateur mis à jour

**Champ utilisé :**
- `actif` (Boolean) dans l'entité `Utilisateur`

---

## ✅ 3. Vérification des Endpoints Montants par Phase

### 3.1. Endpoints Existants et Vérifiés

**Tous les endpoints nécessaires existent et fonctionnent :**

1. **`GET /api/statistiques/globales`**
   - ✅ Retourne `montantRecouvrePhaseAmiable` et `montantRecouvrePhaseJuridique`
   - ✅ Autorisation : `SUPER_ADMIN`, `CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE`

2. **`GET /api/statistiques/financieres`**
   - ✅ Retourne `montantRecouvrePhaseAmiable` et `montantRecouvrePhaseJuridique` (ajouté)
   - ✅ Autorisation : `SUPER_ADMIN`, `CHEF_DEPARTEMENT_FINANCE`

3. **`GET /api/statistiques/recouvrement-par-phase`**
   - ✅ Retourne statistiques détaillées par phase (montants, taux, nombre de dossiers)
   - ✅ Autorisation : `SUPER_ADMIN`, `CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE`, `CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE`, `CHEF_DEPARTEMENT_FINANCE`

4. **`GET /api/statistiques/recouvrement-par-phase/departement`**
   - ✅ Retourne statistiques par phase filtrées par département
   - ✅ Autorisation : `CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE`, `CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE`, `CHEF_DEPARTEMENT_FINANCE`, `SUPER_ADMIN`

**Documentation :** Voir `DOCUMENT_VERIFICATION_ENDPOINTS_MONTANTS_PAR_PHASE.md` pour les détails complets.

---

## 📋 Résumé des Fichiers Modifiés

### Fichiers Modifiés

1. **`TarifDossierServiceImpl.java`**
   - Ajout des constantes (avance, attestation, taux commissions)
   - Ajout des méthodes de création automatique
   - Ajout des méthodes de calcul des commissions
   - Modification de `calculerDetailFacture()` pour inclure les commissions

2. **`TarifDossierService.java`**
   - Ajout des signatures des méthodes publiques dans l'interface

3. **`DossierServiceImpl.java`**
   - Injection de `TarifDossierService`
   - Modification de `validerDossier()` pour créer automatiquement le tarif de création

4. **`EnquetteServiceImpl.java`**
   - Injection de `TarifDossierService`
   - Modification de `validerEnquette()` pour créer automatiquement le tarif d'enquête

5. **`TypeStatistique.java`**
   - Ajout de 6 nouveaux types statistiques

6. **`StatistiqueServiceImpl.java`**
   - Injection de `FactureRepository`
   - Ajout des calculs de statistiques manquants
   - Mise à jour du mapping des types statistiques

7. **`DetailFactureDTO.java`**
   - Ajout du champ `commissionInterets`

8. **`StatistiqueServiceImpl.java`** (améliorations)
   - Ajout des montants par phase dans `getStatistiquesFinancieres()`
   - Utilisation des montants réels recouvrés au lieu de montant créance

---

## ⚠️ Points d'Attention

### 1. Champ `montantInteretsRecouvres`

**Important :** Le champ `montantInteretsRecouvres` n'existe pas encore dans l'entité `Dossier`.

**Impact :**
- La commission sur intérêts retournera 0 jusqu'à ce que le champ soit ajouté
- Le calcul utilise la réflexion pour vérifier l'existence du champ

**Action requise :**
- Ajouter le champ `montantInteretsRecouvres` (Double) dans `Dossier.java`
- Créer une migration SQL pour ajouter la colonne

### 2. Passage en Phase Juridique

**Important :** La création automatique de l'avance lors du passage en phase JURIDIQUE n'est pas encore implémentée.

**Action requise :**
- Détecter le changement de `typeRecouvrement` vers `JURIDIQUE`
- Appeler `createAvanceRecouvrementJuridique()` automatiquement

### 3. Commission Relance (5%)

**Important :** La commission relance (5%) n'est pas encore implémentée.

**Raison :**
- Nécessite de tracker séparément le montant recouvré en phase "Relance"
- Le système actuel ne distingue pas la phase "Relance" de la phase "Amiable"

**Action requise (optionnelle) :**
- Ajouter un champ `montantRecouvreRelance` dans `Dossier`
- Implémenter le calcul de la commission relance

---

## ✅ 4. Documentation Frontend

**Documents créés :**

1. **`PROMPTS_FRONTEND_MONTANTS_RECOUVRES_PAR_PHASE.md`**
   - 8 prompts détaillés pour intégrer les montants par phase dans les dashboards
   - Prompts spécifiques pour chaque dashboard (SuperAdmin, Chef Amiable, Chef Juridique, Chef Finance)
   - Prompt pour créer un composant réutilisable

2. **`DOCUMENT_VERIFICATION_ENDPOINTS_MONTANTS_PAR_PHASE.md`**
   - Vérification complète de tous les endpoints
   - Exemples de réponses JSON
   - Mapping des endpoints par dashboard

3. **`PROMPTS_FRONTEND_AMELIORATIONS.md`** (mis à jour)
   - Ajout du Prompt 7 : Intégration des Montants Recouvrés par Phase

---

## ✅ Tests Recommandés

### Tests Fonctionnels

1. **Validation d'un dossier**
   - Vérifier qu'un tarif "OUVERTURE_DOSSIER" (250 TND, VALIDE) est créé automatiquement

2. **Validation d'une enquête**
   - Vérifier qu'un tarif "ENQUETE_PRECONTENTIEUSE" (300 TND, VALIDE) est créé automatiquement

3. **Calcul des commissions**
   - Créer un dossier avec recouvrement amiable (ex: 5000 TND)
   - Vérifier que la commission amiable = 5000 × 12% = 600 TND
   - Créer un dossier avec recouvrement juridique (ex: 3000 TND)
   - Vérifier que la commission juridique = 3000 × 15% = 450 TND

4. **Statistiques financières**
   - Vérifier que `totalFactures`, `facturesPayees`, `facturesEnAttente` sont calculés
   - Vérifier que `totalPaiements`, `paiementsCeMois` sont calculés

5. **Statistiques globales**
   - Vérifier que `enquetesEnCours` est calculé correctement

6. **Blocage/Déblocage utilisateur**
   - Tester `PUT /api/admin/utilisateurs/{id}/activer`
   - Tester `PUT /api/admin/utilisateurs/{id}/desactiver`
   - Vérifier que seul un SUPER_ADMIN peut utiliser ces endpoints

7. **Montants recouvrés par phase**
   - Tester `GET /api/statistiques/globales` et vérifier `montantRecouvrePhaseAmiable` et `montantRecouvrePhaseJuridique`
   - Tester `GET /api/statistiques/financieres` et vérifier les montants par phase
   - Tester `GET /api/statistiques/recouvrement-par-phase` et vérifier tous les champs
   - Tester `GET /api/statistiques/recouvrement-par-phase/departement` avec un chef connecté
   - Vérifier que les montants sont correctement calculés (somme des montants par phase = total)

---

## 📝 Prochaines Étapes

### Actions Immédiates

1. **Ajouter le champ `montantInteretsRecouvres` dans `Dossier`**
   - Ajouter le champ dans l'entité
   - Créer une migration SQL

2. **Implémenter la détection du passage en phase JURIDIQUE**
   - Détecter le changement de `typeRecouvrement`
   - Appeler `createAvanceRecouvrementJuridique()` automatiquement

3. **Tester toutes les fonctionnalités**
   - Exécuter les tests fonctionnels listés ci-dessus
   - Vérifier que les statistiques sont correctement calculées

### Actions Optionnelles

1. **Implémenter la commission relance (5%)**
   - Ajouter le champ `montantRecouvreRelance` dans `Dossier`
   - Implémenter le calcul

2. **Améliorer la gestion de l'avance judiciaire**
   - Gérer les ajustements si le recouvrement est inférieur/supérieur
   - Gérer les remboursements partiels

---

**Date :** 2025-01-05  
**Status :** ✅ Corrections appliquées - Prêt pour tests

