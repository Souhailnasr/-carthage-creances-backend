# 🚀 Guide d'Intégration Frontend Angular - Backend Spring Boot

## 📋 Table des matières
1. [Configuration de base](#configuration-de-base)
2. [Services Angular](#services-angular)
3. [Modèles TypeScript](#modèles-typescript)
4. [Intercepteurs JWT](#intercepteurs-jwt)
5. [Guards de sécurité](#guards-de-sécurité)
6. [Composants principaux](#composants-principaux)
7. [Endpoints API](#endpoints-api)

---

## 🔧 Configuration de base

### 1. Variables d'environnement (`src/environments/environment.ts`)

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8089/carthage-creance/api',
  authUrl: 'http://localhost:8089/carthage-creance/auth'
};
```

### 2. Configuration HTTP (`src/app/app.config.ts`)

```typescript
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { jwtInterceptor } from './interceptors/jwt.interceptor';
import { errorInterceptor } from './interceptors/error.interceptor';

export const appConfig = {
  providers: [
    provideHttpClient(
      withInterceptors([jwtInterceptor, errorInterceptor])
    )
  ]
};
```

---

## 🔐 Services Angular

### 1. Service d'authentification (`src/app/services/auth.service.ts`)

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  role: string;
}

export interface AuthResponse {
  token: string;
  errors?: string[];
}

export interface User {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  role: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadUserFromStorage();
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.authUrl}/authenticate`, credentials)
      .pipe(
        tap(response => {
          if (response.token) {
            localStorage.setItem('token', response.token);
            this.loadUserFromToken();
          }
        })
      );
  }

  register(userData: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/users`, userData)
      .pipe(
        tap(response => {
          if (response.token) {
            localStorage.setItem('token', response.token);
            this.loadUserFromToken();
          }
        })
      );
  }

  getUserByEmail(email: string): Observable<User> {
    return this.http.get<User>(`${environment.apiUrl}/users/by-email/${email}`);
  }

  logout(): void {
    localStorage.removeItem('token');
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  private loadUserFromStorage(): void {
    const token = this.getToken();
    if (token) {
      this.loadUserFromToken();
    }
  }

  private loadUserFromToken(): void {
    const token = this.getToken();
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        this.getUserByEmail(payload.sub).subscribe({
          next: (user) => this.currentUserSubject.next(user),
          error: () => this.logout()
        });
      } catch (error) {
        this.logout();
      }
    }
  }
}
```

### 2. Service des dossiers (`src/app/services/dossier.service.ts`)

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Dossier {
  id: number;
  titre: string;
  numeroDossier: string;
  description: string;
  montantCreance: number;
  statut: 'EN_ATTENTE_VALIDATION' | 'VALIDE' | 'REJETE' | 'EN_COURS' | 'CLOTURE';
  dossierStatus: 'ENCOURSDETRAITEMENT' | 'CLOTURE';
  dateCreation: string;
  dateValidation?: string;
  dateCloture?: string;
  agentCreateur: User;
  agentResponsable?: User;
  creancier: Creancier;
  debiteur: Debiteur;
  avocat?: Avocat;
  huissier?: Huissier;
  urgence: boolean;
  valide: boolean;
}

export interface Creancier {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  adresse: string;
  codeCreancier: string;
  type: string;
}

export interface Debiteur {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  adresseElue: string;
  codeCreance: string;
  type: string;
}

export interface Avocat {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  adresse: string;
  specialite: string;
}

export interface Huissier {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  adresse: string;
  specialite: string;
}

export interface User {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  role: string;
}

export interface DossierFilters {
  role?: string;
  userId?: number;
  page?: number;
  size?: number;
  search?: string;
}

export interface DossierResponse {
  content: Dossier[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  size: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
}

@Injectable({
  providedIn: 'root'
})
export class DossierService {
  constructor(private http: HttpClient) {}

  getAllDossiers(filters: DossierFilters = {}): Observable<DossierResponse> {
    let params = new HttpParams();
    
    if (filters.role) params = params.set('role', filters.role);
    if (filters.userId) params = params.set('userId', filters.userId.toString());
    if (filters.page !== undefined) params = params.set('page', filters.page.toString());
    if (filters.size) params = params.set('size', filters.size.toString());
    if (filters.search) params = params.set('search', filters.search);

    return this.http.get<DossierResponse>(`${environment.apiUrl}/dossiers`, { params });
  }

  getDossierById(id: number): Observable<Dossier> {
    return this.http.get<Dossier>(`${environment.apiUrl}/dossiers/${id}`);
  }

  createDossier(dossierData: FormData): Observable<Dossier> {
    return this.http.post<Dossier>(`${environment.apiUrl}/dossiers`, dossierData);
  }

  updateDossier(id: number, dossierData: FormData): Observable<Dossier> {
    return this.http.put<Dossier>(`${environment.apiUrl}/dossiers/${id}`, dossierData);
  }

  deleteDossier(id: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/dossiers/${id}`);
  }

  getDossierStats(agentId: number): Observable<any> {
    return this.http.get<any>(`${environment.apiUrl}/dossiers/stats/${agentId}`);
  }
}
```

### 3. Service de validation (`src/app/services/validation.service.ts`)

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ValidationDossier {
  id: number;
  dossierId: number;
  agentCreateurId: number;
  chefValidateurId?: number;
  statut: 'EN_ATTENTE' | 'VALIDE' | 'REJETE';
  commentaire?: string;
  dateCreation: string;
  dateValidation?: string;
}

export interface ValidationRequest {
  dossierId: number;
  commentaire?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ValidationService {
  constructor(private http: HttpClient) {}

  getAllValidations(): Observable<ValidationDossier[]> {
    return this.http.get<ValidationDossier[]>(`${environment.apiUrl}/validation/dossiers`);
  }

  getValidationById(id: number): Observable<ValidationDossier> {
    return this.http.get<ValidationDossier>(`${environment.apiUrl}/validation/dossiers/${id}`);
  }

  createValidation(validation: ValidationRequest): Observable<ValidationDossier> {
    return this.http.post<ValidationDossier>(`${environment.apiUrl}/validation/dossiers`, validation);
  }

  validerDossier(id: number, commentaire?: string): Observable<ValidationDossier> {
    return this.http.post<ValidationDossier>(`${environment.apiUrl}/validation/dossiers/${id}/valider`, { commentaire });
  }

  rejeterDossier(id: number, commentaire?: string): Observable<ValidationDossier> {
    return this.http.post<ValidationDossier>(`${environment.apiUrl}/validation/dossiers/${id}/rejeter`, { commentaire });
  }

  cloturerDossier(id: number, commentaire?: string): Observable<ValidationDossier> {
    return this.http.post<ValidationDossier>(`${environment.apiUrl}/validation/dossiers/${id}/cloturer`, { commentaire });
  }
}
```

---

## 🔒 Intercepteurs

### 1. Intercepteur JWT (`src/app/interceptors/jwt.interceptor.ts`)

```typescript
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('token');
  
  if (token) {
    const authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(authReq);
  }
  
  return next(req);
};
```

### 2. Intercepteur d'erreurs (`src/app/interceptors/error.interceptor.ts`)

```typescript
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  
  return next(req).pipe(
    catchError(error => {
      if (error.status === 401) {
        localStorage.removeItem('token');
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
```

---

## 🛡️ Guards de sécurité

### 1. Guard d'authentification (`src/app/guards/auth.guard.ts`)

```typescript
import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const token = localStorage.getItem('token');
  
  if (!token) {
    router.navigate(['/login']);
    return false;
  }
  
  return true;
};
```

### 2. Guard de rôle (`src/app/guards/role.guard.ts`)

```typescript
import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const roleGuard = (allowedRoles: string[]): CanActivateFn => {
  return (route, state) => {
    const router = inject(Router);
    const authService = inject(AuthService);
    
    let hasRole = false;
    authService.currentUser$.subscribe(user => {
      if (user && allowedRoles.includes(user.role)) {
        hasRole = true;
      }
    });
    
    if (!hasRole) {
      router.navigate(['/unauthorized']);
      return false;
    }
    
    return true;
  };
};
```

---

## 📱 Composants principaux

### 1. Composant de connexion (`src/app/components/login/login.component.ts`)

```typescript
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService, LoginRequest } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="login-container">
      <form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
        <h2>Connexion</h2>
        
        <div class="form-group">
          <label for="email">Email</label>
          <input type="email" id="email" formControlName="email" required>
          <div *ngIf="loginForm.get('email')?.invalid && loginForm.get('email')?.touched" class="error">
            Email requis
          </div>
        </div>
        
        <div class="form-group">
          <label for="password">Mot de passe</label>
          <input type="password" id="password" formControlName="password" required>
          <div *ngIf="loginForm.get('password')?.invalid && loginForm.get('password')?.touched" class="error">
            Mot de passe requis
          </div>
        </div>
        
        <button type="submit" [disabled]="loginForm.invalid || loading">
          {{ loading ? 'Connexion...' : 'Se connecter' }}
        </button>
        
        <div *ngIf="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>
      </form>
    </div>
  `,
  styles: [`
    .login-container {
      max-width: 400px;
      margin: 50px auto;
      padding: 20px;
      border: 1px solid #ddd;
      border-radius: 8px;
    }
    .form-group {
      margin-bottom: 15px;
    }
    .error {
      color: red;
      font-size: 12px;
    }
    .error-message {
      color: red;
      margin-top: 10px;
    }
  `]
})
export class LoginComponent {
  loginForm: FormGroup;
  loading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  onSubmit() {
    if (this.loginForm.valid) {
      this.loading = true;
      this.errorMessage = '';
      
      const credentials: LoginRequest = this.loginForm.value;
      
      this.authService.login(credentials).subscribe({
        next: () => {
          this.router.navigate(['/dashboard']);
        },
        error: (error) => {
          this.errorMessage = 'Erreur de connexion';
          this.loading = false;
        }
      });
    }
  }
}
```

### 2. Composant liste des dossiers (`src/app/components/dossier-list/dossier-list.component.ts`)

```typescript
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DossierService, Dossier, DossierFilters } from '../../services/dossier.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dossier-list',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="dossier-list">
      <div class="filters">
        <input 
          type="text" 
          placeholder="Rechercher..." 
          [(ngModel)]="searchTerm"
          (input)="onSearch()">
        
        <select [(ngModel)]="selectedRole" (change)="onFilterChange()">
          <option value="">Tous les rôles</option>
          <option value="AGENT">Agent</option>
          <option value="CHEF">Chef</option>
        </select>
      </div>
      
      <div class="dossiers">
        <div *ngFor="let dossier of dossiers" class="dossier-card">
          <h3>{{ dossier.titre }}</h3>
          <p><strong>Numéro:</strong> {{ dossier.numeroDossier }}</p>
          <p><strong>Statut:</strong> {{ dossier.statut }}</p>
          <p><strong>Montant:</strong> {{ dossier.montantCreance | currency }}</p>
          <p><strong>Créé le:</strong> {{ dossier.dateCreation | date }}</p>
          
          <div class="actions">
            <button (click)="viewDossier(dossier.id)">Voir</button>
            <button *ngIf="canEdit(dossier)" (click)="editDossier(dossier.id)">Modifier</button>
            <button *ngIf="canValidate(dossier)" (click)="validateDossier(dossier.id)">Valider</button>
          </div>
        </div>
      </div>
      
      <div class="pagination">
        <button (click)="previousPage()" [disabled]="currentPage === 0">Précédent</button>
        <span>Page {{ currentPage + 1 }} sur {{ totalPages }}</span>
        <button (click)="nextPage()" [disabled]="currentPage >= totalPages - 1">Suivant</button>
      </div>
    </div>
  `,
  styles: [`
    .dossier-list {
      padding: 20px;
    }
    .filters {
      margin-bottom: 20px;
      display: flex;
      gap: 10px;
    }
    .dossier-card {
      border: 1px solid #ddd;
      padding: 15px;
      margin-bottom: 10px;
      border-radius: 5px;
    }
    .actions {
      margin-top: 10px;
    }
    .actions button {
      margin-right: 5px;
    }
  `]
})
export class DossierListComponent implements OnInit {
  dossiers: Dossier[] = [];
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  searchTerm = '';
  selectedRole = '';
  currentUser: any;

  constructor(
    private dossierService: DossierService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      this.loadDossiers();
    });
  }

  loadDossiers() {
    const filters: DossierFilters = {
      page: this.currentPage,
      size: this.pageSize,
      search: this.searchTerm || undefined,
      role: this.selectedRole || undefined,
      userId: this.currentUser?.id
    };

    this.dossierService.getAllDossiers(filters).subscribe({
      next: (response) => {
        this.dossiers = response.content;
        this.totalPages = response.totalPages;
      },
      error: (error) => console.error('Erreur lors du chargement des dossiers:', error)
    });
  }

  onSearch() {
    this.currentPage = 0;
    this.loadDossiers();
  }

  onFilterChange() {
    this.currentPage = 0;
    this.loadDossiers();
  }

  previousPage() {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadDossiers();
    }
  }

  nextPage() {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadDossiers();
    }
  }

  viewDossier(id: number) {
    // Navigation vers la vue détaillée
  }

  editDossier(id: number) {
    // Navigation vers l'édition
  }

  validateDossier(id: number) {
    // Logique de validation
  }

  canEdit(dossier: Dossier): boolean {
    return this.currentUser?.id === dossier.agentCreateur.id;
  }

  canValidate(dossier: Dossier): boolean {
    return this.currentUser?.role?.includes('CHEF') && dossier.statut === 'EN_ATTENTE_VALIDATION';
  }
}
```

---

## 🛠️ Configuration des routes (`src/app/app.routes.ts`)

```typescript
import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { roleGuard } from './guards/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'login', loadComponent: () => import('./components/login/login.component').then(m => m.LoginComponent) },
  { 
    path: 'dashboard', 
    loadComponent: () => import('./components/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [authGuard]
  },
  { 
    path: 'dossiers', 
    loadComponent: () => import('./components/dossier-list/dossier-list.component').then(m => m.DossierListComponent),
    canActivate: [authGuard]
  },
  { 
    path: 'dossiers/create', 
    loadComponent: () => import('./components/dossier-form/dossier-form.component').then(m => m.DossierFormComponent),
    canActivate: [authGuard]
  },
  { 
    path: 'validation', 
    loadComponent: () => import('./components/validation/validation.component').then(m => m.ValidationComponent),
    canActivate: [authGuard, roleGuard(['CHEF_DEPARTEMENT_DOSSIER'])]
  }
];
```

---

## 📊 Endpoints API disponibles

### Authentification
- `POST /auth/authenticate` - Connexion
- `POST /auth/register` - Inscription

### Utilisateurs
- `GET /api/users` - Liste des utilisateurs
- `POST /api/users` - Créer un utilisateur
- `GET /api/users/by-email/{email}` - Utilisateur par email

### Dossiers
- `GET /api/dossiers` - Liste des dossiers (avec pagination)
- `GET /api/dossiers/{id}` - Dossier par ID
- `POST /api/dossiers` - Créer un dossier
- `PUT /api/dossiers/{id}` - Modifier un dossier
- `DELETE /api/dossiers/{id}` - Supprimer un dossier
- `GET /api/dossiers/stats/{agentId}` - Statistiques d'un agent

### Validation
- `GET /api/validation/dossiers` - Liste des validations
- `POST /api/validation/dossiers` - Créer une validation
- `POST /api/validation/dossiers/{id}/valider` - Valider un dossier
- `POST /api/validation/dossiers/{id}/rejeter` - Rejeter un dossier
- `POST /api/validation/dossiers/{id}/cloturer` - Clôturer un dossier

---

## 🚀 Instructions de déploiement

1. **Exécuter le script SQL** pour corriger la base de données :
   ```sql
   -- Exécuter fix_database_enums.sql dans votre base de données MySQL
   ```

2. **Installer les dépendances Angular** :
   ```bash
   npm install @angular/common @angular/forms @angular/router
   ```

3. **Configurer CORS** (si nécessaire) dans le backend Spring Boot

4. **Tester les endpoints** avec Postman ou curl

5. **Démarrer l'application** :
   ```bash
   ng serve
   ```

---

## 🔧 Dépannage

### Erreurs communes :
1. **CORS** : Configurer les headers CORS dans Spring Boot
2. **JWT** : Vérifier que le token est correctement envoyé
3. **Enums** : S'assurer que la base de données correspond aux enums Java
4. **Permissions** : Vérifier les rôles utilisateur

### Logs utiles :
- Backend : Vérifier les logs Spring Boot
- Frontend : Ouvrir la console du navigateur
- Réseau : Vérifier les requêtes HTTP dans les DevTools

---

**🎉 Votre frontend Angular est maintenant prêt à consommer l'API Spring Boot !**




