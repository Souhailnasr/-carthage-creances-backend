-- ============================================
-- SCRIPT SQL FINAL - Correction Table dossier_utilisateurs
-- ============================================
-- Exécutez chaque section UNE PAR UNE dans phpMyAdmin
-- Ignorez les erreurs si elles indiquent que quelque chose n'existe pas

-- ============================================
-- ÉTAPE 1: Identifier toutes les contraintes
-- ============================================
SELECT 
    CONSTRAINT_NAME, 
    COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = 'carthage_creances' 
  AND TABLE_NAME = 'dossier_utilisateurs';

-- Notez tous les noms de contraintes trouvés ci-dessus
-- Vous devrez les supprimer dans l'étape suivante

-- ============================================
-- ÉTAPE 2: Supprimer toutes les contraintes
-- ============================================
-- Exécutez UNE PAR UNE (remplacez les noms par ceux trouvés à l'étape 1)

ALTER TABLE dossier_utilisateurs DROP FOREIGN KEY FK9pgk4cpeaa53jbc8xifuirxbv;
ALTER TABLE dossier_utilisateurs DROP FOREIGN KEY FK7qab2690496t98ral;
ALTER TABLE dossier_utilisateurs DROP FOREIGN KEY FKI3p4vah2dskmyyfe;

-- Si vous avez trouvé d'autres contraintes à l'étape 1, ajoutez-les ici:
-- ALTER TABLE dossier_utilisateurs DROP FOREIGN KEY [NOM_CONTRAINTE];

-- ============================================
-- ÉTAPE 3: Vérifier qu'il ne reste plus de contraintes
-- ============================================
SELECT CONSTRAINT_NAME 
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = 'carthage_creances' 
  AND TABLE_NAME = 'dossier_utilisateurs'
  AND CONSTRAINT_NAME LIKE 'FK%';

-- Si cette requête retourne encore des contraintes, supprimez-les avec:
-- ALTER TABLE dossier_utilisateurs DROP FOREIGN KEY [NOM_CONTRAINTE];

-- ============================================
-- ÉTAPE 4: Supprimer les colonnes redondantes
-- ============================================
-- Supprimer utilisateurs_id (ignorez l'erreur si la colonne n'existe pas)
ALTER TABLE dossier_utilisateurs DROP COLUMN utilisateurs_id;

-- Supprimer dossiers_id si elle existe encore (ignorez l'erreur si elle n'existe pas)
ALTER TABLE dossier_utilisateurs DROP COLUMN dossiers_id;

-- ============================================
-- ÉTAPE 5: Ajouter la clé primaire composite
-- ============================================
-- (Ignorez l'erreur si la clé primaire existe déjà)
ALTER TABLE dossier_utilisateurs 
ADD PRIMARY KEY (dossier_id, utilisateur_id);

-- ============================================
-- ÉTAPE 6: Supprimer les anciennes clés étrangères (si elles existent)
-- ============================================
-- (Ignorez les erreurs si les contraintes n'existent pas)
ALTER TABLE dossier_utilisateurs 
DROP FOREIGN KEY IF EXISTS fk_dossier_utilisateurs_dossier;

ALTER TABLE dossier_utilisateurs 
DROP FOREIGN KEY IF EXISTS fk_dossier_utilisateurs_utilisateur;

-- ============================================
-- ÉTAPE 7: Ajouter les nouvelles clés étrangères
-- ============================================
-- (Ignorez les erreurs si les contraintes existent déjà)
ALTER TABLE dossier_utilisateurs
ADD CONSTRAINT fk_dossier_utilisateurs_dossier 
FOREIGN KEY (dossier_id) REFERENCES dossier(id) ON DELETE CASCADE;

ALTER TABLE dossier_utilisateurs
ADD CONSTRAINT fk_dossier_utilisateurs_utilisateur 
FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE;

-- ============================================
-- ÉTAPE 8: Vérifier la structure finale
-- ============================================
DESCRIBE dossier_utilisateurs;

-- La table devrait avoir uniquement 2 colonnes:
-- - dossier_id (BIGINT, NOT NULL, PRIMARY KEY)
-- - utilisateur_id (BIGINT, NOT NULL, PRIMARY KEY)

-- ============================================
-- ÉTAPE 9: Vérifier les contraintes finales
-- ============================================
SELECT 
    CONSTRAINT_NAME, 
    COLUMN_NAME, 
    REFERENCED_TABLE_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = 'carthage_creances' 
  AND TABLE_NAME = 'dossier_utilisateurs';

-- Vous devriez voir:
-- - PRIMARY (clé primaire composite)
-- - fk_dossier_utilisateurs_dossier (clé étrangère vers dossier)
-- - fk_dossier_utilisateurs_utilisateur (clé étrangère vers utilisateur)

