-- Script SQL SIMPLE et SÉCURISÉ pour corriger dossier_utilisateurs
-- Exécutez chaque commande UNE PAR UNE dans phpMyAdmin
-- Ignorez les erreurs si elles indiquent que quelque chose n'existe pas

-- ============================================
-- ÉTAPE 1: Vérifier la structure actuelle
-- ============================================
DESCRIBE dossier_utilisateurs;

-- Notez quelles colonnes existent:
-- - Si vous voyez dossiers_id → vous devrez la supprimer
-- - Si vous voyez utilisateurs_id → vous devrez la supprimer
-- - Vous devriez voir dossier_id et utilisateur_id

-- ============================================
-- ÉTAPE 2: Supprimer les contraintes de clé étrangère
-- ============================================
-- Exécutez cette commande (ignorez l'erreur si la contrainte n'existe pas)
ALTER TABLE dossier_utilisateurs 
DROP FOREIGN KEY IF EXISTS FKxcymrrxt4dj72jnvudf8dj1s;

-- ============================================
-- ÉTAPE 3: Supprimer les colonnes redondantes
-- ============================================
-- Exécutez UNE PAR UNE et ignorez les erreurs si la colonne n'existe pas

-- Si dossiers_id existe (vérifié à l'étape 1), exécutez:
ALTER TABLE dossier_utilisateurs DROP COLUMN dossiers_id;

-- Si utilisateurs_id existe (vérifié à l'étape 1), exécutez:
ALTER TABLE dossier_utilisateurs DROP COLUMN utilisateurs_id;

-- Si vous obtenez une erreur "#1091 - Can't DROP COLUMN", c'est que la colonne n'existe pas
-- C'est normal, continuez avec l'étape suivante

-- ============================================
-- ÉTAPE 4: Ajouter la clé primaire composite
-- ============================================
-- Exécutez cette commande (ignorez l'erreur si la clé primaire existe déjà)
ALTER TABLE dossier_utilisateurs 
ADD PRIMARY KEY (dossier_id, utilisateur_id);

-- Si vous obtenez une erreur "Duplicate key name 'PRIMARY'", c'est normal, continuez

-- ============================================
-- ÉTAPE 5: Supprimer les anciennes clés étrangères (si elles existent)
-- ============================================
ALTER TABLE dossier_utilisateurs 
DROP FOREIGN KEY IF EXISTS fk_dossier_utilisateurs_dossier;

ALTER TABLE dossier_utilisateurs 
DROP FOREIGN KEY IF EXISTS fk_dossier_utilisateurs_utilisateur;

-- ============================================
-- ÉTAPE 6: Ajouter les nouvelles clés étrangères
-- ============================================
ALTER TABLE dossier_utilisateurs
ADD CONSTRAINT fk_dossier_utilisateurs_dossier 
FOREIGN KEY (dossier_id) REFERENCES dossier(id) ON DELETE CASCADE;

ALTER TABLE dossier_utilisateurs
ADD CONSTRAINT fk_dossier_utilisateurs_utilisateur 
FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE;

-- Si vous obtenez une erreur "Duplicate key name", c'est que la contrainte existe déjà
-- C'est normal, la table est déjà correctement configurée

-- ============================================
-- ÉTAPE 7: Vérifier la structure finale
-- ============================================
DESCRIBE dossier_utilisateurs;

-- La table devrait avoir uniquement 2 colonnes:
-- - dossier_id (BIGINT, NOT NULL, PRIMARY KEY)
-- - utilisateur_id (BIGINT, NOT NULL, PRIMARY KEY)

-- ============================================
-- RÉSULTAT ATTENDU
-- ============================================
-- Structure finale correcte:
-- | Field          | Type        | Null | Key | Default | Extra |
-- |----------------|-------------|------|-----|---------|-------|
-- | dossier_id     | bigint(20)   | NO   | PRI | NULL    |       |
-- | utilisateur_id | bigint(20)   | NO   | PRI | NULL    |       |

