# 🎯 Prompt Frontend : Correction Champs JSON Dossier

## 📋 Contexte

Le backend retourne l'erreur `Unrecognized field "contratSigne"` et `Unrecognized field "pouvoir"` lors de la création d'un dossier avec fichiers.

**Problème identifié :** Le frontend envoie `contratSigne: "uploaded"` et `pouvoir: "uploaded"` dans le JSON du dossier, mais ces champs n'existent pas dans le DTO backend `DossierRequest`.

**Solution :** Retirer ces champs du JSON avant l'envoi. Les fichiers sont déjà envoyés séparément dans le FormData, ces indicateurs ne sont pas nécessaires.

---

## 🔧 Modifications Requises

### 1. Localiser le Code de Création de Dossier

Trouver le fichier qui construit le JSON du dossier avant l'envoi :
- Probablement : `dossier-gestion.component.ts` ou `dossier-api.service.ts`
- Chercher : `JSON.stringify(dossierData)` ou `formData.append('dossier', ...)`

### 2. Identifier où les Champs sont Ajoutés

Chercher dans le code où `contratSigne: "uploaded"` et `pouvoir: "uploaded"` sont ajoutés au JSON.

**Indicateurs à chercher :**
- `contratSigne: "uploaded"`
- `pouvoir: "uploaded"`
- `dossierData.contratSigne = "uploaded"`
- `dossierData.pouvoir = "uploaded"`

---

## ✅ Corrections à Appliquer

### Correction 1 : Retirer les Champs du JSON

#### ❌ AVANT (Incorrect)
```typescript
// Construction du JSON du dossier
const dossierData = {
  titre: "Dossier Client Orange",
  description: "Facture impéyée",
  nomCreancier: "Orange",
  nomDebiteur: "Ooredoo",
  montantCreance: 80000,
  // ... autres champs
  
  // ❌ Ces champs ne doivent PAS être dans le JSON
  contratSigne: "uploaded",  // ❌ À RETIRER
  pouvoir: "uploaded"        // ❌ À RETIRER
};

formData.append('dossier', JSON.stringify(dossierData));
```

#### ✅ APRÈS (Correct)
```typescript
// Construction du JSON du dossier
const dossierData = {
  titre: "Dossier Client Orange",
  description: "Facture impéyée",
  nomCreancier: "Orange",
  nomDebiteur: "Ooredoo",
  montantCreance: 80000,
  // ... autres champs
  
  // ✅ Ces champs sont retirés du JSON
  // Les fichiers sont envoyés séparément dans FormData
};

formData.append('dossier', JSON.stringify(dossierData));
```

### Correction 2 : Vérifier la Logique Conditionnelle

S'assurer que les champs ne sont ajoutés nulle part dans le code :

#### ❌ INCORRECT
```typescript
if (contratChecked) {
  dossierData.contratSigne = "uploaded"; // ❌ À RETIRER
}
if (pouvoirChecked) {
  dossierData.pouvoir = "uploaded"; // ❌ À RETIRER
}
```

#### ✅ CORRECT
```typescript
// Ne PAS ajouter ces champs au JSON
// Les fichiers sont gérés séparément dans FormData
if (contratChecked && contratFile) {
  formData.append('contratSigne', contratFile); // ✅ Fichier dans FormData
}
if (pouvoirChecked && pouvoirFile) {
  formData.append('pouvoir', pouvoirFile); // ✅ Fichier dans FormData
}
```

---

## 📝 Exemple Complet de Méthode Corrigée

### Méthode TypeScript/Angular Complète

