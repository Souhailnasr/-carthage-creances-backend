-- Script SQL pour ajouter FINANCE à l'ENUM type_recouvrement
-- À exécuter dans phpMyAdmin ou MySQL Workbench

-- Étape 1 : Vérifier le type actuel de la colonne
-- Exécutez cette requête pour voir le type actuel :
-- SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS 
-- WHERE TABLE_SCHEMA = 'carthage_creances' 
-- AND TABLE_NAME = 'dossier' 
-- AND COLUMN_NAME = 'type_recouvrement';

-- Étape 2 : Modifier l'ENUM pour inclure FINANCE
-- Si la colonne est un ENUM, exécutez cette commande :
ALTER TABLE dossier 
MODIFY COLUMN type_recouvrement ENUM('NON_AFFECTE', 'AMIABLE', 'JURIDIQUE', 'FINANCE') NULL;

-- Étape 3 : Vérifier que la modification a réussi
-- Exécutez cette requête pour confirmer :
-- SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS 
-- WHERE TABLE_SCHEMA = 'carthage_creances' 
-- AND TABLE_NAME = 'dossier' 
-- AND COLUMN_NAME = 'type_recouvrement';
-- 
-- Vous devriez voir : enum('NON_AFFECTE','AMIABLE','JURIDIQUE','FINANCE')

-- Étape 4 : Vérifier les valeurs existantes
-- SELECT type_recouvrement, COUNT(*) as nombre
-- FROM dossier
-- GROUP BY type_recouvrement;

