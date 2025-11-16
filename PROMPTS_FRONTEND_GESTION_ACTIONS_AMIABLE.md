# 🎨 Prompts Frontend - Gestion Complète des Actions en Recouvrement Amiable

## 🎯 Objectif

Créer les interfaces complètes pour gérer les actions dans le module recouvrement amiable, en consommant correctement les APIs de l'entité Action.

---

## 📋 PROMPT 1 : Créer ActionRecouvrementService (Frontend)

```
Dans le projet Angular, créez un service complet pour gérer les actions dans le recouvrement amiable :

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
  // PAS de coutUnitaire ni totalCout (géré dans Finance)
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
  dernieresActions: ActionRecouvrement[];
}
```

2. Implémenter le service :
```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class ActionRecouvrementService {
  private apiUrl = 'http://localhost:8080/api/actions';

  constructor(private http: HttpClient) {}

  // Récupérer toutes les actions d'un dossier
  getActionsByDossier(dossierId: number): Observable<ActionRecouvrement[]> {
    return this.http.get<ActionRecouvrement[]>(`${this.apiUrl}/dossier/${dossierId}`)
      .pipe(
        map(actions => actions.map(action => ({
          ...action,
          dateAction: new Date(action.dateAction)
        })))
      );
  }

  // Créer une action
  createAction(dossierId: number, action: Partial<ActionRecouvrement>): Observable<ActionRecouvrement> {
    const actionToSend = {
      ...action,
      dossier: { id: dossierId },
      dateAction: action.dateAction instanceof Date 
        ? action.dateAction.toISOString().split('T')[0] 
        : action.dateAction
    };
    return this.http.post<ActionRecouvrement>(this.apiUrl, actionToSend);
  }

  // Modifier une action
  updateAction(actionId: number, action: Partial<ActionRecouvrement>): Observable<ActionRecouvrement> {
    const actionToSend = {
      ...action,
      dateAction: action.dateAction instanceof Date 
        ? action.dateAction.toISOString().split('T')[0] 
        : action.dateAction
    };
    return this.http.put<ActionRecouvrement>(`${this.apiUrl}/${actionId}`, actionToSend);
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
          parType: {},
          dernieresActions: actions
            .sort((a, b) => new Date(b.dateAction).getTime() - new Date(a.dateAction).getTime())
            .slice(0, 5)
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

## 📋 PROMPT 2 : Créer le Composant Liste Actions d'un Dossier

```
Dans le projet Angular, créez un composant pour afficher et gérer les actions d'un dossier :

Fichier : src/app/components/recouvrement-amiable/dossier-actions-list/dossier-actions-list.component.ts

```typescript
import { Component, Input, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActionRecouvrementService, ActionRecouvrement, TypeAction, ReponseDebiteur, StatistiquesActions } from 'src/app/services/action-recouvrement.service';
import { ActionDialogComponent } from '../action-dialog/action-dialog.component';

@Component({
  selector: 'app-dossier-actions-list',
  templateUrl: './dossier-actions-list.component.html',
  styleUrls: ['./dossier-actions-list.component.css']
})
export class DossierActionsListComponent implements OnInit, OnChanges {
  @Input() dossierId!: number;
  
  actions: ActionRecouvrement[] = [];
  statistiques: StatistiquesActions = {
    total: 0,
    positives: 0,
    negatives: 0,
    sansReponse: 0,
    parType: {},
    dernieresActions: []
  };
  
  displayedColumns: string[] = ['dateAction', 'type', 'nbOccurrences', 'reponseDebiteur', 'actions'];
  filteredActions: ActionRecouvrement[] = [];
  selectedTypeFilter: TypeAction | '' = '';
  selectedReponseFilter: ReponseDebiteur | '' = '';
  loading = false;

  constructor(
    private actionService: ActionRecouvrementService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    if (this.dossierId) {
      this.loadActions();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['dossierId'] && this.dossierId) {
      this.loadActions();
    }
  }

  loadActions(): void {
    this.loading = true;
    this.actionService.getActionsByDossier(this.dossierId).subscribe({
      next: (actions) => {
        this.actions = actions.sort((a, b) => 
          new Date(b.dateAction).getTime() - new Date(a.dateAction).getTime()
        );
        this.filteredActions = this.actions;
        this.loadStatistiques();
        this.loading = false;
      },
      error: (err) => {
        this.snackBar.open('Erreur lors du chargement des actions', 'Fermer', { duration: 3000 });
        console.error(err);
        this.loading = false;
      }
    });
  }

  loadStatistiques(): void {
    this.actionService.getStatistiquesActions(this.dossierId).subscribe({
      next: (stats) => {
        this.statistiques = stats;
      },
      error: (err) => {
        console.error(err);
      }
    });
  }

  addAction(): void {
    const dialogRef = this.dialog.open(ActionDialogComponent, {
      width: '600px',
      data: { dossierId: this.dossierId }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadActions();
      }
    });
  }

  editAction(action: ActionRecouvrement): void {
    const dialogRef = this.dialog.open(ActionDialogComponent, {
      width: '600px',
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

  getTypeIcon(type: TypeAction): string {
    const icons: { [key: string]: string } = {
      'APPEL': 'phone',
      'EMAIL': 'email',
      'VISITE': 'home',
      'LETTRE': 'mail',
      'AUTRE': 'more_horiz'
    };
    return icons[type] || 'help';
  }
}
```

