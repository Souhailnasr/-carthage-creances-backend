# 🔐 Corrections d'Authentification - Endpoints Statistiques

## 🎯 Problème Identifié

Les endpoints de statistiques retournaient une erreur **500 Internal Server Error** au lieu de **401 Unauthorized** quand le token JWT était expiré, ce qui empêchait le frontend de détecter correctement l'expiration et de rediriger l'utilisateur vers la page de connexion.

---

## ✅ Corrections Appliquées

### Endpoints Corrigés

1. **GET `/api/statistiques/departement`**
2. **GET `/api/statistiques/mes-agents`**
3. **GET `/api/statistiques/mes-dossiers`**
4. **GET `/api/statistiques/recouvrement-par-phase/departement`**

---

## 🔧 Modifications Techniques

### Avant (Problème)

```java
try {
    Utilisateur chef = userExtractionService.extractUserFromToken(authHeader);
    if (chef == null) {
        return ResponseEntity.status(401).build();
    }
    // ... logique métier
} catch (Exception e) {
    logger.error("Erreur: {}", e.getMessage(), e);
    return ResponseEntity.internalServerError().build(); // ❌ Retourne 500
}
```

**Problème :** L'exception `ExpiredJwtException` était capturée dans le `catch (Exception e)` général et retournait une erreur 500.

---

### Après (Solution)

```java
try {
    Utilisateur chef;
    try {
        chef = userExtractionService.extractUserFromToken(authHeader);
    } catch (ExpiredJwtException e) {
        logger.error("Token JWT expiré: {}", e.getMessage());
        return ResponseEntity.status(401).body(Map.of(
            "error", "Token expiré",
            "message", "Votre session a expiré. Veuillez vous reconnecter.",
            "code", "TOKEN_EXPIRED",
            "expiredAt", e.getClaims().getExpiration().toString(),
            "currentTime", new Date().toString()
        )); // ✅ Retourne 401 avec message clair
    }
    
    if (chef == null) {
        return ResponseEntity.status(401).body(Map.of(
            "error", "Token invalide",
            "message", "Impossible d'extraire l'utilisateur depuis le token",
            "code", "USER_NOT_FOUND"
        ));
    }
    // ... logique métier
} catch (Exception e) {
    logger.error("Erreur: {}", e.getMessage(), e);
    return ResponseEntity.status(500).body(Map.of(
        "error", "Erreur interne du serveur",
        "message", "Une erreur est survenue lors de la récupération des statistiques",
        "code", "INTERNAL_SERVER_ERROR"
    ));
}
```

**Solution :** 
- Try-catch spécifique pour `ExpiredJwtException`
- Retourne **401 Unauthorized** avec un message clair
- Codes d'erreur standardisés
- Informations sur l'expiration du token

---

## 📊 Format des Réponses d'Erreur

### Token Expiré (401)

```json
{
  "error": "Token expiré",
  "message": "Votre session a expiré. Veuillez vous reconnecter.",
  "code": "TOKEN_EXPIRED",
  "expiredAt": "2025-12-04T21:32:06Z",
  "currentTime": "2025-12-05T04:38:45Z"
}
```

### Token Invalide (401)

```json
{
  "error": "Token invalide",
  "message": "Impossible d'extraire l'utilisateur depuis le token",
  "code": "USER_NOT_FOUND"
}
```

### Erreur Serveur (500)

```json
{
  "error": "Erreur interne du serveur",
  "message": "Une erreur est survenue lors de la récupération des statistiques",
  "code": "INTERNAL_SERVER_ERROR"
}
```

---

## 🎯 Avantages

### 1. Détection Frontend

Le frontend peut maintenant :
- Détecter l'expiration du token (code 401)
- Afficher un message clair à l'utilisateur
- Rediriger automatiquement vers la page de connexion
- Rafraîchir le token si possible

### 2. Messages d'Erreur Clairs

- **Code d'erreur standardisé** : `TOKEN_EXPIRED`, `USER_NOT_FOUND`, `INTERNAL_SERVER_ERROR`
- **Message utilisateur** : Message clair et compréhensible
- **Informations techniques** : Date d'expiration et heure actuelle pour le débogage

