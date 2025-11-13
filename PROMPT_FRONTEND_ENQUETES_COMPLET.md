# Prompt Complet Frontend - Gestion des Enquêtes avec Améliorations Backend

## Contexte des Améliorations Backend

Le backend a été amélioré pour :
1. **Filtrage automatique des validations orphelines** : L'endpoint `/api/validation/enquetes/en-attente` filtre automatiquement les validations dont l'enquête n'existe plus
2. **Suppression améliorée** : La suppression d'une enquête supprime automatiquement toutes ses validations associées, quel que soit le statut
3. **Nettoyage des orphelines** : Nouveau endpoint pour nettoyer les validations orphelines existantes
4. **Suppression sans restriction** : Suppression possible même si l'enquête est en attente, validée ou rejetée

## Objectif

Intégrer ces améliorations dans le frontend en :
- Garantissant le bon fonctionnement des interfaces existantes
- Ajoutant les boutons appropriés (validation, rejet, suppression)
- Assurant un affichage correct des données
- Gérant les erreurs de manière élégante

---

## PROMPT 1 : Vérifier et Corriger le Service Enquête

**Prompt à utiliser :**
```
Dans le service Angular qui gère les appels API pour les enquêtes (probablement enquete.service.ts), vérifiez et ajoutez/modifiez les méthodes suivantes pour garantir le bon fonctionnement :

1. **getEnquetesEnAttente()** : Récupère les enquêtes en attente de validation
   - GET /api/validation/enquetes/en-attente
   - Le backend filtre maintenant automatiquement les validations orphelines
   - Gérer les erreurs 500 (ne devrait plus arriver, mais prévoir le cas)
   - Retourne : Observable<ValidationEnquete[]>

2. **validerEnquete(validationId: number, chefId: number, commentaire?: string)** : Valide une enquête
   - POST /api/validation/enquetes/{validationId}/valider?chefId={chefId}&commentaire={commentaire}
   - Retourne : Observable<ValidationEnquete>

3. **rejeterEnquete(validationId: number, chefId: number, commentaire?: string)** : Rejette une enquête
   - POST /api/validation/enquetes/{validationId}/rejeter?chefId={chefId}&commentaire={commentaire}
   - Retourne : Observable<ValidationEnquete>

4. **deleteEnquete(id: number)** : Supprime une enquête (amélioré)
   - DELETE /api/enquettes/{id}
   - Supprime automatiquement toutes les validations associées
   - Fonctionne quel que soit le statut de l'enquête
   - Retourne : Observable<void> (204 NO_CONTENT)

5. **nettoyerValidationsOrphelines()** : Nettoie les validations orphelines (nouveau)
   - POST /api/validation/enquetes/nettoyer-orphelines
   - Retourne : Observable<{nombreSupprime: number}>

Exemple d'implémentation :

getEnquetesEnAttente(): Observable<ValidationEnquete[]> {
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${this.getToken()}`
  };
  
  return this.http.get<ValidationEnquete[]>(
    `${this.apiUrl}/validation/enquetes/en-attente`,
    { headers }
  ).pipe(
    catchError(error => {
      console.error('Erreur lors de la récupération des enquêtes en attente:', error);
      // Retourner un tableau vide en cas d'erreur pour éviter de casser l'interface
      return of([]);
    })
  );
}

deleteEnquete(id: number): Observable<void> {
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${this.getToken()}`
  };
  
  return this.http.delete<void>(
    `${this.apiUrl}/enquettes/${id}`,
    { headers }
  );
}

nettoyerValidationsOrphelines(): Observable<number> {
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${this.getToken()}`
  };
  
  return this.http.post<{nombreSupprime: number}>(
    `${this.apiUrl}/validation/enquetes/nettoyer-orphelines`,
    null,
    { headers }
  ).pipe(
    map(response => response.nombreSupprime || 0)
  );
}
```

---

## PROMPT 2 : Créer/Améliorer le Composant Liste des Enquêtes en Attente

**Prompt à utiliser :**
```
Créez ou améliorez le composant Angular qui affiche la liste des enquêtes en attente de validation (probablement enquetes-en-attente.component.ts).

Fonctionnalités requises :

1. **Chargement des données** :
   - Au chargement du composant, appeler `getEnquetesEnAttente()`
   - Afficher un indicateur de chargement pendant la récupération
   - Gérer les erreurs gracieusement (afficher un message au lieu de planter)

