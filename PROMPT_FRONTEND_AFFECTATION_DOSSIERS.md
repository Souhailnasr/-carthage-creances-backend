# Prompt Frontend : Consommation des APIs d'Affectation et Clôture des Dossiers

## 🎯 PROMPT 1 : Mise à Jour du Service DossierService (Frontend)

**Prompt à copier dans Cursor AI :**

```
Dans le projet Angular, localisez le service DossierService (probablement dans src/app/services/dossier.service.ts).

Ajoutez les méthodes suivantes pour consommer les APIs d'affectation et de clôture :

1. affecterAuRecouvrementAmiable(dossierId: number): Observable<Dossier>
   - PUT /api/dossiers/{dossierId}/affecter/recouvrement-amiable
   - Retourne le dossier mis à jour
   - Gère les erreurs : 400 (dossier non validé, chef non trouvé), 404 (dossier non trouvé), 500

2. affecterAuRecouvrementJuridique(dossierId: number): Observable<Dossier>
   - PUT /api/dossiers/{dossierId}/affecter/recouvrement-juridique
   - Retourne le dossier mis à jour
   - Gère les erreurs : 400 (dossier non validé, chef non trouvé), 404 (dossier non trouvé), 500

3. cloturerDossier(dossierId: number): Observable<Dossier>
   - PUT /api/dossiers/{dossierId}/cloturer
   - Retourne le dossier mis à jour
   - Gère les erreurs : 400 (dossier non validé), 404 (dossier non trouvé), 500

4. getDossiersValidesDisponibles(params?: {page?: number, size?: number, sort?: string, direction?: string, search?: string}): Observable<any>
   - GET /api/dossiers/valides-disponibles
   - Paramètres optionnels pour pagination, tri et recherche
   - Retourne un objet avec la liste des dossiers et les métadonnées de pagination

CODE EXEMPLE :

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { Dossier } from '../models/dossier';

@Injectable({
  providedIn: 'root'
})
export class DossierService {
  private apiUrl = 'http://localhost:8089/carthage-creance/api/dossiers';

  constructor(private http: HttpClient) {}

  /**
   * Affecte un dossier validé au recouvrement amiable
   */
  affecterAuRecouvrementAmiable(dossierId: number): Observable<Dossier> {
    return this.http.put<Dossier>(
      `${this.apiUrl}/${dossierId}/affecter/recouvrement-amiable`,
      null
    ).pipe(
      catchError((error) => {
        console.error('Erreur lors de l\'affectation au recouvrement amiable:', error);
        let errorMessage = 'Erreur lors de l\'affectation au recouvrement amiable';
        
        if (error.error?.message) {
          errorMessage = error.error.message;
        } else if (error.error?.error) {
          errorMessage = error.error.error;
        }
        
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  /**
   * Affecte un dossier validé au recouvrement juridique
   */
  affecterAuRecouvrementJuridique(dossierId: number): Observable<Dossier> {
    return this.http.put<Dossier>(
      `${this.apiUrl}/${dossierId}/affecter/recouvrement-juridique`,
      null
    ).pipe(
      catchError((error) => {
        console.error('Erreur lors de l\'affectation au recouvrement juridique:', error);
        let errorMessage = 'Erreur lors de l\'affectation au recouvrement juridique';
        
        if (error.error?.message) {
          errorMessage = error.error.message;
        } else if (error.error?.error) {
          errorMessage = error.error.error;
        }
        
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  /**
   * Clôture un dossier validé
   */
  cloturerDossier(dossierId: number): Observable<Dossier> {
    return this.http.put<Dossier>(
      `${this.apiUrl}/${dossierId}/cloturer`,
      null
    ).pipe(
      catchError((error) => {
        console.error('Erreur lors de la clôture du dossier:', error);
        let errorMessage = 'Erreur lors de la clôture du dossier';
        
        if (error.error?.message) {
          errorMessage = error.error.message;
        } else if (error.error?.error) {
          errorMessage = error.error.error;
        }
        
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  /**
   * Récupère les dossiers validés disponibles pour l'affectation
   */
  getDossiersValidesDisponibles(params?: {
    page?: number;
    size?: number;
    sort?: string;
    direction?: string;
    search?: string;
  }): Observable<any> {
    let httpParams = new HttpParams();
    
    if (params?.page !== undefined) {
      httpParams = httpParams.set('page', params.page.toString());
    }
    if (params?.size !== undefined) {
      httpParams = httpParams.set('size', params.size.toString());
    }
    if (params?.sort) {
      httpParams = httpParams.set('sort', params.sort);
    }
    if (params?.direction) {
      httpParams = httpParams.set('direction', params.direction);
    }
    if (params?.search) {
      httpParams = httpParams.set('search', params.search);
    }
    
    return this.http.get<any>(`${this.apiUrl}/valides-disponibles`, {
      params: httpParams
    }).pipe(
      catchError((error) => {
        console.error('Erreur lors de la récupération des dossiers validés:', error);
        return throwError(() => new Error('Erreur lors de la récupération des dossiers validés'));
      })
    );
  }
}
```

