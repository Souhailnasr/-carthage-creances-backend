# 🧪 Guide : Tester l'IA sur un Dossier Existant avec Postman

## 🎯 Objectif

Tester la prédiction IA sur un dossier qui existe déjà dans la base de données et qui contient toutes les informations nécessaires (enquête, actions, audiences, etc.).

---

## 📋 Prérequis

1. ✅ **Backend démarré** sur `http://localhost:8089`
2. ✅ **Token d'authentification** valide
3. ✅ **Dossier existant** avec ID connu (ex: `38`)
4. ✅ **Fichiers de modèles IA** dans `src/main/resources/ia/` :
   - `model_classification.pkl`
   - `model_regression.pkl`
   - `feature_columns.pkl`
5. ✅ **Python installé** et dans le PATH

---

## 🔐 Étape 1 : Authentification (si nécessaire)

### **Requête : Login**

**Méthode** : `POST`  
**URL** : `http://localhost:8089/carthage-creance/auth/authenticate`

**Headers** :
```
Content-Type: application/json
```

**Body** (raw JSON) :
```json
{
  "email": "votre_email@example.com",
  "password": "votre_mot_de_passe"
}
```

**Réponse** :
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "votre_email@example.com"
}
```

**⚠️ IMPORTANT** : Copiez le `token` pour les requêtes suivantes.

---

## 📁 Étape 2 : Vérifier le Dossier Existant

### **Requête : Récupérer le Dossier**

**Méthode** : `GET`  
**URL** : `http://localhost:8089/carthage-creance/api/dossiers/{ID_DOSSIER}`

**⚠️ Remplacez `{ID_DOSSIER}` par l'ID de votre dossier (ex: `38`)**

**Headers** :
```
Authorization: Bearer VOTRE_TOKEN_ICI
```

**Réponse attendue** :
```json
{
  "id": 38,
  "titre": "Mon Dossier Existant",
  "montantCreance": 50000.0,
  "montantRecouvre": 10000.0,
  "montantRestant": 40000.0,
  "typeRecouvrement": "AMIABLE",
  // ... autres champs
}
```

**✅ Vérifications** :
- Le dossier existe bien
- Il a des données (montant créance, etc.)
- Notez l'ID du dossier pour l'étape suivante

---

## 🤖 Étape 3 : Déclencher la Prédiction IA

### **Requête : Enregistrer Action Amiable (Déclenche la Prédiction IA)**

**Méthode** : `POST`  
**URL** : `http://localhost:8089/carthage-creance/api/dossiers/{ID_DOSSIER}/amiable`

**⚠️ Remplacez `{ID_DOSSIER}` par l'ID de votre dossier (ex: `38`)**

**Headers** :
```
Content-Type: application/json
Authorization: Bearer VOTRE_TOKEN_ICI
```

**Body** (raw JSON) :

