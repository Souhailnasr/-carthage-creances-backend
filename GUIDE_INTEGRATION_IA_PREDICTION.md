# 🤖 Guide d'Intégration : Modèle IA de Prédiction

## 📋 Vue d'Ensemble

Ce guide explique comment le modèle d'IA de prédiction a été intégré dans le backend Spring Boot pour prédire l'état final et le score de risque des dossiers de recouvrement.

---

## 🏗️ Architecture de l'Intégration

### **Composants Créés**

1. **Script Python** : `src/main/resources/ia/predict.py`
   - Charge les modèles `.pkl`
   - Prédit l'état final et le score de risque
   - Retourne un JSON avec les résultats

2. **DTO** : `IaPredictionResult.java`
   - Contient `etatFinal`, `riskScore`, `riskLevel`

3. **Service de Prédiction** : `IaPredictionService.java` / `IaPredictionServiceImpl.java`
   - Exécute le script Python
   - Gère les erreurs et les fallbacks

4. **Service de Construction de Features** : `IaFeatureBuilderService.java`
   - Construit les features à partir des données réelles (Dossier, Enquête, Actions, Audiences)

5. **Intégration dans le Contrôleur** : `DossierController.java`
   - Endpoint `/api/dossiers/{id}/amiable` mis à jour
   - Appelle la prédiction IA après mise à jour du montant recouvré

6. **Champs dans l'Entité Dossier** :
   - `etatPrediction` : État prédit par l'IA
   - `riskScore` : Score de risque (0-100)
   - `riskLevel` : Niveau de risque ("Faible", "Moyen", "Élevé")

---

## 📁 Structure des Fichiers

```
src/main/
├── java/
│   └── projet/carthagecreance_backend/
│       ├── Entity/
│       │   └── Dossier.java (modifié - ajout des champs IA)
│       ├── DTO/
│       │   └── IaPredictionResult.java (nouveau)
│       ├── Service/
│       │   ├── IaPredictionService.java (nouveau)
│       │   └── Impl/
│       │       ├── IaPredictionServiceImpl.java (nouveau)
│       │       └── IaFeatureBuilderService.java (nouveau)
│       └── Controller/
│           └── DossierController.java (modifié)
└── resources/
    └── ia/
        ├── predict.py (nouveau - script Python)
        ├── model_classification.pkl (à placer)
        ├── model_regression.pkl (à placer)
        └── feature_columns.pkl (à placer)
```

---

## 🔧 Détails Techniques

### **1. Script Python (`predict.py`)**

**Fonctionnalités** :
- ✅ Charge les 3 fichiers `.pkl` depuis le même répertoire
- ✅ Lit les données JSON depuis un fichier temporaire
- ✅ Prépare les données avec pandas
- ✅ Prédit avec les deux modèles (classification + régression)
- ✅ Retourne un JSON structuré

**Chemins** :
- Les modèles sont chargés depuis `src/main/resources/ia/`
- Le script utilise des chemins relatifs pour trouver les fichiers `.pkl`

### **2. Service de Prédiction (`IaPredictionServiceImpl`)**

**Fonctionnalités** :
- ✅ Crée un fichier temporaire JSON avec les features
- ✅ Exécute le script Python avec `ProcessBuilder`
- ✅ Lit la sortie JSON du script
- ✅ Parse le résultat en `IaPredictionResult`
- ✅ Gère les erreurs avec un fallback
- ✅ Nettoie les fichiers temporaires

**Détection de Python** :
- Essaie `python3`, `python`, puis `py`
- Retourne une erreur si Python n'est pas trouvé

### **3. Service de Construction de Features (`IaFeatureBuilderService`)**

**Features Extraites** :

#### **Dossier** :
- `montantCreance`, `montantRecouvre`, `montantRestant`
- `pourcentageRecouvre` (calculé)
- `dureeGestionJours` (calculé)
- `urgence_Faible`, `urgence_Moyenne` (one-hot encoding)
- `typeRecouvrement_AMIABLE`, `typeRecouvrement_JURIDIQUE` (one-hot encoding)

