-- Migration pour ajouter le champ etape_huissier dans la table dossier
-- Ce script doit être exécuté après le déploiement du code

-- Ajouter la colonne etape_huissier
ALTER TABLE dossier 
ADD COLUMN etape_huissier VARCHAR(50) NOT NULL DEFAULT 'EN_ATTENTE_DOCUMENTS';

-- Mettre à jour les dossiers existants avec un huissier assigné
-- Si un dossier a déjà des documents huissier, passer à EN_DOCUMENTS
UPDATE dossier d
SET d.etape_huissier = 'EN_DOCUMENTS'
WHERE d.id IN (
    SELECT DISTINCT dh.dossier_id 
    FROM documents_huissier dh
);

-- Si un dossier a déjà des actions huissier, passer à EN_ACTIONS
UPDATE dossier d
SET d.etape_huissier = 'EN_ACTIONS'
WHERE d.id IN (
    SELECT DISTINCT ah.dossier_id 
    FROM actions_huissier ah
);

-- Si un dossier a déjà des audiences, passer à EN_AUDIENCES
UPDATE dossier d
SET d.etape_huissier = 'EN_AUDIENCES'
WHERE d.id IN (
    SELECT DISTINCT a.dossier_id 
    FROM audiences a
    WHERE a.dossier_id IS NOT NULL
);

-- Vérifier les résultats
SELECT etape_huissier, COUNT(*) as nombre
FROM dossier
GROUP BY etape_huissier;

