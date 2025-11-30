# 📋 Prompts Complets pour l'Intégration Frontend - Gestion Documents et Actions Huissier

## 🎯 Vue d'Ensemble

Ce document contient tous les prompts nécessaires pour créer une interface indépendante de gestion des documents et actions huissier dans le frontend Angular, avec un menu dédié dans le sidebar.

---

## 📝 PROMPT 1 : Interfaces TypeScript

**Créer** : `src/app/models/huissier-document.model.ts`

```typescript
export enum TypeDocumentHuissier {
  PV_MISE_EN_DEMEURE = 'PV_MISE_EN_DEMEURE',
  ORDONNANCE_PAIEMENT = 'ORDONNANCE_PAIEMENT',
  PV_NOTIFICATION_ORDONNANCE = 'PV_NOTIFICATION_ORDONNANCE'
}

export enum StatutDocumentHuissier {
  PENDING = 'PENDING',
  EXPIRED = 'EXPIRED',
  COMPLETED = 'COMPLETED'
}

export interface DocumentHuissier {
  id?: number;
  dossierId: number;
  typeDocument: TypeDocumentHuissier;
  dateCreation?: string;
  delaiLegalDays?: number;
  pieceJointeUrl?: string;
  huissierName: string;
  status?: StatutDocumentHuissier;
  notified?: boolean;
}

export interface DocumentHuissierDTO {
  dossierId: number;
  typeDocument: TypeDocumentHuissier;
  huissierName: string;
  pieceJointeUrl?: string;
}
```

**Créer** : `src/app/models/huissier-action.model.ts`

```typescript
export enum TypeActionHuissier {
  ACLA_TA7AFOUDHIA = 'ACLA_TA7AFOUDHIA',
  ACLA_TANFITHIA = 'ACLA_TANFITHIA',
  ACLA_TAW9IFIYA = 'ACLA_TAW9IFIYA',
  ACLA_A9ARYA = 'ACLA_A9ARYA'
}

export interface ActionHuissier {
  id?: number;
  dossierId: number;
  typeAction: TypeActionHuissier;
  montantRecouvre?: number;
  montantRestant?: number;
  etatDossier?: string;
  dateAction?: string;
  pieceJointeUrl?: string;
  huissierName: string;
}

export interface ActionHuissierDTO {
  dossierId: number;
  typeAction: TypeActionHuissier;
  huissierName: string;
  montantRecouvre?: number;
  montantRestant?: number;
  etatDossier?: string;
  pieceJointeUrl?: string;
  updateMode?: 'ADD' | 'SET';
}
```

---

## 📝 PROMPT 2 : Services Angular

**Créer** : `src/app/services/huissier-document.service.ts`

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DocumentHuissier, DocumentHuissierDTO } from '../models/huissier-document.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class HuissierDocumentService {
  private apiUrl = `${environment.apiUrl}/huissier`;

  constructor(private http: HttpClient) {}

  createDocument(dto: DocumentHuissierDTO): Observable<DocumentHuissier> {
    return this.http.post<DocumentHuissier>(`${this.apiUrl}/document`, dto);
  }

  getDocumentById(id: number): Observable<DocumentHuissier> {
    return this.http.get<DocumentHuissier>(`${this.apiUrl}/document/${id}`);
  }

  getDocumentsByDossier(dossierId: number): Observable<DocumentHuissier[]> {
    const params = new HttpParams().set('dossierId', dossierId.toString());
    return this.http.get<DocumentHuissier[]>(`${this.apiUrl}/documents`, { params });
  }

  markDocumentAsExpired(id: number): Observable<DocumentHuissier> {
    return this.http.put<DocumentHuissier>(`${this.apiUrl}/document/${id}/expire`, {});
  }
}
```

**Créer** : `src/app/services/huissier-action.service.ts`

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ActionHuissier, ActionHuissierDTO } from '../models/huissier-action.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class HuissierActionService {
  private apiUrl = `${environment.apiUrl}/huissier`;

  constructor(private http: HttpClient) {}

  createAction(dto: ActionHuissierDTO): Observable<ActionHuissier> {
    return this.http.post<ActionHuissier>(`${this.apiUrl}/action`, dto);
  }

  getActionById(id: number): Observable<ActionHuissier> {
    return this.http.get<ActionHuissier>(`${this.apiUrl}/action/${id}`);
  }

  getActionsByDossier(dossierId: number): Observable<ActionHuissier[]> {
    const params = new HttpParams().set('dossierId', dossierId.toString());
    return this.http.get<ActionHuissier[]>(`${this.apiUrl}/actions`, { params });
  }
}
```

