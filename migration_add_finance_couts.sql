-- Migration SQL pour ajouter les nouveaux champs de coût à la table Finance

-- Ajouter les colonnes de coûts de création et gestion
ALTER TABLE finance
ADD COLUMN IF NOT EXISTS frais_creation_dossier DOUBLE PRECISION DEFAULT 50.0,
ADD COLUMN IF NOT EXISTS frais_gestion_dossier DOUBLE PRECISION DEFAULT 10.0,
ADD COLUMN IF NOT EXISTS duree_gestion_mois INTEGER DEFAULT 0;

-- Ajouter les colonnes de coûts des actions
ALTER TABLE finance
ADD COLUMN IF NOT EXISTS cout_actions_amiable DOUBLE PRECISION DEFAULT 0.0,
ADD COLUMN IF NOT EXISTS cout_actions_juridique DOUBLE PRECISION DEFAULT 0.0,
ADD COLUMN IF NOT EXISTS nombre_actions_amiable INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS nombre_actions_juridique INTEGER DEFAULT 0;

-- Ajouter les colonnes de statut de facturation
ALTER TABLE finance
ADD COLUMN IF NOT EXISTS facture_finalisee BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS date_facturation DATE;

-- Mettre à jour les valeurs existantes si nécessaire
UPDATE finance
SET frais_creation_dossier = 50.0
WHERE frais_creation_dossier IS NULL;

UPDATE finance
SET frais_gestion_dossier = 10.0
WHERE frais_gestion_dossier IS NULL;

UPDATE finance
SET duree_gestion_mois = 0
WHERE duree_gestion_mois IS NULL;

UPDATE finance
SET cout_actions_amiable = 0.0
WHERE cout_actions_amiable IS NULL;

UPDATE finance
SET cout_actions_juridique = 0.0
WHERE cout_actions_juridique IS NULL;

UPDATE finance
SET nombre_actions_amiable = 0
WHERE nombre_actions_amiable IS NULL;

UPDATE finance
SET nombre_actions_juridique = 0
WHERE nombre_actions_juridique IS NULL;

UPDATE finance
SET facture_finalisee = FALSE
WHERE facture_finalisee IS NULL;

-- Créer des index pour optimiser les recherches
CREATE INDEX IF NOT EXISTS idx_finance_facture_finalisee ON finance(facture_finalisee);
CREATE INDEX IF NOT EXISTS idx_finance_date_facturation ON finance(date_facturation);

-- Commentaires pour documentation
COMMENT ON COLUMN finance.frais_creation_dossier IS 'Coût fixe pour la création d''un dossier (par défaut 50 TND)';
COMMENT ON COLUMN finance.frais_gestion_dossier IS 'Coût mensuel de gestion du dossier (par défaut 10 TND/mois)';
COMMENT ON COLUMN finance.duree_gestion_mois IS 'Durée de gestion en mois (calculée automatiquement lors de la clôture)';
COMMENT ON COLUMN finance.cout_actions_amiable IS 'Coût total des actions de recouvrement amiable';
COMMENT ON COLUMN finance.cout_actions_juridique IS 'Coût total des actions de recouvrement juridique';
COMMENT ON COLUMN finance.nombre_actions_amiable IS 'Nombre total d''actions de recouvrement amiable';
COMMENT ON COLUMN finance.nombre_actions_juridique IS 'Nombre total d''actions de recouvrement juridique';
COMMENT ON COLUMN finance.facture_finalisee IS 'Indique si la facture a été finalisée';
COMMENT ON COLUMN finance.date_facturation IS 'Date de finalisation de la facture';

