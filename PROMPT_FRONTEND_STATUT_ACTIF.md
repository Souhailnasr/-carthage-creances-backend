# 🎨 Prompts Frontend : Affichage du Statut Actif

## Prompt Principal : Afficher le Statut Actif avec Points Colorés

```
Crée un composant Angular pour afficher la liste des utilisateurs avec leur statut d'activité :

1. **Créer un composant de liste d'utilisateurs** :
   - Afficher chaque utilisateur avec un point coloré devant son nom
   - Point vert (🟢) si `actif = true` (utilisateur connecté)
   - Point rouge (🔴) si `actif = false` (utilisateur déconnecté)

2. **Filtrage selon le rôle** :
   - **Chef** : Afficher uniquement les agents qu'il a créés
   - **Super Admin** : Afficher tous les utilisateurs

3. **Récupérer le statut actif depuis l'API** :
   - L'API retourne déjà le champ `actif` dans la réponse
   - Utiliser `GET /api/users/agents` pour les chefs
   - Utiliser `GET /api/users` pour le super admin

4. **Design attendu** :
   ```
   🟢 John Doe (Agent Dossier) - Actif
   🔴 Jane Smith (Agent Finance) - Inactif
   🟢 Bob Wilson (Agent Amiable) - Actif
   ```

5. **Code du composant** :
   - Créer `utilisateurs-list.component.ts` et `utilisateurs-list.component.html`
   - Utiliser un service pour récupérer les utilisateurs
   - Filtrer selon le rôle de l'utilisateur connecté
   - Afficher le point coloré avec CSS ou un composant

6. **Styles CSS** :
   - Point vert : `background-color: #28a745;` ou `color: green;`
   - Point rouge : `background-color: #dc3545;` ou `color: red;`
   - Taille du point : `width: 12px; height: 12px; border-radius: 50%;`

Crée le composant avec ces fonctionnalités.
```

---

## Prompt 1 : Créer le Service pour Récupérer les Utilisateurs

```
Crée un service Angular pour récupérer les utilisateurs avec leur statut d'activité :

1. **Créer le service** : `src/app/services/utilisateur.service.ts`

2. **Méthodes à implémenter** :
   ```typescript
   // Pour les chefs : récupérer les agents qu'ils ont créés
   getAgentsByChef(chefId: number): Observable<Utilisateur[]>
   
   // Pour le super admin : récupérer tous les utilisateurs
   getAllUtilisateurs(): Observable<Utilisateur[]>
   
   // Récupérer un utilisateur par ID
   getUtilisateurById(userId: number): Observable<Utilisateur>
   ```

3. **Interface Utilisateur** :
   ```typescript
   export interface Utilisateur {
     id: number;
     nom: string;
     prenom: string;
     email: string;
     roleUtilisateur: string;
     actif: boolean;  // ← Important : statut d'activité
     derniereConnexion: string | null;
     derniereDeconnexion: string | null;
   }
   ```

4. **Code du service** :
   ```typescript
   import { Injectable } from '@angular/core';
   import { HttpClient } from '@angular/common/http';
   import { Observable } from 'rxjs';
   import { map } from 'rxjs/operators';

   @Injectable({
     providedIn: 'root'
   })
   export class UtilisateurService {
     private apiUrl = 'http://localhost:8089/carthage-creance/api';

     constructor(private http: HttpClient) {}

     /**
      * Récupère les agents d'un chef
      */
     getAgentsByChef(chefId: number): Observable<Utilisateur[]> {
       return this.http.get<Utilisateur[]>(`${this.apiUrl}/users/chef/${chefId}`);
     }

     /**
      * Récupère tous les utilisateurs (pour super admin)
      */
     getAllUtilisateurs(): Observable<Utilisateur[]> {
       return this.http.get<Utilisateur[]>(`${this.apiUrl}/users`);
     }

     /**
      * Récupère un utilisateur par ID
      */
     getUtilisateurById(userId: number): Observable<Utilisateur> {
       return this.http.get<Utilisateur>(`${this.apiUrl}/users/${userId}`);
     }
   }
   ```

Crée ce service avec ces méthodes.
```

---

## Prompt 2 : Créer le Composant de Liste d'Utilisateurs

```
Crée un composant Angular pour afficher la liste des utilisateurs avec leur statut d'activité :

1. **Créer le composant** : `src/app/components/utilisateurs-list/utilisateurs-list.component.ts`

2. **Fonctionnalités** :
   - Détecter le rôle de l'utilisateur connecté
   - Si chef : charger uniquement ses agents
   - Si super admin : charger tous les utilisateurs
   - Afficher chaque utilisateur avec un point coloré

