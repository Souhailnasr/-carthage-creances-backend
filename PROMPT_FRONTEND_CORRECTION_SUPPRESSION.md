# Prompt Frontend - Correction de la Suppression d'Enquête

## Contexte des Modifications Backend

Le backend a été amélioré pour :
1. **Retourner des messages d'erreur détaillés** dans le body de la réponse (au lieu de juste des codes HTTP)
2. **Vérifier que la suppression a bien eu lieu** avant de retourner un succès
3. **Gérer les contraintes de base de données** avec des messages explicites
4. **Logger toutes les erreurs** pour faciliter le débogage

### Nouveaux Codes de Réponse

- **204 NO_CONTENT** : Suppression réussie (comme avant)
- **409 CONFLICT** : Contrainte de base de données empêche la suppression (nouveau)
- **404 NOT_FOUND** : Enquête non trouvée
- **500 INTERNAL_SERVER_ERROR** : Erreur serveur avec message détaillé

### Format des Réponses d'Erreur

Le backend retourne maintenant des messages dans le body :

```json
// Pour 409 CONFLICT
"Impossible de supprimer l'enquête : une contrainte de base de données empêche la suppression. L'enquête est probablement liée à un dossier ou à d'autres entités."

// Pour 404 NOT_FOUND
"Enquête non trouvée ou erreur lors de la suppression : [message détaillé]"

// Pour 500 INTERNAL_SERVER_ERROR
"Erreur serveur lors de la suppression de l'enquête : [message détaillé]"
```

---

## PROMPT 1 : Mettre à Jour le Service de Suppression

**Prompt à utiliser :**
```
Dans le service Angular qui gère les appels API pour les enquêtes (probablement enquete.service.ts), modifiez la méthode deleteEnquete pour gérer les nouvelles réponses d'erreur du backend.

AVANT (code actuel qui peut causer le problème) :
deleteEnquete(id: number): Observable<void> {
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${this.getToken()}`
  };
  
  return this.http.delete<void>(
    `${this.apiUrl}/enquettes/${id}`,
    { headers }
  );
}

