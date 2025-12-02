# 🎨 Prompts Frontend - Intégration Complète IA Prédiction

## 📋 Vue d'Ensemble

Ce document contient **TOUS** les prompts détaillés pour intégrer la prédiction IA dans le frontend Angular pour les rôles **Chef**, **Agent** et **SuperAdmin**.

---

## 🎯 Architecture Frontend

### Structure des Services et Composants

```
src/
├── app/
│   ├── services/
│   │   ├── ia-prediction.service.ts          ← NOUVEAU
│   │   └── dossier.service.ts                ← MODIFIER
│   ├── components/
│   │   ├── dossier/
│   │   │   ├── dossier-list/
│   │   │   ├── dossier-detail/
│   │   │   └── dossier-actions/             ← MODIFIER
│   │   ├── dashboard/
│   │   │   ├── chef-dashboard/              ← MODIFIER
│   │   │   ├── agent-dashboard/              ← MODIFIER
│   │   │   └── superadmin-dashboard/        ← MODIFIER
│   │   └── shared/
│   │       └── ia-prediction-badge/          ← NOUVEAU
│   └── models/
│       └── ia-prediction-result.model.ts     ← NOUVEAU
```

---

## 📦 Prompt 1 : Créer le Modèle TypeScript pour la Prédiction IA

### Fichier : `src/app/models/ia-prediction-result.model.ts`

```typescript
/**
 * Modèle pour le résultat de la prédiction IA
 */
export interface IaPredictionResult {
  etatFinal: 'RECOVERED_TOTAL' | 'RECOVERED_PARTIAL' | 'NOT_RECOVERED';
  riskScore: number;  // 0-100
  riskLevel: 'Faible' | 'Moyen' | 'Élevé';
}

/**
 * Enum pour l'état final prédit
 */
export enum EtatPrediction {
  RECOVERED_TOTAL = 'RECOVERED_TOTAL',
  RECOVERED_PARTIAL = 'RECOVERED_PARTIAL',
  NOT_RECOVERED = 'NOT_RECOVERED'
}

/**
 * Enum pour le niveau de risque
 */
export enum RiskLevel {
  FAIBLE = 'Faible',
  MOYEN = 'Moyen',
  ELEVE = 'Élevé'
}

/**
 * Helper pour obtenir la couleur du badge selon le niveau de risque
 */
export function getRiskLevelColor(riskLevel: string): string {
  switch (riskLevel) {
    case RiskLevel.FAIBLE:
      return 'success';  // Vert
    case RiskLevel.MOYEN:
      return 'warning';  // Orange
    case RiskLevel.ELEVE:
      return 'danger';  // Rouge
    default:
      return 'secondary';
  }
}

/**
 * Helper pour obtenir la couleur du badge selon l'état final
 */
export function getEtatPredictionColor(etatFinal: string): string {
  switch (etatFinal) {
    case EtatPrediction.RECOVERED_TOTAL:
      return 'success';  // Vert
    case EtatPrediction.RECOVERED_PARTIAL:
      return 'warning';  // Orange
    case EtatPrediction.NOT_RECOVERED:
      return 'danger';  // Rouge
    default:
      return 'secondary';
  }
}

/**
 * Helper pour obtenir l'icône selon le niveau de risque
 */
export function getRiskLevelIcon(riskLevel: string): string {
  switch (riskLevel) {
    case RiskLevel.FAIBLE:
      return 'check_circle';
    case RiskLevel.MOYEN:
      return 'warning';
    case RiskLevel.ELEVE:
      return 'error';
    default:
      return 'help';
  }
}
```

---

## 🔧 Prompt 2 : Créer le Service Angular pour la Prédiction IA

### Fichier : `src/app/services/ia-prediction.service.ts`

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { IaPredictionResult } from '../models/ia-prediction-result.model';

@Injectable({
  providedIn: 'root'
})
export class IaPredictionService {
  private apiUrl = 'http://localhost:8089/carthage-creance/api/dossiers';

  constructor(private http: HttpClient) {}

