# 🎨 Prompts Frontend Complets - Finance et Recouvrement

## 📋 Vue d'Ensemble

Ce document contient tous les prompts nécessaires pour créer les interfaces frontend complètes pour :
1. **Chef Recouvrement Amiable et ses Agents** : Interfaces sans coûts, uniquement nombre d'occurrences
2. **Chef Financier et ses Agents** : Interfaces complètes avec tous les coûts et facturation

---

## 🎯 PARTIE 1 : Interfaces Chef Recouvrement Amiable

### 📋 PROMPT 1 : Créer ActionService (Frontend) - Version Recouvrement Amiable

```
Dans le projet Angular, créez un service ActionService spécifique pour le recouvrement amiable :

Fichier : src/app/services/action-recouvrement.service.ts

1. Créer l'interface ActionRecouvrement (SANS coûts) :
```typescript
export interface ActionRecouvrement {
  id?: number;
  type: TypeAction;
  reponseDebiteur: ReponseDebiteur | null;
  dateAction: Date;
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
```

2. Méthodes du service :
```typescript
@Injectable({ providedIn: 'root' })
export class ActionRecouvrementService {
  private apiUrl = 'http://localhost:8080/api/actions';

  // Récupérer toutes les actions d'un dossier (sans coûts)
  getActionsByDossier(dossierId: number): Observable<ActionRecouvrement[]>
  
  // Créer une action (sans coût unitaire)
  createAction(dossierId: number, action: ActionRecouvrement): Observable<ActionRecouvrement>
  
  // Modifier une action
  updateAction(actionId: number, action: ActionRecouvrement): Observable<ActionRecouvrement>
  
  // Supprimer une action
  deleteAction(actionId: number): Observable<void>
  
  // Filtrer par type
  getActionsByType(dossierId: number, type: TypeAction): Observable<ActionRecouvrement[]>
  
  // Filtrer par réponse
  getActionsByReponse(dossierId: number, reponse: ReponseDebiteur): Observable<ActionRecouvrement[]>
  
  // Statistiques (sans coûts)
  getStatistiquesActions(dossierId: number): Observable<{
    total: number;
    positives: number;
    negatives: number;
    sansReponse: number;
    parType: { [key: string]: number };
  }>
}
```

IMPORTANT :
- Les coûts ne sont JAMAIS exposés dans ce service
- Le backend calcule automatiquement les coûts et les met dans Finance
- Les interfaces utilisent uniquement nbOccurrences
```

---

### 📋 PROMPT 2 : Créer le Composant Tableau Actions (Recouvrement Amiable)

```
Dans le projet Angular, créez le composant pour afficher les actions d'un dossier (sans coûts) :

Fichier : src/app/components/dossier-actions-amiable/dossier-actions-amiable.component.ts

Fonctionnalités :

1. Affichage du tableau :
   - Colonnes : Date, Type, Nombre d'occurrences, Réponse Débiteur, Actions
   - PAS de colonnes de coût
   - Tri par date (plus récentes en premier)
   - Filtres : par type, par réponse, par date

2. Statistiques affichées :
   - Nombre total d'actions
   - Nombre d'actions positives
   - Nombre d'actions négatives
   - Nombre d'actions sans réponse
   - Répartition par type (graphique)

3. Actions disponibles :
   - Bouton "Ajouter Action" (ouvre dialog)
   - Bouton "Modifier" sur chaque ligne
   - Bouton "Supprimer" sur chaque ligne (avec confirmation)

4. Méthodes :
```typescript
export class DossierActionsAmiableComponent implements OnInit {
  @Input() dossierId!: number;
  actions: ActionRecouvrement[] = [];
  statistiques: any = {};
  