IMPORTANT :
- Aucun affichage de coût
- Focus sur le nombre d'occurrences et les réponses
- Tri par date (plus récentes en premier)
```

---

## 📋 PROMPT 3 : Créer le Template HTML du Composant Liste Actions

```
Dans le projet Angular, créez le template HTML pour afficher les actions :

Fichier : src/app/components/recouvrement-amiable/dossier-actions-list/dossier-actions-list.component.html

```html
<div class="actions-container" *ngIf="dossierId">
  <!-- En-tête avec statistiques -->
  <div class="actions-header">
    <h2>Actions du Dossier</h2>
    <button mat-raised-button color="primary" (click)="addAction()">
      <mat-icon>add</mat-icon>
      Ajouter une Action
    </button>
  </div>

  <!-- Statistiques (Cards) -->
  <div class="stats-cards" *ngIf="statistiques.total > 0">
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
    <mat-form-field appearance="outline">
      <mat-label>Filtrer par type</mat-label>
      <mat-select [(value)]="selectedTypeFilter" (selectionChange)="filterByType($event.value)">
        <mat-option value="">Tous les types</mat-option>
        <mat-option value="APPEL">Appel</mat-option>
        <mat-option value="EMAIL">Email</mat-option>
        <mat-option value="VISITE">Visite</mat-option>
        <mat-option value="LETTRE">Lettre</mat-option>
        <mat-option value="AUTRE">Autre</mat-option>
      </mat-select>
    </mat-form-field>

    <mat-form-field appearance="outline">
      <mat-label>Filtrer par réponse</mat-label>
      <mat-select [(value)]="selectedReponseFilter" (selectionChange)="filterByReponse($event.value)">
        <mat-option value="">Toutes les réponses</mat-option>
        <mat-option value="POSITIVE">Positive</mat-option>
        <mat-option value="NEGATIVE">Négative</mat-option>
      </mat-select>
    </mat-form-field>
  </div>

  <!-- Tableau des Actions (SANS colonnes de coût) -->
  <div class="table-container" *ngIf="!loading">
    <table mat-table [dataSource]="filteredActions" class="actions-table">
      <ng-container matColumnDef="dateAction">
        <th mat-header-cell *matHeaderCellDef>Date</th>
        <td mat-cell *matCellDef="let action">
          <mat-icon class="date-icon">calendar_today</mat-icon>
          {{ action.dateAction | date:'dd/MM/yyyy' }}
        </td>
      </ng-container>
      
      <ng-container matColumnDef="type">
        <th mat-header-cell *matHeaderCellDef>Type d'Action</th>
        <td mat-cell *matCellDef="let action">
          <mat-chip [class]="'chip-' + action.type.toLowerCase()">
            <mat-icon>{{ getTypeIcon(action.type) }}</mat-icon>
            {{ action.type }}
          </mat-chip>
        </td>
      </ng-container>
      
      <ng-container matColumnDef="nbOccurrences">
        <th mat-header-cell *matHeaderCellDef>Nombre</th>
        <td mat-cell *matCellDef="let action">
          <span class="occurrences-badge">
            {{ action.nbOccurrences }} {{ getTypeLabel(action.type) }}
          </span>
        </td>
      </ng-container>
      
      <ng-container matColumnDef="reponseDebiteur">
        <th mat-header-cell *matHeaderCellDef>Réponse Débiteur</th>
        <td mat-cell *matCellDef="let action">
          <mat-chip 
            [class.chip-positive]="action.reponseDebiteur === 'POSITIVE'"
            [class.chip-negative]="action.reponseDebiteur === 'NEGATIVE'"
            [class.chip-neutral]="!action.reponseDebiteur">
            <mat-icon *ngIf="action.reponseDebiteur === 'POSITIVE'">check_circle</mat-icon>
            <mat-icon *ngIf="action.reponseDebiteur === 'NEGATIVE'">cancel</mat-icon>
            <mat-icon *ngIf="!action.reponseDebiteur">help</mat-icon>
            {{ action.reponseDebiteur || 'Aucune réponse' }}
          </mat-chip>
        </td>
      </ng-container>
      
      <ng-container matColumnDef="actions">
        <th mat-header-cell *matHeaderCellDef>Actions</th>
        <td mat-cell *matCellDef="let action">
          <button mat-icon-button (click)="editAction(action)" matTooltip="Modifier">
            <mat-icon>edit</mat-icon>
          </button>
          <button mat-icon-button (click)="deleteAction(action.id!)" matTooltip="Supprimer" color="warn">
            <mat-icon>delete</mat-icon>
          </button>
        </td>
      </ng-container>
      
      <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
      <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
      
      <tr class="mat-row" *matNoDataRow>
        <td class="mat-cell" [attr.colspan]="displayedColumns.length">
          <div class="no-data">
            <mat-icon>info</mat-icon>
            <p>Aucune action enregistrée pour ce dossier</p>
            <button mat-raised-button color="primary" (click)="addAction()">
              Ajouter la première action
            </button>
          </div>
        </td>
      </tr>
    </table>
  </div>

  <!-- Loading -->
  <div class="loading-container" *ngIf="loading">
    <mat-spinner diameter="50"></mat-spinner>
    <p>Chargement des actions...</p>
  </div>
</div>
```

