# 🔧 Solution - Erreur Foreign Key Constraint

## ❌ Erreur Actuelle

```
#1025 - Error on rename of '.\carthage_creances\#sql-57a0_1dc' to '.\carthage_creances\dossier_utilisateurs' 
(errno: 150 "Foreign key constraint is incorrectly formed")
```

## 🔍 Causes Possibles

Cette erreur peut avoir plusieurs causes :

1. **Types de données incompatibles** : Les types de `dossier_id` et `dossier.id` ne correspondent pas
2. **Colonne référencée n'est pas une clé primaire** : `dossier.id` ou `utilisateur.id` n'est pas PRIMARY KEY
3. **Colonnes inexistantes** : Les colonnes `dossier_id` ou `utilisateur_id` n'existent pas
4. **Données invalides** : Il y a des données dans `dossier_utilisateurs` qui référencent des IDs inexistants
5. **Index manquant** : Les colonnes référencées n'ont pas d'index

## ✅ Solution : Vérification et Correction

### ÉTAPE 1 : Vérifier la Structure des Tables

Exécutez ces requêtes pour vérifier :

```sql
-- Vérifier dossier_utilisateurs
DESCRIBE dossier_utilisateurs;

-- Vérifier dossier
DESCRIBE dossier;

-- Vérifier utilisateur
DESCRIBE utilisateur;
```

### ÉTAPE 2 : Vérifier les Types de Données

```sql
-- Vérifier que les types correspondent
SELECT 
    'dossier_utilisateurs.dossier_id' as colonne,
    COLUMN_TYPE as type
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'carthage_creances'
  AND TABLE_NAME = 'dossier_utilisateurs'
  AND COLUMN_NAME = 'dossier_id'
UNION ALL
SELECT 
    'dossier.id' as colonne,
    COLUMN_TYPE as type
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'carthage_creances'
  AND TABLE_NAME = 'dossier'
  AND COLUMN_NAME = 'id';
```

**Les types doivent être identiques** (tous `bigint(20)` ou `bigint`).

### ÉTAPE 3 : Vérifier que dossier.id et utilisateur.id sont des Clés Primaires

```sql
-- Vérifier dossier.id
SELECT CONSTRAINT_TYPE
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
WHERE TABLE_SCHEMA = 'carthage_creances'
  AND TABLE_NAME = 'dossier'
  AND CONSTRAINT_TYPE = 'PRIMARY KEY';

-- Vérifier utilisateur.id
SELECT CONSTRAINT_TYPE
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
WHERE TABLE_SCHEMA = 'carthage_creances'
  AND TABLE_NAME = 'utilisateur'
  AND CONSTRAINT_TYPE = 'PRIMARY KEY';
```

**Les deux doivent retourner 'PRIMARY KEY'**.

### ÉTAPE 4 : Nettoyer les Données Invalides (si la table n'est pas vide)

```sql
-- Supprimer les associations avec des dossiers inexistants
DELETE FROM dossier_utilisateurs
WHERE dossier_id NOT IN (SELECT id FROM dossier);

-- Supprimer les associations avec des utilisateurs inexistants
DELETE FROM dossier_utilisateurs
WHERE utilisateur_id NOT IN (SELECT id FROM utilisateur);
```

### ÉTAPE 5 : Vérifier/Créer les Colonnes si Nécessaire

Si les colonnes n'existent pas, créez-les :

```sql
-- Créer dossier_id si elle n'existe pas
ALTER TABLE dossier_utilisateurs
ADD COLUMN IF NOT EXISTS dossier_id BIGINT NOT NULL;

-- Créer utilisateur_id si elle n'existe pas
ALTER TABLE dossier_utilisateurs
ADD COLUMN IF NOT EXISTS utilisateur_id BIGINT NOT NULL;
```

### ÉTAPE 6 : S'assurer que les Types Correspondent

Si les types ne correspondent pas, modifiez-les :

```sql
-- Modifier le type de dossier_id pour correspondre à dossier.id
ALTER TABLE dossier_utilisateurs
MODIFY COLUMN dossier_id BIGINT NOT NULL;

-- Modifier le type de utilisateur_id pour correspondre à utilisateur.id
ALTER TABLE dossier_utilisateurs
MODIFY COLUMN utilisateur_id BIGINT NOT NULL;
```

### ÉTAPE 7 : Ajouter les Clés Étrangères (Après Vérifications)

Une fois toutes les vérifications passées :

```sql
-- Clé étrangère vers dossier
ALTER TABLE dossier_utilisateurs
ADD CONSTRAINT fk_dossier_utilisateurs_dossier 
FOREIGN KEY (dossier_id) REFERENCES dossier(id) ON DELETE CASCADE;

-- Clé étrangère vers utilisateur
ALTER TABLE dossier_utilisateurs
ADD CONSTRAINT fk_dossier_utilisateurs_utilisateur 
FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE;
```

## 📋 Script Complet de Correction

```sql
-- 1. Vérifier la structure
DESCRIBE dossier_utilisateurs;
DESCRIBE dossier;
DESCRIBE utilisateur;

-- 2. Nettoyer les données invalides (si nécessaire)
DELETE FROM dossier_utilisateurs
WHERE dossier_id NOT IN (SELECT id FROM dossier);

DELETE FROM dossier_utilisateurs
WHERE utilisateur_id NOT IN (SELECT id FROM utilisateur);

-- 3. S'assurer que les colonnes existent et ont le bon type
ALTER TABLE dossier_utilisateurs
MODIFY COLUMN dossier_id BIGINT NOT NULL;

ALTER TABLE dossier_utilisateurs
MODIFY COLUMN utilisateur_id BIGINT NOT NULL;

-- 4. Ajouter la clé primaire composite (si elle n'existe pas)
ALTER TABLE dossier_utilisateurs 
ADD PRIMARY KEY (dossier_id, utilisateur_id);

-- 5. Ajouter les clés étrangères
ALTER TABLE dossier_utilisateurs
ADD CONSTRAINT fk_dossier_utilisateurs_dossier 
FOREIGN KEY (dossier_id) REFERENCES dossier(id) ON DELETE CASCADE;

ALTER TABLE dossier_utilisateurs
ADD CONSTRAINT fk_dossier_utilisateurs_utilisateur 
FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE;
```

## 🆘 Solution Alternative : Recréer la Table

Si les problèmes persistent, recréez la table :

```sql
-- 1. Supprimer la table existante
DROP TABLE IF EXISTS dossier_utilisateurs;

-- 2. Recréer la table avec la bonne structure
CREATE TABLE dossier_utilisateurs (
    dossier_id BIGINT NOT NULL,
    utilisateur_id BIGINT NOT NULL,
    PRIMARY KEY (dossier_id, utilisateur_id),
    CONSTRAINT fk_dossier_utilisateurs_dossier 
        FOREIGN KEY (dossier_id) REFERENCES dossier(id) ON DELETE CASCADE,
    CONSTRAINT fk_dossier_utilisateurs_utilisateur 
        FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## ✅ Vérification Finale

```sql
-- Vérifier la structure
DESCRIBE dossier_utilisateurs;

-- Vérifier les contraintes
SELECT 
    CONSTRAINT_NAME, 
    COLUMN_NAME, 
    REFERENCED_TABLE_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = 'carthage_creances' 
  AND TABLE_NAME = 'dossier_utilisateurs';
```