  loadActions(): void
  addAction(): void
  editAction(action: ActionRecouvrement): void
  deleteAction(actionId: number): void
  filterByType(type: TypeAction): void
  filterByReponse(reponse: ReponseDebiteur): void
}
```

IMPORTANT :
- Aucun affichage de coût
- Focus sur le nombre d'occurrences et les réponses
```

---

### 📋 PROMPT 3 : Créer le Template HTML du Tableau Actions (Sans Coûts)

```
Dans le projet Angular, créez le template HTML pour le composant dossier-actions-amiable :

Fichier : src/app/components/dossier-actions-amiable/dossier-actions-amiable.component.html

Structure :

1. Section Statistiques (Cards) :
```html
<div class="stats-cards">
  <mat-card>
    <mat-card-title>Total Actions</mat-card-title>
    <mat-card-content>{{ statistiques.total }}</mat-card-content>
  </mat-card>
  <mat-card class="positive">
    <mat-card-title>Positives</mat-card-title>
    <mat-card-content>{{ statistiques.positives }}</mat-card-content>
  </mat-card>
  <mat-card class="negative">
    <mat-card-title>Négatives</mat-card-title>
    <mat-card-content>{{ statistiques.negatives }}</mat-card-content>
  </mat-card>
  <mat-card>
    <mat-card-title>Sans Réponse</mat-card-title>
    <mat-card-content>{{ statistiques.sansReponse }}</mat-card-content>
  </mat-card>
</div>
```

2. Filtres :
```html
<mat-form-field>
  <mat-label>Filtrer par type</mat-label>
  <mat-select (selectionChange)="filterByType($event.value)">
    <mat-option value="">Tous</mat-option>
    <mat-option value="APPEL">Appel</mat-option>
    <mat-option value="EMAIL">Email</mat-option>
    <mat-option value="VISITE">Visite</mat-option>
    <mat-option value="LETTRE">Lettre</mat-option>
  </mat-select>
</mat-form-field>
```

3. Tableau (SANS colonnes de coût) :
```html
<table mat-table [dataSource]="actions">
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
      <mat-chip [class.positive]="action.reponseDebiteur === 'POSITIVE'"
                [class.negative]="action.reponseDebiteur === 'NEGATIVE'">
        {{ action.reponseDebiteur || 'Aucune' }}
      </mat-chip>
    </td>
  </ng-container>
  
  <ng-container matColumnDef="actions">
    <th mat-header-cell *matHeaderCellDef>Actions</th>
    <td mat-cell *matCellDef="let action">
      <button mat-icon-button (click)="editAction(action)">
        <mat-icon>edit</mat-icon>
      </button>
      <button mat-icon-button (click)="deleteAction(action.id!)">
        <mat-icon>delete</mat-icon>
      </button>
    </td>
  </ng-container>
  
  <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
  <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
</table>
```

IMPORTANT :
- PAS de colonnes coutUnitaire ou totalCout
- Focus sur nbOccurrences
```

---

### 📋 PROMPT 4 : Créer le Dialog d'Ajout/Modification d'Action (Sans Coût)

```
Dans le projet Angular, créez le dialog pour ajouter/modifier une action (sans coût) :

Fichier : src/app/components/action-dialog-amiable/action-dialog-amiable.component.ts

Formulaire (SANS champ coût) :

```typescript
export class ActionDialogAmiableComponent {
  actionForm: FormGroup;
  
  constructor(
    private fb: FormBuilder,
    @Inject(MAT_DIALOG_DATA) public data: { dossierId: number, action?: ActionRecouvrement }
  ) {
    this.actionForm = this.fb.group({
      type: ['', Validators.required],
      dateAction: [new Date(), Validators.required],
      nbOccurrences: [1, [Validators.required, Validators.min(1)]],
      reponseDebiteur: [null]
    });
    
    if (data.action) {
      this.actionForm.patchValue(data.action);
    }
  }
  
  save(): void {
    if (this.actionForm.valid) {
      const action: ActionRecouvrement = {
        ...this.actionForm.value,
        dossier: { id: this.data.dossierId }
      };
      // Envoyer au backend
    }
  }
}
```

Template HTML :
```html
<h2 mat-dialog-title>{{ data.action ? 'Modifier' : 'Ajouter' }} Action</h2>
<mat-dialog-content>
  <form [formGroup]="actionForm">
    <mat-form-field>
      <mat-label>Type d'action</mat-label>
      <mat-select formControlName="type">
        <mat-option value="APPEL">Appel</mat-option>
        <mat-option value="EMAIL">Email</mat-option>
        <mat-option value="VISITE">Visite</mat-option>
        <mat-option value="LETTRE">Lettre</mat-option>
        <mat-option value="AUTRE">Autre</mat-option>
      </mat-select>
    </mat-form-field>
    
