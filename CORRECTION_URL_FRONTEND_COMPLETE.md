# 🔧 Correction : URL Frontend pour Marquer un Document comme Complété

## 🎯 Problème Identifié

L'erreur `No static resource api/huissier/document/1/complete` indique que l'URL appelée depuis le frontend **ne contient pas le context-path** `/carthage-creance`.

## ✅ Solution : Corriger l'URL dans le Service Angular

### **ÉTAPE 1 : Vérifier la Configuration de l'API URL**

Dans `src/environments/environment.ts` (ou `environment.prod.ts` :

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8089/carthage-creance/api'
  // OU
  // apiUrl: 'http://localhost:8089/carthage-creance'
};
```

### **ÉTAPE 2 : Corriger le Service Angular**

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
  // OPTION 1 : Si apiUrl contient déjà '/api'
  private apiUrl = `${environment.apiUrl}/huissier`;
  
  // OPTION 2 : Si apiUrl ne contient pas '/api'
  // private apiUrl = `${environment.apiUrl}/api/huissier`;

  constructor(private http: HttpClient) {}

  // ... autres méthodes ...

  /**
   * Marque un document comme complété
   * PUT /carthage-creance/api/huissier/document/{id}/complete
   */
  markDocumentAsCompleted(id: number): Observable<DocumentHuissier> {
    // L'URL complète sera : http://localhost:8089/carthage-creance/api/huissier/document/{id}/complete
    return this.http.put<DocumentHuissier>(
      `${this.apiUrl}/document/${id}/complete`, 
      {}
    );
  }
}
```

### **ÉTAPE 3 : Vérifier l'URL Complète**

L'URL complète devrait être :
```
PUT http://localhost:8089/carthage-creance/api/huissier/document/{id}/complete
```

**Décomposition** :
- Base URL : `http://localhost:8089`
- Context-path : `/carthage-creance`
- Route du contrôleur : `/api/huissier`
- Route de la méthode : `/document/{id}/complete`

### **ÉTAPE 4 : Tester avec les DevTools**

Dans la console du navigateur, vérifiez l'URL exacte appelée :

1. Ouvrez les **DevTools** (F12)
2. Allez dans l'onglet **Network**
3. Cliquez sur "Marquer comme complété"
4. Vérifiez l'URL de la requête PUT

L'URL devrait être :
```
http://localhost:8089/carthage-creance/api/huissier/document/1/complete
```

**Si l'URL est différente**, corrigez le service Angular.

---

## 🔍 Exemples de Configuration

### **Configuration 1 : apiUrl avec '/api'**

```typescript
// environment.ts
export const environment = {
  apiUrl: 'http://localhost:8089/carthage-creance/api'
};

// service
private apiUrl = `${environment.apiUrl}/huissier`;
// Résultat : http://localhost:8089/carthage-creance/api/huissier
```

### **Configuration 2 : apiUrl sans '/api'**

```typescript
// environment.ts
export const environment = {
  apiUrl: 'http://localhost:8089/carthage-creance'
};

// service
private apiUrl = `${environment.apiUrl}/api/huissier`;
// Résultat : http://localhost:8089/carthage-creance/api/huissier
```

---

## ✅ Checklist

- [ ] Vérifier que `environment.apiUrl` contient `/carthage-creance`
- [ ] Vérifier que le service utilise correctement `apiUrl`
- [ ] Tester l'URL complète dans les DevTools (Network)
- [ ] Redémarrer le serveur backend
- [ ] Tester à nouveau

---

## 🚀 Action Immédiate

1. **Vérifiez** l'URL dans le service Angular
2. **Corrigez** si nécessaire
3. **Redémarrez** le serveur backend
4. **Testez** à nouveau

---

**Le problème devrait être résolu après avoir corrigé l'URL ! 🎉**

