# Prompts Professionnels pour Mise à Jour Frontend - Gestion des Enquêtes

## 📋 Vue d'ensemble
Ce document contient tous les prompts nécessaires pour mettre à jour le frontend Angular avec toutes les améliorations backend concernant la gestion des enquêtes. Suivez les prompts dans l'ordre pour une intégration complète.

---

## 🎯 PROMPT 1 : Mise à Jour de l'Interface TypeScript Enquette

**Prompt à copier dans Cursor AI :**

```
Dans le projet Angular, localisez l'interface TypeScript qui définit le modèle Enquette (probablement dans src/app/models/enquette.ts ou similaire).

Mettez à jour cette interface pour inclure TOUS les champs suivants :

1. Champs de base :
   - id: number;
   - rapportCode?: string;

2. Éléments financiers :
   - nomElementFinancier?: string;
   - pourcentage?: number;
   - banqueAgence?: string;
   - banques?: string;
   - exercices?: string;
   - chiffreAffaire?: number;
   - resultatNet?: number;
   - disponibiliteBilan?: string;

3. Solvabilité :
   - appreciationBancaire?: string;
   - paiementsCouverture?: string;
   - reputationCommerciale?: string;
   - incidents?: string;

4. Patrimoine débiteur :
   - bienImmobilier?: string;
   - situationJuridiqueImmobilier?: string;
   - bienMobilier?: string;
   - situationJuridiqueMobilier?: string;

5. Autres affaires & observations :
   - autresAffaires?: string;
   - observations?: string;

6. Décision comité recouvrement :
   - decisionComite?: string;
   - visaDirecteurJuridique?: string;
   - visaEnqueteur?: string;
   - visaDirecteurCommercial?: string;
   - registreCommerce?: string;
   - codeDouane?: string;
   - matriculeFiscale?: string;
   - formeJuridique?: string;
   - dateCreation?: string; // Format: YYYY-MM-DD
   - capital?: number;

7. Dirigeants :
   - pdg?: string;
   - directeurAdjoint?: string;
   - directeurFinancier?: string;
   - directeurCommercial?: string;

8. Activité :
   - descriptionActivite?: string;
   - secteurActivite?: string;
   - effectif?: number;

9. Informations diverses :
   - email?: string;
   - marques?: string;
   - groupe?: string;

10. Relations (IMPORTANT - pour création/modification) :
    - dossierId?: number; // OBLIGATOIRE pour création - sera automatiquement affecté lors de la sélection du dossier
    - agentCreateurId?: number; // ID de l'agent créateur
    - agentResponsableId?: number; // ID de l'agent responsable

11. Relations complètes (pour lecture) :
    - dossier?: Dossier; // Objet complet (optionnel, pour lecture)
    - agentCreateur?: Utilisateur; // Objet complet (optionnel, pour lecture)
    - agentResponsable?: Utilisateur; // Objet complet (optionnel, pour lecture)

12. Propriétés de validation :
    - valide?: boolean;
    - dateValidation?: string; // Format ISO 8601
    - commentaireValidation?: string;
    - statut?: 'EN_ATTENTE_VALIDATION' | 'VALIDE' | 'REJETE';

Assurez-vous que tous les champs sont optionnels sauf ceux explicitement marqués comme obligatoires. Gardez la compatibilité avec l'interface existante si elle existe déjà.
```

---

## 🎯 PROMPT 2 : Mise à Jour du Service EnqueteService

**Prompt à copier dans Cursor AI :**

