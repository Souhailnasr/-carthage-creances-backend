-- Script pour vérifier et corriger les tables creancier et debiteur
-- Copier-coller ceci dans phpMyAdmin > SQL

USE carthage_creances;

-- Vérifier la structure des tables
DESCRIBE creancier;
DESCRIBE debiteur;

-- Ajouter la colonne type si elle n'existe pas
ALTER TABLE creancier ADD COLUMN IF NOT EXISTS type ENUM('PERSONNE_PHYSIQUE', 'PERSONNE_MORALE') DEFAULT 'PERSONNE_MORALE';
ALTER TABLE debiteur ADD COLUMN IF NOT EXISTS type ENUM('PERSONNE_PHYSIQUE', 'PERSONNE_MORALE') DEFAULT 'PERSONNE_MORALE';

-- Ajouter la colonne prenom si elle n'existe pas
ALTER TABLE creancier ADD COLUMN IF NOT EXISTS prenom VARCHAR(100);
ALTER TABLE debiteur ADD COLUMN IF NOT EXISTS prenom VARCHAR(100);

-- Vérifier le résultat
DESCRIBE creancier;
DESCRIBE debiteur;



