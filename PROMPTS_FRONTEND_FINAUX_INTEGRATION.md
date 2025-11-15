# 🎨 Prompts Finaux - Intégration Frontend Complète

## 📋 Vue d'Ensemble

Ce document contient **TOUS** les prompts nécessaires pour créer une intégration frontend complète et fonctionnelle avec le backend. Les prompts sont organisés par module et par ordre d'implémentation.

---

## 🎯 PARTIE 1 : Services Angular (Base)

### 📋 PROMPT 1 : Créer ActionRecouvrementService (Sans Coûts)

```
Dans le projet Angular, créez un service pour gérer les actions dans le module recouvrement amiable (SANS affichage de coûts) :

Fichier : src/app/services/action-recouvrement.service.ts

1. Créer les interfaces TypeScript :
```typescript
export interface ActionRecouvrement {
  id?: number;
  type: TypeAction;
  reponseDebiteur: ReponseDebiteur | null;
  dateAction: Date | string;
  nbOccurrences: number;
  dossier: { id: number };
  // PAS de coutUnitaire ni totalCout
}

export enum TypeAction {
  APPEL = 'APPEL',
  EMAIL = 'EMAIL',
  VISITE = 'VISITE',
  LETTRE = 'LETTRE',
  AUTRE = 'AUTRE'
}

export enum ReponseDebiteur {
  POSITIVE = 'POSITIVE',
  NEGATIVE = 'NEGATIVE'
}

export interface StatistiquesActions {
  total: number;
  positives: number;
  negatives: number;
  sansReponse: number;
  parType: { [key: string]: number };
}
```

2. Implémenter le service :
```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class ActionRecouvrementService {
  private apiUrl = 'http://localhost:8080/api/actions';

  constructor(private http: HttpClient) {}

  // Récupérer toutes les actions d'un dossier (sans coûts)
  getActionsByDossier(dossierId: number): Observable<ActionRecouvrement[]> {
    return this.http.get<ActionRecouvrement[]>(`${this.apiUrl}/dossier/${dossierId}`)
      .pipe(
        map(actions => actions.map(action => ({
          ...action,
          dateAction: new Date(action.dateAction)
        })))
      );
  }

  // Créer une action (sans coût unitaire)
  createAction(dossierId: number, action: Partial<ActionRecouvrement>): Observable<ActionRecouvrement> {
    const actionToSend = {
      ...action,
      dossier: { id: dossierId }
    };
    return this.http.post<ActionRecouvrement>(this.apiUrl, actionToSend);
  }

  // Modifier une action
  updateAction(actionId: number, action: Partial<ActionRecouvrement>): Observable<ActionRecouvrement> {
    return this.http.put<ActionRecouvrement>(`${this.apiUrl}/${actionId}`, action);
  }

  // Supprimer une action
  deleteAction(actionId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${actionId}`);
  }

  // Filtrer par type
  getActionsByType(dossierId: number, type: TypeAction): Observable<ActionRecouvrement[]> {
    return this.http.get<ActionRecouvrement[]>(`${this.apiUrl}/type/${type}/dossier/${dossierId}`);
  }

  // Filtrer par réponse
  getActionsByReponse(dossierId: number, reponse: ReponseDebiteur): Observable<ActionRecouvrement[]> {
    return this.http.get<ActionRecouvrement[]>(`${this.apiUrl}/dossier/${dossierId}/reponse/${reponse}`);
  }

  // Calculer les statistiques
  getStatistiquesActions(dossierId: number): Observable<StatistiquesActions> {
    return this.getActionsByDossier(dossierId).pipe(
      map(actions => {
        const stats: StatistiquesActions = {
          total: actions.length,
          positives: actions.filter(a => a.reponseDebiteur === ReponseDebiteur.POSITIVE).length,
          negatives: actions.filter(a => a.reponseDebiteur === ReponseDebiteur.NEGATIVE).length,
          sansReponse: actions.filter(a => !a.reponseDebiteur).length,
          parType: {}
        };
        
        actions.forEach(action => {
          stats.parType[action.type] = (stats.parType[action.type] || 0) + 1;
        });
        
        return stats;
      })
    );
  }
}
```

IMPORTANT :
- Les coûts ne sont JAMAIS exposés dans ce service
- Le backend calcule automatiquement les coûts et les met dans Finance
- Les interfaces utilisent uniquement nbOccurrences
```

---

### 📋 PROMPT 2 : Créer FinanceService (Complet avec Coûts)

```
Dans le projet Angular, créez un service complet pour gérer Finance avec tous les coûts :

Fichier : src/app/services/finance.service.ts

1. Créer les interfaces TypeScript :
```typescript
export interface Finance {
  id?: number;
  devise: string;
  dateOperation: Date | string;
  description: string;
  fraisAvocat?: number;
  fraisHuissier?: number;
  fraisCreationDossier: number;
  fraisGestionDossier: number;
  dureeGestionMois: number;
  coutActionsAmiable: number;
  coutActionsJuridique: number;
  nombreActionsAmiable: number;
  nombreActionsJuridique: number;
  factureFinalisee: boolean;
  dateFacturation?: Date | string;
  dossier: { id: number };
}

export interface DetailFacture {
  fraisCreationDossier: number;
  coutGestionTotal: number;
  coutActionsAmiable: number;
  coutActionsJuridique: number;
  fraisAvocat: number;
  fraisHuissier: number;
  totalFacture: number;
}

export interface StatistiquesCouts {
  totalFraisCreation: number;
  totalFraisGestion: number;
  totalActionsAmiable: number;
  totalActionsJuridique: number;
  totalAvocat: number;
  totalHuissier: number;
  grandTotal: number;
}

export interface ActionFinance {
  id: number;
  type: string;
  reponseDebiteur: string | null;
  dateAction: Date | string;
  nbOccurrences: number;
  coutUnitaire: number;
  totalCout: number;
  dossier: { id: number };
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
```

2. Implémenter le service :
```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class FinanceService {
  private apiUrl = 'http://localhost:8080/api/finances';

  constructor(private http: HttpClient) {}

  // Récupérer Finance par dossier
  getFinanceByDossier(dossierId: number): Observable<Finance> {
    return this.http.get<Finance>(`${this.apiUrl}/dossier/${dossierId}`);
  }

  // Détail facture complète
  getDetailFacture(dossierId: number): Observable<DetailFacture> {
    return this.http.get<DetailFacture>(`${this.apiUrl}/dossier/${dossierId}/facture`);
  }

  // Coûts détaillés par dossier
  getCoutsParDossier(dossierId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/dossier/${dossierId}/detail`);
  }

  // Recalculer tous les coûts
  recalculerCouts(dossierId: number): Observable<Finance> {
    return this.http.post<Finance>(`${this.apiUrl}/dossier/${dossierId}/recalculer`, {});
  }

  // Statistiques globales
  getStatistiquesCouts(): Observable<StatistiquesCouts> {
    return this.http.get<StatistiquesCouts>(`${this.apiUrl}/statistiques`);
  }

  // Liste paginée des dossiers avec coûts
  getDossiersAvecCouts(page: number = 0, size: number = 10, sort: string = 'dateOperation'): Observable<Page<Finance>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);
    return this.http.get<Page<Finance>>(`${this.apiUrl}/dossiers-avec-couts`, { params });
  }

  // Factures en attente
  getFacturesEnAttente(): Observable<Finance[]> {
    return this.http.get<Finance[]>(`${this.apiUrl}/factures-en-attente`);
  }

  // Finaliser une facture
  finaliserFacture(dossierId: number): Observable<Finance> {
    return this.http.put<Finance>(`${this.apiUrl}/dossier/${dossierId}/finaliser-facture`, {});
  }

  // Actions avec coûts détaillés (pour Finance uniquement)
  getActionsAvecCouts(dossierId: number): Observable<ActionFinance[]> {
    // Utiliser l'endpoint actions avec tous les détails
    return this.http.get<ActionFinance[]>(`http://localhost:8080/api/actions/dossier/${dossierId}`);
  }
}
```

IMPORTANT :
- Toutes les méthodes retournent les coûts complets
- Utiliser les endpoints /api/finances/*
- Gérer les erreurs avec catchError
```