```
Dans le projet Angular, localisez le service EnqueteService (probablement dans src/app/services/enquete.service.ts).

Mettez à jour ce service pour inclure TOUTES les méthodes suivantes avec la gestion d'erreurs appropriée :

1. CRUD Operations :
   - createEnquete(enquete: Enquette): Observable<Enquette>
     * POST /api/enquettes
     * IMPORTANT: Le dossierId est OBLIGATOIRE et sera automatiquement affecté lors de la sélection du dossier
     * Gérer les erreurs : 400 (dossierId manquant), 404 (dossier non trouvé), 409 (dossier a déjà une enquête)
   
   - getEnqueteById(id: number): Observable<Enquette>
     * GET /api/enquettes/{id}
     * Retourne null si 404 (pas d'erreur, juste pas trouvé)
   
   - getAllEnquetes(): Observable<Enquette[]>
     * GET /api/enquettes
   
   - updateEnquete(id: number, enquete: Enquette): Observable<Enquette>
     * PUT /api/enquettes/{id}
   
   - deleteEnquete(id: number): Observable<void>
     * DELETE /api/enquettes/{id}
     * Gérer les erreurs : 404 (non trouvé), 409 (contrainte), 500 (erreur serveur)
     * Retourner des messages d'erreur détaillés

2. Recherche Operations :
   - getEnqueteByDossier(dossierId: number): Observable<Enquette | null>
     * GET /api/enquettes/dossier/{dossierId}
     * Retourne null si 404 (pas d'erreur, c'est normal si pas d'enquête)
   
   - getEnquetesByCreationDate(date: string): Observable<Enquette[]>
     * GET /api/enquettes/creation-date/{date}
   
   - getEnquetesByCreationDateRange(startDate: string, endDate: string): Observable<Enquette[]>
     * GET /api/enquettes/creation-date-range?startDate=...&endDate=...
   
   - getEnquetesBySector(sector: string): Observable<Enquette[]>
     * GET /api/enquettes/sector/{sector}
   
   - getEnquetesByLegalForm(legalForm: string): Observable<Enquette[]>
     * GET /api/enquettes/legal-form/{legalForm}
   
   - getEnquetesByPDG(pdg: string): Observable<Enquette[]>
     * GET /api/enquettes/pdg/{pdg}
   
   - getEnquetesByCapitalRange(minCapital: number, maxCapital: number): Observable<Enquette[]>
     * GET /api/enquettes/capital-range?minCapital=...&maxCapital=...
   
   - getEnquetesByRevenueRange(minRevenue: number, maxRevenue: number): Observable<Enquette[]>
     * GET /api/enquettes/revenue-range?minRevenue=...&maxRevenue=...
   
   - getEnquetesByStaffRange(minStaff: number, maxStaff: number): Observable<Enquette[]>
     * GET /api/enquettes/staff-range?minStaff=...&maxStaff=...
   
   - getEnquetesWithRealEstate(): Observable<Enquette[]>
     * GET /api/enquettes/with-real-estate
   
   - getEnquetesWithMovableProperty(): Observable<Enquette[]>
     * GET /api/enquettes/with-movable-property
   
   - getEnquetesWithObservations(): Observable<Enquette[]>
     * GET /api/enquettes/with-observations

3. Validation Operations :
   - validerEnquete(id: number, chefId: number): Observable<Enquette>
     * PUT /api/enquettes/{id}/valider?chefId={chefId}
   
   - rejeterEnquete(id: number, commentaire: string): Observable<Enquette>
     * PUT /api/enquettes/{id}/rejeter?commentaire={commentaire}

4. Statistics Operations (NOUVEAU) :
   - getTotalEnquetes(): Observable<number>
     * GET /api/enquettes/statistiques/total
   
   - getEnquetesByStatut(statut: string): Observable<number>
     * GET /api/enquettes/statistiques/statut/{statut}
   
   - getEnquetesValides(): Observable<number>
     * GET /api/enquettes/statistiques/valides
   
   - getEnquetesNonValides(): Observable<number>
     * GET /api/enquettes/statistiques/non-valides
   
   - getEnquetesCreesCeMois(): Observable<number>
     * GET /api/enquettes/statistiques/ce-mois
   
   - getEnquetesByAgentCreateur(agentId: number): Observable<number>
     * GET /api/enquettes/statistiques/agent/{agentId}/crees
   
   - getEnquetesByAgentResponsable(agentId: number): Observable<number>
     * GET /api/enquettes/statistiques/agent/{agentId}/responsable

IMPORTANT pour toutes les méthodes :
- Utiliser HttpClient avec gestion d'erreurs appropriée
- Utiliser catchError pour gérer les erreurs HTTP
- Retourner des Observables typés
- Pour les méthodes qui peuvent retourner null (getEnqueteById, getEnqueteByDossier), utiliser Observable<Enquette | null>
- Afficher des messages d'erreur utilisateur-friendly avec MatSnackBar
- Logger les erreurs pour le débogage
```

