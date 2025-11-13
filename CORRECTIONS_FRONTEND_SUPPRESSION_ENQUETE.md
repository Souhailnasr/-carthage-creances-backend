# Corrections Frontend - Suppression d'Enquête

## Problème Actuel

L'enquête disparaît de l'interface puis réapparaît, car :
1. Le frontend supprime l'enquête de la liste **avant** la confirmation du backend
2. Le backend peut retourner une erreur (404, 409, 500) avec un message détaillé
3. Le frontend ne gère pas correctement ces erreurs

## Corrections à Appliquer

---

## CORRECTION 1 : Service - Méthode deleteEnquete()

**Fichier** : `src/app/services/enquete.service.ts` (ou similaire)

**AVANT** (code problématique) :
```typescript
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
```

**APRÈS** (code corrigé) :
```typescript
deleteEnquete(id: number): Observable<{ success: boolean; message: string }> {
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
        return { success: true, message: 'Enquête supprimée avec succès' };
      }
      // Sinon, extraire le message d'erreur du body
      return { 
        success: false, 
        message: response.body || 'Erreur lors de la suppression' 
      };
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
      } else if (error.status === 0) {
        errorMessage = 'Erreur de connexion. Vérifiez votre connexion internet.';
      }
      
      return throwError(() => ({ success: false, message: errorMessage }));
    })
  );
}
```

**Imports nécessaires** :
```typescript
import { Observable, throwError } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
```

---

## CORRECTION 2 : Composant - Méthode deleteEnquete()

**Fichier** : `src/app/components/enquetes-en-attente/enquetes-en-attente.component.ts` (ou similaire)

**AVANT** (code problématique) :
```typescript
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
```

**APRÈS** (code corrigé) :
```typescript
deleteEnquete(id: number): void {
  // Afficher un indicateur de chargement
  const loadingSnackBar = this.snackBar.open('Suppression en cours...', '', {
    duration: 0 // Ne pas fermer automatiquement
  });
  
  // ✅ NE PAS supprimer de la liste locale immédiatement
  // Attendre la confirmation du backend
  
  this.enqueteService.deleteEnquete(id).subscribe({
    next: (result) => {
      loadingSnackBar.dismiss(); // Fermer l'indicateur de chargement
      
      if (result.success) {
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
          result.message, 
          'Fermer', 
          { 
            duration: 7000,
            panelClass: ['error-snackbar']
          }
        );
        // ✅ Ne pas rafraîchir, l'enquête est toujours là
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
      
      // ✅ Ne pas rafraîchir la liste, l'enquête est toujours présente
      // Optionnel : recharger pour s'assurer que l'état est à jour
      // this.loadEnquetesEnAttente();
    }
  });
}
```

---

## CORRECTION 3 : Méthode confirmDeleteEnquete()

**Fichier** : `src/app/components/enquetes-en-attente/enquetes-en-attente.component.ts`

**AVANT** :
```typescript
confirmDeleteEnquete(validation: ValidationEnquete): void {
  const dialogRef = this.dialog.open(ConfirmDeleteDialogComponent, {
    width: '400px',
    data: {
      title: 'Supprimer l\'enquête',
      message: `Êtes-vous sûr de vouloir supprimer l'enquête ${validation.enquete?.rapportCode} ?`
    }
  });

  dialogRef.afterClosed().subscribe(confirmed => {
    if (confirmed && validation.enquete?.id) {
      this.deleteEnquete(validation.enquete.id);
    }
  });
}
```

**APRÈS** (amélioré) :
```typescript
confirmDeleteEnquete(validation: ValidationEnquete): void {
  const validationCount = validation.enquete?.validations?.length || 0;
  const validationInfo = validationCount > 0 
    ? ` Cette enquête a ${validationCount} validation(s) associée(s) qui seront également supprimée(s).`
    : '';
  
  const dialogRef = this.dialog.open(ConfirmDeleteDialogComponent, {
    width: '400px',
    data: {
      title: 'Supprimer l\'enquête',
      message: `Êtes-vous sûr de vouloir supprimer l'enquête ${validation.enquete?.rapportCode} ?`,
      details: `${validationInfo} Cette action est irréversible.`
    },
    disableClose: true // Empêcher la fermeture pendant la suppression
  });

  dialogRef.afterClosed().subscribe(confirmed => {
    if (confirmed && validation.enquete?.id) {
      this.deleteEnquete(validation.enquete.id);
    }
  });
}
```

---

## CORRECTION 4 : Ajouter un État de Chargement

**Fichier** : `src/app/components/enquetes-en-attente/enquetes-en-attente.component.ts`

**Ajouter dans la classe** :
```typescript
export class EnquetesEnAttenteComponent {
  validations: ValidationEnquete[] = [];
  loading = false;
  deletingIds = new Set<number>(); // ✅ Nouveau : IDs des enquêtes en cours de suppression

  // ... autres propriétés ...

  deleteEnquete(id: number): void {
    // ✅ Ajouter l'ID à la liste des suppressions en cours
    this.deletingIds.add(id);
    
    const loadingSnackBar = this.snackBar.open('Suppression en cours...', '', {
      duration: 0
    });
    
    this.enqueteService.deleteEnquete(id).subscribe({
      next: (result) => {
        this.deletingIds.delete(id); // ✅ Retirer de la liste
        loadingSnackBar.dismiss();
        
        if (result.success) {
          this.snackBar.open('Enquête supprimée avec succès', 'Fermer', { duration: 5000 });
          this.loadEnquetesEnAttente();
        } else {
          this.snackBar.open(result.message, 'Fermer', { duration: 7000 });
        }
      },
      error: (error) => {
        this.deletingIds.delete(id); // ✅ Retirer même en cas d'erreur
        loadingSnackBar.dismiss();
        
        const errorMessage = error.message || 'Erreur lors de la suppression';
        this.snackBar.open(errorMessage, 'Fermer', { duration: 7000 });
      }
    });
  }

