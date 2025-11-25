# 🔍 Prompt de Débogage : Vérification Logout Frontend

## Prompt pour Vérifier le Logout dans le Frontend Angular

```
Vérifie dans le code frontend Angular si le logout fonctionne correctement :

1. **Localiser le service d'authentification** (ex: `auth.service.ts`, `authentication.service.ts`)
   - Chercher la méthode `logout()` ou `signOut()`
   - Vérifier qu'elle fait un appel HTTP POST vers `/auth/logout`

2. **Vérifier que le header Authorization est envoyé** :
   - Le service doit récupérer le token depuis localStorage/sessionStorage
   - Le header doit être : `Authorization: Bearer {token}`
   - Format attendu : `headers: { 'Authorization': 'Bearer ' + token }`

3. **Vérifier l'URL de l'endpoint** :
   - Doit être : `POST http://localhost:8089/carthage-creance/auth/logout`
   - Ou : `POST ${apiUrl}/auth/logout` où apiUrl = `http://localhost:8089/carthage-creance`

4. **Tester dans la console du navigateur** :
   - Ouvrir F12 → Network
   - Cliquer sur "Déconnexion"
   - Vérifier qu'une requête POST vers `/auth/logout` apparaît
   - Vérifier que les Request Headers contiennent `Authorization: Bearer ...`

5. **Vérifier les logs backend** :
   - Après le logout, les logs doivent afficher :
     - "=== DÉBUT LOGOUT ==="
     - "Logout: Token JWT extrait"
     - "Logout: Utilisateur trouvé"
     - "Logout: derniere_deconnexion mise à jour"

6. **Si le frontend n'appelle pas /auth/logout** :
   - Modifier le service pour ajouter l'appel HTTP POST
   - S'assurer que le token est inclus dans les headers
   - Gérer les erreurs et supprimer le token localement même en cas d'erreur

7. **Code de référence attendu** :
```typescript
logout(): Observable<any> {
  const token = localStorage.getItem('auth_token');
  if (!token) {
    this.clearToken();
    return of({});
  }
  
  const headers = new HttpHeaders({
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  });
  
  return this.http.post(`${this.apiUrl}/auth/logout`, {}, { headers })
    .pipe(
      catchError(error => {
        console.error('Erreur logout:', error);
        this.clearToken();
        return throwError(() => error);
      }),
      finalize(() => {
        this.clearToken();
        this.router.navigate(['/login']);
      })
    );
}
```

8. **Vérifier dans la base de données** :
   - Après logout, exécuter : `SELECT id, email, derniere_connexion, derniere_deconnexion FROM utilisateur WHERE email = 'votre_email';`
   - `derniere_deconnexion` ne doit plus être NULL

Corrige le code frontend si nécessaire pour que le logout appelle bien `/auth/logout` avec le header Authorization.
```

---

## Prompt pour Tester avec Postman/curl

```
Teste l'endpoint de logout avec les commandes suivantes :

**Avec curl :**
```bash
curl -X POST "http://localhost:8089/carthage-creance/auth/logout" \
  -H "Authorization: Bearer VOTRE_TOKEN_JWT_ICI" \
  -H "Content-Type: application/json" \
  -v
```

**Avec Postman :**
- Méthode : POST
- URL : http://localhost:8089/carthage-creance/auth/logout
- Headers :
  - Authorization: Bearer {votre_token}
  - Content-Type: application/json
- Body : (vide)

**Résultat attendu :**
- Status: 200 OK
- Body: {"message":"Logout successful"}
- Logs backend : "=== DÉBUT LOGOUT ===" et "derniere_deconnexion mise à jour"
```

---

## Prompt pour Vérifier dans la Console du Navigateur

```
Ouvre la console du navigateur (F12) et exécute ce code pour tester le logout :

```javascript
// 1. Vérifier le token
const token = localStorage.getItem('auth_token') || 
              localStorage.getItem('token') || 
              sessionStorage.getItem('auth_token');
console.log('Token trouvé:', token ? 'OUI (' + token.substring(0, 20) + '...)' : 'NON');

// 2. Tester l'appel logout
if (token) {
  fetch('http://localhost:8089/carthage-creance/auth/logout', {
    method: 'POST',
    headers: {
      'Authorization': 'Bearer ' + token,
      'Content-Type': 'application/json'
    }
  })
  .then(r => r.json())
  .then(data => console.log('✅ Logout réussi:', data))
  .catch(err => console.error('❌ Erreur:', err));
} else {
  console.warn('⚠️ Aucun token trouvé. Connectez-vous d\'abord.');
}
```

Si ça fonctionne, vous devriez voir "✅ Logout réussi: {message: 'Logout successful'}" dans la console.
```

---

## Checklist Rapide

```
Vérifie ces points dans l'ordre :

□ Le service auth a une méthode logout()
□ La méthode logout() fait POST vers /auth/logout
□ Le header Authorization: Bearer {token} est inclus
□ Le token est récupéré depuis localStorage/sessionStorage
□ Dans Network (F12), la requête POST /auth/logout apparaît
□ Les Request Headers contiennent Authorization
□ Les logs backend montrent "=== DÉBUT LOGOUT ==="
□ La base de données montre derniere_deconnexion remplie

Si une case n'est pas cochée, c'est là que se trouve le problème.
```

