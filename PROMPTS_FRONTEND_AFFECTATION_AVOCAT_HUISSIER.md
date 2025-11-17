# 🎨 Prompts Frontend - Affectation d'Avocat et Huissier aux Dossiers

## 🎯 Objectif

Créer les interfaces complètes pour gérer l'affectation d'avocats et d'huissiers aux dossiers, en consommant correctement les APIs backend.

---

## 📋 PROMPT 1 : Mise à Jour du Service DossierService (Frontend)

**Prompt à copier dans Cursor AI :**

```
Dans le projet Angular, localisez le service DossierService (probablement dans src/app/services/dossier.service.ts).

Ajoutez les méthodes suivantes pour consommer les APIs d'affectation d'avocat et d'huissier :

1. assignerAvocat(dossierId: number, avocatId: number): Observable<Dossier>
   - PUT /api/dossiers/{dossierId}/assign/avocat?avocatId={avocatId}
   - Retourne le dossier mis à jour
   - Gère les erreurs : 400 (dossier/avocat non trouvé), 404, 500

2. assignerHuissier(dossierId: number, huissierId: number): Observable<Dossier>
   - PUT /api/dossiers/{dossierId}/assign/huissier?huissierId={huissierId}
   - Retourne le dossier mis à jour
   - Gère les erreurs : 400 (dossier/huissier non trouvé), 404, 500

3. affecterAvocatEtHuissier(dossierId: number, affectation: {avocatId?: number | null, huissierId?: number | null}): Observable<Dossier>
   - PUT /api/dossiers/{dossierId}/assign/avocat-huissier
   - Body: { "avocatId": number | null, "huissierId": number | null }
   - Permet d'affecter soit un avocat, soit un huissier, soit les deux
   - Si un ID est null, l'affectation correspondante sera retirée
   - Retourne le dossier mis à jour
   - Gère les erreurs : 400 (dossier/avocat/huissier non trouvé), 404, 500

CODE EXEMPLE :

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Dossier } from '../models/dossier';

export interface AffectationDossierDTO {
  avocatId?: number | null;
  huissierId?: number | null;
}

@Injectable({
  providedIn: 'root'
})
export class DossierService {
  private apiUrl = 'http://localhost:8089/carthage-creance/api/dossiers';

  constructor(private http: HttpClient) {}

  /**
   * Assigne un avocat à un dossier
   */
  assignerAvocat(dossierId: number, avocatId: number): Observable<Dossier> {
    const params = new HttpParams().set('avocatId', avocatId.toString());
    
    return this.http.put<Dossier>(
      `${this.apiUrl}/${dossierId}/assign/avocat`,
      null,
      { params }
    ).pipe(
      catchError((error) => {
        console.error('Erreur lors de l\'assignation de l\'avocat:', error);
        let errorMessage = 'Erreur lors de l\'assignation de l\'avocat';
        
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
   * Assigne un huissier à un dossier
   */
  assignerHuissier(dossierId: number, huissierId: number): Observable<Dossier> {
    const params = new HttpParams().set('huissierId', huissierId.toString());
    
    return this.http.put<Dossier>(
      `${this.apiUrl}/${dossierId}/assign/huissier`,
      null,
      { params }
    ).pipe(
      catchError((error) => {
        console.error('Erreur lors de l\'assignation de l\'huissier:', error);
        let errorMessage = 'Erreur lors de l\'assignation de l\'huissier';
        
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
   * Affecte un dossier à un avocat et/ou un huissier de manière flexible
   * Permet d'affecter soit un avocat, soit un huissier, soit les deux
   * Si un ID est null, l'affectation correspondante sera retirée
   */
  affecterAvocatEtHuissier(
    dossierId: number, 
    affectation: AffectationDossierDTO
  ): Observable<Dossier> {
    return this.http.put<Dossier>(
      `${this.apiUrl}/${dossierId}/assign/avocat-huissier`,
      affectation
    ).pipe(
      catchError((error) => {
        console.error('Erreur lors de l\'affectation avocat/huissier:', error);
        let errorMessage = 'Erreur lors de l\'affectation avocat/huissier';
        
        if (error.error?.message) {
          errorMessage = error.error.message;
        } else if (error.error?.error) {
          errorMessage = error.error.error;
        }
        
        return throwError(() => new Error(errorMessage));
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
- Gérer les cas où avocatId ou huissierId peuvent être null
```

