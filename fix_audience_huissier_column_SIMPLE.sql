-- ============================================
-- Script SQL SIMPLIFIÉ - Exécutez UNE requête à la fois
-- ============================================

-- ÉTAPE 1 : Vérifier la structure de la table (MÉTHODE LA PLUS SIMPLE)
-- Copiez et exécutez cette requête dans phpMyAdmin :
DESCRIBE audiences;

-- ÉTAPE 2 : Si vous voyez "hussier_id" (avec faute), exécutez cette commande pour la renommer :
-- ALTER TABLE audiences CHANGE COLUMN hussier_id huissier_id BIGINT NULL;

-- ÉTAPE 3 : Vérifier les données existantes
SELECT id, dossier_id, avocat_id, huissier_id, date_audience FROM audiences LIMIT 10;

-- ÉTAPE 4 : Voir la structure complète de la table
SHOW CREATE TABLE audiences;

