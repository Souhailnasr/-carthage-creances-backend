-- Migration pour ajouter la valeur FINANCE dans l'enum type_recouvrement
-- Ce script doit être exécuté après le déploiement du code

-- Note: Si la colonne type_recouvrement est un ENUM dans MySQL, 
-- il faut modifier l'ENUM pour ajouter la valeur FINANCE
-- Si c'est un VARCHAR, aucune modification n'est nécessaire (Hibernate gère automatiquement)

-- Option 1: Si type_recouvrement est un ENUM, modifier l'ENUM
-- ALTER TABLE dossier MODIFY COLUMN type_recouvrement ENUM('NON_AFFECTE', 'AMIABLE', 'JURIDIQUE', 'FINANCE') NULL;

-- Option 2: Si type_recouvrement est un VARCHAR (recommandé), aucune action nécessaire
-- Hibernate gère automatiquement les nouvelles valeurs d'enum si la colonne est VARCHAR

-- Vérifier le type de la colonne actuelle
-- SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS 
-- WHERE TABLE_SCHEMA = 'carthage_creances' 
-- AND TABLE_NAME = 'dossier' 
-- AND COLUMN_NAME = 'type_recouvrement';

-- Si c'est un ENUM, exécuter la commande ALTER TABLE ci-dessus
-- Si c'est un VARCHAR, aucune action n'est nécessaire

-- Vérifier les dossiers affectés au finance après migration
-- SELECT type_recouvrement, COUNT(*) as nombre
-- FROM dossier
-- GROUP BY type_recouvrement;

