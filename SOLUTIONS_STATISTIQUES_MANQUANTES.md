# 🔧 Solutions : Statistiques Manquantes (Factures, Paiements, Enquêtes)

## 🎯 Problèmes Identifiés

### Problème 1 : Statistiques Factures et Paiements à 0

**Symptômes :**
- Factures : En attente: 0, Payées: 0, Total: 0
- Paiements : Ce mois: 0, Total: 0
- Ces statistiques ne sont **PAS** dans la table `statistiques`
- Ces statistiques ne sont **PAS** calculées dans `getStatistiquesFinancieres()`

**Cause :**
- L'endpoint `/api/statistiques/financieres` ne calcule **PAS** les statistiques de factures et paiements
- Il calcule seulement : `montantRecouvre`, `montantEnCours`, `totalFraisEngages`, `fraisRecuperes`, `netGenere`

---

### Problème 2 : Statistiques Enquêtes Incomplètes

**Symptômes :**
- Capture 2 montre : "En cours: -3" (valeur négative incorrecte)
- "Total: 0" (alors que "Complétées: 3")
- Il manque la statistique "Enquêtes en cours" (non validées)

**Cause :**
- Le backend calcule seulement `totalEnquetes` et `enquetesCompletees`
- Il manque `enquetesEnCours` = enquêtes non validées
- Le frontend calcule probablement : `enquetesEnCours = totalEnquetes - enquetesCompletees` ce qui donne -3 (erreur de logique)

---

## ✅ Solutions Proposées

---

## SOLUTION 1 : Ajouter Statistiques Factures et Paiements

### 📋 Analyse du Code Actuel

**Fichier :** `StatistiqueServiceImpl.java`  
**Méthode :** `getStatistiquesFinancieres()` (ligne 551-587)

**Ce qui est calculé actuellement :**
- `montantRecouvre` : Montant recouvré (dossiers clôturés)
- `montantEnCours` : Montant en cours (dossiers non clôturés)
- `totalFraisEngages` : Total frais engagés (depuis TarifDossier)
- `fraisRecuperes` : Frais récupérés (paiements validés)
- `netGenere` : Net généré

**Ce qui MANQUE :**
- ❌ `totalFactures` : Nombre total de factures
- ❌ `facturesPayees` : Nombre de factures payées (statut = PAYEE)
- ❌ `facturesEnAttente` : Nombre de factures en attente (statut = EN_ATTENTE ou BROUILLON)
- ❌ `totalPaiements` : Nombre total de paiements
- ❌ `paiementsCeMois` : Nombre de paiements ce mois

### 🔧 Solution à Appliquer

**Fichier à modifier :** `src/main/java/projet/carthagecreance_backend/Service/Impl/StatistiqueServiceImpl.java`

**Méthode :** `getStatistiquesFinancieres()`

**Ajouts nécessaires :**

1. **Injecter les repositories manquants :**
   ```java
   @Autowired
   private FactureRepository factureRepository;
   
   @Autowired
   private PaiementRepository paiementRepository;
   ```

2. **Ajouter les calculs de statistiques factures :**
   ```java
   // Statistiques des factures
   List<Facture> toutesFactures = factureRepository.findAll();
   long totalFactures = toutesFactures.size();
   long facturesPayees = toutesFactures.stream()
           .filter(f -> f.getStatut() == FactureStatut.PAYEE)
           .count();
   long facturesEnAttente = toutesFactures.stream()
           .filter(f -> f.getStatut() == FactureStatut.BROUILLON || 
                       f.getStatut() == FactureStatut.EMISE ||
                       f.getStatut() == FactureStatut.EN_RETARD)
           .count();
   
   stats.put("totalFactures", totalFactures);
   stats.put("facturesPayees", facturesPayees);
   stats.put("facturesEnAttente", facturesEnAttente);
   ```

3. **Ajouter les calculs de statistiques paiements :**
   ```java
   // Statistiques des paiements
   List<Paiement> tousPaiements = paiementRepository.findAll();
   long totalPaiements = tousPaiements.size();
   
   // Paiements ce mois
   LocalDate debutMois = LocalDate.now().withDayOfMonth(1);
   long paiementsCeMois = tousPaiements.stream()
           .filter(p -> p.getDatePaiement() != null && 
                       p.getDatePaiement().isAfter(debutMois.minusDays(1)))
           .count();
   
   stats.put("totalPaiements", totalPaiements);
   stats.put("paiementsCeMois", paiementsCeMois);
   ```

4. **Ajouter les types dans l'enum TypeStatistique :**
   ```java
   // Dans TypeStatistique.java, ajouter :
   TOTAL_FACTURES,
   FACTURES_PAYEES,
   FACTURES_EN_ATTENTE,
   TOTAL_PAIEMENTS,
   PAIEMENTS_CE_MOIS
   ```

