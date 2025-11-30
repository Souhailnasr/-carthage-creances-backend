-- ============================================
-- Script pour supprimer la colonne en double hussier_id
-- ============================================
-- IMPORTANT : Exécutez cette commande dans l'onglet SQL de phpMyAdmin
-- ATTENTION : Gardez uniquement huissier_id (avec deux 'i'), supprimez hussier_id (avec un seul 'i')

-- Supprimer la colonne hussier_id (la mauvaise, avec une faute)
ALTER TABLE audiences 
DROP COLUMN hussier_id;

-- Vérifier que seule la colonne huissier_id existe maintenant
DESCRIBE audiences;

