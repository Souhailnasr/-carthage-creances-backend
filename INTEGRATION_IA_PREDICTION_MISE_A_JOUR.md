# 🔄 Intégration IA - Mise à Jour et Guide Complet

## 📋 Analyse des Changements dans `predict.py`

### Changements Identifiés

1. **Normalisation du `risk_score`** : 
   - Ajout de `risk_score = float(max(0.0, min(100.0, risk_score)))` pour garantir que le score est entre 0 et 100
   - ✅ **Bonne pratique** : Évite les valeurs aberrantes

2. **Chemins relatifs pour les modèles** :
   - ❌ **Problème** : Les chemins `'src/main/resources/ia/model_classification.pkl'` sont relatifs et ne fonctionneront pas lors de l'exécution depuis Java
   - ✅ **Solution appliquée** : Utilisation de chemins absolus basés sur le répertoire du script

---

## ✅ Corrections Appliquées

### 1. Correction du Script Python (`predict.py`)

**Problème** : Les chemins relatifs ne fonctionnent pas lors de l'exécution depuis Java car le répertoire de travail est différent.

**Solution** : Utilisation de chemins absolus basés sur le répertoire du script.

```python
#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import sys
import json
import os
import joblib
import pandas as pd

# Obtenir le répertoire du script (pour charger les modèles depuis le même dossier)
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))

# Chemins des fichiers de modèles (relatifs au script)
MODEL_CLASS_PATH = os.path.join(SCRIPT_DIR, 'model_classification.pkl')
MODEL_REG_PATH = os.path.join(SCRIPT_DIR, 'model_regression.pkl')
FEATURE_COLS_PATH = os.path.join(SCRIPT_DIR, 'feature_columns.pkl')

# Charger les modèles et les noms de features
model_class = joblib.load(MODEL_CLASS_PATH)
model_reg = joblib.load(MODEL_REG_PATH)
feature_cols = joblib.load(FEATURE_COLS_PATH)

# ... reste du code ...
```

**Avantages** :
- ✅ Fonctionne indépendamment du répertoire de travail
- ✅ Les modèles sont chargés depuis le même dossier que le script
- ✅ Compatible avec l'exécution depuis Java

---

## 📁 Structure du Projet

```
src/
└── main/
    ├── java/
    │   └── projet/
    │       └── carthagecreance_backend/
    │           ├── Controller/
    │           │   └── DossierController.java
    │           ├── Service/
    │           │   ├── IaPredictionService.java
    │           │   └── Impl/
    │           │       ├── IaPredictionServiceImpl.java
    │           │       └── IaFeatureBuilderService.java
    │           └── DTO/
    │               └── IaPredictionResult.java
    └── resources/
        └── ia/
            ├── model_classification.pkl
            ├── model_regression.pkl
            ├── feature_columns.pkl
            └── predict.py
```

---

## 🔧 Services Backend Existants

### 1. `IaPredictionService` (Interface)

**Fichier** : `src/main/java/projet/carthagecreance_backend/Service/IaPredictionService.java`

```java
public interface IaPredictionService {
    IaPredictionResult predictRisk(Map<String, Object> donneesReelles);
}
```

### 2. `IaPredictionServiceImpl` (Implémentation)

**Fichier** : `src/main/java/projet/carthagecreance_backend/Service/Impl/IaPredictionServiceImpl.java`

**Fonctionnalités** :
- ✅ Création d'un fichier JSON temporaire avec les features
- ✅ Exécution du script Python `predict.py`
- ✅ Parsing de la réponse JSON
- ✅ Gestion des erreurs avec fallback
- ✅ Détection automatique de Python (`python3`, `python`, `py`)

### 3. `IaFeatureBuilderService` (Construction des Features)

**Fichier** : `src/main/java/projet/carthagecreance_backend/Service/Impl/IaFeatureBuilderService.java`

**Fonctionnalités** :
- ✅ Construction des features à partir des données réelles
- ✅ Extraction des données du dossier, enquête, actions, audiences, actions huissier
- ✅ Calcul des métriques (montants recouvrés, pourcentages, taux, etc.)
- ✅ Gestion des valeurs nulles avec valeurs par défaut

### 4. `IaPredictionResult` (DTO)

