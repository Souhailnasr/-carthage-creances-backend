-- ============================================
-- Script SQL pour corriger la table audit_logs
-- Problème : Data truncated for column 'change_type'
-- ============================================

-- ÉTAPE 1 : Vérifier la structure actuelle de la table
-- Exécutez cette requête dans phpMyAdmin pour voir la structure actuelle
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM 
    INFORMATION_SCHEMA.COLUMNS
WHERE 
    TABLE_SCHEMA = 'carthage_creances'
    AND TABLE_NAME = 'audit_logs'
    AND COLUMN_NAME = 'change_type';

-- ============================================
-- ÉTAPE 2 : Corriger la colonne change_type
-- ============================================

-- Option RECOMMANDÉE : Changer en VARCHAR pour plus de flexibilité
ALTER TABLE audit_logs 
MODIFY COLUMN change_type VARCHAR(50) NOT NULL;

-- ============================================
-- ÉTAPE 3 : Vérifier la modification
-- ============================================

-- Vérifier que la colonne a bien été modifiée
DESCRIBE audit_logs;

-- OU

SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE
FROM 
    INFORMATION_SCHEMA.COLUMNS
WHERE 
    TABLE_SCHEMA = 'carthage_creances'
    AND TABLE_NAME = 'audit_logs'
    AND COLUMN_NAME = 'change_type';

-- ============================================
-- RÉSULTAT ATTENDU
-- ============================================
-- change_type | varchar(50) | NO | NULL
-- ============================================

