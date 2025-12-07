# 📋 Prompts pour Améliorer le Frontend

## 🎯 Objectif

Ce document contient tous les prompts nécessaires pour améliorer le frontend Angular, intégrer les nouvelles fonctionnalités backend, et corriger les problèmes identifiés.

---

## 📋 Prompt 1 : Intégration des Statistiques Manquantes

### Prompt

```
Je dois intégrer les statistiques manquantes dans mon application Angular de gestion de recouvrement de créances.

**Contexte :**
- Backend Spring Boot avec endpoints REST
- Frontend Angular avec TypeScript
- Les statistiques sont affichées dans un tableau de bord

**Problème actuel :**
Les statistiques suivantes affichent 0 ou ne sont pas présentes :
- Total Factures
- Factures Payées
- Factures en Attente
- Total Paiements
- Paiements ce Mois
- Enquêtes en Cours (affiche une valeur négative incorrecte)

**Backend disponible :**
- Endpoint : `GET /api/statistiques/financieres` - Retourne un objet avec :
  - `totalFactures` (number)
  - `facturesPayees` (number)
  - `facturesEnAttente` (number)
  - `totalPaiements` (number)
  - `paiementsCeMois` (number)
- Endpoint : `GET /api/statistiques/globales` - Retourne un objet avec :
  - `enquetesEnCours` (number)

**Tâches :**
1. Créer/modifier le service Angular pour appeler ces endpoints
2. Créer/modifier les composants pour afficher ces statistiques
3. S'assurer que les valeurs sont correctement formatées (nombres entiers)
4. Gérer les cas d'erreur (affichage de 0 si l'endpoint échoue)
5. Utiliser RxJS pour la gestion asynchrone

**Structure attendue :**
- Service : `statistique.service.ts` avec méthodes `getStatistiquesFinancieres()` et `getStatistiquesGlobales()`
- Composant : Tableau de bord avec cartes affichant chaque statistique
- Interface TypeScript : Définir les types pour les réponses des endpoints

**Exigences :**
- Utiliser Angular Material pour les cartes
- Afficher un indicateur de chargement pendant la récupération des données
- Gérer les erreurs avec des messages utilisateur appropriés
- Utiliser des observables RxJS (pas de promesses)
```

---

## 📋 Prompt 2 : Intégration du Bouton Blocage/Déblocage Utilisateur

### Prompt

```
Je dois ajouter un bouton de blocage/déblocage utilisateur dans la page de gestion des utilisateurs de mon application Angular.

**Contexte :**
- Backend Spring Boot avec endpoints REST
- Frontend Angular avec TypeScript et Angular Material
- Page de gestion des utilisateurs avec un tableau listant tous les utilisateurs

**Backend disponible :**
- Endpoint : `PUT /api/admin/utilisateurs/{id}/activer` - Active (débloque) un utilisateur
  - Headers : `Authorization: Bearer {token}`
  - Retourne : Utilisateur mis à jour avec `actif: true`
- Endpoint : `PUT /api/admin/utilisateurs/{id}/desactiver` - Désactive (bloque) un utilisateur
  - Headers : `Authorization: Bearer {token}`
  - Retourne : Utilisateur mis à jour avec `actif: false`
  - Protection : Impossible de désactiver un SUPER_ADMIN

**Autorisation :**
- Seul un SUPER_ADMIN peut utiliser ces endpoints
- Vérifier le rôle de l'utilisateur connecté avant d'afficher les boutons

**Tâches :**
1. Ajouter une colonne "Statut" dans le tableau des utilisateurs affichant "Actif" ou "Inactif"
2. Ajouter une colonne "Actions" avec un bouton "Bloquer" ou "Débloquer" selon le statut
3. Implémenter les méthodes dans le service utilisateur pour appeler les endpoints
4. Afficher une confirmation avant de bloquer/débloquer (dialog Angular Material)
5. Afficher un message de succès/erreur après l'action
6. Mettre à jour le tableau après l'action (rafraîchir les données)
7. Désactiver le bouton "Débloquer" pour les SUPER_ADMIN (protection backend)

**Structure attendue :**
- Service : `utilisateur.service.ts` avec méthodes `activerUtilisateur(id)` et `desactiverUtilisateur(id)`
- Composant : Page de gestion des utilisateurs avec tableau Material
- Interface TypeScript : `Utilisateur` avec champ `actif: boolean`

**Exigences :**
- Utiliser Angular Material pour les boutons et le dialog de confirmation
- Utiliser des icônes Material (lock/unlock)
- Gérer les erreurs avec des messages utilisateur appropriés
- Afficher un spinner pendant l'action
- Utiliser RxJS pour la gestion asynchrone
```

