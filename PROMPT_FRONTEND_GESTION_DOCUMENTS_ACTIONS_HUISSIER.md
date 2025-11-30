# 📋 Prompts pour l'Intégration de la Gestion des Documents et Actions Huissier dans le Frontend

## 🎯 Objectif
Créer une interface indépendante pour gérer les documents et actions de l'huissier AVANT de passer à l'audience, avec un menu dédié dans le sidebar.

---

## 📝 PROMPT 1 : Interfaces TypeScript pour Documents et Actions Huissier

**Créer** : `src/app/models/huissier-document.model.ts`

```typescript
export enum TypeDocumentHuissier {
  PV_MISE_EN_DEMEURE = 'PV_MISE_EN_DEMEURE',           // Procès-verbal de mise en demeure (Phase 1)
  ORDONNANCE_PAIEMENT = 'ORDONNANCE_PAIEMENT',          // Ordonnance de paiement (Phase 2)
  PV_NOTIFICATION_ORDONNANCE = 'PV_NOTIFICATION_ORDONNANCE'   // PV de notification d'ordonnance (Phase 2)
}

export enum StatutDocumentHuissier {
  PENDING = 'PENDING',    // En attente (délai légal non expiré)
  EXPIRED = 'EXPIRED',    // Délai légal expiré
  COMPLETED = 'COMPLETED'   // Complété (action suivante effectuée)
}

export interface DocumentHuissier {
  id?: number;
  dossierId: number;
  typeDocument: TypeDocumentHuissier;
  dateCreation?: string;  // ISO string format
  delaiLegalDays?: number;  // 10 pour PV_MISE_EN_DEMEURE, 20 pour ORDONNANCE_PAIEMENT
  pieceJointeUrl?: string;
  huissierName: string;
  status?: StatutDocumentHuissier;
  notified?: boolean;
}

export interface DocumentHuissierDTO {
  dossierId: number;
  typeDocument: TypeDocumentHuissier;
  huissierName: string;
  pieceJointeUrl?: string;
}
```

**Créer** : `src/app/models/huissier-action.model.ts`

```typescript
export enum TypeActionHuissier {
  ACLA_TA7AFOUDHIA = 'ACLA_TA7AFOUDHIA',   // العقلة التحفظية - Saisie conservatoire
  ACLA_TANFITHIA = 'ACLA_TANFITHIA',     // العقلة التنفيذية - Saisie exécutive
  ACLA_TAW9IFIYA = 'ACLA_TAW9IFIYA',     // العقلة التوقيفية - Saisie de blocage
  ACLA_A9ARYA = 'ACLA_A9ARYA'         // العقلة العقارية - Saisie immobilière
}

export enum EtatDossier {
  EN_COURS = 'EN_COURS',
  CLOTURE = 'CLOTURE',
  SUSPENDU = 'SUSPENDU'
}

export interface ActionHuissier {
  id?: number;
  dossierId: number;
  typeAction: TypeActionHuissier;
  montantRecouvre?: number;
  montantRestant?: number;
  etatDossier?: EtatDossier;
  dateAction?: string;  // ISO string format
  pieceJointeUrl?: string;
  huissierName: string;
}

export interface ActionHuissierDTO {
  dossierId: number;
  typeAction: TypeActionHuissier;
  huissierName: string;
  montantRecouvre?: number;
  montantRestant?: number;
  etatDossier?: string;
  pieceJointeUrl?: string;
  updateMode?: 'ADD' | 'SET';  // Pour la mise à jour des montants
}
```

---

## 📝 PROMPT 2 : Services Angular pour Documents et Actions Huissier