---

## 🎯 PROMPT 3 : Amélioration du Composant de Création d'Enquête

**Prompt à copier dans Cursor AI :**

```
Dans le projet Angular, localisez le composant de création d'enquête (probablement create-enquete.component.ts et create-enquete.component.html).

Améliorez ce composant pour :

1. Gestion du dossierId :
   - Ajouter un champ de sélection de dossier (select/dropdown)
   - Le dossierId doit être OBLIGATOIRE et validé avant la soumission
   - Lors de la sélection d'un dossier, le dossierId est automatiquement affecté à l'enquête
   - Filtrer les dossiers pour n'afficher que ceux qui n'ont pas déjà une enquête associée
   - Afficher un message d'erreur si l'utilisateur essaie de créer une enquête pour un dossier qui en a déjà une

2. Formulaire réactif complet :
   - Créer un FormGroup avec TOUS les champs de l'interface Enquette
   - Utiliser FormBuilder pour créer le formulaire
   - Ajouter des validators appropriés (required pour dossierId, email pour email, etc.)
   - Grouper les champs par sections logiques (financier, solvabilité, patrimoine, etc.)

3. Gestion de la soumission :
   - Lors du submit, s'assurer que dossierId est défini
   - Envoyer seulement les champs nécessaires au backend (dossierId, agentCreateurId, et les autres champs remplis)
   - Afficher un loading state pendant la création
   - Gérer les erreurs avec des messages clairs :
     * "Le dossierId est obligatoire" si 400
     * "Dossier non trouvé" si 404
     * "Ce dossier a déjà une enquête associée" si 409
     * Messages d'erreur génériques pour autres erreurs

4. UX/UI :
   - Utiliser Material Design (MatFormField, MatInput, MatSelect, etc.)
   - Organiser le formulaire en sections avec des MatExpansionPanel ou des onglets
   - Ajouter des tooltips pour expliquer les champs
   - Afficher un snackbar de succès après création réussie
   - Rediriger vers la page de détails de l'enquête créée

5. Chargement des données :
   - Charger la liste des dossiers disponibles au ngOnInit
   - Filtrer automatiquement les dossiers qui ont déjà une enquête
   - Afficher un spinner pendant le chargement

IMPORTANT : Ne pas supprimer les fonctionnalités existantes, seulement les améliorer et ajouter les nouvelles.
```

---

## 🎯 PROMPT 4 : Amélioration du Composant de Liste des Enquêtes

**Prompt à copier dans Cursor AI :**

```
Dans le projet Angular, localisez le composant de liste des enquêtes (probablement list-enquetes.component.ts et list-enquetes.component.html).

Améliorez ce composant pour :

1. Affichage amélioré :
   - Utiliser MatTable avec pagination, tri et filtrage
   - Afficher les colonnes principales : ID, Rapport Code, Dossier, Statut, Date Création, Agent Créateur
   - Ajouter des badges colorés pour les statuts (EN_ATTENTE_VALIDATION, VALIDE, REJETE)
   - Afficher un indicateur visuel pour les enquêtes validées/non validées

2. Actions sur les lignes :
   - Bouton "Voir détails" qui redirige vers la page de détails
   - Bouton "Modifier" (seulement si non validée)
   - Bouton "Supprimer" avec confirmation (MatDialog)
   - Bouton "Valider" (seulement pour les chefs, si en attente)
   - Bouton "Rejeter" (seulement pour les chefs, si en attente)

3. Filtres et recherche :
   - Ajouter une barre de recherche globale
   - Filtres par statut (dropdown)
   - Filtres par date de création (date picker)
   - Filtres par agent créateur
   - Filtres par secteur d'activité
   - Bouton "Réinitialiser les filtres"

4. Statistiques en haut de page :
   - Afficher des cartes avec les statistiques :
     * Total d'enquêtes
     * Enquêtes validées
     * Enquêtes non validées
     * Enquêtes créées ce mois
   - Utiliser MatCard pour les afficher
   - Charger les statistiques au ngOnInit

5. Gestion de la suppression :
   - Afficher un MatDialog de confirmation avant suppression
   - Afficher un loading pendant la suppression
   - Gérer les erreurs avec des messages clairs
   - Rafraîchir la liste après suppression réussie
   - Afficher un snackbar de succès/erreur

6. Pagination et performance :
   - Implémenter la pagination côté serveur si possible
   - Limiter le nombre d'éléments par page (10, 25, 50, 100)
   - Ajouter un indicateur de chargement

IMPORTANT : Garder toutes les fonctionnalités existantes et les améliorer.
```