IMPORTANT :
- PAS de colonnes coutUnitaire ou totalCout
- Focus sur nbOccurrences
- Statistiques sans coûts
- Design moderne et responsive
```

---

## 📋 PROMPT 4 : Créer le Dialog d'Ajout/Modification d'Action

```
Dans le projet Angular, créez le dialog pour ajouter/modifier une action (SANS coût) :

Fichier : src/app/components/recouvrement-amiable/action-dialog/action-dialog.component.ts

```typescript
import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActionRecouvrementService, ActionRecouvrement, TypeAction, ReponseDebiteur } from 'src/app/services/action-recouvrement.service';

@Component({
  selector: 'app-action-dialog',
  templateUrl: './action-dialog.component.html',
  styleUrls: ['./action-dialog.component.css']
})
export class ActionDialogComponent implements OnInit {
  actionForm: FormGroup;
  isEditMode: boolean = false;
  
  typeActions = Object.values(TypeAction);
  reponses = [null, ReponseDebiteur.POSITIVE, ReponseDebiteur.NEGATIVE];
  
  maxDate = new Date(); // Ne pas permettre les dates futures

  constructor(
    private fb: FormBuilder,
    private actionService: ActionRecouvrementService,
    private dialogRef: MatDialogRef<ActionDialogComponent>,
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
        type: data.action.type,
        dateAction: new Date(data.action.dateAction),
        nbOccurrences: data.action.nbOccurrences,
        reponseDebiteur: data.action.reponseDebiteur
      });
    }
  }

  ngOnInit(): void {}

  save(): void {
    if (this.actionForm.valid) {
      const actionData: Partial<ActionRecouvrement> = {
        type: this.actionForm.value.type,
        dateAction: this.actionForm.value.dateAction,
        nbOccurrences: this.actionForm.value.nbOccurrences,
        reponseDebiteur: this.actionForm.value.reponseDebiteur
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
    } else {
      this.snackBar.open('Veuillez remplir tous les champs obligatoires', 'Fermer', { duration: 3000 });
    }
  }

  close(): void {
    this.dialogRef.close(false);
  }

  getTypeLabel(type: TypeAction): string {
    const labels: { [key: string]: string } = {
      'APPEL': 'Appel téléphonique',
      'EMAIL': 'Email',
      'VISITE': 'Visite sur place',
      'LETTRE': 'Lettre recommandée',
      'AUTRE': 'Autre action'
    };
    return labels[type] || type;
  }
}
```

Fichier : src/app/components/recouvrement-amiable/action-dialog/action-dialog.component.html

