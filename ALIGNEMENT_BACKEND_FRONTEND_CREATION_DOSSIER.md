# 📋 Alignement Backend/Frontend - Création de Dossier

## 🎯 Vue d'Ensemble

Ce document décrit l'alignement entre le backend et le frontend pour la création de dossiers, avec ou sans fichiers (contrat et pouvoir).

---

## 🔄 Logique de Routage Frontend

### Décision : Quel Endpoint Utiliser ?

Le frontend doit détecter si des fichiers sont nécessaires :

```typescript
// Détecter si des fichiers sont nécessaires
const hasFiles = (contratChecked && contratFile) || (pouvoirChecked && pouvoirFile);

if (hasFiles) {
    // ✅ Utiliser multipart/form-data
    // POST /api/dossiers/create?isChef={boolean}
} else {
    // ✅ Utiliser JSON simple
    // POST /api/dossiers/create?isChef={boolean}
}
```

---

## 📡 Endpoint 1 : Création avec Fichiers (multipart/form-data)

### URL
```
POST /api/dossiers/create?isChef={boolean}
```

### Content-Type
```
multipart/form-data
```

### Headers Requis
```
Authorization: Bearer {token}
```

### Body (FormData)

| Champ | Type | Requis | Description |
|-------|------|--------|-------------|
| `dossier` | **String (JSON)** | ✅ Oui | **IMPORTANT :** Doit être une chaîne JSON, pas un objet JavaScript |
| `contratSigne` | File | ❌ Non | Fichier PDF du contrat signé |
| `pouvoir` | File | ❌ Non | Fichier PDF du pouvoir |

### Format du Champ "dossier"

**⚠️ IMPORTANT :** Le backend accepte maintenant `dossier` comme **String JSON**. Le frontend DOIT envoyer une chaîne JSON, pas un objet JavaScript.

#### ❌ INCORRECT (Ne fonctionne pas)
```typescript
const formData = new FormData();
formData.append('dossier', dossierData); // ❌ Envoie comme objet JavaScript → application/octet-stream
```

#### ✅ CORRECT (Fonctionne)
```typescript
const formData = new FormData();
formData.append('dossier', JSON.stringify(dossierData)); // ✅ Envoie comme JSON string
```

**Note :** Le backend désérialise maintenant manuellement le JSON string avec `ObjectMapper`, ce qui permet d'accepter le format envoyé par le frontend.

### Exemple de Requête Frontend

```typescript
createDossierWithFiles(
    dossierData: any,
    contratChecked: boolean,
    pouvoirChecked: boolean,
    contratFile: File | null,
    pouvoirFile: File | null,
    isChef: boolean = false
): Observable<Dossier> {
    const token = this.authService.getToken();
    const formData = new FormData();
    
    // ✅ CORRECT : Envoyer dossier comme JSON string
    formData.append('dossier', JSON.stringify(dossierData));
    
    // Ajouter les fichiers si présents
    if (contratChecked && contratFile) {
        formData.append('contratSigne', contratFile);
    }
    if (pouvoirChecked && pouvoirFile) {
        formData.append('pouvoir', pouvoirFile);
    }
    
    // ✅ IMPORTANT : Ne PAS définir Content-Type manuellement
    // Le navigateur l'ajoutera automatiquement avec le boundary
    return this.http.post<Dossier>(
        `${this.apiUrl}/dossiers/create?isChef=${isChef}`,
        formData,
        {
            headers: {
                'Authorization': `Bearer ${token}`
                // Ne PAS mettre 'Content-Type' ici
            }
        }
    );
}
```

### Structure JSON du Champ "dossier"

```json
{
  "titre": "Dossier client Orange",
  "description": "Un contract signé mais pas de payement",
  "nomCreancier": "Orange",
  "typeCreancier": "PERSONNE_MORALE",
  "nomDebiteur": "Ooredoo",
  "typeDebiteur": "PERSONNE_MORALE",
  "montantCreance": 50000.0,
  "urgence": "MOYENNE",
  "typeDocumentJustificatif": "CONTRAT",
  "dossierStatus": "ENCOURSDETRAITEMENT"
}
```

