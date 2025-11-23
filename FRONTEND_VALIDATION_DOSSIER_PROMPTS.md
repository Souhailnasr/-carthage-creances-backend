# Prompts Frontend - Intégration API Validation Dossiers

## 📋 PROMPT PRINCIPAL POUR CURSOR IA

```
Créer une intégration complète pour consommer les APIs de validation des dossiers dans Angular.

CONTEXTE:
- Base URL API: http://localhost:8080/api
- Authentification: JWT Token dans le header Authorization (format: "Bearer {token}")
- CORS: Activé pour http://localhost:4200

WORKFLOW DE VALIDATION DES DOSSIERS:

1. CRÉATION D'UN DOSSIER PAR UN AGENT:
   - Endpoint: POST /api/dossiers/create
   - Le dossier est créé avec statut EN_ATTENTE_VALIDATION
   - Une ValidationDossier est automatiquement créée avec statut EN_ATTENTE
   - Le chef reçoit une notification

2. CRÉATION D'UN DOSSIER PAR UN CHEF:
   - Endpoint: POST /api/dossiers/create
   - Le dossier est automatiquement validé (statut VALIDE)
   - Une ValidationDossier est créée avec statut VALIDE

3. VALIDATION D'UN DOSSIER PAR UN CHEF:
   - Le chef voit les dossiers en attente via GET /api/validation/dossiers/en-attente
   - Le chef valide via PUT /api/dossiers/{id}/valider?chefId={chefId}
   - Le dossier passe à statut VALIDE et dossierStatus ENCOURSDETRAITEMENT
   - L'agent créateur reçoit une notification

STRUCTURES DE DONNÉES:

ValidationDossier:
- id: number
- dossier: Dossier (avec id, titre, numeroDossier, statut, etc.)
- agentCreateur: Utilisateur (avec id, nom, prenom)
- chefValidateur: Utilisateur | null
- statut: 'EN_ATTENTE' | 'VALIDE' | 'REJETE'
- commentaires: string | null
- dateCreation: string (ISO 8601)
- dateValidation: string | null (ISO 8601)

Dossier:
- id: number
- titre: string
- numeroDossier: string
- statut: 'EN_ATTENTE_VALIDATION' | 'VALIDE' | 'REJETE' | 'EN_COURS' | 'CLOTURE'
- dossierStatus: 'ENCOURSDETRAITEMENT' | 'CLOTURE' | 'INCONNU'
- valide: boolean
- dateValidation: string | null

ENDPOINTS À IMPLÉMENTER:

1. GET /api/validation/dossiers/en-attente
   - Retourne les dossiers en attente de validation
   - Utilisé par le chef pour voir les dossiers à valider

2. GET /api/validation/dossiers/dossier/{dossierId}
   - Retourne les validations pour un dossier spécifique

3. PUT /api/dossiers/{id}/valider?chefId={chefId}
   - Valide un dossier (endpoint principal)
   - Requiert chefId en paramètre

4. PUT /api/dossiers/{id}/rejeter?commentaire={commentaire}
   - Rejette un dossier

5. POST /api/validation/dossiers/{id}/valider?chefId={chefId}&commentaire={commentaire}
   - Alternative via ValidationDossierController

6. POST /api/validation/dossiers/{id}/rejeter?chefId={chefId}&commentaire={commentaire}
   - Rejette via ValidationDossierController

7. GET /api/validation/dossiers/agent/{agentId}
   - Validations d'un agent spécifique

8. GET /api/validation/dossiers/statut/{statut}
   - Validations filtrées par statut

EXIGENCES TECHNIQUES:
- Utiliser Angular HttpClient avec interceptors pour l'authentification
- Gérer les erreurs HTTP de manière appropriée
- Créer des services Angular pour chaque endpoint
- Créer des interfaces TypeScript pour les modèles de données
- Implémenter la gestion des notifications en temps réel (optionnel)
- Utiliser RxJS pour les observables et la gestion asynchrone
- Créer des composants pour:
  * Liste des dossiers en attente (vue chef)
  * Détails d'un dossier avec boutons validation/rejet (vue chef)
  * Historique des validations (vue agent)
- Respecter les principes Angular (lazy loading, OnPush change detection si possible)
```

---

