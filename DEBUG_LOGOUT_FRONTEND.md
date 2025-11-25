# 🔍 Guide de Débogage : Vérification du Logout Frontend

Ce document vous aide à vérifier si le frontend appelle correctement l'endpoint `/auth/logout` et envoie le header `Authorization`.

---

## 📋 Checklist de Vérification

### 1. Vérifier que le frontend appelle `/auth/logout`

#### Étape 1 : Ouvrir la Console du Navigateur
1. Ouvrez votre application frontend dans le navigateur
2. Appuyez sur `F12` pour ouvrir les outils de développement
3. Allez dans l'onglet **"Network"** (Réseau)

#### Étape 2 : Tester le Logout
1. Connectez-vous à l'application
2. Cliquez sur le bouton de **déconnexion/logout**
3. Dans l'onglet **Network**, cherchez une requête vers `/auth/logout`

#### Étape 3 : Vérifier la Requête
Si vous voyez une requête vers `/auth/logout`, vérifiez :

**✅ Requête Correcte :**
- **Méthode** : `POST`
- **URL** : `http://localhost:8089/carthage-creance/auth/logout`
- **Status** : `200 OK` ou `204 No Content`
- **Headers** : Contient `Authorization: Bearer {votre_token_jwt}`

**❌ Problèmes Possibles :**
- Pas de requête vers `/auth/logout` → Le frontend n'appelle pas l'endpoint
- Requête vers une autre URL (ex: `/logout`, `/api/logout`) → URL incorrecte
- Status `401 Unauthorized` → Token manquant ou invalide
- Status `404 Not Found` → Endpoint non trouvé

---

### 2. Vérifier le Header Authorization

#### Dans l'onglet Network :
1. Cliquez sur la requête `/auth/logout`
2. Allez dans l'onglet **"Headers"**
3. Cherchez la section **"Request Headers"**

#### Vérifications :

**✅ Header Correct :**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**❌ Problèmes Possibles :**
- Pas de header `Authorization` → Le frontend n'envoie pas le token
- Header `Authorization` avec valeur `null` ou vide
- Format incorrect (ex: `Token {token}` au lieu de `Bearer {token}`)

---

### 3. Vérifier le Code Frontend

#### Chercher le Service de Logout

**Fichiers à vérifier :**
- `src/app/services/auth.service.ts` (ou similaire)
- `src/app/services/authentication.service.ts`
- `src/app/services/user.service.ts`

#### Code Attendu :

```typescript
logout(): Observable<any> {
  const token = this.getToken(); // Récupérer le token depuis le localStorage/sessionStorage
  
  const headers = new HttpHeaders({
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  });
  
  return this.http.post(`${this.apiUrl}/auth/logout`, {}, { headers });
}
```

#### Vérifications dans le Code :

1. **Le service récupère-t-il le token ?**
   ```typescript
   // ✅ Correct
   const token = localStorage.getItem('token');
   // ou
   const token = this.authService.getToken();
   ```

2. **Le header Authorization est-il ajouté ?**
   ```typescript
   // ✅ Correct
   headers: {
     'Authorization': `Bearer ${token}`
   }
   ```

3. **L'URL est-elle correcte ?**
   ```typescript
   // ✅ Correct
   POST /auth/logout
   // ou
   POST http://localhost:8089/carthage-creance/auth/logout
   ```

---

### 4. Test Manuel avec Postman/curl

#### Test avec curl :

```bash
# Remplacer YOUR_JWT_TOKEN par votre token réel
curl -X POST "http://localhost:8089/carthage-creance/auth/logout" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

**Résultat Attendu :**
```json
{
  "message": "Logout successful"
}
```

#### Test avec Postman :

1. **Méthode** : `POST`
2. **URL** : `http://localhost:8089/carthage-creance/auth/logout`
3. **Headers** :
   - `Authorization`: `Bearer {votre_token_jwt}`
   - `Content-Type`: `application/json`
4. **Body** : (vide)

**Résultat Attendu :**
- Status: `200 OK`
- Body: `{"message":"Logout successful"}`

---

### 5. Vérifier les Logs Backend

#### Après avoir cliqué sur logout, vérifiez les logs backend :

