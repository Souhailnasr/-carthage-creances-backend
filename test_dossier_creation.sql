-- Script de test pour vérifier la création de dossiers
-- Exécuter après avoir appliqué les corrections

USE carthage_creances;

-- 1. Vérifier la structure de la table dossier
DESCRIBE dossier;

-- 2. Vérifier les valeurs ENUM
SHOW COLUMNS FROM dossier LIKE 'dossier_status';
SHOW COLUMNS FROM dossier LIKE 'statut';

-- 3. Vérifier les tables creancier et debiteur
DESCRIBE creancier;
DESCRIBE debiteur;

-- 4. Insérer des données de test (optionnel)
-- INSERT INTO creancier (nom, type) VALUES ('Test Entreprise', 'PERSONNE_MORALE');
-- INSERT INTO debiteur (nom, prenom, type) VALUES ('Doe', 'John', 'PERSONNE_PHYSIQUE');

-- 5. Vérifier les données
-- SELECT * FROM creancier;
-- SELECT * FROM debiteur;