```html
<h2 mat-dialog-title>
  <mat-icon>{{ isEditMode ? 'edit' : 'add' }}</mat-icon>
  {{ isEditMode ? 'Modifier' : 'Ajouter' }} une Action
</h2>

<mat-dialog-content>
  <form [formGroup]="actionForm" class="action-form">
    <!-- Type d'action -->
    <mat-form-field appearance="outline" class="full-width">
      <mat-label>Type d'action *</mat-label>
      <mat-select formControlName="type">
        <mat-option *ngFor="let type of typeActions" [value]="type">
          <mat-icon>{{ getTypeIcon(type) }}</mat-icon>
          {{ getTypeLabel(type) }}
        </mat-option>
      </mat-select>
      <mat-icon matPrefix>category</mat-icon>
      <mat-error *ngIf="actionForm.get('type')?.hasError('required')">
        Le type d'action est obligatoire
      </mat-error>
    </mat-form-field>
    
    <!-- Date de l'action -->
    <mat-form-field appearance="outline" class="full-width">
      <mat-label>Date de l'action *</mat-label>
      <input matInput [matDatepicker]="picker" formControlName="dateAction" readonly>
      <mat-datepicker-toggle matSuffix [for]="picker"></mat-datepicker-toggle>
      <mat-datepicker #picker [max]="maxDate"></mat-datepicker>
      <mat-icon matPrefix>calendar_today</mat-icon>
      <mat-error *ngIf="actionForm.get('dateAction')?.hasError('required')">
        La date est obligatoire
      </mat-error>
    </mat-form-field>
    
    <!-- Nombre d'occurrences -->
    <mat-form-field appearance="outline" class="full-width">
      <mat-label>Nombre d'occurrences *</mat-label>
      <input matInput type="number" formControlName="nbOccurrences" min="1">
      <mat-icon matPrefix>repeat</mat-icon>
      <mat-hint>Ex: 2 appels, 1 visite, 3 emails, etc.</mat-hint>
      <mat-error *ngIf="actionForm.get('nbOccurrences')?.hasError('required')">
        Le nombre est obligatoire
      </mat-error>
      <mat-error *ngIf="actionForm.get('nbOccurrences')?.hasError('min')">
        Le nombre doit être au moins 1
      </mat-error>
    </mat-form-field>
    
    <!-- Réponse du débiteur -->
    <mat-form-field appearance="outline" class="full-width">
      <mat-label>Réponse du débiteur</mat-label>
      <mat-select formControlName="reponseDebiteur">
        <mat-option [value]="null">
          <mat-icon>help</mat-icon>
          Aucune réponse
        </mat-option>
        <mat-option [value]="reponses[1]">
          <mat-icon>check_circle</mat-icon>
          Positive (Collaboratif)
        </mat-option>
        <mat-option [value]="reponses[2]">
          <mat-icon>cancel</mat-icon>
          Négative (Non collaboratif)
        </mat-option>
      </mat-select>
      <mat-icon matPrefix>feedback</mat-icon>
      <mat-hint>Indiquez la réponse du débiteur à cette action</mat-hint>
    </mat-form-field>
    
    <!-- Message informatif -->
    <mat-hint class="info-hint">
      <mat-icon>info</mat-icon>
      <span>Le coût sera calculé automatiquement par le système et géré dans le module Finance.</span>
    </mat-hint>
  </form>
</mat-dialog-content>

<mat-dialog-actions align="end">
  <button mat-button (click)="close()">Annuler</button>
  <button mat-raised-button color="primary" (click)="save()" [disabled]="!actionForm.valid">
    <mat-icon>{{ isEditMode ? 'save' : 'add' }}</mat-icon>
    {{ isEditMode ? 'Modifier' : 'Ajouter' }}
  </button>
</mat-dialog-actions>
```

IMPORTANT :
- PAS de champ coutUnitaire
- Message informatif sur le calcul automatique
- Validation complète du formulaire
- Design moderne avec Material Design
```

---

## 📋 PROMPT 5 : Intégrer le Composant Actions dans l'Interface Existante

```
Dans le projet Angular, intégrez le composant de gestion des actions dans l'interface existante de recouvrement amiable :

Fichier : src/app/components/recouvrement-amiable/gestion-actions/gestion-actions.component.ts

Modifiez le composant existant pour ajouter un onglet "Actions" :

