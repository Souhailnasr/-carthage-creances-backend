# 🔄 Intégration Clôture et Archivage de Dossier après Paiement Complet

## 📋 Vue d'Ensemble

Ce document explique l'intégration complète de la fonctionnalité de **clôture et archivage automatique** d'un dossier une fois que sa facture est entièrement payée.

---

## ✅ Modifications Appliquées

### 1. Modifications des Entités

#### 1.1. Enum `StatutTarif`

**Fichier** : `src/main/java/projet/carthagecreance_backend/Entity/StatutTarif.java`

**Ajouts** :
- ✅ `FACTURE` : Tarif inclus dans une facture
- ✅ `PAYE` : Tarif payé (facture entièrement payée)

#### 1.2. Entité `Dossier`

**Fichier** : `src/main/java/projet/carthagecreance_backend/Entity/Dossier.java`

**Ajouts** :
```java
@Column(name = "archive")
@Builder.Default
private Boolean archive = false;

@Column(name = "date_archivage")
@Temporal(TemporalType.TIMESTAMP)
private java.util.Date dateArchivage;
```

**Note** : `dateCloture` existait déjà.

---

### 2. Nouveaux DTOs

#### 2.1. `SoldeFactureDTO`

**Fichier** : `src/main/java/projet/carthagecreance_backend/DTO/SoldeFactureDTO.java`

**Champs** :
- `factureId` : ID de la facture
- `montantTTC` : Montant TTC de la facture
- `totalPaiementsValides` : Total des paiements validés
- `soldeRestant` : Solde restant à payer
- `estEntierementPayee` : Boolean indiquant si la facture est entièrement payée

#### 2.2. `PeutEtreClotureDTO`

**Fichier** : `src/main/java/projet/carthagecreance_backend/DTO/PeutEtreClotureDTO.java`

**Champs** :
- `peutEtreCloture` : Boolean indiquant si le dossier peut être clôturé
- `raisons` : Liste des raisons si le dossier ne peut pas être clôturé
- `factureId` : ID de la facture
- `montantTTC` : Montant TTC
- `totalPaiementsValides` : Total payé
- `soldeRestant` : Solde restant
- `statutFacture` : Statut de la facture

#### 2.3. `ClotureDossierDTO`

**Fichier** : `src/main/java/projet/carthagecreance_backend/DTO/ClotureDossierDTO.java`

**Champs** :
- `dossierId` : ID du dossier
- `statut` : Statut du dossier (CLOTURE)
- `dateCloture` : Date de clôture
- `archive` : Boolean indiquant si le dossier est archivé
- `dateArchivage` : Date d'archivage
- `message` : Message de confirmation

---

### 3. Modifications des Services

#### 3.1. `FactureService` et `FactureServiceImpl`

**Nouvelles méthodes** :

1. **`calculerSoldeRestant(Long factureId)`** :
   - Calcule le solde restant d'une facture
   - Retourne un `SoldeFactureDTO`

2. **`verifierEtMettreAJourStatutFacture(Long factureId)`** :
   - Vérifie si la facture est entièrement payée
   - Met à jour le statut à `PAYEE` si le solde est <= 0
   - Met à jour tous les tarifs en statut `PAYE`

**Méthode privée** :
- `mettreAJourStatutFrais(Long dossierId)` : Met à jour tous les tarifs du dossier en `PAYE`

#### 3.2. `PaiementService` et `PaiementServiceImpl`

**Modification de `validerPaiement()`** :
- ✅ Après validation d'un paiement, appelle automatiquement `factureService.verifierEtMettreAJourStatutFacture()`
- ✅ Met à jour automatiquement le statut de la facture si elle est entièrement payée

#### 3.3. `DossierService` et `DossierServiceImpl`

**Nouvelles méthodes** :

1. **`peutEtreCloture(Long dossierId)`** :
   - Vérifie si un dossier peut être clôturé
   - Vérifie que la facture est `PAYEE`
   - Vérifie que le solde est 0
   - Retourne un `PeutEtreClotureDTO` avec les raisons si applicable

2. **`cloturerEtArchiver(Long dossierId)`** :
   - Clôture et archive un dossier
   - Vérifie les préconditions via `peutEtreCloture()`
   - Met à jour le dossier (statut, archive, dates)
   - Met à jour tous les tarifs en `PAYE`
   - Retourne un `ClotureDossierDTO`

---

### 4. Nouveaux Endpoints

#### 4.1. `FactureController`

**GET** `/api/factures/{factureId}/solde`
- Calcule le solde restant d'une facture
- **Réponse** : `SoldeFactureDTO`

**PUT** `/api/factures/{factureId}/verifier-statut`
- Vérifie et met à jour le statut de la facture
- **Réponse** : `FactureDTO` mis à jour

#### 4.2. `DossierController`

**GET** `/api/dossiers/{dossierId}/peut-etre-cloture`
- Vérifie si un dossier peut être clôturé
- **Réponse** : `PeutEtreClotureDTO`

