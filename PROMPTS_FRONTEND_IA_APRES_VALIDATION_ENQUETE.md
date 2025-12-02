# 🎨 Prompts Frontend - Affichage Prédiction IA après Validation Enquête

## 📋 Vue d'Ensemble

Ce document contient les prompts pour intégrer l'affichage de la prédiction IA dans les interfaces frontend après la validation d'une enquête.

---

## 🎯 Prompt 1 : Modifier le Service Enquête pour Détecter la Prédiction IA

### Fichier : `src/app/services/enquete.service.ts`

**Modifications à apporter** :

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { Enquette } from '../models/enquette.model';
import { Dossier } from '../models/dossier.model';

@Injectable({
  providedIn: 'root'
})
export class EnqueteService {
  private apiUrl = 'http://localhost:8089/carthage-creance/api/enquettes';

  constructor(
    private http: HttpClient,
    private dossierService: DossierService  // Injecter DossierService
  ) {}

  /**
   * Valider une enquête
   * Après validation, le backend déclenche automatiquement la prédiction IA
   * 
   * @param enquetteId ID de l'enquête
   * @param chefId ID du chef qui valide
   * @returns Observable de l'enquête validée
   */
  validerEnquette(enquetteId: number, chefId: number): Observable<Enquette> {
    const headers = this.getHeaders();
    return this.http.put<Enquette>(
      `${this.apiUrl}/${enquetteId}/valider`,
      null,
      { 
        headers,
        params: { chefId: chefId.toString() }
      }
    ).pipe(
      tap((enquette) => {
        // ✅ NOUVEAU : Après validation, récupérer le dossier mis à jour avec la prédiction IA
        if (enquette.dossierId) {
          // Le backend a automatiquement mis à jour le dossier avec la prédiction IA
          // On peut optionnellement rafraîchir le dossier pour obtenir les nouvelles valeurs
          this.dossierService.getDossierById(enquette.dossierId).subscribe({
            next: (dossier) => {
              console.log('Dossier mis à jour avec prédiction IA:', dossier);
            },
            error: (error) => {
              console.warn('Impossible de récupérer le dossier mis à jour:', error);
            }
          });
        }
      }),
      catchError(error => {
        console.error('Erreur lors de la validation de l\'enquête:', error);
        return throwError(() => new Error('Erreur lors de la validation de l\'enquête'));
      })
    );
  }

