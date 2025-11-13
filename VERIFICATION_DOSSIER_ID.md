# Vérification de la colonne dossier_id dans la table enquette

## 🔍 Comment vérifier dans phpMyAdmin

1. **Ouvrez phpMyAdmin**
2. **Sélectionnez votre base de données**
3. **Cliquez sur la table `enquette`**
4. **Onglet "Structure"** - Cherchez la colonne `dossier_id` (avec underscore)

## 📋 Requête SQL pour vérifier la structure

```sql
-- Vérifier si la colonne dossier_id existe
SHOW COLUMNS FROM enquette LIKE 'dossier_id';

-- Ou voir toutes les colonnes de la table
DESCRIBE enquette;

-- Ou voir la structure complète
SHOW CREATE TABLE enquette;
```

## 📊 Requête SQL pour voir les valeurs de dossier_id

```sql
-- Voir toutes les enquêtes avec leur dossier_id
SELECT 
    id,
    rapport_code,
    dossier_id,
    statut,
    valide,
    date_creation
FROM enquette
ORDER BY id;

-- Voir les enquêtes avec dossier_id NULL (ne devrait plus y en avoir après les corrections)
SELECT 
    id,
    rapport_code,
    dossier_id,
    statut
FROM enquette
WHERE dossier_id IS NULL;
```

## 🔧 Si la colonne dossier_id n'existe pas

Si la colonne `dossier_id` n'existe pas dans votre table, vous devez l'ajouter :

```sql
-- Ajouter la colonne dossier_id si elle n'existe pas
ALTER TABLE enquette 
ADD COLUMN dossier_id BIGINT NOT NULL AFTER id;

-- Ajouter la contrainte de clé étrangère
ALTER TABLE enquette 
ADD CONSTRAINT fk_enquette_dossier 
FOREIGN KEY (dossier_id) REFERENCES dossier(id);

-- Créer un index pour améliorer les performances
CREATE INDEX idx_enquette_dossier_id ON enquette(dossier_id);
```

## ⚠️ ATTENTION

Si vous ajoutez la colonne avec `NOT NULL` et qu'il y a déjà des enregistrements dans la table, vous devez d'abord :

1. **Soit** supprimer les enregistrements existants sans dossier_id
2. **Soit** ajouter la colonne comme `NULL` d'abord, puis mettre à jour les valeurs, puis la rendre `NOT NULL`

```sql
-- Option 1 : Ajouter comme NULL d'abord
ALTER TABLE enquette 
ADD COLUMN dossier_id BIGINT NULL AFTER id;

-- Mettre à jour les valeurs existantes (remplacer X par un dossier_id valide)
UPDATE enquette SET dossier_id = X WHERE dossier_id IS NULL;

-- Puis rendre la colonne NOT NULL
ALTER TABLE enquette 
MODIFY COLUMN dossier_id BIGINT NOT NULL;

-- Enfin, ajouter la contrainte de clé étrangère
ALTER TABLE enquette 
ADD CONSTRAINT fk_enquette_dossier 
FOREIGN KEY (dossier_id) REFERENCES dossier(id);
```

## ✅ Vérification finale

```sql
-- Vérifier que toutes les enquêtes ont un dossier_id
SELECT 
    COUNT(*) as total_enquetes,
    COUNT(dossier_id) as enquetes_avec_dossier,
    COUNT(*) - COUNT(dossier_id) as enquetes_sans_dossier
FROM enquette;

-- Si enquetes_sans_dossier > 0, il y a un problème à corriger
```

## 📝 Notes importantes

- Le nom de la colonne dans la base de données est **`dossier_id`** (avec underscore)
- Le nom dans le code Java est **`dossierId`** (camelCase) mais c'est un champ `@Transient`
- La relation JPA utilise **`dossier_id`** comme nom de colonne via `@JoinColumn(name = "dossier_id")`
- Si vous voyez `dossier_id = NULL` dans certaines lignes, c'est le problème qu'on a résolu avec les requêtes natives