---

## 🎯 PARTIE 2 : Composants Recouvrement Amiable

### 📋 PROMPT 3 : Créer le Composant Tableau Actions (Sans Coûts)

```
Dans le projet Angular, créez le composant pour afficher les actions d'un dossier dans le recouvrement amiable (SANS coûts) :

Fichier : src/app/components/recouvrement-amiable/dossier-actions/dossier-actions.component.ts

```typescript
import { Component, Input, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActionRecouvrementService, ActionRecouvrement, TypeAction, ReponseDebiteur, StatistiquesActions } from 'src/app/services/action-recouvrement.service';
import { ActionDialogAmiableComponent } from '../action-dialog-amiable/action-dialog-amiable.component';

@Component({
  selector: 'app-dossier-actions-amiable',
  templateUrl: './dossier-actions.component.html',
  styleUrls: ['./dossier-actions.component.css']
})
export class DossierActionsAmiableComponent implements OnInit {
  @Input() dossierId!: number;
  
  actions: ActionRecouvrement[] = [];
  statistiques: StatistiquesActions = {
    total: 0,
    positives: 0,
    negatives: 0,
    sansReponse: 0,
    parType: {}
  };
  
  displayedColumns: string[] = ['dateAction', 'type', 'nbOccurrences', 'reponseDebiteur', 'actions'];
  filteredActions: ActionRecouvrement[] = [];
  selectedTypeFilter: TypeAction | '' = '';
  selectedReponseFilter: ReponseDebiteur | '' = '';

  constructor(
    private actionService: ActionRecouvrementService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadActions();
  }

  loadActions(): void {
    this.actionService.getActionsByDossier(this.dossierId).subscribe({
      next: (actions) => {
        this.actions = actions;
        this.filteredActions = actions;
        this.loadStatistiques();
      },
      error: (err) => {
        this.snackBar.open('Erreur lors du chargement des actions', 'Fermer', { duration: 3000 });
        console.error(err);
      }
    });
  }

  loadStatistiques(): void {
    this.actionService.getStatistiquesActions(this.dossierId).subscribe({
      next: (stats) => {
        this.statistiques = stats;
      }
    });
  }