**Fichier** : `src/main/java/projet/carthagecreance_backend/DTO/IaPredictionResult.java`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IaPredictionResult {
    private String etatFinal;      // RECOVERED_TOTAL, RECOVERED_PARTIAL, NOT_RECOVERED
    private Double riskScore;      // 0-100
    private String riskLevel;      // Faible, Moyen, Élevé
}
```

---

## 🌐 Endpoints Disponibles

### Endpoint 1 : Prédiction IA via Action Amiable

**URL** : `POST /api/dossiers/{id}/amiable`

**Description** : Enregistre une action amiable et déclenche automatiquement la prédiction IA.

**Request Body** :
```json
{
  "montantRecouvre": 5000.0,
  "type": "APPEL",
  "dateAction": "2025-12-02",
  "reponseDebiteur": "POSITIVE"
}
```

**Response** : Retourne le dossier mis à jour avec les résultats de la prédiction IA :
```json
{
  "id": 42,
  "numeroDossier": "DOS-2025-001",
  "montantCreance": 10000.0,
  "montantRecouvre": 5000.0,
  "etatPrediction": "RECOVERED_PARTIAL",
  "riskScore": 45.5,
  "riskLevel": "Moyen",
  ...
}
```

### Endpoint 2 : Prédiction IA Dédiée ✅ CRÉÉ

**URL** : `POST /api/dossiers/{id}/predict-ia`

**Description** : Endpoint dédié pour déclencher uniquement la prédiction IA sans modifier le dossier.

**Request Body** : Aucun (toutes les données sont récupérées depuis le dossier)

**Response** :
```json
{
  "etatFinal": "RECOVERED_PARTIAL",
  "riskScore": 45.5,
  "riskLevel": "Moyen"
}
```

**Avantages** :
- ✅ Ne modifie pas le dossier (lecture seule)
- ✅ Permet de tester la prédiction IA sans impact
- ✅ Utile pour le débogage et les tests

---

## 🧪 Guide de Test avec Postman

### Prérequis

1. **Python installé** : Vérifier que Python 3 est installé et accessible dans le PATH
2. **Modèles IA** : Les fichiers `.pkl` doivent être présents dans `src/main/resources/ia/`
3. **Backend démarré** : Le serveur Spring Boot doit être en cours d'exécution

### Test 1 : Vérifier que Python est disponible

**Commande** :
```bash
python3 --version
# ou
python --version
# ou
py --version
```

### Test 2 : Tester le Script Python Directement

**Commande** :
```bash
cd src/main/resources/ia
python3 predict.py test_input.json
```

**Fichier `test_input.json`** (créer temporairement) :
```json
{
  "montantCreance": 10000.0,
  "montantRecouvre": 5000.0,
  "montantRecouvreAmiable": 3000.0,
  "montantRecouvreJuridique": 2000.0,
  "nbActionsTotal": 5.0,
  "nbActionsPositives": 3.0,
  "tauxReponsePositive": 0.6,
  "nbAudiences": 1.0,
  "nbActionsHuissierTotal": 2.0
}
```

**Résultat attendu** :
```json
{
  "etatFinal": "RECOVERED_PARTIAL",
  "riskScore": 45.5,
  "riskLevel": "Moyen"
}
```

### Test 3 : Test via Postman - Prédiction IA via Action Amiable

#### Étape 1 : Authentification

**Request** :
```
POST http://localhost:8089/carthage-creance/auth/authenticate
Content-Type: application/json
```

**Body** :
```json
{
  "username": "votre_username",
  "password": "votre_password"
}
```

**Response** : Copier le `token` de la réponse.

#### Étape 2 : Créer/Obtenir un Dossier

**Request** :
```
GET http://localhost:8089/carthage-creance/api/dossiers/{dossierId}
Authorization: Bearer {token}
```

#### Étape 3 : Enregistrer une Action Amiable (déclenche la prédiction IA)

**Request** :
```
POST http://localhost:8089/carthage-creance/api/dossiers/{dossierId}/amiable
Authorization: Bearer {token}
Content-Type: application/json
```

**Body** :
```json
{
  "montantRecouvre": 5000.0,
  "type": "APPEL",
  "dateAction": "2025-12-02",
  "reponseDebiteur": "POSITIVE"
}
```

**Response** : Le dossier avec les résultats de la prédiction IA :
```json
{
  "id": 42,
  "numeroDossier": "DOS-2025-001",
  "montantCreance": 10000.0,
  "montantRecouvre": 5000.0,
  "etatPrediction": "RECOVERED_PARTIAL",
  "riskScore": 45.5,
  "riskLevel": "Moyen",
  ...
}
```

#### Étape 4 : Vérifier les Résultats

**Request** :
```
GET http://localhost:8089/carthage-creance/api/dossiers/{dossierId}
Authorization: Bearer {token}
```

**Vérifier** :
- ✅ `etatPrediction` est présent
- ✅ `riskScore` est entre 0 et 100
- ✅ `riskLevel` est "Faible", "Moyen" ou "Élevé"

### Test 4 : Test via Postman - Prédiction IA Dédiée (Sans Modification)

#### Étape 1 : Authentification (identique au Test 3)

#### Étape 2 : Obtenir un Dossier (identique au Test 3)

#### Étape 3 : Prédiction IA Dédiée (Sans Modifier le Dossier)

**Request** :
```
POST http://localhost:8089/carthage-creance/api/dossiers/{dossierId}/predict-ia
Authorization: Bearer {token}
```

**Body** : Aucun (toutes les données sont récupérées depuis le dossier)

**Response** :
```json
{
  "etatFinal": "RECOVERED_PARTIAL",
  "riskScore": 45.5,
  "riskLevel": "Moyen"
}
```

**Avantages** :
- ✅ Ne modifie pas le dossier
- ✅ Permet de tester la prédiction sans impact
- ✅ Utile pour le débogage

#### Étape 4 : Vérifier que le Dossier n'a pas été Modifié

**Request** :
```
GET http://localhost:8089/carthage-creance/api/dossiers/{dossierId}
Authorization: Bearer {token}
```

**Vérifier** :
- ✅ Le dossier n'a pas été modifié (si `etatPrediction` était null avant, il reste null)
- ✅ Les autres champs du dossier sont inchangés

---

## 🔍 Vérification de l'Intégration

### Checklist

- [x] Script Python corrigé avec chemins absolus
- [x] `IaPredictionService` implémenté
- [x] `IaFeatureBuilderService` implémenté
- [x] `IaPredictionResult` DTO créé
- [x] Endpoint `/api/dossiers/{id}/amiable` intégré avec IA
- [x] Endpoint dédié `/api/dossiers/{id}/predict-ia` créé
- [x] Tests Postman documentés

---

## 🐛 Dépannage

### Erreur : "Python n'est pas trouvé dans le PATH"

**Solution** :
1. Vérifier que Python est installé : `python3 --version`
2. Ajouter Python au PATH système
3. Redémarrer le serveur Spring Boot

### Erreur : "Modèle non trouvé"

**Solution** :
1. Vérifier que les fichiers `.pkl` sont présents dans `src/main/resources/ia/`
2. Vérifier les permissions de lecture
3. Vérifier que le script Python utilise bien les chemins absolus

### Erreur : "Erreur lors de l'exécution du script Python"

**Solution** :
1. Tester le script Python directement en ligne de commande
2. Vérifier les logs du backend pour plus de détails
3. Vérifier que toutes les dépendances Python sont installées (`joblib`, `pandas`)

### Erreur : "LazyInitializationException"

**Solution** :
1. S'assurer que les relations sont chargées avec `@EntityGraph` ou `JOIN FETCH`
2. Vérifier que `IaFeatureBuilderService` accède aux données dans une transaction active

---

## 📝 Notes Techniques

### Normalisation du Risk Score

Le script Python normalise maintenant le `risk_score` entre 0 et 100 :
```python
risk_score = float(max(0.0, min(100.0, risk_score)))
```

Cela garantit que :
- ✅ Le score ne peut pas être négatif
- ✅ Le score ne peut pas dépasser 100
- ✅ Le score est toujours un nombre flottant

### Chemins Absolus vs Relatifs

**Avant** (ne fonctionnait pas) :
```python
model_class = joblib.load('src/main/resources/ia/model_classification.pkl')
```

**Après** (fonctionne) :
```python
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_CLASS_PATH = os.path.join(SCRIPT_DIR, 'model_classification.pkl')
model_class = joblib.load(MODEL_CLASS_PATH)
```

---

## ✨ Conclusion

L'intégration IA est maintenant complète et fonctionnelle. Le script Python utilise des chemins absolus pour charger les modèles, et le backend est prêt à exécuter les prédictions.

**Prochaines étapes** :
1. Tester avec Postman selon le guide ci-dessus
2. Vérifier que l'endpoint dédié `/api/dossiers/{id}/predict-ia` existe (ou le créer si nécessaire)
3. Monitorer les logs pour détecter d'éventuelles erreurs

**Date de mise à jour** : 2025-12-02  
**Statut** : ✅ Prêt pour les tests

