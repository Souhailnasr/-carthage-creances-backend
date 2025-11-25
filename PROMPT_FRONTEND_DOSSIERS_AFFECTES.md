# 🔧 Prompts pour Ajuster le Frontend : Dossiers Affectés

## Prompt Principal : Corriger l'Appel API pour les Dossiers Affectés

```
Corrige le code frontend Angular pour récupérer les dossiers affectés à un agent :

1. **Localiser le service qui charge les dossiers affectés** :
   - Chercher : `dossier.service.ts`, `dossier-api.service.ts`, ou similaire
   - Chercher la méthode qui charge "mes dossiers affectés" ou "dossiers assignés"
   - Chercher les appels vers `/api/dossiers` avec `size=1000` ou des paramètres de pagination

2. **Problème identifié** :
   - Le frontend appelle `/api/dossiers?page=0&size=1000` ce qui cause une erreur 400
   - La limite maximale de `size` est 100, pas 1000
   - Il faut utiliser l'endpoint spécifique `/api/dossiers/agent/{agentId}/paginated`

3. **Solution à appliquer** :
   - Remplacer l'appel vers `/api/dossiers?page=0&size=1000` 
   - Par : `/api/dossiers/agent/{agentId}/paginated?page=0&size=10`
   - Limiter `size` à 100 maximum
   - Utiliser l'ID de l'agent connecté (depuis le token JWT ou le service auth)

4. **Code à modifier** :
   - Trouver la méthode qui fait : `this.http.get('/api/dossiers', { params: { page: 0, size: 1000 } })`
   - La remplacer par : `this.http.get(`/api/dossiers/agent/${agentId}/paginated`, { params: { page: 0, size: 10 } })`
   - Ajouter une validation pour limiter `size` à 100 max

5. **Gérer la réponse paginée** :
   - La nouvelle API retourne un objet avec `content`, `totalElements`, `totalPages`, etc.
   - Adapter le code pour utiliser `response.content` au lieu de `response` directement
   - Gérer la pagination avec `totalPages` et `currentPage`

6. **Exemple de code attendu** :
```typescript
getDossiersAffectes(agentId: number, page: number = 0, size: number = 10): Observable<any> {
  // Limiter size à 100 maximum
  const limitedSize = Math.min(size, 100);
  
  return this.http.get(`${this.apiUrl}/dossiers/agent/${agentId}/paginated`, {
    params: {
      page: page.toString(),
      size: limitedSize.toString(),
      sort: 'dateCreation'
    }
  }).pipe(
    map((response: any) => {
      // La réponse contient { content, totalElements, totalPages, ... }
      return {
        dossiers: response.content || [],
        totalElements: response.totalElements || 0,
        totalPages: response.totalPages || 0,
        currentPage: response.currentPage || 0
      };
    }),
    catchError(error => {
      console.error('Erreur lors du chargement des dossiers affectés:', error);
      return throwError(() => error);
    })
  );
}
```

7. **Dans le composant** :
   - Récupérer l'ID de l'agent connecté (depuis le service auth ou le token)
   - Appeler la nouvelle méthode avec l'agentId
   - Gérer la pagination avec les boutons précédent/suivant

Corrige le code pour utiliser le bon endpoint et gérer correctement la pagination.
```

---

## Prompt 1 : Localiser et Identifier le Code à Modifier