---

## 📋 PROMPT 2 : Création/Mise à Jour du Service AvocatService et HuissierService

**Prompt à copier dans Cursor AI :**

```
Dans le projet Angular, créez ou mettez à jour les services pour gérer les avocats et huissiers :

1. AvocatService (src/app/services/avocat.service.ts) :
   - getAllAvocats(): Observable<Avocat[]>
   - getAvocatById(id: number): Observable<Avocat>
   - searchAvocats(searchTerm: string): Observable<Avocat[]>

2. HuissierService (src/app/services/huissier.service.ts) :
   - getAllHuissiers(): Observable<Huissier[]>
   - getHuissierById(id: number): Observable<Huissier>
   - searchHuissiers(searchTerm: string): Observable<Huissier[]>

CODE EXEMPLE :

```typescript
// avocat.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';

export interface Avocat {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  adresse?: string;
  specialite?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AvocatService {
  private apiUrl = 'http://localhost:8089/carthage-creance/api/avocats';

  constructor(private http: HttpClient) {}

  getAllAvocats(): Observable<Avocat[]> {
    return this.http.get<Avocat[]>(this.apiUrl).pipe(
      catchError((error) => {
        console.error('Erreur lors de la récupération des avocats:', error);
        return throwError(() => new Error('Erreur lors de la récupération des avocats'));
      })
    );
  }

  getAvocatById(id: number): Observable<Avocat> {
    return this.http.get<Avocat>(`${this.apiUrl}/${id}`).pipe(
      catchError((error) => {
        console.error('Erreur lors de la récupération de l\'avocat:', error);
        return throwError(() => new Error('Erreur lors de la récupération de l\'avocat'));
      })
    );
  }

  searchAvocats(searchTerm: string): Observable<Avocat[]> {
    const params = new HttpParams().set('search', searchTerm);
    return this.http.get<Avocat[]>(`${this.apiUrl}/search`, { params }).pipe(
      catchError((error) => {
        console.error('Erreur lors de la recherche d\'avocats:', error);
        return throwError(() => new Error('Erreur lors de la recherche d\'avocats'));
      })
    );
  }
}
```

```typescript
// huissier.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';

export interface Huissier {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  adresse?: string;
  specialite?: string;
}

@Injectable({
  providedIn: 'root'
})
export class HuissierService {
  private apiUrl = 'http://localhost:8089/carthage-creance/api/huissiers';

  constructor(private http: HttpClient) {}

  getAllHuissiers(): Observable<Huissier[]> {
    return this.http.get<Huissier[]>(this.apiUrl).pipe(
      catchError((error) => {
        console.error('Erreur lors de la récupération des huissiers:', error);
        return throwError(() => new Error('Erreur lors de la récupération des huissiers'));
      })
    );
  }

  getHuissierById(id: number): Observable<Huissier> {
    return this.http.get<Huissier>(`${this.apiUrl}/${id}`).pipe(
      catchError((error) => {
        console.error('Erreur lors de la récupération de l\'huissier:', error);
        return throwError(() => new Error('Erreur lors de la récupération de l\'huissier'));
      })
    );
  }