3. **Code du composant** :
   ```typescript
   import { Component, OnInit } from '@angular/core';
   import { UtilisateurService } from '../../services/utilisateur.service';
   import { AuthService } from '../../services/auth.service';
   import { Utilisateur } from '../../models/utilisateur.model';

   @Component({
     selector: 'app-utilisateurs-list',
     templateUrl: './utilisateurs-list.component.html',
     styleUrls: ['./utilisateurs-list.component.css']
   })
   export class UtilisateursListComponent implements OnInit {
     utilisateurs: Utilisateur[] = [];
     currentUser: any;
     loading: boolean = false;
     error: string | null = null;

     constructor(
       private utilisateurService: UtilisateurService,
       private authService: AuthService
     ) {}

     ngOnInit() {
       this.currentUser = this.authService.getCurrentUser();
       this.loadUtilisateurs();
     }

     loadUtilisateurs() {
       this.loading = true;
       this.error = null;

       let request: Observable<Utilisateur[]>;

       if (this.currentUser?.role === 'SUPER_ADMIN') {
         // Super admin : charger tous les utilisateurs
         request = this.utilisateurService.getAllUtilisateurs();
       } else if (this.currentUser?.role?.startsWith('CHEF_')) {
         // Chef : charger uniquement ses agents
         request = this.utilisateurService.getAgentsByChef(this.currentUser.id);
       } else {
         this.error = 'Accès non autorisé';
         this.loading = false;
         return;
       }

       request.subscribe({
         next: (utilisateurs) => {
           this.utilisateurs = utilisateurs;
           this.loading = false;
           console.log(`✅ ${utilisateurs.length} utilisateurs chargés`);
         },
         error: (error) => {
           console.error('❌ Erreur lors du chargement des utilisateurs:', error);
           this.error = 'Erreur lors du chargement des utilisateurs';
           this.loading = false;
         }
       });
     }

     /**
      * Vérifie si un utilisateur est actif
      */
     isActif(utilisateur: Utilisateur): boolean {
       return utilisateur.actif === true;
     }

     /**
      * Retourne la classe CSS pour le point de statut
      */
     getStatutClass(utilisateur: Utilisateur): string {
       return this.isActif(utilisateur) ? 'statut-actif' : 'statut-inactif';
     }

     /**
      * Retourne le texte du statut
      */
     getStatutText(utilisateur: Utilisateur): string {
       return this.isActif(utilisateur) ? 'Actif' : 'Inactif';
     }
   }
   ```

Crée ce composant avec ces fonctionnalités.
```

---

## Prompt 3 : Créer le Template HTML avec Points Colorés

```
Crée le template HTML pour afficher la liste des utilisateurs avec des points colorés :

1. **Créer le fichier** : `src/app/components/utilisateurs-list/utilisateurs-list.component.html`

2. **Structure HTML** :
   ```html
   <div class="utilisateurs-list-container">
     <h2>Liste des Utilisateurs</h2>
     
     <!-- Indicateur de chargement -->
     <div *ngIf="loading" class="loading">
       <p>Chargement des utilisateurs...</p>
     </div>
     
     <!-- Message d'erreur -->
     <div *ngIf="error" class="error">
       <p>{{ error }}</p>
     </div>
     
     <!-- Liste des utilisateurs -->
     <div *ngIf="!loading && !error && utilisateurs.length > 0" class="utilisateurs-list">
       <div *ngFor="let utilisateur of utilisateurs" class="utilisateur-item">
         <!-- Point de statut -->
         <span [class]="getStatutClass(utilisateur)" class="statut-dot"></span>
         
         <!-- Informations de l'utilisateur -->
         <div class="utilisateur-info">
           <h3>{{ utilisateur.prenom }} {{ utilisateur.nom }}</h3>
           <p class="role">{{ utilisateur.roleUtilisateur }}</p>
           <p class="email">{{ utilisateur.email }}</p>
           <p class="statut-text" [class.actif]="isActif(utilisateur)" [class.inactif]="!isActif(utilisateur)">
             {{ getStatutText(utilisateur) }}
           </p>
         </div>
       </div>
     </div>
     
     <!-- Message si aucun utilisateur -->
     <div *ngIf="!loading && !error && utilisateurs.length === 0" class="no-users">
       <p>Aucun utilisateur trouvé.</p>
     </div>
   </div>
   ```