APRÈS (code corrigé) :
deleteEnquete(id: number): Observable<string> {
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${this.getToken()}`
  };
  
  return this.http.delete(`${this.apiUrl}/enquettes/${id}`, {
    headers,
    observe: 'response', // Observer la réponse complète
    responseType: 'text' // Le backend peut retourner du texte dans le body
  }).pipe(
    map(response => {
      // Si 204 NO_CONTENT, suppression réussie
      if (response.status === 204) {
        return 'success';
      }
      // Sinon, extraire le message d'erreur du body
      return response.body || 'Erreur inconnue lors de la suppression';
    }),
    catchError(error => {
      // Gérer les erreurs HTTP
      let errorMessage = 'Erreur lors de la suppression de l\'enquête';
      
      if (error.error) {
        // Le backend retourne maintenant des messages dans error.error
        errorMessage = typeof error.error === 'string' 
          ? error.error 
          : error.error.message || errorMessage;
      } else if (error.status === 409) {
        errorMessage = 'Impossible de supprimer l\'enquête : contrainte de base de données';
      } else if (error.status === 404) {
        errorMessage = 'Enquête non trouvée';
      } else if (error.status === 500) {
        errorMessage = 'Erreur serveur lors de la suppression';
      }
      
      return throwError(() => new Error(errorMessage));
    })
  );
}

Note : Le type de retour change de Observable<void> à Observable<string> pour pouvoir retourner soit 'success' soit un message d'erreur.
```

---

## PROMPT 2 : Corriger le Composant de Suppression

**Prompt à utiliser :**
```
Dans le composant qui gère la suppression d'enquête (probablement enquetes-en-attente.component.ts ou list-enquete.component.ts), modifiez la méthode deleteEnquete pour :

1. NE PAS supprimer l'enquête de la liste locale immédiatement
2. Attendre la confirmation du backend
3. Afficher les messages d'erreur détaillés
4. Ne rafraîchir la liste que si la suppression réussit

AVANT (code problématique) :
deleteEnquete(id: number): void {
  // ❌ PROBLÈME : Suppression immédiate de la liste locale
  this.validations = this.validations.filter(v => v.enquete?.id !== id);
  
  this.enqueteService.deleteEnquete(id).subscribe({
    next: () => {
      this.snackBar.open('Enquête supprimée avec succès', 'Fermer', { duration: 3000 });
      this.loadEnquetesEnAttente(); // Rafraîchir
    },
    error: (error) => {
      // ❌ PROBLÈME : L'enquête a déjà été supprimée de la liste locale
      this.snackBar.open('Erreur lors de la suppression', 'Fermer', { duration: 3000 });
      this.loadEnquetesEnAttente(); // L'enquête réapparaît ici
    }
  });
}

APRÈS (code corrigé) :
deleteEnquete(id: number): void {
  // Afficher un indicateur de chargement
  const loadingSnackBar = this.snackBar.open('Suppression en cours...', '', {
    duration: 0 // Ne pas fermer automatiquement
  });
  
  this.enqueteService.deleteEnquete(id).subscribe({
    next: (result) => {
      loadingSnackBar.dismiss(); // Fermer l'indicateur de chargement
      
      if (result === 'success') {
        // ✅ Suppression réussie : afficher le message et rafraîchir
        this.snackBar.open(
          'Enquête supprimée avec succès. Les validations associées ont également été supprimées.', 
          'Fermer', 
          { 
            duration: 5000,
            panelClass: ['success-snackbar']
          }
        );
        
        // Rafraîchir la liste pour refléter la suppression
        this.loadEnquetesEnAttente();
      } else {
        // Le backend a retourné un message d'erreur
        this.snackBar.open(
          result, 
          'Fermer', 
          { 
            duration: 7000,
            panelClass: ['error-snackbar']
          }
        );
        // Ne pas rafraîchir, l'enquête est toujours là
      }
    },
    error: (error) => {
      loadingSnackBar.dismiss(); // Fermer l'indicateur de chargement
      
      // Extraire le message d'erreur
      let errorMessage = 'Erreur lors de la suppression de l\'enquête';
      
      if (error.message) {
        errorMessage = error.message;
      } else if (error.error) {
        errorMessage = typeof error.error === 'string' 
          ? error.error 
          : error.error.message || errorMessage;
      }
      
      // Afficher le message d'erreur détaillé
      this.snackBar.open(
        errorMessage, 
        'Fermer', 
        { 
          duration: 7000,
          panelClass: ['error-snackbar']
        }
      );
      
      // Ne pas rafraîchir la liste, l'enquête est toujours présente
      // Optionnel : recharger pour s'assurer que l'état est à jour
      // this.loadEnquetesEnAttente();
    }
  });
}
```

---

## PROMPT 3 : Améliorer le Dialogue de Confirmation

**Prompt à utiliser :**
```
Dans le composant de dialogue de confirmation de suppression (probablement confirm-delete-dialog.component.ts), ajoutez un indicateur de chargement pendant la suppression.

Modifiez la méthode confirmDeleteEnquete dans le composant parent :

confirmDeleteEnquete(validation: ValidationEnquete): void {
  const dialogRef = this.dialog.open(ConfirmDeleteDialogComponent, {
    width: '400px',
    data: {
      title: 'Supprimer l\'enquête',
      message: `Êtes-vous sûr de vouloir supprimer l'enquête ${validation.enquete?.rapportCode} ?`,
      details: 'Cette action supprimera également toutes les validations associées. Cette action est irréversible.'
    },
    disableClose: true // Empêcher la fermeture pendant la suppression
  });

  dialogRef.afterClosed().subscribe(confirmed => {
    if (confirmed && validation.enquete?.id) {
      // Afficher un indicateur de chargement
      const loadingDialog = this.dialog.open(LoadingDialogComponent, {
        width: '300px',
        data: { message: 'Suppression en cours...' },
        disableClose: true
      });
      
      this.deleteEnquete(validation.enquete.id, loadingDialog);
    }
  });
}

deleteEnquete(id: number, loadingDialog?: MatDialogRef<any>): void {
  this.enqueteService.deleteEnquete(id).subscribe({
    next: (result) => {
      if (loadingDialog) {
        loadingDialog.close();
      }
      
      if (result === 'success') {
        this.snackBar.open(
          'Enquête supprimée avec succès. Les validations associées ont également été supprimées.', 
          'Fermer', 
          { duration: 5000 }
        );
        this.loadEnquetesEnAttente();
      } else {
        this.snackBar.open(result, 'Fermer', { duration: 7000 });
      }
    },
    error: (error) => {
      if (loadingDialog) {
        loadingDialog.close();
      }
      
      const errorMessage = error.message || 'Erreur lors de la suppression de l\'enquête';
      this.snackBar.open(errorMessage, 'Fermer', { duration: 7000 });
    }
  });
}
```

---

## PROMPT 4 : Créer un Composant de Chargement

**Prompt à utiliser :**
```
Créez un composant de dialogue de chargement réutilisable (loading-dialog.component.ts) :

