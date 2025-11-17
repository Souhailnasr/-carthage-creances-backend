# 🎨 Prompts Frontend - Gestion Complète des Audiences

## 🎯 Objectif

Créer les interfaces complètes pour gérer les audiences (audiences judiciaires) dans le frontend, en consommant correctement toutes les APIs backend disponibles.

## ⚠️ IMPORTANT : Utilisation de Données Réelles

**TOUTES les données doivent être chargées depuis les APIs backend réelles.**
- ❌ **NE PAS** utiliser de données mockées, statiques ou hardcodées
- ✅ **TOUJOURS** utiliser les services Angular qui appellent les APIs backend
- ✅ **TOUJOURS** charger les données via `subscribe()` sur les Observables des services
- ✅ Les tableaux vides (`dossiers: any[] = []`) sont uniquement des initialisations - les données réelles sont chargées dans `ngOnInit()` via les services

---

## 📋 PROMPT 1 : Création des Interfaces TypeScript

**Prompt à copier dans Cursor AI :**

```
Dans le projet Angular, créez les interfaces TypeScript pour les audiences :

Fichier : src/app/models/audience.interface.ts

1. Créer l'interface Audience
2. Créer les enums TribunalType et DecisionResult
3. Créer les interfaces pour Avocat et Huissier (simplifiées, sans dossiers)

CODE EXEMPLE :

```typescript
// audience.interface.ts

export enum TribunalType {
  TRIBUNAL_PREMIERE_INSTANCE = 'TRIBUNAL_PREMIERE_INSTANCE',
  TRIBUNAL_APPEL = 'TRIBUNAL_APPEL',
  TRIBUNAL_CASSATION = 'TRIBUNAL_CASSATION'
}

export enum DecisionResult {
  POSITIVE = 'POSITIVE',
  NEGATIVE = 'NEGATIVE',
  Rapporter = 'Rapporter'
}

export interface Avocat {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  specialite?: string;
  adresse?: string;
}

export interface Huissier {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  specialite?: string;
  adresse?: string;
}

export interface Audience {
  id?: number;
  dateAudience: string; // Format ISO: "YYYY-MM-DD"
  dateProchaine?: string; // Format ISO: "YYYY-MM-DD" - prochaine audience si reportée
  tribunalType?: TribunalType;
  lieuTribunal?: string;
  commentaireDecision?: string;
  resultat?: DecisionResult;
  
  // Relations (simplifiées)
  dossier?: {
    id: number;
    numeroDossier?: string;
    titre?: string;
  };
  avocat?: Avocat;
  huissier?: Huissier;
}

export interface AudienceRequest {
  dateAudience: string;
  dateProchaine?: string;
  tribunalType?: TribunalType;
  lieuTribunal?: string;
  commentaireDecision?: string;
  resultat?: DecisionResult;
  
  // Format 1 : IDs simples (recommandé)
  dossierId?: number; // ID du dossier (obligatoire)
  avocatId?: number; // ID de l'avocat (optionnel)
  huissierId?: number; // ID de l'huissier (optionnel)
  
  // Format 2 : Objets avec ID (pour compatibilité - également accepté par le backend)
  dossier?: { id: number };
  avocat?: { id: number };
  huissier?: { id: number };
}

export interface AudienceFilters {
  dossierId?: number;
  date?: string;
  startDate?: string;
  endDate?: string;
  tribunalType?: TribunalType;
  resultat?: DecisionResult;
  avocatId?: number;
  huissierId?: number;
  location?: string;
}
```

IMPORTANT :
- Utiliser des strings pour les dates (format ISO)
- Les enums doivent correspondre exactement aux valeurs backend
- Le dossier est représenté par son ID dans les requêtes (obligatoire)
- Les relations avocat/huissier sont optionnelles
- Le backend accepte les deux formats : dossierId ou dossier: { id }
- Le format avec IDs simples (dossierId) est recommandé
```

---

## 📋 PROMPT 2 : Création du Service AudienceService

**Prompt à copier dans Cursor AI :**