3. **Structure alternative (plus compacte)** :
   ```html
   <div class="utilisateurs-list-container">
     <h2>Liste des Utilisateurs</h2>
     
     <div *ngIf="loading">Chargement...</div>
     <div *ngIf="error" class="error">{{ error }}</div>
     
     <table *ngIf="!loading && !error && utilisateurs.length > 0" class="utilisateurs-table">
       <thead>
         <tr>
           <th>Statut</th>
           <th>Nom</th>
           <th>Email</th>
           <th>Rôle</th>
           <th>Dernière Connexion</th>
         </tr>
       </thead>
       <tbody>
         <tr *ngFor="let utilisateur of utilisateurs">
           <td>
             <span [class]="getStatutClass(utilisateur)" class="statut-dot"></span>
             <span class="statut-text">{{ getStatutText(utilisateur) }}</span>
           </td>
           <td>{{ utilisateur.prenom }} {{ utilisateur.nom }}</td>
           <td>{{ utilisateur.email }}</td>
           <td>{{ utilisateur.roleUtilisateur }}</td>
           <td>{{ utilisateur.derniereConnexion | date:'short' }}</td>
         </tr>
       </tbody>
     </table>
     
     <div *ngIf="!loading && !error && utilisateurs.length === 0" class="no-users">
       <p>Aucun utilisateur trouvé.</p>
     </div>
   </div>
   ```

Crée ce template avec les points colorés.
```

---

## Prompt 4 : Créer les Styles CSS

```
Crée les styles CSS pour afficher les points colorés et la liste des utilisateurs :

1. **Créer le fichier** : `src/app/components/utilisateurs-list/utilisateurs-list.component.css`

2. **Styles pour les points de statut** :
   ```css
   /* Point de statut (cercle coloré) */
   .statut-dot {
     display: inline-block;
     width: 12px;
     height: 12px;
     border-radius: 50%;
     margin-right: 8px;
     vertical-align: middle;
   }

   /* Point vert pour actif */
   .statut-actif {
     background-color: #28a745; /* Vert */
     box-shadow: 0 0 4px rgba(40, 167, 69, 0.5);
   }

   /* Point rouge pour inactif */
   .statut-inactif {
     background-color: #dc3545; /* Rouge */
     box-shadow: 0 0 4px rgba(220, 53, 69, 0.5);
   }

   /* Animation pour le point actif (pulse) */
   .statut-actif {
     animation: pulse 2s infinite;
   }

   @keyframes pulse {
     0% {
       box-shadow: 0 0 0 0 rgba(40, 167, 69, 0.7);
     }
     70% {
       box-shadow: 0 0 0 10px rgba(40, 167, 69, 0);
     }
     100% {
       box-shadow: 0 0 0 0 rgba(40, 167, 69, 0);
     }
   }
   ```

3. **Styles pour la liste** :
   ```css
   .utilisateurs-list-container {
     padding: 20px;
   }

   .utilisateurs-list {
     display: flex;
     flex-direction: column;
     gap: 16px;
   }

   .utilisateur-item {
     display: flex;
     align-items: center;
     padding: 16px;
     background-color: #f8f9fa;
     border-radius: 8px;
     border-left: 4px solid #dee2e6;
     transition: all 0.3s ease;
   }

   .utilisateur-item:hover {
     background-color: #e9ecef;
     transform: translateX(4px);
   }

   .utilisateur-info {
     flex: 1;
   }

   .utilisateur-info h3 {
     margin: 0 0 4px 0;
     font-size: 18px;
     font-weight: 600;
   }

   .utilisateur-info .role {
     color: #6c757d;
     font-size: 14px;
     margin: 4px 0;
   }

   .utilisateur-info .email {
     color: #495057;
     font-size: 14px;
     margin: 4px 0;
   }

   .statut-text {
     font-size: 12px;
     font-weight: 600;
     text-transform: uppercase;
     margin-top: 8px;
   }

   .statut-text.actif {
     color: #28a745;
   }

   .statut-text.inactif {
     color: #dc3545;
   }

   /* Styles pour le tableau */
   .utilisateurs-table {
     width: 100%;
     border-collapse: collapse;
     margin-top: 20px;
   }

   .utilisateurs-table th,
   .utilisateurs-table td {
     padding: 12px;
     text-align: left;
     border-bottom: 1px solid #dee2e6;
   }

   .utilisateurs-table th {
     background-color: #f8f9fa;
     font-weight: 600;
   }

   .utilisateurs-table tr:hover {
     background-color: #f8f9fa;
   }

   /* Indicateur de chargement */
   .loading {
     text-align: center;
     padding: 40px;
     color: #6c757d;
   }

   /* Message d'erreur */
   .error {
     background-color: #f8d7da;
     color: #721c24;
     padding: 12px;
     border-radius: 4px;
     margin: 20px 0;
   }

   /* Message si aucun utilisateur */
   .no-users {
     text-align: center;
     padding: 40px;
     color: #6c757d;
   }
   ```

