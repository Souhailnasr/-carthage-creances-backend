-- Script pour supprimer complètement la table audit_logs
-- ATTENTION: Cette opération est irréversible !

-- Étape 1: Désactiver temporairement les vérifications de clés étrangères
SET FOREIGN_KEY_CHECKS = 0;

-- Étape 2: Supprimer toutes les contraintes de clé étrangère liées à audit_logs
-- (Récupérer les noms des contraintes si nécessaire)
-- SELECT CONSTRAINT_NAME 
-- FROM information_schema.KEY_COLUMN_USAGE 
-- WHERE TABLE_SCHEMA = 'carthage_creances' 
-- AND TABLE_NAME = 'audit_logs' 
-- AND REFERENCED_TABLE_NAME IS NOT NULL;

-- Supprimer les contraintes de clé étrangère (remplacer FK_NAME par le nom réel)
-- ALTER TABLE audit_logs DROP FOREIGN KEY FK_NAME;

-- Étape 3: Supprimer tous les index sur audit_logs
DROP INDEX IF EXISTS idx_audit_date ON audit_logs;
DROP INDEX IF EXISTS idx_audit_utilisateur ON audit_logs;
DROP INDEX IF EXISTS idx_audit_entite ON audit_logs;

-- Étape 4: Supprimer la table audit_logs
DROP TABLE IF EXISTS audit_logs;

-- Étape 5: Réactiver les vérifications de clés étrangères
SET FOREIGN_KEY_CHECKS = 1;

-- Vérification
SELECT 'Table audit_logs supprimée avec succès' AS 'Info';

