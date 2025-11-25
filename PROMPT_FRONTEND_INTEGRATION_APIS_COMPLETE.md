# 📘 Guide Complet d'Intégration Frontend - Système de Recouvrement Tunisien

## 📋 Table des Matières

1. [Vue d'Ensemble](#vue-densemble)
2. [Configuration de Base](#configuration-de-base)
3. [APIs - Montants et États de Dossier](#apis-montants-et-états-de-dossier)
4. [APIs - Documents Huissier (Phase 1 & 2)](#apis-documents-huissier)
5. [APIs - Actions Huissier (Phase 3)](#apis-actions-huissier)
6. [APIs - Notifications](#apis-notifications)
7. [APIs - Recommandations](#apis-recommandations)
8. [APIs - Audit Logs](#apis-audit-logs)
9. [Exemples d'Intégration Complète](#exemples-dintégration-complète)
10. [Gestion des Erreurs](#gestion-des-erreurs)
11. [Bonnes Pratiques](#bonnes-pratiques)

---

## 🎯 Vue d'Ensemble

Ce document fournit toutes les informations nécessaires pour intégrer les nouvelles fonctionnalités du système de recouvrement tunisien dans votre application frontend.

### Base URL
```
http://localhost:8080/api
```

### Authentification
Tous les endpoints nécessitent une authentification. Inclure le token JWT dans les headers :
```typescript
headers: {
  'Authorization': `Bearer ${token}`,
  'Content-Type': 'application/json'
}
```

---

## ⚙️ Configuration de Base

### Service API de Base (TypeScript/Angular)

```typescript
// services/api.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = environment.apiUrl || 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  get<T>(endpoint: string, params?: any): Observable<T> {
    let httpParams = new HttpParams();
    if (params) {
      Object.keys(params).forEach(key => {
        httpParams = httpParams.set(key, params[key]);
      });
    }
    return this.http.get<T>(`${this.baseUrl}${endpoint}`, {
      headers: this.getHeaders(),
      params: httpParams
    });
  }

  post<T>(endpoint: string, body: any): Observable<T> {
    return this.http.post<T>(`${this.baseUrl}${endpoint}`, body, {
      headers: this.getHeaders()
    });
  }

  put<T>(endpoint: string, body: any): Observable<T> {
    return this.http.put<T>(`${this.baseUrl}${endpoint}`, body, {
      headers: this.getHeaders()
    });
  }

  delete<T>(endpoint: string): Observable<T> {
    return this.http.delete<T>(`${this.baseUrl}${endpoint}`, {
      headers: this.getHeaders()
    });
  }
}
```

### Interfaces TypeScript

```typescript
// models/dossier.model.ts
export interface Dossier {
  id: number;
  titre: string;
  description?: string;
  numeroDossier: string;
  montantCreance: number;
  // Nouveaux champs
  montantTotal: number;
  montantRecouvre: number;
  montantRestant: number;
  etatDossier: 'RECOVERED_TOTAL' | 'RECOVERED_PARTIAL' | 'NOT_RECOVERED';
  // ... autres champs existants
}

// models/montant-dossier.model.ts
export interface MontantDossierDTO {
  montantTotal?: number;
  montantRecouvre?: number;
  updateMode: 'ADD' | 'SET';
}

// models/action-amiable.model.ts
export interface ActionAmiableDTO {
  montantRecouvre: number;
}

// models/document-huissier.model.ts
export type TypeDocumentHuissier = 
  | 'PV_MISE_EN_DEMEURE' 
  | 'ORDONNANCE_PAIEMENT' 
  | 'PV_NOTIFICATION_ORDONNANCE';

export type StatutDocumentHuissier = 'PENDING' | 'EXPIRED' | 'COMPLETED';

export interface DocumentHuissier {
  id: number;
  dossierId: number;
  typeDocument: TypeDocumentHuissier;
  dateCreation: string; // ISO 8601
  delaiLegalDays: number;
  pieceJointeUrl?: string;
  huissierName: string;
  status: StatutDocumentHuissier;
  notified: boolean;
}

export interface DocumentHuissierDTO {
  dossierId: number;
  typeDocument: TypeDocumentHuissier;
  huissierName: string;
  pieceJointeUrl?: string;
  dateCreation?: string;
  delaiLegalDays?: number;
}

// models/action-huissier.model.ts
export type TypeActionHuissier = 
  | 'ACLA_TA7AFOUDHIA'   // Saisie conservatoire
  | 'ACLA_TANFITHIA'      // Saisie exécutive
  | 'ACLA_TAW9IFIYA'      // Saisie de blocage
  | 'ACLA_A9ARYA';        // Saisie immobilière

export interface ActionHuissier {
  id: number;
  dossierId: number;
  typeAction: TypeActionHuissier;
  montantRecouvre?: number;
  montantRestant?: number;
  etatDossier?: 'RECOVERED_TOTAL' | 'RECOVERED_PARTIAL' | 'NOT_RECOVERED';
  dateAction: string;
  pieceJointeUrl?: string;
  huissierName: string;
}

export interface ActionHuissierDTO {
  dossierId: number;
  typeAction: TypeActionHuissier;
  montantRecouvre?: number;
  huissierName: string;
  pieceJointeUrl?: string;
  updateMode?: 'ADD' | 'SET';
}

// models/notification-huissier.model.ts
export type TypeNotificationHuissier = 
  | 'DELAY_WARNING'
  | 'DELAY_EXPIRED'
  | 'ACTION_PERFORMED'
  | 'AMIABLE_RESPONSE_POSITIVE'
  | 'AMIABLE_RESPONSE_NEGATIVE'
  | 'AMOUNT_UPDATED'
  | 'DOCUMENT_CREATED'
  | 'STATUS_CHANGED';

export type CanalNotification = 'IN_APP' | 'EMAIL' | 'SMS' | 'WEBHOOK';

export interface NotificationHuissier {
  id: number;
  dossierId: number;
  type: TypeNotificationHuissier;
  channel: CanalNotification;
  message: string;
  payload?: any;
  createdAt: string;
  sentAt?: string;
  acked: boolean;
  recommendationId?: number;
}

// models/recommendation.model.ts
export type PrioriteRecommendation = 'LOW' | 'MEDIUM' | 'HIGH';

export interface Recommendation {
  id: number;
  dossierId: number;
  ruleCode: string;
  title: string;
  description: string;
  priority: PrioriteRecommendation;
  createdAt: string;
  acknowledged: boolean;
  acknowledgedAt?: string;
  acknowledgedBy?: number;
}

// models/audit-log.model.ts
export type TypeChangementAudit = 
  | 'AMOUNT_UPDATE'
  | 'DOCUMENT_CREATE'
  | 'ACTION_CREATE'
  | 'STATUS_UPDATE'
  | 'DOSSIER_UPDATE';

export interface AuditLog {
  id: number;
  dossierId: number;
  userId?: number;
  changeType: TypeChangementAudit;
  before: any;
  after: any;
  timestamp: string;
  description?: string;
}
```

---

## 💰 APIs - Montants et États de Dossier

### 1. Mettre à Jour les Montants d'un Dossier

**Endpoint:** `PUT /api/dossiers/{id}/montant`

**Request Body:**
```json
{
  "montantTotal": 10000.00,
  "montantRecouvre": 5000.00,
  "updateMode": "SET"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "numeroDossier": "DOS-2024-001",
  "montantTotal": 10000.00,
  "montantRecouvre": 5000.00,
  "montantRestant": 5000.00,
  "etatDossier": "RECOVERED_PARTIAL",
  ...
}
```

**Service TypeScript:**
```typescript
// services/dossier-montant.service.ts
import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable } from 'rxjs';
import { Dossier, MontantDossierDTO } from '../models/dossier.model';

@Injectable({
  providedIn: 'root'
})
export class DossierMontantService {
  constructor(private api: ApiService) {}

  updateMontants(dossierId: number, dto: MontantDossierDTO): Observable<Dossier> {
    return this.api.put<Dossier>(`/dossiers/${dossierId}/montant`, dto);
  }
}
```

**Exemple d'utilisation:**
```typescript
// component.ts
updateMontants() {
  const dto: MontantDossierDTO = {
    montantTotal: 10000,
    montantRecouvre: 5000,
    updateMode: 'SET'
  };

  this.dossierMontantService.updateMontants(this.dossierId, dto)
    .subscribe({
      next: (dossier) => {
        console.log('Montants mis à jour:', dossier);
        this.showSuccess('Montants mis à jour avec succès');
        this.loadDossier();
      },
      error: (err) => {
        console.error('Erreur:', err);
        this.showError(err.error?.error || 'Erreur lors de la mise à jour');
      }
    });
}
```

### 2. Enregistrer une Action Amiable avec Montant Recouvré

**Endpoint:** `POST /api/dossiers/{id}/amiable`

**Request Body:**
```json
{
  "montantRecouvre": 2000.00
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "numeroDossier": "DOS-2024-001",
  "montantTotal": 10000.00,
  "montantRecouvre": 7000.00,
  "montantRestant": 3000.00,
  "etatDossier": "RECOVERED_PARTIAL",
  ...
}
```

**Service TypeScript:**
```typescript
// services/action-amiable.service.ts
import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable } from 'rxjs';
import { Dossier, ActionAmiableDTO } from '../models/dossier.model';

@Injectable({
  providedIn: 'root'
})
export class ActionAmiableService {
  constructor(private api: ApiService) {}

  enregistrerActionAmiable(dossierId: number, montantRecouvre: number): Observable<Dossier> {
    const dto: ActionAmiableDTO = { montantRecouvre };
    return this.api.post<Dossier>(`/dossiers/${dossierId}/amiable`, dto);
  }
}
```

**Exemple d'utilisation:**
```typescript
// component.ts
onActionAmiablePositive(montant: number) {
  this.actionAmiableService.enregistrerActionAmiable(this.dossierId, montant)
    .subscribe({
      next: (dossier) => {
        this.showSuccess(`Montant recouvré: ${montant} TND`);
        this.dossier = dossier;
        this.updateUI();
      },
      error: (err) => {
        this.showError(err.error?.error || 'Erreur lors de l\'enregistrement');
      }
    });
}
```

---

## 📄 APIs - Documents Huissier

### 1. Créer un Document Huissier

**Endpoint:** `POST /api/huissier/document`

**Request Body:**
```json
{
  "dossierId": 1,
  "typeDocument": "PV_MISE_EN_DEMEURE",
  "huissierName": "Ahmed Ben Ali",
  "pieceJointeUrl": "https://example.com/document.pdf"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "dossierId": 1,
  "typeDocument": "PV_MISE_EN_DEMEURE",
  "dateCreation": "2024-01-15T10:00:00Z",
  "delaiLegalDays": 10,
  "pieceJointeUrl": "https://example.com/document.pdf",
  "huissierName": "Ahmed Ben Ali",
  "status": "PENDING",
  "notified": false
}
```

**Service TypeScript:**
```typescript
// services/document-huissier.service.ts
import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable } from 'rxjs';
import { DocumentHuissier, DocumentHuissierDTO } from '../models/document-huissier.model';

@Injectable({
  providedIn: 'root'
})
export class DocumentHuissierService {
  constructor(private api: ApiService) {}

  createDocument(dto: DocumentHuissierDTO): Observable<DocumentHuissier> {
    return this.api.post<DocumentHuissier>('/huissier/document', dto);
  }

  getDocumentById(id: number): Observable<DocumentHuissier> {
    return this.api.get<DocumentHuissier>(`/huissier/document/${id}`);
  }

  getDocumentsByDossier(dossierId: number): Observable<DocumentHuissier[]> {
    return this.api.get<DocumentHuissier[]>('/huissier/documents', { dossierId });
  }
}
```

**Exemple d'utilisation:**
```typescript
// component.ts
createDocument() {
  const dto: DocumentHuissierDTO = {
    dossierId: this.dossierId,
    typeDocument: 'PV_MISE_EN_DEMEURE',
    huissierName: 'Ahmed Ben Ali',
    pieceJointeUrl: this.uploadedFileUrl
  };

  this.documentHuissierService.createDocument(dto)
    .subscribe({
      next: (document) => {
        this.showSuccess('Document créé avec succès');
        this.loadDocuments();
      },
      error: (err) => {
        this.showError(err.error?.error || 'Erreur lors de la création');
      }
    });
}
```

### 2. Lister les Documents d'un Dossier

**Endpoint:** `GET /api/huissier/documents?dossierId={id}`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "dossierId": 1,
    "typeDocument": "PV_MISE_EN_DEMEURE",
    "dateCreation": "2024-01-15T10:00:00Z",
    "delaiLegalDays": 10,
    "status": "PENDING",
    "notified": false,
    "huissierName": "Ahmed Ben Ali"
  },
  {
    "id": 2,
    "dossierId": 1,
    "typeDocument": "ORDONNANCE_PAIEMENT",
    "dateCreation": "2024-01-25T10:00:00Z",
    "delaiLegalDays": 20,
    "status": "EXPIRED",
    "notified": true,
    "huissierName": "Ahmed Ben Ali"
  }
]
```

---

## ⚖️ APIs - Actions Huissier

### 1. Créer une Action Huissier

**Endpoint:** `POST /api/huissier/action`

**Request Body:**
```json
{
  "dossierId": 1,
  "typeAction": "ACLA_TA7AFOUDHIA",
  "montantRecouvre": 3000.00,
  "huissierName": "Ahmed Ben Ali",
  "pieceJointeUrl": "https://example.com/action.pdf",
  "updateMode": "ADD"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "dossierId": 1,
  "typeAction": "ACLA_TA7AFOUDHIA",
  "montantRecouvre": 3000.00,
  "montantRestant": 7000.00,
  "etatDossier": "RECOVERED_PARTIAL",
  "dateAction": "2024-02-15T10:00:00Z",
  "huissierName": "Ahmed Ben Ali",
  "pieceJointeUrl": "https://example.com/action.pdf"
}
```

**Service TypeScript:**
```typescript
// services/action-huissier.service.ts
import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable } from 'rxjs';
import { ActionHuissier, ActionHuissierDTO } from '../models/action-huissier.model';

@Injectable({
  providedIn: 'root'
})
export class ActionHuissierService {
  constructor(private api: ApiService) {}

  createAction(dto: ActionHuissierDTO): Observable<ActionHuissier> {
    return this.api.post<ActionHuissier>('/huissier/action', dto);
  }

  getActionById(id: number): Observable<ActionHuissier> {
    return this.api.get<ActionHuissier>(`/huissier/action/${id}`);
  }

  getActionsByDossier(dossierId: number): Observable<ActionHuissier[]> {
    return this.api.get<ActionHuissier[]>('/huissier/actions', { dossierId });
  }
}
```

**Exemple d'utilisation:**
```typescript
// component.ts
createAction() {
  const dto: ActionHuissierDTO = {
    dossierId: this.dossierId,
    typeAction: 'ACLA_TA7AFOUDHIA',
    montantRecouvre: 3000,
    huissierName: 'Ahmed Ben Ali',
    updateMode: 'ADD'
  };

  this.actionHuissierService.createAction(dto)
    .subscribe({
      next: (action) => {
        this.showSuccess('Action créée avec succès');
        this.loadActions();
        this.loadDossier(); // Recharger pour voir les nouveaux montants
      },
      error: (err) => {
        this.showError(err.error?.error || 'Erreur lors de la création');
      }
    });
}
```

### 2. Lister les Actions d'un Dossier

**Endpoint:** `GET /api/huissier/actions?dossierId={id}`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "dossierId": 1,
    "typeAction": "ACLA_TA7AFOUDHIA",
    "montantRecouvre": 3000.00,
    "montantRestant": 7000.00,
    "etatDossier": "RECOVERED_PARTIAL",
    "dateAction": "2024-02-15T10:00:00Z",
    "huissierName": "Ahmed Ben Ali"
  }
]
```

---

## 🔔 APIs - Notifications

### 1. Récupérer les Notifications d'un Dossier

**Endpoint:** `GET /api/notifications?dossierId={id}`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "dossierId": 1,
    "type": "DELAY_WARNING",
    "channel": "IN_APP",
    "message": "Rappel: PV_MISE_EN_DEMEURE du dossier DOS-2024-001 expire le 2024-01-25. Montant restant: 10000.00 TND.",
    "createdAt": "2024-01-23T10:00:00Z",
    "sentAt": "2024-01-23T10:00:01Z",
    "acked": false,
    "recommendationId": null
  },
  {
    "id": 2,
    "dossierId": 1,
    "type": "DELAY_EXPIRED",
    "channel": "IN_APP",
    "message": "Expiration: délai légal terminé pour PV_MISE_EN_DEMEURE du dossier DOS-2024-001.",
    "createdAt": "2024-01-26T10:00:00Z",
    "sentAt": "2024-01-26T10:00:01Z",
    "acked": false,
    "recommendationId": 1
  }
]
```

**Service TypeScript:**
```typescript
// services/notification-huissier.service.ts
import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable } from 'rxjs';
import { NotificationHuissier } from '../models/notification-huissier.model';

@Injectable({
  providedIn: 'root'
})
export class NotificationHuissierService {
  constructor(private api: ApiService) {}

  getNotificationsByDossier(dossierId: number): Observable<NotificationHuissier[]> {
    return this.api.get<NotificationHuissier[]>('/notifications', { dossierId });
  }

  acknowledgeNotification(notificationId: number, userId: number): Observable<any> {
    return this.api.post(`/notifications/${notificationId}/ack`, { userId });
  }
}
```

**Exemple d'utilisation:**
```typescript
// component.ts
loadNotifications() {
  this.notificationService.getNotificationsByDossier(this.dossierId)
    .subscribe({
      next: (notifications) => {
        this.notifications = notifications;
        this.unreadCount = notifications.filter(n => !n.acked).length;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des notifications:', err);
      }
    });
}

acknowledgeNotification(notificationId: number) {
  const userId = this.authService.getCurrentUserId();
  this.notificationService.acknowledgeNotification(notificationId, userId)
    .subscribe({
      next: () => {
        this.loadNotifications();
      },
      error: (err) => {
        this.showError('Erreur lors de l\'acquittement');
      }
    });
}
```

### 2. Acquitter une Notification

**Endpoint:** `POST /api/notifications/{id}/ack`

**Request Body:**
```json
{
  "userId": 1
}
```

**Response (200 OK):**
```json
{
  "message": "Notification acquittée avec succès"
}
```

---

## 💡 APIs - Recommandations

### 1. Récupérer les Recommandations d'un Dossier

**Endpoint:** `GET /api/recommendations?dossierId={id}`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "dossierId": 1,
    "ruleCode": "ESCALATE_TO_ORDONNANCE",
    "title": "Déposer ordonnance de paiement",
    "description": "Le délai légal du PV de mise en demeure a expiré. Déposer une ordonnance de paiement.",
    "priority": "HIGH",
    "createdAt": "2024-01-26T10:00:00Z",
    "acknowledged": false
  },
  {
    "id": 2,
    "dossierId": 1,
    "ruleCode": "ASSIGN_AVOCAT",
    "title": "Assigner un avocat",
    "description": "Plus de 50% du montant reste à recouvrer. Considérer l'assignation d'un avocat.",
    "priority": "MEDIUM",
    "createdAt": "2024-02-01T10:00:00Z",
    "acknowledged": false
  }
]
```

**Service TypeScript:**
```typescript
// services/recommendation.service.ts
import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable } from 'rxjs';
import { Recommendation } from '../models/recommendation.model';

@Injectable({
  providedIn: 'root'
})
export class RecommendationService {
  constructor(private api: ApiService) {}

  getRecommendationsByDossier(dossierId: number): Observable<Recommendation[]> {
    return this.api.get<Recommendation[]>('/recommendations', { dossierId });
  }

  acknowledgeRecommendation(recommendationId: number, userId: number): Observable<any> {
    return this.api.post(`/recommendations/${recommendationId}/ack`, { userId });
  }
}
```

**Exemple d'utilisation:**
```typescript
// component.ts
loadRecommendations() {
  this.recommendationService.getRecommendationsByDossier(this.dossierId)
    .subscribe({
      next: (recommendations) => {
        this.recommendations = recommendations;
        this.highPriorityCount = recommendations
          .filter(r => r.priority === 'HIGH' && !r.acknowledged).length;
      },
      error: (err) => {
        console.error('Erreur:', err);
      }
    });
}

executeRecommendation(recommendation: Recommendation) {
  // Mapper le ruleCode à une action
  const actionMap: { [key: string]: () => void } = {
    'ESCALATE_TO_ORDONNANCE': () => this.openCreateDocumentForm('ORDONNANCE_PAIEMENT'),
    'INITIATE_EXECUTION': () => this.openCreateActionForm('ACLA_TA7AFOUDHIA'),
    'ASSIGN_AVOCAT': () => this.openAssignAvocatForm(),
    'INITIATE_BANK_SAISIE': () => this.openBankSaisieForm(),
    'ESCALATE_TO_DIRECTOR': () => this.openEscalationForm()
  };

  const action = actionMap[recommendation.ruleCode];
  if (action) {
    action();
    // Acquitter la recommandation après action
    this.acknowledgeRecommendation(recommendation.id);
  }
}
```

### 2. Acquitter une Recommandation

**Endpoint:** `POST /api/recommendations/{id}/ack`

**Request Body:**
```json
{
  "userId": 1
}
```

**Response (200 OK):**
```json
{
  "message": "Recommandation acquittée avec succès"
}
```

---

## 📊 APIs - Audit Logs

### 1. Récupérer les Logs d'Audit

**Endpoint:** `GET /api/audit-logs?dossierId={id}` ou `GET /api/audit-logs?userId={id}`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "dossierId": 1,
    "userId": 5,
    "changeType": "AMOUNT_UPDATE",
    "before": {
      "montantTotal": 10000,
      "montantRecouvre": 0,
      "montantRestant": 10000,
      "etatDossier": "NOT_RECOVERED"
    },
    "after": {
      "montantTotal": 10000,
      "montantRecouvre": 5000,
      "montantRestant": 5000,
      "etatDossier": "RECOVERED_PARTIAL"
    },
    "timestamp": "2024-01-15T10:00:00Z",
    "description": "Mise à jour des montants du dossier"
  }
]
```

**Service TypeScript:**
```typescript
// services/audit-log.service.ts
import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable } from 'rxjs';
import { AuditLog } from '../models/audit-log.model';

@Injectable({
  providedIn: 'root'
})
export class AuditLogService {
  constructor(private api: ApiService) {}

  getLogsByDossier(dossierId: number): Observable<AuditLog[]> {
    return this.api.get<AuditLog[]>('/audit-logs', { dossierId });
  }

  getLogsByUser(userId: number): Observable<AuditLog[]> {
    return this.api.get<AuditLog[]>('/audit-logs', { userId });
  }
}
```

---

## 🎨 Exemples d'Intégration Complète

### Composant Dossier avec Toutes les Fonctionnalités

```typescript
// components/dossier-detail/dossier-detail.component.ts
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Dossier } from '../../models/dossier.model';
import { DocumentHuissier } from '../../models/document-huissier.model';
import { ActionHuissier } from '../../models/action-huissier.model';
import { NotificationHuissier } from '../../models/notification-huissier.model';
import { Recommendation } from '../../models/recommendation.model';
import { DossierService } from '../../services/dossier.service';
import { DocumentHuissierService } from '../../services/document-huissier.service';
import { ActionHuissierService } from '../../services/action-huissier.service';
import { NotificationHuissierService } from '../../services/notification-huissier.service';
import { RecommendationService } from '../../services/recommendation.service';

@Component({
  selector: 'app-dossier-detail',
  templateUrl: './dossier-detail.component.html'
})
export class DossierDetailComponent implements OnInit {
  dossierId!: number;
  dossier!: Dossier;
  documents: DocumentHuissier[] = [];
  actions: ActionHuissier[] = [];
  notifications: NotificationHuissier[] = [];
  recommendations: Recommendation[] = [];
  
  unreadNotifications = 0;
  highPriorityRecommendations = 0;

  constructor(
    private route: ActivatedRoute,
    private dossierService: DossierService,
    private documentService: DocumentHuissierService,
    private actionService: ActionHuissierService,
    private notificationService: NotificationHuissierService,
    private recommendationService: RecommendationService
  ) {}

  ngOnInit() {
    this.dossierId = +this.route.snapshot.paramMap.get('id')!;
    this.loadAllData();
    
    // Rafraîchir toutes les 30 secondes
    setInterval(() => this.loadAllData(), 30000);
  }

  loadAllData() {
    this.loadDossier();
    this.loadDocuments();
    this.loadActions();
    this.loadNotifications();
    this.loadRecommendations();
  }

  loadDossier() {
    this.dossierService.getDossierById(this.dossierId)
      .subscribe(dossier => this.dossier = dossier);
  }

  loadDocuments() {
    this.documentService.getDocumentsByDossier(this.dossierId)
      .subscribe(documents => this.documents = documents);
  }

  loadActions() {
    this.actionService.getActionsByDossier(this.dossierId)
      .subscribe(actions => this.actions = actions);
  }

  loadNotifications() {
    this.notificationService.getNotificationsByDossier(this.dossierId)
      .subscribe(notifications => {
        this.notifications = notifications;
        this.unreadNotifications = notifications.filter(n => !n.acked).length;
      });
  }

  loadRecommendations() {
    this.recommendationService.getRecommendationsByDossier(this.dossierId)
      .subscribe(recommendations => {
        this.recommendations = recommendations;
        this.highPriorityRecommendations = recommendations
          .filter(r => r.priority === 'HIGH' && !r.acknowledged).length;
      });
  }

  getEtatDossierBadgeClass(): string {
    switch (this.dossier.etatDossier) {
      case 'RECOVERED_TOTAL': return 'badge-success';
      case 'RECOVERED_PARTIAL': return 'badge-warning';
      case 'NOT_RECOVERED': return 'badge-danger';
      default: return 'badge-secondary';
    }
  }

  getDocumentStatusClass(status: string): string {
    switch (status) {
      case 'PENDING': return 'badge-info';
      case 'EXPIRED': return 'badge-danger';
      case 'COMPLETED': return 'badge-success';
      default: return 'badge-secondary';
    }
  }

  getPriorityClass(priority: string): string {
    switch (priority) {
      case 'HIGH': return 'alert-danger';
      case 'MEDIUM': return 'alert-warning';
      case 'LOW': return 'alert-info';
      default: return 'alert-secondary';
    }
  }
}
```

### Template HTML Exemple

```html
<!-- components/dossier-detail/dossier-detail.component.html -->
<div class="dossier-detail">
  <!-- En-tête avec montants -->
  <div class="card mb-3">
    <div class="card-header">
      <h3>Dossier: {{ dossier.numeroDossier }}</h3>
    </div>
    <div class="card-body">
      <div class="row">
        <div class="col-md-3">
          <strong>Montant Total:</strong>
          <p class="h4">{{ dossier.montantTotal | number:'1.2-2' }} TND</p>
        </div>
        <div class="col-md-3">
          <strong>Montant Recouvré:</strong>
          <p class="h4 text-success">{{ dossier.montantRecouvre | number:'1.2-2' }} TND</p>
        </div>
        <div class="col-md-3">
          <strong>Montant Restant:</strong>
          <p class="h4 text-warning">{{ dossier.montantRestant | number:'1.2-2' }} TND</p>
        </div>
        <div class="col-md-3">
          <strong>État:</strong>
          <p>
            <span [class]="'badge ' + getEtatDossierBadgeClass()">
              {{ dossier.etatDossier }}
            </span>
          </p>
        </div>
      </div>
    </div>
  </div>

  <!-- Documents Huissier -->
  <div class="card mb-3">
    <div class="card-header">
      <h4>Documents Huissier</h4>
    </div>
    <div class="card-body">
      <table class="table">
        <thead>
          <tr>
            <th>Type</th>
            <th>Date Création</th>
            <th>Délai Légal</th>
            <th>Statut</th>
            <th>Huissier</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let doc of documents">
            <td>{{ doc.typeDocument }}</td>
            <td>{{ doc.dateCreation | date:'short' }}</td>
            <td>{{ doc.delaiLegalDays }} jours</td>
            <td>
              <span [class]="'badge ' + getDocumentStatusClass(doc.status)">
                {{ doc.status }}
              </span>
            </td>
            <td>{{ doc.huissierName }}</td>
            <td>
              <button class="btn btn-sm btn-primary" (click)="viewDocument(doc.id)">
                Voir
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <!-- Actions Huissier -->
  <div class="card mb-3">
    <div class="card-header">
      <h4>Actions d'Exécution</h4>
    </div>
    <div class="card-body">
      <table class="table">
        <thead>
          <tr>
            <th>Type</th>
            <th>Date</th>
            <th>Montant Recouvré</th>
            <th>Montant Restant</th>
            <th>État</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let action of actions">
            <td>{{ action.typeAction }}</td>
            <td>{{ action.dateAction | date:'short' }}</td>
            <td>{{ action.montantRecouvre | number:'1.2-2' }} TND</td>
            <td>{{ action.montantRestant | number:'1.2-2' }} TND</td>
            <td>
              <span [class]="'badge ' + getEtatDossierBadgeClass()">
                {{ action.etatDossier }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <!-- Notifications -->
  <div class="card mb-3">
    <div class="card-header">
      <h4>
        Notifications
        <span *ngIf="unreadNotifications > 0" class="badge badge-danger">
          {{ unreadNotifications }}
        </span>
      </h4>
    </div>
    <div class="card-body">
      <div *ngFor="let notification of notifications" class="alert alert-info">
        <p>{{ notification.message }}</p>
        <small>{{ notification.createdAt | date:'short' }}</small>
        <button *ngIf="!notification.acked" 
                class="btn btn-sm btn-primary float-right"
                (click)="acknowledgeNotification(notification.id)">
          Acquitter
        </button>
      </div>
    </div>
  </div>

  <!-- Recommandations -->
  <div class="card mb-3">
    <div class="card-header">
      <h4>
        Recommandations
        <span *ngIf="highPriorityRecommendations > 0" class="badge badge-danger">
          {{ highPriorityRecommendations }}
        </span>
      </h4>
    </div>
    <div class="card-body">
      <div *ngFor="let rec of recommendations" 
           [class]="'alert ' + getPriorityClass(rec.priority)">
        <h5>{{ rec.title }}</h5>
        <p>{{ rec.description }}</p>
        <button class="btn btn-sm btn-primary" 
                (click)="executeRecommendation(rec)">
          Exécuter
        </button>
        <button class="btn btn-sm btn-secondary" 
                (click)="acknowledgeRecommendation(rec.id)">
          Acquitter
        </button>
      </div>
    </div>
  </div>
</div>
```

---

## ⚠️ Gestion des Erreurs

### Intercepteur HTTP pour la Gestion Globale des Erreurs

```typescript
// interceptors/error.interceptor.ts
import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(private router: Router) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<any> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        let errorMessage = 'Une erreur est survenue';

        if (error.error instanceof ErrorEvent) {
          // Erreur côté client
          errorMessage = `Erreur: ${error.error.message}`;
        } else {
          // Erreur côté serveur
          switch (error.status) {
            case 400:
              errorMessage = error.error?.error || 'Requête invalide';
              break;
            case 401:
              errorMessage = 'Non autorisé. Veuillez vous reconnecter.';
              this.router.navigate(['/login']);
              break;
            case 403:
              errorMessage = 'Accès interdit';
              break;
            case 404:
              errorMessage = error.error?.error || 'Ressource non trouvée';
              break;
            case 500:
              errorMessage = 'Erreur serveur. Veuillez réessayer plus tard.';
              break;
            default:
              errorMessage = error.error?.error || `Erreur ${error.status}`;
          }
        }

        console.error('Erreur HTTP:', errorMessage);
        return throwError(() => new Error(errorMessage));
      })
    );
  }
}
```

### Service de Notification Utilisateur

```typescript
// services/notification.service.ts
import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

