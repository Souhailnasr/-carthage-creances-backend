-- ============================================
-- SOLUTION SIMPLE : RECRÉER LA TABLE
-- ============================================
-- Cette solution est la plus simple car la table est vide
-- Exécutez ce script COMPLET dans phpMyAdmin

-- ÉTAPE 1: Supprimer toutes les contraintes existantes
-- (Exécutez d'abord pour voir les contraintes)
SELECT CONSTRAINT_NAME 
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = 'carthage_creances' 
  AND TABLE_NAME = 'dossier_utilisateurs';

-- Supprimer toutes les contraintes trouvées (remplacez [NOM] par les noms réels)
-- ALTER TABLE dossier_utilisateurs DROP FOREIGN KEY [NOM];

-- ÉTAPE 2: Supprimer complètement la table
DROP TABLE IF EXISTS dossier_utilisateurs;

-- ÉTAPE 3: Recréer la table avec la structure correcte
CREATE TABLE dossier_utilisateurs (
    dossier_id BIGINT NOT NULL,
    utilisateur_id BIGINT NOT NULL,
    PRIMARY KEY (dossier_id, utilisateur_id),
    CONSTRAINT fk_dossier_utilisateurs_dossier 
        FOREIGN KEY (dossier_id) REFERENCES dossier(id) ON DELETE CASCADE,
    CONSTRAINT fk_dossier_utilisateurs_utilisateur 
        FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ÉTAPE 4: Vérifier la structure créée
DESCRIBE dossier_utilisateurs;

-- ÉTAPE 5: Vérifier les contraintes
SELECT 
    CONSTRAINT_NAME, 
    COLUMN_NAME, 
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = 'carthage_creances' 
  AND TABLE_NAME = 'dossier_utilisateurs';

-- Résultat attendu:
-- - PRIMARY KEY sur (dossier_id, utilisateur_id)
-- - fk_dossier_utilisateurs_dossier sur dossier_id → dossier.id
-- - fk_dossier_utilisateurs_utilisateur sur utilisateur_id → utilisateur.id