2. **Affichage du tableau** :
   - Colonnes : Code rapport, Dossier, Agent créateur, Date création, Statut, Actions
   - Badge de statut coloré (EN_ATTENTE = orange/jaune)
   - Boutons d'action : Valider (vert), Rejeter (rouge), Voir détails (icône œil)

3. **Boutons d'action** :
   - **Valider** : Visible uniquement pour les chefs, uniquement si statut = EN_ATTENTE
   - **Rejeter** : Visible uniquement pour les chefs, uniquement si statut = EN_ATTENTE
   - **Voir détails** : Visible pour tous
   - **Supprimer** : Visible pour l'agent créateur ou les chefs

4. **Gestion des permissions** :
   - Vérifier le rôle de l'utilisateur connecté
   - Afficher/masquer les boutons selon les permissions

Exemple de structure :

export class EnquetesEnAttenteComponent implements OnInit {
  validations: ValidationEnquete[] = [];
  loading = false;
  error: string | null = null;
  currentUser: Utilisateur | null = null;

  constructor(
    private enqueteService: EnqueteService,
    private authService: AuthService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.loadEnquetesEnAttente();
  }

  loadEnquetesEnAttente(): void {
    this.loading = true;
    this.error = null;
    
    this.enqueteService.getEnquetesEnAttente().subscribe({
      next: (validations) => {
        this.validations = validations;
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur:', error);
        this.error = 'Erreur lors de la récupération des enquêtes en attente';
        this.loading = false;
        this.snackBar.open(this.error, 'Fermer', { duration: 5000 });
      }
    });
  }

  canValidate(): boolean {
    if (!this.currentUser) return false;
    const isChef = this.currentUser.roleUtilisateur?.startsWith('CHEF_DEPARTEMENT') || 
                   this.currentUser.roleUtilisateur === 'SUPER_ADMIN';
    return isChef;
  }

  canDelete(validation: ValidationEnquete): boolean {
    if (!this.currentUser) return false;
    // Agent créateur peut supprimer
    if (validation.agentCreateur?.id === this.currentUser.id) return true;
    // Chef peut supprimer
    const isChef = this.currentUser.roleUtilisateur?.startsWith('CHEF_DEPARTEMENT') || 
                   this.currentUser.roleUtilisateur === 'SUPER_ADMIN';
    return isChef;
  }
}
```

---

## PROMPT 3 : Template HTML pour la Liste des Enquêtes en Attente

**Prompt à utiliser :**
```
Créez ou améliorez le template HTML du composant de liste des enquêtes en attente (enquetes-en-attente.component.html).

Structure requise :

1. **Titre et actions** :
   - Titre : "Enquêtes en attente de validation"
   - Bouton de rafraîchissement
   - Bouton de nettoyage des orphelines (optionnel, pour les admins)

2. **Indicateur de chargement** :
   - Spinner pendant le chargement
   - Message d'erreur si erreur

3. **Tableau** :
   - Colonnes avec tri possible
   - Badges de statut colorés
   - Boutons d'action dans la dernière colonne

4. **Pagination** (si nécessaire)

Exemple de template :

