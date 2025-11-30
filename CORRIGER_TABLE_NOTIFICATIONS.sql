-- ============================================
-- Script pour corriger la colonne 'type' dans la table notifications
-- ============================================
-- IMPORTANT : Exécutez ces requêtes dans phpMyAdmin

-- 1. Vérifier la structure actuelle de la colonne 'type'
DESCRIBE notifications;

-- 2. Voir la définition complète de la table (pour voir l'ENUM)
SHOW CREATE TABLE notifications;

-- 3. Si la colonne 'type' est un ENUM avec des valeurs limitées, 
--    modifiez-la pour accepter toutes les valeurs de TypeNotification :
--    (Exécutez cette commande UNIQUEMENT si la colonne est un ENUM)

-- OPTION 1 : Changer l'ENUM en VARCHAR pour plus de flexibilité (RECOMMANDÉ)
ALTER TABLE notifications 
MODIFY COLUMN type VARCHAR(50) NOT NULL;

-- OPTION 2 : Si vous voulez garder un ENUM, ajoutez toutes les valeurs :
-- ALTER TABLE notifications 
-- MODIFY COLUMN type ENUM(
--     'DOSSIER_CREE',
--     'DOSSIER_VALIDE',
--     'DOSSIER_REJETE',
--     'DOSSIER_EN_ATTENTE',
--     'DOSSIER_AFFECTE',
--     'DOSSIER_CLOTURE',
--     'ENQUETE_CREE',
--     'ENQUETE_VALIDE',
--     'ENQUETE_REJETE',
--     'ENQUETE_EN_ATTENTE',
--     'ACTION_AMIABLE_CREE',
--     'ACTION_AMIABLE_COMPLETEE',
--     'AUDIENCE_PROCHAINE',
--     'AUDIENCE_CREE',
--     'AUDIENCE_REPORTEE',
--     'TACHE_URGENTE',
--     'TACHE_AFFECTEE',
--     'TACHE_COMPLETEE',
--     'TRAITEMENT_DOSSIER',
--     'RAPPEL',
--     'INFO',
--     'NOTIFICATION_MANUELLE'
-- ) NOT NULL;

-- 4. Vérifier que la modification a été appliquée
DESCRIBE notifications;

