-- Migration script to add file path columns to dossier table
-- Compatible with Flyway and Liquibase

-- 1. Add new columns for file paths
ALTER TABLE dossier 
ADD COLUMN pouvoir_file_path VARCHAR(255) NULL,
ADD COLUMN contrat_signe_file_path VARCHAR(255) NULL;

-- 2. Create indexes for better performance
CREATE INDEX idx_dossier_pouvoir_file_path ON dossier(pouvoir_file_path);
CREATE INDEX idx_dossier_contrat_signe_file_path ON dossier(contrat_signe_file_path);

-- 3. Migrate existing data
-- If pouvoir='uploaded' then create a file path
UPDATE dossier 
SET pouvoir_file_path = CONCAT('uploads/dossiers/pouvoir_', id, '.pdf')
WHERE pouvoir = 'uploaded' AND pouvoir_file_path IS NULL;

-- If contrat_signe='uploaded' then create a file path  
UPDATE dossier 
SET contrat_signe_file_path = CONCAT('uploads/dossiers/contrat_', id, '.pdf')
WHERE contrat_signe = 'uploaded' AND contrat_signe_file_path IS NULL;

-- 4. Add comments for documentation
ALTER TABLE dossier 
MODIFY COLUMN pouvoir_file_path VARCHAR(255) NULL COMMENT 'Chemin vers le fichier pouvoir PDF',
MODIFY COLUMN contrat_signe_file_path VARCHAR(255) NULL COMMENT 'Chemin vers le fichier contrat signé PDF';

-- 5. Optional: Create a view for backward compatibility
CREATE VIEW dossier_with_files AS
SELECT 
    d.*,
    CASE 
        WHEN d.pouvoir_file_path IS NOT NULL THEN 'uploaded'
        WHEN d.pouvoir IS NOT NULL AND d.pouvoir != '' THEN d.pouvoir
        ELSE NULL
    END AS pouvoir_status,
    CASE 
        WHEN d.contrat_signe_file_path IS NOT NULL THEN 'uploaded'
        WHEN d.contrat_signe IS NOT NULL AND d.contrat_signe != '' THEN d.contrat_signe
        ELSE NULL
    END AS contrat_signe_status
FROM dossier d;
