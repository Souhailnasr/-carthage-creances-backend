-- Script SQL FINAL pour corriger dossier_utilisateurs
-- Problème: La colonne utilisateurs_id a une contrainte de clé étrangère
-- Solution: Supprimer toutes les contraintes d'abord, puis la colonne

-- ============================================
-- ÉTAPE 1: Identifier TOUTES les contraintes de clé étrangère
-- ============================================
-- Exécutez d'abord cette requête pour voir toutes les contraintes:
SELECT 
    CONSTRAINT_NAME, 
    COLUMN_NAME, 
    REFERENCED_TABLE_NAME, 
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = 'carthage_creances' 
  AND TABLE_NAME = 'dossier_utilisateurs';

-- ============================================
-- ÉTAPE 2: Supprimer TOUTES les contraintes de clé étrangère
-- ============================================
-- Supprimer la contrainte identifiée dans l'erreur
ALTER TABLE dossier_utilisateurs 
DROP FOREIGN KEY FK9pgk4cpeaa53jbc8xifuirxbv;

-- Supprimer les autres contraintes visibles dans la structure
ALTER TABLE dossier_utilisateurs 
DROP FOREIGN KEY FK7qab2690496t98ral;

ALTER TABLE dossier_utilisateurs 
DROP FOREIGN KEY FKI3p4vah2dskmyyfe;

-- Supprimer toutes les autres contraintes possibles
-- (Utilisez les noms trouvés à l'étape 1)
-- ALTER TABLE dossier_utilisateurs DROP FOREIGN KEY [NOM_CONTRAINTE];

-- ============================================
-- ÉTAPE 3: Vérifier qu'il ne reste plus de contraintes
-- ============================================
-- Exécutez à nouveau cette requête pour vérifier:
SELECT CONSTRAINT_NAME 
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = 'carthage_creances' 
  AND TABLE_NAME = 'dossier_utilisateurs'
  AND CONSTRAINT_NAME LIKE 'FK%';

-- Si des contraintes restent, supprimez-les avec:
-- ALTER TABLE dossier_utilisateurs DROP FOREIGN KEY [NOM_CONTRAINTE];

-- ============================================
-- ÉTAPE 4: Supprimer la colonne utilisateurs_id
-- ============================================
-- Maintenant que toutes les contraintes sont supprimées, vous pouvez supprimer la colonne
ALTER TABLE dossier_utilisateurs DROP COLUMN utilisateurs_id;

-- ============================================
-- ÉTAPE 5: Ajouter la clé primaire composite
-- ============================================
-- Vérifier d'abord si une clé primaire existe
SELECT CONSTRAINT_NAME 
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
WHERE TABLE_SCHEMA = 'carthage_creances' 
  AND TABLE_NAME = 'dossier_utilisateurs' 
  AND CONSTRAINT_TYPE = 'PRIMARY KEY';

-- Si aucune clé primaire n'existe, exécutez:
ALTER TABLE dossier_utilisateurs 
ADD PRIMARY KEY (dossier_id, utilisateur_id);

-- ============================================
-- ÉTAPE 6: Ajouter les nouvelles clés étrangères
-- ============================================
ALTER TABLE dossier_utilisateurs
ADD CONSTRAINT fk_dossier_utilisateurs_dossier 
FOREIGN KEY (dossier_id) REFERENCES dossier(id) ON DELETE CASCADE;

ALTER TABLE dossier_utilisateurs
ADD CONSTRAINT fk_dossier_utilisateurs_utilisateur 
FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE;

-- ============================================
-- ÉTAPE 7: Vérifier la structure finale
-- ============================================
DESCRIBE dossier_utilisateurs;

-- La table devrait avoir uniquement 2 colonnes:
-- - dossier_id (BIGINT, NOT NULL, PRIMARY KEY)
-- - utilisateur_id (BIGINT, NOT NULL, PRIMARY KEY)

