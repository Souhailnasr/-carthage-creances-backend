# 🎨 Prompts Frontend - Adaptation aux Changements Backend (Audience DTO)

## 🎯 Objectif

Adapter le frontend pour utiliser le nouveau format `AudienceRequestDTO` au lieu de l'entité `Audience` directement lors de la création et mise à jour.

---

## 📋 PROMPT 1 : Mise à Jour de l'Interface AudienceRequest

**Prompt à copier dans Cursor AI :**

```
Dans le projet Angular, localisez ou créez l'interface AudienceRequest (probablement dans src/app/models/audience.interface.ts).

Mettez à jour cette interface pour correspondre au DTO backend AudienceRequestDTO :

1. L'interface doit accepter soit dossierId soit dossier: { id }
2. L'interface doit accepter soit avocatId soit avocat: { id }
3. L'interface doit accepter soit huissierId soit huissier: { id }
4. Les autres champs restent identiques

CODE EXEMPLE :

```typescript
// audience.interface.ts

export interface AudienceRequest {
  dateAudience: string; // Format ISO: "YYYY-MM-DD"
  dateProchaine?: string; // Format ISO: "YYYY-MM-DD"
  tribunalType?: TribunalType;
  lieuTribunal?: string;
  commentaireDecision?: string;
  resultat?: DecisionResult;
  
  // Format 1 : ID simple (recommandé)
  dossierId?: number;
  avocatId?: number;
  huissierId?: number;
  
  // Format 2 : Objet avec ID (pour compatibilité)
  dossier?: { id: number };
  avocat?: { id: number };
  huissier?: { id: number };
}

// Méthode utilitaire pour créer un AudienceRequest
export function createAudienceRequest(data: {
  dateAudience: string;
  dateProchaine?: string;
  tribunalType?: TribunalType;
  lieuTribunal?: string;
  commentaireDecision?: string;
  resultat?: DecisionResult;
  dossierId?: number;
  dossier?: { id: number };
  avocatId?: number;
  avocat?: { id: number };
  huissierId?: number;
  huissier?: { id: number };
}): AudienceRequest {
  return {
    dateAudience: data.dateAudience,
    dateProchaine: data.dateProchaine,
    tribunalType: data.tribunalType,
    lieuTribunal: data.lieuTribunal,
    commentaireDecision: data.commentaireDecision,
    resultat: data.resultat,
    // Prioriser dossierId, sinon utiliser dossier.id
    dossierId: data.dossierId ?? data.dossier?.id,
    avocatId: data.avocatId ?? data.avocat?.id,
    huissierId: data.huissierId ?? data.huissier?.id
  };
}
```

IMPORTANT :
- Le backend accepte les deux formats, mais il est recommandé d'utiliser les IDs simples (dossierId, avocatId, huissierId)
- Si vous utilisez les objets (dossier: { id }), le backend les convertira automatiquement
- Le dossierId est obligatoire (le backend lève une exception si null)
```

---

## 📋 PROMPT 2 : Mise à Jour du Service AudienceService

**Prompt à copier dans Cursor AI :**

```
Dans le service AudienceService (src/app/services/audience.service.ts), mettez à jour les méthodes createAudience et updateAudience pour utiliser AudienceRequest au lieu de Audience.

Les méthodes doivent :
1. Accepter AudienceRequest au lieu de Audience
2. Envoyer le DTO au backend
3. Gérer les erreurs correctement

CODE EXEMPLE :

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Audience, AudienceRequest } from '../models/audience.interface';

@Injectable({
  providedIn: 'root'
})
export class AudienceService {
  private apiUrl = 'http://localhost:8089/carthage-creance/api/audiences';

  constructor(private http: HttpClient) {}

  /**
   * Crée une nouvelle audience
   * Utilise AudienceRequest qui sera converti en AudienceRequestDTO côté backend
   */
  createAudience(audienceRequest: AudienceRequest): Observable<Audience> {
    return this.http.post<Audience>(this.apiUrl, audienceRequest).pipe(
      catchError((error) => {
        console.error('Erreur lors de la création de l\'audience:', error);
        let errorMessage = 'Erreur lors de la création de l\'audience';
        
        if (error.error?.message) {
          errorMessage = error.error.message;
        } else if (error.error?.error) {
          errorMessage = error.error.error;
        }
        
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  /**
   * Met à jour une audience existante
   * Utilise AudienceRequest qui sera converti en AudienceRequestDTO côté backend
   */
  updateAudience(id: number, audienceRequest: AudienceRequest): Observable<Audience> {
    return this.http.put<Audience>(`${this.apiUrl}/${id}`, audienceRequest).pipe(
      catchError((error) => {
        console.error('Erreur lors de la mise à jour de l\'audience:', error);
        let errorMessage = 'Erreur lors de la mise à jour de l\'audience';
        
        if (error.error?.message) {
          errorMessage = error.error.message;
        } else if (error.error?.error) {
          errorMessage = error.error.error;
        }
        
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  // Les autres méthodes restent identiques...
}
```