  // ✅ Nouvelle méthode pour vérifier si une enquête est en cours de suppression
  isDeleting(id: number | undefined): boolean {
    return id !== undefined && this.deletingIds.has(id);
  }
}
```

**Dans le template** (`enquetes-en-attente.component.html`) :
```html
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

## CORRECTION 5 : Styles pour les Snackbars

**Fichier** : `src/styles.scss` (ou `styles.css`)

**Ajouter** :
```scss
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
```

---

## CORRECTION 6 : Gestion des Erreurs dans le Service (Version Alternative)

Si vous préférez une approche plus simple, voici une version alternative :

**Fichier** : `src/app/services/enquete.service.ts`

```typescript
deleteEnquete(id: number): Observable<string> {
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
        return 'success';
      }
      return response.body || 'Erreur lors de la suppression';
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
      }
      
      return throwError(() => new Error(errorMessage));
    })
  );
}
```

**Utilisation dans le composant** :
```typescript
this.enqueteService.deleteEnquete(id).subscribe({
  next: (result) => {
    if (result === 'success') {
      this.snackBar.open('Enquête supprimée avec succès', 'Fermer', { duration: 5000 });
      this.loadEnquetesEnAttente();
    } else {
      this.snackBar.open(result, 'Fermer', { duration: 7000 });
    }
  },
  error: (error) => {
    this.snackBar.open(error.message, 'Fermer', { duration: 7000 });
  }
});
```

---

## Checklist des Corrections

### Fichiers à Modifier

- [ ] `src/app/services/enquete.service.ts` - Modifier `deleteEnquete()`
- [ ] `src/app/components/enquetes-en-attente/enquetes-en-attente.component.ts` - Modifier `deleteEnquete()` et `confirmDeleteEnquete()`
- [ ] `src/app/components/enquetes-en-attente/enquetes-en-attente.component.html` - Ajouter indicateur de chargement
- [ ] `src/styles.scss` - Ajouter styles pour snackbars

### Points Clés

- [ ] ✅ **NE PAS supprimer immédiatement** de la liste locale
- [ ] ✅ **Attendre la confirmation** du backend (204 = succès)
- [ ] ✅ **Afficher les messages d'erreur** retournés par le backend
- [ ] ✅ **Ne rafraîchir que si succès** (si erreur, l'enquête est toujours là)
- [ ] ✅ **Gérer tous les codes d'erreur** (404, 409, 500, 0)
- [ ] ✅ **Afficher un indicateur de chargement** pendant la suppression

---

## Résumé des Changements

### Avant (Problématique)
1. ❌ Suppression immédiate de la liste locale
2. ❌ Pas de gestion des messages d'erreur du backend
3. ❌ Rafraîchissement même en cas d'erreur
4. ❌ Pas d'indicateur de chargement

### Après (Corrigé)
1. ✅ Attente de la confirmation du backend
2. ✅ Gestion des messages d'erreur détaillés
3. ✅ Rafraîchissement seulement si succès
4. ✅ Indicateur de chargement pendant la suppression
5. ✅ Désactivation du bouton pendant la suppression

---

## Test après Corrections

1. ✅ Cliquer sur supprimer → L'enquête ne disparaît pas immédiatement
2. ✅ Voir l'indicateur de chargement
3. ✅ Si succès → Message de succès + enquête disparaît
4. ✅ Si erreur → Message d'erreur détaillé + enquête reste visible
5. ✅ Vérifier que l'enquête ne réapparaît pas après une erreur

---

## Exemple Complet de Code

### Service (Version Simple)
```typescript
deleteEnquete(id: number): Observable<string> {
  const headers = {
    'Authorization': `Bearer ${this.getToken()}`
  };
  
  return this.http.delete(`${this.apiUrl}/enquettes/${id}`, {
    headers,
    observe: 'response',
    responseType: 'text'
  }).pipe(
    map(response => response.status === 204 ? 'success' : (response.body || 'Erreur')),
    catchError(error => {
      const msg = error.error || 
        (error.status === 409 ? 'Contrainte de base de données' :
         error.status === 404 ? 'Enquête non trouvée' :
         error.status === 500 ? 'Erreur serveur' : 'Erreur de connexion');
      return throwError(() => new Error(msg));
    })
  );
}
```

### Composant (Version Simple)
```typescript
deleteEnquete(id: number): void {
  const loading = this.snackBar.open('Suppression...', '', { duration: 0 });
  
  this.enqueteService.deleteEnquete(id).subscribe({
    next: (result) => {
      loading.dismiss();
      if (result === 'success') {
        this.snackBar.open('Supprimée avec succès', 'Fermer', { duration: 5000 });
        this.loadEnquetesEnAttente();
      } else {
        this.snackBar.open(result, 'Fermer', { duration: 7000 });
      }
    },
    error: (error) => {
      loading.dismiss();
      this.snackBar.open(error.message, 'Fermer', { duration: 7000 });
    }
  });
}
```

Ces corrections garantissent que l'enquête ne disparaîtra pas de l'interface avant la confirmation du backend, et que les erreurs seront correctement affichées à l'utilisateur.


