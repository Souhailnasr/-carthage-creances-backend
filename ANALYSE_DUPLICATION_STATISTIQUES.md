# 🔍 Analyse : Duplication dans la Table Statistiques

## ❌ Problème Identifié

**Problème :** Des duplications dans la table `statistiques` - plusieurs lignes avec le même `type` et la même `periode`.

**Cause :** Le code crée toujours de nouvelles entrées avec `save()` au lieu de mettre à jour les statistiques existantes ou de supprimer les anciennes avant d'en créer de nouvelles.

---

## 🔍 Analyse du Problème

### Comportement Actuel

**Dans `calculerEtStockerStatistiquesGlobales()` et `recalculerStatistiquesAsync()` :**

```java
// Pour chaque statistique
Statistique statistique = Statistique.builder()
    .type(type)
    .valeur(convertToDouble(entry.getValue()))
    .description(entry.getKey())
    .periode(periode)
    .dateCalcul(LocalDateTime.now())
    .build();
statistiqueRepository.save(statistique);  // ❌ Crée toujours une nouvelle entrée
```

**Problème :**
- `save()` crée une nouvelle entrée si l'ID est null
- Aucune vérification si une statistique existe déjà pour ce `type` et cette `periode`
- Chaque appel de recalcul crée de nouvelles lignes au lieu de mettre à jour les existantes
- Résultat : Duplications (plusieurs lignes avec le même type et période)

### Exemple de Duplication

```
id | type                | periode  | valeur | date_calcul
1  | TOTAL_DOSSIERS      | 2025-12  | 10     | 2025-12-04 01:00:00
2  | TOTAL_DOSSIERS      | 2025-12  | 10     | 2025-12-04 01:05:00  ← Duplication
3  | TOTAL_DOSSIERS      | 2025-12  | 12     | 2025-12-05 01:00:00  ← Duplication
```

---

## 🔧 Solutions Possibles

### Solution 1 : Supprimer les Anciennes Statistiques Avant de Créer (Recommandé)

**Logique :**
1. Supprimer toutes les statistiques de la période actuelle
2. Créer de nouvelles statistiques avec les valeurs calculées

**Avantages :**
- Simple à implémenter
- Garantit qu'il n'y a qu'une seule statistique par type/période
- Historique conservé (une statistique par période)

**Inconvénients :**
- Perd l'historique des recalculs dans la même période (mais ce n'est généralement pas nécessaire)

### Solution 2 : Mettre à Jour les Statistiques Existantes

**Logique :**
1. Vérifier si une statistique existe pour ce type et cette période
2. Si oui : mettre à jour la valeur et la date
3. Si non : créer une nouvelle statistique

**Avantages :**
- Conserve l'historique
- Plus efficace (pas de suppression)

**Inconvénients :**
- Plus complexe à implémenter
- Nécessite une requête pour chaque statistique

### Solution 3 : Contrainte UNIQUE sur (type, periode)

**Logique :**
1. Ajouter une contrainte UNIQUE sur (type, periode) dans la base de données
2. Utiliser `saveOrUpdate()` ou gérer les conflits

**Avantages :**
- Empêche les duplications au niveau base de données
- Garantit l'intégrité des données

**Inconvénients :**
- Nécessite une migration de base de données
- Gestion des erreurs de contrainte unique

---

## 📋 Solution Recommandée : Supprimer Avant de Créer

### Logique à Implémenter

**Dans `calculerEtStockerStatistiquesGlobales()` et `recalculerStatistiquesAsync()` :**

```java
String periode = YearMonth.now().toString();

// ✅ NOUVEAU : Supprimer les anciennes statistiques de la période
List<Statistique> anciennesStats = statistiqueRepository.findByPeriode(periode);
if (!anciennesStats.isEmpty()) {
    statistiqueRepository.deleteAll(anciennesStats);
    logger.debug("Suppression de {} anciennes statistiques pour la période {}", 
                 anciennesStats.size(), periode);
}

// Calculer et stocker les nouvelles statistiques
Map<String, Object> stats = getStatistiquesGlobales();
for (Map.Entry<String, Object> entry : stats.entrySet()) {
    // ... créer et sauvegarder les nouvelles statistiques
}
```