---

## 📋 Prompt 3 : Intégration du Calcul des Commissions

### Prompt

```
Je dois intégrer l'affichage des commissions dans la page de détail de facture de mon application Angular.

**Contexte :**
- Backend Spring Boot avec endpoints REST
- Frontend Angular avec TypeScript et Angular Material
- Page de détail de facture affichant tous les frais et le total

**Backend disponible :**
- Endpoint : `GET /api/finances/dossier/{dossierId}/detail-facture` - Retourne un objet `DetailFactureDTO` avec :
  - `commissionAmiable` (BigDecimal) - Commission amiable (12% du montant recouvré)
  - `commissionJuridique` (BigDecimal) - Commission juridique (15% du montant recouvré)
  - `commissionInterets` (BigDecimal) - Commission sur intérêts (50% du montant d'intérêts)
  - `totalHT` (BigDecimal) - Total HT incluant les commissions
  - `tva` (BigDecimal) - TVA (19%)
  - `totalTTC` (BigDecimal) - Total TTC

**Calcul des commissions :**
- Les commissions sont calculées automatiquement par le backend
- Elles dépendent du montant recouvré (phase amiable ou juridique)
- Si aucun montant n'est recouvré, les commissions = 0

**Tâches :**
1. Modifier le service pour récupérer les commissions depuis l'endpoint
2. Afficher une section "Commissions" dans la page de détail de facture
3. Afficher chaque commission avec son libellé et son montant
4. Afficher le total des commissions
5. S'assurer que le total HT inclut bien les commissions
6. Formater les montants en TND avec 2 décimales

**Structure attendue :**
- Service : `facture.service.ts` avec méthode `getDetailFacture(dossierId)`
- Composant : Page de détail de facture avec section commissions
- Interface TypeScript : `DetailFactureDTO` avec champs de commission

**Exigences :**
- Utiliser Angular Material pour l'affichage
- Formater les montants avec le pipe `currency` d'Angular
- Afficher un indicateur si les commissions = 0 (aucun recouvrement)
- Utiliser RxJS pour la gestion asynchrone
```

---

## 📋 Prompt 4 : Intégration des Tarifs Automatiques

### Prompt

```
Je dois m'assurer que les tarifs automatiques sont correctement affichés dans la page de validation des tarifs de mon application Angular.

**Contexte :**
- Backend Spring Boot avec endpoints REST
- Frontend Angular avec TypeScript et Angular Material
- Page de validation des tarifs affichant tous les tarifs d'un dossier organisés par phase

**Backend disponible :**
- Endpoint : `GET /api/finances/dossier/{dossierId}/traitements` - Retourne un objet `TraitementsDossierDTO` avec :
  - `phaseCreation` - Contient le tarif "OUVERTURE_DOSSIER" (250 TND, VALIDE automatiquement)
  - `phaseEnquete` - Contient le tarif "ENQUETE_PRECONTENTIEUSE" (300 TND, VALIDE automatiquement)
  - `phaseJuridique` - Contient l'avance "AVANCE_RECOUVREMENT_JURIDIQUE" (1000 TND, VALIDE automatiquement) si applicable

**Comportement automatique :**
- Lors de la validation d'un dossier → Tarif "OUVERTURE_DOSSIER" créé automatiquement (250 TND, VALIDE)
- Lors de la validation d'une enquête → Tarif "ENQUETE_PRECONTENTIEUSE" créé automatiquement (300 TND, VALIDE)
- Lors du passage en phase JURIDIQUE → Avance "AVANCE_RECOUVREMENT_JURIDIQUE" créée automatiquement (1000 TND, VALIDE)

**Tâches :**
1. Vérifier que les tarifs automatiques sont affichés dans la page de validation
2. Afficher un indicateur visuel pour les tarifs automatiques (badge "Automatique")
3. Désactiver les boutons de modification/suppression pour les tarifs automatiques
4. Afficher le statut "VALIDE" pour les tarifs automatiques
5. Rafraîchir la page après validation d'un dossier/enquête pour afficher les nouveaux tarifs

**Structure attendue :**
- Service : `tarif.service.ts` avec méthode `getTraitementsDossier(dossierId)`
- Composant : Page de validation des tarifs avec affichage par phase
- Interface TypeScript : `TraitementsDossierDTO` avec phases

**Exigences :**
- Utiliser Angular Material pour l'affichage
- Afficher un badge "Automatique" pour les tarifs créés automatiquement
- Utiliser des icônes Material pour les statuts
- Gérer le rafraîchissement automatique après validation
- Utiliser RxJS pour la gestion asynchrone
```

