# 🔧 Correction Frontend : Logout avec JWT

Si l'endpoint de test fonctionne mais que `derniere_deconnexion` reste NULL lors du logout normal, le problème vient du frontend.

---

## ✅ Vérification Rapide

### 1. Vérifier dans la Console du Navigateur (F12)

**Ouvrez F12 → Network → Cliquez sur "Déconnexion"**

**Vérifiez :**
- ✅ Une requête `POST /auth/logout` apparaît
- ✅ Status: `200 OK`
- ✅ Request Headers contient `Authorization: Bearer ...`

**Si la requête n'apparaît PAS :**
→ Le frontend n'appelle pas l'endpoint (voir section "Correction du Service")

**Si la requête apparaît mais SANS le header Authorization :**
→ L'interceptor JWT ne fonctionne pas ou n'est pas appliqué (voir section "Correction de l'Interceptor")

---

## 🔍 Étape 1 : Localiser le Service d'Authentification

**Cherchez ces fichiers dans votre projet frontend :**
- `src/app/services/auth.service.ts`
- `src/app/services/authentication.service.ts`
- `src/app/services/user.service.ts`
- `src/app/core/services/auth.service.ts`

**Ou cherchez la méthode `logout()` dans tout le projet :**
```bash
# Dans le terminal du projet frontend
grep -r "logout" src/app/services/
```

---

## 🔧 Étape 2 : Vérifier le Code du Service

### Code Actuel (à Vérifier)

Ouvrez le service d'authentification et cherchez la méthode `logout()`.

**❌ Code INCORRECT (ne fonctionne pas) :**
```typescript
logout() {
  // Supprime juste le token localement, n'appelle pas le backend
  localStorage.removeItem('token');
  this.router.navigate(['/login']);
}
```

**✅ Code CORRECT (à utiliser) :**
```typescript
logout(): Observable<any> {
  // Récupérer le token
  const token = this.getToken();
  
  if (!token) {
    // Pas de token, juste nettoyer localement
    this.clearToken();
    this.router.navigate(['/login']);
    return of({ message: 'No token to logout' });
  }

  // Si vous utilisez un interceptor JWT, le header sera ajouté automatiquement
  // Sinon, ajoutez-le manuellement :
  const headers = new HttpHeaders({
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  });

  // Appeler l'endpoint de logout
  return this.http.post(`${this.apiUrl}/auth/logout`, {}, { headers }).pipe(
    tap(() => {
      console.log('✅ Logout réussi côté backend');
    }),
    catchError((error) => {
      console.error('❌ Erreur lors du logout:', error);
      // Même en cas d'erreur, supprimer le token localement
      return throwError(() => error);
    }),
    finalize(() => {
      // Toujours supprimer le token et rediriger, même en cas d'erreur
      this.clearToken();
      this.router.navigate(['/login']);
    })
  );
}
```

---

## 🔧 Étape 3 : Vérifier l'Interceptor JWT

**Si vous utilisez un interceptor JWT, vérifiez qu'il ajoute le header à TOUTES les requêtes, y compris `/auth/logout`.**

### Localiser l'Interceptor

**Cherchez :**
- `src/app/interceptors/jwt.interceptor.ts`
- `src/app/core/interceptors/auth.interceptor.ts`
- `src/app/interceptors/token.interceptor.ts`

### Code de l'Interceptor (à Vérifier)

**✅ Code CORRECT :**
```typescript
import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor
} from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    // Récupérer le token depuis localStorage
    const token = localStorage.getItem('token') || 
                  localStorage.getItem('auth_token') || 
                  localStorage.getItem('jwt_token');

    // Si le token existe, ajouter le header Authorization
    if (token) {
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(request);
  }
}
```

**❌ Code INCORRECT (exclut /auth/logout) :**
```typescript
intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
  // ❌ MAUVAIS - exclut /auth/logout
  if (request.url.includes('/auth/')) {
    return next.handle(request); // Sans ajouter le header
  }
  
  const token = localStorage.getItem('token');
  if (token) {
    request = request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }
  return next.handle(request);
}
```