**Avantages :**
- Simple et efficace
- Garantit une seule statistique par type/période
- Pas de duplications

---

## 🔧 Script SQL pour Nettoyer les Duplications Existantes

### Option 1 : Supprimer Toutes les Duplications (Garder la Plus Récente)

```sql
-- Supprimer les duplications en gardant seulement la plus récente pour chaque (type, periode)
DELETE s1 FROM statistiques s1
INNER JOIN statistiques s2 
WHERE s1.type = s2.type 
  AND s1.periode = s2.periode
  AND s1.date_calcul < s2.date_calcul;
```

### Option 2 : Supprimer Toutes les Statistiques de la Période Actuelle

```sql
-- Supprimer toutes les statistiques de la période actuelle
DELETE FROM statistiques 
WHERE periode = DATE_FORMAT(NOW(), '%Y-%m');
```

**Puis appeler l'API `/api/statistiques/recalculer` pour recréer les statistiques.**

### Option 3 : Supprimer Toutes les Duplications (Garder la Première)

```sql
-- Supprimer les duplications en gardant seulement la première (plus ancienne)
DELETE s1 FROM statistiques s1
INNER JOIN statistiques s2 
WHERE s1.type = s2.type 
  AND s1.periode = s2.periode
  AND s1.id > s2.id;
```

---

## 📝 Instructions de Correction

### Étape 1 : Nettoyer les Duplications Existantes

**Option Recommandée : Supprimer toutes les statistiques de la période actuelle**

```sql
DELETE FROM statistiques 
WHERE periode = DATE_FORMAT(NOW(), '%Y-%m');
```

**OU**

**Garder seulement la plus récente pour chaque (type, periode) :**

```sql
DELETE s1 FROM statistiques s1
INNER JOIN statistiques s2 
WHERE s1.type = s2.type 
  AND s1.periode = s2.periode
  AND s1.date_calcul < s2.date_calcul;
```

### Étape 2 : Modifier le Code Backend

**Modifier `calculerEtStockerStatistiquesGlobales()` et `recalculerStatistiquesAsync()` pour :**
1. Supprimer les anciennes statistiques de la période avant de créer de nouvelles
2. Créer les nouvelles statistiques

### Étape 3 : Tester le Recalcul

**Via API :**
```
POST http://localhost:8089/carthage-creance/api/statistiques/recalculer
Headers: Authorization: Bearer {token}
```

**Vérifier :**
- Pas de duplications créées
- Une seule statistique par type/période
- Les valeurs sont correctes

---

## 🔍 Vérification Post-Correction

### Vérifier qu'il n'y a Plus de Duplications

**Requête SQL :**
```sql
-- Compter les duplications
SELECT 
    type,
    periode,
    COUNT(*) as nb_duplications
FROM statistiques
WHERE periode = DATE_FORMAT(NOW(), '%Y-%m')
GROUP BY type, periode
HAVING COUNT(*) > 1;
```

**Résultat attendu :** Aucune ligne (pas de duplications)

### Vérifier les Statistiques

**Requête SQL :**
```sql
SELECT 
    type,
    valeur,
    description,
    periode,
    date_calcul
FROM statistiques
WHERE periode = DATE_FORMAT(NOW(), '%Y-%m')
ORDER BY type, date_calcul DESC;
```

**Vérifications :**
- [ ] Une seule ligne par type
- [ ] Les valeurs sont correctes (pas de 0)
- [ ] Pas de duplications

---

## 📝 Résumé

**Problème :** Le code crée toujours de nouvelles entrées au lieu de mettre à jour ou supprimer les anciennes, ce qui cause des duplications.

**Solution :** 
1. Nettoyer les duplications existantes via SQL
2. Modifier le code pour supprimer les anciennes statistiques de la période avant de créer de nouvelles

**Action :** 
1. Exécuter le script SQL pour nettoyer les duplications
2. Modifier le code backend pour éviter les futures duplications
3. Tester le recalcul

