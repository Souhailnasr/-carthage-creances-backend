# 🔍 Prompt de Débogage : Logout avec JWT Auth Frontend

## Prompt pour Vérifier le Logout avec JWT dans le Frontend Angular

```
Tu utilises JWT auth dans le frontend. Vérifie que le logout fonctionne correctement avec JWT :

1. **Localiser le service/interceptor JWT** :
   - Chercher les fichiers : `jwt.interceptor.ts`, `auth.interceptor.ts`, `token.interceptor.ts`
   - Chercher les services : `jwt.service.ts`, `token.service.ts`, `auth.service.ts`
   - Vérifier comment le token JWT est stocké et récupéré

2. **Vérifier le stockage du token JWT** :
   - Le token doit être stocké après login (localStorage, sessionStorage, ou service)
   - Chercher où le token est sauvegardé : `localStorage.setItem('token', ...)` ou similaire
   - Vérifier la clé utilisée : 'token', 'auth_token', 'jwt_token', 'access_token', etc.

3. **Vérifier l'interceptor JWT** (si utilisé) :
   - L'interceptor doit ajouter automatiquement le header `Authorization: Bearer {token}`
   - Vérifier que l'interceptor inclut `/auth/logout` dans les URLs interceptées
   - Code attendu dans l'interceptor :
   ```typescript
   intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
     const token = this.getToken(); // Récupérer depuis localStorage/service
     if (token) {
       req = req.clone({
         setHeaders: {
           Authorization: `Bearer ${token}`
         }
       });
     }
     return next.handle(req);
   }
   ```

4. **Vérifier la méthode logout() dans le service** :
   - La méthode doit appeler `POST /auth/logout`
   - Si un interceptor JWT est utilisé, le header Authorization sera ajouté automatiquement
   - Sinon, il faut l'ajouter manuellement
   - Code attendu :
   ```typescript
   logout(): Observable<any> {
     // Si interceptor JWT existe, pas besoin d'ajouter le header manuellement
     return this.http.post(`${this.apiUrl}/auth/logout`, {})
       .pipe(
         finalize(() => {
           // Supprimer le token après logout
           this.clearToken();
           this.router.navigate(['/login']);
         })
       );
   }
   ```

5. **Vérifier que le token est supprimé après logout** :
   - Après le logout réussi, le token doit être supprimé du storage
   - Chercher : `localStorage.removeItem('token')` ou `this.tokenService.clearToken()`
   - Vérifier que l'utilisateur est redirigé vers la page de login

6. **Tester dans la console du navigateur (F12 → Network)** :
   - Cliquer sur "Déconnexion"
   - Vérifier qu'une requête `POST /auth/logout` apparaît
   - Vérifier dans "Request Headers" que `Authorization: Bearer {token}` est présent
   - Si le header n'est pas présent, l'interceptor JWT ne fonctionne pas ou n'est pas appliqué à cette requête

7. **Vérifier la configuration de l'interceptor** :
   - L'interceptor doit être enregistré dans `app.module.ts` ou `app.config.ts`
   - Code attendu :
   ```typescript
   providers: [
     {
       provide: HTTP_INTERCEPTORS,
       useClass: JwtInterceptor, // ou AuthInterceptor, TokenInterceptor
       multi: true
     }
   ]
   ```

8. **Vérifier les logs backend** :
   - Après logout, les logs doivent afficher :
     - "=== DÉBUT LOGOUT ==="
     - "Logout: Token JWT extrait (longueur: XXX)"
     - "Logout: Utilisateur trouvé"
     - "Logout: derniere_deconnexion mise à jour"

9. **Si l'interceptor JWT n'ajoute pas le header pour /auth/logout** :
   - Vérifier que l'interceptor ne filtre pas certaines URLs
   - Vérifier que `/auth/logout` n'est pas dans une liste d'exclusions
   - Code à éviter (qui exclurait logout) :
   ```typescript
   // ❌ MAUVAIS - exclut /auth/logout
   if (req.url.includes('/auth/')) {
     return next.handle(req); // Sans ajouter le header
   }
   ```

10. **Solution si le token n'est pas envoyé** :
    - Option 1 : Modifier l'interceptor pour inclure `/auth/logout`
    - Option 2 : Ajouter manuellement le header dans la méthode logout()
    ```typescript
    logout(): Observable<any> {
      const token = this.getToken(); // Récupérer depuis le service JWT
      const headers = new HttpHeaders({
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      });
      return this.http.post(`${this.apiUrl}/auth/logout`, {}, { headers });
    }
    ```

Corrige le code pour que le token JWT soit bien envoyé dans le header Authorization lors du logout.
```