  addAction(): void {
    const dialogRef = this.dialog.open(ActionDialogAmiableComponent, {
      width: '500px',
      data: { dossierId: this.dossierId }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadActions();
      }
    });
  }

  editAction(action: ActionRecouvrement): void {
    const dialogRef = this.dialog.open(ActionDialogAmiableComponent, {
      width: '500px',
      data: { dossierId: this.dossierId, action }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadActions();
      }
    });
  }

  deleteAction(actionId: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette action ?')) {
      this.actionService.deleteAction(actionId).subscribe({
        next: () => {
          this.snackBar.open('Action supprimée avec succès', 'Fermer', { duration: 3000 });
          this.loadActions();
        },
        error: (err) => {
          this.snackBar.open('Erreur lors de la suppression', 'Fermer', { duration: 3000 });
          console.error(err);
        }
      });
    }
  }

  filterByType(type: TypeAction | ''): void {
    this.selectedTypeFilter = type;
    this.applyFilters();
  }

  filterByReponse(reponse: ReponseDebiteur | ''): void {
    this.selectedReponseFilter = reponse;
    this.applyFilters();
  }

  applyFilters(): void {
    this.filteredActions = this.actions.filter(action => {
      const typeMatch = !this.selectedTypeFilter || action.type === this.selectedTypeFilter;
      const reponseMatch = !this.selectedReponseFilter || action.reponseDebiteur === this.selectedReponseFilter;
      return typeMatch && reponseMatch;
    });
  }

  getTypeLabel(type: TypeAction): string {
    const labels: { [key: string]: string } = {
      'APPEL': 'appel(s)',
      'EMAIL': 'email(s)',
      'VISITE': 'visite(s)',
      'LETTRE': 'lettre(s)',
      'AUTRE': 'action(s)'
    };
    return labels[type] || 'action(s)';
  }
}
```

Fichier : src/app/components/recouvrement-amiable/dossier-actions/dossier-actions.component.html

```html
<div class="actions-container">
  <!-- Statistiques -->
  <div class="stats-cards">
    <mat-card class="stat-card">
      <mat-card-title>Total Actions</mat-card-title>
      <mat-card-content class="stat-value">{{ statistiques.total }}</mat-card-content>
    </mat-card>
    
    <mat-card class="stat-card positive">
      <mat-card-title>Positives</mat-card-title>
      <mat-card-content class="stat-value">{{ statistiques.positives }}</mat-card-content>
    </mat-card>
    
    <mat-card class="stat-card negative">
      <mat-card-title>Négatives</mat-card-title>
      <mat-card-content class="stat-value">{{ statistiques.negatives }}</mat-card-content>
    </mat-card>
    
    <mat-card class="stat-card">
      <mat-card-title>Sans Réponse</mat-card-title>
      <mat-card-content class="stat-value">{{ statistiques.sansReponse }}</mat-card-content>
    </mat-card>
  </div>

  <!-- Filtres -->
  <div class="filters-section">
    <mat-form-field>
      <mat-label>Filtrer par type</mat-label>
      <mat-select [(value)]="selectedTypeFilter" (selectionChange)="filterByType($event.value)">
        <mat-option value="">Tous</mat-option>
        <mat-option value="APPEL">Appel</mat-option>
        <mat-option value="EMAIL">Email</mat-option>
        <mat-option value="VISITE">Visite</mat-option>
        <mat-option value="LETTRE">Lettre</mat-option>
        <mat-option value="AUTRE">Autre</mat-option>
      </mat-select>
    </mat-form-field>

    <mat-form-field>
      <mat-label>Filtrer par réponse</mat-label>
      <mat-select [(value)]="selectedReponseFilter" (selectionChange)="filterByReponse($event.value)">
        <mat-option value="">Toutes</mat-option>
        <mat-option value="POSITIVE">Positive</mat-option>
        <mat-option value="NEGATIVE">Négative</mat-option>
      </mat-select>
    </mat-form-field>

    <button mat-raised-button color="primary" (click)="addAction()">
      <mat-icon>add</mat-icon>
      Ajouter Action
    </button>
  </div>

  <!-- Tableau (SANS colonnes de coût) -->
  <table mat-table [dataSource]="filteredActions" class="actions-table">
    <ng-container matColumnDef="dateAction">
      <th mat-header-cell *matHeaderCellDef>Date</th>
      <td mat-cell *matCellDef="let action">{{ action.dateAction | date:'dd/MM/yyyy' }}</td>
    </ng-container>
    
    <ng-container matColumnDef="type">
      <th mat-header-cell *matHeaderCellDef>Type</th>
      <td mat-cell *matCellDef="let action">
        <mat-chip>{{ action.type }}</mat-chip>
      </td>
    </ng-container>
    
    <ng-container matColumnDef="nbOccurrences">
      <th mat-header-cell *matHeaderCellDef>Nombre</th>
      <td mat-cell *matCellDef="let action">
        {{ action.nbOccurrences }} {{ getTypeLabel(action.type) }}
      </td>
    </ng-container>
    
    <ng-container matColumnDef="reponseDebiteur">
      <th mat-header-cell *matHeaderCellDef>Réponse</th>
      <td mat-cell *matCellDef="let action">
        <mat-chip 
          [class.positive]="action.reponseDebiteur === 'POSITIVE'"
          [class.negative]="action.reponseDebiteur === 'NEGATIVE'"
          [class.neutral]="!action.reponseDebiteur">
          {{ action.reponseDebiteur || 'Aucune' }}
        </mat-chip>
      </td>
    </ng-container>
    
    <ng-container matColumnDef="actions">
      <th mat-header-cell *matHeaderCellDef>Actions</th>
      <td mat-cell *matCellDef="let action">
        <button mat-icon-button (click)="editAction(action)" matTooltip="Modifier">
          <mat-icon>edit</mat-icon>
        </button>
        <button mat-icon-button (click)="deleteAction(action.id!)" matTooltip="Supprimer">
          <mat-icon>delete</mat-icon>
        </button>
      </td>
    </ng-container>
    
    <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
    <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
  </table>
