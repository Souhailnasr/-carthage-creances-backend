-- ============================================
-- Script pour corriger TOUTES les tables de notifications
-- ============================================
-- IMPORTANT : Exécutez ces requêtes UNE PAR UNE dans phpMyAdmin

-- ============================================
-- 1. VÉRIFIER LA STRUCTURE ACTUELLE
-- ============================================

-- Vérifier la table notifications
DESCRIBE notifications;
SHOW CREATE TABLE notifications;

-- Vérifier la table notifications_huissier
DESCRIBE notifications_huissier;
SHOW CREATE TABLE notifications_huissier;

-- ============================================
-- 2. CORRIGER LA TABLE notifications
-- ============================================

-- 2.1. Corriger la colonne 'type' (TypeNotification enum)
-- Changer de ENUM à VARCHAR pour accepter toutes les valeurs
ALTER TABLE notifications 
MODIFY COLUMN type VARCHAR(50) NOT NULL;

-- 2.2. Corriger la colonne 'statut' (StatutNotification enum)
-- Les valeurs possibles : NON_LUE, LUE
ALTER TABLE notifications 
MODIFY COLUMN statut VARCHAR(20) NOT NULL DEFAULT 'NON_LUE';

-- 2.3. Corriger la colonne 'entite_type' (TypeEntite enum)
-- Changer de ENUM à VARCHAR pour accepter toutes les valeurs
ALTER TABLE notifications 
MODIFY COLUMN entite_type VARCHAR(50) NULL;

-- 2.4. Vérifier les autres colonnes
-- titre : VARCHAR(255) - OK
-- message : VARCHAR(1000) - OK (peut être augmenté si nécessaire)
-- lien_action : VARCHAR(500) - OK

-- ============================================
-- 3. CORRIGER LA TABLE notifications_huissier
-- ============================================

-- 3.1. Corriger la colonne 'type' (TypeNotificationHuissier enum)
-- Les valeurs possibles : DELAY_WARNING, DELAY_EXPIRED, ACTION_PERFORMED, 
-- AMIABLE_RESPONSE_POSITIVE, AMIABLE_RESPONSE_NEGATIVE, AMOUNT_UPDATED, 
-- DOCUMENT_CREATED, STATUS_CHANGED
ALTER TABLE notifications_huissier 
MODIFY COLUMN type VARCHAR(50) NOT NULL;

-- 3.2. Corriger la colonne 'channel' (CanalNotification enum)
-- Les valeurs possibles : IN_APP, EMAIL, SMS, WEBHOOK
ALTER TABLE notifications_huissier 
MODIFY COLUMN channel VARCHAR(20) NOT NULL;

-- 3.3. Vérifier les autres colonnes
-- message : VARCHAR(2000) - OK
-- payload : TEXT - OK
-- acked : BOOLEAN - OK

-- ============================================
-- 4. VÉRIFICATION FINALE
-- ============================================

-- Vérifier la table notifications après modification
DESCRIBE notifications;

-- Vérifier la table notifications_huissier après modification
DESCRIBE notifications_huissier;

-- ============================================
-- NOTES IMPORTANTES
-- ============================================
-- 
-- Pourquoi utiliser VARCHAR au lieu d'ENUM ?
-- 1. Plus flexible : permet d'ajouter de nouvelles valeurs sans modifier la table
-- 2. Compatible avec Hibernate @Enumerated(EnumType.STRING)
-- 3. Évite les erreurs "Data truncated for column"
-- 
-- Les valeurs seront validées au niveau de l'application Java,
-- pas au niveau de la base de données.