<div class="container">
  <div class="header">
    <h1>Enquêtes en attente de validation</h1>
    <div class="actions">
      <button mat-icon-button (click)="loadEnquetesEnAttente()" [disabled]="loading">
        <mat-icon>refresh</mat-icon>
      </button>
    </div>
  </div>

  <!-- Indicateur de chargement -->
  <div *ngIf="loading" class="loading">
    <mat-spinner diameter="50"></mat-spinner>
    <p>Chargement des enquêtes en attente...</p>
  </div>

  <!-- Message d'erreur -->
  <div *ngIf="error && !loading" class="error">
    <mat-icon color="warn">error</mat-icon>
    <p>{{ error }}</p>
    <button mat-button (click)="loadEnquetesEnAttente()">Réessayer</button>
  </div>

  <!-- Tableau -->
  <div *ngIf="!loading && !error">
    <table mat-table [dataSource]="validations" class="mat-elevation-z8">
      
      <!-- Colonne Code rapport -->
      <ng-container matColumnDef="rapportCode">
        <th mat-header-cell *matHeaderCellDef>Code rapport</th>
        <td mat-cell *matCellDef="let validation">
          {{ validation.enquete?.rapportCode || 'N/A' }}
        </td>
      </ng-container>

      <!-- Colonne Dossier -->
      <ng-container matColumnDef="dossier">
        <th mat-header-cell *matHeaderCellDef>Dossier</th>
        <td mat-cell *matCellDef="let validation">
          {{ validation.enquete?.dossier?.numeroDossier || 'N/A' }}
        </td>
      </ng-container>

      <!-- Colonne Agent créateur -->
      <ng-container matColumnDef="agentCreateur">
        <th mat-header-cell *matHeaderCellDef>Agent créateur</th>
        <td mat-cell *matCellDef="let validation">
          {{ validation.agentCreateur?.nom }} {{ validation.agentCreateur?.prenom }}
        </td>
      </ng-container>

      <!-- Colonne Date création -->
      <ng-container matColumnDef="dateCreation">
        <th mat-header-cell *matHeaderCellDef>Date création</th>
        <td mat-cell *matCellDef="let validation">
          {{ validation.dateCreation | date:'dd/MM/yyyy HH:mm' }}
        </td>
      </ng-container>

      <!-- Colonne Statut -->
      <ng-container matColumnDef="statut">
        <th mat-header-cell *matHeaderCellDef>Statut</th>
        <td mat-cell *matCellDef="let validation">
          <mat-chip [color]="getStatutColor(validation.statut)">
            {{ getStatutLabel(validation.statut) }}
          </mat-chip>
        </td>
      </ng-container>

      <!-- Colonne Actions -->
      <ng-container matColumnDef="actions">
        <th mat-header-cell *matHeaderCellDef>Actions</th>
        <td mat-cell *matCellDef="let validation">
          <button 
            mat-icon-button 
            color="primary"
            (click)="validerEnquete(validation)"
            *ngIf="canValidate() && validation.statut === 'EN_ATTENTE'"
            [matTooltip]="'Valider l\'enquête'">
            <mat-icon>check_circle</mat-icon>
          </button>
          
          <button 
            mat-icon-button 
            color="warn"
            (click)="rejeterEnquete(validation)"
            *ngIf="canValidate() && validation.statut === 'EN_ATTENTE'"
            [matTooltip]="'Rejeter l\'enquête'">
            <mat-icon>cancel</mat-icon>
          </button>
          
          <button 
            mat-icon-button 
            (click)="voirDetails(validation)"
            [matTooltip]="'Voir les détails'">
            <mat-icon>visibility</mat-icon>
          </button>
          
          <button 
            mat-icon-button 
            color="warn"
            (click)="confirmDeleteEnquete(validation)"
            *ngIf="canDelete(validation)"
            [matTooltip]="'Supprimer l\'enquête'">
            <mat-icon>delete</mat-icon>
          </button>
        </td>
      </ng-container>

      <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
      <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
    </table>

    <!-- Message si liste vide -->
    <div *ngIf="validations.length === 0 && !loading" class="empty-state">
      <mat-icon>inbox</mat-icon>
      <p>Aucune enquête en attente de validation</p>
    </div>
  </div>
</div>
```

---

## PROMPT 4 : Implémenter les Méthodes d'Action (Validation, Rejet, Suppression)

**Prompt à utiliser :**
```
Dans le composant enquetes-en-attente.component.ts, implémentez les méthodes suivantes pour gérer les actions :

1. **validerEnquete(validation: ValidationEnquete)** : Valide une enquête
2. **rejeterEnquete(validation: ValidationEnquete)** : Rejette une enquête
3. **confirmDeleteEnquete(validation: ValidationEnquete)** : Confirme et supprime une enquête
4. **voirDetails(validation: ValidationEnquete)** : Affiche les détails de l'enquête

Exemple d'implémentation :

validerEnquete(validation: ValidationEnquete): void {
  if (!this.currentUser?.id) {
    this.snackBar.open('Utilisateur non connecté', 'Fermer', { duration: 3000 });
    return;
  }

  const dialogRef = this.dialog.open(ValidationDialogComponent, {
    width: '500px',
    data: {
      title: 'Valider l\'enquête',
      message: `Êtes-vous sûr de vouloir valider l'enquête ${validation.enquete?.rapportCode} ?`,
      requireComment: false
    }
  });

  dialogRef.afterClosed().subscribe(commentaire => {
    if (commentaire !== undefined) {
      this.enqueteService.validerEnquete(validation.id, this.currentUser!.id, commentaire)
        .subscribe({
          next: () => {
            this.snackBar.open('Enquête validée avec succès', 'Fermer', { duration: 3000 });
            this.loadEnquetesEnAttente(); // Rafraîchir la liste
          },
          error: (error) => {
            console.error('Erreur validation:', error);
            let message = 'Erreur lors de la validation';
            if (error.status === 400) {
              message = error.error?.message || 'Vous ne pouvez pas valider cette enquête';
            }
            this.snackBar.open(message, 'Fermer', { duration: 5000 });
          }
        });
    }
  });
}