**POST** `/api/dossiers/{dossierId}/cloturer-et-archiver`
- Clôture et archive un dossier
- **Préconditions** : Facture payée, solde = 0
- **Réponse** : `ClotureDossierDTO`

---

## 🔄 Workflow Complet

### Scénario : Clôture et Archivage après Paiement Complet

```
1. Facture émise (statut: EMISE)
   └─→ Montant TTC : 10,000 TND

2. Paiements enregistrés et validés
   └─→ POST /api/paiements
   └─→ PUT /api/paiements/{id}/valider
   └─→ Total payé : 10,000 TND

3. ✅ AUTOMATIQUE : Vérification du statut de la facture
   └─→ Appelé automatiquement dans PaiementServiceImpl.validerPaiement()
   └─→ factureService.verifierEtMettreAJourStatutFacture()
   └─→ Si solde <= 0 :
       ├─→ Statut facture → PAYEE ✅
       └─→ Tous les tarifs → PAYE ✅

4. Chef Financier vérifie si le dossier peut être clôturé
   └─→ GET /api/dossiers/{dossierId}/peut-etre-cloture
   └─→ Réponse : { peutEtreCloture: true, raisons: [] }

5. Chef Financier clique sur "Clôturer et Archiver"
   └─→ POST /api/dossiers/{dossierId}/cloturer-et-archiver

6. Backend clôture et archive
   └─→ Vérifie les préconditions
   └─→ Statut dossier → CLOTURE
   └─→ archive → true
   └─→ dateCloture → maintenant
   └─→ dateArchivage → maintenant
   └─→ Tous les tarifs → PAYE (si pas déjà fait)

7. Réponse de succès
   └─→ ClotureDossierDTO avec message de confirmation
```

---

## 🎯 Avantages de cette Approche

### ✅ Mise à Jour Automatique

- **Pas d'intervention manuelle** nécessaire pour mettre à jour le statut de la facture
- **Cohérent** avec le workflow existant
- **Non bloquant** : Si la vérification échoue, le paiement reste validé

### ✅ Vérifications Multiples

- **Préconditions vérifiées** avant clôture
- **Messages d'erreur clairs** si le dossier ne peut pas être clôturé
- **Sécurité** : Vérification des droits utilisateur (à implémenter via Spring Security)

### ✅ Transaction Atomique

- **`@Transactional`** sur `cloturerEtArchiver()`
- **Soit tout réussit, soit rien n'est modifié**
- **Cohérence garantie** des données

---

## 📊 Données Utilisées

Lors de la clôture et archivage, le système utilise :

| Donnée | Source | Disponibilité |
|--------|--------|---------------|
| **Facture** | `FactureRepository.findByDossierId()` | ✅ Toujours |
| **Paiements** | `PaiementRepository.findByFactureIdAndStatut()` | ⚠️ Peut être vide |
| **Tarifs** | `TarifDossierRepository.findByDossierId()` | ⚠️ Peut être vide |
| **Finance** | `FinanceRepository.findByDossierId()` | ⚠️ Peut être null |

---

## 🔍 Gestion des Erreurs

### Stratégie : Vérifications Préalables

Si les préconditions ne sont pas remplies :

1. ✅ **Vérification avant clôture** : `peutEtreCloture()` retourne les raisons
2. ✅ **Exception explicite** : `RuntimeException` avec message détaillé
3. ✅ **Logs détaillés** : Toutes les étapes sont loggées
4. ✅ **Transaction rollback** : En cas d'erreur, rien n'est modifié

**Code** :
```java
PeutEtreClotureDTO verification = peutEtreCloture(dossierId);
if (!verification.getPeutEtreCloture()) {
    throw new RuntimeException("Le dossier ne peut pas être clôturé: " + 
        String.join(", ", verification.getRaisons()));
}
```

---

## 🧪 Tests

### Test 1 : Calcul du Solde Restant

**Requête** :
```
GET http://localhost:8089/carthage-creance/api/factures/4/solde
Authorization: Bearer {token}
```

**Réponse attendue** :
```json
{
  "factureId": 4,
  "montantTTC": 934.15,
  "totalPaiementsValides": 500.00,
  "soldeRestant": 434.15,
  "estEntierementPayee": false
}
```

### Test 2 : Vérification Préconditions Clôture

**Requête** :
```
GET http://localhost:8089/carthage-creance/api/dossiers/42/peut-etre-cloture
Authorization: Bearer {token}
```

**Réponse attendue** (si facture payée) :
```json
{
  "peutEtreCloture": true,
  "raisons": [],
  "factureId": 4,
  "montantTTC": 934.15,
  "totalPaiementsValides": 934.15,
  "soldeRestant": 0.00,
  "statutFacture": "PAYEE"
}
```