---

## 📝 PROMPT 3 : Ajout du Menu dans le Sidebar

**Modifier** : Le composant de navigation/sidebar

```html
<!-- Ajouter dans la liste de navigation -->
<li class="nav-item">
  <a class="nav-link" 
     routerLink="/juridique/gestion-huissier"
     routerLinkActive="active">
    <i class="fas fa-gavel"></i>
    <span>Gestion Huissier</span>
  </a>
</li>
```

---

## 📝 PROMPT 4 : Composant Principal (TypeScript)

**Créer** : `src/app/components/gestion-huissier/gestion-huissier.component.ts`

Voir le fichier `PROMPT_FRONTEND_GESTION_DOCUMENTS_ACTIONS_HUISSIER.md` pour le code complet du composant.

**Points clés** :
- Gestion de deux onglets : Documents et Actions
- Sélection d'un dossier et d'un huissier
- Chargement des documents/actions par dossier
- Formulaires de création avec validation
- Affichage des listes avec statuts et dates

---

## 📝 PROMPT 5 : Template HTML

**Créer** : `src/app/components/gestion-huissier/gestion-huissier.component.html`

Voir le fichier `PROMPT_FRONTEND_GESTION_DOCUMENTS_ACTIONS_HUISSIER.md` pour le template complet.

**Fonctionnalités** :
- Sélection de dossier et huissier
- Onglets Documents/Actions
- Tableaux avec statuts colorés
- Modals de création
- Indicateurs de chargement

---

## 📝 PROMPT 6 : Configuration des Routes

**Modifier** : `src/app/app-routing.module.ts`

```typescript
import { GestionHuissierComponent } from './components/gestion-huissier/gestion-huissier.component';

const routes: Routes = [
  // ... autres routes ...
  {
    path: 'juridique/gestion-huissier',
    component: GestionHuissierComponent
  }
];
```

---

## 📝 PROMPT 7 : Styles CSS (Optionnel)

**Créer** : `src/app/components/gestion-huissier/gestion-huissier.component.css`

```css
.gestion-huissier-container {
  padding: 20px;
}

.selection-section {
  margin-bottom: 20px;
}

.nav-tabs {
  border-bottom: 2px solid #dee2e6;
}

.nav-tabs .nav-link.active {
  border-bottom: 3px solid #007bff;
  font-weight: bold;
}

.table-warning {
  background-color: #fff3cd;
}

.table-danger {
  background-color: #f8d7da;
}

.modal {
  display: none;
  position: fixed;
  z-index: 1000;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0,0,0,0.5);
}

.modal.show {
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-dialog {
  background-color: white;
  padding: 20px;
  border-radius: 5px;
  max-width: 600px;
  width: 90%;
}
```

---

## ✅ Checklist d'Implémentation

### Backend (Vérifications)
- [ ] La colonne `huissier_id` existe dans la table `audiences` (et non `hussier_id`)
- [ ] La colonne est `nullable = true`
- [ ] Les endpoints `/api/huissier/document` et `/api/huissier/action` fonctionnent
- [ ] Les notifications sont isolées dans des transactions séparées

### Frontend
- [ ] Interfaces TypeScript créées
- [ ] Services Angular créés et injectés
- [ ] Menu ajouté dans le sidebar
- [ ] Composant de gestion créé
- [ ] Template HTML créé
- [ ] Routes configurées
- [ ] Module Angular mis à jour avec les imports nécessaires

---

## 🎯 Workflow Recommandé

1. **Phase 1** : Créer les documents huissier (PV mise en demeure, etc.)
2. **Phase 2** : Créer les actions huissier (saisies, etc.)
3. **Phase 3** : Passer à l'audience une fois les documents et actions traités

---

## 🔧 Correction du Problème d'Audience

Le problème de "Transaction silently rolled back" peut être résolu en :

1. **Vérifiant la structure de la table** : Exécutez le script `fix_audience_huissier_column.sql`
2. **Redémarrant le serveur** après toutes les modifications
3. **Vérifiant les logs** pour identifier l'exception exacte
4. **Testant sans notifications** pour isoler le problème

Voir le fichier `CORRECTION_ERREUR_AUDIENCE_TRANSACTION_ROLLBACK.md` pour plus de détails.

---

**Bon développement ! 🎉**

