# 🔄 Prompt Frontend : Mise à Jour Relation Chef-Agent

## 📋 Contexte

Le backend a été modifié pour ajouter une relation explicite entre les chefs et les agents qu'ils créent. Chaque agent doit maintenant être associé à un chef créateur lors de sa création.

## ✅ Modifications Backend Effectuées

1. **Nouveau champ dans l'entité Utilisateur** :
   - `chefCreateur` : Relation ManyToOne vers le chef qui a créé l'agent
   - `chefId` : Champ transitoire pour faciliter l'envoi depuis le frontend

2. **Validation lors de la création** :
   - Un agent **DOIT** avoir un `chefCreateur` associé
   - Le backend valide que le créateur est bien un chef ou un super admin

3. **Endpoint `GET /api/users/chef/{chefId}` amélioré** :
   - Retourne maintenant uniquement les agents explicitement liés au chef via `chefCreateur`
   - Fallback sur le filtrage par rôle si la relation n'est pas encore renseignée (rétrocompatibilité)

---

## 🎯 Prompt Principal : Mettre à Jour le Frontend

```
Mets à jour le frontend Angular pour intégrer la nouvelle relation chef-agent :

1. **Mise à jour du formulaire de création d'agent** :
   - Lorsqu'un chef crée un agent, inclure automatiquement son ID dans la requête
   - Envoyer soit `chefId` soit `chefCreateur: { id: chefId }` dans le payload
   - Le champ `chefId` est préféré car plus simple

2. **Service Utilisateur** :
   - Modifier la méthode `createUtilisateur()` pour inclure le `chefId` du chef connecté
   - Récupérer l'ID du chef depuis le service d'authentification
   - Ajouter le `chefId` au payload avant l'envoi à l'API

3. **Composant de création d'agent** :
   - S'assurer que le `chefId` est automatiquement rempli avec l'ID du chef connecté
   - Ne pas demander à l'utilisateur de sélectionner un chef (c'est automatique)

4. **Affichage des agents par chef** :
   - L'endpoint `GET /api/users/chef/{chefId}` fonctionne déjà correctement
   - Vérifier que la liste des agents affichée correspond bien aux agents créés par le chef
   - Si aucun agent n'est affiché, vérifier que les agents existants ont bien un `chefCreateur` associé

5. **Gestion des erreurs** :
   - Si la création d'agent échoue avec "Un agent doit être rattaché à un chef créateur", 
     vérifier que le `chefId` est bien envoyé dans la requête
   - Afficher un message d'erreur clair à l'utilisateur

6. **Interface TypeScript** :
   - Mettre à jour l'interface `Utilisateur` pour inclure `chefCreateur?: Utilisateur` et `chefId?: number`
   - Ces champs sont optionnels dans l'interface mais `chefId` est obligatoire lors de la création d'un agent
```

---

## 📝 Détails d'Implémentation

### 1. Mise à Jour de l'Interface TypeScript

```typescript
// src/app/models/utilisateur.model.ts
export interface Utilisateur {
  id?: number;
  nom: string;
  prenom: string;
  email: string;
  motDePasse?: string;
  roleUtilisateur: RoleUtilisateur;
  actif?: boolean;
  derniereConnexion?: string;
  derniereDeconnexion?: string;
  
  // Nouveau : Relation chef-agent
  chefCreateur?: Utilisateur;  // Chef qui a créé cet agent
  chefId?: number;             // ID du chef (champ transitoire pour l'envoi)
  
  dateCreation?: Date;
}
```

### 2. Mise à Jour du Service Utilisateur