  /**
   * Créer une enquête
   * Si créée par un chef, la validation et la prédiction IA sont automatiques
   * 
   * @param enquette Données de l'enquête
   * @returns Observable de l'enquête créée
   */
  createEnquette(enquette: Enquette): Observable<Enquette> {
    const headers = this.getHeaders();
    return this.http.post<Enquette>(
      `${this.apiUrl}`,
      enquette,
      { headers }
    ).pipe(
      tap((createdEnquette) => {
        // ✅ NOUVEAU : Si l'enquête est validée automatiquement (créée par chef),
        // le backend a déjà déclenché la prédiction IA
        if (createdEnquette.statut === 'VALIDE' && createdEnquette.dossierId) {
          this.dossierService.getDossierById(createdEnquette.dossierId).subscribe({
            next: (dossier) => {
              console.log('Dossier mis à jour avec prédiction IA après création enquête:', dossier);
            }
          });
        }
      }),
      catchError(error => {
        console.error('Erreur lors de la création de l\'enquête:', error);
        return throwError(() => new Error('Erreur lors de la création de l\'enquête'));
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

## 🎯 Prompt 2 : Modifier le Composant de Validation d'Enquête

### Fichier : `src/app/components/enquete/validation-enquete/validation-enquete.component.ts`

**Modifications à apporter** :

```typescript
import { Component, OnInit, Input } from '@angular/core';
import { EnqueteService } from '../../../services/enquete.service';
import { DossierService } from '../../../services/dossier.service';
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
    private dossierService: DossierService
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
    
    const chefId = this.getCurrentChefId(); // À implémenter selon votre auth
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
        if (dossier.etatPrediction && dossier.riskScore !== undefined) {
          this.prediction = {
            etatFinal: dossier.etatPrediction,
            riskScore: dossier.riskScore,
            riskLevel: dossier.riskLevel || 'Moyen'
          };
        }
        
        this.loadingPrediction = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement du dossier:', error);
        this.loadingPrediction = false;
      }
    });
  }

  private getCurrentChefId(): number | null {
    // À implémenter selon votre système d'authentification
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    return user.id || null;
  }
}
```

**Template HTML** (`validation-enquete.component.html`) :

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
        ></app-ia-prediction-badge>
        
        <!-- Message si pas de prédiction -->
        <div class="alert alert-warning" *ngIf="!prediction && !loadingPrediction">
          <i class="material-icons">info</i>
          La prédiction IA sera disponible après la validation de l'enquête.
        </div>
        
        <!-- Informations supplémentaires -->
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

## 🎯 Prompt 3 : Modifier le Composant Liste des Enquêtes

### Fichier : `src/app/components/enquete/enquete-list/enquete-list.component.ts`

**Modifications à apporter** :

```typescript
import { Component, OnInit } from '@angular/core';
import { EnqueteService } from '../../../services/enquete.service';
import { DossierService } from '../../../services/dossier.service';
import { Enquette } from '../../../models/enquette.model';
import { IaPredictionResult } from '../../../models/ia-prediction-result.model';

@Component({
  selector: 'app-enquete-list',
  templateUrl: './enquete-list.component.html',
  styleUrls: ['./enquete-list.component.css']
})
export class EnqueteListComponent implements OnInit {
  enquetes: Enquette[] = [];
  predictions: Map<number, IaPredictionResult> = new Map(); // Prédictions par dossierId

  constructor(
    private enqueteService: EnqueteService,
    private dossierService: DossierService
  ) {}

  ngOnInit(): void {
    this.loadEnquetes();
  }

  loadEnquetes(): void {
    this.enqueteService.getAllEnquetes().subscribe({
      next: (enquetes) => {
        this.enquetes = enquetes;
        // Charger les prédictions pour les enquêtes validées
        this.loadPredictionsForValidatedEnquetes();
      },
      error: (error) => console.error('Erreur:', error)
    });
  }

  /**
   * Charger les prédictions IA pour les enquêtes validées
   */
  loadPredictionsForValidatedEnquetes(): void {
    this.enquetes.forEach(enquette => {
      if (enquette.statut === 'VALIDE' && enquette.dossierId) {
        this.loadPrediction(enquette.dossierId);
      }
    });
  }

  /**
   * Charger la prédiction IA pour un dossier
   */
  loadPrediction(dossierId: number): void {
    this.dossierService.getDossierById(dossierId).subscribe({
      next: (dossier) => {
        if (dossier.etatPrediction && dossier.riskScore !== undefined) {
          this.predictions.set(dossierId, {
            etatFinal: dossier.etatPrediction,
            riskScore: dossier.riskScore,
            riskLevel: dossier.riskLevel || 'Moyen'
          });
        }
      },
      error: (error) => {
        console.error(`Erreur lors du chargement de la prédiction pour le dossier ${dossierId}:`, error);
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
   * Valider une enquête depuis la liste
   */
  validerEnquette(enquette: Enquette): void {
    const chefId = this.getCurrentChefId();
    if (!chefId) {
      alert('Erreur : Chef non identifié');
      return;
    }

    this.enqueteService.validerEnquette(enquette.id, chefId).subscribe({
      next: (enquetteValidee) => {
        // Mettre à jour la liste
        const index = this.enquetes.findIndex(e => e.id === enquette.id);
        if (index !== -1) {
          this.enquetes[index] = enquetteValidee;
        }
        
        // Charger la prédiction IA
        if (enquetteValidee.dossierId) {
          this.loadPrediction(enquetteValidee.dossierId);
        }
        
        alert('Enquête validée ! La prédiction IA a été calculée automatiquement.');
      },
      error: (error) => {
        console.error('Erreur:', error);
        alert('Erreur lors de la validation');
      }
    });
  }

  private getCurrentChefId(): number | null {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    return user.id || null;
  }
}
```

**Template HTML** (`enquete-list.component.html`) :

```html
<table class="table table-striped">
  <thead>
    <tr>
      <th>Rapport Code</th>
      <th>Dossier</th>
      <th>Statut</th>
      <th>Prédiction IA</th>  <!-- NOUVEAU -->
      <th>Date Création</th>
      <th>Actions</th>
    </tr>
  </thead>
  <tbody>
    <tr *ngFor="let enquette of enquetes">
      <td>{{enquette.rapportCode}}</td>
      <td>{{enquette.dossierId}}</td>
      <td>
        <span class="badge badge-{{enquette.statut === 'VALIDE' ? 'success' : 'warning'}}">
          {{enquette.statut}}
        </span>
      </td>
      
      <!-- Badge de prédiction IA -->
      <td>
        <app-ia-prediction-badge
          *ngIf="enquette.statut === 'VALIDE' && enquette.dossierId"
          [prediction]="getPrediction(enquette.dossierId)"
        ></app-ia-prediction-badge>
        <span *ngIf="enquette.statut !== 'VALIDE'" class="text-muted">
          En attente de validation
        </span>
      </td>
      
      <td>{{enquette.dateCreation | date:'short'}}</td>
      <td>
        <button 
          class="btn btn-sm btn-success" 
          *ngIf="enquette.statut !== 'VALIDE'"
          (click)="validerEnquette(enquette)">
          Valider
        </button>
        <button class="btn btn-sm btn-primary" (click)="viewEnquette(enquette.id)">
          Voir Détails
        </button>
      </td>
    </tr>
  </tbody>
</table>
```

---

## 🎯 Prompt 4 : Modifier le Composant Détails de l'Enquête

### Fichier : `src/app/components/enquete/enquete-detail/enquete-detail.component.ts`

**Modifications similaires au composant de validation** :

```typescript
export class EnqueteDetailComponent implements OnInit {
  enquette: Enquette | null = null;
  dossier: Dossier | null = null;
  prediction: IaPredictionResult | null = null;

  constructor(
    private route: ActivatedRoute,
    private enqueteService: EnqueteService,
    private dossierService: DossierService
  ) {}

  ngOnInit(): void {
    const enquetteId = this.route.snapshot.params['id'];
    this.loadEnquette(enquetteId);
  }

  loadEnquette(enquetteId: number): void {
    this.enqueteService.getEnquetteById(enquetteId).subscribe({
      next: (enquette) => {
        this.enquette = enquette;
        
        // Si l'enquête est validée, charger le dossier avec la prédiction IA
        if (enquette.statut === 'VALIDE' && enquette.dossierId) {
          this.loadDossier(enquette.dossierId);
        }
      }
    });
  }

  loadDossier(dossierId: number): void {
    this.dossierService.getDossierById(dossierId).subscribe({
      next: (dossier) => {
        this.dossier = dossier;
        
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

## 🎯 Prompt 5 : Ajouter Notification après Validation

### Fichier : `src/app/components/enquete/validation-enquete/validation-enquete.component.ts`

**Ajouter une notification toast** :

```typescript
import { ToastrService } from 'ngx-toastr'; // Ou votre service de notification

constructor(
  private enqueteService: EnqueteService,
  private dossierService: DossierService,
  private toastr: ToastrService  // Ajouter
) {}

validerEnquette(): void {
  // ... code existant ...
  
  this.enqueteService.validerEnquette(this.enquette.id, chefId).subscribe({
    next: (enquetteValidee) => {
      this.enquette = enquetteValidee;
      this.loadingValidation = false;
      
      if (enquetteValidee.dossierId) {
        this.loadDossier();
      }
      
      // ✅ NOUVEAU : Notification de succès avec info sur la prédiction IA
      this.toastr.success(
        'Enquête validée avec succès ! La prédiction IA a été calculée automatiquement.',
        'Validation réussie',
        {
          timeOut: 5000,
          closeButton: true
        }
      );
    },
    error: (error) => {
      this.toastr.error('Erreur lors de la validation de l\'enquête', 'Erreur');
    }
  });
}
```

---

## 📋 Résumé des Modifications Frontend

### Fichiers à Modifier :

1. ✅ `enquete.service.ts` - Ajouter logique de rafraîchissement après validation
2. ✅ `validation-enquete.component.ts` - Afficher la prédiction IA après validation
3. ✅ `enquete-list.component.ts` - Afficher badges dans la liste
4. ✅ `enquete-detail.component.ts` - Section prédiction IA dans les détails
5. ✅ Ajouter notifications toast (optionnel)

### Fonctionnalités Ajoutées :

- ✅ **Détection automatique** : Le frontend détecte que la prédiction IA a été calculée
- ✅ **Affichage immédiat** : La prédiction s'affiche après validation
- ✅ **Badges visuels** : Utilisation du composant `IaPredictionBadgeComponent`
- ✅ **Notifications** : Informer l'utilisateur que la prédiction a été calculée

---

## ✅ Checklist d'Implémentation

- [ ] Modifier `enquete.service.ts` pour rafraîchir le dossier après validation
- [ ] Modifier `validation-enquete.component.ts` pour afficher la prédiction
- [ ] Modifier `enquete-list.component.ts` pour afficher les badges
- [ ] Modifier `enquete-detail.component.ts` pour la section prédiction
- [ ] Ajouter notifications toast (optionnel)
- [ ] Tester le workflow complet
- [ ] Vérifier que la prédiction s'affiche correctement

---

## 🎨 Exemple de Workflow Utilisateur

### Scénario : Chef valide une enquête

1. **Chef ouvre la page de validation**
   - Voit les détails de l'enquête
   - Voit le bouton "Valider l'Enquête"

2. **Chef clique sur "Valider"**
   - Le bouton affiche un spinner
   - Requête envoyée au backend

3. **Backend valide l'enquête**
   - Statut → VALIDE
   - Déclenche automatiquement la prédiction IA
   - Met à jour le dossier avec les résultats

4. **Frontend reçoit la réponse**
   - Enquête marquée comme validée
   - Rafraîchit le dossier
   - Affiche la prédiction IA automatiquement

5. **Utilisateur voit**
   - ✅ Badge "Enquête Validée"
   - ✅ Badge de prédiction IA avec score et niveau de risque
   - ✅ Message de confirmation

---

## ✨ Conclusion

Ces prompts permettent d'intégrer l'affichage de la prédiction IA dans toutes les interfaces liées à la validation d'enquête. La prédiction est calculée automatiquement par le backend et affichée immédiatement dans le frontend.

**Date de création** : 2025-12-02  
**Statut** : ✅ Prêt pour implémentation

