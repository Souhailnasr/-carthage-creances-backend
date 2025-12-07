# 📋 Prompts Frontend : Intégration des Montants Recouvrés par Phase

## 🎯 Objectif

Ce document contient les prompts détaillés pour intégrer l'affichage des montants recouvrés par phase (amiable et juridique) dans les dashboards et statistiques du frontend Angular.

---

## 📋 Prompt 1 : Intégration dans les Statistiques Globales

### Prompt

```
Je dois intégrer l'affichage des montants recouvrés par phase (amiable et juridique) dans le tableau de bord des statistiques globales de mon application Angular.

**Contexte :**
- Backend Spring Boot avec endpoints REST
- Frontend Angular avec TypeScript et Angular Material
- Tableau de bord affichant les statistiques globales

**Backend disponible :**
- Endpoint : `GET /api/statistiques/globales` - Retourne un objet avec :
  - `montantRecouvrePhaseAmiable` (number) - Montant recouvré en phase amiable
  - `montantRecouvrePhaseJuridique` (number) - Montant recouvré en phase juridique
  - `montantRecouvre` (number) - Montant total recouvré (somme des deux phases)

**Tâches :**
1. Modifier le service Angular pour récupérer ces champs depuis l'endpoint
2. Créer/modifier les composants pour afficher ces montants dans des cartes séparées
3. Afficher :
   - Carte "Recouvrement Amiable" avec le montant recouvré en phase amiable
   - Carte "Recouvrement Juridique" avec le montant recouvré en phase juridique
   - Carte "Total Recouvré" avec le montant total
4. Formater les montants en TND avec 2 décimales
5. Utiliser des couleurs différentes pour chaque phase (ex: vert pour amiable, bleu pour juridique)

**Structure attendue :**
- Service : `statistique.service.ts` avec méthode `getStatistiquesGlobales()`
- Composant : Tableau de bord avec cartes Material affichant chaque montant
- Interface TypeScript : Définir le type pour la réponse de l'endpoint avec les nouveaux champs

**Exigences :**
- Utiliser Angular Material pour les cartes
- Afficher un indicateur de chargement pendant la récupération des données
- Gérer les erreurs avec des messages utilisateur appropriés
- Utiliser des observables RxJS (pas de promesses)
- Formater les montants avec le pipe `currency` d'Angular
```

---

## 📋 Prompt 2 : Intégration dans le Dashboard SuperAdmin - Recouvrement Amiable

### Prompt

```
Je dois intégrer l'affichage des montants recouvrés par phase dans le dashboard SuperAdmin pour la supervision du recouvrement amiable de mon application Angular.

**Contexte :**
- Backend Spring Boot avec endpoints REST
- Frontend Angular avec TypeScript et Angular Material
- Dashboard SuperAdmin avec section "Supervision Recouvrement Amiable"

**Backend disponible :**
- Endpoint : `GET /api/statistiques/recouvrement-par-phase` - Retourne un objet avec :
  - `montantRecouvrePhaseAmiable` (number) - Montant recouvré en phase amiable
  - `montantRecouvrePhaseJuridique` (number) - Montant recouvré en phase juridique
  - `montantRecouvreTotal` (number) - Montant total recouvré
  - `dossiersAvecRecouvrementAmiable` (number) - Nombre de dossiers avec recouvrement amiable
  - `tauxRecouvrementAmiable` (number) - Taux de recouvrement amiable en pourcentage
  - `montantTotalCreances` (number) - Montant total des créances

**Tâches :**
1. Créer/modifier le service Angular pour appeler l'endpoint `/api/statistiques/recouvrement-par-phase`
2. Créer/modifier le composant du dashboard SuperAdmin pour la section "Recouvrement Amiable"
3. Afficher dans cette section :
   - **Montant Recouvré Amiable** : Montant total recouvré en phase amiable (formaté en TND)
   - **Nombre de Dossiers** : Nombre de dossiers avec recouvrement amiable
   - **Taux de Recouvrement** : Taux de recouvrement amiable en pourcentage (avec indicateur visuel)
   - **Graphique** : Graphique en barres ou camembert montrant la répartition amiable vs juridique
4. Utiliser des cartes Material avec des icônes appropriées
5. Ajouter un indicateur de progression pour le taux de recouvrement

**Structure attendue :**
- Service : `statistique.service.ts` avec méthode `getStatistiquesRecouvrementParPhase()`
- Composant : `superadmin-recuperement-amiable.component.ts` (ou similaire)
- Interface TypeScript : `StatistiquesRecouvrementParPhase` avec tous les champs

**Exigences :**
- Utiliser Angular Material pour les cartes et graphiques
- Utiliser Chart.js ou Angular Material Charts pour les graphiques
- Afficher un indicateur de chargement pendant la récupération des données
- Gérer les erreurs avec des messages utilisateur appropriés
- Utiliser des observables RxJS (pas de promesses)
- Formater les montants avec le pipe `currency` d'Angular
- Utiliser des couleurs cohérentes (vert pour amiable)
```