**Créer** : `src/app/services/huissier-document.service.ts`

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DocumentHuissier, DocumentHuissierDTO } from '../models/huissier-document.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class HuissierDocumentService {
  private apiUrl = `${environment.apiUrl}/huissier`;

  constructor(private http: HttpClient) {}

  /**
   * Crée un document huissier
   * POST /api/huissier/document
   */
  createDocument(dto: DocumentHuissierDTO): Observable<DocumentHuissier> {
    return this.http.post<DocumentHuissier>(`${this.apiUrl}/document`, dto);
  }

  /**
   * Récupère un document par son ID
   * GET /api/huissier/document/{id}
   */
  getDocumentById(id: number): Observable<DocumentHuissier> {
    return this.http.get<DocumentHuissier>(`${this.apiUrl}/document/${id}`);
  }

  /**
   * Récupère tous les documents d'un dossier
   * GET /api/huissier/documents?dossierId={id}
   */
  getDocumentsByDossier(dossierId: number): Observable<DocumentHuissier[]> {
    const params = new HttpParams().set('dossierId', dossierId.toString());
    return this.http.get<DocumentHuissier[]>(`${this.apiUrl}/documents`, { params });
  }

  /**
   * Marque un document comme expiré
   * PUT /api/huissier/document/{id}/expire
   */
  markDocumentAsExpired(id: number): Observable<DocumentHuissier> {
    return this.http.put<DocumentHuissier>(`${this.apiUrl}/document/${id}/expire`, {});
  }
}
```

**Créer** : `src/app/services/huissier-action.service.ts`

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ActionHuissier, ActionHuissierDTO } from '../models/huissier-action.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class HuissierActionService {
  private apiUrl = `${environment.apiUrl}/huissier`;

  constructor(private http: HttpClient) {}

  /**
   * Crée une action huissier
   * POST /api/huissier/action
   */
  createAction(dto: ActionHuissierDTO): Observable<ActionHuissier> {
    return this.http.post<ActionHuissier>(`${this.apiUrl}/action`, dto);
  }

  /**
   * Récupère une action par son ID
   * GET /api/huissier/action/{id}
   */
  getActionById(id: number): Observable<ActionHuissier> {
    return this.http.get<ActionHuissier>(`${this.apiUrl}/action/${id}`);
  }

  /**
   * Récupère toutes les actions d'un dossier
   * GET /api/huissier/actions?dossierId={id}
   */
  getActionsByDossier(dossierId: number): Observable<ActionHuissier[]> {
    const params = new HttpParams().set('dossierId', dossierId.toString());
    return this.http.get<ActionHuissier[]>(`${this.apiUrl}/actions`, { params });
  }
}
```

---

## 📝 PROMPT 3 : Ajout du Menu dans le Sidebar

**Modifier** : `src/app/components/sidebar/sidebar.component.html` ou le composant de navigation principal

```html
<!-- Ajouter dans la liste de navigation -->
<nav class="sidebar-nav">
  <!-- ... autres liens existants ... -->
  
  <!-- ✅ NOUVEAU : Menu pour la gestion des documents et actions huissier -->
  <a routerLink="/juridique/gestion-huissier" 
     routerLinkActive="active"
     class="nav-link">
    <i class="fas fa-gavel"></i>
    <span>Gestion Huissier</span>
  </a>
  
  <!-- ... autres liens ... -->
</nav>
```

**Ou avec un sous-menu (si vous avez des sous-menus)** :

```html
<div class="nav-item dropdown">
  <a class="nav-link dropdown-toggle" 
     data-bs-toggle="dropdown" 
     role="button">
    <i class="fas fa-gavel"></i>
    <span>Gestion Huissier</span>
  </a>
  <ul class="dropdown-menu">
    <li>
      <a class="dropdown-item" routerLink="/juridique/gestion-huissier/documents">
        Documents Huissier
      </a>
    </li>
    <li>
      <a class="dropdown-item" routerLink="/juridique/gestion-huissier/actions">
        Actions Huissier
      </a>
    </li>
  </ul>
</div>
```

---

## 📝 PROMPT 4 : Composant Principal de Gestion Huissier

**Créer** : `src/app/components/gestion-huissier/gestion-huissier.component.ts`