5. **Ajouter le mapping dans getTypeStatistiqueFromKey() :**
   ```java
   // Dans StatistiqueServiceImpl.java, méthode getTypeStatistiqueFromKey()
   mapping.put("totalFactures", TypeStatistique.TOTAL_FACTURES);
   mapping.put("facturesPayees", TypeStatistique.FACTURES_PAYEES);
   mapping.put("facturesEnAttente", TypeStatistique.FACTURES_EN_ATTENTE);
   mapping.put("totalPaiements", TypeStatistique.TOTAL_PAIEMENTS);
   mapping.put("paiementsCeMois", TypeStatistique.PAIEMENTS_CE_MOIS);
   ```

### 📝 Prompt pour le Développeur

```
Je dois ajouter les statistiques de factures et paiements dans la méthode getStatistiquesFinancieres().

**Contexte :**
- Fichier : StatistiqueServiceImpl.java
- Méthode : getStatistiquesFinancieres() (ligne 551)
- Actuellement, seules les statistiques de montants sont calculées
- Il manque les statistiques de factures et paiements

**À faire :**

1. Injecter FactureRepository et PaiementRepository (si pas déjà fait)

2. Ajouter les calculs de statistiques factures :
   - totalFactures : Nombre total de factures
   - facturesPayees : Factures avec statut PAYEE
   - facturesEnAttente : Factures avec statut EN_ATTENTE ou BROUILLON

3. Ajouter les calculs de statistiques paiements :
   - totalPaiements : Nombre total de paiements
   - paiementsCeMois : Paiements créés ce mois (datePaiement >= début du mois)

4. Ajouter ces statistiques dans le Map retourné

5. Ajouter les nouveaux types dans l'enum TypeStatistique :
   - TOTAL_FACTURES
   - FACTURES_PAYEES
   - FACTURES_EN_ATTENTE
   - TOTAL_PAIEMENTS
   - PAIEMENTS_CE_MOIS

6. Ajouter le mapping dans getTypeStatistiqueFromKey()

**Vérifications :**
- Utiliser FactureStatut.PAYEE pour factures payées
- Utiliser FactureStatut.BROUILLON, EMISE, EN_RETARD pour factures en attente
- Filtrer les paiements par datePaiement pour "ce mois"
- Gérer les valeurs null (datePaiement peut être null)
- Exclure les factures ANNULEE du total si nécessaire
```

---

## SOLUTION 2 : Corriger Statistiques Enquêtes

### 📋 Analyse du Code Actuel

**Fichier :** `StatistiqueServiceImpl.java`  
**Méthode :** `getStatistiquesGlobales()` (ligne 165-172)

**Ce qui est calculé actuellement :**
```java
long totalEnquetes = toutesEnquetes.size();
long enquetesCompletees = toutesEnquetes.stream()
        .filter(e -> e.getStatut() == Statut.VALIDE)
        .count();
stats.put("totalEnquetes", totalEnquetes);
stats.put("enquetesCompletees", enquetesCompletees);
```

**Problème :**
- ❌ Il manque `enquetesEnCours` : Enquêtes non validées (statut != VALIDE)
- Le frontend calcule probablement : `enquetesEnCours = totalEnquetes - enquetesCompletees`
- Si `totalEnquetes = 0` et `enquetesCompletees = 3`, alors `enquetesEnCours = -3` (erreur)

### 🔧 Solution à Appliquer

**Fichier à modifier :** `src/main/java/projet/carthagecreance_backend/Service/Impl/StatistiqueServiceImpl.java`

**Méthode :** `getStatistiquesGlobales()`

**Ajout nécessaire :**

```java
// Statistiques des enquêtes
List<Enquette> toutesEnquetes = enquetteRepository.findAll();
long totalEnquetes = toutesEnquetes.size();
long enquetesCompletees = toutesEnquetes.stream()
        .filter(e -> e.getStatut() == Statut.VALIDE)
        .count();
long enquetesEnCours = toutesEnquetes.stream()
        .filter(e -> e.getStatut() != Statut.VALIDE)  // ✅ NOUVEAU : Non validées
        .count();

stats.put("totalEnquetes", totalEnquetes);
stats.put("enquetesCompletees", enquetesCompletees);
stats.put("enquetesEnCours", enquetesEnCours);  // ✅ NOUVEAU
```

**Ajouter dans l'enum TypeStatistique :**
```java
ENQUETES_EN_COURS  // ✅ NOUVEAU
```

**Ajouter le mapping :**
```java
mapping.put("enquetesEnCours", TypeStatistique.ENQUETES_EN_COURS);
```