---

## 📋 Prompt 3 : Intégration dans le Dashboard SuperAdmin - Recouvrement Juridique

### Prompt

```
Je dois intégrer l'affichage des montants recouvrés par phase dans le dashboard SuperAdmin pour la supervision du recouvrement juridique de mon application Angular.

**Contexte :**
- Backend Spring Boot avec endpoints REST
- Frontend Angular avec TypeScript et Angular Material
- Dashboard SuperAdmin avec section "Supervision Recouvrement Juridique"

**Backend disponible :**
- Endpoint : `GET /api/statistiques/recouvrement-par-phase` - Retourne un objet avec :
  - `montantRecouvrePhaseAmiable` (number) - Montant recouvré en phase amiable
  - `montantRecouvrePhaseJuridique` (number) - Montant recouvré en phase juridique
  - `montantRecouvreTotal` (number) - Montant total recouvré
  - `dossiersAvecRecouvrementJuridique` (number) - Nombre de dossiers avec recouvrement juridique
  - `tauxRecouvrementJuridique` (number) - Taux de recouvrement juridique en pourcentage
  - `montantTotalCreances` (number) - Montant total des créances

**Tâches :**
1. Créer/modifier le service Angular pour appeler l'endpoint `/api/statistiques/recouvrement-par-phase`
2. Créer/modifier le composant du dashboard SuperAdmin pour la section "Recouvrement Juridique"
3. Afficher dans cette section :
   - **Montant Recouvré Juridique** : Montant total recouvré en phase juridique (formaté en TND)
   - **Nombre de Dossiers** : Nombre de dossiers avec recouvrement juridique
   - **Taux de Recouvrement** : Taux de recouvrement juridique en pourcentage (avec indicateur visuel)
   - **Graphique** : Graphique en barres ou camembert montrant la répartition juridique vs amiable
4. Utiliser des cartes Material avec des icônes appropriées
5. Ajouter un indicateur de progression pour le taux de recouvrement

**Structure attendue :**
- Service : `statistique.service.ts` avec méthode `getStatistiquesRecouvrementParPhase()`
- Composant : `superadmin-recuperement-juridique.component.ts` (ou similaire)
- Interface TypeScript : `StatistiquesRecouvrementParPhase` avec tous les champs

**Exigences :**
- Utiliser Angular Material pour les cartes et graphiques
- Utiliser Chart.js ou Angular Material Charts pour les graphiques
- Afficher un indicateur de chargement pendant la récupération des données
- Gérer les erreurs avec des messages utilisateur appropriés
- Utiliser des observables RxJS (pas de promesses)
- Formater les montants avec le pipe `currency` d'Angular
- Utiliser des couleurs cohérentes (bleu pour juridique)
```

---

## 📋 Prompt 4 : Intégration dans le Dashboard SuperAdmin - Finance

### Prompt

