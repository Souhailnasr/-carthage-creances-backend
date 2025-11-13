# Corrections Frontend Finales - Suppression d'Enquête

## Problème Actuel

D'après les captures et les logs :
1. Le frontend envoie `DELETE /api/enquettes/8`
2. Le backend retourne `404 Not Found` avec le message : `"Enquête non trouvée ou erreur lors de la suppression: Enquette not found with id: 8"`
3. L'enquête existe bien dans la base de données (visible dans phpMyAdmin)
4. Le problème vient du backend qui ne peut pas charger l'enquête à cause de la relation `Dossier`

## Solution Backend Appliquée

Le backend a été corrigé pour utiliser des **requêtes natives** qui évitent le chargement des relations. La suppression devrait maintenant fonctionner.

## Corrections Frontend à Appliquer

Même si le backend est corrigé, le frontend doit être amélioré pour gérer correctement les erreurs et éviter que l'enquête disparaisse puis réapparaisse.

---

## CORRECTION 1 : Service - Méthode deleteEnquete()

**Fichier** : `src/app/services/enquete.service.ts`

**Code à remplacer** :
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

**Par ce code** :
```typescript
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
        errorMessage = error.error || 'Enquête non trouvée';
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

**Fichier** : `src/app/components/mes-validations-enquete/mes-validations-enquete.component.ts` (ou similaire)

**Code actuel (problématique)** :
```typescript
deleteEnquete(id: number): void {
  // ❌ PROBLÈME : Suppression immédiate de la liste
  this.validations = this.validations.filter(v => v.enquete?.id !== id);
  
  this.enqueteService.deleteEnquete(id).subscribe({
    next: () => {
      this.snackBar.open('Enquête supprimée avec succès', 'Fermer', { duration: 3000 });
      this.loadEnquetesEnAttente();
    },
    error: (error) => {
      // ❌ PROBLÈME : L'enquête a déjà été supprimée de la liste
      this.snackBar.open('Erreur lors de la suppression', 'Fermer', { duration: 3000 });
      this.loadEnquetesEnAttente(); // L'enquête réapparaît
    }
  });
}
```

**Code corrigé** :
```typescript
deleteEnquete(id: number): void {
  // ✅ Afficher un indicateur de chargement
  const loadingSnackBar = this.snackBar.open('Suppression en cours...', '', {
    duration: 0
  });
  
  // ✅ NE PAS supprimer de la liste locale immédiatement
  // Attendre la confirmation du backend
  
  this.enqueteService.deleteEnquete(id).subscribe({
    next: (result) => {
      loadingSnackBar.dismiss();
      
      if (result.success) {
        // ✅ Suppression réussie
        this.snackBar.open(
          'Enquête supprimée avec succès. Les validations associées ont également été supprimées.', 
          'Fermer', 
          { 
            duration: 5000,
            panelClass: ['success-snackbar']
          }
        );
        // Rafraîchir la liste
        this.loadEnquetesEnAttente();
      } else {
        // ✅ Erreur : afficher le message du backend
        this.snackBar.open(
          result.message, 
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
      loadingSnackBar.dismiss();
      
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
      
      // ✅ Ne pas rafraîchir, l'enquête est toujours présente
    }
  });
}
```

---

## CORRECTION 3 : Méthode confirmDeleteEnquete()

**Fichier** : `src/app/components/mes-validations-enquete/mes-validations-enquete.component.ts`

**Code corrigé** :
```typescript
confirmDeleteEnquete(validation: ValidationEnquete): void {
  if (!validation.enquete?.id) {
    this.snackBar.open('ID de l\'enquête manquant', 'Fermer', { duration: 3000 });
    return;
  }
  
  const validationCount = validation.enquete?.validations?.length || 0;
  const validationInfo = validationCount > 0 
    ? ` Cette enquête a ${validationCount} validation(s) associée(s) qui seront également supprimée(s).`
    : '';
  
  const dialogRef = this.dialog.open(ConfirmDeleteDialogComponent, {
    width: '400px',
    data: {
      title: 'Supprimer l\'enquête',
      message: `Êtes-vous sûr de vouloir supprimer l'enquête ${validation.enquete?.rapportCode || validation.enquete?.id} ?`,
      details: `${validationInfo} Cette action est irréversible.`
    },
    disableClose: true
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

**Dans la classe du composant** :
```typescript
export class MesValidationsEnqueteComponent {
  validations: ValidationEnquete[] = [];
  loading = false;
  deletingIds = new Set<number>(); // ✅ Nouveau

  deleteEnquete(id: number): void {
    this.deletingIds.add(id); // ✅ Ajouter à la liste
    
    const loadingSnackBar = this.snackBar.open('Suppression en cours...', '', {
      duration: 0
    });
    
    this.enqueteService.deleteEnquete(id).subscribe({
      next: (result) => {
        this.deletingIds.delete(id); // ✅ Retirer
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

  isDeleting(id: number | undefined): boolean {
    return id !== undefined && this.deletingIds.has(id);
  }
}
```

**Dans le template HTML** :
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

## CORRECTION 5 : Styles CSS

**Fichier** : `src/styles.scss`

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
```

---

## Version Simplifiée (Alternative)

Si vous préférez une version plus simple :

**Service** :
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
        (error.status === 404 ? 'Enquête non trouvée' :
         error.status === 409 ? 'Contrainte de base de données' :
         error.status === 500 ? 'Erreur serveur' : 'Erreur de connexion');
      return throwError(() => new Error(msg));
    })
  );
}
```

**Composant** :
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

---

## Checklist des Corrections

- [ ] Modifier `enquete.service.ts` - méthode `deleteEnquete()`
- [ ] Modifier le composant - méthode `deleteEnquete()`
- [ ] Modifier le composant - méthode `confirmDeleteEnquete()`
- [ ] Ajouter `deletingIds` dans le composant
- [ ] Modifier le template HTML - indicateur de chargement
- [ ] Ajouter les styles CSS pour les snackbars

---

## Points Clés

1. ✅ **NE PAS supprimer immédiatement** de la liste locale
2. ✅ **Attendre la confirmation** du backend (204 = succès)
3. ✅ **Afficher les messages d'erreur** retournés par le backend
4. ✅ **Ne rafraîchir que si succès** (si erreur, l'enquête est toujours là)
5. ✅ **Gérer tous les codes d'erreur** (404, 409, 500, 0)

---

## Test après Corrections

1. ✅ Cliquer sur supprimer → L'enquête ne disparaît pas immédiatement
2. ✅ Voir l'indicateur de chargement
3. ✅ Si succès → Message de succès + enquête disparaît
4. ✅ Si erreur → Message d'erreur détaillé + enquête reste visible
5. ✅ Vérifier que l'enquête ne réapparaît pas après une erreur

Ces corrections garantissent que l'enquête ne disparaîtra pas de l'interface avant la confirmation du backend, et que les erreurs seront correctement affichées à l'utilisateur.


