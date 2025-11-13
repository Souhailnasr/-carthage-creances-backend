# Prompt : Amélioration de la Gestion des Erreurs - Validation d'Enquête

## 🎯 PROMPT À COPIER DANS CURSOR AI

```
Dans le projet Angular, localisez le composant et le service qui gèrent la validation des enquêtes (probablement validation-enquete.service.ts et le composant qui l'utilise).

AMÉLIOREZ la gestion des erreurs pour afficher les messages détaillés retournés par le backend.

PROBLÈME ACTUEL :
- Le backend retourne maintenant des messages d'erreur détaillés dans le body de la réponse (ex: "Erreur : Aucune validation en attente trouvée pour cette enquête")
- Le frontend affiche probablement un message générique au lieu d'utiliser le message détaillé du backend

SOLUTION :
1. Dans le service (validation-enquete.service.ts) :
   - Modifier la méthode handleError() ou catchError() pour extraire le message d'erreur depuis error.error
   - Le backend retourne maintenant le message dans le body : "Erreur : [message détaillé]"
   - Extraire ce message et le retourner dans l'Observable

2. Dans le composant qui utilise le service :
   - Afficher le message d'erreur détaillé dans un MatSnackBar
   - Utiliser error.error ou error.message selon la structure de l'erreur

CODE CORRECT :

```typescript
// Dans le service (validation-enquete.service.ts)
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
      // Extraire le message d'erreur du backend
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
      // Extraire le message d'erreur du backend
      let errorMessage = 'Erreur lors du rejet de l\'enquête';
      
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
      
      console.error('Erreur lors du rejet:', errorMessage);
      return throwError(() => new Error(errorMessage));
    })
  );
}
```

```typescript
// Dans le composant qui utilise le service
validerEnquete(validationId: number): void {
  const chefId = this.getCurrentUserId(); // Récupérer l'ID du chef connecté
  
  this.validationEnqueteService.validerEnquete(validationId, chefId, this.commentaire)
    .subscribe({
      next: (validation) => {
        this.snackBar.open('Enquête validée avec succès', 'Fermer', {
          duration: 3000,
          panelClass: ['success-snackbar']
        });
        this.loadEnquetesEnAttente(); // Rafraîchir la liste
      },
      error: (error) => {
        // Afficher le message d'erreur détaillé du backend
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
        this.snackBar.open('Enquête rejetée avec succès', 'Fermer', {
          duration: 3000,
          panelClass: ['success-snackbar']
        });
        this.loadEnquetesEnAttente(); // Rafraîchir la liste
      },
      error: (error) => {
        // Afficher le message d'erreur détaillé du backend
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

IMPORTANT :
- Importer HttpErrorResponse depuis @angular/common/http
- Importer throwError depuis rxjs
- Utiliser MatSnackBar pour afficher les messages d'erreur
- Nettoyer le message d'erreur pour enlever le préfixe "Erreur : " si présent
- Logger les erreurs dans la console pour le débogage
- Afficher des messages différents pour succès et erreur avec des couleurs appropriées
```

---

## 📋 Messages d'Erreur Possibles du Backend

Le backend retourne maintenant des messages d'erreur spécifiques :

1. **"Erreur : Validation non trouvée avec l'ID X"**
   - La validation n'existe pas dans la base de données

2. **"Erreur : Cette validation n'est pas en attente. Statut actuel : VALIDE"**
   - La validation a déjà été traitée

3. **"Erreur : Aucune enquête associée à cette validation"**
   - Problème de données (enquête manquante)

4. **"Erreur : Chef non trouvé avec l'ID: X"**
   - Le chefId n'existe pas

5. **"Erreur : L'utilisateur n'a pas les droits pour valider des enquêtes"**
   - L'utilisateur n'a pas le rôle de chef

6. **"Erreur : Aucune validation en attente trouvée pour cette enquête"**
   - Il n'y a pas de validation en attente pour cette enquête

7. **"Erreur : Un agent ne peut pas valider ses propres enquêtes"**
   - Règle métier : un agent ne peut pas valider ses propres enquêtes

8. **"Erreur : Enquête non trouvée avec l'ID: X"**
   - L'enquête n'existe pas

## ✅ Checklist de Vérification

Après correction, vérifiez que :

- [ ] Les messages d'erreur du backend sont correctement extraits
- [ ] Les messages sont affichés dans un MatSnackBar
- [ ] Le préfixe "Erreur : " est retiré pour un affichage plus propre
- [ ] Les messages de succès sont affichés différemment des erreurs
- [ ] Les erreurs sont loggées dans la console pour le débogage
- [ ] La liste des enquêtes est rafraîchie après une validation/rejet réussie

## 🔍 Structure de l'Erreur HTTP

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

## 📝 Notes Importantes

- Le backend retourne maintenant des messages d'erreur **détaillés** au lieu de body vide
- Ces messages commencent souvent par "Erreur : " qu'il faut nettoyer pour l'affichage
- Les messages sont retournés comme **string** dans le body, pas comme objet JSON
- Il faut gérer les différents formats possibles (string, object avec message, etc.)