    <mat-form-field>
      <mat-label>Date de l'action</mat-label>
      <input matInput [matDatepicker]="picker" formControlName="dateAction">
      <mat-datepicker-toggle matSuffix [for]="picker"></mat-datepicker-toggle>
      <mat-datepicker #picker></mat-datepicker>
    </mat-form-field>
    
    <mat-form-field>
      <mat-label>Nombre d'occurrences</mat-label>
      <input matInput type="number" formControlName="nbOccurrences" min="1">
      <mat-hint>Ex: 2 appels, 1 visite, etc.</mat-hint>
    </mat-form-field>
    
    <mat-form-field>
      <mat-label>Réponse du débiteur</mat-label>
      <mat-select formControlName="reponseDebiteur">
        <mat-option [value]="null">Aucune réponse</mat-option>
        <mat-option value="POSITIVE">Positive</mat-option>
        <mat-option value="NEGATIVE">Négative</mat-option>
      </mat-select>
    </mat-form-field>
    
    <mat-hint class="info-hint">
      ℹ️ Le coût sera calculé automatiquement par le système et géré dans le module Finance.
    </mat-hint>
  </form>
</mat-dialog-content>
<mat-dialog-actions>
  <button mat-button (click)="close()">Annuler</button>
  <button mat-button color="primary" (click)="save()" [disabled]="!actionForm.valid">
    {{ data.action ? 'Modifier' : 'Ajouter' }}
  </button>
</mat-dialog-actions>
```

IMPORTANT :
- PAS de champ coutUnitaire
- Message informatif sur le calcul automatique
```

---

### 📋 PROMPT 5 : Créer le Composant Détails Dossier avec Actions (Recouvrement Amiable)

```
Dans le projet Angular, créez le composant détails dossier pour le recouvrement amiable :

Fichier : src/app/components/dossier-detail-amiable/dossier-detail-amiable.component.ts

Structure avec onglets :

```typescript
export class DossierDetailAmiableComponent implements OnInit {
  dossierId!: number;
  dossier: Dossier | null = null;
  selectedTab = 0;
  
  tabs = [
    { label: 'Informations', icon: 'info' },
    { label: 'Actions', icon: 'history' },
    { label: 'Recommandations', icon: 'lightbulb' }
  ];
  
  loadDossier(): void {
    // GET /api/dossiers/{id}
  }
  
  onTabChange(index: number): void {
    this.selectedTab = index;
  }
}
```

Template HTML :
```html
<mat-toolbar>
  <span>Dossier #{{ dossier?.numeroDossier }}</span>
  <span class="spacer"></span>
  <button mat-icon-button (click)="goBack()">
    <mat-icon>close</mat-icon>
  </button>
</mat-toolbar>

<mat-tab-group [(selectedIndex)]="selectedTab">
  <mat-tab label="Informations">
    <app-dossier-info [dossier]="dossier"></app-dossier-info>
  </mat-tab>
  
  <mat-tab label="Actions">
    <app-dossier-actions-amiable [dossierId]="dossierId"></app-dossier-actions-amiable>
  </mat-tab>
  
  <mat-tab label="Recommandations">
    <app-recommandations-amiable [dossierId]="dossierId"></app-recommandations-amiable>
  </mat-tab>
</mat-tab-group>
```

IMPORTANT :
- Intégrer le composant actions créé précédemment
- Pas d'affichage de coûts
```

---

## 🎯 PARTIE 2 : Interfaces Chef Financier

### 📋 PROMPT 6 : Créer FinanceService (Frontend) - Version Complète

```
Dans le projet Angular, créez un service FinanceService complet pour le chef financier :

Fichier : src/app/services/finance.service.ts

Interfaces :
```typescript
export interface Finance {
  id?: number;
  devise: string;
  dateOperation: Date;
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
  dateFacturation?: Date;
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
```

Méthodes du service :
```typescript
@Injectable({ providedIn: 'root' })
export class FinanceService {
  private apiUrl = 'http://localhost:8080/api/finances';

  // Récupérer Finance par dossier
  getFinanceByDossier(dossierId: number): Observable<Finance>
  
  // Détail facture complète
  getDetailFacture(dossierId: number): Observable<DetailFacture>
  
  // Coûts détaillés par dossier
  getCoutsParDossier(dossierId: number): Observable<any>
  
  // Recalculer tous les coûts
  recalculerCouts(dossierId: number): Observable<Finance>
  
