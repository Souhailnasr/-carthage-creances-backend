# Guide d'Intégration Frontend - Système de Notifications, Tâches, Statistiques et Performance

## Vue d'ensemble

Ce document fournit tous les prompts et instructions nécessaires pour intégrer le système avancé de notifications, tâches, statistiques et performance dans le frontend.

## 1. Système de Notifications Avancé

### 1.1 Endpoints Disponibles

#### Récupérer les notifications d'un utilisateur
```
GET /api/notifications/user/{userId}
Authorization: Bearer {token}
```

#### Récupérer les notifications non lues
```
GET /api/notifications/user/{userId}/non-lues
Authorization: Bearer {token}
```

#### Marquer une notification comme lue
```
PUT /api/notifications/{notificationId}/marquer-lue
Authorization: Bearer {token}
```

#### Marquer toutes les notifications comme lues
```
PUT /api/notifications/user/{userId}/marquer-toutes-lues
Authorization: Bearer {token}
```

#### Envoyer une notification à plusieurs utilisateurs (Chef)
```
POST /api/notifications/envoyer-multiples
Authorization: Bearer {token}
Content-Type: application/json

{
  "userIds": [1, 2, 3],
  "type": "NOTIFICATION_MANUELLE",
  "titre": "Titre de la notification",
  "message": "Message de la notification",
  "entiteId": null,
  "entiteType": null
}
```

#### Envoyer une notification à tous les agents d'un chef
```
POST /api/notifications/chef/{chefId}/agents
Authorization: Bearer {token}
Content-Type: application/json

{
  "type": "NOTIFICATION_MANUELLE",
  "titre": "Titre de la notification",
  "message": "Message de la notification",
  "entiteId": null,
  "entiteType": null
}
```

#### Envoyer une notification à tous les utilisateurs (Super Admin)
```
POST /api/notifications/envoyer-tous
Authorization: Bearer {token}
Content-Type: application/json

{
  "type": "NOTIFICATION_MANUELLE",
  "titre": "Titre de la notification",
  "message": "Message de la notification",
  "entiteId": null,
  "entiteType": null
}
```

### 1.2 Types de Notifications

Les notifications automatiques sont créées pour:
- **DOSSIER_CREE**: Lors de la création d'un dossier
- **DOSSIER_AFFECTE**: Lors de l'affectation d'un dossier à un agent
- **DOSSIER_VALIDE**: Lors de la validation d'un dossier
- **DOSSIER_REJETE**: Lors du rejet d'un dossier
- **ACTION_AMIABLE_CREE**: Lors de la création d'une action amiable
- **AUDIENCE_PROCHAINE**: Pour les audiences dans les 7 prochains jours
- **AUDIENCE_CREE**: Lors de la création d'une audience
- **TACHE_AFFECTEE**: Lors de l'affectation d'une tâche
- **TACHE_COMPLETEE**: Lors de la complétion d'une tâche
- **TRAITEMENT_DOSSIER**: Lors du traitement d'un dossier

### 1.3 Structure de la Notification

```typescript
interface Notification {
  id: number;
  utilisateur: {
    id: number;
    nom: string;
    prenom: string;
    email: string;
  };
  type: string; // TypeNotification enum
  titre: string;
  message: string;
  statut: 'NON_LUE' | 'LUE';
  dateCreation: string; // ISO 8601
  dateLecture?: string; // ISO 8601
  entiteId?: number;
  entiteType?: string; // TypeEntite enum
  lienAction?: string;
}
```

### 1.4 Prompt Frontend - Service de Notifications

```typescript
// Créer un service Angular pour les notifications
// notifications.service.ts

import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private apiUrl = `${environment.apiUrl}/api/notifications`;

  constructor(private http: HttpClient) {}

  getNotifications(userId: number): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.apiUrl}/user/${userId}`);
  }

  getNotificationsNonLues(userId: number): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.apiUrl}/user/${userId}/non-lues`);
  }

  marquerLue(notificationId: number): Observable<Notification> {
    return this.http.put<Notification>(`${this.apiUrl}/${notificationId}/marquer-lue`, {});
  }

  marquerToutesLues(userId: number): Observable<{ count: number }> {
    return this.http.put<{ count: number }>(`${this.apiUrl}/user/${userId}/marquer-toutes-lues`, {});
  }

  envoyerNotificationMultiples(data: {
    userIds: number[];
    type: string;
    titre: string;
    message: string;
    entiteId?: number;
    entiteType?: string;
  }): Observable<{ count: number }> {
    return this.http.post<{ count: number }>(`${this.apiUrl}/envoyer-multiples`, data);
  }

  envoyerNotificationAAgentsChef(chefId: number, data: {
    type: string;
    titre: string;
    message: string;
    entiteId?: number;
    entiteType?: string;
  }): Observable<{ count: number }> {
    return this.http.post<{ count: number }>(`${this.apiUrl}/chef/${chefId}/agents`, data);
  }

  envoyerNotificationATous(data: {
    type: string;
    titre: string;
    message: string;
    entiteId?: number;
    entiteType?: string;
  }): Observable<{ count: number }> {
    return this.http.post<{ count: number }>(`${this.apiUrl}/envoyer-tous`, data);
  }

  getNombreNotificationsNonLues(userId: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/user/${userId}/count/non-lues`);
  }
}
```

### 1.5 Prompt Frontend - Composant de Notifications

```typescript
// notification-bell.component.ts