```typescript
// src/app/services/utilisateur.service.ts

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Utilisateur } from '../models/utilisateur.model';
import { AuthService } from './auth.service'; // Service d'authentification

@Injectable({
  providedIn: 'root'
})
export class UtilisateurService {
  private apiUrl = 'http://localhost:8080/api/users';

  constructor(
    private http: HttpClient,
    private authService: AuthService  // Injecter le service d'authentification
  ) {}

  /**
   * Crée un nouvel utilisateur (agent)
   * Ajoute automatiquement le chefId du chef connecté si c'est un agent
   */
  createUtilisateur(utilisateur: Utilisateur): Observable<any> {
    // Si c'est un agent, ajouter automatiquement le chefId
    if (this.isAgent(utilisateur.roleUtilisateur)) {
      const currentUser = this.authService.getCurrentUser();
      if (currentUser && this.isChef(currentUser.roleUtilisateur)) {
        utilisateur.chefId = currentUser.id;
      } else if (currentUser && currentUser.roleUtilisateur === 'SUPER_ADMIN') {
        // Le super admin peut aussi créer des agents
        utilisateur.chefId = currentUser.id;
      } else {
        throw new Error('Seuls les chefs et super admins peuvent créer des agents');
      }
    }

    return this.http.post<any>(`${this.apiUrl}`, utilisateur);
  }

  /**
   * Vérifie si un rôle est un agent
   */
  private isAgent(role: string): boolean {
    return role && role.startsWith('AGENT_');
  }

  /**
   * Vérifie si un rôle est un chef
   */
  private isChef(role: string): boolean {
    return role && role.startsWith('CHEF_');
  }

  /**
   * Récupère les agents d'un chef
   * GET /api/users/chef/{chefId}
   */
  getAgentsByChef(chefId: number): Observable<Utilisateur[]> {
    return this.http.get<Utilisateur[]>(`${this.apiUrl}/chef/${chefId}`);
  }

  // ... autres méthodes existantes
}
```

### 3. Mise à Jour du Composant de Création d'Agent

```typescript
// src/app/components/create-agent/create-agent.component.ts

import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { UtilisateurService } from '../../services/utilisateur.service';
import { AuthService } from '../../services/auth.service';
import { RoleUtilisateur } from '../../models/role-utilisateur.enum';

@Component({
  selector: 'app-create-agent',
  templateUrl: './create-agent.component.html',
  styleUrls: ['./create-agent.component.css']
})
export class CreateAgentComponent implements OnInit {
  agentForm: FormGroup;
  roles: string[] = [
    'AGENT_DOSSIER',
    'AGENT_RECOUVREMENT_AMIABLE',
    'AGENT_RECOUVREMENT_JURIDIQUE',
    'AGENT_FINANCE'
  ];

  constructor(
    private fb: FormBuilder,
    private utilisateurService: UtilisateurService,
    private authService: AuthService,
    private router: Router
  ) {
    this.agentForm = this.fb.group({
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      motDePasse: ['', [Validators.required, Validators.minLength(6)]],
      roleUtilisateur: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    // Vérifier que l'utilisateur connecté est un chef ou super admin
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser || (!this.isChef(currentUser.roleUtilisateur) && currentUser.roleUtilisateur !== 'SUPER_ADMIN')) {
      this.router.navigate(['/unauthorized']);
    }
  }

  onSubmit(): void {
    if (this.agentForm.valid) {
      const agentData = {
        ...this.agentForm.value,
        // Le chefId sera ajouté automatiquement par le service
      };

      this.utilisateurService.createUtilisateur(agentData).subscribe({
        next: (response) => {
          console.log('Agent créé avec succès:', response);
          // Rediriger vers la liste des agents
          this.router.navigate(['/agents']);
        },
        error: (error) => {
          console.error('Erreur lors de la création de l\'agent:', error);
          if (error.error?.message) {
            alert('Erreur: ' + error.error.message);
          } else {
            alert('Erreur lors de la création de l\'agent. Veuillez réessayer.');
          }
        }
      });
    }
  }

  private isChef(role: string): boolean {
    return role && role.startsWith('CHEF_');
  }
}
```

### 4. Mise à Jour du Composant Liste des Agents

