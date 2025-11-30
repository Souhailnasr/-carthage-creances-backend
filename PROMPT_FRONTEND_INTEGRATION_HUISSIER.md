# 📋 Prompts pour l'Intégration de l'Huissier dans le Frontend

## 🎯 Objectif
Intégrer correctement la logique de l'huissier dans le formulaire de création d'audience côté frontend Angular.

---

## 📝 PROMPT 1 : Interface TypeScript pour Huissier

**Créer ou modifier** : `src/app/models/huissier.model.ts`

```typescript
export interface Huissier {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  specialite?: string;
  adresse?: string;
}

export interface HuissierReference {
  id: number;
}
```

**Instructions** :
- Créer l'interface `Huissier` avec tous les champs correspondant au backend
- Créer l'interface `HuissierReference` pour les références dans les DTOs
- Les champs optionnels (`specialite`, `adresse`) doivent être marqués avec `?`

---

## 📝 PROMPT 2 : Service Angular pour les Huissiers

**Créer ou modifier** : `src/app/services/huissier.service.ts`

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Huissier } from '../models/huissier.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class HuissierService {
  private apiUrl = `${environment.apiUrl}/huissiers`;

  constructor(private http: HttpClient) {}

  /**
   * Récupère tous les huissiers
   * GET /api/huissiers
   */
  getAllHuissiers(): Observable<Huissier[]> {
    return this.http.get<Huissier[]>(this.apiUrl);
  }

  /**
   * Récupère un huissier par son ID
   * GET /api/huissiers/{id}
   */
  getHuissierById(id: number): Observable<Huissier> {
    return this.http.get<Huissier>(`${this.apiUrl}/${id}`);
  }

  /**
   * Recherche des huissiers par terme de recherche
   * GET /api/huissiers/search?searchTerm={term}
   */
  searchHuissiers(searchTerm: string): Observable<Huissier[]> {
    return this.http.get<Huissier[]>(`${this.apiUrl}/search`, {
      params: { searchTerm }
    });
  }

  /**
   * Crée un nouvel huissier
   * POST /api/huissiers
   */
  createHuissier(huissier: Partial<Huissier>): Observable<Huissier> {
    return this.http.post<Huissier>(this.apiUrl, huissier);
  }

  /**
   * Met à jour un huissier
   * PUT /api/huissiers/{id}
   */
  updateHuissier(id: number, huissier: Partial<Huissier>): Observable<Huissier> {
    return this.http.put<Huissier>(`${this.apiUrl}/${id}`, huissier);
  }

  /**
   * Supprime un huissier
   * DELETE /api/huissiers/{id}
   */
  deleteHuissier(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
```

**Instructions** :
- Créer le service avec toutes les méthodes nécessaires
- Utiliser `environment.apiUrl` pour l'URL de base
- Importer `HttpClient` et `Observable` depuis RxJS
- Ajouter la gestion d'erreurs si nécessaire

---

## 📝 PROMPT 3 : Modification du Service Audience pour inclure l'Huissier

**Modifier** : `src/app/services/audience.service.ts`

**Dans la méthode `createAudience()` ou similaire**, modifier le format du payload pour inclure l'huissier :

```typescript
createAudience(audienceData: any): Observable<Audience> {
  const payload = {
    dateAudience: this.formatDate(audienceData.dateAudience),
    dateProchaine: audienceData.dateProchaine ? this.formatDate(audienceData.dateProchaine) : null,
    tribunalType: audienceData.tribunalType,
    lieuTribunal: audienceData.lieuTribunal,
    commentaireDecision: audienceData.commentaireDecision || null,
    resultat: audienceData.resultat,
    dossier: {
      id: audienceData.dossierId
    },
    avocat: audienceData.avocatId ? {
      id: audienceData.avocatId
    } : null,
    huissier: audienceData.huissierId ? {
      id: audienceData.huissierId
    } : null  // ✅ Important : null si non sélectionné
  };

  console.log('Payload envoyé au backend:', payload);
  
  return this.http.post<Audience>(`${this.apiUrl}`, payload);
}

/**
 * Formate une date au format YYYY-MM-DD
 */
private formatDate(date: Date | string): string {
  if (!date) return null;
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}
```

**Points importants** :
- ✅ `huissier: null` si aucun huissier n'est sélectionné
- ✅ `huissier: { id: X }` si un huissier est sélectionné
- ✅ Format des dates en `YYYY-MM-DD`
- ✅ Gestion des valeurs null pour les champs optionnels

---

## 📝 PROMPT 4 : Modification du Composant de Formulaire d'Audience

**Modifier** : `src/app/components/gestion-audiences/gestion-audiences.component.ts`

### 4.1. Imports et Déclarations

```typescript
import { HuissierService } from '../../services/huissier.service';
import { Huissier } from '../../models/huissier.model';

export class GestionAudiencesComponent implements OnInit {
  // ... autres propriétés existantes ...
  
  // Liste des huissiers disponibles
  huissiers: Huissier[] = [];
  selectedHuissierId: number | null = null;
  isLoadingHuissiers = false;

  constructor(
    // ... autres services ...
    private huissierService: HuissierService
  ) {}
}
```

### 4.2. Chargement des Huissiers dans ngOnInit()

```typescript
ngOnInit(): void {
  // ... autres initialisations ...
  this.loadHuissiers();
}

/**
 * Charge la liste des huissiers depuis le backend
 */
loadHuissiers(): void {
  this.isLoadingHuissiers = true;
  this.huissierService.getAllHuissiers().subscribe({
    next: (huissiers) => {
      this.huissiers = huissiers;
      this.isLoadingHuissiers = false;
      console.log('Huissiers chargés:', huissiers);
    },
    error: (error) => {
      console.error('Erreur lors du chargement des huissiers:', error);
      this.isLoadingHuissiers = false;
      // Optionnel : afficher un message d'erreur à l'utilisateur
    }
  });
}
```

### 4.3. Méthode pour la Sélection d'un Huissier

```typescript
/**
 * Gère la sélection d'un huissier dans le formulaire
 */
onHuissierSelected(huissierId: number | null): void {
  this.selectedHuissierId = huissierId;
  console.log('Huissier sélectionné:', huissierId);
}
```

### 4.4. Méthode de Soumission du Formulaire

```typescript
/**
 * Soumet le formulaire de création d'audience
 */
onSubmitAudienceForm(): void {
  if (!this.audienceForm.valid) {
    console.error('Formulaire invalide');
    return;
  }

  const formData = {
    ...this.audienceForm.value,
    huissierId: this.selectedHuissierId  // ✅ Inclure l'ID de l'huissier sélectionné
  };

  this.audienceService.createAudience(formData).subscribe({
    next: (audience) => {
      console.log('Audience créée avec succès:', audience);
      // Fermer le modal, recharger la liste, etc.
      this.closeModal();
      this.loadAudiences();
    },
    error: (error) => {
      console.error('Erreur lors de la création de l\'audience:', error);
      // Afficher un message d'erreur à l'utilisateur
    }
  });
}
```

---

## 📝 PROMPT 5 : Modification du Template HTML du Formulaire

**Modifier** : `src/app/components/gestion-audiences/gestion-audiences.component.html`

### 5.1. Ajouter le Champ Huissier dans le Formulaire

```html
<!-- Dans le formulaire "Ajouter Audience" -->

<!-- Champ Avocat (existant) -->
<div class="form-group">
  <label for="avocat">Avocat assigné</label>
  <select 
    id="avocat" 
    class="form-control" 
    [(ngModel)]="selectedAvocatId"
    name="avocatId">
    <option [ngValue]="null">Sélectionner un avocat...</option>
    <option *ngFor="let avocat of avocats" [ngValue]="avocat.id">
      {{ avocat.prenom }} {{ avocat.nom }}
    </option>
  </select>
</div>

<!-- ✅ NOUVEAU : Champ Huissier -->
<div class="form-group">
  <label for="huissier">Huissier assigné</label>
  <select 
    id="huissier" 
    class="form-control" 
    [(ngModel)]="selectedHuissierId"
    name="huissierId"
    [disabled]="isLoadingHuissiers">
    <option [ngValue]="null">Sélectionner un huissier...</option>
    <option *ngFor="let huissier of huissiers" [ngValue]="huissier.id">
      {{ huissier.prenom }} {{ huissier.nom }}
      <span *ngIf="huissier.specialite"> - {{ huissier.specialite }}</span>
    </option>
  </select>
  <small class="form-text text-muted" *ngIf="isLoadingHuissiers">
    Chargement des huissiers...
  </small>
  <small class="form-text text-muted">
    Ce champ est optionnel
  </small>
</div>
```

### 5.2. Version avec Material Angular (si utilisé)

```html
<!-- Si vous utilisez Angular Material -->
<mat-form-field appearance="outline">
  <mat-label>Huissier assigné</mat-label>
  <mat-select 
    [(ngModel)]="selectedHuissierId"
    name="huissierId"
    [disabled]="isLoadingHuissiers">
    <mat-option [value]="null">Sélectionner un huissier...</mat-option>
    <mat-option 
      *ngFor="let huissier of huissiers" 
      [value]="huissier.id">
      {{ huissier.prenom }} {{ huissier.nom }}
      <span *ngIf="huissier.specialite"> - {{ huissier.specialite }}</span>
    </mat-option>
  </mat-select>
  <mat-hint>Ce champ est optionnel</mat-hint>
  <mat-spinner *ngIf="isLoadingHuissiers" diameter="20"></mat-spinner>
</mat-form-field>
```

---

## 📝 PROMPT 6 : Gestion des Erreurs et Validation

**Modifier** : `src/app/services/audience.service.ts` ou `error.interceptor.ts`

```typescript
// Dans le service ou l'intercepteur d'erreurs
handleError(error: HttpErrorResponse): Observable<never> {
  let errorMessage = 'Erreur lors de la création de l\'audience';
  
  if (error.error) {
    if (error.error.message) {
      errorMessage = error.error.message;
    } else if (typeof error.error === 'string') {
      errorMessage = error.error;
    }
  }
  
  // Gestion spécifique pour les erreurs de transaction
  if (errorMessage.includes('Transaction silently rolled back')) {
    errorMessage = 'Erreur lors de la sauvegarde. Veuillez vérifier que le dossier existe et que toutes les données sont valides.';
  }
  
  console.error('Détails de l\'erreur:', {
    status: error.status,
    statusText: error.statusText,
    error: error.error,
    message: errorMessage,
    url: error.url
  });
  
  return throwError(() => new Error(errorMessage));
}
```

---

## 📝 PROMPT 7 : Mise à Jour du Module Angular

**Vérifier** : `src/app/app.module.ts` ou le module correspondant

```typescript
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

@NgModule({
  imports: [
    // ... autres imports ...
    HttpClientModule,  // ✅ Nécessaire pour les appels HTTP
    FormsModule,        // ✅ Nécessaire pour ngModel
  ],
  providers: [
    // ... autres services ...
    HuissierService,   // ✅ Ajouter le service si pas déjà fourni
  ]
})
export class AppModule { }
```

---

## 📝 PROMPT 8 : Test et Validation

**Créer** : Tests unitaires si nécessaire

```typescript
// Exemple de test pour le service
describe('AudienceService', () => {
  it('devrait envoyer huissier: null si aucun huissier n\'est sélectionné', () => {
    const payload = service.createAudiencePayload({
      dossierId: 1,
      avocatId: 2,
      huissierId: null
    });
    
    expect(payload.huissier).toBeNull();
  });
  
  it('devrait envoyer huissier: { id: X } si un huissier est sélectionné', () => {
    const payload = service.createAudiencePayload({
      dossierId: 1,
      avocatId: 2,
      huissierId: 5
    });
    
    expect(payload.huissier).toEqual({ id: 5 });
  });
});
```

---

## ✅ Checklist de Vérification

- [ ] Interface `Huissier` créée avec tous les champs
- [ ] Service `HuissierService` créé avec la méthode `getAllHuissiers()`
- [ ] Service injecté dans le composant
- [ ] Liste des huissiers chargée dans `ngOnInit()`
- [ ] Champ `<select>` ajouté dans le template HTML
- [ ] Variable `selectedHuissierId` liée avec `[(ngModel)]`
- [ ] `huissierId` inclus dans les données du formulaire
- [ ] Format du payload : `huissier: null` ou `huissier: { id: X }`
- [ ] Gestion des erreurs améliorée
- [ ] Dates formatées en `YYYY-MM-DD`
- [ ] Tests effectués avec et sans huissier sélectionné

---

## 🎯 Format Final du Payload

Le payload envoyé au backend doit avoir cette structure :

```json
{
  "dateAudience": "2025-11-29",
  "dateProchaine": "2025-12-05",
  "tribunalType": "TRIBUNAL_APPEL",
  "lieuTribunal": "Tunis",
  "commentaireDecision": "Détails...",
  "resultat": "POSITIVE",
  "dossier": {
    "id": 38
  },
  "avocat": {
    "id": 3
  },
  "huissier": null  // ✅ null si non sélectionné, ou { "id": 2 } si sélectionné
}
```

---

## 📌 Notes Importantes

1. **L'huissier est optionnel** : Le champ peut être `null` ou omis
2. **Format objet** : Utiliser `{ id: X }` et non `huissierId: X` directement
3. **Chargement asynchrone** : Afficher un indicateur de chargement pendant le fetch des huissiers
4. **Gestion d'erreurs** : Prévoir des messages clairs si le chargement échoue
5. **Validation** : Le formulaire doit rester valide même si aucun huissier n'est sélectionné

---

## 🚀 Ordre d'Implémentation Recommandé

1. **Étape 1** : Créer l'interface `Huissier` (PROMPT 1)
2. **Étape 2** : Créer le service `HuissierService` (PROMPT 2)
3. **Étape 3** : Modifier le service `AudienceService` (PROMPT 3)
4. **Étape 4** : Modifier le composant TypeScript (PROMPT 4)
5. **Étape 5** : Modifier le template HTML (PROMPT 5)
6. **Étape 6** : Tester et valider (PROMPT 8)

---

**Bon développement ! 🎉**