```
Dans le projet Angular, créez un service complet pour gérer les audiences :

Fichier : src/app/services/audience.service.ts

Implémentez toutes les méthodes pour consommer les APIs backend :

1. CRUD Operations :
   - createAudience(audience: AudienceRequest): Observable<Audience>
   - getAudienceById(id: number): Observable<Audience>
   - getAllAudiences(): Observable<Audience[]>
   - updateAudience(id: number, audience: AudienceRequest): Observable<Audience>
   - deleteAudience(id: number): Observable<void>

2. Search Operations :
   - getAudiencesByDossier(dossierId: number): Observable<Audience[]>
   - getAudiencesByDate(date: string): Observable<Audience[]>
   - getAudiencesByDateRange(startDate: string, endDate: string): Observable<Audience[]>
   - getAudiencesByTribunalType(tribunalType: TribunalType): Observable<Audience[]>
   - getAudiencesByResult(resultat: DecisionResult): Observable<Audience[]>
   - getAudiencesByAvocat(avocatId: number): Observable<Audience[]>
   - getAudiencesByHuissier(huissierId: number): Observable<Audience[]>
   - getAudiencesByLocation(location: string): Observable<Audience[]>

3. Special Operations :
   - getUpcomingAudiences(): Observable<Audience[]>
   - getPastAudiences(): Observable<Audience[]>
   - getNextAudienceByDossier(dossierId: number): Observable<Audience[]>

CODE EXEMPLE :

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Audience, AudienceRequest, TribunalType, DecisionResult } from '../models/audience.interface';

@Injectable({
  providedIn: 'root'
})
export class AudienceService {
  private apiUrl = 'http://localhost:8089/carthage-creance/api/audiences';

  constructor(private http: HttpClient) {}

  // ==================== CRUD Operations ====================

  createAudience(audience: AudienceRequest): Observable<Audience> {
    return this.http.post<Audience>(this.apiUrl, audience).pipe(
      catchError((error) => {
        console.error('Erreur lors de la création de l\'audience:', error);
        let errorMessage = 'Erreur lors de la création de l\'audience';
        if (error.error?.message) {
          errorMessage = error.error.message;
        }
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  getAudienceById(id: number): Observable<Audience> {
    return this.http.get<Audience>(`${this.apiUrl}/${id}`).pipe(
      catchError((error) => {
        console.error('Erreur lors de la récupération de l\'audience:', error);
        return throwError(() => new Error('Erreur lors de la récupération de l\'audience'));
      })
    );
  }

  getAllAudiences(): Observable<Audience[]> {
    return this.http.get<Audience[]>(this.apiUrl).pipe(
      catchError((error) => {
        console.error('Erreur lors de la récupération des audiences:', error);
        return throwError(() => new Error('Erreur lors de la récupération des audiences'));
      })
    );
  }

  updateAudience(id: number, audience: AudienceRequest): Observable<Audience> {
    return this.http.put<Audience>(`${this.apiUrl}/${id}`, audience).pipe(
      catchError((error) => {
        console.error('Erreur lors de la mise à jour de l\'audience:', error);
        let errorMessage = 'Erreur lors de la mise à jour de l\'audience';
        if (error.error?.message) {
          errorMessage = error.error.message;
        }
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  deleteAudience(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      catchError((error) => {
        console.error('Erreur lors de la suppression de l\'audience:', error);
        return throwError(() => new Error('Erreur lors de la suppression de l\'audience'));
      })
    );
  }

  // ==================== Search Operations ====================

  getAudiencesByDossier(dossierId: number): Observable<Audience[]> {
    return this.http.get<Audience[]>(`${this.apiUrl}/dossier/${dossierId}`).pipe(
      catchError((error) => {
        console.error('Erreur lors de la récupération des audiences du dossier:', error);
        return throwError(() => new Error('Erreur lors de la récupération des audiences du dossier'));
      })
    );
  }

  getAudiencesByDate(date: string): Observable<Audience[]> {
    return this.http.get<Audience[]>(`${this.apiUrl}/date/${date}`).pipe(
      catchError((error) => {
        console.error('Erreur lors de la récupération des audiences par date:', error);
        return throwError(() => new Error('Erreur lors de la récupération des audiences par date'));
      })
    );
  }

  getAudiencesByDateRange(startDate: string, endDate: string): Observable<Audience[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    
    return this.http.get<Audience[]>(`${this.apiUrl}/date-range`, { params }).pipe(
      catchError((error) => {
        console.error('Erreur lors de la récupération des audiences par plage de dates:', error);
        return throwError(() => new Error('Erreur lors de la récupération des audiences par plage de dates'));
      })
    );
  }

  getAudiencesByTribunalType(tribunalType: TribunalType): Observable<Audience[]> {
    return this.http.get<Audience[]>(`${this.apiUrl}/tribunal-type/${tribunalType}`).pipe(
      catchError((error) => {
        console.error('Erreur lors de la récupération des audiences par type de tribunal:', error);
        return throwError(() => new Error('Erreur lors de la récupération des audiences par type de tribunal'));
      })
    );
  }

  getAudiencesByResult(resultat: DecisionResult): Observable<Audience[]> {
    return this.http.get<Audience[]>(`${this.apiUrl}/result/${resultat}`).pipe(
      catchError((error) => {
        console.error('Erreur lors de la récupération des audiences par résultat:', error);
        return throwError(() => new Error('Erreur lors de la récupération des audiences par résultat'));
      })
    );
  }

  getAudiencesByAvocat(avocatId: number): Observable<Audience[]> {
    return this.http.get<Audience[]>(`${this.apiUrl}/avocat/${avocatId}`).pipe(
      catchError((error) => {
        console.error('Erreur lors de la récupération des audiences de l\'avocat:', error);
        return throwError(() => new Error('Erreur lors de la récupération des audiences de l\'avocat'));
      })
    );
  }

  getAudiencesByHuissier(huissierId: number): Observable<Audience[]> {
    return this.http.get<Audience[]>(`${this.apiUrl}/huissier/${huissierId}`).pipe(
      catchError((error) => {
        console.error('Erreur lors de la récupération des audiences de l\'huissier:', error);
        return throwError(() => new Error('Erreur lors de la récupération des audiences de l\'huissier'));
      })
    );
  }

  getAudiencesByLocation(location: string): Observable<Audience[]> {
    const params = new HttpParams().set('location', location);
    return this.http.get<Audience[]>(`${this.apiUrl}/location`, { params }).pipe(
      catchError((error) => {
        console.error('Erreur lors de la récupération des audiences par lieu:', error);
        return throwError(() => new Error('Erreur lors de la récupération des audiences par lieu'));
      })
    );
  }

  // ==================== Special Operations ====================

  getUpcomingAudiences(): Observable<Audience[]> {
    return this.http.get<Audience[]>(`${this.apiUrl}/upcoming`).pipe(
      catchError((error) => {
        console.error('Erreur lors de la récupération des audiences à venir:', error);
        return throwError(() => new Error('Erreur lors de la récupération des audiences à venir'));
      })
    );
  }

  getPastAudiences(): Observable<Audience[]> {
    return this.http.get<Audience[]>(`${this.apiUrl}/past`).pipe(
      catchError((error) => {
        console.error('Erreur lors de la récupération des audiences passées:', error);
        return throwError(() => new Error('Erreur lors de la récupération des audiences passées'));
      })
    );
  }

  getNextAudienceByDossier(dossierId: number): Observable<Audience[]> {
    return this.http.get<Audience[]>(`${this.apiUrl}/dossier/${dossierId}/next`).pipe(
      catchError((error) => {
        console.error('Erreur lors de la récupération de la prochaine audience:', error);
        return throwError(() => new Error('Erreur lors de la récupération de la prochaine audience'));
      })
    );
  }
}
```

IMPORTANT :
- Gérer toutes les erreurs HTTP
- Utiliser HttpParams pour les requêtes avec paramètres
- Logger les erreurs pour le débogage
- Retourner des Observables typés
```

---

## 📋 PROMPT 3 : Composant de Liste des Audiences

**Prompt à copier dans Cursor AI :**

```
Créez un composant pour afficher la liste des audiences avec filtres et recherche :

Fichier : src/app/components/audience-list/audience-list.component.ts

Fonctionnalités :
1. Afficher la liste des audiences dans un tableau Material
2. Filtres : par dossier, date, type de tribunal, résultat, avocat, huissier
3. Recherche par lieu
4. Onglets : Toutes / À venir / Passées
5. Pagination
6. Actions : Voir détails, Modifier, Supprimer

CODE EXEMPLE :

