# Prompt pour la gestion de la suppression des enquêtes dans le frontend

## Contexte
Le backend a été amélioré pour permettre la suppression d'enquêtes même si la validation n'est pas effectuée. Lorsqu'une enquête est supprimée, toutes les validations associées sont automatiquement supprimées par le backend.

## Modifications backend effectuées

1. **Suppression automatique des validations** : Lors de la suppression d'une enquête, toutes les validations associées (ValidationEnquete) sont automatiquement supprimées, quel que soit leur statut (EN_ATTENTE, VALIDE, REJETE).

2. **Pas de restriction de validation** : Un agent peut maintenant supprimer une enquête même si elle n'a pas été validée ou si elle est en attente de validation.

## API Endpoint disponible

### DELETE /api/enquettes/{id}

**Description** : Supprime une enquête et toutes ses validations associées automatiquement.

**Méthode** : `DELETE`

**Paramètres** :
- `id` (path) : L'ID de l'enquête à supprimer

**Headers requis** :
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Réponses** :
- `204 NO_CONTENT` : Enquête supprimée avec succès (les validations sont supprimées automatiquement)
- `404 NOT_FOUND` : Enquête non trouvée
- `500 INTERNAL_SERVER_ERROR` : Erreur serveur

**Exemple de requête** :
```http
DELETE /api/enquettes/123
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Comportement backend** :
1. Vérifie que l'enquête existe
2. Récupère toutes les validations associées à l'enquête
3. Supprime toutes les validations (peu importe leur statut)
4. Supprime l'enquête

## Prompt 1 : Ajouter la méthode de suppression dans le service Angular

**Prompt à utiliser :**
```
Dans le service Angular qui gère les appels API pour les enquêtes (probablement enquete.service.ts), ajoutez ou modifiez la méthode deleteEnquete pour gérer correctement la suppression :

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

Cette méthode :
- Envoie une requête DELETE à /api/enquettes/{id}
- Retourne un Observable<void> car le backend retourne 204 NO_CONTENT
- Gère automatiquement la suppression des validations (fait par le backend)
- Inclut le token d'authentification dans les headers
```

## Prompt 2 : Implémenter la suppression dans le composant de liste

**Prompt à utiliser :**
```
Dans le composant Angular qui affiche la liste des enquêtes (probablement list-enquete.component.ts), ajoutez une méthode pour supprimer une enquête avec confirmation :

1. Ajoutez un bouton de suppression dans le template (icône poubelle) :
<button 
  mat-icon-button 
  color="warn" 
  (click)="confirmDeleteEnquete(enquete)"
  [matTooltip]="'Supprimer l\'enquête'"
  *ngIf="canDeleteEnquete(enquete)">
  <mat-icon>delete</mat-icon>
</button>

2. Implémentez la méthode de confirmation et de suppression :
confirmDeleteEnquete(enquete: Enquette): void {
  const dialogRef = this.dialog.open(ConfirmDeleteDialogComponent, {
    width: '400px',
    data: {
      title: 'Supprimer l\'enquête',
      message: `Êtes-vous sûr de vouloir supprimer l'enquête ${enquete.rapportCode} ?`,
      details: 'Cette action supprimera également toutes les validations associées. Cette action est irréversible.'
    }
  });

  dialogRef.afterClosed().subscribe(confirmed => {
    if (confirmed) {
      this.deleteEnquete(enquete.id);
    }
  });
}

deleteEnquete(id: number): void {
  this.enqueteService.deleteEnquete(id).subscribe({
    next: () => {
      this.snackBar.open('Enquête supprimée avec succès', 'Fermer', { duration: 3000 });
      // Rafraîchir la liste
      this.loadEnquetes();
    },
    error: (error) => {
      if (error.status === 404) {
        this.snackBar.open('Enquête non trouvée', 'Fermer', { duration: 3000 });
      } else {
        this.snackBar.open('Erreur lors de la suppression', 'Fermer', { duration: 3000 });
      }
      console.error('Erreur suppression enquête:', error);
    }
  });
}

3. Implémentez la méthode canDeleteEnquete pour déterminer qui peut supprimer :
canDeleteEnquete(enquete: Enquette): boolean {
  const currentUser = this.authService.getCurrentUser();
  if (!currentUser) return false;
  
  // L'agent créateur peut supprimer son enquête
  if (enquete.agentCreateur?.id === currentUser.id) {
    return true;
  }
  
  // Les chefs peuvent supprimer n'importe quelle enquête
  const isChef = currentUser.roleUtilisateur?.startsWith('CHEF_DEPARTEMENT') || 
                 currentUser.roleUtilisateur === 'SUPER_ADMIN';
  return isChef;
}

