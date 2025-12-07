# 🚀 Prompts et Actions Immédiates pour Corriger les Statistiques

## 📋 Résumé des Problèmes

| Interface | Problème | Solution |
|-----------|----------|----------|
| Chef Amiable | Toutes les stats à 0 | Utiliser `/api/statistiques/departement` |
| Chef Dossier | Stats à 0, manque enquêtes | Ajouter stats enquêtes dans l'interface |
| Agent Dossier | Voit toutes les stats au lieu des siennes | Utiliser `/api/statistiques/mes-dossiers` |
| Chef Juridique | Stats incorrectes ou à 0 | Utiliser `/api/statistiques/departement` + audiences |
| Chef Finance | Stats mal structurées, parfois null | Améliorer structure + utiliser `/api/finance/statistiques` |
| SuperAdmin - Vue Dossiers | Stats à 0, manque enquêtes | Ajouter card enquêtes |
| SuperAdmin - Vue Juridique | Stats à 0 | Utiliser `/api/statistiques/audiences` |
| SuperAdmin - Vue Finance | Stats null | Utiliser `/api/statistiques/financieres` |
| SuperAdmin - Vue Amiable | Stats à 0, manque par type | Ajouter stats par type d'action |
| SuperAdmin - Reports | Stats à 0 | Charger toutes les APIs |

---

## 🔧 Actions Backend Immédiates

### 1. Ajouter TOTAL_ENQUETES à l'ENUM

**Script SQL à exécuter :**
```sql
ALTER TABLE statistiques 
MODIFY COLUMN type ENUM(
    'ACTIONS_AMIABLES',
    'ACTIONS_AMIABLES_COMPLETEES',
    'ACTIONS_HUISSIER_COMPLETES',
    'ACTIONS_HUISSIER_CREES',
    'AUDIENCES_PROCHAINES',
    'AUDIENCES_TOTALES',
    'DOCUMENTS_HUISSIER_COMPLETES',
    'DOCUMENTS_HUISSIER_CREES',
    'DOSSIERS_CLOTURES',
    'DOSSIERS_CREES_CE_MOIS',
    'DOSSIERS_EN_COURS',
    'DOSSIERS_PAR_PHASE_AMIABLE',
    'DOSSIERS_PAR_PHASE_CREATION',
    'DOSSIERS_PAR_PHASE_ENQUETE',
    'DOSSIERS_PAR_PHASE_JURIDIQUE',
    'DOSSIERS_REJETES',
    'DOSSIERS_VALIDES',
    'ENQUETES_COMPLETEES',
    'TOTAL_ENQUETES',  -- ✅ AJOUTER
    'MONTANT_EN_COURS',
    'MONTANT_RECOUVRE',
    'PERFORMANCE_AGENTS',
    'PERFORMANCE_CHEFS',
    'TACHES_COMPLETEES',
    'TACHES_EN_COURS',
    'TACHES_EN_RETARD',
    'TAUX_REUSSITE_GLOBAL',
    'TOTAL_DOSSIERS'
) NOT NULL;
```

### 2. Nettoyer les Duplications

**Script SQL :**
```sql
DELETE FROM statistiques 
WHERE periode = DATE_FORMAT(NOW(), '%Y-%m');
```

### 3. Forcer le Recalcul

**Via API :**
```bash
POST http://localhost:8089/carthage-creance/api/statistiques/recalculer
Headers: Authorization: Bearer {token}
```

---

## 📝 Prompts pour le Frontend

### Prompt 1 : Chef Amiable Dashboard

```
Corriger le dashboard du Chef Amiable pour afficher les statistiques correctes.

Problème actuel : Toutes les statistiques affichent 0.

Solution :
1. Créer un service Angular `StatistiqueService` avec la méthode `getStatistiquesDepartement()`
2. Appeler l'API GET /api/statistiques/departement avec le token d'autorisation
3. Mapper les données reçues aux variables du composant :
   - totalDossiers → stats.totalDossiers
   - dossiersEnCours → stats.enCours
   - actionsAmiables → stats.actionsAmiables
   - actionsAmiablesCompletees → stats.actionsAmiablesCompletees
   - tauxReussite → stats.tauxReussite
   - montantRecouvre → stats.montantRecouvre
   - montantEnCours → stats.montantEnCours
4. Afficher les statistiques des agents dans un tableau "Performances des Agents"
5. Gérer les erreurs avec des messages appropriés
6. Afficher un indicateur de chargement pendant la récupération des données

Référence : Voir GUIDE_COMPLET_FRONTEND_STATISTIQUES_PAR_ROLE.md section 1
```