## 📝 PROMPT 1: Service Angular pour Validation Dossiers

```
Créer un service Angular service/validation-dossier.service.ts qui consomme toutes les APIs de validation des dossiers.

REQUIREMENTS:
- Utiliser HttpClient injecté
- Base URL: http://localhost:8080/api
- Tous les appels doivent inclure le header Authorization avec le JWT token
- Retourner des Observables typés avec les interfaces TypeScript

MÉTHODES À IMPLÉMENTER:

1. getDossiersEnAttente(): Observable<ValidationDossier[]>
   - GET /api/validation/dossiers/en-attente
   - Retourne les dossiers en attente de validation

2. getValidationById(id: number): Observable<ValidationDossier>
   - GET /api/validation/dossiers/{id}

3. getValidationsByDossier(dossierId: number): Observable<ValidationDossier[]>
   - GET /api/validation/dossiers/dossier/{dossierId}

4. validerDossier(dossierId: number, chefId: number): Observable<Dossier>
   - PUT /api/dossiers/{dossierId}/valider?chefId={chefId}
   - Retourne le dossier mis à jour

5. rejeterDossier(dossierId: number, commentaire: string): Observable<Dossier>
   - PUT /api/dossiers/{dossierId}/rejeter?commentaire={commentaire}

6. validerDossierViaValidation(validationId: number, chefId: number, commentaire?: string): Observable<ValidationDossier>
   - POST /api/validation/dossiers/{validationId}/valider?chefId={chefId}&commentaire={commentaire}

7. rejeterDossierViaValidation(validationId: number, chefId: number, commentaire?: string): Observable<ValidationDossier>
   - POST /api/validation/dossiers/{validationId}/rejeter?chefId={chefId}&commentaire={commentaire}

8. getValidationsByAgent(agentId: number): Observable<ValidationDossier[]>
   - GET /api/validation/dossiers/agent/{agentId}

9. getValidationsByStatut(statut: 'EN_ATTENTE' | 'VALIDE' | 'REJETE'): Observable<ValidationDossier[]>
   - GET /api/validation/dossiers/statut/{statut}

10. countValidationsByStatut(statut: StatutValidation): Observable<number>
    - GET /api/validation/dossiers/statistiques/statut/{statut}

GESTION DES ERREURS:
- Intercepter les erreurs HTTP 400, 401, 404, 500
- Retourner des messages d'erreur appropriés
- Logger les erreurs pour le debugging

UTILISER:
- RxJS operators (catchError, map, tap)
- TypeScript strict typing
- Angular HttpClient avec interceptors pour l'auth
```

---

## 📝 PROMPT 2: Interfaces TypeScript

```
Créer les interfaces TypeScript dans models/validation-dossier.models.ts pour les entités de validation.

INTERFACES À CRÉER:

1. StatutValidation: type union
   export type StatutValidation = 'EN_ATTENTE' | 'VALIDE' | 'REJETE';

2. Statut: type union
   export type Statut = 'EN_ATTENTE_VALIDATION' | 'VALIDE' | 'REJETE' | 'EN_COURS' | 'CLOTURE';

3. DossierStatus: type union
   export type DossierStatus = 'ENCOURSDETRAITEMENT' | 'CLOTURE' | 'INCONNU';

4. Utilisateur:
   export interface Utilisateur {
     id: number;
     nom: string;
     prenom: string;
     email: string;
     roleUtilisateur: string;
   }

5. Dossier:
   export interface Dossier {
     id: number;
     titre: string;
     description?: string;
     numeroDossier: string;
     montantCreance?: number;
     statut: Statut;
     dossierStatus: DossierStatus;
     valide: boolean;
     dateValidation: string | null;
     dateCreation: string;
     agentCreateur?: Utilisateur;
     agentResponsable?: Utilisateur;
   }

6. ValidationDossier:
   export interface ValidationDossier {
     id: number;
     dossier: Dossier;
     agentCreateur: Utilisateur;
     chefValidateur: Utilisateur | null;
     statut: StatutValidation;
     commentaires: string | null;
     dateCreation: string;
     dateValidation: string | null;
     dateModification?: string | null;
   }

EXIGENCES:
- Tous les champs optionnels doivent être marqués avec ?
- Utiliser des types stricts
- Ajouter des commentaires JSDoc pour chaque interface
```