import { Component, OnInit, OnDestroy } from '@angular/core';
import { NotificationService } from '../services/notification.service';
import { AuthService } from '../services/auth.service';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-notification-bell',
  templateUrl: './notification-bell.component.html',
  styleUrls: ['./notification-bell.component.css']
})
export class NotificationBellComponent implements OnInit, OnDestroy {
  notifications: Notification[] = [];
  nombreNonLues = 0;
  private subscription: Subscription;

  constructor(
    private notificationService: NotificationService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.chargerNotifications();
    // Rafraîchir toutes les 30 secondes
    this.subscription = interval(30000).subscribe(() => {
      this.chargerNotifications();
    });
  }

  ngOnDestroy() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  chargerNotifications() {
    const userId = this.authService.getCurrentUser()?.id;
    if (userId) {
      this.notificationService.getNotificationsNonLues(userId).subscribe(
        notifications => {
          this.notifications = notifications;
          this.nombreNonLues = notifications.length;
        }
      );
    }
  }

  marquerLue(notification: Notification) {
    this.notificationService.marquerLue(notification.id).subscribe(() => {
      this.chargerNotifications();
    });
  }

  marquerToutesLues() {
    const userId = this.authService.getCurrentUser()?.id;
    if (userId) {
      this.notificationService.marquerToutesLues(userId).subscribe(() => {
        this.chargerNotifications();
      });
    }
  }

  naviguerVersLien(notification: Notification) {
    if (notification.lienAction) {
      // Utiliser le router Angular pour naviguer
      this.router.navigate([notification.lienAction]);
      this.marquerLue(notification);
    }
  }
}
```

## 2. Système de Tâches Avancé

### 2.1 Endpoints Disponibles

#### Créer une tâche (Chef ou Super Admin)
```
POST /api/taches-urgentes
Authorization: Bearer {token}
Content-Type: application/json

{
  "titre": "Titre de la tâche",
  "description": "Description de la tâche",
  "type": "DOSSIER",
  "priorite": "HAUTE",
  "agentAssigné": { "id": 1 },
  "chefCreateur": { "id": 2 },
  "dateEcheance": "2024-12-31T23:59:59",
  "dossier": { "id": 1 } // optionnel
}
```

#### Affecter une tâche à plusieurs agents (Chef)
```
POST /api/taches-urgentes/affecter-multiples
Authorization: Bearer {token}
Content-Type: application/json

{
  "titre": "Titre de la tâche",
  "description": "Description de la tâche",
  "type": "DOSSIER",
  "priorite": "HAUTE",
  "agentIds": [1, 2, 3],
  "chefCreateurId": 2,
  "dateEcheance": "2024-12-31T23:59:59"
}
```

#### Affecter une tâche à tous les agents d'un chef
```
POST /api/taches-urgentes/chef/{chefId}/affecter-agents
Authorization: Bearer {token}
Content-Type: application/json

{
  "titre": "Titre de la tâche",
  "description": "Description de la tâche",
  "type": "DOSSIER",
  "priorite": "HAUTE",
  "dateEcheance": "2024-12-31T23:59:59"
}
```

#### Affecter une tâche à tous les utilisateurs (Super Admin)
```
POST /api/taches-urgentes/super-admin/affecter-tous
Authorization: Bearer {token}
Content-Type: application/json

{
  "titre": "Titre de la tâche",
  "description": "Description de la tâche",
  "type": "DOSSIER",
  "priorite": "HAUTE",
  "dateEcheance": "2024-12-31T23:59:59"
}
```

#### Récupérer les tâches d'un agent
```
GET /api/taches-urgentes/agent/{agentId}
Authorization: Bearer {token}
```

#### Récupérer les tâches d'un chef
```
GET /api/taches-urgentes/chef/{chefId}
Authorization: Bearer {token}
```

#### Marquer une tâche comme terminée
```
PUT /api/taches-urgentes/{tacheId}/terminer
Authorization: Bearer {token}
Content-Type: application/json