rejeterEnquete(validation: ValidationEnquete): void {
  if (!this.currentUser?.id) {
    this.snackBar.open('Utilisateur non connecté', 'Fermer', { duration: 3000 });
    return;
  }

  const dialogRef = this.dialog.open(RejetDialogComponent, {
    width: '500px',
    data: {
      title: 'Rejeter l\'enquête',
      message: `Êtes-vous sûr de vouloir rejeter l'enquête ${validation.enquete?.rapportCode} ?`,
      requireComment: true
    }
  });

  dialogRef.afterClosed().subscribe(commentaire => {
    if (commentaire) {
      this.enqueteService.rejeterEnquete(validation.id, this.currentUser!.id, commentaire)
        .subscribe({
          next: () => {
            this.snackBar.open('Enquête rejetée', 'Fermer', { duration: 3000 });
            this.loadEnquetesEnAttente(); // Rafraîchir la liste
          },
          error: (error) => {
            console.error('Erreur rejet:', error);
            let message = 'Erreur lors du rejet';
            if (error.status === 400) {
              message = error.error?.message || 'Vous ne pouvez pas rejeter cette enquête';
            }
            this.snackBar.open(message, 'Fermer', { duration: 5000 });
          }
        });
    }
  });
}

confirmDeleteEnquete(validation: ValidationEnquete): void {
  const dialogRef = this.dialog.open(ConfirmDeleteDialogComponent, {
    width: '400px',
    data: {
      title: 'Supprimer l\'enquête',
      message: `Êtes-vous sûr de vouloir supprimer l'enquête ${validation.enquete?.rapportCode} ?`,
      details: 'Cette action supprimera également toutes les validations associées. Cette action est irréversible.'
    }
  });

  dialogRef.afterClosed().subscribe(confirmed => {
    if (confirmed && validation.enquete?.id) {
      this.enqueteService.deleteEnquete(validation.enquete.id)
        .subscribe({
          next: () => {
            this.snackBar.open('Enquête supprimée avec succès. Les validations associées ont également été supprimées.', 'Fermer', { 
              duration: 5000 
            });
            this.loadEnquetesEnAttente(); // Rafraîchir la liste
          },
          error: (error) => {
            console.error('Erreur suppression:', error);
            let message = 'Erreur lors de la suppression';
            if (error.status === 404) {
              message = 'Enquête non trouvée. Elle a peut-être déjà été supprimée.';
            } else if (error.status === 403) {
              message = 'Vous n\'avez pas les droits pour supprimer cette enquête.';
            }
            this.snackBar.open(message, 'Fermer', { duration: 5000 });
          }
        });
    }
  });
}

voirDetails(validation: ValidationEnquete): void {
  if (validation.enquete?.id) {
    // Navigation vers la page de détails ou ouverture d'un dialogue
    this.router.navigate(['/enquetes', validation.enquete.id]);
  }
}

// Méthodes helper pour l'affichage
getStatutColor(statut: string): string {
  switch(statut) {
    case 'EN_ATTENTE': return 'warn';
    case 'VALIDE': return 'primary';
    case 'REJETE': return 'accent';
    default: return '';
  }
}

getStatutLabel(statut: string): string {
  const labels: { [key: string]: string } = {
    'EN_ATTENTE': 'En attente',
    'VALIDE': 'Validée',
    'REJETE': 'Rejetée'
  };
  return labels[statut] || statut;
}
```

---

## PROMPT 5 : Créer les Composants de Dialogue (Validation, Rejet, Confirmation Suppression)

**Prompt à utiliser :**
```
Créez les composants de dialogue suivants pour gérer les interactions utilisateur :

1. **ValidationDialogComponent** : Dialogue pour valider avec commentaire optionnel
2. **RejetDialogComponent** : Dialogue pour rejeter avec commentaire obligatoire
3. **ConfirmDeleteDialogComponent** : Dialogue de confirmation de suppression