  // Statistiques globales
  getStatistiquesCouts(): Observable<StatistiquesCouts>
  
  // Liste paginée des dossiers avec coûts
  getDossiersAvecCouts(page: number, size: number, sort?: string): Observable<Page<Finance>>
  
  // Factures en attente
  getFacturesEnAttente(): Observable<Finance[]>
  
  // Finaliser une facture
  finaliserFacture(dossierId: number): Observable<Finance>
  
  // Actions avec coûts détaillés
  getActionsAvecCouts(dossierId: number): Observable<ActionFinance[]>
}
```

IMPORTANT :
- Toutes les méthodes retournent les coûts complets
- Utiliser les endpoints /api/finances/*
```

---

### 📋 PROMPT 7 : Créer le Dashboard Chef Financier

```
Dans le projet Angular, créez le composant dashboard complet pour le chef financier :

Fichier : src/app/components/chef-finance-dashboard/chef-finance-dashboard.component.ts

Fonctionnalités :

1. Vue d'ensemble (Cards) :
```typescript
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
  
  loadStatistiques(): void {
    this.financeService.getStatistiquesCouts().subscribe(
      stats => this.statistiques = stats
    );
  }
  
  loadDossiersAvecCouts(): void {
    this.financeService.getDossiersAvecCouts(0, 10).subscribe(
      page => this.dossiersAvecCouts = page.content
    );
  }
  
  loadFacturesEnAttente(): void {
    this.financeService.getFacturesEnAttente().subscribe(
      factures => this.facturesEnAttente = factures
    );
  }
}
```

2. Template HTML avec Cards :
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
      <app-dossiers-couts-table [dossiers]="dossiersAvecCouts"></app-dossiers-couts-table>
    </mat-card-content>
  </mat-card>
  
  <!-- Factures en Attente -->
  <mat-card>
    <mat-card-header>
      <mat-card-title>Factures en Attente</mat-card-title>
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
- Responsive design
```

---

### 📋 PROMPT 8 : Créer le Composant Détail Facture Complète

```
Dans le projet Angular, créez le composant pour afficher le détail complet d'une facture :

Fichier : src/app/components/facture-detail/facture-detail.component.ts

Structure :

```typescript
export class FactureDetailComponent implements OnInit {
  @Input() dossierId!: number;
  detailFacture: DetailFacture | null = null;
  finance: Finance | null = null;
  actionsAmiable: ActionFinance[] = [];
  actionsJuridique: ActionFinance[] = [];
  
  loadDetailFacture(): void {
    this.financeService.getDetailFacture(this.dossierId).subscribe(
      detail => this.detailFacture = detail
    );
  }
  
  loadActions(): void {
    this.financeService.getActionsAvecCouts(this.dossierId).subscribe(
      actions => {
        this.actionsAmiable = actions.filter(a => a.typeRecouvrement === 'AMIABLE');
        this.actionsJuridique = actions.filter(a => a.typeRecouvrement === 'JURIDIQUE');
      }
    );
  }
  
  recalculerCouts(): void {
    this.financeService.recalculerCouts(this.dossierId).subscribe(
      finance => {
        this.finance = finance;
        this.loadDetailFacture();
      }
    );
  }
  
  finaliserFacture(): void {
    this.financeService.finaliserFacture(this.dossierId).subscribe(
      finance => {
        this.finance = finance;
        // Afficher message de succès
      }
    );
  }
  
  imprimerFacture(): void {
    window.print();
  }
}
```

Template HTML :
```html
<div class="facture-container">
  <mat-toolbar>
    <span>Facture - Dossier #{{ dossierId }}</span>
    <span class="spacer"></span>
    <button mat-icon-button (click)="recalculerCouts()" matTooltip="Recalculer les coûts">
      <mat-icon>refresh</mat-icon>
    </button>
    <button mat-icon-button (click)="finaliserFacture()" [disabled]="finance?.factureFinalisee">
      <mat-icon>check_circle</mat-icon>
    </button>
    <button mat-icon-button (click)="imprimerFacture()">
      <mat-icon>print</mat-icon>
    </button>
  </mat-toolbar>
  
