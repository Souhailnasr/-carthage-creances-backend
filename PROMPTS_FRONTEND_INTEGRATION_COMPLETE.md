# 🎯 Prompts Complets - Intégration Frontend et Interfaces Chefs

## 📋 Table des Matières

1. [Prompts pour l'Intégration des APIs](#1-prompts-pour-lintégration-des-apis)
2. [Prompts pour les Interfaces Chefs Recouvrement Amiable](#2-prompts-pour-les-interfaces-chefs-recouvrement-amiable)
3. [Prompts pour les Tests et Validation](#3-prompts-pour-les-tests-et-validation)
4. [Prompts pour la Gestion des Erreurs](#4-prompts-pour-la-gestion-des-erreurs)

---

## 1. Prompts pour l'Intégration des APIs

### 📋 PROMPT 1 : Mettre à Jour le Service DossierService

```
Dans le projet Angular, localisez le service DossierService (src/app/services/dossier.service.ts).

Ajoutez les méthodes suivantes pour consommer les nouvelles APIs d'affectation et de filtrage :

1. getDossiersRecouvrementAmiable(params?: {page?: number, size?: number, sort?: string}): Observable<any>
   - GET /api/dossiers/recouvrement-amiable
   - Paramètres optionnels : page (défaut: 0), size (défaut: 10), sort (défaut: "dateCreation")
   - Retourne un objet avec la liste des dossiers et les métadonnées de pagination
   - Filtre automatiquement : typeRecouvrement = AMIABLE, valide = true, dossierStatus = ENCOURSDETRAITEMENT
   - Gère les erreurs : 400 (paramètres invalides), 500 (erreur serveur)

2. getDossiersRecouvrementJuridique(params?: {page?: number, size?: number, sort?: string}): Observable<any>
   - GET /api/dossiers/recouvrement-juridique
   - Même logique que pour amiable mais pour le recouvrement juridique
   - Filtre automatiquement : typeRecouvrement = JURIDIQUE, valide = true, dossierStatus = ENCOURSDETRAITEMENT

3. affecterAuRecouvrementAmiable(dossierId: number): Observable<Dossier>
   - PUT /api/dossiers/{dossierId}/affecter/recouvrement-amiable
   - Retourne le dossier mis à jour
   - Gère les erreurs : 400 (dossier non validé, chef non trouvé, avocat/huissier présent), 404 (dossier non trouvé), 500

4. affecterAuRecouvrementJuridique(dossierId: number): Observable<Dossier>
   - PUT /api/dossiers/{dossierId}/affecter/recouvrement-juridique
   - Retourne le dossier mis à jour
   - Gère les erreurs : 400 (dossier non validé, chef non trouvé), 404 (dossier non trouvé), 500

5. cloturerDossier(dossierId: number): Observable<Dossier>
   - PUT /api/dossiers/{dossierId}/cloturer
   - Retourne le dossier clôturé
   - Gère les erreurs : 400 (dossier non validé), 404 (dossier non trouvé), 500

6. getDossiersValidesDisponibles(params?: {page?: number, size?: number, sort?: string, direction?: string, search?: string}): Observable<any>
   - GET /api/dossiers/valides-disponibles
   - Paramètres optionnels pour pagination, tri et recherche
   - Retourne un objet avec la liste des dossiers et les métadonnées de pagination

IMPORTANT :
- Utilisez HttpClient avec les headers Authorization si nécessaire
- Ajoutez la gestion d'erreurs avec catchError et throwError
- Utilisez des messages d'erreur en français
- Loggez les erreurs avec console.error pour le débogage
- Retournez des Observables typés
```

---

### 📋 PROMPT 2 : Créer le Modèle TypeRecouvrement

```
Dans le projet Angular, créez un enum TypeRecouvrement dans le fichier src/app/models/type-recouvrement.ts :

export enum TypeRecouvrement {
  NON_AFFECTE = 'NON_AFFECTE',
  AMIABLE = 'AMIABLE',
  JURIDIQUE = 'JURIDIQUE'
}

Mettez à jour l'interface Dossier dans src/app/models/dossier.ts pour inclure :
- typeRecouvrement?: TypeRecouvrement
- utilisateurs?: Utilisateur[] (liste des utilisateurs associés au dossier)

Assurez-vous que l'interface Dossier inclut tous les champs nécessaires :
- id, titre, description, numeroDossier, montantCreance
- statut, valide, dossierStatus
- typeRecouvrement, agentCreateur, agentResponsable
- utilisateurs, dateCreation, dateCloture
- creancier, debiteur, urgence
```

---

### 📋 PROMPT 3 : Créer le Composant Liste Dossiers Recouvrement Amiable

```
Dans le projet Angular, créez un composant pour afficher les dossiers de recouvrement amiable :

Fichier : src/app/components/dossiers-recouvrement-amiable/dossiers-recouvrement-amiable.component.ts

Fonctionnalités requises :

1. Propriétés :
   - dossiers: Dossier[] = []
   - page: number = 0
   - size: number = 10
   - totalElements: number = 0
   - totalPages: number = 0
   - loading: boolean = false
   - error: string | null = null

2. Méthodes :
   - ngOnInit(): void - Charge les dossiers au démarrage
   - loadDossiers(): void - Charge les dossiers avec pagination
   - onPageChange(page: number): void - Gère le changement de page
   - onSizeChange(size: number): void - Gère le changement de taille de page
   - refreshDossiers(): void - Rafraîchit la liste
   - getStatutBadgeClass(statut: string): string - Retourne la classe CSS pour le badge de statut
   - getUrgenceBadgeClass(urgence: string): string - Retourne la classe CSS pour le badge d'urgence

3. Intégration :
   - Utilisez DossierService pour charger les dossiers
   - Affichez un loader pendant le chargement
   - Affichez les erreurs de manière user-friendly
   - Implémentez la pagination avec ngx-pagination ou mat-paginator

4. Template HTML :
   - Tableau avec colonnes : Numéro, Titre, Montant, Statut, Urgence, Date Création, Actions
   - Bouton "Rafraîchir"
   - Pagination en bas
   - Messages d'erreur et de chargement
```

---

### 📋 PROMPT 4 : Créer le Template HTML pour Liste Dossiers Amiable

```
Dans le projet Angular, créez le template HTML pour le composant dossiers-recouvrement-amiable :

Fichier : src/app/components/dossiers-recouvrement-amiable/dossiers-recouvrement-amiable.component.html

Structure requise :

1. En-tête :
   - Titre "Dossiers Recouvrement Amiable"
   - Bouton "Rafraîchir" avec icône
   - Badge affichant le nombre total de dossiers

2. Tableau des dossiers :
   - Colonnes : Numéro, Titre, Montant, Statut, Urgence, Date Création, Actions
   - Lignes cliquables pour voir les détails
   - Badges colorés pour statut et urgence
   - Format de date lisible (ex: "15 Nov 2025")

3. Actions par ligne :
   - Bouton "Voir Détails" (icône œil)
   - Bouton "Clôturer" (icône check) - si le dossier est validé
   - Bouton "Réaffecter" (icône refresh) - si nécessaire

4. Pagination :
   - Utilisez mat-paginator ou ngx-pagination
   - Affichez "Page X sur Y"
   - Boutons précédent/suivant

5. États :
   - Message "Aucun dossier trouvé" si la liste est vide
   - Spinner de chargement pendant le chargement
   - Message d'erreur en cas d'erreur

6. Styles :
   - Utilisez Angular Material ou Bootstrap
   - Responsive design
   - Couleurs cohérentes avec le thème de l'application
```

---

### 📋 PROMPT 5 : Créer le Composant Détails Dossier avec Actions

```
Dans le projet Angular, créez ou mettez à jour le composant de détails de dossier :

Fichier : src/app/components/dossier-details/dossier-details.component.ts

Fonctionnalités requises :

1. Propriétés :
   - dossier: Dossier | null = null
   - dossierId: number | null = null
   - loading: boolean = false
   - error: string | null = null
   - canCloturer: boolean = false
   - canReaffecter: boolean = false

2. Méthodes :
   - ngOnInit(): void - Charge le dossier si dossierId est fourni
   - loadDossier(id: number): void - Charge un dossier par ID
   - cloturerDossier(): void - Clôture le dossier avec confirmation
   - reaffecterAuJuridique(): void - Réaffecte au juridique avec confirmation
   - confirmerAction(message: string): Promise<boolean> - Affiche un dialog de confirmation
   - afficherMessage(type: 'success' | 'error', message: string): void - Affiche un message

3. Logique métier :
   - canCloturer = dossier.valide && dossier.dossierStatus !== 'CLOTURE'
   - canReaffecter = dossier.typeRecouvrement === 'AMIABLE' && dossier.valide
   - Vérifiez les permissions de l'utilisateur connecté

4. Intégration :
   - Utilisez DossierService pour les opérations
   - Utilisez MatDialog pour les confirmations
   - Utilisez MatSnackBar pour les messages
   - Redirigez après clôture réussie
```

---

### 📋 PROMPT 6 : Créer le Template Détails Dossier avec Actions

```
Dans le projet Angular, créez le template HTML pour les détails de dossier :

Fichier : src/app/components/dossier-details/dossier-details.component.html

Structure requise :

1. En-tête :
   - Titre du dossier
   - Badge du statut (VALIDÉ, EN_ATTENTE, CLÔTURÉ)
   - Badge du type de recouvrement (AMIABLE, JURIDIQUE, NON_AFFECTÉ)
   - Badge d'urgence

2. Informations principales :
   - Numéro de dossier
   - Montant de créance (formaté en devise)
   - Date de création
   - Date de clôture (si applicable)
   - Agent créateur
   - Agent responsable

3. Informations complémentaires :
   - Créancier (nom, coordonnées)
   - Débiteur (nom, coordonnées)
   - Description
   - Documents joints (contrat, pouvoir)

4. Section Utilisateurs Associés :
   - Liste des utilisateurs associés au dossier (chef + agents)
   - Affichez nom, prénom, rôle de chaque utilisateur
   - Badge pour distinguer chef et agents

5. Actions disponibles :
   - Bouton "Clôturer" (si canCloturer = true)
     - Couleur : primary
     - Icône : check_circle
     - Confirmation requise
   - Bouton "Réaffecter au Juridique" (si canReaffecter = true)
     - Couleur : accent
     - Icône : gavel
     - Confirmation requise
   - Bouton "Retour à la liste"
   - Bouton "Imprimer" (optionnel)

6. États :
   - Spinner pendant le chargement
   - Message d'erreur si erreur
   - Message "Dossier non trouvé" si null

7. Styles :
   - Utilisez des cards Material Design
   - Sections bien séparées
   - Responsive
```

---

## 2. Prompts pour les Interfaces Chefs Recouvrement Amiable

### 📋 PROMPT 7 : Créer le Module Dashboard Chef Recouvrement Amiable

```
Dans le projet Angular, créez un module complet pour le dashboard du chef de recouvrement amiable :

Fichier : src/app/modules/chef-recouvrement-amiable/chef-recouvrement-amiable.module.ts

Structure requise :

1. Imports :
   - CommonModule
   - FormsModule, ReactiveFormsModule
   - HttpClientModule
   - Angular Material (MatTableModule, MatPaginatorModule, MatButtonModule, MatCardModule, MatDialogModule, MatSnackBarModule, MatIconModule, MatBadgeModule, MatProgressSpinnerModule)

2. Components :
   - ChefRecouvrementAmiableDashboardComponent (composant principal)
   - DossiersRecouvrementAmiableComponent (liste des dossiers)
   - DossierDetailsComponent (détails d'un dossier)
   - StatistiquesRecouvrementAmiableComponent (statistiques)

3. Services :
   - ChefRecouvrementAmiableService (service dédié)

4. Routes :
   - /chef-recouvrement-amiable/dashboard
   - /chef-recouvrement-amiable/dossiers
   - /chef-recouvrement-amiable/dossiers/:id
   - /chef-recouvrement-amiable/statistiques

5. Guards :
   - ChefRecouvrementAmiableGuard (vérifie que l'utilisateur est chef amiable)
```

---

### 📋 PROMPT 8 : Créer le Service Chef Recouvrement Amiable

```
Dans le projet Angular, créez un service dédié pour le chef de recouvrement amiable :

Fichier : src/app/services/chef-recouvrement-amiable.service.ts

Fonctionnalités requises :

1. Méthodes pour les dossiers :
   - getMesDossiers(params?: PaginationParams): Observable<Page<Dossier>>
     - Charge les dossiers affectés au recouvrement amiable
     - Filtre par agentResponsable = utilisateur connecté
   - getDossierById(id: number): Observable<Dossier>
   - getDossiersParAgent(agentId: number): Observable<Dossier[]>
   - getDossiersEnAttente(): Observable<Dossier[]>
   - getDossiersEnCours(): Observable<Dossier[]>
   - getDossiersClotures(): Observable<Dossier[]>

2. Méthodes pour les actions :
   - assignerDossierAagent(dossierId: number, agentId: number): Observable<Dossier>
     - PUT /api/dossiers/{dossierId}/assign/agent?agentId={agentId}
   - cloturerDossier(dossierId: number): Observable<Dossier>
   - reaffecterAuJuridique(dossierId: number): Observable<Dossier>
   - ajouterAgentAuDossier(dossierId: number, agentId: number): Observable<Dossier>
     - Ajoute un agent à la liste utilisateurs du dossier

3. Méthodes pour les statistiques :
   - getStatistiques(): Observable<StatistiquesRecouvrementAmiable>
     - Nombre total de dossiers
     - Dossiers en cours
     - Dossiers clôturés ce mois
     - Répartition par agent
     - Montant total en cours

4. Méthodes pour les agents :
   - getMesAgents(): Observable<Utilisateur[]>
     - Charge les agents du département recouvrement amiable
   - getAgentsDisponibles(): Observable<Utilisateur[]>
     - Agents avec moins de dossiers

5. Gestion d'erreurs :
   - Messages d'erreur en français
   - Logging pour débogage
   - Retry logic pour les erreurs réseau
```

---

### 📋 PROMPT 9 : Créer le Composant Dashboard Chef Recouvrement Amiable

```
Dans le projet Angular, créez le composant principal du dashboard du chef :

Fichier : src/app/components/chef-recouvrement-amiable-dashboard/chef-recouvrement-amiable-dashboard.component.ts

Fonctionnalités requises :

1. Structure du dashboard :
   - Sidebar avec navigation
   - Zone principale avec router-outlet
   - Header avec informations utilisateur et notifications

2. Sections du dashboard :
   - Vue d'ensemble (statistiques)
   - Liste des dossiers
   - Détails d'un dossier
   - Gestion des agents
   - Statistiques détaillées

3. Propriétés :
   - currentUser: Utilisateur | null = null
   - notifications: Notification[] = []
   - statistiques: StatistiquesRecouvrementAmiable | null = null

4. Méthodes :
   - ngOnInit(): void - Charge les données initiales
   - loadStatistiques(): void - Charge les statistiques
   - loadNotifications(): void - Charge les notifications
   - navigateTo(route: string): void - Navigation
   - logout(): void - Déconnexion

5. Guards :
   - Vérifie que l'utilisateur est chef de recouvrement amiable
   - Redirige vers login si non autorisé
```

---

### 📋 PROMPT 10 : Créer le Template Dashboard Chef avec Navigation

```
Dans le projet Angular, créez le template HTML pour le dashboard du chef :

Fichier : src/app/components/chef-recouvrement-amiable-dashboard/chef-recouvrement-amiable-dashboard.component.html

Structure requise :

1. Layout principal :
   - MatSidenavContainer avec sidebar et contenu principal
   - Sidebar fixe à gauche
   - Zone de contenu avec router-outlet

2. Sidebar :
   - Logo/icône de l'application
   - Menu de navigation :
     * Dashboard (icône dashboard)
     * Mes Dossiers (icône folder)
     * Dossiers en Attente (icône pending)
     * Statistiques (icône bar_chart)
     * Gestion Agents (icône people)
     * Paramètres (icône settings)
   - Indicateur visuel pour la section active
   - Badge de notifications sur "Dossiers en Attente"

3. Header :
   - Titre de la section active
   - Informations utilisateur (nom, rôle)
   - Bouton notifications (avec badge)
   - Bouton déconnexion

4. Zone de contenu :
   - Router outlet pour afficher les composants enfants
   - Breadcrumb (optionnel)

5. Styles :
   - Utilisez Angular Material
   - Thème cohérent
   - Responsive (sidebar se cache sur mobile)
```

---

### 📋 PROMPT 11 : Créer le Composant Liste Dossiers avec Filtres et Actions

```
Dans le projet Angular, créez un composant avancé pour la liste des dossiers :

Fichier : src/app/components/dossiers-list-chef/dossiers-list-chef.component.ts

Fonctionnalités requises :

1. Filtres :
   - Par statut (Tous, En cours, Clôturés)
   - Par agent (Tous, ou sélection d'un agent)
   - Par urgence (Tous, Faible, Moyenne, Élevée)
   - Par date (création, clôture)
   - Recherche textuelle (numéro, titre, description)

2. Tri :
   - Par date de création (croissant/décroissant)
   - Par montant (croissant/décroissant)
   - Par urgence
   - Par statut

3. Actions en masse :
   - Sélection multiple de dossiers
   - Clôture en masse (avec confirmation)
   - Réaffectation en masse au juridique
   - Export Excel/PDF (optionnel)

4. Actions par dossier :
   - Voir détails
   - Assigner à un agent
   - Clôturer
   - Réaffecter au juridique
   - Ajouter un commentaire

5. Affichage :
   - Tableau avec colonnes configurables
   - Vue en grille (cards) optionnelle
   - Pagination avancée
   - Export des résultats

6. Propriétés :
   - dossiers: Dossier[] = []
   - filteredDossiers: Dossier[] = []
   - selectedDossiers: Dossier[] = []
   - filters: DossierFilters = {}
   - sortBy: string = 'dateCreation'
   - sortDirection: 'asc' | 'desc' = 'desc'
   - viewMode: 'table' | 'grid' = 'table'

7. Méthodes :
   - applyFilters(): void
   - clearFilters(): void
   - sortDossiers(field: string): void
   - toggleSelection(dossier: Dossier): void
   - selectAll(): void
   - deselectAll(): void
   - bulkCloturer(): void
   - bulkReaffecter(): void
```

---

### 📋 PROMPT 12 : Créer le Composant Assignation Dossier à Agent

```
Dans le projet Angular, créez un composant dialog pour assigner un dossier à un agent :

Fichier : src/app/components/assign-dossier-agent/assign-dossier-agent.component.ts

Fonctionnalités requises :

1. Dialog Material :
   - Titre : "Assigner le dossier à un agent"
   - Liste des agents disponibles
   - Recherche d'agent
   - Affichage des statistiques de chaque agent (nombre de dossiers)

2. Propriétés :
   - dossier: Dossier (injecté)
   - agents: Utilisateur[] = []
   - selectedAgent: Utilisateur | null = null
   - searchTerm: string = ''
   - loading: boolean = false

3. Méthodes :
   - ngOnInit(): void - Charge les agents
   - loadAgents(): void - Charge la liste des agents
   - filterAgents(): Utilisateur[] - Filtre les agents par recherche
   - selectAgent(agent: Utilisateur): void - Sélectionne un agent
   - assigner(): void - Confirme l'assignation
   - cancel(): void - Annule le dialog

4. Affichage des agents :
   - Nom et prénom
   - Email
   - Nombre de dossiers en cours
   - Disponibilité (badge vert/rouge)
   - Charge de travail (barre de progression)

5. Validation :
   - Un agent doit être sélectionné
   - Afficher un message si aucun agent disponible
```

---

### 📋 PROMPT 13 : Créer le Composant Statistiques Recouvrement Amiable

```
Dans le projet Angular, créez un composant pour afficher les statistiques :

Fichier : src/app/components/statistiques-recouvrement-amiable/statistiques-recouvrement-amiable.component.ts

Fonctionnalités requises :

1. Statistiques globales (cards) :
   - Total dossiers en cours
   - Dossiers clôturés ce mois
   - Montant total en cours
   - Taux de clôture

2. Graphiques :
   - Répartition par statut (pie chart)
   - Évolution mensuelle (line chart)
   - Répartition par agent (bar chart)
   - Répartition par urgence (bar chart)

3. Tableaux :
   - Top 5 agents par performance
   - Dossiers les plus anciens
   - Dossiers avec montant le plus élevé

4. Filtres temporels :
   - Période (semaine, mois, trimestre, année)
   - Date de début / Date de fin

5. Propriétés :
   - statistiques: StatistiquesRecouvrementAmiable | null = null
   - period: 'week' | 'month' | 'quarter' | 'year' = 'month'
   - startDate: Date
   - endDate: Date

6. Méthodes :
   - loadStatistiques(): void
   - updatePeriod(period: string): void
   - exportStatistiques(): void
   - refresh(): void

7. Bibliothèques recommandées :
   - Chart.js ou ng2-charts pour les graphiques
   - Date picker Material pour les dates
```

---

## 3. Prompts pour les Tests et Validation

### 📋 PROMPT 14 : Créer les Tests Unitaires pour les Services

```
Dans le projet Angular, créez des tests unitaires pour les services :

Fichier : src/app/services/dossier.service.spec.ts

Tests à créer :

1. Tests pour getDossiersRecouvrementAmiable :
   - Doit appeler GET /api/dossiers/recouvrement-amiable avec les bons paramètres
   - Doit retourner les données paginées
   - Doit gérer les erreurs correctement

2. Tests pour affecterAuRecouvrementAmiable :
   - Doit appeler PUT /api/dossiers/{id}/affecter/recouvrement-amiable
   - Doit retourner le dossier mis à jour
   - Doit gérer les erreurs 400, 404, 500

3. Tests pour cloturerDossier :
   - Doit appeler PUT /api/dossiers/{id}/cloturer
   - Doit retourner le dossier clôturé
   - Doit gérer les erreurs

4. Utilisez :
   - HttpClientTestingModule
   - TestBed
   - jasmine.createSpy
   - fakeAsync et tick pour les observables
```

---

### 📋 PROMPT 15 : Créer les Tests E2E pour les Interfaces Chef

```
Dans le projet Angular, créez des tests E2E avec Protractor ou Cypress :

Fichier : e2e/chef-recouvrement-amiable.e2e-spec.ts

Scénarios à tester :

1. Connexion en tant que chef recouvrement amiable :
   - Se connecter avec un compte chef amiable
   - Vérifier la redirection vers le dashboard
   - Vérifier l'affichage du menu

2. Affichage de la liste des dossiers :
   - Naviguer vers "Mes Dossiers"
   - Vérifier l'affichage des dossiers
   - Vérifier la pagination

3. Filtrage des dossiers :
   - Appliquer un filtre par statut
   - Vérifier que seuls les dossiers filtrés s'affichent
   - Réinitialiser les filtres

4. Assignation d'un dossier à un agent :
   - Cliquer sur "Assigner" pour un dossier
   - Sélectionner un agent
   - Confirmer l'assignation
   - Vérifier le message de succès

5. Clôture d'un dossier :
   - Cliquer sur "Clôturer" pour un dossier
   - Confirmer dans le dialog
   - Vérifier que le dossier disparaît de la liste

6. Navigation :
   - Tester tous les liens du menu
   - Vérifier que le contenu change correctement
```

---

## 4. Prompts pour la Gestion des Erreurs

### 📋 PROMPT 16 : Créer un Service de Gestion d'Erreurs Centralisé

```
Dans le projet Angular, créez un service centralisé pour la gestion des erreurs :

Fichier : src/app/services/error-handler.service.ts

Fonctionnalités requises :

1. Méthodes :
   - handleHttpError(error: HttpErrorResponse): Observable<never>
     - Analyse le code d'erreur HTTP
     - Retourne un message d'erreur en français
     - Log l'erreur pour le débogage
   - getErrorMessage(error: any): string
     - Convertit les erreurs en messages lisibles
   - showError(message: string): void
     - Affiche un message d'erreur avec MatSnackBar
   - showSuccess(message: string): void
     - Affiche un message de succès

2. Mapping des erreurs :
   - 400 : "Requête invalide"
   - 401 : "Non autorisé. Veuillez vous reconnecter."
   - 403 : "Accès interdit"
   - 404 : "Ressource non trouvée"
   - 500 : "Erreur serveur. Veuillez réessayer plus tard."
   - Messages spécifiques selon le contexte

3. Intégration :
   - Intercepteur HTTP pour capturer toutes les erreurs
   - Service injectable dans tous les composants
   - Configuration centralisée des messages
```

---

### 📋 PROMPT 17 : Créer un Intercepteur HTTP pour la Gestion des Erreurs

```
Dans le projet Angular, créez un intercepteur HTTP :

Fichier : src/app/interceptors/error.interceptor.ts

Fonctionnalités requises :

1. Intercepte toutes les requêtes HTTP :
   - Ajoute le token d'authentification si présent
   - Gère les erreurs HTTP
   - Retry logic pour les erreurs réseau

2. Gestion des erreurs :
   - 401 : Redirige vers la page de connexion
   - 403 : Affiche un message d'erreur
   - 404 : Affiche un message spécifique
   - 500 : Affiche un message générique
   - Erreurs réseau : Affiche "Problème de connexion"

3. Logging :
   - Log toutes les erreurs pour le débogage
   - N'envoie pas d'informations sensibles

4. Configuration :
   - Enregistrez l'intercepteur dans app.module.ts
   - Utilisez HTTP_INTERCEPTORS
```

---

## 📝 Checklist d'Intégration

### ✅ Avant de Commencer

- [ ] Vérifier que toutes les APIs backend sont fonctionnelles
- [ ] Tester les endpoints avec Postman ou un client REST
- [ ] Vérifier les modèles de données (interfaces TypeScript)
- [ ] S'assurer que l'authentification JWT fonctionne

### ✅ Intégration des Services

- [ ] Créer/mettre à jour DossierService avec toutes les méthodes
- [ ] Créer ChefRecouvrementAmiableService
- [ ] Créer ErrorHandlerService
- [ ] Tester chaque méthode du service individuellement

### ✅ Création des Composants

- [ ] Composant dashboard chef
- [ ] Composant liste dossiers
- [ ] Composant détails dossier
- [ ] Composant assignation agent
- [ ] Composant statistiques

### ✅ Intégration des Routes

- [ ] Configurer les routes dans le module
- [ ] Créer les guards de sécurité
- [ ] Tester la navigation

### ✅ Tests

- [ ] Tests unitaires pour les services
- [ ] Tests unitaires pour les composants
- [ ] Tests E2E pour les scénarios principaux

### ✅ Déploiement

- [ ] Vérifier que tout fonctionne en production
- [ ] Tester avec des données réelles
- [ ] Documenter les fonctionnalités

---

## 🎯 Résumé des Prompts

### Intégration des APIs (1-6)
1. **PROMPT 1** : Mettre à jour DossierService avec toutes les APIs
2. **PROMPT 2** : Créer le modèle TypeRecouvrement
3. **PROMPT 3** : Créer le composant liste dossiers amiable
4. **PROMPT 4** : Créer le template HTML pour la liste
5. **PROMPT 5** : Créer le composant détails avec actions
6. **PROMPT 6** : Créer le template détails avec actions

### Interfaces Chefs (7-13)
7. **PROMPT 7** : Créer le module dashboard chef
8. **PROMPT 8** : Créer le service chef recouvrement amiable
9. **PROMPT 9** : Créer le composant dashboard principal
10. **PROMPT 10** : Créer le template dashboard avec navigation
11. **PROMPT 11** : Créer le composant liste avancée avec filtres
12. **PROMPT 12** : Créer le composant assignation agent
13. **PROMPT 13** : Créer le composant statistiques

### Tests et Erreurs (14-17)
14. **PROMPT 14** : Créer les tests unitaires
15. **PROMPT 15** : Créer les tests E2E
16. **PROMPT 16** : Créer le service gestion d'erreurs
17. **PROMPT 17** : Créer l'intercepteur HTTP

### Gestion des Actions (18-27) ⭐ NOUVEAU
18. **PROMPT 18** : Créer le Service ActionService (Frontend)
19. **PROMPT 19** : Créer le Composant Tableau des Actions d'un Dossier
20. **PROMPT 20** : Créer le Template HTML du Tableau des Actions
21. **PROMPT 21** : Créer le Dialog d'Ajout/Modification d'Action
22. **PROMPT 22** : Créer le Template HTML du Dialog d'Action
23. **PROMPT 23** : Intégrer la Logique de Décision (Finance vs Juridique)
24. **PROMPT 24** : Créer le Composant de Recommandations et Actions Rapides
25. **PROMPT 25** : Mettre à Jour le Service Chef Recouvrement Amiable avec Actions
26. **PROMPT 26** : Mettre à Jour le Composant Détails Dossier avec Actions
27. **PROMPT 27** : Créer le Composant Vue d'Ensemble Actions (Dashboard)

---

## 📚 Ressources Supplémentaires

- Documentation Angular Material : https://material.angular.io/
- Documentation RxJS : https://rxjs.dev/
- Guide Angular HTTP : https://angular.io/guide/http
- Guide Angular Routing : https://angular.io/guide/router

---

---

## 5. Prompts pour la Gestion des Actions de Recouvrement

### 📋 PROMPT 18 : Créer le Service ActionService (Frontend)

```
Dans le projet Angular, créez un service pour gérer les actions de recouvrement :

Fichier : src/app/services/action.service.ts

Fonctionnalités requises :

1. Méthodes CRUD :
   - createAction(action: Action): Observable<Action>
     - POST /api/actions
     - Crée une nouvelle action pour un dossier
   - updateAction(id: number, action: Action): Observable<Action>
     - PUT /api/actions/{id}
   - deleteAction(id: number): Observable<void>
     - DELETE /api/actions/{id}
   - getActionById(id: number): Observable<Action>
     - GET /api/actions/{id}

2. Méthodes de recherche :
   - getActionsByDossier(dossierId: number): Observable<Action[]>
     - GET /api/actions/dossier/{dossierId}
     - Retourne toutes les actions d'un dossier, triées par date (plus récentes en premier)
   - getActionsByType(type: TypeAction): Observable<Action[]>
     - GET /api/actions/type/{type}
   - getActionsByDate(date: Date): Observable<Action[]>
     - GET /api/actions/date/{date}
   - getActionsByDateRange(startDate: Date, endDate: Date): Observable<Action[]>
     - GET /api/actions/date-range?startDate=...&endDate=...

3. Méthodes pour ReponseDebiteur :
   - getActionsByReponseDebiteur(dossierId: number, reponse: ReponseDebiteur): Observable<Action[]>
     - GET /api/actions/dossier/{dossierId}/reponse/{reponse}
   - getActionsWithPositiveResponse(dossierId: number): Observable<Action[]>
     - Filtre les actions avec reponseDebiteur = POSITIVE
   - getActionsWithNegativeResponse(dossierId: number): Observable<Action[]>
     - Filtre les actions avec reponseDebiteur = NEGATIVE
   - getActionsWithoutResponse(dossierId: number): Observable<Action[]>
     - Filtre les actions sans réponse

4. Méthodes de calcul :
   - getTotalCostByDossier(dossierId: number): Observable<number>
     - GET /api/actions/dossier/{dossierId}/total-cost
     - Retourne le coût total des actions pour un dossier

5. Gestion d'erreurs :
   - Messages d'erreur en français
   - Logging pour débogage
   - Retry logic pour les erreurs réseau

IMPORTANT :
- Créez les interfaces TypeScript correspondantes :
  - Action (id, type, reponseDebiteur, dateAction, nbOccurrences, coutUnitaire, dossier, finance)
  - TypeAction (enum: APPEL, EMAIL, VISITE, LETTRE, AUTRE)
  - ReponseDebiteur (enum: POSITIVE, NEGATIVE)
```

---

### 📋 PROMPT 19 : Créer le Composant Tableau des Actions d'un Dossier

```
Dans le projet Angular, créez un composant pour afficher toutes les actions d'un dossier :

Fichier : src/app/components/dossier-actions/dossier-actions.component.ts

Fonctionnalités requises :

1. Propriétés :
   - dossierId: number (input)
   - actions: Action[] = []
   - loading: boolean = false
   - error: string | null = null
   - totalCost: number = 0
   - filteredActions: Action[] = []

2. Filtres :
   - filterByType: TypeAction | null = null
   - filterByReponse: ReponseDebiteur | null = null
   - filterByDateRange: {start: Date | null, end: Date | null} = {start: null, end: null}

3. Méthodes :
   - ngOnInit(): void - Charge les actions au démarrage
   - loadActions(): void - Charge toutes les actions du dossier
   - loadTotalCost(): void - Charge le coût total
   - applyFilters(): void - Applique les filtres
   - clearFilters(): void - Réinitialise les filtres
   - addAction(): void - Ouvre le dialog pour ajouter une action
   - editAction(action: Action): void - Ouvre le dialog pour modifier une action
   - deleteAction(action: Action): void - Supprime une action avec confirmation
   - getTypeActionLabel(type: TypeAction): string - Retourne le libellé en français
   - getReponseLabel(reponse: ReponseDebiteur | null): string - Retourne le libellé en français
   - getReponseBadgeClass(reponse: ReponseDebiteur | null): string - Retourne la classe CSS pour le badge

4. Logique métier :
   - Analyse les réponses du débiteur pour déterminer la collaboration
   - Calcule le nombre d'actions positives vs négatives
   - Détermine si le dossier doit passer au juridique ou au finance

5. Intégration :
   - Utilise ActionService pour charger les actions
   - Affiche un loader pendant le chargement
   - Affiche les erreurs de manière user-friendly
   - Rafraîchit automatiquement après ajout/modification/suppression
```

---

### 📋 PROMPT 20 : Créer le Template HTML du Tableau des Actions

```
Dans le projet Angular, créez le template HTML pour afficher les actions :

Fichier : src/app/components/dossier-actions/dossier-actions.component.html

Structure requise :

1. En-tête :
   - Titre "Historique des Actions"
   - Bouton "Ajouter une Action" (icône add)
   - Badge affichant le coût total des actions
   - Statistiques rapides (nombre d'actions, positives, négatives)

2. Filtres :
   - Dropdown "Filtrer par type" (Tous, Appel, Email, Visite, Lettre, Autre)
   - Dropdown "Filtrer par réponse" (Tous, Positive, Négative, Sans réponse)
   - Date picker "Date de début"
   - Date picker "Date de fin"
   - Bouton "Réinitialiser les filtres"

3. Tableau des actions :
   - Colonnes :
     * Date Action (format: "15 Nov 2025")
     * Type Action (badge coloré)
     * Nombre d'occurrences
     * Coût unitaire (formaté en devise)
     * Coût total (formaté en devise)
     * Réponse Débiteur (badge vert pour POSITIVE, rouge pour NEGATIVE, gris pour null)
     * Actions (boutons modifier/supprimer)

4. Indicateurs visuels :
   - Badge vert pour réponse POSITIVE (collaboratif)
   - Badge rouge pour réponse NEGATIVE (non collaboratif)
   - Badge gris pour sans réponse
   - Icônes selon le type d'action (phone pour APPEL, email pour EMAIL, etc.)

5. États :
   - Message "Aucune action enregistrée" si la liste est vide
   - Spinner de chargement
   - Message d'erreur en cas d'erreur

6. Actions rapides :
   - Bouton "Voir toutes les actions positives"
   - Bouton "Voir toutes les actions négatives"
   - Bouton "Exporter en Excel" (optionnel)

7. Styles :
   - Utilisez Angular Material Table
   - Responsive design
   - Tri par colonnes
   - Pagination si nécessaire
```

---

### 📋 PROMPT 21 : Créer le Dialog d'Ajout/Modification d'Action

```
Dans le projet Angular, créez un composant dialog pour ajouter ou modifier une action :

Fichier : src/app/components/action-dialog/action-dialog.component.ts

Fonctionnalités requises :

1. Propriétés :
   - action: Action | null = null (pour modification)
   - dossierId: number (injecté)
   - isEditMode: boolean = false
   - form: FormGroup
   - typesAction: TypeAction[] = [APPEL, EMAIL, VISITE, LETTRE, AUTRE]
   - reponsesDebiteur: ReponseDebiteur[] = [POSITIVE, NEGATIVE]

2. Champs du formulaire :
   - type: FormControl (required, dropdown)
   - dateAction: FormControl (required, date picker, défaut: aujourd'hui)
   - nbOccurrences: FormControl (required, number, min: 1, défaut: 1)
   - coutUnitaire: FormControl (required, number, min: 0)
   - reponseDebiteur: FormControl (optional, dropdown avec option "Sans réponse")

3. Calculs automatiques :
   - Afficher le coût total calculé (nbOccurrences * coutUnitaire)
   - Mettre à jour en temps réel

4. Validation :
   - Tous les champs obligatoires doivent être remplis
   - nbOccurrences >= 1
   - coutUnitaire >= 0
   - dateAction ne peut pas être dans le futur

5. Méthodes :
   - ngOnInit(): void - Initialise le formulaire
   - initForm(): void - Crée le FormGroup avec validators
   - loadActionData(): void - Charge les données si mode édition
   - save(): void - Sauvegarde l'action
   - cancel(): void - Ferme le dialog
   - calculateTotalCost(): number - Calcule le coût total

6. Intégration :
   - Utilise MatDialogRef pour fermer le dialog
   - Utilise ActionService pour créer/modifier
   - Affiche un message de succès après sauvegarde
   - Émet un événement pour rafraîchir la liste des actions

7. Logique métier :
   - Si reponseDebiteur = POSITIVE → suggérer de passer au finance
   - Si reponseDebiteur = NEGATIVE → suggérer de passer au juridique
   - Afficher un message contextuel selon la réponse
```

---

### 📋 PROMPT 22 : Créer le Template HTML du Dialog d'Action

```
Dans le projet Angular, créez le template HTML pour le dialog d'action :

Fichier : src/app/components/action-dialog/action-dialog.component.html

Structure requise :

1. En-tête du dialog :
   - Titre : "Ajouter une Action" ou "Modifier une Action"
   - Bouton de fermeture (X)

2. Formulaire :
   - Champ "Type d'Action" (mat-select) :
     * Options : Appel, Email, Visite, Lettre, Autre
     * Required, avec validation
   
   - Champ "Date de l'Action" (mat-datepicker) :
     * Date picker Material
     * Défaut : aujourd'hui
     * Ne peut pas être dans le futur
   
   - Champ "Nombre d'Occurrences" (mat-input number) :
     * Minimum : 1
     * Défaut : 1
     * Validation : nombre entier positif
   
   - Champ "Coût Unitaire" (mat-input number) :
     * Minimum : 0
     * Format : devise (TND)
     * Validation : nombre décimal positif
   
   - Champ "Réponse du Débiteur" (mat-select) :
     * Options : Sans réponse, Positive, Négative
     * Optionnel
     * Aide contextuelle : "Indiquez si le débiteur a répondu de manière positive ou négative"

3. Affichage du coût total :
   - Card Material affichant "Coût Total : XXX TND"
   - Calculé automatiquement (nbOccurrences * coutUnitaire)
   - Mise à jour en temps réel

4. Messages contextuels :
   - Si réponse POSITIVE :
     * Badge vert "Débiteur collaboratif"
     * Message : "Le débiteur semble prêt à payer. Envisagez de passer au finance."
   - Si réponse NEGATIVE :
     * Badge rouge "Débiteur non collaboratif"
     * Message : "Le débiteur ne répond pas favorablement. Envisagez de passer au recouvrement juridique."

5. Actions :
   - Bouton "Annuler" (secondary)
   - Bouton "Enregistrer" (primary, disabled si formulaire invalide)
   - Bouton "Enregistrer et Passer au Finance" (si réponse POSITIVE)
   - Bouton "Enregistrer et Passer au Juridique" (si réponse NEGATIVE)

6. Validation :
   - Messages d'erreur sous chaque champ
   - Indicateur visuel pour les champs invalides
   - Bouton Enregistrer désactivé si formulaire invalide

7. Styles :
   - Utilisez Angular Material Form Fields
   - Layout responsive
   - Espacement cohérent
```

---

### 📋 PROMPT 23 : Intégrer la Logique de Décision (Finance vs Juridique)

```
Dans le projet Angular, créez un service pour gérer la logique de décision après les actions :

Fichier : src/app/services/decision-recouvrement.service.ts

Fonctionnalités requises :

1. Méthodes d'analyse :
   - analyserCollaborationDébiteur(dossierId: number): Observable<AnalyseCollaboration>
     - Analyse toutes les actions d'un dossier
     - Calcule le pourcentage de réponses positives vs négatives
     - Détermine si le débiteur est collaboratif
     - Retourne : {collaboratif: boolean, pourcentagePositif: number, recommandation: string}
   
   - evaluerPassageAuFinance(dossierId: number): Observable<EvaluationFinance>
     - Vérifie si le dossier peut passer au finance
     - Conditions : au moins une réponse POSITIVE récente
     - Retourne : {peutPasser: boolean, raison: string, actionsPositives: Action[]}
   
   - evaluerPassageAuJuridique(dossierId: number): Observable<EvaluationJuridique>
     - Vérifie si le dossier doit passer au juridique
     - Conditions : plusieurs réponses NEGATIVE ou aucune réponse
     - Retourne : {doitPasser: boolean, raison: string, actionsNegatives: Action[]}

2. Méthodes d'action :
   - passerAuFinance(dossierId: number): Observable<Dossier>
     - Appelle l'API pour affecter au finance (si elle existe)
     - Ou met à jour le typeRecouvrement
     - Retourne le dossier mis à jour
   
   - passerAuJuridique(dossierId: number): Observable<Dossier>
     - Appelle PUT /api/dossiers/{id}/affecter/recouvrement-juridique
     - Retourne le dossier mis à jour

3. Règles métier :
   - Si 2+ réponses POSITIVE récentes (30 derniers jours) → Recommander Finance
   - Si 3+ réponses NEGATIVE ou aucune réponse après 5 actions → Recommander Juridique
   - Si mixte → Analyser la tendance (dernières actions plus importantes)

4. Intégration :
   - Utilise ActionService pour récupérer les actions
   - Utilise DossierService pour les affectations
   - Affiche des recommandations contextuelles
   - Propose des actions automatiques
```

---

### 📋 PROMPT 24 : Créer le Composant de Recommandations et Actions Rapides

```
Dans le projet Angular, créez un composant pour afficher les recommandations :

Fichier : src/app/components/dossier-recommandations/dossier-recommandations.component.ts

Fonctionnalités requises :

1. Propriétés :
   - dossierId: number (input)
   - analyse: AnalyseCollaboration | null = null
   - evaluationFinance: EvaluationFinance | null = null
   - evaluationJuridique: EvaluationJuridique | null = null
   - loading: boolean = false

2. Méthodes :
   - ngOnInit(): void - Charge les analyses au démarrage
   - loadAnalyses(): void - Charge toutes les analyses
   - passerAuFinance(): void - Passe le dossier au finance avec confirmation
   - passerAuJuridique(): void - Passe le dossier au juridique avec confirmation
   - getRecommandationColor(): string - Retourne la couleur selon la recommandation
   - getRecommandationIcon(): string - Retourne l'icône selon la recommandation

3. Affichage :
   - Card "Analyse de Collaboration"
     * Pourcentage de réponses positives
     * Statut : Collaboratif / Non Collaboratif
     * Graphique en barres (optionnel)
   
   - Card "Recommandation Finance" (si applicable)
     * Message : "Le débiteur semble prêt à payer"
     * Bouton "Passer au Finance"
     * Liste des actions positives récentes
   
   - Card "Recommandation Juridique" (si applicable)
     * Message : "Le débiteur ne répond pas favorablement"
     * Bouton "Passer au Recouvrement Juridique"
     * Liste des actions négatives
   
   - Card "Actions Récentes"
     * Dernières 5 actions
     * Tendance (amélioration/dégradation)

4. Intégration :
   - Utilise DecisionRecouvrementService
   - Affiche les recommandations en temps réel
   - Propose des actions rapides
   - Confirme avant de passer au finance/juridique
```

---

### 📋 PROMPT 25 : Mettre à Jour le Service Chef Recouvrement Amiable avec Actions

```
Dans le projet Angular, mettez à jour ChefRecouvrementAmiableService :

Fichier : src/app/services/chef-recouvrement-amiable.service.ts

Ajoutez les méthodes suivantes :

1. Méthodes pour les actions :
   - getActionsDossier(dossierId: number): Observable<Action[]>
     - Charge toutes les actions d'un dossier
   - ajouterAction(dossierId: number, action: Action): Observable<Action>
     - Crée une nouvelle action pour un dossier
   - modifierAction(actionId: number, action: Action): Observable<Action>
     - Modifie une action existante
   - supprimerAction(actionId: number): Observable<void>
     - Supprime une action
   - getCoutTotalActions(dossierId: number): Observable<number>
     - Retourne le coût total des actions

2. Méthodes d'analyse :
   - analyserDossier(dossierId: number): Observable<AnalyseDossier>
     - Analyse complète d'un dossier (actions, réponses, recommandations)
   - getStatistiquesActions(dossierId: number): Observable<StatistiquesActions>
     - Statistiques détaillées des actions

3. Méthodes de décision :
   - evaluerPassageFinance(dossierId: number): Observable<boolean>
   - evaluerPassageJuridique(dossierId: number): Observable<boolean>
   - passerAuFinance(dossierId: number): Observable<Dossier>
   - passerAuJuridique(dossierId: number): Observable<Dossier>

4. Intégration :
   - Utilise ActionService en interne
   - Utilise DecisionRecouvrementService
   - Gère les erreurs de manière centralisée
```

---

### 📋 PROMPT 26 : Mettre à Jour le Composant Détails Dossier avec Actions

```
Dans le projet Angular, mettez à jour le composant dossier-details :

Fichier : src/app/components/dossier-details/dossier-details.component.ts

Ajoutez les fonctionnalités suivantes :

1. Onglets supplémentaires :
   - Onglet "Informations" (existant)
   - Onglet "Actions" (nouveau) - Affiche le composant dossier-actions
   - Onglet "Recommandations" (nouveau) - Affiche le composant dossier-recommandations
   - Onglet "Documents" (existant)

2. Propriétés :
   - selectedTab: number = 0
   - actions: Action[] = []
   - totalCostActions: number = 0

3. Méthodes :
   - onTabChange(index: number): void
   - loadActions(): void - Charge les actions quand l'onglet est sélectionné
   - onActionAdded(action: Action): void - Callback après ajout d'action
   - onActionUpdated(action: Action): void - Callback après modification
   - onActionDeleted(): void - Callback après suppression

4. Logique :
   - Charge les actions uniquement quand l'onglet "Actions" est sélectionné (lazy loading)
   - Rafraîchit les recommandations après chaque action
   - Affiche un badge avec le nombre d'actions sur l'onglet

5. Intégration :
   - Utilise MatTabs pour les onglets
   - Intègre les composants dossier-actions et dossier-recommandations
   - Gère la communication entre composants
```

---

### 📋 PROMPT 27 : Créer le Composant Vue d'Ensemble Actions (Dashboard)

```
Dans le projet Angular, créez un composant pour la vue d'ensemble des actions :

Fichier : src/app/components/actions-overview/actions-overview.component.ts

Fonctionnalités requises :

1. Propriétés :
   - dossiers: Dossier[] = [] (tous les dossiers du chef)
   - actionsParDossier: Map<number, Action[]> = new Map()
   - statistiquesGlobales: StatistiquesActionsGlobales | null = null

2. Méthodes :
   - ngOnInit(): void - Charge les données
   - loadAllActions(): void - Charge les actions de tous les dossiers
   - getActionsForDossier(dossierId: number): Action[]
   - getTotalActionsCount(): number
   - getActionsByType(type: TypeAction): Action[]
   - getActionsByReponse(reponse: ReponseDebiteur): Action[]

3. Affichage :
   - Cards de statistiques :
     * Total d'actions aujourd'hui
     * Total d'actions cette semaine
     * Dossiers nécessitant attention (3+ actions négatives)
     * Coût total des actions
   
   - Graphiques :
     * Répartition par type d'action (pie chart)
     * Évolution des actions dans le temps (line chart)
     * Répartition des réponses (bar chart)
   
   - Liste des dossiers nécessitant attention :
     * Dossiers avec plusieurs actions négatives
     * Bouton "Voir Détails" pour chaque dossier
     * Recommandation (Finance ou Juridique)

4. Intégration :
   - Utilise ChefRecouvrementAmiableService
   - Utilise ActionService
   - Rafraîchit périodiquement (toutes les 5 minutes)
```

---

## 📝 Checklist d'Intégration des Actions

### ✅ Services

- [ ] Créer ActionService avec toutes les méthodes
- [ ] Créer DecisionRecouvrementService
- [ ] Mettre à jour ChefRecouvrementAmiableService

### ✅ Composants

- [ ] Composant tableau des actions
- [ ] Composant dialog ajout/modification action
- [ ] Composant recommandations
- [ ] Composant vue d'ensemble actions
- [ ] Mettre à jour composant détails dossier

### ✅ Intégration

- [ ] Intégrer dans le dashboard chef
- [ ] Intégrer dans la liste des dossiers
- [ ] Intégrer dans les détails dossier
- [ ] Tester le flux complet

### ✅ Tests

- [ ] Tests unitaires pour ActionService
- [ ] Tests unitaires pour DecisionRecouvrementService
- [ ] Tests E2E pour l'ajout d'action
- [ ] Tests E2E pour le passage finance/juridique

---

**Note** : Utilisez ces prompts dans l'ordre pour une intégration progressive et complète. Testez chaque étape avant de passer à la suivante.