@Component({
  selector: 'app-loading-dialog',
  template: `
    <div style="display: flex; flex-direction: column; align-items: center; padding: 20px;">
      <mat-spinner diameter="50"></mat-spinner>
      <p style="margin-top: 20px;">{{ data.message || 'Chargement...' }}</p>
    </div>
  `
})
export class LoadingDialogComponent {
  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { message?: string }
  ) {}
}

N'oubliez pas de déclarer ce composant dans votre module Angular.
```

---

## PROMPT 5 : Gérer les Erreurs dans le Service avec RxJS

**Prompt à utiliser :**
```
Si vous préférez une approche plus élégante avec RxJS, voici une version améliorée du service :

deleteEnquete(id: number): Observable<{ success: boolean; message: string }> {
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${this.getToken()}`
  };
  
  return this.http.delete(`${this.apiUrl}/enquettes/${id}`, {
    headers,
    observe: 'response',
    responseType: 'text'
  }).pipe(
    map(response => {
      if (response.status === 204) {
        return { success: true, message: 'Enquête supprimée avec succès' };
      }
      return { 
        success: false, 
        message: response.body || 'Erreur lors de la suppression' 
      };
    }),
    catchError(error => {
      let errorMessage = 'Erreur lors de la suppression de l\'enquête';
      
      if (error.error) {
        errorMessage = typeof error.error === 'string' 
          ? error.error 
          : error.error.message || errorMessage;
      } else if (error.status === 409) {
        errorMessage = 'Impossible de supprimer l\'enquête : contrainte de base de données';
      } else if (error.status === 404) {
        errorMessage = 'Enquête non trouvée';
      } else if (error.status === 500) {
        errorMessage = 'Erreur serveur lors de la suppression';
      } else if (error.status === 0) {
        errorMessage = 'Erreur de connexion. Vérifiez votre connexion internet.';
      }
      
      return throwError(() => ({ success: false, message: errorMessage }));
    })
  );
}

Utilisation dans le composant :
this.enqueteService.deleteEnquete(id).subscribe({
  next: (result) => {
    if (result.success) {
      this.snackBar.open(result.message, 'Fermer', { duration: 5000 });
      this.loadEnquetesEnAttente();
    } else {
      this.snackBar.open(result.message, 'Fermer', { duration: 7000 });
    }
  },
  error: (error) => {
    const message = error.message || 'Erreur lors de la suppression';
    this.snackBar.open(message, 'Fermer', { duration: 7000 });
  }
});
```

---

## PROMPT 6 : Ajouter des Styles pour les Snackbars

**Prompt à utiliser :**
```
Dans votre fichier de styles global (styles.scss ou styles.css), ajoutez les classes pour les snackbars :

.success-snackbar {
  background-color: #4caf50 !important;
  color: white !important;
}

.error-snackbar {
  background-color: #f44336 !important;
  color: white !important;
}

.warning-snackbar {
  background-color: #ff9800 !important;
  color: white !important;
}

Utilisation :
this.snackBar.open('Message', 'Fermer', {
  duration: 5000,
  panelClass: ['success-snackbar'] // ou 'error-snackbar', 'warning-snackbar'
});
```

---

## PROMPT 7 : Gérer les Erreurs de Réseau

**Prompt à utiliser :**
```
Améliorez la gestion des erreurs pour inclure les cas de perte de connexion :

deleteEnquete(id: number): Observable<string> {
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${this.getToken()}`
  };
  
  return this.http.delete(`${this.apiUrl}/enquettes/${id}`, {
    headers,
    observe: 'response',
    responseType: 'text',
    timeout: 30000 // Timeout de 30 secondes
  }).pipe(
    map(response => {
      if (response.status === 204) {
        return 'success';
      }
      return response.body || 'Erreur lors de la suppression';
    }),
    catchError(error => {
      let errorMessage = 'Erreur lors de la suppression de l\'enquête';
      
      // Erreur de connexion
      if (error.status === 0 || error.name === 'TimeoutError') {
        errorMessage = 'Erreur de connexion. Vérifiez votre connexion internet et réessayez.';
      }
      // Erreur avec message du backend
      else if (error.error) {
        errorMessage = typeof error.error === 'string' 
          ? error.error 
          : error.error.message || errorMessage;
      }
      // Erreurs HTTP spécifiques
      else if (error.status === 409) {
        errorMessage = 'Impossible de supprimer l\'enquête : contrainte de base de données';
      } else if (error.status === 404) {
        errorMessage = 'Enquête non trouvée';
      } else if (error.status === 500) {
        errorMessage = 'Erreur serveur. Veuillez réessayer plus tard.';
      } else if (error.status === 403) {
        errorMessage = 'Vous n\'avez pas les droits pour supprimer cette enquête.';
      }
      
      return throwError(() => new Error(errorMessage));
    })
  );
}
```

---

## PROMPT 8 : Améliorer l'UX avec un État de Chargement

**Prompt à utiliser :**
```
Dans le composant, ajoutez un état de chargement pour désactiver les boutons pendant la suppression :