#### **Enquête** :
- `enquete_chiffreAffaire`, `enquete_resultatNet`, `enquete_capital`, `enquete_effectif`
- `enquete_hasAppreciationBancaire`, `enquete_hasBienImmobilier`, `enquete_hasBienMobilier` (booléens)

#### **Actions** :
- `nbActionsTotal`, `nbActionsPositives`, `nbActionsNegatives`
- `tauxReponsePositive` (calculé)
- `coutTotalActions` (calculé)
- `nbActions_APPEL`, `nbActions_EMAIL`, `nbActions_VISITE`, `nbActions_LETTRE`, `nbActions_AUTRE`

#### **Audiences** :
- `nbAudiences`, `nbAudiencesFavorables`, `nbAudiencesDefavorables`
- `tauxAudiencesFavorables` (calculé)

#### **Finance** :
- `finance_fraisCreationDossier`, `finance_fraisGestionDossier`
- `finance_dureeGestionMois`
- `finance_coutActionsAmiable`, `finance_coutActionsJuridique`
- `finance_fraisAvocat`, `finance_fraisHuissier`

### **4. Intégration dans le Contrôleur**

**Endpoint modifié** : `POST /api/dossiers/{id}/amiable`

**Workflow** :
1. Valide les données d'entrée
2. Met à jour le montant recouvré
3. **NOUVEAU** : Récupère les données associées (Enquête, Actions, Audiences)
4. **NOUVEAU** : Construit les features
5. **NOUVEAU** : Appelle la prédiction IA
6. **NOUVEAU** : Met à jour le dossier avec les résultats de la prédiction
7. Retourne le dossier mis à jour

**Gestion d'erreurs** :
- Si la prédiction IA échoue, le dossier est quand même sauvegardé (non bloquant)
- Les erreurs sont loggées mais n'empêchent pas la mise à jour du montant recouvré

---

## 🧪 Guide de Test

### **Prérequis**

1. **Python installé** :
   ```bash
   python --version
   # ou
   python3 --version
   ```

2. **Bibliothèques Python** :
   ```bash
   pip install pandas scikit-learn joblib
   ```

3. **Fichiers de modèles** :
   - Placer `model_classification.pkl` dans `src/main/resources/ia/`
   - Placer `model_regression.pkl` dans `src/main/resources/ia/`
   - Placer `feature_columns.pkl` dans `src/main/resources/ia/`

### **Test 1 : Vérifier le Script Python Directement**

```bash
# Créer un fichier JSON de test
echo '{"montantCreance": 1000.0, "montantRecouvre": 500.0, "nbActionsTotal": 5.0}' > test_input.json

# Exécuter le script
python src/main/resources/ia/predict.py test_input.json

# Résultat attendu :
# {"etatFinal": "RECOVERED_PARTIAL", "riskScore": 45.2, "riskLevel": "Moyen"}
```

### **Test 2 : Test via l'API Backend**

#### **Étape 1 : Créer un dossier avec des données**

```bash
POST http://localhost:8089/carthage-creance/api/dossiers/create
Content-Type: application/json
Authorization: Bearer YOUR_TOKEN

{
  "titre": "Test IA",
  "nomCreancier": "Test Creancier",
  "nomDebiteur": "Test Debiteur",
  "montantCreance": 10000.0,
  "urgence": "MOYENNE",
  "typeRecouvrement": "AMIABLE"
}
```

**Notez l'ID du dossier créé** (ex: `38`)

#### **Étape 2 : Créer une enquête**

```bash
POST http://localhost:8089/carthage-creance/api/enquetes
Content-Type: application/json
Authorization: Bearer YOUR_TOKEN

{
  "dossierId": 38,
  "chiffreAffaire": 500000.0,
  "resultatNet": 50000.0,
  "capital": 100000.0,
  "effectif": 50
}
```

#### **Étape 3 : Créer des actions**