IMPORTANT :
- Les méthodes createAudience et updateAudience acceptent maintenant AudienceRequest
- Le backend convertira automatiquement le format
- Gérer les erreurs avec des messages clairs
```

---

## 📋 PROMPT 3 : Mise à Jour du Composant de Formulaire

**Prompt à copier dans Cursor AI :**

```
Dans le composant AudienceFormComponent (src/app/components/audience-form/audience-form.component.ts), mettez à jour la méthode onSubmit() pour utiliser AudienceRequest au lieu de créer directement un objet Audience.

Le formulaire doit :
1. Créer un AudienceRequest avec les IDs (pas les objets complets)
2. Envoyer le AudienceRequest au service
3. Gérer les erreurs correctement

CODE EXEMPLE :

```typescript
import { Component, OnInit, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AudienceService } from '../../services/audience.service';
import { DossierService } from '../../services/dossier.service';
import { AvocatService } from '../../services/avocat.service';
import { HuissierService } from '../../services/huissier.service';
import { Audience, AudienceRequest, TribunalType, DecisionResult } from '../../models/audience.interface';

@Component({
  selector: 'app-audience-form',
  templateUrl: './audience-form.component.html',
  styleUrls: ['./audience-form.component.css']
})
export class AudienceFormComponent implements OnInit {
  audienceForm!: FormGroup;
  loading = false;
  
  dossiers: any[] = [];
  avocats: any[] = [];
  huissiers: any[] = [];
  
  tribunalTypes = Object.values(TribunalType);
  decisionResults = Object.values(DecisionResult);
  
  isEditMode = false;

  constructor(
    private fb: FormBuilder,
    private audienceService: AudienceService,
    private dossierService: DossierService,
    private avocatService: AvocatService,
    private huissierService: HuissierService,
    private dialogRef: MatDialogRef<AudienceFormComponent>,
    private snackBar: MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public data: { audience?: Audience; dossierId?: number }
  ) {
    this.initForm();
  }

  ngOnInit(): void {
    this.loadDossiers();
    this.loadAvocats();
    this.loadHuissiers();
    
    if (this.data?.audience) {
      this.isEditMode = true;
      this.populateForm(this.data.audience);
    } else if (this.data?.dossierId) {
      this.audienceForm.patchValue({ dossierId: this.data.dossierId });
    }
  }

  initForm(): void {
    this.audienceForm = this.fb.group({
      dossierId: [null, Validators.required],
      dateAudience: [null, Validators.required],
      dateProchaine: [null],
      tribunalType: [null],
      lieuTribunal: [''],
      commentaireDecision: [''],
      resultat: [null],
      avocatId: [null],
      huissierId: [null]
    });
  }

  populateForm(audience: Audience): void {
    this.audienceForm.patchValue({
      dossierId: audience.dossier?.id,
      dateAudience: audience.dateAudience,
      dateProchaine: audience.dateProchaine,
      tribunalType: audience.tribunalType,
      lieuTribunal: audience.lieuTribunal,
      commentaireDecision: audience.commentaireDecision,
      resultat: audience.resultat,
      avocatId: audience.avocat?.id,
      huissierId: audience.huissier?.id
    });
  }

  // ... méthodes loadDossiers(), loadAvocats(), loadHuissiers() ...

  onSubmit(): void {
    if (this.audienceForm.invalid) {
      this.audienceForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    const formValue = this.audienceForm.value;
    
    // ✅ CORRECTION: Créer un AudienceRequest avec les IDs (pas les objets)
    const audienceRequest: AudienceRequest = {
      dateAudience: formValue.dateAudience,
      dateProchaine: formValue.dateProchaine || undefined,
      tribunalType: formValue.tribunalType || undefined,
      lieuTribunal: formValue.lieuTribunal || undefined,
      commentaireDecision: formValue.commentaireDecision || undefined,
      resultat: formValue.resultat || undefined,
      
      // ✅ Utiliser les IDs directement (format recommandé)
      dossierId: formValue.dossierId,
      avocatId: formValue.avocatId || undefined,
      huissierId: formValue.huissierId || undefined
    };

    // Vérifier que le dossier est fourni
    if (!audienceRequest.dossierId) {
      this.snackBar.open('Le dossier est obligatoire', 'Fermer', {
        duration: 5000,
        panelClass: ['error-snackbar']
      });
      this.loading = false;
      return;
    }

    const request = this.isEditMode && this.data.audience?.id
      ? this.audienceService.updateAudience(this.data.audience.id, audienceRequest)
      : this.audienceService.createAudience(audienceRequest);

    request.subscribe({
      next: (audience) => {
        this.snackBar.open(
          this.isEditMode ? 'Audience modifiée avec succès' : 'Audience créée avec succès',
          'Fermer',
          {
            duration: 3000,
            panelClass: ['success-snackbar']
          }
        );
        this.dialogRef.close(audience);
      },
      error: (error) => {
        console.error('Erreur lors de la sauvegarde:', error);
        const errorMessage = error.message || 'Erreur lors de la sauvegarde';
        this.snackBar.open(errorMessage, 'Fermer', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
        this.loading = false;
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  // ... autres méthodes ...
}
```

