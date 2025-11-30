-- ============================================
-- Script pour renommer la colonne hussier_id en huissier_id
-- ============================================
-- IMPORTANT : Exécutez cette commande dans l'onglet SQL de phpMyAdmin

ALTER TABLE audiences 
CHANGE COLUMN hussier_id huissier_id BIGINT NULL;

-- Après avoir exécuté cette commande, vérifiez avec :
DESCRIBE audiences;