  /**
   * Obtenir la prédiction IA pour un dossier (sans modifier le dossier)
   * @param dossierId ID du dossier
   * @returns Observable<IaPredictionResult>
   */
  getPrediction(dossierId: number): Observable<IaPredictionResult> {
    const headers = this.getHeaders();
    return this.http.post<IaPredictionResult>(
      `${this.apiUrl}/${dossierId}/predict-ia`,
      {},
      { headers }
    ).pipe(
      map(response => ({
        etatFinal: response.etatFinal,
        riskScore: Math.round(response.riskScore * 10) / 10,  // Arrondir à 1 décimale
        riskLevel: response.riskLevel
      })),
      catchError(error => {
        console.error('Erreur lors de la prédiction IA:', error);
        return throwError(() => new Error('Erreur lors de la prédiction IA'));
      })
    );
  }

  /**
   * Déclencher la prédiction IA lors de l'enregistrement d'une action amiable
   * La prédiction est automatiquement déclenchée par le backend
   * Cette méthode peut être utilisée pour vérifier le résultat après l'action
   */
  triggerPredictionOnAction(dossierId: number): Observable<IaPredictionResult> {
    // La prédiction est déjà déclenchée par le backend lors de l'enregistrement de l'action
    // On peut simplement récupérer le résultat après
    return this.getPrediction(dossierId);
  }

  /**
   * Obtenir les headers avec le token d'authentification
   */
  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }
}
```

---

## 🎨 Prompt 3 : Créer le Composant Badge de Prédiction IA (Shared)

### Fichier : `src/app/components/shared/ia-prediction-badge/ia-prediction-badge.component.ts`

```typescript
import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IaPredictionResult, getRiskLevelColor, getEtatPredictionColor, getRiskLevelIcon } from '../../../models/ia-prediction-result.model';