---

## 📋 Prompt 5 : Correction de l'Affichage des Enquêtes en Cours

### Prompt

```
Je dois corriger l'affichage des statistiques d'enquêtes dans mon application Angular.

**Problème actuel :**
- La statistique "Enquêtes en cours" affiche une valeur négative incorrecte (ex: -3)
- Le total des enquêtes affiche 0 alors qu'il y a des enquêtes

**Contexte :**
- Backend Spring Boot avec endpoints REST
- Frontend Angular avec TypeScript
- Tableau de bord affichant les statistiques

**Backend disponible :**
- Endpoint : `GET /api/statistiques/globales` - Retourne un objet avec :
  - `totalEnquetes` (number) - Total des enquêtes
  - `enquetesCompletees` (number) - Enquêtes complétées (statut VALIDE)
  - `enquetesEnCours` (number) - Enquêtes en cours (statut EN_COURS ou EN_ATTENTE_VALIDATION)

**Calcul correct :**
- `enquetesEnCours` = Enquêtes avec statut EN_COURS ou EN_ATTENTE_VALIDATION
- Le backend calcule maintenant correctement cette valeur

**Tâches :**
1. Vérifier que le service Angular récupère bien `enquetesEnCours` depuis l'endpoint
2. Afficher la valeur correcte dans le tableau de bord
3. S'assurer que le calcul frontend n'essaie pas de recalculer cette valeur
4. Utiliser directement la valeur du backend (ne pas faire `totalEnquetes - enquetesCompletees`)

**Structure attendue :**
- Service : `statistique.service.ts` avec méthode `getStatistiquesGlobales()`
- Composant : Tableau de bord avec affichage des statistiques d'enquêtes
- Interface TypeScript : Définir le type pour la réponse de l'endpoint

**Exigences :**
- Utiliser directement `enquetesEnCours` du backend
- Ne pas recalculer cette valeur côté frontend
- Afficher un message si la valeur est 0 (aucune enquête en cours)
- Utiliser RxJS pour la gestion asynchrone
```

---

## 📋 Prompt 6 : Amélioration Générale de l'Interface Utilisateur

### Prompt

```
Je dois améliorer l'interface utilisateur de mon application Angular de gestion de recouvrement de créances.

**Contexte :**
- Application Angular avec TypeScript et Angular Material
- Plusieurs pages : Tableau de bord, Gestion des utilisateurs, Validation des tarifs, Détail de facture

**Améliorations demandées :**
1. **Cohérence visuelle :**
   - Utiliser un thème Material cohérent sur toutes les pages
   - Uniformiser les couleurs, espacements, et typographie
   - Utiliser des icônes Material de manière cohérente

2. **Feedback utilisateur :**
   - Afficher des messages de succès/erreur avec des snackbars Material
   - Afficher des indicateurs de chargement (spinners) pendant les opérations
   - Confirmer les actions importantes (dialogs Material)

3. **Gestion des erreurs :**
   - Afficher des messages d'erreur clairs et compréhensibles
   - Gérer les erreurs réseau (timeout, connexion perdue)
   - Afficher des messages d'erreur spécifiques selon le code HTTP

4. **Performance :**
   - Utiliser le lazy loading pour les modules
   - Implémenter la pagination pour les grandes listes
   - Utiliser OnPush change detection où possible

5. **Accessibilité :**
   - Ajouter des labels ARIA pour les éléments interactifs
   - Gérer la navigation au clavier
   - Assurer un contraste suffisant pour le texte

**Structure attendue :**
- Service : `notification.service.ts` pour les messages utilisateur
- Service : `loading.service.ts` pour les indicateurs de chargement
- Composant : Dialog de confirmation réutilisable
- Composant : Snackbar de notification réutilisable

**Exigences :**
- Utiliser Angular Material pour tous les composants UI
- Utiliser RxJS pour la gestion asynchrone
- Implémenter un intercepteur HTTP pour la gestion globale des erreurs
- Créer un service de gestion des erreurs centralisé
```

---

## 📋 Prompt 7 : Intégration des Montants Recouvrés par Phase

### Prompt