**✅ Logs Attendus :**
```
=== DÉBUT LOGOUT ===
Logout: Token JWT extrait (longueur: XXX)
Logout: Token trouvé, ID: XXX
Logout: Utilisateur trouvé - ID: XXX, Email: XXX
Logout: derniere_deconnexion mise à jour pour l'utilisateur XXX: 2025-11-25T...
Logout: Token révoqué
=== FIN LOGOUT ===
```

**❌ Si vous ne voyez PAS ces logs :**
- Le frontend n'appelle pas `/auth/logout`
- Ou l'endpoint n'est pas atteint (problème de routing)

**❌ Si vous voyez :**
```
Logout: Pas de header Authorization ou format invalide
```
→ Le frontend n'envoie pas le header `Authorization`

**❌ Si vous voyez :**
```
Logout: Token non trouvé dans la base de données
```
→ Le token envoyé n'existe pas dans la base (peut-être déjà révoqué)

**❌ Si vous voyez :**
```
Logout: Utilisateur non trouvé pour le token
```
→ Problème de relation entre Token et Utilisateur (déjà corrigé avec JOIN FETCH)

---

### 6. Solutions selon le Problème

#### Problème 1 : Le frontend n'appelle pas `/auth/logout`

**Solution :**
Vérifiez le code du composant/service qui gère le logout :

```typescript
// Exemple de correction
logout() {
  this.authService.logout().subscribe({
    next: () => {
      // Supprimer le token du localStorage
      localStorage.removeItem('token');
      // Rediriger vers la page de login
      this.router.navigate(['/login']);
    },
    error: (error) => {
      console.error('Erreur lors du logout:', error);
      // Même en cas d'erreur, supprimer le token localement
      localStorage.removeItem('token');
      this.router.navigate(['/login']);
    }
  });
}
```

#### Problème 2 : Le header Authorization n'est pas envoyé

**Solution :**
Modifiez le service pour inclure le header :

```typescript
logout(): Observable<any> {
  const token = this.getToken(); // Méthode qui récupère le token
  
  if (!token) {
    console.warn('Aucun token trouvé pour le logout');
    return of({ message: 'No token to logout' });
  }
  
  const headers = new HttpHeaders({
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  });
  
  return this.http.post(`${this.apiUrl}/auth/logout`, {}, { headers })
    .pipe(
      catchError(error => {
        console.error('Erreur logout:', error);
        // Même en cas d'erreur, supprimer le token localement
        this.clearToken();
        return throwError(() => error);
      })
    );
}
```

#### Problème 3 : L'URL est incorrecte

**Solution :**
Vérifiez la configuration de l'URL de base :

```typescript
// Dans votre service
private apiUrl = 'http://localhost:8089/carthage-creance';

// Ou utilisez un environnement
private apiUrl = environment.apiUrl; // http://localhost:8089/carthage-creance
```

---

### 7. Test Complet : Scénario de Débogage

#### Étape par étape :

1. **Ouvrir la console du navigateur (F12)**
2. **Aller dans l'onglet "Network"**
3. **Filtrer par "logout" ou "auth"**
4. **Se connecter à l'application**
5. **Cliquer sur "Déconnexion"**
6. **Vérifier dans Network :**
   - ✅ Une requête `POST /auth/logout` apparaît
   - ✅ Status: `200 OK`
   - ✅ Request Headers contient `Authorization: Bearer ...`
7. **Vérifier dans la console :**
   - ✅ Pas d'erreur JavaScript
   - ✅ Message de succès (si affiché)
8. **Vérifier les logs backend :**
   - ✅ Les logs "=== DÉBUT LOGOUT ===" apparaissent
   - ✅ "derniere_deconnexion mise à jour" apparaît
9. **Vérifier la base de données :**
   ```sql
   SELECT id, email, derniere_connexion, derniere_deconnexion 
   FROM utilisateur 
   WHERE email = 'votre_email@example.com';
   ```
   - ✅ `derniere_deconnexion` n'est plus `NULL`

---

### 8. Code Frontend de Référence (Angular)

