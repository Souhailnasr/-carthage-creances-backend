# 📋 Prompts Frontend : Marquer un Document Huissier comme Complété

## 🎯 Objectif

Permettre à l'utilisateur de marquer un document huissier comme complété depuis l'interface frontend, avec les contraintes suivantes :
- ✅ L'utilisateur peut **UNIQUEMENT** changer le statut vers `COMPLETED`
- ❌ Si le statut est `EXPIRED`, il **ne peut pas** le changer
- ❌ Si le statut est déjà `COMPLETED`, il **ne peut pas** le changer à nouveau

---

## 📝 PROMPT 1 : Ajouter la Méthode dans le Service Angular

**Modifier** : `src/app/services/huissier-document.service.ts`

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

  // ... méthodes existantes ...

  /**
   * Marque un document comme complété
   * PUT /api/huissier/document/{id}/complete
   * 
   * Contraintes :
   * - Seulement si le statut est PENDING
   * - Impossible si le statut est EXPIRED
   * - Impossible si le statut est déjà COMPLETED
   */
  markDocumentAsCompleted(id: number): Observable<DocumentHuissier> {
    return this.http.put<DocumentHuissier>(`${this.apiUrl}/document/${id}/complete`, {});
  }

  /**
   * Marque un document comme expiré (utilisé par le scheduler, pas par l'utilisateur)
   * PUT /api/huissier/document/{id}/expire
   */
  markDocumentAsExpired(id: number): Observable<DocumentHuissier> {
    return this.http.put<DocumentHuissier>(`${this.apiUrl}/document/${id}/expire`, {});
  }
}
```

---

## 📝 PROMPT 2 : Ajouter la Méthode dans le Composant

**Modifier** : `src/app/components/gestion-huissier/gestion-huissier.component.ts`

Ajoutez cette méthode dans la classe du composant :

```typescript
/**
 * Marque un document comme complété
 * Vérifie les contraintes avant d'appeler l'API
 */
markDocumentAsCompleted(document: DocumentHuissier): void {
  // Vérifier les contraintes
  if (document.status === StatutDocumentHuissier.EXPIRED) {
    alert('Impossible de marquer un document expiré comme complété');
    return;
  }
  
  if (document.status === StatutDocumentHuissier.COMPLETED) {
    alert('Ce document est déjà marqué comme complété');
    return;
  }
  
  // Demander confirmation
  if (!confirm('Êtes-vous sûr de vouloir marquer ce document comme complété ?')) {
    return;
  }
  
  this.isLoading = true;
  this.documentService.markDocumentAsCompleted(document.id!).subscribe({
    next: (updatedDocument) => {
      console.log('Document marqué comme complété:', updatedDocument);
      // Recharger la liste des documents
      this.loadDocuments();
      this.isLoading = false;
      alert('Document marqué comme complété avec succès');
    },
    error: (error) => {
      console.error('Erreur lors du marquage du document:', error);
      this.isLoading = false;
      const errorMessage = error.error?.error || error.message || 'Erreur lors du marquage du document';
      alert('Erreur: ' + errorMessage);
    }
  });
}

/**
 * Vérifie si un document peut être marqué comme complété
 */