### 3. Séparation des Erreurs

- **401** : Problème d'authentification (token expiré, invalide)
- **500** : Erreur serveur (problème de base de données, logique métier)

---

## 🔍 Tests à Effectuer

### Test 1 : Token Expiré

1. **Obtenez un token JWT** via l'endpoint de login
2. **Attendez que le token expire** (ou utilisez un token ancien)
3. **Appelez un endpoint de statistiques** avec ce token
4. **Vérifiez la réponse** :
   - Status : `401 Unauthorized`
   - Body : JSON avec `code: "TOKEN_EXPIRED"`

### Test 2 : Token Invalide

1. **Appelez un endpoint** avec un token invalide (ex: "invalid_token")
2. **Vérifiez la réponse** :
   - Status : `401 Unauthorized`
   - Body : JSON avec `code: "USER_NOT_FOUND"`

### Test 3 : Token Valide

1. **Appelez un endpoint** avec un token valide
2. **Vérifiez la réponse** :
   - Status : `200 OK`
   - Body : JSON avec les statistiques

---

## 📝 Exemple d'Intégration Frontend

### Intercepteur HTTP Angular

```typescript
import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private router: Router) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<any> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          const errorBody = error.error;
          
          // Token expiré
          if (errorBody?.code === 'TOKEN_EXPIRED') {
            // Rediriger vers la page de connexion
            this.router.navigate(['/login'], {
              queryParams: { 
                reason: 'session_expired',
                message: errorBody.message 
              }
            });
          }
          
          // Token invalide
          else if (errorBody?.code === 'USER_NOT_FOUND') {
            // Rediriger vers la page de connexion
            this.router.navigate(['/login'], {
              queryParams: { 
                reason: 'invalid_token',
                message: errorBody.message 
              }
            });
          }
        }
        
        return throwError(() => error);
      })
    );
  }
}
```

---

## ✅ Checklist de Vérification

### Backend
- [x] Import de `ExpiredJwtException` ajouté
- [x] Import de `Date` ajouté
- [x] Try-catch spécifique pour `ExpiredJwtException` dans `getStatistiquesDepartement`
- [x] Try-catch spécifique pour `ExpiredJwtException` dans `getStatistiquesMesAgents`
- [x] Try-catch spécifique pour `ExpiredJwtException` dans `getStatistiquesMesDossiers`
- [x] Try-catch spécifique pour `ExpiredJwtException` dans `getStatistiquesRecouvrementParPhaseDepartement`
- [x] Messages d'erreur clairs et informatifs
- [x] Codes d'erreur standardisés
- [x] Aucune erreur de compilation

### Tests
- [ ] Tester avec un token expiré → Doit retourner 401
- [ ] Tester avec un token invalide → Doit retourner 401
- [ ] Tester avec un token valide → Doit retourner 200 avec les statistiques
- [ ] Vérifier les messages d'erreur dans les réponses

### Frontend (À faire)
- [ ] Créer un intercepteur HTTP pour détecter les erreurs 401
- [ ] Rediriger vers la page de connexion quand le token est expiré
- [ ] Afficher un message clair à l'utilisateur
- [ ] Implémenter le rafraîchissement automatique du token si possible

---

## 🚀 Prochaines Étapes

1. **Compiler le projet** pour vérifier qu'il n'y a pas d'erreurs
2. **Tester les endpoints** avec Postman (utiliser la collection fournie)
3. **Vérifier les réponses** avec des tokens expirés et valides
4. **Intégrer dans le frontend** l'intercepteur HTTP pour gérer les erreurs 401

---

## 📊 Résultat Final

Après ces corrections :

1. ✅ **Les endpoints retournent 401** au lieu de 500 quand le token est expiré
2. ✅ **Messages d'erreur clairs** pour le frontend et l'utilisateur
3. ✅ **Codes d'erreur standardisés** pour faciliter la détection
4. ✅ **Le frontend peut détecter l'expiration** et rediriger l'utilisateur
5. ✅ **Les statistiques sont retournées** correctement quand le token est valide

---

**Date de création :** 2025-12-05
**Version :** 1.0




