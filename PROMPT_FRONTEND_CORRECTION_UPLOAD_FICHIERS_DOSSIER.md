# 🎯 Prompt Frontend : Correction Upload Fichiers Dossier

## 📋 Contexte

Le backend retourne l'erreur `Content-Type 'application/octet-stream' is not supported` lors de la création d'un dossier avec fichiers (contrat et/ou pouvoir).

**Problème identifié :** Le champ `dossier` dans le FormData est envoyé comme objet JavaScript au lieu d'une chaîne JSON.

**Solution :** Envoyer le champ `dossier` comme chaîne JSON (`JSON.stringify()`) dans le FormData.

---

## 🔧 Modifications Requises

### 1. Localiser le Service/Component de Création de Dossier

Trouver le fichier qui gère la création de dossier avec upload de fichiers :
- Probablement : `dossier-api.service.ts` ou `dossier.service.ts`
- Ou dans un component : `create-dossier.component.ts`, `gestion-dossier.component.ts`

### 2. Identifier la Méthode de Création

Chercher la méthode qui envoie la requête POST vers `/api/dossiers/create` avec des fichiers.

**Indicateurs à chercher :**
- `FormData` est utilisé
- `append('dossier', ...)` ou `append('contratSigne', ...)` ou `append('pouvoir', ...)`
- URL contient `/api/dossiers/create`
- Méthode HTTP `POST`

---

## ✅ Corrections à Appliquer

### Correction 1 : Format du Champ "dossier" dans FormData

#### ❌ AVANT (Incorrect)
```typescript
const formData = new FormData();
formData.append('dossier', dossierData); // ❌ Envoie comme objet JavaScript
formData.append('contratSigne', contratFile);
formData.append('pouvoir', pouvoirFile);
```

#### ✅ APRÈS (Correct)
```typescript
const formData = new FormData();
formData.append('dossier', JSON.stringify(dossierData)); // ✅ Envoie comme JSON string
formData.append('contratSigne', contratFile);
formData.append('pouvoir', pouvoirFile);
```

### Correction 2 : Ne PAS Définir Content-Type Manuellement

#### ❌ AVANT (Incorrect)
```typescript
const headers = {
  'Content-Type': 'multipart/form-data', // ❌ Ne pas définir manuellement
  'Authorization': 'Bearer ' + token
};
```

#### ✅ APRÈS (Correct)
```typescript
const headers = {
  // Ne PAS mettre 'Content-Type' ici
  // Le navigateur l'ajoutera automatiquement avec le boundary
  'Authorization': 'Bearer ' + token
};
```

### Correction 3 : Logique Conditionnelle (Si Pas Déjà Implémentée)

Détecter si des fichiers sont nécessaires et utiliser le bon endpoint :

```typescript
// Détecter si des fichiers sont nécessaires
const hasFiles = (pouvoirChecked && pouvoirFile) || (contratChecked && contratFile);

if (hasFiles) {
    // Utiliser multipart/form-data
    const formData = new FormData();
    formData.append('dossier', JSON.stringify(dossierData));
    
    if (contratChecked && contratFile) {
        formData.append('contratSigne', contratFile);
    }
    if (pouvoirChecked && pouvoirFile) {
        formData.append('pouvoir', pouvoirFile);
    }
    
    // Envoyer avec FormData (sans Content-Type manuel)
    return this.http.post(
        `${this.apiUrl}/dossiers/create?isChef=${isChef}`,
        formData,
        {
            headers: {
                'Authorization': 'Bearer ' + token
                // Ne PAS mettre 'Content-Type' ici
            }
        }
    );
} else {
    // Utiliser JSON simple
    return this.http.post(
        `${this.apiUrl}/dossiers/create?isChef=${isChef}`,
        dossierData,
        {
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            }
        }
    );
}
```

---

## 📝 Exemple Complet de Méthode Corrigée