{
  "commentaires": "Tâche terminée avec succès"
}
```

### 2.2 Structure de la Tâche

```typescript
interface TacheUrgente {
  id: number;
  titre: string;
  description: string;
  type: 'ENQUETE' | 'RELANCE' | 'DOSSIER' | 'AUDIENCE' | 'ACTION' | 'ACTION_AMIABLE' | 'VALIDATION' | 'TRAITEMENT' | 'SUIVI' | 'RAPPEL';
  priorite: 'BASSE' | 'MOYENNE' | 'HAUTE' | 'URGENTE';
  statut: 'EN_ATTENTE' | 'EN_COURS' | 'TERMINEE' | 'ANNULEE';
  agentAssigné: {
    id: number;
    nom: string;
    prenom: string;
  };
  chefCreateur: {
    id: number;
    nom: string;
    prenom: string;
  };
  dateCreation: string; // ISO 8601
  dateEcheance: string; // ISO 8601
  dateCompletion?: string; // ISO 8601
  dossier?: { id: number; numeroDossier: string };
  commentaires?: string;
}
```

## 3. Système de Statistiques Avancé

### 3.1 Endpoints Disponibles

#### Récupérer les statistiques globales
```
GET /api/statistiques/globales
Authorization: Bearer {token}
```

#### Récupérer les statistiques d'un agent
```
GET /api/statistiques/agent/{agentId}
Authorization: Bearer {token}
```

#### Récupérer les statistiques d'un chef et de ses agents
```
GET /api/statistiques/chef/{chefId}
Authorization: Bearer {token}
```

#### Récupérer les statistiques de tous les chefs (Super Admin)
```
GET /api/statistiques/chefs
Authorization: Bearer {token}
```

#### Récupérer les statistiques par période
```
GET /api/statistiques/periode?dateDebut=2024-01-01&dateFin=2024-12-31
Authorization: Bearer {token}
```

### 3.2 Structure des Statistiques

```typescript
interface StatistiquesGlobales {
  totalDossiers: number;
  dossiersEnCours: number;
  dossiersValides: number;
  dossiersRejetes: number;
  dossiersClotures: number;
  dossiersCreesCeMois: number;
  totalEnquetes: number;
  enquetesCompletees: number;
  totalActionsAmiables: number;
  totalAudiences: number;
  audiencesProchaines: number;
  totalTaches: number;
  tachesCompletees: number;
  tachesEnCours: number;
  tauxReussiteGlobal: number;
  montantRecouvre: number;
  montantEnCours: number;
}

interface StatistiquesAgent {
  agentId: number;
  agentNom: string;
  agentPrenom: string;
  dossiersCrees: number;
  dossiersAssignes: number;
  dossiersValides: number;
  enquetesCompletees: number;
  tachesCompletees: number;
  actionsAmiables: number;
  audiencesGerees: number;
  scorePerformance: number;
  tauxReussite: number;
}

interface StatistiquesChef {
  chefId: number;
  chefNom: string;
  chefPrenom: string;
  nombreAgents: number;
  statistiquesAgents: StatistiquesAgent[];
  moyenneScoreAgents: number;
  totalDossiersAgents: number;
  totalTachesAgents: number;
}
```

## 4. Système de Performance Avancé

### 4.1 Endpoints Disponibles

#### Récupérer les performances d'un agent
```
GET /api/performance-agents/agent/{agentId}
Authorization: Bearer {token}
```

#### Récupérer les performances des agents d'un chef
```
GET /api/performance-agents/chef/{chefId}/agents
Authorization: Bearer {token}
```

#### Récupérer toutes les performances (Super Admin)
```
GET /api/performance-agents/tous
Authorization: Bearer {token}
```

#### Calculer les performances pour une période
```
POST /api/performance-agents/calculer/periode/{periode}
Authorization: Bearer {token}
```

### 4.2 Structure de la Performance

```typescript
interface PerformanceAgent {
  id: number;
  agent: {
    id: number;
    nom: string;
    prenom: string;
    email: string;
  };
  periode: string; // Format: "2024-01" ou "2024-Q1"
  dossiersTraites: number;
  dossiersValides: number;
  enquetesCompletees: number;
  score: number; // 0-100
  tauxReussite: number; // 0-100
  dateCalcul: string; // ISO 8601
  commentaires?: string;
  objectif?: number;
}
```

## 5. Intégration dans les Services Existants

### 5.1 Notifications Automatiques

Les notifications sont automatiquement créées lors de:
- Création d'un dossier → Notifie le créateur et les chefs
- Affectation d'un dossier → Notifie l'agent assigné
- Création d'une action amiable → Notifie l'agent responsable
- Création d'une audience → Notifie l'agent responsable
- Audience prochaine (dans 7 jours) → Notifie automatiquement tous les jours à 8h
- Validation d'un dossier → Notifie le créateur et l'agent responsable
- Création d'une tâche → Notifie l'agent assigné
- Complétion d'une tâche → Notifie le chef créateur

### 5.2 Calcul de Performance

Le calcul de performance inclut:
- **Dossiers traités** (30%): Dossiers créés ou assignés à l'agent
- **Enquêtes complétées** (20%): Enquêtes créées ou assignées à l'agent
- **Tâches complétées** (20%): Taux de complétion des tâches
- **Actions amiables** (15%): Nombre d'actions créées
- **Audiences gérées** (15%): Nombre d'audiences gérées

## 6. Exemple d'Intégration Complète

```typescript
// dashboard.component.ts