```typescript
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AudienceService } from '../../services/audience.service';
import { DossierService } from '../../services/dossier.service';
import { AvocatService } from '../../services/avocat.service';
import { HuissierService } from '../../services/huissier.service';
import { Audience, AudienceFilters, TribunalType, DecisionResult } from '../../models/audience.interface';
import { Subject, Observable } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

@Component({
  selector: 'app-audience-list',
  templateUrl: './audience-list.component.html',
  styleUrls: ['./audience-list.component.css']
})
export class AudienceListComponent implements OnInit {
  displayedColumns: string[] = [
    'dateAudience', 
    'dossier', 
    'tribunalType', 
    'lieuTribunal', 
    'avocat', 
    'huissier', 
    'resultat', 
    'actions'
  ];
  
  dataSource = new MatTableDataSource<Audience>([]);
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  loading = false;
  selectedTab = 0; // 0: Toutes, 1: À venir, 2: Passées
  
  // Filtres
  filterForm!: FormGroup;
  dossiers: any[] = [];
  avocats: any[] = [];
  huissiers: any[] = [];
  
  private searchSubject = new Subject<string>();

  constructor(
    private fb: FormBuilder,
    private audienceService: AudienceService,
    private dossierService: DossierService,
    private avocatService: AvocatService,
    private huissierService: HuissierService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {
    this.initFilterForm();
    
    // Debounce pour la recherche
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(searchTerm => {
      this.applyFilters();
    });
  }

  ngOnInit(): void {
    this.loadDossiers();
    this.loadAvocats();
    this.loadHuissiers();
    this.loadAudiences();
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  initFilterForm(): void {
    this.filterForm = this.fb.group({
      dossierId: [null],
      startDate: [null],
      endDate: [null],
      tribunalType: [null],
      resultat: [null],
      avocatId: [null],
      huissierId: [null],
      location: ['']
    });
  }

  loadDossiers(): void {
    this.dossierService.getAllDossiers({}).subscribe({
      next: (response) => {
        this.dossiers = response.content || [];
      },
      error: (error) => {
        console.error('Erreur lors du chargement des dossiers:', error);
      }
    });
  }

  loadAvocats(): void {
    this.avocatService.getAllAvocats().subscribe({
      next: (avocats) => {
        this.avocats = avocats;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des avocats:', error);
      }
    });
  }

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

  loadAudiences(): void {
    this.loading = true;
    
    let request: Observable<Audience[]>;
    
    switch (this.selectedTab) {
      case 1: // À venir
        request = this.audienceService.getUpcomingAudiences();
        break;
      case 2: // Passées
        request = this.audienceService.getPastAudiences();
        break;
      default: // Toutes
        request = this.audienceService.getAllAudiences();
    }
    
    request.subscribe({
      next: (audiences) => {
        this.dataSource.data = audiences;
        this.applyFilters();
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des audiences:', error);
        this.snackBar.open('Erreur lors du chargement des audiences', 'Fermer', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
        this.loading = false;
      }
    });
  }

  onTabChange(index: number): void {
    this.selectedTab = index;
    this.loadAudiences();
  }

  applyFilters(): void {
    const filters = this.filterForm.value;
    this.loading = true;
    
    // ✅ UTILISER LES APIs BACKEND au lieu de filtrer côté client
    let request: Observable<Audience[]>;
    
    // Priorité aux filtres spécifiques (utiliser les APIs dédiées)
    if (filters.dossierId) {
      request = this.audienceService.getAudiencesByDossier(filters.dossierId);
    } else if (filters.avocatId) {
      request = this.audienceService.getAudiencesByAvocat(filters.avocatId);
    } else if (filters.huissierId) {
      request = this.audienceService.getAudiencesByHuissier(filters.huissierId);
    } else if (filters.tribunalType) {
      request = this.audienceService.getAudiencesByTribunalType(filters.tribunalType);
    } else if (filters.resultat) {
      request = this.audienceService.getAudiencesByResult(filters.resultat);
    } else if (filters.startDate && filters.endDate) {
      const startStr = new Date(filters.startDate).toISOString().split('T')[0];
      const endStr = new Date(filters.endDate).toISOString().split('T')[0];
      request = this.audienceService.getAudiencesByDateRange(startStr, endStr);
    } else if (filters.location && filters.location.trim()) {
      request = this.audienceService.getAudiencesByLocation(filters.location.trim());
    } else {
      // Pas de filtre spécifique, charger selon l'onglet sélectionné
      switch (this.selectedTab) {
        case 1: // À venir
          request = this.audienceService.getUpcomingAudiences();
          break;
        case 2: // Passées
          request = this.audienceService.getPastAudiences();
          break;
        default: // Toutes
          request = this.audienceService.getAllAudiences();
      }
    }
    
    request.subscribe({
      next: (audiences) => {
        this.dataSource.data = audiences; // ✅ Données réelles depuis le backend
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des audiences:', error);
        this.snackBar.open('Erreur lors du chargement des audiences', 'Fermer', {
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

  clearFilters(): void {
    this.filterForm.reset();
    this.loadAudiences();
  }

  openAudienceDialog(audience?: Audience): void {
    // Implémenter le dialog de création/édition
  }

  deleteAudience(audience: Audience): void {
    if (!audience.id) return;
    
    if (confirm('Êtes-vous sûr de vouloir supprimer cette audience ?')) {
      this.audienceService.deleteAudience(audience.id).subscribe({
        next: () => {
          this.snackBar.open('Audience supprimée avec succès', 'Fermer', {
            duration: 3000,
            panelClass: ['success-snackbar']
          });
          this.loadAudiences();
        },
        error: (error) => {
          console.error('Erreur lors de la suppression:', error);
          this.snackBar.open('Erreur lors de la suppression', 'Fermer', {
            duration: 5000,
            panelClass: ['error-snackbar']
          });
        }
      });
    }
  }

  getTribunalTypeLabel(type: TribunalType): string {
    const labels: { [key: string]: string } = {
      'TRIBUNAL_PREMIERE_INSTANCE': 'Tribunal de Première Instance',
      'TRIBUNAL_APPEL': 'Tribunal d\'Appel',
      'TRIBUNAL_CASSATION': 'Tribunal de Cassation'
    };
    return labels[type] || type;
  }

  getDecisionResultLabel(result: DecisionResult): string {
    const labels: { [key: string]: string } = {
      'POSITIVE': 'Positive',
      'NEGATIVE': 'Négative',
      'Rapporter': 'Rapporter'
    };
    return labels[result] || result;
  }
}
```

