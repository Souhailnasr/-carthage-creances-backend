-- ============================================
-- Script de Nettoyage des Doublons de Tarifs
-- ============================================
-- Date: 2025-01-05
-- Description: Supprime les doublons de tarifs pour les actions amiables
--              en gardant uniquement le tarif le plus récent
-- ============================================

-- ⚠️ ATTENTION : Sauvegardez votre base de données avant d'exécuter ce script !

-- ============================================
-- ÉTAPE 1 : Identifier les Doublons
-- ============================================

-- Afficher tous les doublons (tarifs avec même dossier_id, action_id, phase)
SELECT 
    dossier_id,
    action_id,
    phase,
    COUNT(*) AS nombre_doublons,
    GROUP_CONCAT(id ORDER BY date_creation DESC) AS tarif_ids,
    GROUP_CONCAT(statut ORDER BY date_creation DESC) AS statuts,
    GROUP_CONCAT(cout_unitaire ORDER BY date_creation DESC) AS couts_unitaires
FROM tarif_dossier
WHERE action_id IS NOT NULL
  AND phase = 'AMIABLE'
GROUP BY dossier_id, action_id, phase
HAVING COUNT(*) > 1
ORDER BY dossier_id, action_id;

-- ============================================
-- ÉTAPE 2 : Vérifier les Doublons Avant Suppression
-- ============================================

-- Afficher les détails complets des doublons pour dossier_id = 11
SELECT 
    td.id AS tarif_id,
    td.dossier_id,
    td.action_id,
    a.type AS action_type,
    td.phase,
    td.categorie,
    td.cout_unitaire,
    td.quantite,
    td.montant_total,
    td.statut,
    td.date_creation,
    td.date_validation
FROM tarif_dossier td
LEFT JOIN action a ON td.action_id = a.id
WHERE td.dossier_id = 11
  AND td.action_id IS NOT NULL
  AND td.phase = 'AMIABLE'
ORDER BY td.action_id, td.date_creation DESC;

-- ============================================
-- ÉTAPE 3 : Supprimer les Doublons (Garde le Plus Récent)
-- ============================================

-- Option 1 : Supprimer les doublons en gardant le tarif avec l'ID le plus grand (le plus récent)
DELETE td1 FROM tarif_dossier td1
INNER JOIN tarif_dossier td2 
WHERE td1.dossier_id = td2.dossier_id
  AND td1.action_id = td2.action_id
  AND td1.phase = td2.phase
  AND td1.phase = 'AMIABLE'
  AND td1.action_id IS NOT NULL
  AND td1.id < td2.id;  -- Garder le plus récent (id le plus grand)

-- ============================================
-- ÉTAPE 4 : Vérification Après Suppression
-- ============================================

-- Vérifier qu'il n'y a plus de doublons
SELECT 
    dossier_id,
    action_id,
    phase,
    COUNT(*) AS nombre_tarifs
FROM tarif_dossier
WHERE action_id IS NOT NULL
  AND phase = 'AMIABLE'
GROUP BY dossier_id, action_id, phase
HAVING COUNT(*) > 1;

-- Si cette requête ne retourne aucun résultat, tous les doublons ont été supprimés ✅

-- ============================================
-- ÉTAPE 5 : Statistiques Finales
-- ============================================

-- Afficher le nombre de tarifs par dossier après nettoyage
SELECT 
    dossier_id,
    COUNT(*) AS nombre_tarifs_amiable,
    SUM(CASE WHEN statut = 'VALIDE' THEN 1 ELSE 0 END) AS tarifs_valides,
    SUM(CASE WHEN statut = 'EN_ATTENTE_VALIDATION' THEN 1 ELSE 0 END) AS tarifs_en_attente,
    SUM(montant_total) AS montant_total
FROM tarif_dossier
WHERE action_id IS NOT NULL
  AND phase = 'AMIABLE'
GROUP BY dossier_id
ORDER BY dossier_id;

-- ============================================
-- ALTERNATIVE : Suppression Manuelle (Plus Sûre)
-- ============================================

-- Si vous préférez supprimer manuellement via phpMyAdmin :

-- 1. Identifier les doublons avec la requête de l'ÉTAPE 1
-- 2. Pour chaque groupe de doublons :
--    - Garder le tarif avec l'ID le plus grand (le plus récent)
--    - Supprimer les autres tarifs manuellement
-- 3. Vérifier avec la requête de l'ÉTAPE 4

-- ============================================
-- EXEMPLE : Suppression Ciblée pour Dossier #11
-- ============================================

-- Supprimer les doublons spécifiquement pour le dossier #11
-- (Garder uniquement le tarif le plus récent pour chaque action)

DELETE td1 FROM tarif_dossier td1
INNER JOIN tarif_dossier td2 
WHERE td1.dossier_id = 11
  AND td1.dossier_id = td2.dossier_id
  AND td1.action_id = td2.action_id
  AND td1.phase = td2.phase
  AND td1.phase = 'AMIABLE'
  AND td1.action_id IS NOT NULL
  AND td1.id < td2.id;

-- ============================================
-- NOTES IMPORTANTES
-- ============================================

-- 1. Ce script garde le tarif avec l'ID le plus grand (supposé être le plus récent)
-- 2. Si vous préférez garder le tarif avec la date_creation la plus récente, utilisez :
--    DELETE td1 FROM tarif_dossier td1
--    INNER JOIN tarif_dossier td2 
--    WHERE td1.dossier_id = td2.dossier_id
--      AND td1.action_id = td2.action_id
--      AND td1.phase = td2.phase
--      AND td1.phase = 'AMIABLE'
--      AND td1.date_creation < td2.date_creation;

-- 3. Si vous préférez garder le tarif VALIDE plutôt que EN_ATTENTE_VALIDATION :
--    DELETE td1 FROM tarif_dossier td1
--    INNER JOIN tarif_dossier td2 
--    WHERE td1.dossier_id = td2.dossier_id
--      AND td1.action_id = td2.action_id
--      AND td1.phase = td2.phase
--      AND td1.phase = 'AMIABLE'
--      AND (td1.statut = 'EN_ATTENTE_VALIDATION' AND td2.statut = 'VALIDE');

-- ============================================
-- FIN DU SCRIPT
-- ============================================
