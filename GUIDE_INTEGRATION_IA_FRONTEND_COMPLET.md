# 🎨 Guide Complet d'Intégration IA - Frontend Angular

## 📋 Vue d'Ensemble

Ce document contient **TOUS** les prompts et exemples de code nécessaires pour intégrer complètement le modèle IA de prédiction dans le frontend Angular pour tous les rôles (Chef, Agent, SuperAdmin).

---

## 🎯 Table des Matières

1. [Interfaces TypeScript](#1-interfaces-typescript)
2. [Service Angular pour l'IA](#2-service-angular-pour-lia)
3. [Composant Badge de Prédiction IA](#3-composant-badge-de-prédiction-ia)
4. [Intégration dans les Dashboards](#4-intégration-dans-les-dashboards)
5. [Intégration après Validation d'Enquête](#5-intégration-après-validation-denquête)
6. [Intégration dans les Détails de Dossier](#6-intégration-dans-les-détails-de-dossier)
7. [Intégration dans la Liste des Dossiers](#7-intégration-dans-la-liste-des-dossiers)
8. [Intégration dans les Actions Amiables](#8-intégration-dans-les-actions-amiables)
9. [Intégration dans les Actions Huissier](#9-intégration-dans-les-actions-huissier) ✅ **NOUVEAU**
10. [Intégration dans les Audiences](#10-intégration-dans-les-audiences) ✅ **NOUVEAU**
11. [Service pour Actions Huissier](#11-service-pour-actions-huissier-exemple) ✅ **NOUVEAU**
12. [Service pour Audiences](#12-service-pour-audiences-exemple) ✅ **NOUVEAU**
13. [Styles CSS pour les Badges](#13-styles-css-pour-les-badges)

---

## 1. Interfaces TypeScript

### Fichier : `src/app/models/ia-prediction-result.model.ts`

```typescript
/**
 * Interface pour les résultats de prédiction IA
 */
export interface IaPredictionResult {
  etatFinal: 'RECOVERED_TOTAL' | 'RECOVERED_PARTIAL' | 'NOT_RECOVERED';
  riskScore: number; // 0-100
  riskLevel: 'Faible' | 'Moyen' | 'Élevé';
}

/**
 * Interface pour les données de prédiction dans un dossier
 */
export interface DossierPrediction {
  etatPrediction?: 'RECOVERED_TOTAL' | 'RECOVERED_PARTIAL' | 'NOT_RECOVERED';
  riskScore?: number;
  riskLevel?: 'Faible' | 'Moyen' | 'Élevé';
  datePrediction?: Date;
}
```

### Fichier : `src/app/models/dossier.model.ts` (Mise à jour)

```typescript
import { DossierPrediction } from './ia-prediction-result.model';

export interface Dossier {
  id: number;
  numeroDossier: string;
  titre: string;
  description?: string;
  montantCreance?: number;
  montantRecouvre?: number;
  montantRestant?: number;
  etatDossier?: string;
  
  // ✅ NOUVEAU : Champs pour la prédiction IA
  etatPrediction?: 'RECOVERED_TOTAL' | 'RECOVERED_PARTIAL' | 'NOT_RECOVERED';
  riskScore?: number;
  riskLevel?: 'Faible' | 'Moyen' | 'Élevé';
  
  dateCreation?: Date;
  dateCloture?: Date;
  dossierStatus?: string;
  statut?: string;
  // ... autres champs
}
```

---

## 2. Service Angular pour l'IA

### Fichier : `src/app/services/ia-prediction.service.ts`

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { IaPredictionResult } from '../models/ia-prediction-result.model';
import { Dossier } from '../models/dossier.model';

@Injectable({
  providedIn: 'root'
})
export class IaPredictionService {
  private apiUrl = 'http://localhost:8089/carthage-creance/api/dossiers';

  constructor(private http: HttpClient) {}

  /**
   * Déclencher une prédiction IA pour un dossier
   * @param dossierId ID du dossier
   * @returns Observable avec les résultats de la prédiction
   */
  predictForDossier(dossierId: number): Observable<IaPredictionResult> {
    const headers = this.getHeaders();
    return this.http.post<IaPredictionResult>(
      `${this.apiUrl}/${dossierId}/predict-ia`,
      {},
      { headers }
    ).pipe(
      catchError(error => {
        console.error('Erreur lors de la prédiction IA:', error);
        return throwError(() => new Error('Erreur lors de la prédiction IA'));
      })
    );
  }

  /**
   * Obtenir la prédiction depuis un dossier (si déjà calculée)
   * @param dossier Dossier avec les champs de prédiction
   * @returns Résultat de prédiction ou null
   */
  getPredictionFromDossier(dossier: Dossier): IaPredictionResult | null {
    if (dossier.etatPrediction && dossier.riskScore !== undefined) {
      return {
        etatFinal: dossier.etatPrediction,
        riskScore: dossier.riskScore,
        riskLevel: dossier.riskLevel || 'Moyen'
      };
    }
    return null;
  }

  /**
   * Vérifier si un dossier a une prédiction IA
   * @param dossier Dossier à vérifier
   * @returns true si le dossier a une prédiction
   */
  hasPrediction(dossier: Dossier): boolean {
    return dossier.etatPrediction !== undefined && 
           dossier.riskScore !== undefined;
  }

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

## 3. Composant Badge de Prédiction IA

### Fichier : `src/app/components/shared/ia-prediction-badge/ia-prediction-badge.component.ts`

```typescript
import { Component, Input, OnInit } from '@angular/core';
import { IaPredictionService } from '../../../services/ia-prediction.service';
import { IaPredictionResult } from '../../../models/ia-prediction-result.model';

@Component({
  selector: 'app-ia-prediction-badge',
  templateUrl: './ia-prediction-badge.component.html',
  styleUrls: ['./ia-prediction-badge.component.css']
})
export class IaPredictionBadgeComponent implements OnInit {
  @Input() prediction: IaPredictionResult | null = null;
  @Input() loading: boolean = false;
  @Input() showDetails: boolean = true; // Afficher les détails ou juste le badge
  @Input() size: 'small' | 'medium' | 'large' = 'medium';

  constructor(private iaPredictionService: IaPredictionService) {}

  ngOnInit(): void {
    // Le composant reçoit la prédiction en input
  }

  /**
   * Obtenir la couleur du badge selon le niveau de risque
   */
  getRiskLevelColor(riskLevel?: string): string {
    if (!riskLevel) return 'secondary';
    switch (riskLevel.toLowerCase()) {
      case 'faible':
        return 'success';
      case 'moyen':
        return 'warning';
      case 'élevé':
        return 'danger';
      default:
        return 'secondary';
    }
  }

  /**
   * Obtenir l'icône selon le niveau de risque
   */
  getRiskLevelIcon(riskLevel?: string): string {
    if (!riskLevel) return 'help_outline';
    switch (riskLevel.toLowerCase()) {
      case 'faible':
        return 'check_circle';
      case 'moyen':
        return 'warning';
      case 'élevé':
        return 'error';
      default:
        return 'help_outline';
    }
  }

  /**
   * Obtenir le label de l'état final
   */
  getEtatFinalLabel(etatFinal?: string): string {
    if (!etatFinal) return 'Non disponible';
    switch (etatFinal) {
      case 'RECOVERED_TOTAL':
        return 'Récupération Totale';
      case 'RECOVERED_PARTIAL':
        return 'Récupération Partielle';
      case 'NOT_RECOVERED':
        return 'Non Récupéré';
      default:
        return etatFinal;
    }
  }

  /**
   * Obtenir la couleur de l'état final
   */
  getEtatFinalColor(etatFinal?: string): string {
    if (!etatFinal) return 'secondary';
    switch (etatFinal) {
      case 'RECOVERED_TOTAL':
        return 'success';
      case 'RECOVERED_PARTIAL':
        return 'warning';
      case 'NOT_RECOVERED':
        return 'danger';
      default:
        return 'secondary';
    }
  }
}
```

### Fichier : `src/app/components/shared/ia-prediction-badge/ia-prediction-badge.component.html`

```html
<div class="ia-prediction-badge" [ngClass]="'size-' + size">
  <!-- Indicateur de chargement -->
  <div *ngIf="loading" class="loading-indicator">
    <i class="material-icons spin">hourglass_empty</i>
    <span>Calcul de la prédiction...</span>
  </div>

  <!-- Badge de prédiction -->
  <div *ngIf="!loading && prediction" class="prediction-content">
    <!-- Badge principal : Niveau de risque -->
    <div class="risk-badge" [ngClass]="'risk-' + getRiskLevelColor(prediction.riskLevel)">
      <i class="material-icons">{{ getRiskLevelIcon(prediction.riskLevel) }}</i>
      <span class="risk-level">{{ prediction.riskLevel }}</span>
      <span class="risk-score">{{ prediction.riskScore }}%</span>
    </div>

    <!-- Détails (si showDetails = true) -->
    <div *ngIf="showDetails" class="prediction-details">
      <div class="detail-item">
        <span class="detail-label">État Final Prédit :</span>
        <span class="badge badge-{{ getEtatFinalColor(prediction.etatFinal) }}">
          {{ getEtatFinalLabel(prediction.etatFinal) }}
        </span>
      </div>
      <div class="detail-item">
        <span class="detail-label">Score de Risque :</span>
        <span class="score-value">{{ prediction.riskScore }}%</span>
      </div>
    </div>
  </div>

  <!-- Message si pas de prédiction -->
  <div *ngIf="!loading && !prediction" class="no-prediction">
    <i class="material-icons">info</i>
    <span>Prédiction IA non disponible</span>
  </div>
</div>
```

### Fichier : `src/app/components/shared/ia-prediction-badge/ia-prediction-badge.component.css`

```css
.ia-prediction-badge {
  display: inline-flex;
  flex-direction: column;
  gap: 8px;
}

.size-small {
  font-size: 0.875rem;
}

.size-medium {
  font-size: 1rem;
}

.size-large {
  font-size: 1.25rem;
}

.loading-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6c757d;
  font-size: 0.875rem;
}

.loading-indicator .material-icons {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.risk-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  font-weight: 500;
  font-size: 0.875rem;
}

.risk-success {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.risk-warning {
  background-color: #fff3cd;
  color: #856404;
  border: 1px solid #ffeaa7;
}

.risk-danger {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.risk-badge .material-icons {
  font-size: 18px;
}

.risk-level {
  font-weight: 600;
}

.risk-score {
  font-size: 0.75rem;
  opacity: 0.8;
}

.prediction-details {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 8px;
  padding: 8px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.875rem;
}

.detail-label {
  font-weight: 500;
  color: #6c757d;
}

.score-value {
  font-weight: 600;
  color: #495057;
}

.no-prediction {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #6c757d;
  font-size: 0.875rem;
  font-style: italic;
}

.no-prediction .material-icons {
  font-size: 18px;
}
```

---

## 4. Intégration dans les Dashboards

### 4.1. Dashboard Chef Financier

### Fichier : `src/app/components/chef/chef-finance-dashboard/chef-finance-dashboard.component.ts`

```typescript
import { Component, OnInit } from '@angular/core';
import { DossierService } from '../../../services/dossier.service';
import { IaPredictionService } from '../../../services/ia-prediction.service';
import { Dossier } from '../../../models/dossier.model';
import { IaPredictionResult } from '../../../models/ia-prediction-result.model';

@Component({
  selector: 'app-chef-finance-dashboard',
  templateUrl: './chef-finance-dashboard.component.html',
  styleUrls: ['./chef-finance-dashboard.component.css']
})
export class ChefFinanceDashboardComponent implements OnInit {
  dossiers: Dossier[] = [];
  predictions: Map<number, IaPredictionResult> = new Map();
  loading: boolean = false;

  constructor(
    private dossierService: DossierService,
    private iaPredictionService: IaPredictionService
  ) {}

  ngOnInit(): void {
    this.loadDossiers();
  }

  loadDossiers(): void {
    this.loading = true;
    this.dossierService.getAllDossiers().subscribe({
      next: (dossiers) => {
        this.dossiers = dossiers;
        // Charger les prédictions pour les dossiers qui en ont
        this.loadPredictions();
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des dossiers:', error);
        this.loading = false;
      }
    });
  }

  loadPredictions(): void {
    this.dossiers.forEach(dossier => {
      const prediction = this.iaPredictionService.getPredictionFromDossier(dossier);
      if (prediction) {
        this.predictions.set(dossier.id, prediction);
      }
    });
  }

  getPrediction(dossierId: number): IaPredictionResult | null {
    return this.predictions.get(dossierId) || null;
  }

  /**
   * Déclencher une prédiction IA manuellement
   */
  triggerPrediction(dossierId: number): void {
    this.iaPredictionService.predictForDossier(dossierId).subscribe({
      next: (prediction) => {
        this.predictions.set(dossierId, prediction);
        // Rafraîchir le dossier pour obtenir les valeurs mises à jour
        this.refreshDossier(dossierId);
      },
      error: (error) => {
        console.error('Erreur lors de la prédiction:', error);
        alert('Erreur lors du calcul de la prédiction IA');
      }
    });
  }

  refreshDossier(dossierId: number): void {
    this.dossierService.getDossierById(dossierId).subscribe({
      next: (dossier) => {
        const index = this.dossiers.findIndex(d => d.id === dossierId);
        if (index !== -1) {
          this.dossiers[index] = dossier;
          const prediction = this.iaPredictionService.getPredictionFromDossier(dossier);
          if (prediction) {
            this.predictions.set(dossierId, prediction);
          }
        }
      }
    });
  }
}
```

### Fichier : `src/app/components/chef/chef-finance-dashboard/chef-finance-dashboard.component.html`

```html
<div class="dashboard-container">
  <h2>Tableau de Bord - Chef Financier</h2>

  <div class="dossiers-list" *ngIf="!loading">
    <table class="table table-striped">
      <thead>
        <tr>
          <th>Numéro Dossier</th>
          <th>Titre</th>
          <th>Montant Créance</th>
          <th>Prédiction IA</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let dossier of dossiers">
          <td>{{ dossier.numeroDossier }}</td>
          <td>{{ dossier.titre }}</td>
          <td>{{ dossier.montantCreance | number:'1.2-2' }} TND</td>
          <td>
            <!-- Badge de prédiction IA -->
            <app-ia-prediction-badge
              [prediction]="getPrediction(dossier.id)"
              [showDetails]="false"
              size="small"
            ></app-ia-prediction-badge>
            
            <!-- Bouton pour déclencher la prédiction si pas disponible -->
            <button 
              *ngIf="!getPrediction(dossier.id)"
              class="btn btn-sm btn-primary"
              (click)="triggerPrediction(dossier.id)">
              <i class="material-icons">psychology</i>
              Calculer Prédiction
            </button>
          </td>
          <td>
            <button class="btn btn-sm btn-info" (click)="viewDossier(dossier.id)">
              Voir Détails
            </button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>

  <div *ngIf="loading" class="loading">
    <i class="material-icons spin">hourglass_empty</i>
    Chargement...
  </div>
</div>
```

### 4.2. Dashboard Agent

### Fichier : `src/app/components/agent/agent-dashboard/agent-dashboard.component.ts`

```typescript
import { Component, OnInit } from '@angular/core';
import { DossierService } from '../../../services/dossier.service';
import { IaPredictionService } from '../../../services/ia-prediction.service';
import { Dossier } from '../../../models/dossier.model';

@Component({
  selector: 'app-agent-dashboard',
  templateUrl: './agent-dashboard.component.html',
  styleUrls: ['./agent-dashboard.component.css']
})
export class AgentDashboardComponent implements OnInit {
  mesDossiers: Dossier[] = [];
  loading: boolean = false;

  constructor(
    private dossierService: DossierService,
    private iaPredictionService: IaPredictionService
  ) {}

  ngOnInit(): void {
    this.loadMesDossiers();
  }

  loadMesDossiers(): void {
    this.loading = true;
    const userId = this.getCurrentUserId();
    
    this.dossierService.getDossiersByAgent(userId).subscribe({
      next: (dossiers) => {
        this.mesDossiers = dossiers;
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur:', error);
        this.loading = false;
      }
    });
  }

  getPrediction(dossier: Dossier) {
    return this.iaPredictionService.getPredictionFromDossier(dossier);
  }

  private getCurrentUserId(): number {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    return user.id || 0;
  }
}
```

### Fichier : `src/app/components/agent/agent-dashboard/agent-dashboard.component.html`

```html
<div class="dashboard-container">
  <h2>Mes Dossiers</h2>

  <div class="dossiers-grid" *ngIf="!loading">
    <div class="dossier-card" *ngFor="let dossier of mesDossiers">
      <div class="card-header">
        <h4>{{ dossier.numeroDossier }}</h4>
        <span class="badge badge-{{ dossier.statut }}">{{ dossier.statut }}</span>
      </div>
      
      <div class="card-body">
        <p><strong>{{ dossier.titre }}</strong></p>
        <p>Montant: {{ dossier.montantCreance | number:'1.2-2' }} TND</p>
        
        <!-- Prédiction IA -->
        <div class="prediction-section">
          <app-ia-prediction-badge
            [prediction]="getPrediction(dossier)"
            [showDetails]="true"
            size="small"
          ></app-ia-prediction-badge>
        </div>
      </div>
      
      <div class="card-footer">
        <button class="btn btn-sm btn-primary" (click)="viewDossier(dossier.id)">
          Voir Détails
        </button>
      </div>
    </div>
  </div>

  <div *ngIf="loading" class="loading">
    <i class="material-icons spin">hourglass_empty</i>
    Chargement...
  </div>
</div>
```

---

## 5. Intégration après Validation d'Enquête

### Fichier : `src/app/components/enquete/validation-enquete/validation-enquete.component.ts`

```typescript
import { Component, OnInit, Input } from '@angular/core';
import { EnqueteService } from '../../../services/enquete.service';
import { DossierService } from '../../../services/dossier.service';
import { IaPredictionService } from '../../../services/ia-prediction.service';
import { Enquette } from '../../../models/enquette.model';
import { Dossier } from '../../../models/dossier.model';
import { IaPredictionResult } from '../../../models/ia-prediction-result.model';

@Component({
  selector: 'app-validation-enquete',
  templateUrl: './validation-enquete.component.html',
  styleUrls: ['./validation-enquete.component.css']
})
export class ValidationEnqueteComponent implements OnInit {
  @Input() enquette: Enquette | null = null;
  
  dossier: Dossier | null = null;
  prediction: IaPredictionResult | null = null;
  loadingValidation: boolean = false;
  loadingPrediction: boolean = false;

  constructor(
    private enqueteService: EnqueteService,
    private dossierService: DossierService,
    private iaPredictionService: IaPredictionService
  ) {}

  ngOnInit(): void {
    if (this.enquette?.dossierId) {
      this.loadDossier();
    }
  }

  /**
   * Valider l'enquête
   * Le backend déclenche automatiquement la prédiction IA
   */
  validerEnquette(): void {
    if (!this.enquette) return;
    
    const chefId = this.getCurrentChefId();
    if (!chefId) {
      alert('Erreur : Chef non identifié');
      return;
    }

    this.loadingValidation = true;
    
    this.enqueteService.validerEnquette(this.enquette.id, chefId).subscribe({
      next: (enquetteValidee) => {
        this.enquette = enquetteValidee;
        this.loadingValidation = false;
        
        // ✅ NOUVEAU : Rafraîchir le dossier pour obtenir la prédiction IA
        if (enquetteValidee.dossierId) {
          this.loadDossier();
        }
        
        // Afficher un message de succès
        alert('Enquête validée avec succès ! La prédiction IA a été calculée automatiquement.');
      },
      error: (error) => {
        console.error('Erreur lors de la validation:', error);
        this.loadingValidation = false;
        alert('Erreur lors de la validation de l\'enquête');
      }
    });
  }

  /**
   * Charger le dossier avec sa prédiction IA
   */
  loadDossier(): void {
    if (!this.enquette?.dossierId) return;
    
    this.loadingPrediction = true;
    
    this.dossierService.getDossierById(this.enquette.dossierId).subscribe({
      next: (dossier) => {
        this.dossier = dossier;
        
        // Extraire la prédiction IA du dossier
        this.prediction = this.iaPredictionService.getPredictionFromDossier(dossier);
        
        this.loadingPrediction = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement du dossier:', error);
        this.loadingPrediction = false;
      }
    });
  }

  private getCurrentChefId(): number | null {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    return user.id || null;
  }
}
```

### Fichier : `src/app/components/enquete/validation-enquete/validation-enquete.component.html`

```html
<div class="validation-enquete" *ngIf="enquette">
  <div class="card">
    <div class="card-header">
      <h4>Validation de l'Enquête</h4>
    </div>
    <div class="card-body">
      <!-- Informations de l'enquête -->
      <div class="enquete-info mb-4">
        <h5>Détails de l'Enquête</h5>
        <p><strong>Rapport Code:</strong> {{enquette.rapportCode}}</p>
        <p><strong>Statut:</strong> 
          <span class="badge badge-{{enquette.statut === 'VALIDE' ? 'success' : 'warning'}}">
            {{enquette.statut}}
          </span>
        </p>
      </div>

      <!-- Bouton de validation -->
      <div class="validation-actions mb-4" *ngIf="enquette.statut !== 'VALIDE'">
        <button 
          class="btn btn-success" 
          (click)="validerEnquette()"
          [disabled]="loadingValidation">
          <i class="material-icons" *ngIf="!loadingValidation">check</i>
          <span class="spinner-border spinner-border-sm" *ngIf="loadingValidation"></span>
          Valider l'Enquête
        </button>
      </div>

      <!-- ✅ NOUVEAU : Section Prédiction IA après validation -->
      <div class="prediction-section mt-4" *ngIf="enquette.statut === 'VALIDE'">
        <h5>
          <i class="material-icons">psychology</i>
          Prédiction IA (Calculée Automatiquement)
        </h5>
        
        <!-- Indicateur de chargement -->
        <div class="alert alert-info" *ngIf="loadingPrediction">
          <i class="material-icons">hourglass_empty</i>
          Chargement de la prédiction IA...
        </div>
        
        <!-- Badge de prédiction -->
        <app-ia-prediction-badge
          *ngIf="prediction && !loadingPrediction"
          [prediction]="prediction"
          [loading]="false"
          [showDetails]="true"
        ></app-ia-prediction-badge>
        
        <!-- Message si pas de prédiction -->
        <div class="alert alert-warning" *ngIf="!prediction && !loadingPrediction">
          <i class="material-icons">info</i>
          La prédiction IA sera disponible après la validation de l'enquête.
        </div>
      </div>
    </div>
  </div>
</div>
```

---

## 6. Intégration dans les Détails de Dossier

### Fichier : `src/app/components/dossier/dossier-detail/dossier-detail.component.ts`

```typescript
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DossierService } from '../../../services/dossier.service';
import { IaPredictionService } from '../../../services/ia-prediction.service';
import { Dossier } from '../../../models/dossier.model';
import { IaPredictionResult } from '../../../models/ia-prediction-result.model';

@Component({
  selector: 'app-dossier-detail',
  templateUrl: './dossier-detail.component.html',
  styleUrls: ['./dossier-detail.component.css']
})
export class DossierDetailComponent implements OnInit {
  dossier: Dossier | null = null;
  prediction: IaPredictionResult | null = null;
  loading: boolean = false;
  loadingPrediction: boolean = false;

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
    this.loading = true;
    this.dossierService.getDossierById(dossierId).subscribe({
      next: (dossier) => {
        this.dossier = dossier;
        this.prediction = this.iaPredictionService.getPredictionFromDossier(dossier);
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur:', error);
        this.loading = false;
      }
    });
  }

  /**
   * Déclencher une nouvelle prédiction IA
   */
  triggerPrediction(): void {
    if (!this.dossier) return;
    
    this.loadingPrediction = true;
    this.iaPredictionService.predictForDossier(this.dossier.id).subscribe({
      next: (prediction) => {
        this.prediction = prediction;
        this.loadingPrediction = false;
        // Rafraîchir le dossier pour obtenir les valeurs mises à jour
        this.loadDossier(this.dossier!.id);
      },
      error: (error) => {
        console.error('Erreur lors de la prédiction:', error);
        this.loadingPrediction = false;
        alert('Erreur lors du calcul de la prédiction IA');
      }
    });
  }
}
```

### Fichier : `src/app/components/dossier/dossier-detail/dossier-detail.component.html`

```html
<div class="dossier-detail" *ngIf="dossier && !loading">
  <div class="card">
    <div class="card-header">
      <h3>Dossier : {{ dossier.numeroDossier }}</h3>
    </div>
    
    <div class="card-body">
      <!-- Informations générales -->
      <div class="section">
        <h4>Informations Générales</h4>
        <p><strong>Titre:</strong> {{ dossier.titre }}</p>
        <p><strong>Montant Créance:</strong> {{ dossier.montantCreance | number:'1.2-2' }} TND</p>
        <p><strong>Statut:</strong> {{ dossier.statut }}</p>
      </div>

      <!-- ✅ Section Prédiction IA -->
      <div class="section prediction-section">
        <div class="section-header">
          <h4>
            <i class="material-icons">psychology</i>
            Prédiction IA
          </h4>
          <button 
            class="btn btn-sm btn-primary"
            (click)="triggerPrediction()"
            [disabled]="loadingPrediction">
            <i class="material-icons">refresh</i>
            Recalculer
          </button>
        </div>
        
        <app-ia-prediction-badge
          [prediction]="prediction"
          [loading]="loadingPrediction"
          [showDetails]="true"
          size="large"
        ></app-ia-prediction-badge>
      </div>

      <!-- Autres sections... -->
    </div>
  </div>
</div>

<div *ngIf="loading" class="loading">
  <i class="material-icons spin">hourglass_empty</i>
  Chargement...
</div>
```

---

## 7. Intégration dans la Liste des Dossiers

### Fichier : `src/app/components/dossier/dossier-list/dossier-list.component.ts`

```typescript
import { Component, OnInit } from '@angular/core';
import { DossierService } from '../../../services/dossier.service';
import { IaPredictionService } from '../../../services/ia-prediction.service';
import { Dossier } from '../../../models/dossier.model';
import { IaPredictionResult } from '../../../models/ia-prediction-result.model';

@Component({
  selector: 'app-dossier-list',
  templateUrl: './dossier-list.component.html',
  styleUrls: ['./dossier-list.component.css']
})
export class DossierListComponent implements OnInit {
  dossiers: Dossier[] = [];
  predictions: Map<number, IaPredictionResult> = new Map();

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
        // Extraire les prédictions des dossiers
        dossiers.forEach(dossier => {
          const prediction = this.iaPredictionService.getPredictionFromDossier(dossier);
          if (prediction) {
            this.predictions.set(dossier.id, prediction);
          }
        });
      },
      error: (error) => console.error('Erreur:', error)
    });
  }

  getPrediction(dossierId: number): IaPredictionResult | null {
    return this.predictions.get(dossierId) || null;
  }
}
```

### Fichier : `src/app/components/dossier/dossier-list/dossier-list.component.html`

```html
<table class="table table-striped">
  <thead>
    <tr>
      <th>Numéro</th>
      <th>Titre</th>
      <th>Montant</th>
      <th>Prédiction IA</th>
      <th>Actions</th>
    </tr>
  </thead>
  <tbody>
    <tr *ngFor="let dossier of dossiers">
      <td>{{ dossier.numeroDossier }}</td>
      <td>{{ dossier.titre }}</td>
      <td>{{ dossier.montantCreance | number:'1.2-2' }} TND</td>
      <td>
        <app-ia-prediction-badge
          [prediction]="getPrediction(dossier.id)"
          [showDetails]="false"
          size="small"
        ></app-ia-prediction-badge>
      </td>
      <td>
        <button class="btn btn-sm btn-primary" (click)="viewDossier(dossier.id)">
          Voir
        </button>
      </td>
    </tr>
  </tbody>
</table>
```

---

## 8. Intégration dans les Actions Amiables

### Fichier : `src/app/components/action/action-amiable/action-amiable.component.ts`

```typescript
import { Component, OnInit, Input } from '@angular/core';
import { DossierService } from '../../../services/dossier.service';
import { IaPredictionService } from '../../../services/ia-prediction.service';
import { Dossier } from '../../../models/dossier.model';

@Component({
  selector: 'app-action-amiable',
  templateUrl: './action-amiable.component.html',
  styleUrls: ['./action-amiable.component.css']
})
export class ActionAmiableComponent implements OnInit {
  @Input() dossierId: number | null = null;
  dossier: Dossier | null = null;
  prediction: any = null;

  constructor(
    private dossierService: DossierService,
    private iaPredictionService: IaPredictionService
  ) {}

  ngOnInit(): void {
    if (this.dossierId) {
      this.loadDossier();
    }
  }

  loadDossier(): void {
    if (!this.dossierId) return;
    
    this.dossierService.getDossierById(this.dossierId).subscribe({
      next: (dossier) => {
        this.dossier = dossier;
        this.prediction = this.iaPredictionService.getPredictionFromDossier(dossier);
      }
    });
  }

  /**
   * Après enregistrement d'une action amiable, le backend déclenche automatiquement la prédiction IA
   * On rafraîchit le dossier pour obtenir la nouvelle prédiction
   */
  onActionSaved(): void {
    if (this.dossierId) {
      this.loadDossier();
    }
  }
}
```

---

## 9. Intégration dans les Actions Huissier

### Fichier : `src/app/components/action-huissier/action-huissier.component.ts`

```typescript
import { Component, OnInit, Input } from '@angular/core';
import { DossierService } from '../../services/dossier.service';
import { IaPredictionService } from '../../services/ia-prediction.service';
import { ActionHuissierService } from '../../services/action-huissier.service';
import { Dossier } from '../../models/dossier.model';
import { IaPredictionResult } from '../../models/ia-prediction-result.model';

@Component({
  selector: 'app-action-huissier',
  templateUrl: './action-huissier.component.html',
  styleUrls: ['./action-huissier.component.css']
})
export class ActionHuissierComponent implements OnInit {
  @Input() dossierId: number | null = null;
  dossier: Dossier | null = null;
  prediction: IaPredictionResult | null = null;
  actionsHuissier: any[] = [];
  loading: boolean = false;
  loadingPrediction: boolean = false;

  constructor(
    private dossierService: DossierService,
    private iaPredictionService: IaPredictionService,
    private actionHuissierService: ActionHuissierService
  ) {}

  ngOnInit(): void {
    if (this.dossierId) {
      this.loadDossier();
      this.loadActionsHuissier();
    }
  }

  loadDossier(): void {
    if (!this.dossierId) return;
    
    this.loading = true;
    this.dossierService.getDossierById(this.dossierId).subscribe({
      next: (dossier) => {
        this.dossier = dossier;
        this.prediction = this.iaPredictionService.getPredictionFromDossier(dossier);
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement du dossier:', error);
        this.loading = false;
      }
    });
  }

  loadActionsHuissier(): void {
    if (!this.dossierId) return;
    
    this.actionHuissierService.getActionsByDossier(this.dossierId).subscribe({
      next: (actions) => {
        this.actionsHuissier = actions;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des actions huissier:', error);
      }
    });
  }

  /**
   * Après création/modification d'une action huissier
   * Le backend peut déclencher automatiquement la prédiction IA
   * On rafraîchit le dossier pour obtenir la nouvelle prédiction
   */
  onActionHuissierSaved(): void {
    if (this.dossierId) {
      // Rafraîchir le dossier pour obtenir la prédiction mise à jour
      this.loadDossier();
      this.loadActionsHuissier();
    }
  }

  /**
   * Déclencher manuellement une prédiction IA après modification d'actions huissier
   */
  triggerPrediction(): void {
    if (!this.dossierId) return;
    
    this.loadingPrediction = true;
    this.iaPredictionService.predictForDossier(this.dossierId).subscribe({
      next: (prediction) => {
        this.prediction = prediction;
        this.loadingPrediction = false;
        // Rafraîchir le dossier pour obtenir les valeurs mises à jour
        this.loadDossier();
      },
      error: (error) => {
        console.error('Erreur lors de la prédiction:', error);
        this.loadingPrediction = false;
        alert('Erreur lors du calcul de la prédiction IA');
      }
    });
  }
}
```

### Fichier : `src/app/components/action-huissier/action-huissier.component.html`

```html
<div class="action-huissier-container" *ngIf="dossier">
  <div class="card">
    <div class="card-header">
      <h4>
        <i class="material-icons">gavel</i>
        Actions Huissier - Dossier {{ dossier.numeroDossier }}
      </h4>
    </div>
    
    <div class="card-body">
      <!-- ✅ Section Prédiction IA -->
      <div class="prediction-section mb-4">
        <div class="section-header">
          <h5>
            <i class="material-icons">psychology</i>
            Prédiction IA
          </h5>
          <button 
            class="btn btn-sm btn-primary"
            (click)="triggerPrediction()"
            [disabled]="loadingPrediction">
            <i class="material-icons">refresh</i>
            Recalculer
          </button>
        </div>
        
        <app-ia-prediction-badge
          [prediction]="prediction"
          [loading]="loadingPrediction"
          [showDetails]="true"
          size="medium"
        ></app-ia-prediction-badge>
      </div>

      <!-- Liste des actions huissier -->
      <div class="actions-list">
        <h5>Actions Huissier Enregistrées</h5>
        <table class="table table-striped" *ngIf="actionsHuissier.length > 0">
          <thead>
            <tr>
              <th>Type</th>
              <th>Date</th>
              <th>Montant</th>
              <th>Statut</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let action of actionsHuissier">
              <td>{{ action.type }}</td>
              <td>{{ action.dateAction | date:'short' }}</td>
              <td>{{ action.montant | number:'1.2-2' }} TND</td>
              <td>
                <span class="badge badge-{{ action.statut }}">{{ action.statut }}</span>
              </td>
            </tr>
          </tbody>
        </table>
        <p *ngIf="actionsHuissier.length === 0" class="text-muted">
          Aucune action huissier enregistrée pour ce dossier.
        </p>
      </div>

      <!-- Formulaire d'ajout/modification d'action huissier -->
      <div class="action-form mt-4">
        <h5>Ajouter une Action Huissier</h5>
        <!-- Formulaire ici -->
        <!-- Après sauvegarde, appeler onActionHuissierSaved() -->
      </div>
    </div>
  </div>
</div>

<div *ngIf="loading" class="loading">
  <i class="material-icons spin">hourglass_empty</i>
  Chargement...
</div>
```

---

## 10. Intégration dans les Audiences

### Fichier : `src/app/components/audience/audience.component.ts`

```typescript
import { Component, OnInit, Input } from '@angular/core';
import { DossierService } from '../../services/dossier.service';
import { IaPredictionService } from '../../services/ia-prediction.service';
import { AudienceService } from '../../services/audience.service';
import { Dossier } from '../../models/dossier.model';
import { IaPredictionResult } from '../../models/ia-prediction-result.model';

@Component({
  selector: 'app-audience',
  templateUrl: './audience.component.html',
  styleUrls: ['./audience.component.css']
})
export class AudienceComponent implements OnInit {
  @Input() dossierId: number | null = null;
  dossier: Dossier | null = null;
  prediction: IaPredictionResult | null = null;
  audiences: any[] = [];
  loading: boolean = false;
  loadingPrediction: boolean = false;

  constructor(
    private dossierService: DossierService,
    private iaPredictionService: IaPredictionService,
    private audienceService: AudienceService
  ) {}

  ngOnInit(): void {
    if (this.dossierId) {
      this.loadDossier();
      this.loadAudiences();
    }
  }

  loadDossier(): void {
    if (!this.dossierId) return;
    
    this.loading = true;
    this.dossierService.getDossierById(this.dossierId).subscribe({
      next: (dossier) => {
        this.dossier = dossier;
        this.prediction = this.iaPredictionService.getPredictionFromDossier(dossier);
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement du dossier:', error);
        this.loading = false;
      }
    });
  }

  loadAudiences(): void {
    if (!this.dossierId) return;
    
    this.audienceService.getAudiencesByDossier(this.dossierId).subscribe({
      next: (audiences) => {
        this.audiences = audiences;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des audiences:', error);
      }
    });
  }

  /**
   * Après création/modification d'une audience
   * Le backend peut déclencher automatiquement la prédiction IA
   * On rafraîchit le dossier pour obtenir la nouvelle prédiction
   */
  onAudienceSaved(): void {
    if (this.dossierId) {
      // Rafraîchir le dossier pour obtenir la prédiction mise à jour
      this.loadDossier();
      this.loadAudiences();
    }
  }

  /**
   * Déclencher manuellement une prédiction IA après modification d'audiences
   */
  triggerPrediction(): void {
    if (!this.dossierId) return;
    
    this.loadingPrediction = true;
    this.iaPredictionService.predictForDossier(this.dossierId).subscribe({
      next: (prediction) => {
        this.prediction = prediction;
        this.loadingPrediction = false;
        // Rafraîchir le dossier pour obtenir les valeurs mises à jour
        this.loadDossier();
      },
      error: (error) => {
        console.error('Erreur lors de la prédiction:', error);
        this.loadingPrediction = false;
        alert('Erreur lors du calcul de la prédiction IA');
      }
    });
  }
}
```

### Fichier : `src/app/components/audience/audience.component.html`

```html
<div class="audience-container" *ngIf="dossier">
  <div class="card">
    <div class="card-header">
      <h4>
        <i class="material-icons">account_balance</i>
        Audiences - Dossier {{ dossier.numeroDossier }}
      </h4>
    </div>
    
    <div class="card-body">
      <!-- ✅ Section Prédiction IA -->
      <div class="prediction-section mb-4">
        <div class="section-header">
          <h5>
            <i class="material-icons">psychology</i>
            Prédiction IA
          </h5>
          <button 
            class="btn btn-sm btn-primary"
            (click)="triggerPrediction()"
            [disabled]="loadingPrediction">
            <i class="material-icons">refresh</i>
            Recalculer
          </button>
        </div>
        
        <app-ia-prediction-badge
          [prediction]="prediction"
          [loading]="loadingPrediction"
          [showDetails]="true"
          size="medium"
        ></app-ia-prediction-badge>
      </div>

      <!-- Liste des audiences -->
      <div class="audiences-list">
        <h5>Audiences Enregistrées</h5>
        <table class="table table-striped" *ngIf="audiences.length > 0">
          <thead>
            <tr>
              <th>Date</th>
              <th>Type</th>
              <th>Avocat</th>
              <th>Décision</th>
              <th>Statut</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let audience of audiences">
              <td>{{ audience.dateAudience | date:'short' }}</td>
              <td>{{ audience.type }}</td>
              <td>{{ audience.avocat?.nom }} {{ audience.avocat?.prenom }}</td>
              <td>
                <span class="badge badge-{{ audience.decision === 'POSITIVE' ? 'success' : 'danger' }}">
                  {{ audience.decision }}
                </span>
              </td>
              <td>
                <span class="badge badge-{{ audience.statut }}">{{ audience.statut }}</span>
              </td>
            </tr>
          </tbody>
        </table>
        <p *ngIf="audiences.length === 0" class="text-muted">
          Aucune audience enregistrée pour ce dossier.
        </p>
      </div>

      <!-- Formulaire d'ajout/modification d'audience -->
      <div class="audience-form mt-4">
        <h5>Ajouter une Audience</h5>
        <!-- Formulaire ici -->
        <!-- Après sauvegarde, appeler onAudienceSaved() -->
      </div>
    </div>
  </div>
</div>

<div *ngIf="loading" class="loading">
  <i class="material-icons spin">hourglass_empty</i>
  Chargement...
</div>
```

---

## 11. Service pour Actions Huissier (Exemple)

### Fichier : `src/app/services/action-huissier.service.ts`

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ActionHuissierService {
  private apiUrl = 'http://localhost:8089/carthage-creance/api/action-huissier';

  constructor(private http: HttpClient) {}

  /**
   * Récupérer toutes les actions huissier d'un dossier
   */
  getActionsByDossier(dossierId: number): Observable<any[]> {
    const headers = this.getHeaders();
    return this.http.get<any[]>(`${this.apiUrl}/dossier/${dossierId}`, { headers }).pipe(
      catchError(error => {
        console.error('Erreur lors de la récupération des actions huissier:', error);
        return throwError(() => new Error('Erreur lors de la récupération des actions huissier'));
      })
    );
  }

  /**
   * Créer une nouvelle action huissier
   */
  createActionHuissier(action: any): Observable<any> {
    const headers = this.getHeaders();
    return this.http.post<any>(this.apiUrl, action, { headers }).pipe(
      catchError(error => {
        console.error('Erreur lors de la création de l\'action huissier:', error);
        return throwError(() => new Error('Erreur lors de la création de l\'action huissier'));
      })
    );
  }

  /**
   * Mettre à jour une action huissier
   */
  updateActionHuissier(id: number, action: any): Observable<any> {
    const headers = this.getHeaders();
    return this.http.put<any>(`${this.apiUrl}/${id}`, action, { headers }).pipe(
      catchError(error => {
        console.error('Erreur lors de la mise à jour de l\'action huissier:', error);
        return throwError(() => new Error('Erreur lors de la mise à jour de l\'action huissier'));
      })
    );
  }

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

## 12. Service pour Audiences (Exemple)

### Fichier : `src/app/services/audience.service.ts`

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AudienceService {
  private apiUrl = 'http://localhost:8089/carthage-creance/api/audiences';

  constructor(private http: HttpClient) {}

  /**
   * Récupérer toutes les audiences d'un dossier
   */
  getAudiencesByDossier(dossierId: number): Observable<any[]> {
    const headers = this.getHeaders();
    return this.http.get<any[]>(`${this.apiUrl}/dossier/${dossierId}`, { headers }).pipe(
      catchError(error => {
        console.error('Erreur lors de la récupération des audiences:', error);
        return throwError(() => new Error('Erreur lors de la récupération des audiences'));
      })
    );
  }

  /**
   * Créer une nouvelle audience
   */
  createAudience(audience: any): Observable<any> {
    const headers = this.getHeaders();
    return this.http.post<any>(this.apiUrl, audience, { headers }).pipe(
      catchError(error => {
        console.error('Erreur lors de la création de l\'audience:', error);
        return throwError(() => new Error('Erreur lors de la création de l\'audience'));
      })
    );
  }

  /**
   * Mettre à jour une audience
   */
  updateAudience(id: number, audience: any): Observable<any> {
    const headers = this.getHeaders();
    return this.http.put<any>(`${this.apiUrl}/${id}`, audience, { headers }).pipe(
      catchError(error => {
        console.error('Erreur lors de la mise à jour de l\'audience:', error);
        return throwError(() => new Error('Erreur lors de la mise à jour de l\'audience'));
      })
    );
  }

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

## 9. Styles CSS Globaux (Optionnel)

### Fichier : `src/styles.css` (Ajout)

```css
/* Styles globaux pour les badges IA */
.ia-prediction-container {
  margin: 16px 0;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #007bff;
}

.ia-prediction-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-weight: 600;
  color: #495057;
}

.ia-prediction-title .material-icons {
  color: #007bff;
}

/* Animation pour le spinner */
.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
```

---

## 📋 Checklist d'Implémentation

- [ ] Créer les interfaces TypeScript (`ia-prediction-result.model.ts`)
- [ ] Mettre à jour `dossier.model.ts` avec les champs IA
- [ ] Créer `IaPredictionService`
- [ ] Créer le composant `IaPredictionBadgeComponent`
- [ ] Intégrer dans `ChefFinanceDashboardComponent`
- [ ] Intégrer dans `AgentDashboardComponent`
- [ ] Intégrer dans `ValidationEnqueteComponent`
- [ ] Intégrer dans `DossierDetailComponent`
- [ ] Intégrer dans `DossierListComponent`
- [ ] Intégrer dans `ActionAmiableComponent`
- [ ] ✅ **Intégrer dans `ActionHuissierComponent`** (NOUVEAU)
- [ ] ✅ **Intégrer dans `AudienceComponent`** (NOUVEAU)
- [ ] ✅ **Créer `ActionHuissierService`** (NOUVEAU)
- [ ] ✅ **Créer `AudienceService`** (NOUVEAU)
- [ ] Ajouter les styles CSS
- [ ] Tester tous les scénarios

---

## 🎯 Résumé

Ce guide complet fournit :

1. ✅ **Toutes les interfaces TypeScript** nécessaires
2. ✅ **Service Angular complet** pour l'IA
3. ✅ **Composant badge réutilisable** avec styles
4. ✅ **Intégration dans tous les dashboards** (Chef, Agent, SuperAdmin)
5. ✅ **Intégration après validation d'enquête**
6. ✅ **Intégration dans les détails et listes de dossiers**
7. ✅ **Exemples de code complets** prêts à utiliser

**Date de création** : 2025-12-02  
**Statut** : ✅ Complet et prêt pour implémentation

