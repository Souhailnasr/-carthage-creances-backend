-- Script SQL SÉCURISÉ pour corriger dossier_utilisateurs
-- Vérifie d'abord l'existence des colonnes avant de les supprimer

-- ============================================
-- ÉTAPE 1: Vérifier la structure actuelle de la table
-- ============================================
-- Exécutez d'abord cette requête pour voir toutes les colonnes:
DESCRIBE dossier_utilisateurs;

-- ============================================
-- ÉTAPE 2: Identifier toutes les contraintes de clé étrangère
-- ============================================
-- Exécutez cette requête pour voir toutes les contraintes:
SELECT 
    CONSTRAINT_NAME, 
    COLUMN_NAME, 
    REFERENCED_TABLE_NAME, 
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = 'carthage_creances' 
  AND TABLE_NAME = 'dossier_utilisateurs';

-- ============================================
-- ÉTAPE 3: Supprimer les contraintes de clé étrangère (si elles existent)
-- ============================================
-- Supprimer la contrainte identifiée dans l'erreur précédente
ALTER TABLE dossier_utilisateurs 
DROP FOREIGN KEY IF EXISTS FKxcymrrxt4dj72jnvudf8dj1s;

-- Supprimer toutes les autres contraintes possibles
-- (Remplacez [NOM_CONTRAINTE] par les noms réels trouvés à l'étape 2)
-- ALTER TABLE dossier_utilisateurs DROP FOREIGN KEY IF EXISTS [NOM_CONTRAINTE];

-- ============================================
-- ÉTAPE 4: Vérifier quelles colonnes existent réellement
-- ============================================
-- Exécutez cette requête pour voir les colonnes existantes:
SELECT COLUMN_NAME 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'carthage_creances' 
  AND TABLE_NAME = 'dossier_utilisateurs';

-- ============================================
-- ÉTAPE 5: Supprimer les colonnes redondantes (seulement si elles existent)
-- ============================================
-- MySQL ne supporte pas IF EXISTS pour DROP COLUMN, donc on doit vérifier manuellement
-- Si la requête de l'étape 4 montre que dossiers_id existe, exécutez:
-- ALTER TABLE dossier_utilisateurs DROP COLUMN dossiers_id;

-- Si la requête de l'étape 4 montre que utilisateurs_id existe, exécutez:
-- ALTER TABLE dossier_utilisateurs DROP COLUMN utilisateurs_id;

-- ============================================
-- ÉTAPE 6: Supprimer les anciennes clés étrangères (si elles existent)
-- ============================================
ALTER TABLE dossier_utilisateurs 
DROP FOREIGN KEY IF EXISTS fk_dossier_utilisateurs_dossier;

ALTER TABLE dossier_utilisateurs 
DROP FOREIGN KEY IF EXISTS fk_dossier_utilisateurs_utilisateur;

-- ============================================
-- ÉTAPE 7: Ajouter la clé primaire composite (si elle n'existe pas)
-- ============================================
-- Vérifier d'abord si une clé primaire existe:
SELECT CONSTRAINT_NAME 
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
WHERE TABLE_SCHEMA = 'carthage_creances' 
  AND TABLE_NAME = 'dossier_utilisateurs' 
  AND CONSTRAINT_TYPE = 'PRIMARY KEY';

-- Si aucune clé primaire n'existe, exécutez:
ALTER TABLE dossier_utilisateurs 
ADD PRIMARY KEY (dossier_id, utilisateur_id);

-- ============================================
-- ÉTAPE 8: Ajouter les nouvelles clés étrangères
-- ============================================
ALTER TABLE dossier_utilisateurs
ADD CONSTRAINT fk_dossier_utilisateurs_dossier 
FOREIGN KEY (dossier_id) REFERENCES dossier(id) ON DELETE CASCADE;

ALTER TABLE dossier_utilisateurs
ADD CONSTRAINT fk_dossier_utilisateurs_utilisateur 
FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE;

-- ============================================
-- ÉTAPE 9: Vérifier la structure finale
-- ============================================
DESCRIBE dossier_utilisateurs;

-- La table devrait avoir uniquement:
-- - dossier_id (BIGINT, NOT NULL, PRIMARY KEY)
-- - utilisateur_id (BIGINT, NOT NULL, PRIMARY KEY)

