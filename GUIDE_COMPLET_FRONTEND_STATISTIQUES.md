# 📊 Guide Complet Frontend : Intégration des Statistiques

## 🎯 Objectif

Ce guide fournit toutes les informations nécessaires pour intégrer complètement les statistiques dans le frontend, avec tous les endpoints API, leurs formats de réponse, et les instructions d'intégration.

---

## 🔧 Corrections Backend Appliquées

### ✅ Problème Résolu : Statistique `totalEnquetes` Manquante

**Corrections appliquées :**
1. ✅ Ajout de `totalEnquetes` dans `getStatistiquesGlobales()`
2. ✅ Ajout de `TOTAL_ENQUETES` dans l'enum `TypeStatistique`
3. ✅ Ajout du mapping dans `getTypeStatistiqueFromKey()`

**Résultat :** Les statistiques incluent maintenant :
- `totalEnquetes` : Nombre total d'enquêtes créées (validées ou non)
- `enquetesCompletees` : Nombre d'enquêtes validées uniquement

### ✅ Initialisation de la Table Statistiques

**Pour initialiser/recalculer les statistiques dans la base de données :**

1. **Via API (Recommandé) :**
   ```
   POST /api/statistiques/recalculer
   Headers: Authorization: Bearer {token}
   ```
   - Accessible uniquement par SUPER_ADMIN
   - Force le recalcul immédiat et stocke dans la table `statistiques`

2. **Via SQL (Alternative) :**
   - Exécuter le script `initialiser_statistiques.sql`
   - Puis appeler l'API `/api/statistiques/recalculer` pour calculer les valeurs

---

## 📡 Tous les Endpoints API Disponibles

### Base URL
```
http://localhost:8089/carthage-creance/api/statistiques
```

---

### 1. Statistiques Globales (SuperAdmin)

**Endpoint :** `GET /api/statistiques/globales`  
**Accès :** SUPER_ADMIN uniquement  
**Headers :** `Authorization: Bearer {token}`

**Réponse JSON :**
```json
{
  "totalDossiers": 10,
  "dossiersEnCours": 5,
  "dossiersValides": 8,
  "dossiersRejetes": 1,
  "dossiersClotures": 2,
  "dossiersCreesCeMois": 3,
  "dossiersPhaseCreation": 2,
  "dossiersPhaseEnquete": 3,
  "dossiersPhaseAmiable": 4,
  "dossiersPhaseJuridique": 1,
  "totalEnquetes": 5,
  "enquetesCompletees": 3,
  "actionsAmiables": 12,
  "actionsAmiablesCompletees": 8,
  "documentsHuissierCrees": 15,
  "documentsHuissierCompletes": 10,
  "actionsHuissierCrees": 7,
  "actionsHuissierCompletes": 4,
  "audiencesTotales": 6,
  "audiencesProchaines": 2,
  "tachesCompletees": 20,
  "tachesEnCours": 5,
  "tachesEnRetard": 2,
  "tauxReussiteGlobal": 20.0,
  "montantRecouvre": 50000.0,
  "montantEnCours": 80000.0
}
```

**Utilisation :** Dashboard principal SuperAdmin

---

### 2. Statistiques par Période (SuperAdmin)

**Endpoint :** `GET /api/statistiques/periode?dateDebut=2025-12-01&dateFin=2025-12-31`  
**Accès :** SUPER_ADMIN uniquement  
**Headers :** `Authorization: Bearer {token}`

**Paramètres :**
- `dateDebut` : Date de début (format: YYYY-MM-DD)
- `dateFin` : Date de fin (format: YYYY-MM-DD)

**Réponse JSON :** Similaire à `/globales` mais filtré par période

**Utilisation :** Rapports par période, graphiques temporels

---

### 3. Statistiques Département (Chef)

**Endpoint :** `GET /api/statistiques/departement`  
**Accès :** CHEF_DEPARTEMENT_* uniquement  
**Headers :** `Authorization: Bearer {token}`

**Réponse JSON :**
```json
{
  "dossiersEnCours": 5,
  "dossiersValides": 8,
  "dossiersClotures": 2,
  "enquetesCompletees": 3,
  "actionsAmiables": 12,
  "documentsHuissierCrees": 15,
  "audiencesTotales": 6,
  "tachesEnCours": 5,
  "tachesEnRetard": 2,
  "chef": {
    "dossiersTraites": 10,
    "dossiersValides": 8,
    "dossiersClotures": 2,
    "enquetesCompletees": 3,
    "actionsAmiables": 12,
    "documentsHuissier": 15,
    "actionsHuissier": 7,
    "audiences": 6,
    "tachesCompletees": 20,
    "tachesEnCours": 5,
    "tachesEnRetard": 2
  }
}
```

**Utilisation :** Dashboard chef de département

---

### 4. Statistiques Mes Agents (Chef)

**Endpoint :** `GET /api/statistiques/mes-agents`  
**Accès :** CHEF_DEPARTEMENT_* uniquement  
**Headers :** `Authorization: Bearer {token}`

**Réponse JSON :** Statistiques du chef et de ses agents

**Utilisation :** Vue détaillée des performances des agents

---

### 5. Statistiques Mes Dossiers (Agent)

**Endpoint :** `GET /api/statistiques/mes-dossiers`  
**Accès :** AGENT_* uniquement  
**Headers :** `Authorization: Bearer {token}`

**Réponse JSON :**
```json
{
  "dossiersTraites": 10,
  "dossiersValides": 8,
  "dossiersClotures": 2,
  "enquetesCompletees": 3,
  "actionsAmiables": 12,
  "documentsHuissier": 15,
  "actionsHuissier": 7,
  "audiences": 6,
  "tachesCompletees": 20,
  "tachesEnCours": 5,
  "tachesEnRetard": 2
}
```

**Utilisation :** Dashboard agent

---

### 6. Statistiques d'un Agent Spécifique (Chef/SuperAdmin)

**Endpoint :** `GET /api/statistiques/agent/{agentId}`  
**Accès :** CHEF_* ou SUPER_ADMIN  
**Headers :** `Authorization: Bearer {token}`

**Paramètres :**
- `agentId` : ID de l'agent (dans l'URL)

**Réponse JSON :** Similaire à `/mes-dossiers` mais pour l'agent spécifié

**Utilisation :** Vue détaillée d'un agent spécifique

---

### 7. Statistiques Dossiers (SuperAdmin)

**Endpoint :** `GET /api/statistiques/dossiers`  
**Accès :** SUPER_ADMIN uniquement  
**Headers :** `Authorization: Bearer {token}`

**Réponse JSON :** Statistiques détaillées sur les dossiers

**Utilisation :** Vue détaillée des dossiers

---

### 8. Statistiques Actions Amiables (SuperAdmin)

**Endpoint :** `GET /api/statistiques/actions-amiables`  
**Accès :** SUPER_ADMIN uniquement  
**Headers :** `Authorization: Bearer {token}`

**Réponse JSON :** Statistiques détaillées sur les actions amiables

**Utilisation :** Vue détaillée des actions amiables

---

### 9. Statistiques Audiences (SuperAdmin)

**Endpoint :** `GET /api/statistiques/audiences`  
**Accès :** SUPER_ADMIN uniquement  
**Headers :** `Authorization: Bearer {token}`

