# Explication de la Logique de Performance

## Vue d'ensemble

Le système de performance évalue chaque agent sur une période donnée (mois, trimestre, etc.) en analysant toutes ses activités dans le système. Le calcul produit un **score sur 100 points** et un **taux de réussite en pourcentage**.

---

## 📊 Structure des Données de Performance

Chaque performance contient :
- **Agent** : L'agent évalué
- **Période** : La période d'évaluation (ex: "2024-01" pour janvier 2024, "2024-Q1" pour le 1er trimestre)
- **Dossiers traités** : Nombre total de dossiers gérés par l'agent
- **Dossiers validés** : Nombre de dossiers qui ont été validés avec succès
- **Enquêtes complétées** : Nombre d'enquêtes réalisées
- **Score** : Score global sur 100 points
- **Taux de réussite** : Pourcentage de dossiers validés par rapport aux dossiers traités
- **Date de calcul** : Quand la performance a été calculée

---

## 🔄 Processus de Calcul en 3 Étapes

### Étape 1 : Collecte des Statistiques

Le système collecte toutes les données de l'agent pour la période :

#### 1.1 Dossiers Traités
- **Source** : Tous les dossiers où l'agent est soit :
  - Le créateur (agentCreateur)
  - L'agent responsable (agentResponsable)