#### Service d'Authentification Complet :

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8089/carthage-creance';
  private tokenKey = 'auth_token';

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  /**
   * Récupère le token depuis le localStorage
   */
  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  /**
   * Sauvegarde le token dans le localStorage
   */
  setToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
  }

  /**
   * Supprime le token du localStorage
   */
  clearToken(): void {
    localStorage.removeItem(this.tokenKey);
  }

  /**
   * Vérifie si l'utilisateur est connecté
   */
  isAuthenticated(): boolean {
    return this.getToken() !== null;
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
          this.setToken(response.token);
        }
      })
    );
  }

  /**
   * Logout - IMPORTANT : Envoie le token au backend
   */
  logout(): Observable<any> {
    const token = this.getToken();
    
    if (!token) {
      console.warn('Aucun token trouvé, logout local uniquement');
      this.clearToken();
      this.router.navigate(['/login']);
      return of({ message: 'No token to logout' });
    }

    // Créer les headers avec le token
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });

    console.log('🔄 Appel logout avec token:', token.substring(0, 20) + '...');

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
      // Finalement, toujours supprimer le token et rediriger
      tap(() => {
        this.clearToken();
        this.router.navigate(['/login']);
      })
    );
  }
}
```

#### Composant qui Utilise le Service :

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

---

### 9. Vérification Rapide avec la Console du Navigateur

#### Test Direct dans la Console :

Ouvrez la console du navigateur (F12) et exécutez :

```javascript
// 1. Vérifier si le token existe
const token = localStorage.getItem('auth_token');
console.log('Token:', token ? token.substring(0, 20) + '...' : 'NULL');

// 2. Tester l'appel logout directement
if (token) {
  fetch('http://localhost:8089/carthage-creance/auth/logout', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  })
  .then(response => response.json())
  .then(data => {
    console.log('✅ Réponse logout:', data);
  })
  .catch(error => {
    console.error('❌ Erreur:', error);
  });
} else {
  console.warn('⚠️ Aucun token trouvé dans localStorage');
}
```

**Résultat Attendu :**
```
Token: eyJhbGciOiJIUzI1NiIsIn...
✅ Réponse logout: {message: "Logout successful"}
```

---

### 10. Points de Vérification Rapide

**✅ Checklist Rapide :**

- [ ] Le frontend a un service `AuthService` (ou similaire)
- [ ] Le service a une méthode `logout()`
- [ ] La méthode `logout()` fait un `POST` vers `/auth/logout`
- [ ] Le header `Authorization: Bearer {token}` est inclus
- [ ] Le token est récupéré depuis `localStorage` ou `sessionStorage`
- [ ] Après le logout, le token est supprimé du storage local
- [ ] L'utilisateur est redirigé vers la page de login
- [ ] Les logs backend montrent "=== DÉBUT LOGOUT ==="
- [ ] La base de données montre `derniere_deconnexion` remplie

---

### 11. Messages d'Erreur Courants et Solutions

#### Erreur : `401 Unauthorized`
**Cause :** Token manquant ou invalide
**Solution :** Vérifier que le header `Authorization` est bien envoyé avec le token

#### Erreur : `404 Not Found`
**Cause :** URL incorrecte
**Solution :** Vérifier que l'URL est `http://localhost:8089/carthage-creance/auth/logout`

#### Erreur : `CORS error`
**Cause :** Problème de configuration CORS
**Solution :** Vérifier que `@CrossOrigin` est présent sur le contrôleur

#### Pas d'erreur mais `derniere_deconnexion` reste null
**Cause :** Le `LogoutService` n'est pas appelé ou l'utilisateur n'est pas trouvé
**Solution :** Vérifier les logs backend pour voir où ça bloque

---

### 12. Commandes SQL pour Vérifier

```sql
-- Vérifier les dates de connexion/déconnexion
SELECT 
    id,
    email,
    nom,
    prenom,
    derniere_connexion,
    derniere_deconnexion,
    date_creation
FROM utilisateur
ORDER BY date_creation DESC;

-- Vérifier les tokens actifs
SELECT 
    t.TokenId,
    t.token,
    t.expired,
    t.revoked,
    u.id as user_id,
    u.email
FROM token t
LEFT JOIN utilisateur u ON t.user_id = u.id
WHERE t.revoked = false
ORDER BY t.timeStamp DESC;
```

---

## 📝 Résumé

**Pour que `derniere_deconnexion` soit remplie, il faut :**

1. ✅ Le frontend appelle `POST /auth/logout`
2. ✅ Le header `Authorization: Bearer {token}` est envoyé
3. ✅ Le backend trouve le token dans la base
4. ✅ Le backend charge l'utilisateur depuis le token
5. ✅ Le backend met à jour `derniere_deconnexion` et sauvegarde

**Si l'une de ces étapes échoue, `derniere_deconnexion` restera `NULL`.**

Utilisez ce guide pour identifier à quelle étape le processus échoue.