</div>
```

IMPORTANT :
- PAS de colonnes coutUnitaire ou totalCout
- Focus sur nbOccurrences
- Statistiques sans coûts
```

---

### 📋 PROMPT 4 : Créer le Dialog d'Ajout/Modification d'Action (Sans Coût)

```
Dans le projet Angular, créez le dialog pour ajouter/modifier une action (SANS coût) :

Fichier : src/app/components/recouvrement-amiable/action-dialog-amiable/action-dialog-amiable.component.ts

```typescript
import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActionRecouvrementService, ActionRecouvrement, TypeAction, ReponseDebiteur } from 'src/app/services/action-recouvrement.service';

@Component({
  selector: 'app-action-dialog-amiable',
  templateUrl: './action-dialog-amiable.component.html',
  styleUrls: ['./action-dialog-amiable.component.css']
})
export class ActionDialogAmiableComponent implements OnInit {
  actionForm: FormGroup;
  isEditMode: boolean = false;
  
  typeActions = Object.values(TypeAction);
  reponses = [null, ReponseDebiteur.POSITIVE, ReponseDebiteur.NEGATIVE];

  constructor(
    private fb: FormBuilder,
    private actionService: ActionRecouvrementService,
    private dialogRef: MatDialogRef<ActionDialogAmiableComponent>,
    private snackBar: MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public data: { dossierId: number, action?: ActionRecouvrement }
  ) {
    this.actionForm = this.fb.group({
      type: ['', Validators.required],
      dateAction: [new Date(), Validators.required],
      nbOccurrences: [1, [Validators.required, Validators.min(1)]],
      reponseDebiteur: [null]
    });
    
    if (data.action) {
      this.isEditMode = true;
      this.actionForm.patchValue({
        ...data.action,
        dateAction: new Date(data.action.dateAction)
      });
    }
  }

  ngOnInit(): void {}

  save(): void {
    if (this.actionForm.valid) {
      const actionData: Partial<ActionRecouvrement> = {
        ...this.actionForm.value,
        dateAction: this.actionForm.value.dateAction.toISOString().split('T')[0]
      };

      const operation = this.isEditMode
        ? this.actionService.updateAction(this.data.action!.id!, actionData)
        : this.actionService.createAction(this.data.dossierId, actionData);

      operation.subscribe({
        next: () => {
          this.snackBar.open(
            `Action ${this.isEditMode ? 'modifiée' : 'créée'} avec succès`,
            'Fermer',
            { duration: 3000 }
          );
          this.dialogRef.close(true);
        },
        error: (err) => {
          this.snackBar.open('Erreur lors de l\'enregistrement', 'Fermer', { duration: 3000 });
          console.error(err);
        }
      });
    }
  }

  close(): void {
    this.dialogRef.close(false);
  }
}
```

Fichier : src/app/components/recouvrement-amiable/action-dialog-amiable/action-dialog-amiable.component.html

```html
<h2 mat-dialog-title>{{ isEditMode ? 'Modifier' : 'Ajouter' }} Action</h2>
<mat-dialog-content>
  <form [formGroup]="actionForm">
    <mat-form-field appearance="outline" class="full-width">
      <mat-label>Type d'action</mat-label>
      <mat-select formControlName="type">
        <mat-option *ngFor="let type of typeActions" [value]="type">
          {{ type }}
        </mat-option>
      </mat-select>
      <mat-error *ngIf="actionForm.get('type')?.hasError('required')">
        Le type est requis
      </mat-error>
    </mat-form-field>
    
    <mat-form-field appearance="outline" class="full-width">
      <mat-label>Date de l'action</mat-label>
      <input matInput [matDatepicker]="picker" formControlName="dateAction" readonly>
      <mat-datepicker-toggle matSuffix [for]="picker"></mat-datepicker-toggle>
      <mat-datepicker #picker></mat-datepicker>
      <mat-error *ngIf="actionForm.get('dateAction')?.hasError('required')">
        La date est requise
      </mat-error>
    </mat-form-field>
    
    <mat-form-field appearance="outline" class="full-width">
      <mat-label>Nombre d'occurrences</mat-label>
      <input matInput type="number" formControlName="nbOccurrences" min="1">
      <mat-hint>Ex: 2 appels, 1 visite, etc.</mat-hint>
      <mat-error *ngIf="actionForm.get('nbOccurrences')?.hasError('required')">
        Le nombre est requis
      </mat-error>
      <mat-error *ngIf="actionForm.get('nbOccurrences')?.hasError('min')">
        Le nombre doit être au moins 1
      </mat-error>
    </mat-form-field>
    
    <mat-form-field appearance="outline" class="full-width">
      <mat-label>Réponse du débiteur</mat-label>
      <mat-select formControlName="reponseDebiteur">
        <mat-option [value]="null">Aucune réponse</mat-option>
        <mat-option [value]="reponses[1]">Positive</mat-option>
        <mat-option [value]="reponses[2]">Négative</mat-option>
      </mat-select>
    </mat-form-field>
    
    <mat-hint class="info-hint">
      <mat-icon>info</mat-icon>
      Le coût sera calculé automatiquement par le système et géré dans le module Finance.
    </mat-hint>
  </form>