### 📝 Prompt pour le Développeur

```
Je dois corriger les statistiques d'enquêtes pour ajouter "enquetesEnCours".

**Contexte :**
- Fichier : StatistiqueServiceImpl.java
- Méthode : getStatistiquesGlobales() (ligne 165-172)
- Actuellement, seulement totalEnquetes et enquetesCompletees sont calculés
- Il manque enquetesEnCours (enquêtes non validées)

**Problème actuel :**
- Le frontend calcule enquetesEnCours = totalEnquetes - enquetesCompletees
- Cela donne des valeurs négatives incorrectes
- Il faut calculer explicitement enquetesEnCours côté backend

**À faire :**

1. Ajouter le calcul de enquetesEnCours :
   - Filtrer les enquêtes avec statut != VALIDE
   - Compter ces enquêtes

2. Ajouter dans le Map retourné :
   - stats.put("enquetesEnCours", enquetesEnCours);

3. Ajouter dans l'enum TypeStatistique :
   - ENQUETES_EN_COURS

4. Ajouter le mapping dans getTypeStatistiqueFromKey() :
   - mapping.put("enquetesEnCours", TypeStatistique.ENQUETES_EN_COURS);

**Vérifications :**
- Utiliser Statut.VALIDE pour différencier complétées vs en cours
- Gérer les valeurs null (statut peut être null)
- S'assurer que totalEnquetes = enquetesCompletees + enquetesEnCours
```

---

## SOLUTION 3 : Vérifier le Frontend

### 📋 Problème Frontend - Enquêtes

**Problème identifié :**
- Le frontend calcule probablement `enquetesEnCours = totalEnquetes - enquetesCompletees`
- Si `totalEnquetes = 0` (non retourné ou mal mappé) et `enquetesCompletees = 3`, alors `-3`

**Solution Frontend :**

1. **Vérifier que `totalEnquetes` est bien retourné par le backend**
2. **Ne pas calculer `enquetesEnCours` côté frontend**
3. **Utiliser directement `enquetesEnCours` retourné par le backend** (après correction)

### 📝 Prompt pour le Frontend

```
Je dois corriger l'affichage des statistiques d'enquêtes dans le frontend.

**Problème :**
- "En cours" affiche -3 (valeur négative incorrecte)
- Le calcul frontend est probablement : enquetesEnCours = totalEnquetes - enquetesCompletees

**Solution :**

1. Vérifier que le backend retourne bien :
   - totalEnquetes
   - enquetesCompletees
   - enquetesEnCours (après correction backend)

2. Ne PAS calculer enquetesEnCours côté frontend
   - Utiliser directement la valeur retournée par le backend

3. Gérer les valeurs null/undefined :
   - Si enquetesEnCours est null/undefined, afficher 0
   - Ne pas faire de calculs avec des valeurs null

4. Vérifier le mapping des données :
   - S'assurer que les clés JSON correspondent aux propriétés TypeScript
   - Vérifier que totalEnquetes est bien mappé (pas 0 si des enquêtes existent)
```

---

## 📋 Checklist des Corrections

### Backend - Statistiques Financières

- [ ] Injecter `FactureRepository` dans `StatistiqueServiceImpl`
- [ ] Injecter `PaiementRepository` dans `StatistiqueServiceImpl`
- [ ] Ajouter calcul `totalFactures` dans `getStatistiquesFinancieres()`
- [ ] Ajouter calcul `facturesPayees` dans `getStatistiquesFinancieres()`
- [ ] Ajouter calcul `facturesEnAttente` dans `getStatistiquesFinancieres()`
- [ ] Ajouter calcul `totalPaiements` dans `getStatistiquesFinancieres()`
- [ ] Ajouter calcul `paiementsCeMois` dans `getStatistiquesFinancieres()`
- [ ] Ajouter les 5 nouveaux types dans `TypeStatistique` enum
- [ ] Ajouter le mapping dans `getTypeStatistiqueFromKey()`

### Backend - Statistiques Enquêtes

- [ ] Ajouter calcul `enquetesEnCours` dans `getStatistiquesGlobales()`
- [ ] Ajouter `ENQUETES_EN_COURS` dans `TypeStatistique` enum
- [ ] Ajouter le mapping dans `getTypeStatistiqueFromKey()`
- [ ] Vérifier que `totalEnquetes` est bien calculé (pas 0 si des enquêtes existent)

### Frontend - Statistiques Enquêtes

- [ ] Vérifier que `totalEnquetes` est bien reçu du backend
- [ ] Ne pas calculer `enquetesEnCours` côté frontend
- [ ] Utiliser directement `enquetesEnCours` du backend
- [ ] Gérer les valeurs null/undefined (afficher 0)