### ValidationDialogComponent

@Component({
  selector: 'app-validation-dialog',
  template: `
    <h2 mat-dialog-title>{{ data.title }}</h2>
    <mat-dialog-content>
      <p>{{ data.message }}</p>
      <mat-form-field appearance="outline" class="full-width" *ngIf="data.requireComment">
        <mat-label>Commentaire (optionnel)</mat-label>
        <textarea matInput [(ngModel)]="commentaire" rows="4"></textarea>
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Annuler</button>
      <button mat-raised-button color="primary" (click)="onConfirm()">
        Valider
      </button>
    </mat-dialog-actions>
  `
})
export class ValidationDialogComponent {
  commentaire: string = '';

  constructor(
    public dialogRef: MatDialogRef<ValidationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {
      title: string;
      message: string;
      requireComment: boolean;
    }
  ) {}

  onCancel(): void {
    this.dialogRef.close();
  }

  onConfirm(): void {
    this.dialogRef.close(this.commentaire || null);
  }
}

### RejetDialogComponent

@Component({
  selector: 'app-rejet-dialog',
  template: `
    <h2 mat-dialog-title>{{ data.title }}</h2>
    <mat-dialog-content>
      <p>{{ data.message }}</p>
      <mat-form-field appearance="outline" class="full-width">
        <mat-label>Commentaire de rejet *</mat-label>
        <textarea matInput [(ngModel)]="commentaire" required minlength="10" rows="4"></textarea>
        <mat-hint>Veuillez expliquer la raison du rejet (minimum 10 caractères)</mat-hint>
        <mat-error *ngIf="commentaire && commentaire.length < 10">
          Le commentaire doit contenir au moins 10 caractères
        </mat-error>
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Annuler</button>
      <button mat-raised-button color="warn" (click)="onConfirm()" [disabled]="!commentaire || commentaire.length < 10">
        Rejeter
      </button>
    </mat-dialog-actions>
  `
})
export class RejetDialogComponent {
  commentaire: string = '';

  constructor(
    public dialogRef: MatDialogRef<RejetDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {
      title: string;
      message: string;
      requireComment: boolean;
    }
  ) {}

  onCancel(): void {
    this.dialogRef.close();
  }

  onConfirm(): void {
    if (this.commentaire && this.commentaire.length >= 10) {
      this.dialogRef.close(this.commentaire);
    }
  }
}

### ConfirmDeleteDialogComponent

@Component({
  selector: 'app-confirm-delete-dialog',
  template: `
    <h2 mat-dialog-title>{{ data.title }}</h2>
    <mat-dialog-content>
      <p>{{ data.message }}</p>
      <p *ngIf="data.details" class="details">{{ data.details }}</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Annuler</button>
      <button mat-raised-button color="warn" (click)="onConfirm()">
        Supprimer
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .details {
      color: #666;
      font-size: 0.9em;
      margin-top: 10px;
    }
  `]
})
export class ConfirmDeleteDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ConfirmDeleteDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {
      title: string;
      message: string;
      details?: string;
    }
  ) {}

  onCancel(): void {
    this.dialogRef.close(false);
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }
}
```

---

## PROMPT 6 : Améliorer le Composant de Détails d'Enquête

**Prompt à utiliser :**
```
Dans le composant de détails d'enquête (probablement detail-enquete.component.ts), ajoutez les fonctionnalités suivantes :

1. **Bouton de suppression** : Visible pour l'agent créateur ou les chefs
2. **Affichage du statut** : Badge coloré avec le statut de l'enquête
3. **Affichage des validations** : Liste des validations associées avec leurs statuts
4. **Boutons de validation/rejet** : Pour les chefs si l'enquête est en attente

Exemple d'ajout dans le template :