**Réponse attendue** (si facture non payée) :
```json
{
  "peutEtreCloture": false,
  "raisons": [
    "La facture n'est pas entièrement payée",
    "Il reste un solde de 434.15 TND à payer"
  ],
  "factureId": 4,
  "montantTTC": 934.15,
  "totalPaiementsValides": 500.00,
  "soldeRestant": 434.15,
  "statutFacture": "EMISE"
}
```

### Test 3 : Clôture et Archivage

**Requête** :
```
POST http://localhost:8089/carthage-creance/api/dossiers/42/cloturer-et-archiver
Authorization: Bearer {token}
```

**Réponse attendue** :
```json
{
  "dossierId": 42,
  "statut": "CLOTURE",
  "dateCloture": "2025-12-02T10:30:00",
  "archive": true,
  "dateArchivage": "2025-12-02T10:30:00",
  "message": "Dossier clôturé et archivé avec succès"
}
```

### Test 4 : Mise à Jour Automatique après Validation Paiement

**Requête** :
```
PUT http://localhost:8089/carthage-creance/api/paiements/{id}/valider
Authorization: Bearer {token}
```

**Vérifications** :
1. ✅ Le paiement est validé
2. ✅ Si le solde devient 0, la facture passe en `PAYEE`
3. ✅ Tous les tarifs passent en `PAYE`
4. ✅ Les logs montrent la mise à jour automatique

---

## 📝 Logs

### Logs de Succès

```
INFO - Validation du paiement ID: 10
INFO - Statut de la facture 4 vérifié après validation du paiement 10
INFO - Mise à jour du statut de la facture 4 à PAYEE
INFO - 5 tarifs mis à jour en statut PAYE pour le dossier 42
INFO - Vérification si le dossier 42 peut être clôturé
INFO - Clôture et archivage du dossier 42
INFO - Dossier 42 clôturé et archivé avec succès
```

### Logs d'Erreur

```
WARN - Erreur lors de la vérification du statut de la facture: Facture non trouvée
ERROR - Le dossier ne peut pas être clôturé: La facture n'est pas entièrement payée, Il reste un solde de 434.15 TND à payer
```

---

## ⚠️ Points d'Attention

### 1. **Permissions Utilisateur**

⚠️ **À implémenter** : Vérifier que l'utilisateur a le rôle `CHEF_DEPARTEMENT_FINANCE` avant de permettre la clôture.

**Suggestion** :
```java
@PreAuthorize("hasRole('CHEF_DEPARTEMENT_FINANCE')")
@PostMapping("/{dossierId}/cloturer-et-archiver")
public ResponseEntity<?> cloturerEtArchiver(...) {
    // ...
}
```

### 2. **Historique et Traçabilité**

⚠️ **Recommandation** : Conserver un historique complet :
- Date de clôture
- Utilisateur qui a clôturé
- Raison de clôture (optionnel)
- État du dossier au moment de la clôture

### 3. **Archivage vs Suppression**

⚠️ **Important** : **Archiver** ne signifie **PAS supprimer** :
- Les données doivent rester accessibles
- Créer une vue "Archives" pour consulter les dossiers archivés
- Les statistiques doivent inclure les dossiers archivés

### 4. **Factures Multiples**

⚠️ **Cas à gérer** : Si un dossier a plusieurs factures :
- Actuellement, on prend la dernière facture
- **Recommandation** : Vérifier que **toutes** les factures sont payées avant de clôturer

---

## ✨ Résumé

### Ce qui a été ajouté :

1. ✅ **Champs d'archivage** dans `Dossier` (`archive`, `dateArchivage`)
2. ✅ **Statut `PAYE`** dans `StatutTarif`
3. ✅ **3 nouveaux DTOs** (`SoldeFactureDTO`, `PeutEtreClotureDTO`, `ClotureDossierDTO`)
4. ✅ **Méthodes de calcul** dans `FactureService` (`calculerSoldeRestant`, `verifierEtMettreAJourStatutFacture`)
5. ✅ **Mise à jour automatique** dans `PaiementService` après validation
6. ✅ **Méthodes de clôture** dans `DossierService` (`peutEtreCloture`, `cloturerEtArchiver`)
7. ✅ **4 nouveaux endpoints** (2 dans `FactureController`, 2 dans `DossierController`)

### Résultat :

- ✅ La facture est mise à jour automatiquement après validation d'un paiement
- ✅ Le chef financier peut vérifier si un dossier peut être clôturé
- ✅ Le chef financier peut clôturer et archiver un dossier en un clic
- ✅ Tous les tarifs sont mis à jour en `PAYE` automatiquement
- ✅ Le workflow est cohérent et transactionnel

---

## 📚 Références

- `FactureServiceImpl.java` : Service modifié
- `PaiementServiceImpl.java` : Service modifié avec vérification automatique
- `DossierServiceImpl.java` : Service avec nouvelles méthodes de clôture
- `FactureController.java` : Nouveaux endpoints
- `DossierController.java` : Nouveaux endpoints

**Date de modification** : 2025-12-02  
**Statut** : ✅ Implémenté et prêt pour tests