</mat-dialog-content>
<mat-dialog-actions align="end">
  <button mat-button (click)="close()">Annuler</button>
  <button mat-raised-button color="primary" (click)="save()" [disabled]="!actionForm.valid">
    {{ isEditMode ? 'Modifier' : 'Ajouter' }}
  </button>
</mat-dialog-actions>
```

IMPORTANT :
- PAS de champ coutUnitaire
- Message informatif sur le calcul automatique
- Validation complète du formulaire
```

---

## 🎯 PARTIE 3 : Composants Finance

### 📋 PROMPT 5 : Créer le Dashboard Chef Financier

```
Dans le projet Angular, créez le composant dashboard complet pour le chef financier :

Fichier : src/app/components/finance/chef-finance-dashboard/chef-finance-dashboard.component.ts

```typescript
import { Component, OnInit } from '@angular/core';
import { FinanceService, StatistiquesCouts, Finance, Page } from 'src/app/services/finance.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-chef-finance-dashboard',
  templateUrl: './chef-finance-dashboard.component.html',
  styleUrls: ['./chef-finance-dashboard.component.css']
})
export class ChefFinanceDashboardComponent implements OnInit {
  statistiques: StatistiquesCouts = {
    totalFraisCreation: 0,
    totalFraisGestion: 0,
    totalActionsAmiable: 0,
    totalActionsJuridique: 0,
    totalAvocat: 0,
    totalHuissier: 0,
    grandTotal: 0
  };
  
  dossiersAvecCouts: Finance[] = [];
  facturesEnAttente: Finance[] = [];
  
  pageSize = 10;
  currentPage = 0;
  totalElements = 0;

  constructor(
    private financeService: FinanceService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadStatistiques();
    this.loadDossiersAvecCouts();
    this.loadFacturesEnAttente();
  }

  loadStatistiques(): void {
    this.financeService.getStatistiquesCouts().subscribe({
      next: (stats) => {
        this.statistiques = stats;
      },
      error: (err) => {
        this.snackBar.open('Erreur lors du chargement des statistiques', 'Fermer', { duration: 3000 });
        console.error(err);
      }
    });
  }

  loadDossiersAvecCouts(): void {
    this.financeService.getDossiersAvecCouts(this.currentPage, this.pageSize).subscribe({
      next: (page: Page<Finance>) => {
        this.dossiersAvecCouts = page.content;
        this.totalElements = page.totalElements;
      },
      error: (err) => {
        this.snackBar.open('Erreur lors du chargement des dossiers', 'Fermer', { duration: 3000 });
        console.error(err);
      }
    });
  }

  loadFacturesEnAttente(): void {
    this.financeService.getFacturesEnAttente().subscribe({
      next: (factures) => {
        this.facturesEnAttente = factures;
      },
      error: (err) => {
        console.error(err);
      }
    });
  }

  onPageChange(event: any): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadDossiersAvecCouts();
  }

  getTotalFacture(finance: Finance): number {
    return (finance.fraisCreationDossier || 0) +
           (finance.fraisGestionDossier || 0) * (finance.dureeGestionMois || 0) +
           (finance.coutActionsAmiable || 0) +
           (finance.coutActionsJuridique || 0) +
           (finance.fraisAvocat || 0) +
           (finance.fraisHuissier || 0);
  }
}
```

Fichier : src/app/components/finance/chef-finance-dashboard/chef-finance-dashboard.component.html

```html
<div class="dashboard-container">
  <h1>Dashboard Finance</h1>
  
  <!-- Statistiques Globales -->
  <div class="stats-grid">
    <mat-card class="stat-card">
      <mat-card-title>Frais Création</mat-card-title>
      <mat-card-content class="stat-value">
        {{ statistiques.totalFraisCreation | number:'1.2-2' }} TND
      </mat-card-content>
    </mat-card>
    
    <mat-card class="stat-card">
      <mat-card-title>Frais Gestion</mat-card-title>
      <mat-card-content class="stat-value">
        {{ statistiques.totalFraisGestion | number:'1.2-2' }} TND
      </mat-card-content>
    </mat-card>
    
    <mat-card class="stat-card">
      <mat-card-title>Actions Amiable</mat-card-title>
      <mat-card-content class="stat-value">
        {{ statistiques.totalActionsAmiable | number:'1.2-2' }} TND
      </mat-card-content>
    </mat-card>
    
    <mat-card class="stat-card">
      <mat-card-title>Actions Juridique</mat-card-title>
      <mat-card-content class="stat-value">
        {{ statistiques.totalActionsJuridique | number:'1.2-2' }} TND
      </mat-card-content>
    </mat-card>
    
    <mat-card class="stat-card">
      <mat-card-title>Frais Avocat</mat-card-title>
      <mat-card-content class="stat-value">
        {{ statistiques.totalAvocat | number:'1.2-2' }} TND
      </mat-card-content>
    </mat-card>
    
    <mat-card class="stat-card">
      <mat-card-title>Frais Huissier</mat-card-title>
      <mat-card-content class="stat-value">
        {{ statistiques.totalHuissier | number:'1.2-2' }} TND
      </mat-card-content>
    </mat-card>
    
    <mat-card class="stat-card total">
      <mat-card-title>Grand Total</mat-card-title>
      <mat-card-content class="stat-value">
        {{ statistiques.grandTotal | number:'1.2-2' }} TND
      </mat-card-content>
    </mat-card>
  </div>
  
  <!-- Liste des Dossiers avec Coûts -->
  <mat-card>
    <mat-card-header>
      <mat-card-title>Dossiers avec Coûts</mat-card-title>
    </mat-card-header>
    <mat-card-content>
      <app-dossiers-couts-table 
        [dossiers]="dossiersAvecCouts"
        (voirDetail)="voirDetail($event)"
        (finaliserFacture)="finaliserFacture($event)">
      </app-dossiers-couts-table>
      
      <mat-paginator
        [length]="totalElements"
        [pageSize]="pageSize"
        [pageIndex]="currentPage"
        (page)="onPageChange($event)">
      </mat-paginator>
    </mat-card-content>
  </mat-card>
  
  <!-- Factures en Attente -->
  <mat-card>
    <mat-card-header>
      <mat-card-title>Factures en Attente ({{ facturesEnAttente.length }})</mat-card-title>
    </mat-card-header>
    <mat-card-content>
      <app-factures-en-attente [factures]="facturesEnAttente"></app-factures-en-attente>
    </mat-card-content>
  </mat-card>