---

## 📝 PROMPT 3: Composant Liste Dossiers en Attente (Chef)

```
Créer un composant Angular components/dossiers-en-attente/dossiers-en-attente.component.ts pour afficher les dossiers en attente de validation.

CONTEXTE:
- Ce composant est utilisé par les chefs pour voir et valider/rejeter les dossiers créés par les agents
- Affiche une liste de ValidationDossier avec statut EN_ATTENTE

FONCTIONNALITÉS:

1. AFFICHAGE:
   - Tableau/liste des dossiers en attente
   - Colonnes: Numéro dossier, Titre, Agent créateur, Date création, Actions
   - Badge/indicateur pour le statut EN_ATTENTE
   - Bouton "Voir détails" pour chaque dossier

2. ACTIONS:
   - Bouton "Valider" qui ouvre un dialog/modal de confirmation
   - Bouton "Rejeter" qui ouvre un dialog pour saisir un commentaire
   - Les actions doivent appeler le service de validation

3. DIALOG DE VALIDATION:
   - Confirmation: "Êtes-vous sûr de vouloir valider ce dossier?"
   - Bouton "Valider" et "Annuler"
   - Afficher le titre et numéro du dossier

4. DIALOG DE REJET:
   - Champ textarea pour commentaire (requis)
   - Validation du formulaire
   - Bouton "Rejeter" et "Annuler"

5. GESTION D'ÉTAT:
   - Loading spinner pendant les appels API
   - Message de succès après validation/rejet
   - Message d'erreur si échec
   - Rafraîchir la liste après validation/rejet

6. FILTRES (optionnel):
   - Filtrer par agent créateur
   - Filtrer par date de création
   - Recherche par numéro de dossier ou titre

UTILISER:
- Angular Material pour les composants UI (table, dialog, button, etc.)
- Reactive Forms pour le formulaire de rejet
- RxJS pour gérer les observables
- ChangeDetectionStrategy.OnPush pour la performance
- Service de validation injecté
- Service de notification pour les messages (snackbar/toast)
```

---

## 📝 PROMPT 4: Composant Détails Dossier avec Validation

```
Créer un composant Angular components/dossier-detail/dossier-detail.component.ts pour afficher les détails d'un dossier avec possibilité de validation.

CONTEXTE:
- Composant de détail utilisé par les chefs pour examiner un dossier avant validation
- Peut être ouvert depuis la liste des dossiers en attente

FONCTIONNALITÉS:

1. AFFICHAGE DES DÉTAILS:
   - Informations du dossier (titre, numéro, description, montant, etc.)
   - Informations de l'agent créateur
   - Informations du créancier et débiteur
   - Statut actuel du dossier
   - Date de création
   - Historique des validations (si existe)

2. ACTIONS DISPONIBLES (selon le rôle):
   - Si utilisateur est CHEF et dossier est EN_ATTENTE_VALIDATION:
     * Bouton "Valider le dossier"
     * Bouton "Rejeter le dossier"
   - Si dossier est déjà VALIDE:
     * Afficher qui a validé et quand
     * Afficher le commentaire de validation (si existe)
   - Si dossier est REJETE:
     * Afficher le commentaire de rejet
     * Bouton "Remettre en attente" (si applicable)

3. DIALOG DE VALIDATION:
   - Confirmation avec prévisualisation des infos du dossier
   - Champ optionnel pour commentaire de validation
   - Bouton "Valider" et "Annuler"

4. DIALOG DE REJET:
   - Champ obligatoire pour commentaire de rejet
   - Validation du formulaire
   - Avertissement: "Cette action rejettera le dossier et notifiera l'agent créateur"
   - Bouton "Rejeter" et "Annuler"

5. GESTION D'ÉTAT:
   - Loading pendant le chargement des détails
   - Loading pendant la validation/rejet
   - Message de succès/erreur
   - Redirection ou fermeture après validation/rejet

UTILISER:
- Angular Material (card, button, dialog, etc.)
- Reactive Forms pour les formulaires
- Service de validation injecté
- Service d'authentification pour vérifier le rôle
- Router pour la navigation si nécessaire
```

---

## 📝 PROMPT 5: Composant Historique Validations (Agent)