Crée ces styles avec les points colorés et les animations.
```

---

## Prompt 5 : Intégrer dans le Routing et la Navigation

```
Intègre le composant de liste des utilisateurs dans la navigation de l'application :

1. **Ajouter la route** dans `app-routing.module.ts` :
   ```typescript
   {
     path: 'utilisateurs',
     component: UtilisateursListComponent,
     canActivate: [AuthGuard] // Si vous avez un guard d'authentification
   }
   ```

2. **Ajouter le lien dans le menu** :
   - Pour les chefs : Afficher "Mes Agents" dans le menu
   - Pour le super admin : Afficher "Tous les Utilisateurs" dans le menu

3. **Exemple de menu** :
   ```html
   <!-- Dans le composant de navigation -->
   <nav>
     <ul>
       <li *ngIf="currentUser?.role === 'SUPER_ADMIN'">
         <a routerLink="/utilisateurs">Tous les Utilisateurs</a>
       </li>
       <li *ngIf="currentUser?.role?.startsWith('CHEF_')">
         <a routerLink="/utilisateurs">Mes Agents</a>
       </li>
     </ul>
   </nav>
   ```

4. **Vérifier les permissions** :
   - Seuls les chefs et super admin peuvent accéder à cette page
   - Afficher un message d'erreur si l'utilisateur n'a pas les permissions

Intègre le composant dans la navigation avec les bonnes permissions.
```

---

## Prompt 6 : Ajouter un Badge/Indicateur dans d'Autres Composants

```
Ajoute un indicateur de statut actif dans d'autres composants qui affichent des utilisateurs :

1. **Dans le composant de profil utilisateur** :
   - Afficher un point vert/rouge à côté du nom
   - Afficher "En ligne" ou "Hors ligne"

2. **Dans une liste de sélection d'agents** :
   - Afficher le point coloré dans le dropdown/select
   - Filtrer par défaut sur les agents actifs

3. **Code exemple pour un badge** :
   ```html
   <div class="user-badge">
     <span [class]="getStatutClass(user)" class="statut-dot"></span>
     <span>{{ user.prenom }} {{ user.nom }}</span>
     <span class="statut-badge" [class.actif]="isActif(user)" [class.inactif]="!isActif(user)">
       {{ getStatutText(user) }}
     </span>
   </div>
   ```

4. **Styles pour le badge** :
   ```css
   .user-badge {
     display: flex;
     align-items: center;
     gap: 8px;
   }

   .statut-badge {
     padding: 2px 8px;
     border-radius: 12px;
     font-size: 11px;
     font-weight: 600;
     text-transform: uppercase;
   }

   .statut-badge.actif {
     background-color: #d4edda;
     color: #155724;
   }

   .statut-badge.inactif {
     background-color: #f8d7da;
     color: #721c24;
   }
   ```

Ajoute ces indicateurs dans les autres composants qui affichent des utilisateurs.
```

---

## 📋 Code Complet de Référence

### Service Complet (`utilisateur.service.ts`)

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Utilisateur {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  roleUtilisateur: string;
  actif: boolean;
  derniereConnexion: string | null;
  derniereDeconnexion: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class UtilisateurService {
  private apiUrl = 'http://localhost:8089/carthage-creance/api';

  constructor(private http: HttpClient) {}

  /**
   * Récupère les agents d'un chef
   */
  getAgentsByChef(chefId: number): Observable<Utilisateur[]> {
    return this.http.get<Utilisateur[]>(`${this.apiUrl}/users/chef/${chefId}`);
  }

  /**
   * Récupère tous les utilisateurs (pour super admin)
   */
  getAllUtilisateurs(): Observable<Utilisateur[]> {
    return this.http.get<Utilisateur[]>(`${this.apiUrl}/users`);
  }

  /**
   * Récupère un utilisateur par ID
   */
  getUtilisateurById(userId: number): Observable<Utilisateur> {
    return this.http.get<Utilisateur>(`${this.apiUrl}/users/${userId}`);
  }
}
```

### Composant Complet (`utilisateurs-list.component.ts`)

