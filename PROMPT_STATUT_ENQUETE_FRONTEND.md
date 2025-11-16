# Prompts pour intégrer le système de statut et validation des enquêtes dans le frontend

## Contexte
Le backend a été amélioré pour que les enquêtes utilisent le même système de statut et de validation que les dossiers. Les enquêtes ont maintenant :
- Un champ `statut` (enum Statut : EN_ATTENTE_VALIDATION, VALIDE, REJETE, EN_COURS, CLOTURE)
- Une logique de validation automatique si créée par un chef
- Des endpoints pour valider/rejeter les enquêtes
- Des notifications automatiques lors de la validation/rejet

## Prompt 1 : Mettre à jour l'interface TypeScript Enquette

**Prompt à utiliser :**
```
Dans le projet Angular, localisez l'interface TypeScript qui définit le modèle Enquette (probablement dans un fichier models/enquette.ts ou similaire).

Ajoutez le champ statut à l'interface Enquette :

export interface Enquette {
  // ... champs existants ...
  
  // Statut de l'enquête (même logique que Dossier)
  statut?: 'EN_ATTENTE_VALIDATION' | 'VALIDE' | 'REJETE' | 'EN_COURS' | 'CLOTURE';
  
  // Propriétés de validation (déjà existantes, vérifier qu'elles sont présentes)
  valide?: boolean;
  dateValidation?: string; // ISO 8601 format
  commentaireValidation?: string;
  
  // ... autres champs ...
}
```

## Prompt 2 : Créer un enum Statut pour les enquêtes

**Prompt à utiliser :**
```
Dans le projet Angular, créez un fichier models/statut.ts (ou ajoutez dans un fichier models/enums.ts) avec l'enum Statut :

export enum Statut {
  EN_ATTENTE_VALIDATION = 'EN_ATTENTE_VALIDATION',
  VALIDE = 'VALIDE',
  REJETE = 'REJETE',
  EN_COURS = 'EN_COURS',
  CLOTURE = 'CLOTURE'
}

Si vous avez déjà un enum Statut pour les dossiers, réutilisez-le pour les enquêtes.
```

## Prompt 3 : Ajouter les méthodes de validation dans le service enquête

**Prompt à utiliser :**
```
Dans le service Angular qui gère les appels API pour les enquêtes (probablement enquete.service.ts), ajoutez les méthodes suivantes :

1. validerEnquete(id: number, chefId: number): Observable<Enquette>
   - PUT /api/enquettes/{id}/valider?chefId={chefId}
   - Headers: Authorization: Bearer {token}
   - Retourne: Enquette mise à jour avec statut VALIDE
   - Gérer les erreurs: 400 (droits insuffisants), 404, 500

2. rejeterEnquete(id: number, commentaire: string): Observable<Enquette>
   - PUT /api/enquettes/{id}/rejeter?commentaire={commentaire}
   - Headers: Authorization: Bearer {token}
   - Retourne: Enquête mise à jour avec statut REJETE
   - Gérer les erreurs: 400, 404, 500

Exemple d'implémentation :
validerEnquete(id: number, chefId: number): Observable<Enquette> {
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${this.getToken()}`
  };
  
  return this.http.put<Enquette>(
    `${this.apiUrl}/enquettes/${id}/valider?chefId=${chefId}`,
    null,
    { headers }
  );
}