### Frontend - Statistiques Financières

- [ ] Vérifier que les nouvelles statistiques sont bien reçues
- [ ] Afficher `totalFactures`, `facturesPayees`, `facturesEnAttente`
- [ ] Afficher `totalPaiements`, `paiementsCeMois`
- [ ] Gérer les valeurs null/undefined (afficher 0)

---

## 🔍 Vérifications à Effectuer

### 1. Vérifier les Données dans la Base

**Factures :**
```sql
SELECT COUNT(*) as total FROM factures;
SELECT COUNT(*) as payees FROM factures WHERE statut = 'PAYEE';
SELECT COUNT(*) as en_attente FROM factures WHERE statut IN ('EN_ATTENTE', 'BROUILLON');
```

**Paiements :**
```sql
SELECT COUNT(*) as total FROM paiements;
SELECT COUNT(*) as ce_mois FROM paiements 
WHERE MONTH(date_paiement) = MONTH(CURRENT_DATE) 
AND YEAR(date_paiement) = YEAR(CURRENT_DATE);
```

**Enquêtes :**
```sql
SELECT COUNT(*) as total FROM enquette;
SELECT COUNT(*) as completees FROM enquette WHERE statut = 'VALIDE';
SELECT COUNT(*) as en_cours FROM enquette WHERE statut != 'VALIDE' OR statut IS NULL;
```

### 2. Vérifier les Endpoints

**Tester l'endpoint financier :**
```
GET /api/statistiques/financieres
```

**Vérifier que la réponse contient :**
- `totalFactures`
- `facturesPayees`
- `facturesEnAttente`
- `totalPaiements`
- `paiementsCeMois`

**Tester l'endpoint globales :**
```
GET /api/statistiques/globales
```

**Vérifier que la réponse contient :**
- `totalEnquetes`
- `enquetesCompletees`
- `enquetesEnCours` (nouveau)

---

## 📝 Notes Importantes

### Pour les Factures

**Statuts possibles (FactureStatut) :**
- `BROUILLON` : Facture en brouillon
- `EMISE` : Facture émise (en attente de paiement)
- `PAYEE` : Facture payée
- `EN_RETARD` : Facture en retard
- `ANNULEE` : Facture annulée

**Logique :**
- `facturesEnAttente` = `BROUILLON` + `EMISE` + `EN_RETARD` (factures non payées)
- `facturesPayees` = `PAYEE`
- `totalFactures` = Toutes les factures (sauf peut-être `ANNULEE`)

### Pour les Paiements

**Statuts possibles (StatutPaiement) :**
- `EN_ATTENTE` : Paiement en attente
- `VALIDE` : Paiement validé
- `REJETE` : Paiement rejeté

**Logique :**
- `totalPaiements` = Tous les paiements
- `paiementsCeMois` = Paiements avec `datePaiement` dans le mois en cours

### Pour les Enquêtes

**Statuts possibles (Statut) :**
- `VALIDE` : Enquête validée (complétée)
- `EN_ATTENTE_VALIDATION` : En attente de validation
- `REJETE` : Enquête rejetée
- `NULL` : Pas de statut (en cours de création)

**Logique :**
- `enquetesCompletees` = `statut == VALIDE`
- `enquetesEnCours` = `statut != VALIDE` OU `statut IS NULL`
- `totalEnquetes` = Toutes les enquêtes

---

## 🎯 Résumé des Actions

### Backend

1. **Modifier `StatistiqueServiceImpl.getStatistiquesFinancieres()`**
   - Ajouter calculs factures et paiements
   - Injecter repositories nécessaires

2. **Modifier `StatistiqueServiceImpl.getStatistiquesGlobales()`**
   - Ajouter calcul `enquetesEnCours`

3. **Modifier `TypeStatistique` enum**
   - Ajouter 6 nouveaux types :
     - `TOTAL_FACTURES`
     - `FACTURES_PAYEES`
     - `FACTURES_EN_ATTENTE`
     - `TOTAL_PAIEMENTS`
     - `PAIEMENTS_CE_MOIS`
     - `ENQUETES_EN_COURS`

4. **Modifier `getTypeStatistiqueFromKey()`**
   - Ajouter les mappings pour les nouveaux types

### Frontend

1. **Vérifier le service de statistiques**
   - S'assurer que toutes les statistiques sont bien mappées

2. **Corriger l'affichage des enquêtes**
   - Utiliser `enquetesEnCours` du backend (pas de calcul frontend)

3. **Ajouter l'affichage des factures/paiements**
   - Afficher les nouvelles statistiques dans le dashboard finance

---

**Date de création :** 2025-01-05  
**Status :** ✅ Solutions proposées - Prêt pour implémentation