import { Component, OnInit } from '@angular/core';
import { StatistiqueService } from '../services/statistique.service';
import { PerformanceService } from '../services/performance.service';
import { NotificationService } from '../services/notification.service';
import { TacheService } from '../services/tache.service';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  statistiques: StatistiquesGlobales;
  performances: PerformanceAgent[];
  notifications: Notification[];
  taches: TacheUrgente[];
  userRole: string;

  constructor(
    private statistiqueService: StatistiqueService,
    private performanceService: PerformanceService,
    private notificationService: NotificationService,
    private tacheService: TacheService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.userRole = this.authService.getCurrentUser()?.roleUtilisateur;
    this.chargerDonnees();
  }

  chargerDonnees() {
    // Charger les statistiques selon le rôle
    if (this.userRole === 'SUPER_ADMIN') {
      this.statistiqueService.getStatistiquesGlobales().subscribe(
        stats => this.statistiques = stats
      );
      this.performanceService.getToutesPerformances().subscribe(
        perfs => this.performances = perfs
      );
    } else if (this.userRole?.startsWith('CHEF_')) {
      const chefId = this.authService.getCurrentUser()?.id;
      this.statistiqueService.getStatistiquesChef(chefId).subscribe(
        stats => this.statistiques = stats
      );
      this.performanceService.getPerformancesChef(chefId).subscribe(
        perfs => this.performances = perfs
      );
    } else {
      const agentId = this.authService.getCurrentUser()?.id;
      this.statistiqueService.getStatistiquesAgent(agentId).subscribe(
        stats => this.statistiques = stats
      );
      this.performanceService.getPerformancesAgent(agentId).subscribe(
        perfs => this.performances = perfs
      );
    }

    // Charger les notifications
    const userId = this.authService.getCurrentUser()?.id;
    this.notificationService.getNotificationsNonLues(userId).subscribe(
      notifs => this.notifications = notifs
    );

    // Charger les tâches
    if (this.userRole?.startsWith('CHEF_') || this.userRole === 'SUPER_ADMIN') {
      const userId = this.authService.getCurrentUser()?.id;
      this.tacheService.getTachesChef(userId).subscribe(
        taches => this.taches = taches
      );
    } else {
      const agentId = this.authService.getCurrentUser()?.id;
      this.tacheService.getTachesAgent(agentId).subscribe(
        taches => this.taches = taches
      );
    }
  }
}
```

## 7. Notes Importantes

1. **Authentification**: Tous les endpoints nécessitent un token JWT dans le header `Authorization: Bearer {token}`
2. **Permissions**: 
   - Les chefs peuvent envoyer des notifications et affecter des tâches à leurs agents
   - Le super admin peut envoyer des notifications et affecter des tâches à tous les utilisateurs
   - Les agents peuvent voir leurs propres statistiques et performances
3. **Notifications Automatiques**: Les notifications sont créées automatiquement par le backend, pas besoin de les créer manuellement
4. **Performance**: Le calcul de performance est automatique et se base sur toutes les entités (dossiers, enquêtes, tâches, actions, audiences)
5. **Statistiques**: Toutes les statistiques sont calculées en temps réel à partir des données de la base de données

## 8. Prochaines Étapes

1. Créer les services Angular pour chaque module (notifications, tâches, statistiques, performance)
2. Créer les composants UI pour afficher les données
3. Intégrer les notifications dans le header avec un badge de compteur
4. Créer un dashboard personnalisé selon le rôle de l'utilisateur
5. Implémenter les formulaires pour créer des notifications et tâches manuelles
6. Ajouter des graphiques pour visualiser les statistiques et performances