### Prompt 2 : Chef Dossier Dashboard

```
Corriger le dashboard du Chef Dossier pour afficher les statistiques correctes et ajouter les statistiques d'enquêtes.

Problème actuel : Toutes les statistiques affichent 0, et les statistiques d'enquêtes sont manquantes.

Solution :
1. Appeler GET /api/statistiques/departement pour les stats générales
2. Appeler GET /api/statistiques/mes-agents pour les performances des agents
3. Extraire les statistiques d'enquêtes de la réponse :
   - totalEnquetes → stats.totalEnquetes
   - enquetesCompletees → stats.enquetesCompletees
4. Ajouter une nouvelle section "Statistiques des Enquêtes" dans le template HTML avec :
   - Total Enquêtes
   - Enquêtes Complétées
   - Enquêtes en Cours (calculé : total - complétées)
5. Afficher les performances des agents dans un tableau avec colonnes :
   - Agent
   - Dossiers Traités
   - Dossiers Clôturés
   - Taux de Réussite
   - Montant Récupéré
   - Performance (barre de progression)

Référence : Voir GUIDE_COMPLET_FRONTEND_STATISTIQUES_PAR_ROLE.md section 2
```

### Prompt 3 : Agent Dossier Dashboard

```
Corriger le dashboard de l'Agent Dossier pour afficher UNIQUEMENT ses propres statistiques.

Problème actuel : L'agent voit toutes les statistiques au lieu de seulement les siennes.

Solution :
1. Utiliser GET /api/statistiques/mes-dossiers au lieu de /api/statistiques/departement
2. Mapper les données reçues aux variables :
   - totalDossiers → stats.mesDossiers
   - dossiersEnCours → stats.mesDossiersEnCours
   - dossiersClotures → stats.mesDossiersClotures
   - tauxReussite → stats.monTauxReussite
   - montantRecouvre → stats.monMontantRecouvre
3. Modifier le template pour afficher "Mes Dossiers", "Mon Taux de Réussite", etc.
4. Ajouter une section "Mes Performances" avec un résumé personnel
5. S'assurer que l'agent ne peut pas voir les statistiques des autres agents

Référence : Voir GUIDE_COMPLET_FRONTEND_STATISTIQUES_PAR_ROLE.md section 3
```

### Prompt 4 : Chef Juridique Dashboard

```
Corriger le dashboard du Chef Juridique pour afficher les statistiques correctes des audiences, documents huissier et actions huissier.

Problème actuel : Statistiques incorrectes ou à 0.

Solution :
1. Appeler GET /api/statistiques/departement pour les stats du département juridique
2. Extraire les statistiques :
   - dossiersParPhaseJuridique → stats.dossiersJuridiques
   - audiencesProchaines → stats.audiencesProchaines
   - audiencesTotales → stats.audiencesTotales
   - documentsHuissierCompletes → stats.documentsHuissierCompletes
   - documentsHuissierCrees → stats.documentsHuissierCrees
   - actionsHuissierCompletes → stats.actionsHuissierCompletes
   - actionsHuissierCrees → stats.actionsHuissierCrees
3. Ajouter des sections dans le template :
   - Section "Audiences" avec Complétées, Prochaines (7j), Total
   - Section "Documents Huissier" avec Complétés, Créés
   - Section "Actions Huissier" avec Complétées, Créées
4. Compter les avocats et huissiers (créer endpoints si nécessaire)

Référence : Voir GUIDE_COMPLET_FRONTEND_STATISTIQUES_PAR_ROLE.md section 4
```

### Prompt 5 : Chef Finance Dashboard