IMPORTANT :
- ✅ **TOUJOURS charger les données depuis les APIs backend** via les services (AudienceService, DossierService, AvocatService, HuissierService)
- ❌ **NE JAMAIS utiliser de données mockées ou statiques**
- Utiliser MatTable avec pagination et tri
- Gérer les états de chargement
- Implémenter les filtres côté client ou serveur selon les besoins
- Afficher des messages d'erreur/succès
- Les méthodes `loadDossiers()`, `loadAvocats()`, `loadHuissiers()`, `loadAudiences()` doivent TOUJOURS appeler les services backend
```

---

## 📋 PROMPT 4 : Composant de Formulaire d'Audience

**Prompt à copier dans Cursor AI :**

```
Créez un composant de formulaire pour créer et modifier une audience :

Fichier : src/app/components/audience-form/audience-form.component.ts

Fonctionnalités :
1. Formulaire réactif avec validation
2. Sélection du dossier (obligatoire)
3. Sélection de l'avocat et/ou huissier (optionnels)
4. Sélection du type de tribunal
5. Date de l'audience (obligatoire)
6. Date prochaine (si reportée)
7. Lieu du tribunal
8. Commentaire de décision
9. Résultat (optionnel)

CODE EXEMPLE :

```typescript
import { Component, OnInit, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AudienceService } from '../../services/audience.service';
import { DossierService } from '../../services/dossier.service';
import { AvocatService } from '../../services/avocat.service';
import { HuissierService } from '../../services/huissier.service';
import { Audience, AudienceRequest, TribunalType, DecisionResult } from '../../models/audience.interface';

@Component({
  selector: 'app-audience-form',
  templateUrl: './audience-form.component.html',
  styleUrls: ['./audience-form.component.css']
})
export class AudienceFormComponent implements OnInit {
  audienceForm!: FormGroup;
  loading = false;
  
  dossiers: any[] = [];
  avocats: any[] = [];
  huissiers: any[] = [];
  
  tribunalTypes = Object.values(TribunalType);
  decisionResults = Object.values(DecisionResult);
  
  isEditMode = false;

  constructor(
    private fb: FormBuilder,
    private audienceService: AudienceService,
    private dossierService: DossierService,
    private avocatService: AvocatService,
    private huissierService: HuissierService,
    private dialogRef: MatDialogRef<AudienceFormComponent>,
    private snackBar: MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public data: { audience?: Audience; dossierId?: number }
  ) {
    this.initForm();
  }

  ngOnInit(): void {
    this.loadDossiers();
    this.loadAvocats();
    this.loadHuissiers();
    
    if (this.data?.audience) {
      this.isEditMode = true;
      this.populateForm(this.data.audience);
    } else if (this.data?.dossierId) {
      this.audienceForm.patchValue({ dossierId: this.data.dossierId });
    }
  }

  initForm(): void {
    this.audienceForm = this.fb.group({
      dossierId: [null, Validators.required],
      dateAudience: [null, Validators.required],
      dateProchaine: [null],
      tribunalType: [null],
      lieuTribunal: [''],
      commentaireDecision: [''],
      resultat: [null],
      avocatId: [null],
      huissierId: [null]
    });
  }

  populateForm(audience: Audience): void {
    this.audienceForm.patchValue({
      dossierId: audience.dossier?.id,
      dateAudience: audience.dateAudience,
      dateProchaine: audience.dateProchaine,
      tribunalType: audience.tribunalType,
      lieuTribunal: audience.lieuTribunal,
      commentaireDecision: audience.commentaireDecision,
      resultat: audience.resultat,
      avocatId: audience.avocat?.id,
      huissierId: audience.huissier?.id
    });
  }

  loadDossiers(): void {
    this.dossierService.getAllDossiers({}).subscribe({
      next: (response) => {
        this.dossiers = response.content || [];
      },
      error: (error) => {
        console.error('Erreur lors du chargement des dossiers:', error);
      }
    });
  }

  loadAvocats(): void {
    this.avocatService.getAllAvocats().subscribe({
      next: (avocats) => {
        this.avocats = avocats;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des avocats:', error);
      }
    });
  }

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

  onSubmit(): void {
    if (this.audienceForm.invalid) {
      this.audienceForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    const formValue = this.audienceForm.value;
    
    // ✅ CORRECTION: Créer un AudienceRequest avec les IDs (format recommandé)
    // Le backend accepte aussi le format objet (dossier: { id }), mais les IDs simples sont recommandés
    const audienceRequest: AudienceRequest = {
      dateAudience: formValue.dateAudience,
      dateProchaine: formValue.dateProchaine || undefined,
      tribunalType: formValue.tribunalType || undefined,
      lieuTribunal: formValue.lieuTribunal || undefined,
      commentaireDecision: formValue.commentaireDecision || undefined,
      resultat: formValue.resultat || undefined,
      
      // ✅ Utiliser les IDs directement (format recommandé par le backend)
      dossierId: formValue.dossierId, // Obligatoire
      avocatId: formValue.avocatId || undefined,
      huissierId: formValue.huissierId || undefined
    };

    // Vérifier que le dossier est fourni (obligatoire)
    if (!audienceRequest.dossierId) {
      this.snackBar.open('Le dossier est obligatoire', 'Fermer', {
        duration: 5000,
        panelClass: ['error-snackbar']
      });
      this.loading = false;
      return;
    }

    const request = this.isEditMode && this.data.audience?.id
      ? this.audienceService.updateAudience(this.data.audience.id, audienceRequest)
      : this.audienceService.createAudience(audienceRequest);

    request.subscribe({
      next: (audience) => {
        this.snackBar.open(
          this.isEditMode ? 'Audience modifiée avec succès' : 'Audience créée avec succès',
          'Fermer',
          {
            duration: 3000,
            panelClass: ['success-snackbar']
          }
        );
        this.dialogRef.close(audience);
      },
      error: (error) => {
        console.error('Erreur lors de la sauvegarde:', error);
        const errorMessage = error.message || 'Erreur lors de la sauvegarde';
        this.snackBar.open(errorMessage, 'Fermer', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
        this.loading = false;
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  getDossierDisplayName(dossier: any): string {
    return `${dossier.numeroDossier || dossier.id} - ${dossier.titre || 'Sans titre'}`;
  }

  getAvocatDisplayName(avocat: any): string {
    return `${avocat.prenom} ${avocat.nom}`;
  }

  getHuissierDisplayName(huissier: any): string {
    return `${huissier.prenom} ${huissier.nom}`;
  }
}
```