```typescript
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { DossierService } from '../../services/dossier.service';
import { HuissierService } from '../../services/huissier.service';
import { HuissierDocumentService } from '../../services/huissier-document.service';
import { HuissierActionService } from '../../services/huissier-action.service';
import { Dossier } from '../../models/dossier.model';
import { Huissier } from '../../models/huissier.model';
import { DocumentHuissier, TypeDocumentHuissier } from '../../models/huissier-document.model';
import { ActionHuissier, TypeActionHuissier } from '../../models/huissier-action.model';

@Component({
  selector: 'app-gestion-huissier',
  templateUrl: './gestion-huissier.component.html',
  styleUrls: ['./gestion-huissier.component.css']
})
export class GestionHuissierComponent implements OnInit {
  // Onglets
  activeTab: 'documents' | 'actions' = 'documents';
  
  // Données
  dossiers: Dossier[] = [];
  huissiers: Huissier[] = [];
  selectedDossierId: number | null = null;
  selectedHuissierId: number | null = null;
  
  // Documents
  documents: DocumentHuissier[] = [];
  showDocumentForm = false;
  documentForm: any = {
    dossierId: null,
    typeDocument: null,
    huissierName: '',
    pieceJointeUrl: null
  };
  
  // Actions
  actions: ActionHuissier[] = [];
  showActionForm = false;
  actionForm: any = {
    dossierId: null,
    typeAction: null,
    huissierName: '',
    montantRecouvre: null,
    montantRestant: null,
    etatDossier: null,
    pieceJointeUrl: null
  };
  
  // Enums pour les templates
  TypeDocumentHuissier = TypeDocumentHuissier;
  TypeActionHuissier = TypeActionHuissier;
  
  // États de chargement
  isLoading = false;
  isLoadingDocuments = false;
  isLoadingActions = false;

  constructor(
    private router: Router,
    private dossierService: DossierService,
    private huissierService: HuissierService,
    private documentService: HuissierDocumentService,
    private actionService: HuissierActionService
  ) {}

  ngOnInit(): void {
    this.loadDossiers();
    this.loadHuissiers();
  }

  /**
   * Charge la liste des dossiers
   */
  loadDossiers(): void {
    this.isLoading = true;
    this.dossierService.getAllDossiers().subscribe({
      next: (dossiers) => {
        this.dossiers = dossiers;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des dossiers:', error);
        this.isLoading = false;
      }
    });
  }

  /**
   * Charge la liste des huissiers
   */
  loadHuissiers(): void {
    this.huissierService.getAllHuissiers().subscribe({
      next: (huissiers) => {
        this.huissiers = huissiers;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des huissiers:', error);
      }
    });
  }

  /**
   * Change d'onglet
   */
  switchTab(tab: 'documents' | 'actions'): void {
    this.activeTab = tab;
    if (tab === 'documents' && this.selectedDossierId) {
      this.loadDocuments();
    } else if (tab === 'actions' && this.selectedDossierId) {
      this.loadActions();
    }
  }

  /**
   * Sélectionne un dossier et charge ses documents/actions
   */
  onDossierSelected(dossierId: number): void {
    this.selectedDossierId = dossierId;
    if (this.activeTab === 'documents') {
      this.loadDocuments();
    } else {
      this.loadActions();
    }
  }

  /**
   * Charge les documents d'un dossier
   */
  loadDocuments(): void {
    if (!this.selectedDossierId) return;
    
    this.isLoadingDocuments = true;
    this.documentService.getDocumentsByDossier(this.selectedDossierId).subscribe({
      next: (documents) => {
        this.documents = documents;
        this.isLoadingDocuments = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des documents:', error);
        this.isLoadingDocuments = false;
      }
    });
  }

  /**
   * Charge les actions d'un dossier
   */
  loadActions(): void {
    if (!this.selectedDossierId) return;
    
    this.isLoadingActions = true;
    this.actionService.getActionsByDossier(this.selectedDossierId).subscribe({
      next: (actions) => {
        this.actions = actions;
        this.isLoadingActions = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des actions:', error);
        this.isLoadingActions = false;
      }
    });
  }

  /**
   * Ouvre le formulaire de création de document
   */
  openDocumentForm(): void {
    if (!this.selectedDossierId) {
      alert('Veuillez sélectionner un dossier d\'abord');
      return;
    }
    this.documentForm = {
      dossierId: this.selectedDossierId,
      typeDocument: null,
      huissierName: this.getSelectedHuissierName(),
      pieceJointeUrl: null
    };
    this.showDocumentForm = true;
  }

  /**
   * Ouvre le formulaire de création d'action
   */
  openActionForm(): void {
    if (!this.selectedDossierId) {
      alert('Veuillez sélectionner un dossier d\'abord');
      return;
    }
    this.actionForm = {
      dossierId: this.selectedDossierId,
      typeAction: null,
      huissierName: this.getSelectedHuissierName(),
      montantRecouvre: null,
      montantRestant: null,
      etatDossier: null,
      pieceJointeUrl: null
    };
    this.showActionForm = true;
  }

  /**
   * Récupère le nom complet de l'huissier sélectionné
   */
  getSelectedHuissierName(): string {
    if (!this.selectedHuissierId) return '';
    const huissier = this.huissiers.find(h => h.id === this.selectedHuissierId);
    return huissier ? `${huissier.prenom} ${huissier.nom}` : '';
  }

  /**
   * Crée un document
   */
  createDocument(): void {
    if (!this.documentForm.dossierId || !this.documentForm.typeDocument || !this.documentForm.huissierName) {
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    this.isLoading = true;
    this.documentService.createDocument(this.documentForm).subscribe({
      next: (document) => {
        console.log('Document créé avec succès:', document);
        this.showDocumentForm = false;
        this.loadDocuments();
        this.isLoading = false;
        alert('Document créé avec succès');
      },
      error: (error) => {
        console.error('Erreur lors de la création du document:', error);
        this.isLoading = false;
        alert('Erreur lors de la création du document: ' + (error.error?.error || error.message));
      }
    });
  }

  /**
   * Crée une action
   */
  createAction(): void {
    if (!this.actionForm.dossierId || !this.actionForm.typeAction || !this.actionForm.huissierName) {
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    this.isLoading = true;
    this.actionService.createAction(this.actionForm).subscribe({
      next: (action) => {
        console.log('Action créée avec succès:', action);
        this.showActionForm = false;
        this.loadActions();
        this.isLoading = false;
        alert('Action créée avec succès');
      },
      error: (error) => {
        console.error('Erreur lors de la création de l\'action:', error);
        this.isLoading = false;
        alert('Erreur lors de la création de l\'action: ' + (error.error?.error || error.message));
      }
    });
  }

  /**
   * Formate une date pour l'affichage
   */
  formatDate(dateString: string): string {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  /**
   * Formate le type de document pour l'affichage
   */
  getDocumentTypeLabel(type: TypeDocumentHuissier): string {
    const labels = {
      [TypeDocumentHuissier.PV_MISE_EN_DEMEURE]: 'PV Mise en Demeure',
      [TypeDocumentHuissier.ORDONNANCE_PAIEMENT]: 'Ordonnance de Paiement',
      [TypeDocumentHuissier.PV_NOTIFICATION_ORDONNANCE]: 'PV Notification Ordonnance'
    };
    return labels[type] || type;
  }

  /**
   * Formate le type d'action pour l'affichage
   */
  getActionTypeLabel(type: TypeActionHuissier): string {
    const labels = {
      [TypeActionHuissier.ACLA_TA7AFOUDHIA]: 'Saisie Conservatoire (العقلة التحفظية)',
      [TypeActionHuissier.ACLA_TANFITHIA]: 'Saisie Exécutive (العقلة التنفيذية)',
      [TypeActionHuissier.ACLA_TAW9IFIYA]: 'Saisie de Blocage (العقلة التوقيفية)',
      [TypeActionHuissier.ACLA_A9ARYA]: 'Saisie Immobilière (العقلة العقارية)'
    };
    return labels[type] || type;
  }

  /**
   * Calcule la date d'expiration d'un document
   */
  getExpirationDate(document: DocumentHuissier): string {
    if (!document.dateCreation || !document.delaiLegalDays) return '';
    const date = new Date(document.dateCreation);
    date.setDate(date.getDate() + document.delaiLegalDays);
    return this.formatDate(date.toISOString());
  }

  /**
   * Vérifie si un document est expiré
   */
  isDocumentExpired(document: DocumentHuissier): boolean {
    if (!document.dateCreation || !document.delaiLegalDays) return false;
    const expirationDate = new Date(document.dateCreation);
    expirationDate.setDate(expirationDate.getDate() + document.delaiLegalDays);
    return new Date() > expirationDate;
  }
}
```