```typescript
// src/app/components/agents-list/agents-list.component.ts

import { Component, OnInit } from '@angular/core';
import { UtilisateurService } from '../../services/utilisateur.service';
import { AuthService } from '../../services/auth.service';
import { Utilisateur } from '../../models/utilisateur.model';

@Component({
  selector: 'app-agents-list',
  templateUrl: './agents-list.component.html',
  styleUrls: ['./agents-list.component.css']
})
export class AgentsListComponent implements OnInit {
  agents: Utilisateur[] = [];
  loading = false;
  error: string | null = null;

  constructor(
    private utilisateurService: UtilisateurService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadAgents();
  }

  loadAgents(): void {
    this.loading = true;
    this.error = null;

    const currentUser = this.authService.getCurrentUser();
    
    if (!currentUser) {
      this.error = 'Utilisateur non connecté';
      this.loading = false;
      return;
    }

    // Si c'est un chef, récupérer ses agents
    if (this.isChef(currentUser.roleUtilisateur)) {
      this.utilisateurService.getAgentsByChef(currentUser.id!).subscribe({
        next: (agents) => {
          this.agents = agents;
          this.loading = false;
        },
        error: (error) => {
          console.error('Erreur lors du chargement des agents:', error);
          this.error = 'Erreur lors du chargement des agents';
          this.loading = false;
        }
      });
    } 
    // Si c'est un super admin, récupérer tous les agents
    else if (currentUser.roleUtilisateur === 'SUPER_ADMIN') {
      this.utilisateurService.getAllAgents().subscribe({
        next: (agents) => {
          this.agents = agents;
          this.loading = false;
        },
        error: (error) => {
          console.error('Erreur lors du chargement des agents:', error);
          this.error = 'Erreur lors du chargement des agents';
          this.loading = false;
        }
      });
    } else {
      this.error = 'Accès non autorisé';
      this.loading = false;
    }
  }

  private isChef(role: string): boolean {
    return role && role.startsWith('CHEF_');
  }

  /**
   * Affiche le statut actif avec un point coloré
   */
  getStatusIcon(actif: boolean | undefined): string {
    return actif ? '🟢' : '🔴';
  }

  getStatusText(actif: boolean | undefined): string {
    return actif ? 'Actif' : 'Inactif';
  }
}
```

### 5. Template HTML pour la Liste des Agents

```html
<!-- src/app/components/agents-list/agents-list.component.html -->

<div class="agents-container">
  <h2>Liste des Agents</h2>

  <div *ngIf="loading" class="loading">
    Chargement en cours...
  </div>

  <div *ngIf="error" class="error">
    {{ error }}
  </div>

  <div *ngIf="!loading && !error">
    <table class="table">
      <thead>
        <tr>
          <th>Statut</th>
          <th>Nom</th>
          <th>Prénom</th>
          <th>Email</th>
          <th>Rôle</th>
          <th>Dernière Connexion</th>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let agent of agents">
          <td>
            <span [class]="agent.actif ? 'status-active' : 'status-inactive'">
              {{ getStatusIcon(agent.actif) }}
            </span>
            {{ getStatusText(agent.actif) }}
          </td>
          <td>{{ agent.nom }}</td>
          <td>{{ agent.prenom }}</td>
          <td>{{ agent.email }}</td>
          <td>{{ agent.roleUtilisateur }}</td>
          <td>{{ agent.derniereConnexion | date:'short' }}</td>
        </tr>
      </tbody>
    </table>

    <div *ngIf="agents.length === 0" class="no-agents">
      Aucun agent trouvé.
    </div>
  </div>
</div>
```

### 6. Styles CSS pour le Statut

```css
/* src/app/components/agents-list/agents-list.component.css */

.status-active {
  color: #28a745;
  font-weight: bold;
}

.status-inactive {
  color: #dc3545;
  font-weight: bold;
}

.loading {
  text-align: center;
  padding: 20px;
}

.error {
  color: #dc3545;
  padding: 10px;
  background-color: #f8d7da;
  border-radius: 4px;
  margin: 10px 0;
}

.no-agents {
  text-align: center;
  padding: 20px;
  color: #6c757d;
}
```

---