IMPORTANT :
- Utiliser HttpClient avec gestion d'erreurs appropriée
- Extraire les messages d'erreur du backend
- Retourner des Observables typés
- Logger les erreurs pour le débogage
```

---

## 🎯 PROMPT 2 : Mise à Jour du Composant d'Affectation des Dossiers

**Prompt à copier dans Cursor AI :**

```
Dans le projet Angular, localisez le composant d'affectation des dossiers (probablement affectation-dossiers.component.ts et affectation-dossiers.component.html).

Mettez à jour ce composant pour :

1. Charger les dossiers validés disponibles :
   - Au ngOnInit, charger les dossiers via getDossiersValidesDisponibles()
   - Afficher un spinner pendant le chargement
   - Gérer les erreurs avec des messages clairs

2. Implémenter la recherche :
   - Champ de recherche "Rechercher un dossier"
   - Filtrer les dossiers par numeroDossier, titre, etc.
   - Utiliser un debounce pour éviter trop de requêtes

3. Implémenter les filtres et tri :
   - Dropdown "Trier par" : Date création, Montant, Titre
   - Dropdown "Ordre" : Croissant, Décroissant
   - Dropdown "Par page" : 5, 10, 25, 50
   - Appliquer les filtres lors du changement

4. Implémenter les actions d'affectation :
   - Bouton "Affecter au Recouvrement Amiable" :
     * Afficher un MatDialog de confirmation
     * Appeler dossierService.affecterAuRecouvrementAmiable(dossierId)
     * Afficher un snackbar de succès/erreur
     * Rafraîchir la liste après succès
   
   - Bouton "Affecter au Recouvrement Juridique" :
     * Même logique que pour amiable
     * Appeler dossierService.affecterAuRecouvrementJuridique(dossierId)
   
   - Bouton "Clôturer" :
     * Afficher un MatDialog de confirmation avec message d'avertissement
     * Appeler dossierService.cloturerDossier(dossierId)
     * Afficher un snackbar de succès/erreur
     * Rafraîchir la liste après succès

5. Gestion de la sélection :
   - Champ "Numéro de Dossier" pour sélectionner un dossier spécifique
   - Lors de la sélection, activer les boutons d'action
   - Afficher les informations du dossier sélectionné

6. Affichage du tableau :
   - Utiliser MatTable avec pagination
   - Colonnes : NUMÉRO, TITRE, MONTANT, CRÉANCIER, DÉBITEUR, URGENCE, STATUT, DATE CRÉATION, ACTIONS
   - Afficher uniquement les dossiers avec statut VALIDE
   - Badges colorés pour les statuts
   - Actions sur chaque ligne (affecter amiable, affecter juridique, clôturer)

CODE EXEMPLE :