```
Dans le projet frontend Angular, localise le code qui charge les "dossiers affectés" ou "mes dossiers affectés" :

1. **Chercher dans les fichiers suivants** :
   - `src/app/services/dossier.service.ts`
   - `src/app/services/dossier-api.service.ts`
   - `src/app/components/mes-dossiers/mes-dossiers.component.ts`
   - `src/app/pages/dossiers/dossiers.component.ts`
   - Tout fichier contenant "dossiers affectés" ou "mes dossiers"

2. **Identifier les appels HTTP problématiques** :
   - Chercher : `this.http.get('/api/dossiers'`
   - Chercher : `size=1000` ou `size: 1000`
   - Chercher : `params: { page: 0, size: 1000 }`
   - Chercher : Les erreurs dans la console "La taille de page doit être entre 1 et 100"

3. **Identifier l'agent connecté** :
   - Comment l'ID de l'agent est-il récupéré ?
   - Est-ce depuis le token JWT ?
   - Est-ce depuis un service d'authentification ?
   - Chercher : `this.authService.getCurrentUser()`, `this.authService.getUserId()`, etc.

4. **Identifier la gestion de la pagination** :
   - Y a-t-il des boutons "Précédent" / "Suivant" ?
   - Y a-t-il une variable `currentPage` ou `page` ?
   - Comment la pagination est-elle gérée actuellement ?

Liste tous les fichiers et méthodes qui doivent être modifiés.
```

---

## Prompt 2 : Corriger le Service de Dossiers

```
Modifie le service de dossiers (dossier.service.ts ou dossier-api.service.ts) pour utiliser le nouvel endpoint :

1. **Trouver la méthode qui charge les dossiers affectés** :
   - Chercher : `getDossiersAffectes()`, `loadMesDossiers()`, `getMyDossiers()`, etc.
   - Ou la méthode qui fait l'appel avec `size=1000`

2. **Remplacer l'ancien code** :
   ```typescript
   // ❌ ANCIEN CODE (à remplacer)
   getDossiersAffectes(): Observable<Dossier[]> {
     return this.http.get<Dossier[]>(`${this.apiUrl}/dossiers`, {
       params: {
         page: '0',
         size: '1000'  // ❌ Trop grand
       }
     });
   }
   ```

3. **Par le nouveau code** :
   ```typescript
   // ✅ NOUVEAU CODE
   getDossiersAffectes(agentId: number, page: number = 0, size: number = 10): Observable<any> {
     // Limiter size à 100 maximum
     const limitedSize = Math.min(Math.max(size, 1), 100);
     
     return this.http.get(`${this.apiUrl}/dossiers/agent/${agentId}/paginated`, {
       params: {
         page: page.toString(),
         size: limitedSize.toString(),
         sort: 'dateCreation'
       }
     }).pipe(
       map((response: any) => {
         // La réponse contient { content, totalElements, totalPages, ... }
         return {
           dossiers: response.content || [],
           totalElements: response.totalElements || 0,
           totalPages: response.totalPages || 0,
           currentPage: response.currentPage || 0,
           size: response.size || limitedSize,
           first: response.first || false,
           last: response.last || false
         };
       }),
       catchError(error => {
         console.error('Erreur lors du chargement des dossiers affectés:', error);
         if (error.status === 400 && error.error?.error?.includes('taille de page')) {
           console.warn('⚠️ Taille de page invalide, utilisation de size=10 par défaut');
           // Retry avec size=10
           return this.getDossiersAffectes(agentId, page, 10);
         }
         return throwError(() => error);
       })
     );
   }
   ```

4. **Ajouter les imports nécessaires** :
   ```typescript
   import { Observable, throwError } from 'rxjs';
   import { map, catchError } from 'rxjs/operators';
   ```

5. **Vérifier que l'agentId est disponible** :
   - Si le service a accès à `AuthService`, utiliser : `this.authService.getCurrentUser().id`
   - Sinon, passer l'agentId en paramètre depuis le composant

Applique ces modifications au service.
```

---

## Prompt 3 : Corriger le Composant qui Affiche les Dossiers

```
Modifie le composant qui affiche "Mes dossiers affectés" pour utiliser le nouveau service :

1. **Trouver le composant** :
   - Chercher : `mes-dossiers.component.ts`, `dossiers-affectes.component.ts`, etc.
   - Ou le composant qui affiche "Aucun dossier trouvé" quand il n'y a pas de dossiers

2. **Récupérer l'ID de l'agent connecté** :
   ```typescript
   // Dans le composant
   constructor(
     private dossierService: DossierService,
     private authService: AuthService  // Ou le service qui gère l'auth
   ) {}
   
   ngOnInit() {
     // Récupérer l'agent connecté
     const currentUser = this.authService.getCurrentUser();
     this.currentAgentId = currentUser?.id;
     
     // Charger les dossiers
     this.loadDossiersAffectes();
   }
   ```

3. **Modifier la méthode de chargement** :
   ```typescript
   // ❌ ANCIEN CODE (à remplacer)
   loadDossiersAffectes() {
     this.dossierService.getDossiersAffectes().subscribe({
       next: (dossiers) => {
         this.dossiers = dossiers;
       },
       error: (error) => {
         console.error('Erreur:', error);
       }
     });
   }
   ```

4. **Par le nouveau code** :
   ```typescript
   // ✅ NOUVEAU CODE
   currentPage: number = 0;
   pageSize: number = 10;
   totalElements: number = 0;
   totalPages: number = 0;
   dossiers: Dossier[] = [];
   loading: boolean = false;
   
   loadDossiersAffectes() {
     if (!this.currentAgentId) {
       console.warn('⚠️ Agent ID non disponible');
       return;
     }
     
     this.loading = true;
     this.dossierService.getDossiersAffectes(
       this.currentAgentId, 
       this.currentPage, 
       this.pageSize
     ).subscribe({
       next: (response) => {
         this.dossiers = response.dossiers || [];
         this.totalElements = response.totalElements || 0;
         this.totalPages = response.totalPages || 0;
         this.currentPage = response.currentPage || 0;
         this.loading = false;
         
         console.log(`✅ ${this.dossiers.length} dossiers chargés (page ${this.currentPage + 1}/${this.totalPages})`);
       },
       error: (error) => {
         console.error('❌ Erreur lors du chargement des dossiers:', error);
         this.dossiers = [];
         this.loading = false;
       }
     });
   }
   
   // Méthodes pour la pagination
   goToPage(page: number) {
     if (page >= 0 && page < this.totalPages) {
       this.currentPage = page;
       this.loadDossiersAffectes();
     }
   }
   
   nextPage() {
     if (this.currentPage < this.totalPages - 1) {
       this.goToPage(this.currentPage + 1);
     }
   }
   
   previousPage() {
     if (this.currentPage > 0) {
       this.goToPage(this.currentPage - 1);
     }
   }
   ```

5. **Mettre à jour le template HTML** :
   ```html
   <!-- Afficher les dossiers -->
   <div *ngIf="loading">Chargement...</div>
   <div *ngIf="!loading && dossiers.length === 0">
     <p>Aucun dossier ne vous a été affecté pour le moment.</p>
   </div>
   <div *ngIf="!loading && dossiers.length > 0">
     <!-- Liste des dossiers -->
     <div *ngFor="let dossier of dossiers">
       <!-- Affichage du dossier -->
     </div>
     
     <!-- Pagination -->
     <div class="pagination">
       <button (click)="previousPage()" [disabled]="currentPage === 0">
         Précédent
       </button>
       <span>Page {{ currentPage + 1 }} / {{ totalPages }}</span>
       <button (click)="nextPage()" [disabled]="currentPage >= totalPages - 1">
         Suivant
       </button>
     </div>
   </div>
   ```

Applique ces modifications au composant.
```

---

## Prompt 4 : Gérer le Fallback et les Erreurs

```
Améliore la gestion des erreurs et le fallback dans le service de dossiers :

1. **Si l'endpoint paginé échoue, essayer l'endpoint simple** :
   ```typescript
   getDossiersAffectes(agentId: number, page: number = 0, size: number = 10): Observable<any> {
     const limitedSize = Math.min(Math.max(size, 1), 100);
     
     return this.http.get(`${this.apiUrl}/dossiers/agent/${agentId}/paginated`, {
       params: {
         page: page.toString(),
         size: limitedSize.toString(),
         sort: 'dateCreation'
       }
     }).pipe(
       map((response: any) => ({
         dossiers: response.content || [],
         totalElements: response.totalElements || 0,
         totalPages: response.totalPages || 0,
         currentPage: response.currentPage || 0
       })),
       catchError(error => {
         console.warn('⚠️ Endpoint paginé échoué, tentative avec endpoint simple...');
         
         // Fallback : utiliser l'endpoint simple
         return this.http.get<Dossier[]>(`${this.apiUrl}/dossiers/agent/${agentId}`).pipe(
           map((dossiers: Dossier[]) => {
             // Paginer manuellement côté client
             const start = page * limitedSize;
             const end = start + limitedSize;
             const pagedDossiers = dossiers.slice(start, end);
             const totalPages = Math.ceil(dossiers.length / limitedSize);
             
             return {
               dossiers: pagedDossiers,
               totalElements: dossiers.length,
               totalPages: totalPages,
               currentPage: page
             };
           }),
           catchError(fallbackError => {
             console.error('❌ Erreur lors du chargement des dossiers (fallback aussi échoué):', fallbackError);
             return throwError(() => fallbackError);
           })
         );
       })
     );
   }
   ```

2. **Gérer les erreurs spécifiques** :
   ```typescript
   catchError(error => {
     if (error.status === 400) {
       console.error('❌ Erreur 400: Paramètres invalides', error.error);
       // Afficher un message à l'utilisateur
     } else if (error.status === 401) {
       console.error('❌ Erreur 401: Non autorisé', error.error);
       // Rediriger vers la page de login
     } else if (error.status === 404) {
       console.error('❌ Erreur 404: Endpoint non trouvé', error.error);
       // Essayer le fallback
     } else {
       console.error('❌ Erreur inconnue:', error);
     }
     return throwError(() => error);
   })
   ```

3. **Ajouter un indicateur de chargement** :
   ```typescript
   loading: boolean = false;
   
   loadDossiersAffectes() {
     this.loading = true;
     this.dossierService.getDossiersAffectes(this.currentAgentId, this.currentPage, this.pageSize)
       .subscribe({
         next: (response) => {
           // ... traitement
           this.loading = false;
         },
         error: (error) => {
           // ... gestion erreur
           this.loading = false;
         }
       });
   }
   ```

Applique ces améliorations pour une meilleure gestion des erreurs.
```

---

## Prompt 5 : Tester et Vérifier

```
Teste les modifications apportées au frontend :

1. **Vérifier dans la console du navigateur (F12)** :
   - Plus d'erreur "La taille de page doit être entre 1 et 100"
   - L'appel HTTP utilise `/api/dossiers/agent/{agentId}/paginated`
   - Le paramètre `size` est <= 100
   - La réponse contient `content`, `totalElements`, `totalPages`

2. **Vérifier l'affichage** :
   - Les dossiers affectés s'affichent correctement
   - La pagination fonctionne (boutons Précédent/Suivant)
   - Le message "Aucun dossier trouvé" s'affiche seulement s'il n'y a vraiment aucun dossier

3. **Tester différents scénarios** :
   - Agent avec 0 dossiers → Affiche "Aucun dossier trouvé"
   - Agent avec 5 dossiers → Affiche les 5 dossiers
   - Agent avec 50 dossiers → Pagination fonctionne (10 par page = 5 pages)

4. **Vérifier les logs backend** :
   - L'endpoint `/api/dossiers/agent/{agentId}/paginated` est appelé
   - Pas d'erreur 400
   - Les dossiers sont retournés correctement

5. **Tester avec différents paramètres** :
   - `size=10` → Fonctionne
   - `size=50` → Fonctionne
   - `size=100` → Fonctionne
   - `size=101` → Limité à 100 automatiquement
   - `size=1000` → Limité à 100 automatiquement

Si tout fonctionne, les modifications sont correctes.
```

---

## 📋 Checklist de Vérification

```
Vérifie que toutes ces modifications ont été appliquées :

□ Le service utilise `/api/dossiers/agent/{agentId}/paginated` au lieu de `/api/dossiers?size=1000`
□ Le paramètre `size` est limité à 100 maximum
□ L'agentId est récupéré correctement (depuis le token ou le service auth)
□ La réponse paginée est gérée correctement (`response.content` au lieu de `response`)
□ La pagination fonctionne (boutons Précédent/Suivant)
□ Les erreurs sont gérées avec un fallback si nécessaire
□ Plus d'erreur 400 dans la console
□ Les dossiers s'affichent correctement
□ Le message "Aucun dossier trouvé" s'affiche seulement s'il n'y a vraiment aucun dossier

Si toutes les cases sont cochées, les modifications sont complètes.
```

---

## 🔧 Code Complet de Référence

### Service Complet (`dossier.service.ts`)

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class DossierService {
  private apiUrl = 'http://localhost:8089/carthage-creance/api';

  constructor(private http: HttpClient) {}

  /**
   * Récupère les dossiers affectés à un agent avec pagination
   */
  getDossiersAffectes(agentId: number, page: number = 0, size: number = 10): Observable<any> {
    // Limiter size à 100 maximum
    const limitedSize = Math.min(Math.max(size, 1), 100);
    
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', limitedSize.toString())
      .set('sort', 'dateCreation');
    
    return this.http.get(`${this.apiUrl}/dossiers/agent/${agentId}/paginated`, { params }).pipe(
      map((response: any) => ({
        dossiers: response.content || [],
        totalElements: response.totalElements || 0,
        totalPages: response.totalPages || 0,
        currentPage: response.currentPage || 0,
        size: response.size || limitedSize,
        first: response.first || false,
        last: response.last || false
      })),
      catchError(error => {
        console.error('❌ Erreur lors du chargement des dossiers affectés:', error);
        
        // Fallback : utiliser l'endpoint simple
        if (error.status === 404 || error.status === 500) {
          console.warn('⚠️ Endpoint paginé non disponible, utilisation de l\'endpoint simple...');
          return this.http.get<any[]>(`${this.apiUrl}/dossiers/agent/${agentId}`).pipe(
            map((dossiers: any[]) => {
              const start = page * limitedSize;
              const end = start + limitedSize;
              const pagedDossiers = dossiers.slice(start, end);
              const totalPages = Math.ceil(dossiers.length / limitedSize);
              
              return {
                dossiers: pagedDossiers,
                totalElements: dossiers.length,
                totalPages: totalPages,
                currentPage: page,
                size: limitedSize,
                first: page === 0,
                last: page >= totalPages - 1
              };
            }),
            catchError(fallbackError => {
              console.error('❌ Erreur lors du chargement (fallback aussi échoué):', fallbackError);
              return throwError(() => fallbackError);
            })
          );
        }
        
        return throwError(() => error);
      })
    );
  }
}
```

### Composant Complet (`mes-dossiers.component.ts`)

```typescript
import { Component, OnInit } from '@angular/core';
import { DossierService } from '../services/dossier.service';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-mes-dossiers',
  templateUrl: './mes-dossiers.component.html',
  styleUrls: ['./mes-dossiers.component.css']
})
export class MesDossiersComponent implements OnInit {
  dossiers: any[] = [];
  currentAgentId: number | null = null;
  currentPage: number = 0;
  pageSize: number = 10;
  totalElements: number = 0;
  totalPages: number = 0;
  loading: boolean = false;

