-- Test simple pour vérifier que les ENUMs fonctionnent
-- Copier-coller ceci dans phpMyAdmin > SQL

USE carthage_creances;

-- Test d'insertion avec les nouvelles valeurs
INSERT INTO creancier (nom, type) VALUES ('Test Entreprise', 'PERSONNE_MORALE');
INSERT INTO debiteur (nom, prenom, type) VALUES ('Doe', 'John', 'PERSONNE_PHYSIQUE');

-- Test d'insertion d'un dossier avec les nouvelles valeurs ENUM
INSERT INTO dossier (
    titre, 
    numero_dossier, 
    montant_creance, 
    dossier_status, 
    statut,
    creancier_id,
    debiteur_id
) VALUES (
    'Test Dossier', 
    'TEST-001', 
    1000.00, 
    'VALIDE', 
    'VALIDE',
    (SELECT id FROM creancier WHERE nom = 'Test Entreprise' LIMIT 1),
    (SELECT id FROM debiteur WHERE nom = 'Doe' LIMIT 1)
);

-- Vérifier que l'insertion a fonctionné
SELECT * FROM dossier WHERE numero_dossier = 'TEST-001';

-- Nettoyer les données de test
DELETE FROM dossier WHERE numero_dossier = 'TEST-001';
DELETE FROM creancier WHERE nom = 'Test Entreprise';
DELETE FROM debiteur WHERE nom = 'Doe';