export interface Notification {
  type: 'success' | 'error' | 'warning' | 'info';
  message: string;
  duration?: number;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private notificationSubject = new Subject<Notification>();
  public notifications$ = this.notificationSubject.asObservable();

  showSuccess(message: string, duration = 3000) {
    this.notificationSubject.next({ type: 'success', message, duration });
  }

  showError(message: string, duration = 5000) {
    this.notificationSubject.next({ type: 'error', message, duration });
  }

  showWarning(message: string, duration = 4000) {
    this.notificationSubject.next({ type: 'warning', message, duration });
  }

  showInfo(message: string, duration = 3000) {
    this.notificationSubject.next({ type: 'info', message, duration });
  }
}
```

---

## ✅ Bonnes Pratiques

### 1. Gestion des États de Chargement

```typescript
export class DossierDetailComponent {
  loading = false;
  error: string | null = null;

  loadDossier() {
    this.loading = true;
    this.error = null;
    
    this.dossierService.getDossierById(this.dossierId)
      .subscribe({
        next: (dossier) => {
          this.dossier = dossier;
          this.loading = false;
        },
        error: (err) => {
          this.error = err.message;
          this.loading = false;
        }
      });
  }
}
```

### 2. Validation des Formulaires

```typescript
// Validators personnalisés
export function montantValidator(control: AbstractControl): ValidationErrors | null {
  const value = control.value;
  if (value === null || value === undefined) {
    return null;
  }
  if (value < 0) {
    return { negativeAmount: true };
  }
  return null;
}