</div>
```

IMPORTANT :
- Afficher tous les coûts
- Format monétaire (TND)
- Pagination pour la liste
```

---

### 📋 PROMPT 6 : Créer le Composant Détail Facture Complète

```
Dans le projet Angular, créez le composant pour afficher le détail complet d'une facture :

Fichier : src/app/components/finance/facture-detail/facture-detail.component.ts

```typescript
import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FinanceService, DetailFacture, Finance, ActionFinance } from 'src/app/services/finance.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-facture-detail',
  templateUrl: './facture-detail.component.html',
  styleUrls: ['./facture-detail.component.css']
})
export class FactureDetailComponent implements OnInit {
  dossierId!: number;
  detailFacture: DetailFacture | null = null;
  finance: Finance | null = null;
  actionsAmiable: ActionFinance[] = [];
  actionsJuridique: ActionFinance[] = [];
  loading = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private financeService: FinanceService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.dossierId = +this.route.snapshot.paramMap.get('id')!;
    this.loadDetailFacture();
    this.loadFinance();
    this.loadActions();
  }

  loadDetailFacture(): void {
    this.loading = true;
    this.financeService.getDetailFacture(this.dossierId).subscribe({
      next: (detail) => {
        this.detailFacture = detail;
        this.loading = false;
      },
      error: (err) => {
        this.snackBar.open('Erreur lors du chargement de la facture', 'Fermer', { duration: 3000 });
        this.loading = false;
        console.error(err);
      }
    });
  }

  loadFinance(): void {
    this.financeService.getFinanceByDossier(this.dossierId).subscribe({
      next: (finance) => {
        this.finance = finance;
      },
      error: (err) => {
        console.error(err);
      }
    });
  }

  loadActions(): void {
    this.financeService.getActionsAvecCouts(this.dossierId).subscribe({
      next: (actions) => {
        // Filtrer par type de recouvrement (nécessite une modification backend ou logique frontend)
        this.actionsAmiable = actions.filter(a => {
          // Logique à adapter selon la structure des données
          return true; // Placeholder
        });
        this.actionsJuridique = actions.filter(a => {
          return true; // Placeholder
        });
      },
      error: (err) => {
        console.error(err);
      }
    });
  }

  recalculerCouts(): void {
    this.loading = true;
    this.financeService.recalculerCouts(this.dossierId).subscribe({
      next: (finance) => {
        this.finance = finance;
        this.loadDetailFacture();
        this.snackBar.open('Coûts recalculés avec succès', 'Fermer', { duration: 3000 });
      },
      error: (err) => {
        this.snackBar.open('Erreur lors du recalcul', 'Fermer', { duration: 3000 });
        this.loading = false;
        console.error(err);
      }
    });
  }

  finaliserFacture(): void {
    if (confirm('Êtes-vous sûr de vouloir finaliser cette facture ?')) {
      this.loading = true;
      this.financeService.finaliserFacture(this.dossierId).subscribe({
        next: (finance) => {
          this.finance = finance;
          this.snackBar.open('Facture finalisée avec succès', 'Fermer', { duration: 3000 });
          this.loading = false;
        },
        error: (err) => {
          this.snackBar.open('Erreur lors de la finalisation', 'Fermer', { duration: 3000 });
          this.loading = false;
          console.error(err);
        }
      });
    }
  }

  imprimerFacture(): void {
    window.print();
  }

  goBack(): void {
    this.router.navigate(['/finance']);
  }
}
```

Fichier : src/app/components/finance/facture-detail/facture-detail.component.html

```html
<div class="facture-container" *ngIf="detailFacture && finance">
  <mat-toolbar>
    <span>Facture - Dossier #{{ dossierId }}</span>
    <span class="spacer"></span>
    <button mat-icon-button (click)="recalculerCouts()" matTooltip="Recalculer les coûts" [disabled]="loading">
      <mat-icon>refresh</mat-icon>
    </button>
    <button mat-icon-button (click)="finaliserFacture()" 
            [disabled]="finance.factureFinalisee || loading" 
            matTooltip="Finaliser la facture">
      <mat-icon>check_circle</mat-icon>
    </button>
    <button mat-icon-button (click)="imprimerFacture()" matTooltip="Imprimer">
      <mat-icon>print</mat-icon>
    </button>
    <button mat-icon-button (click)="goBack()" matTooltip="Retour">
      <mat-icon>arrow_back</mat-icon>
    </button>
  </mat-toolbar>
  
  <div class="facture-content">
    <!-- Section 1: Coûts Création et Gestion -->
    <mat-card>
      <mat-card-title>1. Coûts de Création et Gestion</mat-card-title>
      <mat-card-content>
        <div class="facture-line">
          <span>Frais création dossier</span>
          <span class="amount">{{ detailFacture.fraisCreationDossier | number:'1.2-2' }} TND</span>
        </div>
        <div class="facture-line">
          <span>Frais gestion ({{ finance.dureeGestionMois }} mois × {{ finance.fraisGestionDossier }} TND/mois)</span>
          <span class="amount">{{ detailFacture.coutGestionTotal | number:'1.2-2' }} TND</span>
        </div>
        <div class="facture-line subtotal">
          <span>Sous-total</span>
          <span class="amount">{{ detailFacture.fraisCreationDossier + detailFacture.coutGestionTotal | number:'1.2-2' }} TND</span>
        </div>
      </mat-card-content>
    </mat-card>
    
    <!-- Section 2: Coûts des Actions -->
    <mat-card>
      <mat-card-title>2. Coûts des Actions</mat-card-title>
      <mat-card-content>
        <h3>Actions Recouvrement Amiable</h3>
        <table class="actions-table" *ngIf="actionsAmiable.length > 0">
          <thead>
            <tr>
              <th>Date</th>
              <th>Type</th>
              <th>Occurrences</th>
              <th>Coût Unitaire</th>
              <th>Total</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let action of actionsAmiable">
              <td>{{ action.dateAction | date:'dd/MM/yyyy' }}</td>
              <td>{{ action.type }}</td>
              <td>{{ action.nbOccurrences }}</td>
              <td>{{ action.coutUnitaire | number:'1.2-2' }} TND</td>
              <td>{{ action.nbOccurrences * action.coutUnitaire | number:'1.2-2' }} TND</td>
            </tr>
          </tbody>
        </table>
        <p *ngIf="actionsAmiable.length === 0">Aucune action amiable</p>
        <div class="facture-line subtotal">
          <span>Total actions amiable</span>
          <span class="amount">{{ detailFacture.coutActionsAmiable | number:'1.2-2' }} TND</span>
        </div>
        
        <h3>Actions Recouvrement Juridique</h3>
        <table class="actions-table" *ngIf="actionsJuridique.length > 0">
          <!-- Même structure -->
        </table>
        <p *ngIf="actionsJuridique.length === 0">Aucune action juridique</p>
        <div class="facture-line subtotal">
          <span>Total actions juridique</span>
          <span class="amount">{{ detailFacture.coutActionsJuridique | number:'1.2-2' }} TND</span>
        </div>
        
        <div class="facture-line subtotal">
          <span>Total actions</span>
          <span class="amount">{{ detailFacture.coutActionsAmiable + detailFacture.coutActionsJuridique | number:'1.2-2' }} TND</span>
        </div>
      </mat-card-content>
    </mat-card>
    
    <!-- Section 3: Frais Professionnels -->
    <mat-card>
      <mat-card-title>3. Frais Professionnels</mat-card-title>
      <mat-card-content>
        <div class="facture-line">
          <span>Frais avocat</span>
          <span class="amount">{{ detailFacture.fraisAvocat | number:'1.2-2' }} TND</span>
        </div>
        <div class="facture-line">
          <span>Frais huissier</span>
          <span class="amount">{{ detailFacture.fraisHuissier | number:'1.2-2' }} TND</span>
        </div>
        <div class="facture-line subtotal">
          <span>Sous-total</span>
          <span class="amount">{{ detailFacture.fraisAvocat + detailFacture.fraisHuissier | number:'1.2-2' }} TND</span>
        </div>
      </mat-card-content>
    </mat-card>
    
    <!-- Section 4: Total Facture -->
    <mat-card class="total-card">
      <mat-card-title>Total Facture</mat-card-title>
      <mat-card-content>
        <div class="facture-line total">
          <span>Grand Total</span>
          <span class="amount">{{ detailFacture.totalFacture | number:'1.2-2' }} TND</span>
        </div>
      </mat-card-content>
    </mat-card>
  </div>
