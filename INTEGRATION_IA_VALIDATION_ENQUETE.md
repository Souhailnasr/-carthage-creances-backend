# 🔄 Intégration IA - Déclenchement Automatique lors de la Validation de l'Enquête

## 📋 Vue d'Ensemble

Ce document explique l'intégration de la prédiction IA qui se déclenche **automatiquement** lors de la validation d'une enquête.

---

## ✅ Modifications Appliquées

### 1. Modification de `EnquetteServiceImpl`

**Fichier** : `src/main/java/projet/carthagecreance_backend/Service/Impl/EnquetteServiceImpl.java`

#### Ajouts :

1. **Injection des services IA** :
```java
@Autowired
private IaPredictionService iaPredictionService;

@Autowired
private IaFeatureBuilderService iaFeatureBuilderService;

@Autowired
private ActionRepository actionRepository;

@Autowired
private AudienceRepository audienceRepository;

@Autowired
private ActionHuissierRepository actionHuissierRepository;
```

2. **Nouvelle méthode privée `triggerIaPrediction()`** :
```java
/**
 * Déclenche la prédiction IA pour un dossier après validation de l'enquête
 * 
 * @param dossierId ID du dossier
 * @param enquette L'enquête validée
 */
private void triggerIaPrediction(Long dossierId, Enquette enquette) {
    // Récupère le dossier et toutes les données associées
    // Construit les features
    // Fait la prédiction IA
    // Met à jour le dossier avec les résultats
}
```

3. **Appel dans `validerEnquette()`** :
   - Après la validation réussie de l'enquête
   - La prédiction IA est déclenchée automatiquement

4. **Appel dans `createEnquette()`** :
   - Lorsqu'une enquête est créée par un chef (validation automatique)
   - La prédiction IA est également déclenchée

---

## 🔄 Workflow Complet

### Scénario 1 : Validation Manuelle par un Chef

```
1. Agent crée une enquête
   └─> Enquête en statut EN_ATTENTE_VALIDATION
   
2. Chef valide l'enquête
   └─> PUT /api/enquettes/{id}/valider?chefId={chefId}
   
3. Backend valide l'enquête
   └─> Statut → VALIDE
   └─> dateValidation → maintenant
   
4. ✅ NOUVEAU : Déclenchement automatique de la prédiction IA
   └─> Récupération du dossier
   └─> Récupération des données (enquête, actions, audiences, actions huissier)
   └─> Construction des features
   └─> Prédiction IA
   └─> Mise à jour du dossier (etatPrediction, riskScore, riskLevel)
   
5. Notification envoyée à l'agent créateur
```

### Scénario 2 : Création par un Chef (Validation Automatique)

```
1. Chef crée une enquête
   └─> POST /api/enquettes
   
2. Backend détecte que le créateur est un chef
   └─> Validation automatique
   └─> Statut → VALIDE immédiatement
   
3. ✅ NOUVEAU : Déclenchement automatique de la prédiction IA
   └─> Même processus que le scénario 1
   
4. Enquête retournée avec statut VALIDE
```

---

## 🎯 Avantages de cette Approche

### ✅ Moment Optimal

La validation de l'enquête est le **moment idéal** pour déclencher la prédiction IA car :

1. **Données validées** : L'enquête contient des informations fiables et vérifiées
2. **Données complètes** : L'enquête est une source importante de features pour l'IA
3. **Workflow naturel** : La validation est une étape clé du processus métier
4. **Prédiction précoce** : Permet d'avoir une première prédiction dès que possible

### ✅ Prédiction Automatique

- **Pas d'intervention manuelle** nécessaire
- **Cohérent** avec le workflow existant
- **Non bloquant** : Si l'IA échoue, l'enquête reste validée

---

## 📊 Données Utilisées pour la Prédiction

Lors de la validation de l'enquête, la prédiction IA utilise :

| Donnée | Source | Disponibilité |
|--------|--------|---------------|
| **Dossier** | `DossierRepository.findById()` | ✅ Toujours |
| **Enquête** | Paramètre `enquette` (validée) | ✅ Toujours |
| **Actions** | `ActionRepository.findByDossierId()` | ⚠️ Peut être vide |
| **Audiences** | `AudienceRepository.findByDossierId()` | ⚠️ Peut être vide |
| **Actions Huissier** | `ActionHuissierRepository.findByDossierId()` | ⚠️ Peut être vide |
| **Finance** | `dossier.getFinance()` | ⚠️ Peut être null |

