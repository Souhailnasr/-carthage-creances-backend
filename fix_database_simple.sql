-- Script simple pour corriger l'ENUM dossier_status
-- Copier-coller ceci dans phpMyAdmin > SQL

USE carthage_creances;

-- Supprimer et recréer la colonne dossier_status
ALTER TABLE dossier DROP COLUMN dossier_status;
ALTER TABLE dossier ADD COLUMN dossier_status ENUM('EN_ATTENTE_VALIDATION', 'VALIDE', 'REJETE', 'EN_COURS', 'CLOTURE') DEFAULT 'VALIDE';

-- Supprimer et recréer la colonne statut
ALTER TABLE dossier DROP COLUMN statut;
ALTER TABLE dossier ADD COLUMN statut ENUM('EN_ATTENTE_VALIDATION', 'VALIDE', 'REJETE', 'EN_COURS', 'CLOTURE') DEFAULT 'VALIDE';

-- Vérifier le résultat
DESCRIBE dossier;