---

## 📝 PROMPT 5 : Template HTML pour le Composant de Gestion Huissier

**Créer** : `src/app/components/gestion-huissier/gestion-huissier.component.html`

```html
<div class="gestion-huissier-container">
  <div class="page-header">
    <h2>Gestion des Documents et Actions Huissier</h2>
  </div>

  <!-- Sélection du dossier et de l'huissier -->
  <div class="selection-section card mb-4">
    <div class="card-body">
      <div class="row">
        <div class="col-md-6">
          <label for="dossier-select">Sélectionner un dossier *</label>
          <select 
            id="dossier-select"
            class="form-control"
            [(ngModel)]="selectedDossierId"
            (change)="onDossierSelected(selectedDossierId)">
            <option [ngValue]="null">Sélectionner un dossier...</option>
            <option *ngFor="let dossier of dossiers" [ngValue]="dossier.id">
              {{ dossier.numeroDossier || 'Dossier #' + dossier.id }}
            </option>
          </select>
        </div>
        <div class="col-md-6">
          <label for="huissier-select">Sélectionner un huissier</label>
          <select 
            id="huissier-select"
            class="form-control"
            [(ngModel)]="selectedHuissierId">
            <option [ngValue]="null">Sélectionner un huissier...</option>
            <option *ngFor="let huissier of huissiers" [ngValue]="huissier.id">
              {{ huissier.prenom }} {{ huissier.nom }}
            </option>
          </select>
        </div>
      </div>
    </div>
  </div>

  <!-- Onglets Documents / Actions -->
  <ul class="nav nav-tabs mb-4">
    <li class="nav-item">
      <a class="nav-link" 
         [class.active]="activeTab === 'documents'"
         (click)="switchTab('documents')">
        Documents Huissier
      </a>
    </li>
    <li class="nav-item">
      <a class="nav-link" 
         [class.active]="activeTab === 'actions'"
         (click)="switchTab('actions')">
        Actions Huissier
      </a>
    </li>
  </ul>

  <!-- Contenu des Documents -->
  <div *ngIf="activeTab === 'documents'" class="documents-section">
    <div class="d-flex justify-content-between mb-3">
      <h3>Documents Huissier</h3>
      <button 
        class="btn btn-primary"
        (click)="openDocumentForm()"
        [disabled]="!selectedDossierId">
        <i class="fas fa-plus"></i> Créer un Document
      </button>
    </div>

    <!-- Liste des documents -->
    <div *ngIf="isLoadingDocuments" class="text-center">
      <div class="spinner-border" role="status">
        <span class="sr-only">Chargement...</span>
      </div>
    </div>

    <div *ngIf="!isLoadingDocuments && documents.length === 0" class="alert alert-info">
      Aucun document trouvé pour ce dossier.
    </div>

    <div *ngIf="!isLoadingDocuments && documents.length > 0" class="table-responsive">
      <table class="table table-striped">
        <thead>
          <tr>
            <th>Type</th>
            <th>Date de Création</th>
            <th>Délai Légal</th>
            <th>Date d'Expiration</th>
            <th>Statut</th>
            <th>Huissier</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let document of documents" 
              [class.table-warning]="isDocumentExpired(document)"
              [class.table-danger]="document.status === 'EXPIRED'">
            <td>{{ getDocumentTypeLabel(document.typeDocument) }}</td>
            <td>{{ formatDate(document.dateCreation) }}</td>
            <td>{{ document.delaiLegalDays }} jours</td>
            <td>{{ getExpirationDate(document) }}</td>
            <td>
              <span class="badge" 
                    [class.badge-warning]="document.status === 'PENDING'"
                    [class.badge-danger]="document.status === 'EXPIRED'"
                    [class.badge-success]="document.status === 'COMPLETED'">
                {{ document.status }}
              </span>
            </td>
            <td>{{ document.huissierName }}</td>
            <td>
              <button class="btn btn-sm btn-info" 
                      *ngIf="document.pieceJointeUrl"
                      (click)="openDocument(document.pieceJointeUrl)">
                <i class="fas fa-file"></i> Voir
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <!-- Contenu des Actions -->
  <div *ngIf="activeTab === 'actions'" class="actions-section">
    <div class="d-flex justify-content-between mb-3">
      <h3>Actions Huissier</h3>
      <button 
        class="btn btn-primary"
        (click)="openActionForm()"
        [disabled]="!selectedDossierId">
        <i class="fas fa-plus"></i> Créer une Action
      </button>
    </div>

    <!-- Liste des actions -->
    <div *ngIf="isLoadingActions" class="text-center">
      <div class="spinner-border" role="status">
        <span class="sr-only">Chargement...</span>
      </div>
    </div>

    <div *ngIf="!isLoadingActions && actions.length === 0" class="alert alert-info">
      Aucune action trouvée pour ce dossier.
    </div>

    <div *ngIf="!isLoadingActions && actions.length > 0" class="table-responsive">
      <table class="table table-striped">
        <thead>
          <tr>
            <th>Type d'Action</th>
            <th>Date</th>
            <th>Montant Recouvré</th>
            <th>Montant Restant</th>
            <th>État Dossier</th>
            <th>Huissier</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let action of actions">
            <td>{{ getActionTypeLabel(action.typeAction) }}</td>
            <td>{{ formatDate(action.dateAction) }}</td>
            <td>{{ action.montantRecouvre ? (action.montantRecouvre | number:'1.2-2') + ' TND' : '-' }}</td>
            <td>{{ action.montantRestant ? (action.montantRestant | number:'1.2-2') + ' TND' : '-' }}</td>
            <td>
              <span class="badge badge-info" *ngIf="action.etatDossier">
                {{ action.etatDossier }}
              </span>
            </td>
            <td>{{ action.huissierName }}</td>
            <td>
              <button class="btn btn-sm btn-info" 
                      *ngIf="action.pieceJointeUrl"
                      (click)="openDocument(action.pieceJointeUrl)">
                <i class="fas fa-file"></i> Voir
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <!-- Modal de création de document -->
  <div class="modal" 
       [class.show]="showDocumentForm"
       *ngIf="showDocumentForm"
       (click)="showDocumentForm = false">
    <div class="modal-dialog" (click)="$event.stopPropagation()">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">Créer un Document Huissier</h5>
          <button type="button" class="close" (click)="showDocumentForm = false">
            <span>&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <form>
            <div class="form-group">
              <label>Type de Document *</label>
              <select class="form-control" [(ngModel)]="documentForm.typeDocument" name="typeDocument">
                <option [ngValue]="null">Sélectionner...</option>
                <option [ngValue]="TypeDocumentHuissier.PV_MISE_EN_DEMEURE">
                  PV Mise en Demeure (10 jours)
                </option>
                <option [ngValue]="TypeDocumentHuissier.ORDONNANCE_PAIEMENT">
                  Ordonnance de Paiement (20 jours)
                </option>
                <option [ngValue]="TypeDocumentHuissier.PV_NOTIFICATION_ORDONNANCE">
                  PV Notification Ordonnance (20 jours)
                </option>
              </select>
            </div>
            <div class="form-group">
              <label>Nom de l'Huissier *</label>
              <input type="text" 
                     class="form-control" 
                     [(ngModel)]="documentForm.huissierName"
                     name="huissierName"
                     placeholder="Prénom Nom">
            </div>
            <div class="form-group">
              <label>Pièce Jointe (URL)</label>
              <input type="text" 
                     class="form-control" 
                     [(ngModel)]="documentForm.pieceJointeUrl"
                     name="pieceJointeUrl"
                     placeholder="URL du document">
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" (click)="showDocumentForm = false">
            Annuler
          </button>
          <button type="button" class="btn btn-primary" (click)="createDocument()" [disabled]="isLoading">
            <span *ngIf="isLoading" class="spinner-border spinner-border-sm"></span>
            Créer
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- Modal de création d'action -->
  <div class="modal" 
       [class.show]="showActionForm"
       *ngIf="showActionForm"
       (click)="showActionForm = false">
    <div class="modal-dialog" (click)="$event.stopPropagation()">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">Créer une Action Huissier</h5>
          <button type="button" class="close" (click)="showActionForm = false">
            <span>&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <form>
            <div class="form-group">
              <label>Type d'Action *</label>
              <select class="form-control" [(ngModel)]="actionForm.typeAction" name="typeAction">
                <option [ngValue]="null">Sélectionner...</option>
                <option [ngValue]="TypeActionHuissier.ACLA_TA7AFOUDHIA">
                  Saisie Conservatoire (العقلة التحفظية)
                </option>
                <option [ngValue]="TypeActionHuissier.ACLA_TANFITHIA">
                  Saisie Exécutive (العقلة التنفيذية)
                </option>
                <option [ngValue]="TypeActionHuissier.ACLA_TAW9IFIYA">
                  Saisie de Blocage (العقلة التوقيفية)
                </option>
                <option [ngValue]="TypeActionHuissier.ACLA_A9ARYA">
                  Saisie Immobilière (العقلة العقارية)
                </option>
              </select>
            </div>
            <div class="form-group">
              <label>Nom de l'Huissier *</label>
              <input type="text" 
                     class="form-control" 
                     [(ngModel)]="actionForm.huissierName"
                     name="huissierName"
                     placeholder="Prénom Nom">
            </div>
            <div class="form-group">
              <label>Montant Recouvré (TND)</label>
              <input type="number" 
                     class="form-control" 
                     [(ngModel)]="actionForm.montantRecouvre"
                     name="montantRecouvre"
                     step="0.01"
                     min="0"
                     placeholder="0.00">
            </div>
            <div class="form-group">
              <label>Montant Restant (TND)</label>
              <input type="number" 
                     class="form-control" 
                     [(ngModel)]="actionForm.montantRestant"
                     name="montantRestant"
                     step="0.01"
                     min="0"
                     placeholder="0.00">
            </div>
            <div class="form-group">
              <label>État du Dossier</label>
              <select class="form-control" [(ngModel)]="actionForm.etatDossier" name="etatDossier">
                <option [ngValue]="null">Sélectionner...</option>
                <option value="EN_COURS">En Cours</option>
                <option value="CLOTURE">Clôturé</option>
                <option value="SUSPENDU">Suspendu</option>
              </select>
            </div>
            <div class="form-group">
              <label>Pièce Jointe (URL)</label>
              <input type="text" 
                     class="form-control" 
                     [(ngModel)]="actionForm.pieceJointeUrl"
                     name="pieceJointeUrl"
                     placeholder="URL du document">
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" (click)="showActionForm = false">
            Annuler
          </button>
          <button type="button" class="btn btn-primary" (click)="createAction()" [disabled]="isLoading">
            <span *ngIf="isLoading" class="spinner-border spinner-border-sm"></span>
            Créer
          </button>
        </div>
      </div>
    </div>
  </div>
</div>
```

