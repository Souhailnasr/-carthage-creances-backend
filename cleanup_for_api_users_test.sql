-- Script de nettoyage pour tester l'endpoint /api/users
-- ATTENTION: Ce script supprime TOUS les utilisateurs et tokens existants

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

-- 5. Instructions pour tester l'endpoint /api/users
/*
TEST AVEC L'ENDPOINT /api/users:

POST http://localhost:8089/carthage-creance/api/users
Content-Type: application/json

{
  "nom": "Admin",
  "prenom": "System",
  "email": "admin@carthage.com",
  "motDePasse": "admin123",
  "roleUtilisateur": "SUPER_ADMIN"
}

RÉPONSE ATTENDUE:
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "errors": null
}

VÉRIFICATIONS EN BASE:
1. Le mot de passe doit être crypté (ex: $2a$10$...)
2. Un token doit être créé dans la table token
3. L'utilisateur doit être créé avec le bon rôle
*/