### Méthode TypeScript/Angular Complète

```typescript
createDossierWithFiles(
    dossierData: any,
    contratChecked: boolean,
    pouvoirChecked: boolean,
    contratFile: File | null,
    pouvoirFile: File | null,
    isChef: boolean = false
): Observable<any> {
    const token = this.authService.getToken();
    
    // Détecter si des fichiers sont nécessaires
    const hasFiles = (pouvoirChecked && pouvoirFile) || (contratChecked && contratFile);
    
    if (hasFiles) {
        // ✅ CORRECTION : Utiliser multipart/form-data avec dossier comme JSON string
        const formData = new FormData();
        
        // ✅ CORRECTION PRINCIPALE : Envoyer dossier comme JSON string
        formData.append('dossier', JSON.stringify(dossierData));
        
        // Ajouter les fichiers si présents
        if (contratChecked && contratFile) {
            formData.append('contratSigne', contratFile);
        }
        if (pouvoirChecked && pouvoirFile) {
            formData.append('pouvoir', pouvoirFile);
        }
        
        // ✅ CORRECTION : Ne PAS définir Content-Type manuellement
        return this.http.post<any>(
            `${this.apiUrl}/dossiers/create?isChef=${isChef}`,
            formData,
            {
                headers: {
                    'Authorization': `Bearer ${token}`
                    // Ne PAS mettre 'Content-Type' ici
                }
            }
        );
    } else {
        // Utiliser JSON simple (sans fichiers)
        return this.http.post<any>(
            `${this.apiUrl}/dossiers/create?isChef=${isChef}`,
            dossierData,
            {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                }
            }
        );
    }
}
```

---

## 🔍 Points de Vérification

### 1. Vérifier le Format du FormData

Dans la console du navigateur, vérifier que le FormData contient :
- `dossier` : doit être une **string JSON**, pas un objet
- `contratSigne` : doit être un **File**
- `pouvoir` : doit être un **File**

**Test dans la console :**
```javascript
const formData = new FormData();
formData.append('dossier', JSON.stringify({test: 'data'}));
formData.append('file', new File([''], 'test.pdf'));

// Vérifier
for (let [key, value] of formData.entries()) {
    console.log(key, value, typeof value);
}
// Résultat attendu :
// dossier "[object Object]" string  (ou la string JSON)
// file File object
```

### 2. Vérifier les Headers HTTP

Dans l'onglet Network des DevTools :
- **Content-Type** doit être : `multipart/form-data; boundary=----WebKitFormBoundary...`
- Le **boundary** doit être ajouté automatiquement par le navigateur
- **Ne PAS** avoir de `Content-Type: multipart/form-data` sans boundary dans les headers manuels

### 3. Vérifier la Requête HTTP

Dans l'onglet Network, la requête doit :
- **URL** : `http://localhost:8089/carthage-creance/api/dossiers/create?isChef=true`
- **Method** : `POST`
- **Content-Type** : `multipart/form-data; boundary=...` (ajouté automatiquement)
- **Payload** : doit montrer les parties FormData avec `dossier` comme text/JSON

---

## 🐛 Dépannage

### Problème : L'erreur persiste après correction

**Vérifications :**
1. ✅ Le champ `dossier` est bien envoyé comme `JSON.stringify(dossierData)` ?
2. ✅ Le `Content-Type` n'est PAS défini manuellement dans les headers ?
3. ✅ Les fichiers sont bien des objets `File` et non des strings/URLs ?
4. ✅ Le token JWT est bien présent dans le header `Authorization` ?

### Problème : Le dossier est créé mais sans fichiers

**Vérifications :**
1. ✅ Les fichiers sont bien ajoutés au FormData avec `formData.append('contratSigne', file)` ?
2. ✅ Les fichiers ne sont pas `null` ou `undefined` ?
3. ✅ Les noms des champs correspondent : `contratSigne` et `pouvoir` (exactement) ?

