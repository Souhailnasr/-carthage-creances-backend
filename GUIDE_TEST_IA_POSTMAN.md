# 🧪 Guide de Test IA avec Postman

## 📋 Prérequis

### **1. Vérifier les Fichiers de Modèles**

Assurez-vous que les 3 fichiers `.pkl` sont dans `src/main/resources/ia/` :
- ✅ `model_classification.pkl`
- ✅ `model_regression.pkl`
- ✅ `feature_columns.pkl`

### **2. Vérifier Python**

```bash
python --version
# ou
python3 --version
```

### **3. Démarrer le Backend**

Le serveur Spring Boot doit être démarré sur :
```
http://localhost:8089/carthage-creance
```

### **4. Obtenir un Token d'Authentification**

Vous devez d'abord vous connecter pour obtenir un token JWT.

---

## 🔐 Étape 1 : Authentification

### **Requête : Login**

**Méthode** : `POST`  
**URL** : `http://localhost:8089/carthage-creance/api/auth/login`

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

**Réponse attendue** :
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "votre_email@example.com",
    "role": "CHEF_AMIABLE"
  }
}
```

**⚠️ IMPORTANT** : Copiez le `token` pour l'utiliser dans les requêtes suivantes.

---

## 📁 Étape 2 : Créer un Dossier de Test

### **Requête : Créer un Dossier**

**Méthode** : `POST`  
**URL** : `http://localhost:8089/carthage-creance/api/dossiers/create`

**Headers** :
```
Content-Type: application/json
Authorization: Bearer VOTRE_TOKEN_ICI
```

**Body** (raw JSON) :
```json
{
  "titre": "Test Prédiction IA",
  "description": "Dossier de test pour la prédiction IA",
  "numeroDossier": "TEST-IA-001",
  "montantCreance": 50000.0,
  "urgence": "MOYENNE",
  "dossierStatus": "ENCOURSDETRAITEMENT",
  "statut": "VALIDE",
  "typeRecouvrement": "AMIABLE",
  "typeCreancier": "PERSONNE_MORALE",
  "nomCreancier": "Entreprise Test",
  "typeDebiteur": "PERSONNE_MORALE",
  "nomDebiteur": "Débiteur Test"
}
```

**Réponse attendue** :
```json
{
  "id": 38,
  "titre": "Test Prédiction IA",
  "montantCreance": 50000.0,
  "montantRecouvre": 0.0,
  "numeroDossier": "TEST-IA-001",
  "typeRecouvrement": "AMIABLE",
  // ... autres champs
}
```

**⚠️ IMPORTANT** : Notez l'`id` du dossier créé (ex: `38`) pour les étapes suivantes.

---

## 🔍 Étape 3 : Créer une Enquête (Optionnel mais Recommandé)

### **Requête : Créer une Enquête**

**Méthode** : `POST`  
**URL** : `http://localhost:8089/carthage-creance/api/enquetes`

**Headers** :
```
Content-Type: application/json
Authorization: Bearer VOTRE_TOKEN_ICI
```

**Body** (raw JSON) :
```json
{
  "dossierId": 38,
  "rapportCode": "CC6008",
  "chiffreAffaire": 1000000.0,
  "resultatNet": 100000.0,
  "capital": 500000.0,
  "effectif": 100,
  "appreciationBancaire": "Bonne",
  "bienImmobilier": "Oui, plusieurs biens",
  "bienMobilier": "Véhicules et équipements",
  "secteurActivite": "Commerce",
  "descriptionActivite": "Vente de produits"
}
```

**Réponse attendue** :
```json
{
  "id": 15,
  "dossierId": 38,
  "chiffreAffaire": 1000000.0,
  "resultatNet": 100000.0,
  // ... autres champs
}
```

---

## 📞 Étape 4 : Créer des Actions (Optionnel mais Recommandé)

### **Requête : Créer une Action Amiable**

**Méthode** : `POST`  
**URL** : `http://localhost:8089/carthage-creance/api/actions`

**Headers** :
```
Content-Type: application/json
Authorization: Bearer VOTRE_TOKEN_ICI
```

**Body** (raw JSON) :
```json
{
  "dossierId": 38,
  "type": "APPEL",
  "dateAction": "2024-12-01",
  "nbOccurrences": 3,
  "coutUnitaire": 5.0,
  "reponseDebiteur": "POSITIVE"
}
```

**Créer plusieurs actions pour un meilleur test** :

**Action 2** :
```json
{
  "dossierId": 38,
  "type": "EMAIL",
  "dateAction": "2024-12-02",
  "nbOccurrences": 2,
  "coutUnitaire": 2.0,
  "reponseDebiteur": "POSITIVE"
}
```