---

## 🎯 PROMPT 5 : Amélioration du Composant de Détails d'Enquête

**Prompt à copier dans Cursor AI :**

```
Dans le projet Angular, localisez le composant de détails d'enquête (probablement detail-enquete.component.ts et detail-enquete.component.html).

Améliorez ce composant pour :

1. Affichage complet des informations :
   - Organiser les informations en sections avec MatExpansionPanel ou MatTabs :
     * Informations générales (ID, Rapport Code, Statut, Dates)
     * Informations financières
     * Solvabilité
     * Patrimoine
     * Dirigeants
     * Activité
     * Observations
   - Afficher toutes les propriétés de l'enquête
   - Utiliser des labels clairs et formatage approprié (dates, nombres, etc.)

2. Affichage des relations :
   - Afficher les informations du dossier associé (avec lien vers le dossier)
   - Afficher les informations de l'agent créateur
   - Afficher les informations de l'agent responsable
   - Afficher l'historique des validations (liste des ValidationEnquete)

3. Actions disponibles :
   - Bouton "Modifier" (seulement si non validée)
   - Bouton "Supprimer" avec confirmation (seulement si non validée)
   - Bouton "Valider" (seulement pour les chefs, si en attente)
   - Bouton "Rejeter" (seulement pour les chefs, si en attente)
   - Bouton "Retour à la liste"

4. Section de validation :
   - Afficher le statut actuel de validation
   - Afficher la date de validation si validée
   - Afficher le commentaire de validation/rejet
   - Afficher le chef validateur si validée/rejetée
   - Afficher l'historique complet des validations

5. Gestion des erreurs :
   - Gérer le cas où l'enquête n'existe pas (404)
   - Afficher un message d'erreur approprié
   - Rediriger vers la liste si l'enquête n'existe pas

6. UX/UI :
   - Utiliser Material Design pour un affichage professionnel
   - Ajouter des icônes appropriées
   - Utiliser des couleurs pour les statuts
   - Ajouter des tooltips pour plus d'informations

IMPORTANT : Afficher TOUTES les informations disponibles de l'enquête de manière organisée et lisible.
```

---

## 🎯 PROMPT 6 : Création/Amélioration du Composant de Validation d'Enquête

**Prompt à copier dans Cursor AI :**

```
Dans le projet Angular, créez ou améliorez le composant de validation d'enquête pour les chefs.

Ce composant doit permettre :

1. Liste des enquêtes en attente :
   - Afficher toutes les enquêtes en attente de validation
   - Utiliser MatTable avec pagination
   - Colonnes : ID, Rapport Code, Dossier, Agent Créateur, Date Création
   - Bouton "Voir détails" pour chaque enquête
   - Bouton "Valider" et "Rejeter" pour chaque enquête

2. Dialog de validation :
   - Créer un MatDialog pour valider une enquête
   - Champ commentaire (optionnel)
   - Bouton "Valider" et "Annuler"
   - Afficher un loading pendant la validation
   - Gérer les erreurs avec des messages clairs

3. Dialog de rejet :
   - Créer un MatDialog pour rejeter une enquête
   - Champ commentaire (OBLIGATOIRE pour rejet)
   - Bouton "Rejeter" et "Annuler"
   - Afficher un loading pendant le rejet
   - Gérer les erreurs avec des messages clairs

4. Permissions :
   - Vérifier que l'utilisateur connecté est un chef
   - Masquer les boutons de validation/rejet si l'utilisateur n'est pas chef
   - Afficher un message si l'utilisateur n'a pas les permissions

5. Notifications :
   - Afficher un snackbar de succès après validation/rejet
   - Rafraîchir la liste après validation/rejet
   - Afficher des messages d'erreur appropriés

6. Intégration avec ValidationEnqueteService :
   - Utiliser le service ValidationEnqueteService pour les opérations
   - Charger les enquêtes en attente via GET /api/validation/enquetes/en-attente
   - Utiliser les endpoints de validation/rejet appropriés

IMPORTANT : Ce composant doit être accessible uniquement aux chefs et afficher uniquement les enquêtes en attente de validation.
```