### Problème : Erreur CORS ou 401 Unauthorized

**Vérifications :**
1. ✅ Le token JWT est valide et non expiré ?
2. ✅ Le header `Authorization` est bien formaté : `Bearer {token}` ?
3. ✅ Les CORS sont bien configurés côté backend ?

---

## 📋 Checklist de Vérification

Avant de tester, vérifier que :

- [ ] Le champ `dossier` est envoyé avec `JSON.stringify(dossierData)`
- [ ] Le `Content-Type` n'est PAS défini manuellement pour FormData
- [ ] Les fichiers sont ajoutés au FormData uniquement s'ils existent
- [ ] La logique conditionnelle détecte correctement si des fichiers sont nécessaires
- [ ] Le token JWT est bien inclus dans le header `Authorization`
- [ ] L'URL de l'endpoint est correcte : `/api/dossiers/create?isChef={boolean}`

---

## 🧪 Tests à Effectuer

### Test 1 : Création avec Contrat uniquement
- ✅ Cocher "Contrat Signé"
- ✅ Sélectionner un fichier PDF
- ✅ Créer le dossier
- **Résultat attendu :** Dossier créé avec le fichier contrat uploadé

### Test 2 : Création avec Pouvoir uniquement
- ✅ Cocher "Pouvoir"
- ✅ Sélectionner un fichier PDF
- ✅ Créer le dossier
- **Résultat attendu :** Dossier créé avec le fichier pouvoir uploadé

### Test 3 : Création avec Contrat ET Pouvoir
- ✅ Cocher "Contrat Signé" ET "Pouvoir"
- ✅ Sélectionner deux fichiers PDF
- ✅ Créer le dossier
- **Résultat attendu :** Dossier créé avec les deux fichiers uploadés

### Test 4 : Création sans fichiers
- ✅ Ne cocher ni "Contrat Signé" ni "Pouvoir"
- ✅ Créer le dossier
- **Résultat attendu :** Dossier créé en JSON simple (sans FormData)

### Test 5 : Vérification dans la Console
- ✅ Ouvrir DevTools > Network
- ✅ Filtrer sur "create"
- ✅ Vérifier que le Content-Type est `multipart/form-data; boundary=...`
- ✅ Vérifier dans "Payload" que `dossier` est une string JSON

---

## 📝 Résumé des Changements

### Changement Principal
```typescript
// ❌ AVANT
formData.append('dossier', dossierData);

// ✅ APRÈS
formData.append('dossier', JSON.stringify(dossierData));
```

### Changement Secondaire
```typescript
// ❌ AVANT
headers: {
  'Content-Type': 'multipart/form-data', // ❌
  'Authorization': 'Bearer ' + token
}

// ✅ APRÈS
headers: {
  // Ne PAS mettre Content-Type, le navigateur le gère
  'Authorization': 'Bearer ' + token
}
```

---

## ✅ Résultat Attendu

Après ces corrections :
- ✅ La création de dossier avec fichiers fonctionne sans erreur 500
- ✅ Les fichiers sont correctement uploadés et sauvegardés
- ✅ Le backend reçoit le champ `dossier` comme JSON et peut le désérialiser
- ✅ Aucune erreur `Content-Type 'application/octet-stream' is not supported`

---

## 🔗 Références

- **Endpoint Backend :** `POST /api/dossiers/create?isChef={boolean}`
- **Content-Type attendu :** `multipart/form-data` (avec boundary automatique)
- **Format attendu :**
  - `dossier` : JSON string
  - `contratSigne` : File (optionnel)
  - `pouvoir` : File (optionnel)

---

## 📞 Support

Si le problème persiste après ces corrections :
1. Vérifier les logs backend pour voir exactement ce qui est reçu
2. Vérifier dans DevTools > Network le format exact de la requête
3. Vérifier que le backend accepte bien `multipart/form-data` (déjà configuré)