IMPORTANT :
- ✅ **TOUJOURS charger les données depuis les APIs backend** via les services dans `ngOnInit()`
- ❌ **NE JAMAIS utiliser de données mockées ou statiques pour les listes de dossiers, avocats, huissiers**
- Validation complète du formulaire
- Gérer les modes création et édition
- Les méthodes `loadDossiers()`, `loadAvocats()`, `loadHuissiers()` doivent TOUJOURS appeler les services backend
- Afficher des messages d'erreur/succès
```

---

## 📋 PROMPT 5 : Template HTML du Formulaire

**Prompt à copier dans Cursor AI :**

```
Créez le template HTML pour le formulaire d'audience :

Fichier : src/app/components/audience-form/audience-form.component.html

CODE EXEMPLE :

```html
<h2 mat-dialog-title>
  {{ isEditMode ? 'Modifier l\'audience' : 'Créer une nouvelle audience' }}
</h2>

<mat-dialog-content>
  <form [formGroup]="audienceForm" class="audience-form">
    <!-- Dossier (obligatoire) -->
    <mat-form-field appearance="outline" class="full-width">
      <mat-label>Dossier *</mat-label>
      <mat-select formControlName="dossierId" required>
        <mat-option *ngFor="let dossier of dossiers" [value]="dossier.id">
          {{ getDossierDisplayName(dossier) }}
        </mat-option>
      </mat-select>
      <mat-error *ngIf="audienceForm.get('dossierId')?.hasError('required')">
        Le dossier est obligatoire
      </mat-error>
    </mat-form-field>

    <!-- Date de l'audience (obligatoire) -->
    <mat-form-field appearance="outline" class="full-width">
      <mat-label>Date de l'audience *</mat-label>
      <input matInput [matDatepicker]="picker" formControlName="dateAudience" required>
      <mat-datepicker-toggle matSuffix [for]="picker"></mat-datepicker-toggle>
      <mat-datepicker #picker></mat-datepicker>
      <mat-error *ngIf="audienceForm.get('dateAudience')?.hasError('required')">
        La date de l'audience est obligatoire
      </mat-error>
    </mat-form-field>

    <!-- Date prochaine (si reportée) -->
    <mat-form-field appearance="outline" class="full-width">
      <mat-label>Date prochaine (si reportée)</mat-label>
      <input matInput [matDatepicker]="pickerNext" formControlName="dateProchaine">
      <mat-datepicker-toggle matSuffix [for]="pickerNext"></mat-datepicker-toggle>
      <mat-datepicker #pickerNext></mat-datepicker>
    </mat-form-field>

    <!-- Type de tribunal -->
    <mat-form-field appearance="outline" class="full-width">
      <mat-label>Type de tribunal</mat-label>
      <mat-select formControlName="tribunalType">
        <mat-option [value]="null">Aucun</mat-option>
        <mat-option *ngFor="let type of tribunalTypes" [value]="type">
          {{ getTribunalTypeLabel(type) }}
        </mat-option>
      </mat-select>
    </mat-form-field>

    <!-- Lieu du tribunal -->
    <mat-form-field appearance="outline" class="full-width">
      <mat-label>Lieu du tribunal</mat-label>
      <input matInput formControlName="lieuTribunal" placeholder="Ex: Tribunal de Tunis">
    </mat-form-field>

    <!-- Avocat -->
    <mat-form-field appearance="outline" class="full-width">
      <mat-label>Avocat</mat-label>
      <mat-select formControlName="avocatId">
        <mat-option [value]="null">Aucun</mat-option>
        <mat-option *ngFor="let avocat of avocats" [value]="avocat.id">
          {{ getAvocatDisplayName(avocat) }}
        </mat-option>
      </mat-select>
    </mat-form-field>

    <!-- Huissier -->
    <mat-form-field appearance="outline" class="full-width">
      <mat-label>Huissier</mat-label>
      <mat-select formControlName="huissierId">
        <mat-option [value]="null">Aucun</mat-option>
        <mat-option *ngFor="let huissier of huissiers" [value]="huissier.id">
          {{ getHuissierDisplayName(huissier) }}
        </mat-option>
      </mat-select>
    </mat-form-field>

    <!-- Résultat -->
    <mat-form-field appearance="outline" class="full-width">
      <mat-label>Résultat</mat-label>
      <mat-select formControlName="resultat">
        <mat-option [value]="null">Non défini</mat-option>
        <mat-option *ngFor="let result of decisionResults" [value]="result">
          {{ getDecisionResultLabel(result) }}
        </mat-option>
      </mat-select>
    </mat-form-field>

    <!-- Commentaire de décision -->
    <mat-form-field appearance="outline" class="full-width">
      <mat-label>Commentaire de décision</mat-label>
      <textarea matInput formControlName="commentaireDecision" rows="4" 
                placeholder="Détails de la décision..."></textarea>
    </mat-form-field>
  </form>
</mat-dialog-content>

<mat-dialog-actions align="end">
  <button mat-button (click)="onCancel()" [disabled]="loading">Annuler</button>
  <button mat-raised-button color="primary" (click)="onSubmit()" [disabled]="loading || audienceForm.invalid">
    <mat-spinner *ngIf="loading" diameter="20" class="button-spinner"></mat-spinner>
    <span *ngIf="!loading">{{ isEditMode ? 'Modifier' : 'Créer' }}</span>
    <span *ngIf="loading">Enregistrement...</span>
  </button>
</mat-dialog-actions>
```

IMPORTANT :
- Utiliser Material Design components
- Afficher les erreurs de validation
- Gérer les états de chargement
- Design responsive
```

---

## 📋 PROMPT 6 : Composant Calendrier des Audiences

**Prompt à copier dans Cursor AI :**

```
Créez un composant calendrier pour visualiser les audiences :

Fichier : src/app/components/audience-calendar/audience-calendar.component.ts

Fonctionnalités :
1. Calendrier mensuel avec les audiences marquées
2. Vue jour/semaine/mois
3. Clic sur une date pour voir les audiences
4. Couleurs différentes selon le type de tribunal ou résultat
5. Filtres par dossier, avocat, huissier

CODE EXEMPLE :