---

## 🎯 PROMPT 6.5 : CORRECTION CRITIQUE - Format des Paramètres de Validation

**⚠️ PROMPT URGENT - À APPLIQUER EN PRIORITÉ**

**Prompt à copier dans Cursor AI :**

```
Dans le projet Angular, localisez le service qui gère la validation des enquêtes (probablement validation-enquete.service.ts ou enquete.service.ts).

CORRIGEZ IMMÉDIATEMENT les méthodes validerEnquete() et rejeterEnquete() pour envoyer les paramètres dans l'URL (query parameters) au lieu du body JSON.

PROBLÈME ACTUEL :
- Le backend attend chefId et commentaire comme @RequestParam (dans l'URL)
- Le frontend les envoie dans le body JSON
- Résultat : Erreur 500 "Required request parameter 'chefId' for method parameter type Long is not present"

SOLUTION :
1. Méthode validerEnquete(id, chefId, commentaire?) :
   - Utiliser HttpParams pour construire les query parameters
   - Envoyer chefId dans l'URL : ?chefId={chefId}
   - Envoyer commentaire dans l'URL si présent : &commentaire={commentaire}
   - Body JSON doit être null ou vide
   - Format URL : POST /api/validation/enquetes/{id}/valider?chefId={chefId}&commentaire={commentaire}

2. Méthode rejeterEnquete(id, chefId, commentaire?) :
   - Même correction que validerEnquete()
   - Format URL : POST /api/validation/enquetes/{id}/rejeter?chefId={chefId}&commentaire={commentaire}

CODE CORRECT :

```typescript
import { HttpParams } from '@angular/common/http';

validerEnquete(id: number, chefId: number, commentaire?: string): Observable<ValidationEnquete> {
  let params = new HttpParams().set('chefId', chefId.toString());
  
  if (commentaire && commentaire.trim() !== '') {
    params = params.set('commentaire', commentaire);
  }
  
  return this.http.post<ValidationEnquete>(
    `${this.apiUrl}/validation/enquetes/${id}/valider`,
    null, // IMPORTANT : Pas de body JSON
    { params: params }
  ).pipe(
    catchError(this.handleError)
  );
}

rejeterEnquete(id: number, chefId: number, commentaire?: string): Observable<ValidationEnquete> {
  let params = new HttpParams().set('chefId', chefId.toString());
  
  if (commentaire && commentaire.trim() !== '') {
    params = params.set('commentaire', commentaire);
  }
  
  return this.http.post<ValidationEnquete>(
    `${this.apiUrl}/validation/enquetes/${id}/rejeter`,
    null, // IMPORTANT : Pas de body JSON
    { params: params }
  ).pipe(
    catchError(this.handleError)
  );
}
```

VÉRIFICATIONS :
- [ ] HttpParams est importé depuis @angular/common/http
- [ ] chefId est dans l'URL (query parameter), pas dans le body
- [ ] commentaire est dans l'URL (query parameter) si présent, pas dans le body
- [ ] Le body est null ou vide
- [ ] Les headers d'authentification (JWT) sont toujours présents
- [ ] La gestion d'erreurs fonctionne toujours

TEST :
Après correction, la requête dans la console réseau doit montrer :
- URL : POST /api/validation/enquetes/5/valider?chefId=32&commentaire=valider
- Body : (vide ou null)
- Status : 200 OK (au lieu de 500)
```

---

## 🎯 PROMPT 7 : Création du Composant de Statistiques des Enquêtes

**Prompt à copier dans Cursor AI :**

