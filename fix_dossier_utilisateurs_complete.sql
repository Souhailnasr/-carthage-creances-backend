-- ============================================
-- SCRIPT COMPLET DE CORRECTION - dossier_utilisateurs
-- ============================================
-- Exécutez chaque section UNE PAR UNE
-- Ignorez les erreurs si elles indiquent que quelque chose existe déjà

-- ============================================
-- PARTIE 1: VÉRIFICATIONS
-- ============================================

-- 1.1 Vérifier la structure actuelle
DESCRIBE dossier_utilisateurs;

-- 1.2 Vérifier que dossier.id existe et est PRIMARY KEY
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_KEY
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'carthage_creances'
  AND TABLE_NAME = 'dossier'
  AND COLUMN_NAME = 'id';

-- 1.3 Vérifier que utilisateur.id existe et est PRIMARY KEY
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_KEY
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'carthage_creances'
  AND TABLE_NAME = 'utilisateur'
  AND COLUMN_NAME = 'id';

-- ============================================
-- PARTIE 2: NETTOYAGE
-- ============================================

-- 2.1 Supprimer toutes les contraintes existantes
-- (Exécutez pour chaque contrainte trouvée)
ALTER TABLE dossier_utilisateurs DROP FOREIGN KEY IF EXISTS FK9pgk4cpeaa53jbc8xifuirxbv;
ALTER TABLE dossier_utilisateurs DROP FOREIGN KEY IF EXISTS FK7qab2690496t98ral;
ALTER TABLE dossier_utilisateurs DROP FOREIGN KEY IF EXISTS FKI3p4vah2dskmyyfe;
ALTER TABLE dossier_utilisateurs DROP FOREIGN KEY IF EXISTS fk_dossier_utilisateurs_dossier;
ALTER TABLE dossier_utilisateurs DROP FOREIGN KEY IF EXISTS fk_dossier_utilisateurs_utilisateur;

-- 2.2 Supprimer les colonnes redondantes
ALTER TABLE dossier_utilisateurs DROP COLUMN IF EXISTS utilisateurs_id;
ALTER TABLE dossier_utilisateurs DROP COLUMN IF EXISTS dossiers_id;

-- 2.3 Nettoyer les données invalides (si la table n'est pas vide)
DELETE FROM dossier_utilisateurs
WHERE dossier_id NOT IN (SELECT id FROM dossier);

DELETE FROM dossier_utilisateurs
WHERE utilisateur_id NOT IN (SELECT id FROM utilisateur);

-- ============================================
-- PARTIE 3: CRÉATION/MODIFICATION DES COLONNES
-- ============================================

-- 3.1 Créer les colonnes si elles n'existent pas
-- (MySQL ne supporte pas IF EXISTS pour ADD COLUMN, donc vérifiez d'abord avec DESCRIBE)
-- Si dossier_id n'existe pas:
-- ALTER TABLE dossier_utilisateurs ADD COLUMN dossier_id BIGINT NOT NULL;

-- Si utilisateur_id n'existe pas:
-- ALTER TABLE dossier_utilisateurs ADD COLUMN utilisateur_id BIGINT NOT NULL;

-- 3.2 S'assurer que les types sont corrects (BIGINT)
ALTER TABLE dossier_utilisateurs
MODIFY COLUMN dossier_id BIGINT NOT NULL;

ALTER TABLE dossier_utilisateurs
MODIFY COLUMN utilisateur_id BIGINT NOT NULL;

-- ============================================
-- PARTIE 4: CLÉ PRIMAIRE
-- ============================================

-- 4.1 Supprimer l'ancienne clé primaire si elle existe
ALTER TABLE dossier_utilisateurs DROP PRIMARY KEY;

-- 4.2 Ajouter la clé primaire composite
ALTER TABLE dossier_utilisateurs 
ADD PRIMARY KEY (dossier_id, utilisateur_id);

-- ============================================
-- PARTIE 5: CLÉS ÉTRANGÈRES
-- ============================================

-- 5.1 Vérifier que dossier.id est bien une clé primaire
SELECT CONSTRAINT_TYPE
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
WHERE TABLE_SCHEMA = 'carthage_creances'
  AND TABLE_NAME = 'dossier'
  AND CONSTRAINT_TYPE = 'PRIMARY KEY';

-- 5.2 Vérifier que utilisateur.id est bien une clé primaire
SELECT CONSTRAINT_TYPE
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
WHERE TABLE_SCHEMA = 'carthage_creances'
  AND TABLE_NAME = 'utilisateur'
  AND CONSTRAINT_TYPE = 'PRIMARY KEY';

-- 5.3 Ajouter la clé étrangère vers dossier
ALTER TABLE dossier_utilisateurs
ADD CONSTRAINT fk_dossier_utilisateurs_dossier 
FOREIGN KEY (dossier_id) REFERENCES dossier(id) ON DELETE CASCADE;

-- 5.4 Ajouter la clé étrangère vers utilisateur
ALTER TABLE dossier_utilisateurs
ADD CONSTRAINT fk_dossier_utilisateurs_utilisateur 
FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE;

-- ============================================
-- PARTIE 6: VÉRIFICATION FINALE
-- ============================================

-- 6.1 Vérifier la structure
DESCRIBE dossier_utilisateurs;

-- 6.2 Vérifier les contraintes
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
-- - fk_dossier_utilisateurs_dossier sur dossier_id
-- - fk_dossier_utilisateurs_utilisateur sur utilisateur_id