### Réponse Succès (201 CREATED)

```json
{
  "id": 1,
  "numeroDossier": "DOS-2025-001",
  "titre": "Dossier client Orange",
  "description": "Un contract signé mais pas de payement",
  "montantCreance": 50000.0,
  "statut": "VALIDE",
  "contratSigneFilePath": "/uploads/contrat/abc123_contrat.pdf",
  "pouvoirFilePath": "/uploads/pouvoir/def456_pouvoir.pdf",
  "dateCreation": "2025-12-04T20:30:00",
  ...
}
```

### Réponses d'Erreur

#### 400 Bad Request - JSON Invalide
```json
{
  "error": "Format de données invalide",
  "message": "Le champ 'dossier' doit être un JSON valide. Erreur: ...",
  "code": "INVALID_JSON",
  "timestamp": "2025-12-04T20:30:00"
}
```

#### 401 Unauthorized - Token Manquant
```json
{
  "error": "Non autorisé",
  "message": "Token d'authentification requis pour créer un dossier",
  "code": "TOKEN_MISSING",
  "timestamp": "2025-12-04T20:30:00"
}
```

#### 500 Internal Server Error
```json
{
  "error": "Erreur interne du serveur",
  "message": "Erreur lors de la création du dossier: ...",
  "timestamp": "2025-12-04T20:30:00"
}
```

---

## 📡 Endpoint 2 : Création Simple (JSON)

### URL
```
POST /api/dossiers/create?isChef={boolean}
```

### Content-Type
```
application/json
```

### Headers Requis
```
Content-Type: application/json
Authorization: Bearer {token}
```

### Body (JSON)

Même structure que le champ "dossier" dans le FormData, mais envoyé directement comme JSON.

### Exemple de Requête Frontend

```typescript
createDossierSimple(
    dossierData: any,
    isChef: boolean = false
): Observable<Dossier> {
    const token = this.authService.getToken();
    
    return this.http.post<Dossier>(
        `${this.apiUrl}/dossiers/create?isChef=${isChef}`,
        dossierData, // ✅ Envoyer directement comme objet JSON
        {
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        }
    );
}
```

### Réponse

Identique à l'endpoint multipart (201 CREATED avec le dossier créé).

---

## 🔀 Logique Complète Frontend

### Exemple de Méthode Unifiée