rejeterEnquete(id: number, commentaire: string): Observable<Enquette> {
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${this.getToken()}`
  };
  
  const encodedCommentaire = encodeURIComponent(commentaire);
  return this.http.put<Enquette>(
    `${this.apiUrl}/enquettes/${id}/rejeter?commentaire=${encodedCommentaire}`,
    null,
    { headers }
  );
}
```

## Prompt 4 : Afficher le statut dans la liste des enquêtes

**Prompt à utiliser :**
```
Dans le composant Angular qui affiche la liste des enquêtes (probablement list-enquete.component.ts ou enquete-list.component.ts), ajoutez une colonne ou un badge pour afficher le statut de chaque enquête.

Utilisez des couleurs différentes selon le statut :
- EN_ATTENTE_VALIDATION : orange/jaune
- VALIDE : vert
- REJETE : rouge
- EN_COURS : bleu
- CLOTURE : gris

Exemple avec Angular Material :
<mat-chip [color]="getStatutColor(enquete.statut)">
  {{ getStatutLabel(enquete.statut) }}
</mat-chip>

Créez les méthodes helper :
getStatutColor(statut: string): string {
  switch(statut) {
    case 'EN_ATTENTE_VALIDATION': return 'warn';
    case 'VALIDE': return 'primary';
    case 'REJETE': return 'accent';
    case 'EN_COURS': return 'primary';
    case 'CLOTURE': return '';
    default: return '';
  }
}

getStatutLabel(statut: string): string {
  const labels = {
    'EN_ATTENTE_VALIDATION': 'En attente',
    'VALIDE': 'Validée',
    'REJETE': 'Rejetée',
    'EN_COURS': 'En cours',
    'CLOTURE': 'Clôturée'
  };
  return labels[statut] || statut;
}
```

## Prompt 5 : Ajouter les boutons de validation/rejet pour les chefs

**Prompt à utiliser :**
```
Dans le composant Angular qui affiche les détails d'une enquête (probablement detail-enquete.component.ts ou enquete-detail.component.ts), ajoutez des boutons pour valider/rejeter l'enquête, visibles uniquement pour les chefs.

Conditions d'affichage :
- L'enquête doit avoir le statut EN_ATTENTE_VALIDATION
- L'utilisateur connecté doit être un chef (CHEF_DEPARTEMENT_DOSSIER, etc.)
- L'utilisateur ne doit pas être le créateur de l'enquête

Exemple d'implémentation :
<button 
  mat-raised-button 
  color="primary" 
  *ngIf="canValidate()"
  (click)="validerEnquete()">
  Valider l'enquête
</button>

<button 
  mat-raised-button 
  color="warn" 
  *ngIf="canValidate()"
  (click)="openRejetDialog()">
  Rejeter l'enquête
</button>

Dans le composant :
canValidate(): boolean {
  const currentUser = this.authService.getCurrentUser();
  const isChef = currentUser?.roleUtilisateur?.startsWith('CHEF_DEPARTEMENT') || 
                 currentUser?.roleUtilisateur === 'SUPER_ADMIN';
  const isEnAttente = this.enquete?.statut === 'EN_ATTENTE_VALIDATION';
  const isNotCreator = this.enquete?.agentCreateur?.id !== currentUser?.id;
  
  return isChef && isEnAttente && isNotCreator;
}

validerEnquete(): void {
  const currentUser = this.authService.getCurrentUser();
  if (!currentUser?.id) return;
  
  this.enqueteService.validerEnquete(this.enquete.id, currentUser.id)
    .subscribe({
      next: (enquete) => {
        this.enquete = enquete;
        this.snackBar.open('Enquête validée avec succès', 'Fermer', { duration: 3000 });
        // Rafraîchir la liste si nécessaire
      },
      error: (error) => {
        this.snackBar.open('Erreur lors de la validation', 'Fermer', { duration: 3000 });
        console.error(error);
      }
    });
}

openRejetDialog(): void {
  const dialogRef = this.dialog.open(RejetEnqueteDialogComponent, {
    width: '500px',
    data: { enquete: this.enquete }
  });
  
  dialogRef.afterClosed().subscribe(commentaire => {
    if (commentaire) {
      this.rejeterEnquete(commentaire);
    }
  });
}

rejeterEnquete(commentaire: string): void {
  this.enqueteService.rejeterEnquete(this.enquete.id, commentaire)
    .subscribe({
      next: (enquete) => {
        this.enquete = enquete;
        this.snackBar.open('Enquête rejetée', 'Fermer', { duration: 3000 });
      },
      error: (error) => {
        this.snackBar.open('Erreur lors du rejet', 'Fermer', { duration: 3000 });
        console.error(error);
      }
    });
}
```

## Prompt 6 : Créer un dialogue de rejet

**Prompt à utiliser :**
```
Créez un composant de dialogue Angular Material pour le rejet d'enquête (rejet-enquete-dialog.component.ts) :

1. Le dialogue doit demander un commentaire obligatoire
2. Afficher les informations de l'enquête (rapportCode, etc.)
3. Valider que le commentaire n'est pas vide
4. Retourner le commentaire au composant parent

Exemple :
@Component({
  selector: 'app-rejet-enquete-dialog',
  template: `
    <h2 mat-dialog-title>Rejeter l'enquête</h2>
    <mat-dialog-content>
      <p>Êtes-vous sûr de vouloir rejeter l'enquête <strong>{{ data.enquete.rapportCode }}</strong> ?</p>
      <mat-form-field appearance="outline" class="full-width">
        <mat-label>Commentaire de rejet *</mat-label>
        <textarea matInput [(ngModel)]="commentaire" required minlength="10"></textarea>
        <mat-hint>Veuillez expliquer la raison du rejet (minimum 10 caractères)</mat-hint>
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Annuler</button>
      <button mat-raised-button color="warn" (click)="onReject()" [disabled]="!commentaire || commentaire.length < 10">
        Rejeter
      </button>
    </mat-dialog-actions>
  `
})
export class RejetEnqueteDialogComponent {
  commentaire: string = '';
  
  constructor(
    public dialogRef: MatDialogRef<RejetEnqueteDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { enquete: Enquette }
  ) {}
  
  onCancel(): void {
    this.dialogRef.close();
  }
  
  onReject(): void {
    if (this.commentaire && this.commentaire.length >= 10) {
      this.dialogRef.close(this.commentaire);
    }
  }
}
```

## Prompt 7 : Filtrer les enquêtes par statut

**Prompt à utiliser :**
```
Dans le composant de liste des enquêtes, ajoutez des filtres pour afficher les enquêtes selon leur statut.

Options de filtres :
- Toutes les enquêtes
- En attente de validation (pour les chefs)
- Validées
- Rejetées
- En cours
- Clôturées

Exemple avec Angular Material :
<mat-form-field appearance="outline">
  <mat-label>Filtrer par statut</mat-label>
  <mat-select [(ngModel)]="selectedStatut" (selectionChange)="filterEnquetes()">
    <mat-option value="">Toutes</mat-option>
    <mat-option value="EN_ATTENTE_VALIDATION">En attente</mat-option>
    <mat-option value="VALIDE">Validées</mat-option>
    <mat-option value="REJETE">Rejetées</mat-option>
    <mat-option value="EN_COURS">En cours</mat-option>
    <mat-option value="CLOTURE">Clôturées</mat-option>
  </mat-select>
</mat-form-field>

Dans le composant :
filterEnquetes(): void {
  if (!this.selectedStatut) {
    this.filteredEnquetes = this.enquetes;
  } else {
    this.filteredEnquetes = this.enquetes.filter(e => e.statut === this.selectedStatut);
  }
}
```

## Prompt 8 : Afficher le statut dans le formulaire de création

**Prompt à utiliser :**
```
Dans le composant de création d'enquête (create-enquete.component.ts), affichez le statut initial de l'enquête après la création.

Logique :
- Si créé par un agent : statut = EN_ATTENTE_VALIDATION (affiché en orange)
- Si créé par un chef : statut = VALIDE (affiché en vert, validation automatique)

Après la création réussie :
this.enqueteService.createEnquete(enqueteData).subscribe({
  next: (enquete) => {
    this.enquete = enquete;
    // Afficher le statut
    if (enquete.statut === 'VALIDE') {
      this.snackBar.open('Enquête créée et validée automatiquement', 'Fermer', { duration: 5000 });
    } else {
      this.snackBar.open('Enquête créée, en attente de validation', 'Fermer', { duration: 5000 });
    }
  }
});
```

## Prompt 9 : Afficher les commentaires de validation/rejet

**Prompt à utiliser :**
```
Dans le composant de détails d'enquête, affichez les commentaires de validation ou de rejet si présents.

Exemple :
<mat-card *ngIf="enquete.commentaireValidation">
  <mat-card-header>
    <mat-card-title>
      <mat-icon>{{ enquete.statut === 'REJETE' ? 'error' : 'check_circle' }}</mat-icon>
      {{ enquete.statut === 'REJETE' ? 'Commentaire de rejet' : 'Commentaire de validation' }}
    </mat-card-title>
  </mat-card-header>
  <mat-card-content>
    <p>{{ enquete.commentaireValidation }}</p>
    <small *ngIf="enquete.dateValidation">
      Date : {{ enquete.dateValidation | date:'dd/MM/yyyy HH:mm' }}
    </small>
  </mat-card-content>
</mat-card>
```

## Prompt 10 : Gérer les notifications de validation/rejet

**Prompt à utiliser :**
```
Intégrez les notifications de validation/rejet dans le système de notifications existant.

Lorsqu'une enquête est validée ou rejetée, le backend envoie automatiquement une notification à l'agent créateur.

Dans le composant de notifications (notifications.component.ts), ajoutez la gestion des notifications d'enquêtes :

- Type : DOSSIER_VALIDE ou DOSSIER_REJETE (utiliser les mêmes types que pour les dossiers)
- Entité : Enquête
- Message : "Votre enquête {rapportCode} a été {validée/rejetée}"

Filtrez et affichez ces notifications dans la liste des notifications de l'utilisateur.
```

## Résumé des modifications nécessaires

1. **Interface TypeScript** : Ajouter le champ `statut` à l'interface Enquette
2. **Enum Statut** : Créer ou réutiliser l'enum Statut
3. **Service** : Ajouter les méthodes `validerEnquete()` et `rejeterEnquete()`
4. **Liste** : Afficher le statut avec des badges colorés
5. **Détails** : Ajouter les boutons de validation/rejet pour les chefs
6. **Dialogue** : Créer un dialogue de rejet avec commentaire
7. **Filtres** : Ajouter des filtres par statut
8. **Création** : Afficher le statut initial après création
9. **Commentaires** : Afficher les commentaires de validation/rejet
10. **Notifications** : Gérer les notifications de validation/rejet

## Fichiers à modifier/créer

- `src/app/models/enquette.ts` (interface)
- `src/app/models/statut.ts` ou `enums.ts` (enum)
- `src/app/services/enquete.service.ts` (méthodes de validation)
- `src/app/components/list-enquete/list-enquete.component.ts` (affichage statut, filtres)
- `src/app/components/detail-enquete/detail-enquete.component.ts` (boutons validation/rejet)
- `src/app/components/create-enquete/create-enquete.component.ts` (affichage statut initial)
- `src/app/components/rejet-enquete-dialog/rejet-enquete-dialog.component.ts` (nouveau composant)
- `src/app/components/notifications/notifications.component.ts` (gestion notifications)

## Endpoints backend disponibles

- `PUT /api/enquettes/{id}/valider?chefId={chefId}` - Valider une enquête
- `PUT /api/enquettes/{id}/rejeter?commentaire={commentaire}` - Rejeter une enquête
- `GET /api/enquettes` - Liste des enquêtes (inclut le statut)
- `GET /api/enquettes/{id}` - Détails d'une enquête (inclut le statut)

## Workflow métier

1. **Agent crée enquête** → statut `EN_ATTENTE_VALIDATION` → ValidationEnquete créée avec statut `EN_ATTENTE`
2. **Chef crée enquête** → validation automatique → statut `VALIDE` → ValidationEnquete créée avec statut `VALIDE`
3. **Chef valide enquête** → enquête passe à `VALIDE` + notification agent
4. **Chef rejette enquête** → enquête passe à `REJETE` + notification agent

## Test après correction

1. Créer une enquête en tant qu'agent → vérifier statut EN_ATTENTE_VALIDATION
2. Créer une enquête en tant que chef → vérifier statut VALIDE (validation automatique)
3. Valider une enquête en tant que chef → vérifier statut VALIDE + notification
4. Rejeter une enquête en tant que chef → vérifier statut REJETE + notification
5. Filtrer les enquêtes par statut
6. Vérifier l'affichage des badges de statut







