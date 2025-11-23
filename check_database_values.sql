-- Script pour vérifier les valeurs problématiques dans la base de données
-- Exécuter ce script pour identifier les valeurs invalides

-- 1. Vérifier les valeurs NULL dans dossier_status
SELECT 
    'NULL values' as issue_type,
    COUNT(*) as count
FROM dossier 
WHERE dossier_status IS NULL;

-- 2. Vérifier les valeurs vides dans dossier_status
SELECT 
    'Empty values' as issue_type,
    COUNT(*) as count
FROM dossier 
WHERE dossier_status = '' OR dossier_status = ' ';

-- 3. Vérifier toutes les valeurs distinctes dans dossier_status
SELECT 
    'All distinct values' as issue_type,
    dossier_status as value,
    COUNT(*) as count
FROM dossier 
GROUP BY dossier_status
ORDER BY dossier_status;

-- 4. Vérifier les valeurs avec des espaces ou caractères spéciaux
SELECT 
    'Problematic values' as issue_type,
    dossier_status as value,
    COUNT(*) as count
FROM dossier 
WHERE dossier_status IS NOT NULL 
  AND dossier_status NOT IN ('ENCOURSDETRAITEMENT', 'CLOTURE')
  AND dossier_status != ''
GROUP BY dossier_status;

-- 5. Vérifier la structure de la colonne
DESCRIBE dossier;


