```
Dans le projet Angular, créez un nouveau composant de statistiques des enquêtes (statistiques-enquetes.component.ts et statistiques-enquetes.component.html).

Ce composant doit :

1. Afficher des cartes de statistiques :
   - Total d'enquêtes (carte avec icône)
   - Enquêtes validées (carte verte)
   - Enquêtes non validées (carte orange)
   - Enquêtes créées ce mois (carte bleue)
   - Utiliser MatCard pour chaque statistique

2. Graphiques (optionnel, si Chart.js ou similaire disponible) :
   - Graphique en barres : Enquêtes par statut
   - Graphique en ligne : Évolution des enquêtes créées par mois
   - Graphique en camembert : Répartition des enquêtes par secteur d'activité

3. Tableau des statistiques par agent :
   - Afficher un tableau avec :
     * Nom de l'agent
     * Nombre d'enquêtes créées
     * Nombre d'enquêtes dont il est responsable
     * Taux de validation
   - Utiliser MatTable avec tri

4. Filtres temporels :
   - Sélecteur de période (ce mois, ce trimestre, cette année, personnalisé)
   - Date picker pour période personnalisée
   - Rafraîchir les statistiques selon la période sélectionnée

5. Chargement des données :
   - Charger toutes les statistiques au ngOnInit
   - Afficher un spinner pendant le chargement
   - Gérer les erreurs avec des messages appropriés

6. Service de statistiques :
   - Créer ou utiliser EnqueteService pour appeler les endpoints de statistiques
   - Utiliser les endpoints :
     * GET /api/enquettes/statistiques/total
     * GET /api/enquettes/statistiques/valides
     * GET /api/enquettes/statistiques/non-valides
     * GET /api/enquettes/statistiques/ce-mois
     * GET /api/enquettes/statistiques/statut/{statut}
     * GET /api/enquettes/statistiques/agent/{agentId}/crees
     * GET /api/enquettes/statistiques/agent/{agentId}/responsable

7. UX/UI :
   - Utiliser Material Design
   - Responsive design
   - Animations pour les cartes
   - Couleurs appropriées pour chaque type de statistique

IMPORTANT : Ce composant doit être accessible et afficher des statistiques en temps réel.
```

---

## 🎯 PROMPT 8 : Mise à Jour du Service ValidationEnqueteService

**Prompt à copier dans Cursor AI :**

```
Dans le projet Angular, localisez ou créez le service ValidationEnqueteService (probablement dans src/app/services/validation-enquete.service.ts).

Mettez à jour ce service pour inclure TOUTES les méthodes suivantes :

1. CRUD Operations :
   - createValidationEnquete(validation: ValidationEnquete): Observable<ValidationEnquete>
     * POST /api/validation/enquetes
   
   - getValidationEnqueteById(id: number): Observable<ValidationEnquete>
     * GET /api/validation/enquetes/{id}
   
   - getAllValidationsEnquete(): Observable<ValidationEnquete[]>
     * GET /api/validation/enquetes
   
   - updateValidationEnquete(id: number, validation: ValidationEnquete): Observable<ValidationEnquete>
     * PUT /api/validation/enquetes/{id}
   
   - deleteValidationEnquete(id: number): Observable<void>
     * DELETE /api/validation/enquetes/{id}

2. Opérations spécifiques :
   - getEnquetesEnAttente(): Observable<ValidationEnquete[]>
     * GET /api/validation/enquetes/en-attente
     * IMPORTANT : Filtrer les validations orphelines côté frontend si nécessaire
   
   - getValidationsByEnquete(enqueteId: number): Observable<ValidationEnquete[]>
     * GET /api/validation/enquetes/enquete/{enqueteId}
   
   - getValidationsByAgent(agentId: number): Observable<ValidationEnquete[]>
     * GET /api/validation/enquetes/agent/{agentId}
   
   - getValidationsByChef(chefId: number): Observable<ValidationEnquete[]>
     * GET /api/validation/enquetes/chef/{chefId}
   
   - getValidationsByStatut(statut: string): Observable<ValidationEnquete[]>
     * GET /api/validation/enquetes/statut/{statut}

3. Validation/Rejet (⚠️ IMPORTANT - Format des paramètres) :
   - validerEnquete(validationId: number, chefId: number, commentaire?: string): Observable<ValidationEnquete>
     * POST /api/validation/enquetes/{id}/valider?chefId={chefId}&commentaire={commentaire}
     * ⚠️ CRITIQUE : chefId et commentaire doivent être dans l'URL (query parameters), PAS dans le body JSON
     * Utiliser HttpParams pour construire les query parameters
     * Body doit être null ou vide
   
   - rejeterEnquete(validationId: number, chefId: number, commentaire?: string): Observable<ValidationEnquete>
     * POST /api/validation/enquetes/{id}/rejeter?chefId={chefId}&commentaire={commentaire}
     * ⚠️ CRITIQUE : chefId et commentaire doivent être dans l'URL (query parameters), PAS dans le body JSON
     * Utiliser HttpParams pour construire les query parameters
     * Body doit être null ou vide
   
   - remettreEnAttente(validationId: number, commentaire?: string): Observable<ValidationEnquete>
     * POST /api/validation/enquetes/{id}/en-attente?commentaire={commentaire}

4. Statistiques :
   - countValidationsByStatut(statut: string): Observable<number>
     * GET /api/validation/enquetes/statistiques/statut/{statut}
   
   - countValidationsByAgent(agentId: number): Observable<number>
     * GET /api/validation/enquetes/statistiques/agent/{agentId}
   
   - countValidationsByChef(chefId: number): Observable<number>
     * GET /api/validation/enquetes/statistiques/chef/{chefId}

5. Maintenance :
   - nettoyerValidationsOrphelines(): Observable<number>
     * POST /api/validation/enquetes/nettoyer-orphelines

IMPORTANT :
- Utiliser HttpClient avec gestion d'erreurs
- Retourner des Observables typés
- Gérer les cas où les réponses peuvent être null
- Afficher des messages d'erreur utilisateur-friendly
- ⚠️ CRITIQUE : Pour validerEnquete() et rejeterEnquete(), utiliser HttpParams pour envoyer chefId et commentaire dans l'URL (query parameters), PAS dans le body JSON. Voir PROMPT 6.5 pour les détails.
```