---

## Prompt pour Vérifier l'Interceptor JWT

```
Vérifie l'interceptor JWT dans le frontend Angular :

1. **Localiser le fichier de l'interceptor** :
   - Chercher : `jwt.interceptor.ts`, `auth.interceptor.ts`, `token.interceptor.ts`
   - Ou dans : `src/app/interceptors/`, `src/app/core/interceptors/`

2. **Vérifier que l'interceptor ajoute le header Authorization** :
   - Le code doit ressembler à :
   ```typescript
   export class JwtInterceptor implements HttpInterceptor {
     intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
       const token = this.getToken(); // Depuis localStorage ou service
       
       if (token) {
         req = req.clone({
           setHeaders: {
             Authorization: `Bearer ${token}`
           }
         });
       }
       
       return next.handle(req);
     }
   }
   ```

3. **Vérifier que l'interceptor est enregistré** :
   - Dans `app.module.ts` ou `app.config.ts` :
   ```typescript
   providers: [
     {
       provide: HTTP_INTERCEPTORS,
       useClass: JwtInterceptor,
       multi: true
     }
   ]
   ```

4. **Vérifier qu'il n'y a pas d'exclusions pour /auth/logout** :
   - L'interceptor ne doit PAS exclure `/auth/logout`
   - Éviter les conditions comme :
   ```typescript
   // ❌ MAUVAIS
   if (req.url.includes('/auth/')) {
     return next.handle(req); // Sans header
   }
   ```

5. **Tester que l'interceptor fonctionne** :
   - Faire une requête quelconque (ex: GET /api/dossiers)
   - Vérifier dans Network que le header Authorization est présent
   - Si oui, l'interceptor fonctionne et devrait aussi fonctionner pour /auth/logout

6. **Si l'interceptor ne fonctionne pas pour /auth/logout** :
   - Vérifier s'il y a une condition qui exclut cette URL
   - Vérifier si l'interceptor est bien enregistré
   - Vérifier si le token est bien récupéré dans l'interceptor

Corrige l'interceptor si nécessaire pour qu'il ajoute le header Authorization à toutes les requêtes, y compris /auth/logout.
```

---

## Prompt pour Tester le Logout avec JWT

```
Teste le logout avec JWT dans la console du navigateur :

1. **Ouvrir la console (F12)**

2. **Vérifier le token JWT stocké** :
```javascript
// Essayer différentes clés possibles
const token1 = localStorage.getItem('token');
const token2 = localStorage.getItem('auth_token');
const token3 = localStorage.getItem('jwt_token');
const token4 = localStorage.getItem('access_token');
const token5 = sessionStorage.getItem('token');

console.log('Token dans localStorage.token:', token1);
console.log('Token dans localStorage.auth_token:', token2);
console.log('Token dans localStorage.jwt_token:', token3);
console.log('Token dans localStorage.access_token:', token4);
console.log('Token dans sessionStorage.token:', token5);

// Trouver lequel contient le token
const token = token1 || token2 || token3 || token4 || token5;
console.log('Token trouvé:', token ? token.substring(0, 30) + '...' : 'AUCUN');
```

3. **Tester l'appel logout avec le token** :
```javascript
if (token) {
  fetch('http://localhost:8089/carthage-creance/auth/logout', {
    method: 'POST',
    headers: {
      'Authorization': 'Bearer ' + token,
      'Content-Type': 'application/json'
    }
  })
  .then(response => {
    console.log('Status:', response.status);
    return response.json();
  })
  .then(data => {
    console.log('✅ Logout réussi:', data);
    // Supprimer le token
    localStorage.removeItem('token');
    localStorage.removeItem('auth_token');
    localStorage.removeItem('jwt_token');
  })
  .catch(error => {
    console.error('❌ Erreur:', error);
  });
} else {
  console.warn('⚠️ Aucun token JWT trouvé');
}
```

4. **Vérifier les logs backend** :
   - Les logs doivent afficher "=== DÉBUT LOGOUT ==="
   - Et "derniere_deconnexion mise à jour"

Si ça fonctionne dans la console mais pas dans l'application, le problème est dans le code frontend (service ou interceptor).
```