@Component({
  selector: 'app-ia-prediction-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="ia-prediction-badge" *ngIf="prediction">
      <!-- Badge du niveau de risque -->
      <span class="badge badge-{{getRiskColor()}}" [title]="'Score de risque: ' + prediction.riskScore + '%'">
        <i class="material-icons">{{getRiskIcon()}}</i>
        {{prediction.riskLevel}} ({{prediction.riskScore}}%)
      </span>
      
      <!-- Badge de l'état final prédit -->
      <span class="badge badge-{{getEtatColor()}}" [title]="'État final prédit: ' + prediction.etatFinal">
        {{getEtatLabel()}}
      </span>
    </div>
    
    <!-- Indicateur de chargement -->
    <div class="ia-prediction-loading" *ngIf="loading">
      <span class="spinner-border spinner-border-sm" role="status"></span>
      <span class="ml-2">Calcul de la prédiction...</span>
    </div>
    
    <!-- Message d'erreur -->
    <div class="ia-prediction-error alert alert-warning" *ngIf="error">
      <i class="material-icons">warning</i>
      {{error}}
    </div>
  `,
  styles: [`
    .ia-prediction-badge {
      display: flex;
      gap: 8px;
      align-items: center;
      flex-wrap: wrap;
    }
    
    .ia-prediction-badge .badge {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      padding: 4px 8px;
      font-size: 0.875rem;
    }
    
    .ia-prediction-badge .badge i {
      font-size: 16px;
    }
    
    .ia-prediction-loading {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #666;
      font-size: 0.875rem;
    }
    
    .ia-prediction-error {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 0.875rem;
      padding: 8px;
      margin: 0;
    }
  `]
})
export class IaPredictionBadgeComponent implements OnInit {
  @Input() prediction: IaPredictionResult | null = null;
  @Input() loading: boolean = false;
  @Input() error: string | null = null;

  ngOnInit(): void {}

  getRiskColor(): string {
    return this.prediction ? getRiskLevelColor(this.prediction.riskLevel) : 'secondary';
  }

  getEtatColor(): string {
    return this.prediction ? getEtatPredictionColor(this.prediction.etatFinal) : 'secondary';
  }

  getRiskIcon(): string {
    return this.prediction ? getRiskLevelIcon(this.prediction.riskLevel) : 'help';
  }

  getEtatLabel(): string {
    if (!this.prediction) return '';
    
    const labels: { [key: string]: string } = {
      'RECOVERED_TOTAL': 'Récupération Totale',
      'RECOVERED_PARTIAL': 'Récupération Partielle',
      'NOT_RECOVERED': 'Non Récupéré'
    };
    
    return labels[this.prediction.etatFinal] || this.prediction.etatFinal;
  }
}
```

---

## 📋 Prompt 4 : Modifier le Service Dossier pour Inclure la Prédiction IA

### Fichier : `src/app/services/dossier.service.ts`

**Ajouter les méthodes suivantes** :

```typescript
import { IaPredictionService } from './ia-prediction.service';
import { IaPredictionResult } from '../models/ia-prediction-result.model';

// Dans la classe DossierService, ajouter :

/**
 * Interface pour le Dossier avec prédiction IA
 */
export interface DossierWithPrediction extends Dossier {
  etatPrediction?: 'RECOVERED_TOTAL' | 'RECOVERED_PARTIAL' | 'NOT_RECOVERED';
  riskScore?: number;
  riskLevel?: 'Faible' | 'Moyen' | 'Élevé';
}

/**
 * Obtenir la prédiction IA pour un dossier
 */
getPredictionIa(dossierId: number): Observable<IaPredictionResult> {
  return this.iaPredictionService.getPrediction(dossierId);
}

/**
 * Obtenir un dossier avec sa prédiction IA
 */
getDossierWithPrediction(dossierId: number): Observable<DossierWithPrediction> {
  return forkJoin({
    dossier: this.getDossierById(dossierId),
    prediction: this.iaPredictionService.getPrediction(dossierId).pipe(
      catchError(() => of(null))  // Ne pas bloquer si la prédiction échoue
    )
  }).pipe(
    map(({ dossier, prediction }) => ({
      ...dossier,
      etatPrediction: prediction?.etatFinal,
      riskScore: prediction?.riskScore,
      riskLevel: prediction?.riskLevel
    }))
  );
}
```

---

## 🎯 Prompt 5 : Intégration dans la Liste des Dossiers (Tous les Rôles)

### Fichier : `src/app/components/dossier/dossier-list/dossier-list.component.ts`

**Modifications à apporter** :

```typescript
import { IaPredictionBadgeComponent } from '../../shared/ia-prediction-badge/ia-prediction-badge.component';
import { IaPredictionResult } from '../../../models/ia-prediction-result.model';
import { IaPredictionService } from '../../../services/ia-prediction.service';

// Dans la classe DossierListComponent :

export class DossierListComponent implements OnInit {
  dossiers: Dossier[] = [];
  predictions: Map<number, IaPredictionResult> = new Map();
  loadingPredictions: Set<number> = new Set();

  constructor(
    private dossierService: DossierService,
    private iaPredictionService: IaPredictionService
  ) {}

  ngOnInit(): void {
    this.loadDossiers();
  }

  loadDossiers(): void {
    this.dossierService.getAllDossiers().subscribe({
      next: (dossiers) => {
        this.dossiers = dossiers;
        // Charger les prédictions pour les dossiers qui n'en ont pas encore
        this.loadPredictionsForDossiers();
      },
      error: (error) => console.error('Erreur lors du chargement des dossiers:', error)
    });
  }

  /**
   * Charger les prédictions IA pour les dossiers qui n'ont pas encore de prédiction
   */
  loadPredictionsForDossiers(): void {
    this.dossiers.forEach(dossier => {
      // Ne charger que si le dossier n'a pas déjà de prédiction
      if (!dossier.etatPrediction && !this.predictions.has(dossier.id)) {
        this.loadPrediction(dossier.id);
      }
    });
  }

  /**
   * Charger la prédiction IA pour un dossier spécifique
   */
  loadPrediction(dossierId: number): void {
    if (this.loadingPredictions.has(dossierId)) return;
    
    this.loadingPredictions.add(dossierId);
    this.iaPredictionService.getPrediction(dossierId).subscribe({
      next: (prediction) => {
        this.predictions.set(dossierId, prediction);
        this.loadingPredictions.delete(dossierId);
      },
      error: (error) => {
        console.error(`Erreur lors du chargement de la prédiction pour le dossier ${dossierId}:`, error);
        this.loadingPredictions.delete(dossierId);
      }
    });
  }

  /**
   * Obtenir la prédiction pour un dossier
   */
  getPrediction(dossierId: number): IaPredictionResult | null {
    return this.predictions.get(dossierId) || null;
  }

  /**
   * Vérifier si une prédiction est en cours de chargement
   */
  isPredictionLoading(dossierId: number): boolean {
    return this.loadingPredictions.has(dossierId);
  }
}
```

**Template HTML** (`dossier-list.component.html`) :

```html
<table class="table table-striped">
  <thead>
    <tr>
      <th>Numéro</th>
      <th>Client</th>
      <th>Montant Créance</th>
      <th>Montant Recouvré</th>
      <th>Prédiction IA</th>  <!-- NOUVEAU -->
      <th>Statut</th>
      <th>Actions</th>
    </tr>
  </thead>
  <tbody>
    <tr *ngFor="let dossier of dossiers">
      <td>{{dossier.numeroDossier}}</td>
      <td>{{dossier.nomClient}}</td>
      <td>{{dossier.montantCreance | number:'1.2-2'}} TND</td>
      <td>{{dossier.montantRecouvre | number:'1.2-2'}} TND</td>
      
      <!-- Badge de prédiction IA -->
      <td>
        <app-ia-prediction-badge
          [prediction]="getPrediction(dossier.id)"
          [loading]="isPredictionLoading(dossier.id)"
        ></app-ia-prediction-badge>
      </td>
      
      <td>
        <span class="badge badge-{{getStatusColor(dossier.statut)}}">
          {{dossier.statut}}
        </span>
      </td>
      <td>
        <button class="btn btn-sm btn-primary" (click)="viewDossier(dossier.id)">
          Voir Détails
        </button>
      </td>
    </tr>
  </tbody>
</table>
```

---

## 🔍 Prompt 6 : Intégration dans les Détails du Dossier (Tous les Rôles)

### Fichier : `src/app/components/dossier/dossier-detail/dossier-detail.component.ts`

**Modifications à apporter** :

```typescript
import { IaPredictionBadgeComponent } from '../../shared/ia-prediction-badge/ia-prediction-badge.component';
import { IaPredictionResult } from '../../../models/ia-prediction-result.model';
import { IaPredictionService } from '../../../services/ia-prediction.service';

export class DossierDetailComponent implements OnInit {
  dossier: Dossier | null = null;
  prediction: IaPredictionResult | null = null;
  loadingPrediction: boolean = false;
  predictionError: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private dossierService: DossierService,
    private iaPredictionService: IaPredictionService
  ) {}

  ngOnInit(): void {
    const dossierId = this.route.snapshot.params['id'];
    this.loadDossier(dossierId);
  }

  loadDossier(dossierId: number): void {
    this.dossierService.getDossierById(dossierId).subscribe({
      next: (dossier) => {
        this.dossier = dossier;
        
        // Si le dossier a déjà une prédiction, l'utiliser
        if (dossier.etatPrediction && dossier.riskScore !== undefined) {
          this.prediction = {
            etatFinal: dossier.etatPrediction,
            riskScore: dossier.riskScore,
            riskLevel: dossier.riskLevel || 'Moyen'
          };
        } else {
          // Sinon, charger la prédiction
          this.loadPrediction(dossierId);
        }
      },
      error: (error) => console.error('Erreur lors du chargement du dossier:', error)
    });
  }

  /**
   * Charger la prédiction IA pour ce dossier
   */
  loadPrediction(dossierId: number): void {
    this.loadingPrediction = true;
    this.predictionError = null;
    
    this.iaPredictionService.getPrediction(dossierId).subscribe({
      next: (prediction) => {
        this.prediction = prediction;
        this.loadingPrediction = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement de la prédiction:', error);
        this.predictionError = 'Impossible de charger la prédiction IA';
        this.loadingPrediction = false;
      }
    });
  }

  /**
   * Rafraîchir la prédiction IA
   */
  refreshPrediction(): void {
    if (this.dossier) {
      this.loadPrediction(this.dossier.id);
    }
  }
}
```

**Template HTML** (`dossier-detail.component.html`) :

```html
<div class="dossier-detail" *ngIf="dossier">
  <!-- Informations du dossier -->
  <div class="card mb-3">
    <div class="card-header">
      <h4>Détails du Dossier {{dossier.numeroDossier}}</h4>
    </div>
    <div class="card-body">
      <!-- ... autres informations du dossier ... -->
      
      <!-- Section Prédiction IA -->
      <div class="prediction-section mt-4">
        <h5>
          <i class="material-icons">psychology</i>
          Prédiction IA
          <button class="btn btn-sm btn-outline-secondary ml-2" (click)="refreshPrediction()" [disabled]="loadingPrediction">
            <i class="material-icons">refresh</i>
            Actualiser
          </button>
        </h5>
        
        <app-ia-prediction-badge
          [prediction]="prediction"
          [loading]="loadingPrediction"
          [error]="predictionError"
        ></app-ia-prediction-badge>
        
        <!-- Détails supplémentaires de la prédiction -->
        <div class="prediction-details mt-3" *ngIf="prediction">
          <div class="row">
            <div class="col-md-4">
              <strong>État Final Prédit :</strong>
              <span class="badge badge-{{getEtatPredictionColor(prediction.etatFinal)}}">
                {{getEtatPredictionLabel(prediction.etatFinal)}}
              </span>
            </div>
            <div class="col-md-4">
              <strong>Score de Risque :</strong>
              <span>{{prediction.riskScore}}%</span>
            </div>
            <div class="col-md-4">
              <strong>Niveau de Risque :</strong>
              <span class="badge badge-{{getRiskLevelColor(prediction.riskLevel)}}">
                {{prediction.riskLevel}}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
```

---

## 📊 Prompt 7 : Intégration dans le Dashboard Chef

### Fichier : `src/app/components/dashboard/chef-dashboard/chef-dashboard.component.ts`

**Modifications à apporter** :

```typescript
import { IaPredictionService } from '../../../services/ia-prediction.service';
import { IaPredictionResult } from '../../../models/ia-prediction-result.model';

export class ChefDashboardComponent implements OnInit {
  // Statistiques de prédiction IA
  predictionStats = {
    totalDossiers: 0,
    dossiersFaibleRisque: 0,
    dossiersMoyenRisque: 0,
    dossiersEleveRisque: 0,
    dossiersRecoveredTotal: 0,
    dossiersRecoveredPartial: 0,
    dossiersNotRecovered: 0
  };

  constructor(
    private dossierService: DossierService,
    private iaPredictionService: IaPredictionService
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.dossierService.getAllDossiers().subscribe({
      next: (dossiers) => {
        this.calculatePredictionStats(dossiers);
      },
      error: (error) => console.error('Erreur:', error)
    });
  }

  /**
   * Calculer les statistiques de prédiction IA
   */
  calculatePredictionStats(dossiers: Dossier[]): void {
    this.predictionStats.totalDossiers = dossiers.length;
    
    dossiers.forEach(dossier => {
      if (dossier.riskLevel) {
        switch (dossier.riskLevel) {
          case 'Faible':
            this.predictionStats.dossiersFaibleRisque++;
            break;
          case 'Moyen':
            this.predictionStats.dossiersMoyenRisque++;
            break;
          case 'Élevé':
            this.predictionStats.dossiersEleveRisque++;
            break;
        }
      }
      
      if (dossier.etatPrediction) {
        switch (dossier.etatPrediction) {
          case 'RECOVERED_TOTAL':
            this.predictionStats.dossiersRecoveredTotal++;
            break;
          case 'RECOVERED_PARTIAL':
            this.predictionStats.dossiersRecoveredPartial++;
            break;
          case 'NOT_RECOVERED':
            this.predictionStats.dossiersNotRecovered++;
            break;
        }
      }
    });
  }
}
```

**Template HTML** (`chef-dashboard.component.html`) :

```html
<div class="dashboard">
  <h2>Dashboard Chef</h2>
  
  <!-- Statistiques de Prédiction IA -->
  <div class="row mb-4">
    <div class="col-md-12">
      <div class="card">
        <div class="card-header">
          <h5>
            <i class="material-icons">psychology</i>
            Statistiques de Prédiction IA
          </h5>
        </div>
        <div class="card-body">
          <div class="row">
            <!-- Niveaux de Risque -->
            <div class="col-md-4">
              <h6>Niveaux de Risque</h6>
              <div class="stat-item">
                <span class="badge badge-success">Faible</span>
                <strong>{{predictionStats.dossiersFaibleRisque}}</strong>
              </div>
              <div class="stat-item">
                <span class="badge badge-warning">Moyen</span>
                <strong>{{predictionStats.dossiersMoyenRisque}}</strong>
              </div>
              <div class="stat-item">
                <span class="badge badge-danger">Élevé</span>
                <strong>{{predictionStats.dossiersEleveRisque}}</strong>
              </div>
            </div>
            
            <!-- États Finaux Prédits -->
            <div class="col-md-4">
              <h6>États Finaux Prédits</h6>
              <div class="stat-item">
                <span class="badge badge-success">Récupération Totale</span>
                <strong>{{predictionStats.dossiersRecoveredTotal}}</strong>
              </div>
              <div class="stat-item">
                <span class="badge badge-warning">Récupération Partielle</span>
                <strong>{{predictionStats.dossiersRecoveredPartial}}</strong>
              </div>
              <div class="stat-item">
                <span class="badge badge-danger">Non Récupéré</span>
                <strong>{{predictionStats.dossiersNotRecovered}}</strong>
              </div>
            </div>
            
            <!-- Graphique (optionnel) -->
            <div class="col-md-4">
              <canvas id="predictionChart"></canvas>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  
  <!-- Liste des dossiers avec prédictions -->
  <app-dossier-list></app-dossier-list>
</div>
```

---

## 👤 Prompt 8 : Intégration dans le Dashboard Agent

### Fichier : `src/app/components/dashboard/agent-dashboard/agent-dashboard.component.ts`

**Modifications similaires au Dashboard Chef, mais avec focus sur les dossiers de l'agent** :

```typescript
export class AgentDashboardComponent implements OnInit {
  mesDossiers: Dossier[] = [];
  predictions: Map<number, IaPredictionResult> = new Map();

  constructor(
    private dossierService: DossierService,
    private iaPredictionService: IaPredictionService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadMesDossiers();
  }

  loadMesDossiers(): void {
    const agentId = this.authService.getCurrentUser()?.id;
    if (agentId) {
      this.dossierService.getDossiersByAgent(agentId).subscribe({
        next: (dossiers) => {
          this.mesDossiers = dossiers;
          this.loadPredictions();
        }
      });
    }
  }

  loadPredictions(): void {
    this.mesDossiers.forEach(dossier => {
      if (!dossier.etatPrediction) {
        this.iaPredictionService.getPrediction(dossier.id).subscribe({
          next: (prediction) => {
            this.predictions.set(dossier.id, prediction);
          }
        });
      }
    });
  }
}
```

---

## 👑 Prompt 9 : Intégration dans le Dashboard SuperAdmin

### Fichier : `src/app/components/dashboard/superadmin-dashboard/superadmin-dashboard.component.ts`

**Modifications similaires au Dashboard Chef, mais avec vue globale** :

```typescript
export class SuperAdminDashboardComponent implements OnInit {
  // Statistiques globales de prédiction IA
  globalPredictionStats = {
    totalDossiers: 0,
    dossiersAvecPrediction: 0,
    dossiersSansPrediction: 0,
    moyenneRiskScore: 0,
    distributionRisque: {
      faible: 0,
      moyen: 0,
      eleve: 0
    }
  };

  constructor(
    private dossierService: DossierService,
    private iaPredictionService: IaPredictionService
  ) {}

  ngOnInit(): void {
    this.loadGlobalStats();
  }

  loadGlobalStats(): void {
    this.dossierService.getAllDossiers().subscribe({
      next: (dossiers) => {
        this.calculateGlobalStats(dossiers);
      }
    });
  }

  calculateGlobalStats(dossiers: Dossier[]): void {
    this.globalPredictionStats.totalDossiers = dossiers.length;
    
    let totalRiskScore = 0;
    let countWithScore = 0;
    
    dossiers.forEach(dossier => {
      if (dossier.etatPrediction) {
        this.globalPredictionStats.dossiersAvecPrediction++;
        
        if (dossier.riskScore !== undefined) {
          totalRiskScore += dossier.riskScore;
          countWithScore++;
        }
        
        if (dossier.riskLevel) {
          switch (dossier.riskLevel) {
            case 'Faible':
              this.globalPredictionStats.distributionRisque.faible++;
              break;
            case 'Moyen':
              this.globalPredictionStats.distributionRisque.moyen++;
              break;
            case 'Élevé':
              this.globalPredictionStats.distributionRisque.eleve++;
              break;
          }
        }
      } else {
        this.globalPredictionStats.dossiersSansPrediction++;
      }
    });
    
    if (countWithScore > 0) {
      this.globalPredictionStats.moyenneRiskScore = totalRiskScore / countWithScore;
    }
  }
}
```

---

## 🎯 Prompt 10 : Intégration dans les Actions du Dossier

### Fichier : `src/app/components/dossier/dossier-actions/dossier-actions.component.ts`

**Modifications pour déclencher la prédiction après une action** :

```typescript
export class DossierActionsComponent implements OnInit {
  dossierId: number;
  prediction: IaPredictionResult | null = null;

  constructor(
    private route: ActivatedRoute,
    private dossierService: DossierService,
    private iaPredictionService: IaPredictionService
  ) {}

  /**
   * Enregistrer une action amiable (déclenche automatiquement la prédiction IA)
   */
  enregistrerActionAmiable(action: ActionAmiableDTO): void {
    this.dossierService.enregistrerActionAmiable(this.dossierId, action).subscribe({
      next: (dossier) => {
        // La prédiction est automatiquement mise à jour par le backend
        // Récupérer le dossier mis à jour pour obtenir la nouvelle prédiction
        this.loadDossier();
      },
      error: (error) => console.error('Erreur:', error)
    });
  }

  /**
   * Charger le dossier avec sa prédiction
   */
  loadDossier(): void {
    this.dossierService.getDossierById(this.dossierId).subscribe({
      next: (dossier) => {
        if (dossier.etatPrediction && dossier.riskScore !== undefined) {
          this.prediction = {
            etatFinal: dossier.etatPrediction,
            riskScore: dossier.riskScore,
            riskLevel: dossier.riskLevel || 'Moyen'
          };
        }
      }
    });
  }
}
```

---

## 📝 Résumé des Prompts

### Fichiers à Créer :
1. ✅ `src/app/models/ia-prediction-result.model.ts`
2. ✅ `src/app/services/ia-prediction.service.ts`
3. ✅ `src/app/components/shared/ia-prediction-badge/ia-prediction-badge.component.ts`

### Fichiers à Modifier :
1. ✅ `src/app/services/dossier.service.ts` - Ajouter méthodes de prédiction
2. ✅ `src/app/components/dossier/dossier-list/dossier-list.component.ts` - Afficher badges
3. ✅ `src/app/components/dossier/dossier-detail/dossier-detail.component.ts` - Section prédiction
4. ✅ `src/app/components/dashboard/chef-dashboard/chef-dashboard.component.ts` - Statistiques
5. ✅ `src/app/components/dashboard/agent-dashboard/agent-dashboard.component.ts` - Mes dossiers
6. ✅ `src/app/components/dashboard/superadmin-dashboard/superadmin-dashboard.component.ts` - Vue globale
7. ✅ `src/app/components/dossier/dossier-actions/dossier-actions.component.ts` - Déclencher prédiction

---

## ✅ Checklist d'Implémentation

- [ ] Créer le modèle `IaPredictionResult`
- [ ] Créer le service `IaPredictionService`
- [ ] Créer le composant badge `IaPredictionBadgeComponent`
- [ ] Modifier `DossierService` pour inclure les méthodes de prédiction
- [ ] Intégrer dans `DossierListComponent`
- [ ] Intégrer dans `DossierDetailComponent`
- [ ] Intégrer dans `ChefDashboardComponent`
- [ ] Intégrer dans `AgentDashboardComponent`
- [ ] Intégrer dans `SuperAdminDashboardComponent`
- [ ] Intégrer dans `DossierActionsComponent`
- [ ] Tester avec Postman
- [ ] Vérifier les permissions selon les rôles

---

## 🎨 Styles CSS Supplémentaires (Optionnel)

### Fichier : `src/styles.css` ou `src/app/components/shared/ia-prediction-badge/ia-prediction-badge.component.css`

```css
/* Styles pour les badges de prédiction IA */
.ia-prediction-badge {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.ia-prediction-badge .badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 500;
}

.ia-prediction-badge .badge i {
  font-size: 16px;
  vertical-align: middle;
}

/* Couleurs pour les niveaux de risque */
.badge-success {
  background-color: #28a745;
  color: white;
}

.badge-warning {
  background-color: #ffc107;
  color: #212529;
}

.badge-danger {
  background-color: #dc3545;
  color: white;
}

/* Animation pour le chargement */
.ia-prediction-loading {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 0.875rem;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}
```

---

## 🔐 Gestion des Permissions (Optionnel)

### Fichier : `src/app/guards/ia-prediction.guard.ts`

```typescript
import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class IaPredictionGuard implements CanActivate {
  constructor(private authService: AuthService) {}

  canActivate(route: ActivatedRouteSnapshot): boolean {
    const user = this.authService.getCurrentUser();
    const requiredRole = route.data['role'];
    
    // Tous les rôles peuvent voir les prédictions IA
    return user && ['CHEF', 'AGENT', 'SUPERADMIN'].includes(user.role);
  }
}
```

---

## ✨ Conclusion

Ces prompts couvrent l'intégration complète de la prédiction IA dans le frontend pour tous les rôles. L'implémentation est modulaire et réutilisable, avec un composant badge partagé et des services dédiés.

**Date de création** : 2025-12-02  
**Statut** : ✅ Prêt pour implémentation

