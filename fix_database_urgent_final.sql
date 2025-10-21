-- SCRIPT URGENT FINAL - Correction définitive de l'erreur DossierStatus
-- Ce script DOIT être exécuté pour résoudre l'erreur "No enum constant"

-- 1. DIAGNOSTIC COMPLET AVANT CORRECTION
SELECT 'DIAGNOSTIC AVANT CORRECTION' as info;
SELECT 
    dossier_status as valeur_actuelle,
    COUNT(*) as nombre,
    CASE 
        WHEN dossier_status IS NULL THEN 'NULL'
        WHEN dossier_status = '' THEN 'VIDE'
        WHEN dossier_status = ' ' THEN 'ESPACE'
        WHEN dossier_status NOT IN ('ENCOURSDETRAITEMENT', 'CLOTURE') THEN 'INVALIDE'
        ELSE 'VALIDE'
    END as statut
FROM dossier 
GROUP BY dossier_status
ORDER BY dossier_status;

-- 2. CORRECTION URGENTE - Toutes les valeurs problématiques
UPDATE dossier 
SET dossier_status = 'ENCOURSDETRAITEMENT' 
WHERE dossier_status IS NULL 
   OR dossier_status = '' 
   OR dossier_status = ' ' 
   OR dossier_status NOT IN ('ENCOURSDETRAITEMENT', 'CLOTURE');

-- 3. Vérification que toutes les valeurs sont maintenant valides
SELECT 'VÉRIFICATION APRÈS CORRECTION' as info;
SELECT 
    dossier_status as valeur_finale,
    COUNT(*) as nombre
FROM dossier 
GROUP BY dossier_status
ORDER BY dossier_status;

-- 4. Mise à jour du schéma pour garantir la cohérence
ALTER TABLE dossier MODIFY COLUMN dossier_status ENUM('ENCOURSDETRAITEMENT', 'CLOTURE') NOT NULL DEFAULT 'ENCOURSDETRAITEMENT';

-- 5. Vérification finale
SELECT 'RÉSULTAT FINAL' as info;
SELECT 
    COUNT(*) as total_dossiers,
    COUNT(dossier_status) as dossiers_avec_status,
    SUM(CASE WHEN dossier_status = 'ENCOURSDETRAITEMENT' THEN 1 ELSE 0 END) as encours,
    SUM(CASE WHEN dossier_status = 'CLOTURE' THEN 1 ELSE 0 END) as clotures
FROM dossier;

-- 6. Test de cohérence
SELECT 'TEST DE COHÉRENCE' as info;
SELECT 
    id,
    titre,
    dossier_status,
    CASE 
        WHEN dossier_status IN ('ENCOURSDETRAITEMENT', 'CLOTURE') THEN 'OK'
        ELSE 'ERREUR'
    END as statut_validation
FROM dossier 
LIMIT 10;