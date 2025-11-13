# 📋 Changements Frontend Complets - Gestion des Enquêtes

## 🎯 Vue d'ensemble

Ce document liste **TOUS les changements nécessaires** côté frontend après les corrections backend pour la gestion des enquêtes et leur validation.

---

## ✅ Changement 1 : Format des Paramètres de Validation (CRITIQUE)

### Problème

Le backend attend `chefId` et `commentaire` comme **query parameters** dans l'URL, mais le frontend les envoie dans le **body JSON**.

### Solution

**Fichier à modifier** : `validation-enquete.service.ts` (ou `enquete.service.ts`)

**Code à corriger** :

```typescript
// ❌ AVANT (incorrect)
validerEnquete(id: number, chefId: number, commentaire?: string): Observable<ValidationEnquete> {
  return this.http.post<ValidationEnquete>(
    `${this.apiUrl}/validation/enquetes/${id}/valider`,
    { chefId: chefId, commentaire: commentaire } // ❌ Dans le body
  );
}

// ✅ APRÈS (correct)
import { HttpParams } from '@angular/common/http';

validerEnquete(id: number, chefId: number, commentaire?: string): Observable<ValidationEnquete> {
  let params = new HttpParams().set('chefId', chefId.toString());
  
  if (commentaire && commentaire.trim() !== '') {
    params = params.set('commentaire', commentaire);
  }
  
  return this.http.post<ValidationEnquete>(
    `${this.apiUrl}/validation/enquetes/${id}/valider`,
    null, // ✅ Body vide
    { params: params } // ✅ Paramètres dans l'URL
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
    null, // ✅ Body vide
    { params: params } // ✅ Paramètres dans l'URL
  ).pipe(
    catchError(this.handleError)
  );
}
```

**Vérifications** :
- [ ] `HttpParams` est importé depuis `@angular/common/http`
- [ ] `chefId` est dans l'URL (query parameter)
- [ ] `commentaire` est dans l'URL (si présent)
- [ ] Le body est `null` ou vide

**Document de référence** : `PROMPT_CORRECTION_VALIDATION_ENQUETE_FRONTEND.md`

---

## ✅ Changement 2 : Affichage des Messages d'Erreur Détaillés (IMPORTANT)

### Problème

Le backend retourne maintenant des messages d'erreur détaillés dans le body de la réponse (ex: `"Erreur : Aucune validation en attente trouvée pour cette enquête"`), mais le frontend affiche un message générique.

### Solution

**Fichiers à modifier** :
1. `validation-enquete.service.ts` - Extraire le message d'erreur
2. Composant qui utilise le service - Afficher le message détaillé

**Code à ajouter/modifier** :

```typescript
// Dans le service (validation-enquete.service.ts)
import { HttpErrorResponse } from '@angular/common/http';
import { throwError } from 'rxjs';

validerEnquete(id: number, chefId: number, commentaire?: string): Observable<ValidationEnquete> {
  let params = new HttpParams().set('chefId', chefId.toString());
  
  if (commentaire && commentaire.trim() !== '') {
    params = params.set('commentaire', commentaire);
  }
  
  return this.http.post<ValidationEnquete>(
    `${this.apiUrl}/validation/enquetes/${id}/valider`,
    null,
    { params: params }
  ).pipe(
    catchError((error: HttpErrorResponse) => {
      // ✅ Extraire le message d'erreur du backend
      let errorMessage = 'Erreur lors de la validation de l\'enquête';
      
      if (error.error) {
        // Le backend retourne maintenant "Erreur : [message détaillé]"
        if (typeof error.error === 'string') {
          errorMessage = error.error;
        } else if (error.error.message) {
          errorMessage = error.error.message;
        } else if (error.error.error) {
          errorMessage = error.error.error;
        }
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      console.error('Erreur lors de la validation:', errorMessage);
      return throwError(() => new Error(errorMessage));
    })
  );
}

rejeterEnquete(id: number, chefId: number, commentaire?: string): Observable<ValidationEnquete> {
  let params = new HttpParams().set('chefId', chefId.toString());
  
  if (commentaire && commentaire.trim() !== '') {
    params = params.set('commentaire', commentaire);
  }
  
  return this.http.post<ValidationEnquete>(
    `${this.apiUrl}/validation/enquetes/${id}/rejeter`,
    null,
    { params: params }
  ).pipe(
    catchError((error: HttpErrorResponse) => {
      // ✅ Extraire le message d'erreur du backend
      let errorMessage = 'Erreur lors du rejet de l\'enquête';
      
      if (error.error) {
        if (typeof error.error === 'string') {
          errorMessage = error.error;
        } else if (error.error.message) {
          errorMessage = error.error.message;
        } else if (error.error.error) {
          errorMessage = error.error.error;
        }
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      console.error('Erreur lors du rejet:', errorMessage);
      return throwError(() => new Error(errorMessage));
    })
  );
}
```

