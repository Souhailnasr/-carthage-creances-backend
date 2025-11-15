-- Script de correction CORRIGÉ pour la table dossier_utilisateurs
-- Problème: Les colonnes dossiers_id et utilisateurs_id ont des contraintes de clé étrangère
-- Solution: Supprimer d'abord les contraintes, puis les colonnes

-- ÉTAPE 1: Identifier et supprimer les contraintes de clé étrangère existantes
-- D'abord, vérifiez les contraintes existantes avec cette requête:
-- SELECT CONSTRAINT_NAME, TABLE_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME
-- FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
-- WHERE TABLE_SCHEMA = 'carthage_creances' AND TABLE_NAME = 'dossier_utilisateurs';

-- Supprimer les contraintes de clé étrangère liées à dossiers_id et utilisateurs_id
-- (Les noms peuvent varier, ajustez selon votre base)

-- Supprimer la contrainte sur dossiers_id (si elle existe)
ALTER TABLE dossier_utilisateurs 
DROP FOREIGN KEY IF EXISTS FKxcymrrxt4dj72jnvudf8dj1s;

-- Chercher et supprimer toutes les autres contraintes possibles
-- Exécutez d'abord cette requête pour voir toutes les contraintes:
SELECT CONSTRAINT_NAME 
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = 'carthage_creances' 
  AND TABLE_NAME = 'dossier_utilisateurs'
  AND COLUMN_NAME IN ('dossiers_id', 'utilisateurs_id');

-- Supprimez chaque contrainte trouvée avec:
-- ALTER TABLE dossier_utilisateurs DROP FOREIGN KEY [NOM_DE_LA_CONTRAINTE];

-- ÉTAPE 2: Supprimer les index associés (si nécessaire)
ALTER TABLE dossier_utilisateurs DROP INDEX IF EXISTS FKxcymrrxt4dj72jnvudf8dj1s;

-- ÉTAPE 3: Maintenant, supprimer les colonnes redondantes
ALTER TABLE dossier_utilisateurs DROP COLUMN IF EXISTS dossiers_id;
ALTER TABLE dossier_utilisateurs DROP COLUMN IF EXISTS utilisateurs_id;

-- ÉTAPE 4: Supprimer les anciennes clés étrangères sur dossier_id et utilisateur_id si elles existent
-- (pour éviter les conflits lors de l'ajout de nouvelles)
ALTER TABLE dossier_utilisateurs 
DROP FOREIGN KEY IF EXISTS fk_dossier_utilisateurs_dossier;

ALTER TABLE dossier_utilisateurs 
DROP FOREIGN KEY IF EXISTS fk_dossier_utilisateurs_utilisateur;

-- ÉTAPE 5: Ajouter une clé primaire composite (si elle n'existe pas)
-- Vérifier d'abord si une clé primaire existe
ALTER TABLE dossier_utilisateurs 
ADD PRIMARY KEY (dossier_id, utilisateur_id);

-- Si la clé primaire existe déjà, cette commande échouera mais ce n'est pas grave

-- ÉTAPE 6: Ajouter les nouvelles clés étrangères
ALTER TABLE dossier_utilisateurs
ADD CONSTRAINT fk_dossier_utilisateurs_dossier 
FOREIGN KEY (dossier_id) REFERENCES dossier(id) ON DELETE CASCADE;

ALTER TABLE dossier_utilisateurs
ADD CONSTRAINT fk_dossier_utilisateurs_utilisateur 
FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE;

-- ÉTAPE 7: Vérifier la structure finale
-- Exécutez: DESCRIBE dossier_utilisateurs;
-- La table devrait avoir uniquement:
-- - dossier_id (BIGINT, NOT NULL, PRIMARY KEY)
-- - utilisateur_id (BIGINT, NOT NULL, PRIMARY KEY)