```typescript
createDossier(
    dossierData: any,
    contratChecked: boolean,
    pouvoirChecked: boolean,
    contratFile: File | null,
    pouvoirFile: File | null,
    isChef: boolean = false
): Observable<Dossier> {
    const token = this.authService.getToken();
    
    // Détecter si des fichiers sont nécessaires
    const hasFiles = (contratChecked && contratFile) || (pouvoirChecked && pouvoirFile);
    
    if (hasFiles) {
        // ✅ Utiliser multipart/form-data
        const formData = new FormData();
        
        // ✅ CORRECT : Envoyer dossier comme JSON string
        formData.append('dossier', JSON.stringify(dossierData));
        
        // Ajouter les fichiers si présents
        if (contratChecked && contratFile) {
            formData.append('contratSigne', contratFile);
        }
        if (pouvoirChecked && pouvoirFile) {
            formData.append('pouvoir', pouvoirFile);
        }
        
        return this.http.post<Dossier>(
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
        // ✅ Utiliser JSON simple
        return this.http.post<Dossier>(
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

## ✅ Checklist de Vérification Frontend

### Pour Multipart (avec fichiers)

- [ ] Le champ `dossier` est envoyé avec `JSON.stringify(dossierData)`
- [ ] Le `Content-Type` n'est **PAS** défini manuellement dans les headers
- [ ] Les fichiers sont ajoutés uniquement s'ils existent (`contratFile` et `pouvoirFile`)
- [ ] Le token JWT est inclus dans le header `Authorization`
- [ ] Le paramètre `isChef` est passé dans l'URL (`?isChef=true` ou `?isChef=false`)

### Pour JSON Simple (sans fichiers)

- [ ] Le `Content-Type: application/json` est défini dans les headers
- [ ] Le body est envoyé directement comme objet JSON (pas de `JSON.stringify()`)
- [ ] Le token JWT est inclus dans le header `Authorization`
- [ ] Le paramètre `isChef` est passé dans l'URL

### Gestion des Erreurs

- [ ] Les erreurs 400 sont gérées (JSON invalide, validation)
- [ ] Les erreurs 401 sont gérées (token manquant/expiré)
- [ ] Les erreurs 500 sont gérées (erreur serveur)
- [ ] Les messages d'erreur sont affichés à l'utilisateur

---

## 🧪 Tests Recommandés

### Test 1 : Création avec Contrat uniquement
- ✅ Cocher "Contrat Signé"
- ✅ Sélectionner un fichier PDF
- ✅ Ne pas cocher "Pouvoir"
- ✅ Créer le dossier
- **Résultat attendu :** Dossier créé avec le fichier contrat uploadé

### Test 2 : Création avec Pouvoir uniquement
- ✅ Cocher "Pouvoir"
- ✅ Sélectionner un fichier PDF
- ✅ Ne pas cocher "Contrat Signé"
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

### Test 5 : Vérification dans DevTools
- ✅ Ouvrir DevTools > Network
- ✅ Filtrer sur "create"
- ✅ Vérifier le Content-Type :
  - Avec fichiers : `multipart/form-data; boundary=...`
  - Sans fichiers : `application/json`
- ✅ Vérifier dans "Payload" que `dossier` est une string JSON (avec fichiers)

---

## 📊 Comparaison des Endpoints

| Aspect | Multipart (avec fichiers) | JSON Simple (sans fichiers) |
|--------|---------------------------|-----------------------------|
| **URL** | `/api/dossiers/create?isChef={boolean}` | `/api/dossiers/create?isChef={boolean}` |
| **Method** | POST | POST |
| **Content-Type** | `multipart/form-data` (automatique) | `application/json` |
| **Champ dossier** | String JSON dans FormData | Objet JSON direct |
| **Fichiers** | `contratSigne` et `pouvoir` (optionnels) | Aucun |
| **Headers** | `Authorization` uniquement | `Content-Type` + `Authorization` |
| **Quand utiliser** | Si contrat OU pouvoir coché | Si aucun fichier |

---

## 🔍 Détails Techniques Backend

### Endpoint Multipart

**Fichier :** `DossierController.java`  
**Méthode :** `createDossierWithFiles()`  
**Lignes :** 244-344

**Paramètres :**
- `@RequestPart("dossier") String dossierJson` - JSON string (désérialisé avec ObjectMapper)
- `@RequestPart(value = "contratSigne", required = false) MultipartFile contratSigne`
- `@RequestPart(value = "pouvoir", required = false) MultipartFile pouvoir`
- `@RequestParam(value = "isChef", required = false) boolean isChef`
- `@RequestHeader(name = "Authorization", required = false) String authHeader`

**Traitement :**
1. Extraction de l'utilisateur depuis le token
2. Désérialisation du JSON `dossier` avec `ObjectMapper`
3. Validation des fichiers PDF
4. Ajout des fichiers au `DossierRequest`
5. Création du dossier via `dossierService.createDossier()`
6. Validation automatique si chef

### Endpoint JSON Simple

**Fichier :** `DossierController.java`  
**Méthode :** `createDossierSimple()`  
**Lignes :** 362-437

**Paramètres :**
- `@RequestBody DossierRequest request` - Objet JSON direct
- `@RequestParam(value = "isChef", required = false) boolean isChef`
- `@RequestHeader(name = "Authorization", required = false) String authHeader`

**Traitement :**
1. Extraction de l'utilisateur depuis le token
2. Validation des données
3. Création du dossier via `dossierService.createDossier()`
4. Validation automatique si chef

---

## ⚠️ Points d'Attention

### 1. Format du Champ "dossier" (Multipart)

**CRITIQUE :** Le champ `dossier` doit être une **chaîne JSON**, pas un objet JavaScript.

```typescript
// ❌ INCORRECT
formData.append('dossier', dossierData);