```typescript
import { Component, OnInit } from '@angular/core';
import { CalendarEvent, CalendarView } from 'angular-calendar';
import { startOfDay } from 'date-fns';
import { AudienceService } from '../../services/audience.service';
import { Audience, TribunalType } from '../../models/audience.interface';

@Component({
  selector: 'app-audience-calendar',
  templateUrl: './audience-calendar.component.html',
  styleUrls: ['./audience-calendar.component.css']
})
export class AudienceCalendarComponent implements OnInit {
  view: CalendarView = CalendarView.Month;
  viewDate: Date = new Date();
  events: CalendarEvent[] = [];
  
  audiences: Audience[] = [];
  loading = false;

  constructor(private audienceService: AudienceService) {}

  ngOnInit(): void {
    this.loadAudiences();
  }

  loadAudiences(): void {
    this.loading = true;
    // ✅ CHARGER LES DONNÉES RÉELLES depuis l'API backend
    this.audienceService.getAllAudiences().subscribe({
      next: (audiences) => {
        this.audiences = audiences; // ✅ Données réelles depuis le backend
        this.events = this.convertAudiencesToEvents(audiences);
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des audiences:', error);
        this.loading = false;
      }
    });
  }

  convertAudiencesToEvents(audiences: Audience[]): CalendarEvent[] {
    return audiences.map(audience => ({
      start: startOfDay(new Date(audience.dateAudience)),
      title: this.getEventTitle(audience),
      color: this.getEventColor(audience),
      meta: { audience }
    }));
  }

  getEventTitle(audience: Audience): string {
    const dossier = audience.dossier?.numeroDossier || audience.dossier?.id || 'N/A';
    const tribunal = audience.tribunalType ? this.getTribunalTypeLabel(audience.tribunalType) : 'Audience';
    return `${tribunal} - Dossier ${dossier}`;
  }

  getEventColor(audience: Audience): any {
    if (audience.resultat === 'POSITIVE') {
      return { primary: '#4caf50', secondary: '#c8e6c9' };
    } else if (audience.resultat === 'NEGATIVE') {
      return { primary: '#f44336', secondary: '#ffcdd2' };
    } else if (audience.tribunalType === TribunalType.TRIBUNAL_CASSATION) {
      return { primary: '#9c27b0', secondary: '#e1bee7' };
    } else if (audience.tribunalType === TribunalType.TRIBUNAL_APPEL) {
      return { primary: '#ff9800', secondary: '#ffe0b2' };
    } else {
      return { primary: '#2196f3', secondary: '#bbdefb' };
    }
  }

  onEventClick(event: CalendarEvent): void {
    const audience = event.meta?.audience;
    if (audience) {
      // Ouvrir le dialog de détails
    }
  }

  setView(view: CalendarView): void {
    this.view = view;
  }

  getTribunalTypeLabel(type: TribunalType): string {
    const labels: { [key: string]: string } = {
      'TRIBUNAL_PREMIERE_INSTANCE': 'TPI',
      'TRIBUNAL_APPEL': 'Appel',
      'TRIBUNAL_CASSATION': 'Cassation'
    };
    return labels[type] || type;
  }
}
```

IMPORTANT :
- ✅ **TOUJOURS charger les audiences depuis l'API backend** via `audienceService.getAllAudiences()`
- ❌ **NE JAMAIS utiliser de données mockées ou statiques**
- Utiliser angular-calendar ou une bibliothèque similaire
- Gérer les couleurs selon le contexte
- Permettre la navigation entre les mois
- La méthode `loadAudiences()` doit TOUJOURS appeler le service backend
```

---

## 📋 PROMPT 7 : Intégration dans la Page de Détails du Dossier

**Prompt à copier dans Cursor AI :**

```
Intégrez la gestion des audiences dans la page de détails du dossier :

Fichier : src/app/components/dossier-detail/dossier-detail.component.html

