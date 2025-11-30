-- ============================================
-- Script pour vérifier la structure de la table notifications
-- ============================================
-- IMPORTANT : Exécutez ces requêtes dans phpMyAdmin

-- 1. Vérifier la structure de la table notifications
DESCRIBE notifications;

-- 2. Voir la définition complète de la table (pour voir l'ENUM)
SHOW CREATE TABLE notifications;

-- 3. Vérifier les valeurs actuelles dans la colonne type
SELECT DISTINCT type FROM notifications;