**Si votre interceptor exclut `/auth/logout`, modifiez-le pour inclure toutes les requêtes.**

---

## 🔧 Étape 4 : Vérifier l'Enregistrement de l'Interceptor

**Vérifiez que l'interceptor est bien enregistré dans `app.config.ts` ou `app.module.ts` :**

### Pour Angular 15+ (app.config.ts)

```typescript
import { ApplicationConfig, provideHttpClient, withInterceptors } from '@angular/common/http';
import { JwtInterceptor } from './interceptors/jwt.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(
      withInterceptors([JwtInterceptor])
    ),
    // ... autres providers
  ]
};
```

### Pour Angular < 15 (app.module.ts)

```typescript
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { JwtInterceptor } from './interceptors/jwt.interceptor';

@NgModule({
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: JwtInterceptor,
      multi: true
    }
  ]
})
```

---

## 🔧 Étape 5 : Vérifier le Composant qui Appelle le Logout

**Cherchez le composant qui contient le bouton "Déconnexion" :**
- `header.component.ts`
- `navbar.component.ts`
- `sidebar.component.ts`
- `profile.component.ts`

### Code du Composant (à Vérifier)

**✅ Code CORRECT :**
```typescript
import { Component } from '@angular/core';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-header',
  template: `
    <button (click)="onLogout()">Déconnexion</button>
  `
})
export class HeaderComponent {
  constructor(private authService: AuthService) {}

  onLogout(): void {
    console.log('🔄 Début du processus de logout');
    
    this.authService.logout().subscribe({
      next: (response) => {
        console.log('✅ Logout réussi:', response);
        // La redirection est déjà gérée dans le service
      },
      error: (error) => {
        console.error('❌ Erreur logout:', error);
        // Même en cas d'erreur, on est déconnecté localement
        // La redirection est déjà gérée dans le service
      }
    });
  }
}
```

**❌ Code INCORRECT :**
```typescript
onLogout(): void {
  // ❌ Ne fait que supprimer le token localement
  localStorage.removeItem('token');
  this.router.navigate(['/login']);
}
```

---

## 📝 Code Complet de Référence

