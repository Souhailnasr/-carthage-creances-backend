-- Script complet pour corriger tous les problèmes de base de données
-- Exécuter ce script dans phpMyAdmin (onglet SQL)

USE carthage_creances;

-- 1. Corriger l'ENUM dossier_status
ALTER TABLE dossier MODIFY COLUMN dossier_status ENUM(
    'EN_ATTENTE_VALIDATION', 
    'VALIDE', 
    'REJETE', 
    'EN_COURS', 
    'CLOTURE'
) DEFAULT 'EN_COURS';

-- 2. Vérifier et corriger la colonne statut si nécessaire
ALTER TABLE dossier MODIFY COLUMN statut ENUM(
    'EN_ATTENTE_VALIDATION', 
    'VALIDE', 
    'REJETE', 
    'EN_COURS', 
    'CLOTURE'
) DEFAULT 'EN_ATTENTE_VALIDATION';

-- 3. Vérifier la structure de la table dossier
DESCRIBE dossier;

-- 4. Vérifier les tables liées
DESCRIBE creancier;
DESCRIBE debiteur;

-- 5. Vérifier que les colonnes type existent dans creancier et debiteur
-- Si elles n'existent pas, les ajouter :
-- ALTER TABLE creancier ADD COLUMN type ENUM('PERSONNE_PHYSIQUE', 'PERSONNE_MORALE') DEFAULT 'PERSONNE_MORALE';
-- ALTER TABLE debiteur ADD COLUMN type ENUM('PERSONNE_PHYSIQUE', 'PERSONNE_MORALE') DEFAULT 'PERSONNE_MORALE';

-- 6. Vérifier les colonnes prenom dans creancier et debiteur
-- Si elles n'existent pas, les ajouter :
-- ALTER TABLE creancier ADD COLUMN prenom VARCHAR(100);
-- ALTER TABLE debiteur ADD COLUMN prenom VARCHAR(100);