```bash
POST http://localhost:8089/carthage-creance/api/actions
Content-Type: application/json
Authorization: Bearer YOUR_TOKEN

{
  "dossierId": 38,
  "type": "APPEL",
  "dateAction": "2024-12-01",
  "nbOccurrences": 3,
  "coutUnitaire": 5.0,
  "reponseDebiteur": "POSITIVE"
}
```

#### **Étape 4 : Tester la prédiction IA**

```bash
POST http://localhost:8089/carthage-creance/api/dossiers/38/amiable
Content-Type: application/json
Authorization: Bearer YOUR_TOKEN

{
  "montantRecouvre": 3000.0
}
```

**Réponse attendue** :

```json
{
  "id": 38,
  "titre": "Test IA",
  "montantCreance": 10000.0,
  "montantRecouvre": 3000.0,
  "montantRestant": 7000.0,
  "etatPrediction": "RECOVERED_PARTIAL",  // ✅ NOUVEAU
  "riskScore": 45.2,                       // ✅ NOUVEAU
  "riskLevel": "Moyen",                    // ✅ NOUVEAU
  // ... autres champs
}
```

### **Test 3 : Vérifier les Logs Backend**

Vérifier les logs pour confirmer que la prédiction fonctionne :

```
INFO  - IaPredictionServiceImpl - Exécution de la prédiction IA pour le dossier...
INFO  - IaPredictionServiceImpl - Prédiction IA réussie: etatFinal=RECOVERED_PARTIAL, riskScore=45.2, riskLevel=Moyen
INFO  - DossierController - Prédiction IA appliquée au dossier 38: etatPrediction=RECOVERED_PARTIAL, riskScore=45.2, riskLevel=Moyen
```

### **Test 4 : Test avec cURL**

```bash
# Test complet
curl -X POST "http://localhost:8089/carthage-creance/api/dossiers/38/amiable" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "montantRecouvre": 5000.0
  }' | jq '.etatPrediction, .riskScore, .riskLevel'
```

### **Test 5 : Vérifier la Base de Données**

```sql
-- Vérifier que les champs IA sont bien sauvegardés
SELECT id, titre, etat_prediction, risk_score, risk_level 
FROM dossier 
WHERE id = 38;
```

**Résultat attendu** :
```
id | titre    | etat_prediction    | risk_score | risk_level
38 | Test IA  | RECOVERED_PARTIAL  | 45.2       | Moyen
```

---

## 🔍 Dépannage

### **Erreur : "Python n'est pas trouvé"**

**Solution** :
1. Vérifier que Python est installé : `python --version`
2. Vérifier que Python est dans le PATH
3. Sur Windows, peut-être utiliser `py` au lieu de `python`

**Modification dans `IaPredictionServiceImpl.java`** :
```java
// Ajouter d'autres commandes si nécessaire
String[] commands = {"python3", "python", "py", "C:\\Python\\python.exe"};
```

### **Erreur : "Modèle non trouvé"**

**Solution** :
1. Vérifier que les 3 fichiers `.pkl` sont dans `src/main/resources/ia/`
2. Vérifier les noms exacts :
   - `model_classification.pkl`
   - `model_regression.pkl`
   - `feature_columns.pkl`

### **Erreur : "Erreur lors de l'exécution du script Python"**

**Vérifications** :
1. Tester le script Python directement (voir Test 1)
2. Vérifier que toutes les bibliothèques Python sont installées
3. Vérifier les logs backend pour voir l'erreur exacte

### **Erreur : "Feature columns mismatch"**

**Solution** :
1. Vérifier que `feature_columns.pkl` correspond aux features construites
2. Vérifier que toutes les colonnes attendues sont présentes dans `IaFeatureBuilderService`

### **Prédiction toujours en Fallback**

**Causes possibles** :
1. Python non trouvé
2. Script Python qui échoue
3. Modèles non trouvés
4. Erreur de parsing JSON

**Solution** :
- Vérifier les logs backend pour l'erreur exacte
- Tester le script Python directement

---

## 📊 Exemple de Features Générées

