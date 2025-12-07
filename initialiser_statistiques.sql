-- Script SQL pour initialiser/recalculer les statistiques dans la table statistiques
-- Ce script supprime les anciennes statistiques et les recalcule à partir des données actuelles

-- Supprimer toutes les statistiques existantes pour la période actuelle
DELETE FROM statistiques WHERE periode = DATE_FORMAT(NOW(), '%Y-%m');

-- Note: Les statistiques seront recalculées automatiquement par le backend
-- via l'endpoint POST /api/statistiques/recalculer ou automatiquement après chaque action

-- Pour forcer le recalcul immédiat, utiliser l'endpoint API:
-- POST http://localhost:8089/carthage-creance/api/statistiques/recalculer
-- Headers: Authorization: Bearer {token}

-- Vérification: Voir les statistiques calculées
SELECT 
    type,
    valeur,
    description,
    periode,
    date_calcul
FROM statistiques
WHERE periode = DATE_FORMAT(NOW(), '%Y-%m')
ORDER BY type, date_calcul DESC;

