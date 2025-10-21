-- Script de nettoyage et test pour les utilisateurs
-- ATTENTION: Ce script supprime TOUS les utilisateurs existants

-- 1. Supprimer tous les tokens existants
DELETE FROM token;

-- 2. Supprimer tous les utilisateurs existants
DELETE FROM utilisateur;

-- 3. Réinitialiser l'auto-increment
ALTER TABLE utilisateur AUTO_INCREMENT = 1;
ALTER TABLE token AUTO_INCREMENT = 1;

-- 4. Vérifier que les tables sont vides
SELECT 'Utilisateurs restants:' as info, COUNT(*) as count FROM utilisateur;
SELECT 'Tokens restants:' as info, COUNT(*) as count FROM token;

-- 5. Instructions pour tester avec Postman/curl
/*
TEST AVEC POSTMAN/CURL:

1. Créer un utilisateur avec le bon endpoint:
POST http://localhost:8089/carthage-creance/auth/register
Content-Type: application/json

{
  "firstName": "Admin",
  "lastName": "System",
  "email": "admin@carthage.com",
  "password": "admin123",
  "role": "SUPER_ADMIN"
}

2. Ou utiliser l'autre endpoint:
POST http://localhost:8089/carthage-creance/api/users
Content-Type: application/json

{
  "nom": "Admin",
  "prenom": "System", 
  "email": "admin2@carthage.com",
  "motDePasse": "admin123",
  "roleUtilisateur": "SUPER_ADMIN"
}

3. Tester l'authentification:
POST http://localhost:8089/carthage-creance/auth/authenticate
Content-Type: application/json

{
  "email": "admin@carthage.com",
  "password": "admin123"
}
*/