// Utilisation dans le formulaire
this.form = this.fb.group({
  montantRecouvre: [0, [Validators.required, montantValidator]],
  montantTotal: [0, [Validators.required, montantValidator]]
}, {
  validators: (group: FormGroup) => {
    const montantTotal = group.get('montantTotal')?.value || 0;
    const montantRecouvre = group.get('montantRecouvre')?.value || 0;
    if (montantRecouvre > montantTotal) {
      return { montantExceeded: true };
    }
    return null;
  }
});
```

### 3. Formatage des Montants

```typescript
// Pipes personnalisés
@Pipe({ name: 'tnd' })
export class TndPipe implements PipeTransform {
  transform(value: number | null | undefined): string {
    if (value === null || value === undefined) {
      return '0.00 TND';
    }
    return new Intl.NumberFormat('fr-TN', {
      style: 'currency',
      currency: 'TND',
      minimumFractionDigits: 2
    }).format(value);
  }
}

// Utilisation
{{ dossier.montantTotal | tnd }}
```

### 4. Polling pour les Mises à Jour en Temps Réel

```typescript
import { interval, Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';

export class DossierDetailComponent implements OnDestroy {
  private pollingSubscription?: Subscription;

  ngOnInit() {
    // Polling toutes les 30 secondes
    this.pollingSubscription = interval(30000)
      .pipe(
        switchMap(() => this.dossierService.getDossierById(this.dossierId))
      )
      .subscribe(dossier => {
        this.dossier = dossier;
      });
  }

  ngOnDestroy() {
    if (this.pollingSubscription) {
      this.pollingSubscription.unsubscribe();
    }
  }
}
```

---

## 📝 Checklist d'Intégration

- [ ] Créer les services API de base
- [ ] Définir toutes les interfaces TypeScript
- [ ] Implémenter le service DossierMontantService
- [ ] Implémenter le service ActionAmiableService
- [ ] Implémenter le service DocumentHuissierService
- [ ] Implémenter le service ActionHuissierService
- [ ] Implémenter le service NotificationHuissierService
- [ ] Implémenter le service RecommendationService
- [ ] Implémenter le service AuditLogService
- [ ] Créer les composants UI pour chaque fonctionnalité
- [ ] Ajouter la gestion des erreurs globale
- [ ] Implémenter le formatage des montants
- [ ] Ajouter les validations de formulaires
- [ ] Tester tous les endpoints
- [ ] Implémenter le polling pour les mises à jour
- [ ] Ajouter les notifications utilisateur
- [ ] Tester sur mobile/tablet/desktop

---

## 🎯 Conclusion

Ce document fournit toutes les informations nécessaires pour intégrer complètement le système de recouvrement tunisien dans votre application frontend. Suivez les exemples de code, adaptez-les à votre architecture, et testez régulièrement.

Pour toute question ou problème, référez-vous aux logs du backend et aux messages d'erreur détaillés.

**Bon développement ! 🚀**