IMPORTANT :
- Utiliser les IDs directement (dossierId, avocatId, huissierId) au lieu des objets
- Vérifier que dossierId est fourni (obligatoire)
- Le backend acceptera ce format et chargera les entités depuis la base de données
```

---

## 📋 PROMPT 4 : Alternative - Utiliser le Format Objet (Compatibilité)

**Prompt à copier dans Cursor AI :**

```
Si vous préférez utiliser le format objet (dossier: { id }) pour rester cohérent avec d'autres parties du code, vous pouvez utiliser cette approche :

CODE EXEMPLE :

```typescript
onSubmit(): void {
  if (this.audienceForm.invalid) {
    this.audienceForm.markAllAsTouched();
    return;
  }

  this.loading = true;
  const formValue = this.audienceForm.value;
  
  // Format objet (également accepté par le backend)
  const audienceRequest: AudienceRequest = {
    dateAudience: formValue.dateAudience,
    dateProchaine: formValue.dateProchaine || undefined,
    tribunalType: formValue.tribunalType || undefined,
    lieuTribunal: formValue.lieuTribunal || undefined,
    commentaireDecision: formValue.commentaireDecision || undefined,
    resultat: formValue.resultat || undefined,
    
    // Format objet
    dossier: formValue.dossierId ? { id: formValue.dossierId } : undefined,
    avocat: formValue.avocatId ? { id: formValue.avocatId } : undefined,
    huissier: formValue.huissierId ? { id: formValue.huissierId } : undefined
  };

  // Vérifier que le dossier est fourni
  if (!audienceRequest.dossier?.id) {
    this.snackBar.open('Le dossier est obligatoire', 'Fermer', {
      duration: 5000,
      panelClass: ['error-snackbar']
    });
    this.loading = false;
    return;
  }

  const request = this.isEditMode && this.data.audience?.id
    ? this.audienceService.updateAudience(this.data.audience.id, audienceRequest)
    : this.audienceService.createAudience(audienceRequest);

  request.subscribe({
    next: (audience) => {
      this.snackBar.open(
        this.isEditMode ? 'Audience modifiée avec succès' : 'Audience créée avec succès',
        'Fermer',
        {
          duration: 3000,
          panelClass: ['success-snackbar']
        }
      );
      this.dialogRef.close(audience);
    },
    error: (error) => {
      console.error('Erreur lors de la sauvegarde:', error);
      const errorMessage = error.message || 'Erreur lors de la sauvegarde';
      this.snackBar.open(errorMessage, 'Fermer', {
        duration: 5000,
        panelClass: ['error-snackbar']
      });
      this.loading = false;
    }
  });
}
```