  <div class="facture-content" *ngIf="detailFacture">
    <!-- Section 1: Coûts Création et Gestion -->
    <mat-card>
      <mat-card-title>1. Coûts de Création et Gestion</mat-card-title>
      <mat-card-content>
        <div class="facture-line">
          <span>Frais création dossier</span>
          <span class="amount">{{ detailFacture.fraisCreationDossier | number:'1.2-2' }} TND</span>
        </div>
        <div class="facture-line">
          <span>Frais gestion ({{ finance?.dureeGestionMois }} mois × {{ finance?.fraisGestionDossier }} TND/mois)</span>
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
        <table class="actions-table">
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
        <div class="facture-line subtotal">
          <span>Total actions amiable</span>
          <span class="amount">{{ detailFacture.coutActionsAmiable | number:'1.2-2' }} TND</span>
        </div>
        
        <h3>Actions Recouvrement Juridique</h3>
        <table class="actions-table">
          <!-- Même structure pour juridique -->
        </table>
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
```

IMPORTANT :
- Affichage professionnel pour impression
- Tous les coûts détaillés
- Format monétaire
```

---

### 📋 PROMPT 9 : Créer le Tableau des Dossiers avec Coûts

```
Dans le projet Angular, créez le composant tableau pour afficher les dossiers avec leurs coûts :

Fichier : src/app/components/dossiers-couts-table/dossiers-couts-table.component.ts

```typescript
export class DossiersCoutsTableComponent implements OnInit {
  @Input() dossiers: Finance[] = [];
  displayedColumns: string[] = [
    'numeroDossier',
    'creancier',
    'montantCreance',
    'fraisCreation',
    'coutGestion',
    'coutActionsAmiable',
    'coutActionsJuridique',
    'fraisAvocat',
    'fraisHuissier',
    'totalFacture',
    'statut',
    'actions'
  ];
  
  getTotalFacture(finance: Finance): number {
    return finance.calculerFactureFinale();
  }
  
  voirDetail(dossierId: number): void {
    // Naviguer vers détail facture
  }
  
  finaliserFacture(dossierId: number): void {
    // Finaliser la facture
  }
}
```

Template HTML :
```html
<table mat-table [dataSource]="dossiers" class="couts-table">
  <ng-container matColumnDef="numeroDossier">
    <th mat-header-cell *matHeaderCellDef>N° Dossier</th>
    <td mat-cell *matCellDef="let finance">{{ finance.dossier.numeroDossier }}</td>
  </ng-container>
  
  <ng-container matColumnDef="fraisCreation">
    <th mat-header-cell *matHeaderCellDef>Création</th>
    <td mat-cell *matCellDef="let finance">{{ finance.fraisCreationDossier | number:'1.2-2' }} TND</td>
  </ng-container>
  
  <ng-container matColumnDef="coutGestion">
    <th mat-header-cell *matHeaderCellDef>Gestion</th>
    <td mat-cell *matCellDef="let finance">
      {{ finance.calculerCoutGestionTotal() | number:'1.2-2' }} TND
    </td>
  </ng-container>
  
  <ng-container matColumnDef="coutActionsAmiable">
    <th mat-header-cell *matHeaderCellDef>Actions Amiable</th>
    <td mat-cell *matCellDef="let finance">{{ finance.coutActionsAmiable | number:'1.2-2' }} TND</td>
  </ng-container>
  
  <ng-container matColumnDef="coutActionsJuridique">
    <th mat-header-cell *matHeaderCellDef>Actions Juridique</th>
    <td mat-cell *matCellDef="let finance">{{ finance.coutActionsJuridique | number:'1.2-2' }} TND</td>
  </ng-container>
  
  <ng-container matColumnDef="totalFacture">
    <th mat-header-cell *matHeaderCellDef>Total</th>
    <td mat-cell *matCellDef="let finance" class="total-cell">
      <strong>{{ getTotalFacture(finance) | number:'1.2-2' }} TND</strong>
    </td>
  </ng-container>
  
  <ng-container matColumnDef="statut">
    <th mat-header-cell *matHeaderCellDef>Statut</th>
    <td mat-cell *matCellDef="let finance">
      <mat-chip [class.finalisee]="finance.factureFinalisee">
        {{ finance.factureFinalisee ? 'Finalisée' : 'En attente' }}
      </mat-chip>
    </td>
  </ng-container>
  