```typescript
import { Component, OnInit } from '@angular/core';
import { DossierService } from 'src/app/services/dossier.service';
// ... autres imports

@Component({
  selector: 'app-gestion-actions',
  templateUrl: './gestion-actions.component.html',
  styleUrls: ['./gestion-actions.component.css']
})
export class GestionActionsComponent implements OnInit {
  selectedDossier: any = null;
  selectedTab = 0; // 0 = Liste, 1 = Actions, 2 = Détails

  tabs = [
    { label: 'Liste des Dossiers', icon: 'list' },
    { label: 'Actions', icon: 'history', disabled: true },
    { label: 'Détails', icon: 'info', disabled: true }
  ];

  onDossierSelect(dossier: any): void {
    this.selectedDossier = dossier;
    // Activer les onglets Actions et Détails
    this.tabs[1].disabled = false;
    this.tabs[2].disabled = false;
    this.selectedTab = 1; // Aller directement à l'onglet Actions
  }

  onTabChange(index: number): void {
    this.selectedTab = index;
  }
}
```

Fichier : src/app/components/recouvrement-amiable/gestion-actions/gestion-actions.component.html

```html
<div class="gestion-actions-container">
  <!-- En-tête -->
  <div class="header">
    <h1>Gérez les dossiers et actions du département de recouvrement amiable</h1>
  </div>

  <!-- Onglets -->
  <mat-tab-group [(selectedIndex)]="selectedTab" (selectedIndexChange)="onTabChange($event)">
    <!-- Onglet 1: Liste des Dossiers -->
    <mat-tab [label]="tabs[0].label">
      <div class="tab-content">
        <!-- Recherche -->
        <div class="search-section">
          <mat-form-field appearance="outline" class="search-field">
            <mat-label>Rechercher par numéro, créancier ou débiteur...</mat-label>
            <input matInput (keyup)="onSearch($event)">
            <mat-icon matSuffix>search</mat-icon>
          </mat-form-field>
          <button mat-raised-button color="primary" (click)="search()">
            <mat-icon>search</mat-icon>
            Rechercher
          </button>
          <button mat-icon-button (click)="refresh()">
            <mat-icon>refresh</mat-icon>
          </button>
        </div>

        <!-- Liste des dossiers -->
        <div class="dossiers-list">
          <mat-card *ngFor="let dossier of dossiers" 
                    class="dossier-card" 
                    [class.selected]="selectedDossier?.id === dossier.id"
                    (click)="onDossierSelect(dossier)">
            <mat-card-header>
              <mat-card-title>{{ dossier.numeroDossier }}</mat-card-title>
              <span class="actions-badge" *ngIf="getActionsCount(dossier.id) > 0">
                {{ getActionsCount(dossier.id) }} action(s)
              </span>
            </mat-card-header>
            <mat-card-content>
              <p><strong>Créancier:</strong> {{ dossier.creancier?.nom }}</p>
              <p><strong>Débiteur:</strong> {{ dossier.debiteur?.nom }}</p>
            </mat-card-content>
          </mat-card>
        </div>
      </div>
    </mat-tab>

    <!-- Onglet 2: Actions (NOUVEAU) -->
    <mat-tab [label]="tabs[1].label" [disabled]="tabs[1].disabled">
      <div class="tab-content" *ngIf="selectedDossier">
        <app-dossier-actions-list [dossierId]="selectedDossier.id"></app-dossier-actions-list>
      </div>
      <div class="no-selection" *ngIf="!selectedDossier">
        <mat-icon>info</mat-icon>
        <p>Sélectionnez un dossier pour voir ses actions</p>
      </div>
    </mat-tab>

    <!-- Onglet 3: Détails -->
    <mat-tab [label]="tabs[2].label" [disabled]="tabs[2].disabled">
      <div class="tab-content" *ngIf="selectedDossier">
        <!-- Détails du dossier -->
        <mat-card>
          <mat-card-header>
            <mat-card-title>Dossier: {{ selectedDossier.numeroDossier }}</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <p><strong>Créancier:</strong> {{ selectedDossier.creancier?.nom }}</p>
            <p><strong>Débiteur:</strong> {{ selectedDossier.debiteur?.nom }}</p>
            <p><strong>Montant:</strong> {{ selectedDossier.montantCreance }} TND</p>
            <!-- Autres détails -->
          </mat-card-content>
          <mat-card-actions>
            <button mat-raised-button color="accent" (click)="affecterAuJuridique()">
              <mat-icon>gavel</mat-icon>
              Affecter au Juridique
            </button>
            <button mat-raised-button color="warn" (click)="cloturerDossier()">
              <mat-icon>lock</mat-icon>
              Clôturer le Dossier
            </button>
          </mat-card-actions>
        </mat-card>
      </div>
    </mat-tab>
  </mat-tab-group>
</div>
```

