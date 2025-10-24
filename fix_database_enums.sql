-- Script pour corriger les enums dans la base de données
-- Exécuter ce script pour aligner la base de données avec les enums Java

-- 1. Corriger la colonne dossier_status
UPDATE dossier 
SET dossier_status = 'ENCOURSDETRAITEMENT' 
WHERE dossier_status = 'EN_COURS';

UPDATE dossier 
SET dossier_status = 'CLOTURE' 
WHERE dossier_status = 'CLOTURE';

-- 2. Corriger la colonne statut
UPDATE dossier 
SET statut = 'EN_ATTENTE_VALIDATION' 
WHERE statut = 'EN_ATTENTE_VALIDATION';

UPDATE dossier 
SET statut = 'VALIDE' 
WHERE statut = 'VALIDE';

UPDATE dossier 
SET statut = 'REJETE' 
WHERE statut = 'REJETE';

UPDATE dossier 
SET statut = 'EN_COURS' 
WHERE statut = 'EN_COURS';

UPDATE dossier 
SET statut = 'CLOTURE' 
WHERE statut = 'CLOTURE';

-- 3. Vérifier les valeurs actuelles
SELECT DISTINCT dossier_status FROM dossier;
SELECT DISTINCT statut FROM dossier;

-- 4. Si nécessaire, mettre à jour le schéma de la colonne
-- ALTER TABLE dossier MODIFY COLUMN dossier_status ENUM('ENCOURSDETRAITEMENT', 'CLOTURE');
-- ALTER TABLE dossier MODIFY COLUMN statut ENUM('EN_ATTENTE_VALIDATION', 'VALIDE', 'REJETE', 'EN_COURS', 'CLOTURE');