---

## 🎯 PROMPT 9 : Mise à Jour du Routing et Navigation

**Prompt à copier dans Cursor AI :**

```
Dans le projet Angular, localisez le fichier de routing (probablement app-routing.module.ts ou routes dans app.config.ts).

Ajoutez ou mettez à jour les routes suivantes pour la gestion des enquêtes :

1. Routes principales :
   - /enquetes : Liste des enquêtes (list-enquetes component)
   - /enquetes/nouvelle : Création d'une nouvelle enquête (create-enquete component)
   - /enquetes/:id : Détails d'une enquête (detail-enquete component)
   - /enquetes/:id/modifier : Modification d'une enquête (edit-enquete component)

2. Routes de validation (pour les chefs) :
   - /enquetes/validation : Liste des enquêtes en attente de validation (validation-enquetes component)
   - /enquetes/:id/valider : Page de validation d'une enquête (valider-enquete component)

3. Routes de statistiques :
   - /enquetes/statistiques : Page de statistiques des enquêtes (statistiques-enquetes component)

4. Guards (si nécessaire) :
   - Créer un guard pour protéger les routes de validation (seulement pour les chefs)
   - Créer un guard pour protéger les routes de modification (seulement si non validée)

5. Navigation :
   - Ajouter les liens dans le menu de navigation principal
   - Ajouter des breadcrumbs pour la navigation
   - Ajouter des boutons de navigation dans les composants

IMPORTANT : Assurez-vous que toutes les routes sont correctement configurées et que la navigation fonctionne correctement.
```

---

## 🎯 PROMPT 10 : Gestion des Erreurs Globale

**Prompt à copier dans Cursor AI :**

