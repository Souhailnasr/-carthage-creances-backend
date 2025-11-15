-- Script pour corriger les valeurs d'enum dans la base de données
-- Ce script vérifie et corrige les valeurs invalides dans les colonnes d'enum

USE carthage_creances;

-- Vérifier les valeurs actuelles dans la table dossiers
SELECT DISTINCT dossier_status FROM dossiers;

-- Vérifier les valeurs actuelles dans la table utilisateurs
SELECT DISTINCT role_Utilisateur FROM utilisateurs;

-- Corriger les valeurs d'enum invalides dans la table dossiers
-- Remplacer les valeurs invalides par des valeurs valides
UPDATE dossiers 
SET dossier_status = 'ENCOURSDETRAITEMENT' 
WHERE dossier_status NOT IN ('ENCOURSDETRAITEMENT', 'EN_ATTENTE_VALIDATION', 'VALIDE', 'REJETE', 'CLOTURE');

-- Corriger les valeurs d'enum invalides dans la table utilisateurs
UPDATE utilisateurs 
SET role_Utilisateur = 'AGENT' 
WHERE role_Utilisateur NOT IN ('AGENT', 'CHEF_DEPARTEMENT_DOSSIER', 'SUPER_ADMIN', 'AVOCAT', 'HUISSIER');

-- Vérifier les corrections
SELECT DISTINCT dossier_status FROM dossiers;
SELECT DISTINCT role_Utilisateur FROM utilisateurs;

-- Afficher le nombre de dossiers par statut
SELECT dossier_status, COUNT(*) as nombre_dossiers 
FROM dossiers 
GROUP BY dossier_status;

-- Afficher le nombre d'utilisateurs par rôle
SELECT role_Utilisateur, COUNT(*) as nombre_utilisateurs 
FROM utilisateurs 
GROUP BY role_Utilisateur;