IMPORTANT :
- Intégrer le composant app-dossier-actions-list dans l'onglet Actions
- Activer l'onglet Actions quand un dossier est sélectionné
- Afficher le nombre d'actions dans la liste des dossiers
```

---

## 📋 PROMPT 6 : Créer le Composant Recommandations (Analyse des Actions)

```
Dans le projet Angular, créez un composant pour afficher les recommandations basées sur les actions :

Fichier : src/app/components/recouvrement-amiable/dossier-recommandations/dossier-recommandations.component.ts

```typescript
import { Component, Input, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { ActionRecouvrementService, StatistiquesActions, ReponseDebiteur } from 'src/app/services/action-recouvrement.service';
import { DossierService } from 'src/app/services/dossier.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-dossier-recommandations',
  templateUrl: './dossier-recommandations.component.html',
  styleUrls: ['./dossier-recommandations.component.css']
})
export class DossierRecommandationsComponent implements OnInit, OnChanges {
  @Input() dossierId!: number;
  
  statistiques: StatistiquesActions | null = null;
  recommandationFinance = false;
  recommandationJuridique = false;
  pourcentagePositif = 0;

  constructor(
    private actionService: ActionRecouvrementService,
    private dossierService: DossierService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (this.dossierId) {
      this.loadRecommandations();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['dossierId'] && this.dossierId) {
      this.loadRecommandations();
    }
  }

  loadRecommandations(): void {
    this.actionService.getStatistiquesActions(this.dossierId).subscribe({
      next: (stats) => {
        this.statistiques = stats;
        this.analyserRecommandations(stats);
      },
      error: (err) => {
        console.error(err);
      }
    });
  }

  analyserRecommandations(stats: StatistiquesActions): void {
    const totalAvecReponse = stats.positives + stats.negatives;
    
    if (totalAvecReponse > 0) {
      this.pourcentagePositif = (stats.positives / totalAvecReponse) * 100;
    }

    // Recommandation Finance : 2+ réponses positives récentes (30 derniers jours)
    const actionsPositivesRecentes = stats.dernieresActions
      .filter(a => a.reponseDebiteur === ReponseDebiteur.POSITIVE)
      .filter(a => {
        const dateAction = new Date(a.dateAction);
        const joursDepuis = (Date.now() - dateAction.getTime()) / (1000 * 60 * 60 * 24);
        return joursDepuis <= 30;
      });

    this.recommandationFinance = actionsPositivesRecentes.length >= 2;

    // Recommandation Juridique : 3+ réponses négatives OU aucune réponse après 5 actions
    this.recommandationJuridique = 
      stats.negatives >= 3 || 
      (stats.total >= 5 && stats.sansReponse >= 3);
  }

  passerAuFinance(): void {
    if (confirm('Êtes-vous sûr de vouloir passer ce dossier au Finance ?')) {
      // Appeler l'API pour passer au finance
      // this.dossierService.passerAuFinance(this.dossierId).subscribe(...);
      alert('Fonctionnalité à implémenter : Passage au Finance');
    }
  }

  passerAuJuridique(): void {
    if (confirm('Êtes-vous sûr de vouloir passer ce dossier au Recouvrement Juridique ?')) {
      this.dossierService.affecterAuRecouvrementJuridique(this.dossierId).subscribe({
        next: () => {
          alert('Dossier affecté au recouvrement juridique avec succès');
          this.router.navigate(['/recouvrement-amiable']);
        },
        error: (err) => {
          alert('Erreur lors de l\'affectation au juridique');
          console.error(err);
        }
      });
    }
  }
}
```

Fichier : src/app/components/recouvrement-amiable/dossier-recommandations/dossier-recommandations.component.html

