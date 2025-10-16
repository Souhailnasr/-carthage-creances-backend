-- Script pour corriger la colonne dossier_status
-- Exécuter ce script dans votre client MySQL (phpMyAdmin, MySQL Workbench, ou ligne de commande)

USE carthage_creances;

-- Vérifier la structure actuelle de la table
DESCRIBE dossier;

-- Modifier la colonne dossier_status pour accepter des valeurs plus longues
ALTER TABLE dossier MODIFY COLUMN dossier_status VARCHAR(50);

-- Vérifier que la modification a été appliquée
DESCRIBE dossier;

-- Afficher les valeurs possibles de l'enum Statut
-- EN_ATTENTE_VALIDATION (20 caractères)
-- VALIDE (6 caractères)
-- REJETE (6 caractères)
-- EN_COURS (8 caractères)
-- CLOTURE (7 caractères)