```
Je dois intégrer l'affichage des montants recouvrés par phase dans le dashboard SuperAdmin pour la supervision financière de mon application Angular.

**Contexte :**
- Backend Spring Boot avec endpoints REST
- Frontend Angular avec TypeScript et Angular Material
- Dashboard SuperAdmin avec section "Supervision Finance"

**Backend disponible :**
- Endpoint : `GET /api/statistiques/financieres` - Retourne un objet avec :
  - `montantRecouvrePhaseAmiable` (number) - Montant recouvré en phase amiable ✅ NOUVEAU
  - `montantRecouvrePhaseJuridique` (number) - Montant recouvré en phase juridique ✅ NOUVEAU
  - `montantRecouvre` (number) - Montant total recouvré (somme des deux phases)
  - `montantEnCours` (number) - Montant en cours de recouvrement
  - `totalFraisEngages` (number) - Total des frais engagés
  - `fraisRecuperes` (number) - Frais récupérés
  - `netGenere` (number) - Net généré
  - `totalFactures` (number) - Total des factures
  - `facturesPayees` (number) - Factures payées
  - `facturesEnAttente` (number) - Factures en attente
  - `totalPaiements` (number) - Total des paiements
  - `paiementsCeMois` (number) - Paiements ce mois

**Tâches :**
1. Modifier le service Angular pour récupérer les nouveaux champs `montantRecouvrePhaseAmiable` et `montantRecouvrePhaseJuridique`
2. Créer/modifier le composant du dashboard SuperAdmin pour la section "Finance"
3. Afficher dans cette section :
   - **Section "Recouvrement par Phase"** :
     - Carte "Recouvrement Amiable" avec montant et pourcentage du total
     - Carte "Recouvrement Juridique" avec montant et pourcentage du total
     - Graphique comparatif (barres ou camembert) montrant la répartition
   - **Section "Résumé Financier"** :
     - Montant total recouvré
     - Montant en cours
     - Net généré
   - **Section "Factures et Paiements"** :
     - Total factures, factures payées, factures en attente
     - Total paiements, paiements ce mois
4. Organiser les informations de manière claire et structurée

**Structure attendue :**
- Service : `statistique.service.ts` avec méthode `getStatistiquesFinancieres()`
- Composant : `superadmin-finance.component.ts` (ou similaire)
- Interface TypeScript : `StatistiquesFinancieres` avec tous les champs incluant les nouveaux

**Exigences :**
- Utiliser Angular Material pour les cartes et graphiques
- Utiliser Chart.js ou Angular Material Charts pour les graphiques
- Organiser les informations en sections claires (grid Material)
- Afficher un indicateur de chargement pendant la récupération des données
- Gérer les erreurs avec des messages utilisateur appropriés
- Utiliser des observables RxJS (pas de promesses)
- Formater les montants avec le pipe `currency` d'Angular
- Utiliser des couleurs cohérentes (vert pour amiable, bleu pour juridique)
```

---

## 📋 Prompt 5 : Intégration dans le Dashboard Chef Amiable

### Prompt

```
Je dois intégrer l'affichage des montants recouvrés par phase dans le dashboard du Chef Amiable de mon application Angular.

**Contexte :**
- Backend Spring Boot avec endpoints REST
- Frontend Angular avec TypeScript et Angular Material
- Dashboard Chef Amiable affichant les statistiques de son département

**Backend disponible :**
- Endpoint : `GET /api/statistiques/recouvrement-par-phase/departement` - Retourne un objet avec :
  - `montantRecouvrePhaseAmiable` (number) - Montant recouvré en phase amiable pour le département
  - `montantRecouvrePhaseJuridique` (number) - Montant recouvré en phase juridique pour le département
  - `montantRecouvreTotal` (number) - Montant total recouvré pour le département
  - `dossiersAvecRecouvrementAmiable` (number) - Nombre de dossiers avec recouvrement amiable
  - `tauxRecouvrementAmiable` (number) - Taux de recouvrement amiable en pourcentage
  - `montantTotalCreances` (number) - Montant total des créances du département
  - `totalDossiers` (number) - Total des dossiers du département

**Tâches :**
1. Créer/modifier le service Angular pour appeler l'endpoint `/api/statistiques/recouvrement-par-phase/departement`
2. Créer/modifier le composant du dashboard Chef Amiable
3. Afficher dans ce dashboard :
   - **Section "Recouvrement Amiable"** (prioritaire) :
     - Carte principale avec le montant recouvré en phase amiable (grande, mise en avant)
     - Nombre de dossiers avec recouvrement amiable
     - Taux de recouvrement amiable avec indicateur visuel
   - **Section "Vue d'Ensemble"** :
     - Montant total recouvré (amiable + juridique)
     - Montant total des créances
     - Graphique comparatif montrant amiable vs juridique
4. Mettre en avant les statistiques amiable (couleur verte, taille plus grande)

**Structure attendue :**
- Service : `statistique.service.ts` avec méthode `getStatistiquesRecouvrementParPhaseDepartement()`
- Composant : `chef-amiable-dashboard.component.ts` (ou similaire)
- Interface TypeScript : `StatistiquesRecouvrementParPhaseDepartement` avec tous les champs

**Exigences :**
- Utiliser Angular Material pour les cartes et graphiques
- Mettre en avant les statistiques amiable (couleur verte, taille plus grande)
- Afficher un indicateur de chargement pendant la récupération des données
- Gérer les erreurs avec des messages utilisateur appropriés
- Utiliser des observables RxJS (pas de promesses)
- Formater les montants avec le pipe `currency` d'Angular
- Utiliser Chart.js ou Angular Material Charts pour les graphiques
```

