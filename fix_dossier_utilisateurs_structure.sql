-- Script de correction pour la table dossier_utilisateurs
-- Problème: La table a 4 colonnes (dossiers_id, utilisateurs_id, dossier_id, utilisateur_id)
-- Solution: Supprimer les colonnes redondantes dossiers_id et utilisateurs_id

-- Étape 1: Vérifier la structure actuelle (déjà fait via phpMyAdmin)
-- La table a:
-- - dossiers_id (redondant avec dossier_id)
-- - utilisateurs_id (redondant avec utilisateur_id)
-- - dossier_id (utilisé par JPA)
-- - utilisateur_id (utilisé par JPA)

-- Étape 2: Supprimer les colonnes redondantes
ALTER TABLE dossier_utilisateurs DROP COLUMN IF EXISTS dossiers_id;
ALTER TABLE dossier_utilisateurs DROP COLUMN IF EXISTS utilisateurs_id;

-- Étape 3: Vérifier que les colonnes dossier_id et utilisateur_id existent et sont correctes
-- Elles existent déjà selon la capture, donc pas besoin de les créer

-- Étape 4: Ajouter une clé primaire composite si elle n'existe pas
-- Vérifier d'abord si une clé primaire existe
ALTER TABLE dossier_utilisateurs 
ADD PRIMARY KEY (dossier_id, utilisateur_id);

-- Si la clé primaire existe déjà, cette commande échouera mais ce n'est pas grave
-- Vous pouvez ignorer l'erreur si elle dit que la clé primaire existe déjà

-- Étape 5: Vérifier les clés étrangères
-- Ajouter les clés étrangères si elles n'existent pas
ALTER TABLE dossier_utilisateurs
ADD CONSTRAINT fk_dossier_utilisateurs_dossier 
FOREIGN KEY (dossier_id) REFERENCES dossier(id) ON DELETE CASCADE;

ALTER TABLE dossier_utilisateurs
ADD CONSTRAINT fk_dossier_utilisateurs_utilisateur 
FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE;

-- Si les contraintes existent déjà, ces commandes échoueront mais ce n'est pas grave

-- Étape 6: Vérifier la structure finale
-- Exécutez: DESCRIBE dossier_utilisateurs;
-- La table devrait avoir uniquement:
-- - dossier_id (BIGINT, NOT NULL, PRIMARY KEY)
-- - utilisateur_id (BIGINT, NOT NULL, PRIMARY KEY)