```typescript
createDossier(
    formData: FormGroup,
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
        const formDataToSend = new FormData();
        
        // ✅ Construire le JSON du dossier SANS les champs contratSigne et pouvoir
        const dossierData = {
            titre: formData.get('titre')?.value,
            description: formData.get('description')?.value,
            numeroDossier: formData.get('numeroDossier')?.value,
            montantCreance: formData.get('montantCreance')?.value,
            typeDocumentJustificatif: formData.get('typeDocumentJustificatif')?.value,
            urgence: formData.get('urgence')?.value,
            dossierStatus: formData.get('dossierStatus')?.value,
            typeCreancier: formData.get('typeCreancier')?.value,
            nomCreancier: formData.get('nomCreancier')?.value,
            prenomCreancier: formData.get('prenomCreancier')?.value || "",
            typeDebiteur: formData.get('typeDebiteur')?.value,
            nomDebiteur: formData.get('nomDebiteur')?.value,
            prenomDebiteur: formData.get('prenomDebiteur')?.value || "",
            // ✅ NE PAS inclure contratSigne et pouvoir ici
        };
        
        // ✅ Envoyer dossier comme JSON string
        formDataToSend.append('dossier', JSON.stringify(dossierData));
        
        // Ajouter les fichiers si présents
        if (contratChecked && contratFile) {
            formDataToSend.append('contratSigne', contratFile);
        }
        if (pouvoirChecked && pouvoirFile) {
            formDataToSend.append('pouvoir', pouvoirFile);
        }
        
        return this.http.post<Dossier>(
            `${this.apiUrl}/dossiers/create?isChef=${isChef}`,
            formDataToSend,
            {
                headers: {
                    'Authorization': `Bearer ${token}`
                    // Ne PAS mettre 'Content-Type' ici
                }
            }
        );
    } else {
        // ✅ Utiliser JSON simple (sans fichiers)
        const dossierData = {
            titre: formData.get('titre')?.value,
            description: formData.get('description')?.value,
            // ... autres champs
            // ✅ Pas de contratSigne ni pouvoir ici non plus
        };
        
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

## 🔍 Points de Vérification

### 1. Vérifier le JSON Avant Envoi

Dans la console du navigateur, vérifier que le JSON ne contient PAS :
- ❌ `"contratSigne": "uploaded"`
- ❌ `"pouvoir": "uploaded"`

**Test dans la console :**
```typescript
const dossierData = { /* ... */ };
console.log('Dossier JSON:', JSON.stringify(dossierData));
// Vérifier qu'il n'y a pas de contratSigne ni pouvoir
```

### 2. Vérifier les Fichiers dans FormData

Les fichiers doivent être dans le FormData, pas dans le JSON :
```typescript
// ✅ CORRECT
formData.append('dossier', JSON.stringify(dossierData)); // JSON sans fichiers
formData.append('contratSigne', contratFile); // Fichier séparé
formData.append('pouvoir', pouvoirFile); // Fichier séparé
```

### 3. Vérifier la Logique Conditionnelle

S'assurer qu'aucune condition n'ajoute ces champs :
```typescript
// ❌ À RETIRER si présent
if (contratChecked) {
  dossierData.contratSigne = "uploaded"; // ❌
}
if (pouvoirChecked) {
  dossierData.pouvoir = "uploaded"; // ❌
}
```

---

## 📊 Structure JSON Attendue par le Backend

### Champs Acceptés dans DossierRequest

Le backend accepte ces champs dans le JSON :

```json
{
  "titre": "string",
  "description": "string",
  "numeroDossier": "string",
  "montantCreance": "number",
  "typeDocumentJustificatif": "FACTURE | CONTRAT | ...",
  "urgence": "FAIBLE | MOYENNE | ELEVEE",
  "dossierStatus": "ENCOURSDETRAITEMENT | CLOTURE | ...",
  "typeCreancier": "PERSONNE_PHYSIQUE | PERSONNE_MORALE",
  "nomCreancier": "string",
  "prenomCreancier": "string (optionnel)",
  "codeCreancier": "string (optionnel)",
  "codeCreanceCreancier": "string (optionnel)",
  "typeDebiteur": "PERSONNE_PHYSIQUE | PERSONNE_MORALE",
  "nomDebiteur": "string",
  "prenomDebiteur": "string (optionnel)",
  "codeCreanceDebiteur": "string (optionnel)",
  "agentCreateurId": "number (optionnel, sera défini automatiquement)",
  "statut": "EN_ATTENTE_VALIDATION | VALIDE | REJETE (optionnel)"
}
```

### Champs NON Acceptés (à retirer)

- ❌ `contratSigne` - Les fichiers sont envoyés séparément
- ❌ `pouvoir` - Les fichiers sont envoyés séparément

---

## 🐛 Dépannage

### Problème : L'erreur persiste après correction

**Vérifications :**
1. ✅ Les champs `contratSigne` et `pouvoir` sont bien retirés du JSON ?
2. ✅ Le JSON est bien stringifié avec `JSON.stringify()` ?
3. ✅ Les fichiers sont bien ajoutés séparément dans FormData ?
4. ✅ Vérifier dans DevTools > Network > Payload que le JSON ne contient pas ces champs ?

### Problème : Comment savoir quels champs retirer ?

**Solution :** Vérifier l'erreur backend qui liste les champs acceptés :
```
22 known properties: "nomDebiteur", "montantCreance", ..., "contratSigneFile", "pouvoirFile", ...
```

Les champs acceptés sont listés. Si un champ n'est pas dans cette liste, il doit être retiré.

---

## 📋 Checklist de Vérification

Avant de tester, vérifier que :

- [ ] Les champs `contratSigne` et `pouvoir` sont retirés du JSON
- [ ] Aucune condition n'ajoute ces champs au JSON
- [ ] Les fichiers sont bien ajoutés séparément dans FormData
- [ ] Le JSON est bien stringifié avec `JSON.stringify()`
- [ ] Le Content-Type n'est PAS défini manuellement pour FormData
- [ ] Le token JWT est bien inclus dans le header `Authorization`

---

## 🧪 Tests à Effectuer

### Test 1 : Création avec Contrat uniquement
- ✅ Cocher "Contrat Signé"
- ✅ Sélectionner un fichier PDF
- ✅ Ne pas cocher "Pouvoir"
- ✅ Créer le dossier
- **Vérifier dans console :** Le JSON ne contient pas `contratSigne: "uploaded"`
- **Résultat attendu :** Dossier créé avec le fichier contrat uploadé

### Test 2 : Création avec Pouvoir uniquement
- ✅ Cocher "Pouvoir"
- ✅ Sélectionner un fichier PDF
- ✅ Ne pas cocher "Contrat Signé"
- ✅ Créer le dossier
- **Vérifier dans console :** Le JSON ne contient pas `pouvoir: "uploaded"`
- **Résultat attendu :** Dossier créé avec le fichier pouvoir uploadé

### Test 3 : Création avec Contrat ET Pouvoir
- ✅ Cocher "Contrat Signé" ET "Pouvoir"
- ✅ Sélectionner deux fichiers PDF
- ✅ Créer le dossier
- **Vérifier dans console :** Le JSON ne contient ni `contratSigne` ni `pouvoir`
- **Résultat attendu :** Dossier créé avec les deux fichiers uploadés

### Test 4 : Vérification dans DevTools
- ✅ Ouvrir DevTools > Network
- ✅ Filtrer sur "create"
- ✅ Cliquer sur la requête POST
- ✅ Aller dans l'onglet "Payload"
- ✅ Vérifier que le champ "dossier" (text) ne contient pas `"contratSigne"` ni `"pouvoir"`
- ✅ Vérifier que les fichiers sont bien dans "contratSigne" (file) et "pouvoir" (file)

---

## 📝 Résumé des Changements

### Changement Principal
```typescript
// ❌ AVANT
const dossierData = {
  // ...
  contratSigne: "uploaded",  // ❌
  pouvoir: "uploaded"         // ❌
};