**Réponse JSON :** Statistiques détaillées sur les audiences

**Utilisation :** Vue détaillée des audiences

---

### 10. Statistiques Tâches (SuperAdmin)

**Endpoint :** `GET /api/statistiques/taches`  
**Accès :** SUPER_ADMIN uniquement  
**Headers :** `Authorization: Bearer {token}`

**Réponse JSON :** Statistiques détaillées sur les tâches

**Utilisation :** Vue détaillée des tâches

---

### 11. Statistiques Financières (SuperAdmin)

**Endpoint :** `GET /api/statistiques/financieres`  
**Accès :** SUPER_ADMIN uniquement  
**Headers :** `Authorization: Bearer {token}`

**Réponse JSON :**
```json
{
  "montantRecouvre": 50000.0,
  "montantEnCours": 80000.0,
  "totalFraisEngages": 5000.0,
  "fraisRecuperes": 4000.0,
  "netGenere": 45000.0
}
```

**Utilisation :** Dashboard financier

---

### 12. Statistiques Tous les Chefs (SuperAdmin)

**Endpoint :** `GET /api/statistiques/chefs`  
**Accès :** SUPER_ADMIN uniquement  
**Headers :** `Authorization: Bearer {token}`

**Réponse JSON :** Statistiques de tous les chefs

**Utilisation :** Comparaison des performances des chefs

---

### 13. Recalcul Manuel des Statistiques (SuperAdmin)

**Endpoint :** `POST /api/statistiques/recalculer`  
**Accès :** SUPER_ADMIN uniquement  
**Headers :** `Authorization: Bearer {token}`

**Réponse :**
```json
"Statistiques recalculées avec succès"
```

**Utilisation :** Forcer le recalcul immédiat des statistiques (utile après import de données ou correction)

---

## 🔄 Logique de Détection du Rôle

Le frontend doit détecter le rôle de l'utilisateur et appeler l'endpoint approprié :

```typescript
// Exemple de logique
const userRole = this.authService.getUserRole();

if (userRole === 'SUPER_ADMIN') {
    // Appeler /api/statistiques/globales
    this.loadGlobalStats();
} else if (userRole?.startsWith('CHEF_')) {
    // Appeler /api/statistiques/departement
    this.loadDepartmentStats();
} else if (userRole?.startsWith('AGENT_')) {
    // Appeler /api/statistiques/mes-dossiers
    this.loadMyStats();
}
```

---

## 📊 Mapping Statistiques → Interface

### Tableau de Correspondance Complet

| Clé API | Label Interface | Type | Emplacement Suggéré |
|---------|----------------|------|---------------------|
| `totalDossiers` | "Total Dossiers" | number | Dashboard principal |
| `dossiersEnCours` | "Dossiers en Cours" | number | Dashboard principal |
| `dossiersValides` | "Dossiers Validés" | number | Dashboard principal |
| `dossiersRejetes` | "Dossiers Rejetés" | number | Dashboard principal |
| `dossiersClotures` | "Dossiers Clôturés" | number | Dashboard principal |
| `dossiersCreesCeMois` | "Dossiers Créés ce Mois" | number | Dashboard principal |
| `dossiersPhaseCreation` | "Phase Création" | number | Vue par phase |
| `dossiersPhaseEnquete` | "Phase Enquête" | number | Vue par phase |
| `dossiersPhaseAmiable` | "Phase Amiable" | number | Vue par phase |
| `dossiersPhaseJuridique` | "Phase Juridique" | number | Vue par phase |
| `totalEnquetes` | "Total Enquêtes" | number | Dashboard enquêtes |
| `enquetesCompletees` | "Enquêtes Complétées" | number | Dashboard enquêtes |
| `actionsAmiables` | "Actions Amiables" | number | Dashboard actions |
| `actionsAmiablesCompletees` | "Actions Complétées" | number | Dashboard actions |
| `documentsHuissierCrees` | "Documents Créés" | number | Dashboard huissier |
| `documentsHuissierCompletes` | "Documents Complétés" | number | Dashboard huissier |
| `actionsHuissierCrees` | "Actions Créées" | number | Dashboard huissier |
| `actionsHuissierCompletes` | "Actions Complétées" | number | Dashboard huissier |
| `audiencesTotales` | "Total Audiences" | number | Dashboard audiences |
| `audiencesProchaines` | "Audiences Prochaines" | number | Dashboard audiences |
| `tachesCompletees` | "Tâches Complétées" | number | Dashboard tâches |
| `tachesEnCours` | "Tâches en Cours" | number | Dashboard tâches |
| `tachesEnRetard` | "Tâches en Retard" | number | Dashboard tâches (alerte) |
| `tauxReussiteGlobal` | "Taux de Réussite" | number (%) | Dashboard principal |
| `montantRecouvre` | "Montant Recouvré" | number (TND) | Dashboard financier |
| `montantEnCours` | "Montant en Cours" | number (TND) | Dashboard financier |

---

## 🔧 Service Angular à Créer

### Fichier : `statistique.service.ts`

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { AuthService } from './auth.service';

export interface StatistiquesGlobales {
  totalDossiers: number;
  dossiersEnCours: number;
  dossiersValides: number;
  dossiersRejetes: number;
  dossiersClotures: number;
  dossiersCreesCeMois: number;
  dossiersPhaseCreation: number;
  dossiersPhaseEnquete: number;
  dossiersPhaseAmiable: number;
  dossiersPhaseJuridique: number;
  totalEnquetes: number;
  enquetesCompletees: number;
  actionsAmiables: number;
  actionsAmiablesCompletees: number;
  documentsHuissierCrees: number;
  documentsHuissierCompletes: number;
  actionsHuissierCrees: number;
  actionsHuissierCompletes: number;
  audiencesTotales: number;
  audiencesProchaines: number;
  tachesCompletees: number;
  tachesEnCours: number;
  tachesEnRetard: number;
  tauxReussiteGlobal: number;
  montantRecouvre: number;
  montantEnCours: number;
}

@Injectable({
  providedIn: 'root'
})
export class StatistiqueService {
  private apiUrl = 'http://localhost:8089/carthage-creance/api/statistiques';
  private statsSubject = new BehaviorSubject<StatistiquesGlobales | null>(null);
  public stats$ = this.statsSubject.asObservable();

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  /**
   * Charge les statistiques selon le rôle de l'utilisateur
   */
  loadStatistiques(): Observable<StatistiquesGlobales> {
    const endpoint = this.getEndpointByRole();
    const headers = this.getHeaders();
    
    return this.http.get<StatistiquesGlobales>(endpoint, { headers }).pipe(
      tap(stats => {
        console.log('Statistiques chargées:', stats);
        this.statsSubject.next(stats);
      }),
      catchError(this.handleError)
    );
  }

  /**
   * Charge les statistiques globales (SuperAdmin uniquement)
   */
  loadGlobalStats(): Observable<StatistiquesGlobales> {
    const headers = this.getHeaders();
    return this.http.get<StatistiquesGlobales>(`${this.apiUrl}/globales`, { headers }).pipe(
      tap(stats => this.statsSubject.next(stats)),
      catchError(this.handleError)
    );
  }