---

## 📋 Prompt 6 : Intégration dans le Dashboard Chef Juridique

### Prompt

```
Je dois intégrer l'affichage des montants recouvrés par phase dans le dashboard du Chef Juridique de mon application Angular.

**Contexte :**
- Backend Spring Boot avec endpoints REST
- Frontend Angular avec TypeScript et Angular Material
- Dashboard Chef Juridique affichant les statistiques de son département

**Backend disponible :**
- Endpoint : `GET /api/statistiques/recouvrement-par-phase/departement` - Retourne un objet avec :
  - `montantRecouvrePhaseAmiable` (number) - Montant recouvré en phase amiable pour le département
  - `montantRecouvrePhaseJuridique` (number) - Montant recouvré en phase juridique pour le département
  - `montantRecouvreTotal` (number) - Montant total recouvré pour le département
  - `dossiersAvecRecouvrementJuridique` (number) - Nombre de dossiers avec recouvrement juridique
  - `tauxRecouvrementJuridique` (number) - Taux de recouvrement juridique en pourcentage
  - `montantTotalCreances` (number) - Montant total des créances du département
  - `totalDossiers` (number) - Total des dossiers du département

**Tâches :**
1. Créer/modifier le service Angular pour appeler l'endpoint `/api/statistiques/recouvrement-par-phase/departement`
2. Créer/modifier le composant du dashboard Chef Juridique
3. Afficher dans ce dashboard :
   - **Section "Recouvrement Juridique"** (prioritaire) :
     - Carte principale avec le montant recouvré en phase juridique (grande, mise en avant)
     - Nombre de dossiers avec recouvrement juridique
     - Taux de recouvrement juridique avec indicateur visuel
   - **Section "Vue d'Ensemble"** :
     - Montant total recouvré (amiable + juridique)
     - Montant total des créances
     - Graphique comparatif montrant juridique vs amiable
4. Mettre en avant les statistiques juridique (couleur bleue, taille plus grande)

**Structure attendue :**
- Service : `statistique.service.ts` avec méthode `getStatistiquesRecouvrementParPhaseDepartement()`
- Composant : `chef-juridique-dashboard.component.ts` (ou similaire)
- Interface TypeScript : `StatistiquesRecouvrementParPhaseDepartement` avec tous les champs

**Exigences :**
- Utiliser Angular Material pour les cartes et graphiques
- Mettre en avant les statistiques juridique (couleur bleue, taille plus grande)
- Afficher un indicateur de chargement pendant la récupération des données
- Gérer les erreurs avec des messages utilisateur appropriés
- Utiliser des observables RxJS (pas de promesses)
- Formater les montants avec le pipe `currency` d'Angular
- Utiliser Chart.js ou Angular Material Charts pour les graphiques
```

---