**Action 3** :
```json
{
  "dossierId": 38,
  "type": "VISITE",
  "dateAction": "2024-12-05",
  "nbOccurrences": 1,
  "coutUnitaire": 50.0,
  "reponseDebiteur": "NEGATIVE"
}
```

---

## 🤖 Étape 5 : Tester la Prédiction IA

### **Requête : Enregistrer une Action Amiable (Déclenche la Prédiction IA)**

**Méthode** : `POST`  
**URL** : `http://localhost:8089/carthage-creance/api/dossiers/38/amiable`

**⚠️ Remplacez `38` par l'ID de votre dossier**

**Headers** :
```
Content-Type: application/json
Authorization: Bearer VOTRE_TOKEN_ICI
```

**Body** (raw JSON) :
```json
{
  "montantRecouvre": 15000.0
}
```

**Réponse attendue** :
```json
{
  "id": 38,
  "titre": "Test Prédiction IA",
  "montantCreance": 50000.0,
  "montantRecouvre": 15000.0,
  "montantRestant": 35000.0,
  "etatPrediction": "RECOVERED_PARTIAL",  // ✅ PRÉDICTION IA
  "riskScore": 45.2,                       // ✅ PRÉDICTION IA
  "riskLevel": "Moyen",                    // ✅ PRÉDICTION IA
  "typeRecouvrement": "AMIABLE",
  "numeroDossier": "TEST-IA-001",
  // ... autres champs
}
```

**✅ Vérifications** :
- `etatPrediction` doit être présent : `RECOVERED_TOTAL`, `RECOVERED_PARTIAL`, ou `NOT_RECOVERED`
- `riskScore` doit être un nombre entre 0 et 100
- `riskLevel` doit être : `"Faible"`, `"Moyen"`, ou `"Élevé"`

---

## 📊 Étape 6 : Vérifier les Résultats

### **Requête : Récupérer le Dossier**

**Méthode** : `GET`  
**URL** : `http://localhost:8089/carthage-creance/api/dossiers/38`

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

---

## 🔄 Scénarios de Test

### **Scénario 1 : Dossier avec Bonne Solvabilité**

**Données** :
- Montant créance : 10000.0
- Montant recouvré : 8000.0
- Enquête : Chiffre d'affaires élevé, résultat net positif
- Actions : Plusieurs réponses positives

**Résultat attendu** :
- `etatPrediction` : `RECOVERED_PARTIAL` ou `RECOVERED_TOTAL`
- `riskScore` : < 50
- `riskLevel` : `"Faible"` ou `"Moyen"`

### **Scénario 2 : Dossier avec Faible Solvabilité**

**Données** :
- Montant créance : 10000.0
- Montant recouvré : 500.0
- Enquête : Chiffre d'affaires faible, résultat net négatif
- Actions : Plusieurs réponses négatives

**Résultat attendu** :
- `etatPrediction` : `NOT_RECOVERED`
- `riskScore` : > 70
- `riskLevel` : `"Élevé"`

### **Scénario 3 : Dossier Sans Données Complémentaires**

**Données** :
- Montant créance : 10000.0
- Montant recouvré : 3000.0
- Pas d'enquête
- Pas d'actions

**Résultat attendu** :
- La prédiction fonctionne quand même (avec valeurs par défaut)
- `etatPrediction`, `riskScore`, `riskLevel` sont présents

---

## 🐛 Dépannage

### **Erreur : "Python n'est pas trouvé"**

**Symptôme** :
```json
{
  "error": "Erreur interne: ..."
}
```

**Solution** :
1. Vérifier que Python est installé : `python --version`
2. Vérifier que Python est dans le PATH
3. Redémarrer le backend après installation de Python

### **Erreur : "Modèle non trouvé"**

**Symptôme** :
- La prédiction retourne toujours le fallback (`NOT_RECOVERED`, `riskScore=100.0`)

**Solution** :
1. Vérifier que les 3 fichiers `.pkl` sont dans `src/main/resources/ia/`
2. Vérifier les noms exacts des fichiers
3. Vérifier les logs backend pour l'erreur exacte

### **Prédiction Toujours en Fallback**

**Vérifications** :
1. Tester le script Python directement :
   ```bash
   python src/main/resources/ia/predict.py test.json
   ```
2. Vérifier les logs backend (console Spring Boot)
3. Vérifier que toutes les bibliothèques Python sont installées :
   ```bash
   pip install pandas scikit-learn joblib
   ```

### **Erreur 401 : Unauthorized**

