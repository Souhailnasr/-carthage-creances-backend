-- Script de correction pour la table dossier_utilisateurs
-- Problème: Field 'dossiers_id' doesn't have a default value

-- Étape 1: Vérifier la structure actuelle de la table
-- Exécutez d'abord cette commande pour voir la structure:
-- DESCRIBE dossier_utilisateurs;
-- ou
-- SHOW CREATE TABLE dossier_utilisateurs;

-- Étape 2: Si la table a un champ dossiers_id qui n'est pas utilisé
-- OPTION A: Supprimer le champ dossiers_id s'il est redondant avec dossier_id
ALTER TABLE dossier_utilisateurs DROP COLUMN IF EXISTS dossiers_id;

-- OPTION B: Si dossiers_id doit exister, le rendre nullable ou lui donner une valeur par défaut
-- ALTER TABLE dossier_utilisateurs MODIFY COLUMN dossiers_id BIGINT DEFAULT NULL;
-- ou
-- ALTER TABLE dossier_utilisateurs MODIFY COLUMN dossiers_id BIGINT DEFAULT 0;

-- Étape 3: Vérifier que les colonnes dossier_id et utilisateur_id existent et sont correctes
-- Si elles n'existent pas, les créer:
-- ALTER TABLE dossier_utilisateurs 
-- ADD COLUMN IF NOT EXISTS dossier_id BIGINT NOT NULL,
-- ADD COLUMN IF NOT EXISTS utilisateur_id BIGINT NOT NULL;

-- Étape 4: Ajouter les clés étrangères si elles n'existent pas
-- ALTER TABLE dossier_utilisateurs
-- ADD CONSTRAINT fk_dossier_utilisateurs_dossier 
-- FOREIGN KEY (dossier_id) REFERENCES dossier(id) ON DELETE CASCADE,
-- ADD CONSTRAINT fk_dossier_utilisateurs_utilisateur 
-- FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE;

-- Étape 5: Ajouter une clé primaire composite si elle n'existe pas
-- ALTER TABLE dossier_utilisateurs
-- ADD PRIMARY KEY (dossier_id, utilisateur_id);

-- Étape 6: Si la table n'existe pas du tout, la créer:
/*
CREATE TABLE IF NOT EXISTS dossier_utilisateurs (
    dossier_id BIGINT NOT NULL,
    utilisateur_id BIGINT NOT NULL,
    PRIMARY KEY (dossier_id, utilisateur_id),
    CONSTRAINT fk_dossier_utilisateurs_dossier 
        FOREIGN KEY (dossier_id) REFERENCES dossier(id) ON DELETE CASCADE,
    CONSTRAINT fk_dossier_utilisateurs_utilisateur 
        FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
*/

-- SOLUTION RECOMMANDÉE: Supprimer dossiers_id si redondant
-- Exécutez cette commande si dossiers_id et dossier_id sont la même chose:
ALTER TABLE dossier_utilisateurs DROP COLUMN IF EXISTS dossiers_id;