---

## 📝 PROMPT 6 : Configuration des Routes

**Modifier** : `src/app/app-routing.module.ts` ou le fichier de routing

```typescript
import { GestionHuissierComponent } from './components/gestion-huissier/gestion-huissier.component';

const routes: Routes = [
  // ... autres routes ...
  {
    path: 'juridique/gestion-huissier',
    component: GestionHuissierComponent,
    canActivate: [AuthGuard] // Si vous avez un guard d'authentification
  },
  // ... autres routes ...
];
```

---

## 📝 PROMPT 7 : Correction du Problème d'Audience

**Modifier** : `src/main/java/projet/carthagecreance_backend/Controller/AudienceController.java`

Le problème vient probablement du fait que les notifications sont toujours appelées dans la transaction. Vérifiez que le code est bien celui-ci :

```java
@PostMapping
public ResponseEntity<?> createAudience(@RequestBody AudienceRequestDTO dto) {
    try {
        logger.info("📥 Requête de création d'audience reçue: {}", dto);
        logger.info("📥 Dossier ID: {}, Avocat ID: {}, Huissier ID: {}", 
                dto.getDossierIdValue(), dto.getAvocatIdValue(), dto.getHuissierIdValue());
        
        // ✅ Créer l'audience (transaction commitée ici)
        Audience createdAudience = audienceServiceImpl.createAudienceFromDTO(dto);
        
        logger.info("✅ Audience créée avec succès, ID: {}, dossier_id: {}", 
                createdAudience.getId(), 
                createdAudience.getDossier() != null ? createdAudience.getDossier().getId() : "NULL");
        
        // ✅ Envoyer les notifications APRÈS le commit (dans une transaction séparée)
        // Utiliser un try-catch pour isoler les erreurs de notification
        try {
            if (createdAudience.getDossier() != null) {
                automaticNotificationService.notifierCreationAudience(createdAudience, createdAudience.getDossier());
                // Vérifier si c'est une audience prochaine
                if (createdAudience.getDateProchaine() != null) {
                    automaticNotificationService.notifierAudienceProchaine(createdAudience, createdAudience.getDossier());
                }
            }
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi des notifications (non bloquante): {}", e.getMessage(), e);
            // Ne pas propager l'exception - l'audience est déjà créée
        }
        
        return new ResponseEntity<>(createdAudience, HttpStatus.CREATED);
    } catch (RuntimeException e) {
        logger.error("❌ Erreur lors de la création de l'audience: {}", e.getMessage(), e);
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Erreur lors de la création de l'audience",
                "message", e.getMessage(),
                "timestamp", new Date().toString()
        ));
    } catch (Exception e) {
        logger.error("❌ Erreur interne lors de la création de l'audience: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Erreur interne du serveur",
                "message", "Erreur lors de la création de l'audience: " + e.getMessage(),
                "timestamp", new Date().toString()
        ));
    }
}
```

**Vérifier aussi** que `AutomaticNotificationService` a bien `@Transactional(propagation = Propagation.REQUIRES_NEW)` sur les méthodes de notification.

---

## ✅ Checklist de Vérification

### Backend
- [ ] Les notifications ont `@Transactional(propagation = Propagation.REQUIRES_NEW)`
- [ ] Les notifications sont appelées APRÈS le commit de la transaction principale
- [ ] Les exceptions de notification sont catchées et ne sont pas propagées
- [ ] Le logging est activé pour voir où l'erreur se produit

### Frontend - Documents et Actions
- [ ] Interfaces TypeScript créées
- [ ] Services Angular créés
- [ ] Menu ajouté dans le sidebar
- [ ] Composant de gestion créé
- [ ] Routes configurées
- [ ] Templates HTML créés
- [ ] Tests effectués

---

## 🎯 Workflow Recommandé

1. **Phase 1** : Créer les documents huissier (PV mise en demeure, etc.)
2. **Phase 2** : Créer les actions huissier (saisies, etc.)
3. **Phase 3** : Passer à l'audience une fois les documents et actions traités

---

**Bon développement ! 🎉**