```
Créer un composant Angular components/mes-validations/mes-validations.component.ts pour afficher l'historique des validations d'un agent.

CONTEXTE:
- Composant utilisé par les agents pour voir l'historique de leurs dossiers créés
- Affiche tous les dossiers créés par l'agent avec leur statut de validation

FONCTIONNALITÉS:

1. AFFICHAGE:
   - Liste/tableau des dossiers créés par l'agent
   - Colonnes: Numéro dossier, Titre, Statut validation, Date création, Date validation, Chef validateur, Commentaires
   - Badges colorés pour les statuts:
     * EN_ATTENTE: orange
     * VALIDE: vert
     * REJETE: rouge

2. FILTRES:
   - Filtrer par statut (EN_ATTENTE, VALIDE, REJETE)
   - Recherche par numéro de dossier ou titre
   - Filtrer par date de création

3. DÉTAILS:
   - Clic sur un dossier pour voir les détails complets
   - Afficher le commentaire de validation/rejet si disponible
   - Afficher qui a validé/rejeté et quand

4. STATISTIQUES (optionnel):
   - Nombre total de dossiers créés
   - Nombre en attente
   - Nombre validés
   - Nombre rejetés

5. GESTION D'ÉTAT:
   - Loading pendant le chargement
   - Message si aucun dossier trouvé
   - Pagination si nécessaire

UTILISER:
- Angular Material (table, paginator, chips, etc.)
- Service de validation injecté
- Service d'authentification pour obtenir l'ID de l'agent connecté
- RxJS pour les filtres et la recherche
```

---

## 📝 PROMPT 6: Service de Notification

```
Créer un service Angular service/notification.service.ts pour gérer les notifications de validation.

CONTEXTE:
- Les notifications sont envoyées par le backend lors de la validation/rejet
- Le frontend doit afficher ces notifications à l'utilisateur

FONCTIONNALITÉS:

1. MÉTHODES:
   - showSuccess(message: string): void
     * Affiche un message de succès (snackbar vert)
   - showError(message: string): void
     * Affiche un message d'erreur (snackbar rouge)
   - showInfo(message: string): void
     * Affiche un message d'information (snackbar bleu)
   - showWarning(message: string): void
     * Affiche un avertissement (snackbar orange)

2. MESSAGES PRÉDÉFINIS:
   - "Dossier validé avec succès"
   - "Dossier rejeté"
   - "Erreur lors de la validation"
   - "Dossier en attente de validation"
   - "Vous n'avez pas les droits pour valider ce dossier"

3. CONFIGURATION:
   - Durée d'affichage: 3 secondes par défaut
   - Position: bottom-right
   - Action "Fermer" optionnelle

UTILISER:
- Angular Material Snackbar
- Injection de MatSnackBar
- Configuration centralisée
```

---

## 📝 PROMPT 7: Guard pour Vérification Rôle Chef

```
Créer un guard Angular guards/chef.guard.ts pour protéger les routes de validation.

CONTEXTE:
- Seuls les chefs peuvent accéder aux pages de validation
- Vérifier le rôle de l'utilisateur avant d'autoriser l'accès

FONCTIONNALITÉS:

1. VÉRIFICATION:
   - Vérifier si l'utilisateur est authentifié
   - Vérifier si l'utilisateur a le rôle CHEF_DEPARTEMENT_DOSSIER ou SUPER_ADMIN
   - Rediriger vers une page d'erreur si non autorisé

2. UTILISATION:
   - Ajouter le guard aux routes de validation:
     * /dossiers/en-attente
     * /dossiers/validation
     * /dossiers/:id/valider

3. GESTION D'ERREUR:
   - Afficher un message d'erreur si accès refusé
   - Rediriger vers la page d'accueil ou login

UTILISER:
- Angular Router Guards (CanActivate)
- Service d'authentification pour obtenir le rôle
- Service de notification pour afficher les erreurs
```

---

## 📝 PROMPT 8: Module de Validation (Lazy Loading)

