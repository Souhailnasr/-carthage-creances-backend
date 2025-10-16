-- Script pour corriger l'ENUM dossier_status
-- Exécuter ce script dans phpMyAdmin (onglet SQL)

USE carthage_creances;

-- Corriger l'ENUM dossier_status avec les bonnes valeurs
ALTER TABLE dossier MODIFY COLUMN dossier_status ENUM(
    'EN_ATTENTE_VALIDATION', 
    'VALIDE', 
    'REJETE', 
    'EN_COURS', 
    'CLOTURE'
) DEFAULT 'EN_COURS';

-- Vérifier que la modification a été appliquée
DESCRIBE dossier;

