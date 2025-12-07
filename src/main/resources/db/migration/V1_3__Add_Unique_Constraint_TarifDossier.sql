-- Migration pour ajouter la contrainte d'unicité (audience_id, categorie) dans tarif_dossier
-- Version: 1.3
-- Date: 2025-01-05

-- Vérifier d'abord les doublons existants
-- Si des doublons existent, ils doivent être nettoyés avant d'ajouter la contrainte
-- Exécuter cette requête manuellement pour vérifier :
-- SELECT audience_id, categorie, COUNT(*) as count
-- FROM tarif_dossier
-- WHERE audience_id IS NOT NULL
-- GROUP BY audience_id, categorie
-- HAVING COUNT(*) > 1;

-- Ajouter la contrainte d'unicité (audience_id, categorie)
-- Note: Cette contrainte ne s'applique que si audience_id IS NOT NULL
ALTER TABLE tarif_dossier
ADD CONSTRAINT uk_tarif_audience_categorie 
UNIQUE (audience_id, categorie);


