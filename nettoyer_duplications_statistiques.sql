-- Script SQL pour nettoyer les duplications dans la table statistiques
-- Problème: Plusieurs lignes avec le même type et la même période
-- Solution: Supprimer les duplications en gardant seulement la plus récente

-- Option 1: Supprimer toutes les statistiques de la période actuelle (Recommandé)
-- Utilisez cette option si vous voulez repartir à zéro pour la période actuelle
-- Puis appelez l'API /api/statistiques/recalculer pour recréer les statistiques
DELETE FROM statistiques 
WHERE periode = DATE_FORMAT(NOW(), '%Y-%m');

-- Option 2: Supprimer les duplications en gardant seulement la plus récente
-- Utilisez cette option si vous voulez garder l'historique mais supprimer les doublons
-- Décommentez la ligne suivante et commentez l'option 1 si vous préférez cette approche
-- DELETE s1 FROM statistiques s1
-- INNER JOIN statistiques s2 
-- WHERE s1.type = s2.type 
--   AND s1.periode = s2.periode
--   AND s1.date_calcul < s2.date_calcul;

-- Option 3: Supprimer les duplications en gardant seulement la première (plus ancienne)
-- Utilisez cette option si vous voulez garder la première statistique calculée
-- DELETE s1 FROM statistiques s1
-- INNER JOIN statistiques s2 
-- WHERE s1.type = s2.type 
--   AND s1.periode = s2.periode
--   AND s1.id > s2.id;

-- Vérification: Compter les duplications restantes
-- SELECT 
--     type,
--     periode,
--     COUNT(*) as nb_duplications
-- FROM statistiques
-- WHERE periode = DATE_FORMAT(NOW(), '%Y-%m')
-- GROUP BY type, periode
-- HAVING COUNT(*) > 1;
-- Résultat attendu: Aucune ligne (pas de duplications)

-- Note: Après avoir nettoyé les duplications, appelez l'API:
-- POST http://localhost:8089/carthage-creance/api/statistiques/recalculer
-- Headers: Authorization: Bearer {token}
-- Cela recréera les statistiques sans duplications (si le code backend est corrigé)

