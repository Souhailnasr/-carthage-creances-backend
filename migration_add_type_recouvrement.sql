-- Migration : Ajout de la colonne type_recouvrement à la table dossiers
-- Date : 2025-11-15
-- Description : Ajoute le champ type_recouvrement pour gérer le type de recouvrement (NON_AFFECTE, AMIABLE, JURIDIQUE)

-- Étape 1 : Ajouter la colonne type_recouvrement
ALTER TABLE dossiers 
ADD COLUMN type_recouvrement VARCHAR(50) NULL DEFAULT 'NON_AFFECTE';

-- Étape 2 : Mettre à jour les dossiers existants pour qu'ils soient NON_AFFECTE
UPDATE dossiers 
SET type_recouvrement = 'NON_AFFECTE' 
WHERE type_recouvrement IS NULL;

-- Étape 3 : Ajouter un index pour améliorer les performances des requêtes de filtrage
CREATE INDEX idx_dossiers_type_recouvrement ON dossiers(type_recouvrement);

-- Étape 4 : Ajouter un index composite pour les requêtes fréquentes (type_recouvrement + valide + dossier_status)
CREATE INDEX idx_dossiers_type_valide_status ON dossiers(type_recouvrement, valide, dossier_status);

-- Note : La table dossier_utilisateurs existe déjà (créée automatiquement par Hibernate pour la relation Many-to-Many)
-- Si elle n'existe pas, elle sera créée automatiquement au prochain démarrage de l'application