```typescript
import { Component, OnInit } from '@angular/core';
import { UtilisateurService, Utilisateur } from '../../services/utilisateur.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-utilisateurs-list',
  templateUrl: './utilisateurs-list.component.html',
  styleUrls: ['./utilisateurs-list.component.css']
})
export class UtilisateursListComponent implements OnInit {
  utilisateurs: Utilisateur[] = [];
  currentUser: any;
  loading: boolean = false;
  error: string | null = null;

  constructor(
    private utilisateurService: UtilisateurService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.currentUser = this.authService.getCurrentUser();
    this.loadUtilisateurs();
  }

  loadUtilisateurs() {
    this.loading = true;
    this.error = null;

    let request: Observable<Utilisateur[]>;

    if (this.currentUser?.role === 'SUPER_ADMIN') {
      request = this.utilisateurService.getAllUtilisateurs();
    } else if (this.currentUser?.role?.startsWith('CHEF_')) {
      request = this.utilisateurService.getAgentsByChef(this.currentUser.id);
    } else {
      this.error = 'Accès non autorisé';
      this.loading = false;
      return;
    }

    request.subscribe({
      next: (utilisateurs) => {
        this.utilisateurs = utilisateurs;
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur:', error);
        this.error = 'Erreur lors du chargement';
        this.loading = false;
      }
    });
  }

  isActif(utilisateur: Utilisateur): boolean {
    return utilisateur.actif === true;
  }

  getStatutClass(utilisateur: Utilisateur): string {
    return this.isActif(utilisateur) ? 'statut-actif' : 'statut-inactif';
  }

  getStatutText(utilisateur: Utilisateur): string {
    return this.isActif(utilisateur) ? 'Actif' : 'Inactif';
  }
}
```

### Template HTML Complet (`utilisateurs-list.component.html`)

```html
<div class="utilisateurs-list-container">
  <h2>
    <span *ngIf="currentUser?.role === 'SUPER_ADMIN'">Tous les Utilisateurs</span>
    <span *ngIf="currentUser?.role?.startsWith('CHEF_')">Mes Agents</span>
  </h2>
  
  <div *ngIf="loading" class="loading">
    <p>Chargement des utilisateurs...</p>
  </div>
  
  <div *ngIf="error" class="error">
    <p>{{ error }}</p>
  </div>
  
  <div *ngIf="!loading && !error && utilisateurs.length > 0" class="utilisateurs-list">
    <div *ngFor="let utilisateur of utilisateurs" class="utilisateur-item">
      <span [class]="getStatutClass(utilisateur)" class="statut-dot"></span>
      
      <div class="utilisateur-info">
        <h3>{{ utilisateur.prenom }} {{ utilisateur.nom }}</h3>
        <p class="role">{{ utilisateur.roleUtilisateur }}</p>
        <p class="email">{{ utilisateur.email }}</p>
        <p class="statut-text" [class.actif]="isActif(utilisateur)" [class.inactif]="!isActif(utilisateur)">
          {{ getStatutText(utilisateur) }}
        </p>
      </div>
    </div>
  </div>
  
  <div *ngIf="!loading && !error && utilisateurs.length === 0" class="no-users">
    <p>Aucun utilisateur trouvé.</p>
  </div>
</div>
```

### Styles CSS Complets (`utilisateurs-list.component.css`)

```css
.utilisateurs-list-container {
  padding: 20px;
}

.statut-dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 8px;
  vertical-align: middle;
}

.statut-actif {
  background-color: #28a745;
  animation: pulse 2s infinite;
}

.statut-inactif {
  background-color: #dc3545;
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(40, 167, 69, 0.7); }
  70% { box-shadow: 0 0 0 10px rgba(40, 167, 69, 0); }
  100% { box-shadow: 0 0 0 0 rgba(40, 167, 69, 0); }
}

.utilisateurs-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.utilisateur-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #dee2e6;
}

.utilisateur-item:hover {
  background-color: #e9ecef;
}

.utilisateur-info h3 {
  margin: 0 0 4px 0;
  font-size: 18px;
  font-weight: 600;
}

.statut-text.actif {
  color: #28a745;
}

.statut-text.inactif {
  color: #dc3545;
}
```

---

## 📝 Checklist de Vérification

- [ ] Le service récupère les utilisateurs avec le champ `actif`
- [ ] Le composant filtre selon le rôle (chef vs super admin)
- [ ] Les points colorés s'affichent correctement (vert/rouge)
- [ ] Le texte "Actif"/"Inactif" s'affiche
- [ ] Les chefs voient uniquement leurs agents
- [ ] Le super admin voit tous les utilisateurs
- [ ] Les styles CSS sont appliqués correctement
- [ ] L'animation pulse fonctionne pour les utilisateurs actifs
- [ ] La page est accessible depuis le menu
- [ ] Les permissions sont vérifiées

---

Utilisez ces prompts pour créer l'affichage du statut d'activité dans le frontend ! ✅