canMarkAsCompleted(document: DocumentHuissier): boolean {
  // Seulement si le statut est PENDING
  return document.status === StatutDocumentHuissier.PENDING;
}
```

---

## 📝 PROMPT 3 : Modifier le Template HTML

**Modifier** : `src/app/components/gestion-huissier/gestion-huissier.component.html`

Dans la section du tableau des documents, ajoutez un bouton "Marquer comme complété" :

```html
<!-- Dans le tableau des documents -->
<table class="table table-striped">
  <thead>
    <tr>
      <th>Type</th>
      <th>Date de Création</th>
      <th>Délai Légal</th>
      <th>Date d'Expiration</th>
      <th>Statut</th>
      <th>Huissier</th>
      <th>Actions</th>
    </tr>
  </thead>
  <tbody>
    <tr *ngFor="let document of documents" 
        [class.table-warning]="isDocumentExpired(document)"
        [class.table-danger]="document.status === 'EXPIRED'"
        [class.table-success]="document.status === 'COMPLETED'">
      <td>{{ getDocumentTypeLabel(document.typeDocument) }}</td>
      <td>{{ formatDate(document.dateCreation) }}</td>
      <td>{{ document.delaiLegalDays }} jours</td>
      <td>{{ getExpirationDate(document) }}</td>
      <td>
        <span class="badge" 
              [class.badge-warning]="document.status === 'PENDING'"
              [class.badge-danger]="document.status === 'EXPIRED'"
              [class.badge-success]="document.status === 'COMPLETED'">
          {{ document.status }}
        </span>
      </td>
      <td>{{ document.huissierName }}</td>
      <td>
        <!-- Bouton pour voir la pièce jointe -->
        <button class="btn btn-sm btn-info" 
                *ngIf="document.pieceJointeUrl"
                (click)="openDocument(document.pieceJointeUrl)"
                title="Voir la pièce jointe">
          <i class="fas fa-file"></i> Voir
        </button>
        
        <!-- Bouton pour marquer comme complété -->
        <button class="btn btn-sm btn-success" 
                *ngIf="canMarkAsCompleted(document)"
                (click)="markDocumentAsCompleted(document)"
                [disabled]="isLoading"
                title="Marquer comme complété">
          <i class="fas fa-check-circle"></i> Marquer comme complété
        </button>
        
        <!-- Message si expiré -->
        <span class="text-danger" 
              *ngIf="document.status === 'EXPIRED'"
              title="Ce document est expiré et ne peut pas être marqué comme complété">
          <i class="fas fa-exclamation-triangle"></i> Expiré
        </span>
        
        <!-- Message si déjà complété -->
        <span class="text-success" 
              *ngIf="document.status === 'COMPLETED'"
              title="Ce document est déjà complété">
          <i class="fas fa-check"></i> Complété
        </span>
      </td>
    </tr>
  </tbody>
</table>
```

---

## 📝 PROMPT 4 : Améliorer l'Affichage avec des Icônes

**Modifier** : `src/app/components/gestion-huissier/gestion-huissier.component.html`

Version améliorée avec des icônes et des tooltips :

```html
<td>
  <div class="btn-group" role="group">
    <!-- Bouton Voir -->
    <button class="btn btn-sm btn-info" 
            *ngIf="document.pieceJointeUrl"
            (click)="openDocument(document.pieceJointeUrl)"
            title="Voir la pièce jointe">
      <i class="fas fa-file"></i>
    </button>
    
    <!-- Bouton Marquer comme complété (seulement si PENDING) -->
    <button class="btn btn-sm btn-success" 
            *ngIf="canMarkAsCompleted(document)"
            (click)="markDocumentAsCompleted(document)"
            [disabled]="isLoading"
            title="Marquer ce document comme complété">
      <i class="fas fa-check-circle"></i>
      <span class="d-none d-md-inline">Compléter</span>
    </button>
    
    <!-- Message d'information pour EXPIRED -->
    <span class="badge badge-danger" 
          *ngIf="document.status === 'EXPIRED'"
          title="Ce document est expiré. Il ne peut pas être marqué comme complété.">
      <i class="fas fa-exclamation-triangle"></i> Expiré
    </span>
    
    <!-- Message de confirmation pour COMPLETED -->
    <span class="badge badge-success" 
          *ngIf="document.status === 'COMPLETED'"
          title="Ce document a été marqué comme complété.">
      <i class="fas fa-check"></i> Complété
    </span>
  </div>
</td>
```

---

## 📝 PROMPT 5 : Gestion des Erreurs Améliorée

**Modifier** : `src/app/components/gestion-huissier/gestion-huissier.component.ts`

Version améliorée avec gestion d'erreurs détaillée :

```typescript
/**
 * Marque un document comme complété avec gestion d'erreurs améliorée
 */
markDocumentAsCompleted(document: DocumentHuissier): void {
  // Vérification côté client
  if (document.status === StatutDocumentHuissier.EXPIRED) {
    this.showError('Impossible de marquer un document expiré comme complété. Le délai légal est dépassé.');
    return;
  }
  
  if (document.status === StatutDocumentHuissier.COMPLETED) {
    this.showInfo('Ce document est déjà marqué comme complété.');
    return;
  }
  
  // Demander confirmation avec détails
  const message = `Êtes-vous sûr de vouloir marquer ce document comme complété ?\n\n` +
                  `Type: ${this.getDocumentTypeLabel(document.typeDocument)}\n` +
                  `Date de création: ${this.formatDate(document.dateCreation)}\n` +
                  `Huissier: ${document.huissierName}`;
  
  if (!confirm(message)) {
    return;
  }
  
  this.isLoading = true;
  this.documentService.markDocumentAsCompleted(document.id!).subscribe({
    next: (updatedDocument) => {
      console.log('Document marqué comme complété:', updatedDocument);
      this.loadDocuments();
      this.isLoading = false;
      this.showSuccess('Document marqué comme complété avec succès');
    },
    error: (error) => {
      console.error('Erreur lors du marquage du document:', error);
      this.isLoading = false;
      
      // Gestion des erreurs spécifiques
      if (error.status === 400) {
        const errorMessage = error.error?.error || 'Erreur lors du marquage du document';
        this.showError(errorMessage);
      } else if (error.status === 404) {
        this.showError('Document non trouvé');
      } else {
        this.showError('Erreur lors du marquage du document. Veuillez réessayer.');
      }
    }
  });
}