  searchHuissiers(searchTerm: string): Observable<Huissier[]> {
    const params = new HttpParams().set('search', searchTerm);
    return this.http.get<Huissier[]>(`${this.apiUrl}/search`, { params }).pipe(
      catchError((error) => {
        console.error('Erreur lors de la recherche d\'huissiers:', error);
        return throwError(() => new Error('Erreur lors de la recherche d\'huissiers'));
      })
    );
  }
}
```

IMPORTANT :
- Créer les interfaces TypeScript correspondantes
- Gérer les erreurs HTTP
- Utiliser des Observables typés
```

---

## 📋 PROMPT 3 : Création du Composant d'Affectation Avocat/Huissier

**Prompt à copier dans Cursor AI :**

```
Dans le projet Angular, créez un composant pour gérer l'affectation d'avocat et d'huissier aux dossiers :

Fichier : src/app/components/affectation-avocat-huissier/affectation-avocat-huissier.component.ts

Ce composant doit :

1. Accepter un dossierId en input (@Input)
2. Charger les avocats et huissiers disponibles au ngOnInit
3. Afficher les affectations actuelles (si existantes)
4. Permettre de sélectionner un avocat et/ou un huissier
5. Permettre de retirer une affectation (mettre à null)
6. Afficher un formulaire avec :
   - Un select pour l'avocat (avec option "Aucun" pour retirer)
   - Un select pour l'huissier (avec option "Aucun" pour retirer)
   - Un bouton "Enregistrer" pour sauvegarder
7. Gérer les états de chargement et les erreurs
8. Afficher des messages de succès/erreur avec MatSnackBar

CODE EXEMPLE :

```typescript
import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { DossierService } from '../../services/dossier.service';
import { AvocatService, Avocat } from '../../services/avocat.service';
import { HuissierService, Huissier } from '../../services/huissier.service';
import { Dossier } from '../../models/dossier';

@Component({
  selector: 'app-affectation-avocat-huissier',
  templateUrl: './affectation-avocat-huissier.component.html',
  styleUrls: ['./affectation-avocat-huissier.component.css']
})
export class AffectationAvocatHuissierComponent implements OnInit {
  @Input() dossierId!: number;
  @Input() dossier?: Dossier;

  affectationForm!: FormGroup;
  avocats: Avocat[] = [];
  huissiers: Huissier[] = [];
  loading = false;
  loadingData = false;

  constructor(
    private fb: FormBuilder,
    private dossierService: DossierService,
    private avocatService: AvocatService,
    private huissierService: HuissierService,
    private snackBar: MatSnackBar
  ) {
    this.affectationForm = this.fb.group({
      avocatId: [null],
      huissierId: [null]
    });
  }

  ngOnInit(): void {
    this.loadAvocats();
    this.loadHuissiers();
    this.loadDossierData();
  }