</div>

<div *ngIf="loading" class="loading">
  <mat-spinner></mat-spinner>
</div>
```

IMPORTANT :
- Affichage professionnel pour impression
- Tous les coûts détaillés
- Format monétaire
- Actions pour recalculer et finaliser
```

---

## 🎯 PARTIE 4 : Routing et Module

### 📋 PROMPT 7 : Créer le Module et Routing Complet

```
Dans le projet Angular, créez le module et le routing pour organiser toutes les interfaces :

Fichier : src/app/app-routing.module.ts

```typescript
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './guards/auth.guard';
import { RoleGuard } from './guards/role.guard';

// Composants Recouvrement Amiable
import { DossiersAmiableListComponent } from './components/recouvrement-amiable/dossiers-amiable-list/dossiers-amiable-list.component';
import { DossierDetailAmiableComponent } from './components/recouvrement-amiable/dossier-detail-amiable/dossier-detail-amiable.component';

// Composants Finance
import { ChefFinanceDashboardComponent } from './components/finance/chef-finance-dashboard/chef-finance-dashboard.component';
import { FactureDetailComponent } from './components/finance/facture-detail/facture-detail.component';

const routes: Routes = [
  // Routes Chef Recouvrement Amiable
  {
    path: 'recouvrement-amiable',
    component: DossiersAmiableListComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: { 
      roles: ['CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE', 'AGENT_RECOUVREMENT_AMIABLE'] 
    }
  },
  {
    path: 'recouvrement-amiable/dossier/:id',
    component: DossierDetailAmiableComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: { 
      roles: ['CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE', 'AGENT_RECOUVREMENT_AMIABLE'] 
    }
  },
  
  // Routes Chef Financier
  {
    path: 'finance',
    component: ChefFinanceDashboardComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: { 
      roles: ['CHEF_DEPARTEMENT_FINANCE', 'AGENT_FINANCE'] 
    }
  },
  {
    path: 'finance/dossier/:id/facture',
    component: FactureDetailComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: { 
      roles: ['CHEF_DEPARTEMENT_FINANCE', 'AGENT_FINANCE'] 
    }
  },
  
  // Route par défaut
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: '**', redirectTo: '/dashboard' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
```

Fichier : src/app/app.module.ts

```typescript
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule } from '@angular/common/http';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