### 1. Service d'Authentification Complet (`auth.service.ts`)

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError, tap, finalize } from 'rxjs/operators';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8089/carthage-creance'; // Ajuster selon votre config
  private tokenKey = 'token'; // Ajuster selon votre clé

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  /**
   * Récupère le token depuis le localStorage
   */
  getToken(): string | null {
    return localStorage.getItem(this.tokenKey) ||
           localStorage.getItem('auth_token') ||
           localStorage.getItem('jwt_token');
  }

  /**
   * Supprime le token du localStorage
   */
  clearToken(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem('auth_token');
    localStorage.removeItem('jwt_token');
    sessionStorage.removeItem('token');
  }

  /**
   * Login
   */
  login(email: string, password: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/authenticate`, {
      email,
      password
    }).pipe(
      tap((response: any) => {
        if (response.token) {
          localStorage.setItem(this.tokenKey, response.token);
        }
      })
    );
  }

  /**
   * Logout - IMPORTANT : Appelle le backend pour mettre à jour derniere_deconnexion
   */
  logout(): Observable<any> {
    const token = this.getToken();
    
    if (!token) {
      console.warn('⚠️ Aucun token trouvé, logout local uniquement');
      this.clearToken();
      this.router.navigate(['/login']);
      return of({ message: 'No token to logout' });
    }

    console.log('🔄 Appel logout avec token:', token.substring(0, 20) + '...');

    // Si vous utilisez un interceptor JWT, le header sera ajouté automatiquement
    // Sinon, décommentez ces lignes :
    /*
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
    return this.http.post(`${this.apiUrl}/auth/logout`, {}, { headers }).pipe(...)
    */

    // Avec interceptor JWT (recommandé)
    return this.http.post(`${this.apiUrl}/auth/logout`, {}).pipe(
      tap((response) => {
        console.log('✅ Logout réussi côté backend:', response);
      }),
      catchError((error) => {
        console.error('❌ Erreur lors du logout:', error);
        // Même en cas d'erreur, supprimer le token localement
        return throwError(() => error);
      }),
      finalize(() => {
        // Toujours supprimer le token et rediriger, même en cas d'erreur
        this.clearToken();
        this.router.navigate(['/login']);
      })
    );
  }

  /**
   * Vérifie si l'utilisateur est connecté
   */
  isAuthenticated(): boolean {
    return this.getToken() !== null;
  }
}
```

### 2. Interceptor JWT (`jwt.interceptor.ts`)

```typescript
import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor
} from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    // Récupérer le token depuis localStorage
    const token = localStorage.getItem('token') || 
                  localStorage.getItem('auth_token') || 
                  localStorage.getItem('jwt_token');

    // Si le token existe, ajouter le header Authorization à TOUTES les requêtes
    if (token) {
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(request);
  }
}
```

### 3. Enregistrement de l'Interceptor (`app.config.ts` ou `app.module.ts`)

**Angular 15+ :**
```typescript
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { jwtInterceptor } from './interceptors/jwt.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(
      withInterceptors([jwtInterceptor])
    )
  ]
};
```

**Angular < 15 :**
```typescript
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { JwtInterceptor } from './interceptors/jwt.interceptor';

@NgModule({
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: JwtInterceptor,
      multi: true
    }
  ]
})
```

---

## 🧪 Test après Correction

### 1. Redémarrer le Frontend

```bash
ng serve
```

### 2. Tester le Logout

1. Se connecter à l'application
2. Ouvrir F12 → Network
3. Cliquer sur "Déconnexion"
4. Vérifier :
   - ✅ Une requête `POST /auth/logout` apparaît
   - ✅ Status: `200 OK`
   - ✅ Request Headers contient `Authorization: Bearer ...`
   - ✅ Response: `{"message":"Logout successful"}`

### 3. Vérifier les Logs Backend

Vous devriez voir :
```
=== DÉBUT LOGOUT ===
Logout: Token JWT extrait (longueur: XXX)
Logout: Token trouvé, ID: XXX
Logout: Utilisateur trouvé - ID: XXX, Email: XXX
Logout: ✅ SUCCÈS - derniere_deconnexion correctement sauvegardée: 2025-11-25T...
```

### 4. Vérifier dans la Base de Données

```sql
SELECT id, email, derniere_connexion, derniere_deconnexion 
FROM utilisateur 
WHERE email = 'votre_email@example.com';
```

`derniere_deconnexion` devrait maintenant être remplie ! ✅

---

## 📋 Checklist de Vérification

- [ ] Le service `logout()` appelle `POST /auth/logout`
- [ ] Le header `Authorization: Bearer {token}` est inclus (via interceptor ou manuellement)
- [ ] L'interceptor JWT n'exclut PAS `/auth/logout`
- [ ] L'interceptor est bien enregistré dans `app.config.ts` ou `app.module.ts`
- [ ] Le composant appelle `authService.logout().subscribe(...)`
- [ ] Dans Network (F12), la requête POST /auth/logout apparaît avec le header Authorization
- [ ] Les logs backend montrent "=== DÉBUT LOGOUT ==="
- [ ] La base de données montre `derniere_deconnexion` remplie

---

## 🚨 Si ça ne fonctionne toujours pas

**Vérifiez dans la console du navigateur (F12 → Console) :**

1. Y a-t-il des erreurs JavaScript ?
2. Le message "🔄 Appel logout avec token: ..." apparaît-il ?
3. Le message "✅ Logout réussi côté backend" apparaît-il ?

**Vérifiez dans Network (F12 → Network) :**

1. La requête `POST /auth/logout` apparaît-elle ?
2. Le header `Authorization` est-il présent ?
3. Quel est le Status de la réponse ?

**Avec ces informations, on pourra identifier précisément le problème.**