```
Créer un module Angular modules/validation-dossier.module.ts qui regroupe tous les composants et services de validation.

STRUCTURE:
- Déclarations: tous les composants de validation
- Imports: Angular Material, FormsModule, HttpClientModule
- Exports: composants réutilisables
- Providers: services de validation

COMPOSANTS À INCLURE:
- DossiersEnAttenteComponent
- DossierDetailComponent
- MesValidationsComponent

SERVICES À INCLURE:
- ValidationDossierService
- NotificationService

ROUTES:
- /dossiers/en-attente (lazy loaded)
- /dossiers/:id/validation (lazy loaded)
- /mes-validations (lazy loaded)

UTILISER:
- Lazy loading pour optimiser les performances
- Angular Material modules
- SharedModule si existant
```

---

## 📝 PROMPT 9: Exemple d'Utilisation Complète

```
Créer un exemple complet d'intégration dans un composant qui démontre:
1. Chargement des dossiers en attente
2. Affichage dans un tableau Angular Material
3. Action de validation avec dialog de confirmation
4. Gestion des erreurs et notifications

EXEMPLE DE CODE À GÉNÉRER:

```typescript
export class DossiersEnAttenteComponent implements OnInit {
  dossiersEnAttente$: Observable<ValidationDossier[]>;
  loading = false;
  
  constructor(
    private validationService: ValidationDossierService,
    private authService: AuthService,
    private notificationService: NotificationService,
    private dialog: MatDialog
  ) {}
  
  ngOnInit() {
    this.loadDossiersEnAttente();
  }
  
  loadDossiersEnAttente() {
    this.loading = true;
    this.dossiersEnAttente$ = this.validationService.getDossiersEnAttente()
      .pipe(
        tap(() => this.loading = false),
        catchError(error => {
          this.loading = false;
          this.notificationService.showError('Erreur lors du chargement');
          return of([]);
        })
      );
  }
  
  validerDossier(validation: ValidationDossier) {
    const dialogRef = this.dialog.open(ValidationDialogComponent, {
      data: { dossier: validation.dossier }
    });
    
    dialogRef.afterClosed().subscribe(result => {
      if (result && result.confirmed) {
        const chefId = this.authService.getCurrentUser().id;
        this.validationService.validerDossier(validation.dossier.id, chefId)
          .subscribe({
            next: () => {
              this.notificationService.showSuccess('Dossier validé avec succès');
              this.loadDossiersEnAttente();
            },
            error: () => {
              this.notificationService.showError('Erreur lors de la validation');
            }
          });
      }
    });
  }
}
```

INCLURE:
- Gestion complète des erreurs
- Loading states
- Notifications
- Rafraîchissement après action
```

---

## 🔧 CONFIGURATION REQUISE

### Base URL
```typescript
// environment.ts
export const environment = {
  apiUrl: 'http://localhost:8080/api'
};
```

### Interceptor pour l'authentification
```typescript
// auth.interceptor.ts
export class AuthInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = localStorage.getItem('token');
    if (token) {
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }
    return next.handle(req);
  }
}
```

### Provider dans app.module.ts
```typescript
providers: [
  {
    provide: HTTP_INTERCEPTORS,
    useClass: AuthInterceptor,
    multi: true
  }
]
```

---

## 📌 NOTES IMPORTANTES

1. **Workflow de validation:**
   - Les dossiers créés par les agents sont automatiquement en attente (EN_ATTENTE_VALIDATION)
   - Les dossiers créés par les chefs sont automatiquement validés
   - Lors de la validation, le dossier passe à statut VALIDE et dossierStatus ENCOURSDETRAITEMENT

2. **Notifications:**
   - Le backend envoie automatiquement des notifications lors de la validation/rejet
   - Le frontend doit afficher ces notifications à l'utilisateur

3. **Sécurité:**
   - Tous les endpoints nécessitent une authentification JWT
   - Seuls les chefs peuvent valider/rejeter les dossiers
   - Vérifier les rôles côté frontend ET backend

4. **Gestion des erreurs:**
   - 400: Requête invalide (afficher le message d'erreur)
   - 401: Non authentifié (rediriger vers login)
   - 403: Non autorisé (afficher message d'erreur)
   - 404: Ressource non trouvée
   - 500: Erreur serveur (afficher message générique)

5. **Performance:**
   - Utiliser OnPush change detection
   - Implémenter la pagination si beaucoup de dossiers
   - Mettre en cache les données si nécessaire
   - Utiliser lazy loading pour les modules de validation


















