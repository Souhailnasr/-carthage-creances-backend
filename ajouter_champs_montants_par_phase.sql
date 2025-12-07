-- Script SQL pour ajouter les champs de montants recouvrés par phase
-- À exécuter sur la base de données MySQL

-- 1. Ajouter les colonnes pour les montants par phase dans la table dossiers
ALTER TABLE dossiers
ADD COLUMN montant_recouvre_phase_amiable DOUBLE DEFAULT 0.0 AFTER montant_recouvre,
ADD COLUMN montant_recouvre_phase_juridique DOUBLE DEFAULT 0.0 AFTER montant_recouvre_phase_amiable;

-- 2. Initialiser les valeurs existantes
-- Si un dossier a déjà un montant recouvré, on le répartit selon le type de recouvrement
UPDATE dossiers d
LEFT JOIN dossiers d2 ON d.id = d2.id
SET 
    d.montant_recouvre_phase_amiable = CASE 
        WHEN d.type_recouvrement = 'AMIABLE' AND d.montant_recouvre > 0 
        THEN d.montant_recouvre 
        ELSE 0.0 
    END,
    d.montant_recouvre_phase_juridique = CASE 
        WHEN d.type_recouvrement = 'JURIDIQUE' AND d.montant_recouvre > 0 
        THEN d.montant_recouvre 
        ELSE 0.0 
    END
WHERE d.montant_recouvre IS NOT NULL;

-- 3. Créer la table historique_recouvrement
CREATE TABLE IF NOT EXISTS historique_recouvrement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dossier_id BIGINT NOT NULL,
    phase ENUM('AMIABLE', 'JURIDIQUE') NOT NULL,
    montant_recouvre DECIMAL(19, 2) NOT NULL,
    montant_total_recouvre DECIMAL(19, 2) NOT NULL,
    montant_restant DECIMAL(19, 2) NOT NULL,
    type_action ENUM('ACTION_AMIABLE', 'ACTION_HUISSIER', 'FINALISATION_AMIABLE', 'FINALISATION_JURIDIQUE'),
    action_id BIGINT NULL,
    utilisateur_id BIGINT NULL,
    date_enregistrement DATETIME NOT NULL,
    commentaire VARCHAR(500) NULL,
    INDEX idx_dossier_id (dossier_id),
    INDEX idx_phase (phase),
    INDEX idx_date_enregistrement (date_enregistrement),
    INDEX idx_utilisateur_id (utilisateur_id),
    FOREIGN KEY (dossier_id) REFERENCES dossiers(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Vérifier la cohérence des données
-- Les montants recouvrés par phase doivent être égaux au montant recouvré total
UPDATE dossiers
SET montant_recouvre = COALESCE(montant_recouvre_phase_amiable, 0) + COALESCE(montant_recouvre_phase_juridique, 0)
WHERE montant_recouvre IS NULL 
   OR ABS(montant_recouvre - (COALESCE(montant_recouvre_phase_amiable, 0) + COALESCE(montant_recouvre_phase_juridique, 0))) > 0.01;