**Dans le composant** :

```typescript
// Dans le composant (ex: enquetes-en-attente.component.ts)
import { MatSnackBar } from '@angular/material/snack-bar';

validerEnquete(validationId: number): void {
  const chefId = this.getCurrentUserId(); // Récupérer l'ID du chef connecté
  
  this.validationEnqueteService.validerEnquete(validationId, chefId, this.commentaire)
    .subscribe({
      next: (validation) => {
        // ✅ Message de succès
        this.snackBar.open('Enquête validée avec succès', 'Fermer', {
          duration: 3000,
          panelClass: ['success-snackbar']
        });
        this.loadEnquetesEnAttente(); // Rafraîchir la liste
      },
      error: (error) => {
        // ✅ Afficher le message d'erreur détaillé du backend
        const errorMessage = error.message || 'Erreur lors de la validation de l\'enquête';
        
        // Nettoyer le message si il commence par "Erreur : "
        const cleanMessage = errorMessage.startsWith('Erreur : ') 
          ? errorMessage.substring(9) 
          : errorMessage;
        
        this.snackBar.open(cleanMessage, 'Fermer', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
        
        console.error('Erreur de validation:', error);
      }
    });
}

rejeterEnquete(validationId: number): void {
  const chefId = this.getCurrentUserId();
  
  if (!this.commentaire || this.commentaire.trim() === '') {
    this.snackBar.open('Le commentaire est obligatoire pour rejeter une enquête', 'Fermer', {
      duration: 3000,
      panelClass: ['warning-snackbar']
    });
    return;
  }
  
  this.validationEnqueteService.rejeterEnquete(validationId, chefId, this.commentaire)
    .subscribe({
      next: (validation) => {
        // ✅ Message de succès
        this.snackBar.open('Enquête rejetée avec succès', 'Fermer', {
          duration: 3000,
          panelClass: ['success-snackbar']
        });
        this.loadEnquetesEnAttente(); // Rafraîchir la liste
      },
      error: (error) => {
        // ✅ Afficher le message d'erreur détaillé du backend
        const errorMessage = error.message || 'Erreur lors du rejet de l\'enquête';
        
        // Nettoyer le message si il commence par "Erreur : "
        const cleanMessage = errorMessage.startsWith('Erreur : ') 
          ? errorMessage.substring(9) 
          : errorMessage;
        
        this.snackBar.open(cleanMessage, 'Fermer', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
        
        console.error('Erreur de rejet:', error);
      }
    });
}
```

**Vérifications** :
- [ ] Le message d'erreur est extrait depuis `error.error`
- [ ] Le préfixe "Erreur : " est retiré pour l'affichage
- [ ] Le message est affiché dans un MatSnackBar
- [ ] Les messages de succès sont différents des erreurs
- [ ] Les erreurs sont loggées dans la console

**Document de référence** : `PROMPT_AMELIORATION_GESTION_ERREURS_VALIDATION_FRONTEND.md`

---

## 📋 Messages d'Erreur Possibles du Backend

Le backend retourne maintenant des messages spécifiques :

| Message | Signification |
|---------|---------------|
| "Validation non trouvée avec l'ID X" | La validation n'existe pas |
| "Cette validation n'est pas en attente. Statut actuel : VALIDE" | Déjà traitée |
| "Aucune enquête associée à cette validation" | Problème de données |
| "Chef non trouvé avec l'ID: X" | Le chefId n'existe pas |
| "L'utilisateur n'a pas les droits pour valider des enquêtes" | Pas le rôle de chef |
| "Aucune validation en attente trouvée pour cette enquête" | Pas de validation en attente |
| "Un agent ne peut pas valider ses propres enquêtes" | Règle métier |
| "Enquête non trouvée avec l'ID: X" | L'enquête n'existe pas |

---

## ✅ Checklist Complète

### Changement 1 : Format des Paramètres
- [ ] `HttpParams` est importé depuis `@angular/common/http`
- [ ] `chefId` est envoyé dans l'URL (query parameter)
- [ ] `commentaire` est envoyé dans l'URL (si présent)
- [ ] Le body est `null` ou vide
- [ ] Les méthodes `validerEnquete()` et `rejeterEnquete()` sont corrigées