## 📋 Prompt 7 : Intégration dans le Dashboard Chef Finance

### Prompt

```
Je dois intégrer l'affichage des montants recouvrés par phase dans le dashboard du Chef Finance de mon application Angular.

**Contexte :**
- Backend Spring Boot avec endpoints REST
- Frontend Angular avec TypeScript et Angular Material
- Dashboard Chef Finance affichant les statistiques financières de son département

**Backend disponible :**
- Endpoint : `GET /api/statistiques/financieres` - Retourne un objet avec :
  - `montantRecouvrePhaseAmiable` (number) - Montant recouvré en phase amiable ✅ NOUVEAU
  - `montantRecouvrePhaseJuridique` (number) - Montant recouvré en phase juridique ✅ NOUVEAU
  - `montantRecouvre` (number) - Montant total recouvré
  - `montantEnCours` (number) - Montant en cours de recouvrement
  - `totalFraisEngages` (number) - Total des frais engagés
  - `fraisRecuperes` (number) - Frais récupérés
  - `netGenere` (number) - Net généré
  - `totalFactures` (number) - Total des factures
  - `facturesPayees` (number) - Factures payées
  - `facturesEnAttente` (number) - Factures en attente
  - `totalPaiements` (number) - Total des paiements
  - `paiementsCeMois` (number) - Paiements ce mois

**Tâches :**
1. Modifier le service Angular pour récupérer les nouveaux champs `montantRecouvrePhaseAmiable` et `montantRecouvrePhaseJuridique`
2. Créer/modifier le composant du dashboard Chef Finance
3. Afficher dans ce dashboard :
   - **Section "Recouvrement par Phase"** :
     - Carte "Recouvrement Amiable" avec montant et pourcentage du total
     - Carte "Recouvrement Juridique" avec montant et pourcentage du total
     - Graphique comparatif (barres ou camembert) montrant la répartition
   - **Section "Résumé Financier"** :
     - Montant total recouvré
     - Montant en cours
     - Total frais engagés
     - Frais récupérés
     - Net généré
   - **Section "Factures et Paiements"** :
     - Total factures, factures payées, factures en attente
     - Total paiements, paiements ce mois
4. Organiser les informations de manière claire et structurée avec un design professionnel

**Structure attendue :**
- Service : `statistique.service.ts` avec méthode `getStatistiquesFinancieres()`
- Composant : `chef-finance-dashboard.component.ts` (ou similaire)
- Interface TypeScript : `StatistiquesFinancieres` avec tous les champs incluant les nouveaux

**Exigences :**
- Utiliser Angular Material pour les cartes et graphiques
- Utiliser Chart.js ou Angular Material Charts pour les graphiques
- Organiser les informations en sections claires (grid Material)
- Afficher un indicateur de chargement pendant la récupération des données
- Gérer les erreurs avec des messages utilisateur appropriés
- Utiliser des observables RxJS (pas de promesses)
- Formater les montants avec le pipe `currency` d'Angular
- Utiliser des couleurs cohérentes (vert pour amiable, bleu pour juridique)
- Design professionnel et moderne
```

---

## 📋 Prompt 8 : Création d'un Composant Réutilisable pour les Montants par Phase

### Prompt