**Option 1 : Sans modifier le montant recouvré** (recommandé pour juste tester l'IA)
```json
{
  "montantRecouvre": 0.0
}
```

**Option 2 : Ajouter un montant recouvré**
```json
{
  "montantRecouvre": 5000.0
}
```

**⚠️ Note** : Même avec `montantRecouvre: 0.0`, la prédiction IA sera déclenchée et calculée à partir de toutes les données existantes du dossier.

---

## ✅ Réponse Attendue (avec Prédiction IA)

```json
{
  "id": 38,
  "titre": "Mon Dossier Existant",
  "montantCreance": 50000.0,
  "montantRecouvre": 10000.0,  // Ou 15000.0 si vous avez ajouté 5000.0
  "montantRestant": 40000.0,
  "etatPrediction": "RECOVERED_PARTIAL",  // ✅ PRÉDICTION IA
  "riskScore": 45.2,                       // ✅ PRÉDICTION IA
  "riskLevel": "Moyen",                    // ✅ PRÉDICTION IA
  "typeRecouvrement": "AMIABLE",
  "numeroDossier": "DOSSIER-001",
  // ... autres champs
}
```

**✅ Vérifications** :
- ✅ `etatPrediction` est présent : `RECOVERED_TOTAL`, `RECOVERED_PARTIAL`, ou `NOT_RECOVERED`
- ✅ `riskScore` est un nombre entre 0 et 100
- ✅ `riskLevel` est : `"Faible"`, `"Moyen"`, ou `"Élevé"`

---

## 📊 Étape 4 : Vérifier les Résultats Sauvegardés

### **Requête : Récupérer le Dossier (après prédiction)**

**Méthode** : `GET`  
**URL** : `http://localhost:8089/carthage-creance/api/dossiers/{ID_DOSSIER}`

**Headers** :
```
Authorization: Bearer VOTRE_TOKEN_ICI
```

**Réponse attendue** :
```json
{
  "id": 38,
  "etatPrediction": "RECOVERED_PARTIAL",
  "riskScore": 45.2,
  "riskLevel": "Moyen",
  // ... autres champs
}
```

**✅ Confirmation** : Les champs de prédiction IA sont maintenant sauvegardés dans la base de données.

---

## 🔍 Vérification des Logs Backend

Dans la console Spring Boot, vous devriez voir :

```
INFO - IaPredictionServiceImpl - Exécution de la prédiction IA pour le dossier...
INFO - IaPredictionServiceImpl - Prédiction IA réussie: etatFinal=RECOVERED_PARTIAL, riskScore=45.2, riskLevel=Moyen
INFO - DossierController - Prédiction IA appliquée au dossier 38: etatPrediction=RECOVERED_PARTIAL, riskScore=45.2, riskLevel=Moyen
```

---

## 🎯 Exemple Complet avec Postman

### **1. Configuration de l'Environnement Postman**

Créez des variables dans Postman :
- `base_url` : `http://localhost:8089/carthage-creance`
- `token` : (sera rempli après login)
- `dossier_id` : `38` (remplacez par votre ID)

### **2. Requête Complète**

**Méthode** : `POST`  
**URL** : `{{base_url}}/api/dossiers/{{dossier_id}}/amiable`

**Headers** :
```
Content-Type: application/json
Authorization: Bearer {{token}}
```

**Body** :
```json
{
  "montantRecouvre": 0.0
}
```

### **3. Tests Automatiques dans Postman**

Ajoutez ces tests dans l'onglet "Tests" de Postman :

```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Prédiction IA présente", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('etatPrediction');
    pm.expect(jsonData).to.have.property('riskScore');
    pm.expect(jsonData).to.have.property('riskLevel');
});

pm.test("Valeurs de prédiction valides", function () {
    var jsonData = pm.response.json();
    
    // Vérifier etatPrediction
    var etatsValides = ["RECOVERED_TOTAL", "RECOVERED_PARTIAL", "NOT_RECOVERED"];
    pm.expect(etatsValides).to.include(jsonData.etatPrediction);
    
    // Vérifier riskScore
    pm.expect(jsonData.riskScore).to.be.a('number');
    pm.expect(jsonData.riskScore).to.be.at.least(0);
    pm.expect(jsonData.riskScore).to.be.at.most(100);
    
    // Vérifier riskLevel
    var niveauxValides = ["Faible", "Moyen", "Élevé"];
    pm.expect(niveauxValides).to.include(jsonData.riskLevel);
    
    console.log("✅ Prédiction IA réussie:");
    console.log("   - État prédit: " + jsonData.etatPrediction);
    console.log("   - Score de risque: " + jsonData.riskScore);
    console.log("   - Niveau de risque: " + jsonData.riskLevel);
});
```

---

## 🐛 Dépannage

### **Erreur : "Dossier non trouvé" (404)**

**Solution** :
- Vérifiez que l'ID du dossier est correct
- Vérifiez que le dossier existe dans la base de données

### **Erreur : "montantRecouvre est requis" (400)**

**Solution** :
- Assurez-vous que le body contient `{"montantRecouvre": 0.0}` (ou un autre montant)

### **Prédiction toujours en Fallback (NOT_RECOVERED, riskScore=100.0)**

**Causes possibles** :
1. Python non trouvé
2. Fichiers de modèles manquants
3. Erreur dans le script Python

**Solutions** :
- Vérifiez les logs backend pour l'erreur exacte
- Vérifiez que Python est installé : `python --version`
- Vérifiez que les 3 fichiers `.pkl` sont dans `src/main/resources/ia/`
- Testez le script Python directement :
  ```bash
  python src/main/resources/ia/predict.py test.json
  ```

### **Erreur : "Unauthorized" (401)**

**Solution** :
- Vérifiez que le token est valide
- Reconnectez-vous pour obtenir un nouveau token

---

## 📝 Résumé Rapide

| Étape | Action | URL |
|-------|--------|-----|
| 1 | Login | `POST /auth/authenticate` |
| 2 | Vérifier dossier | `GET /api/dossiers/{id}` |
| 3 | **Tester IA** | `POST /api/dossiers/{id}/amiable` |
| 4 | Vérifier résultats | `GET /api/dossiers/{id}` |

**Body pour l'étape 3** :
```json
{
  "montantRecouvre": 0.0
}
```

**Résultat attendu** :
- `etatPrediction` : État prédit
- `riskScore` : Score entre 0 et 100
- `riskLevel` : "Faible", "Moyen", ou "Élevé"

---

## ✅ Checklist de Test

- [ ] Backend démarré
- [ ] Token d'authentification obtenu
- [ ] ID du dossier existant connu
- [ ] Fichiers de modèles IA présents
- [ ] Python installé
- [ ] Requête POST `/api/dossiers/{id}/amiable` exécutée
- [ ] Réponse contient `etatPrediction`, `riskScore`, `riskLevel`
- [ ] Valeurs de prédiction cohérentes
- [ ] Logs backend montrent "Prédiction IA réussie"
- [ ] Dossier mis à jour en base de données

---

**Date** : 2024-12-02  
**Version** : 1.0.0  
**Statut** : ✅ Guide complet pour tester l'IA sur un dossier existant