**Note** : Même si certaines données sont vides, la prédiction fonctionne grâce aux valeurs par défaut dans `IaFeatureBuilderService`.

---

## 🔍 Gestion des Erreurs

### Stratégie : Non Bloquante

Si la prédiction IA échoue :

1. ✅ **L'enquête reste validée** : La validation n'est pas annulée
2. ✅ **Log de l'erreur** : L'erreur est enregistrée dans les logs
3. ✅ **Pas d'exception** : L'exception est catchée et loggée
4. ✅ **Workflow continue** : Le processus métier continue normalement

**Code** :
```java
try {
    // Prédiction IA
    triggerIaPrediction(dossierId, enquette);
} catch (Exception e) {
    logger.warn("Erreur lors de la prédiction IA: {}. Le dossier sera sauvegardé sans prédiction.", e.getMessage());
    // Continue sans bloquer
}
```

---

## 🧪 Tests

### Test 1 : Validation Manuelle

**Requête** :
```
PUT http://localhost:8089/carthage-creance/api/enquettes/{enquetteId}/valider?chefId={chefId}
Authorization: Bearer {token}
```

**Vérifications** :
1. ✅ L'enquête est validée
2. ✅ Le dossier a `etatPrediction`, `riskScore`, `riskLevel` mis à jour
3. ✅ Les logs montrent la prédiction IA réussie

### Test 2 : Création par Chef

**Requête** :
```
POST http://localhost:8089/carthage-creance/api/enquettes
Authorization: Bearer {token}
Body: {
  "dossierId": 42,
  "agentCreateurId": {chefId},
  ...
}
```

**Vérifications** :
1. ✅ L'enquête est créée avec statut VALIDE
2. ✅ Le dossier a `etatPrediction`, `riskScore`, `riskLevel` mis à jour
3. ✅ Les logs montrent la prédiction IA réussie

### Test 3 : Vérification du Dossier

**Requête** :
```
GET http://localhost:8089/carthage-creance/api/dossiers/{dossierId}
Authorization: Bearer {token}
```

**Réponse attendue** :
```json
{
  "id": 42,
  "numeroDossier": "DOS-2025-001",
  ...
  "etatPrediction": "RECOVERED_PARTIAL",
  "riskScore": 45.5,
  "riskLevel": "Moyen",
  ...
}
```

---

## 📝 Logs

### Logs de Succès

```
INFO - Déclenchement de la prédiction IA pour le dossier 42 après validation de l'enquête 10
INFO - Prédiction IA appliquée au dossier 42 après validation de l'enquête: etatPrediction=RECOVERED_PARTIAL, riskScore=45.5, riskLevel=Moyen
```

### Logs d'Erreur

```
WARN - Erreur lors de la prédiction IA pour le dossier 42 après validation de l'enquête 10: Python non disponible. Le dossier sera sauvegardé sans prédiction.
```

---

## 🔄 Comparaison avec les Autres Déclenchements

| Événement | Déclenchement IA | Moment |
|-----------|------------------|--------|
| **Création Enquête** | ❌ Non | Trop tôt, données non validées |
| **Validation Enquête** | ✅ **OUI** | ✅ **Optimal** - Données validées |
| **Action Amiable** | ✅ OUI | Bon moment - Données mises à jour |
| **Endpoint Dédié** | ✅ OUI | Manuel - Sur demande |

---

## ✨ Résumé

### Ce qui a été ajouté :

1. ✅ **Déclenchement automatique** dans `validerEnquette()`
2. ✅ **Déclenchement automatique** dans `createEnquette()` (si créé par chef)
3. ✅ **Méthode privée `triggerIaPrediction()`** pour centraliser la logique
4. ✅ **Gestion d'erreurs non bloquante**
5. ✅ **Logs détaillés** pour le débogage

### Résultat :

- ✅ La prédiction IA se déclenche **automatiquement** après chaque validation d'enquête
- ✅ Le dossier est mis à jour avec les résultats de la prédiction
- ✅ Le workflow métier n'est pas bloqué si l'IA échoue
- ✅ Les utilisateurs voient immédiatement la prédiction après validation

---

## 📚 Références

- `EnquetteServiceImpl.java` : Service modifié
- `IaPredictionService.java` : Service de prédiction IA
- `IaFeatureBuilderService.java` : Construction des features
- `DossierController.java` : Autres déclenchements IA

**Date de modification** : 2025-12-02  
**Statut** : ✅ Implémenté et testé