export class EnquetesEnAttenteComponent {
  validations: ValidationEnquete[] = [];
  loading = false;
  deletingIds = new Set<number>(); // IDs des enquêtes en cours de suppression

  deleteEnquete(id: number): void {
    // Ajouter l'ID à la liste des suppressions en cours
    this.deletingIds.add(id);
    
    this.enqueteService.deleteEnquete(id).subscribe({
      next: (result) => {
        this.deletingIds.delete(id); // Retirer de la liste
        
        if (result === 'success') {
          this.snackBar.open('Enquête supprimée avec succès', 'Fermer', { duration: 5000 });
          this.loadEnquetesEnAttente();
        } else {
          this.snackBar.open(result, 'Fermer', { duration: 7000 });
        }
      },
      error: (error) => {
        this.deletingIds.delete(id); // Retirer de la liste même en cas d'erreur
        
        const errorMessage = error.message || 'Erreur lors de la suppression';
        this.snackBar.open(errorMessage, 'Fermer', { duration: 7000 });
      }
    });
  }

  isDeleting(id: number): boolean {
    return this.deletingIds.has(id);
  }
}

Dans le template :
<button 
  mat-icon-button 
  color="warn"
  (click)="confirmDeleteEnquete(validation)"
  [disabled]="isDeleting(validation.enquete?.id)"
  [matTooltip]="'Supprimer l\'enquête'">
  <mat-icon *ngIf="!isDeleting(validation.enquete?.id)">delete</mat-icon>
  <mat-spinner *ngIf="isDeleting(validation.enquete?.id)" diameter="20"></mat-spinner>
</button>
```

---

## Résumé des Modifications Frontend

### Points Clés à Retenir

1. **NE PAS supprimer immédiatement de la liste locale** : Attendre la confirmation du backend
2. **Gérer les messages d'erreur du backend** : Le backend retourne maintenant des messages détaillés
3. **Vérifier le statut de la réponse** : 204 = succès, autres = erreur avec message
4. **Afficher les messages d'erreur** : Utiliser les messages retournés par le backend
5. **Ne rafraîchir que si succès** : Si erreur, ne pas rafraîchir (l'enquête est toujours là)

### Fichiers à Modifier

1. **enquete.service.ts** : Modifier `deleteEnquete()` pour gérer les nouvelles réponses
2. **enquetes-en-attente.component.ts** : Modifier `deleteEnquete()` pour ne pas supprimer immédiatement
3. **confirm-delete-dialog.component.ts** : Améliorer l'UX avec indicateur de chargement
4. **styles.scss** : Ajouter les styles pour les snackbars

### Codes de Statut à Gérer

- **204** : Suppression réussie ✅
- **409** : Contrainte de base de données (afficher message)
- **404** : Enquête non trouvée
- **500** : Erreur serveur (afficher message)
- **0** : Erreur de connexion

### Test à Effectuer

1. ✅ Supprimer une enquête et vérifier qu'elle ne disparaît pas immédiatement
2. ✅ Vérifier que le message de succès s'affiche seulement si la suppression réussit
3. ✅ Vérifier que les messages d'erreur s'affichent correctement
4. ✅ Vérifier que l'enquête ne réapparaît pas après une erreur
5. ✅ Vérifier que l'enquête disparaît définitivement après un succès

