# 📊 Guide Frontend : Traçabilité des Montants Recouvrés par Phase

## 🎯 Objectif

Ce guide fournit toutes les informations nécessaires pour intégrer la traçabilité des montants recouvrés par phase dans le frontend Angular.

---

## 📋 Table des Matières

1. [APIs Disponibles](#apis-disponibles)
2. [Modifications des Modèles](#modifications-des-modèles)
3. [Interface de Finalisation Juridique](#interface-de-finalisation-juridique)
4. [Interface de Finalisation Amiable](#interface-de-finalisation-amiable)
5. [Affichage des Montants par Phase](#affichage-des-montants-par-phase)
6. [Historique des Recouvrements](#historique-des-recouvements)
7. [Statistiques par Phase](#statistiques-par-phase)

---

## 🔌 APIs Disponibles

### 1. Finalisation Juridique

**Endpoint :** `PUT /api/dossiers/{dossierId}/juridique/finaliser`

**Headers :**
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Body :**
```json
{
  "etatFinal": "RECOUVREMENT_TOTAL" | "RECOUVREMENT_PARTIEL" | "NON_RECOUVRE",
  "montantRecouvre": 149000.05
}
```

**Réponse 200 OK :**
```json
{
  "id": 4,
  "numeroDossier": "D23",
  "montantCreance": 230000.00,
  "montantTotal": 230000.00,
  "montantRecouvre": 230000.00,
  "montantRecouvrePhaseAmiable": 81000.00,
  "montantRecouvrePhaseJuridique": 149000.00,
  "montantRestant": 0.00,
  "etatDossier": "RECOVERED_TOTAL",
  "dossierStatus": "CLOTURE",
  "dateCloture": "2025-12-05T10:30:00",
  ...
}
```

---

### 2. Finalisation Amiable

**Endpoint :** `PUT /api/dossiers/{dossierId}/amiable/finaliser`

**Headers :**
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Body :**
```json
{
  "etatFinal": "RECOUVREMENT_TOTAL" | "RECOUVREMENT_PARTIEL" | "NON_RECOUVRE",
  "montantRecouvre": 50000.00
}
```

**Réponse 200 OK :** (Même format que finalisation juridique)

---

### 3. Historique des Recouvrements

**Endpoint :** `GET /api/historique-recouvrement/dossier/{dossierId}`

**Headers :**
```
Authorization: Bearer {token}
```

**Réponse 200 OK :**
```json
[
  {
    "id": 1,
    "dossierId": 4,
    "phase": "JURIDIQUE",
    "montantRecouvre": 149000.05,
    "montantTotalRecouvre": 230000.05,
    "montantRestant": 0.00,
    "typeAction": "FINALISATION_JURIDIQUE",
    "actionId": null,
    "utilisateurId": 1,
    "dateEnregistrement": "2025-12-05T10:30:00",
    "commentaire": "Finalisation juridique - RECOUVREMENT_TOTAL"
  },
  {
    "id": 2,
    "dossierId": 4,
    "phase": "AMIABLE",
    "montantRecouvre": 81000.00,
    "montantTotalRecouvre": 81000.00,
    "montantRestant": 149000.00,
    "typeAction": "ACTION_AMIABLE",
    "actionId": 5,
    "utilisateurId": 2,
    "dateEnregistrement": "2025-11-15T14:20:00",
    "commentaire": "Recouvrement suite à action amiable"
  }
]
```

---

### 4. Résumé des Montants par Phase

**Endpoint :** `GET /api/historique-recouvrement/dossier/{dossierId}/resume`

**Headers :**
```
Authorization: Bearer {token}
```

**Réponse 200 OK :**
```json
{
  "dossierId": 4,
  "montantRecouvrePhaseAmiable": 81000.00,
  "montantRecouvrePhaseJuridique": 149000.00,
  "montantRecouvreTotal": 230000.00,
  "nombreOperationsAmiable": 3,
  "nombreOperationsJuridique": 2,
  "dernierEnregistrement": {
    "date": "2025-12-05T10:30:00",
    "montant": 149000.05,
    "phase": "JURIDIQUE",
    "typeAction": "FINALISATION_JURIDIQUE"
  }
}
```

---

### 5. Statistiques de Recouvrement par Phase

**Endpoint :** `GET /api/statistiques/recouvrement-par-phase`

**Headers :**
```
Authorization: Bearer {token}
```

**Réponse 200 OK :**
```json
{
  "montantRecouvrePhaseAmiable": 500000.00,
  "montantRecouvrePhaseJuridique": 300000.00,
  "montantRecouvreTotal": 800000.00,
  "dossiersAvecRecouvrementAmiable": 25,
  "dossiersAvecRecouvrementJuridique": 15,
  "tauxRecouvrementAmiable": 45.5,
  "tauxRecouvrementJuridique": 27.3,
  "tauxRecouvrementTotal": 72.8,
  "montantTotalCreances": 1100000.00
}
```

---

### 6. Statistiques par Phase pour un Département

**Endpoint :** `GET /api/statistiques/recouvrement-par-phase/departement`

**Headers :**
```
Authorization: Bearer {token}
```

**Réponse 200 OK :** (Même format que ci-dessus, mais filtré par département)

---

## 📝 Modifications des Modèles

### 1. Modèle Dossier

**Fichier :** `src/app/models/dossier.model.ts`

```typescript
export interface Dossier {
  id: number;
  numeroDossier: string;
  titre: string;
  description?: string;
  montantCreance?: number;
  montantTotal?: number;
  montantRecouvre?: number;
  
  // ✅ NOUVEAU : Montants par phase
  montantRecouvrePhaseAmiable?: number;
  montantRecouvrePhaseJuridique?: number;
  montantRestant?: number;
  etatDossier?: 'RECOVERED_TOTAL' | 'RECOVERED_PARTIAL' | 'NOT_RECOVERED';
  
  dateCreation?: Date;
  dateCloture?: Date;
  dossierStatus?: string;
  statut?: string;
  // ... autres champs
}
```

---

### 2. Modèle HistoriqueRecouvrement

**Fichier :** `src/app/models/historique-recouvrement.model.ts` (NOUVEAU)

```typescript
export interface HistoriqueRecouvrement {
  id: number;
  dossierId: number;
  phase: 'AMIABLE' | 'JURIDIQUE';
  montantRecouvre: number;
  montantTotalRecouvre: number;
  montantRestant: number;
  typeAction: 'ACTION_AMIABLE' | 'ACTION_HUISSIER' | 'FINALISATION_AMIABLE' | 'FINALISATION_JURIDIQUE';
  actionId?: number;
  utilisateurId?: number;
  dateEnregistrement: Date;
  commentaire?: string;
}

export interface ResumeRecouvrement {
  dossierId: number;
  montantRecouvrePhaseAmiable: number;
  montantRecouvrePhaseJuridique: number;
  montantRecouvreTotal: number;
  nombreOperationsAmiable: number;
  nombreOperationsJuridique: number;
  dernierEnregistrement?: {
    date: Date;
    montant: number;
    phase: string;
    typeAction: string;
  };
}
```

---

### 3. Modèle FinalisationDossierDTO

**Fichier :** `src/app/models/finalisation-dossier.model.ts` (NOUVEAU)

```typescript
export interface FinalisationDossierDTO {
  etatFinal: 'RECOUVREMENT_TOTAL' | 'RECOUVREMENT_PARTIEL' | 'NON_RECOUVRE';
  montantRecouvre: number;
}
```

---

## 🎨 Interface de Finalisation Juridique

### Service Angular

**Fichier :** `src/app/services/dossier.service.ts`

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Dossier } from '../models/dossier.model';
import { FinalisationDossierDTO } from '../models/finalisation-dossier.model';

@Injectable({
  providedIn: 'root'
})
export class DossierService {
  private apiUrl = 'http://localhost:8089/carthage-creance/api';

  constructor(private http: HttpClient) {}

  /**
   * Finalise un dossier juridique
   */
  finaliserDossierJuridique(
    dossierId: number, 
    dto: FinalisationDossierDTO
  ): Observable<Dossier> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
    
    return this.http.put<Dossier>(
      `${this.apiUrl}/dossiers/${dossierId}/juridique/finaliser`,
      dto,
      { headers }
    );
  }

  /**
   * Finalise un dossier amiable
   */
  finaliserDossierAmiable(
    dossierId: number, 
    dto: FinalisationDossierDTO
  ): Observable<Dossier> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
    
    return this.http.put<Dossier>(
      `${this.apiUrl}/dossiers/${dossierId}/amiable/finaliser`,
      dto,
      { headers }
    );
  }
}
```

---

### Composant Angular - Modal de Finalisation

**Fichier :** `src/app/components/juridique/finaliser-dossier-modal.component.ts`

```typescript
import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { DossierService } from '../../services/dossier.service';
import { Dossier } from '../../models/dossier.model';
import { FinalisationDossierDTO } from '../../models/finalisation-dossier.model';

@Component({
  selector: 'app-finaliser-dossier-modal',
  templateUrl: './finaliser-dossier-modal.component.html'
})
export class FinaliserDossierModalComponent implements OnInit {
  @Input() dossier!: Dossier;
  @Output() dossierFinalise = new EventEmitter<Dossier>();
  @Output() annuler = new EventEmitter<void>();

  etatFinal: 'RECOUVREMENT_TOTAL' | 'RECOUVREMENT_PARTIEL' | 'NON_RECOUVRE' = 'RECOUVREMENT_TOTAL';
  montantRecouvre: number = 0;
  
  // Calculs pour l'affichage
  montantTotal: number = 0;
  montantDejaRecouvre: number = 0;
  montantRestantAvant: number = 0;
  montantRestantApres: number = 0;
  
  loading: boolean = false;
  error: string | null = null;

  constructor(private dossierService: DossierService) {}

  ngOnInit() {
    this.initializeMontants();
  }

  initializeMontants() {
    this.montantTotal = this.dossier.montantCreance || 0;
    this.montantDejaRecouvre = this.dossier.montantRecouvre || 0;
    
    // ✅ NOUVEAU : Calculer le montant restant AVANT finalisation
    // Montant restant = montant créance - montant déjà recouvré (amiable + juridique)
    this.montantRestantAvant = this.montantTotal - this.montantDejaRecouvre;
    
    // Par défaut, si RECOUVREMENT_TOTAL, le montant recouvré = montant restant
    if (this.etatFinal === 'RECOUVREMENT_TOTAL') {
      this.montantRecouvre = this.montantRestantAvant;
    }
    
    this.calculerMontantRestantApres();
  }

  onEtatFinalChange() {
    if (this.etatFinal === 'RECOUVREMENT_TOTAL') {
      // Si recouvrement total, le montant recouvré = montant restant
      this.montantRecouvre = this.montantRestantAvant;
    } else if (this.etatFinal === 'NON_RECOUVRE') {
      // Si non recouvré, montant = 0
      this.montantRecouvre = 0;
    }
    // Pour RECOUVREMENT_PARTIEL, l'utilisateur saisit le montant
    
    this.calculerMontantRestantApres();
  }

  onMontantRecouvreChange() {
    this.calculerMontantRestantApres();
  }

  calculerMontantRestantApres() {
    const montantTotalRecouvreApres = this.montantDejaRecouvre + this.montantRecouvre;
    this.montantRestantApres = this.montantTotal - montantTotalRecouvreApres;
    
    // Validation
    if (this.montantRestantApres < 0) {
      this.montantRestantApres = 0;
      // Ajuster le montant recouvré si nécessaire
      this.montantRecouvre = this.montantRestantAvant;
    }
  }

  valider() {
    // Validation
    if (this.montantRecouvre < 0) {
      this.error = 'Le montant recouvré ne peut pas être négatif';
      return;
    }

    if (this.etatFinal === 'RECOUVREMENT_TOTAL') {
      // Vérifier que le montant total recouvré = montant créance (tolérance 0.01)
      const montantTotalRecouvre = this.montantDejaRecouvre + this.montantRecouvre;
      const difference = Math.abs(montantTotalRecouvre - this.montantTotal);
      if (difference > 0.01) {
        this.error = `Pour RECOUVREMENT_TOTAL, le montant total recouvré (${montantTotalRecouvre.toFixed(2)}) doit être égal au montant créance (${this.montantTotal.toFixed(2)})`;
        return;
      }
    } else if (this.etatFinal === 'RECOUVREMENT_PARTIEL') {
      const montantTotalRecouvre = this.montantDejaRecouvre + this.montantRecouvre;
      if (montantTotalRecouvre <= 0) {
        this.error = 'Pour RECOUVREMENT_PARTIEL, le montant recouvré doit être supérieur à 0';
        return;
      }
      if (montantTotalRecouvre >= this.montantTotal) {
        this.error = `Pour RECOUVREMENT_PARTIEL, le montant total recouvré (${montantTotalRecouvre.toFixed(2)}) doit être inférieur au montant créance (${this.montantTotal.toFixed(2)})`;
        return;
      }
    }

    this.loading = true;
    this.error = null;

    const dto: FinalisationDossierDTO = {
      etatFinal: this.etatFinal,
      montantRecouvre: this.montantRecouvre
    };

    this.dossierService.finaliserDossierJuridique(this.dossier.id, dto).subscribe({
      next: (dossier) => {
        this.loading = false;
        this.dossierFinalise.emit(dossier);
      },
      error: (error) => {
        this.loading = false;
        this.error = error.error?.error || 'Erreur lors de la finalisation du dossier';
      }
    });
  }

  annulerModal() {
    this.annuler.emit();
  }
}
```

---

### Template HTML - Modal de Finalisation

**Fichier :** `src/app/components/juridique/finaliser-dossier-modal.component.html`

```html
<div class="modal-overlay" (click)="annulerModal()">
  <div class="modal-content" (click)="$event.stopPropagation()">
    <div class="modal-header">
      <h2>État Final du Dossier</h2>
      <button class="close-btn" (click)="annulerModal()">×</button>
    </div>

    <div class="modal-body">
      <p class="instruction">
        Sélectionnez l'état final du dossier après les audiences:
      </p>

      <!-- Options d'état final -->
      <div class="etat-options">
        <button 
          class="etat-option" 
          [class.selected]="etatFinal === 'RECOUVREMENT_TOTAL'"
          (click)="etatFinal = 'RECOUVREMENT_TOTAL'; onEtatFinalChange()">
          <div class="etat-icon">✓</div>
          <div class="etat-label">Recouvrement Total</div>
        </button>

        <button 
          class="etat-option" 
          [class.selected]="etatFinal === 'RECOUVREMENT_PARTIEL'"
          (click)="etatFinal = 'RECOUVREMENT_PARTIEL'; onEtatFinalChange()">
          <div class="etat-icon">%</div>
          <div class="etat-label">Recouvrement Partiel</div>
        </button>

        <button 
          class="etat-option" 
          [class.selected]="etatFinal === 'NON_RECOUVRE'"
          (click)="etatFinal = 'NON_RECOUVRE'; onEtatFinalChange()">
          <div class="etat-icon">✗</div>
          <div class="etat-label">Non Recouvré</div>
        </button>
      </div>

      <div class="etat-selected">
        État sélectionné: 
        <strong>{{ etatFinal === 'RECOUVREMENT_TOTAL' ? 'Recouvrement Total' : 
                   etatFinal === 'RECOUVREMENT_PARTIEL' ? 'Recouvrement Partiel' : 
                   'Non Recouvré' }}</strong>
      </div>

      <!-- Section Montant Recouvré -->
      <div class="montant-section">
        <p class="instruction">
          Indiquez le montant recouvré dans cette étape juridique :
        </p>

        <div class="form-group">
          <label>Montant Recouvré (TND)</label>
          <input 
            type="number" 
            [(ngModel)]="montantRecouvre" 
            (input)="onMontantRecouvreChange()"
            [disabled]="etatFinal === 'RECOUVREMENT_TOTAL'"
            class="form-control"
            step="0.01"
            min="0">
        </div>

        <!-- ✅ NOUVEAU : Résumé des montants -->
        <div class="montant-summary">
          <div class="summary-item">
            <span class="label">Montant total:</span>
            <span class="value">{{ montantTotal | number:'1.2-2' }} TND</span>
          </div>
          
          <div class="summary-item">
            <span class="label">Montant déjà recouvré (amiable):</span>
            <span class="value">{{ dossier.montantRecouvrePhaseAmiable || 0 | number:'1.2-2' }} TND</span>
          </div>
          
          <div class="summary-item">
            <span class="label">Montant déjà recouvré (juridique):</span>
            <span class="value">{{ dossier.montantRecouvrePhaseJuridique || 0 | number:'1.2-2' }} TND</span>
          </div>
          
          <div class="summary-item">
            <span class="label">Montant total déjà recouvré:</span>
            <span class="value">{{ montantDejaRecouvre | number:'1.2-2' }} TND</span>
          </div>
          
          <div class="summary-item">
            <span class="label">Montant restant (avant finalisation):</span>
            <span class="value">{{ montantRestantAvant | number:'1.2-2' }} TND</span>
          </div>
          
          <div class="summary-item highlight" [class.success]="montantRestantApres === 0">
            <span class="label">Montant restant (après finalisation):</span>
            <span class="value">
              {{ montantRestantApres | number:'1.2-2' }} TND
              <span *ngIf="montantRestantApres === 0" class="checkmark">✓ Dossier recouvré totalement</span>
            </span>
          </div>
        </div>
      </div>

      <!-- Message d'erreur -->
      <div *ngIf="error" class="error-message">
        {{ error }}
      </div>
    </div>

    <div class="modal-footer">
      <button class="btn-cancel" (click)="annulerModal()">× Annuler</button>
      <button 
        class="btn-confirm" 
        (click)="valider()"
        [disabled]="loading">
        <span *ngIf="!loading">✓ Finaliser le Dossier</span>
        <span *ngIf="loading">Traitement...</span>
      </button>
    </div>
  </div>
</div>
```

---

## 📊 Affichage des Montants par Phase

### Composant - Détails Dossier

**Fichier :** `src/app/components/dossier/dossier-details.component.html`

```html
<div class="dossier-details">
  <!-- ... autres informations ... -->
  
  <!-- ✅ NOUVEAU : Section Montants par Phase -->
  <div class="montants-section">
    <h3>Montants Recouvrés par Phase</h3>
    
    <div class="montants-grid">
      <div class="montant-card">
        <div class="montant-label">Montant Créance</div>
        <div class="montant-value">{{ dossier.montantCreance | number:'1.2-2' }} TND</div>
      </div>
      
      <div class="montant-card phase-amiable">
        <div class="montant-label">Phase Amiable</div>
        <div class="montant-value">{{ dossier.montantRecouvrePhaseAmiable || 0 | number:'1.2-2' }} TND</div>
        <div class="montant-percentage">
          {{ (dossier.montantRecouvrePhaseAmiable || 0) / (dossier.montantCreance || 1) * 100 | number:'1.1-1' }}%
        </div>
      </div>
      
      <div class="montant-card phase-juridique">
        <div class="montant-label">Phase Juridique</div>
        <div class="montant-value">{{ dossier.montantRecouvrePhaseJuridique || 0 | number:'1.2-2' }} TND</div>
        <div class="montant-percentage">
          {{ (dossier.montantRecouvrePhaseJuridique || 0) / (dossier.montantCreance || 1) * 100 | number:'1.1-1' }}%
        </div>
      </div>
      
      <div class="montant-card total">
        <div class="montant-label">Total Recouvré</div>
        <div class="montant-value">{{ dossier.montantRecouvre || 0 | number:'1.2-2' }} TND</div>
        <div class="montant-percentage">
          {{ (dossier.montantRecouvre || 0) / (dossier.montantCreance || 1) * 100 | number:'1.1-1' }}%
        </div>
      </div>
      
      <div class="montant-card restant">
        <div class="montant-label">Montant Restant</div>
        <div class="montant-value">{{ dossier.montantRestant || 0 | number:'1.2-2' }} TND</div>
      </div>
    </div>
    
    <!-- Barre de progression -->
    <div class="progress-bar-container">
      <div class="progress-bar">
        <div 
          class="progress-fill phase-amiable" 
          [style.width.%]="(dossier.montantRecouvrePhaseAmiable || 0) / (dossier.montantCreance || 1) * 100">
        </div>
        <div 
          class="progress-fill phase-juridique" 
          [style.width.%]="(dossier.montantRecouvrePhaseJuridique || 0) / (dossier.montantCreance || 1) * 100">
        </div>
      </div>
      <div class="progress-labels">
        <span>Amiable: {{ (dossier.montantRecouvrePhaseAmiable || 0) / (dossier.montantCreance || 1) * 100 | number:'1.1-1' }}%</span>
        <span>Juridique: {{ (dossier.montantRecouvrePhaseJuridique || 0) / (dossier.montantCreance || 1) * 100 | number:'1.1-1' }}%</span>
      </div>
    </div>
  </div>
</div>
```

---

## 📜 Historique des Recouvrements

### Service Angular

**Fichier :** `src/app/services/historique-recouvrement.service.ts` (NOUVEAU)

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { HistoriqueRecouvrement, ResumeRecouvrement } from '../models/historique-recouvrement.model';

@Injectable({
  providedIn: 'root'
})
export class HistoriqueRecouvrementService {
  private apiUrl = 'http://localhost:8089/carthage-creance/api';

  constructor(private http: HttpClient) {}

  /**
   * Récupère l'historique complet d'un dossier
   */
  getHistoriqueByDossier(dossierId: number): Observable<HistoriqueRecouvrement[]> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    
    return this.http.get<HistoriqueRecouvrement[]>(
      `${this.apiUrl}/historique-recouvrement/dossier/${dossierId}`,
      { headers }
    );
  }

  /**
   * Récupère l'historique d'un dossier par phase
   */
  getHistoriqueByDossierAndPhase(
    dossierId: number, 
    phase: 'AMIABLE' | 'JURIDIQUE'
  ): Observable<HistoriqueRecouvrement[]> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    
    return this.http.get<HistoriqueRecouvrement[]>(
      `${this.apiUrl}/historique-recouvrement/dossier/${dossierId}/phase/${phase}`,
      { headers }
    );
  }

  /**
   * Récupère le résumé des montants par phase
   */
  getResumeByDossier(dossierId: number): Observable<ResumeRecouvrement> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    
    return this.http.get<ResumeRecouvrement>(
      `${this.apiUrl}/historique-recouvrement/dossier/${dossierId}/resume`,
      { headers }
    );
  }
}
```

---

### Composant - Historique des Recouvrements

**Fichier :** `src/app/components/dossier/historique-recouvrement.component.ts`

```typescript
import { Component, Input, OnInit } from '@angular/core';
import { HistoriqueRecouvrementService } from '../../services/historique-recouvrement.service';
import { HistoriqueRecouvrement, ResumeRecouvrement } from '../../models/historique-recouvrement.model';

@Component({
  selector: 'app-historique-recouvrement',
  templateUrl: './historique-recouvrement.component.html'
})
export class HistoriqueRecouvrementComponent implements OnInit {
  @Input() dossierId!: number;

  historique: HistoriqueRecouvrement[] = [];
  resume: ResumeRecouvrement | null = null;
  loading: boolean = false;
  error: string | null = null;

  constructor(private historiqueService: HistoriqueRecouvrementService) {}

  ngOnInit() {
    this.loadHistorique();
    this.loadResume();
  }

  loadHistorique() {
    this.loading = true;
    this.historiqueService.getHistoriqueByDossier(this.dossierId).subscribe({
      next: (data) => {
        this.historique = data;
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Erreur lors du chargement de l\'historique';
        this.loading = false;
      }
    });
  }

  loadResume() {
    this.historiqueService.getResumeByDossier(this.dossierId).subscribe({
      next: (data) => {
        this.resume = data;
      },
      error: (error) => {
        console.error('Erreur lors du chargement du résumé:', error);
      }
    });
  }

  getTypeActionLabel(type: string): string {
    const labels: { [key: string]: string } = {
      'ACTION_AMIABLE': 'Action Amiable',
      'ACTION_HUISSIER': 'Action Huissier',
      'FINALISATION_AMIABLE': 'Finalisation Amiable',
      'FINALISATION_JURIDIQUE': 'Finalisation Juridique'
    };
    return labels[type] || type;
  }
}
```

---

### Template HTML - Historique

**Fichier :** `src/app/components/dossier/historique-recouvrement.component.html`

```html
<div class="historique-container">
  <h3>Historique des Recouvrements</h3>

  <!-- Résumé -->
  <div *ngIf="resume" class="resume-section">
    <div class="resume-card">
      <div class="resume-item">
        <span class="label">Phase Amiable:</span>
        <span class="value">{{ resume.montantRecouvrePhaseAmiable | number:'1.2-2' }} TND</span>
        <span class="count">({{ resume.nombreOperationsAmiable }} opérations)</span>
      </div>
      <div class="resume-item">
        <span class="label">Phase Juridique:</span>
        <span class="value">{{ resume.montantRecouvrePhaseJuridique | number:'1.2-2' }} TND</span>
        <span class="count">({{ resume.nombreOperationsJuridique }} opérations)</span>
      </div>
      <div class="resume-item total">
        <span class="label">Total Recouvré:</span>
        <span class="value">{{ resume.montantRecouvreTotal | number:'1.2-2' }} TND</span>
      </div>
    </div>
  </div>

  <!-- Liste de l'historique -->
  <div class="historique-list">
    <div *ngFor="let item of historique" class="historique-item" [class.phase-amiable]="item.phase === 'AMIABLE'" [class.phase-juridique]="item.phase === 'JURIDIQUE'">
      <div class="historique-header">
        <div class="phase-badge" [class.amiable]="item.phase === 'AMIABLE'" [class.juridique]="item.phase === 'JURIDIQUE'">
          {{ item.phase }}
        </div>
        <div class="date">{{ item.dateEnregistrement | date:'dd/MM/yyyy HH:mm' }}</div>
      </div>
      
      <div class="historique-body">
        <div class="type-action">{{ getTypeActionLabel(item.typeAction) }}</div>
        <div class="montants">
          <div class="montant-item">
            <span class="label">Montant recouvré:</span>
            <span class="value">{{ item.montantRecouvre | number:'1.2-2' }} TND</span>
          </div>
          <div class="montant-item">
            <span class="label">Total recouvré:</span>
            <span class="value">{{ item.montantTotalRecouvre | number:'1.2-2' }} TND</span>
          </div>
          <div class="montant-item">
            <span class="label">Montant restant:</span>
            <span class="value">{{ item.montantRestant | number:'1.2-2' }} TND</span>
          </div>
        </div>
        <div *ngIf="item.commentaire" class="commentaire">
          {{ item.commentaire }}
        </div>
      </div>
    </div>

    <div *ngIf="historique.length === 0 && !loading" class="no-data">
      Aucun historique disponible
    </div>

    <div *ngIf="loading" class="loading">
      Chargement...
    </div>
  </div>
</div>
```

---

## 📈 Statistiques par Phase

### Service Angular

**Fichier :** `src/app/services/statistique.service.ts`

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class StatistiqueService {
  private apiUrl = 'http://localhost:8089/carthage-creance/api';

  constructor(private http: HttpClient) {}

  /**
   * ✅ NOUVEAU : Récupère les statistiques de recouvrement par phase
   */
  getStatistiquesRecouvrementParPhase(): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    
    return this.http.get(`${this.apiUrl}/statistiques/recouvrement-par-phase`, { headers });
  }

  /**
   * ✅ NOUVEAU : Récupère les statistiques de recouvrement par phase pour un département
   */
  getStatistiquesRecouvrementParPhaseDepartement(): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    
    return this.http.get(`${this.apiUrl}/statistiques/recouvrement-par-phase/departement`, { headers });
  }
}
```

---

### Composant - Statistiques par Phase

**Fichier :** `src/app/components/statistiques/recouvrement-par-phase.component.ts`

```typescript
import { Component, OnInit } from '@angular/core';
import { StatistiqueService } from '../../services/statistique.service';

@Component({
  selector: 'app-recouvrement-par-phase',
  templateUrl: './recouvrement-par-phase.component.html'
})
export class RecouvrementParPhaseComponent implements OnInit {
  stats: any = {
    montantRecouvrePhaseAmiable: 0,
    montantRecouvrePhaseJuridique: 0,
    montantRecouvreTotal: 0,
    dossiersAvecRecouvrementAmiable: 0,
    dossiersAvecRecouvrementJuridique: 0,
    tauxRecouvrementAmiable: 0,
    tauxRecouvrementJuridique: 0,
    tauxRecouvrementTotal: 0,
    montantTotalCreances: 0
  };

  loading: boolean = false;

  constructor(private statistiqueService: StatistiqueService) {}

  ngOnInit() {
    this.loadStats();
  }

  loadStats() {
    this.loading = true;
    this.statistiqueService.getStatistiquesRecouvrementParPhase().subscribe({
      next: (data) => {
        this.stats = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur:', error);
        this.loading = false;
      }
    });
  }
}
```

---

### Template HTML - Statistiques par Phase

**Fichier :** `src/app/components/statistiques/recouvrement-par-phase.component.html`

```html
<div class="stats-container">
  <h2>Statistiques de Recouvrement par Phase</h2>

  <div class="stats-grid">
    <!-- Phase Amiable -->
    <div class="stat-card phase-amiable">
      <h3>Phase Amiable</h3>
      <div class="stat-value">{{ stats.montantRecouvrePhaseAmiable | number:'1.2-2' }} TND</div>
      <div class="stat-details">
        <div class="detail-item">
          <span class="label">Dossiers:</span>
          <span class="value">{{ stats.dossiersAvecRecouvrementAmiable }}</span>
        </div>
        <div class="detail-item">
          <span class="label">Taux:</span>
          <span class="value">{{ stats.tauxRecouvrementAmiable | number:'1.2-2' }}%</span>
        </div>
      </div>
    </div>

    <!-- Phase Juridique -->
    <div class="stat-card phase-juridique">
      <h3>Phase Juridique</h3>
      <div class="stat-value">{{ stats.montantRecouvrePhaseJuridique | number:'1.2-2' }} TND</div>
      <div class="stat-details">
        <div class="detail-item">
          <span class="label">Dossiers:</span>
          <span class="value">{{ stats.dossiersAvecRecouvrementJuridique }}</span>
        </div>
        <div class="detail-item">
          <span class="label">Taux:</span>
          <span class="value">{{ stats.tauxRecouvrementJuridique | number:'1.2-2' }}%</span>
        </div>
      </div>
    </div>

    <!-- Total -->
    <div class="stat-card total">
      <h3>Total</h3>
      <div class="stat-value">{{ stats.montantRecouvreTotal | number:'1.2-2' }} TND</div>
      <div class="stat-details">
        <div class="detail-item">
          <span class="label">Taux global:</span>
          <span class="value">{{ stats.tauxRecouvrementTotal | number:'1.2-2' }}%</span>
        </div>
        <div class="detail-item">
          <span class="label">Montant créances:</span>
          <span class="value">{{ stats.montantTotalCreances | number:'1.2-2' }} TND</span>
        </div>
      </div>
    </div>
  </div>

  <!-- Graphique de répartition -->
  <div class="chart-container">
    <canvas id="recouvrementChart"></canvas>
  </div>
</div>
```

---

## 🎨 Styles CSS Recommandés

**Fichier :** `src/app/components/juridique/finaliser-dossier-modal.component.css`

```css
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  padding: 24px;
  max-width: 600px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.etat-options {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin: 20px 0;
}

.etat-option {
  padding: 20px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;
}

.etat-option:hover {
  border-color: #007bff;
  transform: translateY(-2px);
}

.etat-option.selected {
  border-color: #28a745;
  background: #d4edda;
}

.etat-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.montant-summary {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  margin-top: 16px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #e0e0e0;
}

.summary-item:last-child {
  border-bottom: none;
}

.summary-item.highlight {
  background: #e8f5e9;
  padding: 12px;
  border-radius: 4px;
  margin-top: 8px;
}

.summary-item.success {
  background: #c8e6c9;
}

.checkmark {
  color: #28a745;
  font-weight: bold;
  margin-left: 8px;
}

.montants-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin: 20px 0;
}

.montant-card {
  padding: 16px;
  border-radius: 8px;
  background: white;
  border: 1px solid #e0e0e0;
}

.montant-card.phase-amiable {
  border-left: 4px solid #2196F3;
}

.montant-card.phase-juridique {
  border-left: 4px solid #FF9800;
}

.montant-card.total {
  border-left: 4px solid #4CAF50;
  background: #f1f8f4;
}

.montant-card.restant {
  border-left: 4px solid #f44336;
}

.montant-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.montant-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.progress-bar-container {
  margin-top: 20px;
}

.progress-bar {
  height: 30px;
  background: #e0e0e0;
  border-radius: 15px;
  overflow: hidden;
  position: relative;
}

.progress-fill {
  height: 100%;
  float: left;
  transition: width 0.3s;
}

.progress-fill.phase-amiable {
  background: #2196F3;
}

.progress-fill.phase-juridique {
  background: #FF9800;
}

.progress-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
  color: #666;
}
```

---

## ✅ Checklist d'Intégration

### Backend
- [x] Champs montantRecouvrePhaseAmiable et montantRecouvrePhaseJuridique ajoutés à Dossier
- [x] Entité HistoriqueRecouvrement créée
- [x] Méthodes updateMontantRecouvrePhaseAmiable et updateMontantRecouvrePhaseJuridique implémentées
- [x] Endpoints de finalisation utilisent les méthodes par phase
- [x] Endpoint d'action amiable utilise updateMontantRecouvrePhaseAmiable
- [x] Endpoint d'action huissier utilise updateMontantRecouvrePhaseJuridique
- [x] Endpoints d'historique créés
- [x] Endpoints de statistiques par phase créés
- [x] DossierResponseDTO inclut les champs par phase

### Frontend
- [ ] Modèle Dossier mis à jour avec les champs par phase
- [ ] Modèle HistoriqueRecouvrement créé
- [ ] Service DossierService avec méthodes de finalisation
- [ ] Service HistoriqueRecouvrementService créé
- [ ] Service StatistiqueService avec méthodes par phase
- [ ] Composant modal de finalisation créé
- [ ] Composant historique des recouvrements créé
- [ ] Composant statistiques par phase créé
- [ ] Affichage des montants par phase dans les détails dossier
- [ ] Styles CSS pour les nouveaux composants

---

## 🚀 Prochaines Étapes

1. **Créer les modèles TypeScript** pour HistoriqueRecouvrement et FinalisationDossierDTO
2. **Créer les services Angular** pour les nouvelles APIs
3. **Créer les composants** pour l'affichage et la finalisation
4. **Intégrer dans les interfaces existantes** (détails dossier, dashboard, etc.)
5. **Tester** avec des données réelles

---

## 📝 Notes Importantes

### Calcul du Montant Restant

Le montant restant est calculé automatiquement par le backend :
```
montantRestant = montantCreance - montantRecouvreTotal
```

Où :
```
montantRecouvreTotal = montantRecouvrePhaseAmiable + montantRecouvrePhaseJuridique
```

### Validation Frontend

Le frontend doit valider que :
- Le montant recouvré ne dépasse pas le montant restant
- Pour RECOUVREMENT_TOTAL, le montant total recouvré = montant créance (tolérance 0.01)
- Pour RECOUVREMENT_PARTIEL, 0 < montant total recouvré < montant créance

### Affichage dans le Modal

Le modal doit afficher clairement :
- Montant total (créance)
- Montant déjà recouvré par phase (amiable et juridique)
- Montant restant avant finalisation
- Montant recouvré dans cette étape
- Montant restant après finalisation

---

## 🎯 Résultat Attendu

Après l'implémentation, vous devriez avoir :

1. ✅ **Traçabilité complète** : Chaque recouvrement est enregistré avec sa phase, son type d'action, et son utilisateur
2. ✅ **Affichage clair** : Les montants par phase sont visibles partout où nécessaire
3. ✅ **Historique détaillé** : Possibilité de voir l'historique complet des recouvrements
4. ✅ **Statistiques par phase** : Analyse de performance par phase de recouvrement
5. ✅ **Interface intuitive** : Modal de finalisation avec tous les détails nécessaires

