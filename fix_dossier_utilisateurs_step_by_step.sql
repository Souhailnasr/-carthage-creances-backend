-- Script SQL ÉTAPE PAR ÉTAPE pour corriger dossier_utilisateurs
-- Exécutez chaque section séparément dans phpMyAdmin

-- ============================================
-- ÉTAPE 1: Identifier toutes les contraintes
-- ============================================
-- Exécutez d'abord cette requête pour voir toutes les contraintes:
SELECT 
    CONSTRAINT_NAME, 
    COLUMN_NAME, 
    REFERENCED_TABLE_NAME, 
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = 'carthage_creances' 
  AND TABLE_NAME = 'dossier_utilisateurs'
  AND COLUMN_NAME IN ('dossiers_id', 'utilisateurs_id', 'dossier_id', 'utilisateur_id');

-- ============================================
-- ÉTAPE 2: Supprimer la contrainte identifiée dans l'erreur
-- ============================================
ALTER TABLE dossier_utilisateurs 
DROP FOREIGN KEY FKxcymrrxt4dj72jnvudf8dj1s;

-- ============================================
-- ÉTAPE 3: Supprimer toutes les autres contraintes trouvées à l'étape 1
-- ============================================
-- Remplacez [NOM_CONTRAINTE] par les noms réels trouvés à l'étape 1
-- Exemple:
-- ALTER TABLE dossier_utilisateurs DROP FOREIGN KEY [NOM_CONTRAINTE];

-- ============================================
-- ÉTAPE 4: Supprimer les colonnes redondantes
-- ============================================
ALTER TABLE dossier_utilisateurs DROP COLUMN dossiers_id;
ALTER TABLE dossier_utilisateurs DROP COLUMN utilisateurs_id;

-- ============================================
-- ÉTAPE 5: Supprimer les anciennes clés étrangères (si elles existent)
-- ============================================
-- Ces commandes peuvent échouer si les contraintes n'existent pas, c'est normal
ALTER TABLE dossier_utilisateurs 
DROP FOREIGN KEY IF EXISTS fk_dossier_utilisateurs_dossier;

ALTER TABLE dossier_utilisateurs 
DROP FOREIGN KEY IF EXISTS fk_dossier_utilisateurs_utilisateur;

-- ============================================
-- ÉTAPE 6: Ajouter la clé primaire composite
-- ============================================
-- Cette commande peut échouer si la clé primaire existe déjà, c'est normal
ALTER TABLE dossier_utilisateurs 
ADD PRIMARY KEY (dossier_id, utilisateur_id);

-- ============================================
-- ÉTAPE 7: Ajouter les nouvelles clés étrangères
-- ============================================
ALTER TABLE dossier_utilisateurs
ADD CONSTRAINT fk_dossier_utilisateurs_dossier 
FOREIGN KEY (dossier_id) REFERENCES dossier(id) ON DELETE CASCADE;

ALTER TABLE dossier_utilisateurs
ADD CONSTRAINT fk_dossier_utilisateurs_utilisateur 
FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE;

-- ============================================
-- ÉTAPE 8: Vérifier la structure finale
-- ============================================
DESCRIBE dossier_utilisateurs;

-- La table devrait avoir uniquement:
-- - dossier_id (BIGINT, NOT NULL, PRIMARY KEY)
-- - utilisateur_id (BIGINT, NOT NULL, PRIMARY KEY)