// Angular Material
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatInputModule } from '@angular/material/input';
import { MatChipsModule } from '@angular/material/chips';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

// Services
import { ActionRecouvrementService } from './services/action-recouvrement.service';
import { FinanceService } from './services/finance.service';

// Composants Recouvrement Amiable
import { DossierActionsAmiableComponent } from './components/recouvrement-amiable/dossier-actions/dossier-actions.component';
import { ActionDialogAmiableComponent } from './components/recouvrement-amiable/action-dialog-amiable/action-dialog-amiable.component';
import { DossierDetailAmiableComponent } from './components/recouvrement-amiable/dossier-detail-amiable/dossier-detail-amiable.component';
import { DossiersAmiableListComponent } from './components/recouvrement-amiable/dossiers-amiable-list/dossiers-amiable-list.component';

// Composants Finance
import { ChefFinanceDashboardComponent } from './components/finance/chef-finance-dashboard/chef-finance-dashboard.component';
import { FactureDetailComponent } from './components/finance/facture-detail/facture-detail.component';
import { DossiersCoutsTableComponent } from './components/finance/dossiers-couts-table/dossiers-couts-table.component';
import { FacturesEnAttenteComponent } from './components/finance/factures-en-attente/factures-en-attente.component';

@NgModule({
  declarations: [
    AppComponent,
    // Composants Recouvrement Amiable
    DossierActionsAmiableComponent,
    ActionDialogAmiableComponent,
    DossierDetailAmiableComponent,
    DossiersAmiableListComponent,
    // Composants Finance
    ChefFinanceDashboardComponent,
    FactureDetailComponent,
    DossiersCoutsTableComponent,
    FacturesEnAttenteComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    HttpClientModule,
    ReactiveFormsModule,
    FormsModule,
    // Angular Material
    MatTableModule,
    MatCardModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatInputModule,
    MatChipsModule,
    MatTabsModule,
    MatToolbarModule,
    MatIconModule,
    MatSnackBarModule,
    MatPaginatorModule,
    MatProgressSpinnerModule
  ],
  providers: [
    ActionRecouvrementService,
    FinanceService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
```

IMPORTANT :
- Séparation claire des routes selon les rôles
- Guards pour la sécurité
- Modules bien organisés
- Tous les modules Angular Material nécessaires
```

---

## ✅ Checklist d'Implémentation

### Services
- [ ] ActionRecouvrementService (sans coûts)
- [ ] FinanceService (complet avec coûts)

### Composants Recouvrement Amiable
- [ ] DossierActionsAmiableComponent
- [ ] ActionDialogAmiableComponent
- [ ] DossierDetailAmiableComponent
- [ ] DossiersAmiableListComponent

### Composants Finance
- [ ] ChefFinanceDashboardComponent
- [ ] FactureDetailComponent
- [ ] DossiersCoutsTableComponent
- [ ] FacturesEnAttenteComponent

### Routing et Module
- [ ] AppRoutingModule avec toutes les routes
- [ ] AppModule avec tous les imports
- [ ] Guards de sécurité (AuthGuard, RoleGuard)

### Styles CSS
- [ ] Styles pour les tableaux
- [ ] Styles pour les cards de statistiques
- [ ] Styles pour la facture (impression)
- [ ] Responsive design

---

## 🎯 Résumé des Prompts

1. **PROMPT 1** : ActionRecouvrementService (Sans Coûts)
2. **PROMPT 2** : FinanceService (Complet avec Coûts)
3. **PROMPT 3** : Composant Tableau Actions (Sans Coûts)
4. **PROMPT 4** : Dialog d'Ajout/Modification d'Action (Sans Coût)
5. **PROMPT 5** : Dashboard Chef Financier
6. **PROMPT 6** : Composant Détail Facture Complète
7. **PROMPT 7** : Module et Routing Complet

---

**Ces prompts couvrent toute l'intégration frontend nécessaire pour consommer les APIs backend et créer des interfaces fonctionnelles, bien organisées et homogènes.**

