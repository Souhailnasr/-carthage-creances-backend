# 🔄 Intégration API Multipart - Création de Dossier

## 📋 Objectif

Adapter le service Angular existant pour :
- ✅ Utiliser la **nouvelle API multipart** quand des fichiers sont présents
- ✅ Garder l'**ancienne méthode JSON** quand il n'y a pas de fichiers
- ✅ Maintenir la compatibilité avec le code existant

---

## 🔧 Modification du Service Angular

### **Solution : Méthode Unifiée avec Détection Automatique**

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
   * Crée un dossier - Détecte automatiquement si des fichiers sont présents
   * 
   * @param dossierData Données du dossier
   * @param contratFile Fichier contrat (optionnel)
   * @param pouvoirFile Fichier pouvoir (optionnel)
   * @param isChef Indique si création en tant que chef
   * @returns Observable du dossier créé
   */
  createDossier(
    dossierData: any,
    contratFile?: File | null,
    pouvoirFile?: File | null,
    isChef: boolean = false
  ): Observable<any> {
    // Vérifier si des fichiers sont présents
    const hasFiles = (contratFile && contratFile instanceof File) || 
                     (pouvoirFile && pouvoirFile instanceof File);

    if (hasFiles) {
      // ✅ NOUVEAU : Utiliser multipart/form-data avec fichiers
      return this.createDossierWithFiles(dossierData, contratFile, pouvoirFile, isChef);
    } else {
      // ✅ ANCIEN : Utiliser application/json sans fichiers (méthode existante)
      return this.createDossierSimple(dossierData, isChef);
    }
  }

  /**
   * Crée un dossier avec fichiers (multipart/form-data)
   * NOUVEAU - Utilisé automatiquement quand des fichiers sont présents
   */
  private createDossierWithFiles(
    dossierData: any,
    contratFile?: File | null,
    pouvoirFile?: File | null,
    isChef: boolean = false
  ): Observable<any> {
    const formData = new FormData();
    
    // 1. Ajouter le JSON du dossier (OBLIGATOIRE)
    formData.append('dossier', JSON.stringify(dossierData));
    
    // 2. Ajouter les fichiers si présents (OPTIONNELS)
    if (contratFile && contratFile instanceof File) {
      formData.append('contratSigne', contratFile);
    }
    if (pouvoirFile && pouvoirFile instanceof File) {
      formData.append('pouvoir', pouvoirFile);
    }
    
    // 3. Envoyer la requête multipart
    // ⚠️ IMPORTANT : Ne PAS définir Content-Type manuellement
    return this.http.post(
      `${this.apiUrl}/create?isChef=${isChef}`,
      formData,
      {
        headers: {
          // Le navigateur ajoute automatiquement le Content-Type avec boundary
          // Ne pas mettre 'Content-Type': 'multipart/form-data'
        }
      }
    );
  }

  /**
   * Crée un dossier sans fichiers (application/json)
   * ANCIEN - Garde la méthode existante qui fonctionne
   */
  private createDossierSimple(
    dossierData: any,
    isChef: boolean = false
  ): Observable<any> {
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

## 📝 Utilisation dans le Composant

### **Exemple d'utilisation - Aucun changement nécessaire dans la logique**

```typescript
import { Component } from '@angular/core';
import { DossierService } from './services/dossier.service';

@Component({
  selector: 'app-dossier-form',
  templateUrl: './dossier-form.component.html'
})
export class DossierFormComponent {
  constructor(private dossierService: DossierService) {}

  // Variables pour les fichiers (déjà existantes dans votre code)
  contratFile: File | null = null;
  pouvoirFile: File | null = null;

  onSubmit() {
    // Données du formulaire (déjà existantes)
    const dossierData = {
      titre: this.form.value.titre,
      nomCreancier: this.form.value.nomCreancier,
      nomDebiteur: this.form.value.nomDebiteur,
      typeDocumentJustificatif: this.form.value.typeDocumentJustificatif,
      urgence: this.form.value.urgence,
      description: this.form.value.description,
      typeCreancier: this.form.value.typeCreancier,
      typeDebiteur: this.form.value.typeDebiteur,
      // ... autres champs existants
    };

    // ✅ Appel unifié - Le service détecte automatiquement s'il y a des fichiers
    this.dossierService.createDossier(
      dossierData,
      this.contratFile,  // Peut être null
      this.pouvoirFile,  // Peut être null
      this.isChef
    ).subscribe({
      next: (response) => {
        console.log('Dossier créé avec succès:', response);
        // Votre logique de succès existante
        this.router.navigate(['/dossiers']);
      },
      error: (error) => {
        console.error('Erreur lors de la création:', error);
        // Votre gestion d'erreur existante
        this.showError(error.error?.message || 'Erreur lors de la création du dossier');
      }
    });
  }

  // Méthodes de sélection de fichiers (déjà existantes)
  onContratFileSelected(event: any) {
    const file = event.target.files[0];
    if (file && file.type === 'application/pdf') {
      this.contratFile = file;
    } else {
      this.showError('Le fichier contrat doit être un PDF');
    }
  }

  onPouvoirFileSelected(event: any) {
    const file = event.target.files[0];
    if (file && file.type === 'application/pdf') {
      this.pouvoirFile = file;
    } else {
      this.showError('Le fichier pouvoir doit être un PDF');
    }
  }
}
```

---

## 🔍 Vérification de l'Intercepteur HTTP

### **Si vous avez un intercepteur, vérifiez qu'il gère FormData**

```typescript
import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler } from '@angular/common/http';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler) {
    const token = this.getToken(); // Votre méthode pour récupérer le token
    
    // ✅ Gérer FormData différemment
    if (req.body instanceof FormData) {
      // Pour FormData, ne pas modifier le Content-Type
      // Le navigateur le définit automatiquement avec le bon boundary
      if (token) {
        req = req.clone({
          setHeaders: {
            'Authorization': `Bearer ${token}`
            // ❌ NE PAS ajouter 'Content-Type' ici
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
}
```

---

## ✅ Avantages de cette Approche

1. **✅ Aucun changement dans le composant** - La méthode `createDossier()` reste la même
2. **✅ Détection automatique** - Le service choisit la bonne méthode selon les fichiers
3. **✅ Rétrocompatibilité** - L'ancienne méthode JSON continue de fonctionner
4. **✅ Code propre** - Une seule méthode publique, logique interne gérée automatiquement

---

## 🧪 Scénarios de Test

### **Scénario 1 : Création sans fichiers (ancienne méthode)**

```typescript
// ✅ Utilise automatiquement createDossierSimple()
this.dossierService.createDossier(dossierData, null, null, false)
  .subscribe(response => {
    // Fonctionne comme avant
  });
```

### **Scénario 2 : Création avec contrat uniquement**

```typescript
// ✅ Utilise automatiquement createDossierWithFiles()
this.dossierService.createDossier(dossierData, contratFile, null, false)
  .subscribe(response => {
    // Utilise multipart/form-data
  });
```

### **Scénario 3 : Création avec pouvoir uniquement**

```typescript
// ✅ Utilise automatiquement createDossierWithFiles()
this.dossierService.createDossier(dossierData, null, pouvoirFile, false)
  .subscribe(response => {
    // Utilise multipart/form-data
  });
```

### **Scénario 4 : Création avec les deux fichiers**

```typescript
// ✅ Utilise automatiquement createDossierWithFiles()
this.dossierService.createDossier(dossierData, contratFile, pouvoirFile, true)
  .subscribe(response => {
    // Utilise multipart/form-data
    // Dossier créé en tant que chef
  });
```

---

## ⚠️ Points d'Attention

### **1. Format du champ "dossier" dans FormData**

```typescript
// ✅ CORRECT
formData.append('dossier', JSON.stringify(dossierData));

// ❌ INCORRECT
formData.append('dossier', dossierData); // Ne pas envoyer l'objet directement
```

### **2. Noms des champs pour les fichiers**

```typescript
// ✅ CORRECT
formData.append('contratSigne', contratFile); // Nom exact
formData.append('pouvoir', pouvoirFile);       // Nom exact

// ❌ INCORRECT
formData.append('contrat', contratFile);      // Mauvais nom
formData.append('pouvoirFile', pouvoirFile);  // Mauvais nom
```

### **3. Headers HTTP pour FormData**

```typescript
// ✅ CORRECT - Le navigateur ajoute automatiquement le Content-Type
headers: {
  'Authorization': `Bearer ${token}`
  // Pas de 'Content-Type' pour FormData
}

// ❌ INCORRECT
headers: {
  'Content-Type': 'multipart/form-data', // ❌ Ne pas faire ça
  'Authorization': `Bearer ${token}`
}
```

---

## 📋 Checklist d'Intégration

- [ ] Service Angular modifié avec méthode `createDossier()` unifiée
- [ ] Méthode privée `createDossierWithFiles()` pour multipart
- [ ] Méthode privée `createDossierSimple()` garde l'ancienne logique
- [ ] Intercepteur HTTP vérifié pour gérer FormData (si applicable)
- [ ] Test de création sans fichiers (doit utiliser JSON)
- [ ] Test de création avec fichiers (doit utiliser multipart)
- [ ] Vérification des logs backend pour confirmer le bon endpoint utilisé

---

## 🎯 Résultat Final

Avec cette modification, votre code existant continue de fonctionner **sans changement**, et vous pouvez maintenant :

1. ✅ Créer des dossiers **sans fichiers** → Utilise l'ancienne méthode JSON (fonctionne déjà)
2. ✅ Créer des dossiers **avec fichiers** → Utilise automatiquement la nouvelle méthode multipart
3. ✅ **Aucun changement** dans les composants existants
4. ✅ **Détection automatique** selon la présence de fichiers

---

## 🔄 Migration Progressive

Si vous voulez migrer progressivement :

1. **Étape 1** : Ajouter la méthode `createDossierWithFiles()` dans le service
2. **Étape 2** : Modifier `createDossier()` pour détecter les fichiers
3. **Étape 3** : Tester avec et sans fichiers
4. **Étape 4** : Déployer - Aucun changement visible pour les utilisateurs

**Le code existant continue de fonctionner pendant la migration !**

---

**Cette solution vous permet d'utiliser la nouvelle API multipart tout en gardant la compatibilité avec votre code existant.**