<div class="enquete-header">
  <h1>Enquête {{ enquete.rapportCode }}</h1>
  <div class="actions">
    <!-- Badge de statut -->
    <mat-chip [color]="getStatutColor(enquete.statut)">
      {{ getStatutLabel(enquete.statut) }}
    </mat-chip>
    
    <!-- Boutons d'action -->
    <button 
      mat-raised-button 
      color="primary" 
      *ngIf="canValidate() && enquete.statut === 'EN_ATTENTE_VALIDATION'"
      (click)="validerEnquete()">
      <mat-icon>check_circle</mat-icon>
      Valider
    </button>
    
    <button 
      mat-raised-button 
      color="warn" 
      *ngIf="canValidate() && enquete.statut === 'EN_ATTENTE_VALIDATION'"
      (click)="rejeterEnquete()">
      <mat-icon>cancel</mat-icon>
      Rejeter
    </button>
    
    <button 
      mat-raised-button 
      color="warn" 
      *ngIf="canDelete()"
      (click)="confirmDeleteEnquete()">
      <mat-icon>delete</mat-icon>
      Supprimer
    </button>
  </div>
</div>

<!-- Section validations -->
<mat-card *ngIf="validations && validations.length > 0">
  <mat-card-header>
    <mat-card-title>Validations associées</mat-card-title>
  </mat-card-header>
  <mat-card-content>
    <mat-list>
      <mat-list-item *ngFor="let validation of validations">
        <mat-icon matListItemIcon>{{ getValidationIcon(validation.statut) }}</mat-icon>
        <div matListItemTitle>Statut: {{ getStatutLabel(validation.statut) }}</div>
        <div matListItemLine *ngIf="validation.dateCreation">
          Créée le: {{ validation.dateCreation | date:'dd/MM/yyyy HH:mm' }}
        </div>
        <div matListItemLine *ngIf="validation.chefValidateur">
          Validée par: {{ validation.chefValidateur.nom }} {{ validation.chefValidateur.prenom }}
        </div>
      </mat-list-item>
    </mat-list>
  </mat-card-content>
</mat-card>
```

---

## PROMPT 7 : Gérer les Erreurs et les Messages Utilisateur

**Prompt à utiliser :**
```
Dans tous les composants, implémentez une gestion d'erreurs cohérente :

1. **Messages d'erreur clairs** : Messages compréhensibles pour l'utilisateur
2. **Gestion des erreurs réseau** : Timeout, connexion perdue, etc.
3. **Gestion des erreurs serveur** : 400, 403, 404, 500
4. **Feedback visuel** : Snackbar, spinners, messages d'erreur

Exemple de service d'erreur centralisé :

@Injectable({
  providedIn: 'root'
})
export class ErrorHandlerService {
  constructor(private snackBar: MatSnackBar) {}

  handleError(error: any, defaultMessage: string = 'Une erreur est survenue'): void {
    let message = defaultMessage;

    if (error.error?.message) {
      message = error.error.message;
    } else if (error.status === 0) {
      message = 'Erreur de connexion. Vérifiez votre connexion internet.';
    } else if (error.status === 400) {
      message = 'Requête invalide. Veuillez vérifier les données saisies.';
    } else if (error.status === 403) {
      message = 'Vous n\'avez pas les droits pour effectuer cette action.';
    } else if (error.status === 404) {
      message = 'Ressource non trouvée.';
    } else if (error.status === 500) {
      message = 'Erreur serveur. Veuillez réessayer plus tard.';
    }

    this.snackBar.open(message, 'Fermer', {
      duration: 5000,
      panelClass: ['error-snackbar']
    });
  }
}

Utilisation dans les composants :

this.enqueteService.deleteEnquete(id).subscribe({
  next: () => {
    this.snackBar.open('Enquête supprimée avec succès', 'Fermer', { duration: 3000 });
    this.loadEnquetes();
  },
  error: (error) => {
    this.errorHandler.handleError(error, 'Erreur lors de la suppression de l\'enquête');
  }
});
```

---

## PROMPT 8 : Interface TypeScript pour ValidationEnquete

**Prompt à utiliser :**
```
Créez ou mettez à jour l'interface TypeScript pour ValidationEnquete dans models/validation-enquete.ts :

export interface ValidationEnquete {
  id: number;
  enquete?: Enquette;
  agentCreateur?: Utilisateur;
  chefValidateur?: Utilisateur;
  dateValidation?: string; // ISO 8601 format
  statut: 'EN_ATTENTE' | 'VALIDE' | 'REJETE';
  commentaires?: string;
  dateCreation: string; // ISO 8601 format
  dateModification?: string; // ISO 8601 format
}

Assurez-vous que l'interface Enquette inclut :
- statut?: 'EN_ATTENTE_VALIDATION' | 'VALIDE' | 'REJETE' | 'EN_COURS' | 'CLOTURE';
- valide?: boolean;
- dateValidation?: string;
- commentaireValidation?: string;
```

---

## PROMPT 9 : Styles CSS pour les Composants

**Prompt à utiliser :**
```
Ajoutez les styles CSS suivants dans les fichiers .scss des composants :

