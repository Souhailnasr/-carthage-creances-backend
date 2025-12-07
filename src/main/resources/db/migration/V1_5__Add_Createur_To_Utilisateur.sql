-- Migration pour ajouter le champ createur_id à la table utilisateur
-- Date: 2025-01-05

-- Ajouter la colonne createur_id (nullable pour les utilisateurs existants)
ALTER TABLE utilisateur 
ADD COLUMN createur_id BIGINT NULL;

-- Ajouter la contrainte de clé étrangère
ALTER TABLE utilisateur 
ADD CONSTRAINT FK_utilisateur_createur 
FOREIGN KEY (createur_id) REFERENCES utilisateur(id) ON DELETE SET NULL;

-- Ajouter un index pour améliorer les performances des requêtes de filtrage
CREATE INDEX idx_utilisateur_createur ON utilisateur(createur_id);

-- Note: Les utilisateurs existants auront createur_id = NULL
-- Les nouveaux utilisateurs créés par des chefs auront leur createur_id défini

