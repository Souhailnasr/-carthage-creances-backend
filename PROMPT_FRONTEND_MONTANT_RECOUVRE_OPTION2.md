# 🎯 Prompt Frontend : Intégration Montant Recouvré (Option 2)

## 📋 Vue d'Ensemble

Ce document contient les prompts pour intégrer la logique de montant recouvré dans le frontend pour :
- **Actions Amiable** : Lorsque `reponseDebiteur === 'POSITIVE'`
- **Actions Huissier** : Lorsque `montantRecouvre` est fourni

**Architecture** : Utilisation de deux appels API séparés (Option 2) pour maintenir la cohérence avec les actions huissier.

---

## 🔧 Endpoints Backend Disponibles

### 1. Actions Amiable
- **Créer action** : `POST /carthage-creance/api/actions`
- **Mettre à jour montant** : `POST /carthage-creance/api/dossiers/{dossierId}/amiable`

### 2. Actions Huissier
- **Créer action avec montant** : `POST /carthage-creance/api/huissier/action`
  - Le montant est géré directement dans la création (pas besoin d'appel séparé)

### 3. Endpoint Générique (Alternative)
- **Mettre à jour montant** : `PUT /carthage-creance/api/dossiers/{dossierId}/montant`

---

## 📝 PROMPT 1 : Actions Amiable - Formulaire de Création

```
Intégrer la logique de montant recouvré dans le formulaire de création d'action amiable :

1. **Structure du Formulaire** :
   - Champ "Type d'action" (dropdown) : Obligatoire
   - Champ "Date de l'action" (date picker) : Obligatoire
   - Champ "Nombre d'occurrences" (number) : Obligatoire, min: 1
   - Champ "Coût unitaire (TND)" (number) : Obligatoire, min: 0
   - Champ "Réponse du débiteur" (dropdown) : POSITIVE / NEGATIVE / null
   - Champ "Montant recouvré (TND)" (number) : 
     * Visible UNIQUEMENT si "Réponse du débiteur" === 'POSITIVE'
     * Obligatoire si réponse === 'POSITIVE'
     * Format : nombre décimal (ex: 1500.50)
     * Validation : >= 0
     * Placeholder : "Ex: 1500.50"
     * Hint : "Montant recouvré suite à cette action"

2. **Logique d'Affichage Conditionnel** :
   ```typescript
   // Dans le composant Angular
   get showMontantRecouvre(): boolean {
     return this.form.get('reponseDebiteur')?.value === 'POSITIVE';
   }
   ```

3. **Validation du Formulaire** :
   - Si `reponseDebiteur === 'POSITIVE'` :
     * `montantRecouvre` est REQUIS
     * `montantRecouvre >= 0`
     * `montantRecouvre <= montantRestantActuel` (optionnel mais recommandé)
   - Si `reponseDebiteur === 'NEGATIVE'` ou null :
     * `montantRecouvre` ne doit PAS être rempli
     * Ne pas envoyer `montantRecouvre` dans la requête

4. **Validators Angular** :
   ```typescript
   // Dans le FormGroup
   this.form = this.fb.group({
     type: ['', Validators.required],
     dateAction: ['', Validators.required],
     nbOccurrences: [1, [Validators.required, Validators.min(1)]],
     coutUnitaire: [0, [Validators.required, Validators.min(0)]],
     reponseDebiteur: [null],
     montantRecouvre: [null]
   });

   // Ajouter un validator conditionnel
   this.form.get('reponseDebiteur')?.valueChanges.subscribe(value => {
     const montantControl = this.form.get('montantRecouvre');
     if (value === 'POSITIVE') {
       montantControl?.setValidators([Validators.required, Validators.min(0)]);
       montantControl?.enable();
     } else {
       montantControl?.clearValidators();
       montantControl?.setValue(null);
       montantControl?.disable();
     }
     montantControl?.updateValueAndValidity();
   });
   ```

5. **Affichage du Montant Restant Actuel** :
   - Afficher un message informatif :
     * "Montant restant à recouvrer : {dossier.montantRestant} TND"
     * Afficher uniquement si `dossier.montantRestant > 0`
   - Si `montantRecouvre` est saisi et dépasse `montantRestant` :
     * Afficher un warning : "Le montant saisi dépasse le montant restant"
     * Optionnel : Limiter la saisie au montant restant maximum
```

---

## 📝 PROMPT 2 : Actions Amiable - Service Angular

```
Créer ou modifier le service Angular pour gérer la création d'action avec mise à jour du montant :

1. **Méthode de Création d'Action** :
   ```typescript
   createActionWithMontant(actionData: ActionRequestDTO, montantRecouvre?: number): Observable<any> {
     // Étape 1 : Créer l'action
     return this.http.post<Action>(`${this.baseUrl}/actions`, actionData).pipe(
       switchMap((createdAction: Action) => {
         // Étape 2 : Si réponse positive et montant fourni, mettre à jour le montant
         if (actionData.reponseDebiteur === 'POSITIVE' && montantRecouvre != null && montantRecouvre > 0) {
           return this.updateMontantRecouvre(createdAction.dossier.id, montantRecouvre).pipe(
             map((updatedDossier: Dossier) => {
               return {
                 action: createdAction,
                 dossier: updatedDossier,
                 montantUpdated: true
               };
             }),
             catchError((error) => {
               // Si la mise à jour du montant échoue, retourner quand même l'action créée
               console.error('Erreur lors de la mise à jour du montant:', error);
               return of({
                 action: createdAction,
                 dossier: null,
                 montantUpdated: false,
                 error: error
               });
             })
           );
         } else {
           // Pas de mise à jour de montant nécessaire
           return of({
             action: createdAction,
             dossier: null,
             montantUpdated: false
           });
         }
       }),
       catchError((error) => {
         console.error('Erreur lors de la création de l\'action:', error);
         return throwError(() => error);
       })
     );
   }

   // Méthode pour mettre à jour le montant recouvré
   updateMontantRecouvre(dossierId: number, montantRecouvre: number): Observable<Dossier> {
     const payload = {
       montantRecouvre: montantRecouvre
     };
     return this.http.post<Dossier>(
       `${this.baseUrl}/dossiers/${dossierId}/amiable`,
       payload
     );
   }
   ```

2. **Gestion des Erreurs** :
   - Si la création d'action réussit mais la mise à jour du montant échoue :
     * Logger l'erreur
     * Afficher un message d'avertissement à l'utilisateur
     * Proposer de réessayer la mise à jour du montant
     * L'action reste créée (pas de rollback)

3. **TypeScript Interfaces** :
   ```typescript
   interface ActionRequestDTO {
     type: string;
     dateAction: string; // Format: 'YYYY-MM-DD'
     nbOccurrences: number;
     coutUnitaire: number;
     reponseDebiteur?: 'POSITIVE' | 'NEGATIVE' | null;
     dossierId: number;
   }

   interface ActionResponse {
     action: Action;
     dossier: Dossier | null;
     montantUpdated: boolean;
     error?: any;
   }
   ```
```

---

## 📝 PROMPT 3 : Actions Amiable - Composant Angular (Soumission)

```
Intégrer la logique de soumission dans le composant Angular :

1. **Méthode onSubmit** :
   ```typescript
   onSubmit(): void {
     if (this.form.invalid) {
       this.markFormGroupTouched(this.form);
       return;
     }

     const formValue = this.form.value;
     const actionData: ActionRequestDTO = {
       type: formValue.type,
       dateAction: formValue.dateAction,
       nbOccurrences: formValue.nbOccurrences,
       coutUnitaire: formValue.coutUnitaire,
       reponseDebiteur: formValue.reponseDebiteur,
       dossierId: this.dossierId
     };

     const montantRecouvre = formValue.reponseDebiteur === 'POSITIVE' 
       ? formValue.montantRecouvre 
       : null;

     // Afficher un loader
     this.isLoading = true;

     this.actionService.createActionWithMontant(actionData, montantRecouvre).subscribe({
       next: (response: ActionResponse) => {
         this.isLoading = false;
         
         if (response.montantUpdated) {
           // Succès complet : action créée + montant mis à jour
           this.showSuccessMessage(
             `Action créée avec succès. Montant recouvré mis à jour. Montant restant: ${response.dossier?.montantRestant} TND`
           );
           
           // Rafraîchir les données du dossier
           if (response.dossier) {
             this.updateDossierData(response.dossier);
           }
         } else if (response.error) {
           // Action créée mais montant non mis à jour
           this.showWarningMessage(
             `Action créée avec succès, mais la mise à jour du montant a échoué. ` +
             `Voulez-vous réessayer ?`
           );
           // Optionnel : Proposer un bouton pour réessayer
           this.pendingMontantUpdate = {
             dossierId: this.dossierId,
             montant: montantRecouvre
           };
         } else {
           // Action créée sans montant (réponse négative)
           this.showSuccessMessage('Action créée avec succès.');
         }
         
         // Fermer le modal et rafraîchir la liste
         this.dialogRef.close(true);
         this.refreshActionsList();
       },
       error: (error) => {
         this.isLoading = false;
         this.showErrorMessage('Erreur lors de la création de l\'action: ' + (error.error?.message || error.message));
       }
     });
   }

   // Méthode pour réessayer la mise à jour du montant
   retryMontantUpdate(): void {
     if (!this.pendingMontantUpdate) return;
     
     this.isLoading = true;
     this.actionService.updateMontantRecouvre(
       this.pendingMontantUpdate.dossierId,
       this.pendingMontantUpdate.montant
     ).subscribe({
       next: (dossier: Dossier) => {
         this.isLoading = false;
         this.showSuccessMessage(
           `Montant recouvré mis à jour. Montant restant: ${dossier.montantRestant} TND`
         );
         this.updateDossierData(dossier);
         this.pendingMontantUpdate = null;
       },
       error: (error) => {
         this.isLoading = false;
         this.showErrorMessage('Erreur lors de la mise à jour du montant: ' + (error.error?.message || error.message));
       }
     });
   }
   ```

2. **Affichage des Informations du Dossier** :
   - Afficher dans le formulaire :
     * Montant total : {dossier.montantTotal} TND
     * Montant déjà recouvré : {dossier.montantRecouvre} TND
     * Montant restant : {dossier.montantRestant} TND
   - Mettre à jour ces valeurs après la création réussie
```

---

## 📝 PROMPT 4 : Actions Huissier - Formulaire de Création

```
Intégrer la logique de montant recouvré dans le formulaire de création d'action huissier :

1. **Structure du Formulaire** :
   - Champ "Type d'action" (dropdown) : Obligatoire
   - Champ "Date de l'action" (date picker) : Obligatoire
   - Champ "Nom de l'huissier" (text) : Obligatoire
   - Champ "Pièce jointe" (file upload) : Optionnel
   - Champ "Montant recouvré (TND)" (number) : 
     * Optionnel (peut être null)
     * Format : nombre décimal (ex: 1500.50)
     * Validation : >= 0 si fourni
     * Placeholder : "Ex: 1500.50 (optionnel)"
     * Hint : "Montant recouvré suite à cette action huissier"

2. **Validation du Formulaire** :
   - `montantRecouvre` est optionnel
   - Si fourni : `montantRecouvre >= 0`
   - Pas de validation conditionnelle (contrairement aux actions amiable)

3. **Payload à Envoyer** :
   ```typescript
   interface ActionHuissierDTO {
     dossierId: number;
     typeAction: string;
     dateAction: string; // Format ISO
     huissierName: string;
     pieceJointeUrl?: string;
     montantRecouvre?: number; // Optionnel
     updateMode?: 'ADD' | 'SET'; // Par défaut: 'ADD'
   }
   ```

4. **Note** : 
   - Pour les actions huissier, le montant est géré directement dans la création
   - Pas besoin d'appel API séparé
   - Le backend met automatiquement à jour le montant recouvré du dossier
```

---

## 📝 PROMPT 5 : Actions Huissier - Service Angular

```
Créer ou modifier le service Angular pour les actions huissier :

1. **Méthode de Création** :
   ```typescript
   createActionHuissier(actionData: ActionHuissierDTO): Observable<ActionHuissier> {
     return this.http.post<ActionHuissier>(
       `${this.baseUrl}/huissier/action`,
       actionData
     ).pipe(
       tap((action) => {
         if (action.montantRecouvre) {
           console.log(`Action huissier créée avec montant recouvré: ${action.montantRecouvre} TND`);
           console.log(`Montant restant: ${action.montantRestant} TND`);
         }
       }),
       catchError((error) => {
         console.error('Erreur lors de la création de l\'action huissier:', error);
         return throwError(() => error);
       })
     );
   }
   ```

2. **Note** :
   - Le backend gère automatiquement la mise à jour du montant
   - La réponse contient `montantRecouvre`, `montantRestant`, et `etatDossier`
   - Pas besoin de logique supplémentaire côté frontend
```

---

## 📝 PROMPT 6 : Affichage des Montants dans les Listes et Détails

```
Afficher les informations de montant dans les composants de liste et de détail :

1. **Dans la Liste des Actions** :
   - Afficher une colonne "Montant recouvré" si applicable
   - Pour les actions avec réponse POSITIVE : Afficher le montant
   - Pour les actions huissier : Afficher le montant si présent
   - Format : "{montant} TND" ou "-" si non applicable

2. **Dans les Détails d'un Dossier** :
   - Section "Informations Financières" :
     * Montant total : {dossier.montantTotal} TND
     * Montant recouvré : {dossier.montantRecouvre} TND
     * Montant restant : {dossier.montantRestant} TND
     * État : {dossier.etatDossier} (avec badge coloré)
       - RECOVERED_TOTAL : Badge vert "Totalement recouvré"
       - RECOVERED_PARTIAL : Badge orange "Partiellement recouvré"
       - NOT_RECOVERED : Badge rouge "Non recouvré"

3. **Dans l'Historique des Actions** :
   - Pour chaque action avec montant recouvré :
     * Afficher : "Montant recouvré : {montant} TND"
     * Afficher la date et l'heure de l'action
     * Afficher le type d'action

4. **Composant Angular Exemple** :
   ```html
   <div class="financial-info">
     <h3>Informations Financières</h3>
     <div class="info-row">
       <span class="label">Montant total :</span>
       <span class="value">{{ dossier.montantTotal | number:'1.2-2' }} TND</span>
     </div>
     <div class="info-row">
       <span class="label">Montant recouvré :</span>
       <span class="value success">{{ dossier.montantRecouvre | number:'1.2-2' }} TND</span>
     </div>
     <div class="info-row">
       <span class="label">Montant restant :</span>
       <span class="value" [ngClass]="{'warning': dossier.montantRestant > 0, 'success': dossier.montantRestant === 0}">
         {{ dossier.montantRestant | number:'1.2-2' }} TND
       </span>
     </div>
     <div class="info-row">
       <span class="label">État :</span>
       <span class="badge" [ngClass]="getEtatBadgeClass(dossier.etatDossier)">
         {{ getEtatLabel(dossier.etatDossier) }}
       </span>
     </div>
   </div>
   ```

5. **Méthodes Helper** :
   ```typescript
   getEtatBadgeClass(etat: string): string {
     switch(etat) {
       case 'RECOVERED_TOTAL': return 'badge-success';
       case 'RECOVERED_PARTIAL': return 'badge-warning';
       case 'NOT_RECOVERED': return 'badge-danger';
       default: return 'badge-secondary';
     }
   }

   getEtatLabel(etat: string): string {
     switch(etat) {
       case 'RECOVERED_TOTAL': return 'Totalement recouvré';
       case 'RECOVERED_PARTIAL': return 'Partiellement recouvré';
       case 'NOT_RECOVERED': return 'Non recouvré';
       default: return 'Inconnu';
     }
   }
   ```
```

---

## 📝 PROMPT 7 : Gestion des Erreurs et Messages Utilisateur

```
Implémenter la gestion des erreurs et les messages utilisateur :

1. **Messages de Succès** :
   - Action créée sans montant : "Action créée avec succès."
   - Action créée avec montant : "Action créée avec succès. Montant recouvré mis à jour. Montant restant: {montantRestant} TND"
   - Montant mis à jour séparément : "Montant recouvré mis à jour. Montant restant: {montantRestant} TND"

2. **Messages d'Erreur** :
   - Erreur création action : "Erreur lors de la création de l'action: {message}"
   - Erreur mise à jour montant : "Erreur lors de la mise à jour du montant: {message}"
   - Action créée mais montant non mis à jour : "Action créée avec succès, mais la mise à jour du montant a échoué. Voulez-vous réessayer ?"

3. **Messages d'Avertissement** :
   - Montant dépasse le montant restant : "Le montant saisi ({montantSaisi} TND) dépasse le montant restant ({montantRestant} TND). Le montant sera limité au montant restant."
   - Montant négatif : "Le montant recouvré ne peut pas être négatif."

4. **Service de Notification** :
   ```typescript
   // Utiliser un service de notification (ex: MatSnackBar pour Angular Material)
   showSuccessMessage(message: string): void {
     this.snackBar.open(message, 'Fermer', {
       duration: 5000,
       panelClass: ['success-snackbar']
     });
   }

   showErrorMessage(message: string): void {
     this.snackBar.open(message, 'Fermer', {
       duration: 7000,
       panelClass: ['error-snackbar']
     });
   }

   showWarningMessage(message: string): void {
     this.snackBar.open(message, 'Réessayer', {
       duration: 10000,
       panelClass: ['warning-snackbar']
     }).onAction().subscribe(() => {
       // Action à effectuer (ex: réessayer la mise à jour du montant)
     });
   }
   ```
```

---

## 📝 PROMPT 8 : Tests et Validation

```
Créer des tests pour valider la logique :

1. **Tests Unitaires** :
   - Tester l'affichage conditionnel du champ montant
   - Tester la validation du formulaire
   - Tester la soumission avec réponse POSITIVE
   - Tester la soumission avec réponse NEGATIVE
   - Tester la gestion des erreurs

2. **Tests d'Intégration** :
   - Tester le flux complet : création action + mise à jour montant
   - Tester le cas où la création réussit mais la mise à jour échoue
   - Tester la récupération des données du dossier après mise à jour

3. **Scénarios à Tester** :
   - ✅ Création action avec réponse POSITIVE et montant valide
   - ✅ Création action avec réponse POSITIVE et montant > montant restant
   - ✅ Création action avec réponse NEGATIVE (pas de montant)
   - ✅ Création action sans réponse (pas de montant)
   - ✅ Erreur lors de la création de l'action
   - ✅ Erreur lors de la mise à jour du montant (action créée)
   - ✅ Réessai de la mise à jour du montant après échec
```

---

## 📋 Résumé des Endpoints Utilisés

### Actions Amiable
1. **POST** `/carthage-creance/api/actions` - Créer l'action
2. **POST** `/carthage-creance/api/dossiers/{dossierId}/amiable` - Mettre à jour le montant

### Actions Huissier
1. **POST** `/carthage-creance/api/huissier/action` - Créer l'action (avec montant si fourni)

### Endpoint Générique (Alternative)
1. **PUT** `/carthage-creance/api/dossiers/{dossierId}/montant` - Mettre à jour les montants (plus flexible)

---

## ✅ Checklist d'Implémentation

- [ ] Formulaire de création action amiable avec champ montant conditionnel
- [ ] Validation du formulaire (montant requis si réponse POSITIVE)
- [ ] Service Angular avec méthode `createActionWithMontant()`
- [ ] Gestion des erreurs (action créée mais montant non mis à jour)
- [ ] Affichage des informations financières dans les détails du dossier
- [ ] Affichage du montant dans les listes d'actions
- [ ] Messages de succès/erreur/avertissement
- [ ] Tests unitaires et d'intégration
- [ ] Formulaire de création action huissier (déjà géré par le backend)
- [ ] Documentation pour les développeurs frontend

---

## 🎯 Notes Importantes

1. **Mode de Mise à Jour** :
   - Par défaut : `ADD` (ajouter au montant existant)
   - Alternative : `SET` (remplacer le montant)
   - Pour les actions amiable : Toujours utiliser `ADD`
   - Pour les actions huissier : Spécifier dans le DTO si nécessaire

2. **Calcul Automatique** :
   - Le backend calcule automatiquement : `montantRestant = montantTotal - montantRecouvre`
   - Le backend met à jour automatiquement l'état du dossier selon le montant restant

3. **Cohérence** :
   - Les actions huissier gèrent le montant directement dans la création
   - Les actions amiable nécessitent un appel séparé
   - Cette différence est intentionnelle pour maintenir la flexibilité

---

## 📞 Support

Si vous rencontrez des problèmes lors de l'implémentation :
1. Vérifier que les endpoints backend sont accessibles
2. Vérifier les logs du backend pour les erreurs
3. Vérifier la structure des DTOs envoyés
4. Tester avec Postman/curl avant d'intégrer dans le frontend