// ✅ CORRECT
formData.append('dossier', JSON.stringify(dossierData));
```

### 2. Content-Type pour FormData

**IMPORTANT :** Ne pas définir `Content-Type` manuellement pour FormData. Le navigateur l'ajoute automatiquement avec le boundary.

```typescript
// ❌ INCORRECT
headers: {
  'Content-Type': 'multipart/form-data', // ❌
  'Authorization': 'Bearer ' + token
}

// ✅ CORRECT
headers: {
  // Ne PAS mettre Content-Type
  'Authorization': 'Bearer ' + token
}
```

### 3. Validation des Fichiers

Le backend valide que les fichiers sont des PDF :
- Taille maximale : 20MB
- Type MIME : `application/pdf`
- Extension : `.pdf`

### 4. Paramètre isChef

Le paramètre `isChef` détermine le statut initial du dossier :
- `isChef=true` → Statut `VALIDE` (validation automatique)
- `isChef=false` → Statut `EN_ATTENTE_VALIDATION`

---

## 📝 Résumé des Changements Backend

### Modification Appliquée

**Fichier :** `DossierController.java`  
**Méthode :** `createDossierWithFiles()`

**Changement :**
- **AVANT :** `@RequestPart("dossier") DossierRequest request` (désérialisation automatique par Spring)
- **APRÈS :** `@RequestPart("dossier") String dossierJson` (désérialisation manuelle avec ObjectMapper)

**Code ajouté :**
```java
// Désérialiser le JSON du dossier
DossierRequest request;
try {
    ObjectMapper objectMapper = new ObjectMapper();
    request = objectMapper.readValue(dossierJson, DossierRequest.class);
    logger.info("/api/dossiers/create (multipart) - Dossier JSON désérialisé avec succès");
} catch (Exception e) {
    logger.error("/api/dossiers/create (multipart) - Erreur de désérialisation JSON: {}", e.getMessage(), e);
    return ResponseEntity.badRequest().body(Map.of(
        "error", "Format de données invalide",
        "message", "Le champ 'dossier' doit être un JSON valide. Erreur: " + e.getMessage(),
        "code", "INVALID_JSON",
        "timestamp", new Date().toString()
    ));
}
```

**Avantages :**
- ✅ Accepte le format envoyé par le frontend (String JSON dans FormData)
- ✅ Gestion d'erreur explicite pour JSON invalide
- ✅ Compatible avec le frontend qui envoie `JSON.stringify(dossierData)`
- ✅ Cohérent avec `HuissierDocumentController` et `HuissierActionController`

---

## 🔗 Références

- **Endpoint Multipart :** `POST /api/dossiers/create?isChef={boolean}` (Content-Type: multipart/form-data)
- **Endpoint JSON :** `POST /api/dossiers/create?isChef={boolean}` (Content-Type: application/json)
- **Taille max fichier :** 20MB
- **Types de fichiers acceptés :** PDF uniquement

---

## ✅ Checklist Finale

### Backend
- [x] Endpoint multipart accepte `dossier` comme String
- [x] Désérialisation manuelle avec ObjectMapper
- [x] Gestion d'erreur pour JSON invalide
- [x] Validation des fichiers PDF
- [x] Endpoint JSON simple fonctionne toujours

### Frontend (À Vérifier)
- [ ] Détection correcte si fichiers nécessaires
- [ ] Champ `dossier` envoyé comme JSON string (multipart)
- [ ] Champ `dossier` envoyé comme objet JSON (simple)
- [ ] Content-Type non défini pour FormData
- [ ] Content-Type défini pour JSON simple
- [ ] Gestion des erreurs 400, 401, 500
- [ ] Affichage des messages d'erreur

---

## 🎯 Résultat Attendu

Après ces corrections :
- ✅ La création de dossier avec fichiers fonctionne sans erreur 500
- ✅ Les fichiers sont correctement uploadés et sauvegardés
- ✅ La création de dossier sans fichiers fonctionne toujours
- ✅ Le backend accepte le format envoyé par le frontend
- ✅ Les messages d'erreur sont explicites et utiles