Ajoutez un onglet "Audiences" qui affiche :
1. Liste des audiences du dossier
2. Bouton pour créer une nouvelle audience
3. Prochaine audience (si disponible)
4. Statistiques (nombre d'audiences, résultats, etc.)

CODE EXEMPLE :

```html
<mat-tab-group>
  <mat-tab label="Informations générales">
    <!-- Contenu existant -->
  </mat-tab>
  
  <mat-tab label="Audiences">
    <div class="audiences-tab">
      <!-- Prochaine audience -->
      <mat-card *ngIf="nextAudience" class="next-audience-card">
        <mat-card-header>
          <mat-card-title>Prochaine audience</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <p><strong>Date:</strong> {{ nextAudience.dateAudience | date:'dd/MM/yyyy' }}</p>
          <p *ngIf="nextAudience.tribunalType">
            <strong>Type:</strong> {{ getTribunalTypeLabel(nextAudience.tribunalType) }}
          </p>
          <p *ngIf="nextAudience.lieuTribunal">
            <strong>Lieu:</strong> {{ nextAudience.lieuTribunal }}
          </p>
        </mat-card-content>
      </mat-card>

      <!-- Liste des audiences -->
      <div class="audiences-actions">
        <button mat-raised-button color="primary" (click)="openAudienceDialog()">
          <mat-icon>add</mat-icon>
          Nouvelle audience
        </button>
      </div>

      <mat-list>
        <mat-list-item *ngFor="let audience of audiences">
          <mat-icon matListIcon>event</mat-icon>
          <div matLine>
            <span class="audience-date">{{ audience.dateAudience | date:'dd/MM/yyyy' }}</span>
            <span class="audience-type" *ngIf="audience.tribunalType">
              {{ getTribunalTypeLabel(audience.tribunalType) }}
            </span>
          </div>
          <div matLine class="audience-meta">
            <span *ngIf="audience.lieuTribunal">{{ audience.lieuTribunal }}</span>
            <span *ngIf="audience.resultat" class="result-badge" [ngClass]="getResultClass(audience.resultat)">
              {{ getDecisionResultLabel(audience.resultat) }}
            </span>
          </div>
          <button mat-icon-button (click)="editAudience(audience)">
            <mat-icon>edit</mat-icon>
          </button>
          <button mat-icon-button (click)="deleteAudience(audience)">
            <mat-icon>delete</mat-icon>
          </button>
        </mat-list-item>
      </mat-list>

      <div *ngIf="audiences.length === 0" class="no-audiences">
        <p>Aucune audience enregistrée pour ce dossier.</p>
      </div>
    </div>
  </mat-tab>
</mat-tab-group>
```

```typescript
// Dans dossier-detail.component.ts
loadAudiences(): void {
  if (this.dossier?.id) {
    this.audienceService.getAudiencesByDossier(this.dossier.id).subscribe({
      next: (audiences) => {
        this.audiences = audiences;
        this.loadNextAudience();
      },
      error: (error) => {
        console.error('Erreur lors du chargement des audiences:', error);
      }
    });
  }
}

loadNextAudience(): void {
  if (this.dossier?.id) {
    this.audienceService.getNextAudienceByDossier(this.dossier.id).subscribe({
      next: (audiences) => {
        this.nextAudience = audiences.length > 0 ? audiences[0] : null;
      },
      error: (error) => {
        console.error('Erreur lors du chargement de la prochaine audience:', error);
      }
    });
  }
}

openAudienceDialog(): void {
  const dialogRef = this.dialog.open(AudienceFormComponent, {
    width: '600px',
    data: { dossierId: this.dossier?.id }
  });

  dialogRef.afterClosed().subscribe(result => {
    if (result) {
      this.loadAudiences();
    }
  });
}
```

IMPORTANT :
- ✅ **TOUJOURS charger les audiences depuis l'API backend** via `audienceService.getAudiencesByDossier()`
- ❌ **NE JAMAIS utiliser de données mockées ou statiques**
- Charger les audiences au chargement du dossier via les services backend
- Permettre la création d'audience directement depuis le dossier
- Afficher la prochaine audience en évidence
- Les méthodes `loadAudiences()` et `loadNextAudience()` doivent TOUJOURS appeler les services backend
```

---

## ✅ Checklist de Vérification Frontend

- [ ] **Les interfaces TypeScript sont créées**
- [ ] **Le service AudienceService est complet et appelle les APIs backend réelles**
- [ ] **Toutes les données sont chargées depuis les APIs backend (pas de données mockées)**
- [ ] **Les méthodes loadDossiers(), loadAvocats(), loadHuissiers(), loadAudiences() appellent les services backend**
- [ ] Le composant de liste fonctionne avec filtres et charge les données réelles
- [ ] Le formulaire de création/édition fonctionne et charge les listes depuis les APIs
- [ ] Le calendrier affiche les audiences correctement depuis l'API backend
- [ ] L'intégration dans la page de détails du dossier fonctionne avec données réelles
- [ ] Les messages de succès/erreur s'affichent
- [ ] Les états de chargement sont gérés
- [ ] La validation des formulaires fonctionne
- [ ] Les enums sont correctement mappés
- [ ] Les dates sont formatées correctement
- [ ] Les relations (dossier, avocat, huissier) sont chargées depuis les APIs backend

---

## 📋 PROMPT 8 : Utilisation Complète de TOUTES les APIs

**Prompt à copier dans Cursor AI :**

```
Assurez-vous que TOUTES les APIs d'audience sont utilisées dans le frontend :

LISTE COMPLÈTE DES APIs BACKEND À CONSOMMER :

1. ✅ CRUD Operations (5 APIs) :
   - POST /api/audiences → createAudience() ✅ Utilisé dans AudienceFormComponent
   - GET /api/audiences/{id} → getAudienceById() ✅ Utilisé pour voir les détails
   - GET /api/audiences → getAllAudiences() ✅ Utilisé dans AudienceListComponent
   - PUT /api/audiences/{id} → updateAudience() ✅ Utilisé dans AudienceFormComponent
   - DELETE /api/audiences/{id} → deleteAudience() ✅ Utilisé dans AudienceListComponent

2. ✅ Search Operations (8 APIs) :
   - GET /api/audiences/dossier/{dossierId} → getAudiencesByDossier() ✅ Utilisé dans DossierDetailComponent
   - GET /api/audiences/date/{date} → getAudiencesByDate() ⚠️ À IMPLÉMENTER
   - GET /api/audiences/date-range → getAudiencesByDateRange() ⚠️ À IMPLÉMENTER
   - GET /api/audiences/tribunal-type/{tribunalType} → getAudiencesByTribunalType() ⚠️ À IMPLÉMENTER
   - GET /api/audiences/result/{result} → getAudiencesByResult() ⚠️ À IMPLÉMENTER
   - GET /api/audiences/avocat/{avocatId} → getAudiencesByAvocat() ⚠️ À IMPLÉMENTER
   - GET /api/audiences/huissier/{huissierId} → getAudiencesByHuissier() ⚠️ À IMPLÉMENTER
   - GET /api/audiences/location?location=X → getAudiencesByLocation() ⚠️ À IMPLÉMENTER

3. ✅ Special Operations (3 APIs) :
   - GET /api/audiences/upcoming → getUpcomingAudiences() ✅ Utilisé dans AudienceListComponent
   - GET /api/audiences/past → getPastAudiences() ✅ Utilisé dans AudienceListComponent
   - GET /api/audiences/dossier/{dossierId}/next → getNextAudienceByDossier() ✅ Utilisé dans DossierDetailComponent

EXEMPLES D'UTILISATION POUR CHAQUE API :

```typescript
// ==================== 1. Recherche par Date ====================
// Dans un composant de recherche ou filtre avancé
loadAudiencesByDate(date: Date): void {
  const dateStr = date.toISOString().split('T')[0]; // Format YYYY-MM-DD
  this.audienceService.getAudiencesByDate(dateStr).subscribe({
    next: (audiences) => {
      this.dataSource.data = audiences;
    },
    error: (error) => {
      console.error('Erreur:', error);
    }
  });
}

// ==================== 2. Recherche par Plage de Dates ====================
// Dans un composant de filtre avec date de début et fin
loadAudiencesByDateRange(startDate: Date, endDate: Date): void {
  const startStr = startDate.toISOString().split('T')[0];
  const endStr = endDate.toISOString().split('T')[0];
  
  this.audienceService.getAudiencesByDateRange(startStr, endStr).subscribe({
    next: (audiences) => {
      this.dataSource.data = audiences;
    },
    error: (error) => {
      console.error('Erreur:', error);
    }
  });
}

// ==================== 3. Recherche par Type de Tribunal ====================
// Dans un filtre ou une page dédiée aux tribunaux
loadAudiencesByTribunalType(tribunalType: TribunalType): void {
  this.audienceService.getAudiencesByTribunalType(tribunalType).subscribe({
    next: (audiences) => {
      this.dataSource.data = audiences;
    },
    error: (error) => {
      console.error('Erreur:', error);
    }
  });
}

// ==================== 4. Recherche par Résultat ====================
// Dans un composant de statistiques ou filtres
loadAudiencesByResult(resultat: DecisionResult): void {
  this.audienceService.getAudiencesByResult(resultat).subscribe({
    next: (audiences) => {
      this.dataSource.data = audiences;
    },
    error: (error) => {
      console.error('Erreur:', error);
    }
  });
}

// ==================== 5. Recherche par Avocat ====================
// Dans la page de détails d'un avocat
loadAudiencesByAvocat(avocatId: number): void {
  this.audienceService.getAudiencesByAvocat(avocatId).subscribe({
    next: (audiences) => {
      this.audiences = audiences;
    },
    error: (error) => {
      console.error('Erreur:', error);
    }
  });
}

// ==================== 6. Recherche par Huissier ====================
// Dans la page de détails d'un huissier
loadAudiencesByHuissier(huissierId: number): void {
  this.audienceService.getAudiencesByHuissier(huissierId).subscribe({
    next: (audiences) => {
      this.audiences = audiences;
    },
    error: (error) => {
      console.error('Erreur:', error);
    }
  });
}

// ==================== 7. Recherche par Lieu ====================
// Dans un composant de recherche
loadAudiencesByLocation(location: string): void {
  if (location && location.trim()) {
    this.audienceService.getAudiencesByLocation(location.trim()).subscribe({
      next: (audiences) => {
        this.dataSource.data = audiences;
      },
      error: (error) => {
        console.error('Erreur:', error);
      }
    });
  }
}

// ==================== 8. Voir les Détails d'une Audience ====================
// Dans un composant de détails
loadAudienceDetails(audienceId: number): void {
  this.audienceService.getAudienceById(audienceId).subscribe({
    next: (audience) => {
      this.audience = audience;
    },
    error: (error) => {
      console.error('Erreur:', error);
      this.snackBar.open('Audience non trouvée', 'Fermer', { duration: 3000 });
    }
  });
}
```

AMÉLIORATION DU COMPOSANT DE LISTE POUR UTILISER TOUTES LES APIs :

```typescript
// Dans audience-list.component.ts
applyFilters(): void {
  const filters = this.filterForm.value;
  this.loading = true;
  
  // ✅ Utiliser les APIs backend au lieu de filtrer côté client
  let request: Observable<Audience[]>;
  
  // Priorité aux filtres spécifiques (utiliser les APIs dédiées)
  if (filters.dossierId) {
    request = this.audienceService.getAudiencesByDossier(filters.dossierId);
  } else if (filters.avocatId) {
    request = this.audienceService.getAudiencesByAvocat(filters.avocatId);
  } else if (filters.huissierId) {
    request = this.audienceService.getAudiencesByHuissier(filters.huissierId);
  } else if (filters.tribunalType) {
    request = this.audienceService.getAudiencesByTribunalType(filters.tribunalType);
  } else if (filters.resultat) {
    request = this.audienceService.getAudiencesByResult(filters.resultat);
  } else if (filters.startDate && filters.endDate) {
    const startStr = new Date(filters.startDate).toISOString().split('T')[0];
    const endStr = new Date(filters.endDate).toISOString().split('T')[0];
    request = this.audienceService.getAudiencesByDateRange(startStr, endStr);
  } else if (filters.location) {
    request = this.audienceService.getAudiencesByLocation(filters.location);
  } else {
    // Pas de filtre spécifique, charger selon l'onglet sélectionné
    switch (this.selectedTab) {
      case 1:
        request = this.audienceService.getUpcomingAudiences();
        break;
      case 2:
        request = this.audienceService.getPastAudiences();
        break;
      default:
        request = this.audienceService.getAllAudiences();
    }
  }
  
  request.subscribe({
    next: (audiences) => {
      this.dataSource.data = audiences;
      this.loading = false;
    },
    error: (error) => {
      console.error('Erreur lors du chargement:', error);
      this.snackBar.open('Erreur lors du chargement des audiences', 'Fermer', {
        duration: 5000,
        panelClass: ['error-snackbar']
      });
      this.loading = false;
    }
  });
}
```

IMPORTANT :
- ✅ TOUTES les 16 APIs doivent être utilisées dans le frontend
- ✅ Utiliser les APIs backend pour filtrer au lieu de filtrer côté client
- ✅ Créer des composants dédiés si nécessaire (ex: page de détails avocat/huissier)
- ✅ Intégrer les APIs de recherche dans les composants existants
```

---

## 📋 Messages d'Erreur Possibles

| Message Backend | Signification | Action Frontend |
|----------------|---------------|-----------------|
| "Audience not found with id: X" | L'audience n'existe pas | Afficher message d'erreur, rediriger si nécessaire |
| Erreur de validation | Données invalides | Afficher les erreurs de validation dans le formulaire |
| Erreur de création | Problème lors de la création | Afficher message d'erreur détaillé |

---

## 📊 Tableau de Vérification des APIs

| API Backend | Méthode Service | Utilisé dans | Status |
|------------|----------------|--------------|--------|
| POST /api/audiences | createAudience() | AudienceFormComponent | ✅ |
| GET /api/audiences/{id} | getAudienceById() | AudienceDetailComponent | ✅ |
| GET /api/audiences | getAllAudiences() | AudienceListComponent | ✅ |
| PUT /api/audiences/{id} | updateAudience() | AudienceFormComponent | ✅ |
| DELETE /api/audiences/{id} | deleteAudience() | AudienceListComponent | ✅ |
| GET /api/audiences/dossier/{id} | getAudiencesByDossier() | DossierDetailComponent | ✅ |
| GET /api/audiences/date/{date} | getAudiencesByDate() | Filtres avancés | ⚠️ À ajouter |
| GET /api/audiences/date-range | getAudiencesByDateRange() | Filtres avancés | ⚠️ À ajouter |
| GET /api/audiences/tribunal-type/{type} | getAudiencesByTribunalType() | Filtres avancés | ⚠️ À ajouter |
| GET /api/audiences/result/{result} | getAudiencesByResult() | Filtres avancés | ⚠️ À ajouter |
| GET /api/audiences/avocat/{id} | getAudiencesByAvocat() | AvocatDetailComponent | ⚠️ À ajouter |
| GET /api/audiences/huissier/{id} | getAudiencesByHuissier() | HuissierDetailComponent | ⚠️ À ajouter |
| GET /api/audiences/location | getAudiencesByLocation() | Recherche | ⚠️ À ajouter |
| GET /api/audiences/upcoming | getUpcomingAudiences() | AudienceListComponent | ✅ |
| GET /api/audiences/past | getPastAudiences() | AudienceListComponent | ✅ |
| GET /api/audiences/dossier/{id}/next | getNextAudienceByDossier() | DossierDetailComponent | ✅ |

**Légende :**
- ✅ = Déjà implémenté et utilisé
- ⚠️ = À ajouter dans les composants

---

**Ces prompts vous permettront d'implémenter complètement la gestion des audiences dans le frontend avec TOUTES les APIs backend ! 🚀**