### Changement 2 : Messages d'Erreur
- [ ] `HttpErrorResponse` est importé depuis `@angular/common/http`
- [ ] `throwError` est importé depuis `rxjs`
- [ ] Le message d'erreur est extrait depuis `error.error`
- [ ] Le préfixe "Erreur : " est retiré pour l'affichage
- [ ] Le message est affiché dans un MatSnackBar
- [ ] Les messages de succès sont différents des erreurs
- [ ] Les erreurs sont loggées dans la console
- [ ] La liste des enquêtes est rafraîchie après une validation/rejet réussie

---

## 🚀 Ordre d'Application

1. **D'abord** : Corriger le format des paramètres (Changement 1) - **CRITIQUE**
2. **Ensuite** : Améliorer la gestion des erreurs (Changement 2) - **IMPORTANT**

---

## 📚 Documents de Référence

1. **`PROMPT_CORRECTION_VALIDATION_ENQUETE_FRONTEND.md`**
   - Correction du format des paramètres (chefId dans l'URL)
   - Code complet avec exemples

2. **`PROMPT_AMELIORATION_GESTION_ERREURS_VALIDATION_FRONTEND.md`**
   - Amélioration de l'affichage des messages d'erreur détaillés
   - Code complet pour service et composant

3. **`PROMPTS_FRONTEND_ENQUETES_COMPLET.md`**
   - Tous les prompts pour la mise à jour complète du frontend
   - Inclut PROMPT 6.5 (correction critique)

---

## 🧪 Test

Après les corrections :

1. **Tester la validation** d'une enquête
2. **Vérifier** dans la console réseau que :
   - Les paramètres sont dans l'URL (`?chefId=32&commentaire=...`)
   - Le body est vide
3. **Vérifier** que le message d'erreur détaillé s'affiche (si erreur)
4. **Vérifier** que le message de succès s'affiche (si succès)

---

## ⚠️ Important

- Le **Changement 1** est **CRITIQUE** - sans lui, vous aurez toujours une erreur 400
- Le **Changement 2** améliore l'**expérience utilisateur** - messages clairs au lieu de messages génériques
- Les deux changements sont **indépendants** mais **recommandés**

---

## 📝 Structure de l'Erreur HTTP

Le backend retourne maintenant :

```json
// Status: 400 Bad Request
"Erreur : Aucune validation en attente trouvée pour cette enquête"
```

Ou pour les erreurs serveur :

```json
// Status: 500 Internal Server Error
"Erreur serveur lors de la validation de l'enquête : [détails]"
```

Le frontend doit extraire ce message depuis `error.error` (qui est une string) ou `error.error.message` (si c'est un objet).

---

## 🔍 Exemple Complet

**Service complet** (`validation-enquete.service.ts`) :

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ValidationEnquete } from '../models/validation-enquete';

@Injectable({
  providedIn: 'root'
})
export class ValidationEnqueteService {
  private apiUrl = 'http://localhost:8089/carthage-creance/api';

  constructor(private http: HttpClient) {}

  validerEnquete(id: number, chefId: number, commentaire?: string): Observable<ValidationEnquete> {
    let params = new HttpParams().set('chefId', chefId.toString());
    
    if (commentaire && commentaire.trim() !== '') {
      params = params.set('commentaire', commentaire);
    }
    
    return this.http.post<ValidationEnquete>(
      `${this.apiUrl}/validation/enquetes/${id}/valider`,
      null,
      { params: params }
    ).pipe(
      catchError((error: HttpErrorResponse) => {
        let errorMessage = 'Erreur lors de la validation de l\'enquête';
        
        if (error.error) {
          if (typeof error.error === 'string') {
            errorMessage = error.error;
          } else if (error.error.message) {
            errorMessage = error.error.message;
          }
        }
        
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  rejeterEnquete(id: number, chefId: number, commentaire?: string): Observable<ValidationEnquete> {
    let params = new HttpParams().set('chefId', chefId.toString());
    
    if (commentaire && commentaire.trim() !== '') {
      params = params.set('commentaire', commentaire);
    }
    
    return this.http.post<ValidationEnquete>(
      `${this.apiUrl}/validation/enquetes/${id}/rejeter`,
      null,
      { params: params }
    ).pipe(
      catchError((error: HttpErrorResponse) => {
        let errorMessage = 'Erreur lors du rejet de l\'enquête';
        
        if (error.error) {
          if (typeof error.error === 'string') {
            errorMessage = error.error;
          } else if (error.error.message) {
            errorMessage = error.error.message;
          }
        }
        
        return throwError(() => new Error(errorMessage));
      })
    );
  }
}
```

---

**Bon développement ! 🚀**