```json
{
  "montantCreance": 10000.0,
  "montantRecouvre": 3000.0,
  "montantRestant": 7000.0,
  "pourcentageRecouvre": 30.0,
  "dureeGestionJours": 45.0,
  "urgence_Faible": 0.0,
  "urgence_Moyenne": 1.0,
  "typeRecouvrement_AMIABLE": 1.0,
  "typeRecouvrement_JURIDIQUE": 0.0,
  "enquete_chiffreAffaire": 500000.0,
  "enquete_resultatNet": 50000.0,
  "enquete_capital": 100000.0,
  "enquete_effectif": 50,
  "enquete_hasAppreciationBancaire": 1.0,
  "enquete_hasBienImmobilier": 1.0,
  "enquete_hasBienMobilier": 0.0,
  "nbActionsTotal": 5.0,
  "nbActionsPositives": 3.0,
  "nbActionsNegatives": 1.0,
  "tauxReponsePositive": 0.6,
  "coutTotalActions": 25.0,
  "nbActions_APPEL": 3.0,
  "nbActions_EMAIL": 2.0,
  "nbActions_VISITE": 0.0,
  "nbActions_LETTRE": 0.0,
  "nbActions_AUTRE": 0.0,
  "nbAudiences": 0.0,
  "nbAudiencesFavorables": 0.0,
  "nbAudiencesDefavorables": 0.0,
  "tauxAudiencesFavorables": 0.0,
  "finance_fraisCreationDossier": 50.0,
  "finance_fraisGestionDossier": 10.0,
  "finance_dureeGestionMois": 1,
  "finance_coutActionsAmiable": 25.0,
  "finance_coutActionsJuridique": 0.0,
  "finance_fraisAvocat": 0.0,
  "finance_fraisHuissier": 0.0
}
```

---

## ✅ Checklist d'Intégration

### **Fichiers à Placer**
- [ ] `model_classification.pkl` dans `src/main/resources/ia/`
- [ ] `model_regression.pkl` dans `src/main/resources/ia/`
- [ ] `feature_columns.pkl` dans `src/main/resources/ia/`

### **Fichiers Créés/Modifiés**
- [x] ✅ `predict.py` créé
- [x] ✅ `IaPredictionResult.java` créé
- [x] ✅ `IaPredictionService.java` créé
- [x] ✅ `IaPredictionServiceImpl.java` créé
- [x] ✅ `IaFeatureBuilderService.java` créé
- [x] ✅ `Dossier.java` modifié (champs IA ajoutés)
- [x] ✅ `DossierController.java` modifié (intégration IA)

### **Dépendances**
- [ ] Python installé et dans le PATH
- [ ] Bibliothèques Python installées : `pandas`, `scikit-learn`, `joblib`

### **Tests**
- [ ] Test du script Python directement
- [ ] Test via l'API backend
- [ ] Vérification des logs
- [ ] Vérification de la base de données

---

## 🎯 Résultat Attendu

Après l'intégration complète :

1. ✅ Le script Python est exécutable
2. ✅ Les modèles sont chargés correctement
3. ✅ Les features sont construites à partir des données réelles
4. ✅ La prédiction est effectuée lors de l'enregistrement d'une action amiable
5. ✅ Le dossier est mis à jour avec `etatPrediction`, `riskScore`, `riskLevel`
6. ✅ Les résultats sont sauvegardés en base de données
7. ✅ Le frontend peut afficher les prédictions

---

## 📝 Notes Importantes

1. **Performance** : L'exécution du script Python peut prendre quelques secondes. C'est normal.

2. **Non-bloquant** : Si la prédiction IA échoue, le dossier est quand même sauvegardé (l'IA est optionnelle).

3. **Fallback** : En cas d'erreur, le système retourne :
   - `etatPrediction = NOT_RECOVERED`
   - `riskScore = 100.0`
   - `riskLevel = "Élevé"`

4. **Sécurité** : Les fichiers temporaires sont automatiquement supprimés après utilisation.

5. **Chemins** : Le script Python utilise des chemins relatifs pour trouver les modèles dans le même répertoire.

---

**Date d'intégration** : 2024-12-01  
**Version** : 1.0.0  
**Statut** : ✅ Intégration complète et prête pour test