- **Calcul** : Compte unique (un dossier compté une seule fois même si l'agent est à la fois créateur et responsable)
- **Résultat** : Nombre total de dossiers gérés par l'agent

#### 1.2 Dossiers Validés
- **Source** : Parmi les dossiers traités, ceux qui ont le statut "VALIDE"
- **Calcul** : Compte uniquement les dossiers avec statut = VALIDE
- **Résultat** : Nombre de dossiers validés avec succès

#### 1.3 Enquêtes Complétées
- **Source** : Toutes les enquêtes où l'agent est soit :
  - Le créateur de l'enquête
  - L'agent responsable de l'enquête
- **Calcul** : Compte unique (une enquête comptée une seule fois)
- **Résultat** : Nombre total d'enquêtes gérées par l'agent

#### 1.4 Tâches Complétées
- **Source** : Toutes les tâches assignées à l'agent
- **Calcul** : Compte uniquement les tâches avec statut = TERMINEE
- **Résultat** : Nombre de tâches complétées (utilisé dans le calcul du score)

---

### Étape 2 : Calcul du Score (sur 100 points)

Le score est calculé en additionnant 5 composantes avec des poids différents :

#### Composante 1 : Qualité des Dossiers (30 points)
- **Formule** : (Nombre de dossiers validés / Nombre de dossiers traités) × 30
- **Exemple** : 
  - Si un agent a traité 10 dossiers et 8 sont validés
  - Taux = 8/10 = 0.8 (80%)
  - Score = 0.8 × 30 = **24 points**
- **Logique** : Mesure la qualité du travail (pas seulement la quantité)

#### Composante 2 : Enquêtes Complétées (20 points maximum)
- **Formule** : Nombre d'enquêtes × 1.5, plafonné à 20 points
- **Exemple** :
  - 5 enquêtes = 5 × 1.5 = 7.5 points
  - 15 enquêtes = 15 × 1.5 = 22.5 → plafonné à **20 points**
- **Logique** : Récompense l'activité d'enquête, avec un plafond pour éviter les abus

#### Composante 3 : Taux de Complétion des Tâches (20 points)
- **Formule** : (Nombre de tâches complétées / Nombre total de tâches) × 20
- **Exemple** :
  - Si un agent a 10 tâches et en a complété 7
  - Taux = 7/10 = 0.7 (70%)
  - Score = 0.7 × 20 = **14 points**
- **Logique** : Mesure la fiabilité et la ponctualité dans l'exécution des tâches

#### Composante 4 : Actions Amiables (15 points maximum)
- **Formule** : Nombre total d'actions amiables × 0.5, plafonné à 15 points
- **Source** : Toutes les actions créées dans les dossiers dont l'agent est responsable
- **Exemple** :
  - 20 actions = 20 × 0.5 = 10 points
  - 40 actions = 40 × 0.5 = 20 → plafonné à **15 points**
- **Logique** : Récompense l'activité de recouvrement amiable

#### Composante 5 : Audiences Gérées (15 points maximum)
- **Formule** : Nombre total d'audiences × 0.5, plafonné à 15 points
- **Source** : Toutes les audiences créées dans les dossiers dont l'agent est responsable
- **Exemple** :
  - 10 audiences = 10 × 0.5 = 5 points
  - 35 audiences = 35 × 0.5 = 17.5 → plafonné à **15 points**
- **Logique** : Récompense l'activité de recouvrement juridique

#### Score Final
- **Somme** : Addition de toutes les composantes
- **Plafond** : Le score est limité à 100 points maximum
- **Exemple complet** :
  - Qualité dossiers : 24 points
  - Enquêtes : 7.5 points
  - Tâches : 14 points
  - Actions : 10 points
  - Audiences : 5 points
  - **Total = 60.5 points**

---

### Étape 3 : Calcul du Taux de Réussite

- **Formule** : (Nombre de dossiers validés / Nombre de dossiers traités) × 100
- **Exemple** :
  - 8 dossiers validés sur 10 traités
  - Taux = (8/10) × 100 = **80%**
- **Cas particulier** : Si aucun dossier n'est traité, le taux est 0%
- **Plafond** : Le taux est limité à 100% maximum

---

## 🎯 Exemple Concret de Calcul

### Scénario : Agent "Ahmed" pour la période "2024-01"

#### Données collectées :
- **Dossiers créés** : 5
- **Dossiers assignés** : 8
- **Dossiers uniques** : 10 (certains sont à la fois créés et assignés)
- **Dossiers validés** : 7
- **Enquêtes complétées** : 4
- **Tâches totales** : 12
- **Tâches complétées** : 9
- **Actions amiables** : 15 (dans les dossiers dont il est responsable)
- **Audiences** : 6 (dans les dossiers dont il est responsable)

#### Calcul du score :

1. **Qualité des dossiers** :
   - Taux = 7/10 = 0.7
   - Points = 0.7 × 30 = **21 points**

2. **Enquêtes** :
   - Points = 4 × 1.5 = **6 points**

3. **Tâches** :
   - Taux = 9/12 = 0.75
   - Points = 0.75 × 20 = **15 points**

4. **Actions amiables** :
   - Points = 15 × 0.5 = 7.5 → **7.5 points**

5. **Audiences** :
   - Points = 6 × 0.5 = **3 points**

**Score total** : 21 + 6 + 15 + 7.5 + 3 = **52.5 points sur 100**

#### Calcul du taux de réussite :
- Taux = (7/10) × 100 = **70%**

---

## 📈 Visualisation des Poids

```
Score Total (100 points)
│
├── Qualité Dossiers ──────────── 30% (30 points)
│   └── Mesure la qualité du travail
│
├── Enquêtes ──────────────────── 20% (20 points max)
│   └── Récompense l'activité d'enquête
│
├── Tâches ────────────────────── 20% (20 points)
│   └── Mesure la fiabilité et ponctualité
│
├── Actions Amiables ──────────── 15% (15 points max)
│   └── Récompense l'activité de recouvrement amiable
│
└── Audiences ──────────────────── 15% (15 points max)
    └── Récompense l'activité de recouvrement juridique
```

---

## 🔍 Points Importants à Comprendre

### 1. Période de Calcul
- La performance est calculée **pour une période spécifique** (mois, trimestre, etc.)
- Chaque période a sa propre performance
- Un agent peut avoir plusieurs performances (une par période)

### 2. Données en Temps Réel
- Les statistiques sont calculées **à partir des données réelles** de la base de données
- Pas de données statiques ou pré-calculées
- Le calcul se fait au moment de la demande

### 3. Dossiers Uniques
- Un dossier n'est compté qu'**une seule fois** même si l'agent est à la fois créateur et responsable
- Évite la double comptabilisation

### 4. Plafonds et Limites
- Certaines composantes ont des **plafonds** pour éviter que certains agents ne dominent grâce à une seule activité
- Le score total est **plafonné à 100 points**
- Le taux de réussite est **plafonné à 100%**

### 5. Gestion des Cas Limites
- Si un agent n'a traité aucun dossier : taux de réussite = 0%
- Si un agent n'a aucune tâche : la composante "tâches" = 0 points
- Si un agent n'a aucune action/audience : ces composantes = 0 points

---

## 🎓 Philosophie du Calcul

### Qualité vs Quantité
- Le système privilégie la **qualité** (30% pour la validation des dossiers)
- Mais récompense aussi la **quantité** d'activité (enquêtes, actions, audiences)

### Diversité des Activités
- Le score encourage la **diversité** : un agent performant dans plusieurs domaines aura un meilleur score
- Un agent qui excelle dans un seul domaine ne peut pas dépasser le plafond de ce domaine

### Équilibre
- Les poids sont équilibrés pour refléter l'importance relative de chaque activité
- La validation des dossiers (30%) est la plus importante car c'est le résultat final
- Les autres activités (enquêtes, tâches, actions, audiences) sont des moyens d'atteindre ce résultat

---

## 📊 Utilisation des Performances

### Pour les Agents
- Voir leur propre performance
- Comprendre leurs points forts et faibles
- Suivre leur évolution dans le temps

### Pour les Chefs
- Voir les performances de **tous leurs agents**
- Identifier les agents performants
- Identifier les agents qui ont besoin d'aide
- Comparer les agents entre eux

### Pour le Super Admin
- Voir les performances de **tous les agents et chefs**
- Vue d'ensemble de la performance de l'organisation
- Identifier les tendances et problèmes

---

## 🔄 Mise à Jour des Performances

### Calcul Manuel
- Un chef ou le super admin peut déclencher le calcul pour une période
- Utile pour recalculer après des corrections de données

### Calcul Automatique
- Le système peut calculer automatiquement les performances
- Peut être déclenché périodiquement (mensuel, trimestriel)

### Données Dynamiques
- Les performances sont calculées à partir des données actuelles
- Si un dossier est validé après le calcul, il faut recalculer pour voir l'impact

---

## 💡 Exemples de Scénarios

### Scénario 1 : Agent Très Performant
- **Dossiers** : 20 traités, 18 validés (90%) → 27 points
- **Enquêtes** : 10 → 15 points (plafonné)
- **Tâches** : 15/15 complétées (100%) → 20 points
- **Actions** : 30 → 15 points (plafonné)
- **Audiences** : 20 → 15 points (plafonné)
- **Score total** : **92 points** (excellent)

### Scénario 2 : Agent Moyen
- **Dossiers** : 10 traités, 6 validés (60%) → 18 points
- **Enquêtes** : 3 → 4.5 points
- **Tâches** : 8/12 complétées (67%) → 13.4 points
- **Actions** : 10 → 5 points
- **Audiences** : 4 → 2 points
- **Score total** : **42.9 points** (moyen)

### Scénario 3 : Agent avec Problèmes
- **Dossiers** : 15 traités, 3 validés (20%) → 6 points
- **Enquêtes** : 1 → 1.5 points
- **Tâches** : 5/20 complétées (25%) → 5 points
- **Actions** : 2 → 1 point
- **Audiences** : 1 → 0.5 points
- **Score total** : **14 points** (faible, nécessite une intervention)

---

## 🎯 Conclusion

Le système de performance est conçu pour :
1. **Évaluer objectivement** chaque agent sur plusieurs critères
2. **Encourager la qualité** tout en récompensant l'activité
3. **Fournir des données exploitables** aux chefs et au super admin
4. **Permettre le suivi** de l'évolution dans le temps

Le calcul est **transparent**, **équitable** et **basé sur des données réelles** du système.