  /**
   * Charge les statistiques du département (Chef uniquement)
   */
  loadDepartmentStats(): Observable<any> {
    const headers = this.getHeaders();
    return this.http.get<any>(`${this.apiUrl}/departement`, { headers }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Charge les statistiques personnelles (Agent uniquement)
   */
  loadMyStats(): Observable<any> {
    const headers = this.getHeaders();
    return this.http.get<any>(`${this.apiUrl}/mes-dossiers`, { headers }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Charge les statistiques par période (SuperAdmin uniquement)
   */
  loadStatsByPeriod(dateDebut: string, dateFin: string): Observable<StatistiquesGlobales> {
    const headers = this.getHeaders();
    const params = new HttpParams()
      .set('dateDebut', dateDebut)
      .set('dateFin', dateFin);
    
    return this.http.get<StatistiquesGlobales>(`${this.apiUrl}/periode`, { headers, params }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Charge les statistiques financières (SuperAdmin uniquement)
   */
  loadFinancialStats(): Observable<any> {
    const headers = this.getHeaders();
    return this.http.get<any>(`${this.apiUrl}/financieres`, { headers }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Charge les statistiques d'un agent spécifique (Chef/SuperAdmin)
   */
  loadAgentStats(agentId: number): Observable<any> {
    const headers = this.getHeaders();
    return this.http.get<any>(`${this.apiUrl}/agent/${agentId}`, { headers }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Rafraîchit les statistiques après une action
   */
  refreshAfterAction(): void {
    // Attendre 1 seconde pour laisser le temps au backend de recalculer
    setTimeout(() => {
      this.loadStatistiques().subscribe({
        next: () => console.log('Statistiques rafraîchies'),
        error: (err) => console.error('Erreur rafraîchissement stats:', err)
      });
    }, 1000);
  }

  /**
   * Force le recalcul des statistiques (SuperAdmin uniquement)
   */
  recalculerStatistiques(): Observable<string> {
    const headers = this.getHeaders();
    return this.http.post<string>(`${this.apiUrl}/recalculer`, {}, { headers }).pipe(
      tap(() => {
        // Rafraîchir après recalcul
        setTimeout(() => this.loadStatistiques().subscribe(), 2000);
      }),
      catchError(this.handleError)
    );
  }

  /**
   * Détermine l'endpoint selon le rôle
   */
  private getEndpointByRole(): string {
    const role = this.authService.getUserRole();
    
    if (role === 'SUPER_ADMIN') {
      return `${this.apiUrl}/globales`;
    } else if (role?.startsWith('CHEF_')) {
      return `${this.apiUrl}/departement`;
    } else if (role?.startsWith('AGENT_')) {
      return `${this.apiUrl}/mes-dossiers`;
    }
    
    // Par défaut, essayer globales
    return `${this.apiUrl}/globales`;
  }

  /**
   * Crée les headers avec le token
   */
  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  /**
   * Gère les erreurs HTTP
   */
  private handleError = (error: any): Observable<never> => {
    let errorMessage = 'Une erreur est survenue';
    
    if (error.status === 401) {
      errorMessage = 'Token expiré. Veuillez vous reconnecter.';
      // Rediriger vers login
      // this.router.navigate(['/login']);
    } else if (error.status === 403) {
      errorMessage = 'Vous n\'avez pas les droits pour accéder à ces statistiques.';
    } else if (error.status === 500) {
      errorMessage = 'Erreur serveur. Veuillez réessayer plus tard.';
    } else if (error.error?.message) {
      errorMessage = error.error.message;
    }
    
    console.error('Erreur statistiques:', errorMessage, error);
    return throwError(() => new Error(errorMessage));
  };
}
```

---

## 📱 Composants à Créer/Modifier

### 1. Dashboard Principal (SuperAdmin)

**Fichier :** `dashboard-admin.component.ts`

```typescript
import { Component, OnInit } from '@angular/core';
import { StatistiqueService, StatistiquesGlobales } from '../services/statistique.service';

@Component({
  selector: 'app-dashboard-admin',
  templateUrl: './dashboard-admin.component.html',
  styleUrls: ['./dashboard-admin.component.css']
})
export class DashboardAdminComponent implements OnInit {
  stats: StatistiquesGlobales | null = null;
  loading = true;
  error: string | null = null;

  constructor(private statistiqueService: StatistiqueService) {}

  ngOnInit(): void {
    this.loadStats();
  }

  loadStats(): void {
    this.loading = true;
    this.error = null;
    
    this.statistiqueService.loadGlobalStats().subscribe({
      next: (stats) => {
        this.stats = stats;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.message;
        this.loading = false;
      }
    });
  }

  recalculer(): void {
    this.statistiqueService.recalculerStatistiques().subscribe({
      next: () => {
        alert('Statistiques recalculées avec succès');
        this.loadStats();
      },
      error: (err) => {
        alert('Erreur lors du recalcul: ' + err.message);
      }
    });
  }
}
```

**Template :** `dashboard-admin.component.html`

```html
<div class="dashboard-container">
  <div class="dashboard-header">
    <h1>Tableau de Bord Administrateur</h1>
    <button (click)="recalculer()" class="btn btn-secondary">Recalculer les Statistiques</button>
  </div>

  <div *ngIf="loading" class="loading">
    <p>Chargement des statistiques...</p>
  </div>

  <div *ngIf="error" class="error">
    <p>{{ error }}</p>
  </div>

  <div *ngIf="stats && !loading" class="stats-grid">
    <!-- Cartes principales -->
    <div class="stat-card primary">
      <h3>Total Dossiers</h3>
      <p class="stat-value">{{ stats.totalDossiers | number }}</p>
    </div>

    <div class="stat-card">
      <h3>Dossiers en Cours</h3>
      <p class="stat-value">{{ stats.dossiersEnCours | number }}</p>
    </div>

    <div class="stat-card success">
      <h3>Dossiers Validés</h3>
      <p class="stat-value">{{ stats.dossiersValides | number }}</p>
    </div>

    <div class="stat-card success">
      <h3>Dossiers Clôturés</h3>
      <p class="stat-value">{{ stats.dossiersClotures | number }}</p>
    </div>

    <div class="stat-card">
      <h3>Dossiers Créés ce Mois</h3>
      <p class="stat-value">{{ stats.dossiersCreesCeMois | number }}</p>
    </div>

    <div class="stat-card info">
      <h3>Taux de Réussite</h3>
      <p class="stat-value">{{ stats.tauxReussiteGlobal | number:'1.1-1' }}%</p>
    </div>

    <!-- Statistiques Enquêtes -->
    <div class="stat-card">
      <h3>Total Enquêtes</h3>
      <p class="stat-value">{{ stats.totalEnquetes | number }}</p>
    </div>

    <div class="stat-card success">
      <h3>Enquêtes Complétées</h3>
      <p class="stat-value">{{ stats.enquetesCompletees | number }}</p>
    </div>

    <!-- Statistiques Financières -->
    <div class="stat-card success">
      <h3>Montant Recouvré</h3>
      <p class="stat-value">{{ stats.montantRecouvre | number:'1.0-0' }} TND</p>
    </div>

    <div class="stat-card warning">
      <h3>Montant en Cours</h3>
      <p class="stat-value">{{ stats.montantEnCours | number:'1.0-0' }} TND</p>
    </div>

    <!-- Autres statistiques... -->
  </div>
</div>
```

---

## 🔄 Points d'Intégration Frontend

### Actions qui Déclenchent le Rafraîchissement

| Action | Composant | Méthode à Modifier |
|--------|-----------|-------------------|
| Création de dossier | `create-dossier.component.ts` | `onDossierCreated()` |
| Modification de dossier | `edit-dossier.component.ts` | `onDossierUpdated()` |
| Validation de dossier | `validate-dossier.component.ts` | `onDossierValidated()` |
| Création d'enquête | `create-enquete.component.ts` | `onEnqueteCreated()` |
| Validation d'enquête | `validate-enquete.component.ts` | `onEnqueteValidated()` |
| Création d'action amiable | `create-action.component.ts` | `onActionCreated()` |
| Création de document huissier | `create-document-huissier.component.ts` | `onDocumentCreated()` |
| Création d'action huissier | `create-action-huissier.component.ts` | `onActionHuissierCreated()` |
| Création d'audience | `create-audience.component.ts` | `onAudienceCreated()` |
| Affectation avocat/huissier | `assign-avocat-huissier.component.ts` | `onAffectationDone()` |
| Validation de frais | `validate-frais.component.ts` | `onFraisValidated()` |
| Génération de facture | `generate-facture.component.ts` | `onFactureGenerated()` |

**Exemple d'intégration :**

```typescript
// Dans create-enquete.component.ts
onEnqueteCreated() {
  this.enqueteService.createEnquete(data).subscribe({
    next: () => {
      // Rafraîchir les statistiques après création
      this.statistiqueService.refreshAfterAction();
      // Afficher message de succès
      this.showSuccessMessage('Enquête créée avec succès');
    },
    error: (err) => {
      // Gérer l'erreur
      this.showErrorMessage('Erreur lors de la création: ' + err.message);
    }
  });
}
```

---

## ✅ Checklist d'Intégration Complète

### Service Statistique
- [ ] Créer `statistique.service.ts`
- [ ] Implémenter `loadStatistiques()`
- [ ] Implémenter `loadGlobalStats()`
- [ ] Implémenter `loadDepartmentStats()`
- [ ] Implémenter `loadMyStats()`
- [ ] Implémenter `refreshAfterAction()`
- [ ] Implémenter `recalculerStatistiques()`
- [ ] Implémenter détection de rôle pour endpoint
- [ ] Gérer les erreurs (401, 403, 500)

### Composants Dashboard
- [ ] Créer/modifier `dashboard-admin.component.ts`
- [ ] Créer/modifier `dashboard-chef.component.ts`
- [ ] Créer/modifier `dashboard-agent.component.ts`
- [ ] Créer/modifier `dashboard-enquetes.component.ts`
- [ ] Créer/modifier `dashboard-actions.component.ts`
- [ ] Créer/modifier `dashboard-huissier.component.ts`
- [ ] Créer/modifier `dashboard-audiences.component.ts`
- [ ] Créer/modifier `dashboard-taches.component.ts`
- [ ] Créer/modifier `dashboard-phases.component.ts`
- [ ] Créer/modifier `dashboard-financier.component.ts`

### Intégration Actions
- [ ] Ajouter `refreshAfterAction()` dans création de dossier
- [ ] Ajouter `refreshAfterAction()` dans modification de dossier
- [ ] Ajouter `refreshAfterAction()` dans validation de dossier
- [ ] Ajouter `refreshAfterAction()` dans création d'enquête
- [ ] Ajouter `refreshAfterAction()` dans validation d'enquête
- [ ] Ajouter `refreshAfterAction()` dans création d'action
- [ ] Ajouter `refreshAfterAction()` dans création de document huissier
- [ ] Ajouter `refreshAfterAction()` dans création d'action huissier
- [ ] Ajouter `refreshAfterAction()` dans création d'audience
- [ ] Ajouter `refreshAfterAction()` dans affectation avocat/huissier
- [ ] Ajouter `refreshAfterAction()` dans validation de frais
- [ ] Ajouter `refreshAfterAction()` dans génération de facture

### Affichage
- [ ] Afficher toutes les statistiques dans les interfaces appropriées
- [ ] Utiliser des cartes/étiquettes visuellement claires
- [ ] Afficher des indicateurs de chargement
- [ ] Gérer les cas où les statistiques sont null/undefined
- [ ] Formater les nombres (ex: 1,000 au lieu de 1000)
- [ ] Formater les pourcentages (ex: 20.5% au lieu de 20.5)
- [ ] Formater les montants (ex: 50,000 TND)

### Gestion d'Erreurs
- [ ] Gérer l'erreur 401 (token expiré) → Rediriger vers login
- [ ] Gérer l'erreur 403 (pas les droits) → Afficher message
- [ ] Gérer l'erreur 500 (erreur serveur) → Afficher message d'erreur
- [ ] Gérer les timeouts → Afficher message

---

## 🎨 Formatage des Données

### Nombres Entiers
```typescript
{{ stats.totalDossiers | number }}  // 1,000
```

### Pourcentages
```typescript
{{ stats.tauxReussiteGlobal | number:'1.1-1' }}%  // 20.5%
```

### Montants
```typescript
{{ stats.montantRecouvre | number:'1.0-0' }} TND  // 50,000 TND
```

---

## 🔍 Tests à Effectuer

### Test 1 : Chargement Initial
1. Se connecter en tant que SuperAdmin
2. Accéder au dashboard
3. **Vérifier :** Les statistiques s'affichent correctement
4. **Vérifier :** Les valeurs correspondent aux données réelles

### Test 2 : Mise à Jour après Action
1. Créer un nouveau dossier
2. **Vérifier :** `totalDossiers` s'incrémente
3. **Vérifier :** `dossiersCreesCeMois` s'incrémente
4. **Vérifier :** L'affichage se met à jour automatiquement

### Test 3 : Création d'Enquête
1. Créer une nouvelle enquête
2. **Vérifier :** `totalEnquetes` s'incrémente
3. **Vérifier :** `enquetesCompletees` reste à 0 (si non validée)
4. Valider l'enquête
5. **Vérifier :** `enquetesCompletees` s'incrémente

### Test 4 : Différents Rôles
1. Tester avec SuperAdmin → Voir toutes les statistiques
2. Tester avec Chef → Voir statistiques du département
3. Tester avec Agent → Voir statistiques personnelles

### Test 5 : Recalcul Manuel
1. Se connecter en tant que SuperAdmin
2. Cliquer sur "Recalculer les Statistiques"
3. **Vérifier :** Message de succès affiché
4. **Vérifier :** Les statistiques se rechargent automatiquement

---

## 📝 Notes Importantes

### Performance
- **Cache :** Mettre en cache les statistiques pendant 30 secondes
- **Rafraîchissement :** Ne pas rafraîchir trop souvent (max 1 fois par seconde)
- **Lazy Loading :** Charger les statistiques seulement quand nécessaire

### Accessibilité
- **Labels clairs :** Utiliser des labels descriptifs
- **Unités :** Toujours afficher les unités (TND, %, etc.)
- **Couleurs :** Utiliser des couleurs cohérentes (vert = positif, rouge = négatif)

---

## 🔗 Références

- **API Base URL :** `http://localhost:8089/carthage-creance/api`
- **Documentation Backend :** Voir `RAPPORT_RECALCUL_AUTOMATIQUE_STATISTIQUES.md`
- **Script SQL :** Voir `initialiser_statistiques.sql`

---

## ✅ Résultat Attendu

Après intégration complète :
- ✅ Toutes les statistiques sont affichées dans les interfaces appropriées
- ✅ Les statistiques se mettent à jour automatiquement après chaque action
- ✅ Les statistiques sont calculées en temps réel (pas de délai)
- ✅ Les statistiques sont formatées correctement (nombres, pourcentages, montants)
- ✅ La gestion d'erreurs est robuste
- ✅ L'expérience utilisateur est fluide et réactive
- ✅ La table `statistiques` contient des valeurs réelles (pas de 0)

---

## 🔍 CHECKLIST COMPLÈTE DE VÉRIFICATION

### 📊 Étape 1 : Vérification de la Base de Données

#### 1.1 Vérifier que la table `statistiques` contient des valeurs

**Requête SQL :**
```sql
SELECT 
    type,
    valeur,
    description,
    periode,
    date_calcul
FROM statistiques
WHERE periode = DATE_FORMAT(NOW(), '%Y-%m')
ORDER BY type, date_calcul DESC;
```

**Vérifications :**
- [ ] La table `statistiques` n'est pas vide
- [ ] Toutes les statistiques ont des valeurs (pas de 0 partout)
- [ ] La période correspond au mois actuel (format: "2025-12")
- [ ] Les valeurs correspondent aux données réelles de la base

#### 1.2 Vérifier les statistiques par type

**Requête SQL :**
```sql
-- Vérifier chaque type de statistique
SELECT type, COUNT(*) as nb_enregistrements, MAX(valeur) as valeur_max, MAX(date_calcul) as derniere_calcul
FROM statistiques
WHERE periode = DATE_FORMAT(NOW(), '%Y-%m')
GROUP BY type
ORDER BY type;
```

**Types de statistiques attendus :**
- [ ] `TOTAL_DOSSIERS` - Doit correspondre au nombre total de dossiers
- [ ] `DOSSIERS_EN_COURS` - Doit correspondre aux dossiers avec statut ENCOURSDETRAITEMENT
- [ ] `DOSSIERS_VALIDES` - Doit correspondre aux dossiers validés
- [ ] `DOSSIERS_REJETES` - Doit correspondre aux dossiers rejetés
- [ ] `DOSSIERS_CLOTURES` - Doit correspondre aux dossiers clôturés
- [ ] `DOSSIERS_CREES_CE_MOIS` - Doit correspondre aux dossiers créés ce mois
- [ ] `DOSSIERS_PAR_PHASE_CREATION` - Doit correspondre aux dossiers en phase création
- [ ] `DOSSIERS_PAR_PHASE_ENQUETE` - Doit correspondre aux dossiers en phase enquête
- [ ] `DOSSIERS_PAR_PHASE_AMIABLE` - Doit correspondre aux dossiers en phase amiable
- [ ] `DOSSIERS_PAR_PHASE_JURIDIQUE` - Doit correspondre aux dossiers en phase juridique
- [ ] `TOTAL_ENQUETES` - Doit correspondre au nombre total d'enquêtes
- [ ] `ENQUETES_COMPLETEES` - Doit correspondre aux enquêtes validées
- [ ] `ACTIONS_AMIABLES` - Doit correspondre au nombre total d'actions amiables
- [ ] `ACTIONS_AMIABLES_COMPLETEES` - Doit correspondre aux actions complétées
- [ ] `DOCUMENTS_HUISSIER_CREES` - Doit correspondre aux documents créés
- [ ] `DOCUMENTS_HUISSIER_COMPLETES` - Doit correspondre aux documents complétés
- [ ] `ACTIONS_HUISSIER_CREES` - Doit correspondre aux actions huissier créées
- [ ] `ACTIONS_HUISSIER_COMPLETES` - Doit correspondre aux actions huissier complétées
- [ ] `AUDIENCES_TOTALES` - Doit correspondre au nombre total d'audiences
- [ ] `AUDIENCES_PROCHAINES` - Doit correspondre aux audiences dans les 7 prochains jours
- [ ] `TACHES_COMPLETEES` - Doit correspondre aux tâches terminées
- [ ] `TACHES_EN_COURS` - Doit correspondre aux tâches en cours
- [ ] `TACHES_EN_RETARD` - Doit correspondre aux tâches en retard
- [ ] `TAUX_REUSSITE_GLOBAL` - Doit être un pourcentage (0-100)
- [ ] `MONTANT_RECOUVRE` - Doit correspondre au montant recouvré
- [ ] `MONTANT_EN_COURS` - Doit correspondre au montant en cours

#### 1.3 Comparer avec les données réelles

**Requêtes de vérification :**
```sql
-- Vérifier totalDossiers
SELECT COUNT(*) as total_dossiers FROM dossier;
-- Comparer avec la valeur dans statistiques pour TOTAL_DOSSIERS

-- Vérifier totalEnquetes
SELECT COUNT(*) as total_enquetes FROM enquette;
-- Comparer avec la valeur dans statistiques pour TOTAL_ENQUETES

-- Vérifier dossiersEnCours
SELECT COUNT(*) as dossiers_en_cours 
FROM dossier 
WHERE dossier_status = 'ENCOURSDETRAITEMENT';
-- Comparer avec la valeur dans statistiques pour DOSSIERS_EN_COURS
```

---

### 🖥️ Étape 2 : Vérification des APIs Backend

#### 2.1 Tester l'endpoint `/api/statistiques/globales`

**Requête :**
```http
GET http://localhost:8089/carthage-creance/api/statistiques/globales
Headers: Authorization: Bearer {token}
```

**Vérifications :**
- [ ] La requête retourne un statut 200 OK
- [ ] La réponse JSON contient toutes les clés suivantes :
  - [ ] `totalDossiers`
  - [ ] `dossiersEnCours`
  - [ ] `dossiersValides`
  - [ ] `dossiersRejetes`
  - [ ] `dossiersClotures`
  - [ ] `dossiersCreesCeMois`
  - [ ] `dossiersPhaseCreation`
  - [ ] `dossiersPhaseEnquete`
  - [ ] `dossiersPhaseAmiable`
  - [ ] `dossiersPhaseJuridique`
  - [ ] `totalEnquetes`
  - [ ] `enquetesCompletees`
  - [ ] `actionsAmiables`
  - [ ] `actionsAmiablesCompletees`
  - [ ] `documentsHuissierCrees`
  - [ ] `documentsHuissierCompletes`
  - [ ] `actionsHuissierCrees`
  - [ ] `actionsHuissierCompletes`
  - [ ] `audiencesTotales`
  - [ ] `audiencesProchaines`
  - [ ] `tachesCompletees`
  - [ ] `tachesEnCours`
  - [ ] `tachesEnRetard`
  - [ ] `tauxReussiteGlobal`
  - [ ] `montantRecouvre`
  - [ ] `montantEnCours`
- [ ] Toutes les valeurs sont des nombres (pas null, pas undefined)
- [ ] Les valeurs correspondent aux données réelles

#### 2.2 Tester l'endpoint `/api/statistiques/recalculer`

**Requête :**
```http
POST http://localhost:8089/carthage-creance/api/statistiques/recalculer
Headers: Authorization: Bearer {token}
```

**Vérifications :**
- [ ] La requête retourne un statut 200 OK
- [ ] Le message de succès est retourné
- [ ] Après le recalcul, les valeurs dans la table `statistiques` sont mises à jour
- [ ] Les nouvelles valeurs correspondent aux données actuelles

#### 2.3 Tester les autres endpoints selon le rôle

**Pour SuperAdmin :**
- [ ] `/api/statistiques/globales` - Fonctionne
- [ ] `/api/statistiques/periode?dateDebut=...&dateFin=...` - Fonctionne
- [ ] `/api/statistiques/dossiers` - Fonctionne
- [ ] `/api/statistiques/actions-amiables` - Fonctionne
- [ ] `/api/statistiques/audiences` - Fonctionne
- [ ] `/api/statistiques/taches` - Fonctionne
- [ ] `/api/statistiques/financieres` - Fonctionne
- [ ] `/api/statistiques/chefs` - Fonctionne
- [ ] `/api/statistiques/agent/{agentId}` - Fonctionne

**Pour Chef :**
- [ ] `/api/statistiques/departement` - Fonctionne
- [ ] `/api/statistiques/mes-agents` - Fonctionne

**Pour Agent :**
- [ ] `/api/statistiques/mes-dossiers` - Fonctionne

---

### 🎨 Étape 3 : Vérification Frontend - Interfaces et Composants

#### 3.1 Dashboard Principal (SuperAdmin)

**Fichier :** `dashboard-admin.component.ts` / `dashboard-admin.component.html`

**Vérifications :**
- [ ] Le composant appelle `statistiqueService.loadGlobalStats()` au `ngOnInit()`
- [ ] Les statistiques sont stockées dans une variable (ex: `stats: StatistiquesGlobales`)
- [ ] Toutes les statistiques suivantes sont affichées :
  - [ ] `totalDossiers` - Affiche le nombre total de dossiers
  - [ ] `dossiersEnCours` - Affiche les dossiers en cours
  - [ ] `dossiersValides` - Affiche les dossiers validés
  - [ ] `dossiersRejetes` - Affiche les dossiers rejetés
  - [ ] `dossiersClotures` - Affiche les dossiers clôturés
  - [ ] `dossiersCreesCeMois` - Affiche les dossiers créés ce mois
  - [ ] `tauxReussiteGlobal` - Affiche le taux de réussite (format: XX.X%)
  - [ ] `montantRecouvre` - Affiche le montant recouvré (format: X,XXX TND)
  - [ ] `montantEnCours` - Affiche le montant en cours (format: X,XXX TND)
- [ ] Les valeurs sont formatées correctement (nombres avec séparateurs, pourcentages, montants)
- [ ] Un indicateur de chargement est affiché pendant le chargement
- [ ] Les erreurs sont gérées et affichées

#### 3.2 Dashboard Enquêtes

**Fichier :** `dashboard-enquetes.component.ts` / `dashboard-enquetes.component.html`

**Vérifications :**
- [ ] Le composant charge les statistiques globales ou appelle un endpoint spécifique
- [ ] Les statistiques suivantes sont affichées :
  - [ ] `totalEnquetes` - Affiche le nombre total d'enquêtes
  - [ ] `enquetesCompletees` - Affiche le nombre d'enquêtes complétées
  - [ ] Taux de complétion calculé : `(enquetesCompletees / totalEnquetes) * 100`
- [ ] Les valeurs sont formatées correctement
- [ ] Un graphique ou tableau affiche la répartition

#### 3.3 Dashboard Actions

**Fichier :** `dashboard-actions.component.ts` / `dashboard-actions.component.html`

**Vérifications :**
- [ ] Les statistiques suivantes sont affichées :
  - [ ] `actionsAmiables` - Affiche le nombre total d'actions amiables
  - [ ] `actionsAmiablesCompletees` - Affiche le nombre d'actions complétées
  - [ ] Taux de complétion calculé
- [ ] Les valeurs sont formatées correctement

#### 3.4 Dashboard Huissier

**Fichier :** `dashboard-huissier.component.ts` / `dashboard-huissier.component.html`

**Vérifications :**
- [ ] Les statistiques suivantes sont affichées :
  - [ ] `documentsHuissierCrees` - Affiche le nombre de documents créés
  - [ ] `documentsHuissierCompletes` - Affiche le nombre de documents complétés
  - [ ] `actionsHuissierCrees` - Affiche le nombre d'actions créées
  - [ ] `actionsHuissierCompletes` - Affiche le nombre d'actions complétées
- [ ] Les valeurs sont formatées correctement

#### 3.5 Dashboard Audiences

**Fichier :** `dashboard-audiences.component.ts` / `dashboard-audiences.component.html`

**Vérifications :**
- [ ] Les statistiques suivantes sont affichées :
  - [ ] `audiencesTotales` - Affiche le nombre total d'audiences
  - [ ] `audiencesProchaines` - Affiche les audiences dans les 7 prochains jours
- [ ] Les valeurs sont formatées correctement
- [ ] Un calendrier ou liste affiche les audiences prochaines

#### 3.6 Dashboard Tâches

**Fichier :** `dashboard-taches.component.ts` / `dashboard-taches.component.html`

**Vérifications :**
- [ ] Les statistiques suivantes sont affichées :
  - [ ] `tachesCompletees` - Affiche le nombre de tâches complétées
  - [ ] `tachesEnCours` - Affiche le nombre de tâches en cours
  - [ ] `tachesEnRetard` - Affiche le nombre de tâches en retard (alerte si > 0)
- [ ] Les valeurs sont formatées correctement
- [ ] Une alerte visuelle est affichée si `tachesEnRetard > 0`

#### 3.7 Dashboard Phases

**Fichier :** `dashboard-phases.component.ts` / `dashboard-phases.component.html`

**Vérifications :**
- [ ] Les statistiques suivantes sont affichées :
  - [ ] `dossiersPhaseCreation` - Affiche les dossiers en phase création
  - [ ] `dossiersPhaseEnquete` - Affiche les dossiers en phase enquête
  - [ ] `dossiersPhaseAmiable` - Affiche les dossiers en phase amiable
  - [ ] `dossiersPhaseJuridique` - Affiche les dossiers en phase juridique
- [ ] Les valeurs sont formatées correctement
- [ ] Un graphique (barres ou camembert) affiche la répartition

#### 3.8 Dashboard Financier

**Fichier :** `dashboard-financier.component.ts` / `dashboard-financier.component.html`

**Vérifications :**
- [ ] Les statistiques suivantes sont affichées :
  - [ ] `montantRecouvre` - Affiche le montant recouvré (format: X,XXX TND)
  - [ ] `montantEnCours` - Affiche le montant en cours (format: X,XXX TND)
- [ ] Les valeurs sont formatées correctement avec devise
- [ ] Un graphique affiche l'évolution

---

### 🔄 Étape 4 : Vérification du Rafraîchissement Automatique

#### 4.1 Vérifier que `refreshAfterAction()` est appelé

**Composants à vérifier :**
- [ ] `create-dossier.component.ts` - Appelle `refreshAfterAction()` après création
- [ ] `edit-dossier.component.ts` - Appelle `refreshAfterAction()` après modification
- [ ] `validate-dossier.component.ts` - Appelle `refreshAfterAction()` après validation
- [ ] `create-enquete.component.ts` - Appelle `refreshAfterAction()` après création
- [ ] `validate-enquete.component.ts` - Appelle `refreshAfterAction()` après validation
- [ ] `create-action.component.ts` - Appelle `refreshAfterAction()` après création
- [ ] `create-document-huissier.component.ts` - Appelle `refreshAfterAction()` après création
- [ ] `create-action-huissier.component.ts` - Appelle `refreshAfterAction()` après création
- [ ] `create-audience.component.ts` - Appelle `refreshAfterAction()` après création
- [ ] `assign-avocat-huissier.component.ts` - Appelle `refreshAfterAction()` après affectation
- [ ] `validate-frais.component.ts` - Appelle `refreshAfterAction()` après validation
- [ ] `generate-facture.component.ts` - Appelle `refreshAfterAction()` après génération

**Exemple de code à vérifier :**
```typescript
onDossierCreated() {
  this.dossierService.createDossier(data).subscribe({
    next: () => {
      // ✅ DOIT être présent
      this.statistiqueService.refreshAfterAction();
      this.showSuccessMessage('Dossier créé avec succès');
    },
    error: (err) => {
      this.showErrorMessage('Erreur: ' + err.message);
    }
  });
}
```

#### 4.2 Tester le rafraîchissement automatique

**Tests à effectuer :**
1. **Créer un dossier :**
   - [ ] Créer un nouveau dossier
   - [ ] Vérifier que `totalDossiers` s'incrémente dans le dashboard
   - [ ] Vérifier que `dossiersCreesCeMois` s'incrémente
   - [ ] Vérifier que l'affichage se met à jour automatiquement (sans recharger la page)

2. **Créer une enquête :**
   - [ ] Créer une nouvelle enquête
   - [ ] Vérifier que `totalEnquetes` s'incrémente
   - [ ] Vérifier que `enquetesCompletees` reste à 0 (si non validée)
   - [ ] Valider l'enquête
   - [ ] Vérifier que `enquetesCompletees` s'incrémente

3. **Créer une action amiable :**
   - [ ] Créer une nouvelle action amiable
   - [ ] Vérifier que `actionsAmiables` s'incrémente
   - [ ] Vérifier que l'affichage se met à jour

---

### 📋 Étape 5 : Checklist Complète par Interface

#### Interface Dashboard Principal (SuperAdmin)

**URL :** `/dashboard` ou `/admin/dashboard`

**Vérifications :**
- [ ] L'interface charge les statistiques au chargement de la page
- [ ] Toutes les cartes de statistiques sont visibles
- [ ] Les valeurs affichées correspondent aux valeurs de l'API
- [ ] Les valeurs sont formatées correctement (nombres, pourcentages, montants)
- [ ] Un bouton "Recalculer les Statistiques" est présent (SuperAdmin uniquement)
- [ ] Le bouton fonctionne et met à jour les statistiques
- [ ] Les erreurs sont gérées et affichées

**Statistiques à afficher :**
- [ ] Total Dossiers
- [ ] Dossiers en Cours
- [ ] Dossiers Validés
- [ ] Dossiers Rejetés
- [ ] Dossiers Clôturés
- [ ] Dossiers Créés ce Mois
- [ ] Taux de Réussite
- [ ] Montant Recouvré
- [ ] Montant en Cours

#### Interface Dashboard Enquêtes

**URL :** `/dashboard/enquetes` ou `/enquetes/dashboard`

**Vérifications :**
- [ ] L'interface charge les statistiques
- [ ] Total Enquêtes est affiché
- [ ] Enquêtes Complétées est affiché
- [ ] Taux de complétion est calculé et affiché
- [ ] Les valeurs se mettent à jour après création/validation d'enquête

#### Interface Dashboard Actions

**URL :** `/dashboard/actions` ou `/actions/dashboard`

**Vérifications :**
- [ ] L'interface charge les statistiques
- [ ] Actions Amiables est affiché
- [ ] Actions Complétées est affiché
- [ ] Taux de complétion est calculé et affiché

#### Interface Dashboard Huissier

**URL :** `/dashboard/huissier` ou `/huissier/dashboard`

**Vérifications :**
- [ ] L'interface charge les statistiques
- [ ] Documents Créés est affiché
- [ ] Documents Complétés est affiché
- [ ] Actions Créées est affiché
- [ ] Actions Complétées est affiché

#### Interface Dashboard Audiences

**URL :** `/dashboard/audiences` ou `/audiences/dashboard`

**Vérifications :**
- [ ] L'interface charge les statistiques
- [ ] Total Audiences est affiché
- [ ] Audiences Prochaines est affiché
- [ ] Liste ou calendrier des audiences prochaines est affiché

#### Interface Dashboard Tâches

**URL :** `/dashboard/taches` ou `/taches/dashboard`

**Vérifications :**
- [ ] L'interface charge les statistiques
- [ ] Tâches Complétées est affiché
- [ ] Tâches en Cours est affiché
- [ ] Tâches en Retard est affiché avec alerte si > 0

#### Interface Dashboard Phases

**URL :** `/dashboard/phases` ou `/phases/dashboard`

**Vérifications :**
- [ ] L'interface charge les statistiques
- [ ] Phase Création est affiché
- [ ] Phase Enquête est affiché
- [ ] Phase Amiable est affiché
- [ ] Phase Juridique est affiché
- [ ] Un graphique affiche la répartition

---

### 🧪 Étape 6 : Tests Fonctionnels Complets

#### Test 1 : Création de Dossier

**Actions :**
1. Créer un nouveau dossier
2. Attendre 1-2 secondes
3. Vérifier le dashboard

**Vérifications :**
- [ ] `totalDossiers` s'incrémente de 1
- [ ] `dossiersCreesCeMois` s'incrémente de 1
- [ ] `dossiersPhaseCreation` s'incrémente de 1
- [ ] Les valeurs dans la base de données sont mises à jour
- [ ] L'affichage frontend se met à jour automatiquement

#### Test 2 : Validation de Dossier

**Actions :**
1. Valider un dossier
2. Attendre 1-2 secondes
3. Vérifier le dashboard

**Vérifications :**
- [ ] `dossiersValides` s'incrémente de 1
- [ ] `dossiersPhaseCreation` diminue de 1
- [ ] Les valeurs se mettent à jour

#### Test 3 : Création d'Enquête

**Actions :**
1. Créer une nouvelle enquête
2. Attendre 1-2 secondes
3. Vérifier le dashboard

**Vérifications :**
- [ ] `totalEnquetes` s'incrémente de 1
- [ ] `enquetesCompletees` reste à 0 (si non validée)
- [ ] `dossiersPhaseEnquete` s'incrémente de 1
- [ ] Les valeurs se mettent à jour

#### Test 4 : Validation d'Enquête

**Actions :**
1. Valider une enquête
2. Attendre 1-2 secondes
3. Vérifier le dashboard

**Vérifications :**
- [ ] `enquetesCompletees` s'incrémente de 1
- [ ] `dossiersPhaseEnquete` diminue de 1
- [ ] Les valeurs se mettent à jour

#### Test 5 : Création d'Action Amiable

**Actions :**
1. Créer une nouvelle action amiable
2. Attendre 1-2 secondes
3. Vérifier le dashboard

**Vérifications :**
- [ ] `actionsAmiables` s'incrémente de 1
- [ ] Les valeurs se mettent à jour

#### Test 6 : Recalcul Manuel

**Actions :**
1. Se connecter en tant que SuperAdmin
2. Cliquer sur "Recalculer les Statistiques"
3. Attendre 2-3 secondes
4. Vérifier le dashboard

**Vérifications :**
- [ ] Message de succès affiché
- [ ] Les statistiques se rechargent automatiquement
- [ ] Les valeurs dans la base de données sont mises à jour
- [ ] Les valeurs affichées correspondent aux données réelles

---

### 📊 Étape 7 : Tableau de Correspondance Complet

| Clé API | Label Interface | Interface | Format | Vérifié |
|---------|----------------|-----------|--------|---------|
| `totalDossiers` | "Total Dossiers" | Dashboard Principal | Nombre | [ ] |
| `dossiersEnCours` | "Dossiers en Cours" | Dashboard Principal | Nombre | [ ] |
| `dossiersValides` | "Dossiers Validés" | Dashboard Principal | Nombre | [ ] |
| `dossiersRejetes` | "Dossiers Rejetés" | Dashboard Principal | Nombre | [ ] |
| `dossiersClotures` | "Dossiers Clôturés" | Dashboard Principal | Nombre | [ ] |
| `dossiersCreesCeMois` | "Dossiers Créés ce Mois" | Dashboard Principal | Nombre | [ ] |
| `dossiersPhaseCreation` | "Phase Création" | Dashboard Phases | Nombre | [ ] |
| `dossiersPhaseEnquete` | "Phase Enquête" | Dashboard Phases | Nombre | [ ] |
| `dossiersPhaseAmiable` | "Phase Amiable" | Dashboard Phases | Nombre | [ ] |
| `dossiersPhaseJuridique` | "Phase Juridique" | Dashboard Phases | Nombre | [ ] |
| `totalEnquetes` | "Total Enquêtes" | Dashboard Enquêtes | Nombre | [ ] |
| `enquetesCompletees` | "Enquêtes Complétées" | Dashboard Enquêtes | Nombre | [ ] |
| `actionsAmiables` | "Actions Amiables" | Dashboard Actions | Nombre | [ ] |
| `actionsAmiablesCompletees` | "Actions Complétées" | Dashboard Actions | Nombre | [ ] |
| `documentsHuissierCrees` | "Documents Créés" | Dashboard Huissier | Nombre | [ ] |
| `documentsHuissierCompletes` | "Documents Complétés" | Dashboard Huissier | Nombre | [ ] |
| `actionsHuissierCrees` | "Actions Créées" | Dashboard Huissier | Nombre | [ ] |
| `actionsHuissierCompletes` | "Actions Complétées" | Dashboard Huissier | Nombre | [ ] |
| `audiencesTotales` | "Total Audiences" | Dashboard Audiences | Nombre | [ ] |
| `audiencesProchaines` | "Audiences Prochaines" | Dashboard Audiences | Nombre | [ ] |
| `tachesCompletees` | "Tâches Complétées" | Dashboard Tâches | Nombre | [ ] |
| `tachesEnCours` | "Tâches en Cours" | Dashboard Tâches | Nombre | [ ] |
| `tachesEnRetard` | "Tâches en Retard" | Dashboard Tâches | Nombre (alerte) | [ ] |
| `tauxReussiteGlobal` | "Taux de Réussite" | Dashboard Principal | Pourcentage | [ ] |
| `montantRecouvre` | "Montant Recouvré" | Dashboard Financier | Montant (TND) | [ ] |
| `montantEnCours` | "Montant en Cours" | Dashboard Financier | Montant (TND) | [ ] |

**Instructions :**
1. Cocher chaque case après avoir vérifié que la statistique est bien affichée dans l'interface correspondante
2. Vérifier que le format est correct (nombre, pourcentage, montant)
3. Vérifier que la valeur correspond à la valeur de l'API

---

### 🔧 Étape 8 : Résolution de Problèmes

#### Problème : Les statistiques affichent 0

**Solutions :**
1. **Vérifier la base de données :**
   ```sql
   SELECT * FROM statistiques WHERE periode = DATE_FORMAT(NOW(), '%Y-%m');
   ```
   - Si la table est vide ou contient des 0, appeler l'API `/api/statistiques/recalculer`

2. **Vérifier l'API :**
   - Tester l'endpoint `/api/statistiques/globales` directement
   - Vérifier que les valeurs retournées ne sont pas 0

3. **Vérifier le frontend :**
   - Vérifier que le service `statistiqueService` est injecté
   - Vérifier que `loadStatistiques()` est appelé
   - Vérifier la console du navigateur pour les erreurs

#### Problème : Les statistiques ne se mettent pas à jour

**Solutions :**
1. **Vérifier que `refreshAfterAction()` est appelé :**
   - Vérifier dans chaque composant de création/modification
   - Ajouter des logs pour vérifier l'appel

2. **Vérifier le timing :**
   - Le rafraîchissement attend 1 seconde avant de recharger
   - Augmenter le délai si nécessaire

3. **Vérifier les erreurs :**
   - Vérifier la console du navigateur
   - Vérifier les logs du backend

#### Problème : Les valeurs ne correspondent pas

**Solutions :**
1. **Recalculer les statistiques :**
   - Appeler `/api/statistiques/recalculer`
   - Vérifier que les valeurs sont mises à jour

2. **Vérifier les données réelles :**
   - Comparer avec les requêtes SQL de vérification
   - Vérifier que les filtres sont corrects

---

## 📝 Résumé de Vérification

### ✅ Checklist Finale

**Base de Données :**
- [ ] Table `statistiques` contient des valeurs (pas de 0 partout)
- [ ] Toutes les statistiques sont présentes
- [ ] Les valeurs correspondent aux données réelles

**APIs Backend :**
- [ ] Tous les endpoints fonctionnent
- [ ] Les réponses JSON contiennent toutes les clés
- [ ] Les valeurs sont correctes

**Frontend :**
- [ ] Toutes les interfaces affichent les statistiques
- [ ] Les valeurs sont formatées correctement
- [ ] Le rafraîchissement automatique fonctionne
- [ ] Les erreurs sont gérées

**Tests Fonctionnels :**
- [ ] Création de dossier met à jour les statistiques
- [ ] Création d'enquête met à jour les statistiques
- [ ] Création d'action met à jour les statistiques
- [ ] Recalcul manuel fonctionne

---

## 🎯 Objectif Final

Après avoir complété toutes les vérifications :
- ✅ Toutes les statistiques sont visibles dans la base de données
- ✅ Toutes les statistiques sont visibles dans le frontend
- ✅ Toutes les interfaces consomment correctement les APIs
- ✅ Le rafraîchissement automatique fonctionne
- ✅ Les valeurs correspondent aux données réelles