```html
<div class="recommandations-container" *ngIf="statistiques">
  <h2>Analyse et Recommandations</h2>

  <!-- Analyse de Collaboration -->
  <mat-card class="analysis-card">
    <mat-card-header>
      <mat-card-title>
        <mat-icon>analytics</mat-icon>
        Analyse de Collaboration
      </mat-card-title>
    </mat-card-header>
    <mat-card-content>
      <div class="collaboration-stats">
        <div class="stat-item">
          <span class="label">Taux de réponse positive:</span>
          <span class="value positive">{{ pourcentagePositif | number:'1.0-0' }}%</span>
        </div>
        <div class="stat-item">
          <span class="label">Total actions:</span>
          <span class="value">{{ statistiques.total }}</span>
        </div>
        <div class="stat-item">
          <span class="label">Réponses positives:</span>
          <span class="value positive">{{ statistiques.positives }}</span>
        </div>
        <div class="stat-item">
          <span class="label">Réponses négatives:</span>
          <span class="value negative">{{ statistiques.negatives }}</span>
        </div>
      </div>
      
      <div class="collaboration-status">
        <mat-chip [class]="pourcentagePositif >= 50 ? 'chip-positive' : 'chip-negative'">
          <mat-icon>{{ pourcentagePositif >= 50 ? 'check_circle' : 'cancel' }}</mat-icon>
          {{ pourcentagePositif >= 50 ? 'Débiteur Collaboratif' : 'Débiteur Non Collaboratif' }}
        </mat-chip>
      </div>
    </mat-card-content>
  </mat-card>

  <!-- Recommandation Finance -->
  <mat-card class="recommendation-card finance" *ngIf="recommandationFinance">
    <mat-card-header>
      <mat-card-title>
        <mat-icon>attach_money</mat-icon>
        Recommandation : Passer au Finance
      </mat-card-title>
    </mat-card-header>
    <mat-card-content>
      <p>Le débiteur semble prêt à payer. Plusieurs réponses positives récentes indiquent une collaboration.</p>
      <ul>
        <li *ngFor="let action of statistiques.dernieresActions | slice:0:3">
          {{ action.type }} - {{ action.dateAction | date:'dd/MM/yyyy' }} - Réponse: {{ action.reponseDebiteur }}
        </li>
      </ul>
      <button mat-raised-button color="primary" (click)="passerAuFinance()">
        <mat-icon>arrow_forward</mat-icon>
        Passer au Finance
      </button>
    </mat-card-content>
  </mat-card>

  <!-- Recommandation Juridique -->
  <mat-card class="recommendation-card juridique" *ngIf="recommandationJuridique">
    <mat-card-header>
      <mat-card-title>
        <mat-icon>gavel</mat-icon>
        Recommandation : Passer au Recouvrement Juridique
      </mat-card-title>
    </mat-card-header>
    <mat-card-content>
      <p>Le débiteur ne répond pas favorablement aux actions de recouvrement amiable.</p>
      <ul>
        <li *ngIf="statistiques.negatives >= 3">
          {{ statistiques.negatives }} réponses négatives enregistrées
        </li>
        <li *ngIf="statistiques.total >= 5 && statistiques.sansReponse >= 3">
          Aucune réponse après {{ statistiques.total }} actions
        </li>
      </ul>
      <button mat-raised-button color="accent" (click)="passerAuJuridique()">
        <mat-icon>arrow_forward</mat-icon>
        Passer au Recouvrement Juridique
      </button>
    </mat-card-content>
  </mat-card>

  <!-- Aucune recommandation -->
  <mat-card class="recommendation-card neutral" *ngIf="!recommandationFinance && !recommandationJuridique">
    <mat-card-content>
      <mat-icon>info</mat-icon>
      <p>Continuez les actions de recouvrement amiable. Pas assez de données pour une recommandation.</p>
    </mat-card-content>
  </mat-card>
</div>
```

IMPORTANT :
- Analyse automatique des actions
- Recommandations basées sur les réponses
- Boutons d'action rapide pour passer au finance/juridique
```

---

## 📋 PROMPT 7 : Mettre à Jour le Service DossierService (Frontend)

```
Dans le projet Angular, mettez à jour DossierService pour inclure les méthodes d'affectation :

Fichier : src/app/services/dossier.service.ts

Ajoutez ces méthodes :

```typescript
// Affecter un dossier au recouvrement amiable
affecterAuRecouvrementAmiable(dossierId: number): Observable<Dossier> {
  return this.http.put<Dossier>(`${this.apiUrl}/${dossierId}/affecter/recouvrement-amiable`, {});
}

// Affecter un dossier au recouvrement juridique
affecterAuRecouvrementJuridique(dossierId: number): Observable<Dossier> {
  return this.http.put<Dossier>(`${this.apiUrl}/${dossierId}/affecter/recouvrement-juridique`, {});
}

// Clôturer un dossier
cloturerDossier(dossierId: number): Observable<Dossier> {
  return this.http.put<Dossier>(`${this.apiUrl}/${dossierId}/cloturer`, {});
}

// Récupérer les dossiers du recouvrement amiable
getDossiersRecouvrementAmiable(page: number = 0, size: number = 10, sort: string = 'dateCreation'): Observable<Page<Dossier>> {
  const params = new HttpParams()
    .set('page', page.toString())
    .set('size', size.toString())
    .set('sort', sort);
  return this.http.get<Page<Dossier>>(`${this.apiUrl}/recouvrement-amiable`, { params });
}
```

