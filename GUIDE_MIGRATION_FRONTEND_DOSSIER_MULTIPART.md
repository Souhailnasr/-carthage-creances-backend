# 🔄 Guide de Migration Frontend - Création de Dossier avec Fichiers

## 📋 Résumé des Modifications Backend

**Nouvel endpoint ajouté** : `POST /api/dossiers/create` accepte maintenant `multipart/form-data`

**Ancien comportement** :
- ❌ `/api/dossiers/create` → Acceptait uniquement `application/json` (sans fichiers)

**Nouveau comportement** :
- ✅ `/api/dossiers/create` → Accepte maintenant **les deux formats** :
  - `application/json` (sans fichiers) - **inchangé**
  - `multipart/form-data` (avec fichiers) - **nouveau**

---

## ✅ Modifications Nécessaires Côté Frontend

### **1. Service Angular - Création de Dossier avec Fichiers**

#### **Avant (ne fonctionne plus avec fichiers)** :

```typescript
// ❌ ANCIEN CODE - Ne fonctionne pas avec multipart
createDossier(dossierData: any, isChef: boolean = false): Observable<any> {
  return this.http.post(
    `${this.apiUrl}/dossiers/create?isChef=${isChef}`,
    dossierData,  // ❌ Envoie du JSON directement
    {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.getToken()}`
      }
    }
  );
}
```

#### **Après (nouveau code avec support fichiers)** :

```typescript
// ✅ NOUVEAU CODE - Fonctionne avec multipart/form-data
createDossier(
  dossierData: any, 
  contratFile?: File, 
  pouvoirFile?: File, 
  isChef: boolean = false
): Observable<any> {
  const formData = new FormData();
  
  // 1. Ajouter le JSON du dossier (OBLIGATOIRE)
  formData.append('dossier', JSON.stringify(dossierData));
  
  // 2. Ajouter les fichiers (OPTIONNELS)
  if (contratFile) {
    formData.append('contratSigne', contratFile);
  }
  if (pouvoirFile) {
    formData.append('pouvoir', pouvoirFile);
  }
  
  // 3. Envoyer la requête
  // ⚠️ IMPORTANT : Ne PAS définir Content-Type manuellement
  // Le navigateur le fait automatiquement avec le bon boundary
  return this.http.post(
    `${this.apiUrl}/dossiers/create?isChef=${isChef}`,
    formData,
    {
      headers: {
        // ❌ NE PAS mettre 'Content-Type': 'multipart/form-data'
        'Authorization': `Bearer ${this.getToken()}`
      }
    }
  );
}