  loadAvocats(): void {
    this.loadingData = true;
    this.avocatService.getAllAvocats().subscribe({
      next: (avocats) => {
        this.avocats = avocats;
        this.loadingData = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des avocats:', error);
        this.snackBar.open('Erreur lors du chargement des avocats', 'Fermer', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
        this.loadingData = false;
      }
    });
  }

  loadHuissiers(): void {
    this.loadingData = true;
    this.huissierService.getAllHuissiers().subscribe({
      next: (huissiers) => {
        this.huissiers = huissiers;
        this.loadingData = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des huissiers:', error);
        this.snackBar.open('Erreur lors du chargement des huissiers', 'Fermer', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
        this.loadingData = false;
      }
    });
  }

  loadDossierData(): void {
    if (this.dossier) {
      // Si le dossier est déjà chargé, utiliser ses données
      this.affectationForm.patchValue({
        avocatId: this.dossier.avocat?.id || null,
        huissierId: this.dossier.huissier?.id || null
      });
    } else if (this.dossierId) {
      // Sinon, charger le dossier
      this.loadingData = true;
      this.dossierService.getDossierById(this.dossierId).subscribe({
        next: (dossier) => {
          this.dossier = dossier;
          this.affectationForm.patchValue({
            avocatId: dossier.avocat?.id || null,
            huissierId: dossier.huissier?.id || null
          });
          this.loadingData = false;
        },
        error: (error) => {
          console.error('Erreur lors du chargement du dossier:', error);
          this.snackBar.open('Erreur lors du chargement du dossier', 'Fermer', {
            duration: 5000,
            panelClass: ['error-snackbar']
          });
          this.loadingData = false;
        }
      });
    }
  }

  onSubmit(): void {
    if (this.affectationForm.invalid || !this.dossierId) {
      return;
    }

    this.loading = true;
    const formValue = this.affectationForm.value;

    // Préparer l'objet d'affectation
    const affectation = {
      avocatId: formValue.avocatId || null,
      huissierId: formValue.huissierId || null
    };

    // Vérifier qu'au moins un est sélectionné
    if (!affectation.avocatId && !affectation.huissierId) {
      this.snackBar.open('Veuillez sélectionner au moins un avocat ou un huissier', 'Fermer', {
        duration: 5000,
        panelClass: ['warning-snackbar']
      });
      this.loading = false;
      return;
    }

    this.dossierService.affecterAvocatEtHuissier(this.dossierId, affectation).subscribe({
      next: (dossier) => {
        this.dossier = dossier;
        this.snackBar.open('Affectation enregistrée avec succès', 'Fermer', {
          duration: 3000,
          panelClass: ['success-snackbar']
        });
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur lors de l\'affectation:', error);
        const errorMessage = error.message || 'Erreur lors de l\'affectation';
        this.snackBar.open(errorMessage, 'Fermer', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
        this.loading = false;
      }
    });
  }

  getAvocatDisplayName(avocat: Avocat): string {
    return `${avocat.prenom} ${avocat.nom}${avocat.specialite ? ` - ${avocat.specialite}` : ''}`;
  }

  getHuissierDisplayName(huissier: Huissier): string {
    return `${huissier.prenom} ${huissier.nom}${huissier.specialite ? ` - ${huissier.specialite}` : ''}`;
  }
}
```

IMPORTANT :
- Utiliser Reactive Forms
- Gérer les états de chargement
- Afficher les messages d'erreur/succès
- Valider qu'au moins un est sélectionné (ou permettre de retirer les deux)
- Utiliser MatSnackBar pour les notifications
```

---

## 📋 PROMPT 4 : Template HTML du Composant d'Affectation

**Prompt à copier dans Cursor AI :**

```
Créez le template HTML pour le composant d'affectation avocat/huissier :

Fichier : src/app/components/affectation-avocat-huissier/affectation-avocat-huissier.component.html

Le template doit inclure :

1. Un formulaire avec Material Design
2. Deux mat-select pour avocat et huissier
3. Affichage des affectations actuelles (si existantes)
4. Bouton de sauvegarde
5. Gestion des états de chargement
6. Messages d'information

CODE EXEMPLE :

```html
<div class="affectation-container">
  <h2>Affectation Avocat / Huissier</h2>

  <!-- Affichage des affectations actuelles -->
  <div *ngIf="dossier" class="current-assignments">
    <mat-card *ngIf="dossier.avocat" class="assignment-card">
      <mat-card-header>
        <mat-card-title>Avocat actuel</mat-card-title>
      </mat-card-header>
      <mat-card-content>
        <p><strong>{{ dossier.avocat.prenom }} {{ dossier.avocat.nom }}</strong></p>
        <p *ngIf="dossier.avocat.email">{{ dossier.avocat.email }}</p>
        <p *ngIf="dossier.avocat.telephone">{{ dossier.avocat.telephone }}</p>
        <p *ngIf="dossier.avocat.specialite">{{ dossier.avocat.specialite }}</p>
      </mat-card-content>
    </mat-card>

    <mat-card *ngIf="dossier.huissier" class="assignment-card">
      <mat-card-header>
        <mat-card-title>Huissier actuel</mat-card-title>
      </mat-card-header>
      <mat-card-content>
        <p><strong>{{ dossier.huissier.prenom }} {{ dossier.huissier.nom }}</strong></p>
        <p *ngIf="dossier.huissier.email">{{ dossier.huissier.email }}</p>
        <p *ngIf="dossier.huissier.telephone">{{ dossier.huissier.telephone }}</p>
        <p *ngIf="dossier.huissier.specialite">{{ dossier.huissier.specialite }}</p>
      </mat-card-content>
    </mat-card>

    <mat-card *ngIf="!dossier.avocat && !dossier.huissier" class="no-assignment-card">
      <mat-card-content>
        <p>Aucun avocat ni huissier n'est actuellement affecté à ce dossier.</p>
      </mat-card-content>
    </mat-card>
  </div>

  <!-- Formulaire d'affectation -->
  <form [formGroup]="affectationForm" (ngSubmit)="onSubmit()" class="affectation-form">
    <div class="form-row">
      <!-- Sélection Avocat -->
      <mat-form-field appearance="outline" class="full-width">
        <mat-label>Avocat</mat-label>
        <mat-select formControlName="avocatId">
          <mat-option [value]="null">Aucun (retirer l'affectation)</mat-option>
          <mat-option *ngFor="let avocat of avocats" [value]="avocat.id">
            {{ getAvocatDisplayName(avocat) }}
          </mat-option>
        </mat-select>
        <mat-hint>Sélectionnez un avocat ou "Aucun" pour retirer l'affectation</mat-hint>
      </mat-form-field>
    </div>

    <div class="form-row">
      <!-- Sélection Huissier -->
      <mat-form-field appearance="outline" class="full-width">
        <mat-label>Huissier</mat-label>
        <mat-select formControlName="huissierId">
          <mat-option [value]="null">Aucun (retirer l'affectation)</mat-option>
          <mat-option *ngFor="let huissier of huissiers" [value]="huissier.id">
            {{ getHuissierDisplayName(huissier) }}
          </mat-option>
        </mat-select>
        <mat-hint>Sélectionnez un huissier ou "Aucun" pour retirer l'affectation</mat-hint>
      </mat-form-field>
    </div>

    <div class="form-actions">
      <button 
        mat-raised-button 
        color="primary" 
        type="submit"
        [disabled]="affectationForm.invalid || loading || loadingData">
        <mat-spinner *ngIf="loading" diameter="20" class="button-spinner"></mat-spinner>
        <span *ngIf="!loading">Enregistrer</span>
        <span *ngIf="loading">Enregistrement...</span>
      </button>
      <button 
        mat-button 
        type="button"
        (click)="loadDossierData()"
        [disabled]="loading || loadingData">
        Annuler
      </button>
    </div>
  </form>

  <!-- Spinner de chargement -->
  <div *ngIf="loadingData" class="loading-overlay">
    <mat-spinner></mat-spinner>
    <p>Chargement des données...</p>
  </div>
</div>
```

IMPORTANT :
- Utiliser Material Design components
- Gérer les états de chargement
- Afficher clairement les affectations actuelles
- Permettre de retirer une affectation (option "Aucun")
```

---

## 📋 PROMPT 5 : Styles CSS du Composant

**Prompt à copier dans Cursor AI :**

```
Créez les styles CSS pour le composant d'affectation :

Fichier : src/app/components/affectation-avocat-huissier/affectation-avocat-huissier.component.css

CODE EXEMPLE :

```css
.affectation-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.affectation-container h2 {
  margin-bottom: 20px;
  color: #333;
}

.current-assignments {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.assignment-card {
  background-color: #f5f5f5;
}

.assignment-card mat-card-content {
  padding-top: 10px;
}

.assignment-card p {
  margin: 5px 0;
}

.no-assignment-card {
  background-color: #fff3cd;
  border: 1px solid #ffc107;
}

.affectation-form {
  margin-top: 30px;
}

.form-row {
  margin-bottom: 20px;
}

.full-width {
  width: 100%;
}

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 20px;
}

.button-spinner {
  display: inline-block;
  margin-right: 10px;
}

.loading-overlay {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  text-align: center;
}

.loading-overlay mat-spinner {
  margin-bottom: 20px;
}

/* Responsive */
@media (max-width: 768px) {
  .affectation-container {
    padding: 10px;
  }

  .current-assignments {
    grid-template-columns: 1fr;
  }

  .form-actions {
    flex-direction: column;
  }

  .form-actions button {
    width: 100%;
  }
}
```

IMPORTANT :
- Design responsive
- Utiliser les couleurs Material Design
- Espacement cohérent
```

---

## 📋 PROMPT 6 : Intégration dans la Page de Détails du Dossier

**Prompt à copier dans Cursor AI :**

```
Dans le composant de détails du dossier (dossier-detail.component.ts), intégrez le composant d'affectation avocat/huissier :

1. Ajoutez un onglet ou une section "Affectation Avocat/Huissier"
2. Passez le dossierId et le dossier au composant d'affectation
3. Rafraîchissez les données du dossier après une affectation réussie

CODE EXEMPLE :

```html
<!-- Dans dossier-detail.component.html -->
<mat-tab-group>
  <mat-tab label="Informations générales">
    <!-- Contenu existant -->
  </mat-tab>
  
  <mat-tab label="Affectation Avocat/Huissier">
    <app-affectation-avocat-huissier 
      [dossierId]="dossier.id"
      [dossier]="dossier">
    </app-affectation-avocat-huissier>
  </mat-tab>
  
  <!-- Autres onglets -->
</mat-tab-group>
```

```typescript
// Dans dossier-detail.component.ts
// Ajoutez un EventEmitter pour rafraîchir les données après affectation
@ViewChild(AffectationAvocatHuissierComponent) affectationComponent?: AffectationAvocatHuissierComponent;

// Ou écoutez les changements via un service partagé
```

IMPORTANT :
- Intégrer dans l'interface existante
- Rafraîchir les données après affectation
- Gérer les erreurs
```

---

## 📋 PROMPT 7 : Affichage dans le Tableau des Dossiers

**Prompt à copier dans Cursor AI :**

```
Dans le composant de liste des dossiers, ajoutez des colonnes pour afficher l'avocat et l'huissier affectés :

1. Ajoutez les colonnes "Avocat" et "Huissier" dans displayedColumns
2. Affichez le nom de l'avocat/huissier ou "Non affecté"
3. Ajoutez un bouton d'action pour ouvrir le dialog d'affectation

CODE EXEMPLE :

```html
<!-- Dans la table des dossiers -->
<ng-container matColumnDef="avocat">
  <th mat-header-cell *matHeaderCellDef>Avocat</th>
  <td mat-cell *matCellDef="let dossier">
    <span *ngIf="dossier.avocat">
      {{ dossier.avocat.prenom }} {{ dossier.avocat.nom }}
    </span>
    <span *ngIf="!dossier.avocat" class="not-assigned">Non affecté</span>
  </td>
</ng-container>

<ng-container matColumnDef="huissier">
  <th mat-header-cell *matHeaderCellDef>Huissier</th>
  <td mat-cell *matCellDef="let dossier">
    <span *ngIf="dossier.huissier">
      {{ dossier.huissier.prenom }} {{ dossier.huissier.nom }}
    </span>
    <span *ngIf="!dossier.huissier" class="not-assigned">Non affecté</span>
  </td>
</ng-container>

<ng-container matColumnDef="affectationActions">
  <th mat-header-cell *matHeaderCellDef>Actions</th>
  <td mat-cell *matCellDef="let dossier">
    <button 
      mat-icon-button 
      matTooltip="Affecter avocat/huissier"
      (click)="openAffectationDialog(dossier)">
      <mat-icon>assignment</mat-icon>
    </button>
  </td>
</ng-container>
```

```typescript
// Dans le composant
openAffectationDialog(dossier: Dossier): void {
  const dialogRef = this.dialog.open(AffectationAvocatHuissierDialogComponent, {
    width: '600px',
    data: { dossier }
  });

  dialogRef.afterClosed().subscribe(result => {
    if (result) {
      // Rafraîchir la liste des dossiers
      this.loadDossiers();
    }
  });
}
```

IMPORTANT :
- Afficher clairement les affectations
- Permettre d'ouvrir le dialog depuis la liste
- Rafraîchir après modification
```

---

## 📋 PROMPT 8 : Création d'un Dialog d'Affectation (Optionnel)

**Prompt à copier dans Cursor AI :**

```
Créez un dialog Material pour l'affectation avocat/huissier qui peut être ouvert depuis la liste des dossiers :

Fichier : src/app/components/affectation-avocat-huissier-dialog/affectation-avocat-huissier-dialog.component.ts

CODE EXEMPLE :

```typescript
import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Dossier } from '../../models/dossier';
import { AffectationAvocatHuissierComponent } from '../affectation-avocat-huissier/affectation-avocat-huissier.component';

@Component({
  selector: 'app-affectation-avocat-huissier-dialog',
  template: `
    <h2 mat-dialog-title>Affecter Avocat / Huissier</h2>
    <mat-dialog-content>
      <app-affectation-avocat-huissier 
        [dossierId]="data.dossier.id"
        [dossier]="data.dossier">
      </app-affectation-avocat-huissier>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onClose()">Fermer</button>
    </mat-dialog-actions>
  `
})
export class AffectationAvocatHuissierDialogComponent implements OnInit {
  constructor(
    public dialogRef: MatDialogRef<AffectationAvocatHuissierDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { dossier: Dossier }
  ) {}

  ngOnInit(): void {}

  onClose(): void {
    this.dialogRef.close();
  }
}
```

IMPORTANT :
- Réutiliser le composant d'affectation
- Gérer la fermeture du dialog
- Retourner un résultat si nécessaire
```

---

## ✅ Checklist de Vérification Frontend

- [ ] Les méthodes sont ajoutées dans DossierService
- [ ] Les services AvocatService et HuissierService sont créés/mis à jour
- [ ] Le composant d'affectation est créé et fonctionne
- [ ] Le template HTML est complet et responsive
- [ ] Les styles CSS sont appliqués
- [ ] L'intégration dans la page de détails fonctionne
- [ ] L'affichage dans le tableau des dossiers fonctionne
- [ ] Le dialog d'affectation fonctionne (si implémenté)
- [ ] Les messages de succès/erreur s'affichent correctement
- [ ] Les états de chargement sont gérés
- [ ] La validation du formulaire fonctionne
- [ ] Les erreurs HTTP sont gérées correctement

---

## 📋 Messages d'Erreur Possibles

| Message Backend | Signification | Action Frontend |
|----------------|---------------|-----------------|
| "Dossier non trouvé avec l'ID: X" | Le dossier n'existe pas | Afficher message d'erreur, rediriger si nécessaire |
| "Avocat non trouvé avec l'ID: X" | L'avocat n'existe pas | Afficher message d'erreur, recharger la liste |
| "Huissier non trouvé avec l'ID: X" | L'huissier n'existe pas | Afficher message d'erreur, recharger la liste |
| "Erreur d'affectation" | Erreur générique | Afficher le message d'erreur détaillé |

---

## 🎯 Exemples d'Utilisation

### Affecter uniquement un avocat :
```typescript
this.dossierService.affecterAvocatEtHuissier(dossierId, {
  avocatId: 3,
  huissierId: null
}).subscribe(...);
```

### Affecter uniquement un huissier :
```typescript
this.dossierService.affecterAvocatEtHuissier(dossierId, {
  avocatId: null,
  huissierId: 2
}).subscribe(...);
```

### Affecter les deux :
```typescript
this.dossierService.affecterAvocatEtHuissier(dossierId, {
  avocatId: 3,
  huissierId: 2
}).subscribe(...);
```

### Retirer les deux affectations :
```typescript
this.dossierService.affecterAvocatEtHuissier(dossierId, {
  avocatId: null,
  huissierId: null
}).subscribe(...);
```

---

**Ces prompts vous permettront d'implémenter complètement la fonctionnalité d'affectation d'avocat et d'huissier aux dossiers ! 🚀**