IMPORTANT :
- Utiliser les endpoints backend corrects
- Gérer les erreurs avec catchError
```

---

## 📋 PROMPT 8 : Créer les Styles CSS pour les Composants Actions

```
Dans le projet Angular, créez les styles CSS pour les composants de gestion des actions :

Fichier : src/app/components/recouvrement-amiable/dossier-actions-list/dossier-actions-list.component.css

```css
.actions-container {
  padding: 20px;
}

.actions-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  text-align: center;
}

.stat-card.positive {
  border-left: 4px solid #4caf50;
}

.stat-card.negative {
  border-left: 4px solid #f44336;
}

.stat-value {
  font-size: 2em;
  font-weight: bold;
  color: #673ab7;
}

.filters-section {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.actions-table {
  width: 100%;
}

.chip-positive {
  background-color: #4caf50;
  color: white;
}

.chip-negative {
  background-color: #f44336;
  color: white;
}

.chip-neutral {
  background-color: #9e9e9e;
  color: white;
}

.occurrences-badge {
  background-color: #e3f2fd;
  padding: 4px 12px;
  border-radius: 12px;
  font-weight: 500;
}

.no-data {
  text-align: center;
  padding: 40px;
  color: #9e9e9e;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px;
}
```

IMPORTANT :
- Design moderne et responsive
- Couleurs cohérentes avec Material Design
- Badges et chips pour les statuts
```

---

## 📋 PROMPT 9 : Intégrer dans le Routing

```
Dans le projet Angular, mettez à jour le routing pour inclure les nouvelles routes :

Fichier : src/app/app-routing.module.ts

```typescript
{
  path: 'chef-amiable/gestion-actions',
  component: GestionActionsComponent,
  canActivate: [AuthGuard, RoleGuard],
  data: { roles: ['CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE', 'AGENT_RECOUVREMENT_AMIABLE'] }
},
{
  path: 'chef-amiable/dossier/:id/actions',
  component: DossierActionsListComponent,
  canActivate: [AuthGuard, RoleGuard],
  data: { roles: ['CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE', 'AGENT_RECOUVREMENT_AMIABLE'] }
}
```

IMPORTANT :
- Routes protégées par AuthGuard et RoleGuard
- Rôles appropriés pour accès
```

---

## 📋 PROMPT 10 : Mettre à Jour le Module Angular

```
Dans le projet Angular, mettez à jour app.module.ts pour inclure tous les nouveaux composants :

Fichier : src/app/app.module.ts

```typescript
// Imports nécessaires
import { MatTableModule } from '@angular/material/table';
import { MatDialogModule } from '@angular/material/dialog';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatChipsModule } from '@angular/material/chips';
import { MatTabsModule } from '@angular/material/tabs';

// Composants
import { DossierActionsListComponent } from './components/recouvrement-amiable/dossier-actions-list/dossier-actions-list.component';
import { ActionDialogComponent } from './components/recouvrement-amiable/action-dialog/action-dialog.component';
import { DossierRecommandationsComponent } from './components/recouvrement-amiable/dossier-recommandations/dossier-recommandations.component';

@NgModule({
  declarations: [
    // ... autres composants
    DossierActionsListComponent,
    ActionDialogComponent,
    DossierRecommandationsComponent
  ],
  imports: [
    // ... autres imports
    MatTableModule,
    MatDialogModule,
    MatDatepickerModule,
    MatChipsModule,
    MatTabsModule
  ]
})
```

IMPORTANT :
- Tous les modules Angular Material nécessaires
- Tous les composants déclarés
```

---

## ✅ Checklist d'Implémentation

- [ ] Créer ActionRecouvrementService
- [ ] Créer DossierActionsListComponent
- [ ] Créer ActionDialogComponent
- [ ] Créer DossierRecommandationsComponent
- [ ] Intégrer dans GestionActionsComponent
- [ ] Mettre à jour DossierService
- [ ] Ajouter les styles CSS
- [ ] Mettre à jour le routing
- [ ] Mettre à jour app.module.ts
- [ ] Tester toutes les fonctionnalités

---

**Ces prompts vous permettront de créer une interface complète pour gérer les actions dans le recouvrement amiable, en consommant correctement toutes les APIs de l'entité Action.**

