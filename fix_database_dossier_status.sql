-- Script de nettoyage URGENT pour corriger l'erreur DossierStatus
-- Exécuter ce script pour corriger toutes les valeurs problématiques

-- 1. Vérifier les valeurs actuelles (DIAGNOSTIC)
SELECT 
    'DIAGNOSTIC - Valeurs actuelles' as info,
    dossier_status as valeur,
    COUNT(*) as nombre
FROM dossier 
GROUP BY dossier_status
ORDER BY dossier_status;

-- 2. Corriger les valeurs NULL
UPDATE dossier 
SET dossier_status = 'ENCOURSDETRAITEMENT' 
WHERE dossier_status IS NULL;

-- 3. Corriger les valeurs vides
UPDATE dossier 
SET dossier_status = 'ENCOURSDETRAITEMENT' 
WHERE dossier_status = '' OR dossier_status = ' ';

-- 4. Corriger les valeurs avec espaces
UPDATE dossier 
SET dossier_status = 'ENCOURSDETRAITEMENT' 
WHERE dossier_status LIKE '% %';

-- 5. Corriger les valeurs en minuscules
UPDATE dossier 
SET dossier_status = 'ENCOURSDETRAITEMENT' 
WHERE dossier_status = 'encourdetraitement' OR dossier_status = 'cloture';

-- 6. Corriger les valeurs avec des caractères spéciaux
UPDATE dossier 
SET dossier_status = 'ENCOURSDETRAITEMENT' 
WHERE dossier_status NOT IN ('ENCOURSDETRAITEMENT', 'CLOTURE') 
  AND dossier_status IS NOT NULL;

-- 7. Mettre à jour le schéma de la colonne pour accepter NULL
ALTER TABLE dossier MODIFY COLUMN dossier_status ENUM('ENCOURSDETRAITEMENT', 'CLOTURE') NULL DEFAULT 'ENCOURSDETRAITEMENT';

-- 8. Vérifier le résultat final
SELECT 
    'RÉSULTAT FINAL' as info,
    dossier_status as valeur,
    COUNT(*) as nombre
FROM dossier 
GROUP BY dossier_status
ORDER BY dossier_status;

-- 9. Vérifier qu'il n'y a plus de valeurs problématiques
SELECT 
    'VÉRIFICATION FINALE' as info,
    COUNT(*) as total_dossiers,
    COUNT(dossier_status) as dossiers_avec_status,
    COUNT(*) - COUNT(dossier_status) as dossiers_sans_status
FROM dossier;


