```
Je dois intégrer l'affichage des montants recouvrés par phase (amiable et juridique) dans les dashboards et statistiques de mon application Angular.

**Contexte :**
- Backend Spring Boot avec endpoints REST
- Frontend Angular avec TypeScript et Angular Material
- Plusieurs dashboards nécessitent l'affichage des montants par phase

**Backend disponible :**
- Endpoint : `GET /api/statistiques/globales` - Retourne :
  - `montantRecouvrePhaseAmiable` (number)
  - `montantRecouvrePhaseJuridique` (number)
  - `montantRecouvre` (number) - Total
- Endpoint : `GET /api/statistiques/financieres` - Retourne :
  - `montantRecouvrePhaseAmiable` (number)
  - `montantRecouvrePhaseJuridique` (number)
  - `montantRecouvre` (number) - Total
- Endpoint : `GET /api/statistiques/recouvrement-par-phase` - Retourne :
  - `montantRecouvrePhaseAmiable` (number)
  - `montantRecouvrePhaseJuridique` (number)
  - `montantRecouvreTotal` (number)
  - `dossiersAvecRecouvrementAmiable` (number)
  - `dossiersAvecRecouvrementJuridique` (number)
  - `tauxRecouvrementAmiable` (number)
  - `tauxRecouvrementJuridique` (number)
- Endpoint : `GET /api/statistiques/recouvrement-par-phase/departement` - Retourne les mêmes champs filtrés par département

**Tâches :**
1. Modifier les services Angular pour récupérer ces champs depuis les endpoints appropriés
2. Créer/modifier les composants pour afficher ces montants dans les dashboards :
   - Dashboard SuperAdmin (sections Recouvrement Amiable, Recouvrement Juridique, Finance)
   - Dashboard Chef Amiable
   - Dashboard Chef Juridique
   - Dashboard Chef Finance
   - Tableau de bord statistiques globales
3. Afficher pour chaque phase :
   - Montant recouvré formaté en TND
   - Pourcentage du total (si applicable)
   - Nombre de dossiers (si disponible)
   - Taux de recouvrement (si disponible)
4. Utiliser des graphiques comparatifs (barres ou camembert) pour visualiser la répartition
5. Utiliser des couleurs cohérentes (vert pour amiable, bleu pour juridique)

**Structure attendue :**
- Service : `statistique.service.ts` avec méthodes mises à jour
- Composants : Dashboards modifiés pour inclure les montants par phase
- Interface TypeScript : Types mis à jour avec les nouveaux champs
- Composant réutilisable (optionnel) : `montants-par-phase.component.ts`

**Exigences :**
- Utiliser Angular Material pour les cartes et graphiques
- Utiliser Chart.js ou Angular Material Charts pour les graphiques
- Formater les montants avec le pipe `currency` d'Angular
- Afficher un indicateur de chargement pendant la récupération des données
- Gérer les erreurs avec des messages utilisateur appropriés
- Utiliser des observables RxJS (pas de promesses)
- Design professionnel et moderne
- Responsive design (adaptation mobile/tablette/desktop)

**Référence :** Voir le document `PROMPTS_FRONTEND_MONTANTS_RECOUVRES_PAR_PHASE.md` pour des prompts détaillés par dashboard.
```

---

## 📋 Résumé des Prompts

### Prompts par Priorité

1. **Priorité Haute :**
   - Prompt 1 : Intégration des Statistiques Manquantes
   - Prompt 2 : Intégration du Bouton Blocage/Déblocage Utilisateur
   - Prompt 5 : Correction de l'Affichage des Enquêtes en Cours
   - Prompt 7 : Intégration des Montants Recouvrés par Phase

2. **Priorité Moyenne :**
   - Prompt 3 : Intégration du Calcul des Commissions
   - Prompt 4 : Intégration des Tarifs Automatiques

3. **Priorité Basse :**
   - Prompt 6 : Amélioration Générale de l'Interface Utilisateur

---

## 📝 Notes d'Utilisation

### Comment Utiliser ces Prompts

1. **Copier le prompt complet** dans votre outil de développement IA (ChatGPT, Claude, etc.)
2. **Adapter le prompt** selon vos besoins spécifiques
3. **Tester les modifications** après chaque implémentation
4. **Itérer** si nécessaire pour affiner les résultats

### Structure Recommandée

Pour chaque prompt :
1. Lire attentivement le contexte et les exigences
2. Vérifier que les endpoints backend sont disponibles
3. Implémenter les modifications étape par étape
4. Tester chaque fonctionnalité
5. Documenter les changements

---

**Date :** 2025-01-05  
**Status :** ✅ Prompts prêts pour utilisation