  <ng-container matColumnDef="actions">
    <th mat-header-cell *matHeaderCellDef>Actions</th>
    <td mat-cell *matCellDef="let finance">
      <button mat-icon-button (click)="voirDetail(finance.dossier.id)" matTooltip="Voir détail">
        <mat-icon>visibility</mat-icon>
      </button>
      <button mat-icon-button (click)="finaliserFacture(finance.dossier.id)" 
              [disabled]="finance.factureFinalisee" matTooltip="Finaliser">
        <mat-icon>check_circle</mat-icon>
      </button>
    </td>
  </ng-container>
  
  <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
  <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
</table>
```

IMPORTANT :
- Toutes les colonnes de coût affichées
- Format monétaire
- Actions pour voir détail et finaliser
```

---

## 🎯 PARTIE 3 : Intégration et Navigation

### 📋 PROMPT 10 : Créer le Module et Routing Complet

```
Dans le projet Angular, créez le module et le routing pour organiser toutes les interfaces :

Fichier : src/app/app-routing.module.ts

```typescript
const routes: Routes = [
  // Routes Chef Recouvrement Amiable
  {
    path: 'recouvrement-amiable',
    component: ChefRecouvrementAmiableLayoutComponent,
    canActivate: [AuthGuard],
    data: { roles: ['CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE', 'AGENT_RECOUVREMENT_AMIABLE'] },
    children: [
      { path: '', component: DossiersAmiableListComponent },
      { path: 'dossier/:id', component: DossierDetailAmiableComponent },
      { path: 'dossier/:id/actions', component: DossierActionsAmiableComponent }
    ]
  },
  
  // Routes Chef Financier
  {
    path: 'finance',
    component: ChefFinanceLayoutComponent,
    canActivate: [AuthGuard],
    data: { roles: ['CHEF_DEPARTEMENT_FINANCE', 'AGENT_FINANCE'] },
    children: [
      { path: '', component: ChefFinanceDashboardComponent },
      { path: 'dossier/:id/facture', component: FactureDetailComponent },
      { path: 'dossiers', component: DossiersCoutsTableComponent },
      { path: 'factures-en-attente', component: FacturesEnAttenteComponent }
    ]
  }
];
```

Fichier : src/app/app.module.ts

```typescript
@NgModule({
  declarations: [
    // Composants Recouvrement Amiable
    DossierActionsAmiableComponent,
    DossierDetailAmiableComponent,
    ActionDialogAmiableComponent,
    RecommandationsAmiableComponent,
    
    // Composants Finance
    ChefFinanceDashboardComponent,
    FactureDetailComponent,
    DossiersCoutsTableComponent,
    FacturesEnAttenteComponent
  ],
  imports: [
    // Modules Angular Material
    MatTableModule,
    MatCardModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatDatepickerModule,
    MatChipsModule,
    MatTabsModule,
    MatToolbarModule,
    MatIconModule
  ],
  providers: [
    ActionRecouvrementService,
    FinanceService,
    DossierService
  ]
})
export class AppModule { }
```

IMPORTANT :
- Séparation claire des routes selon les rôles
- Guards pour la sécurité
- Modules bien organisés
```

---

## ✅ Checklist d'Implémentation Frontend

### Recouvrement Amiable
- [ ] Créer ActionRecouvrementService (sans coûts)
- [ ] Créer DossierActionsAmiableComponent
- [ ] Créer ActionDialogAmiableComponent (sans coût)
- [ ] Créer DossierDetailAmiableComponent
- [ ] Créer RecommandationsAmiableComponent
- [ ] Intégrer dans le routing

### Finance
- [ ] Créer FinanceService (complet avec coûts)
- [ ] Créer ChefFinanceDashboardComponent
- [ ] Créer FactureDetailComponent
- [ ] Créer DossiersCoutsTableComponent
- [ ] Créer FacturesEnAttenteComponent
- [ ] Intégrer dans le routing

### Général
- [ ] Créer les interfaces TypeScript
- [ ] Configurer les guards de sécurité
- [ ] Ajouter les styles CSS
- [ ] Tester toutes les fonctionnalités
- [ ] Documenter les APIs utilisées

---

**Ce document contient tous les prompts nécessaires pour créer des interfaces complètes, bien organisées et fonctionnelles pour les chefs de recouvrement amiable et financier.**