IMPORTANT :
- Les deux formats fonctionnent (IDs simples ou objets)
- Le format avec IDs simples est recommandé (plus simple)
- Le backend convertira automatiquement les deux formats
```

---

## 📋 PROMPT 5 : Mise à Jour des Tests

**Prompt à copier dans Cursor AI :**

```
Mettez à jour tous les tests unitaires qui créent ou modifient des audiences pour utiliser AudienceRequest au lieu de Audience.

CODE EXEMPLE :

```typescript
// ❌ AVANT
describe('AudienceService', () => {
  it('should create an audience', () => {
    const audience: Audience = {
      id: 1,
      dateAudience: '2025-11-17',
      dossier: { id: 38 } as any,
      // ...
    };
    
    service.createAudience(audience).subscribe(...);
  });
});

// ✅ APRÈS
describe('AudienceService', () => {
  it('should create an audience', () => {
    const audienceRequest: AudienceRequest = {
      dateAudience: '2025-11-17',
      dossierId: 38, // Format recommandé
      // ...
    };
    
    service.createAudience(audienceRequest).subscribe(...);
  });
  
  // OU avec format objet
  it('should create an audience with object format', () => {
    const audienceRequest: AudienceRequest = {
      dateAudience: '2025-11-17',
      dossier: { id: 38 }, // Format objet (également accepté)
      // ...
    };
    
    service.createAudience(audienceRequest).subscribe(...);
  });
});
```

IMPORTANT :
- Mettre à jour tous les mocks de données
- Utiliser AudienceRequest dans les tests
- Tester les deux formats si nécessaire
```

---

## 📋 PROMPT 6 : Vérification des Appels API Existants

**Prompt à copier dans Cursor AI :**

```
Recherchez tous les endroits dans le code frontend où createAudience() ou updateAudience() sont appelés.

Vérifiez et corrigez pour utiliser AudienceRequest :

1. Composants de formulaire
2. Services
3. Tests
4. Utilitaires

CODE EXEMPLE DE RECHERCHE :

```bash
# Rechercher les appels à createAudience
grep -r "createAudience" src/app/

# Rechercher les appels à updateAudience
grep -r "updateAudience" src/app/
```

CODE EXEMPLE DE CORRECTION :

```typescript
// ❌ AVANT
const audience: Audience = {
  dateAudience: '2025-11-17',
  dossier: { id: 38 } as any,
  avocat: { id: 3 } as any
};
this.audienceService.createAudience(audience).subscribe(...);

// ✅ APRÈS (Format recommandé)
const audienceRequest: AudienceRequest = {
  dateAudience: '2025-11-17',
  dossierId: 38,
  avocatId: 3
};
this.audienceService.createAudience(audienceRequest).subscribe(...);

// ✅ APRÈS (Format objet - également accepté)
const audienceRequest: AudienceRequest = {
  dateAudience: '2025-11-17',
  dossier: { id: 38 },
  avocat: { id: 3 }
};
this.audienceService.createAudience(audienceRequest).subscribe(...);
```

IMPORTANT :
- Vérifier tous les appels existants
- Utiliser le format avec IDs simples (recommandé)
- Le format objet fonctionne aussi mais est moins optimal
```

---

## 📋 PROMPT 7 : Gestion des Erreurs Améliorée

**Prompt à copier dans Cursor AI :**

```
Mettez à jour la gestion des erreurs pour afficher les messages spécifiques du backend :

CODE EXEMPLE :

```typescript
createAudience(audienceRequest: AudienceRequest): Observable<Audience> {
  return this.http.post<Audience>(this.apiUrl, audienceRequest).pipe(
    catchError((error) => {
      console.error('Erreur lors de la création de l\'audience:', error);
      
      let errorMessage = 'Erreur lors de la création de l\'audience';
      
      // Messages spécifiques du backend
      if (error.error?.message) {
        errorMessage = error.error.message;
      } else if (error.error?.error) {
        errorMessage = error.error.error;
      }
      
      // Messages spécifiques pour les erreurs courantes
      if (errorMessage.includes('Dossier non trouvé')) {
        errorMessage = 'Le dossier sélectionné n\'existe pas. Veuillez sélectionner un dossier valide.';
      } else if (errorMessage.includes('Avocat non trouvé')) {
        errorMessage = 'L\'avocat sélectionné n\'existe pas. Veuillez sélectionner un avocat valide.';
      } else if (errorMessage.includes('Huissier non trouvé')) {
        errorMessage = 'L\'huissier sélectionné n\'existe pas. Veuillez sélectionner un huissier valide.';
      } else if (errorMessage.includes('obligatoire')) {
        errorMessage = 'Le dossier est obligatoire pour créer une audience.';
      }
      
      return throwError(() => new Error(errorMessage));
    })
  );
}
```