// Méthode pour créer sans fichiers (compatible avec l'ancien code)
createDossierSimple(dossierData: any, isChef: boolean = false): Observable<any> {
  return this.http.post(
    `${this.apiUrl}/dossiers/create?isChef=${isChef}`,
    dossierData,
    {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.getToken()}`
      }
    }
  );
}
```

---

### **2. Composant Angular - Formulaire de Création**

#### **Avant** :

```typescript
// ❌ ANCIEN CODE
onSubmit() {
  const dossierData = {
    titre: this.form.value.titre,
    nomCreancier: this.form.value.nomCreancier,
    nomDebiteur: this.form.value.nomDebiteur,
    // ... autres champs
  };
  
  this.dossierService.createDossier(dossierData, this.isChef)
    .subscribe({
      next: (response) => {
        console.log('Dossier créé:', response);
      },
      error: (error) => {
        console.error('Erreur:', error);
      }
    });
}
```

#### **Après** :

```typescript
// ✅ NOUVEAU CODE
onSubmit() {
  const dossierData = {
    titre: this.form.value.titre,
    nomCreancier: this.form.value.nomCreancier,
    nomDebiteur: this.form.value.nomDebiteur,
    typeDocumentJustificatif: this.form.value.typeDocumentJustificatif,
    urgence: this.form.value.urgence,
    description: this.form.value.description,
    typeCreancier: this.form.value.typeCreancier,
    typeDebiteur: this.form.value.typeDebiteur,
    // ... autres champs
  };
  
  // Récupérer les fichiers depuis les inputs file
  const contratFile = this.contratFileInput?.nativeElement?.files?.[0];
  const pouvoirFile = this.pouvoirFileInput?.nativeElement?.files?.[0];
  
  this.dossierService.createDossier(
    dossierData, 
    contratFile, 
    pouvoirFile, 
    this.isChef
  ).subscribe({
    next: (response) => {
      console.log('Dossier créé avec succès:', response);
      this.router.navigate(['/dossiers']);
    },
    error: (error) => {
      console.error('Erreur lors de la création:', error);
      this.showError(error.error?.message || 'Erreur lors de la création du dossier');
    }
  });
}
```

---

### **3. Template HTML - Inputs de Fichiers**

#### **Ajout dans le formulaire** :

```html
<!-- Formulaire existant -->
<form [formGroup]="form" (ngSubmit)="onSubmit()">
  
  <!-- Champs existants -->
  <input formControlName="titre" />
  <input formControlName="nomCreancier" />
  <!-- ... autres champs ... -->
  
  <!-- ✅ NOUVEAU : Inputs pour les fichiers -->
  <div class="form-group">
    <label for="contratSigne">Contrat Signé (PDF)</label>
    <input 
      type="file" 
      id="contratSigne"
      #contratFileInput
      accept=".pdf"
      (change)="onContratFileSelected($event)"
    />
    <small *ngIf="contratFile">Fichier sélectionné: {{ contratFile.name }}</small>
  </div>
  
  <div class="form-group">
    <label for="pouvoir">Pouvoir (PDF)</label>
    <input 
      type="file" 
      id="pouvoir"
      #pouvoirFileInput
      accept=".pdf"
      (change)="onPouvoirFileSelected($event)"
    />
    <small *ngIf="pouvoirFile">Fichier sélectionné: {{ pouvoirFile.name }}</small>
  </div>
  
  <button type="submit">Créer le Dossier</button>
</form>
```

#### **Méthodes dans le composant** :

```typescript
contratFile: File | null = null;
pouvoirFile: File | null = null;
@ViewChild('contratFileInput') contratFileInput: ElementRef | undefined;
@ViewChild('pouvoirFileInput') pouvoirFileInput: ElementRef | undefined;

onContratFileSelected(event: any) {
  const file = event.target.files[0];
  if (file) {
    // Vérifier que c'est un PDF
    if (file.type !== 'application/pdf') {
      this.showError('Le fichier contrat doit être un PDF');
      return;
    }
    // Vérifier la taille (ex: max 10MB)
    if (file.size > 10 * 1024 * 1024) {
      this.showError('Le fichier contrat ne doit pas dépasser 10MB');
      return;
    }
    this.contratFile = file;
  }
}

onPouvoirFileSelected(event: any) {
  const file = event.target.files[0];
  if (file) {
    if (file.type !== 'application/pdf') {
      this.showError('Le fichier pouvoir doit être un PDF');
      return;
    }
    if (file.size > 10 * 1024 * 1024) {
      this.showError('Le fichier pouvoir ne doit pas dépasser 10MB');
      return;
    }
    this.pouvoirFile = file;
  }
}
```

---

### **4. Intercepteur HTTP (si nécessaire)**

Si vous avez un intercepteur qui ajoute automatiquement le token, vérifiez qu'il fonctionne avec FormData :

```typescript
// ✅ Intercepteur compatible avec FormData
intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
  const token = this.authService.getToken();
  
  // Si c'est une FormData, ne pas modifier les headers Content-Type
  if (req.body instanceof FormData) {
    if (token) {
      req = req.clone({
        setHeaders: {
          'Authorization': `Bearer ${token}`
          // ❌ NE PAS ajouter 'Content-Type' pour FormData
        }
      });
    }
  } else {
    // Pour les requêtes JSON normales
    if (token) {
      req = req.clone({
        setHeaders: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
    }
  }
  
  return next.handle(req);
}
```

---

## 🔍 Vérifications à Faire

### **1. Vérifier le Service Angular**

- [ ] La méthode `createDossier` accepte maintenant les paramètres `contratFile` et `pouvoirFile`
- [ ] Utilise `FormData` pour les requêtes avec fichiers
- [ ] N'ajoute **PAS** le header `Content-Type` manuellement pour FormData
- [ ] Le champ `dossier` est bien stringifié avec `JSON.stringify()`

### **2. Vérifier le Composant**

- [ ] Les inputs file sont présents dans le template
- [ ] Les fichiers sont récupérés depuis les inputs
- [ ] La validation des fichiers (type PDF, taille) est implémentée
- [ ] Les fichiers sont passés à la méthode `createDossier`

### **3. Vérifier l'Intercepteur (si applicable)**

- [ ] L'intercepteur ne modifie pas le `Content-Type` pour les FormData
- [ ] Le token JWT est bien ajouté aux requêtes FormData

---

## 🧪 Test de l'Intégration

### **Test 1 : Création sans fichiers (rétrocompatibilité)**

```typescript
// Doit toujours fonctionner
this.dossierService.createDossierSimple(dossierData, false)
  .subscribe(response => console.log('OK'));
```

### **Test 2 : Création avec fichiers**

```typescript
// Doit maintenant fonctionner
const contratFile = new File(['...'], 'contrat.pdf', { type: 'application/pdf' });
const pouvoirFile = new File(['...'], 'pouvoir.pdf', { type: 'application/pdf' });

this.dossierService.createDossier(dossierData, contratFile, pouvoirFile, true)
  .subscribe(response => {
    console.log('Dossier créé avec fichiers:', response);
    // Vérifier que response.contratSigneUrl et response.pouvoirUrl existent
  });
```

---

## ⚠️ Erreurs Courantes à Éviter

### **Erreur 1 : Content-Type défini manuellement**

```typescript
// ❌ INCORRECT
headers: {
  'Content-Type': 'multipart/form-data', // ❌ Ne pas faire ça
  'Authorization': `Bearer ${token}`
}

// ✅ CORRECT
headers: {
  // Le navigateur ajoute automatiquement le bon Content-Type avec boundary
  'Authorization': `Bearer ${token}`
}
```

### **Erreur 2 : Objet JavaScript au lieu de JSON string**

```typescript
// ❌ INCORRECT
formData.append('dossier', dossierData); // ❌ Envoie un objet

// ✅ CORRECT
formData.append('dossier', JSON.stringify(dossierData)); // ✅ String JSON
```

### **Erreur 3 : Noms de champs incorrects**

```typescript
// ❌ INCORRECT
formData.append('contrat', file); // ❌ Mauvais nom
formData.append('pouvoirFile', file); // ❌ Mauvais nom

// ✅ CORRECT
formData.append('contratSigne', file); // ✅ Bon nom
formData.append('pouvoir', file); // ✅ Bon nom
```

---

## 📝 Exemple Complet de Service Angular

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DossierService {
  private apiUrl = `${environment.apiUrl}/dossiers`;

  constructor(private http: HttpClient) {}

  /**
   * Crée un dossier avec fichiers (multipart/form-data)
   */
  createDossier(
    dossierData: any,
    contratFile?: File,
    pouvoirFile?: File,
    isChef: boolean = false
  ): Observable<any> {
    const formData = new FormData();
    
    // Ajouter le JSON du dossier
    formData.append('dossier', JSON.stringify(dossierData));
    
    // Ajouter les fichiers si présents
    if (contratFile) {
      formData.append('contratSigne', contratFile);
    }
    if (pouvoirFile) {
      formData.append('pouvoir', pouvoirFile);
    }
    
    return this.http.post(
      `${this.apiUrl}/create?isChef=${isChef}`,
      formData,
      {
        headers: {
          // Ne pas définir Content-Type - le navigateur le fait automatiquement
        }
      }
    );
  }

  /**
   * Crée un dossier sans fichiers (application/json)
   * Pour rétrocompatibilité
   */
  createDossierSimple(dossierData: any, isChef: boolean = false): Observable<any> {
    return this.http.post(
      `${this.apiUrl}/create?isChef=${isChef}`,
      dossierData,
      {
        headers: {
          'Content-Type': 'application/json'
        }
      }
    );
  }
}
```

---

## 🎯 Résumé des Modifications

| Élément | Modification Nécessaire | Priorité |
|---------|-------------------------|----------|
| **Service Angular** | Ajouter méthode avec FormData | 🔴 **Obligatoire** |
| **Composant** | Ajouter inputs file et passer fichiers au service | 🔴 **Obligatoire** |
| **Template HTML** | Ajouter les inputs `<input type="file">` | 🔴 **Obligatoire** |
| **Intercepteur HTTP** | Vérifier compatibilité FormData | 🟡 **Recommandé** |
| **Validation fichiers** | Ajouter validation PDF/taille | 🟡 **Recommandé** |

---

## ✅ Checklist de Migration

- [ ] Service Angular modifié pour accepter fichiers
- [ ] Méthode `createDossier` utilise FormData
- [ ] Template HTML contient les inputs file
- [ ] Composant récupère les fichiers depuis les inputs
- [ ] Fichiers passés à la méthode `createDossier`
- [ ] Intercepteur HTTP compatible avec FormData
- [ ] Validation des fichiers (type PDF, taille max)
- [ ] Test de création avec fichiers fonctionne
- [ ] Test de création sans fichiers fonctionne (rétrocompatibilité)
- [ ] Messages d'erreur appropriés affichés

---

**Une fois ces modifications appliquées, le frontend sera compatible avec le nouvel endpoint backend et pourra créer des dossiers avec les fichiers contrat et pouvoir.**