```
Améliorer la structure et corriger les statistiques du dashboard du Chef Finance.

Problème actuel : Statistiques mal structurées, parfois null ou fausses.

Solution :
1. Appeler GET /api/statistiques/departement pour les stats générales
2. Appeler GET /api/finance/statistiques pour les stats financières détaillées
3. Organiser les statistiques en sections claires :
   - Section 1 : Frais et Coûts (Frais Création, Gestion, Avocat, Huissier, Actions)
   - Section 2 : Résumé et Performance (Taux de Réussite, Dossiers Total, Phases)
   - Section 3 : Statut Dossiers et Récupération (Montant Récupéré, En Cours)
   - Section 4 : Factures (Émises, Payées, En Attente)
4. Calculer le Grand Total des frais
5. Gérer les valeurs null en affichant 0 par défaut
6. Formater correctement les montants (2 décimales, séparateur de milliers)

Référence : Voir GUIDE_COMPLET_FRONTEND_STATISTIQUES_PAR_ROLE.md section 5
```

### Prompt 6 : SuperAdmin - Vue d'Ensemble Dossiers

```
Corriger la vue d'ensemble des dossiers du SuperAdmin et ajouter les statistiques d'enquêtes.

Problème actuel : Statistiques à 0, manque les statistiques d'enquêtes.

Solution :
1. Appeler GET /api/statistiques/dossiers
2. Extraire les statistiques :
   - totalDossiers → stats.dossiers.total
   - dossiersEnCours → stats.dossiers.enCours
   - dossiersClotures → stats.dossiers.clotures
   - dossiersCreesCeMois → stats.dossiers.creesCeMois
   - dossiersParPhaseCreation → stats.dossiersParPhase.creation
   - dossiersParPhaseEnquete → stats.dossiersParPhase.enquete
   - dossiersParPhaseAmiable → stats.dossiersParPhase.amiable
   - dossiersParPhaseJuridique → stats.dossiersParPhase.juridique
   - totalEnquetes → stats.enquetes.total
   - enquetesCompletees → stats.enquetes.completees
3. Ajouter une nouvelle card "Enquêtes" avec :
   - Total
   - Complétées
   - En cours (calculé)
4. Ajouter un bouton "Actualiser" pour recharger les statistiques

Référence : Voir GUIDE_COMPLET_FRONTEND_STATISTIQUES_PAR_ROLE.md section 6
```

### Prompt 7 : SuperAdmin - Vue d'Ensemble Juridique

```
Corriger la vue d'ensemble juridique du SuperAdmin pour afficher les statistiques correctes.

Problème actuel : Toutes les statistiques affichent 0.

Solution :
1. Appeler GET /api/statistiques/audiences pour les stats d'audiences
2. Appeler GET /api/statistiques/globales pour les stats documents et actions huissier
3. Extraire et mapper les données :
   - audiencesTotales, audiencesProchaines → stats.audiences
   - documentsHuissierCompletes, documentsHuissierCrees → stats.documentsHuissier
   - actionsHuissierCompletes, actionsHuissierCrees → stats.actionsHuissier
4. Afficher les statistiques dans les cards existantes
5. Calculer "Complétées" = Total - Prochaines pour les audiences

Référence : Voir GUIDE_COMPLET_FRONTEND_STATISTIQUES_PAR_ROLE.md section 6
```

### Prompt 8 : SuperAdmin - Vue d'Ensemble Finance

```
Corriger la vue d'ensemble finance du SuperAdmin pour afficher les statistiques correctes (actuellement null).

Problème actuel : Statistiques bien présentées mais valeurs null.

Solution :
1. Appeler GET /api/statistiques/financieres
2. Extraire les statistiques :
   - montantEnCours → stats.financier.montantEnCours
   - montantRecouvre → stats.financier.montantRecouvre
   - tauxReussite → stats.financier.tauxReussite
   - facturesEnAttente → stats.factures.enAttente
   - facturesPayees → stats.factures.payees
   - facturesTotal → stats.factures.total
   - paiementsCeMois → stats.paiements.ceMois
   - paiementsTotal → stats.paiements.total
3. Gérer les valeurs null en affichant 0 par défaut
4. Formater correctement les montants
5. Afficher un message d'erreur si l'API échoue

Référence : Voir GUIDE_COMPLET_FRONTEND_STATISTIQUES_PAR_ROLE.md section 6
```