```
Dans le projet Angular, créez ou améliorez un service global de gestion des erreurs (error-handler.service.ts).

Ce service doit :

1. Intercepter les erreurs HTTP :
   - Créer un HttpInterceptor pour intercepter toutes les requêtes HTTP
   - Gérer les codes d'erreur suivants :
     * 400 : Bad Request - Afficher le message d'erreur du backend
     * 401 : Unauthorized - Rediriger vers la page de connexion
     * 403 : Forbidden - Afficher un message de permission refusée
     * 404 : Not Found - Gérer selon le contexte (peut être normal pour certaines requêtes)
     * 409 : Conflict - Afficher le message d'erreur du backend
     * 500 : Internal Server Error - Afficher un message générique

2. Messages d'erreur utilisateur-friendly :
   - Traduire les messages techniques en messages compréhensibles
   - Afficher les messages avec MatSnackBar
   - Logger les erreurs pour le débogage

3. Gestion spécifique pour les enquêtes :
   - "Le dossierId est obligatoire" pour 400 avec message dossierId
   - "Dossier non trouvé" pour 404 lors de la création
   - "Ce dossier a déjà une enquête associée" pour 409
   - "Enquête non trouvée" pour 404 lors de la lecture
   - "Impossible de supprimer l'enquête : contrainte de base de données" pour 409 lors de la suppression

4. Configuration :
   - Enregistrer l'interceptor dans app.config.ts ou app.module.ts
   - Configurer les options de MatSnackBar (durée, position, etc.)

IMPORTANT : Ce service doit être utilisé globalement pour toutes les requêtes HTTP du projet.
```

---

## 🎯 PROMPT 11 : Amélioration de l'UX/UI Globale

**Prompt à copier dans Cursor AI :**

```
Dans le projet Angular, améliorez l'UX/UI globale pour la gestion des enquêtes :

1. Loading States :
   - Ajouter des MatSpinner ou MatProgressBar pour toutes les opérations asynchrones
   - Afficher un loading overlay pendant les opérations longues
   - Désactiver les boutons pendant les opérations

2. Confirmations :
   - Utiliser MatDialog pour toutes les actions destructives (suppression, rejet)
   - Afficher des messages de confirmation clairs
   - Permettre l'annulation

3. Notifications :
   - Utiliser MatSnackBar pour tous les messages de succès/erreur
   - Configurer la durée et la position
   - Utiliser des couleurs appropriées (vert pour succès, rouge pour erreur, orange pour avertissement)

4. Formulaires :
   - Utiliser Material Design pour tous les formulaires
   - Ajouter des validators visuels (messages d'erreur sous les champs)
   - Utiliser des tooltips pour expliquer les champs
   - Organiser les formulaires en sections logiques

5. Tableaux :
   - Utiliser MatTable avec pagination, tri et filtrage
   - Ajouter des actions sur les lignes
   - Utiliser des badges pour les statuts
   - Responsive design

6. Couleurs et thème :
   - Utiliser un thème Material cohérent
   - Couleurs pour les statuts :
     * EN_ATTENTE_VALIDATION : Orange
     * VALIDE : Vert
     * REJETE : Rouge

7. Responsive Design :
   - Assurer que tous les composants sont responsive
   - Utiliser FlexLayout si disponible
   - Tester sur différentes tailles d'écran

IMPORTANT : Assurez-vous que l'interface est professionnelle, intuitive et cohérente dans tout le projet.
```

---

## 📝 Notes Importantes

1. **Ordre d'exécution** : Suivez les prompts dans l'ordre pour une intégration progressive.

2. **Compatibilité** : Ne supprimez pas les fonctionnalités existantes, seulement améliorez-les.

3. **Tests** : Après chaque prompt, testez les fonctionnalités pour vous assurer qu'elles fonctionnent correctement.

4. **Documentation** : Commentez le code pour faciliter la maintenance future.

5. **Gestion d'erreurs** : Toujours gérer les erreurs avec des messages clairs pour l'utilisateur.

6. **Performance** : Optimisez les requêtes HTTP et évitez les appels inutiles.

7. **Sécurité** : Vérifiez les permissions avant d'afficher les actions sensibles.

---

## ✅ Checklist de Vérification

Après avoir exécuté tous les prompts, vérifiez que :

- [ ] Toutes les interfaces TypeScript sont à jour
- [ ] Tous les services sont implémentés et fonctionnels
- [ ] Tous les composants CRUD fonctionnent correctement
- [ ] La validation des enquêtes fonctionne pour les chefs
- [ ] Les statistiques s'affichent correctement
- [ ] La gestion des erreurs est complète
- [ ] L'UX/UI est professionnelle et cohérente
- [ ] Le routing et la navigation fonctionnent
- [ ] Les permissions sont correctement gérées
- [ ] Le dossierId est automatiquement affecté lors de la sélection

---

**Bon développement ! 🚀**