Note importante : La suppression est maintenant possible même si l'enquête n'est pas validée ou est en attente de validation.
```

## Prompt 3 : Implémenter la suppression dans le composant de détails

**Prompt à utiliser :**
```
Dans le composant Angular qui affiche les détails d'une enquête (probablement detail-enquete.component.ts), ajoutez un bouton de suppression :

1. Ajoutez le bouton dans le template :
<button 
  mat-raised-button 
  color="warn" 
  (click)="confirmDeleteEnquete()"
  *ngIf="canDeleteEnquete()"
  style="margin-left: 10px;">
  <mat-icon>delete</mat-icon>
  Supprimer l'enquête
</button>

2. Implémentez les méthodes dans le composant :
canDeleteEnquete(): boolean {
  const currentUser = this.authService.getCurrentUser();
  if (!currentUser || !this.enquete) return false;
  
  // L'agent créateur peut supprimer son enquête
  if (this.enquete.agentCreateur?.id === currentUser.id) {
    return true;
  }
  
  // Les chefs peuvent supprimer n'importe quelle enquête
  const isChef = currentUser.roleUtilisateur?.startsWith('CHEF_DEPARTEMENT') || 
                 currentUser.roleUtilisateur === 'SUPER_ADMIN';
  return isChef;
}

confirmDeleteEnquete(): void {
  const dialogRef = this.dialog.open(ConfirmDeleteDialogComponent, {
    width: '400px',
    data: {
      title: 'Supprimer l\'enquête',
      message: `Êtes-vous sûr de vouloir supprimer l'enquête ${this.enquete.rapportCode} ?`,
      details: 'Cette action supprimera également toutes les validations associées. Cette action est irréversible.'
    }
  });

  dialogRef.afterClosed().subscribe(confirmed => {
    if (confirmed) {
      this.deleteEnquete();
    }
  });
}

deleteEnquete(): void {
  this.enqueteService.deleteEnquete(this.enquete.id).subscribe({
    next: () => {
      this.snackBar.open('Enquête supprimée avec succès', 'Fermer', { duration: 3000 });
      // Rediriger vers la liste des enquêtes
      this.router.navigate(['/enquetes']);
    },
    error: (error) => {
      if (error.status === 404) {
        this.snackBar.open('Enquête non trouvée', 'Fermer', { duration: 3000 });
      } else {
        this.snackBar.open('Erreur lors de la suppression', 'Fermer', { duration: 3000 });
      }
      console.error('Erreur suppression enquête:', error);
    }
  });
}
```

## Prompt 4 : Créer un composant de dialogue de confirmation de suppression

**Prompt à utiliser :**
```
Créez un composant de dialogue Angular Material réutilisable pour confirmer la suppression (confirm-delete-dialog.component.ts) :

@Component({
  selector: 'app-confirm-delete-dialog',
  template: `
    <h2 mat-dialog-title>{{ data.title || 'Confirmer la suppression' }}</h2>
    <mat-dialog-content>
      <p>{{ data.message }}</p>
      <p *ngIf="data.details" class="details">{{ data.details }}</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Annuler</button>
      <button mat-raised-button color="warn" (click)="onConfirm()">
        Supprimer
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .details {
      color: #666;
      font-size: 0.9em;
      margin-top: 10px;
    }
  `]
})
export class ConfirmDeleteDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ConfirmDeleteDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {
      title?: string;
      message: string;
      details?: string;
    }
  ) {}

  onCancel(): void {
    this.dialogRef.close(false);
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }
}

N'oubliez pas de déclarer ce composant dans le module Angular approprié.
```

## Prompt 5 : Gérer l'affichage des validations avant suppression

**Prompt à utiliser :**
```
Dans le composant de détails d'enquête, affichez les informations sur les validations associées avant la suppression :

1. Affichez les validations associées dans le template :
<mat-card *ngIf="validations && validations.length > 0">
  <mat-card-header>
    <mat-card-title>Validations associées</mat-card-title>
  </mat-card-header>
  <mat-card-content>
    <p>Cette enquête a {{ validations.length }} validation(s) associée(s) qui seront supprimées :</p>
    <mat-list>
      <mat-list-item *ngFor="let validation of validations">
        <mat-icon matListItemIcon>
          {{ getValidationIcon(validation.statut) }}
        </mat-icon>
        <div matListItemTitle>Statut: {{ getStatutLabel(validation.statut) }}</div>
        <div matListItemLine *ngIf="validation.dateCreation">
          Créée le: {{ validation.dateCreation | date:'dd/MM/yyyy HH:mm' }}
        </div>
      </mat-list-item>
    </mat-list>
  </mat-card-content>
