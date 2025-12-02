# 🔧 Solution : Erreur Multipart/Form-Data lors de la Création de Dossier

## 🚨 Problème Identifié

**Erreur** : `Content-Type 'multipart/form-data' is not supported`

**Cause** : Le frontend envoie une requête `multipart/form-data` à l'endpoint `/api/dossiers/create`, mais cet endpoint n'acceptait que `application/json`.

---

## ✅ Solution Appliquée

Un **nouvel endpoint** a été ajouté pour accepter les requêtes `multipart/form-data` avec les fichiers (contrat et pouvoir).

### **Avant** :
- ❌ `/api/dossiers/create` → Acceptait uniquement `application/json`
- ✅ `/api/dossiers/create/{id}` → Acceptait `multipart/form-data` mais nécessitait l'ID dans l'URL

### **Après** :
- ✅ `/api/dossiers/create` → Accepte **les deux** :
  - `application/json` (sans fichiers)
  - `multipart/form-data` (avec fichiers contrat et pouvoir)

---

## 📋 Détails Techniques

### **Nouvel Endpoint Multipart**

```java
@PostMapping(path = "/create", consumes = {"multipart/form-data"})
public ResponseEntity<?> createDossierWithFiles(
    @RequestPart("dossier") DossierRequest request,
    @RequestPart(value = "contratSigne", required = false) MultipartFile contratSigne,
    @RequestPart(value = "pouvoir", required = false) MultipartFile pouvoir,
    @RequestParam(value = "isChef", required = false, defaultValue = "false") boolean isChef,
    @RequestHeader(name = "Authorization", required = false) String authHeader)
```

### **Fonctionnalités** :

1. **Extraction automatique de l'utilisateur** depuis le token JWT
2. **Validation des fichiers PDF** (contrat et pouvoir)
3. **Gestion du statut** selon le rôle (chef ou agent)
4. **Validation automatique** si l'utilisateur est chef
5. **Même logique métier** que l'endpoint JSON

---

## 🔄 Format de Requête Attendue

### **Frontend doit envoyer** :

```typescript
const formData = new FormData();

// 1. Ajouter le JSON du dossier (obligatoire)
formData.append('dossier', JSON.stringify({
  titre: "Dossier test",
  nomCreancier: "Orange",
  nomDebiteur: "Ooredoo",
  typeDocumentJustificatif: "FACTURE",
  urgence: "Faible",
  description: "rien",
  // ... autres champs
}));

// 2. Ajouter les fichiers (optionnels)
if (contratFile) {
  formData.append('contratSigne', contratFile);
}
if (pouvoirFile) {
  formData.append('pouvoir', pouvoirFile);
}

// 3. Envoyer la requête
this.http.post(
  `${this.apiUrl}/dossiers/create?isChef=true`,
  formData,
  {
    headers: {
      'Authorization': `Bearer ${token}`
      // NE PAS mettre 'Content-Type' - le navigateur le fait automatiquement
    }
  }
)
```

---

## ⚠️ Points Importants pour le Frontend

### **1. Format du champ "dossier"**

Le champ `dossier` doit être une **chaîne JSON**, pas un objet JavaScript :

```typescript
// ✅ CORRECT
formData.append('dossier', JSON.stringify(dossierData));

// ❌ INCORRECT
formData.append('dossier', dossierData);
```

### **2. Headers HTTP**

**Ne pas** définir manuellement le header `Content-Type` pour les FormData :

```typescript
// ✅ CORRECT - Le navigateur ajoute automatiquement le bon Content-Type
const headers = {
  'Authorization': `Bearer ${token}`
};

// ❌ INCORRECT - Ne pas définir Content-Type manuellement
const headers = {
  'Authorization': `Bearer ${token}`,
  'Content-Type': 'multipart/form-data' // ❌ À NE PAS FAIRE
};
```

### **3. Noms des champs**

Les noms des champs dans FormData doivent correspondre exactement :