  constructor(
    private dossierService: DossierService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    // Récupérer l'agent connecté
    const currentUser = this.authService.getCurrentUser();
    this.currentAgentId = currentUser?.id || null;
    
    if (this.currentAgentId) {
      this.loadDossiersAffectes();
    } else {
      console.warn('⚠️ Agent ID non disponible');
    }
  }

  loadDossiersAffectes() {
    if (!this.currentAgentId) {
      return;
    }
    
    this.loading = true;
    this.dossierService.getDossiersAffectes(
      this.currentAgentId, 
      this.currentPage, 
      this.pageSize
    ).subscribe({
      next: (response) => {
        this.dossiers = response.dossiers || [];
        this.totalElements = response.totalElements || 0;
        this.totalPages = response.totalPages || 0;
        this.currentPage = response.currentPage || 0;
        this.loading = false;
        
        console.log(`✅ ${this.dossiers.length} dossiers chargés (page ${this.currentPage + 1}/${this.totalPages})`);
      },
      error: (error) => {
        console.error('❌ Erreur lors du chargement des dossiers:', error);
        this.dossiers = [];
        this.loading = false;
      }
    });
  }

  goToPage(page: number) {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadDossiersAffectes();
    }
  }

  nextPage() {
    if (this.currentPage < this.totalPages - 1) {
      this.goToPage(this.currentPage + 1);
    }
  }

  previousPage() {
    if (this.currentPage > 0) {
      this.goToPage(this.currentPage - 1);
    }
  }
}
```

### Template HTML (`mes-dossiers.component.html`)

```html
<div class="mes-dossiers-container">
  <h2>Mes Dossiers Affectés</h2>
  
  <!-- Indicateur de chargement -->
  <div *ngIf="loading" class="loading">
    <p>Chargement des dossiers...</p>
  </div>
  
  <!-- Message si aucun dossier -->
  <div *ngIf="!loading && dossiers.length === 0" class="no-dossiers">
    <p>Aucun dossier ne vous a été affecté pour le moment.</p>
  </div>
  
  <!-- Liste des dossiers -->
  <div *ngIf="!loading && dossiers.length > 0">
    <div class="dossiers-list">
      <div *ngFor="let dossier of dossiers" class="dossier-item">
        <!-- Affichage du dossier -->
        <h3>{{ dossier.titre }}</h3>
        <p>Numéro: {{ dossier.numeroDossier }}</p>
        <p>Date de création: {{ dossier.dateCreation | date }}</p>
        <!-- Autres champs du dossier -->
      </div>
    </div>
    
    <!-- Pagination -->
    <div class="pagination" *ngIf="totalPages > 1">
      <button (click)="previousPage()" [disabled]="currentPage === 0">
        Précédent
      </button>
      <span>Page {{ currentPage + 1 }} / {{ totalPages }} ({{ totalElements }} dossiers)</span>
      <button (click)="nextPage()" [disabled]="currentPage >= totalPages - 1">
        Suivant
      </button>
    </div>
  </div>
</div>
```

---

Utilisez ces prompts pour ajuster le frontend aux changements backend.