```typescript
import { Component, OnInit } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { DossierService } from '../services/dossier.service';
import { Dossier } from '../models/dossier';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

@Component({
  selector: 'app-affectation-dossiers',
  templateUrl: './affectation-dossiers.component.html',
  styleUrls: ['./affectation-dossiers.component.css']
})
export class AffectationDossiersComponent implements OnInit {
  displayedColumns: string[] = ['numero', 'titre', 'montant', 'creancier', 'debiteur', 'urgence', 'statut', 'dateCreation', 'actions'];
  dataSource = new MatTableDataSource<Dossier>([]);
  
  loading = false;
  selectedDossierId: number | null = null;
  searchTerm = '';
  sortBy = 'dateCreation';
  sortDirection = 'DESC';
  pageSize = 10;
  currentPage = 0;
  totalElements = 0;
  
  private searchSubject = new Subject<string>();

  constructor(
    private dossierService: DossierService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {
    // Debounce pour la recherche
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(searchTerm => {
      this.searchTerm = searchTerm;
      this.loadDossiers();
    });
  }

  ngOnInit(): void {
    this.loadDossiers();
  }

  loadDossiers(): void {
    this.loading = true;
    this.dossierService.getDossiersValidesDisponibles({
      page: this.currentPage,
      size: this.pageSize,
      sort: this.sortBy,
      direction: this.sortDirection,
      search: this.searchTerm || undefined
    }).subscribe({
      next: (response) => {
        this.dataSource.data = response.content || response.dossiers || [];
        this.totalElements = response.totalElements || response.total || 0;
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des dossiers:', error);
        this.snackBar.open('Erreur lors du chargement des dossiers', 'Fermer', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
        this.loading = false;
      }
    });
  }

  onSearchChange(searchTerm: string): void {
    this.searchSubject.next(searchTerm);
  }

  onSortChange(sortBy: string): void {
    this.sortBy = sortBy;
    this.currentPage = 0;
    this.loadDossiers();
  }

  onDirectionChange(direction: string): void {
    this.sortDirection = direction;
    this.currentPage = 0;
    this.loadDossiers();
  }

  onPageSizeChange(size: number): void {
    this.pageSize = size;
    this.currentPage = 0;
    this.loadDossiers();
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadDossiers();
  }

  selectDossierByNumber(numero: string): void {
    // Trouver le dossier par numéro
    const dossier = this.dataSource.data.find(d => d.numeroDossier === numero);
    if (dossier) {
      this.selectedDossierId = dossier.id;
    } else {
      this.snackBar.open('Dossier non trouvé', 'Fermer', {
        duration: 3000,
        panelClass: ['warning-snackbar']
      });
    }
  }

  affecterAmiable(dossierId: number): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        title: 'Affecter au Recouvrement Amiable',
        message: 'Êtes-vous sûr de vouloir affecter ce dossier au recouvrement amiable ?',
        confirmText: 'Affecter',
        cancelText: 'Annuler'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loading = true;
        this.dossierService.affecterAuRecouvrementAmiable(dossierId).subscribe({
          next: (dossier) => {
            this.snackBar.open('Dossier affecté au recouvrement amiable avec succès', 'Fermer', {
              duration: 3000,
              panelClass: ['success-snackbar']
            });
            this.loadDossiers();
          },
          error: (error) => {
            const errorMessage = error.message || 'Erreur lors de l\'affectation';
            this.snackBar.open(errorMessage, 'Fermer', {
              duration: 5000,
              panelClass: ['error-snackbar']
            });
            this.loading = false;
          }
        });
      }
    });
  }

  affecterJuridique(dossierId: number): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        title: 'Affecter au Recouvrement Juridique',
        message: 'Êtes-vous sûr de vouloir affecter ce dossier au recouvrement juridique ?',
        confirmText: 'Affecter',
        cancelText: 'Annuler'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loading = true;
        this.dossierService.affecterAuRecouvrementJuridique(dossierId).subscribe({
          next: (dossier) => {
            this.snackBar.open('Dossier affecté au recouvrement juridique avec succès', 'Fermer', {
              duration: 3000,
              panelClass: ['success-snackbar']
            });
            this.loadDossiers();
          },
          error: (error) => {
            const errorMessage = error.message || 'Erreur lors de l\'affectation';
            this.snackBar.open(errorMessage, 'Fermer', {
              duration: 5000,
              panelClass: ['error-snackbar']
            });
            this.loading = false;
          }
        });
      }
    });
  }

  cloturerDossier(dossierId: number): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        title: 'Clôturer le Dossier',
        message: 'Êtes-vous sûr de vouloir clôturer ce dossier ? Cette action est irréversible.',
        confirmText: 'Clôturer',
        cancelText: 'Annuler',
        warning: true
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loading = true;
        this.dossierService.cloturerDossier(dossierId).subscribe({
          next: (dossier) => {
            this.snackBar.open('Dossier clôturé avec succès', 'Fermer', {
              duration: 3000,
              panelClass: ['success-snackbar']
            });
            this.loadDossiers();
          },
          error: (error) => {
            const errorMessage = error.message || 'Erreur lors de la clôture';
            this.snackBar.open(errorMessage, 'Fermer', {
              duration: 5000,
              panelClass: ['error-snackbar']
            });
            this.loading = false;
          }
        });
      }
    });
  }
}
```

