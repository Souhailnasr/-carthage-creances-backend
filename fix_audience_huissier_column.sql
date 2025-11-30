-- Script SQL pour vérifier et corriger la colonne huissier_id dans la table audiences
-- IMPORTANT : Exécutez ces requêtes UNE PAR UNE dans phpMyAdmin

-- ============================================
-- 1. Vérifier la structure actuelle de la table audiences
-- ============================================
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'audiences'
AND TABLE_SCHEMA = 'carthage_creances'
AND (COLUMN_NAME LIKE '%huissier%' OR COLUMN_NAME LIKE '%hussier%');

-- ============================================
-- 2. Vérifier toutes les colonnes de la table audiences
-- ============================================
DESCRIBE audiences;

-- ou

SHOW COLUMNS FROM audiences;

-- ============================================
-- 3. Si la colonne s'appelle "hussier_id" (avec faute), la renommer
-- ATTENTION : Exécutez cette commande UNIQUEMENT si la colonne s'appelle "hussier_id"
-- ============================================
-- ALTER TABLE audiences CHANGE COLUMN hussier_id huissier_id BIGINT NULL;

-- ============================================
-- 4. Vérifier les contraintes de clé étrangère
-- ============================================
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_NAME = 'audiences'
AND TABLE_SCHEMA = 'carthage_creances'
AND (COLUMN_NAME LIKE '%huissier%' OR COLUMN_NAME LIKE '%hussier%');

-- ============================================
-- 5. Si nécessaire, ajouter la contrainte de clé étrangère
-- ATTENTION : Exécutez cette commande UNIQUEMENT si la contrainte n'existe pas
-- ============================================
-- ALTER TABLE audiences 
-- ADD CONSTRAINT fk_audience_huissier 
-- FOREIGN KEY (huissier_id) REFERENCES huissiers(id) ON DELETE SET NULL;

-- ============================================
-- 6. Vérifier les données existantes
-- ============================================
SELECT 
    id,
    dossier_id,
    avocat_id,
    huissier_id,
    date_audience
FROM audiences
LIMIT 10;

-- ============================================
-- 7. Vérifier si la colonne huissier_id existe et sa structure
-- ============================================
SHOW CREATE TABLE audiences;