- `dossier` → JSON string du dossier
- `contratSigne` → Fichier PDF du contrat
- `pouvoir` → Fichier PDF du pouvoir

---

## 🧪 Test de l'Endpoint

### **Avec cURL** :

```bash
curl -X POST "http://localhost:8089/carthage-creance/api/dossiers/create?isChef=true" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "dossier={\"titre\":\"Test\",\"nomCreancier\":\"Orange\",\"nomDebiteur\":\"Ooredoo\"}" \
  -F "contratSigne=@/path/to/contrat.pdf" \
  -F "pouvoir=@/path/to/pouvoir.pdf"
```

### **Réponse attendue** :

```json
{
  "id": 123,
  "titre": "Test",
  "statut": "VALIDE",
  "contratSigneUrl": "/uploads/contrat_xxx.pdf",
  "pouvoirUrl": "/uploads/pouvoir_xxx.pdf",
  ...
}
```

---

## 🔍 Vérifications

### **1. Vérifier que l'endpoint existe** :

```bash
# Vérifier les endpoints disponibles
GET http://localhost:8089/carthage-creance/api/dossiers/create
```

### **2. Vérifier les logs backend** :

Après l'envoi de la requête, vérifier les logs pour :
- ✅ `agentCreateurId défini automatiquement à partir de l'utilisateur connecté`
- ✅ `Dossier créé avec succès`
- ❌ Aucune erreur `Content-Type not supported`

### **3. Vérifier le frontend** :

Dans la console du navigateur, vérifier :
- ✅ La requête est envoyée avec `Content-Type: multipart/form-data`
- ✅ Le token JWT est inclus dans les headers
- ✅ Les fichiers sont bien attachés au FormData

---

## 🐛 Dépannage

### **Erreur : "Content-Type not supported"**

**Cause** : Le frontend envoie toujours à l'ancien endpoint JSON.

**Solution** : Vérifier que le frontend utilise bien `FormData` et non `JSON.stringify()` directement.

---

### **Erreur : "Token manquant"**

**Cause** : Le header `Authorization` n'est pas envoyé.

**Solution** : S'assurer que le service Angular ajoute le token dans les headers pour les requêtes FormData.

---

### **Erreur : "dossier field is missing"**

**Cause** : Le champ `dossier` n'est pas présent dans le FormData ou n'est pas au bon format.

**Solution** : Vérifier que `formData.append('dossier', JSON.stringify(...))` est bien appelé.

---

### **Erreur : "File validation failed"**

**Cause** : Les fichiers ne sont pas des PDF valides.

**Solution** : Vérifier que les fichiers uploadés sont bien des PDF et non corrompus.

---

## 📝 Checklist de Vérification

- [ ] L'endpoint `/api/dossiers/create` accepte maintenant `multipart/form-data`
- [ ] Le frontend envoie le champ `dossier` comme JSON string
- [ ] Le frontend envoie les fichiers avec les noms `contratSigne` et `pouvoir`
- [ ] Le token JWT est inclus dans les headers
- [ ] Le Content-Type n'est pas défini manuellement dans le frontend
- [ ] Les fichiers sont bien des PDF valides
- [ ] L'application backend a été redémarrée après les modifications

---

## 🎯 Résultat Attendu

Après cette correction, vous devriez pouvoir :

1. ✅ Créer un dossier avec le formulaire
2. ✅ Uploader le fichier contrat (PDF)
3. ✅ Uploader le fichier pouvoir (PDF)
4. ✅ Voir le dossier créé avec les fichiers attachés
5. ✅ Aucune erreur `Content-Type not supported`

---

## 📚 Fichiers Modifiés

- `src/main/java/projet/carthagecreance_backend/Controller/DossierController.java`
  - Ajout de l'endpoint `createDossierWithFiles()` qui accepte `multipart/form-data`

---

**La logique métier reste inchangée** - Seul le format d'entrée a été adapté pour accepter les fichiers uploadés.


