# 🔍 Analyse : Problème Statistiques Enquête

## ❌ Problème Identifié

Après avoir ajouté une enquête, les statistiques restent à 0 dans l'interface.

### Cause Racine

**Dans `getStatistiquesGlobales()` (lignes 156-161) :**

Le code calcule uniquement `enquetesCompletees` qui compte **seulement les enquêtes validées** :

```java
// Statistiques des enquêtes
List<Enquette> toutesEnquetes = enquetteRepository.findAll();
long enquetesCompletees = toutesEnquetes.stream()
        .filter(e -> e.getStatut() == Statut.VALIDE)  // ❌ Seulement les validées
        .count();
stats.put("enquetesCompletees", enquetesCompletees);
```

**Problème :** Il manque une statistique pour le **total des enquêtes créées** (pas seulement validées).

### Pourquoi les Statistiques Restent à 0

1. **Enquête créée mais non validée** :
   - Statut : `EN_ATTENTE_VALIDATION` (pas `VALIDE`)
   - `enquetesCompletees` ne compte que les enquêtes `VALIDE`
   - Résultat : `enquetesCompletees = 0` même si une enquête existe

2. **Statistique manquante** :
   - Il n'y a pas de `totalEnquetes` ou `enquetesCrees` dans les statistiques
   - Le frontend ne peut pas afficher le nombre total d'enquêtes créées

### Statistiques Disponibles Actuellement

| Clé API | Description | Filtre |
|---------|-------------|--------|
| `enquetesCompletees` | Enquêtes validées | `statut == VALIDE` uniquement |
| ❌ **MANQUE** | Total enquêtes créées | Toutes les enquêtes |

---

## 🔧 Solution Backend (À Appliquer)

### Problème 1 : Ajouter Statistique Total Enquêtes

**Fichier :** `StatistiqueServiceImpl.java`  
**Méthode :** `getStatistiquesGlobales()`  
**Ligne :** Après la ligne 161

**Ajout nécessaire :**
```java
// Statistiques des enquêtes
List<Enquette> toutesEnquetes = enquetteRepository.findAll();
long totalEnquetes = toutesEnquetes.size();  // ✅ NOUVEAU : Total enquêtes
long enquetesCompletees = toutesEnquetes.stream()
        .filter(e -> e.getStatut() == Statut.VALIDE)
        .count();
stats.put("totalEnquetes", totalEnquetes);  // ✅ NOUVEAU
stats.put("enquetesCompletees", enquetesCompletees);
```

### Problème 2 : Ajouter TypeStatistique pour Total Enquêtes

**Fichier :** `TypeStatistique.java`  
**Ajout nécessaire :**
```java
TOTAL_ENQUETES,  // ✅ NOUVEAU
ENQUETES_COMPLETEES,
```

### Problème 3 : Ajouter Mapping dans getTypeStatistiqueFromKey

**Fichier :** `StatistiqueServiceImpl.java`  
**Méthode :** `getTypeStatistiqueFromKey()`  
**Ligne :** Après la ligne 677

**Ajout nécessaire :**
```java
mapping.put("totalEnquetes", TypeStatistique.TOTAL_ENQUETES);  // ✅ NOUVEAU
mapping.put("enquetesCompletees", TypeStatistique.ENQUETES_COMPLETEES);
```

---

## 📊 Vérifications Nécessaires

### Vérification 1 : Statut de l'Enquête Créée

Dans la base de données, vérifier le statut de l'enquête créée :
```sql
SELECT id, statut, valide, date_creation 
FROM enquette 
ORDER BY date_creation DESC 
LIMIT 1;
```

**Si `statut = 'EN_ATTENTE_VALIDATION'` :**
- ✅ C'est normal que `enquetesCompletees = 0`
- ❌ Mais il devrait y avoir `totalEnquetes = 1` (statistique manquante)

### Vérification 2 : Appel API

Vérifier que le frontend appelle bien l'API :
- Endpoint : `GET /api/statistiques/globales`
- Headers : `Authorization: Bearer {token}`
- Réponse : Doit contenir toutes les statistiques calculées en temps réel

### Vérification 3 : Affichage Frontend

Vérifier que le frontend :
- ✅ Appelle l'API `/api/statistiques/globales`
- ✅ Affiche les valeurs retournées
- ✅ Met à jour l'affichage après chaque action

---

## 🎯 Résumé du Problème

### Problème Principal
- **Statistique manquante** : Il n'y a pas de `totalEnquetes` dans les statistiques
- **Filtre trop restrictif** : `enquetesCompletees` ne compte que les enquêtes validées
- **Résultat** : Si une enquête est créée mais non validée, aucune statistique ne la reflète

### Solution
1. Ajouter `totalEnquetes` dans `getStatistiquesGlobales()`
2. Ajouter `TOTAL_ENQUETES` dans `TypeStatistique`
3. Ajouter le mapping dans `getTypeStatistiqueFromKey()`
4. Le frontend pourra alors afficher le total des enquêtes créées

---

## ✅ Après Correction

Après ces corrections :
- ✅ `totalEnquetes` affichera le nombre total d'enquêtes créées (validées ou non)
- ✅ `enquetesCompletees` affichera le nombre d'enquêtes validées
- ✅ Les statistiques seront mises à jour automatiquement après création d'enquête
- ✅ Le frontend pourra afficher les deux valeurs