/**
 * Affiche un message de succès
 */
showSuccess(message: string): void {
  // Utilisez votre système de notification (toast, snackbar, etc.)
  alert(message); // Ou remplacez par votre système de notification
}

/**
 * Affiche un message d'erreur
 */
showError(message: string): void {
  // Utilisez votre système de notification (toast, snackbar, etc.)
  alert('Erreur: ' + message); // Ou remplacez par votre système de notification
}

/**
 * Affiche un message d'information
 */
showInfo(message: string): void {
  // Utilisez votre système de notification (toast, snackbar, etc.)
  alert(message); // Ou remplacez par votre système de notification
}
```

---

## 📝 PROMPT 6 : Styles CSS pour les Statuts

**Modifier** : `src/app/components/gestion-huissier/gestion-huissier.component.css`

Ajoutez ces styles pour améliorer l'affichage :

```css
/* Styles pour les statuts */
.table-success {
  background-color: #d4edda;
}

.table-warning {
  background-color: #fff3cd;
}

.table-danger {
  background-color: #f8d7da;
}

/* Badges de statut */
.badge-warning {
  background-color: #ffc107;
  color: #000;
}

.badge-danger {
  background-color: #dc3545;
  color: #fff;
}

.badge-success {
  background-color: #28a745;
  color: #fff;
}

/* Bouton marquer comme complété */
.btn-success {
  margin-left: 5px;
}

.btn-success:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Tooltip personnalisé */
[title] {
  cursor: help;
}
```

---

## ✅ Checklist d'Implémentation

### Backend
- [x] Méthode `markAsCompleted()` ajoutée dans `DocumentHuissierService`
- [x] Implémentation avec contraintes dans `DocumentHuissierServiceImpl`
- [x] Endpoint `PUT /api/huissier/document/{id}/complete` créé
- [x] Validation : impossible si EXPIRED ou déjà COMPLETED

### Frontend
- [ ] Méthode `markDocumentAsCompleted()` ajoutée dans le service
- [ ] Méthode `markDocumentAsCompleted()` ajoutée dans le composant
- [ ] Méthode `canMarkAsCompleted()` pour vérifier les contraintes
- [ ] Bouton "Marquer comme complété" ajouté dans le template
- [ ] Gestion des erreurs implémentée
- [ ] Messages de confirmation ajoutés
- [ ] Styles CSS pour les statuts

---

## 🎯 Résumé des Contraintes

| Statut Actuel | Peut être marqué comme COMPLETED ? | Raison |
|---------------|-----------------------------------|--------|
| **PENDING** | ✅ **OUI** | Le document est en attente, peut être complété |
| **EXPIRED** | ❌ **NON** | Le délai légal est dépassé, action requise |
| **COMPLETED** | ❌ **NON** | Déjà complété, pas besoin de le refaire |

---

## 📋 Exemple d'Utilisation

1. **Utilisateur voit un document avec statut PENDING**
   - Le bouton "Marquer comme complété" est visible
   - L'utilisateur clique sur le bouton
   - Une confirmation est demandée
   - Le document est marqué comme COMPLETED

2. **Utilisateur voit un document avec statut EXPIRED**
   - Le bouton "Marquer comme complété" n'est **PAS** visible
   - Un message "Expiré" est affiché
   - L'utilisateur ne peut pas changer le statut

3. **Utilisateur voit un document avec statut COMPLETED**
   - Le bouton "Marquer comme complété" n'est **PAS** visible
   - Un badge "Complété" est affiché
   - L'utilisateur ne peut pas changer le statut

---

**Tous les prompts nécessaires pour implémenter cette fonctionnalité ! 🎉**

