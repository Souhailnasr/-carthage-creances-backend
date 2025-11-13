# Prompt : Correction de la Validation d'Enquête - Frontend

## 🎯 PROMPT À COPIER DANS CURSOR AI

```
Dans le projet Angular, localisez le service qui gère la validation des enquêtes (probablement validation-enquete.service.ts ou enquete.service.ts).

CORRIGEZ les méthodes suivantes pour envoyer les paramètres dans l'URL (query parameters) au lieu du body JSON :

1. Méthode validerEnquete() :
   - ACTUELLEMENT : Envoie chefId et commentaire dans le body JSON
   - À CORRIGER : Envoyer chefId et commentaire comme query parameters dans l'URL
   
   Format correct de l'URL :
   POST /api/validation/enquetes/{id}/valider?chefId={chefId}&commentaire={commentaire}
   
   Si commentaire est null/undefined/vide, ne pas l'inclure dans l'URL :
   POST /api/validation/enquetes/{id}/valider?chefId={chefId}

2. Méthode rejeterEnquete() :
   - ACTUELLEMENT : Envoie chefId et commentaire dans le body JSON
   - À CORRIGER : Envoyer chefId et commentaire comme query parameters dans l'URL
   
   Format correct de l'URL :
   POST /api/validation/enquetes/{id}/rejeter?chefId={chefId}&commentaire={commentaire}
   
   Si commentaire est null/undefined/vide, ne pas l'inclure dans l'URL :
   POST /api/validation/enquetes/{id}/rejeter?chefId={chefId}

IMPORTANT :
- Utiliser HttpParams pour construire les query parameters proprement
- Ne pas envoyer de body JSON (ou envoyer un body vide/null)
- Gérer les cas où commentaire est optionnel (ne pas l'inclure s'il est vide)
- Conserver la gestion d'erreurs existante
- Conserver les headers d'authentification (JWT token)
- Conserver les logs de débogage si présents

Exemple de code TypeScript correct :

```typescript
validerEnquete(id: number, chefId: number, commentaire?: string): Observable<ValidationEnquete> {
  let params = new HttpParams().set('chefId', chefId.toString());
  
  if (commentaire && commentaire.trim() !== '') {
    params = params.set('commentaire', commentaire);
  }
  
  return this.http.post<ValidationEnquete>(
    `${this.apiUrl}/validation/enquetes/${id}/valider`,
    null, // Pas de body JSON
    { params: params }
  ).pipe(
    catchError(this.handleError)
  );
}

rejeterEnquete(id: number, chefId: number, commentaire?: string): Observable<ValidationEnquete> {
  let params = new HttpParams().set('chefId', chefId.toString());
  
  if (commentaire && commentaire.trim() !== '') {
    params = params.set('commentaire', commentaire);
  }
  
  return this.http.post<ValidationEnquete>(
    `${this.apiUrl}/validation/enquetes/${id}/rejeter`,
    null, // Pas de body JSON
    { params: params }
  ).pipe(
    catchError(this.handleError)
  );
}
```

Vérifiez également que :
- HttpParams est importé depuis @angular/common/http
- Les méthodes sont correctement typées
- Les composants qui appellent ces méthodes n'ont pas besoin de modification (ils passent déjà les paramètres correctement)
```

---

## 📋 Détails Techniques

### Problème Actuel

Le backend attend les paramètres comme **query parameters** dans l'URL :
```java
@RequestParam Long chefId
@RequestParam(required = false) String commentaire
```

Mais le frontend les envoie dans le **body JSON** :
```typescript
// ❌ INCORRECT
this.http.post(url, { chefId: 32, commentaire: "valider" })
```

### Solution

Utiliser `HttpParams` pour construire les query parameters :
```typescript
// ✅ CORRECT
let params = new HttpParams().set('chefId', chefId.toString());
if (commentaire) {
  params = params.set('commentaire', commentaire);
}
this.http.post(url, null, { params: params })
```

### Endpoints à Corriger

1. **POST /api/validation/enquetes/{id}/valider**
   - Paramètres : `chefId` (requis), `commentaire` (optionnel)
   - Format URL : `/api/validation/enquetes/5/valider?chefId=32&commentaire=valider`

2. **POST /api/validation/enquetes/{id}/rejeter**
   - Paramètres : `chefId` (requis), `commentaire` (optionnel)
   - Format URL : `/api/validation/enquetes/5/rejeter?chefId=32&commentaire=rejet`

### Imports Nécessaires

```typescript
import { HttpParams } from '@angular/common/http';
```

### Vérification

Après correction, la requête HTTP doit ressembler à :
```
POST http://localhost:8089/carthage-creance/api/validation/enquetes/5/valider?chefId=32&commentaire=valider
Headers: Authorization: Bearer <token>
Body: (vide)
```

---

## ✅ Checklist de Vérification

Après correction, vérifiez que :

- [ ] `HttpParams` est importé
- [ ] `chefId` est envoyé comme query parameter
- [ ] `commentaire` est envoyé comme query parameter (si présent)
- [ ] Le body JSON est `null` ou vide
- [ ] Les méthodes fonctionnent avec et sans commentaire
- [ ] Les erreurs sont toujours gérées correctement
- [ ] Le JWT token est toujours inclus dans les headers
- [ ] Les logs de débogage fonctionnent toujours

---

## 🔍 Comment Tester

1. Ouvrez la console du navigateur (F12)
2. Allez dans l'onglet "Network"
3. Tentez de valider une enquête
4. Vérifiez que la requête POST contient :
   - URL avec `?chefId=...&commentaire=...`
   - Body vide ou null
   - Status 200 OK (au lieu de 500)

---

**Ce prompt corrige le problème de format de requête HTTP entre le frontend et le backend.**

