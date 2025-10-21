-- Script pour corriger les énumérations dans la base de données
-- Exécuter ce script dans phpMyAdmin (onglet SQL)

USE carthage_creances;

-- 1. Vérifier la structure actuelle de la table dossier
DESCRIBE dossier;

-- 2. Corriger l'ENUM statut avec les bonnes valeurs
ALTER TABLE dossier MODIFY COLUMN statut ENUM(
    'EN_ATTENTE_VALIDATION', 
    'VALIDE', 
    'REJETE', 
    'EN_COURS', 
    'CLOTURE'
) DEFAULT 'EN_ATTENTE_VALIDATION';

-- 3. Corriger l'ENUM dossier_status avec les bonnes valeurs
ALTER TABLE dossier MODIFY COLUMN dossier_status ENUM(
    'ENCOURSDETRAITEMENT',
    'CLOTURE'
) DEFAULT 'ENCOURSDETRAITEMENT';

-- 4. Vérifier que les modifications ont été appliquées
DESCRIBE dossier;

-- 5. Vérifier les valeurs existantes
SELECT DISTINCT statut FROM dossier;
SELECT DISTINCT dossier_status FROM dossier;