</mat-card>

2. Chargez les validations dans le composant :
ngOnInit(): void {
  // ... code existant ...
  this.loadValidations();
}

loadValidations(): void {
  if (this.enquete?.id) {
    this.validationEnqueteService.getValidationsByEnquete(this.enquete.id)
      .subscribe({
        next: (validations) => {
          this.validations = validations;
        },
        error: (error) => {
          console.error('Erreur chargement validations:', error);
        }
      });
  }
}

3. Ajoutez les méthodes helper :
getValidationIcon(statut: string): string {
  switch(statut) {
    case 'EN_ATTENTE': return 'schedule';
    case 'VALIDE': return 'check_circle';
    case 'REJETE': return 'cancel';
    default: return 'help';
  }
}

getStatutLabel(statut: string): string {
  const labels = {
    'EN_ATTENTE': 'En attente',
    'VALIDE': 'Validée',
    'REJETE': 'Rejetée'
  };
  return labels[statut] || statut;
}
```

## Prompt 6 : Mettre à jour le message de confirmation

**Prompt à utiliser :**
```
Mettez à jour le message de confirmation de suppression pour informer l'utilisateur que les validations seront également supprimées :

Dans la méthode confirmDeleteEnquete, modifiez le message :
confirmDeleteEnquete(enquete: Enquette): void {
  const validationCount = enquete.validations?.length || 0;
  const validationInfo = validationCount > 0 
    ? ` Cette enquête a ${validationCount} validation(s) associée(s) qui seront également supprimée(s).`
    : '';
  
  const dialogRef = this.dialog.open(ConfirmDeleteDialogComponent, {
    width: '400px',
    data: {
      title: 'Supprimer l\'enquête',
      message: `Êtes-vous sûr de vouloir supprimer l'enquête ${enquete.rapportCode} ?`,
      details: `${validationInfo} Cette action est irréversible.`
    }
  });

  dialogRef.afterClosed().subscribe(confirmed => {
    if (confirmed) {
      this.deleteEnquete(enquete.id);
    }
  });
}
```

## Prompt 7 : Gérer les erreurs de suppression

**Prompt à utiliser :**
```
Améliorez la gestion des erreurs lors de la suppression d'une enquête :

deleteEnquete(id: number): void {
  // Afficher un indicateur de chargement
  this.isDeleting = true;
  
  this.enqueteService.deleteEnquete(id).subscribe({
    next: () => {
      this.isDeleting = false;
      this.snackBar.open('Enquête supprimée avec succès. Les validations associées ont également été supprimées.', 'Fermer', { 
        duration: 5000,
        panelClass: ['success-snackbar']
      });
      // Rafraîchir la liste
      this.loadEnquetes();
    },
    error: (error) => {
      this.isDeleting = false;
      let errorMessage = 'Erreur lors de la suppression';
      
      if (error.status === 404) {
        errorMessage = 'Enquête non trouvée. Elle a peut-être déjà été supprimée.';
      } else if (error.status === 403) {
        errorMessage = 'Vous n\'avez pas les droits pour supprimer cette enquête.';
      } else if (error.status === 500) {
        errorMessage = 'Erreur serveur. Veuillez réessayer plus tard.';
      }
      
      this.snackBar.open(errorMessage, 'Fermer', { 
        duration: 5000,
        panelClass: ['error-snackbar']
      });
      console.error('Erreur suppression enquête:', error);
    }
  });
}
```

## Prompt 8 : Filtrer les enquêtes supprimables

**Prompt à utiliser :**
```
Dans le composant de liste, ajoutez un indicateur visuel pour les enquêtes qui peuvent être supprimées :

1. Ajoutez une méthode pour vérifier si une enquête peut être supprimée :
canDeleteEnquete(enquete: Enquette): boolean {
  const currentUser = this.authService.getCurrentUser();
  if (!currentUser) return false;
  
  // L'agent créateur peut supprimer son enquête (même si non validée)
  if (enquete.agentCreateur?.id === currentUser.id) {
    return true;
  }
  
  // Les chefs peuvent supprimer n'importe quelle enquête
  const isChef = currentUser.roleUtilisateur?.startsWith('CHEF_DEPARTEMENT') || 
                 currentUser.roleUtilisateur === 'SUPER_ADMIN';
  return isChef;
}