## 🔍 Points de Vérification

### ✅ Checklist de Mise à Jour

- [ ] Interface `Utilisateur` mise à jour avec `chefCreateur?` et `chefId?`
- [ ] Service `UtilisateurService.createUtilisateur()` ajoute automatiquement le `chefId`
- [ ] Composant de création d'agent envoie le `chefId` (via le service)
- [ ] Composant liste des agents utilise `getAgentsByChef(chefId)` pour les chefs
- [ ] Gestion des erreurs si `chefId` manquant lors de la création
- [ ] Affichage du statut actif avec points colorés (vert/rouge)
- [ ] Test de création d'agent par un chef
- [ ] Test d'affichage des agents par chef
- [ ] Test avec super admin (doit voir tous les agents)

---

## 🐛 Dépannage

### Problème : "Un agent doit être rattaché à un chef créateur"

**Solution** : Vérifier que :
1. Le service `AuthService.getCurrentUser()` retourne bien l'utilisateur connecté
2. Le `chefId` est bien ajouté dans `createUtilisateur()` avant l'envoi
3. L'utilisateur connecté est bien un chef ou super admin

### Problème : Aucun agent affiché pour un chef

**Solution** : 
1. Vérifier que les agents existants ont bien un `chefCreateur` associé dans la base de données
2. Si non, mettre à jour les agents existants avec leur chef créateur (migration SQL ou via l'interface admin)
3. L'endpoint `GET /api/users/chef/{chefId}` retourne une liste vide si aucun agent n'est lié au chef

### Problème : Le chef voit tous les agents au lieu de seulement les siens

**Solution** :
1. Vérifier que l'endpoint utilisé est bien `GET /api/users/chef/{chefId}` et non `GET /api/users/agents`
2. Vérifier que le `chefId` passé correspond bien à l'ID du chef connecté

---

## 📚 API Endpoints Utilisés

### POST /api/users
**Création d'un utilisateur (agent)**

**Body** :
```json
{
  "nom": "Dupont",
  "prenom": "Jean",
  "email": "jean.dupont@example.com",
  "motDePasse": "password123",
  "roleUtilisateur": "AGENT_DOSSIER",
  "chefId": 46  // ⚠️ OBLIGATOIRE pour les agents
}
```

**Réponse** :
```json
{
  "token": "jwt_token_here",
  "userId": 50,
  "email": "jean.dupont@example.com",
  "nom": "Dupont",
  "prenom": "Jean",
  "role": "AGENT_DOSSIER"
}
```

### GET /api/users/chef/{chefId}
**Récupération des agents d'un chef**

**Réponse** :
```json
[
  {
    "id": 50,
    "nom": "Dupont",
    "prenom": "Jean",
    "email": "jean.dupont@example.com",
    "roleUtilisateur": "AGENT_DOSSIER",
    "actif": true,
    "derniereConnexion": "2024-01-15T10:30:00",
    "derniereDeconnexion": null,
    "chefCreateur": {
      "id": 46,
      "nom": "Chef",
      "prenom": "Dossier",
      "email": "chef.dossier@example.com"
    }
  }
]
```

---

## 🎯 Résumé des Actions Requises

1. **Mettre à jour l'interface TypeScript** `Utilisateur` avec `chefCreateur?` et `chefId?`
2. **Modifier le service** pour ajouter automatiquement le `chefId` lors de la création d'un agent
3. **Vérifier les composants** de création et liste pour utiliser la nouvelle logique
4. **Tester** la création d'agent et l'affichage des agents par chef
5. **Gérer les erreurs** si le `chefId` est manquant

---

## ✅ Résultat Attendu

- ✅ Un chef peut créer un agent et celui-ci est automatiquement associé au chef
- ✅ Un chef voit uniquement les agents qu'il a créés
- ✅ Le super admin voit tous les agents
- ✅ Le statut actif est affiché avec des points colorés (vert/rouge)
- ✅ Les erreurs sont gérées et affichées clairement à l'utilisateur