IMPORTANT :
- Afficher des messages d'erreur clairs et spécifiques
- Gérer les cas où le dossier/avocat/huissier n'existe pas
- Informer l'utilisateur de manière compréhensible
```

---

## ✅ Checklist de Vérification Frontend

- [ ] Interface `AudienceRequest` créée/mise à jour
- [ ] Service `AudienceService` mis à jour (createAudience et updateAudience)
- [ ] Composant de formulaire mis à jour pour utiliser AudienceRequest
- [ ] Tous les appels à createAudience/updateAudience vérifiés et corrigés
- [ ] Tests unitaires mis à jour
- [ ] Gestion des erreurs améliorée
- [ ] Validation que dossierId est obligatoire
- [ ] Messages d'erreur clairs pour l'utilisateur
- [ ] Format des dates vérifié (ISO: "YYYY-MM-DD")

---

## 🔍 Exemples d'Utilisation

### Exemple 1 : Création avec IDs simples (Recommandé)
```typescript
const audienceRequest: AudienceRequest = {
  dateAudience: '2025-11-17',
  dateProchaine: '2025-11-27',
  tribunalType: TribunalType.TRIBUNAL_PREMIERE_INSTANCE,
  lieuTribunal: 'Tunis',
  resultat: DecisionResult.Rapporter,
  dossierId: 38,
  avocatId: 3,
  huissierId: null
};

this.audienceService.createAudience(audienceRequest).subscribe({
  next: (audience) => {
    console.log('Audience créée:', audience);
    // Le dossier_id sera correctement sauvegardé (38)
  },
  error: (error) => {
    console.error('Erreur:', error);
  }
});
```

### Exemple 2 : Création avec format objet
```typescript
const audienceRequest: AudienceRequest = {
  dateAudience: '2025-11-17',
  dossier: { id: 38 },
  avocat: { id: 3 }
};

this.audienceService.createAudience(audienceRequest).subscribe(...);
```

### Exemple 3 : Mise à jour
```typescript
const audienceRequest: AudienceRequest = {
  dateAudience: '2025-11-20',
  dossierId: 38,
  avocatId: 5, // Changer l'avocat
  huissierId: null // Retirer l'huissier
};

this.audienceService.updateAudience(audienceId, audienceRequest).subscribe(...);
```

---

## 📋 Messages d'Erreur Possibles

| Message Backend | Signification | Action Frontend |
|----------------|---------------|-----------------|
| "Le dossier est obligatoire pour créer une audience" | dossierId est null | Afficher message d'erreur, mettre en évidence le champ dossier |
| "Dossier non trouvé avec l'ID: X" | Le dossier n'existe pas | Afficher message d'erreur, recharger la liste des dossiers |
| "Avocat non trouvé avec l'ID: X" | L'avocat n'existe pas | Afficher message d'erreur, recharger la liste des avocats |
| "Huissier non trouvé avec l'ID: X" | L'huissier n'existe pas | Afficher message d'erreur, recharger la liste des huissiers |

---

## 🎯 Résumé des Changements

### Ce qui change côté Backend :
- ✅ Le backend accepte maintenant `AudienceRequestDTO` au lieu de `Audience` directement
- ✅ Les entités (Dossier, Avocat, Huissier) sont chargées depuis la base de données
- ✅ Le `dossier_id` est maintenant correctement sauvegardé

### Ce que vous devez faire côté Frontend :
1. ✅ Créer/mettre à jour l'interface `AudienceRequest`
2. ✅ Mettre à jour `createAudience()` et `updateAudience()` pour accepter `AudienceRequest`
3. ✅ Mettre à jour les formulaires pour créer des `AudienceRequest` avec les IDs
4. ✅ Vérifier tous les appels existants
5. ✅ Mettre à jour les tests

---

**Ces prompts vous permettront d'adapter complètement le frontend aux changements backend ! 🚀**