---

## Checklist Spécifique JWT

```
Vérifie ces points pour le logout avec JWT :

□ Le token JWT est stocké après login (localStorage/sessionStorage)
□ L'interceptor JWT ajoute automatiquement `Authorization: Bearer {token}`
□ L'interceptor est enregistré dans app.module.ts ou app.config.ts
□ L'interceptor n'exclut PAS /auth/logout
□ La méthode logout() appelle POST /auth/logout
□ Dans Network (F12), la requête POST /auth/logout a le header Authorization
□ Après logout, le token est supprimé du storage
□ Les logs backend montrent "=== DÉBUT LOGOUT ==="
□ La base de données montre derniere_deconnexion remplie

Si une case n'est pas cochée, c'est là que se trouve le problème.
```

---

## Code de Référence Complet avec JWT Interceptor

### 1. JWT Interceptor (`jwt.interceptor.ts`)

```typescript
import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  constructor(private router: Router) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    // Récupérer le token depuis localStorage
    const token = this.getToken();

    // Cloner la requête et ajouter le header Authorization si le token existe
    if (token) {
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          // Token expiré ou invalide
          this.clearToken();
          this.router.navigate(['/login']);
        }
        return throwError(() => error);
      })
    );
  }

  private getToken(): string | null {
    // Essayer différentes clés possibles
    return localStorage.getItem('token') ||
           localStorage.getItem('auth_token') ||
           localStorage.getItem('jwt_token') ||
           sessionStorage.getItem('token');
  }

  private clearToken(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('auth_token');
    localStorage.removeItem('jwt_token');
    sessionStorage.removeItem('token');
  }
}
```

### 2. Enregistrement de l'Interceptor (`app.config.ts` ou `app.module.ts`)

```typescript
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { JwtInterceptor } from './interceptors/jwt.interceptor';

// Dans app.config.ts (Angular 15+)
export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(
      withInterceptors([JwtInterceptor])
    ),
    // ... autres providers
  ]
};

// OU dans app.module.ts (Angular < 15)
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

### 3. Service d'Authentification avec Logout (`auth.service.ts`)

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { tap, finalize } from 'rxjs/operators';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8089/carthage-creance';
  private tokenKey = 'token'; // Ajuster selon votre clé

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  login(email: string, password: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/authenticate`, {
      email,
      password
    }).pipe(
      tap((response: any) => {
        // Sauvegarder le token après login
        if (response.token) {
          localStorage.setItem(this.tokenKey, response.token);
        }
      })
    );
  }

  logout(): Observable<any> {
    // L'interceptor JWT ajoutera automatiquement le header Authorization
    return this.http.post(`${this.apiUrl}/auth/logout`, {}).pipe(
      tap(() => {
        console.log('✅ Logout réussi côté backend');
      }),
      finalize(() => {
        // Toujours supprimer le token et rediriger, même en cas d'erreur
        this.clearToken();
        this.router.navigate(['/login']);
      })
    );
  }

  private clearToken(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem('auth_token');
    localStorage.removeItem('jwt_token');
    sessionStorage.removeItem('token');
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  isAuthenticated(): boolean {
    return this.getToken() !== null;
  }
}
```

### 4. Utilisation dans un Composant

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

---

## Points Importants pour JWT

1. **L'interceptor JWT doit ajouter le header à TOUTES les requêtes**, y compris `/auth/logout`
2. **Le token doit être récupéré depuis le même endroit** (localStorage/sessionStorage) dans l'interceptor et le service
3. **Après logout, le token doit être supprimé** pour éviter les requêtes avec un token invalide
4. **Si l'interceptor ne fonctionne pas pour `/auth/logout`**, ajouter manuellement le header dans la méthode `logout()`

Utilisez ces prompts pour identifier et corriger le problème de logout avec JWT.