IMPORTANT :
- Utiliser MatTable, MatPaginator, MatSort pour le tableau
- Utiliser MatDialog pour les confirmations
- Utiliser MatSnackBar pour les notifications
- Gérer les états de chargement
- Implémenter la pagination côté serveur
- Utiliser debounce pour la recherche
```

---

## 🎯 PROMPT 3 : Création du Dialog de Confirmation

**Prompt à copier dans Cursor AI :**

```
Dans le projet Angular, créez un composant de dialog de confirmation réutilisable (confirmation-dialog.component.ts et confirmation-dialog.component.html).

Ce composant doit :

1. Accepter des données d'entrée :
   - title: string (titre du dialog)
   - message: string (message de confirmation)
   - confirmText: string (texte du bouton de confirmation, défaut: "Confirmer")
   - cancelText: string (texte du bouton d'annulation, défaut: "Annuler")
   - warning: boolean (afficher en rouge si true, défaut: false)

2. Afficher :
   - Un titre (MatDialogTitle)
   - Un message (MatDialogContent)
   - Deux boutons : Confirmer et Annuler (MatDialogActions)

3. Retourner :
   - true si l'utilisateur clique sur "Confirmer"
   - false si l'utilisateur clique sur "Annuler" ou ferme le dialog

CODE EXEMPLE :

```typescript
import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-confirmation-dialog',
  template: `
    <h2 mat-dialog-title>{{ data.title }}</h2>
    <mat-dialog-content>
      <p [class.warning]="data.warning">{{ data.message }}</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">{{ data.cancelText || 'Annuler' }}</button>
      <button 
        mat-button 
        [color]="data.warning ? 'warn' : 'primary'"
        (click)="onConfirm()">
        {{ data.confirmText || 'Confirmer' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .warning {
      color: #f44336;
      font-weight: bold;
    }
    mat-dialog-content {
      min-width: 300px;
    }
  `]
})
export class ConfirmationDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ConfirmationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {
      title: string;
      message: string;
      confirmText?: string;
      cancelText?: string;
      warning?: boolean;
    }
  ) {}

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
```

IMPORTANT :
- Déclarer ce composant dans le module ou utiliser standalone
- Exporter le composant pour qu'il soit utilisable dans d'autres composants
```

---

## ✅ Checklist de Vérification Frontend

- [ ] Les méthodes sont ajoutées dans DossierService
- [ ] Le composant charge les dossiers validés disponibles
- [ ] La recherche fonctionne avec debounce
- [ ] Les filtres et tri fonctionnent
- [ ] Les boutons d'affectation fonctionnent
- [ ] Le bouton de clôture fonctionne
- [ ] Les dialogs de confirmation sont implémentés
- [ ] Les messages de succès/erreur s'affichent
- [ ] La liste se rafraîchit après les actions
- [ ] La pagination fonctionne
- [ ] Les états de chargement sont gérés

---

## 📋 Messages d'Erreur Possibles

| Message Backend | Signification | Action Frontend |
|----------------|---------------|-----------------|
| "Dossier non trouvé avec l'ID: X" | Le dossier n'existe pas | Afficher message d'erreur |
| "Seuls les dossiers validés peuvent être affectés" | Le dossier n'est pas validé | Afficher message d'avertissement |
| "Aucun chef du département recouvrement amiable trouvé" | Pas de chef amiable disponible | Afficher message d'erreur |
| "Aucun chef du département recouvrement juridique trouvé" | Pas de chef juridique disponible | Afficher message d'erreur |

---

**Ces prompts vous permettront d'implémenter complètement la fonctionnalité d'affectation et de clôture des dossiers ! 🚀**