// ✅ APRÈS
const dossierData = {
  // ...
  // Pas de contratSigne ni pouvoir
  // Les fichiers sont envoyés séparément dans FormData
};
```

### Points Clés

1. ✅ **Retirer `contratSigne` et `pouvoir` du JSON**
2. ✅ **Les fichiers sont déjà dans FormData** (pas besoin d'indicateurs)
3. ✅ **Le backend n'a pas besoin de ces champs** dans le JSON
4. ✅ **Vérifier dans la console** que le JSON est correct avant envoi

---

## ✅ Résultat Attendu

Après ces corrections :
- ✅ La création de dossier avec fichiers fonctionne sans erreur 400
- ✅ Le backend accepte le JSON sans erreur `Unrecognized field`
- ✅ Les fichiers sont correctement uploadés et sauvegardés
- ✅ Aucune erreur de désérialisation JSON

---

## 🔗 Références

- **Endpoint :** `POST /api/dossiers/create?isChef={boolean}`
- **Content-Type :** `multipart/form-data` (avec fichiers) ou `application/json` (sans fichiers)
- **Champs acceptés :** Voir la liste dans l'erreur backend (22 propriétés connues)
- **Champs à retirer :** `contratSigne`, `pouvoir`

---

## 📞 Support

Si le problème persiste après ces corrections :
1. Vérifier dans DevTools > Network > Payload le contenu exact du JSON
2. Vérifier que les fichiers sont bien dans FormData (pas dans le JSON)
3. Vérifier les logs backend pour voir exactement ce qui est reçu
4. Comparer avec la liste des 22 propriétés connues du DTO