```
Je dois créer un composant Angular réutilisable pour afficher les montants recouvrés par phase (amiable et juridique) dans différents dashboards de mon application.

**Contexte :**
- Backend Spring Boot avec endpoints REST
- Frontend Angular avec TypeScript et Angular Material
- Plusieurs dashboards nécessitent l'affichage des montants par phase

**Données à afficher :**
- `montantRecouvrePhaseAmiable` (number) - Montant recouvré en phase amiable
- `montantRecouvrePhaseJuridique` (number) - Montant recouvré en phase juridique
- `montantRecouvreTotal` (number) - Montant total recouvré (optionnel, peut être calculé)

**Tâches :**
1. Créer un composant réutilisable `montants-par-phase.component.ts`
2. Le composant doit accepter les montants en input (via `@Input()`)
3. Afficher :
   - Deux cartes Material côte à côte (ou en colonne sur mobile)
   - Carte "Recouvrement Amiable" avec :
     - Icône Material (ex: handshake)
     - Montant formaté en TND
     - Pourcentage du total (si montantRecouvreTotal fourni)
     - Couleur verte
   - Carte "Recouvrement Juridique" avec :
     - Icône Material (ex: gavel)
     - Montant formaté en TND
     - Pourcentage du total (si montantRecouvreTotal fourni)
     - Couleur bleue
4. Optionnel : Afficher un graphique comparatif (barres ou camembert)
5. Gérer les cas où les montants sont 0 ou null
6. Responsive design (adaptation mobile/tablette/desktop)

**Structure attendue :**
- Composant : `montants-par-phase.component.ts`
- Template : `montants-par-phase.component.html`
- Styles : `montants-par-phase.component.scss`
- Interface TypeScript : `MontantsParPhase` pour les inputs

**Exigences :**
- Utiliser Angular Material pour les cartes
- Utiliser des icônes Material appropriées
- Formater les montants avec le pipe `currency` d'Angular
- Responsive design avec Angular Material Grid
- Gérer les cas d'erreur (montants null ou undefined)
- Optionnel : Intégrer Chart.js ou Angular Material Charts pour les graphiques
- Utiliser OnPush change detection pour la performance
```

---

## 📋 Résumé des Endpoints Backend

### Endpoints Disponibles

1. **`GET /api/statistiques/globales`**
   - Retourne : `montantRecouvrePhaseAmiable`, `montantRecouvrePhaseJuridique`, `montantRecouvre`
   - Autorisation : `SUPER_ADMIN`, `CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE`

2. **`GET /api/statistiques/financieres`**
   - Retourne : `montantRecouvrePhaseAmiable`, `montantRecouvrePhaseJuridique`, `montantRecouvre`, et autres statistiques financières
   - Autorisation : `SUPER_ADMIN`, `CHEF_DEPARTEMENT_FINANCE`

3. **`GET /api/statistiques/recouvrement-par-phase`**
   - Retourne : Statistiques détaillées de recouvrement par phase (montants, taux, nombre de dossiers)
   - Autorisation : `SUPER_ADMIN`, `CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE`, `CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE`, `CHEF_DEPARTEMENT_FINANCE`

4. **`GET /api/statistiques/recouvrement-par-phase/departement`**
   - Retourne : Statistiques de recouvrement par phase filtrées par département du chef connecté
   - Autorisation : `CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE`, `CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE`, `CHEF_DEPARTEMENT_FINANCE`, `SUPER_ADMIN`

---

## 📝 Notes d'Utilisation

### Comment Utiliser ces Prompts

1. **Copier le prompt complet** dans votre outil de développement IA (ChatGPT, Claude, etc.)
2. **Adapter le prompt** selon vos besoins spécifiques et la structure de votre application
3. **Tester les modifications** après chaque implémentation
4. **Itérer** si nécessaire pour affiner les résultats

### Structure Recommandée

Pour chaque prompt :
1. Lire attentivement le contexte et les exigences
2. Vérifier que les endpoints backend sont disponibles et fonctionnels
3. Implémenter les modifications étape par étape
4. Tester chaque fonctionnalité avec des données réelles
5. Documenter les changements

### Priorités

1. **Priorité Haute :**
   - Prompt 1 : Intégration dans les Statistiques Globales
   - Prompt 4 : Intégration dans le Dashboard SuperAdmin - Finance
   - Prompt 7 : Intégration dans le Dashboard Chef Finance

2. **Priorité Moyenne :**
   - Prompt 2 : Intégration dans le Dashboard SuperAdmin - Recouvrement Amiable
   - Prompt 3 : Intégration dans le Dashboard SuperAdmin - Recouvrement Juridique
   - Prompt 5 : Intégration dans le Dashboard Chef Amiable
   - Prompt 6 : Intégration dans le Dashboard Chef Juridique

3. **Priorité Basse :**
   - Prompt 8 : Création d'un Composant Réutilisable

---

**Date :** 2025-01-05  
**Status :** ✅ Prompts prêts pour intégration frontend