### Styles pour la liste des enquêtes en attente

.container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.error {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px;
  color: #f44336;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
  
  mat-icon {
    font-size: 64px;
    width: 64px;
    height: 64px;
    margin-bottom: 16px;
  }
}

table {
  width: 100%;
}

.actions {
  display: flex;
  gap: 8px;
}

### Styles pour les badges de statut

mat-chip {
  font-weight: 500;
}

### Styles pour les dialogues

.full-width {
  width: 100%;
}
```

---

## PROMPT 10 : Tests et Vérifications

**Prompt à utiliser :**
```
Après avoir implémenté toutes les fonctionnalités, testez les scénarios suivants :

1. **Affichage de la liste** :
   - ✅ La liste des enquêtes en attente s'affiche sans erreur 500
   - ✅ Les validations orphelines ne sont pas affichées (filtrées automatiquement)
   - ✅ Le statut est affiché avec le bon badge coloré

2. **Validation d'une enquête** :
   - ✅ Un chef peut valider une enquête en attente
   - ✅ Un agent ne peut pas valider ses propres enquêtes
   - ✅ Après validation, la liste se rafraîchit automatiquement
   - ✅ Un message de succès s'affiche

3. **Rejet d'une enquête** :
   - ✅ Un chef peut rejeter une enquête en attente
   - ✅ Un commentaire est obligatoire pour le rejet
   - ✅ Après rejet, la liste se rafraîchit automatiquement

4. **Suppression d'une enquête** :
   - ✅ Un agent peut supprimer ses propres enquêtes
   - ✅ Un chef peut supprimer n'importe quelle enquête
   - ✅ La suppression fonctionne même si l'enquête est en attente
   - ✅ La suppression fonctionne même si l'enquête est validée
   - ✅ Les validations associées sont supprimées automatiquement
   - ✅ Un message de confirmation s'affiche avant suppression

5. **Gestion des erreurs** :
   - ✅ Les erreurs réseau sont gérées gracieusement
   - ✅ Les erreurs serveur affichent des messages clairs
   - ✅ Les erreurs 403 affichent un message de permission
   - ✅ Les erreurs 404 affichent un message approprié

6. **Permissions** :
   - ✅ Les boutons sont affichés/masqués selon les permissions
   - ✅ Les actions sont bloquées côté frontend ET backend
```

---

## Résumé des Modifications Frontend

### Fichiers à créer/modifier :

1. **Services** :
   - `enquete.service.ts` - Ajouter/modifier les méthodes d'API

2. **Composants** :
   - `enquetes-en-attente.component.ts` - Composant principal de liste
   - `enquetes-en-attente.component.html` - Template de la liste
   - `enquetes-en-attente.component.scss` - Styles
   - `detail-enquete.component.ts` - Améliorer avec boutons de suppression
   - `validation-dialog.component.ts` - Dialogue de validation
   - `rejet-dialog.component.ts` - Dialogue de rejet
   - `confirm-delete-dialog.component.ts` - Dialogue de confirmation

3. **Models** :
   - `validation-enquete.ts` - Interface TypeScript
   - `enquete.ts` - Vérifier que l'interface inclut les champs de statut

4. **Services utilitaires** :
   - `error-handler.service.ts` - Gestion centralisée des erreurs (optionnel)

### Endpoints utilisés :

- `GET /api/validation/enquetes/en-attente` - Liste des enquêtes en attente
- `POST /api/validation/enquetes/{id}/valider` - Valider une enquête
- `POST /api/validation/enquetes/{id}/rejeter` - Rejeter une enquête
- `DELETE /api/enquettes/{id}` - Supprimer une enquête
- `POST /api/validation/enquetes/nettoyer-orphelines` - Nettoyer les orphelines (optionnel)

### Fonctionnalités garanties :

✅ Affichage correct de la liste des enquêtes en attente
✅ Filtrage automatique des validations orphelines (côté backend)
✅ Boutons de validation/rejet pour les chefs
✅ Bouton de suppression pour agents et chefs
✅ Suppression possible quel que soit le statut
✅ Gestion d'erreurs élégante
✅ Messages utilisateur clairs
✅ Permissions respectées