### Prompt 9 : SuperAdmin - Vue d'Ensemble Amiable

```
Corriger la vue d'ensemble amiable du SuperAdmin et ajouter les statistiques par type d'action amiable.

Problème actuel : Statistiques à 0, manque les statistiques par type d'action.

Solution :
1. Appeler GET /api/statistiques/actions-amiables
2. Extraire les statistiques :
   - total → stats.actionsAmiables.total
   - completees → stats.actionsAmiables.completees
   - enCours → stats.actionsAmiables.enCours
   - tauxReussite → stats.performance.tauxReussite
3. Créer un endpoint backend GET /api/statistiques/actions-amiables/par-type (si n'existe pas)
4. Appeler cet endpoint pour obtenir les statistiques par type d'action
5. Afficher un tableau "Actions par Type" avec colonnes :
   - Type d'Action
   - Total
   - Complétées
   - En Cours
   - Taux de Réussite
6. Afficher les statistiques dans les cards existantes

Référence : Voir GUIDE_COMPLET_FRONTEND_STATISTIQUES_PAR_ROLE.md section 6
```

### Prompt 10 : SuperAdmin - Reports & Analyses

```
Corriger la page Reports & Analyses du SuperAdmin pour afficher toutes les statistiques.

Problème actuel : Toutes les statistiques affichent 0.

Solution :
1. Charger toutes les statistiques en parallèle :
   - GET /api/statistiques/globales
   - GET /api/statistiques/dossiers
   - GET /api/statistiques/actions-amiables
   - GET /api/statistiques/financieres
   - GET /api/statistiques/chefs
   - GET /api/statistiques/audiences
   - GET /api/statistiques/taches
2. Organiser les statistiques par catégorie :
   - Statistiques Globales
   - Statistiques Dossiers
   - Statistiques Actions
   - Statistiques Finance
   - Statistiques Performance
3. Afficher les statistiques dans des sections organisées
4. Gérer les erreurs pour chaque API individuellement
5. Afficher un indicateur de chargement global

Référence : Voir GUIDE_COMPLET_FRONTEND_STATISTIQUES_PAR_ROLE.md section 7
```

---

## 🎯 Checklist de Vérification

### Backend
- [ ] L'ENUM contient TOTAL_ENQUETES
- [ ] Les duplications sont nettoyées
- [ ] Le recalcul a été forcé
- [ ] Les statistiques sont calculées correctement dans getStatistiquesGlobales()
- [ ] Les statistiques sont stockées dans la table statistiques

### Frontend - Service
- [ ] StatistiqueService créé avec toutes les méthodes nécessaires
- [ ] Les headers d'autorisation sont inclus dans toutes les requêtes
- [ ] Les erreurs sont gérées avec des messages appropriés
- [ ] Les valeurs par défaut sont définies si les données sont null

### Frontend - Composants
- [ ] Chef Amiable : Utilise /api/statistiques/departement
- [ ] Chef Dossier : Affiche les stats d'enquêtes
- [ ] Agent Dossier : Utilise /api/statistiques/mes-dossiers
- [ ] Chef Juridique : Affiche audiences, documents et actions huissier
- [ ] Chef Finance : Structure améliorée, stats correctes
- [ ] SuperAdmin - Vue Dossiers : Affiche les stats d'enquêtes
- [ ] SuperAdmin - Vue Juridique : Stats correctes
- [ ] SuperAdmin - Vue Finance : Stats non null
- [ ] SuperAdmin - Vue Amiable : Stats par type d'action
- [ ] SuperAdmin - Reports : Toutes les stats chargées

### Tests
- [ ] Tester chaque interface avec un utilisateur du rôle approprié
- [ ] Vérifier que les statistiques s'affichent correctement
- [ ] Vérifier que les erreurs sont gérées
- [ ] Vérifier que les valeurs null affichent 0
- [ ] Vérifier que le formatage est correct (nombres, pourcentages, montants)

---

## 📞 Support

Si vous rencontrez des problèmes :
1. Vérifier les logs du backend
2. Vérifier la console du navigateur
3. Vérifier que le token d'autorisation est valide
4. Tester les APIs directement via Postman
5. Vérifier que les statistiques sont stockées dans la base de données