**Solution** :
- Vérifier que le token est valide
- Se reconnecter pour obtenir un nouveau token
- Vérifier que le header `Authorization` est correct : `Bearer VOTRE_TOKEN`

### **Erreur 404 : Dossier non trouvé**

**Solution** :
- Vérifier que l'ID du dossier est correct
- Vérifier que le dossier existe dans la base de données

---

## 📝 Collection Postman Complète

### **Créer une Collection Postman**

1. **Créer une nouvelle Collection** : "Test IA Prédiction"

2. **Ajouter les Variables d'Environnement** :
   - `base_url` : `http://localhost:8089/carthage-creance`
   - `token` : (sera rempli après login)
   - `dossier_id` : (sera rempli après création du dossier)

3. **Créer les Requêtes** :

#### **1. Login**
```
POST {{base_url}}/api/auth/login
Body: { "email": "...", "password": "..." }
Tests: pm.environment.set("token", pm.response.json().token);
```

#### **2. Créer Dossier**
```
POST {{base_url}}/api/dossiers/create
Headers: Authorization: Bearer {{token}}
Body: { ... }
Tests: pm.environment.set("dossier_id", pm.response.json().id);
```

#### **3. Créer Enquête**
```
POST {{base_url}}/api/enquetes
Headers: Authorization: Bearer {{token}}
Body: { "dossierId": {{dossier_id}}, ... }
```

#### **4. Créer Action**
```
POST {{base_url}}/api/actions
Headers: Authorization: Bearer {{token}}
Body: { "dossierId": {{dossier_id}}, ... }
```

#### **5. Tester Prédiction IA**
```
POST {{base_url}}/api/dossiers/{{dossier_id}}/amiable
Headers: Authorization: Bearer {{token}}
Body: { "montantRecouvre": 15000.0 }
Tests: 
  pm.test("Prédiction IA présente", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('etatPrediction');
    pm.expect(jsonData).to.have.property('riskScore');
    pm.expect(jsonData).to.have.property('riskLevel');
  });
```

#### **6. Vérifier Dossier**
```
GET {{base_url}}/api/dossiers/{{dossier_id}}
Headers: Authorization: Bearer {{token}}
```

---

## ✅ Checklist de Test

### **Avant de Commencer**
- [ ] Backend démarré
- [ ] Fichiers `.pkl` dans `src/main/resources/ia/`
- [ ] Python installé et dans le PATH
- [ ] Bibliothèques Python installées (`pandas`, `scikit-learn`, `joblib`)

### **Tests à Effectuer**
- [ ] ✅ Login réussi (token obtenu)
- [ ] ✅ Dossier créé avec succès
- [ ] ✅ Enquête créée (optionnel)
- [ ] ✅ Actions créées (optionnel)
- [ ] ✅ Prédiction IA fonctionne (`etatPrediction`, `riskScore`, `riskLevel` présents)
- [ ] ✅ Valeurs de prédiction cohérentes
- [ ] ✅ Dossier mis à jour en base de données

### **Vérifications**
- [ ] Logs backend montrent "Prédiction IA réussie"
- [ ] Pas d'erreurs dans les logs
- [ ] Les champs IA sont sauvegardés en base de données

---

## 🎯 Résultat Attendu

Après avoir exécuté la requête `POST /api/dossiers/{id}/amiable`, vous devriez voir :

1. **Dans la Réponse Postman** :
   ```json
   {
     "etatPrediction": "RECOVERED_PARTIAL",
     "riskScore": 45.2,
     "riskLevel": "Moyen"
   }
   ```

2. **Dans les Logs Backend** :
   ```
   INFO - IaPredictionServiceImpl - Exécution de la prédiction IA pour le dossier...
   INFO - IaPredictionServiceImpl - Prédiction IA réussie: etatFinal=RECOVERED_PARTIAL, riskScore=45.2, riskLevel=Moyen
   INFO - DossierController - Prédiction IA appliquée au dossier 38: etatPrediction=RECOVERED_PARTIAL, riskScore=45.2, riskLevel=Moyen
   ```

3. **En Base de Données** :
   ```sql
   SELECT id, etat_prediction, risk_score, risk_level 
   FROM dossier 
   WHERE id = 38;
   ```
   Résultat : Les champs sont remplis avec les valeurs de prédiction.

---

## 📞 Support

Si vous rencontrez des problèmes :

1. **Vérifier les logs backend** pour les erreurs détaillées
2. **Tester le script Python directement** pour isoler le problème
3. **Vérifier que tous les prérequis sont remplis**

---

**Date** : 2024-12-01  
**Version** : 1.0.0  
**Statut** : ✅ Guide complet pour test Postman

