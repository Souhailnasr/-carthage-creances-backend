-- Script SQL pour corriger la colonne 'type' dans la table statistiques
-- Problème: La colonne est trop petite pour contenir toutes les valeurs de l'enum TypeStatistique
-- Solution: Modifier la colonne en VARCHAR(50) pour accepter toutes les valeurs

-- Étape 1: Vérifier la structure actuelle
-- DESCRIBE statistiques;
-- OU
-- SHOW COLUMNS FROM statistiques WHERE Field = 'type';

-- Étape 2: Modifier la colonne type en VARCHAR(50)
ALTER TABLE statistiques 
MODIFY COLUMN type VARCHAR(50) NOT NULL;

-- Étape 3: Vérifier la modification
-- DESCRIBE statistiques;

-- Note: Après cette modification, vous pouvez appeler l'API:
-- POST http://localhost:8089/carthage-creance/api/statistiques/recalculer
-- Headers: Authorization: Bearer {token}

-- Vérification: Voir les statistiques stockées
-- SELECT type, valeur, description, periode, date_calcul
-- FROM statistiques
-- WHERE periode = DATE_FORMAT(NOW(), '%Y-%m')
-- ORDER BY type, date_calcul DESC;