2. Utilisez cette méthode dans le template pour afficher/masquer le bouton de suppression :
<button 
  mat-icon-button 
  color="warn" 
  (click)="confirmDeleteEnquete(enquete)"
  [matTooltip]="'Supprimer l\'enquête'"
  *ngIf="canDeleteEnquete(enquete)">
  <mat-icon>delete</mat-icon>
</button>

Note : Contrairement à avant, la suppression est maintenant possible même si :
- L'enquête est en attente de validation
- L'enquête n'a pas été validée
- L'enquête a été rejetée
```

## Résumé des modifications nécessaires

1. **Service** : Ajouter/modifier la méthode `deleteEnquete()` dans le service enquête
2. **Liste** : Ajouter le bouton de suppression avec confirmation
3. **Détails** : Ajouter le bouton de suppression dans la vue détaillée
4. **Dialogue** : Créer un composant de confirmation réutilisable
5. **Validations** : Afficher les validations associées avant suppression
6. **Messages** : Informer l'utilisateur que les validations seront supprimées
7. **Erreurs** : Gérer correctement les erreurs de suppression
8. **Permissions** : Vérifier les droits de suppression (agent créateur ou chef)

## Fichiers à modifier/créer

- `src/app/services/enquete.service.ts` (méthode deleteEnquete)
- `src/app/components/list-enquete/list-enquete.component.ts` (bouton suppression, méthode deleteEnquete)
- `src/app/components/list-enquete/list-enquete.component.html` (bouton suppression)
- `src/app/components/detail-enquete/detail-enquete.component.ts` (bouton suppression, méthode deleteEnquete)
- `src/app/components/detail-enquete/detail-enquete.component.html` (bouton suppression)
- `src/app/components/confirm-delete-dialog/confirm-delete-dialog.component.ts` (nouveau composant)
- `src/app/components/confirm-delete-dialog/confirm-delete-dialog.component.html` (nouveau composant)

## Endpoints backend utilisés

- `DELETE /api/enquettes/{id}` - Supprime une enquête et toutes ses validations associées

## Workflow de suppression

1. **Agent ou Chef clique sur supprimer** → Dialogue de confirmation s'affiche
2. **Utilisateur confirme** → Requête DELETE envoyée au backend
3. **Backend supprime** :
   - Toutes les validations associées (peu importe leur statut)
   - L'enquête elle-même
4. **Frontend reçoit 204 NO_CONTENT** → Affiche message de succès et rafraîchit la liste
5. **En cas d'erreur** → Affiche message d'erreur approprié

## Règles de suppression

- ✅ Un agent peut supprimer ses propres enquêtes (même si non validées)
- ✅ Un chef peut supprimer n'importe quelle enquête
- ✅ La suppression est possible même si l'enquête est en attente de validation
- ✅ La suppression est possible même si l'enquête a été rejetée
- ✅ La suppression est possible même si l'enquête a été validée
- ✅ Toutes les validations associées sont automatiquement supprimées par le backend

## Tests à effectuer

1. ✅ Agent supprime sa propre enquête en attente de validation
2. ✅ Agent supprime sa propre enquête validée
3. ✅ Agent supprime sa propre enquête rejetée
4. ✅ Chef supprime une enquête d'un autre agent
5. ✅ Vérifier que les validations sont bien supprimées après suppression de l'enquête
6. ✅ Vérifier les messages d'erreur (404, 403, 500)
7. ✅ Vérifier que la liste se rafraîchit après suppression
8. ✅ Vérifier la redirection après suppression depuis la vue détaillée

## Notes importantes

- **Suppression automatique des validations** : Le backend supprime automatiquement toutes les validations associées, donc le frontend n'a pas besoin de faire d'appel API supplémentaire pour supprimer les validations.

- **Pas de restriction de validation** : Contrairement à certaines logiques métier, la suppression est maintenant possible indépendamment du statut de validation de l'enquête.

- **Transaction atomique** : Le backend utilise `@Transactional` pour garantir que soit tout est supprimé (enquête + validations), soit rien n'est supprimé en cas d'erreur.

- **Sécurité** : Assurez-vous que les vérifications de permissions sont effectuées côté backend également (pas seulement côté frontend).

