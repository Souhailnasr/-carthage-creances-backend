-- Script de VÉRIFICATION et CORRECTION pour dossier_utilisateurs
-- Erreur: Foreign key constraint is incorrectly formed

-- ============================================
-- ÉTAPE 1: Vérifier la structure de la table dossier_utilisateurs
-- ============================================
DESCRIBE dossier_utilisateurs;

-- ============================================
-- ÉTAPE 2: Vérifier la structure de la table dossier
-- ============================================
DESCRIBE dossier;

-- Vérifier que la colonne id existe et est de type BIGINT
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_KEY
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'carthage_creances'
  AND TABLE_NAME = 'dossier'
  AND COLUMN_NAME = 'id';

-- ============================================
-- ÉTAPE 3: Vérifier la structure de la table utilisateur
-- ============================================
DESCRIBE utilisateur;

-- Vérifier que la colonne id existe et est de type BIGINT
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_KEY
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'carthage_creances'
  AND TABLE_NAME = 'utilisateur'
  AND COLUMN_NAME = 'id';

-- ============================================
-- ÉTAPE 4: Vérifier que dossier.id est une clé primaire
-- ============================================
SELECT CONSTRAINT_NAME, CONSTRAINT_TYPE
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
WHERE TABLE_SCHEMA = 'carthage_creances'
  AND TABLE_NAME = 'dossier'
  AND CONSTRAINT_TYPE = 'PRIMARY KEY';

-- ============================================
-- ÉTAPE 5: Vérifier que utilisateur.id est une clé primaire
-- ============================================
SELECT CONSTRAINT_NAME, CONSTRAINT_TYPE
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
WHERE TABLE_SCHEMA = 'carthage_creances'
  AND TABLE_NAME = 'utilisateur'
  AND CONSTRAINT_TYPE = 'PRIMARY KEY';

-- ============================================
-- ÉTAPE 6: Vérifier les types de données correspondent
-- ============================================
SELECT 
    'dossier_utilisateurs.dossier_id' as colonne_source,
    COLUMN_TYPE as type_source
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'carthage_creances'
  AND TABLE_NAME = 'dossier_utilisateurs'
  AND COLUMN_NAME = 'dossier_id'
UNION ALL
SELECT 
    'dossier.id' as colonne_source,
    COLUMN_TYPE as type_source
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'carthage_creances'
  AND TABLE_NAME = 'dossier'
  AND COLUMN_NAME = 'id'
UNION ALL
SELECT 
    'dossier_utilisateurs.utilisateur_id' as colonne_source,
    COLUMN_TYPE as type_source
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'carthage_creances'
  AND TABLE_NAME = 'dossier_utilisateurs'
  AND COLUMN_NAME = 'utilisateur_id'
UNION ALL
SELECT 
    'utilisateur.id' as colonne_source,
    COLUMN_TYPE as type_source
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'carthage_creances'
  AND TABLE_NAME = 'utilisateur'
  AND COLUMN_NAME = 'id';

-- Les types doivent correspondre (tous BIGINT ou BIGINT(20))

-- ============================================
-- ÉTAPE 7: Vérifier s'il y a des données qui violent la contrainte
-- ============================================
-- Vérifier si des dossier_id dans dossier_utilisateurs n'existent pas dans dossier
SELECT du.dossier_id
FROM dossier_utilisateurs du
LEFT JOIN dossier d ON du.dossier_id = d.id
WHERE d.id IS NULL;

-- Vérifier si des utilisateur_id dans dossier_utilisateurs n'existent pas dans utilisateur
SELECT du.utilisateur_id
FROM dossier_utilisateurs du
LEFT JOIN utilisateur u ON du.utilisateur_id = u.id
WHERE u.id IS NULL;

-- Si ces requêtes retournent des résultats, il faut nettoyer les données avant d'ajouter les contraintes

-- ============================================
-- ÉTAPE 8: Vérifier que les colonnes dossier_id et utilisateur_id existent
-- ============================================
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'carthage_creances'
  AND TABLE_NAME = 'dossier_utilisateurs'
  AND COLUMN_NAME IN ('dossier_id', 'utilisateur_id');

-- Les deux colonnes doivent exister et être NOT NULL

