-- ============================================
-- Script de création d'un SuperAdmin
-- ============================================
-- Ce script crée un utilisateur SUPER_ADMIN dans la base de données
-- 
-- IMPORTANT: Le mot de passe doit être encodé en BCrypt
-- Pour "admin123", le hash BCrypt est: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- 
-- Si vous voulez un autre mot de passe, vous devez le hasher avec BCrypt
-- ============================================

USE carthage_creances;

-- Vérifier si l'utilisateur existe déjà
SELECT 'Vérification utilisateur existant...' as info;
SELECT * FROM utilisateur WHERE email = 'admin@carthage.com';

-- Supprimer l'utilisateur s'il existe (pour réinitialisation)
DELETE FROM token WHERE utilisateur_id IN (SELECT id FROM utilisateur WHERE email = 'admin@carthage.com');
DELETE FROM utilisateur WHERE email = 'admin@carthage.com';

-- Créer le SuperAdmin
-- Mot de passe: admin123 (hash BCrypt: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy)
INSERT INTO utilisateur (
    nom,
    prenom,
    email,
    mot_de_passe,
    role_utilisateur,
    actif,
    date_creation
) VALUES (
    'Admin',
    'System',
    'admin@carthage.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- Mot de passe: admin123
    'SUPER_ADMIN',
    true,
    NOW()
);

-- Vérifier la création
SELECT 'SuperAdmin créé avec succès!' as info;
SELECT 
    id,
    nom,
    prenom,
    email,
    role_utilisateur,
    actif,
    date_creation
FROM utilisateur 
WHERE email = 'admin@carthage.com';

-- ============================================
-- INFORMATIONS DE CONNEXION
-- ============================================
-- Email: admin@carthage.com
-- Mot de passe: admin123
-- Rôle: SUPER_ADMIN
-- ============================================


